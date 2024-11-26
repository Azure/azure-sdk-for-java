// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.client.traits.AzureKeyCredentialTrait;
import com.azure.core.client.traits.ConfigurationTrait;
import com.azure.core.client.traits.EndpointTrait;
import com.azure.core.client.traits.HttpTrait;
import com.azure.core.client.traits.TokenCredentialTrait;
import com.azure.core.credential.AzureKeyCredential;
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
import com.azure.search.documents.models.SearchAudience;
import com.azure.search.documents.SearchServiceVersion;
import com.azure.search.documents.implementation.util.Constants;
import com.azure.search.documents.implementation.util.Utility;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of {@link
 * SearchIndexerClient SearchIndexerClients} and {@link SearchIndexerAsyncClient SearchIndexerAsyncClients}.
 *
 * <h2>
 *     Overview
 * </h2>
 *
 * <p>
 *     This client allows you to create instances of {@link SearchIndexerClient} and {@link SearchIndexerAsyncClient} to
 *     utilize synchronous and asynchronous APIs respectively to interact with Azure AI Search.
 * </p>
 *
 * <h2>
 *     Getting Started
 * </h2>
 *
 * <h3>
 *     Authentication
 * </h3>
 *
 * <p>
 *     Azure AI Search supports <a href="https://learn.microsoft.com/azure/search/search-security-rbac?tabs=config-svc-portal%2Croles-portal%2Ctest-portal%2Ccustom-role-portal%2Cdisable-keys-portal">
 *         Microsoft Entra ID (role-based) authentication </a> and <a href="https://learn.microsoft.com/azure/search/search-security-api-keys?tabs=portal-use%2Cportal-find%2Cportal-query">API keys</a> for authentication.
 * </p>
 *
 * <p>
 *     For more information about the scopes of authorization, see the <a href="https://learn.microsoft.com/azure/search/search-security-overview#authentication">Azure AI Search Security Overview</a> documentation.
 * </p>
 *
 * <h4>
 *     Building and Authenticating a {@link SearchIndexerClient} or {@link SearchIndexerAsyncClient} using API keys
 * </h4>
 *
 * <p>
 *     To build an instance of {@link SearchIndexerClient} or {@link SearchIndexerAsyncClient} using API keys, call
 *     {@link #buildClient() buildClient} and {@link #buildAsyncClient() buildAsyncClient} respectively from the
 *     {@link SearchIndexerClientBuilder}.
 * </p>
 *
 * <p>
 *     The following must be provided to construct a client instance:
 * </p>
 *
 * <ul>
 *     <li>The Azure AI Search service URL.</li>
 *     <li>An {@link AzureKeyCredential API Key} that grants access to the Azure AI Search service.</li>
 * </ul>
 *
 * <p><strong>Instantiating a synchronous Search Indexer Client</strong></p>
 *
 * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClientBuilder.instantiation.SearchIndexerClient -->
 * <pre>
 * SearchIndexerClient searchIndexerClient = new SearchIndexerClientBuilder&#40;&#41;
 *     .credential&#40;new AzureKeyCredential&#40;&quot;&#123;key&#125;&quot;&#41;&#41;
 *     .endpoint&#40;&quot;&#123;endpoint&#125;&quot;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.search.documents.indexes.SearchIndexerClientBuilder.instantiation.SearchIndexerClient -->
 *
 * <p><strong>Instantiating an asynchronous Search Indexer Client</strong></p>
 *
 * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClientBuilder.instantiation.SearchIndexerAsyncClient -->
 * <pre>
 * SearchIndexerAsyncClient searchIndexerAsyncClient = new SearchIndexerClientBuilder&#40;&#41;
 *     .credential&#40;new AzureKeyCredential&#40;&quot;&#123;key&#125;&quot;&#41;&#41;
 *     .endpoint&#40;&quot;&#123;endpoint&#125;&quot;&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.search.documents.indexes.SearchIndexerClientBuilder.instantiation.SearchIndexerAsyncClient -->
 *
 * <h4>
 *     Building and Authenticating a {@link SearchIndexerClient} or {@link SearchIndexerAsyncClient} using Microsoft Entra ID
 * </h4>
 *
 * <p>
 *   You can also create a {@link SearchIndexerClient} or {@link SearchIndexerAsyncClient} using Microsoft Entra ID
 *   authentication. Your user or service principal must be assigned the "Search Index Data Reader" role. Using the
 *   DefaultAzureCredential you can authenticate a service using Managed Identity or a service principal, authenticate
 *   as a developer working on an application, and more all without changing code. Please refer the <a href="https://learn.microsoft.com/azure/search/search-security-rbac?tabs=config-svc-portal,roles-portal,test-portal,custom-role-portal,disable-keys-portal">documentation</a> for
 *   instructions on how to connect to Azure AI Search using Azure role-based access control (Azure RBAC).
 * </p>
 *
 * <p>
 *     Before you can use the `DefaultAzureCredential`, or any credential type from Azure.Identity, you'll first need to install the Azure.Identity package.
 * </p>
 *
 * <p>
 *     To use DefaultAzureCredential with a client ID and secret, you'll need to set the `AZURE_TENANT_ID`, `AZURE_CLIENT_ID`,
 *     and `AZURE_CLIENT_SECRET` environment variables; alternatively, you can pass those values to the
 *     `ClientSecretCredential` also in azure-identity.
 * </p>
 *
 * <p>
 *     Make sure you use the right namespace for DefaultAzureCredential at the top of your source file:
 * </p>
 *
 * <!-- src_embed DefaultAzureCredentialImports -->
 * <pre>
 * import com.azure.identity.DefaultAzureCredential;
 * import com.azure.identity.DefaultAzureCredentialBuilder;
 * </pre>
 * <!-- end DefaultAzureCredentialImports -->
 *
 * <p>
 *     Then you can create an instance of DefaultAzureCredential and pass it to a new instance of your client:
 * </p>
 *
 * <p><strong>Instantiating a synchronous Search Indexer Client</strong></p>
 *
 * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClientBuilder-classLevelJavaDoc.DefaultAzureCredential -->
 * <pre>
 * DefaultAzureCredential credential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 *
 * SearchIndexerClient searchIndexerClient = new SearchIndexerClientBuilder&#40;&#41;
 *     .endpoint&#40;&quot;&#123;endpoint&#125;&quot;&#41;
 *     .credential&#40;credential&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.search.documents.indexes.SearchIndexerClientBuilder-classLevelJavaDoc.DefaultAzureCredential -->
 *
 * <p><strong>Instantiating an asynchronous Search Indexer Client</strong></p>
 *
 * <!-- src_embed com.azure.search.documents.indexes.SearchIndexerClientBuilder-classLevelJavaDoc.async.DefaultAzureCredential -->
 * <pre>
 * DefaultAzureCredential credential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 *
 * SearchIndexerAsyncClient searchIndexerAsyncClient = new SearchIndexerClientBuilder&#40;&#41;
 *     .endpoint&#40;&quot;&#123;endpoint&#125;&quot;&#41;
 *     .credential&#40;credential&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.search.documents.indexes.SearchIndexerClientBuilder-classLevelJavaDoc.async.DefaultAzureCredential -->
 *
 * @see SearchIndexerClient
 * @see SearchIndexerAsyncClient
 * @see com.azure.search.documents.indexes
 */
@ServiceClientBuilder(serviceClients = {SearchIndexerClient.class, SearchIndexerAsyncClient.class})
public class SearchIndexerClientBuilder implements
    AzureKeyCredentialTrait<SearchIndexerClientBuilder>,
    ConfigurationTrait<SearchIndexerClientBuilder>,
    EndpointTrait<SearchIndexerClientBuilder>,
    HttpTrait<SearchIndexerClientBuilder>,
    TokenCredentialTrait<SearchIndexerClientBuilder> {
    private static final ClientLogger LOGGER = new ClientLogger(SearchIndexerClientBuilder.class);

    private final List<HttpPipelinePolicy> perCallPolicies = new ArrayList<>();
    private final List<HttpPipelinePolicy> perRetryPolicies = new ArrayList<>();

    private AzureKeyCredential azureKeyCredential;
    private TokenCredential tokenCredential;
    private SearchAudience audience;

    private SearchServiceVersion serviceVersion;
    private String endpoint;
    private HttpClient httpClient;
    private HttpPipeline httpPipeline;
    private ClientOptions clientOptions;
    private HttpLogOptions httpLogOptions;
    private Configuration configuration;
    private RetryPolicy retryPolicy;
    private RetryOptions retryOptions;

    /**
     * Creates a builder instance that is able to configure and construct {@link SearchIndexerClient
     * SearchIndexerClients} and {@link SearchIndexerAsyncClient SearchIndexerAsyncClients}.
     */
    public SearchIndexerClientBuilder() {
    }

    /**
     * Creates a {@link SearchIndexerClient} based on options set in the Builder. Every time {@code buildClient()} is
     * called a new instance of {@link SearchIndexerClient} is created.
     * <p>
     * If {@link #pipeline(HttpPipeline) pipeline} is set, then only the {@code pipeline} and {@link #endpoint(String)
     * endpoint} are used to create the {@link SearchIndexerClient client}. All other builder settings are ignored.
     *
     * @return A SearchIndexerClient with the options set from the builder.
     * @throws NullPointerException If {@code endpoint} are {@code null}.
     * @throws IllegalStateException If both {@link #retryOptions(RetryOptions)}
     * and {@link #retryPolicy(RetryPolicy)} have been set.
     */
    public SearchIndexerClient buildClient() {
        Objects.requireNonNull(endpoint, "'endpoint' cannot be null.");

        SearchServiceVersion buildVersion = (serviceVersion == null)
            ? SearchServiceVersion.getLatest()
            : serviceVersion;

        if (httpPipeline != null) {
            return new SearchIndexerClient(endpoint, buildVersion, httpPipeline);
        }

        HttpPipeline pipeline = Utility.buildHttpPipeline(clientOptions, httpLogOptions, configuration,
            retryPolicy, retryOptions, azureKeyCredential, tokenCredential, audience, perCallPolicies, perRetryPolicies,
            httpClient, LOGGER);

        return new SearchIndexerClient(endpoint, buildVersion, pipeline);
    }

    /**
     * Creates a {@link SearchIndexerAsyncClient} based on options set in the Builder. Every time {@code
     * buildAsyncClient()} is called a new instance of {@link SearchIndexerAsyncClient} is created.
     * <p>
     * If {@link #pipeline(HttpPipeline) pipeline} is set, then only the {@code pipeline} and {@link #endpoint(String)
     * endpoint} are used to create the {@link SearchIndexerAsyncClient client}. All other builder settings are
     * ignored.
     *
     * @return A SearchIndexerAsyncClient with the options set from the builder.
     * @throws NullPointerException If {@code endpoint} are {@code null}.
     * @throws IllegalStateException If both {@link #retryOptions(RetryOptions)}
     * and {@link #retryPolicy(RetryPolicy)} have been set.
     */
    public SearchIndexerAsyncClient buildAsyncClient() {
        Objects.requireNonNull(endpoint, "'endpoint' cannot be null.");

        SearchServiceVersion buildVersion = (serviceVersion == null)
            ? SearchServiceVersion.getLatest()
            : serviceVersion;

        if (httpPipeline != null) {
            return new SearchIndexerAsyncClient(endpoint, buildVersion, httpPipeline);
        }

        HttpPipeline pipeline = Utility.buildHttpPipeline(clientOptions, httpLogOptions, configuration,
            retryPolicy, retryOptions, azureKeyCredential, tokenCredential, audience, perCallPolicies, perRetryPolicies,
            httpClient, LOGGER);

        return new SearchIndexerAsyncClient(endpoint, buildVersion, pipeline);
    }

    /**
     * Sets the service endpoint for the Azure AI Search instance.
     *
     * @param endpoint The URL of the Azure AI Search instance.
     * @return The updated SearchIndexerClientBuilder object.
     * @throws IllegalArgumentException If {@code endpoint} is null or it cannot be parsed into a valid URL.
     */
    @Override
    public SearchIndexerClientBuilder endpoint(String endpoint) {
        try {
            new URL(endpoint);
        } catch (MalformedURLException ex) {
            throw LOGGER.logExceptionAsWarning(new IllegalArgumentException("'endpoint' must be a valid URL", ex));
        }
        this.endpoint = endpoint;
        return this;
    }

    /**
     * Sets the {@link AzureKeyCredential} used to authenticate HTTP requests.
     *
     * @param credential The {@link AzureKeyCredential} used to authenticate HTTP requests.
     * @return The updated SearchIndexerClientBuilder object.
     */
    @Override
    public SearchIndexerClientBuilder credential(AzureKeyCredential credential) {
        this.azureKeyCredential = credential;
        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authorize requests sent to the service. Refer to the Azure SDK for Java
     * <a href="https://aka.ms/azsdk/java/docs/identity">identity and authentication</a>
     * documentation for more details on proper usage of the {@link TokenCredential} type.
     *
     * @param credential {@link TokenCredential} used to authorize requests sent to the service.
     * @return The updated SearchIndexerClientBuilder object.
     */
    @Override
    public SearchIndexerClientBuilder credential(TokenCredential credential) {
        this.tokenCredential = credential;
        return this;
    }

    /**
     * Sets the Audience to use for authentication with Microsoft Entra ID.
     * <p>
     * The audience is not considered when using a {@link #credential(AzureKeyCredential) shared key}.
     * <p>
     * If {@code audience} is null the public cloud audience will be assumed.
     *
     * @param audience The Audience to use for authentication with Microsoft Entra ID.
     * @return The updated SearchClientBuilder object.
     */
    public SearchIndexerClientBuilder audience(SearchAudience audience) {
        this.audience = audience;
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
     * @param logOptions The {@link HttpLogOptions logging configuration} to use when sending and receiving requests to
     * and from the service.
     * @return The updated SearchIndexerClientBuilder object.
     */
    @Override
    public SearchIndexerClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        httpLogOptions = logOptions;
        return this;
    }

    /**
     * Gets the default Azure Search headers and query parameters allow list.
     *
     * @return The default {@link HttpLogOptions} allow list.
     */
    public static HttpLogOptions getDefaultLogOptions() {
        return Constants.DEFAULT_LOG_OPTIONS_SUPPLIER.get();
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
     * @return The updated SearchIndexerClientBuilder object.
     * @see HttpClientOptions
     */
    @Override
    public SearchIndexerClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
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
     * @return The updated SearchIndexerClientBuilder object.
     * @throws NullPointerException If {@code policy} is {@code null}.
     */
    @Override
    public SearchIndexerClientBuilder addPolicy(HttpPipelinePolicy policy) {
        Objects.requireNonNull(policy, "'policy' cannot be null.");

        if (policy.getPipelinePosition() == HttpPipelinePosition.PER_CALL) {
            perCallPolicies.add(policy);
        } else {
            perRetryPolicies.add(policy);
        }

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
     * @param client The {@link HttpClient} to use for requests.
     * @return The updated SearchIndexerClientBuilder object.
     */
    @Override
    public SearchIndexerClientBuilder httpClient(HttpClient client) {
        if (this.httpClient != null && client == null) {
            LOGGER.info("HttpClient is being set to 'null' when it was previously configured.");
        }

        this.httpClient = client;
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
     * If {@code pipeline} is set, all other settings are ignored, aside from {@link #endpoint(String) endpoint} when
     * building a {@link SearchIndexerClient} or {@link SearchIndexerAsyncClient}.
     *
     * @param httpPipeline {@link HttpPipeline} to use for sending service requests and receiving responses.
     * @return The updated SearchIndexerClientBuilder object.
     */
    @Override
    public SearchIndexerClientBuilder pipeline(HttpPipeline httpPipeline) {
        if (this.httpPipeline != null && httpPipeline == null) {
            LOGGER.info("HttpPipeline is being set to 'null' when it was previously configured.");
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
     * @param configuration The configuration store that will be used.
     * @return The updated SearchIndexerClientBuilder object.
     */
    @Override
    public SearchIndexerClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the {@link HttpPipelinePolicy} that will attempt to retry requests when needed.
     * <p>
     * A default retry policy will be supplied if one isn't provided.
     * <p>
     * Setting this is mutually exclusive with using {@link #retryOptions(RetryOptions)}.
     *
     * @param retryPolicy The {@link RetryPolicy} that will attempt to retry requests when needed.
     * @return The updated SearchIndexerClientBuilder object.
     */
    public SearchIndexerClientBuilder retryPolicy(RetryPolicy retryPolicy) {
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
     * @return The updated SearchIndexerClientBuilder object.
     */
    @Override
    public SearchIndexerClientBuilder retryOptions(RetryOptions retryOptions) {
        this.retryOptions = retryOptions;
        return this;
    }

    /**
     * Sets the {@link SearchServiceVersion} that is used when making API requests.
     * <p>
     * If a service version is not provided, {@link SearchServiceVersion#getLatest()} will be used as a default. When
     * this default is used updating to a newer client library may result in a newer version of the service being used.
     *
     * @param serviceVersion The version of the service to be used when making requests.
     * @return The updated SearchIndexerClientBuilder object.
     */
    public SearchIndexerClientBuilder serviceVersion(SearchServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;
        return this;
    }
}
