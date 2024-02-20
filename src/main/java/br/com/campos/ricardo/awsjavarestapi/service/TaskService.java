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
    log.info("GetTasksList - {} tasks found", tasks.size());
    log.info("GetTasksList - Finished!");
    return tasks;
  }

  //
  // HandleTask methods
  //
  public TaskResponseDto handleTask(String taskName) {
    TaskEnum taskEnum = TaskEnum.getByCode(taskName);
    if (taskEnum == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Task!");
    }

    log.info("HandleTask - Handling task: {}", taskName);

    // Get from AWS S3
    TaskDto task = awsS3Service.getSingleTaskFile(taskEnum, "HandleTask");

    // Send to SQS to be sent through email
    sqsService.addMessageToQueue(task.description(), "HandleTask");

    TaskResponseDto response = new TaskResponseDto();
    response.setTask(task);
    response.setMessage("Task processed and added in the Queue to be processed!");
    log.info("HandleTask - Task processed and added in the Queue to be processed!");
    log.info("HandleTask - Finished!");

    return response;
  }
}
