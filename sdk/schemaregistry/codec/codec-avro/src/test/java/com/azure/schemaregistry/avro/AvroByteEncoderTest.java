/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.azure.schemaregistry.avro;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AvroByteEncoderTest extends TestCase {
    private static final String MOCK_AVRO_SCHEMA_STRING = "{\"namespace\":\"example2.avro\",\"type\":\"record\",\"name\":\"User\",\"fields\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"favorite_number\",\"type\": [\"int\", \"null\"]}]}";
    private static final Character namespaceDelimiter = '/';

    public AvroByteEncoderTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(AvroByteEncoderTest.class);
    }

    protected void setUp() {
    }

    protected void tearDown() {
    }

    public void testPlaceholder() {
        getEncoder();
        assertTrue(true);
    }

    private AvroByteEncoder getEncoder() {
        return new AvroByteEncoder.Builder().build();
    }
}
