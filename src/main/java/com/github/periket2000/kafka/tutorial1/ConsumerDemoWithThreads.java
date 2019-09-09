package com.github.periket2000.kafka.tutorial1;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class ConsumerDemoWithThreads {

    Logger logger = LoggerFactory.getLogger(ConsumerDemoWithThreads.class);
    private CountDownLatch latch;

    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger(ConsumerDemoWithThreads.class);
        new ConsumerDemoWithThreads().run();
    }

    private ConsumerDemoWithThreads() {
        this.latch = new CountDownLatch(1);
    }

    private void run() {
        Runnable runnable = new ConsumerThread();
        Thread thread = new Thread(runnable);
        thread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutdown hook executing");
            ((ConsumerThread)runnable).shutdown();
            try {
                latch.await();
            } catch (InterruptedException e) {
                logger.error("Interrupted exception", e);
            } finally {
                logger.info("Application exited.");
            }
        }));

        try {
            latch.await();
        } catch (InterruptedException e) {
            logger.error("Application interrupted", e);
        } finally {
            logger.info("Closing");
        }
    }

    public class ConsumerThread implements Runnable {

        private KafkaConsumer<String, String> consumer;
        Logger logger = LoggerFactory.getLogger(ConsumerThread.class);

        public ConsumerThread() {
            // create properties
            Properties properties = new Properties();
            properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
            properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "app7");
            properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

            // Create consumer
            this.consumer = consumer = new KafkaConsumer<String, String>(properties);

            // Subscribe
            consumer.subscribe(Arrays.asList("first_topic"));
        }

        @Override
        public void run() {
            try {
                // Poll data
                while (true) {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                    for (ConsumerRecord record : records) {
                        logger.info("key: " + record.key() + " -> value: " + record.value());
                    }
                }
            } catch (WakeupException e) {
                logger.info("Received shutdown signal", e);
            } finally {
                consumer.close();
                latch.countDown();
            }
        }

        public void shutdown() {
            consumer.wakeup();
        }
    }

}
