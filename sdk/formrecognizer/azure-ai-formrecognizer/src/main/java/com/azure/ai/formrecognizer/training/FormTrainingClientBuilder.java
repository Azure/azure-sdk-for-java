// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.training;

import com.azure.ai.formrecognizer.FormRecognizerServiceVersion;
import com.azure.ai.formrecognizer.implementation.Constants;
import com.azure.ai.formrecognizer.implementation.FormRecognizerClientImpl;
import com.azure.ai.formrecognizer.implementation.FormRecognizerClientImplBuilder;
import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.azure.ai.formrecognizer.implementation.Utility.buildHttpPipeline;

/**
 * This class provides a fluent builder API to help instantiation of {@link FormTrainingClient FormTrainingClient}
 * and {@link FormTrainingAsyncClient FormTrainingAsyncClient}, call {@link #buildClient()} buildClient} and
 * {@link #buildAsyncClient() buildAsyncClient} respectively to construct an instance of the desired client.
 *
 * <p>
 * The client needs the service endpoint of the Azure Form Recognizer to access the resource service.
 * {@link #credential(AzureKeyCredential)} or {@link #credential(TokenCredential) credential(TokenCredential)} gives
 * the builder access credential.
 * </p>
 *
 * <p><strong>Instantiating an asynchronous Form Training Client</strong></p>
 *
 * {@codesnippet com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.initialization}
 *
 * <p><strong>Instantiating a synchronous Form Training Client</strong></p>
 *
 * {@codesnippet com.azure.ai.formrecognizer.training.FormTrainingClient.initialization}
 *
 * <p>
 * Another way to construct the client is using a {@link HttpPipeline}. The pipeline gives the client an
 * authenticated way to communicate with the service. Set the pipeline with {@link #pipeline(HttpPipeline) this} and
 * set the service endpoint with {@link #endpoint(String) this}. Using a
 * pipeline requires additional setup but allows for finer control on how the {@link FormTrainingClient} and
 * {@link FormTrainingAsyncClient} is built.
 * </p>
 *
 * {@codesnippet com.azure.ai.formrecognizer.training.FormTrainingAsyncClient.pipeline.instantiation}
 *
 * @see FormTrainingAsyncClient
 * @see FormTrainingClient
 */
@ServiceClientBuilder(serviceClients = {FormTrainingAsyncClient.class, FormTrainingClient.class})
public final class FormTrainingClientBuilder {
    private final ClientLogger logger = new ClientLogger(FormTrainingClientBuilder.class);

    private final List<HttpPipelinePolicy> perCallPolicies = new ArrayList<>();
    private final List<HttpPipelinePolicy> perRetryPolicies = new ArrayList<>();

    private ClientOptions clientOptions;
    private String endpoint;
    private AzureKeyCredential credential;
    private HttpClient httpClient;
    private HttpLogOptions httpLogOptions;
    private HttpPipeline httpPipeline;
    private Configuration configuration;
    private RetryPolicy retryPolicy;
    private TokenCredential tokenCredential;
    private FormRecognizerServiceVersion version;

    /**
     * Creates a {@link FormTrainingClient} based on options set in the builder. Every time
     * {@code buildClient()} is called a new instance of {@link FormTrainingClient} is created.
     *
     * <p>
     * If {@link #pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and
     * {@link #endpoint(String) endpoint} are used to create the {@link FormTrainingClient client}. All other builder
     * settings are ignored.
     * </p>
     *
     * @return A FormTrainingClient with the options set from the builder.
     * @throws NullPointerException if {@link #endpoint(String) endpoint} or
     * {@link #credential(AzureKeyCredential)} has not been set.
     * @throws IllegalArgumentException if {@link #endpoint(String) endpoint} cannot be parsed into a valid URL.
     */
    public FormTrainingClient buildClient() {
        return new FormTrainingClient(buildAsyncClient());
    }

