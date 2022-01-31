// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.client.traits;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryOptions;

/**
 * The interface for client builders that support an HTTP protocol.
 * @param <TBuilder> the type of client builder.
 */
public interface HttpTrait<TBuilder extends HttpTrait<TBuilder>> {
    /**
     * Sets the {@link HttpClient} to use for sending and receiving requests to and from the service.
     *
     * @param httpClient The {@link HttpClient} to use for requests.
     * @return the updated TBuilder object.
     */
    TBuilder httpClient(HttpClient httpClient);

    /**
     * Sets the {@link HttpPipeline} to use for the service client.
     *
     * @param pipeline {@link HttpPipeline} to use for sending service requests and receiving responses.
     * @return the updated TBuilder object.
     */
    TBuilder pipeline(HttpPipeline pipeline);

    /**
     * Adds a {@link HttpPipelinePolicy pipeline policy} to apply on each request sent.
     *
     * @param pipelinePolicy A {@link HttpPipelinePolicy pipeline policy}.
     * @return the updated TBuilder object.
     */
    TBuilder addPolicy(HttpPipelinePolicy pipelinePolicy);

    /**
     * Sets the {@link RetryOptions} for all the requests made through the client.
     *
     * @param retryOptions The {@link RetryOptions} to use for all the requests made through the client.
     * @return the updated TBuilder object.
     */
    TBuilder retryOptions(RetryOptions retryOptions);

    /**
     * Sets the {@link HttpLogOptions logging configuration} to use when sending and receiving requests to and from
     * the service. If a {@code logLevel} is not provided, default value of {@link HttpLogDetailLevel#NONE} is set.
     *
     * @param logOptions The {@link HttpLogOptions logging configuration} to use when sending and receiving requests to
     * and from the service.
     * @return the updated TBuilder object.
     */
    TBuilder httpLogOptions(HttpLogOptions logOptions);
}
