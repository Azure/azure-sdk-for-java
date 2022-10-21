// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

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
import com.azure.core.http.policy.AddHeadersPolicy;
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
import com.azure.core.util.builder.ClientBuilderUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.security.keyvault.certificates.implementation.KeyVaultCredentialPolicy;
import com.azure.security.keyvault.certificates.models.KeyVaultCertificateIdentifier;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of the {@link
 * CertificateAsyncClient certificate async client} and {@link CertificateClient certificate sync client},
 * by calling {@link CertificateClientBuilder#buildAsyncClient() buildAsyncClient} and {@link
 * CertificateClientBuilder#buildClient() buildClient} respectively
 * It constructs an instance of the desired client.
 *
 * <p> The minimal configuration options required by {@link CertificateClientBuilder} to build {@link
 * CertificateAsyncClient}
 * are {@link String vaultUrl} and {@link TokenCredential credential}. </p>
 *
 * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.instantiation -->
 * <pre>
 * CertificateAsyncClient certificateAsyncClient = new CertificateClientBuilder&#40;&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .vaultUrl&#40;&quot;&lt;your-key-vault-url&gt;&quot;&#41;
 *     .httpLogOptions&#40;new HttpLogOptions&#40;&#41;.setLogLevel&#40;HttpLogDetailLevel.BODY_AND_HEADERS&#41;&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.instantiation -->
 *
 * <p>The {@link HttpLogDetailLevel log detail level}, multiple custom {@link HttpLoggingPolicy policies} and custom
 * {@link HttpClient http client} can be optionally configured in the {@link CertificateClientBuilder}.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.certificates.CertificateAsyncClient.instantiation.withHttpClient -->
 * <pre>
 * CertificateAsyncClient certificateAsyncClient = new CertificateClientBuilder&#40;&#41;
 *     .httpLogOptions&#40;new HttpLogOptions&#40;&#41;.setLogLevel&#40;HttpLogDetailLevel.BODY_AND_HEADERS&#41;&#41;
 *     .vaultUrl&#40;&quot;&lt;your-key-vault-url&gt;&quot;&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .httpClient&#40;HttpClient.createDefault&#40;&#41;&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.certificates.CertificateAsyncClient.instantiation.withHttpClient -->
 *
 * <p>Alternatively, custom {@link HttpPipeline http pipeline} with custom {@link HttpPipelinePolicy} policies and
 * {@link String vaultUrl}
 * can be specified. It provides finer control over the construction of {@link CertificateAsyncClient} and {@link
 * CertificateClient}</p>
 *
 * <p> The minimal configuration options required by {@link CertificateClientBuilder certificateClientBuilder} to build
 * {@link CertificateClient}
 * are {@link String vaultUrl} and {@link TokenCredential credential}. </p>
 *
 * <!-- src_embed com.azure.security.keyvault.certificates.CertificateClient.instantiation -->
 * <pre>
 * CertificateClient certificateClient = new CertificateClientBuilder&#40;&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .vaultUrl&#40;&quot;&lt;your-key-vault-url&gt;&quot;&#41;
 *     .httpLogOptions&#40;new HttpLogOptions&#40;&#41;.setLogLevel&#40;HttpLogDetailLevel.BODY_AND_HEADERS&#41;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.certificates.CertificateClient.instantiation -->
 *
 * @see CertificateAsyncClient
 * @see CertificateClient
 */
@ServiceClientBuilder(serviceClients = {CertificateClient.class, CertificateAsyncClient.class})
public final class CertificateClientBuilder implements
    TokenCredentialTrait<CertificateClientBuilder>,
    HttpTrait<CertificateClientBuilder>,
    ConfigurationTrait<CertificateClientBuilder> {
    private final ClientLogger logger = new ClientLogger(CertificateClientBuilder.class);
    // This is properties file's name.
    private static final String AZURE_KEY_VAULT_CERTIFICATES_PROPERTIES = "azure-key-vault-certificates.properties";
    private static final String SDK_NAME = "name";
    private static final String SDK_VERSION = "version";

    private final List<HttpPipelinePolicy> perCallPolicies;
    private final List<HttpPipelinePolicy> perRetryPolicies;
    private final Map<String, String> properties;

    private TokenCredential credential;
    private HttpPipeline pipeline;
    private URL vaultUrl;
    private HttpClient httpClient;
    private HttpLogOptions httpLogOptions;
    private RetryPolicy retryPolicy;
    private RetryOptions retryOptions;
    private Configuration configuration;
    private CertificateServiceVersion version;
    private ClientOptions clientOptions;
    private boolean disableChallengeResourceVerification = false;

    /**
     * The constructor with defaults.
     */
    public CertificateClientBuilder() {
        httpLogOptions = new HttpLogOptions();
        perCallPolicies = new ArrayList<>();
        perRetryPolicies = new ArrayList<>();
        properties = CoreUtils.getProperties(AZURE_KEY_VAULT_CERTIFICATES_PROPERTIES);
    }

    /**
     * Creates a {@link CertificateClient} based on options set in the builder.
     * Every time {@code buildClient()} is called, a new instance of {@link CertificateClient} is created.
     *
     * <p>If {@link CertificateClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and
     * {@link CertificateClientBuilder#vaultUrl(String) serviceEndpoint} are used to create the
     * {@link CertificateClientBuilder client}. All other builder settings are ignored. If {@code pipeline} is not set,
     * then {@link CertificateClientBuilder#credential(TokenCredential) key vault credential}  and
     * {@link CertificateClientBuilder#vaultUrl(String) key vault url} are required to build the {@link
     * CertificateClient client}.</p>
     *
     * @return A {@link CertificateClient} with the options set from the builder.
     *
     * @throws IllegalStateException If {@link CertificateClientBuilder#credential(TokenCredential)} or
     * {@link CertificateClientBuilder#vaultUrl(String)} have not been set.
     * @throws IllegalStateException If both {@link #retryOptions(RetryOptions)}
     * and {@link #retryPolicy(RetryPolicy)} have been set.
     */
    public CertificateClient buildClient() {
        return new CertificateClient(buildAsyncClient());
    }

    /**
     * Creates a {@link CertificateAsyncClient} based on options set in the builder. Every time
     * {@code buildAsyncClient()} is called, a new instance of {@link CertificateAsyncClient} is created.
     *
     * <p>If {@link CertificateClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and
     * {@link CertificateClientBuilder#vaultUrl(String) serviceEndpoint} are used to create the {@link
     * CertificateClientBuilder client}. All other builder settings are ignored. If {@code pipeline} is not set, then
     * {@link CertificateClientBuilder#credential(TokenCredential) key vault credential} and {@link
     * CertificateClientBuilder#vaultUrl(String)} key vault url are required to build the {@link CertificateAsyncClient
     * client}.</p>
     *
     * @return A {@link CertificateAsyncClient} with the options set from the builder.
     *
     * @throws IllegalStateException If {@link CertificateClientBuilder#credential(TokenCredential)} or {@link
     * CertificateClientBuilder#vaultUrl(String)} have not been set.
     * @throws IllegalStateException If both {@link #retryOptions(RetryOptions)}
     * and {@link #retryPolicy(RetryPolicy)} have been set.
     */
    public CertificateAsyncClient buildAsyncClient() {
        Configuration buildConfiguration = (configuration != null) ? configuration
            : Configuration.getGlobalConfiguration().clone();

        URL buildEndpoint = getBuildEndpoint(buildConfiguration);

        if (buildEndpoint == null) {
            throw logger.logExceptionAsError(new IllegalStateException(
                KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.VAULT_END_POINT_REQUIRED)));
        }

        CertificateServiceVersion serviceVersion = version != null ? version : CertificateServiceVersion.getLatest();

        if (pipeline != null) {
            return new CertificateAsyncClient(vaultUrl, pipeline, serviceVersion);
        }

        if (credential == null) {
            throw logger.logExceptionAsError(new IllegalStateException(
                KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.CREDENTIALS_REQUIRED)));
        }

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();

        String clientName = properties.getOrDefault(SDK_NAME, "UnknownName");
        String clientVersion = properties.getOrDefault(SDK_VERSION, "UnknownVersion");

        httpLogOptions = (httpLogOptions == null) ? new HttpLogOptions() : httpLogOptions;

        policies.add(new UserAgentPolicy(CoreUtils.getApplicationId(clientOptions, httpLogOptions), clientName,
            clientVersion, buildConfiguration));

        if (clientOptions != null) {
            List<HttpHeader> httpHeaderList = new ArrayList<>();
            clientOptions.getHeaders().forEach(header ->
                httpHeaderList.add(new HttpHeader(header.getName(), header.getValue())));
            policies.add(new AddHeadersPolicy(new HttpHeaders(httpHeaderList)));
        }

        // Add per call additional policies.
        policies.addAll(perCallPolicies);
        HttpPolicyProviders.addBeforeRetryPolicies(policies);

        // Add retry policy.
        policies.add(ClientBuilderUtil.validateAndGetRetryPolicy(retryPolicy, retryOptions));

        policies.add(new KeyVaultCredentialPolicy(credential, disableChallengeResourceVerification));

        // Add per retry additional policies.
        policies.addAll(perRetryPolicies);

        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(httpLogOptions));

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();

        return new CertificateAsyncClient(vaultUrl, pipeline, serviceVersion);
    }

    /**
     * Sets the vault endpoint URL to send HTTP requests to. You should validate that this URL references a valid Key
     * Vault resource. Refer to the following <a href=https://aka.ms/azsdk/blog/vault-uri>documentation</a> for details.
     *
     * @param vaultUrl The vault endpoint url is used as destination on Azure to send requests to. If you have a
     * certificate identifier, create a new {@link KeyVaultCertificateIdentifier} to parse it and obtain the
     * {@code vaultUrl} and other information.
     *
     * @return The updated {@link CertificateClientBuilder} object.
     *
     * @throws IllegalArgumentException if {@code vaultUrl} is null or it cannot be parsed into a valid URL.
     */
    public CertificateClientBuilder vaultUrl(String vaultUrl) {
        if (vaultUrl == null) {
            throw logger.logExceptionAsError(new NullPointerException("'vaultUrl' cannot be null."));
        }

        try {
            this.vaultUrl = new URL(vaultUrl);
        } catch (MalformedURLException e) {
            throw logger.logExceptionAsError(new IllegalArgumentException("The Azure Key Vault endpoint url is malformed.", e));
        }

        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authorize requests sent to the service. Refer to the Azure SDK for Java
     * <a href="https://aka.ms/azsdk/java/docs/identity">identity and authentication</a>
     * documentation for more details on proper usage of the {@link TokenCredential} type.
     *
     * @param credential {@link TokenCredential} used to authorize requests sent to the service.
     *
     * @return The updated {@link CertificateClientBuilder} object.
     *
     * @throws NullPointerException If {@code credential} is {@code null}.
     *
     */
    @Override
    public CertificateClientBuilder credential(TokenCredential credential) {
        if (credential == null) {
            throw logger.logExceptionAsError(new NullPointerException("'credential' cannot be null."));
        }

        this.credential = credential;

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
     * @return The updated {@link CertificateClientBuilder} object.
     */
    @Override
    public CertificateClientBuilder httpLogOptions(HttpLogOptions logOptions) {
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
     * @return The updated {@link CertificateClientBuilder} object.
     *
     * @throws NullPointerException If {@code policy} is {@code null}.
     */
    @Override
    public CertificateClientBuilder addPolicy(HttpPipelinePolicy policy) {
        if (policy == null) {
            throw logger.logExceptionAsError(new NullPointerException("'policy' cannot be null."));
        }

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
     * @return The updated {@link CertificateClientBuilder} object.
     */
    @Override
    public CertificateClientBuilder httpClient(HttpClient client) {
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
     * The {@link #vaultUrl(String) vaultUrl} is not ignored when
     * {@code pipeline} is set.
     *
     * @param pipeline {@link HttpPipeline} to use for sending service requests and receiving responses.
     * @return The updated {@link CertificateClientBuilder} object.
     */
    @Override
    public CertificateClientBuilder pipeline(HttpPipeline pipeline) {
        this.pipeline = pipeline;

        return this;
    }

    /**
     * Sets the configuration store that is used during construction of the service client.
     *
     * The default configuration store is a clone of the {@link Configuration#getGlobalConfiguration() global
     * configuration store}, use {@link Configuration#NONE} to bypass using configuration settings during construction.
     *
     * @param configuration The configuration store used to get configuration details.
     *
     * @return The updated {@link CertificateClientBuilder} object.
     */
    @Override
    public CertificateClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;

        return this;
    }

    /**
     * Sets the {@link CertificateServiceVersion} that is used when making API requests.
     * <p>
     * If a service version is not provided, the service version that will be used will be the latest known service
     * version based on the version of the client library being used. If no service version is specified, updating to a
     * newer version the client library will have the result of potentially moving to a newer service version.
     *
     * @param version {@link CertificateServiceVersion} of the service to be used when making requests.
     *
     * @return The updated {@link CertificateClientBuilder} object.
     */
    public CertificateClientBuilder serviceVersion(CertificateServiceVersion version) {
        this.version = version;

        return this;
    }

    /**
     * Sets the {@link RetryPolicy} that is used when each request is sent.
     *
     * The default retry policy will be used in the pipeline, if not provided.
     *
     * Setting this is mutually exclusive with using {@link #retryOptions(RetryOptions)}.
     *
     * @param retryPolicy user's retry policy applied to each request.
     *
     * @return The updated {@link CertificateClientBuilder} object.
     */
    public CertificateClientBuilder retryPolicy(RetryPolicy retryPolicy) {
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
     * @return The updated {@link CertificateClientBuilder} object.
     */
    @Override
    public CertificateClientBuilder retryOptions(RetryOptions retryOptions) {
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
     * @see HttpClientOptions
     * @return The updated {@link CertificateClientBuilder} object.
     */
    @Override
    public CertificateClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;

        return this;
    }

    /**
     * Disables verifying if the authentication challenge resource matches the Key Vault domain. This verification is
     * performed by default.
     *
     * @return The updated {@link CertificateClientBuilder} object.
     */
    public CertificateClientBuilder disableChallengeResourceVerification() {
        this.disableChallengeResourceVerification = true;

        return this;
    }

    private URL getBuildEndpoint(Configuration configuration) {
        if (vaultUrl != null) {
            return vaultUrl;
        }

        String configEndpoint = configuration.get("AZURE_KEYVAULT_ENDPOINT");
        if (CoreUtils.isNullOrEmpty(configEndpoint)) {
            return null;
        }

        try {
            return new URL(configEndpoint);
        } catch (MalformedURLException ex) {
            return null;
        }
    }
}
