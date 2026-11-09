// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.routing.UInt128;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public abstract class DistinctMap {

    private static final ObjectMapper OBJECT_MAPPER =
        new ObjectMapper().configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

    public static DistinctMap create(DistinctQueryType distinctQueryType, UInt128 previousHash) {
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

    public abstract boolean add(Object object, Utils.ValueHolder<UInt128> outHash);

    String getSortedJsonStringValueFromResource(Resource resource) {
        final Object obj;
        try {
            obj = OBJECT_MAPPER.treeToValue(resource.getPropertyBag(), Object.class);
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to obtain serialized sorted json");
        }
    }

}
