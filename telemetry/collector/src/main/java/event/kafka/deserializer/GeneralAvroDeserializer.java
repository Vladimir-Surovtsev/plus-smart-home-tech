package event.kafka.deserializer;

import event.kafka.KafkaTopics;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.util.HashMap;
import java.util.Map;

public class GeneralAvroDeserializer implements Deserializer<SpecificRecordBase> {
    private final DecoderFactory decoderFactory = DecoderFactory.get();
    private final Map<String, SpecificDatumReader<SpecificRecordBase>> readers = new HashMap<>();

    public GeneralAvroDeserializer() {
        readers.put(KafkaTopics.SENSORS, new SpecificDatumReader<>(SensorEventAvro.getClassSchema()));
        readers.put(KafkaTopics.HUBS, new SpecificDatumReader<>(HubEventAvro.getClassSchema()));
    }

    @Override
    public SpecificRecordBase deserialize(String topic, byte[] bytes) {
        try {
            if (bytes != null) {
                BinaryDecoder decoder = decoderFactory.binaryDecoder(bytes, null);
                SpecificDatumReader<SpecificRecordBase> reader = readers.get(topic);

                if (reader == null) {
                    throw new IllegalArgumentException("Неизвестный топик: " + topic);
                }

                return reader.read(null, decoder);
            }
            return null;
        } catch (Exception e) {
            throw new SerializationException("Ошибка десереализации данных из топика [" + topic + "]", e);
        }
    }
}