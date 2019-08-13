// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.query;

import com.azure.data.cosmos.internal.Undefined;

public final class ItemTypeHelper {
    public static ItemType getOrderByItemType(Object obj) {
        if (obj == null) {
            return ItemType.Null;
        }

        if (obj instanceof Undefined) {
            return ItemType.NoValue;
        }

        if (obj instanceof Boolean) {
            return ItemType.Boolean;
        }

        if (obj instanceof Number) {
            return ItemType.Number;
        }

        if (obj instanceof String) {
            return ItemType.String;
        }

        throw new IllegalArgumentException(String.format("Unexpected type: %s", obj.getClass().toString()));
    }
}