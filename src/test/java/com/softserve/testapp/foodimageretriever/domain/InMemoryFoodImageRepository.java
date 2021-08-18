package com.softserve.testapp.foodimageretriever.domain;

import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class InMemoryFoodImageRepository implements FoodImageRepository {
    private final Map<String, Image> imageById = new ConcurrentHashMap<>();

    @Override
    public Mono<Image> getImage(String imageId) {
        return Mono.justOrEmpty(imageById.get(imageId));
    }

    @Override
    public Mono<Image> saveImage(String imageId, Image image) {
        return Mono.justOrEmpty(imageById.put(imageId, image)).thenReturn(image);
    }
}
