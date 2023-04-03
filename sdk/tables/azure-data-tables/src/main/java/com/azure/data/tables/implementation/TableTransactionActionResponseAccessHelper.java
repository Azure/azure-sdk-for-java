// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables.implementation;

import com.azure.core.http.HttpRequest;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.tables.models.TableTransactionActionResponse;

/**
 * Helper class to access internal values of {@link TableTransactionActionResponse}.
 */
public final class TableTransactionActionResponseAccessHelper {
    private static TableTransactionActionResponseAccessor accessor;

    public interface TableTransactionActionResponseAccessor {
        /**
         * Creates a {@link TableTransactionActionResponse}.
         *
         * @param statusCode The status code for the {@link TableTransactionActionResponse}.
         * @param value The value for the {@link TableTransactionActionResponse}.
         * @return The created {@link TableTransactionActionResponse}.
         */
        TableTransactionActionResponse createTableTransactionActionResponse(int statusCode, Object value);

        /**
         * Updates a {@link TableTransactionActionResponse} with a request object.
         *
         * @param subject The {@link TableTransactionActionResponse} to update.
         * @param request The request to attach to the {@link TableTransactionActionResponse}.
         */
        void updateTableTransactionActionResponse(TableTransactionActionResponse subject, HttpRequest request);
    }

    /**
     * Sets the accessor to {@link TableTransactionActionResponse}.
     *
     * @param accessor The accessor to {@link TableTransactionActionResponse}.
     */
    public static void setAccessor(TableTransactionActionResponseAccessor accessor) {
        TableTransactionActionResponseAccessHelper.accessor = accessor;
    }

    /**
     * Creates a {@link TableTransactionActionResponse}.
     *
     * @param statusCode The status code for the {@link TableTransactionActionResponse}.
     * @param value The value for the {@link TableTransactionActionResponse}.
     * @return The created {@link TableTransactionActionResponse}.
     */
    public static TableTransactionActionResponse createTableTransactionActionResponse(int statusCode, Object value) {
        ensureLoaded();

        return accessor.createTableTransactionActionResponse(statusCode, value);
    }

    /**
     * Updates a {@link TableTransactionActionResponse} with a request object.
     *
     * @param subject The {@link TableTransactionActionResponse} to update.
     * @param request The request to attach to the {@link TableTransactionActionResponse}.
     */
    public static void updateTableTransactionActionResponse(TableTransactionActionResponse subject, HttpRequest request) {
        accessor.updateTableTransactionActionResponse(subject, request);
    }

    private static void ensureLoaded() {
        try {
            Class.forName(TableTransactionActionResponse.class.getName(), true,
                TableTransactionActionResponseAccessHelper.class.getClassLoader());
        } catch (ReflectiveOperationException ex) {
            throw new ClientLogger(TableTransactionActionResponseAccessHelper.class).logExceptionAsError(
                new IllegalStateException("Failed to load 'TableTransactionActionResponse' class within "
                    + "'TableTransactionActionResponseAccessHelper'.", ex));
        }
    }

    private TableTransactionActionResponseAccessHelper() {
    }
}
