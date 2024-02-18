package br.com.campos.ricardo.awsjavarestapi.service;

import br.com.campos.ricardo.awsjavarestapi.aws.AwsS3Service;
import br.com.campos.ricardo.awsjavarestapi.aws.AwsSqsService;
import br.com.campos.ricardo.awsjavarestapi.dto.TaskDto;
import br.com.campos.ricardo.awsjavarestapi.dto.TaskResponseDto;
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

  public List<TaskDto> getTaskList() {
    List<TaskDto> tasks = awsS3Service.getAllTaskFiles();
    return tasks;
  }

  public TaskResponseDto handleTask(String taskName) {
    TaskEnum taskEnum = TaskEnum.getByCode(taskName);
    if (taskEnum == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Task!");
    }

    log.info("Handling task: {}", taskName);

    // Get from AWS S3
    TaskDto task = awsS3Service.getSingleTaskFile(taskEnum);

    // Send to SQS to be sent through email
    sqsService.addMessageToQueue(task.description());

    TaskResponseDto response = new TaskResponseDto();
    response.setTask(task);
    response.setMessage("Task processed and sent to the Queue!");

    return response;
  }
}
