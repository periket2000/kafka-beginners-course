package com.github.periket2000.kafka.streams.map;

import com.github.periket2000.net.utils.Request;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class StreamsMapTweets {
    public static void main(String[] args) {
        // create properties
        Properties p = new Properties();
        p.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:29092");
        p.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "stream-map-tweets");
        p.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName());
        p.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName());
        // create topology
        StreamsBuilder builder = new StreamsBuilder();
        // input
        KStream<String, String> input = builder.stream("filtered_tweets");

        // only spanish/english tweets
        List<String> list = Arrays.asList(new String[] {"en", "es"});
        KStream<String, String> filtered = input.filter((k, tweet) -> list.contains(getIdiom(getTweetText(tweet),
                "http://localhost:8000/lang")));

        // map to new format
        KStream<String, String> mapped = filtered.map((k, tweet) -> {
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

    /**
     * Map a tweet to another json format
     * @param tweet
     * @return
     */
    private static String map(String tweet) {
        try {
            JsonObject source = parser.parse(tweet)
                    .getAsJsonObject()
                    .get("payload")
                    .getAsJsonObject();

            final String schemaStr = "{\"type\":\"struct\",\"fields\":[{\"type\":\"string\",\"optional\":false,\"field\":\"time\"},{\"type\":\"string\",\"optional\":false,\"field\":\"text\"},{\"type\":\"string\",\"optional\":false,\"field\":\"user\"}],\"optional\":false,\"name\":\"tweet-schema\"}";
            JsonObject schema = parser.parse(schemaStr).getAsJsonObject();

            // we should create a json with "schema" and "payload" if we want use it later with a sink jdbc
            JsonObject mappedTweet = new JsonObject();
            mappedTweet.add("schema", new Gson().toJsonTree(schema));

            JsonObject payload = new JsonObject();
            Date d = new Date(Long.parseLong(source.get("CreatedAt").getAsString()));
            String dd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(d);
            payload.addProperty("time", dd);
            payload.addProperty("text", source.get("Text").getAsString());
            payload.addProperty("user", source.get("User").getAsJsonObject().get("Name").getAsString());

            mappedTweet.add("payload", new Gson().toJsonTree(payload));
            return mappedTweet.toString();
        } catch (NullPointerException e) {
            return "{}";
        }
    }

    private static String getTweetText(String tweet) {
        return parser.parse(tweet)
                .getAsJsonObject()
                .get("payload")
                .getAsJsonObject()
                .get("Text").getAsString();
    }

    private static String getIdiom(String sentence, String serviceUrl) {
        try {
            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"sentence\":\"" + sentence + "\"");
            json.append("}");
            String resp = Request.sendPOST(serviceUrl, json.toString());
            JsonElement e = parser.parse(resp);
            return e.getAsJsonObject().get("idiom").getAsString();
        } catch (IOException | NullPointerException e) {
            return "Not known";
        }
    }
}
