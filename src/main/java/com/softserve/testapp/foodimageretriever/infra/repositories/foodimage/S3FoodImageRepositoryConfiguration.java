package com.softserve.testapp.foodimageretriever.infra.repositories.foodimage;

import static software.amazon.awssdk.utils.StringUtils.isBlank;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softserve.testapp.foodimageretriever.domain.FoodImageRepository;
import com.softserve.testapp.foodimageretriever.domain.Image;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.time.Duration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(S3ConnectionProperties.class)
class S3FoodImageRepositoryConfiguration {
    @Bean
    FoodImageRepository foodImageRepository(S3ConnectionProperties s3ConnectionProps, ObjectMapper objectMapper) {
        var awsCredentialsProvider = awsCredentialsProvider(s3ConnectionProps);
        var client = s3AsyncClient(s3ConnectionProps, awsCredentialsProvider);
        var retrieverDelegate = new S3FoodImageRetrieverDelegate(client, s3ConnectionProps, objectMapper);
        var saverDelegate = new S3FoodImageSaverDelegate(client, s3ConnectionProps, objectMapper);

        return new FoodImageRepository() {
            @Override
            public Mono<Image> getImage(String imageId) {
                return retrieverDelegate.getImage(imageId);
            }

            @Override
            public Mono<Image> saveImage(String imageId, Image image) {
                return saverDelegate.saveImage(imageId, image);
            }
        };
    }

    private S3AsyncClient s3AsyncClient(S3ConnectionProperties s3ConnectionProps, AwsCredentialsProvider credentialsProvider) {
        SdkAsyncHttpClient httpClient = NettyNioAsyncHttpClient.builder()
                .writeTimeout(Duration.ZERO)
                .build();

        S3Configuration serviceConfiguration = S3Configuration.builder()
                .checksumValidationEnabled(false)
                .chunkedEncodingEnabled(true)
                .build();

        return S3AsyncClient.builder()
                .httpClient(httpClient)
                .region(s3ConnectionProps.getRegion())
                .credentialsProvider(credentialsProvider)
                .serviceConfiguration(serviceConfiguration)
                .build();
    }

    private AwsCredentialsProvider awsCredentialsProvider(S3ConnectionProperties s3ConnectionProps) {
        if (isBlank(s3ConnectionProps.getAccessKeyId())) {
            return DefaultCredentialsProvider.create();
        } else {
            return () -> AwsBasicCredentials.create(s3ConnectionProps.getAccessKeyId(), s3ConnectionProps.getSecretAccessKey());
        }
    }
}
