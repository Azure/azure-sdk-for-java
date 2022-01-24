// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.attestation;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.client.traits.HttpConfigTrait;
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
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.security.attestation.implementation.AttestationClientImpl;
import com.azure.security.attestation.models.AttestationTokenValidationOptions;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.azure.core.util.CoreUtils.getApplicationId;

/** This class provides a fluent builder API to help add in the configuration and instantiation of the
 * {@link AttestationClient} and {@link AttestationAsyncClient} classes calling the
 * {@link AttestationClientBuilder#buildClient()} or {@link AttestationClientBuilder#buildAsyncClient()}.
 *
 * <p>The minimal configuration option required by {@link AttestationClientBuilder} is {@code String endpoint}.
 *
 * For the {@link AttestationClient#attestTpm(String)} API, the client also requires that a {@link TokenCredential} object
 * be configured.
 *
 * <p><strong>Instantiate a synchronous Attestation Client</strong></p>
 * <!-- src_embed com.azure.security.attestation.AttestationClientBuilder.buildClient -->
 * <pre>
 * AttestationClient client = new AttestationClientBuilder&#40;&#41;
 *     .endpoint&#40;endpoint&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.security.attestation.AttestationClientBuilder.buildClient -->
 * <!-- src_embed com.azure.security.attestation.AttestationClientBuilder.buildAsyncClient -->
 * <pre>
 * AttestationAsyncClient asyncClient = new AttestationClientBuilder&#40;&#41;
 *     .endpoint&#40;endpoint&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.security.attestation.AttestationClientBuilder.buildAsyncClient -->
 *     <p><strong>Build a attestation client for use with the {@link AttestationClient#attestTpm(String)} API</strong></p>
 *     <!-- src_embed com.azure.security.attestation.AttestationClientBuilder.buildAsyncClientForTpm -->
 * <pre>
 * AttestationAsyncClient asyncClientForTpm = new AttestationClientBuilder&#40;&#41;
 *     .endpoint&#40;endpoint&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 *     <!-- end com.azure.security.attestation.AttestationClientBuilder.buildAsyncClientForTpm -->
 */
@ServiceClientBuilder(
        serviceClients = {
            AttestationClient.class,
            AttestationAsyncClient.class,
        })
