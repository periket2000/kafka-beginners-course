package com.github.periket2000.kafka.streams.filter;

import com.google.gson.JsonParser;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;

import java.util.Properties;

public class StreamsFilterTweets {
    public static void main(String[] args) {
        // create properties
        Properties p = new Properties();
        p.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:29092");
        p.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "stream-filter-tweets");
        p.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName());
        p.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName());
        // create topology
        StreamsBuilder builder = new StreamsBuilder();
        // input
        KStream<String, String> input = builder.stream("twitter_status_connect");
        KStream<String, String> filtered = input.filter((k, tweet) -> extractFollowers(tweet) > 1);
        filtered.to("filtered_tweets");
        // build topology
        KafkaStreams stream = new KafkaStreams(builder.build(), p);
        // start
        stream.start();
        Runtime.getRuntime().addShutdownHook(new Thread(stream::close));
    }

    private static JsonParser parser = new JsonParser();

    private static Integer extractFollowers(String tweet) {
        try {
            return parser.parse(tweet)
                    .getAsJsonObject()
                    .get("payload")
                    .getAsJsonObject()
                    .get("User")
                    .getAsJsonObject()
                    .get("FollowersCount")
                    .getAsInt();
        } catch (NullPointerException e) {
            return 0;
        }
    }
}
