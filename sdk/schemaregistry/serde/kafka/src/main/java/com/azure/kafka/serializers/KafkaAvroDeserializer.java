/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.azure.kafka.serializers;

import com.azure.schemaregistry.AbstractDataDeserializer;
import com.azure.schemaregistry.avro.AvroByteDecoder;
import com.azure.schemaregistry.client.CachedSchemaRegistryClient;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

/**
 * Deserializer implementation for Kafka consumer, implementing Kafka Deserializer interface.
 *
 * Byte arrays are converted into Java objects by using the schema referenced by GUID prefix to deserialize the payload.
 *
 * Receiving Avro GenericRecords and SpecificRecords is supported.  Avro reflection capabilities have been disabled on
 * com.azure.schemaregistry.kafka.KafkaAvroSerializer.
 *
 * @see AbstractDataDeserializer See abstract parent class for implementation details
 * @see KafkaAvroSerializer See serializer class for upstream serializer implementation
 */
public class KafkaAvroDeserializer extends AbstractDataDeserializer
        implements Deserializer<Object> {

    // Constructor used by Kafka consumer.
    public KafkaAvroDeserializer() {
        super();
    }

    /**
     * Configures deserializer instance.
     *
     * @param props Map of properties used to configure instance
     * @param isKey Indicates if deserializing record key or value.  Required by Kafka deserializer interface,
     *              no specific functionality has been implemented for key use.
     *
     * @see KafkaAvroDeserializerConfig Deserializer will use configs found in here and inherited classes.
     */
    @Override
    public void configure(Map<String, ?> props, boolean isKey) {
        String registryUrl = (String) props.get(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG);

        Boolean useAvroSpecificReader = (Boolean) props.get(KafkaAvroDeserializerConfig.AVRO_SPECIFIC_READER_CONFIG);
        AvroByteDecoder.Builder decoderBuilder = new AvroByteDecoder.Builder();
        if (useAvroSpecificReader != null) {
            decoderBuilder = decoderBuilder.avroSpecificReader(useAvroSpecificReader);
        }
        AvroByteDecoder decoder = decoderBuilder.build();
        this.schemaRegistryClient = new CachedSchemaRegistryClient.Builder(registryUrl)
                .loadSchemaParser(decoder.serializationFormat(), (s) -> decoder.parseSchemaString(s))
                .build();

        this.byteDecoderMap.put(decoder.serializationFormat(), decoder);
    }

    /**
     * Deserializes byte array into Java object
     * @param topic topic associated with the record bytes
     * @param bytes serialized bytes, may be null
     * @return deserialize object, may be null
     * @throws SerializationException catchable by core Kafka fetcher code
     */
    @Override
    public Object deserialize(String topic, byte[] bytes) throws SerializationException {
        try {
            return deserialize(bytes);
        } catch (com.azure.schemaregistry.SerializationException e) {
            throw new SerializationException(e.getCause());
        }
    }

    @Override
    public void close() { }
}