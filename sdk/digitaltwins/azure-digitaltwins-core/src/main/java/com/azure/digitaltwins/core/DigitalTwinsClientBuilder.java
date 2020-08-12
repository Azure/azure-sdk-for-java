// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of {@link DigitalTwinsClient
 * DigitalTwinsClients} and {@link DigitalTwinsAsyncClient DigitalTwinsAsyncClients}, call {@link #buildClient() buildClient} and {@link
 * #buildAsyncClient() buildAsyncClient} respectively to construct an instance of the desired client.
 */
@ServiceClientBuilder(serviceClients = {DigitalTwinsClient.class, DigitalTwinsAsyncClient.class})
public class DigitalTwinsClientBuilder {
    private final List<HttpPipelinePolicy> additionalPolicies = new ArrayList<>();
    // mandatory
    private String endpoint;
    private TokenCredential tokenCredential;
    // optional/have default values
    private DigitalTwinsServiceVersion serviceVersion;
    private HttpPipeline httpPipeline;
    private HttpClient httpClient;
    private HttpLogOptions logOptions;
    private RetryPolicy retryPolicy;

    private static HttpPipeline buildPipeline(TokenCredential tokenCredential, String endpoint,
                                              HttpLogOptions logOptions, HttpClient httpClient,
                                              List<HttpPipelinePolicy> additionalPolicies, RetryPolicy retryPolicy) {
        // Closest to API goes first, closest to wire goes last.
        List<HttpPipelinePolicy> policies = new ArrayList<>();

        // Adds a "x-ms-client-request-id" header to each request. This header is useful for tracing requests through Azure ecosystems
        policies.add(new RequestIdPolicy());

        // Only the RequestIdPolicy will take effect prior to the retry policy
        HttpPolicyProviders.addBeforeRetryPolicies(policies);

        policies.add(retryPolicy);

        // Adds a date header to each HTTP request for tracking purposes
        policies.add(new AddDatePolicy());

        // Add authentication policy so that each HTTP request has authorization header
        HttpPipelinePolicy credentialPolicy = new BearerTokenAuthenticationPolicy(tokenCredential, GetAuthorizationScopes(endpoint));
        policies.add(credentialPolicy);

        policies.addAll(additionalPolicies);

        // Custom policies, authentication policy, and add date policy all take place after the retry policy
        HttpPolicyProviders.addAfterRetryPolicies(policies);

        policies.add(new HttpLoggingPolicy(logOptions));

        return new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();
    }

    private static String[] GetAuthorizationScopes(String endpoint) {
        // Uri representation for azure digital twin app Id "0b07f429-9f4b-4714-9392-cc5e8e80c8b0" in the public cloud.
        String adtPublicCloudAppId = "https://digitaltwins.azure.net";
        String defaultPermissionConsent = "/.default";

        // If the endpoint is in Azure public cloud, the suffix will have "azure.net" or "ppe.net".
        // Once ADT becomes available in other clouds, their corresponding scope has to be matched and set.
        if (endpoint.indexOf("azure.net") > 0
            || endpoint.indexOf("ppe.net") > 0) {
            return new String[]{adtPublicCloudAppId + defaultPermissionConsent};
        }

        throw new IllegalArgumentException(String.format("Azure digital twins instance endpoint %s is not valid.", endpoint));
    }

    /**
     * Create a {@link DigitalTwinsClient} based on the builder settings.
     *
     * @return the created synchronous DigitalTwinsClient
     */
    public DigitalTwinsClient buildClient() {
        return new DigitalTwinsClient(buildAsyncClient());
    }

    /**
     * Create a {@link DigitalTwinsAsyncClient} based on the builder settings.
     *
     * @return the created synchronous DigitalTwinsAsyncClient
     */
    public DigitalTwinsAsyncClient buildAsyncClient() {
        Objects.requireNonNull(tokenCredential, "'tokenCredential' cannot be null.");
        Objects.requireNonNull(endpoint, "'endpoint' cannot be null");

        // Set defaults for these fields if they were not set while building the client
        this.serviceVersion = this.serviceVersion != null ? this.serviceVersion : DigitalTwinsServiceVersion.getLatest();
        this.retryPolicy = this.retryPolicy != null ? this.retryPolicy : new RetryPolicy(); // Default is exponential backoff

        if (this.httpPipeline == null)
        {
            this.httpPipeline = buildPipeline(
                this.tokenCredential,
                this.endpoint,
                this.logOptions,
                this.httpClient,
                this.additionalPolicies,
                this.retryPolicy);
        }

        return new DigitalTwinsAsyncClient(this.httpPipeline, this.serviceVersion, this.endpoint);
    }

    /**
     * Set the service endpoint that the built client will communicate with. This field is mandatory to set.
     *
     * @param endpoint URL of the service.
     * @return the updated DigitalTwinsClientBuilder instance for fluent building.
     */
    public DigitalTwinsClientBuilder endpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    /**
     * Set the authentication token provider that the built client will use for all service requests. This field is
     * mandatory to set unless you set the http pipeline directly and that set pipeline has an authentication policy configured.
     *
     * @param tokenCredential the authentication token provider.
     * @return the updated DigitalTwinsClientBuilder instance for fluent building.
     */
    public DigitalTwinsClientBuilder tokenCredential(TokenCredential tokenCredential) {
        this.tokenCredential = tokenCredential;
        return this;
    }

    /**
     * Sets the {@link DigitalTwinsServiceVersion} that is used when making API requests.
     * <p>
     * If a service version is not provided, the service version that will be used will be the latest known service
     * version based on the version of the client library being used. If no service version is specified, updating to a
     * newer version of the client library will have the result of potentially moving to a newer service version.
     * <p>
     * Targeting a specific service version may also mean that the service will return an error for newer APIs.
     *
     * @param serviceVersion The service API version to use.
     * @return the updated DigitalTwinsClientBuilder instance for fluent building.
     */
    public DigitalTwinsClientBuilder serviceVersion(DigitalTwinsServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;
        return this;
    }

    /**
     * Sets the {@link HttpClient} to use for sending a receiving requests to and from the service.
     *
     * @param httpClient HttpClient to use for requests.
     * @return the updated DigitalTwinsClientBuilder instance for fluent building.
     */
    public DigitalTwinsClientBuilder httpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    /**
     * Sets the {@link HttpLogOptions} for service requests.
     *
     * @param logOptions The logging configuration to use when sending and receiving HTTP requests/responses.
     * @return the updated DigitalTwinsClientBuilder instance for fluent building.
     * @throws NullPointerException If {@code logOptions} is {@code null}.
     */
    public DigitalTwinsClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        this.logOptions = Objects.requireNonNull(logOptions, "'logOptions' cannot be null.");
        return this;
    }

    /**
     * Adds a pipeline policy to apply on each request sent. The policy will be added after the retry policy. If
     * the method is called multiple times, all policies will be added and their order preserved.
     *
     * @param pipelinePolicy a pipeline policy
     * @return the updated DigitalTwinsClientBuilder instance for fluent building.
     * @throws NullPointerException If {@code pipelinePolicy} is {@code null}.
     */
    public DigitalTwinsClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        this.additionalPolicies.add(Objects.requireNonNull(pipelinePolicy, "'pipelinePolicy' cannot be null"));
        return this;
    }

    /**
     * Sets the request retry options for all the requests made through the client. By default, the pipeline will
     * use an exponential backoff retry value as detailed in {@link RetryPolicy#RetryPolicy()}.
     *
     * @param retryPolicy {@link RetryPolicy}.
     * @return the updated DigitalTwinsClientBuilder instance for fluent building.
     * @throws NullPointerException If {@code retryOptions} is {@code null}.
     */
    public DigitalTwinsClientBuilder retryOptions(RetryPolicy retryPolicy) {
        this.retryPolicy = Objects.requireNonNull(retryPolicy, "'retryPolicy' cannot be null.");
        return this;
    }

    /**
     * Sets the {@link HttpPipeline} to use for the service client.
     * <p>
     * If {@code pipeline} is set, all other settings are ignored, aside from {@link #endpoint(String) endpoint}.
     *
     * @param httpPipeline HttpPipeline to use for sending service requests and receiving responses.
     * @return the updated DigitalTwinsClientBuilder instance for fluent building.
     */
    public DigitalTwinsClientBuilder httpPipeline(HttpPipeline httpPipeline) {
        this.httpPipeline = httpPipeline;
        return this;
    }
}
