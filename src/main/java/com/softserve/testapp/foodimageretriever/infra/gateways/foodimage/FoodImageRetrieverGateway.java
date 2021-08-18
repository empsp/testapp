package com.softserve.testapp.foodimageretriever.infra.gateways.foodimage;

import com.softserve.testapp.foodimageretriever.domain.FoodImageRetrieverPort;
import com.softserve.testapp.foodimageretriever.domain.Image;
import com.softserve.testapp.shared.infra.gatewaysendpoints.GatewayProperties;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.Base64;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@RequiredArgsConstructor
class FoodImageRetrieverGateway implements FoodImageRetrieverPort {
    private final WebClient client;
    private final GatewayProperties endpoints;

    @Override
    public Mono<Image> getRandomBurgerImage() {
        return getImageMetadata(endpoints.getEndpointUri("burgers-uri"))
                .flatMap(this::getImage);
    }

    private Mono<ImageMetadataResponse> getImageMetadata(String uri) {
        return client.get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(ImageMetadataResponse.class);
    }

    private Mono<Image> getImage(ImageMetadataResponse imageMetadata) {
        return client.get()
                .uri(imageMetadata.getImageUri())
                .retrieve()
                .bodyToFlux(ByteBuffer.class)
                .transform(Base64ByteBuffersLengthNormalizer::normalizeBuffersForBase64Encoding)
                .map(Base64.getEncoder()::encode)
                .transform(base64EncodedBody -> Flux.just(new Image(imageMetadata.getImageUri(), base64EncodedBody)))
                .next();
    }
}
