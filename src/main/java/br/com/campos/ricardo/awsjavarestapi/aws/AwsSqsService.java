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
import software.amazon.awssdk.services.sqs.model.DeleteMessageBatchRequest;
import software.amazon.awssdk.services.sqs.model.DeleteMessageBatchRequestEntry;
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

  //
  // Commom methods
  //
  private SqsClient getSqsClient(String method) {
    log.info("{} - AWS Region: {}", method, awsConfig.getAwsRegion());
    Region region = Region.of(awsConfig.getAwsRegion());

    return SqsClient.builder().region(region).build();
  }

  private List<AwsMessageDto> getAllMessagesFromSqsQueue(SqsClient sqsClient, String method) {
    String queueName = awsConfig.getAwsSqsQueueName();
    log.info("{} - AWS SQS Queue name: {}", method, queueName);

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
      log.info("{} - {} message(s) successfully pulled from SQS queue!", method, messages.size());
      List<AwsMessageDto> messageDtos = new ArrayList<>();
      messages.forEach(
          message -> {
            log.info("{} - Message id {}", method, message.messageId());
            messageDtos.add(
                new AwsMessageDto(message.messageId(), message.body(), message.receiptHandle()));
          });

      return messageDtos;
    } catch (SqsException se) {
      log.error("{} - SqsException: {}", method, se.awsErrorDetails().errorMessage());
      se.printStackTrace();
    }

    return List.of();
  }

  //
  // DoTask methods
  //
  public void addMessageToQueue(String message, String method) {
    log.info("{} - Sending task to SQS!", method);

    SqsClient client = getSqsClient(method);

    sendMessage(client, message, method);

    client.close();
  }

  //
  // GetMessages methods
  //
  public List<AwsMessageDto> getAllMessagesInQueue() {
    log.info("GetMessages - Getting all messages in SQS Queue!");

    SqsClient sqsClient = getSqsClient("GetMessages");

    List<AwsMessageDto> list = getAllMessagesFromSqsQueue(sqsClient, "GetMessages");
    removeMessageFromQueue(sqsClient, list, "GetMessages");

    sqsClient.close();

    log.info("GetMessages - Finished!");

    return list;
  }

  private void sendMessage(SqsClient sqsClient, String message, String method) {
    String queueName = awsConfig.getAwsSqsQueueName();
    log.info("{} - AWS SQS queue name: {}", method, queueName);

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

      log.info("{} - Message successfully added in the queue!", method);
    } catch (SqsException se) {
      log.error("{} - SqsException: {}", method, se.awsErrorDetails().errorMessage());
      se.printStackTrace();
    }
  }

  //
  // ProcessFirstMessage methods
  //
  public void processFirstMessage() {
    log.info("ProcessFirstMessage - Getting all messages from SQS Queue to filter from!");

    SqsClient sqsClient = getSqsClient("ProcessFirstMessage");

    List<AwsMessageDto> list = getAllMessagesFromSqsQueue(sqsClient, "ProcessFirstMessage");
    log.info("ProcessFirstMessage - {} Message(s) found in SQS Queue!", list.size());

    Optional<AwsMessageDto> meOptional =
        list.stream().filter(m -> !m.receiptHandler().isBlank()).findFirst();

    if (meOptional.isEmpty()) {
      log.error("ProcessFirstMessage - No messages found in Queue!");
      sqsClient.close();
      return;
    }

    removeMessageFromQueue(sqsClient, List.of(meOptional.get()), "ProcessFirstMessage");
    log.info("ProcessFirstMessage - Message removed from SQS Queue!");

    sqsClient.close();

    if (emailService.sendEmail(meOptional.get().messageBody())) {
      log.info("ProcessFirstMessage - Email sent!");
      log.info("ProcessFirstMessage - Finished!");
    } else {
      log.error("ProcessFirstMessage - Unable to send email!");
      log.info("ProcessFirstMessage - Finished!");
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error sending email!");
    }
  }

  private void removeMessageFromQueue(SqsClient sqsClient, List<AwsMessageDto> messageDtos, String method) {
    String queueName = awsConfig.getAwsSqsQueueName();
    log.info("{} - AWS SQS queue name: {}", method, queueName);

    try {
      CreateQueueRequest request = CreateQueueRequest.builder().queueName(queueName).build();

      sqsClient.createQueue(request);

      GetQueueUrlRequest getQueueUrlRequest =
          GetQueueUrlRequest.builder().queueName(queueName).build();

      String queueUrl = sqsClient.getQueueUrl(getQueueUrlRequest).queueUrl();

      List<DeleteMessageBatchRequestEntry> entries = new ArrayList<>();
      messageDtos.forEach(
          message -> {
            entries.add(
                DeleteMessageBatchRequestEntry.builder()
                    .id(message.messageId())
                    .receiptHandle(message.receiptHandler())
                    .build());
          });

      DeleteMessageBatchRequest deleteMessageBatchRequest =
          DeleteMessageBatchRequest.builder().queueUrl(queueUrl).entries(entries).build();

      sqsClient.deleteMessageBatch(deleteMessageBatchRequest);
      log.info("{} - {} message(s) sucessfully deleted from Queue!", method, messageDtos.size());
      log.info("{} - Finished!", method);
    } catch (SqsException se) {
      log.error("{} - SqsException: {}", method, se.awsErrorDetails().errorMessage());
      se.printStackTrace();
    }
  }
}
