package br.com.campos.ricardo.awsjavarestapi.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskResponse {

  private String message;
  private Task task;
}
