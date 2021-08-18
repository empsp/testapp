package com.softserve.testapp.foodimageretriever.utils;

import com.softserve.testapp.foodimageretriever.domain.Image;
import lombok.SneakyThrows;
import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;
import java.util.UUID;

public interface SampleImages {
    String SAMPLE_IMAGE_ID = UUID.randomUUID().toString();
    String SAMPLE_IMAGE_URL = "https://foodish-api.herokuapp.com/images/burger/burger44.jpg";

    Image SAMPLE_IMAGE = new Image(SAMPLE_IMAGE_URL, burgerImage());

    @SneakyThrows
    private static Flux<ByteBuffer> burgerImage() {
        try (var image = SampleImages.class.getResourceAsStream("burger.jpg")) {
            return Flux.just(ByteBuffer.wrap(image.readNBytes(9491)), ByteBuffer.wrap(image.readNBytes(1235)), ByteBuffer.wrap(image.readAllBytes()));
        }
    }
}
