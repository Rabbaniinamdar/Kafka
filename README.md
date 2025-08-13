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

This stops and removes the containers.

