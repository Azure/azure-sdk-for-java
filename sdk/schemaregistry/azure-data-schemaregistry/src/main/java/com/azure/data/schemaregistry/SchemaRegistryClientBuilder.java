// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.client.traits.ConfigurationTrait;
import com.azure.core.client.traits.HttpTrait;
import com.azure.core.client.traits.TokenCredentialTrait;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.AddHeadersFromContextPolicy;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.HttpClientOptions;
import com.azure.core.util.ServiceVersion;
import com.azure.core.util.builder.ClientBuilderUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.data.schemaregistry.implementation.AzureSchemaRegistryImpl;
import com.azure.data.schemaregistry.implementation.AzureSchemaRegistryImplBuilder;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Fluent builder for interacting with the Schema Registry service via {@link SchemaRegistryAsyncClient} and
 * {@link SchemaRegistryClient}.  To build the client, the builder requires the service endpoint of the Schema Registry
 * and an Azure AD credential.
 *
 * <p><strong>Sample: Construct a sync service client</strong></p>
 *
 *  <p>The following code sample demonstrates the creation of the synchronous client
 * {@link com.azure.data.schemaregistry.SchemaRegistryClient}.  The credential used is {@code DefaultAzureCredential}
 * because it combines commonly used credentials in deployment and development and chooses the credential to used based
 * on its running environment.</p>
 *
 * <!-- src_embed com.azure.data.schemaregistry.schemaregistryclient.construct -->
 * <pre>
 * DefaultAzureCredential azureCredential = new DefaultAzureCredentialBuilder&#40;&#41;
 *     .build&#40;&#41;;
 * SchemaRegistryClient client = new SchemaRegistryClientBuilder&#40;&#41;
 *     .fullyQualifiedNamespace&#40;&quot;https:&#47;&#47;&lt;your-schema-registry-endpoint&gt;.servicebus.windows.net&quot;&#41;
 *     .credential&#40;azureCredential&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.data.schemaregistry.schemaregistryclient.construct -->

 * <p><strong>Sample: Construct an async service client</strong></p>
 *
 * <p>The following code sample demonstrates the creation of the asynchronous client
 * {@link com.azure.data.schemaregistry.SchemaRegistryAsyncClient}.  The credential used is
 * {@code DefaultAzureCredential} because it combines commonly used credentials in deployment and development and
 * chooses the credential to used based on its running environment.</p>
 *
 * <!-- src_embed com.azure.data.schemaregistry.schemaregistryasyncclient.construct -->
 * <pre>
 * DefaultAzureCredential azureCredential = new DefaultAzureCredentialBuilder&#40;&#41;
 *     .build&#40;&#41;;
 * SchemaRegistryAsyncClient client = new SchemaRegistryClientBuilder&#40;&#41;
 *     .fullyQualifiedNamespace&#40;&quot;https:&#47;&#47;&lt;your-schema-registry-endpoint&gt;.servicebus.windows.net&quot;&#41;
 *     .credential&#40;azureCredential&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.data.schemaregistry.schemaregistryasyncclient.construct -->
 *
 * <p><strong>Sample: Instantiating with custom retry policy and HTTP log options</strong></p>
 *
 * <p>The following code sample demonstrates customizing parts of the HTTP pipeline and client behavior such as
 * outputting the body of the HTTP request and response or using another retry policy.</p>
 *
 * <!-- src_embed com.azure.data.schemaregistry.schemaregistryasyncclient.retrypolicy.construct -->
 * <pre>
 * DefaultAzureCredential azureCredential = new DefaultAzureCredentialBuilder&#40;&#41;
 *     .build&#40;&#41;;
 *
 * HttpLogOptions httpLogOptions = new HttpLogOptions&#40;&#41;
 *     .setLogLevel&#40;HttpLogDetailLevel.BODY&#41;
 *     .setPrettyPrintBody&#40;true&#41;;
 *
 * RetryPolicy retryPolicy = new RetryPolicy&#40;new FixedDelay&#40;5, Duration.ofSeconds&#40;30&#41;&#41;&#41;;
 * SchemaRegistryAsyncClient client = new SchemaRegistryClientBuilder&#40;&#41;
 *     .fullyQualifiedNamespace&#40;&quot;https:&#47;&#47;&lt;your-schema-registry-endpoint&gt;.servicebus.windows.net&quot;&#41;
 *     .httpLogOptions&#40;httpLogOptions&#41;
 *     .retryPolicy&#40;retryPolicy&#41;
 *     .credential&#40;azureCredential&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.data.schemaregistry.schemaregistryasyncclient.retrypolicy.construct -->
 */
