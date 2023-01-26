// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.client.traits.HttpTrait;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.HttpClientOptions;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.IdentityClientOptions;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Function;

/**
 * The base class for all the credential builders.
 * @param <T> the type of the credential builder
 */
public abstract class CredentialBuilderBase<T extends CredentialBuilderBase<T>> implements HttpTrait<T> {
    private static final ClientLogger LOGGER = new ClientLogger(CredentialBuilderBase.class);
    IdentityClientOptions identityClientOptions;

    CredentialBuilderBase() {
        this.identityClientOptions = new IdentityClientOptions();
    }

    /**
     * Specifies the max number of retries when an authentication request fails.
     *
     * @param maxRetry the number of retries
     * @return An updated instance of this builder with the max retry set as specified.
     */
    @SuppressWarnings("unchecked")
    public T maxRetry(int maxRetry) {
        this.identityClientOptions.setMaxRetry(maxRetry);
        return (T) this;
    }

    /**
     * Specifies a Function to calculate seconds of timeout on every retried request.
     *
     * @param retryTimeout the Function that returns a timeout in seconds given the number of retry
     * @return An updated instance of this builder with the retry timeout set as specified.
     */
    @SuppressWarnings("unchecked")
    public T retryTimeout(Function<Duration, Duration> retryTimeout) {
        this.identityClientOptions.setRetryTimeout(retryTimeout);
        return (T) this;
    }


    /**
     * Specifies the options for proxy configuration.
     *
     * @deprecated Configure the proxy options on the {@link HttpClient} instead and then set that
     * client on the credential using {@link #httpClient(HttpClient)}.
     *
     * @param proxyOptions the options for proxy configuration
     * @return An updated instance of this builder with the proxy options set as specified.
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public T proxyOptions(ProxyOptions proxyOptions) {
        this.identityClientOptions.setProxyOptions(proxyOptions);
        return (T) this;
    }

    /**
     * Specifies the HttpPipeline to send all requests. This setting overrides the others.
     * @deprecated This method is deprecated.
     * <p>Use {@link CredentialBuilderBase#pipeline(HttpPipeline)} instead</p>
     *
     * @param httpPipeline the HttpPipeline to send all requests
     * @return An updated instance of this builder with the http pipeline set as specified.
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public T httpPipeline(HttpPipeline httpPipeline) {
        this.identityClientOptions.setHttpPipeline(httpPipeline);
        return (T) this;
    }

    /**
     * Sets the HTTP client to use for sending and receiving requests to and from the service.
     *
     * @param client The HTTP client to use for requests.
     * @return An updated instance of this builder with the http client set as specified.
     * @throws NullPointerException If {@code client} is {@code null}.
     */
    @SuppressWarnings("unchecked")
    public T httpClient(HttpClient client) {
        Objects.requireNonNull(client);
        this.identityClientOptions.setHttpClient(client);
        return (T) this;
    }

    /**
     * Sets the configuration store that is used during construction of the credential.
     *
     * The default configuration store is a clone of the {@link Configuration#getGlobalConfiguration() global
     * configuration store}.
     *
     * @param configuration The configuration store used to load Env variables and/or properties from.
     *
     * @return An updated instance of this builder with the configuration store set as specified.
     */
    @SuppressWarnings("unchecked")
    public T configuration(Configuration configuration) {
        identityClientOptions.setConfiguration(configuration);
        return (T) this;
    }

    /**
     * Allows for setting common properties such as application ID, headers, proxy configuration, etc. Note that it is
     * recommended that this method be called with an instance of the {@link HttpClientOptions}
     * class (a subclass of the {@link ClientOptions} base class). The HttpClientOptions subclass provides more
     * configuration options suitable for HTTP clients, which is applicable for any class that implements this HttpTrait
     * interface.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, a HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     *
     * @param clientOptions A configured instance of {@link HttpClientOptions}.
     * @see HttpClientOptions
     * @return An updated instance of this builder with the client options configured.
     */
    @SuppressWarnings("unchecked")
    @Override
    public T clientOptions(ClientOptions clientOptions) {
        identityClientOptions.setClientOptions(clientOptions);
        return (T) this;
    }

