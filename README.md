# AWS Java REST API
Java REST API to play with AWS technologies

Tasks:
- List all tasks available through GET http request: http://localhost:8080/tasks/get-list
- Receive a task name through POST HTTP request to be processed: http://localhost:8080/tasks/handle-task/CODE_REVIEW
  - Get task description from AWS S3 given the task name
  - Send to SQS to be sent through email as finished
- List all messages in Queue and remove them through GET http://localhost:8080/messages
- Process first message and send an email with its content through POST http://localhost:8080/messages/process-first

### Technologies
- Amazon EC2
- Amazon S3
- Amazon SQS
- Mailtrap.io (free email testing)
- Cloud Native (with GraalVM)
- Docker

### Packaging

- Group: `br.com.campos.ricardo`
- Artifact: `aws-java-rest-api`
- Java version: `17`
- Project: `Maven`
- Spring Boot version: `3.2.2`
- Version: 0.0.1

### Dependencies
- Spring Web
- Spring Boot DevTools
- Spring Boot Actuator
- Lombok
- AWS SDK S3
- AWS SDK SQS
- Java Mail

## Docker

Building:

```sh
docker build -t rmcampos/aws-java-rest-api:1.0.0 .
```

Running:

```sh
docker run -it --rm \
  -p 8080:8080 \
  --name rmcampos/aws-java-rest-api:1.0.0 \
  aws-java-rest-api
```

Login:

```sh
docker login -u rmcampos
```

Push image
```sh
docker image push rmcampos/aws-java-rest-api:1.0.0
```

### AWS

Running:

```sh
docker run -it --rm -d \
  -p 8080:8080 \
  --name aws-java-rest-api \
  docker.io/rmcampos/aws-java-rest-api:1.0.0
```
