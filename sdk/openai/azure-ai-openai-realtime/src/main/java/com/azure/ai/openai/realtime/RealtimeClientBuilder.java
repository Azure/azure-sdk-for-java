// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.realtime;

import com.azure.ai.openai.realtime.implementation.websocket.AuthenticationProvider;
import com.azure.ai.openai.realtime.implementation.websocket.ClientEndpointConfiguration;
import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.client.traits.ConfigurationTrait;
import com.azure.core.client.traits.EndpointTrait;
import com.azure.core.client.traits.KeyCredentialTrait;
import com.azure.core.client.traits.TokenCredentialTrait;
import com.azure.core.credential.KeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.UserAgentUtil;
import com.azure.core.util.logging.ClientLogger;

import java.util.Map;

/**
 * A builder to create a new instance of the RealtimeClient.
 */
@ServiceClientBuilder(serviceClients = { RealtimeAsyncClient.class, RealtimeClient.class })
public final class RealtimeClientBuilder
    implements ConfigurationTrait<RealtimeClientBuilder>, TokenCredentialTrait<RealtimeClientBuilder>,
    KeyCredentialTrait<RealtimeClientBuilder>, EndpointTrait<RealtimeClientBuilder> {

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

    private OpenAIRealtimeServiceVersion serviceVersion = OpenAIRealtimeServiceVersion.V2024_10_01_PREVIEW;

    /**
     * Creates a new instance of the {@link RealtimeClientBuilder}.
     */
    public RealtimeClientBuilder() {
        this.properties = CoreUtils.getProperties(PROPERTIES);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RealtimeClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the {@link ClientOptions} used to configure the service client.
     *
     * @param clientOptions the {@link ClientOptions} used to configure the service client.
     * @return the {@link RealtimeClientBuilder}.
     */
    public RealtimeClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RealtimeClientBuilder credential(TokenCredential tokenCredential) {
        this.tokenCredential = tokenCredential;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RealtimeClientBuilder credential(KeyCredential keyCredential) {
        this.keyCredential = keyCredential;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RealtimeClientBuilder endpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    /**
     * Sets the deployment name in the case of Azure or the model name in case of OpenAI.
     *
     * @param deploymentOrModelName the deployment or model name.
     * @return the {@link RealtimeClientBuilder}.
     */
    public RealtimeClientBuilder deploymentOrModelName(String deploymentOrModelName) {
        this.deploymentOrModelName = deploymentOrModelName;
        return this;
    }

    /**
     * Sets Service version.
     *
     * @param serviceVersion the serviceVersion value.
     * @return the OpenAIClientBuilder.
     */
    public RealtimeClientBuilder serviceVersion(OpenAIRealtimeServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;
        return this;
    }

    /**
     * Builds a new instance of the {@link RealtimeAsyncClient}.
     *
     * @return a new instance of the {@link RealtimeAsyncClient}.
     */
    public RealtimeAsyncClient buildAsyncClient() {
        String applicationId = CoreUtils.getApplicationId(clientOptions, null);
        return new RealtimeAsyncClient(getClientEndpointConfiguration(), applicationId, getAuthenticationProvider());
    }

    /**
     * Builds a new instance of the {@link RealtimeClient}.
     *
     * @return a new instance of the {@link RealtimeClient}.
     */
    public RealtimeClient buildClient() {
        return new RealtimeClient(buildAsyncClient());
    }

    /**
     * Creates an instance of the {@link AuthenticationProvider} based on the provided credential type.
     *
     * @return the {@link AuthenticationProvider}.
     */
    private AuthenticationProvider getAuthenticationProvider() {
        if (useNonAzureOpenAIService()) {
            return new AuthenticationProvider(keyCredential, false);
        } else if (keyCredential != null) {
            return new AuthenticationProvider(keyCredential, true);
        } else if (tokenCredential != null) {
            return new AuthenticationProvider(tokenCredential, DEFAULT_SCOPES);
        } else {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("Missing credential information while building a client."));
        }
    }

    /**
     * Creates the {@link ClientEndpointConfiguration}.
     *
     * @return the {@link ClientEndpointConfiguration}.
     */
    private ClientEndpointConfiguration getClientEndpointConfiguration() {
        // user-agent
        final String clientName = properties.getOrDefault(SDK_NAME, "UnknownName");
        final String clientVersion = properties.getOrDefault(SDK_VERSION, "UnknownVersion");
        String applicationId = CoreUtils.getApplicationId(clientOptions, null);
        String userAgent = UserAgentUtil.toUserAgentString(applicationId, clientName, clientVersion,
            configuration == null ? Configuration.getGlobalConfiguration() : configuration);

        return useNonAzureOpenAIService()
            ? ClientEndpointConfiguration.createNonAzureClientEndpointConfiguration(OPENAI_BASE_URL, userAgent,
                deploymentOrModelName)
            : ClientEndpointConfiguration.createAzureClientEndpointConfiguration(endpoint, userAgent,
                deploymentOrModelName, this.serviceVersion);
    }

    /**
     * OpenAI service can be used by either not setting the endpoint or by setting the endpoint to start with
     * "wss://api.openai.com/"
     */
    private boolean useNonAzureOpenAIService() {
        return endpoint == null || endpoint.startsWith(OPENAI_BASE_URL);
    }
}
