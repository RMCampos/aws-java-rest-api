package br.com.campos.ricardo.awsjavarestapi.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "config.mail")
@Getter
@Setter
public class MailPropertiesConfig {

  private String fromAddress;

  private String fromName;

  private String fromPassword;

  private String debug;

  private String smtpHost;

  private String smtpPort;

  private String smtpAuth;

  private String smtpStarttlsEnabled;

  private String smtpSocketFactoryClass;
}
