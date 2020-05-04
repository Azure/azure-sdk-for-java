// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.Resource;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.azure.cosmos.implementation.routing.UInt128;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class OrderedDistinctMap extends DistinctMap {
    private volatile UInt128 lastHash;

    public OrderedDistinctMap(UInt128 lastHash) {
        this.lastHash = lastHash;
    }

    @Override
    public boolean add(Resource resource, Utils.ValueHolder<UInt128> outHash) {
        try {
            outHash.v = DistinctHash.getHash(resource);
            // value should be true if hashes are not equal
//            final boolean value = !StringUtils.equals(lastHash, outHash.v);
            boolean value = true;
            if (lastHash != null) {
                value = !(outHash.v.equals(lastHash));
            }
            lastHash = outHash.v;
            return value;
        } catch (JsonProcessingException | NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
