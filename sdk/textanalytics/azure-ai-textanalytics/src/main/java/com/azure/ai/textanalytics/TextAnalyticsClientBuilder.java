// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.ai.textanalytics.implementation.Constants;
import com.azure.ai.textanalytics.implementation.MicrosoftCognitiveLanguageServiceTextAnalysisImpl;
import com.azure.ai.textanalytics.implementation.MicrosoftCognitiveLanguageServiceTextAnalysisImplBuilder;
import com.azure.ai.textanalytics.implementation.TextAnalyticsClientImpl;
import com.azure.ai.textanalytics.implementation.TextAnalyticsClientImplBuilder;
import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.client.traits.AzureKeyCredentialTrait;
import com.azure.core.client.traits.ConfigurationTrait;
import com.azure.core.client.traits.EndpointTrait;
import com.azure.core.client.traits.HttpTrait;
import com.azure.core.client.traits.TokenCredentialTrait;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.AddHeadersFromContextPolicy;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.AzureKeyCredentialPolicy;
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
import com.azure.core.util.TracingOptions;
import com.azure.core.util.HttpClientOptions;
import com.azure.core.util.builder.ClientBuilderUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.Tracer;
import com.azure.core.util.tracing.TracerProvider;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This class provides a fluent builder API to help instantiation of {@link TextAnalyticsClient TextAnalyticsClients}
 * and {@link TextAnalyticsAsyncClient TextAnalyticsAsyncClients}, call {@link #buildClient()} buildClient} and {@link
 * #buildAsyncClient() buildAsyncClient} respectively to construct an instance of the desired client.
 *
 * <p>
 * The client needs the service endpoint of the Azure Text Analytics to access the resource service. {@link
 * #credential(AzureKeyCredential)} or {@link #credential(TokenCredential) credential(TokenCredential)} give the builder
 * access credential.
 * </p>
 *
 * <p><strong>Instantiating an asynchronous Text Analytics Client</strong></p>
 *
 * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsAsyncClient.instantiation -->
 * <pre>
 * TextAnalyticsAsyncClient textAnalyticsAsyncClient = new TextAnalyticsClientBuilder&#40;&#41;
 *     .credential&#40;new AzureKeyCredential&#40;&quot;&#123;key&#125;&quot;&#41;&#41;
 *     .endpoint&#40;&quot;&#123;endpoint&#125;&quot;&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.ai.textanalytics.TextAnalyticsAsyncClient.instantiation -->
 *
 * <p><strong>Instantiating a synchronous Text Analytics Client</strong></p>
 *
 * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsClient.instantiation -->
 * <pre>
 * TextAnalyticsClient textAnalyticsClient = new TextAnalyticsClientBuilder&#40;&#41;
 *     .credential&#40;new AzureKeyCredential&#40;&quot;&#123;key&#125;&quot;&#41;&#41;
 *     .endpoint&#40;&quot;&#123;endpoint&#125;&quot;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.ai.textanalytics.TextAnalyticsClient.instantiation -->
 *
 * <p>
 * Another way to construct the client is using a {@link HttpPipeline}. The pipeline gives the client an authenticated
 * way to communicate with the service. Set the pipeline with {@link #pipeline(HttpPipeline) this} and set the service
 * endpoint with {@link #endpoint(String) this}. Using a pipeline requires additional setup but allows for finer control
 * on how the {@link TextAnalyticsClient} and {@link TextAnalyticsAsyncClient} is built.
 * </p>
 *
 * <!-- src_embed com.azure.ai.textanalytics.TextAnalyticsClient.pipeline.instantiation -->
 * <pre>
 * HttpPipeline pipeline = new HttpPipelineBuilder&#40;&#41;
 *     .policies&#40;&#47;* add policies *&#47;&#41;
 *     .build&#40;&#41;;
 *
 * TextAnalyticsClient textAnalyticsClient = new TextAnalyticsClientBuilder&#40;&#41;
 *     .credential&#40;new AzureKeyCredential&#40;&quot;&#123;key&#125;&quot;&#41;&#41;
 *     .endpoint&#40;&quot;&#123;endpoint&#125;&quot;&#41;
 *     .pipeline&#40;pipeline&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.ai.textanalytics.TextAnalyticsClient.pipeline.instantiation -->
 *
 * @see TextAnalyticsAsyncClient
 * @see TextAnalyticsClient
 */
@ServiceClientBuilder(serviceClients = {TextAnalyticsAsyncClient.class, TextAnalyticsClient.class})
public final class TextAnalyticsClientBuilder implements
    AzureKeyCredentialTrait<TextAnalyticsClientBuilder>,
    ConfigurationTrait<TextAnalyticsClientBuilder>,
    EndpointTrait<TextAnalyticsClientBuilder>,
    HttpTrait<TextAnalyticsClientBuilder>,
    TokenCredentialTrait<TextAnalyticsClientBuilder> {
    private static final String DEFAULT_SCOPE = "https://cognitiveservices.azure.com/.default";
    private static final String NAME = "name";
    private static final String OCP_APIM_SUBSCRIPTION_KEY = "Ocp-Apim-Subscription-Key";
    private static final String TEXT_ANALYTICS_PROPERTIES = "azure-ai-textanalytics.properties";
    private static final String VERSION = "version";

    private static final RetryPolicy DEFAULT_RETRY_POLICY = new RetryPolicy();
    private static final ClientOptions DEFAULT_CLIENT_OPTIONS = new ClientOptions();
    private static final HttpLogOptions DEFAULT_LOG_OPTIONS = new HttpLogOptions();
    private static final HttpHeaders DEFAULT_HTTP_HEADERS = new HttpHeaders();
    private static final String COGNITIVE_TRACING_NAMESPACE_VALUE = "Microsoft.CognitiveServices";
    private final ClientLogger logger = new ClientLogger(TextAnalyticsClientBuilder.class);

    private final List<HttpPipelinePolicy> perCallPolicies = new ArrayList<>();
    private final List<HttpPipelinePolicy> perRetryPolicies = new ArrayList<>();

    private ClientOptions clientOptions;
    private Configuration configuration;
    private AzureKeyCredential credential;
    private String defaultCountryHint;
    private String defaultLanguage;
    private String endpoint;
    private HttpClient httpClient;
    private HttpLogOptions httpLogOptions;
    private HttpPipeline httpPipeline;
    private RetryPolicy retryPolicy;
    private RetryOptions retryOptions;
    private TokenCredential tokenCredential;
    private TextAnalyticsServiceVersion version;

    private static final String CLIENT_NAME;
    private static final String CLIENT_VERSION;
    static {
        Map<String, String> properties = CoreUtils.getProperties(TEXT_ANALYTICS_PROPERTIES);
        CLIENT_NAME = properties.getOrDefault(NAME, "UnknownName");
        CLIENT_VERSION = properties.getOrDefault(VERSION, "UnknownVersion");
    }

    /**
     * Creates a {@link TextAnalyticsClient} based on options set in the builder. Every time {@code buildClient()} is
     * called a new instance of {@link TextAnalyticsClient} is created.
     *
     * <p>
     * If {@link #pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and {@link #endpoint(String)
     * endpoint} are used to create the {@link TextAnalyticsClient client}. All other builder settings are ignored
     * </p>
     *
     * @return A {@link TextAnalyticsClient} with the options set from the builder.
     * @throws NullPointerException if {@link #endpoint(String) endpoint} or {@link #credential(AzureKeyCredential)}
     * has not been set.
     * @throws IllegalArgumentException if {@link #endpoint(String) endpoint} cannot be parsed into a valid URL.
     * @throws IllegalStateException If both {@link #retryOptions(RetryOptions)}
     * and {@link #retryPolicy(RetryPolicy)} have been set.
     */
    public TextAnalyticsClient buildClient() {
        return new TextAnalyticsClient(buildAsyncClient());
    }

    /**
     * Creates a {@link TextAnalyticsAsyncClient} based on options set in the builder. Every time {@code
     * buildAsyncClient()} is called a new instance of {@link TextAnalyticsAsyncClient} is created.
     *
     * <p>
     * If {@link #pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and {@link #endpoint(String)
     * endpoint} are used to create the {@link TextAnalyticsClient client}. All other builder settings are ignored.
     * </p>
     *
     * @return A {@link TextAnalyticsAsyncClient} with the options set from the builder.
     * @throws NullPointerException if {@link #endpoint(String) endpoint} or {@link #credential(AzureKeyCredential)}
     * has not been set.
     * @throws IllegalArgumentException if {@link #endpoint(String) endpoint} cannot be parsed into a valid URL.
     * @throws IllegalStateException If both {@link #retryOptions(RetryOptions)}
     * and {@link #retryPolicy(RetryPolicy)} have been set.
     */
    public TextAnalyticsAsyncClient buildAsyncClient() {
        // Global Env configuration store
        final Configuration buildConfiguration = (configuration == null)
            ? Configuration.getGlobalConfiguration().clone() : configuration;
        // Service Version
        final TextAnalyticsServiceVersion serviceVersion =
            version != null ? version : TextAnalyticsServiceVersion.getLatest();

        // Endpoint cannot be null, which is required in request authentication
        Objects.requireNonNull(endpoint, "'Endpoint' is required and can not be null.");

        HttpPipeline pipeline = httpPipeline;
        // Create a default Pipeline if it is not given
        if (pipeline == null) {
            // Client options
            ClientOptions buildClientOptions = this.clientOptions == null ? DEFAULT_CLIENT_OPTIONS : this.clientOptions;
            // Log options
            HttpLogOptions buildLogOptions = this.httpLogOptions == null ? DEFAULT_LOG_OPTIONS : this.httpLogOptions;
            final String applicationId = CoreUtils.getApplicationId(buildClientOptions, buildLogOptions);

            // Closest to API goes first, closest to wire goes last.
            final List<HttpPipelinePolicy> policies = new ArrayList<>();

            policies.add(new AddHeadersPolicy(DEFAULT_HTTP_HEADERS));
            policies.add(new AddHeadersFromContextPolicy());
            policies.add(new UserAgentPolicy(applicationId, CLIENT_NAME, CLIENT_VERSION, buildConfiguration));
            policies.add(new RequestIdPolicy());

            policies.addAll(perCallPolicies);
            HttpPolicyProviders.addBeforeRetryPolicies(policies);
            policies.add(ClientBuilderUtil.validateAndGetRetryPolicy(retryPolicy, retryOptions, DEFAULT_RETRY_POLICY));

            policies.add(new AddDatePolicy());

            // Authentications
            if (tokenCredential != null) {
                // User token based policy
                policies.add(new BearerTokenAuthenticationPolicy(tokenCredential, DEFAULT_SCOPE));
            } else if (credential != null) {
                policies.add(new AzureKeyCredentialPolicy(OCP_APIM_SUBSCRIPTION_KEY, credential));
            } else {
                // Throw exception that credential and tokenCredential cannot be null
                throw logger.logExceptionAsError(
                    new IllegalArgumentException("Missing credential information while building a client."));
            }

            policies.addAll(perRetryPolicies);
            HttpPolicyProviders.addAfterRetryPolicies(policies);

            policies.add(new HttpLoggingPolicy(httpLogOptions));

            HttpHeaders headers = new HttpHeaders();
            buildClientOptions.getHeaders().forEach(header -> headers.set(header.getName(), header.getValue()));
            if (headers.getSize() > 0) {
                policies.add(new AddHeadersPolicy(headers));
            }

            policies.add(new HttpLoggingPolicy(buildLogOptions));

            TracingOptions tracingOptions = null;
            if (clientOptions != null) {
                tracingOptions = clientOptions.getTracingOptions();
            }
            
            Tracer tracer = TracerProvider.getDefaultProvider()
                .createTracer(CLIENT_NAME, CLIENT_VERSION, COGNITIVE_TRACING_NAMESPACE_VALUE, tracingOptions);

            pipeline = new HttpPipelineBuilder()
                .clientOptions(buildClientOptions)
                .httpClient(httpClient)
                .policies(policies.toArray(new HttpPipelinePolicy[0]))
                .tracer(tracer)
                .build();
        }

        if (!isConsolidatedServiceVersion(version)) {
            final TextAnalyticsClientImpl textAnalyticsAPI = new TextAnalyticsClientImplBuilder()
                                                                 .endpoint(endpoint)
                                                                 .apiVersion(serviceVersion.getVersion())
                                                                 .pipeline(pipeline)
                                                                 .buildClient();

            return new TextAnalyticsAsyncClient(textAnalyticsAPI, serviceVersion, defaultCountryHint, defaultLanguage);
        } else {
            final MicrosoftCognitiveLanguageServiceTextAnalysisImpl batchApiTextAnalyticsClient =
                new MicrosoftCognitiveLanguageServiceTextAnalysisImplBuilder()
                    .endpoint(endpoint)
                    .apiVersion(serviceVersion.getVersion())
                    .pipeline(pipeline)
                    .buildClient();

            return new TextAnalyticsAsyncClient(batchApiTextAnalyticsClient, serviceVersion,
                defaultCountryHint, defaultLanguage);
        }
    }

    /**
     * Set the default language option for one client.
     *
     * @param language default language
     * @return The updated {@link TextAnalyticsClientBuilder} object.
     */
    public TextAnalyticsClientBuilder defaultLanguage(String language) {
        this.defaultLanguage = language;
        return this;
    }

    /**
     * Set the default country hint option for one client.
     *
     * @param countryHint default country hint
     * @return The updated {@link TextAnalyticsClientBuilder} object.
     */
    public TextAnalyticsClientBuilder defaultCountryHint(String countryHint) {
        this.defaultCountryHint = countryHint;
        return this;
    }

    /**
     * Sets the service endpoint for the Azure Text Analytics instance.
     *
     * @param endpoint The URL of the Azure Text Analytics instance service requests to and receive responses from.
     * @return The updated {@link TextAnalyticsClientBuilder} object.
     * @throws NullPointerException if {@code endpoint} is null
     * @throws IllegalArgumentException if {@code endpoint} cannot be parsed into a valid URL.
     */
    @Override
    public TextAnalyticsClientBuilder endpoint(String endpoint) {
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
     * {@link TextAnalyticsClientBuilder}.
     *
     * @param keyCredential {@link AzureKeyCredential} API key credential
     * @return The updated {@link TextAnalyticsClientBuilder} object.
     * @throws NullPointerException If {@code keyCredential} is null
     */
    @Override
    public TextAnalyticsClientBuilder credential(AzureKeyCredential keyCredential) {
        this.credential = Objects.requireNonNull(keyCredential, "'keyCredential' cannot be null.");
        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authorize requests sent to the service. Refer to the Azure SDK for Java
     * <a href="https://aka.ms/azsdk/java/docs/identity">identity and authentication</a>
     * documentation for more details on proper usage of the {@link TokenCredential} type.
     *
     * @param tokenCredential {@link TokenCredential} used to authorize requests sent to the service.
     * @return The updated {@link TextAnalyticsClientBuilder} object.
     * @throws NullPointerException If {@code tokenCredential} is null.
     */
    @Override
    public TextAnalyticsClientBuilder credential(TokenCredential tokenCredential) {
        Objects.requireNonNull(tokenCredential, "'tokenCredential' cannot be null.");
        this.tokenCredential = tokenCredential;
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
     * @return The updated {@link TextAnalyticsClientBuilder} object.
     */
    public TextAnalyticsClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        this.httpLogOptions = logOptions;
        return this;
    }

    /**
     * Gets the default Azure Text Analytics headers and query parameters allow list.
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
     * @return The updated TextAnalyticsClientBuilder object.
     * @see HttpClientOptions
     */
    @Override
    public TextAnalyticsClientBuilder clientOptions(ClientOptions clientOptions) {
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
     * @return The updated {@link TextAnalyticsClientBuilder} object.
     * @throws NullPointerException If {@code policy} is null.
     */
    public TextAnalyticsClientBuilder addPolicy(HttpPipelinePolicy policy) {
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
     * @return The updated {@link TextAnalyticsClientBuilder} object.
     */
    @Override
    public TextAnalyticsClientBuilder httpClient(HttpClient client) {
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
     * <p>
     * If {@code pipeline} is set, all other settings are ignored, aside from {@link
     * TextAnalyticsClientBuilder#endpoint(String) endpoint} to build {@link TextAnalyticsAsyncClient} or {@link
     * TextAnalyticsClient}.
     *
     * @param httpPipeline {@link HttpPipeline} to use for sending service requests and receiving responses.
     * @return The updated {@link TextAnalyticsClientBuilder} object.
     */
    @Override
    public TextAnalyticsClientBuilder pipeline(HttpPipeline httpPipeline) {
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
     * @param configuration The configuration store used to
     * @return The updated {@link TextAnalyticsClientBuilder} object.
     */
    @Override
    public TextAnalyticsClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the {@link RetryPolicy} that is used when each request is sent.
     * <p>
     * The default retry policy will be used if not provided {@link TextAnalyticsClientBuilder#buildAsyncClient()} to
     * build {@link TextAnalyticsAsyncClient} or {@link TextAnalyticsClient}.
     * <p>
     * Setting this is mutually exclusive with using {@link #retryOptions(RetryOptions)}.
     *
     * @param retryPolicy user's retry policy applied to each request.
     * @return The updated {@link TextAnalyticsClientBuilder} object.
     */
    public TextAnalyticsClientBuilder retryPolicy(RetryPolicy retryPolicy) {
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
     * @return The updated {@link TextAnalyticsClientBuilder} object.
     */
    @Override
    public TextAnalyticsClientBuilder retryOptions(RetryOptions retryOptions) {
        this.retryOptions = retryOptions;
        return this;
    }

    /**
     * Sets the {@link TextAnalyticsServiceVersion} that is used when making API requests.
     * <p>
     * If a service version is not provided, the service version that will be used will be the latest known service
     * version based on the version of the client library being used. If no service version is specified, updating to a
     * newer version the client library will have the result of potentially moving to a newer service version.
     *
     * @param version {@link TextAnalyticsServiceVersion} of the service to be used when making requests.
     * @return The updated {@link TextAnalyticsClientBuilder} object.
     */
    public TextAnalyticsClientBuilder serviceVersion(TextAnalyticsServiceVersion version) {
        this.version = version;
        return this;
    }

    private boolean isConsolidatedServiceVersion(TextAnalyticsServiceVersion serviceVersion) {
        if (serviceVersion == null) {
            serviceVersion = TextAnalyticsServiceVersion.V2022_05_01;
        }
        return !(TextAnalyticsServiceVersion.V3_0 == serviceVersion
                     || TextAnalyticsServiceVersion.V3_1 == serviceVersion);
    }
}
