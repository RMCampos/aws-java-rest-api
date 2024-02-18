package br.com.campos.ricardo.awsjavarestapi.aws;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SqsException;

@Slf4j
@Service
public class AwsSqsService {

  @Autowired private AwsConfig awsConfig;

  private SqsClient getClient() {
    log.info("AWS Region: {}", awsConfig.getAwsRegion());
    Region region = Region.of(awsConfig.getAwsRegion());

    return SqsClient.builder().region(region).build();
  }

  public void addMessageToQueue(String message) {
    log.info("Sending task to SQS!");

    SqsClient client = getClient();

    sendMessage(client, message);

    client.close();
  }

  private void sendMessage(SqsClient sqsClient, String message) {
    String queueName = awsConfig.getAwsSqsQueueName();
    log.info("AWS SQS queue name: {}", queueName);

    try {
      CreateQueueRequest request = CreateQueueRequest.builder().queueName(queueName).build();

      sqsClient.createQueue(request);

      GetQueueUrlRequest getQueueUrlRequest =
          GetQueueUrlRequest.builder().queueName(queueName).build();

      String queueUrl = sqsClient.getQueueUrl(getQueueUrlRequest).queueUrl();

      SendMessageRequest messageRequest =
          SendMessageRequest.builder()
              .queueUrl(queueUrl)
              .messageBody(message)
              .delaySeconds(10)
              .build();

      sqsClient.sendMessage(messageRequest);

      log.info("Message successfully added in the queue!");
    } catch (SqsException se) {
      log.error("SqsException: {}", se.awsErrorDetails().errorMessage());
      se.printStackTrace();
    }
  }
}
