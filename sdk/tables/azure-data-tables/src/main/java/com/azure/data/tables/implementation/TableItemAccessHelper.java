// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.data.tables.implementation.models.TableResponseProperties;
import com.azure.data.tables.models.TableItem;

import java.util.function.Function;

/**
 * Helper class to access internal values of {@link TableItem}.
 */
public final class TableItemAccessHelper {
    private static Function<TableResponseProperties, TableItem> creator;

    /**
     * Sets the {@link TableItem} creator.
     *
     * @param creator The {@link TableItem} creator.
     */
    public static void setTableItemCreator(Function<TableResponseProperties, TableItem> creator) {
        TableItemAccessHelper.creator = creator;
    }

    /**
     * Creates a {@link TableItem}.
     *
     * @param properties The {@link TableResponseProperties} used to construct the table.
     * @return The created {@link TableItem}.
     */
    public static TableItem createItem(TableResponseProperties properties) {
        if (creator == null) {
            // Since the access helper needs to access a constructor the class needs to be initialized first.
            try {
                Class.forName(TableItem.class.getName(), true, TableItemAccessHelper.class.getClassLoader());
            } catch (ReflectiveOperationException ex) {
                throw new ClientLogger(TableItemAccessHelper.class).logExceptionAsError(
                    new IllegalStateException("Failed to load 'TableItem' class within 'TableItemAccessHelper'."));
            }
        }

        assert creator != null;
        return creator.apply(properties);
    }

    private TableItemAccessHelper() {
    }
}
