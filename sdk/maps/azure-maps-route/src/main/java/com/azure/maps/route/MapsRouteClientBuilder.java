// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.route;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.client.traits.AzureKeyCredentialTrait;
import com.azure.core.client.traits.ConfigurationTrait;
import com.azure.core.client.traits.EndpointTrait;
import com.azure.core.client.traits.HttpTrait;
import com.azure.core.client.traits.TokenCredentialTrait;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.AzureKeyCredentialPolicy;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.CookiePolicy;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.builder.ClientBuilderUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.maps.route.implementation.RouteClientImpl;
import com.azure.maps.route.implementation.RouteClientImplBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Builder class used to instantiate both synchronous and asynchronous {@link MapsRouteClient} clients.
 * Creating a sync client using a {@link AzureKeyCredential}:
 * <!-- src_embed com.azure.maps.route.sync.builder.key.instantiation -->
 * <pre>
 * &#47;&#47; Authenticates using subscription key
 * AzureKeyCredential keyCredential = new AzureKeyCredential&#40;System.getenv&#40;&quot;SUBSCRIPTION_KEY&quot;&#41;&#41;;
 *
 * &#47;&#47; Creates a builder
 * MapsRouteClientBuilder builder = new MapsRouteClientBuilder&#40;&#41;;
 * builder.credential&#40;keyCredential&#41;;
 * builder.httpLogOptions&#40;new HttpLogOptions&#40;&#41;.setLogLevel&#40;HttpLogDetailLevel.BODY_AND_HEADERS&#41;&#41;;
 *
 * &#47;&#47; Builds the client
 * MapsRouteClient client = builder.buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.maps.route.sync.builder.key.instantiation -->
 * Creating a sync client using a {@link TokenCredential}:
 * <!-- src_embed com.azure.maps.route.sync.builder.ad.instantiation -->
 * <pre>
 * &#47;&#47; Authenticates using Azure AD building a default credential
 * &#47;&#47; This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
 * DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 *
 * &#47;&#47; Creates a builder
 * MapsRouteClientBuilder builder = new MapsRouteClientBuilder&#40;&#41;;
 * builder.credential&#40;tokenCredential&#41;;
 * builder.mapsClientId&#40;System.getenv&#40;&quot;MAPS_CLIENT_ID&quot;&#41;&#41;;
 * builder.httpLogOptions&#40;new HttpLogOptions&#40;&#41;.setLogLevel&#40;HttpLogDetailLevel.BODY_AND_HEADERS&#41;&#41;;
 *
 * &#47;&#47; Builds a client
 * MapsRouteClient client = builder.buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.maps.route.sync.builder.ad.instantiation -->
 */
