// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.ModelBridgeInternal;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class ChangeFeedOperationState extends FeedOperationState {
    private static final ImplementationBridgeHelpers
        .CosmosChangeFeedRequestOptionsHelper
        .CosmosChangeFeedRequestOptionsAccessor cfOptAccessor = ImplementationBridgeHelpers
        .CosmosChangeFeedRequestOptionsHelper
        .getCosmosChangeFeedRequestOptionsAccessor();

    private final CosmosChangeFeedRequestOptions options;

    public ChangeFeedOperationState(
        CosmosAsyncClient cosmosAsyncClient,
        String spanName,
        String dbName,
        String containerName,
        ResourceType resourceType,
        OperationType operationType,
        String operationId,
        CosmosChangeFeedRequestOptions changeFeedRequestOptions,
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
                null),
            clientAccessor.getEffectiveDiagnosticsThresholds(
                cosmosAsyncClient,
                cfOptAccessor.getDiagnosticsThresholds(
                    checkNotNull(changeFeedRequestOptions, "Argument 'changeFeedRequestOptions' must not be null."))),
            fluxOptions,
            getEffectiveMaxItemCount(fluxOptions, changeFeedRequestOptions),
            cfOptAccessor.getImpl(checkNotNull(changeFeedRequestOptions, "Argument 'changeFeedRequestOptions' must not be null."))
        );

        this.options = ModelBridgeInternal
            .getEffectiveChangeFeedRequestOptions(
                changeFeedRequestOptions, fluxOptions);
    }

    public CosmosChangeFeedRequestOptions getChangeFeedOptions() {
        return this.options;
    }

    @Override
    public void setRequestContinuation(String requestContinuation) {
        super.setRequestContinuation(requestContinuation);

        if (this.options != null) {
            ModelBridgeInternal.setChangeFeedRequestOptionsContinuation(
                requestContinuation,
                this.options
            );
        }
    }

    @Override
    public void setMaxItemCount(Integer maxItemCount) {
        super.setMaxItemCount(maxItemCount);

        if (this.options != null) {
            this.options.setMaxItemCount(maxItemCount);
        }
    }

    private static Integer getEffectiveMaxItemCount(
        CosmosPagedFluxOptions pagedFluxOptions,
        CosmosChangeFeedRequestOptions changeFeedOptions) {

        if (pagedFluxOptions != null && pagedFluxOptions.getMaxItemCount() != null) {
            return pagedFluxOptions.getMaxItemCount();
        }

        if (changeFeedOptions == null) {
            return null;
        }

        return changeFeedOptions.getMaxItemCount();
    }
}