@ServiceClientBuilder(serviceClients = {SchemaRegistryAsyncClient.class, SchemaRegistryClient.class})
public class SchemaRegistryClientBuilder implements
    ConfigurationTrait<SchemaRegistryClientBuilder>,
    HttpTrait<SchemaRegistryClientBuilder>,
    TokenCredentialTrait<SchemaRegistryClientBuilder> {
    private final ClientLogger logger = new ClientLogger(SchemaRegistryClientBuilder.class);

    private static final String DEFAULT_SCOPE = "https://eventhubs.azure.net/.default";
    private static final String CLIENT_PROPERTIES = "azure-data-schemaregistry.properties";
    private static final String NAME = "name";
    private static final String VERSION = "version";
    private static final RetryPolicy DEFAULT_RETRY_POLICY = new RetryPolicy("retry-after-ms", ChronoUnit.MILLIS);

    private final List<HttpPipelinePolicy> perCallPolicies = new ArrayList<>();
    private final List<HttpPipelinePolicy> perRetryPolicies = new ArrayList<>();

    private final String clientName;
    private final String clientVersion;

    private String fullyQualifiedNamespace;
    private HttpClient httpClient;
    private TokenCredential credential;
    private ClientOptions clientOptions;
    private HttpLogOptions httpLogOptions;
    private HttpPipeline httpPipeline;
    private RetryPolicy retryPolicy;
    private RetryOptions retryOptions;
    private Configuration configuration;
    private ServiceVersion serviceVersion;

    /**
     * Constructor for SchemaRegistryClientBuilder. Supplies client defaults.
     */
    public SchemaRegistryClientBuilder() {
        this.httpLogOptions = new HttpLogOptions();
        this.httpClient = null;
        this.credential = null;

        Map<String, String> properties = CoreUtils.getProperties(CLIENT_PROPERTIES);
        clientName = properties.getOrDefault(NAME, "UnknownName");
        clientVersion = properties.getOrDefault(VERSION, "UnknownVersion");
    }

    /**
     * Sets the fully qualified namespace for the Azure Schema Registry instance. This is likely to be
     * similar to <strong>{@literal "{your-namespace}.servicebus.windows.net}"</strong>.
     *
     * @param fullyQualifiedNamespace The fully qualified namespace of the Azure Schema Registry instance.
     * @return The updated {@link SchemaRegistryClientBuilder} object.
     * @throws NullPointerException if {@code fullyQualifiedNamespace} is null
     * @throws IllegalArgumentException if {@code fullyQualifiedNamespace} cannot be parsed into a valid URL
     */
    public SchemaRegistryClientBuilder fullyQualifiedNamespace(String fullyQualifiedNamespace) {
        Objects.requireNonNull(fullyQualifiedNamespace, "'fullyQualifiedNamespace' cannot be null.");
        try {
            URL url = new URL(fullyQualifiedNamespace);
            this.fullyQualifiedNamespace = url.getHost();
        } catch (MalformedURLException ex) {
            logger.verbose("Fully qualified namespace did not contain protocol.");
            this.fullyQualifiedNamespace = fullyQualifiedNamespace;
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
     * @param httpClient The {@link HttpClient} to use for requests.
     * @return The updated {@link SchemaRegistryClientBuilder} object.
     */
    @Override
    public SchemaRegistryClientBuilder httpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
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
     * If {@code pipeline} is set, all other HTTP settings are ignored to build {@link SchemaRegistryAsyncClient}.
     *
     * @param httpPipeline {@link HttpPipeline} to use for sending service requests and receiving responses.
     * @return The updated {@link SchemaRegistryClientBuilder} object.
     */
    @Override
    public SchemaRegistryClientBuilder pipeline(HttpPipeline httpPipeline) {
        if (this.httpPipeline != null && httpPipeline == null) {
            logger.info("HttpPipeline is being set to 'null' when it was previously configured.");
        }

        this.httpPipeline = httpPipeline;
        return this;
    }

    /**
     * Sets the configuration store that is used during construction of the service client.
     *
     * The default configuration store is a clone of the {@link Configuration#getGlobalConfiguration() global
     * configuration store}, use {@link Configuration#NONE} to bypass using configuration settings during construction.
     *
     * @param configuration The configuration store used to
     * @return The updated SchemaRegistryClientBuilder object.
     */
    @Override
    public SchemaRegistryClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authorize requests sent to the service. Refer to the Azure SDK for Java
     * <a href="https://aka.ms/azsdk/java/docs/identity">identity and authentication</a>
     * documentation for more details on proper usage of the {@link TokenCredential} type.
     *
     * @param credential {@link TokenCredential} used to authorize requests sent to the service.
     * @return The updated {@link SchemaRegistryClientBuilder} object.
     * @throws NullPointerException If {@code credential} is {@code null}
     */
    @Override
    public SchemaRegistryClientBuilder credential(TokenCredential credential) {
        this.credential = Objects.requireNonNull(credential, "'credential' cannot be null.");
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
     * @return The updated SchemaRegistryClientBuilder object.
     * @see HttpClientOptions
     */
    @Override
    public SchemaRegistryClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
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
     * @return The updated {@link SchemaRegistryClientBuilder} object.
     */
    @Override
    public SchemaRegistryClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        this.httpLogOptions = logOptions;
        return this;
    }

    /**
     * Sets the {@link RetryPolicy} that is used when each request is sent.
     * <p>
     * The default retry policy will be used if not provided to build {@link SchemaRegistryAsyncClient} .
     * <p>
     * Setting this is mutually exclusive with using {@link #retryOptions(RetryOptions)}.
     *
     * @param retryPolicy user's retry policy applied to each request.
     * @return The updated {@link SchemaRegistryClientBuilder} object.
     */
    public SchemaRegistryClientBuilder retryPolicy(RetryPolicy retryPolicy) {
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
     * @return The updated {@link SchemaRegistryClientBuilder} object.
     */
    @Override
    public SchemaRegistryClientBuilder retryOptions(RetryOptions retryOptions) {
        this.retryOptions = retryOptions;
        return this;
    }

    /**
     * Sets the service version to use.
     *
     * @param serviceVersion Service version.
     * @return The updated instance.
     */
    public SchemaRegistryClientBuilder serviceVersion(ServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;
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
     * @return The updated {@link SchemaRegistryClientBuilder} object.
     * @throws NullPointerException If {@code policy} is {@code null}.
     */
    @Override
    public SchemaRegistryClientBuilder addPolicy(HttpPipelinePolicy policy) {
        Objects.requireNonNull(policy, "'policy' cannot be null.");

        if (policy.getPipelinePosition() == HttpPipelinePosition.PER_CALL) {
            perCallPolicies.add(policy);
        } else {
            perRetryPolicies.add(policy);
        }

        return this;
    }

    /**
     * Creates a {@link SchemaRegistryAsyncClient} based on options set in the builder. Every time {@code buildClient()}
     * is called a new instance of {@link SchemaRegistryAsyncClient} is created.
     *
     * If {@link #pipeline(HttpPipeline) pipeline} is set, then all HTTP pipeline related settings are ignored.
     *
     * @return A {@link SchemaRegistryAsyncClient} with the options set from the builder.
     * @throws NullPointerException if {@link #fullyQualifiedNamespace(String) fullyQualifiedNamespace} and
     *      {@link #credential(TokenCredential) credential} are not set.
     * @throws IllegalArgumentException if {@link #fullyQualifiedNamespace(String) fullyQualifiedNamespace} is an empty
     *      string.
     * @throws IllegalStateException If both {@link #retryOptions(RetryOptions)}
     *      and {@link #retryPolicy(RetryPolicy)} have been set.
     */
    public SchemaRegistryAsyncClient buildAsyncClient() {
        AzureSchemaRegistryImpl restService = getAzureSchemaRegistryImplService();

        return new SchemaRegistryAsyncClient(restService);
    }

    private AzureSchemaRegistryImpl getAzureSchemaRegistryImplService() {
        Objects.requireNonNull(credential,
            "'credential' cannot be null and must be set via builder.credential(TokenCredential)");
        Objects.requireNonNull(fullyQualifiedNamespace,
            "'fullyQualifiedNamespace' cannot be null and must be set via builder.fullyQualifiedNamespace(String)");

        if (CoreUtils.isNullOrEmpty(fullyQualifiedNamespace)) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "'fullyQualifiedNamespace' cannot be an empty string."));
        }

        Configuration buildConfiguration = (configuration == null)
            ? Configuration.getGlobalConfiguration()
            : configuration;

        HttpPipeline buildPipeline = this.httpPipeline;
        // Create a default Pipeline if it is not given
        if (buildPipeline == null) {
            // Closest to API goes first, closest to wire goes last.
            final List<HttpPipelinePolicy> policies = new ArrayList<>();

            policies.add(new UserAgentPolicy(CoreUtils.getApplicationId(clientOptions, httpLogOptions), clientName,
                clientVersion, buildConfiguration));
            policies.add(new RequestIdPolicy());
            policies.add(new AddHeadersFromContextPolicy());

            policies.addAll(perCallPolicies);
            HttpPolicyProviders.addBeforeRetryPolicies(policies);

            policies.add(ClientBuilderUtil.validateAndGetRetryPolicy(retryPolicy, retryOptions, DEFAULT_RETRY_POLICY));

            policies.add(new AddDatePolicy());
            policies.add(new BearerTokenAuthenticationPolicy(credential, DEFAULT_SCOPE));

            policies.addAll(perRetryPolicies);

            if (clientOptions != null) {
                List<HttpHeader> clientOptionsHeaders = new ArrayList<>();
                clientOptions.getHeaders()
                    .forEach(header -> clientOptionsHeaders.add(new HttpHeader(header.getName(), header.getValue())));

                if (!CoreUtils.isNullOrEmpty(clientOptionsHeaders)) {
                    policies.add(new AddHeadersPolicy(new HttpHeaders(clientOptionsHeaders)));
                }
            }

            HttpPolicyProviders.addAfterRetryPolicies(policies);

            policies.add(new HttpLoggingPolicy(httpLogOptions));

            buildPipeline = new HttpPipelineBuilder()
                .policies(policies.toArray(new HttpPipelinePolicy[0]))
                .httpClient(httpClient)
                .clientOptions(clientOptions)
                .build();
        }

        ServiceVersion version = (serviceVersion == null) ? SchemaRegistryVersion.getLatest() : serviceVersion;
        SerializerAdapter serializerAdapter = new SchemaRegistryJsonSerializer();

        AzureSchemaRegistryImpl restService = new AzureSchemaRegistryImplBuilder()
            .serializerAdapter(serializerAdapter)
            .endpoint(fullyQualifiedNamespace)
            .apiVersion(version.getVersion())
            .pipeline(buildPipeline)
            .buildClient();
        return restService;
    }

    /**
     * Creates synchronous {@link SchemaRegistryClient} instance. See async builder method for options validation.
     *
     * @return {@link SchemaRegistryClient} with the options set from the builder.
     * @throws NullPointerException if {@link #fullyQualifiedNamespace(String) endpoint} and {@link #credential(TokenCredential)
     * credential} are not set.
     * @throws IllegalStateException If both {@link #retryOptions(RetryOptions)}
     * and {@link #retryPolicy(RetryPolicy)} have been set.
     */
    public SchemaRegistryClient buildClient() {
        AzureSchemaRegistryImpl restService = getAzureSchemaRegistryImplService();
        return new SchemaRegistryClient(restService);
    }
}
