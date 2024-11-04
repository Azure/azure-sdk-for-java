// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.ModelBridgeInternal;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public final class QueryFeedOperationState extends FeedOperationState {

    private static final ImplementationBridgeHelpers
        .CosmosQueryRequestOptionsHelper
        .CosmosQueryRequestOptionsAccessor qryOptAccessor = ImplementationBridgeHelpers
        .CosmosQueryRequestOptionsHelper
        .getCosmosQueryRequestOptionsAccessor();

    private final CosmosQueryRequestOptions options;
    private final RequestOptions requestOptions;

    public QueryFeedOperationState(
        CosmosAsyncClient cosmosAsyncClient,
        String spanName,
        String dbName,
        String containerName,
        ResourceType resourceType,
        OperationType operationType,
        String operationId,
        CosmosQueryRequestOptions queryRequestOptions,
        CosmosPagedFluxOptions fluxOptions
    ) {
        super(
            cosmosAsyncClient,
            spanName,
            dbName,
            containerName,
            resourceType,
            checkNotNull(operationType, "Argument 'operationType' must not be null."),
            operationId,
            clientAccessor.getEffectiveConsistencyLevel(
                cosmosAsyncClient,
                operationType,
                queryRequestOptions.getConsistencyLevel()),
            clientAccessor.getEffectiveDiagnosticsThresholds(
                cosmosAsyncClient,
                qryOptAccessor.getImpl(
                    checkNotNull(queryRequestOptions, "Argument 'queryRequestOptions' must not be null.")
                ).getDiagnosticsThresholds()),
            fluxOptions,
            getEffectiveMaxItemCount(fluxOptions, queryRequestOptions),
            qryOptAccessor.getImpl(checkNotNull(queryRequestOptions, "Argument 'queryRequestOptions' must not be null."))
        );

        String requestOptionsContinuation = qryOptAccessor.getRequestContinuation(queryRequestOptions);
        if (requestOptionsContinuation != null &&
            (fluxOptions == null || fluxOptions.getRequestContinuation() == null)) {

            this.setRequestContinuation(requestOptionsContinuation);

            if (fluxOptions != null) {
                fluxOptions.setRequestContinuation(requestOptionsContinuation);
            }
        }

        Integer maxItemCountFromRequestOptions = qryOptAccessor.getMaxItemCount(queryRequestOptions);
        if (maxItemCountFromRequestOptions != null &&
            (fluxOptions == null || fluxOptions.getMaxItemCount() == null)) {

            this.setMaxItemCount(maxItemCountFromRequestOptions);

            if (fluxOptions != null) {
                fluxOptions.setMaxItemCount(maxItemCountFromRequestOptions);
            }
        }

        this.options = qryOptAccessor.clone(queryRequestOptions);
        // apply the maxItemCount/continuation to the cloned request options
        this.setMaxItemCountCore(this.getMaxItemCount());
        this.setRequestContinuationCore(this.getRequestContinuation());
        this.requestOptions = qryOptAccessor.toRequestOptions(this.options);
    }

    public RequestOptions toRequestOptions() {
        return this.requestOptions;
    }

    public CosmosQueryRequestOptions getQueryOptions() {
        return this.options;
    }

    @Override
    public void setRequestContinuation(String requestContinuation) {
        super.setRequestContinuation(requestContinuation);

        this.setRequestContinuationCore(requestContinuation);
    }

    private void setRequestContinuationCore(String requestContinuation) {
        if (this.options != null) {
            ModelBridgeInternal.setQueryRequestOptionsContinuationToken(
                this.options,
                requestContinuation
            );
        }
    }

    @Override
    public void setMaxItemCount(Integer maxItemCount) {
        super.setMaxItemCount(maxItemCount);

        this.setMaxItemCountCore(maxItemCount);
    }

    private void setMaxItemCountCore(Integer maxItemCount) {

        if (this.options != null) {
            ModelBridgeInternal.setQueryRequestOptionsMaxItemCount(
                this.options,
                maxItemCount
            );
        }
    }

    private static Integer getEffectiveMaxItemCount(
        CosmosPagedFluxOptions pagedFluxOptions,
        CosmosQueryRequestOptions queryOptions) {

        if (pagedFluxOptions != null && pagedFluxOptions.getMaxItemCount() != null) {
            return pagedFluxOptions.getMaxItemCount();
        }

        if (queryOptions == null) {
            return null;
        }

        return qryOptAccessor.getMaxItemCount(queryOptions);
    }
}
