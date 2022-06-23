// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.implementation.AzureCommunicationCallingServerServiceImpl;
import com.azure.communication.callingserver.implementation.AzureCommunicationCallingServerServiceImplBuilder;
import com.azure.communication.common.implementation.CommunicationConnectionString;
import com.azure.communication.common.implementation.HmacAuthenticationPolicy;
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
import com.azure.core.http.policy.RedirectPolicy;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.builder.ClientBuilderUtil;
import com.azure.core.util.logging.ClientLogger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Client builder that creates CallingServerAsyncClient and CallingServerClient.
 *
 * <p><strong>Instantiating synchronous and asynchronous Calling Server Clients</strong></p>
 */

@ServiceClientBuilder(serviceClients = { CallingServerClient.class, CallingServerAsyncClient.class })
public final class CallingServerClientBuilder implements
    AzureKeyCredentialTrait<CallingServerClientBuilder>,
    ConfigurationTrait<CallingServerClientBuilder>,
    ConnectionStringTrait<CallingServerClientBuilder>,
    EndpointTrait<CallingServerClientBuilder>,
    HttpTrait<CallingServerClientBuilder>,
    TokenCredentialTrait<CallingServerClientBuilder> {
    private static final String SDK_NAME = "name";
    private static final String SDK_VERSION = "version";
    private static final String APP_CONFIG_PROPERTIES = "azure-communication-callingserver.properties";

    private final ClientLogger logger = new ClientLogger(CallingServerClientBuilder.class);
    private String connectionString;
    private String endpoint;
    private String hostName;
    private AzureKeyCredential azureKeyCredential;
    private TokenCredential tokenCredential;
    private HttpClient httpClient;
    private HttpLogOptions httpLogOptions = new HttpLogOptions();
    private HttpPipeline pipeline;
    private Configuration configuration;
    private final Map<String, String> properties = CoreUtils.getProperties(APP_CONFIG_PROPERTIES);
    private final List<HttpPipelinePolicy> customPolicies = new ArrayList<>();
    private ClientOptions clientOptions;
    private RetryPolicy retryPolicy;
    private RetryOptions retryOptions;

    /**
     * Set endpoint of the service.
     *
     * @param endpoint url of the service.
     * @return CallingServerClientBuilder object.
     */
    @Override
    public CallingServerClientBuilder endpoint(String endpoint) {
        this.endpoint = Objects.requireNonNull(endpoint, "'endpoint' cannot be null.");
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
     * @param pipeline {@link HttpPipeline} to use for sending service requests and receiving responses,
     * if a pipeline is not supplied, the
     * credential and httpClient fields must be set.
     * @return CallingServerClientBuilder object.
     */
    @Override
    public CallingServerClientBuilder pipeline(HttpPipeline pipeline) {
        this.pipeline = Objects.requireNonNull(pipeline, "'pipeline' cannot be null.");
        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authorize requests sent to the service. Refer to the Azure SDK for Java
     * <a href="https://aka.ms/azsdk/java/docs/identity">identity and authentication</a>
     * documentation for more details on proper usage of the {@link TokenCredential} type.
     *
     * @param tokenCredential {@link TokenCredential} used to authorize requests sent to the service.
     * @return Updated {@link CallingServerClientBuilder} object.
     * @throws NullPointerException If {@code tokenCredential} is null.
     */
    @Override
    public CallingServerClientBuilder credential(TokenCredential tokenCredential) {
        this.tokenCredential = Objects.requireNonNull(tokenCredential, "'tokenCredential' cannot be null.");
        return this;
    }

    /**
     * Sets the {@link AzureKeyCredential} used to authenticate HTTP requests.
     *
     * @param keyCredential The {@link AzureKeyCredential} used to authenticate HTTP
     *                      requests.
     * @return Updated {@link CallingServerClientBuilder} object.
     * @throws NullPointerException If {@code keyCredential} is null.
     */
    @Override
    public CallingServerClientBuilder credential(AzureKeyCredential keyCredential) {
        this.azureKeyCredential = Objects.requireNonNull(keyCredential, "'keyCredential' cannot be null.");
        return this;
    }

    /**
     * Set connectionString to use.
     *
     * @param connectionString connection string to set.
     * @return Updated {@link CallingServerClientBuilder} object.
     */
    @Override
    public CallingServerClientBuilder connectionString(String connectionString) {
        Objects.requireNonNull(connectionString, "'connectionString' cannot be null.");
        this.connectionString = connectionString;
        return this;
    }

    /**
     * Sets the retry policy to use (using the RetryPolicy type).
     * <p>
     * Setting this is mutually exclusive with using {@link #retryOptions(RetryOptions)}.
     *
     * @param retryPolicy object to be applied
     * @return Updated {@link CallingServerClientBuilder} object.
     */
    public CallingServerClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = Objects.requireNonNull(retryPolicy, "'retryPolicy' cannot be null.");
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
     * @return Updated {@link CallingServerClientBuilder} object.
     */
    @Override
    public CallingServerClientBuilder retryOptions(RetryOptions retryOptions) {
        this.retryOptions = retryOptions;
        return this;
    }

    /**
     * Sets the configuration object used to retrieve environment configuration
     * values during building of the client.
     *
     * @param configuration Configuration store used to retrieve environment
     * configurations.
     * @return Updated {@link CallingServerClientBuilder} object.
     */
    @Override
    public CallingServerClientBuilder configuration(Configuration configuration) {
        this.configuration = Objects.requireNonNull(configuration, "'configuration' cannot be null.");
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
     * @return The updated {@link CallingServerClientBuilder} object.
     */
    @Override
    public CallingServerClientBuilder httpLogOptions(HttpLogOptions logOptions) {
        this.httpLogOptions = Objects.requireNonNull(logOptions, "'logOptions' cannot be null.");
        return this;
    }

    /**
     * Sets the {@link CallingServerServiceVersion} that is used when making API requests.
     * <p>
     * If a service version is not provided, the service version that will be used will be the latest known service
     * version based on the version of the client library being used. If no service version is specified, updating to a
     * newer version of the client library will have the result of potentially moving to a newer service version.
     * <p>
     * Targeting a specific service version may also mean that the service will return an error for newer APIs.
     *
     * @param version {@link CallingServerServiceVersion} of the service to be used when making requests.
     * @return Updated CallingServerClientBuilder object
     */
    public CallingServerClientBuilder serviceVersion(CallingServerServiceVersion version) {
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
     * @return Updated {@link CallingServerClientBuilder} object.
     */
    @Override
    public CallingServerClientBuilder httpClient(HttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(httpClient, "'httpClient' cannot be null.");
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
     * @param customPolicy A {@link HttpPipelinePolicy pipeline policy}.
     * @return Updated {@link CallingServerClientBuilder} object.
     * @throws NullPointerException If {@code pipelinePolicy} is {@code null}.
     */
    @Override
    public CallingServerClientBuilder addPolicy(HttpPipelinePolicy customPolicy) {
        this.customPolicies.add(Objects.requireNonNull(customPolicy, "'customPolicy' cannot be null."));
        return this;
    }

    /**
     * Create asynchronous client applying HMACAuthenticationPolicy,
     * UserAgentPolicy, RetryPolicy, and CookiePolicy. Additional HttpPolicies
     * specified by additionalPolicies will be applied after them
     *
     * @param isDebug Input for debug mode
     * @return The updated {@link CallingServerClientBuilder} object.
     * @throws IllegalStateException If both {@link #retryOptions(RetryOptions)}
     * and {@link #retryPolicy(RetryPolicy)} have been set.
     */
    public CallingServerAsyncClient buildAsyncClient(boolean isDebug) {
        return new CallingServerAsyncClient(createServiceImpl(isDebug));
    }

    /**
     * Create synchronous client applying HmacAuthenticationPolicy, UserAgentPolicy,
     * RetryPolicy, and CookiePolicy. Additional HttpPolicies specified by
     * additionalPolicies will be applied after them.
     *
     * @param isDebug Input for debug mode
     * @return Updated {@link CallingServerClientBuilder} object.
     * @throws IllegalStateException If both {@link #retryOptions(RetryOptions)}
     * and {@link #retryPolicy(RetryPolicy)} have been set.
     */
    public CallingServerClient buildClient(boolean isDebug) {
        return new CallingServerClient(buildAsyncClient(isDebug));
    }

    private AzureCommunicationCallingServerServiceImpl createServiceImpl(boolean isDebug) {
        boolean isConnectionStringSet = connectionString != null && !connectionString.trim().isEmpty();
        boolean isEndpointSet = endpoint != null && !endpoint.trim().isEmpty();
        boolean isAzureKeyCredentialSet = azureKeyCredential != null;
        boolean isTokenCredentialSet = tokenCredential != null;

        if (!isDebug) {
            if (isConnectionStringSet && isEndpointSet) {
                throw logger.logExceptionAsError(new IllegalArgumentException(
                    "Both 'connectionString' and 'endpoint' are set. Just one may be used."));
            }

            if (isConnectionStringSet && isAzureKeyCredentialSet) {
                throw logger.logExceptionAsError(new IllegalArgumentException(
                    "Both 'connectionString' and 'keyCredential' are set. Just one may be used."));
            }

            if (isConnectionStringSet && isTokenCredentialSet) {
                throw logger.logExceptionAsError(new IllegalArgumentException(
                    "Both 'connectionString' and 'tokenCredential' are set. Just one may be used."));
            }

            if (isAzureKeyCredentialSet && isTokenCredentialSet) {
                throw logger.logExceptionAsError(new IllegalArgumentException(
                    "Both 'tokenCredential' and 'keyCredential' are set. Just one may be used."));
            }
        }

        if (isConnectionStringSet && !isDebug) {
            CommunicationConnectionString connectionStringObject = new CommunicationConnectionString(connectionString);
            String endpoint = connectionStringObject.getEndpoint();
            String accessKey = connectionStringObject.getAccessKey();
            endpoint(endpoint).credential(new AzureKeyCredential(accessKey));
        } else {
            if (isDebug && isEndpointSet) {
                CommunicationConnectionString connectionStringObject = new CommunicationConnectionString(connectionString);
                String accessKey = connectionStringObject.getAccessKey();
                credential(new AzureKeyCredential(accessKey));
            } else {
                CommunicationConnectionString connectionStringObject = new CommunicationConnectionString(connectionString);
                String endpoint = connectionStringObject.getEndpoint();
                String accessKey = connectionStringObject.getAccessKey();
                endpoint(endpoint).credential(new AzureKeyCredential(accessKey));
            }
        }

        Objects.requireNonNull(endpoint);
        if (isTokenCredentialSet) {
            try {
                hostName = getHostNameFromEndpoint();
            } catch (MalformedURLException e) {
                throw logger.logExceptionAsError(new RuntimeException(e.getMessage()));
            }
        }

        HttpPipeline builderPipeline = pipeline;
        if (pipeline == null) {
            builderPipeline = createHttpPipeline(httpClient);
        }

        AzureCommunicationCallingServerServiceImplBuilder clientBuilder = new AzureCommunicationCallingServerServiceImplBuilder();
        clientBuilder.endpoint(endpoint).pipeline(builderPipeline);

        return clientBuilder.buildClient();
    }

    /**
     * Allows the user to set a variety of client-related options, such as
     * user-agent string, headers, etc.
     *
     * @param clientOptions object to be applied.
     * @return Updated {@link CallingServerClientBuilder} object.
     */
    public CallingServerClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
        return this;
    }

    private List<HttpPipelinePolicy> createHttpPipelineAuthPolicies() {
        if (tokenCredential != null && azureKeyCredential != null) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "Both 'credential' and 'keyCredential' are set. Just one may be used."));
        }

        List<HttpPipelinePolicy> pipelinePolicies = new ArrayList<>();
        if (tokenCredential != null) {
            pipelinePolicies.add(new BearerTokenAuthenticationPolicy(tokenCredential,
                "https://communication.azure.com//.default"));
            Map<String, String> httpHeaders = new HashMap<>();
            httpHeaders.put("x-ms-host", hostName);
            pipelinePolicies.add(new AddHeadersPolicy(new HttpHeaders(httpHeaders)));
        } else if (azureKeyCredential != null) {
            pipelinePolicies.add(new HmacAuthenticationPolicy(azureKeyCredential));
        } else {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Missing credential information while building a client."));
        }

        return pipelinePolicies;
    }

    private HttpPipeline createHttpPipeline(HttpClient httpClient) {
        if (pipeline != null) {
            return pipeline;
        }

        List<HttpPipelinePolicy> policyList = new ArrayList<>();

        ClientOptions buildClientOptions = (clientOptions == null) ? new ClientOptions() : clientOptions;
        HttpLogOptions buildLogOptions = (httpLogOptions == null) ? new HttpLogOptions() : httpLogOptions;

        String applicationId = null;
        if (!CoreUtils.isNullOrEmpty(buildClientOptions.getApplicationId())) {
            applicationId = buildClientOptions.getApplicationId();
        } else if (!CoreUtils.isNullOrEmpty(buildLogOptions.getApplicationId())) {
            applicationId = buildLogOptions.getApplicationId();
        }

        // Add required policies
        String clientName = properties.getOrDefault(SDK_NAME, "UnknownName");
        String clientVersion = properties.getOrDefault(SDK_VERSION, "UnknownVersion");
        policyList.add(new UserAgentPolicy(applicationId, clientName, clientVersion, configuration));
        policyList.add(new RequestIdPolicy());
        policyList.add(ClientBuilderUtil.validateAndGetRetryPolicy(retryPolicy, retryOptions));
        policyList.add(new RedirectPolicy());
        policyList.addAll(createHttpPipelineAuthPolicies());
        policyList.add(new CookiePolicy());

        // Add additional policies
        if (!customPolicies.isEmpty()) {
            policyList.addAll(customPolicies);
        }

        // Add logging policy
        policyList.add(new HttpLoggingPolicy(getHttpLogOptions()));

        return new HttpPipelineBuilder().policies(policyList.toArray(new HttpPipelinePolicy[0])).httpClient(httpClient)
            .build();
    }

    private HttpLogOptions getHttpLogOptions() {
        if (httpLogOptions == null) {
            httpLogOptions = new HttpLogOptions();
        }

        return httpLogOptions;
    }

    private String getHostNameFromEndpoint() throws MalformedURLException {
        return new URL(endpoint).getHost();
    }
}
