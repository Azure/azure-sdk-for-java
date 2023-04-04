// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.CosmosClientBuilder;

public final class CosmosUpsertItemRequestOptions extends CosmosItemRequestOptions {
    public CosmosUpsertItemRequestOptions() {
        super();
    }

    /**
     * Enables automatic retries for Replace operations even when the SDK can't
     * guarantee that they are idempotent. This is an override of the
     * {@link CosmosClientBuilder} behavior for a specific create operation.
     * Retries are for example not guaranteed to be idempotent, when retrying an upsertItem operation
     * after the initial attempt timed-out after writing the request payload on the network connection. It is
     * unclear whether the initial request ever reached the service and was processed there or not. The retry
     * could return a 200-OK response even when the initial attempt actually created the document (which would have
     * resulted in a 201-Created response).
     * <p>
     * NOTE: the setting on the CosmosClientBuilder will determine the default behavior for Create, Replace,
     * Upsert and Delete operations. It can be overridden on per-request base in the request options. For patch
     * operations by default (unless overridden in the request options) retries are always disabled by default
     * when the retry can't be guaranteed to be idempotent. The exception for patch is used because whether
     * a retry is "safe" for a patch operation really depends on the set of patch instructions. The documentation
     * for the patch operation has more details.
     * @return the CosmosUpsertItemRequestOptions.
     */
    public CosmosUpsertItemRequestOptions enableNonIdempotentWriteRetries() {

        this.setNonIdempotentWriteRetryPolicy(true, false);
        return this;
    }

    /**
     * Disables automatic retries for write operations when the SDK can't
     * guarantee that they are idempotent. This is an override of the
     * {@link CosmosClientBuilder} behavior for a specific operation.
     * <p>
     *
     * @return the CosmosUpsertItemRequestOptions.
     */
    public CosmosUpsertItemRequestOptions disableNonIdempotentWriteRetries() {
        this.setNonIdempotentWriteRetryPolicy(false, false);

        return this;
    }
}
