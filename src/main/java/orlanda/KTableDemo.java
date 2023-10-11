package orlanda;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;

import java.util.Properties;

public class KTableDemo {
    private static final String INPUT_TOPIC = "topic_a";
    private static final String OUTPUT_TOPIC = "topic_b";

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "localhost:9092");
        properties.setProperty("application.id", "kafka-streams-demo");

        StreamsBuilder streamsBuilder = new StreamsBuilder();

        KTable<String, String> kTable = streamsBuilder.table(INPUT_TOPIC, Consumed.with(Serdes.String(), Serdes.String()));
        kTable.filter((key, value) -> !value.contains("order"))
                .mapValues(value -> "(this value is latest and lives in a KTable 1) " + value)
                .toStream()
                .to(OUTPUT_TOPIC, Produced.with(Serdes.String(), Serdes.String()));

        KafkaStreams kafkaStreams = new KafkaStreams(streamsBuilder.build(), properties);
        kafkaStreams.start();
    }
}
