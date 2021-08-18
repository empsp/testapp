package com.softserve.testapp.foodimageretriever.domain;

import static com.softserve.testapp.foodimageretriever.utils.SampleImages.SAMPLE_IMAGE;
import static com.softserve.testapp.foodimageretriever.utils.SampleImages.SAMPLE_IMAGE_ID;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FoodImageRetrieverTest {
    @Mock
    private FoodImageRetrieverPort foodImageRetrieverPort;

    private FoodImageRetrieverFacade facade;

    @BeforeEach
    void setup() {
        facade = new FoodImageRetrieverFacadeConfiguration().foodImageRetrieverFacade(new InMemoryFoodImageRepository(), foodImageRetrieverPort);
    }

    @Test
    void shouldRetrieveImageFromExternalSourceIfCacheEmpty() {
        given(foodImageRetrieverPort.getRandomBurgerImage()).willReturn(Mono.just(SAMPLE_IMAGE));

        StepVerifier.create(facade.retrieve(sampleImageRequest()))
                .expectNext(SAMPLE_IMAGE)
                .verifyComplete();
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldRetrieveImageFromCacheOnceCachePopulated() {
        given(foodImageRetrieverPort.getRandomBurgerImage()).willReturn(Mono.just(SAMPLE_IMAGE), Mono.error(new RuntimeException("too many attempts")));

        var retrievalFromCacheScenario = facade.retrieve(sampleImageRequest()).then(Mono.defer(() -> facade.retrieve(sampleImageRequest())));
        StepVerifier.create(retrievalFromCacheScenario)
                .expectNext(SAMPLE_IMAGE)
                .verifyComplete();

        then(foodImageRetrieverPort).should(times(1)).getRandomBurgerImage();
    }

    private ImageRequest sampleImageRequest() {
        return new ImageRequest(SAMPLE_IMAGE_ID);
    }
}
