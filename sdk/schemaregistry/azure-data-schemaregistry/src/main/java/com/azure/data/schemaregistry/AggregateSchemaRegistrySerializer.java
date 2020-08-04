package com.azure.data.schemaregistry;

import com.azure.core.experimental.serializer.ObjectSerializer;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.schemaregistry.models.SerializationType;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

public class AggregateSchemaRegistrySerializer implements ObjectSerializer {
    private final ClientLogger logger = new ClientLogger(AggregateSchemaRegistrySerializer.class);

    private SchemaRegistryAsyncClient schemaRegistryClient;
    private SchemaRegistrySerializer serializer;
    private Map<String, SchemaRegistrySerializer> deserializerMap =
        new ConcurrentSkipListMap<>(String.CASE_INSENSITIVE_ORDER);

    public AggregateSchemaRegistrySerializer(SchemaRegistrySerializer serializer,
                                             List<SchemaRegistrySerializer> deserializerList) {
        this.serializer = serializer;
        this.schemaRegistryClient = serializer.schemaRegistryClient;
        for (SchemaRegistrySerializer d : deserializerList) {
            if (this.deserializerMap.containsKey(d.getSerializationType())) {
                throw logger.logExceptionAsError(
                    new IllegalArgumentException("Only on Codec can be provided per schema serialization type."));
            }
            this.deserializerMap.put(d.getSerializationType().toString(), d);
        }
    }

    @Override
    public <T> Mono<T> deserialize(InputStream s, Class<T> clazz) {
        return Mono.fromCallable(() -> {
            byte[] payload = new byte[s.available()];
            while (s.read(payload) != -1) {}
            return payload;
        }).flatMap(payload -> {
            if (payload == null || payload.length == 0) {
                return Mono.empty();
            }

            ByteBuffer buffer = ByteBuffer.wrap(payload);
            byte[] schemaGuidByteArray = new byte[SchemaRegistrySerializer.SCHEMA_ID_SIZE];
            buffer.get(schemaGuidByteArray);

            String schemaId = new String(schemaGuidByteArray, StandardCharsets.UTF_8);

            return this.schemaRegistryClient.getSchema(schemaId)
//                    .onErrorMap(IOException.class,
//                        e -> logger.logExceptionAsError(new SerializationException(e.getMessage(), e)))
                .handle((registryObject, sink) -> {
                    Object payloadSchema = registryObject.getSchema();

                    if (payloadSchema == null) {
                        sink.error(logger.logExceptionAsError(
                            new NullPointerException(
                                String.format("Payload schema returned as null. Schema type: %s, Schema ID: %s",
                                    registryObject.getSerializationType(), registryObject.getSchemaId()))));
                        return;
                    }

                    int start = buffer.position() + buffer.arrayOffset();
                    int length = buffer.limit() - SchemaRegistrySerializer.SCHEMA_ID_SIZE;
                    byte[] b = Arrays.copyOfRange(buffer.array(), start, start + length);

                    SchemaRegistrySerializer innerDeserializer = getDeserializer(registryObject.getSerializationType());
                    sink.next((T) innerDeserializer.decode(b, payloadSchema));
                });
        });
    }

    @Override
    public <S extends OutputStream> Mono<S> serialize(S s, Object value) {
        return serializer.serializeAsync(s, value);
    }

    private SchemaRegistrySerializer getDeserializer(SerializationType type) {
        SchemaRegistrySerializer serializer = deserializerMap.get(type);
        if (serializer == null) {
            throw logger.logExceptionAsError(
                new NullPointerException(
                    String.format("No deserializer class found for serialization type '%s'.", type)
                ));
        }
        return serializer;
    }
}
