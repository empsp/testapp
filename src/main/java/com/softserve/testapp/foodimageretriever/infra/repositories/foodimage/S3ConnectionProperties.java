package com.softserve.testapp.foodimageretriever.infra.repositories.foodimage;

import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.regions.Region;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("aws.s3")
@Setter
@Getter
class S3ConnectionProperties {
    private Region region;
    private String bucket;

    private String accessKeyId;
    private String secretAccessKey;
    private int multipartMinPartSize = 5 * 1024 * 1024; // S3 multipart upload requires parts to be 5MB minimum (except last part)
}
