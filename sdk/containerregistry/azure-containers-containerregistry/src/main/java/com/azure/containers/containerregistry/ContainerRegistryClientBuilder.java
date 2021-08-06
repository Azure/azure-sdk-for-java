// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.models.ContainerRegistryAudience;
import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of {@link
 * ContainerRegistryClient ContainerRegistryClients} and {@link ContainerRegistryAsyncClient ContainerRegistryAsyncClients}, call {@link
 * #buildClient() buildClient} and {@link #buildAsyncClient() buildAsyncClient} respectively to construct an instance of
 * the desired client.
 *
 * <p>The client needs the service endpoint of the Azure Container Registry, Audience for ACR that you want to target and Azure access credentials to use for authentication.
 * <p><strong>Instantiating an asynchronous Container Registry client</strong></p>
 * {@codesnippet com.azure.containers.containerregistry.ContainerRegistryAsyncClient.instantiation}
 *
 * <p><strong>Instantiating a synchronous Container Registry client</strong></p>
 * {@codesnippet com.azure.containers.containerregistry.ContainerRegistryClient.instantiation}
 *
 * <p>Another way to construct the client is using a {@link HttpPipeline}. The pipeline gives the client an
 * authenticated way to communicate with the service but it doesn't contain the service endpoint. Set the pipeline with
 * {@link #pipeline(HttpPipeline) this} and set the service endpoint with {@link #endpoint(String) this}. Using a
 * pipeline requires additional setup but allows for finer control on how the {@link ContainerRegistryClient} and {@link
 * ContainerRegistryAsyncClient} is built.</p>
 * <p>The service does not directly support AAD credentials and as a result the clients internally depend on a policy that converts
 * the AAD credentials to the Azure Container Registry specific service credentials. In case you use your own pipeline, you
 * would need to provide implementation for this policy as well.
 * For more information please see <a href="https://github.com/Azure/acr/blob/main/docs/AAD-OAuth.md"> Azure Container Registry Authentication </a>.</p>
 *
 * <p><strong>Instantiating an asynchronous Container Registry client using a custom pipeline</strong></p>
 * {@codesnippet com.azure.containers.containerregistry.ContainerRegistryAsyncClient.pipeline.instantiation}
 *
 * <p><strong>Instantiating a synchronous Container Registry client with custom pipeline</strong></p>
 * {@codesnippet com.azure.containers.containerregistry.ContainerRegistryClient.pipeline.instantiation}
 *
 *
 * @see ContainerRegistryAsyncClient
 * @see ContainerRegistryClient
 */
@ServiceClientBuilder(
    serviceClients = {
        ContainerRegistryClient.class,
        ContainerRegistryAsyncClient.class
    })
public final class ContainerRegistryClientBuilder {
    private static final String CLIENT_NAME;
    private static final String CLIENT_VERSION;

    static {
        Map<String, String> properties =
            CoreUtils.getProperties("azure-containers-containerregistry.properties");
        CLIENT_NAME = properties.getOrDefault("name", "UnknownName");
        CLIENT_VERSION = properties.getOrDefault("version", "UnknownVersion");
    }

    private final ClientLogger logger = new ClientLogger(ContainerRegistryClientBuilder.class);
    private final List<HttpPipelinePolicy> perCallPolicies = new ArrayList<>();
    private final List<HttpPipelinePolicy> perRetryPolicies = new ArrayList<>();
    private ClientOptions clientOptions;
    private Configuration configuration;
    private String endpoint;
    private HttpClient httpClient;
    private TokenCredential credential;
    private HttpPipeline httpPipeline;
    private HttpLogOptions httpLogOptions;
    private RetryPolicy retryPolicy;
    private ContainerRegistryServiceVersion version;
    private ContainerRegistryAudience audience;

    /**
     * Sets the service endpoint for the Azure Container Registry instance.
     *
     * @param endpoint The URL of the Container Registry instance.
     * @return The updated {@link ContainerRegistryClientBuilder} object.
     * @throws IllegalArgumentException If {@code endpoint} is null or it cannot be parsed into a valid URL.
     */
    public ContainerRegistryClientBuilder endpoint(String endpoint) {
        try {
            new URL(endpoint);
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("'endpoint' must be a valid URL"));
        }

        this.endpoint = endpoint;
        return this;
    }

    /**
     * Sets the audience for the Azure Container Registry service.
     *
     * @param audience ARM management scope associated with the given registry.
     * @throws NullPointerException If {@code audience} is null.
     * @return The updated {@link ContainerRegistryClientBuilder} object.
     */
    public ContainerRegistryClientBuilder audience(ContainerRegistryAudience audience) {
        Objects.requireNonNull(audience, "audience can't be null");
        this.audience = audience;
        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authenticate REST API calls.
     *
     * @param credential Azure token credentials used to authenticate HTTP requests.
     * @return The updated {@link ContainerRegistryClientBuilder} object.
     */
    public ContainerRegistryClientBuilder credential(TokenCredential credential) {
        this.credential = credential;
        return this;
    }

    /**
     * Sets the HTTP pipeline to use for the service client.
     * <p>
     * If {@code pipeline} is set, all settings other than {@link #endpoint(String) endpoint} are ignored
     * to build {@link ContainerRegistryAsyncClient} or {@link ContainerRegistryClient}.<br>
     * </p>
     *
     * This service takes dependency on an internal policy which converts Azure token credentials into Azure Container Registry specific service credentials.
     * In case you use your own pipeline you will have to create your own credential policy.<br>
     *
     * {For more information please see <a href="https://github.com/Azure/acr/blob/main/docs/AAD-OAuth.md"> Azure Container Registry Authentication </a> }.
     *
     * @param httpPipeline The HTTP pipeline to use for sending service requests and receiving responses.
     * @return The updated {@link ContainerRegistryClientBuilder} object.
     */
    public ContainerRegistryClientBuilder pipeline(HttpPipeline httpPipeline) {
        if (this.httpPipeline != null && httpPipeline == null) {
            logger.info("HttpPipeline is being set to 'null' when it was previously configured.");
        }
        this.httpPipeline = httpPipeline;
        return this;
    }

    /**
     * Sets the {@link ContainerRegistryServiceVersion} that is used when making API requests.
     * <p>
     * If a service version is not provided, the service version that will be used will be the latest known service version and so
     * newer version of the client library may result in moving to a newer service version.
     *
     * @param version {@link ContainerRegistryServiceVersion} of the service to be used when making requests.
     * @return The updated {@link ContainerRegistryClientBuilder} object.
     */
    public ContainerRegistryClientBuilder serviceVersion(ContainerRegistryServiceVersion version) {
        this.version = version;
        return this;
    }

    /**
     * Sets the HTTP client to use for sending and receiving requests to and from the service.
     *
     * @param httpClient The HTTP client to use for requests.
     * @return The updated {@link ContainerRegistryClientBuilder} object.
     */
    public ContainerRegistryClientBuilder httpClient(HttpClient httpClient) {
        if (this.httpClient != null && httpClient == null) {
            logger.info("HttpClient is being set to 'null' when it was previously configured.");
        }
        this.httpClient = httpClient;
        return this;
    }

    /**
     * Sets the {@link ClientOptions} which enables various options to be set on the client. For example setting an
     * {@code applicationId} using {@link ClientOptions#setApplicationId(String)} to configure
     * the {@link UserAgentPolicy} for telemetry/monitoring purposes.
     *
     * <p>More About <a href="https://azure.github.io/azure-sdk/general_azurecore.html#telemetry-policy">Azure Core: Telemetry policy</a>
     *
     * @param clientOptions {@link ClientOptions}.
     *
     * @return the updated {@link ContainerRegistryClientBuilder} object
     */
    public ContainerRegistryClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
        return this;
    }

    /**
     * Sets the configuration store that is used during construction of the service client.
     *
     * <p>The default configuration store is a clone of the {@link Configuration#getGlobalConfiguration() global
     * configuration store}, use {@link Configuration#NONE} to bypass using configuration settings during construction.</p>
     *
     * @param configuration The configuration store to be used.
     * @return The updated {@link ContainerRegistryClientBuilder} object.
     */
    public ContainerRegistryClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the logging configuration for HTTP requests and responses.
     *
     * <p> If logLevel is not provided, HTTP request or response logging will not happen.</p>
     *
     * @param httpLogOptions The logging configuration to use when sending and receiving HTTP requests/responses.
     * @return The updated {@link ContainerRegistryClientBuilder} object.
     */
    public ContainerRegistryClientBuilder httpLogOptions(HttpLogOptions httpLogOptions) {
        this.httpLogOptions = httpLogOptions;
        return this;
    }

    /**
     * Sets the {@link HttpPipelinePolicy} that is used to retry requests.
     * <p>
     * The default retry policy will be used if not provided {@link #buildAsyncClient()} to
     * build {@link ContainerRegistryClient} or {@link ContainerRegistryAsyncClient}.
     *
     * @param retryPolicy The {@link HttpPipelinePolicy} that will be used to retry requests. For example,
     * {@link RetryPolicy} can be used to retry requests.
     *
     * @return The updated ContainerRegistryClientBuilder object.
     */
    public ContainerRegistryClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
        return this;
    }

    /**
     * Adds a policy to the set of existing policies.
     *
     * @param policy The policy for service requests.
     * @return The updated ContainerRegistryClientBuilder object.
     * @throws NullPointerException If {@code policy} is null.
     */
    public ContainerRegistryClientBuilder addPolicy(HttpPipelinePolicy policy) {
        Objects.requireNonNull(policy, "'policy' cannot be null.");

        if (policy.getPipelinePosition() == HttpPipelinePosition.PER_CALL) {
            perCallPolicies.add(policy);
        } else {
            perRetryPolicies.add(policy);
        }

        return this;
    }

    /**
     * Creates a {@link ContainerRegistryAsyncClient} based on options set in the Builder. Every time {@code
     * buildAsyncClient()} is called a new instance of {@link ContainerRegistryAsyncClient} is created.
     * <p>
     * If {@link #pipeline(HttpPipeline)}  pipeline} is set, then the {@code pipeline} and {@link #endpoint(String) endpoint}
     * are used to create the {@link ContainerRegistryAsyncClient client}. All other builder settings are ignored.
     *
     * @return A {@link ContainerRegistryAsyncClient} with the options set from the builder.
     * @throws NullPointerException If {@code endpoint} has not been set. You can set it by calling {@link #endpoint(String)}.
     * @throws NullPointerException If {@code audience} has not been set. You can set it by calling {@link #audience(ContainerRegistryAudience)}.
     */
    public ContainerRegistryAsyncClient buildAsyncClient() {
        Objects.requireNonNull(endpoint, "endpoint can't be null");
        Objects.requireNonNull(audience, "audience can't be null");

        // Service version
        ContainerRegistryServiceVersion serviceVersion = (version != null)
            ? version
            : ContainerRegistryServiceVersion.getLatest();

        HttpPipeline pipeline = getHttpPipeline();

        ContainerRegistryAsyncClient client = new ContainerRegistryAsyncClient(pipeline, endpoint, serviceVersion.getVersion());
        return client;
    }

    private HttpPipeline getHttpPipeline() {
        if (httpPipeline != null) {
            return httpPipeline;
        }

        return Utils.buildHttpPipeline(
            this.clientOptions,
            this.httpLogOptions,
            this.configuration,
            this.retryPolicy,
            this.credential,
            this.audience,
            this.perCallPolicies,
            this.perRetryPolicies,
            this.httpClient,
            this.endpoint,
            this.logger);
    }

    /**
     * Creates a {@link ContainerRegistryClient} based on options set in the Builder. Every time {@code
     * buildAsyncClient()} is called a new instance of {@link ContainerRegistryClient} is created.
     * <p>
     * If {@link #pipeline(HttpPipeline)}  pipeline} is set, then the {@code pipeline}
     * and {@link #endpoint(String) endpoint} are used to create the {@link ContainerRegistryClient client}.
     * All other builder settings are ignored.
     *
     * @return A {@link ContainerRegistryClient} with the options set from the builder.
     * @throws NullPointerException If {@code endpoint} has not been set. You can set it by calling {@link #endpoint(String)}.
     * @throws NullPointerException If {@code audience} has not been set. You can set it by calling {@link #audience(ContainerRegistryAudience)}.
     */
    public ContainerRegistryClient buildClient() {
        return new ContainerRegistryClient(buildAsyncClient());
    }
}
