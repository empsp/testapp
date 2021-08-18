package com.softserve.testapp.foodimageretriever.domain;

import reactor.core.publisher.Mono;

public interface FoodImageRetrieverPort {
    Mono<Image> getRandomBurgerImage();
}
