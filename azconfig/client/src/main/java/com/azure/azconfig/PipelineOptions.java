// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.azconfig;

import com.microsoft.rest.v3.http.policy.HttpLogDetailLevel;

/**
 * This type encapsulates all the possible configuration for the default pipeline. It may be passed to the
 * createPipeline method on {@link AzConfigClient}. All the options fields have default values if nothing is passed, and
 * no logger will be used if it is not set.
 */
public final class PipelineOptions {
    private HttpLogDetailLevel detailLevel = HttpLogDetailLevel.BASIC;

    /**
     * Configures the built-in request logging policy.
     * @return log detail level
     */
    public HttpLogDetailLevel httpLogDetailLevel() {
        return detailLevel;
    }

    /**
     * Configures the built-in request logging policy.
     * @param detailLevel log detail level
     * @return PipelineOptions object itself
     */
    public PipelineOptions withHttpLogDetailLevel(HttpLogDetailLevel detailLevel) {
        this.detailLevel = detailLevel;
        return this;
    }
}
