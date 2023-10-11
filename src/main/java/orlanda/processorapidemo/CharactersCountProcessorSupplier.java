package orlanda.processorapidemo;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;

public class CharactersCountProcessorSupplier implements ProcessorSupplier<String, String, String, Long> {
    private String storeName;

    public CharactersCountProcessorSupplier(String storeName) {
        this.storeName = storeName;
    }

    @Override
    public Processor<String, String, String, Long> get() {
        return new Processor<String, String, String, Long>() {
            private KeyValueStore<String, Long> store;

            @Override
            public void init(ProcessorContext<String, Long> context) {
                this.store = context.getStateStore(storeName);

                //задаем punctuation - то, что будет отправлять результат аггрегации
                context.schedule(Duration.ofSeconds(5), PunctuationType.WALL_CLOCK_TIME,
                        timestamp -> {

                            KeyValueIterator<String, Long> iterator = store.all();
                            while (iterator.hasNext()) {
                                KeyValue<String, Long> keyValue = iterator.next();
                                Record<String, Long> outputRecord = new Record<>(keyValue.key, keyValue.value, timestamp);
                                // forward record to child nodes
                                context.forward(outputRecord);
                            }
                            iterator.close();
                        });
            }

            //сама аггрегация
            @Override
            public void process(Record<String, String> record) {
                String key = record.key();
                Long currentCharactersCount = store.get(key);
                if (currentCharactersCount == null) {
                    currentCharactersCount = 0L;
                }
                store.put(key, currentCharactersCount + record.value().length());
            }
        };
    }

    @Override
    public Set<StoreBuilder<?>> stores() {
        StoreBuilder<KeyValueStore<String, Long>> storeBuilder = Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore(storeName),
                Serdes.String(),
                Serdes.Long()
        );
        return Collections.singleton(storeBuilder);
    }
}
