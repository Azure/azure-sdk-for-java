// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.avro;

import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;

public class AvroSchemaRegistryUtilsTest {

    @Mock
    private EncoderFactory encoderFactory;

    @Mock
    private DecoderFactory decoderFactory;

    private AutoCloseable mocksCloseable;

    @BeforeEach
    public void beforeEach() {

    }

    @AfterEach
    public void afterEach() {

    }
}
