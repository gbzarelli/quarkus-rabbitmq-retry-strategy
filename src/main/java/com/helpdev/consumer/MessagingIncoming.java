package com.helpdev.consumer;

import com.helpdev.InputMessageDto;
import com.helpdev.ProcessInputMessage;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class MessagingIncoming {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ParkingLotManager parkingLotManager;
    private final ProcessInputMessage processor;

    public MessagingIncoming(final @Channel("parkingLot") Emitter<JsonObject> parkingLotManager,
                             final ProcessInputMessage processor) {
        this.parkingLotManager = new ParkingLotManager(parkingLotManager);
        this.processor = processor;
    }

    @Incoming("sample-queue")
    public CompletionStage<Void> incoming(final Message<JsonObject> message) {
        final var inputMessage = message.getPayload().mapTo(InputMessageDto.class);

        logger.debug("Input message: " + inputMessage);

        try {
            processor.doProcess(inputMessage);
        } catch (Throwable tr) {
            if (parkingLotManager.isEligibleToParkingLot(message)) {
                parkingLotManager.sendToParkingLot(message, tr);
                logger.info("Message was sent to parkingLot");
            } else {
                return message.nack(tr);
            }
        }
        return message.ack();
    }


}