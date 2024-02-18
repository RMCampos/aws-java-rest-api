package br.com.campos.ricardo.awsjavarestapi;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
public class TaskService {

  @Autowired private AwsS3Service awsS3Service;

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
    sendToSqs(task);

    TaskResponse response = new TaskResponse();
    response.setTask(task);
    response.setMessage("Task processed and sent to the Queue!");

    return response;
  }

  private void sendToSqs(Task task) {
    log.info("Sending task to SQS {}", task.name());
  }
}
