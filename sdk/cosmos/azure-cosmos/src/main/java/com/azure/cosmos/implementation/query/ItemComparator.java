// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.routing.UInt128;

import java.io.IOException;
import java.io.Serializable;
import java.util.Comparator;

public final class ItemComparator implements Comparator<Object>, Serializable {
    private static final long serialVersionUID = 3597176477260908315L;

    private ItemComparator() {
    }

    private static class SingletonHelper {
        private static final ItemComparator INSTANCE = new ItemComparator();
    }

    public static ItemComparator getInstance() {
        return SingletonHelper.INSTANCE;
    }

    @Override
    public int compare(Object obj1, Object obj2) {
        ItemType type1 = ItemTypeHelper.getOrderByItemType(obj1);
        ItemType type2 = ItemTypeHelper.getOrderByItemType(obj2);

        int cmp = Integer.compare(type1.getVal(), type2.getVal());

        if (cmp != 0) {
            return cmp;
        }

        switch (type1) {
        case NoValue:
        case Null:
            return 0;
        case Boolean:
            return Boolean.compare((Boolean) obj1, (Boolean) obj2);
        case Number:
            return Double.compare(((Number) obj1).doubleValue(), ((Number) obj2).doubleValue());
        case String:
            return ((String) obj1).compareTo((String) obj2);
        case ArrayNode:
        case ObjectNode:
            try{
                UInt128 hash1 = DistinctHash.getHash(obj1);
                UInt128 hash2 = DistinctHash.getHash(obj2);
                return hash1.compareTo(hash2);
            }
            catch (IOException e) {
                throw new IllegalStateException(String.format("Getting hash exception for type %s ", type1.toString()), e);
            }

        default:
            throw new ClassCastException(String.format("Unexpected type: %s", type1.toString()));
        }
    }
}
