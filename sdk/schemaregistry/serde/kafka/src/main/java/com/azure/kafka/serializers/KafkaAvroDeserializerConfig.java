/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.azure.kafka.serializers;

public class KafkaAvroDeserializerConfig extends AbstractKafkaSerdeConfig {

    /**
     * Configures deserializer to look up and decode into SpecificRecord class instance when reading encoded bytes
     */
    public static String AVRO_SPECIFIC_READER_CONFIG = "specific.avro.reader";
}