    /**
     * Creates a {@link FormTrainingAsyncClient} based on options set in the builder. Every time
     * {@code buildAsyncClient()} is called a new instance of {@link FormTrainingAsyncClient} is created.
     *
     * <p>
     * If {@link #pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and
     * {@link #endpoint(String) endpoint} are used to create the {@link FormTrainingClient client}. All other builder
     * settings are ignored.
     * </p>
     *
     * @return A FormTrainingAsyncClient with the options set from the builder.
     * @throws NullPointerException if {@link #endpoint(String) endpoint} or {@link #credential(AzureKeyCredential)}
     * has not been set.
     * @throws IllegalArgumentException if {@link #endpoint(String) endpoint} cannot be parsed into a valid URL.
     */
    public FormTrainingAsyncClient buildAsyncClient() {
        // Endpoint cannot be null, which is required in request authentication
        Objects.requireNonNull(endpoint, "'Endpoint' is required and can not be null.");

        // Global Env configuration store
        final Configuration buildConfiguration =
            (configuration == null) ? Configuration.getGlobalConfiguration().clone() : configuration;

        // Service Version
        final FormRecognizerServiceVersion serviceVersion =
            version != null ? version : FormRecognizerServiceVersion.getLatest();

        HttpPipeline pipeline = httpPipeline;
        // Create a default Pipeline if it is not given
        if (pipeline == null) {
            pipeline = buildHttpPipeline(clientOptions, httpLogOptions, buildConfiguration,
                retryPolicy, credential, tokenCredential, perCallPolicies, perRetryPolicies, httpClient);
        }
        final FormRecognizerClientImpl formRecognizerAPI = new FormRecognizerClientImplBuilder()
                                                               .endpoint(endpoint)
                                                               .apiVersion(serviceVersion.getVersion())
                                                               .pipeline(pipeline)
                                                               .buildClient();

        return new FormTrainingAsyncClient(formRecognizerAPI, serviceVersion);
    }

    /**
     * Sets the service endpoint for the Azure Form Recognizer instance.
     *
     * @param endpoint The URL of the Azure Form Recognizer instance service requests to and receive responses from.
     *
     * @return The updated FormTrainingClientBuilder object.
     * @throws NullPointerException if {@code endpoint} is null.
     * @throws IllegalArgumentException if {@code endpoint} cannot be parsed into a valid URL.
     */
    public FormTrainingClientBuilder endpoint(String endpoint) {
        Objects.requireNonNull(endpoint, "'endpoint' cannot be null.");

        try {
            new URL(endpoint);
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("'endpoint' must be a valid URL.", ex));
        }

        if (endpoint.endsWith("/")) {
            this.endpoint = endpoint.substring(0, endpoint.length() - 1);
        } else {
            this.endpoint = endpoint;
        }

