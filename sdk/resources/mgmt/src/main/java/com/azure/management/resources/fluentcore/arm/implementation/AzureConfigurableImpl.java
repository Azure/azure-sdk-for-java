/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.resources.fluentcore.arm.implementation;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.serializer.AzureJacksonAdapter;
import com.azure.management.AzureTokenCredential;
import com.azure.management.RestClient;
import com.azure.management.RestClientBuilder;
import com.azure.management.resources.fluentcore.arm.AzureConfigurable;
import com.azure.management.resources.fluentcore.policy.ProviderRegistrationPolicy;
import com.azure.management.resources.fluentcore.policy.ResourceManagerThrottlingPolicy;

import java.net.Proxy;
import java.util.concurrent.TimeUnit;

/**
 * The implementation for {@link AzureConfigurable <T>} and the base class for
 * configurable implementations.
 *
 * @param <T> the type of the configurable interface
 */
public class AzureConfigurableImpl<T extends AzureConfigurable<T>>
        implements AzureConfigurable<T> {
    protected RestClientBuilder restClientBuilder;

    protected AzureConfigurableImpl() {
        this.restClientBuilder = new RestClientBuilder()
                .withSerializerAdapter(new AzureJacksonAdapter());
    }

    @Override
    public T withLogOptions(HttpLogOptions level) {
        this.restClientBuilder = this.restClientBuilder.withHttpLogOptions(level);
        return (T) this;
    }

    @Override
    public T withPolicy(HttpPipelinePolicy policy) {
        this.restClientBuilder = this.restClientBuilder.withPolicy(policy);
        return (T) this;
    }

    @Override
    public T withAuxiliaryCredentials(AzureTokenCredential... tokens) {
        return null;
    }

//    @Override
//    public T withAuxiliaryCredentials(AzureTokenCredentials... tokens) {
//        if (tokens != null) {
//            if (tokens.length > 3) {
//                throw new IllegalArgumentException("Only can hold up to three auxiliary tokens.");
//            }
//            AuxiliaryCredentialsInterceptor interceptor = new AuxiliaryCredentialsInterceptor(tokens);
//            this.restClientBuilder = this.restClientBuilder.withInterceptor(interceptor);
//        }
//        return (T) this;
//    }

    @Override
    public T withUserAgent(String userAgent) {
        this.restClientBuilder = this.restClientBuilder.withUserAgent(userAgent);
        return (T) this;
    }

    @Override
    public T withReadTimeout(long timeout, TimeUnit unit) {
        return null;
    }

    @Override
    public T withConnectionTimeout(long timeout, TimeUnit unit) {
        return null;
    }

    @Override
    public T useHttpClientThreadPool(boolean useHttpClientThreadPool) {
        return null;
    }

    @Override
    public T withProxy(Proxy proxy) {
        return null;
    }

    protected RestClient buildRestClient(AzureTokenCredential credential, AzureEnvironment.Endpoint endpoint) {
        RestClient client = restClientBuilder
                .withBaseUrl(credential.getEnvironment(), endpoint)
                .withCredential(credential)
//                .withPolicy(new ProviderRegistrationPolicy())
//                .withPolicy(new ResourceManagerThrottlingPolicy())
                .buildClient();
        // TODO: Add proxy support
//        if (client.httpClient().proxy() != null) {
//            credentials.withProxy(client.httpClient().proxy());
//        }
        return client;
    }

    protected RestClient buildRestClient(AzureTokenCredential credential) {
        return buildRestClient(credential, AzureEnvironment.Endpoint.RESOURCE_MANAGER);
    }
}
