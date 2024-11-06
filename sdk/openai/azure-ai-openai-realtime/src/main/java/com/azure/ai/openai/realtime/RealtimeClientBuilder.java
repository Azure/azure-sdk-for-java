// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.realtime;

import com.azure.ai.openai.realtime.implementation.websocket.AuthenticationProvider;
import com.azure.ai.openai.realtime.implementation.websocket.ClientEndpointConfiguration;
import com.azure.ai.openai.realtime.implementation.websocket.WebSocketClient;
import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.client.traits.ConfigurationTrait;
import com.azure.core.credential.KeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.ExponentialBackoff;
import com.azure.core.http.policy.FixedDelay;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.RetryStrategy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.UserAgentUtil;
import com.azure.core.util.logging.ClientLogger;

import java.util.Map;

@ServiceClientBuilder(serviceClients = { RealtimeAsyncClient.class, RealtimeClient.class })
public class RealtimeClientBuilder implements ConfigurationTrait<RealtimeClientBuilder> {

    private static final String[] DEFAULT_SCOPES = new String[] { "https://cognitiveservices.azure.com/.default" };

    private static final String OPENAI_BASE_URL = "wss://api.openai.com";

    private static final ClientLogger LOGGER = new ClientLogger(RealtimeClientBuilder.class);

    private static final String PROPERTIES = "azure-ai-openai-realtime.properties";
    private static final String SDK_NAME = "name";
    private static final String SDK_VERSION = "version";

    private Configuration configuration;
    private ClientOptions clientOptions;
    private final Map<String, String> properties;

    private String endpoint;
    private String deploymentOrModelName;
    private TokenCredential tokenCredential;
    private KeyCredential keyCredential;

    private OpenAIServiceVersion serviceVersion = OpenAIServiceVersion.V2024_10_01_PREVIEW;

    private RetryOptions retryOptions;
    private WebSocketClient webSocketClient;

    @Override
    public RealtimeClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    public RealtimeClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
        return this;
    }

    public RealtimeClientBuilder retryOptions(RetryOptions retryOptions) {
        this.retryOptions = retryOptions;
        return this;
    }

    public RealtimeClientBuilder credential(TokenCredential tokenCredential) {
        this.tokenCredential = tokenCredential;
        return this;
    }

    public RealtimeClientBuilder credential(KeyCredential keyCredential) {
        this.keyCredential = keyCredential;
        return this;
    }

    public RealtimeClientBuilder endpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public RealtimeClientBuilder deploymentOrModelName(String deploymentOrModelName) {
        this.deploymentOrModelName = deploymentOrModelName;
        return this;
    }

    public RealtimeClientBuilder serviceVersion(OpenAIServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;
        return this;
    }

    public RealtimeAsyncClient buildAsyncClient() {
        String applicationId = CoreUtils.getApplicationId(clientOptions, null);
        return new RealtimeAsyncClient(webSocketClient, getClientEndpointConfiguration(),
                applicationId, getRetryStrategy(), getAuthenticationProvider());
    }

    public RealtimeClient buildClient() {
        return new RealtimeClient(buildAsyncClient());
    }

    private AuthenticationProvider getAuthenticationProvider() {
        if(useNonAzureOpenAIService()) {
            return new AuthenticationProvider(keyCredential, false);
        } else if (keyCredential != null) {
            return new AuthenticationProvider(keyCredential, true);
        } else if (tokenCredential != null) {
            return new AuthenticationProvider(tokenCredential, DEFAULT_SCOPES);
        } else {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Missing credential information while building a client."));
        }
    }

    private RetryStrategy getRetryStrategy() {
        RetryStrategy retryStrategy;
        if (retryOptions != null) {
            if (retryOptions.getExponentialBackoffOptions() != null) {
                retryStrategy = new ExponentialBackoff(retryOptions.getExponentialBackoffOptions());
            } else if (retryOptions.getFixedDelayOptions() != null) {
                retryStrategy = new FixedDelay(retryOptions.getFixedDelayOptions());
            } else {
                throw LOGGER.logExceptionAsError(
                        new IllegalArgumentException("'retryOptions' didn't define any retry strategy options"));
            }
        } else {
            // default retry strategy be ExponentialBackoff
            retryStrategy = new ExponentialBackoff();
        }
        return retryStrategy;
    }

    private ClientEndpointConfiguration getClientEndpointConfiguration() {
        // user-agent
        final String clientName = properties.getOrDefault(SDK_NAME, "UnknownName");
        final String clientVersion = properties.getOrDefault(SDK_VERSION, "UnknownVersion");
        String applicationId = CoreUtils.getApplicationId(clientOptions, null);
        String userAgent = UserAgentUtil.toUserAgentString(applicationId, clientName, clientVersion,
                configuration == null ? Configuration.getGlobalConfiguration() : configuration);

        return useNonAzureOpenAIService() ?
            ClientEndpointConfiguration.createNonAzureClientEndpointConfiguration(OPENAI_BASE_URL, userAgent, deploymentOrModelName) :
            ClientEndpointConfiguration.createAzureClientEndpointConfiguration(endpoint, userAgent, deploymentOrModelName, this.serviceVersion);
    }

    /**
     * OpenAI service can be used by either not setting the endpoint or by setting the endpoint to start with
     * "wss://api.openai.com/"
     */
    private boolean useNonAzureOpenAIService() {
        return endpoint == null || endpoint.startsWith(OPENAI_BASE_URL);
    }

    public RealtimeClientBuilder() {
        this.properties = CoreUtils.getProperties(PROPERTIES);
    }
}
