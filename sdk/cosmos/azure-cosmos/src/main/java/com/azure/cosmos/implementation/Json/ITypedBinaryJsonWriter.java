package com.azure.cosmos.implementation.Json;

/// <summary>
/// Interface for all TypedBinaryJsonWriter that know how to write typed binary JSON.
/// </summary>
public interface ITypedBinaryJsonWriter extends IJsonWriter
{
    /// <summary>
    /// Writes a pre-blitted binary JSON scope.
    /// </summary>
    /// <param name="scope">Scope to write.</param>
    void write(PreblittedBinaryJsonScope scope);

    /// <summary>
    /// Writes a "{ $t: cosmosBsonType, $v: " snippet.
    /// </summary>
    /// <param name="cosmosBsonTypeByte">Cosmos BSON type.</param>
    void writeDollarTBsonTypeDollarV(byte cosmosBsonTypeByte);

    /// <summary>
    /// Writes a "{ $t: cosmosBsonType, $v: {" snippet (or "{ $t: cosmosBsonType, $v: [" if array).
    /// </summary>
    /// <param name="isNestedArray">Indicates whether the nested scope should be an array.</param>
    /// <param name="cosmosBsonTypeByte">Cosmos BSON type.</param>
    void writeDollarTBsonTypeDollarVNestedScope(boolean isNestedArray, byte cosmosBsonTypeByte);
}
