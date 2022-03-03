package com.helpdev;

import io.smallrye.reactive.messaging.rabbitmq.IncomingRabbitMQMetadata;
import io.smallrye.reactive.messaging.rabbitmq.OutgoingRabbitMQMetadata;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.reactive.messaging.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class MessagingIncoming {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Emitter<InputMessage> parkingLotEmitter;
    private final ProcessInputMessage processor;

    public MessagingIncoming(final @Channel("parkingLot") Emitter<InputMessage> parkingLotEmitter,
                             final ProcessInputMessage processor) {
        this.parkingLotEmitter = parkingLotEmitter;
        this.processor = processor;
    }

    @Incoming("sample-queue")
    public CompletionStage<Void> incoming(final Message<JsonObject> message) {
        final var inputMessage = message.getPayload().mapTo(InputMessage.class);
        logger.debug("Input message: " + inputMessage);

        try {
            processor.doProcess(inputMessage);
        } catch (Throwable tr) {
            if (isEligibleToParkingLot(message)) {
                sendToParkingLot(message, inputMessage, tr);
            } else {
                return message.nack(tr);
            }
        }
        return message.ack();
    }

    private void sendToParkingLot(final Message<JsonObject> originalInputMessage,
                                  final InputMessage inputMessage,
                                  final Throwable exception) {
        final var headerException = new HashMap<String, String>();
        headerException.put("cause", String.valueOf(exception.getCause()));
        headerException.put("message", exception.getMessage());
        headerException.put("localizedMessage", exception.getLocalizedMessage());
        headerException.put("toString", exception.toString());

        final var metadata = new OutgoingRabbitMQMetadata.Builder()
                .withHeader("exception", headerException)
                .withTimestamp(ZonedDateTime.now());

        originalInputMessage.getMetadata()
                .get(IncomingRabbitMQMetadata.class)
                .ifPresent(it -> metadata.withHeader("original-meta-headers", it.getHeaders()));

        parkingLotEmitter.send(Message.of(inputMessage, Metadata.of(metadata.build())));
        logger.info("Message was sent to parkingLot");
    }

    @SuppressWarnings("unchecked")
    public boolean isEligibleToParkingLot(final Message<?> message) {
        final var xDeathHeaders = message.getMetadata()
                .get(IncomingRabbitMQMetadata.class)
                .map(IncomingRabbitMQMetadata::getHeaders)
                .map(it -> (List<?>) it.getOrDefault("x-death", Collections.emptyList()))
                .orElse(Collections.emptyList())
                .stream()
                .findFirst();

        if (xDeathHeaders.isPresent()) {
            HashMap<String, Object> hash = (HashMap<String, Object>) xDeathHeaders.get();
            return (Long) hash.get("count") >= 2;
        }
        return false;
    }

}