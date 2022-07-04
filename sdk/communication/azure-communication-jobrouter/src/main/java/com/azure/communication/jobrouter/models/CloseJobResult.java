package com.azure.communication.jobrouter.models;

import com.azure.core.annotation.Fluent;

/**
 * Response object for close job request.
 */
@Fluent
public class CloseJobResult {
    private Object emptyResponse;

    public CloseJobResult(Object emptyResponse) {
        this.emptyResponse = emptyResponse;
    }
}