    /**
     * Sets the {@link HttpLogOptions logging configuration} to use when sending and receiving requests to and from
     * the service. If a {@code logLevel} is not provided, default value of {@link HttpLogDetailLevel#NONE} is set.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, a HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     *
     * @param logOptions The {@link HttpLogOptions logging configuration} to use when sending and receiving requests to
     * and from the service.
     * @return An updated instance of this builder with the Http log options configured.
     */
    @SuppressWarnings("unchecked")
    @Override
    public T httpLogOptions(HttpLogOptions logOptions) {
        identityClientOptions.setHttpLogOptions(logOptions);
        return (T) this;
    }

    /**
     * Sets the {@link RetryPolicy} that is used when each request is sent.
     * Setting this is mutually exclusive with using {@link #retryOptions(RetryOptions)}.
     *
     * The default retry policy will be used in the pipeline, if not provided.
     *
     * @param retryPolicy user's retry policy applied to each request.
     *
     * @return An updated instance of this builder with the retry policy configured.
     */
    @SuppressWarnings("unchecked")
    public T retryPolicy(RetryPolicy retryPolicy) {
        identityClientOptions.setRetryPolicy(retryPolicy);
        return (T) this;
    }

    /**
     * Sets the {@link RetryOptions} for all the requests made through the client.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, a HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     * <p>
     * Setting this is mutually exclusive with using {@link #retryPolicy(RetryPolicy)}.
     *
     * @param retryOptions The {@link RetryOptions} to use for all the requests made through the client.
     * @return An updated instance of this builder with the retry options configured.
     */
    @SuppressWarnings("unchecked")
    @Override
    public T retryOptions(RetryOptions retryOptions) {
        identityClientOptions.setRetryOptions(retryOptions);
        return (T) this;
    }

    /**
     * Adds a {@link HttpPipelinePolicy pipeline policy} to apply on each request sent.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, a HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     *
     * @param policy A {@link HttpPipelinePolicy pipeline policy}.
     * @return An updated instance of this builder with the policy configured.
     *
     * @throws NullPointerException If {@code policy} is {@code null}.
     */
    @Override
    @SuppressWarnings("unchecked")
    public T addPolicy(HttpPipelinePolicy policy) {
        if (policy == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'policy' cannot be null."));
        }

        if (policy.getPipelinePosition() == HttpPipelinePosition.PER_CALL) {
            identityClientOptions.addPerCallPolicy(policy);
        } else {
            identityClientOptions.addPerRetryPolicy(policy);
        }
        return (T) this;
    }

    /**
     * Sets the {@link HttpPipeline} to use for the service client.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, a HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     *
     * @param pipeline {@link HttpPipeline} to use for sending service requests and receiving responses.
     * @return An updated instance of this builder with the http pipeline set as specified.
     */
    @Override
    @SuppressWarnings("unchecked")
    public T pipeline(HttpPipeline pipeline) {
        identityClientOptions.setHttpPipeline(pipeline);
        return (T) this;
    }

    /**
     * Enables account identifiers to be logged on client side for debugging/monitoring purposes.
     * By default, it is disabled.
     * <p>
     * The Account Identifier logs can contain sensitive information and should be enabled on protected machines only.
     * Enabling this logs Application ID, Object ID, Tenant ID and User Principal Name at INFO level when an
     * access token is successfully retrieved. Ensure that INFO level logs are enabled to
     * see the account identifier logs.
     * </p>
     *
     * @return An updated instance of this builder.
     */
    @SuppressWarnings("unchecked")
    public T enableAccountIdentifierLogging() {
        identityClientOptions
            .getIdentityLogOptionsImpl()
            .setLoggingAccountIdentifiersAllowed(true);
        return (T) this;
    }
}
