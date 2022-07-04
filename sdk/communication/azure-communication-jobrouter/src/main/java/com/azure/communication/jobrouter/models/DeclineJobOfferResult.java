package com.azure.communication.jobrouter.models;

import com.azure.core.annotation.Fluent;

/**
 * Response object for decline job offer request.
 */
@Fluent
public class DeclineJobOfferResult {
    private Object emptyResponse;

    public DeclineJobOfferResult(Object emptyResponse) {
        this.emptyResponse = emptyResponse;
    }
}
