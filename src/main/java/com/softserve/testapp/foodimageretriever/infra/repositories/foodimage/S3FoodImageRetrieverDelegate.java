package com.softserve.testapp.foodimageretriever.infra.repositories.foodimage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softserve.testapp.foodimageretriever.domain.Image;
import com.softserve.testapp.foodimageretriever.infra.repositories.foodimage.S3FoodImageRetrieverDelegate.FluxResponseProvider.FluxResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.core.async.SdkPublisher;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
class S3FoodImageRetrieverDelegate {
    private final S3AsyncClient client;
    private final S3ConnectionProperties s3ConnectionProperties;
    private final ObjectMapper objectMapper;

    Mono<Image> getImage(String imageId) {
        return Mono.zip(getImageMetadata(imageId), getImageBody(imageId), (meta, body) -> new Image(meta.getOriginalUrl(), body));
    }

    private Mono<S3ImageMetadata> getImageMetadata(String imageId) {
        return Mono.fromFuture(client.getObject(getObjectRequest(imageId + "/meta"), AsyncResponseTransformer
                .toBytes()))
                .onErrorResume(NoSuchKeyException.class, err -> Mono.empty())
                .map(ResponseBytes::asUtf8String)
                .map(response -> deserialize(response, S3ImageMetadata.class));
    }

    private Mono<Flux<ByteBuffer>> getImageBody(String imageId) {
        return Mono.fromFuture(client.getObject(getObjectRequest(imageId + "/body"), new FluxResponseProvider()))
                .onErrorResume(NoSuchKeyException.class, err -> Mono.empty())
                .map(FluxResponse::getBody);
    }

    private GetObjectRequest getObjectRequest(String key) {
        return GetObjectRequest.builder()
                .bucket(s3ConnectionProperties.getBucket())
                .key(key)
                .build();
    }

    @SneakyThrows
    private <T> T deserialize(String response, Class<T> clazz) {
        return objectMapper.readValue(response, clazz);
    }

    static class FluxResponseProvider implements AsyncResponseTransformer<GetObjectResponse, FluxResponse> {
        private FluxResponse.FluxResponseBuilder responseBuilder;

        @Override
        public CompletableFuture<FluxResponse> prepare() {
            responseBuilder = FluxResponse.builder().cf(new CompletableFuture<>());
            return responseBuilder.cf;
        }

        @Override
        public void onResponse(GetObjectResponse response) {
            responseBuilder = responseBuilder.response(response);
        }

        @Override
        public void onStream(SdkPublisher<ByteBuffer> publisher) {
            responseBuilder = responseBuilder.body(Flux.from(publisher));
            responseBuilder.cf.complete(responseBuilder.build());
        }

        @Override
        public void exceptionOccurred(Throwable error) {
            responseBuilder.cf.completeExceptionally(error);
        }

        @Builder
        static class FluxResponse {
            private CompletableFuture<FluxResponse> cf;
            GetObjectResponse response;
            @Getter
            Flux<ByteBuffer> body;
        }
    }
}
