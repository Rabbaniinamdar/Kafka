Here’s the **step-by-step guide** to install and run Kafka with Docker instead of doing the manual installation.
We’ll use **Docker + Docker Compose** to quickly spin up both **Kafka** and **ZooKeeper**.

---

## **1️⃣ Install Docker**

* Download and install **Docker Desktop** from:
  [https://www.docker.com/products/docker-desktop/](https://www.docker.com/products/docker-desktop/)
* Make sure Docker is running by checking:

```bash
docker --version
```

---

## **2️⃣ Create a `docker-compose.yml` file**

In an empty folder, create a file named `docker-compose.yml`:

```yaml
version: '3.8'

services:
  zookeeper:
    image: confluentinc/cp-zookeeper:7.4.0
    container_name: zookeeper
    ports:
      - "2181:2181"
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000

  kafka:
    image: confluentinc/cp-kafka:7.4.0
    container_name: kafka
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    depends_on:
      - zookeeper
```

---

## **3️⃣ Start Kafka and Zookeeper**

In the same folder where `docker-compose.yml` is saved, run:

```bash
docker-compose up -d
```

* `-d` runs the containers in the background.
* This will pull the **Confluent Kafka** and **ZooKeeper** images and start them.

---

## **4️⃣ Verify Services are Running**

```bash
docker ps
```

You should see **zookeeper** and **kafka** containers running.

---

## **5️⃣ Using Kafka CLI Commands in Docker**

Since Kafka is inside the container, you’ll run commands using:

```bash
docker exec -it kafka bash
```

Now inside the Kafka container, you can run:

**Create a Topic**

```bash
kafka-topics --create --topic topic_demo --bootstrap-server localhost:9092
```

**List Topics**

```bash
kafka-topics --list --bootstrap-server localhost:9092
```

**Start Producer**

```bash
kafka-console-producer --topic topic_demo --bootstrap-server localhost:9092
```

(Type messages here and press Enter)

**Start Consumer**

```bash
kafka-console-consumer --topic topic_demo --from-beginning --bootstrap-server localhost:9092
```

---

## **6️⃣ Stop Kafka**

```bash
docker-compose down
```



# **Apache Kafka – Detailed Notes (with Spring Boot Context)**

---

## **1. What is Kafka?**

* **Apache Kafka** is a **distributed event streaming platform** used for:

  * High-throughput messaging
  * Real-time data pipelines
  * Event-driven architectures
* Works like a publish-subscribe system but **distributed** and **fault-tolerant**.

---

## **2. Core Concepts**

| Concept                | Description                                                        |
| ---------------------- | ------------------------------------------------------------------ |
| **Producer**           | Application that sends messages (events) to Kafka topics.          |
| **Consumer**           | Application that reads messages from Kafka topics.                 |
| **Topic**              | Named category to which messages are published.                    |
| **Partition**          | Subdivision of a topic for parallelism and scalability.            |
| **Offset**             | Sequence number that identifies each record within a partition.    |
| **Broker**             | Kafka server that stores topics and handles requests.              |
| **Cluster**            | Group of Kafka brokers working together.                           |
| **Consumer Group**     | Set of consumers sharing a group ID for load-balanced consumption. |
| **Replication Factor** | Number of copies of a partition for fault tolerance.               |
| **Retention Period**   | How long Kafka keeps a message before deletion.                    |

---

## **3. Kafka Message Flow**

1. **Producer** → serializes data → sends to a topic.
2. **Kafka Broker** → stores it in partitions.
3. **Consumers** → in a **consumer group** read messages.
4. Kafka tracks **offsets** for each group.

---

## **4. Serialization & Deserialization**

Kafka sends data as **byte arrays**.

* **Serializer** → Converts Java objects → bytes (Producer side).
* **Deserializer** → Converts bytes → Java objects (Consumer side).

### Common Serializers

| Type          | Serializer Class      | Deserializer Class      |
| ------------- | --------------------- | ----------------------- |
| String        | `StringSerializer`    | `StringDeserializer`    |
| JSON (Spring) | `JsonSerializer`      | `JsonDeserializer`      |
| Avro          | `KafkaAvroSerializer` | `KafkaAvroDeserializer` |

**Example (Spring Boot Producer):**

```yaml
spring:
  kafka:
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
```

**Why?**

* `key-serializer`: Key as a String (e.g., orderId).
* `value-serializer`: Value as JSON (Java object → JSON → bytes).

**Example (Spring Boot Consumer):**

```yaml
spring:
  kafka:
    consumer:
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "*"
```

* `spring.json.trusted.packages: "*"` → Allows deserializing any package (needed for POJOs).

---

## **5. Spring Boot Kafka Integration**

### Producer Example

```java
@Service
public class OrderProducer {
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public OrderProducer(KafkaTemplate<String, OrderEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendOrder(OrderEvent event) {
        kafkaTemplate.send("order-created", event.getOrderId(), event);
    }
}
```

---

### Consumer Example

```java
@Service
public class OrderConsumer {
    @KafkaListener(topics = "order-created", groupId = "email-service-group")
    public void consume(OrderEvent event) {
        System.out.println("Received: " + event);
    }
}
```

---

## **6. Kafka with Partitions and Consumer Groups**

* **Partitions** → Increase throughput by parallelizing.
* **Consumer Groups**:

  * Each partition is consumed by **only one consumer in a group**.
  * Multiple consumers in the same group share load.
  * Different groups get all messages.

---

## **7. Offset Management**

* Kafka tracks the **offset** for each consumer group.
* Consumers can:

  * **Auto commit** (`enable.auto.commit=true`)
  * **Manual commit** (commit offsets only after successful processing).

---

## **8. Dead Letter Topics (DLT)**

* If a consumer fails to process a message even after retries, Kafka can redirect it to a **DLT**.

**Example Spring Config:**

```yaml
spring:
  kafka:
    consumer:
      enable-auto-commit: false
    listener:
      ack-mode: manual
      retry:
        max-attempts: 3
      dlt:
        topic-suffix: .DLT
```

---

## **9. Advantages of Kafka**

* **High throughput** – millions of messages/sec.
* **Durability** – data stored on disk & replicated.
* **Scalable** – add partitions & brokers.
* **Fault tolerant** – replication ensures availability.
* **Real-time** – low latency message delivery.

---

## **10. Real Project Flow Example** (Your `Order-Service` & `Email-Service`)

**Step-by-step:**

1. **Order-Service**:

   * Receives API request to create order.
   * Publishes `OrderEvent` → `order-created` topic using:

     * `StringSerializer` (for key)
     * `JsonSerializer` (for value)
2. **Kafka**:

   * Stores event in topic partitions.
   * Distributes to consumers in `email-service-group`.
3. **Email-Service**:

   * Listens to `order-created`.
   * Uses `StringDeserializer` + `JsonDeserializer<OrderEvent>` to read event.
   * Sends confirmation email.
4. **If fails**:

   * Retries 3 times.
   * If still fails → goes to `order-created.DLT` for later processing.

---

## **11. Common Interview Questions**

1. Difference between Kafka and RabbitMQ?
2. How does Kafka ensure fault tolerance?
3. What is the role of Zookeeper (in older Kafka versions)?
4. How does partitioning affect ordering?
5. What is exactly-once delivery in Kafka?
6. What is a compacted topic?
7. How do you handle message reprocessing?

---
