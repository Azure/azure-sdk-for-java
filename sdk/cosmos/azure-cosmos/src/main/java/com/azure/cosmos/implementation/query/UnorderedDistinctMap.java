// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.Resource;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class UnorderedDistinctMap extends DistinctMap {
    // This is intended to be used as a concurrent hash set only
    private final Set<String> resultSet;


    public UnorderedDistinctMap() {
        resultSet = Collections.newSetFromMap(new ConcurrentHashMap<>());
    }

    @Override
    public boolean add(Resource resource, Utils.ValueHolder<String> outHash) {
        try {
            outHash.v = getHash(resource);
            return resultSet.add(outHash.v);
        } catch (JsonProcessingException | NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
