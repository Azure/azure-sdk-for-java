// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import java.io.Closeable;

/**
 * Writes a JSON encoded value to a stream.
 */
public interface JsonWriter extends Closeable {
    JsonWriter writeStartObject();
    JsonWriter writeEndObject();

    JsonWriter writeStartArray();
    JsonWriter writeEndArray();

    JsonWriter writeBinaryField(String fieldName, byte[] value);
    JsonWriter writeBooleanField(String fieldName, boolean value);
    JsonWriter writeDoubleField(String fieldName, double value);
    JsonWriter writeIntField(String fieldName, int value);
    JsonWriter writeLongField(String fieldName, long value);
    JsonWriter writeNullField(String fieldName);
    JsonWriter writeStringField(String fieldName, String value);
}
