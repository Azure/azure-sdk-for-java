// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.containers.containerregistry.specialized;

import com.azure.containers.containerregistry.ContainerRegistryServiceVersion;
import com.azure.containers.containerregistry.implementation.UtilsImpl;
import com.azure.containers.containerregistry.models.ContainerRegistryAudience;
import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.client.traits.ConfigurationTrait;
import com.azure.core.client.traits.EndpointTrait;
import com.azure.core.client.traits.HttpTrait;
import com.azure.core.client.traits.TokenCredentialTrait;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.HttpClientOptions;
import com.azure.core.util.logging.ClientLogger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of {@link
 * ContainerRegistryBlobClient ContainerRegistryBlobClients} and {@link ContainerRegistryBlobAsyncClient ContainerRegistryBlobAsyncClients}, call {@link
 * #buildClient() buildClient} and {@link #buildAsyncClient() buildAsyncClient} respectively to construct an instance of
 * the desired client.
 *
 * <p>Another way to construct the client is using a {@link HttpPipeline}. The pipeline gives the client an
 * authenticated way to communicate with the service but it doesn't contain the service endpoint. Set the pipeline with
 * {@link #pipeline(HttpPipeline) this} and set the service endpoint with {@link #endpoint(String) this}. Using a
 * pipeline requires additional setup but allows for finer control on how the {@link ContainerRegistryBlobClient} and {@link
 * ContainerRegistryBlobAsyncClient} is built.</p>
 * <p>The service does not directly support AAD credentials and as a result the clients internally depend on a policy that converts
 * the AAD credentials to the Azure Container Registry specific service credentials. In case you use your own pipeline, you
 * would need to provide implementation for this policy as well.
 * For more information please see <a href="https://github.com/Azure/acr/blob/main/docs/AAD-OAuth.md"> Azure Container Registry Authentication </a>.</p>
 *
 */
@ServiceClientBuilder(
    serviceClients = {
        ContainerRegistryBlobAsyncClient.class,
        ContainerRegistryBlobClient.class
    })
public final class ContainerRegistryBlobClientBuilder implements
    ConfigurationTrait<ContainerRegistryBlobClientBuilder>,
    EndpointTrait<ContainerRegistryBlobClientBuilder>,
    HttpTrait<ContainerRegistryBlobClientBuilder>,
    TokenCredentialTrait<ContainerRegistryBlobClientBuilder> {
    private final ClientLogger logger = new ClientLogger(ContainerRegistryBlobClientBuilder.class);
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
    private RetryOptions retryOptions;
    private ContainerRegistryServiceVersion version;
    private ContainerRegistryAudience audience;
    private String repositoryName;

    /**
     * Sets the service endpoint for the Azure Container Registry instance.
     *
     * @param endpoint The URL of the Container Registry instance.
     * @return The updated {@link ContainerRegistryBlobClientBuilder} object.
     * @throws IllegalArgumentException If {@code endpoint} is null or it cannot be parsed into a valid URL.
     */
    @Override
    public ContainerRegistryBlobClientBuilder endpoint(String endpoint) {
        try {
            new URL(endpoint);
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("'endpoint' must be a valid URL", ex));
        }

        this.endpoint = endpoint;
        return this;
    }

    /**
     * Sets the repository name for the Azure Container Registry Blob instance.
     *
     * @param repositoryName The URL of the Container Registry instance.
     * @return The updated {@link ContainerRegistryBlobClientBuilder} object.
     */
    public ContainerRegistryBlobClientBuilder repository(String repositoryName) {
        this.repositoryName = repositoryName;
        return this;
    }

    /**
     * Sets the audience for the Azure Container Registry service.
     *
     * @param audience ARM management scope associated with the given registry.
     * @return The updated {@link ContainerRegistryBlobClientBuilder} object.
     */
    public ContainerRegistryBlobClientBuilder audience(ContainerRegistryAudience audience) {
        this.audience = audience;
        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authorize requests sent to the service. Refer to the Azure SDK for Java
     * <a href="https://aka.ms/azsdk/java/docs/identity">identity and authentication</a>
     * documentation for more details on proper usage of the {@link TokenCredential} type.
     *
     * @param credential {@link TokenCredential} used to authorize requests sent to the service.
     * @return The updated {@link ContainerRegistryBlobClientBuilder} object.
     */
    @Override
    public ContainerRegistryBlobClientBuilder credential(TokenCredential credential) {
        this.credential = credential;
        return this;
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
     * <p>
     * If {@code pipeline} is set, all settings other than {@link #endpoint(String) endpoint} are ignored
     * to build {@link ContainerRegistryBlobClient} or {@link ContainerRegistryBlobAsyncClient}.<br>
     * </p>
     *
     * This service takes dependency on an internal policy which converts Azure token credentials into Azure Container Registry specific service credentials.
     * In case you use your own pipeline you will have to create your own credential policy.<br>
     *
     * {For more information please see <a href="https://github.com/Azure/acr/blob/main/docs/AAD-OAuth.md"> Azure Container Registry Authentication </a> }.
     *
     * @param httpPipeline {@link HttpPipeline} to use for sending service requests and receiving responses.
     * @return The updated {@link ContainerRegistryBlobClientBuilder} object.
     */
    @Override
    public ContainerRegistryBlobClientBuilder pipeline(HttpPipeline httpPipeline) {
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
     * @return The updated {@link ContainerRegistryBlobClientBuilder} object.
     */
    public ContainerRegistryBlobClientBuilder serviceVersion(ContainerRegistryServiceVersion version) {
        this.version = version;
        return this;
    }

    /**
     * Sets the {@link HttpClient} to use for sending and receiving requests to and from the service.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, a HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     *
     * @param httpClient The {@link HttpClient} to use for requests.
     * @return The updated {@link ContainerRegistryBlobClientBuilder} object.
     */
    @Override
    public ContainerRegistryBlobClientBuilder httpClient(HttpClient httpClient) {
        if (this.httpClient != null && httpClient == null) {
            logger.info("HttpClient is being set to 'null' when it was previously configured.");
        }
        this.httpClient = httpClient;
        return this;
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
     *
     * @return the updated {@link ContainerRegistryBlobClientBuilder} object
     * @see HttpClientOptions
     */
    @Override
    public ContainerRegistryBlobClientBuilder clientOptions(ClientOptions clientOptions) {
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
     * @return The updated {@link ContainerRegistryBlobClientBuilder} object.
     */
    @Override
    public ContainerRegistryBlobClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
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
     * @param httpLogOptions The {@link HttpLogOptions logging configuration} to use when sending and receiving requests
     * to and from the service.
     * @return The updated {@link ContainerRegistryBlobClientBuilder} object.
     */
    @Override
    public ContainerRegistryBlobClientBuilder httpLogOptions(HttpLogOptions httpLogOptions) {
        this.httpLogOptions = httpLogOptions;
        return this;
    }

    /**
     * Sets the {@link HttpPipelinePolicy} that is used to retry requests.
     * <p>
     * The default retry policy will be used if not provided {@link #buildAsyncClient()} to
     * build {@link ContainerRegistryBlobAsyncClient}.
     *
     * @param retryPolicy The {@link HttpPipelinePolicy} that will be used to retry requests. For example,
     * {@link RetryPolicy} can be used to retry requests.
     *
     * @return The updated ContainerRegistryBlobClientBuilder object.
     */
    public ContainerRegistryBlobClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
        return this;
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
     *
     * @return The updated ContainerRegistryBlobClientBuilder object.
     */
    @Override
    public ContainerRegistryBlobClientBuilder retryOptions(RetryOptions retryOptions) {
        this.retryOptions = retryOptions;
        return this;
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
     * @return The updated ContainerRegistryBlobClientBuilder object.
     * @throws NullPointerException If {@code policy} is null.
     */
    @Override
    public ContainerRegistryBlobClientBuilder addPolicy(HttpPipelinePolicy policy) {
        Objects.requireNonNull(policy, "'policy' cannot be null.");

        if (policy.getPipelinePosition() == HttpPipelinePosition.PER_CALL) {
            perCallPolicies.add(policy);
        } else {
            perRetryPolicies.add(policy);
        }

        return this;
    }

    /**
     * Creates a {@link ContainerRegistryBlobAsyncClient} based on options set in the Builder. Every time {@code
     * buildAsyncClient()} is called a new instance of {@link ContainerRegistryBlobAsyncClient} is created.
     * <p>
     * If {@link #pipeline(HttpPipeline)}  pipeline} is set, then the {@code pipeline} and {@link #endpoint(String) endpoint}
     * are used to create the {@link ContainerRegistryBlobAsyncClient client}. All other builder settings are ignored.
     *
     * @return A {@link ContainerRegistryBlobAsyncClient} with the options set from the builder.
     * @throws NullPointerException If {@code endpoint} is null.
     * You can set the values by calling {@link #endpoint(String)} and {@link #audience(ContainerRegistryAudience)} respectively.
     */
    public ContainerRegistryBlobAsyncClient buildAsyncClient() {
        Objects.requireNonNull(endpoint, "'endpoint' can't be null");

        // Service version
        ContainerRegistryServiceVersion serviceVersion = (version != null)
            ? version
            : ContainerRegistryServiceVersion.getLatest();

        HttpPipeline pipeline = getHttpPipeline();

        ContainerRegistryBlobAsyncClient client = new ContainerRegistryBlobAsyncClient(repositoryName, pipeline, endpoint, serviceVersion.getVersion());
        return client;
    }

    /**
     * Creates a {@link ContainerRegistryBlobClient} based on options set in the Builder. Every time {@code
     * buildAsyncClient()} is called a new instance of {@link ContainerRegistryBlobClient} is created.
     * <p>
     * If {@link #pipeline(HttpPipeline)}  pipeline} is set, then the {@code pipeline} and {@link #endpoint(String) endpoint}
     * are used to create the {@link ContainerRegistryBlobClient client}. All other builder settings are ignored.
     *
     * @return A {@link ContainerRegistryBlobClient} with the options set from the builder.
     * @throws NullPointerException If {@code endpoint} or {@code audience} is null.
     * You can set the values by calling {@link #endpoint(String)} and {@link #audience(ContainerRegistryAudience)} respectively.
     */
    public ContainerRegistryBlobClient buildClient() {
        return new ContainerRegistryBlobClient(buildAsyncClient());
    }

    private HttpPipeline getHttpPipeline() {
        if (httpPipeline != null) {
            return httpPipeline;
        }

        return UtilsImpl.buildHttpPipeline(
            this.clientOptions,
            this.httpLogOptions,
            this.configuration,
            this.retryPolicy,
            this.retryOptions,
            this.credential,
            this.audience,
            this.perCallPolicies,
            this.perRetryPolicies,
            this.httpClient,
            this.endpoint,
            this.version,
            this.logger);
    }
}
