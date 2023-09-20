// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.internal.avro.implementation;

import com.azure.storage.internal.avro.implementation.schema.AvroSchema;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

import static com.azure.core.test.utils.TestUtils.assertArraysEqual;

public class AvroSchemaTests {
    @Test
    public void getBytes() {
        ByteBuffer b1 = ByteBuffer.wrap("Hello ".getBytes());
        ByteBuffer b2 = ByteBuffer.wrap("World!".getBytes());
        LinkedList<ByteBuffer> buffers = new LinkedList<>();
        buffers.add(b1);
        buffers.add(b2);

        assertArraysEqual("Hello World!".getBytes(StandardCharsets.UTF_8), AvroSchema.getBytes(buffers));
    }
}
