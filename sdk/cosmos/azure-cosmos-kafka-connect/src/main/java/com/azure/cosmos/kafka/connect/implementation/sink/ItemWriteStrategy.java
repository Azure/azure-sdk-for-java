// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.sink;

public enum ItemWriteStrategy {
    ITEM_OVERWRITE("ItemOverwrite"),
    ITEM_APPEND("ItemAppend"),
    ITEM_DELETE("ItemDelete"),
    ITEM_DELETE_IF_NOT_MODIFIED("ItemDeleteIfNotModified"),
    ITEM_OVERWRITE_IF_NOT_MODIFIED("ItemOverwriteIfNotModified"),
    ITEM_PATCH("ItemPatch");

    // TODO[GA] Add ItemBulkUpdate
    private final String name;

    ItemWriteStrategy(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static ItemWriteStrategy fromName(String name) {
        for (ItemWriteStrategy mode : ItemWriteStrategy.values()) {
            if (mode.getName().equalsIgnoreCase(name)) {
                return mode;
            }
        }
        return null;
    }
}
