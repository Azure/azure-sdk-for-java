// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.data.tables.implementation.models.TableResponseProperties;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.TableItem;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Used to access internal methods on models.
 */
public final class TablesModelHelper {
    private static EntityAccessor entityAccessor;
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
     * @param accessor The entity accessor.
     * @throws IllegalStateException if the accessor has already been set.
     */
    public static void setEntityAccessor(EntityAccessor accessor) {
        Objects.requireNonNull(accessor, "'accessor' cannot be null.");

        if (TablesModelHelper.entityAccessor != null) {
            throw new ClientLogger(TablesModelHelper.class).logExceptionAsError(new IllegalStateException(
                "'entityAccessor' is already set."));
        }

        entityAccessor = accessor;
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
     * Sets values on a {@link TableEntity}.
     *
     * @param entity Entity to set the values on.
     * @param timestamp Timestamp to set.
     * @param eTag ETag to set.
     */
    public static void setValues(TableEntity entity, OffsetDateTime timestamp, String eTag) {
        if (entityAccessor == null) {
            throw new ClientLogger(TablesModelHelper.class).logExceptionAsError(
                new IllegalStateException("'entityAccessor' should not be null."));
        }

        entityAccessor.setValues(entity, timestamp, eTag);
    }

    /**
     * Creates a {@link TableItem}.
     *
     * @param properties The TableResponseProperties used to construct the table
     */
    public static TableItem createItem(TableResponseProperties properties) {
        if (itemCreator == null) {
            throw new ClientLogger(TablesModelHelper.class).logExceptionAsError(
                new IllegalStateException("'itemCreator' should not be null."));
        }

        return itemCreator.create(properties);
    }

    public interface EntityAccessor {
        /**
         * Sets values on a {@link TableEntity}.
         *
         * @param entity Entity to set the ETag on.
         * @param timestamp Timestamp to set.
         * @param eTag ETag to set.
         */
        void setValues(TableEntity entity, OffsetDateTime timestamp, String eTag);
    }

    public interface ItemCreator {
        /**
         * Creates a {@link TableItem}.
         *
         * @param properties The TableResponseProperties used to construct the table
         */
        TableItem create(TableResponseProperties properties);
    }
}
