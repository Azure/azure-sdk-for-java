// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.implementation.AzureCommunicationCallingServerServiceImpl;
import com.azure.communication.callingserver.implementation.AzureCommunicationCallingServerServiceImplBuilder;
import com.azure.communication.common.implementation.CommunicationConnectionString;
import com.azure.communication.common.implementation.HmacAuthenticationPolicy;
import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.CookiePolicy;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RedirectPolicy;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
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
 *
 * {@codesnippet com.azure.communication.callingserver.CallingServerClientBuilder.pipeline.instantiation}
 */
@ServiceClientBuilder(serviceClients = { CallingServerClient.class, CallingServerAsyncClient.class })
public final class CallingServerClientBuilder {
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

    /**
     * Set endpoint of the service.
     *
     * @param endpoint url of the service.
     * @return CallingServerClientBuilder object.
     */
    public CallingServerClientBuilder endpoint(String endpoint) {
        this.endpoint = Objects.requireNonNull(endpoint, "'endpoint' cannot be null.");
        return this;
    }

    /**
     * Set endpoint of the service.
     *
     * @param pipeline HttpPipeline to use, if a pipeline is not supplied, the
     * credential and httpClient fields must be set.
     * @return CallingServerClientBuilder object.
     */
    public CallingServerClientBuilder pipeline(HttpPipeline pipeline) {
        this.pipeline = Objects.requireNonNull(pipeline, "'pipeline' cannot be null.");
        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authenticate HTTP requests.
     *
     * @param tokenCredential {@link TokenCredential} used to authenticate HTTP
     * requests.
     * @return Updated {@link CallingServerClientBuilder} object.
     * @throws NullPointerException If {@code tokenCredential} is null.
     */
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
    CallingServerClientBuilder credential(AzureKeyCredential keyCredential) {
        this.azureKeyCredential = Objects.requireNonNull(keyCredential, "'keyCredential' cannot be null.");
        return this;
    }

    /**
     * Set connectionString to use.
     *
     * @param connectionString connection string to set.
     * @return Updated {@link CallingServerClientBuilder} object.
     */
    public CallingServerClientBuilder connectionString(String connectionString) {
        Objects.requireNonNull(connectionString, "'connectionString' cannot be null.");
        this.connectionString = connectionString;
        return this;
    }

    /**
     * Sets the retry policy to use (using the RetryPolicy type).
     *
     * @param retryPolicy object to be applied
     * @return Updated {@link CallingServerClientBuilder} object.
     */
    public CallingServerClientBuilder retryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = Objects.requireNonNull(retryPolicy, "'retryPolicy' cannot be null.");
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
    public CallingServerClientBuilder configuration(Configuration configuration) {
        this.configuration = Objects.requireNonNull(configuration, "'configuration' cannot be null.");
        return this;
    }

    /**
     * Sets the {@link HttpLogOptions} for service requests.
     *
     * @param logOptions The logging configuration to use when sending and receiving
     * HTTP requests/responses.
     * @return The updated {@link CallingServerClientBuilder} object.
     */
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
     * Set httpClient to use
     *
     * @param httpClient httpClient to use, overridden by the pipeline field.
     * @return Updated {@link CallingServerClientBuilder} object.
     */
    public CallingServerClientBuilder httpClient(HttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(httpClient, "'httpClient' cannot be null.");
        return this;
    }

    /**
     * Apply additional HttpPipelinePolicy
     *
     * @param customPolicy HttpPipelinePolicy object to be applied after
     *                     AzureKeyCredentialPolicy, UserAgentPolicy, RetryPolicy, and CookiePolicy.
     * @return Updated {@link CallingServerClientBuilder} object.
     */
    public CallingServerClientBuilder addPolicy(HttpPipelinePolicy customPolicy) {
        this.customPolicies.add(Objects.requireNonNull(customPolicy, "'customPolicy' cannot be null."));
        return this;
    }

    /**
     * Create asynchronous client applying HMACAuthenticationPolicy,
     * UserAgentPolicy, RetryPolicy, and CookiePolicy. Additional HttpPolicies
     * specified by additionalPolicies will be applied after them
     *
     * @return The updated {@link CallingServerClientBuilder} object.
     */
    public CallingServerAsyncClient buildAsyncClient() {
        return new CallingServerAsyncClient(createServiceImpl());
    }

    /**
     * Create synchronous client applying HmacAuthenticationPolicy, UserAgentPolicy,
     * RetryPolicy, and CookiePolicy. Additional HttpPolicies specified by
     * additionalPolicies will be applied after them.
     *
     * @return Updated {@link CallingServerClientBuilder} object.
     */
    public CallingServerClient buildClient() {
        return new CallingServerClient(buildAsyncClient());
    }

    private AzureCommunicationCallingServerServiceImpl createServiceImpl() {
        boolean isConnectionStringSet = connectionString != null && !connectionString.trim().isEmpty();
        boolean isEndpointSet = endpoint != null && !endpoint.trim().isEmpty();
        boolean isAzureKeyCredentialSet = azureKeyCredential != null;
        boolean isTokenCredentialSet = tokenCredential != null;

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

        if (isConnectionStringSet) {
            CommunicationConnectionString connectionStringObject = new CommunicationConnectionString(connectionString);
            String endpoint = connectionStringObject.getEndpoint();
            String accessKey = connectionStringObject.getAccessKey();
            endpoint(endpoint).credential(new AzureKeyCredential(accessKey));
        }

        Objects.requireNonNull(endpoint);
        if (isTokenCredentialSet) {
            try {
                hostName = getHostNameFromEndpoint();
            } catch (MalformedURLException e) {
                throw logger.logExceptionAsError(new RuntimeException(e.getMessage()));
            }
        }

        if (pipeline == null) {
            Objects.requireNonNull(httpClient);
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
        policyList.add((retryPolicy == null) ? new RetryPolicy() : retryPolicy);
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
