// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.implementation;

import com.azure.core.http.HttpRequest;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.tables.implementation.models.TableResponseProperties;
import com.azure.data.tables.models.TableTransactionActionResponse;
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
    private static BiFunction<Integer, Object, TableTransactionActionResponse> tableTransactionActionResponseCreator;
    private static BiConsumer<TableTransactionActionResponse, HttpRequest> tableTransactionActionResponseUpdater;

    static {
        // Force classes' static blocks to execute.
        try {
            Class.forName(TableEntity.class.getName(), true, TableEntity.class.getClassLoader());
            Class.forName(TableItem.class.getName(), true, TableItem.class.getClassLoader());
            Class.forName(TableTransactionActionResponse.class.getName(), true,
                TableTransactionActionResponse.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(new ClientLogger(ModelHelper.class).logThrowableAsError(e));
        }
    }

    /**
     * Sets the {@link TableEntity} creator.
     *
     * @param creator The {@link TableEntity} creator.
     * @throws IllegalStateException If the creator has already been set.
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
     * Sets the {@link TableItem} creator.
     *
     * @param creator The {@link TableItem} creator.
     * @throws IllegalStateException If the creator has already been set.
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
     * Sets the {@link TableTransactionActionResponse} creator.
     *
     * @param creator The creator.
     * @throws IllegalStateException If the creator has already been set.
     */
    public static void setTableTransactionActionResponseCreator(BiFunction<Integer, Object, TableTransactionActionResponse> creator) {
        Objects.requireNonNull(creator, "'creator' cannot be null.");

        if (ModelHelper.tableTransactionActionResponseCreator != null) {
            throw new ClientLogger(ModelHelper.class).logExceptionAsError(new IllegalStateException(
                "'tableTransactionActionResponseCreator' is already set."));
        }

        tableTransactionActionResponseCreator = creator;
    }

    /**
     * Sets the {@link TableTransactionActionResponse} updater.
     *
     * @param updater The updater.
     * @throws IllegalStateException If the updater has already been set.
     */
    public static void setTableTransactionActionResponseUpdater(BiConsumer<TableTransactionActionResponse, HttpRequest> updater) {
        Objects.requireNonNull(updater, "'updater' cannot be null.");

        if (ModelHelper.tableTransactionActionResponseUpdater != null) {
            throw new ClientLogger(ModelHelper.class).logExceptionAsError(new IllegalStateException(
                "'tableTransactionActionResponseUpdater' is already set."));
        }

        tableTransactionActionResponseUpdater = updater;
    }

    /**
     * Creates a {@link TableEntity}.
     *
     * @param properties The properties used to construct the {@link TableEntity}.
     * @return The created {@link TableEntity}.
     */
    public static TableEntity createEntity(Map<String, Object> properties) {
        if (entityCreator == null) {
            throw new ClientLogger(ModelHelper.class).logExceptionAsError(
                new IllegalStateException("'entityCreator' should not be null."));
        }

        return entityCreator.get().setProperties(properties);
    }

    /**
     * Creates a {@link TableItem}.
     *
     * @param properties The {@link TableResponseProperties} used to construct the table.
     * @return The created {@link TableItem}.
     */
    public static TableItem createItem(TableResponseProperties properties) {
        if (itemCreator == null) {
            throw new ClientLogger(ModelHelper.class).logExceptionAsError(
                new IllegalStateException("'itemCreator' should not be null."));
        }

        return itemCreator.apply(properties);
    }

    /**
     * Creates a {@link TableTransactionActionResponse}.
     *
     * @param statusCode The status code for the {@link TableTransactionActionResponse}.
     * @param value The value for the {@link TableTransactionActionResponse}.
     * @return The created {@link TableTransactionActionResponse}.
     */
    public static TableTransactionActionResponse createTableTransactionActionResponse(int statusCode, Object value) {
        if (tableTransactionActionResponseCreator == null) {
            throw new ClientLogger(ModelHelper.class).logExceptionAsError(
                new IllegalStateException("'tableTransactionActionResponseCreator' should not be null."));
        }

        return tableTransactionActionResponseCreator.apply(statusCode, value);
    }

    /**
     * Updates a {@link TableTransactionActionResponse} with a request object.
     *
     * @param subject The {@link TableTransactionActionResponse} to update.
     * @param request The request to attach to the {@link TableTransactionActionResponse}.
     */
    public static void updateTableTransactionActionResponse(TableTransactionActionResponse subject, HttpRequest request) {
        if (tableTransactionActionResponseUpdater == null) {
            throw new ClientLogger(ModelHelper.class).logExceptionAsError(
                new IllegalStateException("'tableTransactionActionResponseUpdater' should not be null."));
        }

        tableTransactionActionResponseUpdater.accept(subject, request);
    }
}
