package com.helpdev.consumer;

import io.smallrye.reactive.messaging.rabbitmq.IncomingRabbitMQMetadata;
import io.smallrye.reactive.messaging.rabbitmq.OutgoingRabbitMQMetadata;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ParkingLotManager {

    public static final int DEFAULT_RETRY_COUNT = 2;
    private final Emitter<JsonObject> parkingLotEmitter;

    public ParkingLotManager(final Emitter<JsonObject> parkingLotEmitter) {
        this.parkingLotEmitter = parkingLotEmitter;
    }

    public void sendToParkingLot(final Message<JsonObject> originalInputMessage,
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

        parkingLotEmitter.send(Message.of(originalInputMessage.getPayload(), Metadata.of(metadata.build())));
    }

    @SuppressWarnings("unchecked")
    public boolean isEligibleToParkingLot(final Message<?> message, int maxRetryAccepted) {
        final var xDeathHeaders = message.getMetadata()
                .get(IncomingRabbitMQMetadata.class)
                .map(IncomingRabbitMQMetadata::getHeaders)
                .map(it -> (List<?>) it.getOrDefault("x-death", Collections.emptyList()))
                .orElse(Collections.emptyList())
                .stream()
                .findFirst();

        if (xDeathHeaders.isPresent()) {
            HashMap<String, Object> hash = (HashMap<String, Object>) xDeathHeaders.get();
            return (Long) hash.get("count") >= maxRetryAccepted;
        }
        return false;
    }


    public boolean isEligibleToParkingLot(final Message<?> message) {
        return isEligibleToParkingLot(message, DEFAULT_RETRY_COUNT);
    }
}
