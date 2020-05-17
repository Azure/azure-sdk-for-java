// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.Resource;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.security.NoSuchAlgorithmException;

public class OrderedDistinctMap extends DistinctMap {
    private volatile String lastHash;

    public OrderedDistinctMap(String lastHash) {
        this.lastHash = lastHash;
    }

    @Override
    public boolean add(Resource resource, Utils.ValueHolder<String> outHash) {
        try {
            outHash.v = getHash(resource);
            // value should be true if hashes are not equal
            final boolean value = !StringUtils.equals(lastHash, outHash.v);
            lastHash = outHash.v;
            return value;
        } catch (JsonProcessingException | NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
