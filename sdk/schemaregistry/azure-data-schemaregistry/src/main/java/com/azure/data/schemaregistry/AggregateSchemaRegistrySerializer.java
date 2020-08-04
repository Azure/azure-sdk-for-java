package com.azure.data.schemaregistry;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.TypeReference;
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
    public <T> T deserialize(InputStream stream, TypeReference<T> typeReference) {
        return deserializeAsync(stream, typeReference).block();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Mono<T> deserializeAsync(InputStream stream, TypeReference<T> typeReference) {
        if (Object.class.equals(typeReference.getJavaType())) {

        }
        return Mono.fromCallable(() -> {
            byte[] payload = new byte[stream.available()];
            while (stream.read(payload) != -1) {}
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
                    Object deserialized = innerDeserializer.decode(b, payloadSchema);
                    sink.next((T)deserialized);
                });
        });
    }

    @Override
    public <S extends OutputStream> S serialize(S stream, Object value) {
        return serializer.serializeAsync(stream, value).block();
    }

    @Override
    public <S extends OutputStream> Mono<S> serializeAsync(S stream, Object value) {
        return serializer.serializeAsync(stream, value);
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
