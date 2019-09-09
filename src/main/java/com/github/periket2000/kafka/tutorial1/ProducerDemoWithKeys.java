package com.github.periket2000.kafka.tutorial1;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class ProducerDemoWithKeys {
    public static void main(String[] args) throws ExecutionException, InterruptedException {

        final Logger logger = LoggerFactory.getLogger(ProducerDemoWithKeys.class);

        // create properties
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // create producer
        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);

        // send data
        for(int i = 0; i < 10; i++) {
            final String key = "id_" + i;
            producer.send(new ProducerRecord<String, String>("first_topic", key, "value with key " + i),
                    new Callback() {
                        public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                            if(e == null) {
                                logger.info("Topic: " + recordMetadata.topic());
                                logger.info("Partition: " + recordMetadata.partition());
                                logger.info("key: " + key);
                            }
                        }
                    });
        }
        producer.close();
    }
}
