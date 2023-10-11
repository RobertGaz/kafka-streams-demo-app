package orlanda;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Properties;

public class AggregationDemo {
    private static final String INPUT_TOPIC = "topic_a";
    private static final String OUTPUT_TOPIC = "topic_a_characters_count";

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "localhost:9092");
        properties.setProperty("application.id", "kafka-streams-demo");

        StreamsBuilder streamsBuilder = new StreamsBuilder();

        KStream<String, String> kStream = streamsBuilder.stream(INPUT_TOPIC, Consumed.with(Serdes.String(), Serdes.String()));

        kStream.peek((key, value) -> System.out.println("[input peek] key: " + key + ", value: " + value))
                .groupByKey().aggregate(() -> 0L, (key, value, totalChars) -> totalChars + value.length(), Materialized.with(Serdes.String(), Serdes.Long()))
                .toStream()
                .peek((key, value) -> System.out.println("[output peek] key: " + key + ", value: " + value))
                .to(OUTPUT_TOPIC, Produced.with(Serdes.String(), Serdes.Long()));

        KafkaStreams kafkaStreams = new KafkaStreams(streamsBuilder.build(), properties);
        kafkaStreams.start();

    }
}
