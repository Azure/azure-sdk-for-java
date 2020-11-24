// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.administration;

/**
 * Builder for creating clients of Communication Service phone number configuration
 */
@ServiceClientBuilder(serviceClients = {PhoneNumberClient.class, PhoneNumberAsyncClient.class})
public final class PhoneNumberClientBuilder {
    public PhoneNumberClientBuilder credential(TokenCredential credential) {
        // implementation
        return this;
    }

    public PhoneNumberClientBuilder httpClient(HttpClient httpClient) {
        // implementation
        return this;
    }

    public PhoneNumberClientBuilder endpoint(String endpoint) {
        // implementation
        return this;
    }

    public PhoneNumberClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        // implementation
        return this;
    }

    public PhoneNumberClientBuilder addPolicy(HttpPipelinePolicy policy) {
        // implementation
        return this;
    }

    public PhoneNumberClientBuilder pipeline(HttpPipeline pipeline) {
        // implementation
        return this;
    }

    public PhoneNumberClientBuilder configuration(Configuration configuration) {
        // implementation
        return this;
    }

    public PhoneNumberClientBuilder retryPolicy(HttpPipelinePolicy retryPolicy) {
        // implementation
        return this;
    }

    public PhoneNumberClientBuilder serviceVersion(ServiceVersion version) {
        // implementation
        return this;
    }

    public PhoneNumberClient buildClient() {
        // implementation
        return new PhoneNumberClient();
    }

    public PhoneNumberAsyncClient buildAsyncClient() {
        // implementation
        return new PhoneNumberAsyncClient();
    }
}
