package br.com.campos.ricardo.awsjavarestapi.enums;

public enum TaskEnum {
  CODE_REVIEW("CODE_REVIEW"),
  HELP_COLLEAGUES("HELP_COLLEAGUES");

  private String code;

  TaskEnum(String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }

  public static TaskEnum getByCode(String code) {
    for (TaskEnum taskEnum : values()) {
      if (taskEnum.code.equals(code)) {
        return taskEnum;
      }
    }
    return null;
  }
}
