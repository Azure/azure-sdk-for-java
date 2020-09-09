// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

/**
 * An interface defining operations required for registry deserializer to convert encoded bytes to Java object.
 */
public interface ByteDecoder extends Codec {
    /**
     * Decodes byte array into Object given provided schema object.
     * @param encodedBytes payload to be decoded
     * @param schemaObject object used to decode the payload
     * @return deserialized object
     * @throws SerializationException if decode operation fails
     */
    Object decodeBytes(byte[] encodedBytes, Object schemaObject) throws SerializationException;
}
