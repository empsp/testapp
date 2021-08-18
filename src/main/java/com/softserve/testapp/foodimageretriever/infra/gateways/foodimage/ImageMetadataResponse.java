package com.softserve.testapp.foodimageretriever.infra.gateways.foodimage;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
class ImageMetadataResponse {
    @JsonAlias("image")
    String imageUri;
}
