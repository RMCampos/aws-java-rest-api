package br.com.campos.ricardo.awsjavarestapi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter(value = AccessLevel.PRIVATE)
@Component
public class AwsConfig {
  
  @Value("${aws.config.region}")
  private String awsRegion;

  @Value("${aws.config.s3.bucketName}")
  private String awsS3BucketName;

}
