// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.Context;
import com.azure.data.tables.TableAsyncClient;
import com.azure.data.tables.TableClient;

import java.time.Duration;
import java.util.List;

/**
 * Exception thrown for an invalid response on a transactional operation with {@link TableServiceError} information.
 */
@Immutable
public final class TableTransactionFailedException extends TableServiceException {
    private final Integer failedTransactionActionIndex;

    /**
     * Initializes a new instance of the {@link TableTransactionFailedException} class.
     *
     * @param message The exception message or the response content if a message is not available.
     * @param response The HTTP response.
     * @param value The deserialized response value.
     * @param failedTransactionActionIndex The index position of the failed {@link TableTransactionAction} in the
     * collection submitted to {@link TableClient#submitTransaction(List)},
     * {@link TableClient#submitTransactionWithResponse(List, Duration, Context)},
     * {@link TableAsyncClient#submitTransaction(List)} or {@link TableAsyncClient#submitTransactionWithResponse(List)}
     * which caused the transaction to fail. If {@code null}, it means the service did not indicate which
     * {@link TableTransactionAction} failed.
     */
    public TableTransactionFailedException(String message, HttpResponse response, TableServiceError value,
                                           Integer failedTransactionActionIndex) {
        super(message, response, value);

        this.failedTransactionActionIndex = failedTransactionActionIndex;
    }

    /**
     * Get the index position of the failed {@link TableTransactionAction} in the collection submitted to
     * {@link TableClient#submitTransaction(List)},
     * {@link TableClient#submitTransactionWithResponse(List, Duration, Context)},
     * {@link TableAsyncClient#submitTransaction(List)} or {@link TableAsyncClient#submitTransactionWithResponse(List)}
     * which caused the transaction to fail. If {@code null}, it means the service did not indicate which
     * {@link TableTransactionAction} failed.
     *
     * @return The index of the failed {@link TableTransactionAction}.
     */
    public Integer getFailedTransactionActionIndex() {
        return failedTransactionActionIndex;
    }
}
