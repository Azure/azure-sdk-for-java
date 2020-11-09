// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.implementation;

import com.azure.core.http.HttpRequest;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.tables.implementation.models.TableResponseProperties;
import com.azure.data.tables.models.BatchOperationResponse;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.TableItem;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Used to access internal methods on models.
 */
public final class ModelHelper {
    private static Supplier<TableEntity> entityCreator;
    private static Function<TableResponseProperties, TableItem> itemCreator;
    private static BiFunction<Integer, Object, BatchOperationResponse> batchOperationResponseCreator;
    private static BiConsumer<BatchOperationResponse, HttpRequest> batchOperationResponseUpdater;

    static {
        // Force classes' static blocks to execute
        try {
            Class.forName(TableEntity.class.getName(), true, TableEntity.class.getClassLoader());
            Class.forName(TableItem.class.getName(), true, TableItem.class.getClassLoader());
            Class.forName(BatchOperationResponse.class.getName(), true, BatchOperationResponse.class.getClassLoader());
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
    public static void setEntityCreator(Supplier<TableEntity> creator) {
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
     * Sets the batch operation response creator.
     *
     * @param creator The creator.
     * @throws IllegalStateException if the creator has already been set.
     */
    public static void setBatchOperationResponseCreator(BiFunction<Integer, Object, BatchOperationResponse> creator) {
        Objects.requireNonNull(creator, "'creator' cannot be null.");

        if (ModelHelper.batchOperationResponseCreator != null) {
            throw new ClientLogger(ModelHelper.class).logExceptionAsError(new IllegalStateException(
                "'batchOperationResponseCreator' is already set."));
        }

        batchOperationResponseCreator = creator;
    }

    /**
     * Sets the batch operation response updater.
     *
     * @param updater The updater.
     * @throws IllegalStateException if the updater has already been set.
     */
    public static void setBatchOperationResponseUpdater(BiConsumer<BatchOperationResponse, HttpRequest> updater) {
        Objects.requireNonNull(updater, "'updater' cannot be null.");

        if (ModelHelper.batchOperationResponseUpdater != null) {
            throw new ClientLogger(ModelHelper.class).logExceptionAsError(new IllegalStateException(
                "'batchOperationResponseUpdater' is already set."));
        }

        batchOperationResponseUpdater = updater;
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

        return entityCreator.get().addProperties(properties);
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

    /**
     * Creates a {@link BatchOperationResponse}.
     *
     * @param statusCode The status code for the BatchOperationResponse
     * @param value The value for the BatchOperationResponse
     * @return The created BatchOperationResponse
     */
    public static BatchOperationResponse createBatchOperationResponse(int statusCode, Object value) {
        if (batchOperationResponseCreator == null) {
            throw new ClientLogger(ModelHelper.class).logExceptionAsError(
                new IllegalStateException("'batchOperationResponseCreator' should not be null."));
        }

        return batchOperationResponseCreator.apply(statusCode, value);
    }

    /**
     * Updates a {@link BatchOperationResponse} with a request object.
     *
     * @param subject The BatchOperationResponse to update
     * @param request The request to attach to the BatchOperationResponse
     */
    public static void updateBatchOperationResponse(BatchOperationResponse subject, HttpRequest request) {
        if (batchOperationResponseUpdater == null) {
            throw new ClientLogger(ModelHelper.class).logExceptionAsError(
                new IllegalStateException("'batchOperationResponseUpdater' should not be null."));
        }

        batchOperationResponseUpdater.accept(subject, request);
    }
}
