package com.github.periket2000.kafka.streams.map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;

import java.util.Properties;

public class StreamsMapTweets {
    public static void main(String[] args) {
        // create properties
        Properties p = new Properties();
        p.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        p.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "stream-map-tweets");
        p.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName());
        p.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName());
        // create topology
        StreamsBuilder builder = new StreamsBuilder();
        // input
        KStream<String, String> input = builder.stream("filtered_tweets");
        KStream<String, String> mapped = input.map((k, tweet) -> {
            return new KeyValue<>(k, map(tweet));
        });
        mapped.to("reduced_tweets");
        // build topology
        KafkaStreams stream = new KafkaStreams(builder.build(), p);
        // start
        stream.start();
        Runtime.getRuntime().addShutdownHook(new Thread(stream::close));
    }

    private static JsonParser parser = new JsonParser();

    private static String map(String tweet) {
        try {
            JsonObject source = parser.parse(tweet)
                    .getAsJsonObject()
                    .get("payload")
                    .getAsJsonObject();

            JsonObject mappedTweet = new JsonObject();
            mappedTweet.addProperty("time", source.get("CreatedAt").getAsString());
            mappedTweet.addProperty("text", source.get("Text").getAsString());
            mappedTweet.addProperty("user", source.get("User").getAsJsonObject().get("Name").getAsString());
            return mappedTweet.toString();
        } catch (NullPointerException e) {
            return "{}";
        }
    }
}
