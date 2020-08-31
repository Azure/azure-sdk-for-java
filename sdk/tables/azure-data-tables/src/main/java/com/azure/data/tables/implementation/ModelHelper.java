// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.data.tables.implementation.models.TableResponseProperties;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.TableItem;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Used to access internal methods on models.
 */
public final class ModelHelper {
    private static Function<Map<String, Object>, TableEntity> entityCreator;
    private static Function<TableResponseProperties, TableItem> itemCreator;

    static {
        // Force classes' static blocks to execute
        try {
            Class.forName(TableEntity.class.getName(), true, TableEntity.class.getClassLoader());
            Class.forName(TableItem.class.getName(), true, TableItem.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(new ClientLogger(ModelHelper.class).logThrowableAsError(e));
        }
    }

    /**
     * Sets the entity creator.
     *
     * @param creator The entity creator.
     * @throws IllegalStateException if the creator has already been set.
     */
    public static void setEntityCreator(Function<Map<String, Object>, TableEntity> creator) {
        Objects.requireNonNull(creator, "'creator' cannot be null.");

        if (ModelHelper.entityCreator != null) {
            throw new ClientLogger(ModelHelper.class).logExceptionAsError(new IllegalStateException(
                "'entityCreator' is already set."));
        }

        entityCreator = creator;
    }

    /**
     * Sets the item creator.
     *
     * @param creator The item creator.
     * @throws IllegalStateException if the creator has already been set.
     */
    public static void setItemCreator(Function<TableResponseProperties, TableItem> creator) {
        Objects.requireNonNull(creator, "'creator' cannot be null.");

        if (ModelHelper.itemCreator != null) {
            throw new ClientLogger(ModelHelper.class).logExceptionAsError(new IllegalStateException(
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
            throw new ClientLogger(ModelHelper.class).logExceptionAsError(
                new IllegalStateException("'entityCreator' should not be null."));
        }

        return entityCreator.apply(properties);
    }

    /**
     * Creates a {@link TableItem}.
     *
     * @param properties The TableResponseProperties used to construct the table
     * @return The created TableItem
     */
    public static TableItem createItem(TableResponseProperties properties) {
        if (itemCreator == null) {
            throw new ClientLogger(ModelHelper.class).logExceptionAsError(
                new IllegalStateException("'itemCreator' should not be null."));
        }

        return itemCreator.apply(properties);
    }
}
