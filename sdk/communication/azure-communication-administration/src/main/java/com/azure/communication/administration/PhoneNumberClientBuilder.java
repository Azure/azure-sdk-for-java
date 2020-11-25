// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.administration;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.Configuration;
import com.azure.core.util.ServiceVersion;

/**
 * Builder for creating clients of Communication Service phone number configuration
 */
@ServiceClientBuilder(serviceClients = {PhoneNumberClient.class, PhoneNumberAsyncClient.class})
public final class PhoneNumberClientBuilder {

    /**
     * Set credential of the service
     *
     * @param credential credential to use
     * @return PhoneNumberClientBuilder
     */
    public PhoneNumberClientBuilder credential(TokenCredential credential) {
        // implementation
        return this;
    }

    /**
     * Set httpClient to use
     *
     * @param httpClient httpClient to use
     * @return PhoneNumberClientBuilder
     */
    public PhoneNumberClientBuilder httpClient(HttpClient httpClient) {
        // implementation
        return this;
    }

    /**
     * Set endpoint of the service
     *
     * @param endpoint url of the service
     * @return PhoneNumberClientBuilder
     */
    public PhoneNumberClientBuilder endpoint(String endpoint) {
        // implementation
        return this;
    }

    /**
     * Set HttpLogOptions for the service
     *
     * @param logOptions HttpLogOptions to use
     * @return PhoneNumberClientBuilder
     */
    public PhoneNumberClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        // implementation
        return this;
    }

    /**
     * Add Policy for the service
     *
     * @param policy HttpPipelinePolicy to use
     * @return PhoneNumberClientBuilder
     */
    public PhoneNumberClientBuilder addPolicy(HttpPipelinePolicy policy) {
        // implementation
        return this;
    }

    /**
     * Set pipeline of the service
     *
     * @param pipeline HttpPipeline to use
     * @return PhoneNumberClientBuilder
     */
    public PhoneNumberClientBuilder pipeline(HttpPipeline pipeline) {
        // implementation
        return this;
    }

    /**
     * Set configuration for the service
     *
     * @param configuration Configuration to use
     * @return PhoneNumberClientBuilder
     */
    public PhoneNumberClientBuilder configuration(Configuration configuration) {
        // implementation
        return this;
    }

    /**
     * Set retry policy for the service
     *
     * @param retryPolicy RetryPolicy to use
     * @return PhoneNumberClientBuilder
     */
    public PhoneNumberClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        // implementation
        return this;
    }

    /**
     * Set the service version
     *
     * @param version ServiceVersion to use
     * @return PhoneNumberClientBuilder
     */
    public PhoneNumberClientBuilder serviceVersion(ServiceVersion version) {
        // implementation
        return this;
    }

    /**
     * Create synchronous client
     *
     * @return PhoneNumberClient instance
     */
    public PhoneNumberClient buildClient() {
        // implementation
        return new PhoneNumberClient();
    }

    /**
     * Create asynchronous client
     *
     * @return PhoneNumberAsyncClient instance
     */
    public PhoneNumberAsyncClient buildAsyncClient() {
        // implementation
        return new PhoneNumberAsyncClient();
    }
}
