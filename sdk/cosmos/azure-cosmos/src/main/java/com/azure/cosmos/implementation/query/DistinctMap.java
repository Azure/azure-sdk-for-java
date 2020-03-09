// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.Utils;

public abstract class DistinctMap {

    public static DistinctMap create(DistinctQueryType distinctQueryType, String previousHash) {
        switch (distinctQueryType) {
            case None:
                throw new IllegalArgumentException("distinct query type cannot be None");
            case Unordered:
                return new UnorderedDistinctMap();
            case Ordered:
                return new OrderedDistinctMap(previousHash);
            default:
                throw new IllegalArgumentException("Unrecognized DistinctQueryType");
        }
    }

    public abstract boolean add(Object object, Utils.ValueHolder<String> outHash);

}
