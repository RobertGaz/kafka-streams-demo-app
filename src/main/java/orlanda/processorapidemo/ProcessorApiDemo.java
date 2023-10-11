package orlanda.processorapidemo;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;

import java.util.Properties;

public class ProcessorApiDemo {
    private static final String INPUT_TOPIC = "topic_a";
    private static final String OUTPUT_TOPIC = "topic_a_characters_count";

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "localhost:9092");
        properties.setProperty("application.id", "kafka-streams-demo");

        Topology topology = new Topology();
        topology.addSource("source-node", Serdes.String().deserializer(), Serdes.String().deserializer(),
                INPUT_TOPIC);

        final String storeName = "charactersCountStore";
        topology.addProcessor("characters-count-processor", new CharactersCountProcessorSupplier(storeName), "source-node");

        topology.addSink("sink-node", OUTPUT_TOPIC, Serdes.String().serializer(), Serdes.Long().serializer(), "characters-count-processor");

        KafkaStreams kafkaStreams = new KafkaStreams(topology, properties);
        kafkaStreams.start();
    }
}
