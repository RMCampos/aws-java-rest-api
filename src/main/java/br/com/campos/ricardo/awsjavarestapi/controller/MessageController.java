package br.com.campos.ricardo.awsjavarestapi.controller;

import br.com.campos.ricardo.awsjavarestapi.aws.AwsSqsService;
import br.com.campos.ricardo.awsjavarestapi.dto.AwsMessageDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/messages")
public class MessageController {

  private final AwsSqsService awsSqsService;

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public List<AwsMessageDto> getMessages() {
    return awsSqsService.getAllMessagesInQueue();
  }

  @PostMapping("/{id}")
  public void processMessage(@PathVariable String id) {
    awsSqsService.processMessage(id);
  }
}
