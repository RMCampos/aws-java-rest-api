package br.com.campos.ricardo.awsjavarestapi.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskResponseDto {

  private String message;
  private TaskDto task;
}
