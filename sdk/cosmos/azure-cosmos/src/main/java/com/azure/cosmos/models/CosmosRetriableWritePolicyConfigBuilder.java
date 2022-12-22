// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import java.time.Duration;

public class CosmosRetriableWritePolicyConfigBuilder {
    private final boolean isEnabled;
    private int softDeleteTTLInDays = -1;
    private Duration speculativeProcessingThreshold = null;

    public CosmosRetriableWritePolicyConfigBuilder() {
        this(true);
    }

    CosmosRetriableWritePolicyConfigBuilder(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public CosmosRetriableWritePolicyConfig build() {
        // TODO implement
        return null;
    }

    // -1 - don't use soft deletes - all deletes will directly delete
    // which results in some edge-cases where retriable writes might not be strictly idempotent anymore
    // like between initial replace and retry a delete form another machine sneaked in - transactionId concept
    // won't help when these concurrent operations are deletes
    // soft deletes (are one option to try to address it - but would result in cases where SDK would also need
    // to filter out soft deleted docs for read/query etc. - adds significant level of complexity
    // marking this as internal for now - because I am not sure going down this path is worthwhile
    // still leaving it here to get some feedback
    CosmosRetriableWritePolicyConfigBuilder softDeleteTTLInSeconds(
        Duration endToEndOperationTimeout) {

        // TODO implement
        return null;
    }

    // this needs more investigation. My initial proposal to adding retriable writes adds a bunch of new APIs
    // my understanding was that ideally we would add option to retry writes without introducing new APIs
    // Internal and external feedback pointed int his direction
    // for createItem this is relatively straight forward when using transactionId
    // but when documents are mutable - like can be patched/replaced you would also need to keep a set of transactionIds
    // persisted in the document to identify whether a retried operation had been processed or not
    // investigating how far we can get adding this without any new APIs and whether the behavior would be intuitive
    // enough in that case will need to be done in the next week or early Jan.
    // adding this builder to allow adding config for how many transactionIds at
    // most too persist (how long to keep them etc.)
}
