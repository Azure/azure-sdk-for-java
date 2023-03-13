package com.azure.cosmos.implementation.Json;


import java.nio.ByteBuffer;

/// <summary>
/// Interface for all TypedJsonReaders that know how to read typed json.
/// </summary>
public interface ITypedJsonReader extends IJsonReader
{
    /// <summary>
    /// Attempt to read a '$t': TYPECODE, '$v' in one call.
    /// If unsuccessful, the reader is left in its original state.
    /// Otherwise, it is positioned at the value after the $v.
    /// </summary>
    /// <param name="typeCode">The type code read.</param>
    /// <returns>Success.</returns>
    boolean tryReadTypedJsonValueWrapper(int typeCode);

    /// <summary>
    /// Gets the next JSON token from the JsonReader as a UTF-8 span.
    /// </summary>
    /// <returns>The next JSON token from the JsonReader as a span.</returns>
    ByteBuffer getUtf8SpanValue();
}
