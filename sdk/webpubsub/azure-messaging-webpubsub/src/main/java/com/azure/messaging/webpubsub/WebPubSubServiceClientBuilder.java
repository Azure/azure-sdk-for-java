// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.client.traits.AzureKeyCredentialTrait;
import com.azure.core.client.traits.ConfigurationTrait;
import com.azure.core.client.traits.ConnectionStringTrait;
import com.azure.core.client.traits.EndpointTrait;
import com.azure.core.client.traits.HttpTrait;
import com.azure.core.client.traits.TokenCredentialTrait;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.CookiePolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
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
import com.azure.core.util.HttpClientOptions;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.builder.ClientBuilderUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.webpubsub.implementation.AzureWebPubSubServiceRestApiImpl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * This class provides a fluent builder API to aid the configuration and instantiation of
 * {@link WebPubSubServiceClient sync} and {@link WebPubSubServiceAsyncClient async} Azure Web Pub Sub clients, using the
 * {@link #buildClient() buildClient} and {@link #buildAsyncClient() buildAsyncClient} methods respectively.
 *
 * <p>To fully configure a Azure Web Pub Sub client, it is necessary to supply a
 * {@link #connectionString(String) connection string} retrieved from the Azure Portal, or else a combination of
 * {@link #credential(AzureKeyCredential) credential} and {@link #endpoint(String) endpoint}.</p>
 *
 * <p>An Azure Web Pub Sub client is required to connect to a specific {@link #hub(String) hub}. An exception will be
 * thrown when the build methods are called if the hub value is null or an empty String.</p>
 *
 * <p><strong>Code Samples</strong></p>
 *
 * <!-- src_embed com.azure.messaging.webpubsub.webpubsubclientbuilder.connectionstring.async -->
 * <pre>
 * WebPubSubServiceAsyncClient client = new WebPubSubServiceClientBuilder&#40;&#41;
 *     .connectionString&#40;&quot;&lt;Insert connection string from Azure Portal&gt;&quot;&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.webpubsub.webpubsubclientbuilder.connectionstring.async -->
 *
 * <p>This demonstrates using the connection string provided by the Azure Portal. Another approach is to use the
 * combination of credential and endpoint details, as shown below:</p>
 *
 * <!-- src_embed com.azure.messaging.webpubsub.webpubsubclientbuilder.credential.endpoint.async -->
 * <pre>
 * WebPubSubServiceAsyncClient client = new WebPubSubServiceClientBuilder&#40;&#41;
 *     .credential&#40;new AzureKeyCredential&#40;&quot;&lt;Insert key from Azure Portal&gt;&quot;&#41;&#41;
 *     .endpoint&#40;&quot;&lt;Insert endpoint from Azure Portal&gt;&quot;&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.webpubsub.webpubsubclientbuilder.credential.endpoint.async -->
 *
 * <p>Of course, synchronous clients may also be instantiated, by calling {@link #buildClient() buildClient} rather than
 * {@link #buildAsyncClient() buildAsyncClient}.</p>
 *
 * <!-- src_embed com.azure.messaging.webpubsub.webpubsubclientbuilder.connectionstring.sync -->
 * <pre>
 * WebPubSubServiceClient client = new WebPubSubServiceClientBuilder&#40;&#41;
 *     .connectionString&#40;&quot;&lt;Insert connection string from Azure Portal&gt;&quot;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.webpubsub.webpubsubclientbuilder.connectionstring.sync -->
 *
 * @see WebPubSubServiceAsyncClient
 * @see WebPubSubServiceClient
 */
@ServiceClientBuilder(serviceClients = {WebPubSubServiceAsyncClient.class, WebPubSubServiceClient.class})
public final class WebPubSubServiceClientBuilder implements
    AzureKeyCredentialTrait<WebPubSubServiceClientBuilder>,
    ConfigurationTrait<WebPubSubServiceClientBuilder>,
    ConnectionStringTrait<WebPubSubServiceClientBuilder>,
    EndpointTrait<WebPubSubServiceClientBuilder>,
    HttpTrait<WebPubSubServiceClientBuilder>,
    TokenCredentialTrait<WebPubSubServiceClientBuilder> {
    private static final String WPS_DEFAULT_SCOPE = "https://webpubsub.azure.com/.default";
    private final ClientLogger logger = new ClientLogger(WebPubSubServiceClientBuilder.class);

    private static final String WEBPUBSUB_PROPERTIES = "azure-messaging-webpubsub.properties";
    private static final String SDK_NAME = "name";
    private static final String SDK_VERSION = "version";

    private static final HttpPipelinePolicy DEFAULT_RETRY_POLICY = new RetryPolicy();

    private final Map<String, String> properties;
    private final List<HttpPipelinePolicy> policies;

    // the user should set either the connectionString, or the endpoint and credential.
    // endpoint + credential will take precedence over connection string
    private String connectionString;
    private String endpoint;
    private AzureKeyCredential credential;
    private TokenCredential tokenCredential;

    private HttpClient httpClient;
    private HttpLogOptions httpLogOptions;
    private HttpPipeline pipeline;
    private RetryPolicy retryPolicy;
    private RetryOptions retryOptions;
    private Configuration configuration;
    private WebPubSubServiceVersion version = WebPubSubServiceVersion.getLatest();
    private String hub;
    private ClientOptions clientOptions;
    private String reverseProxyEndpoint;

    /**
     * Creates a new builder instance with all values set to their default value.
     */
    public WebPubSubServiceClientBuilder() {
        policies = new ArrayList<>();
        httpLogOptions = new HttpLogOptions();
        properties = CoreUtils.getProperties(WEBPUBSUB_PROPERTIES);
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
     * @return The updated {@link WebPubSubServiceClientBuilder} object.
     * @see HttpClientOptions
     */
    @Override
    public WebPubSubServiceClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
        return this;
    }

    /**
     * Sets the credential to use when authenticating HTTP requests.
     *
     * @param connectionString Connection string in the format "endpoint={endpoint_value};accesskey={accesskey_value}"
     * @return The updated {@link WebPubSubServiceClientBuilder} object.
     * @throws NullPointerException If {@code connectionString} is {@code null}.
     */
    @Override
    public WebPubSubServiceClientBuilder connectionString(final String connectionString) {
        Objects.requireNonNull(connectionString, "'connectionString' cannot be null.");
        this.connectionString = connectionString;
        return this;
    }

    /**
     * Sets the service endpoint for the Azure Web Pub Sub instance.
     *
     * @param endpoint The URL of the Azure Web Pub Sub instance to send service requests to, and receive responses
     *      from.
     * @return The updated {@link WebPubSubServiceClientBuilder} object.
     * @throws IllegalArgumentException if {@code endpoint} is {@code null}.
     */
    @Override
    public WebPubSubServiceClientBuilder endpoint(final String endpoint) {
        Objects.requireNonNull(endpoint, "'endpoint' cannot be null.");
        try {
            new URL(endpoint);
        } catch (MalformedURLException e) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("'endpoint' must be valid URL", e));
        }
        this.endpoint = endpoint;
        return this;
    }

    /**
     * Sets the {@link AzureKeyCredential} used to authenticate HTTP requests.
     *
     * @param credential AzureKeyCredential used to authenticate HTTP requests.
     * @return The updated {@link WebPubSubServiceClientBuilder} object.
     */
    @Override
    public WebPubSubServiceClientBuilder credential(final AzureKeyCredential credential) {
        this.credential = credential;
        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authorize requests sent to the service. Refer to the Azure SDK for Java
     * <a href="https://aka.ms/azsdk/java/docs/identity">identity and authentication</a>
     * documentation for more details on proper usage of the {@link TokenCredential} type.
     *
     * @param credential {@link TokenCredential} used to authorize requests sent to the service.
     * @return The updated {@link WebPubSubServiceClientBuilder} object.
     */
    @Override
    public WebPubSubServiceClientBuilder credential(final TokenCredential credential) {
        this.tokenCredential = credential;
        return this;
    }

    /**
     * Sets the reverse proxy endpoint.
     *
     * @param reverseProxyEndpoint The reverse proxy endpoint.
     * @return The updated {@link WebPubSubServiceClientBuilder} object.
     */
    public WebPubSubServiceClientBuilder reverseProxyEndpoint(String reverseProxyEndpoint) {
        this.reverseProxyEndpoint = reverseProxyEndpoint;
        return this;
    }

    /**
     * Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or
     * underscore.
     *
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric
     * characters or underscore.
     * @return The updated {@link WebPubSubServiceClientBuilder} object.
     * @throws NullPointerException If {@code hub} is {@code null}.
     */
    public WebPubSubServiceClientBuilder hub(final String hub) {
        Objects.requireNonNull(hub, "'hub' cannot be null.");
        this.hub = hub;
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
     * @return The updated {@link WebPubSubServiceClientBuilder} object.
     */
    @Override
    public WebPubSubServiceClientBuilder httpLogOptions(final HttpLogOptions logOptions) {
        httpLogOptions = logOptions;
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
     * @return The updated {@link WebPubSubServiceClientBuilder} object.
     * @throws NullPointerException If {@code policy} is {@code null}.
     */
    @Override
    public WebPubSubServiceClientBuilder addPolicy(final HttpPipelinePolicy policy) {
        Objects.requireNonNull(policy);
        policies.add(policy);
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
     * @return The updated {@link WebPubSubServiceClientBuilder} object.
     */
    @Override
    public WebPubSubServiceClientBuilder httpClient(final HttpClient client) {
        if (this.httpClient != null && client == null) {
            logger.info("HttpClient is being set to 'null' when it was previously configured.");
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
     *
     * <p>
     * If {@code pipeline} is set, all other settings are ignored, aside from
     * {@link WebPubSubServiceClientBuilder#connectionString(String) connectionString} to build {@link WebPubSubServiceAsyncClient} or
     * {@link WebPubSubServiceClient}.
     *
     * @param pipeline {@link HttpPipeline} to use for sending service requests and receiving responses.
     * @return The updated {@link WebPubSubServiceClientBuilder} object.
     */
    @Override
    public WebPubSubServiceClientBuilder pipeline(final HttpPipeline pipeline) {
        if (this.pipeline != null && pipeline == null) {
            logger.info("HttpPipeline is being set to 'null' when it was previously configured.");
        }

        this.pipeline = pipeline;
        return this;
    }

    /**
     * Sets the configuration store that is used during construction of the service client.
     *
     * The default configuration store is a clone of the {@link Configuration#getGlobalConfiguration() global
     * configuration store}, use {@link Configuration#NONE} to bypass using configuration settings during construction.
     *
     * @param configuration The configuration store used to
     * @return The updated {@link WebPubSubServiceClientBuilder} object.
     */
    @Override
    public WebPubSubServiceClientBuilder configuration(final Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the {@link HttpPipelinePolicy} that is used when each request is sent. The default retry policy will be
     * used if not provided.
     * <p>
     * Setting this is mutually exclusive with using {@link #retryOptions(RetryOptions)}.
     *
     * @param retryPolicy user's retry policy applied to each request.
     * @return The updated {@link WebPubSubServiceClientBuilder} object.
     */
    public WebPubSubServiceClientBuilder retryPolicy(final RetryPolicy retryPolicy) {
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
     * @return The updated {@link WebPubSubServiceClientBuilder} object.
     */
    @Override
    public WebPubSubServiceClientBuilder retryOptions(RetryOptions retryOptions) {
        this.retryOptions = retryOptions;
        return this;
    }

    /**
     * Sets the {@link WebPubSubServiceVersion} that is used when making API requests.
     * <p>
     * If a service version is not provided, the service version that will be used will be the latest known service
     * version based on the version of the client library being used. If no service version is specified, updating to a
     * newer version the client library will have the result of potentially moving to a newer service version.
     *
     * @param version {@link WebPubSubServiceVersion} of the service to be used when making requests.
     * @return The updated {@link WebPubSubServiceClientBuilder} object.
     */
    public WebPubSubServiceClientBuilder serviceVersion(final WebPubSubServiceVersion version) {
        this.version = version;
        return this;
    }


    private AzureWebPubSubServiceRestApiImpl buildInnerClient() {
        if (hub == null || hub.isEmpty()) {
            logger.logThrowableAsError(
                new IllegalStateException("hub is not valid - it must be non-null and non-empty."));
        }

        if (connectionString != null) {
            final Map<String, String> csParams = parseConnectionString(connectionString);
            if (!csParams.containsKey("endpoint") && !csParams.containsKey("accesskey")) {
                logger.logThrowableAsError(new IllegalArgumentException(
                    "Connection string does not contain required 'endpoint' and 'accesskey' values"));
            }

            final String accessKey = csParams.get("accesskey");

            this.credential = new AzureKeyCredential(accessKey);
            String csEndpoint = csParams.get("endpoint");
            URL url;
            try {
                url = new URL(csEndpoint);
                this.endpoint = csEndpoint;
            } catch (MalformedURLException e) {
                throw logger.logExceptionAsWarning(new IllegalArgumentException("Connection string contains invalid "
                    + "endpoint", e));
            }

            String port = csParams.get("port");
            if (!CoreUtils.isNullOrEmpty(port)) {
                this.endpoint = UrlBuilder.parse(url).setPort(port).toString();
            }
        }

        if (endpoint == null || endpoint.isEmpty()) {
            logger.logThrowableAsError(
                new IllegalStateException("endpoint is not valid - it must be non-null and non-empty."));
        }

        // Service version
        final WebPubSubServiceVersion serviceVersion =
            version != null ? version : WebPubSubServiceVersion.getLatest();


        if (pipeline != null) {
            return new AzureWebPubSubServiceRestApiImpl(pipeline, endpoint, serviceVersion);
        }

        // Global Env configuration store
        final Configuration buildConfiguration =
            (configuration == null) ? Configuration.getGlobalConfiguration().clone() : configuration;

        final String clientName = properties.getOrDefault(SDK_NAME, "UnknownName");
        final String clientVersion = properties.getOrDefault(SDK_VERSION, "UnknownVersion");
        String applicationId =
            clientOptions == null ? httpLogOptions.getApplicationId() : clientOptions.getApplicationId();

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new UserAgentPolicy(applicationId, clientName, clientVersion,
            buildConfiguration));
        policies.add(new CookiePolicy());
        HttpPolicyProviders.addBeforeRetryPolicies(policies);
        policies.add(ClientBuilderUtil.validateAndGetRetryPolicy(retryPolicy, retryOptions, DEFAULT_RETRY_POLICY));
        if (this.credential != null) {
            WebPubSubAuthenticationPolicy webPubSubAuthPolicy = new WebPubSubAuthenticationPolicy(credential);
            policies.add(webPubSubAuthPolicy);
        } else if (this.tokenCredential != null) {
            BearerTokenAuthenticationPolicy tokenPolicy = new BearerTokenAuthenticationPolicy(this.tokenCredential,
                WPS_DEFAULT_SCOPE);
            policies.add(tokenPolicy);
        } else {
            throw logger.logExceptionAsError(
                new IllegalStateException("No credential available to create the client. "
                    + "Please provide connection string or AzureKeyCredential or TokenCredential."));
        }

        if (!CoreUtils.isNullOrEmpty(reverseProxyEndpoint)) {
            policies.add(new ReverseProxyPolicy(reverseProxyEndpoint));
        }
        policies.addAll(this.policies);

        if (clientOptions != null) {
            List<HttpHeader> httpHeaderList = new ArrayList<>();
            clientOptions.getHeaders().forEach(header ->
                httpHeaderList.add(new HttpHeader(header.getName(), header.getValue())));
            policies.add(new AddHeadersPolicy(new HttpHeaders(httpHeaderList)));
        }

        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(httpLogOptions));
        HttpPipeline buildPipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();
        return new AzureWebPubSubServiceRestApiImpl(buildPipeline, endpoint, serviceVersion);
    }


    /**
     * Builds an instance of WebPubSubAsyncServiceClient with the provided parameters.
     *
     * @return an instance of WebPubSubAsyncServiceClient.
     * @throws IllegalStateException If both {@link #retryOptions(RetryOptions)}
     *      and {@link #retryPolicy(RetryPolicy)} have been set.
     */
    public WebPubSubServiceAsyncClient buildAsyncClient() {
        return new WebPubSubServiceAsyncClient(buildInnerClient().getWebPubSubs(), hub, endpoint, credential, version);
    }

    /**
     * Builds an instance of WebPubSubServiceClient with the provided parameters.
     *
     * @return an instance of WebPubSubServiceClient.
     * @throws IllegalStateException If both {@link #retryOptions(RetryOptions)}
     *      and {@link #retryPolicy(RetryPolicy)} have been set.
     */
    public WebPubSubServiceClient buildClient() {
        return new WebPubSubServiceClient(buildInnerClient().getWebPubSubs(), hub, endpoint, credential, version);
    }

    private Map<String, String> parseConnectionString(final String cs) {
        final String[] params = cs.split(";");

        final Map<String, String> connectionStringParams = new HashMap<>();
        for (final String param : params) {
            final String[] paramSplit = param.split("=", 2);
            if (paramSplit.length != 2) {
                continue;
            }

            final String key = paramSplit[0].trim().toLowerCase(Locale.ROOT);

            if (connectionStringParams.containsKey(key)) {
                logger.logThrowableAsError(new IllegalArgumentException(
                    "Duplicate connection string key parameter provided for key '" + key + "'"));
            }

            final String value = paramSplit[1].trim();
            connectionStringParams.put(key, value);
        }
        return connectionStringParams;
    }
}
