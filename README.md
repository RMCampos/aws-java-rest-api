# AWS Java REST API
Java REST API to play with AWS technologies

Tasks:
- List all tasks available through GET http request: http://localhost:8080/tasks/get-list
- Receive a task name through POST HTTP request to be processed: http://localhost:8080/tasks/handle-task/CODE_REVIEW
  - Get task description from AWS S3 given the task name
  - Send to SQS to be sent through email as finished

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

## Docker

Building:

```sh
docker build -t aws-java-rest-api:0.0.1 .
```

Running:

```sh
docker run -it --rm \
  -p 8080:8080 \
  --name aws-java-rest-api \
  aws-java-rest-api
```

Login:

```sh
docker login -u rmcampos
```

Push image
```sh
docker image push rmcampos/aws-java-rest-api:0.0.1
```

### AWS

Running:

```sh
docker run -it --rm -d \
  -p 8080:8080 \
  --name aws-java-rest-api \
  docker.io/rmcampos/aws-java-rest-api:0.0.1
```
