package com.softserve.testapp.foodimageretriever.domain;

import reactor.core.publisher.Mono;

public interface FoodImageRepository {
    Mono<Image> getImage(String imageId);

    Mono<Image> saveImage(String imageId, Image image);
}
