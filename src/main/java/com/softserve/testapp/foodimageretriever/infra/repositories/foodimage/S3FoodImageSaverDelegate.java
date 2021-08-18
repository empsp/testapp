package com.softserve.testapp.foodimageretriever.infra.repositories.foodimage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softserve.testapp.foodimageretriever.domain.Image;
import com.softserve.testapp.foodimageretriever.infra.repositories.foodimage.S3FoodImageSaverDelegate.UploadResult.UploadResultBuilder;
import com.softserve.testapp.shared.utils.ByteBufferUtil;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Singular;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
class S3FoodImageSaverDelegate {
    private final S3AsyncClient client;
    private final S3ConnectionProperties s3ConnectionProperties;
    private final ObjectMapper objectMapper;

    Mono<Image> saveImage(String imageId, Image image) {
        return saveImageMetadata(imageId, image).then(saveImageBody(imageId, image)).thenReturn(image);
    }

    private Mono<PutObjectResponse> saveImageMetadata(String imageId, Image image) {
        var imageMetadata = new S3ImageMetadata(image.getOriginalUrl());
        var serializedPayload = serializePayload(imageMetadata);
        var request = PutObjectRequest.builder()
                .bucket(s3ConnectionProperties.getBucket())
                .key(imageId + "/meta")
                .contentLength((long) serializedPayload.length)
                .build();

        return Mono.fromFuture(client.putObject(request, AsyncRequestBody.fromBytes(serializedPayload)));
    }

    @SneakyThrows
    private byte[] serializePayload(Object payload) {
        return objectMapper.writeValueAsBytes(payload);
    }

    private Mono<Void> saveImageBody(String imageId, Image image) {
        var request = CreateMultipartUploadRequest.builder()
                .bucket(s3ConnectionProperties.getBucket())
                .key(imageId + "/body")
                .build();

        return Mono.fromFuture(client.createMultipartUpload(request))
                .flatMap(uploadParts(image))
                .flatMap(this::completeUpload)
                .then();
    }

    private Function<CreateMultipartUploadResponse, Mono<UploadResult>> uploadParts(Image image) {
        var bufferSize = new AtomicInteger();

        return createResponse -> image.getBase64body()
                .bufferUntil(buffer -> {
                    if (bufferSize.addAndGet(buffer.remaining()) >= s3ConnectionProperties.getMultipartMinPartSize()) {
                        bufferSize.set(0);
                        return true;
                    } else {
                        return false;
                    }
                })
                .map(ByteBufferUtil::concatBuffers)
                .concatMap(uploadPart(createResponse))
                .reduce(UploadResult.builder(), UploadResultBuilder::completedPart).map(builder -> builder.createResponse(createResponse).build());
    }

    private Function<ByteBuffer, Mono<CompletedPart>> uploadPart(CreateMultipartUploadResponse createResponse) {
        var partCounter = new AtomicInteger();

        return imageBody -> {
            var partNumber = partCounter.incrementAndGet();

            var request = UploadPartRequest.builder()
                    .bucket(createResponse.bucket())
                    .key(createResponse.key())
                    .partNumber(partNumber)
                    .uploadId(createResponse.uploadId())
                    .contentLength((long) imageBody.remaining())
                    .build();

            return Mono.fromFuture(client.uploadPart(request, AsyncRequestBody.fromPublisher(Mono.just(imageBody))))
                    .map(response -> CompletedPart.builder()
                            .eTag(response.eTag())
                            .partNumber(partNumber)
                            .build());
        };
    }

    private Mono<CompleteMultipartUploadResponse> completeUpload(UploadResult uploadResult) {
        var createResponse = uploadResult.createResponse;
        var multipartUpload = CompletedMultipartUpload.builder()
                .parts(uploadResult.completedParts)
                .build();
        var request = CompleteMultipartUploadRequest.builder()
                .bucket(createResponse.bucket())
                .uploadId(createResponse.uploadId())
                .multipartUpload(multipartUpload)
                .key(createResponse.key())
                .build();

        return Mono.fromFuture(client.completeMultipartUpload(request));
    }

    @Builder
    static class UploadResult {
        @Singular
        List<CompletedPart> completedParts;
        CreateMultipartUploadResponse createResponse;
    }
}
