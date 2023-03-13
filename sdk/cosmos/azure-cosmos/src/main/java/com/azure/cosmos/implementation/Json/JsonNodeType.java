package com.azure.cosmos.implementation.Json;

public enum JsonNodeType
{
    /// <summary>
    /// Corresponds to the 'null' value in JSON.
    /// </summary>
    Null,

    /// <summary>
    /// Corresponds to the 'false' value in JSON.
    /// </summary>
    False,

    /// <summary>
    /// Corresponds to the 'true' value in JSON.
    /// </summary>
    True,

    /// <summary>
    /// Corresponds to the number type in JSON (number = [ minus ] integer [ fraction ] [ exponent ])
    /// </summary>
    Number64,

    /// <summary>
    /// Corresponds to the string type in JSON (string = quotation-mark *char quotation-mark)
    /// </summary>
    String,

    /// <summary>
    /// Corresponds to the array type in JSON ( begin-array [ value *( value-separator value ) ] end-array)
    /// </summary>
    Array,

    /// <summary>
    /// Corresponds to the object type in JSON (begin-object [ member *( value-separator member ) ] end-object)
    /// </summary>
    Object,

    /// <summary>
    /// Corresponds to the property name of a JSON object property (which is also a string).
    /// </summary>
    FieldName,

    /// <summary>
    /// Corresponds to the sbyte type in C# for the extended types.
    /// </summary>
    Int8,

    /// <summary>
    /// Corresponds to the short type in C# for the extended types.
    /// </summary>
    Int16,

    /// <summary>
    /// Corresponds to the int type in C# for the extended types.
    /// </summary>
    Int32,

    /// <summary>
    /// Corresponds to the long type in C# for the extended types.
    /// </summary>
    Int64,

    /// <summary>
    /// Corresponds to the uint type in C# for the extended types.
    /// </summary>
    UInt32,

    /// <summary>
    /// Corresponds to the float type in C# for the extended types.
    /// </summary>
    Float32,

    /// <summary>
    /// Corresponds to the double type in C# for the extended types.
    /// </summary>
    Float64,

    /// <summary>
    /// Corresponds to an arbitrary sequence of bytes (equivalent to a byte[] in C#)
    /// </summary>
    Binary,

    /// <summary>
    /// Corresponds to a GUID type in C# for teh extended types.
    /// </summary>
    Guid,

    /// <summary>
    /// Unknown JsonNodeType.
    /// </summary>
    Unknown,
}
