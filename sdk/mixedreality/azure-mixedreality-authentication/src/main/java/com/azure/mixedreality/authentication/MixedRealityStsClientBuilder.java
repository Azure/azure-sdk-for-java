// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.mixedreality.authentication;

import com.azure.core.annotation.ServiceClientBuilder;
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
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.mixedreality.authentication.implementation.MixedRealityStsRestClientImpl;
import com.azure.mixedreality.authentication.implementation.MixedRealityStsRestClientImplBuilder;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of {@link
 * MixedRealityStsClient MixedRealityStsClients} and {@link MixedRealityStsAsyncClient MixedRealityStsAsyncClient}, call {@link
 * #buildClient() buildClient} and {@link #buildAsyncClient() buildAsyncClient} respectively to construct an instance of
 * the desired client.
 *
 * @see MixedRealityStsAsyncClient
 * @see MixedRealityStsClient
 */
@ServiceClientBuilder(serviceClients = {MixedRealityStsClient.class, MixedRealityStsAsyncClient.class})
public final class MixedRealityStsClientBuilder {
    private static final String MIXED_REALITY_STS_PROPERTIES = "azure-mixedreality-authentication.properties";
    private static final String SDK_NAME = "name";
    private static final String SDK_VERSION = "version";

    private final List<HttpPipelinePolicy> customPolicies = new ArrayList<HttpPipelinePolicy>();
    private final ClientLogger logger = new ClientLogger(MixedRealityStsClientBuilder.class);

    private String accountDomain;
    private String accountId;
    private MixedRealityStsServiceVersion apiVersion;
    private ClientOptions clientOptions;
    private Configuration configuration;
    private String endpoint;
    private HttpClient httpClient;
    private AzureKeyCredential keyCredential;
    private HttpLogOptions logOptions = new HttpLogOptions();
    private HttpPipeline pipeline;
    private RetryPolicy retryPolicy;
    private TokenCredential tokenCredential;

    /**
     * Constructs a new builder used to configure and build {@link MixedRealityStsClient MixedRealityStsClients} and
     * {@link MixedRealityStsAsyncClient MixedRealityStsAsyncClients}.
     */
    public MixedRealityStsClientBuilder() {
    }

    /**
     * Sets the Mixed Reality service account domain.
     *
     * @param accountDomain The Mixed Reality service account domain.
     * @return The updated {@link MixedRealityStsClientBuilder} object.
     * @throws IllegalArgumentException If {@code accountDomain} is null or empty.
     */
    public MixedRealityStsClientBuilder accountDomain(String accountDomain) {
        Objects.requireNonNull(accountDomain, "'accountDomain' cannot be null.");

        if (accountDomain.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'accountDomain' cannot be an empty string."));
        }

        this.accountDomain = accountDomain;

        return this;
    }

    /**
     * Sets the Mixed Reality service account identifier.
     *
     * @param accountId The Mixed Reality service account identifier. The value is expected to be in UUID format.
     * @return The updated {@link MixedRealityStsClientBuilder} object.
     * @throws IllegalArgumentException If {@code accountId} is null or empty.
     */
    public MixedRealityStsClientBuilder accountId(String accountId) {
        Objects.requireNonNull(accountId, "'accountId' cannot be null.");

        if (accountId.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'accountId' cannot be an empty string."));
        }

        this.accountId = accountId;

        return this;
    }

    /**
     * Apply additional {@link HttpPipelinePolicy policies}.
     *
     * @param customPolicy An HttpPipelinePolicy object to be applied after the defaults.
     * @return The updated {@link MixedRealityStsClientBuilder} object.
     */
    public MixedRealityStsClientBuilder addPolicy(HttpPipelinePolicy customPolicy) {
        this.customPolicies.add(Objects.requireNonNull(customPolicy, "'customPolicy' cannot be null."));

        return this;
    }

    /**
     * Create a {@link MixedRealityStsClient} based on options set in the builder. Every time {@code buildClient()} is
     * called a new instance of {@link MixedRealityStsClient} is created.
     *
     * @return A {@link MixedRealityStsClient} with the options set from the builder.
     */
    public MixedRealityStsClient buildClient() {
        return new MixedRealityStsClient(this.buildAsyncClient());
    }

    /**
     * Create a {@link MixedRealityStsAsyncClient} based on options set in the builder. Every time {@code buildAsyncClient()} is
     * called a new instance of {@link MixedRealityStsAsyncClient} is created.
     *
     * @return A {@link MixedRealityStsAsyncClient} with the options set from the builder.
     * @throws NullPointerException If any required values are null.
     * @throws IllegalArgumentException If the accountId or endpoint are not properly formatted.
     */
    public MixedRealityStsAsyncClient buildAsyncClient() {
        Objects.requireNonNull(this.accountId, "The 'accountId' has not been set and is required.");
        Objects.requireNonNull(this.accountDomain, "The 'accountDomain' has not been set and is required.");

        UUID accountId;
        try {
            accountId = UUID.fromString(this.accountId);
        } catch (IllegalArgumentException ex) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("The 'accountId' must be a UUID formatted value."));
        }

        String endpoint;
        if (this.endpoint != null) {
            try {
                new URL(this.endpoint);
                endpoint = this.endpoint;
            } catch (MalformedURLException ex) {
                throw logger.logExceptionAsWarning(new IllegalArgumentException("The 'endpoint' must be a valid URL."));
            }
        } else {
            endpoint = AuthenticationEndpoint.constructFromDomain(this.accountDomain);
        }

        if (this.pipeline == null) {
            if (this.tokenCredential != null && this.keyCredential != null) {
                throw logger.logExceptionAsWarning(
                    new IllegalArgumentException("Only a single type of credential may be specified."));
            }

            if (this.tokenCredential == null && this.keyCredential != null) {
                this.tokenCredential = new MixedRealityAccountKeyCredential(accountId, this.keyCredential);
            }

            Objects.requireNonNull(this.tokenCredential, "The 'credential' has not been set and is required.");
            String scope = AuthenticationEndpoint.constructScope(endpoint);
            HttpPipelinePolicy authPolicy = new BearerTokenAuthenticationPolicy(this.tokenCredential, scope);
            this.pipeline = createHttpPipeline(this.httpClient, authPolicy, this.customPolicies);
        }

        MixedRealityStsServiceVersion version;

        if (this.apiVersion != null) {
            version = this.apiVersion;
        } else {
            version = MixedRealityStsServiceVersion.getLatest();
        }

        MixedRealityStsRestClientImpl serviceClient = new MixedRealityStsRestClientImplBuilder()
            .apiVersion(version.getVersion())
            .pipeline(this.pipeline)
            .host(endpoint)
            .buildClient();

        return new MixedRealityStsAsyncClient(accountId, serviceClient);
    }

    /**
     * Sets the {@link ClientOptions} which enables various options to be set on the client. For example setting an
     * {@code applicationId} using {@link ClientOptions#setApplicationId(String)} to configure
     * the {@link UserAgentPolicy} for telemetry/monitoring purposes.
     *
     * <p>More About <a href="https://azure.github.io/azure-sdk/general_azurecore.html#telemetry-policy">Azure Core: Telemetry policy</a>
     *
     * @param clientOptions the {@link ClientOptions} to be set on the client.
     * @return The updated {@link MixedRealityStsClientBuilder} object.
     */
    public MixedRealityStsClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;

        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authenticate HTTP requests.
     *
     * @param tokenCredential The {@link TokenCredential} used to authenticate HTTP requests.
     * @return The updated {@link MixedRealityStsClientBuilder} object.
     * @throws NullPointerException If {@code tokenCredential} is null.
     */
    public MixedRealityStsClientBuilder credential(TokenCredential tokenCredential) {
        this.tokenCredential = Objects.requireNonNull(tokenCredential, "'tokenCredential' cannot be null.");

        return this;
    }

    /**
     * Sets the {@link AzureKeyCredential} used to authenticate HTTP requests.
     *
     * <p>
     * <b>Note:</b> Not recommended for production applications.
     *
     * @param keyCredential The {@link AzureKeyCredential} used to authenticate HTTP requests.
     * @return The updated {@link MixedRealityStsClientBuilder} object.
     * @throws NullPointerException If {@code keyCredential} is null.
     */
    public MixedRealityStsClientBuilder credential(AzureKeyCredential keyCredential) {
        this.keyCredential = Objects.requireNonNull(keyCredential, "'keyCredential' cannot be null.");

        return this;
    }

    /**
     * Sets the configuration store that is used during construction of the service client.
     *
     * The default configuration store is a clone of the {@link Configuration#getGlobalConfiguration() global
     * configuration store}, use {@link Configuration#NONE} to bypass using configuration settings during construction.
     *
     * @param configuration The configuration store used to
     * @return The updated MixedRealityStsClientBuilder object.
     */
    public MixedRealityStsClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;

        return this;
    }

    /**
     * Sets the Mixed Reality STS service endpoint.
     *
     * @param endpoint The Mixed Reality STS service endpoint.
     * @return The updated MixedRealityStsClientBuilder object.
     * @throws IllegalArgumentException If {@code endpoint} is null or it cannot be parsed into a valid URL.
     */
    public MixedRealityStsClientBuilder endpoint(String endpoint) {
        this.endpoint = endpoint;

        return this;
    }

    /**
     * Sets the HTTP client to use for sending and receiving requests to and from the service.
     *
     * @param client The HTTP client to use for requests.
     * @return The updated ConfigurationClientBuilder object.
     */
    public MixedRealityStsClientBuilder httpClient(HttpClient client) {
        if (this.httpClient != null && client == null) {
            logger.info("HttpClient is being set to 'null' when it was previously configured.");
        }

        this.httpClient = client;

        return this;
    }

    /**
     * Sets the {@link HttpLogOptions} for service requests.
     *
     * @param logOptions The logging configuration to use when sending and receiving HTTP requests/responses.
     * @return The updated {@link MixedRealityStsClientBuilder} object.
     */
    public MixedRealityStsClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        this.logOptions = Objects.requireNonNull(logOptions, "'logOptions' cannot be null.");

        return this;
    }

    /**
     * Sets the HTTP pipeline to use for the service client.
     * <p>
     * If {@code pipeline} is set, all other settings are ignored, aside from {@link
     * MixedRealityStsClientBuilder#endpoint(String) endpoint} to build {@link MixedRealityStsAsyncClient} or {@link
     * MixedRealityStsClient}.
     *
     * @param pipeline The HTTP pipeline to use for sending service requests and receiving responses.
     * @return The updated {@link MixedRealityStsClientBuilder} object.
     */
    public MixedRealityStsClientBuilder pipeline(HttpPipeline pipeline) {
        if (this.pipeline != null && pipeline == null) {
            logger.info("HttpPipeline is being set to 'null' when it was previously configured.");
        }

        this.pipeline = pipeline;

        return this;
    }

    /**
     * Sets the {@link RetryPolicy} that is used to retry requests.
     * <p>
     * The default retry policy will be used if not provided {@link MixedRealityStsClientBuilder#buildAsyncClient()} to
     * build {@link MixedRealityStsAsyncClient} or {@link MixedRealityStsClient}.
     *
     * @param retryPolicy The {@link RetryPolicy} that will be used to retry requests.
     * @return The updated MixedRealityStsClientBuilder object.
     */
    public MixedRealityStsClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;

        return this;
    }

    /**
     * Sets the {@link MixedRealityStsServiceVersion} that is used when making API requests.
     * <p>
     * If a service version is not provided, the service version that will be used will be the latest known service
     * version based on the version of the client library being used. If no service version is specified, updating to a
     * newer version the client library will have the result of potentially moving to a newer service version.
     *
     * @param version {@link MixedRealityStsServiceVersion} of the service to be used when making requests.
     * @return The updated ConfigurationClientBuilder object.
     */
    public MixedRealityStsClientBuilder serviceVersion(MixedRealityStsServiceVersion version) {
        this.apiVersion = version;

        return this;
    }

    private void applyRequiredPolicies(List<HttpPipelinePolicy> policies) {
        RetryPolicy retryPolicy = this.retryPolicy != null
            ? this.retryPolicy
            : new RetryPolicy();

        policies.add(getUserAgentPolicy());

        // If client options has headers configured, add a policy for each.
        if (this.clientOptions != null) {
            List<HttpHeader> httpHeaderList = new ArrayList<>();
            this.clientOptions.getHeaders().forEach(header ->
                httpHeaderList.add(new HttpHeader(header.getName(), header.getValue())));
            policies.add(new AddHeadersPolicy(new HttpHeaders(httpHeaderList)));
        }

        policies.add(retryPolicy);
        policies.add(new CookiePolicy());
        policies.add(new HttpLoggingPolicy(this.logOptions));
    }

    private HttpPipeline createHttpPipeline(HttpClient httpClient,
                                            HttpPipelinePolicy authorizationPolicy,
                                            List<HttpPipelinePolicy> additionalPolicies) {

        List<HttpPipelinePolicy> policies = new ArrayList<HttpPipelinePolicy>();
        policies.add(authorizationPolicy);
        applyRequiredPolicies(policies);

        if (additionalPolicies != null && additionalPolicies.size() > 0) {
            policies.addAll(additionalPolicies);
        }

        return new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();
    }

    /*
     * Creates a {@link UserAgentPolicy} using the default service module name and version.
     *
     * @return The default {@link UserAgentPolicy} for the module.
     */
    private UserAgentPolicy getUserAgentPolicy() {
        Map<String, String> properties = CoreUtils.getProperties(MIXED_REALITY_STS_PROPERTIES);

        String clientName = properties.getOrDefault(SDK_NAME, "UnknownName");
        String clientVersion = properties.getOrDefault(SDK_VERSION, "UnknownVersion");

        // Give precedence to applicationId configured in clientOptions over the one configured in httpLogOptions.
        // Azure.Core deprecated setting the applicationId in httpLogOptions, but we should still support it.
        String applicationId = this.clientOptions == null
            ? this.logOptions.getApplicationId()
            : this.clientOptions.getApplicationId();

        return new UserAgentPolicy(
            applicationId, clientName, clientVersion, this.configuration);
    }
}
