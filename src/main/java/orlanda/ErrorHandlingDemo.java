package orlanda;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.DeserializationExceptionHandler;
import org.apache.kafka.streams.errors.StreamsException;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.processor.ProcessorContext;

import java.util.Map;
import java.util.Properties;

public class ErrorHandlingDemo {
    private static final String INPUT_TOPIC = "topic_a";
    private static final String OUTPUT_TOPIC = "topic_b";

    public static class MyDeserializationExceptionHandler implements DeserializationExceptionHandler {
        int errorCounter = 0;
        @Override
        public DeserializationHandlerResponse handle(ProcessorContext context, ConsumerRecord<byte[], byte[]> record, Exception exception) {
            System.out.println("I see deserialization exception with number " + errorCounter);
            if (errorCounter++ < 5) {
                return DeserializationHandlerResponse.CONTINUE;
            }

            return DeserializationHandlerResponse.FAIL;
        }

        @Override
        public void configure(Map<String, ?> configs) {}
    }

    public static class MyUncaughtExceptionHandler implements StreamsUncaughtExceptionHandler {
        @Override
        public StreamThreadExceptionResponse handle(Throwable exception) {
            System.out.println("I see uncaught exception...");

            // wrapped user exception
            if (exception instanceof StreamsException) {
                Throwable originalException = exception.getCause();
                if (originalException.getMessage().equals("retryable transient error")) {
                    return StreamThreadExceptionResponse.REPLACE_THREAD;
                }
            }

            return StreamThreadExceptionResponse.SHUTDOWN_CLIENT;
        }
    }

    private static boolean exceptionShouldOccur = true;

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "localhost:9092");
        properties.setProperty("application.id", "kafka-streams-demo");
        properties.setProperty(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, MyDeserializationExceptionHandler.class.getName());

        StreamsBuilder streamsBuilder = new StreamsBuilder();



        KStream<String, String> kStream = streamsBuilder.stream(INPUT_TOPIC, Consumed.with(Serdes.String(), Serdes.String()));
        kStream.peek((key, value) -> System.out.println("peeking... key: " + key + ", value: " + value))
                .mapValues(value -> {
            if (exceptionShouldOccur) {
                exceptionShouldOccur = false;
                throw new IllegalStateException("retryable transient error");
            }
            return "[processed] " + value;
        })
        .to(OUTPUT_TOPIC, Produced.with(Serdes.String(), Serdes.String()));

        KafkaStreams kafkaStreams = new KafkaStreams(streamsBuilder.build(), properties);
        kafkaStreams.setUncaughtExceptionHandler(new MyUncaughtExceptionHandler());
        kafkaStreams.start();

    }
}
