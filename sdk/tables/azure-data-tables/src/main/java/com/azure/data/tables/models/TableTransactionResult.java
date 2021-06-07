// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.Context;
import com.azure.data.tables.TableAsyncClient;
import com.azure.data.tables.TableClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A result type returned from calling {@link TableClient#submitTransaction(List)},
 * {@link TableClient#submitTransactionWithResponse(List, Duration, Context)},
 * {@link TableAsyncClient#submitTransaction(List)} or {@link TableAsyncClient#submitTransactionWithResponse(List)}.
 */
@Immutable
public final class TableTransactionResult {
    private final List<TableTransactionActionResponse> transactionActionResponses;
    private final Map<String, TableTransactionActionResponse> lookupMap;

    /**
     * Create a new {@link TableTransactionResult}.
     *
     * @param transactionActions The list of {@link TableTransactionAction transaction actions} sent in the request.
     * @param transactionActionResponses The list of {@link TableTransactionActionResponse responses} that correspond
     * to each transaction action.
     */
    public TableTransactionResult(List<TableTransactionAction> transactionActions,
                                  List<TableTransactionActionResponse> transactionActionResponses) {
        this.transactionActionResponses = transactionActionResponses;
        this.lookupMap = new HashMap<>();

        for (int i = 0; i < transactionActions.size(); i++) {
            lookupMap.put(transactionActions.get(i).getEntity().getRowKey(), transactionActionResponses.get(i));
        }
    }

    /**
     * Get all the {@link TableTransactionActionResponse sub-responses} obtained from the submit transaction operation.
     *
     * @return The {@link TableTransactionActionResponse sub-responses} obtained from the submit transaction operation
     */
    public List<TableTransactionActionResponse> getTransactionActionResponses() {
        return transactionActionResponses;
    }

    /**
     * Obtain the corresponding {@link TableTransactionActionResponse sub-response} for a given {@code rowKey}.
     *
     * @param rowKey The {@code rowKey} to look a {@link TableTransactionActionResponse sub-response} with.
     *
     * @return The {@link TableTransactionActionResponse} that corresponds to the given {@code rowKey}.
     */
    public TableTransactionActionResponse getTableTransactionActionResponseByRowKey(String rowKey) {
        return lookupMap.get(rowKey);
    }
}
