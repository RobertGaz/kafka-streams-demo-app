package orlanda;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;

import java.time.Duration;
import java.util.Properties;

public class WindowingDemo {
    private static final String INPUT_TOPIC = "topic_a";
    private static final String OUTPUT_TOPIC = "topic_a_records_count";

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "localhost:9092");
        properties.setProperty("application.id", "kafka-streams-demo");

        StreamsBuilder streamsBuilder = new StreamsBuilder();

        KStream<String, String> kStream = streamsBuilder.stream(INPUT_TOPIC, Consumed.with(Serdes.String(), Serdes.String()));

        kStream.groupByKey()
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofSeconds(5)))
                .count()
                .toStream()
                .map((windowed, value) -> KeyValue.pair(windowed.key(), "count: " + value + " for "+ windowed.window().start() + " - " + windowed.window().end() + " window"))
                .to(OUTPUT_TOPIC, Produced.with(Serdes.String(), Serdes.String()));

        KafkaStreams kafkaStreams = new KafkaStreams(streamsBuilder.build(), properties);
        kafkaStreams.start();

    }
}
