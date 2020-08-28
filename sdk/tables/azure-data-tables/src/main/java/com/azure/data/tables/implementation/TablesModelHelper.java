// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.data.tables.implementation.models.TableResponseProperties;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.TableItem;

import java.util.Map;
import java.util.Objects;

/**
 * Used to access internal methods on models.
 */
public final class TablesModelHelper {
    private static EntityCreator entityCreator;
    private static ItemCreator itemCreator;

    static {
        // Force classes' static blocks to execute
        try {
            Class.forName(TableEntity.class.getName(), true, TableEntity.class.getClassLoader());
            Class.forName(TableItem.class.getName(), true, TableItem.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            AssertionError err = new AssertionError("Failed to initialize TablesModelHelper dependency classes.", e);
            new ClientLogger(TablesModelHelper.class).logThrowableAsError(err);
            throw err;
        }
    }

    /**
     * Sets the entity accessor.
     *
     * @param creator The entity accessor.
     * @throws IllegalStateException if the accessor has already been set.
     */
    public static void setEntityCreator(EntityCreator creator) {
        Objects.requireNonNull(creator, "'creator' cannot be null.");

        if (TablesModelHelper.entityCreator != null) {
            throw new ClientLogger(TablesModelHelper.class).logExceptionAsError(new IllegalStateException(
                "'entityCreator' is already set."));
        }

        entityCreator = creator;
    }

    /**
     * Sets the item creator.
     *
     * @param creator The item creator.
     * @throws IllegalStateException if the accessor has already been set.
     */
    public static void setItemCreator(ItemCreator creator) {
        Objects.requireNonNull(creator, "'creator' cannot be null.");

        if (TablesModelHelper.itemCreator != null) {
            throw new ClientLogger(TablesModelHelper.class).logExceptionAsError(new IllegalStateException(
                "'itemCreator' is already set."));
        }

        itemCreator = creator;
    }

    /**
     * Creates a {@link TableEntity}.
     *
     * @param properties The properties used to construct the entity
     * @return The created TableEntity
     */
    public static TableEntity createEntity(Map<String, Object> properties) {
        if (entityCreator == null) {
            throw new ClientLogger(TablesModelHelper.class).logExceptionAsError(
                new IllegalStateException("'entityCreator' should not be null."));
        }

        return entityCreator.create(properties);
    }

    /**
     * Creates a {@link TableItem}.
     *
     * @param properties The TableResponseProperties used to construct the table
     * @return The created TableItem
     */
    public static TableItem createItem(TableResponseProperties properties) {
        if (itemCreator == null) {
            throw new ClientLogger(TablesModelHelper.class).logExceptionAsError(
                new IllegalStateException("'itemCreator' should not be null."));
        }

        return itemCreator.create(properties);
    }

    public interface EntityCreator {
        /**
         * Creates a {@link TableEntity}.
         *
         * @param properties The properties used to construct the entity
         * @return The created TableEntity
         */
        TableEntity create(Map<String, Object> properties);
    }

    public interface ItemCreator {
        /**
         * Creates a {@link TableItem}.
         *
         * @param properties The TableResponseProperties used to construct the table
         * @return The created TableItem
         */
        TableItem create(TableResponseProperties properties);
    }
}
