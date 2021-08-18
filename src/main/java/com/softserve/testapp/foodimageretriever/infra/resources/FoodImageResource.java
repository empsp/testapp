package com.softserve.testapp.foodimageretriever.infra.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softserve.testapp.foodimageretriever.domain.FoodImageRetrieverFacade;
import com.softserve.testapp.foodimageretriever.domain.ImageRequest;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
class FoodImageResource {
    private final FoodImageRetrieverFacade facade;
    private final ObjectMapper objectMapper;

    @GetMapping(value = "/burgers/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<ByteBuffer> getBurgers(@PathVariable("id") String imageId) {
        return facade.retrieve(new ImageRequest(imageId)).flatMapMany(image -> ImageDto.toJson(objectMapper, image));
    }
}
