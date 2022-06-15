// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.core.annotation.Fluent;

/**
 * The options for answering a call.
 */
@Fluent
public final class AnswerCallOptions {
    /**
     * The callback uri
     */
    private String callbackUri;

    /**
     * Get the callbackUri.
     *
     * @return the callbackUri value.
     */
    public String getCallbackUri() {
        return callbackUri;
    }

    /**
     * Set the callbackUri.
     *
     * @param callbackUri the callback uri to set
     * @return the callbackUri string itself.
     */
    public AnswerCallOptions setCallbackUri(String callbackUri) {
        this.callbackUri = callbackUri;
        return this;
    }
}
