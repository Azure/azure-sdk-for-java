// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management.resources.fluentcore.arm;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.util.Configuration;

import java.net.Proxy;
import java.util.concurrent.TimeUnit;

/**
 * The base interface for allowing configurations to be made on the HTTP client.
 *
 * @param <T> the actual type of the interface extending this interface
 */
public interface AzureConfigurable<T extends AzureConfigurable<T>> {

    /**
     * Set the logging options on the HTTP client.
     *
     * @param logOptions the HttpLogDetailLevel logging options
     * @return the configurable object itself
     */
    T withLogOptions(HttpLogOptions logOptions);

    /**
     * Sets the logging detail level on the HTTP client.
     *
     * If set, this configure will override {@link HttpLogOptions#setLogLevel(HttpLogDetailLevel)} configure of
     * {@link AzureConfigurable#withLogOptions(HttpLogOptions)}.

     * @param logLevel the logging level
     * @return the configurable object itself
     */
    T withLogLevel(HttpLogDetailLevel logLevel);

    /**
     * Plug in a policy into the HTTP pipeline.
     *
     * @param policy the policy to plug in
     * @return the configurable object itself
     */
    T withPolicy(HttpPipelinePolicy policy);

    /**
     * Set the cross-tenant auxiliary credentials for Azure which can hold up to three.
     *
     * @param tokens the AzureTokenCredential list
     * @return the configurable object itself
     */
    T withAuxiliaryCredentials(TokenCredential... tokens);

    /**
     * Specify the user agent header.
     *
     * @param userAgent the user agent to use
     * @return the configurable object itself
     */
    T withUserAgent(String userAgent);

    /**
     * Set the read timeout on the HTTP client. Default is 10 seconds.
     *
     * @param timeout the timeout numeric value
     * @param unit the time unit for the numeric value
     * @return the configurable object itself for chaining
     */
    T withReadTimeout(long timeout, TimeUnit unit);

    /**
     * Set the connection timeout on the HTTP client. Default is 10 seconds.
     *
     * @param timeout the timeout numeric value
     * @param unit the time unit for the numeric value
     * @return the configurable object itself for chaining
     */
    T withConnectionTimeout(long timeout, TimeUnit unit);

    /**
     * Sets whether to use the thread pool in OkHttp/Netty client or Reactor schedulers.
     * If set to true, the thread pool in Http client will be used. Default is false.
     *
     * @param useHttpClientThreadPool whether to use the thread pool in Okhttp/Netty client. Default is false.
     * @return the configurable object itself for chaining
     */
    T useHttpClientThreadPool(boolean useHttpClientThreadPool);


    /**
     * Sets the proxy for the HTTP client.
     *
     * @param proxy the proxy to use
     * @return the configurable object itself for chaining
     */
    T withProxy(Proxy proxy);

    /**
     * Sets the credential scope.
     *
     * @param scope the credential scope
     * @return the configurable object itself for chaining
     */
    T withScope(String scope);

    /**
     * Sets the http client.
     *
     * @param httpClient the http client
     * @return the configurable object itself for chaining
     */
    T withHttpClient(HttpClient httpClient);

    /**
     * Sets the configuration.
     *
     * @param configuration the proxy to use
     * @return the configurable object itself for chaining
     */
    T withConfiguration(Configuration configuration);
}
