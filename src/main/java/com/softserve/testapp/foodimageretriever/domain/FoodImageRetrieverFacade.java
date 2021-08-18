package com.softserve.testapp.foodimageretriever.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class FoodImageRetrieverFacade {
    private final FoodImageRepository foodImageRepository;
    private final FoodImageRetrieverPort foodImageRetrieverPort;

    public Mono<Image> retrieve(ImageRequest imageRequest) {
        return foodImageRepository.getImage(imageRequest.getImageId())
                .switchIfEmpty(Mono.defer(() -> retrieveAndCache(imageRequest.getImageId())));
    }

    private Mono<Image> retrieveAndCache(String imageId) {
        return foodImageRetrieverPort.getRandomBurgerImage()
                .flatMap(image -> foodImageRepository.saveImage(imageId, image));
    }
}
