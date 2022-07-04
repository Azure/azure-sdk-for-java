package com.azure.communication.jobrouter.models;

import com.azure.core.annotation.Fluent;

/**
 * Result object for reclassify job.
 */
@Fluent
public class ReclassifyJobResult {
    private Object emptyResponse;

    public ReclassifyJobResult(Object emptyResponse) {
        this.emptyResponse = emptyResponse;
    }
}
