package com.azure.communication.jobrouter.models;

import com.azure.core.annotation.Fluent;

/**
 * Response object for decline job offer request.
 */
@Fluent
public class DeclineJobOfferResult {
    private Object emptyResponse;

    /**
     * Constructor to create a place-holder result.
     * Optionally use Fluent set and get to add more fields to the result.
     * @param emptyResponse
     */
    public DeclineJobOfferResult(Object emptyResponse) {
        this.emptyResponse = emptyResponse;
    }
}
