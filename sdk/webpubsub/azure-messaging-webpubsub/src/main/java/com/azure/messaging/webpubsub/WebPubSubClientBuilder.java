// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.CookiePolicy;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.webpubsub.implementation.AzureWebPubSubServiceRestAPIImplBuilder;

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
 * {@link WebPubSubServiceClient sync} and {@link WebPubSubAsyncServiceClient async} Azure Web Pub Sub clients, using the
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
 * {@codesnippet com.azure.messaging.webpubsub.webpubsubclientbuilder.connectionstring.async}
 *
 * <p>This demonstrates using the connection string provided by the Azure Portal. Another approach is to use the
 * combination of credential and endpoint details, as shown below:</p>
 *
 * {@codesnippet com.azure.messaging.webpubsub.webpubsubclientbuilder.credential.endpoint.async}
 *
 * <p>Of course, synchronous clients may also be instantiated, by calling {@link #buildClient() buildClient} rather than
 * {@link #buildAsyncClient() buildAsyncClient}.</p>
 *
 * {@codesnippet com.azure.messaging.webpubsub.webpubsubclientbuilder.connectionstring.sync}
 *
 * @see WebPubSubAsyncServiceClient
 * @see WebPubSubServiceClient
 */
@ServiceClientBuilder(serviceClients = {WebPubSubAsyncServiceClient.class, WebPubSubServiceClient.class})
public final class WebPubSubClientBuilder {
    private final ClientLogger logger = new ClientLogger(WebPubSubClientBuilder.class);

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

    private HttpClient httpClient;
    private HttpLogOptions httpLogOptions;
    private HttpPipeline pipeline;
    private RetryPolicy retryPolicy;
    private Configuration configuration;
    private WebPubSubServiceVersion version;
    private String hub;
    private ClientOptions clientOptions;

    /**
     * Creates a new builder instance with all values set to their default value.
     */
    public WebPubSubClientBuilder() {
        policies = new ArrayList<>();
        httpLogOptions = new HttpLogOptions();
        properties = CoreUtils.getProperties(WEBPUBSUB_PROPERTIES);
    }

    /**
     * Sets the {@link ClientOptions} which enables various options to be set on the client. For example setting an
     * {@code applicationId} using {@link ClientOptions#setApplicationId(String)} to configure
     * the {@link UserAgentPolicy} for telemetry/monitoring purposes.
     *
     * <p>More About <a href="https://azure.github.io/azure-sdk/general_azurecore.html#telemetry-policy">Azure Core: Telemetry policy</a>
     *
     * @param clientOptions the {@link ClientOptions} to be set on the client.
     * @return The updated WebPubSubClientBuilder object.
     */
    public WebPubSubClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
        return this;
    }

    /**
     * Sets the credential to use when authenticating HTTP requests.
     *
     * @param connectionString Connection string in the format "endpoint={endpoint_value};accesskey={accesskey_value}"
     * @return The updated WebPubSubClientBuilder object.
     * @throws NullPointerException If {@code connectionString} is {@code null}.
     */
    public WebPubSubClientBuilder connectionString(final String connectionString) {
        Objects.requireNonNull(connectionString, "'connectionString' cannot be null.");
        this.connectionString = connectionString;
        return this;
    }

    /**
     * Sets the service endpoint for the Azure Web Pub Sub instance.
     *
     * @param endpoint The URL of the Azure Web Pub Sub instance to send service requests to, and receive responses
     *      from.
     * @return The updated WebPubSubClientBuilder object.
     * @throws IllegalArgumentException if {@code endpoint} is {@code null}.
     */
    public WebPubSubClientBuilder endpoint(final String endpoint) {
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
     * @return The updated WebPubSubClientBuilder object.
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public WebPubSubClientBuilder credential(final AzureKeyCredential credential) {
        Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.credential = credential;
        return this;
    }

    /**
     * Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or
     * underscore.
     *
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric
     * characters or underscore.
     * @return The updated WebPubSubClientBuilder object.
     * @throws NullPointerException If {@code hub} is {@code null}.
     */
    public WebPubSubClientBuilder hub(final String hub) {
        Objects.requireNonNull(hub, "'hub' cannot be null.");
        this.hub = hub;
        return this;
    }

    /**
     * Sets the logging configuration for HTTP requests and responses.
     *
     * <p> If logLevel is not provided, default value of {@link com.azure.core.http.policy.HttpLogDetailLevel#NONE} is
     * set.</p>
     *
     * @param logOptions The logging configuration to use when sending and receiving HTTP requests/responses.
     * @return The updated WebPubSubClientBuilder object.
     */
    public WebPubSubClientBuilder httpLogOptions(final HttpLogOptions logOptions) {
        httpLogOptions = logOptions;
        return this;
    }

    /**
     * Adds a policy to the set of existing policies that are executed after required policies.
     *
     * @param policy The retry policy for service requests.
     * @return The updated WebPubSubClientBuilder object.
     * @throws NullPointerException If {@code policy} is {@code null}.
     */
    public WebPubSubClientBuilder addPolicy(final HttpPipelinePolicy policy) {
        Objects.requireNonNull(policy);
        policies.add(policy);
        return this;
    }

    /**
     * Sets the HTTP client to use for sending and receiving requests to and from the service.
     *
     * @param client The HTTP client to use for requests.
     * @return The updated WebPubSubClientBuilder object.
     */
    public WebPubSubClientBuilder httpClient(final HttpClient client) {
        if (this.httpClient != null && client == null) {
            logger.info("HttpClient is being set to 'null' when it was previously configured.");
        }

        this.httpClient = client;
        return this;
    }

    /**
     * Sets the HTTP pipeline to use for the service client.
     *
     * If {@code pipeline} is set, all other settings are ignored, aside from
     * {@link WebPubSubClientBuilder#connectionString(String) connectionString} to build {@link WebPubSubAsyncServiceClient} or
     * {@link WebPubSubServiceClient}.
     *
     * @param pipeline The HTTP pipeline to use for sending service requests and receiving responses.
     * @return The updated WebPubSubClientBuilder object.
     */
    public WebPubSubClientBuilder pipeline(final HttpPipeline pipeline) {
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
     * @return The updated WebPubSubClientBuilder object.
     */
    public WebPubSubClientBuilder configuration(final Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the {@link HttpPipelinePolicy} that is used when each request is sent. The default retry policy will be
     * used if not provided.
     *
     * @param retryPolicy user's retry policy applied to each request.
     * @return The updated WebPubSubClientBuilder object.
     */
    public WebPubSubClientBuilder retryPolicy(final RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
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
     * @return The updated WebPubSubClientBuilder object.
     */
    public WebPubSubClientBuilder serviceVersion(final WebPubSubServiceVersion version) {
        this.version = version;
        return this;
    }

    /**
     * Builds an instance of WebPubSubAsyncServiceClient with the provided parameters.
     *
     * @return an instance of WebPubSubAsyncServiceClient.
     */
    public WebPubSubAsyncServiceClient buildAsyncClient() {
        if (hub == null || hub.isEmpty()) {
            logger.logThrowableAsError(
                new IllegalStateException("hub is not valid - it must be non-null and non-empty."));
        }

        if (endpoint == null && credential == null) {
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

        final AzureWebPubSubServiceRestAPIImplBuilder innerBuilder = new AzureWebPubSubServiceRestAPIImplBuilder();

        if (endpoint == null || endpoint.isEmpty()) {
            logger.logThrowableAsError(
                new IllegalStateException("endpoint is not valid - it must be non-null and non-empty."));
        }
        innerBuilder.host(endpoint);

        // Service version
        final WebPubSubServiceVersion serviceVersion =
            version != null ? version : WebPubSubServiceVersion.getLatest();

        WebPubSubAuthenticationPolicy webPubSubAuthPolicy = new WebPubSubAuthenticationPolicy(credential);

        if (pipeline != null) {
            innerBuilder.pipeline(pipeline);
            return buildAsyncClient(innerBuilder, hub, endpoint, webPubSubAuthPolicy, serviceVersion);
        }

        if (credential == null) {
            logger.logThrowableAsError(
                new IllegalStateException("No credential has been specified - it must be non-null and non-empty."));
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
        policies.add(retryPolicy == null ? DEFAULT_RETRY_POLICY : retryPolicy);
        policies.add(webPubSubAuthPolicy);
        policies.addAll(this.policies);

        if (clientOptions != null) {
            List<HttpHeader> httpHeaderList = new ArrayList<>();
            clientOptions.getHeaders().forEach(header ->
                httpHeaderList.add(new HttpHeader(header.getName(), header.getValue())));
            policies.add(new AddHeadersPolicy(new HttpHeaders(httpHeaderList)));
        }

        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(httpLogOptions));

        innerBuilder.pipeline(new HttpPipelineBuilder()
                                  .policies(policies.toArray(new HttpPipelinePolicy[0]))
                                  .httpClient(httpClient)
                                  .build());
        return buildAsyncClient(innerBuilder, hub, endpoint, webPubSubAuthPolicy, serviceVersion);
    }

    private WebPubSubAsyncServiceClient buildAsyncClient(final AzureWebPubSubServiceRestAPIImplBuilder innerBuilder,
                                                         final String hub,
                                                         final String endpoint,
                                                         final WebPubSubAuthenticationPolicy webPubSubAuthPolicy,
                                                         final WebPubSubServiceVersion serviceVersion) {
        return new WebPubSubAsyncServiceClient(
            innerBuilder.buildClient().getWebPubSubs(), hub, endpoint, webPubSubAuthPolicy, serviceVersion);
    }

    /**
     * Builds an instance of WebPubSubServiceClient with the provided parameters.
     *
     * @return an instance of WebPubSubServiceClient.
     */
    public WebPubSubServiceClient buildClient() {
        return new WebPubSubServiceClient(buildAsyncClient());
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
