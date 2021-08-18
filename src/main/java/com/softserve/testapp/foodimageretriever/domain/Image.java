package com.softserve.testapp.foodimageretriever.domain;

import lombok.Value;
import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;

@Value
public class Image {
    String originalUrl;
    Flux<ByteBuffer> base64body;
}
