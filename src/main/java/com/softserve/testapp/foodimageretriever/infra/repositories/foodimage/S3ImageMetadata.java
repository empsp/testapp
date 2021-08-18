package com.softserve.testapp.foodimageretriever.infra.repositories.foodimage;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
class S3ImageMetadata {
    String originalUrl;
}
