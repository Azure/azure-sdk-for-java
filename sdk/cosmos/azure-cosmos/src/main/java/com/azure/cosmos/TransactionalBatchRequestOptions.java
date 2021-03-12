// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.util.Beta;

/**
 * Encapsulates options that can be specified for a {@link TransactionalBatch}.
 */
@Beta(value = Beta.SinceVersion.V4_7_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public final class TransactionalBatchRequestOptions {
    private ConsistencyLevel consistencyLevel;
    private String sessionToken;

    /**
     * Gets the consistency level required for the request.
     *
     * @return the consistency level.
     */
    ConsistencyLevel getConsistencyLevel() {
        return consistencyLevel;
    }

    /**
     * Sets the consistency level required for the request.
     *
     * @param consistencyLevel the consistency level.
     * @return the TransactionalBatchRequestOptions.
     */
    TransactionalBatchRequestOptions setConsistencyLevel(ConsistencyLevel consistencyLevel) {
        this.consistencyLevel = consistencyLevel;
        return this;
    }

    /**
     * Gets the token for use with session consistency.
     *
     * @return the session token.
     */
    @Beta(value = Beta.SinceVersion.V4_7_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public String getSessionToken() {
        return sessionToken;
    }

    /**
     * Sets the token for use with session consistency.
     *
     * @param sessionToken the session token.
     * @return the TransactionalBatchRequestOptions.
     */
    @Beta(value = Beta.SinceVersion.V4_7_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public TransactionalBatchRequestOptions setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
        return this;
    }

    RequestOptions toRequestOptions() {
        final RequestOptions requestOptions = new RequestOptions();
        requestOptions.setConsistencyLevel(getConsistencyLevel());
        requestOptions.setSessionToken(sessionToken);
        return requestOptions;
    }
}
