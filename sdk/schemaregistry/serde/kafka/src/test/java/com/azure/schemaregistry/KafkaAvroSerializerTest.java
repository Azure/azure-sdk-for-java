/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.azure.schemaregistry;

import com.azure.kafka.serializers.KafkaAvroSerializer;
import com.azure.kafka.serializers.KafkaAvroSerializerConfig;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.HashMap;
import java.util.Map;

public class KafkaAvroSerializerTest extends TestCase {

    public KafkaAvroSerializerTest( String testName )
    {
        super( testName );
    }

    public static Test suite()
    {
        return new TestSuite( KafkaAvroSerializerTest.class );
    }

    public void testMinimalConfigShouldNotLeaveNullRequiredFields()
    {
        KafkaAvroSerializer s = new KafkaAvroSerializer();
        Map<String, Object> props = new HashMap<>();
        props.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "dummy.servicebus.windows.net");
        s.configure(props, false);
        assertEquals(AbstractDataSerializer.AUTO_REGISTER_SCHEMAS_DEFAULT, s.autoRegisterSchemas);
        assertEquals(AbstractDataSerializer.SCHEMA_GROUP_DEFAULT, s.schemaGroup);
        assertEquals(s.byteEncoder.serializationFormat(), s.serializationFormat);
    }
}
