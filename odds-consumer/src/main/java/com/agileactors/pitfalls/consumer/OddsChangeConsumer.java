package com.agileactors.pitfalls.consumer;

import com.agileactors.pitfalls.service.OddsChangeProcessor;
import com.rabbitmq.client.Channel;
import java.util.concurrent.ExecutorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OddsChangeConsumer {

    private final OddsChangeProcessor processor;
    private final ExecutorService workerPool;
    private final ExecutorService ackExecutor;

    public OddsChangeConsumer(OddsChangeProcessor processor, @Qualifier("workerPool") ExecutorService workerPool,
        @Qualifier("ackExecutor") ExecutorService ackExecutor) {
        this.processor = processor;
        this.workerPool = workerPool;
        this.ackExecutor = ackExecutor;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue-name}")
    public void onMessage(Message message, Channel channel) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        log.info("Received odds change message, deliveryTag={}", deliveryTag);

        /* P3 - Throughput Collapse
         * Offload processing to worker pool to prevent blocking the listener thread,
         * and use a separate executor for acknowledgments to ensure timely ACKs even under load.
         */
        workerPool.submit(() -> {
            Action action = processor.process(message);
            ackExecutor.submit(() -> {
                try {
                    switch (action) {
                        case ACK -> channel.basicAck(deliveryTag, false);
                        case REJECT -> channel.basicReject(deliveryTag, false);
                        case RETRY -> channel.basicNack(deliveryTag, false, true);
                    }
                } catch (Exception e) {
                    log.error("Failed to ack/nack message, deliveryTag={}", deliveryTag, e);
                }
            });
        });
    }

}

