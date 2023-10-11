package orlanda.abc;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Properties;

public class ABC2 {

    private static final String INPUT_TOPIC = "topic_a";
    private static final String OUTPUT_TOPIC = "topic_b";

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "localhost:9092");
        properties.setProperty("application.id", "kafka-streams-abc");
        properties.setProperty("basic.input.topic", INPUT_TOPIC);
        properties.setProperty("basic.output.topic", OUTPUT_TOPIC);

        StreamsBuilder streamsBuilder = new StreamsBuilder();

        KStream<String, String> kStream = streamsBuilder.stream(INPUT_TOPIC, Consumed.with(Serdes.String(), Serdes.String()));
        kStream.mapValues(value -> "[abc 2] " + value)
                .to(OUTPUT_TOPIC, Produced.with(Serdes.String(), Serdes.String()));

        KafkaStreams kafkaStreams = new KafkaStreams(streamsBuilder.build(), properties);
        kafkaStreams.start();

    }
}