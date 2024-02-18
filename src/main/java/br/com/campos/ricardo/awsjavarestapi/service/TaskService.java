package br.com.campos.ricardo.awsjavarestapi.service;

import br.com.campos.ricardo.awsjavarestapi.aws.AwsS3Service;
import br.com.campos.ricardo.awsjavarestapi.aws.AwsSqsService;
import br.com.campos.ricardo.awsjavarestapi.dto.Task;
import br.com.campos.ricardo.awsjavarestapi.dto.TaskResponse;
import br.com.campos.ricardo.awsjavarestapi.enums.TaskEnum;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

  private final AwsS3Service awsS3Service;

  private final AwsSqsService sqsService;

  public List<Task> getTaskList() {
    List<Task> tasks = awsS3Service.getAllTaskFiles();
    return tasks;
  }

  public TaskResponse handleTask(String taskName) {
    TaskEnum taskEnum = TaskEnum.getByCode(taskName);
    if (taskEnum == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Task!");
    }

    log.info("Handling task: {}", taskName);

    // Get from AWS S3
    Task task = awsS3Service.getSingleTaskFile(taskEnum);

    // Send to SQS to be sent through email
    sqsService.addMessageToQueue(task.description());

    TaskResponse response = new TaskResponse();
    response.setTask(task);
    response.setMessage("Task processed and sent to the Queue!");

    return response;
  }
}
