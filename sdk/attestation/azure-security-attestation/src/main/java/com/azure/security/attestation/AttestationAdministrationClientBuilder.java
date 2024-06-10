// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.attestation;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.client.traits.ConfigurationTrait;
import com.azure.core.client.traits.EndpointTrait;
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
import com.azure.core.util.builder.ClientBuilderUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.security.attestation.implementation.AttestationClientImpl;
import com.azure.security.attestation.models.AttestationPolicySetOptions;
import com.azure.security.attestation.models.AttestationTokenValidationOptions;
import com.azure.security.attestation.models.AttestationType;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.azure.core.util.CoreUtils.getApplicationId;

/**
 * This class provides a fluent builder API to help add in the configuration and instantiation of the
 * administrative APIs implemented by the Attestation Service:
 * {@link com.azure.security.attestation.AttestationAdministrationClient} and
 * {@link com.azure.security.attestation.AttestationAdministrationAsyncClient} classes calling the
 * {@link AttestationClientBuilder#buildClient()} or {@link AttestationClientBuilder#buildAsyncClient()}.
 *
 * <p>
 * More information on attestation policies can be found <a href='https://docs.microsoft.com/azure/attestation/basic-concepts#attestation-policy'>here</a>
 * </p>
 *
 * There are two main families of APIs available from the Administration client.
 * <ul>
 *     <li>Attestation Policy Management</li>
 *     <li>Policy Management Certificate Management</li>
 * </ul>
 *
 * The Policy Management APIs provide the ability to retrieve, modify and reset attestation policies.
 * The policy management APIs are:
 * <ul>
 *     <li>
 * {@link AttestationAdministrationClient#getAttestationPolicy(AttestationType)}
 * </li>
 *     <li>
 * {@link AttestationAdministrationAsyncClient#getAttestationPolicy(AttestationType)}
 * </li>
 *     <li>
 * {@link AttestationAdministrationClient#setAttestationPolicy(AttestationType, AttestationPolicySetOptions)}
 * </li>
 *     <li>
 * {@link AttestationAdministrationAsyncClient#setAttestationPolicy(AttestationType, AttestationPolicySetOptions)}
 * </li>
 *     <li>
 * {@link AttestationAdministrationClient#resetAttestationPolicy(AttestationType, AttestationPolicySetOptions)}
 * </li>
 *     <li>
 * {@link AttestationAdministrationAsyncClient#resetAttestationPolicy(AttestationType, AttestationPolicySetOptions)}
 * </li>
 * </ul>
 * <p>
 *     The Policy Management Certificate APIs provide the ability to manage the certificates which are
 *     used to establish authorization for Isolated mode attestation service instances. They include apis to
 *     enumerate, add and remove policy management certificates.
 * </p>
 *
 * <p>The minimal configuration options required by {@link AttestationClientBuilder} are:
 * <ul>
 *     <li>A {@link String} endpoint.</li>
 *     <li>A {@link TokenCredential} object.</li>
 * </ul>
 *
 * <p><strong>Instantiate a synchronous Attestation Client</strong></p>
 * <!-- src_embed com.azure.security.attestation.AttestationAdministrationClientBuilder.buildClient -->
 * <pre>
 * AttestationAdministrationClient client = new AttestationAdministrationClientBuilder&#40;&#41;
 *     .endpoint&#40;endpoint&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.security.attestation.AttestationAdministrationClientBuilder.buildClient -->
 * <!-- src_embed com.azure.security.attestation.AttestationAdministrationClientBuilder.buildAsyncClient -->
 * <pre>
 * AttestationAdministrationAsyncClient asyncClient = new AttestationAdministrationClientBuilder&#40;&#41;
 *     .endpoint&#40;endpoint&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.security.attestation.AttestationAdministrationClientBuilder.buildAsyncClient -->
 */
@ServiceClientBuilder(
    serviceClients = {
        AttestationAdministrationClient.class, AttestationAdministrationAsyncClient.class, })
