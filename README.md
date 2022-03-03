# quarkus-rabbitmq-retry-strategy

This project was created to implement a replay strategy for article-based message consumption: https://programmerfriend.com/rabbit-mq-retry/

![image](https://user-images.githubusercontent.com/6283514/156647036-3c571a2e-a10b-4a59-8205-40e768d64154.png)

For this sample, the auto-create queues and bindings were disabled, but if you need the application auto-create it just enables and configures the other properties, uses that guide with references:

- https://smallrye.io/smallrye-reactive-messaging/smallrye-reactive-messaging/3.11/rabbitmq/rabbitmq.html

on the other hand, was created the [definitions.json](https://github.com/gbzarelli/quarkus-rabbitmq-retry-strategy/blob/main/stack/rabbitmq/definitions.json) to up with the [docker-compose](https://github.com/gbzarelli/quarkus-rabbitmq-retry-strategy/tree/main/stack)

## Running the application in dev mode

Run the docker compose:

```shell
docker-compose -f ./stack/docker-compose.yml up -d
```

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application

The application can be packaged using:
```shell script
./mvnw package
```
It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:
```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using: 
```shell script
./mvnw package -Pnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell script
./mvnw package -Pnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/sample-rabbitmq-worker-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.

## Provided Code

### SmallRye Reactive Messaging RabbitMQ

- https://quarkus.io/guides/rabbitmq
- https://quarkus.io/guides/rabbitmq-reference