@ServiceClientBuilder(serviceClients = { MapsRouteClient.class, MapsRouteAsyncClient.class })
public final class MapsRouteClientBuilder
    implements AzureKeyCredentialTrait<MapsRouteClientBuilder>, TokenCredentialTrait<MapsRouteClientBuilder>,
    HttpTrait<MapsRouteClientBuilder>, ConfigurationTrait<MapsRouteClientBuilder>,
    EndpointTrait<MapsRouteClientBuilder> {

    // auth scope
    static final String[] DEFAULT_SCOPES = new String[] { "https://atlas.microsoft.com/.default" };

    // constants
    private static final ClientLogger LOGGER = new ClientLogger(MapsRouteClientBuilder.class);
    private static final String SDK_NAME = "name";
    private static final String SDK_VERSION = "version";
    private static final HttpHeaderName X_MS_CLIENT_ID = HttpHeaderName.fromString("x-ms-client-id");
    private static final Map<String, String> PROPERTIES = CoreUtils.getProperties("azure-maps-route.properties");

    //subscription-key
    static final String MAPS_SUBSCRIPTION_KEY = "subscription-key";

    // instance fields
    private String endpoint;
    private MapsRouteServiceVersion serviceVersion;
    private String mapsClientId;
    private HttpPipeline pipeline;
    private HttpClient httpClient;
    private Configuration configuration;
    private HttpLogOptions httpLogOptions;
    private final List<HttpPipelinePolicy> pipelinePolicies;
    private ClientOptions clientOptions;
    private RetryPolicy retryPolicy;
    private RetryOptions retryOptions;

    // credentials
    private AzureKeyCredential keyCredential;
    private TokenCredential tokenCredential;

    /**
     * Default constructor for the builder class.
     */
    public MapsRouteClientBuilder() {
        this.pipelinePolicies = new ArrayList<>();
    }

    /**
     * Sets the Azure Maps client id for use with Azure AD Authentication. This client id
     * is the account-based GUID that appears on the Azure Maps Authentication page.
     * <p>
     * More details: <a href="https://docs.microsoft.com/azure/azure-maps/azure-maps-authentication">Azure Maps AD Authentication</a>
     *
     * @param mapsClientId the clientId value.
     * @return the SearchClientBuilder.
     */
    public MapsRouteClientBuilder mapsClientId(String mapsClientId) {
        this.mapsClientId = Objects.requireNonNull(mapsClientId, "'mapsClientId' cannot be null.");
        return this;
    }

    /**
     * Set endpoint of the service.
     *
     * @param endpoint url of the service
     * @return SearchClientBuilder
     */
    @Override
    public MapsRouteClientBuilder endpoint(String endpoint) {
        this.endpoint = Objects.requireNonNull(endpoint, "'endpoint' cannot be null.");
        return this;
    }

    /**
     * Sets the {@link MapsRouteServiceVersion} that is used when making API requests.
     * <p>
     * If a service version is not provided, the service version that will be used will be the latest known service
     * version based on the version of the client library being used. If no service version is specified, updating to a
     * newer version of the client library will have the result of potentially moving to a newer service version.
     * <p>
     * Targeting a specific service version may also mean that the service will return an error for newer APIs.
     *
     * @param version {@link MapsRouteServiceVersion} of the service to be used when making requests.
     * @return the updated RouteClientBuilder object
     */
    public MapsRouteClientBuilder serviceVersion(MapsRouteServiceVersion version) {
        this.serviceVersion = version;
        return this;
    }

    /**
     * Sets The HTTP pipeline to send requests through.
     *
     * @param pipeline the pipeline value.
     * @return the SearchClientBuilder.
     */
    @Override
    public MapsRouteClientBuilder pipeline(HttpPipeline pipeline) {
        if (this.pipeline != null && pipeline == null) {
            LOGGER.info("Pipeline is being set to 'null' when it was previously configured.");

        }
        this.pipeline = pipeline;
        return this;
    }

    /**
     * Sets The HTTP client used to send the request.
     *
     * @param httpClient the httpClient value.
     * @return the SearchClientBuilder.
     */
    @Override
    public MapsRouteClientBuilder httpClient(HttpClient httpClient) {
        if (this.httpClient != null && httpClient == null) {
            LOGGER.info("HttpClient is being set to 'null' when it was previously configured.");

        }
        this.httpClient = httpClient;
        return this;
    }

    /**
     * Sets The configuration store that is used during construction of the service client.
     *
     * @param configuration the configuration value.
     * @return the SearchClientBuilder.
     */
    public MapsRouteClientBuilder configuration(Configuration configuration) {
        this.configuration = Objects.requireNonNull(configuration, "'configuration' cannot be null.");
        return this;
    }

    /**
     * Sets The logging configuration for HTTP requests and responses.
     *
     * @param httpLogOptions the httpLogOptions value.
     * @return the SearchClientBuilder.
     */
    @Override
    public MapsRouteClientBuilder httpLogOptions(HttpLogOptions httpLogOptions) {
        this.httpLogOptions = Objects.requireNonNull(httpLogOptions, "'logOptions' cannot be null.");
        return this;
    }

    /**
     * Sets The retry policy that will attempt to retry failed requests, if applicable.
     *
     * @param retryPolicy the retryPolicy value.
     * @return the SearchClientBuilder.
     */
    public MapsRouteClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = Objects.requireNonNull(retryPolicy, "'retryPolicy' cannot be null.");
        return this;
    }

    /**
     * Sets The client options such as application ID and custom headers to set on a request.
     *
     * @param clientOptions the clientOptions value.
     * @return the SearchClientBuilder.
     */
    @Override
    public MapsRouteClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = Objects.requireNonNull(clientOptions, "'clientOptions' cannot be null.");
        return this;
    }

    /**
     * Adds a custom Http pipeline policy.
     *
     * @param customPolicy The custom Http pipeline policy to add.
     * @return the SearchClientBuilder.
     */
    @Override
    public MapsRouteClientBuilder addPolicy(HttpPipelinePolicy customPolicy) {
        pipelinePolicies.add(Objects.requireNonNull(customPolicy, "'customPolicy' cannot be null."));
        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authenticate HTTP requests.
     *
     * @param tokenCredential {@link TokenCredential} used to authenticate HTTP requests.
     * @return The updated {@link MapsRouteClientBuilder} object.
     * @throws NullPointerException If {@code tokenCredential} is null.
     */
    @Override
    public MapsRouteClientBuilder credential(TokenCredential tokenCredential) {
        this.tokenCredential = Objects.requireNonNull(tokenCredential, "'tokenCredential' cannot be null.");
        return this;
    }

    /**
     * Sets the {@link AzureKeyCredential} used to authenticate HTTP requests.
     *
     * @param keyCredential The {@link AzureKeyCredential} used to authenticate HTTP requests.
     * @return The updated {@link MapsRouteClientBuilder} object.
     * @throws NullPointerException If {@code keyCredential} is null.
     */
    @Override
    public MapsRouteClientBuilder credential(AzureKeyCredential keyCredential) {
        this.keyCredential = Objects.requireNonNull(keyCredential, "'keyCredential' cannot be null.");
        return this;
    }

    /**
     * Sets retry options
     *
     * @param retryOptions the retry options for the client
     * @return a reference to this {@code RouteClientBuilder}
     */
    @Override
    public MapsRouteClientBuilder retryOptions(RetryOptions retryOptions) {
        this.retryOptions = retryOptions;
        return this;
    }

    /**
     * Builds an instance of SearchClientImpl with the provided parameters.
     *
     * @return an instance of SearchClientImpl.
     */
    private RouteClientImpl buildInnerClient() {
        if (endpoint == null) {
            this.endpoint = "https://atlas.microsoft.com";
        }
        if (serviceVersion == null) {
            this.serviceVersion = MapsRouteServiceVersion.getLatest();
        }
        if (pipeline == null) {
            this.pipeline = createHttpPipeline();
        }
        // client impl
        RouteClientImplBuilder builder = new RouteClientImplBuilder();
        builder.host(this.endpoint);
        builder.apiVersion(this.serviceVersion.getVersion());
        builder.pipeline(this.pipeline);
        builder.clientId(this.mapsClientId);
        builder.httpClient(this.httpClient);
        builder.httpLogOptions(this.httpLogOptions);

        return builder.buildClient();
    }

    private HttpPipeline createHttpPipeline() {
        Configuration buildConfiguration = (configuration == null)
            ? Configuration.getGlobalConfiguration()
            : configuration;
        if (httpLogOptions == null) {
            httpLogOptions = new HttpLogOptions();
        }
        if (clientOptions == null) {
            clientOptions = new ClientOptions();
        }

        // Configure pipelines and user agent
        List<HttpPipelinePolicy> policies = new ArrayList<>();
        String clientName = PROPERTIES.getOrDefault(SDK_NAME, "JavaSearchSDK");
        String clientVersion = PROPERTIES.getOrDefault(SDK_VERSION, serviceVersion.getVersion());
        String applicationId = CoreUtils.getApplicationId(clientOptions, httpLogOptions);
        policies.add(new UserAgentPolicy(applicationId, clientName, clientVersion, buildConfiguration));

        // configure headers
        HttpHeaders headers = CoreUtils.createHttpHeadersFromClientOptions(clientOptions);
        if (headers != null) {
            policies.add(new AddHeadersPolicy(headers));
        }

        // Authentications
        if (tokenCredential != null) {
            if (this.mapsClientId == null) {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                    "Missing 'mapsClientId' parameter required for Azure AD Authentication"));
            }
            // we need the x-ms-client header
            HttpHeaders clientHeader = new HttpHeaders();
            clientHeader.add(X_MS_CLIENT_ID, this.mapsClientId);
            policies.add(new AddHeadersPolicy(clientHeader));

            // User token based policy
            policies.add(new BearerTokenAuthenticationPolicy(tokenCredential, DEFAULT_SCOPES));
        } else if (keyCredential != null) {
            policies.add(new AzureKeyCredentialPolicy(MAPS_SUBSCRIPTION_KEY, keyCredential));
        } else {
            // Throw exception that credential and tokenCredential cannot be null
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("Missing credential information while building a client."));
        }

        // Add final policies
        HttpPolicyProviders.addBeforeRetryPolicies(policies);
        policies.add(ClientBuilderUtil.validateAndGetRetryPolicy(retryPolicy, retryOptions, new RetryPolicy()));
        policies.add(new CookiePolicy());
        policies.addAll(this.pipelinePolicies);
        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(httpLogOptions));

        // build the http pipeline
        return new HttpPipelineBuilder().policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();
    }

    /**
     * Builds an instance of SearchAsyncClient async client.
     *
     * @return an instance of SearchAsyncClient.
     */
    public MapsRouteAsyncClient buildAsyncClient() {
        return new MapsRouteAsyncClient(buildInnerClient().getRoutes(), this.pipeline);
    }

    /**
     * Builds an instance of SearchClient sync client.
     *
     * @return an instance of SearchClient.
     */
    public MapsRouteClient buildClient() {
        return new MapsRouteClient(buildAsyncClient());
    }
}
