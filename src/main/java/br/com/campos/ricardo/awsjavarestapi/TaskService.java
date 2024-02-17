package br.com.campos.ricardo.awsjavarestapi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
public class TaskService {

  public List<String> getTaskList() {
    List<String> tasks = new ArrayList<>();
    Arrays.asList(TaskEnum.values())
        .forEach(
            task -> {
              tasks.add(task.toString());
            });
    return tasks;
  }

  public TaskResponse handleTask(String taskName) {
    TaskEnum taskEnum = TaskEnum.getByCode(taskName);
    if (taskEnum == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Task!");
    }

    log.info("Handling task: {}", taskName);

    // Get from AWS S3
    String template = getTemplate(taskEnum);
    if (template.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No template for this Task!");
    }

    // Send to SQS to be sent through email
    Task task = new Task(taskEnum.name(), template);
    sendToSqs(task);

    TaskResponse response = new TaskResponse();
    response.setTask(task);
    response.setMessage("Task processed and sent to the Queue!");

    return response;
  }

  private String getTemplate(TaskEnum task) {
    log.info("Getting task template from AWS S3");

    if (task.equals(TaskEnum.CODE_REVIEW)) {
      log.info("Template found for task CODE_REVIEW");
      return "Here 1";
    } else if (task.equals(TaskEnum.HELP_COLLEAGUES)) {
      log.info("Template found for task HELP_COLLEAGUES");
      return "Here 2";
    }
    log.warn("No template found for task :(");
    return "";
  }

  private void sendToSqs(Task task) {
    log.info("Sending task to SQS {}", task.name());
  }
}
