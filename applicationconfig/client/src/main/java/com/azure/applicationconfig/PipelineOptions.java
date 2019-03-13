// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.applicationconfig;

import com.microsoft.rest.v3.http.HttpClient;
import com.microsoft.rest.v3.http.policy.HttpPipelinePolicy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * This type encapsulates all the possible configuration for the default pipeline. It may be passed to the
 * createPipeline method on {@link AzConfigClient}. All the options fields have default values if nothing is passed, and
 * no logger will be used if it is not set.
 */
public final class PipelineOptions {
    private HttpClient client;
    private final List<HttpPipelinePolicy> additionalPolicies = new ArrayList<HttpPipelinePolicy>();

    public HttpClient client() { return this.client; }

    public Iterable<HttpPipelinePolicy> additionalPolicies() { return this.additionalPolicies; }

    /**
     * Sets an HTTP client to use for communication to the service rather than the default one.
     */
    public PipelineOptions withHttpClient(HttpClient client) {
        Objects.requireNonNull(client);

        this.client = client;
        return this;
    }

    /**
     * Adds policies to the pipeline that will be executed after all the default ones have been processed.
     */
    public PipelineOptions withPolicies(HttpPipelinePolicy... policies) {
        Objects.requireNonNull(policies);

        additionalPolicies.addAll(Arrays.asList(policies));
        return this;
    }
}
