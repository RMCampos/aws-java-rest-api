package br.com.campos.ricardo.awsjavarestapi.aws;

import br.com.campos.ricardo.awsjavarestapi.dto.AwsMessageDto;
import br.com.campos.ricardo.awsjavarestapi.service.EmailService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SqsException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AwsSqsService {

  private final AwsConfig awsConfig;

  private final EmailService emailService;

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

  public List<AwsMessageDto> getAllMessagesInQueue() {
    log.info("Getting all messages in SQS Queue!");

    SqsClient client = getClient();

    List<AwsMessageDto> list = getAllMessages(client);

    client.close();

    return list;
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

  private List<AwsMessageDto> getAllMessages(SqsClient sqsClient) {
    String queueName = awsConfig.getAwsSqsQueueName();
    log.info("AWS SQS queue name: {}", queueName);

    try {
      CreateQueueRequest request = CreateQueueRequest.builder().queueName(queueName).build();

      sqsClient.createQueue(request);

      GetQueueUrlRequest getQueueUrlRequest =
          GetQueueUrlRequest.builder().queueName(queueName).build();

      String queueUrl = sqsClient.getQueueUrl(getQueueUrlRequest).queueUrl();

      ReceiveMessageRequest receiveMessageRequest =
          ReceiveMessageRequest.builder().queueUrl(queueUrl).maxNumberOfMessages(10).build();

      ReceiveMessageResponse response = sqsClient.receiveMessage(receiveMessageRequest);

      List<Message> messages = response.messages();
      log.info("{} message(s) successfully pulled from the queue!", messages.size());
      List<AwsMessageDto> messageDtos = new ArrayList<>();
      messages.forEach(
          message -> {
            messageDtos.add(
                new AwsMessageDto(message.messageId(), message.body(), message.receiptHandle()));
          });

      return messageDtos;
    } catch (SqsException se) {
      log.error("SqsException: {}", se.awsErrorDetails().errorMessage());
      se.printStackTrace();
    }

    return List.of();
  }

  public void processMessage(String messageId) {
    log.info("Getting all messages in SQS Queue!");

    SqsClient sqsClient = getClient();

    List<AwsMessageDto> list = getAllMessages(sqsClient);

    Optional<AwsMessageDto> meOptional =
        list.stream().filter(m -> m.messageId().equals(messageId)).findFirst();

    if (meOptional.isEmpty()) {
      log.error("Message not found in Queue! id: {}", messageId);
      sqsClient.close();
      return;
    }

    if (emailService.sendEmail(meOptional.get().messageBody())) {
      removeMessageFromQueue(sqsClient, meOptional.get().receiptHandler());
      sqsClient.close();
    } else {
      log.error("Unable to send email! Message kept in the queue!");
      sqsClient.close();
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error sending email!");
    }
  }

  private void removeMessageFromQueue(SqsClient sqsClient, String receiptHandler) {
    String queueName = awsConfig.getAwsSqsQueueName();
    log.info("AWS SQS queue name: {}", queueName);

    try {
      CreateQueueRequest request = CreateQueueRequest.builder().queueName(queueName).build();

      sqsClient.createQueue(request);

      GetQueueUrlRequest getQueueUrlRequest =
          GetQueueUrlRequest.builder().queueName(queueName).build();

      String queueUrl = sqsClient.getQueueUrl(getQueueUrlRequest).queueUrl();

      DeleteMessageRequest deleteMessageRequest =
          DeleteMessageRequest.builder().queueUrl(queueUrl).receiptHandle(receiptHandler).build();

      sqsClient.deleteMessage(deleteMessageRequest);
      log.info("Message sucessfully deleted from Queue!");

    } catch (SqsException se) {
      log.error("SqsException: {}", se.awsErrorDetails().errorMessage());
      se.printStackTrace();
    }
  }
}
