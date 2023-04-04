// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.CosmosClientBuilder;

public final class CosmosCreateItemRequestOptions extends CosmosItemRequestOptions {
    public CosmosCreateItemRequestOptions() {
        super();
    }

    /**
     * Enables automatic retries for Create operations even when the SDK can't
     * guarantee that they are idempotent. This is an override of the
     * {@link CosmosClientBuilder} behavior for a specific create operation.
     * Retries are for example not guaranteed to be idempotent, when retrying a createItem operation
     * after the initial attempt timed-out after writing the request payload on the network connection. It is
     * unclear whether the initial request ever reached the service and was processed there or not. The retry
     * could return a 409-Conflict response simply because the initial attempt was processed.
     * When enabling write retries even when idempotency is not guaranteed the SDK will apply a few extra
     * steps to minimize the risk for the caller of facing these idempotency issues due to retries. If
     * useTrackingIdPropertyForCreateAndReplace is enabled,
     * the SDK will use a system property "_trackingId" which will be stored in the documents to help
     * filter out failure conditions caused simply by retries for operations that have actually been processed
     * already by the service. For example a 409 on a retry would be mapped back to a 201 if the document has the same
     * _trackingId value the initial attempt to create the document used.
     <p>
     * NOTE: the setting on the CosmosClientBuilder will determine the default behavior for Create, Replace,
     * Upsert and Delete operations. It can be overridden on per-request base in the request options. For patch
     * operations by default (unless overridden in the request options) retries are always disabled by default
     * when the retry can't be guaranteed to be idempotent. The exception for patch is used because whether
     * a retry is "safe" for a patch operation really depends on the set of patch instructions. The documentation
     * for the patch operation has more details.
     *
     * @param useTrackingIdPropertyForCreateAndReplace a flag indicating whether write operations can use the
     * trackingId system property '/_trackingId' to allow identification of conflicts and pre-condition failures due
     * to retries. If enabled, each document being created or replaced will have an additional '/_trackingId' property
     * for which the value will be updated by the SDK. If it is not desired to add this new json property (for example
     * due to the RU-increase based on the payload size or because it causes documents to exceed the max payload size
     * upper limit), the usage of this system property can be disabled by setting this parameter to false. This means
     * there could be a higher level of 409/312 due to retries - and applications would need to handle them gracefully
     * on their own.
     * @return the CosmosCreateItemRequestOptions.
     */
    public CosmosCreateItemRequestOptions enableNonIdempotentWriteRetries(
        boolean useTrackingIdPropertyForCreateAndReplace) {

        this.setNonIdempotentWriteRetryPolicy(true, useTrackingIdPropertyForCreateAndReplace);
        return this;
    }

    /**
     * Disables automatic retries for write operations when the SDK can't
     * guarantee that they are idempotent. This is an override of the
     * {@link CosmosClientBuilder} behavior for a specific operation.
     * <p>
     *
     * @return the CosmosCreateItemRequestOptions.
     */
    public CosmosCreateItemRequestOptions disableNonIdempotentWriteRetries() {
        this.setNonIdempotentWriteRetryPolicy(false, false);

        return this;
    }
}
