package br.com.campos.ricardo.awsjavarestapi.service;

import br.com.campos.ricardo.awsjavarestapi.config.MailPropertiesConfig;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

  @Autowired private MailPropertiesConfig mailPropertiesConfig;

  public boolean sendEmail(String body) {
    Authenticator authenticator =
        new Authenticator() {
          @Override
          protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(
                mailPropertiesConfig.getFromName(), mailPropertiesConfig.getFromPassword());
          }
        };

    Properties propvls = getProperties();
    Session session = Session.getInstance(propvls, authenticator);

    String toEmail = "to@example.com";
    String toName = "Ricardo Campos";

    try {
      Message message = new MimeMessage(session);
      message.setFrom(new InternetAddress(mailPropertiesConfig.getFromAddress(), "Mailtrap"));
      message.setRecipient(RecipientType.TO, new InternetAddress(toEmail, toName));
      message.setSubject("AWS Java Rest on " + LocalDateTime.now().toString());
      message.setContent(body, "text/plain; charset=UTF-8");
      message.setSentDate(new java.util.Date());

      Transport.send(message);
      log.info("Email successfully sent!");

      return true;
    } catch (MessagingException | UnsupportedEncodingException me) {
      me.printStackTrace();
    }

    return false;
  }

  private Properties getProperties() {
    Properties propvls = System.getProperties();

    propvls.setProperty("mail.smtp.host", mailPropertiesConfig.getSmtpHost());
    propvls.put("mail.smtp.ssl.trust", mailPropertiesConfig.getSmtpHost());
    if (!Objects.isNull(mailPropertiesConfig.getDebug())
        && !mailPropertiesConfig.getDebug().isBlank()) {
      propvls.put("mail.debug", mailPropertiesConfig.getDebug());
    }
    propvls.put("mail.smtp.port", mailPropertiesConfig.getSmtpPort());
    if (!Objects.isNull(mailPropertiesConfig.getSmtpAuth())
        && !mailPropertiesConfig.getSmtpAuth().isBlank()) {
      propvls.put("mail.smtp.auth", mailPropertiesConfig.getSmtpAuth());
    }
    if (!Objects.isNull(mailPropertiesConfig.getSmtpStarttlsEnabled())
        && !mailPropertiesConfig.getSmtpStarttlsEnabled().isBlank()) {
      propvls.put("mail.smtp.starttls.enable", mailPropertiesConfig.getSmtpStarttlsEnabled());
    }
    propvls.put("mail.smtp.socketFactory.class", mailPropertiesConfig.getSmtpSocketFactoryClass());

    return propvls;
  }
}
