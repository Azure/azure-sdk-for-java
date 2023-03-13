package com.azure.cosmos.implementation.Json;

import java.nio.ByteBuffer;

public interface IJsonTextWriterExtensions extends IJsonWriter
{
    void writeRawJsonValue(
        ByteBuffer buffer,
        boolean isFieldName);
}
