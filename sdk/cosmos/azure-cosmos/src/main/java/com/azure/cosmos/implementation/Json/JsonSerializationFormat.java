package com.azure.cosmos.implementation.Json;

/// <summary>
/// Defines JSON different serialization Formats
/// </summary>
/// <remarks>
/// Every enumeration type has an underlying type, which can be any integral type except char.
/// The default underlying type of enumeration elements is integer.
/// To declare an enum of another integral type, such as byte, use a colon after the identifier followed by the type, as shown in the following example.
/// </remarks>
public enum JsonSerializationFormat
{
    /// <summary>
    /// Plain text
    /// </summary>
    Text((byte)0),

    /// <summary>
    /// Binary Encoding
    /// </summary>
    Binary((byte)128),

    /// <summary>
    /// HybridRow Binary Encoding
    /// </summary>
    HybridRow((byte)129);

    // All other format values need to be > 127,
    // otherwise a valid JSON starting character (0-9, f[alse], t[rue], n[ull],{,[,") might be interpreted as a serialization format.

    private byte value;

    private JsonSerializationFormat(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }
}
