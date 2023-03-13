package com.azure.cosmos.implementation.Json;

import java.nio.ByteBuffer;

public interface IJsonBinaryWriterExtensions extends IJsonWriter
{
    void writeRawJsonValue(
        ByteBuffer rootBuffer,
        ByteBuffer rawJsonValue,
        boolean isRootNode,
        boolean isFieldName);
}
