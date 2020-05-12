// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.implementation.Resource;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public abstract class DistinctMap {
    private static final ObjectMapper OBJECT_MAPPER =
        new ObjectMapper().configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

    public static DistinctMap create(DistinctQueryType distinctQueryType, String previousHash) {
        switch (distinctQueryType) {
            case NONE:
                throw new IllegalArgumentException("distinct query type cannot be None");
            case UNORDERED:
                return new UnorderedDistinctMap();
            case ORDERED:
                return new OrderedDistinctMap(previousHash);
            default:
                throw new IllegalArgumentException("Unrecognized DistinctQueryType");
        }
    }

    public abstract boolean add(Resource object, Utils.ValueHolder<String> outHash);

    String getHash(Resource resource) throws JsonProcessingException,
                                                 NoSuchAlgorithmException {
        final Object obj = OBJECT_MAPPER.treeToValue(ModelBridgeInternal.getPropertyBagFromJsonSerializable(resource)
            , Object.class);
        final String sortedJson =
            OBJECT_MAPPER.writeValueAsString(obj);
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] digest = md.digest(sortedJson.getBytes(Charset.defaultCharset()));
        return Base64.getEncoder().encodeToString(digest);
    }

}
