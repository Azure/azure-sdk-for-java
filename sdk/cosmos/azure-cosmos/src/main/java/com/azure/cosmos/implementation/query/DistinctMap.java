// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.routing.MurmurHash3_128;
import com.azure.cosmos.implementation.routing.UInt128;
import com.azure.cosmos.models.JsonSerializable;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.implementation.Resource;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public abstract class DistinctMap {

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

    public abstract boolean add(Resource object, Utils.ValueHolder<UInt128> outHash);

}
