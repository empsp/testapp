package com.softserve.testapp.foodimageretriever.domain;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class FoodImageRetrieverFacadeConfiguration {
    @Bean
    FoodImageRetrieverFacade foodImageRetrieverFacade(
            FoodImageRepository foodImageRepository,
            FoodImageRetrieverPort foodImageRetrieverPort) {

        return new FoodImageRetrieverFacade(foodImageRepository, foodImageRetrieverPort);
    }
}