        return this;
    }

    /**
     * Sets the {@link AzureKeyCredential} to use when authenticating HTTP requests for this
     * FormTrainingClientBuilder.
     *
     * @param apiKeyCredential {@link AzureKeyCredential} API key credential.
     *
     * @return The updated FormTrainingClientBuilder object.
     * @throws NullPointerException If {@code apiKeyCredential} is null.
     */
    public FormTrainingClientBuilder credential(AzureKeyCredential apiKeyCredential) {
        this.credential = Objects.requireNonNull(apiKeyCredential, "'apiKeyCredential' cannot be null.");
        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authenticate HTTP requests.
     *
     * @param tokenCredential {@link TokenCredential} used to authenticate HTTP requests.
     * @return The updated {@link FormTrainingClientBuilder} object.
     * @throws NullPointerException If {@code tokenCredential} is null.
     */
    public FormTrainingClientBuilder credential(TokenCredential tokenCredential) {
        this.tokenCredential = Objects.requireNonNull(tokenCredential, "'tokenCredential' cannot be null.");
        return this;
    }

    /**
     * Sets the logging configuration for HTTP requests and responses.
     *
     * <p>If {@code logOptions} isn't provided, the default options will use {@link HttpLogDetailLevel#NONE}
     * which will prevent logging.</p>
     *
     * @param logOptions The logging configuration to use when sending and receiving HTTP requests/responses.
     *
     * @return The updated FormTrainingClientBuilder object.
     */
    public FormTrainingClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        this.httpLogOptions = logOptions;
        return this;
    }

    /**
     * Gets the default Azure Form Training client headers and query parameters that are logged by default if
     * HTTP logging is enabled.
     *
     * @return The default {@link HttpLogOptions} allow list.
     */
    public static HttpLogOptions getDefaultLogOptions() {
        return Constants.DEFAULT_LOG_OPTIONS_SUPPLIER.get();
    }

    /**
     * Sets the client options such as application ID and custom headers to set on a request.
     *
     * @param clientOptions The client options.
     * @return The updated FormTrainingClientBuilder object.
     */
    public FormTrainingClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
        return this;
    }

    /**
     * Adds a policy to the set of existing policies that are executed after required policies.
     *
     * @param policy The retry policy for service requests.
     *
     * @return The updated FormTrainingClientBuilder object.
     * @throws NullPointerException If {@code policy} is null.
     */
    public FormTrainingClientBuilder addPolicy(HttpPipelinePolicy policy) {
        Objects.requireNonNull(policy, "'policy' cannot be null.");

        if (policy.getPipelinePosition() == HttpPipelinePosition.PER_CALL) {
            perCallPolicies.add(policy);
        } else {
            perRetryPolicies.add(policy);
        }
        return this;
    }

    /**
     * Sets the HTTP client to use for sending and receiving requests to and from the service.
     *
     * @param client The HTTP client to use for requests.
     *
     * @return The updated FormTrainingClientBuilder object.
     */
    public FormTrainingClientBuilder httpClient(HttpClient client) {
        if (this.httpClient != null && client == null) {
            logger.info("HttpClient is being set to 'null' when it was previously configured.");
        }

        this.httpClient = client;
        return this;
    }

    /**
     * Sets the HTTP pipeline to use for the service client.
     * <p>
     * If {@code pipeline} is set, all other settings are ignored, aside from
     * {@link FormTrainingClientBuilder#endpoint(String) endpoint} to build {@link FormTrainingAsyncClient} or
     * {@link FormTrainingClient}.
     *
     * @param httpPipeline The HTTP pipeline to use for sending service requests and receiving responses.
     *
     * @return The updated FormTrainingClientBuilder object.
     */
    public FormTrainingClientBuilder pipeline(HttpPipeline httpPipeline) {
        if (this.httpPipeline != null && httpPipeline == null) {
            logger.info("HttpPipeline is being set to 'null' when it was previously configured.");
        }

        this.httpPipeline = httpPipeline;
        return this;
    }

    /**
     * Sets the configuration store that is used during construction of the service client.
     * <p>
     * The default configuration store is a clone of the {@link Configuration#getGlobalConfiguration() global
     * configuration store}, use {@link Configuration#NONE} to bypass using configuration settings during construction.
     *
     * @param configuration The configuration store used to.
     *
     * @return The updated FormTrainingClientBuilder object.
     */
    public FormTrainingClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the {@link RetryPolicy#RetryPolicy()} that is used when each request is sent.
     * <p>
     * The default retry policy will be used if not provided {@link FormTrainingClientBuilder#buildAsyncClient()}
     * to build {@link FormTrainingAsyncClient} or {@link FormTrainingClient}.
     *
     * @param retryPolicy user's retry policy applied to each request.
     *
     * @return The updated FormTrainingClientBuilder object.
     */
    public FormTrainingClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
        return this;
    }

    /**
     * Sets the {@link FormRecognizerServiceVersion} that is used when making API requests.
     * <p>
     * If a service version is not provided, the service version that will be used will be the latest known service
     * version based on the version of the client library being used. If no service version is specified, updating to a
     * newer version the client library will have the result of potentially moving to a newer service version.
     *
     * @param version {@link FormRecognizerServiceVersion} of the service to be used when making requests.
     *
     * @return The updated FormTrainingClientBuilder object.
     */
    public FormTrainingClientBuilder serviceVersion(FormRecognizerServiceVersion version) {
        this.version = version;
        return this;
    }
}
