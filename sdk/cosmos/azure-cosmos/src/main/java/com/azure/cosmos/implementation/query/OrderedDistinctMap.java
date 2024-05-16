// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.routing.UInt128;

import java.io.IOException;

public class OrderedDistinctMap extends DistinctMap {
    private volatile UInt128 lastHash;

    public OrderedDistinctMap(UInt128 lastHash) {
        this.lastHash = lastHash;
    }

    @Override
    public boolean add(Object resource, Utils.ValueHolder<UInt128> outHash) {
        try {
            if (resource instanceof Resource) {
                // We do this to ensure the property order in document should not effect the hash
                resource = getSortedJsonStringValueFromResource((Resource)resource);
            }
            outHash.v = DistinctHash.getHash(resource);
            // value should be true if hashes are not equal
            boolean value = true;
            if (lastHash != null) {
                value = !(outHash.v.equals(lastHash));
            }
            lastHash = outHash.v;
            return value;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to add value to distinct map", e);
        }
    }
}
