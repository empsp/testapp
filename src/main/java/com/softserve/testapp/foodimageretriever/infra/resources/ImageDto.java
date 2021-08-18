package com.softserve.testapp.foodimageretriever.infra.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softserve.testapp.foodimageretriever.domain.Image;
import lombok.SneakyThrows;
import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;

public interface ImageDto {
    @SneakyThrows
    static Flux<ByteBuffer> toJson(ObjectMapper objectMapper, Image image) {
        var response = objectMapper.createObjectNode().put("originalUrl", image.getOriginalUrl()).put("image", "");
        var jsonRaw = objectMapper.writeValueAsString(response);
        var jsonPrefix = jsonRaw.substring(0, jsonRaw.length() - 2);
        var jsonSuffix = "\"}";

        return image.getBase64body()
                .startWith(ByteBuffer.wrap(jsonPrefix.getBytes()))
                .concatWithValues(ByteBuffer.wrap(jsonSuffix.getBytes()));
    }
}
