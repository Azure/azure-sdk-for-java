// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.client.traits;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryOptions;

/**
 * The interface for client builders that support an HTTP protocol.
 * @param <TBuilder> the type of client builder.
 */
public interface HttpTrait<TBuilder extends HttpTrait<TBuilder>> {
    /**
     * Sets the {@link HttpClient} to use for sending a receiving requests to and from the service.
     *
     * @param httpClient HttpClient to use for requests.
     * @return the updated TBuilder object.
     */
    TBuilder httpClient(HttpClient httpClient);

    /**
     * Sets the {@link HttpPipeline} to use for the service client.
     *
     * @param httpPipeline HttpPipeline to use for sending service requests and receiving responses.
     * @return the updated TBuilder object.
     */
    TBuilder pipeline(HttpPipeline httpPipeline);

    /**
     * Adds a pipeline policy to apply on each request sent.
     *
     * @param pipelinePolicy a pipeline policy
     * @return the updated TBuilder object.
     */
    TBuilder addPolicy(HttpPipelinePolicy pipelinePolicy);

    /**
     * Sets the configuration of retry policy.
     * @param retryOptions the options of retry policy.
     * @return the updated TBuilder object.
     */
    TBuilder retryOptions(RetryOptions retryOptions);

    /**
     * Sets the {@link HttpLogOptions} for service requests.
     *
     * @param logOptions The logging configuration to use when sending and receiving HTTP requests/responses.
     * @return the updated TBuilder object.
     */
    TBuilder httpLogOptions(HttpLogOptions logOptions);
}
