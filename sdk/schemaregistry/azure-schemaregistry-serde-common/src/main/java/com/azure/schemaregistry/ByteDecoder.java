/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.azure.schemaregistry;

public interface ByteDecoder extends Codec {
    /**
     * Decodes byte array into Object given provided schema object.
     * @param encodedBytes payload to be decoded
     * @param schemaObject object used to decode the payload
     * @return deserialized object
     * @throws SerializationException
     */
    public Object decodeBytes(byte[] encodedBytes, Object schemaObject) throws SerializationException;
}
