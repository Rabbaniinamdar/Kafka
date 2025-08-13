package com.kafka.emailservice;

import com.kafka.kafkacommon.dto.OrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class EmailConsumer {
    private static final Logger log = LoggerFactory.getLogger(EmailConsumer.class);

    @KafkaListener(topics = "${app.topics.orders}",groupId = "${spring.kafka.consumer.group-id}")
    public void onOrderCreated(OrderEvent evt) {
        log.info("[email] sending to {} for order {}", evt.getOrder().getName(), evt.getOrder().getOrderId());
        // TODO: send email (SMTP/SES/etc.)
    }
}
