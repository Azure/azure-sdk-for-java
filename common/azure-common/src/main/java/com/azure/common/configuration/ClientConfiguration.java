// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.common.configuration;

import com.azure.common.credentials.AsyncServiceClientCredentials;
import com.azure.common.http.HttpClient;
import com.azure.common.http.policy.HttpLogDetailLevel;
import com.azure.common.http.policy.HttpPipelinePolicy;
import com.azure.common.http.policy.RetryPolicy;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Client configuration settings common to Azure {@link com.azure.common.ServiceClient}. Settings such as
 * {@link HttpClient}, retry policy, service endpoint, etc. can be set here.
 */
public class ClientConfiguration {
    private HttpClient client;
    private AsyncServiceClientCredentials credentials;
    private String userAgent;
    private RetryPolicy retryPolicy;
    private List<HttpPipelinePolicy> policies;
    private HttpLogDetailLevel httpLogDetailLevel;
    private ILoggerFactory loggerFactory;
    private URL serviceEndpoint;

    /**
     * Gets the default configuration settings
     */
    public ClientConfiguration() {
        this.retryPolicy = new RetryPolicy();
        this.policies = new ArrayList<>();
        this.httpLogDetailLevel = HttpLogDetailLevel.NONE;
        this.loggerFactory = LoggerFactory.getILoggerFactory();
    }

    /**
     * Returns the credentials used to authenticate HTTP requests sent.
     *
     * @return Gets the credentials that will be used by ServiceClient to authenticate with.
     */
    public AsyncServiceClientCredentials credentials() {
        return credentials;
    }

    /**
     * Sets the credentials to use when authenticating HTTP requests.
     *
     * @param credentials The credentials to use for authenticating HTTP requests.
     * @return The updated ClientConfiguration object.
     */
    public ClientConfiguration credentials(AsyncServiceClientCredentials credentials) {
        Objects.requireNonNull(credentials);
        this.credentials = credentials;
        return this;
    }

    /**
     * Gets the 'User-Agent' value sent with HTTP requests.
     *
     * @return The user agent value currently sent with HTTP requests.
     */
    public String userAgent() {
        return userAgent;
    }

    /**
     * Sets the new 'User-Agent' value to send with HTTP requests.
     *
     * @param userAgent The 'User-Agent' header value to send with requests.
     * @return The updated ClientConfiguration object.
     */
    public ClientConfiguration userAgent(String userAgent) {
        Objects.requireNonNull(userAgent);
        this.userAgent = userAgent;
        return this;
    }

    /**
     * Gets the current retry policy.
     *
     * @return The retry policy to use for service requests.
     */
    public RetryPolicy retryPolicy() {
        return retryPolicy;
    }

    /**
     * Sets a new retry policy to use for service requests.
     *
     * @param retryPolicy The retry policy for service requests.
     * @return The updated ClientConfiguration object.
     */
    public ClientConfiguration retryPolicy(RetryPolicy retryPolicy) {
        Objects.requireNonNull(retryPolicy);
        this.retryPolicy = retryPolicy;
        return this;
    }

    /**
     * Gets the additional policies to execute for each service request.
     *
     * @return The policies to execute for each service request.
     */
    public List<HttpPipelinePolicy> policies() {
        return policies;
    }

    /**
     * Adds a policy to the set of existing policies that are executed after the {@link com.azure.common.ServiceClient}
     * required policies.
     *
     * @param policy The retry policy for service requests.
     * @return The updated ClientConfiguration object.
     */
    public ClientConfiguration addPolicy(HttpPipelinePolicy policy) {
        Objects.requireNonNull(policy);
        this.policies.add(policy);
        return this;
    }

    /**
     * The HttpClient to use when sending and receiving service requests.
     *
     * @return The client to send and receive HTTP requests and responses.
     */
    public HttpClient httpClient() {
        return client;
    }

    /**
     * Sets the HTTP client to use for sending and receiving requests to and from the service.
     *
     * @param client The HTTP client to use for requests.
     * @return The updated ClientConfiguration object.
     */
    public ClientConfiguration httpClient(HttpClient client) {
        Objects.requireNonNull(client);
        this.client = client;
        return this;
    }

    /**
     * Gets the current log level when logging HTTP requests and responses.
     *
     * @return The log level for logging HTTP requests and responses.
     */
    public HttpLogDetailLevel httpLogDetailLevel() { return httpLogDetailLevel; }

    /**
     * Sets the logging level for HTTP requests and responses.
     *
     * @param logLevel The amount of logging output when sending and receiving HTTP requests/responses.
     * @return The updated ClientConfiguration object.
     */
    public ClientConfiguration httpLogDetailLevel(HttpLogDetailLevel logLevel) {
        this.httpLogDetailLevel = logLevel;
        return this;
    }

    /**
     * Gets the current factory used to generate instances of {@link org.slf4j.Logger}.
     *
     * @return The factory used to generate {@link org.slf4j.Logger} instances.
     */
    public ILoggerFactory loggerFactory() { return loggerFactory; }

    /**
     * Factory used for generating {@link org.slf4j.Logger} instances for SDK logging.
     *
     * @param loggerFactory The LoggerFactory to use for generating Loggers.
     * @return The updated ClientConfiguration object.
     */
    public ClientConfiguration loggerFactory(ILoggerFactory loggerFactory) {
        Objects.requireNonNull(loggerFactory);
        this.loggerFactory = loggerFactory;
        return this;
    }

    /**
     * Gets the service endpoint URL used for making calls to the service.
     *
     * @return A URL pointing to the service's endpoint.
     */
    public URL serviceEndpoint() { return serviceEndpoint; }

    /**
     * Sets the endpoint used when making service calls.
     *
     * @param serviceEndpoint The URL to the Azure service for making service calls.
     * @return The updated ClientConfiguration object.
     */
    public ClientConfiguration serviceEndpoint(String serviceEndpoint) {
        Objects.requireNonNull(serviceEndpoint);

        if (serviceEndpoint.equals("")) {
            throw new IllegalArgumentException("'serviceEndpoint' cannot be empty.");
        }

        try {
            this.serviceEndpoint = new URL(serviceEndpoint);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("'serviceEndpoint' is not a valid URL.", e);
        }

        return this;
    }
}
