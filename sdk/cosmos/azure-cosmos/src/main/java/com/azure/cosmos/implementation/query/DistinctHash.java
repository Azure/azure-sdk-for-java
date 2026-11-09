// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.routing.MurmurHash3_128;
import com.azure.cosmos.implementation.routing.UInt128;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class DistinctHash {

    private static final UInt128 ROOT_HASH_SEED = new UInt128(0xbfc2359eafc0e2b7L, 0x8846e00284c4cf1fL);

    private static class HashSeeds {
        public static final UInt128 NULL = new UInt128(0x1380f68bb3b0cfe4L, 0x156c918bf564ee48L);
        public static final UInt128 FALSE = new UInt128(0xc1be517fe893b40cL, 0xe9fc8a4c531cd0ddL);
        public static final UInt128 TRUE = new UInt128(0xf86d4abf9a412e74L, 0x788488365c8a985dL);
        public static final UInt128 STRING = new UInt128(0x61f53f0a44204cfbL, 0x09481be8ef4b56ddL);
        public static final UInt128 ARRAY = new UInt128(0xfa573b014c4dc18eL, 0xa014512c858eb115L);
        public static final UInt128 OBJECT = new UInt128(0x77b285ac511aef30L, 0x3dcf187245822449L);
        public static final UInt128 INTEGER = new UInt128(0x0320dc908e0d3e71L, 0xf575de218f09ffa5L);
        public static final UInt128 LONG = new UInt128(0xed93baf7fdc76638L, 0x0d5733c37e079869L);
        public static final UInt128 DOUBLE = new UInt128(0x62fb48cc659963a0L, 0xe9e690779309c403L);
        public static final UInt128 ARRAY_INDEX = new UInt128(0xfe057204216db999L, 0x5b1cc3178bd9c593L);
        public static final UInt128 PROPERTY_NAME = new UInt128(0xc915dde058492a8aL, 0x7c8be2eba72e4634L);
    }

    @SuppressWarnings("unchecked")
    public static UInt128 getHash(Object resource) throws IOException {
        return getHash(resource, ROOT_HASH_SEED);
    }

    @SuppressWarnings("unchecked")
    private static UInt128 getHash(Object resource, UInt128 seed) throws IOException {
        if (resource == null) {
            return MurmurHash3_128.hash128(HashSeeds.NULL, seed);
        }
        if (resource instanceof JsonSerializable) {
            return getHashFromJsonSerializable((JsonSerializable) resource, seed);
        }
        if (resource instanceof List) {
            return getHashFromIterator(((List) resource).iterator(), seed);
        }
        if (resource instanceof Boolean) {
            return (Boolean)resource ? MurmurHash3_128.hash128(HashSeeds.TRUE, seed) : MurmurHash3_128.hash128(HashSeeds.FALSE, seed);
        }
        if (resource instanceof Integer) {
            UInt128 hash = MurmurHash3_128.hash128(HashSeeds.INTEGER, seed);
            return MurmurHash3_128.hash128(resource, hash);
        }
        if (resource instanceof Long) {
            UInt128 hash = MurmurHash3_128.hash128(HashSeeds.LONG, seed);
            return MurmurHash3_128.hash128(resource, hash);
        }
        if (resource instanceof Double) {
            UInt128 hash = MurmurHash3_128.hash128(HashSeeds.DOUBLE, seed);
            return MurmurHash3_128.hash128(resource, hash);
        }
        if (resource instanceof String) {
            UInt128 hash = MurmurHash3_128.hash128(HashSeeds.STRING, seed);
            return MurmurHash3_128.hash128(resource, hash);
        }
        if (resource instanceof NullNode) {
            return getHash(null, seed);
        }
        if (resource instanceof ValueNode) {
            return getHash(JsonSerializable.getValue((JsonNode) resource), seed);
        }
        if (resource instanceof ArrayNode) {
            return getHashFromIterator(((ArrayNode) resource).iterator(), seed);
        }
        if (resource instanceof ObjectNode) {
            return getHashFromObjectNode((ObjectNode) resource, seed);
        }

        throw new IllegalArgumentException(String.format("Unexpected type: %s", resource.getClass().toString()));
    }

    private static UInt128 getHashFromJsonSerializable(JsonSerializable resource, UInt128 seed) throws IOException {
        resource.populatePropertyBag();
        return getHash(resource.getPropertyBag(), seed);
    }

    private static UInt128 getHashFromObjectNode(ObjectNode objectNode, UInt128 seed) throws IOException {
        UInt128 hash = MurmurHash3_128.hash128(HashSeeds.OBJECT, seed);
        UInt128 intermediateHash = UInt128.ZERO;

        // Property order should not result in a different hash.
        // This is consistent with equality comparison.
        Iterator<Map.Entry<String, JsonNode>> children = objectNode.fields();
        while ((children.hasNext())) {
            Map.Entry<String, JsonNode> child = children.next();
            UInt128 nameHash = MurmurHash3_128.hash128(child.getKey(), HashSeeds.PROPERTY_NAME);
            UInt128 propertyHash = getHash(child.getValue(), nameHash);
            intermediateHash = intermediateHash.xor(propertyHash);
        }

        if (!intermediateHash.equals(UInt128.ZERO)) {
            hash = MurmurHash3_128.hash128(intermediateHash, hash);
        }

        return hash;
    }

    private static <T extends Object>UInt128 getHashFromIterator (Iterator<T> iterator, UInt128 seed) throws IOException {
        UInt128 hash = MurmurHash3_128.hash128(HashSeeds.ARRAY, seed);

        int index = 0;
        while (iterator.hasNext()) {
            // index matters
            UInt128 arrayItemSeed = HashSeeds.ARRAY_INDEX.add(index);
            hash = MurmurHash3_128.hash128(hash, getHash(iterator.next(), arrayItemSeed));
        }

        return hash;
    }
}