public final class AttestationAdministrationClientBuilder
    implements ConfigurationTrait<AttestationAdministrationClientBuilder>,
    EndpointTrait<AttestationAdministrationClientBuilder>, HttpTrait<AttestationAdministrationClientBuilder>,
    TokenCredentialTrait<AttestationAdministrationClientBuilder> {
    private static final String SDK_NAME = "name";
    private static final String SDK_VERSION = "version";
    private static final RetryPolicy DEFAULT_RETRY_POLICY = new RetryPolicy("retry-after-ms", ChronoUnit.MILLIS);
    private static final ClientOptions DEFAULT_CLIENT_OPTIONS = new ClientOptions();

    private final String[] dataplaneScope = new String[] { "https://attest.azure.net/.default" };

    private final ClientLogger logger = new ClientLogger(AttestationAdministrationClientBuilder.class);

    private final List<HttpPipelinePolicy> perCallPolicies = new ArrayList<>();
    private final List<HttpPipelinePolicy> perRetryPolicies = new ArrayList<>();

    private ClientOptions clientOptions;

    private String endpoint;
    private HttpClient httpClient;
    private HttpLogOptions httpLogOptions;
    private HttpPipeline pipeline;
    private HttpPipelinePolicy retryPolicy;
    private RetryOptions retryOptions;
    private Configuration configuration;
    private AttestationServiceVersion serviceVersion;
    private AttestationTokenValidationOptions tokenValidationOptions;
    private TokenCredential tokenCredential = null;
    private static final String CLIENT_NAME;
    private static final String CLIENT_VERSION;

    static {
        Map<String, String> properties = CoreUtils.getProperties("azure-security-attestation.properties");
        CLIENT_NAME = properties.getOrDefault(SDK_NAME, "UnknownName");
        CLIENT_VERSION = properties.getOrDefault(SDK_VERSION, "UnknownVersion");
    }

    /**
     * Creates a new instance of the AttestationClientBuilder class.
     */
    public AttestationAdministrationClientBuilder() {
        serviceVersion = AttestationServiceVersion.V2020_10_01;
        tokenValidationOptions = new AttestationTokenValidationOptions();
        httpLogOptions = new HttpLogOptions();
    }

    /**
     * Builds an instance of AttestationClient sync client.
     *
     * Instantiating a synchronous Attestation client:
     * <br>
     * <!-- src_embed com.azure.security.attestation.AttestationAdministrationClientBuilder.buildClient -->
     * <pre>
     * AttestationAdministrationClient client = new AttestationAdministrationClientBuilder&#40;&#41;
     *     .endpoint&#40;endpoint&#41;
     *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
     *     .buildClient&#40;&#41;;
     * </pre>
     * <!-- end com.azure.security.attestation.AttestationAdministrationClientBuilder.buildClient -->
     *
     * @return an instance of {@link AttestationClient}.
     * @throws IllegalStateException If both {@link #retryOptions(RetryOptions)}
     * and {@link #retryPolicy(RetryPolicy)} have been set.
     */
    public AttestationAdministrationClient buildClient() {
        return new AttestationAdministrationClient(buildAsyncClient());
    }

    /**
     * Builds an instance of AttestationAsyncClient async client.
     * <p>
     * Instantiating a synchronous Attestation client:
     * <br>
     * <!-- src_embed com.azure.security.attestation.AttestationAdministrationClientBuilder.buildAsyncClient -->
     * <pre>
     * AttestationAdministrationAsyncClient asyncClient = new AttestationAdministrationClientBuilder&#40;&#41;
     *     .endpoint&#40;endpoint&#41;
     *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
     *     .buildAsyncClient&#40;&#41;;
     * </pre>
     * <!-- end com.azure.security.attestation.AttestationAdministrationClientBuilder.buildAsyncClient -->
     *
     * @return an instance of {@link AttestationClient}.
     * @throws IllegalStateException If both {@link #retryOptions(RetryOptions)}
     * and {@link #retryPolicy(RetryPolicy)} have been set.
     */
    public AttestationAdministrationAsyncClient buildAsyncClient() {
        return new AttestationAdministrationAsyncClient(buildInnerClient(), this.tokenValidationOptions);
    }

    /**
     * Sets The attestation endpoint URI, for example https://mytenant.attest.azure.net.
     *
     * @param endpoint The endpoint to connect to.
     * @return the AttestationClientBuilder.
     */
    @Override
    public AttestationAdministrationClientBuilder endpoint(String endpoint) {
        Objects.requireNonNull(endpoint);
        try {
            new URL(endpoint);
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsError(new IllegalArgumentException(ex));
        }
        this.endpoint = endpoint;
        return this;
    }

    /**
     * Sets the desired API version for this attestation client.
     *
     * @param serviceVersion Specifies the API version to use in the outgoing API calls.
     * @return the AttestationClientBuilder.
     */
    public AttestationAdministrationClientBuilder serviceVersion(AttestationServiceVersion serviceVersion) {
        Objects.requireNonNull(serviceVersion);
        this.serviceVersion = serviceVersion;
        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authorize requests sent to the service. Refer to the Azure SDK for Java
     * <a href="https://aka.ms/azsdk/java/docs/identity">identity and authentication</a>
     * documentation for more details on proper usage of the {@link TokenCredential} type.
     *
     * @param credential {@link TokenCredential} used to authorize requests sent to the service.
     * @return the AttestationClientBuilder.
     */
    @Override
    public AttestationAdministrationClientBuilder credential(TokenCredential credential) {
        Objects.requireNonNull(credential);
        this.tokenCredential = credential;
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
     * @param pipeline {@link HttpPipeline} to use for sending service requests and receiving responses.
     * @return the AttestationClientBuilder.
     */
    @Override
    public AttestationAdministrationClientBuilder pipeline(HttpPipeline pipeline) {
        this.pipeline = pipeline;
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
     * @return the AttestationClientBuilder.
     */
    @Override
    public AttestationAdministrationClientBuilder httpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    /**
     * Sets the client-specific configuration used to retrieve client or global configuration properties
     * when building a client.
     *
     * @param configuration Configuration store used to retrieve client configurations.
     * @return the AttestationClientBuilder.
     */
    @Override
    public AttestationAdministrationClientBuilder configuration(Configuration configuration) {
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
     * @return the AttestationClientBuilder.
     */
    @Override
    public AttestationAdministrationClientBuilder httpLogOptions(HttpLogOptions httpLogOptions) {
        this.httpLogOptions = httpLogOptions;
        return this;
    }

    /**
     * Sets The retry policy that will attempt to retry failed requests, if applicable.
     *
     * <p>
     * Setting this is mutually exclusive with using {@link #retryOptions(RetryOptions)}.
     *
     * @param retryPolicy the retryPolicy value.
     * @return the AttestationClientBuilder.
     */
    public AttestationAdministrationClientBuilder retryPolicy(RetryPolicy retryPolicy) {
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
     * @return the AttestationAdministrationClientBuilder.
     */
    @Override
    public AttestationAdministrationClientBuilder retryOptions(RetryOptions retryOptions) {
        this.retryOptions = retryOptions;
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
     * @return the updated {@link AttestationAdministrationClientBuilder} object
     * @see HttpClientOptions
     */
    @Override
    public AttestationAdministrationClientBuilder clientOptions(ClientOptions clientOptions) {
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
     * @return this {@link AttestationAdministrationClientBuilder}.
     * @throws NullPointerException If {@code pipelinePolicy} is {@code null}.
     */
    @Override
    public AttestationAdministrationClientBuilder addPolicy(HttpPipelinePolicy policy) {
        Objects.requireNonNull(policy, "'policy' cannot be null.");

        if (policy.getPipelinePosition() == HttpPipelinePosition.PER_CALL) {
            perCallPolicies.add(policy);
        } else {
            perRetryPolicies.add(policy);
        }
        return this;
    }

    /**
     * Sets {@link com.azure.security.attestation.models.AttestationToken} validation options for clients created from this builder.
     * <p>Because attestation service clients need to have the ability to validate that the data returned by the attestation
     * service actually originated from within the service, most Attestation Service APIs embed their response in a
     * <a href=https://datatracker.ietf.org/doc/html/rfc7519>RFC 7519 JSON Web Token</a>.</p>
     * <p>The {@link AttestationTokenValidationOptions} provides a mechanism for a client to customize the validation
     * of responses sent by the attestation service.</p>
     * <p>The {@code tokenValidationOptions} property sets the default validation options used by the {@link AttestationClient}
     * or {@link AttestationAsyncClient} returned from this builder.</p>
     * <p>Note: most APIs allow this value to be overridden on a per-api basis if that flexibility is needed.</p>
     *
     * <!-- src_embed com.azure.security.attestation.AttestationAdministrationClientBuilder.buildClientWithValidation -->
     * <pre>
     * AttestationAdministrationClient validatedClient = new AttestationAdministrationClientBuilder&#40;&#41;
     *     .endpoint&#40;endpoint&#41;
     *     .tokenValidationOptions&#40;new AttestationTokenValidationOptions&#40;&#41;
     *         &#47;&#47; Allow 10 seconds of clock drift between attestation service and client.
     *         .setValidationSlack&#40;Duration.ofSeconds&#40;10&#41;&#41;
     *         .setValidationCallback&#40;&#40;token, signer&#41; -&gt; &#123; &#47;&#47; Perform custom validation steps.
     *             System.out.printf&#40;&quot;Validate token signed by signer %s&#92;n&quot;,
     *                 signer.getCertificates&#40;&#41;.get&#40;0&#41;.getSubjectDN&#40;&#41;.toString&#40;&#41;&#41;;
     *         &#125;&#41;&#41;
     *     .buildClient&#40;&#41;;
     * </pre>
     * <!-- end com.azure.security.attestation.AttestationAdministrationClientBuilder.buildClientWithValidation -->
     *
     * @param tokenValidationOptions - Validation options used when validating JSON Web Tokens returned by the attestation service.
     * @return this {@link AttestationAdministrationClientBuilder}
     */
    public AttestationAdministrationClientBuilder tokenValidationOptions(
        AttestationTokenValidationOptions tokenValidationOptions) {
        this.tokenValidationOptions = tokenValidationOptions;
        return this;
    }

    /**
     * Builds an instance of AttestationClientImpl with the provided parameters.
     *
     * @return an instance of AttestationClientImpl.
     */
    private AttestationClientImpl buildInnerClient() {
        // Global Env configuration store
        Configuration buildConfiguration = (configuration == null)
            ? Configuration.getGlobalConfiguration()
            : configuration;

        // Service version
        AttestationServiceVersion version = serviceVersion != null
            ? serviceVersion
            : AttestationServiceVersion.getLatest();

        // endpoint cannot be null.
        String endpoint = this.endpoint;
        Objects.requireNonNull(endpoint, "'Endpoint' is required and can not be null.");

        // If the customer provided a pipeline, use it, otherwise configure the pipeline based on the options
        // which were provided.

        HttpPipeline pipeline = this.pipeline;
        if (pipeline == null) {
            ClientOptions localClientOptions = clientOptions != null ? clientOptions : DEFAULT_CLIENT_OPTIONS;

            // Closest to API goes first, closest to wire goes last.
            final List<HttpPipelinePolicy> policies = new ArrayList<>();
            policies.add(
                new UserAgentPolicy(getApplicationId(localClientOptions, httpLogOptions), CLIENT_NAME, CLIENT_VERSION,
                    buildConfiguration));
            policies.add(new RequestIdPolicy());
            policies.add(new AddHeadersFromContextPolicy());

            policies.addAll(perCallPolicies);
            HttpPolicyProviders.addBeforeRetryPolicies(policies);

            policies.add(ClientBuilderUtil.validateAndGetRetryPolicy(retryPolicy, retryOptions, DEFAULT_RETRY_POLICY));

            policies.add(new AddDatePolicy());

            // If we want an authenticated connection, add a bearer token policy.
            if (tokenCredential != null) {
                // User token based policy
                policies.add(new BearerTokenAuthenticationPolicy(tokenCredential, dataplaneScope));
            }
            policies.addAll(perRetryPolicies);

            List<HttpHeader> httpHeaderList = new ArrayList<>();
            localClientOptions.getHeaders()
                .forEach(header -> httpHeaderList.add(new HttpHeader(header.getName(), header.getValue())));
            policies.add(new AddHeadersPolicy(new HttpHeaders(httpHeaderList)));

            HttpPolicyProviders.addAfterRetryPolicies(policies);
            policies.add(new HttpLoggingPolicy(httpLogOptions));

            // Create a new pipeline based on the policies and with the specified HTTP client.
            pipeline = new HttpPipelineBuilder().policies(policies.toArray(new HttpPipelinePolicy[0]))
                .httpClient(httpClient)
                .clientOptions(localClientOptions)
                .build();
        }

        return new AttestationClientImpl(pipeline, endpoint, version.getVersion());
    }
}
