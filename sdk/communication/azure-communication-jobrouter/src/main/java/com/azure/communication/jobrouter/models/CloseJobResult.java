// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models;

import com.azure.core.annotation.Fluent;

/**
 * Response object for close job request.
 */
@Fluent
public class CloseJobResult extends EmptyPlaceHolderObject {
    /**
     * Constructor to create a place-holder result.
     * Optionally use Fluent set and get to add more fields to the result.
     * @param emptyResponse Placeholder object.
     */
    public CloseJobResult(Object emptyResponse) {
        this.emptyResponse = emptyResponse;
    }
}
