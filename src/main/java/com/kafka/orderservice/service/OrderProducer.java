package com.kafka.orderservice.service;


import com.kafka.kafkacommon.dto.OrderEvent;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OrderProducer {
    private final KafkaTemplate<String, OrderEvent> kafka;
    private final String ordersTopic;

    public OrderProducer(KafkaTemplate<String, OrderEvent> kafka,
                         Environment env) {
        this.kafka = kafka;
        this.ordersTopic = env.getProperty("app.topics.orders");
    }

    public void send(OrderEvent event) {

        System.out.println("Sending order event: " + event.toString());
        var correlationId = UUID.randomUUID().toString();
        Message<OrderEvent> msg = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, ordersTopic)
                .setHeader("correlationId", correlationId)
                .build();
        kafka.send(msg);
    }
}
