package br.com.campos.ricardo.awsjavarestapi.aws;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Setter(value = AccessLevel.PRIVATE)
@Component
public class AwsConfig {

  @Value("${aws.config.region}")
  private String awsRegion;

  @Value("${aws.config.s3.bucketName}")
  private String awsS3BucketName;

  @Value("${aws.config.sqs.queueName}")
  private String awsSqsQueueName;

}
