package com.kafka.orderservice.controller;



import com.kafka.kafkacommon.dto.Order;
import com.kafka.kafkacommon.dto.OrderEvent;
import com.kafka.orderservice.service.OrderProducer;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderProducer producer;

    public OrderController(OrderProducer producer) {
        this.producer = producer;
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody Order req) {
        var event = new Order(
                req.getOrderId(), req.getName(), req.getQuantity(), req.getPrice()
        );
        OrderEvent orderEvent = new OrderEvent();
        orderEvent.setMessage("Order is Pending");
        orderEvent.setStatus("Pending");
        orderEvent.setOrder(event);
        producer.send(orderEvent);
        return ResponseEntity.accepted().body("Order accepted: " + req.getOrderId());
    }
}
