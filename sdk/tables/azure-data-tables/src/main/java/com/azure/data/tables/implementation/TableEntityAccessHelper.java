// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables.implementation;

import com.azure.data.tables.models.TableEntity;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Helper class to access internal values of {@link TableEntity}.
 */
public final class TableEntityAccessHelper {
    private static Supplier<TableEntity> creator;

    /**
     * Sets the {@link TableEntity} creator.
     *
     * @param creator The {@link TableEntity} creator.
     * @throws IllegalStateException If the creator has already been set.
     */
    public static void setTableEntityCreator(Supplier<TableEntity> creator) {
        TableEntityAccessHelper.creator = creator;
    }

    /**
     * Creates a {@link TableEntity}.
     *
     * @param properties The properties used to construct the {@link TableEntity}.
     * @return The created {@link TableEntity}.
     */
    public static TableEntity createEntity(Map<String, Object> properties) {
        if (creator == null) {
            // Since the access helper needs to access a constructor the class needs to be initialized first.
            new TableEntity("dummyPartitionKey", "dummyRowKey");
        }

        assert creator != null;
        return creator.get().setProperties(properties);
    }

    private TableEntityAccessHelper() {
    }
}
