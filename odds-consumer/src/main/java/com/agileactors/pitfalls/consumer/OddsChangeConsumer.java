package com.agileactors.pitfalls.consumer;

import com.agileactors.pitfalls.executor.KeyedWorkerPool;
import com.agileactors.pitfalls.service.OddsChangeProcessor;
import com.rabbitmq.client.Channel;
import java.util.concurrent.ExecutorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class OddsChangeConsumer {

    private final OddsChangeProcessor processor;
    private final KeyedWorkerPool workerPool;
    private final ExecutorService ackExecutor;

    @RabbitListener(queues = "${app.rabbitmq.queue-name}")
    public void onMessage(Message message, Channel channel) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        log.info("Received OddsChange message, deliveryTag={}", deliveryTag);
        long eventId = (long) message.getMessageProperties().getHeaders().get("eventId");

        /* P3 - Throughput Collapse
         * Offload processing to worker pool to prevent blocking the listener thread,
         * and use a separate executor for acknowledgments to ensure timely ACKs even under load.
         *
         * P4 - Stale Data
         * EventId is used to route messages to the same worker, ensuring message order is preserved
         */
        workerPool.submit(eventId, () -> {
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

