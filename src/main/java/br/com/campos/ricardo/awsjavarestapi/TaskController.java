package br.com.campos.ricardo.awsjavarestapi;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tasks")
public class TaskController {

  @Autowired private TaskService taskService;

  @GetMapping(path = "/get-list", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Task> getTasksList() {
    return taskService.getTaskList();
  }

  @PostMapping(path = "/handle-task/{taskName}", produces = MediaType.APPLICATION_JSON_VALUE)
  public TaskResponse doTask(@PathVariable String taskName) {
    return taskService.handleTask(taskName);
  }
}