public final class AttestationClientBuilder implements
    TokenCredentialTrait<AttestationClientBuilder>,
    HttpConfigTrait<AttestationClientBuilder> {
    private static final String SDK_NAME = "name";

    private static final String SDK_VERSION = "version";
    private static final RetryPolicy DEFAULT_RETRY_POLICY = new RetryPolicy("retry-after-ms", ChronoUnit.MILLIS);

    private final String[] dataplaneScope = new String[] {"https://attest.azure.net/.default"};

    private final ClientLogger logger = new ClientLogger(AttestationClientBuilder.class);

    private final List<HttpPipelinePolicy> perCallPolicies = new ArrayList<>();
    private final List<HttpPipelinePolicy> perRetryPolicies = new ArrayList<>();

    private ClientOptions clientOptions;

    private String endpoint;
    private HttpClient httpClient;
    private HttpLogOptions httpLogOptions;
    private HttpPipeline pipeline;
    private HttpPipelinePolicy retryPolicy;
    private Configuration configuration;
    private AttestationServiceVersion serviceVersion;
    private AttestationTokenValidationOptions tokenValidationOptions;
    private SerializerAdapter serializerAdapter;
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
    public AttestationClientBuilder() {
        serviceVersion = AttestationServiceVersion.V2020_10_01;
        tokenValidationOptions = new AttestationTokenValidationOptions();
        httpLogOptions = new HttpLogOptions();
    }

    /**
     * Builds an instance of {@link AttestationClient} synchronous client.
     *
     * Instantiating a synchronous Attestation client:
     * <br>
     * <!-- src_embed com.azure.security.attestation.AttestationClientBuilder.buildClient -->
     * <pre>
     * AttestationClient client = new AttestationClientBuilder&#40;&#41;
     *     .endpoint&#40;endpoint&#41;
     *     .buildClient&#40;&#41;;
     * </pre>
     * <!-- end com.azure.security.attestation.AttestationClientBuilder.buildClient -->
     * @return an instance of {@link AttestationClient}.
     */
    public AttestationClient buildClient() {
        return new AttestationClient(buildAsyncClient());
    }

    /**
     * Builds an instance of AttestationAsyncClient async client.
     *
     * Instantiating a synchronous Attestation client:
     * <br>
     * <!-- src_embed com.azure.security.attestation.AttestationClientBuilder.buildAsyncClient -->
     * <pre>
     * AttestationAsyncClient asyncClient = new AttestationClientBuilder&#40;&#41;
     *     .endpoint&#40;endpoint&#41;
     *     .buildAsyncClient&#40;&#41;;
     * </pre>
     * <!-- end com.azure.security.attestation.AttestationClientBuilder.buildAsyncClient -->
     * @return an instance of {@link AttestationClient}.
     */
    public AttestationAsyncClient buildAsyncClient() {
        return new AttestationAsyncClient(buildInnerClient(), this.tokenValidationOptions);
    }

    /**
     * Sets The attestation endpoint URI, for example https://myinstance.attest.azure.net.
     *
     * @param endpoint The endpoint to connect to.
     * @return the AttestationClientBuilder.
     */
    public AttestationClientBuilder endpoint(String endpoint) {
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
     * @param serviceVersion Specifies the API version to use in the outgoing API calls.
     * @return the AttestationClientBuilder.
     */
    public AttestationClientBuilder serviceVersion(AttestationServiceVersion serviceVersion) {
        Objects.requireNonNull(serviceVersion);
        this.serviceVersion = serviceVersion;
        return this;
    }
    /**
     * Sets the credential to be used for communicating with the service.
     * <p>Note that this property is only required for the {@link AttestationClient#attestTpm(String)} and
     * {@link AttestationAsyncClient#attestTpm(String)} APIs - other attestation APIs can be anonymous.</p>
     * @param credential Specifies the credential to be used for authentication.
     * @return the AttestationClientBuilder.
     */
    @Override
    public AttestationClientBuilder credential(TokenCredential credential) {
        Objects.requireNonNull(credential);
        this.tokenCredential = credential;
        return this;
    }

    /**
     * Sets The HTTP pipeline to send requests through.
     *
     * @param pipeline the pipeline value.
     * @return the AttestationClientBuilder.
     */
    @Override
    public AttestationClientBuilder pipeline(HttpPipeline pipeline) {
        this.pipeline = pipeline;
        return this;
    }

    /**
     * Sets The serializer to serialize an object into a string.
     *
     * @param serializerAdapter the serializerAdapter value.
     * @return the AttestationClientBuilder.
     */
    public AttestationClientBuilder serializerAdapter(SerializerAdapter serializerAdapter) {
        this.serializerAdapter = serializerAdapter;
        return this;
    }

    /**
     * Sets The HTTP client used to send the request.
     *
     * @param httpClient the httpClient value.
     * @return the AttestationClientBuilder.
     */
    @Override
    public AttestationClientBuilder httpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    /**
     * Sets The configuration store that is used during construction of the service client.
     *
     * @param configuration the configuration value.
     * @return the AttestationClientBuilder.
     */
    public AttestationClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets The logging configuration for HTTP requests and responses.
     *
     * @param httpLogOptions the httpLogOptions value.
     * @return the AttestationClientBuilder.
     */
    @Override
    public AttestationClientBuilder httpLogOptions(HttpLogOptions httpLogOptions) {
        this.httpLogOptions = httpLogOptions;
        return this;
    }

    /**
     * Sets The retry policy that will attempt to retry failed requests, if applicable.
     *
     * @param retryPolicy the retryPolicy value.
     * @return the AttestationClientBuilder.
     */
    public AttestationClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
        return this;
    }

    /**
     * Sets the {@link RetryOptions} for the {@link RetryPolicy} that is used when each request is sent.
     *
     * @param retryOptions the {@link RetryOptions} for the {@link RetryPolicy} that is used when each request is sent.
     *
     * @return The updated {@link AttestationClientBuilder} object.
     */
    @Override
    public AttestationClientBuilder retryOptions(RetryOptions retryOptions) {
        Objects.requireNonNull(retryOptions, "'retryOptions' cannot be null.");
        return retryPolicy(new RetryPolicy(retryOptions));
    }

    /**
     * Adds a custom Http pipeline policy.
     *
     * @param policy The custom Http pipeline policy to add.
     * @return this {@link AttestationClientBuilder}.
     */
    @Override
    public AttestationClientBuilder addPolicy(HttpPipelinePolicy policy) {
        Objects.requireNonNull(policy, "'policy' cannot be null.");

        if (policy.getPipelinePosition() == HttpPipelinePosition.PER_CALL) {
            perCallPolicies.add(policy);
        } else {
            perRetryPolicies.add(policy);
        }

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
     * @return the updated {@link AttestationClientBuilder} object
     */
    public AttestationClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
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
     * <!-- src_embed com.azure.security.attestation.AttestationClientBuilder.buildClientWithValidation -->
     * <pre>
     * AttestationClient validatedClient = new AttestationClientBuilder&#40;&#41;
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
     *     <!-- end com.azure.security.attestation.AttestationClientBuilder.buildClientWithValidation -->
     * @param tokenValidationOptions - Validation options used when validating JSON Web Tokens returned by the attestation service.
     * @return this {@link AttestationClientBuilder}
     */
    public AttestationClientBuilder tokenValidationOptions(AttestationTokenValidationOptions tokenValidationOptions) {
        this.tokenValidationOptions = tokenValidationOptions;
        return this;
    }

    /**
     * Builds an instance of AttestationClientImpl with the provided parameters.
     *
     * @return an instance of AttestationClientImpl.
     */
    private AttestationClientImpl buildInnerClient() {

//        AttestationClientImplBuilder clientImplBuilder = new AttestationClientImplBuilder();
        // Global Env configuration store
        Configuration buildConfiguration = (configuration == null)
            ? Configuration.getGlobalConfiguration()
            : configuration;

        // Service version
        AttestationServiceVersion version = serviceVersion != null
            ? serviceVersion
            : AttestationServiceVersion.getLatest();

        // endpoint cannot be null, which is required in request authentication
        String endpoint = this.endpoint;
        Objects.requireNonNull(endpoint, "'Endpoint' is required and can not be null.");

        // If the customer provided a pipeline, use it, otherwise configure the pipeline based on the options
        // which were provided.
        HttpPipeline pipeline = this.pipeline;
        if (pipeline == null) {
            // Closest to API goes first, closest to wire goes last.
            final List<HttpPipelinePolicy> policies = new ArrayList<>();
            policies.add(new UserAgentPolicy(
                getApplicationId(clientOptions, httpLogOptions), CLIENT_NAME, CLIENT_VERSION, buildConfiguration));
            policies.add(new RequestIdPolicy());
            policies.add(new AddHeadersFromContextPolicy());

            policies.addAll(perCallPolicies);
            HttpPolicyProviders.addBeforeRetryPolicies(policies);

            policies.add(retryPolicy == null ? DEFAULT_RETRY_POLICY : retryPolicy);

            policies.add(new AddDatePolicy());

            // If we want an authenticated connection, add a bearer token policy.
            if (tokenCredential != null) {
                // User token based policy
                policies.add(new BearerTokenAuthenticationPolicy(tokenCredential, dataplaneScope));
            }
            policies.addAll(perRetryPolicies);

            if (clientOptions != null) {
                List<HttpHeader> httpHeaderList = new ArrayList<>();
                clientOptions.getHeaders().forEach(
                    header -> httpHeaderList.add(new HttpHeader(header.getName(), header.getValue())));
                policies.add(new AddHeadersPolicy(new HttpHeaders(httpHeaderList)));
            }

            HttpPolicyProviders.addAfterRetryPolicies(policies);
            policies.add(new HttpLoggingPolicy(httpLogOptions));

            // customized pipeline
            pipeline = new HttpPipelineBuilder()
                .policies(policies.toArray(new HttpPipelinePolicy[0]))
                .httpClient(httpClient)
                .build();
        }

        SerializerAdapter serializerAdapter = this.serializerAdapter;
        if (serializerAdapter == null) {
            serializerAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        }

        return new AttestationClientImpl(pipeline, serializerAdapter, endpoint, version.getVersion());
    }
}
