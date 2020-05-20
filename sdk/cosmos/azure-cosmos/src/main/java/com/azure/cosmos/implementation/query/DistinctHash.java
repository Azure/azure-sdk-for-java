// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.routing.MurmurHash3_128;
import com.azure.cosmos.implementation.routing.UInt128;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

public class DistinctHash {

    private static final UInt128 ArrayHashSeed = new UInt128(0xfa573b014c4dc18eL, 0xa014512c858eb115L);

    private static final ObjectMapper OBJECT_MAPPER =
        new ObjectMapper()
            .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

    @SuppressWarnings("unchecked")
    public static UInt128 getHash(Object resource) throws IOException {

        if (resource instanceof List) {
            return hashList((List<Object>) resource);
        }

        if (resource instanceof JsonSerializable) {
            return getHashFromJsonSerializable((JsonSerializable) resource);
        }

        final byte[] bytes = Utils.serializeObjectToByteArray(resource);
        UInt128 uInt128 = MurmurHash3_128.hash128(bytes, bytes.length);
        return uInt128;
    }

    private static UInt128 getHashFromJsonSerializable(JsonSerializable resource) {
        final ByteBuffer byteBuffer = ModelBridgeInternal.serializeJsonToByteBuffer(resource, OBJECT_MAPPER);
        final byte[] bytes = byteBuffer.array();
        return MurmurHash3_128.hash128(bytes, bytes.length);
    }

    private static UInt128 hashList(List<Object> resource) {
        UInt128 hash = ArrayHashSeed;
        for (Object obj : resource) {
            if (obj instanceof JsonSerializable) {
                byte[] bytes = hash.toByteBuffer().array();
                if (bytes.length == 0) {
                    throw new IllegalStateException("Failed to hash!");
                }
                hash = MurmurHash3_128.hash128(bytes, bytes.length,
                                               getHashFromJsonSerializable((JsonSerializable) obj));
            }
        }
        return hash;
    }
}
