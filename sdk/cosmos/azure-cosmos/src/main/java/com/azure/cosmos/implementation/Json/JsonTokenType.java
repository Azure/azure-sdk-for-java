package com.azure.cosmos.implementation.Json;

public enum JsonTokenType
{
    /// <summary>
    /// Reserved for no other value
    /// </summary>
    NotStarted,

    /// <summary>
    /// Corresponds to the beginning of a JSON array ('[')
    /// </summary>
    BeginArray,

    /// <summary>
    /// Corresponds to the end of a JSON array (']')
    /// </summary>
    EndArray,

    /// <summary>
    /// Corresponds to the beginning of a JSON object ('{')
    /// </summary>
    BeginObject,

    /// <summary>
    /// Corresponds to the end of a JSON object ('}')
    /// </summary>
    EndObject,

    /// <summary>
    /// Corresponds to a JSON string.
    /// </summary>
    String,

    /// <summary>
    /// Corresponds to a JSON number.
    /// </summary>
    Number,

    /// <summary>
    /// Corresponds to the JSON 'true' value.
    /// </summary>
    True,

    /// <summary>
    /// Corresponds to the JSON 'false' value.
    /// </summary>
    False,

    /// <summary>
    /// Corresponds to the JSON 'null' value.
    /// </summary>
    Null,

    /// <summary>
    /// Corresponds to the JSON field name in a JSON object.
    /// </summary>
    FieldName,

    /// <summary>
    /// Corresponds to a signed 1 byte integer.
    /// </summary>
    Int8,

    /// <summary>
    /// Corresponds to a signed 2 byte integer.
    /// </summary>
    Int16,

    /// <summary>
    /// Corresponds to a signed 4 byte integer.
    /// </summary>
    Int32,

    /// <summary>
    /// Corresponds to a signed 8 byte integer.
    /// </summary>
    Int64,

    /// <summary>
    /// Corresponds to an unsigned 4 byte integer
    /// </summary>
    UInt32,

    /// <summary>
    /// Corresponds to a single precision floating point.
    /// </summary>
    Float32,

    /// <summary>
    /// Corresponds to a double precision floating point.
    /// </summary>
    Float64,

    /// <summary>
    /// Corresponds to a GUID.
    /// </summary>
    Guid,

    /// <summary>
    /// Corresponds to an arbitrary sequence of bytes in an object.
    /// </summary>
    Binary,
}
