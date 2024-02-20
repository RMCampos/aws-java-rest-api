package br.com.campos.ricardo.awsjavarestapi.aws;

import br.com.campos.ricardo.awsjavarestapi.dto.TaskDto;
import br.com.campos.ricardo.awsjavarestapi.enums.TaskEnum;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@Service
public class AwsS3Service {

  @Autowired private AwsConfig awsConfig;

  public TaskDto getSingleTaskFile(TaskEnum task, String method) {
    S3Client s3Client = buildS3Client(method);

    List<String> taskStrings = getS3Objects(s3Client, List.of(task.getCode()), method);

    s3Client.close();

    return parseTasks(taskStrings, method).get(0);
  }

  public List<TaskDto> getAllTaskFiles() {
    S3Client s3Client = buildS3Client("GetTasksList");

    List<String> taskFileNames = new ArrayList<>();
    Arrays.asList(TaskEnum.values()).forEach(task -> taskFileNames.add(task.toString()));

    List<String> taskStrings = getS3Objects(s3Client, taskFileNames, "GetTasksList");

    s3Client.close();

    return parseTasks(taskStrings, "GetTasksList");
  }

  private S3Client buildS3Client(String method) {
    log.info("{} - AWS Region: {}", method, awsConfig.getAwsRegion());
    Region region = Region.of(awsConfig.getAwsRegion());

    return S3Client.builder().region(region).build();
  }

  /**
   * Gets all S3 objects.
   *
   * @param s3Client The {@link S3Client} configuration
   * @return A Lit of {@link File} containing all files or an empty list.
   */
  private List<String> getS3Objects(S3Client s3Client, List<String> taskNames, String method) {
    log.info("{} - AWS S3 Bucket name: {}", method, awsConfig.getAwsS3BucketName());

    String bucketName = awsConfig.getAwsS3BucketName();
    List<String> tasks = new ArrayList<>();
    taskNames.forEach(
        task -> {
          tasks.add(task.toString() + "_TASK.json");
        });

    List<String> s3FilesFound = new ArrayList<>();

    try {
      for (String key : tasks) {
        GetObjectRequest objectRequest =
            GetObjectRequest.builder().key(key).bucket(bucketName).build();

        ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(objectRequest);
        byte[] data = objectBytes.asByteArray();

        String fileContent = new String(data);
        log.info("{} - File read {} bytes: {}", method, key, data.length);
        s3FilesFound.add(fileContent);
      }

      return s3FilesFound;
    } catch (S3Exception se) {
      log.error("{} - S3Exception: {}", method, se.awsErrorDetails().errorMessage());
      se.printStackTrace();
    }

    return List.of();
  }

  /**
   * Parse a task from JSON format to a {@link TaskDto}.
   *
   * @param taskStrings List of Tasks in JSON format to be parsed.
   * @return List of {@link TaskDto} or empty list.
   */
  private List<TaskDto> parseTasks(List<String> taskStrings, String method) {
    List<TaskDto> tasks = new ArrayList<>();
    ObjectMapper mapper = new ObjectMapper();

    for (String fileJsonContent : taskStrings) {
      try {
        TaskDto taskFromJson = mapper.readValue(fileJsonContent, TaskDto.class);

        log.info(
            "{} - Parsed from json content - name {} and description {}",
            method,
            taskFromJson.name(),
            taskFromJson.description());

        tasks.add(taskFromJson);
      } catch (JsonProcessingException jme) {
        jme.printStackTrace();
      }
    }

    return tasks;
  }
}
