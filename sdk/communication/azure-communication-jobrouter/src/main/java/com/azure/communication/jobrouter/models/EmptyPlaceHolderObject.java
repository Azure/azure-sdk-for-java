// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models;

/**
 * Abstract class for placeholder responses.
 */
public abstract class EmptyPlaceHolderObject {
    /**
     * Placeholder object.
     */
    protected Object emptyResponse;

    /**
     * Returns emptyResponse.
     * @return emptyResponse
     */
    public Object getEmptyResponse() {
        return this.emptyResponse;
    }
}
