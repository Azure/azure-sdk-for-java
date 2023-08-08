// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.client.traits.ConfigurationTrait;
import com.azure.core.http.policy.ExponentialBackoff;
import com.azure.core.http.policy.FixedDelay;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.RetryStrategy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.HttpClientOptions;
import com.azure.core.util.UserAgentUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.webpubsub.client.implementation.websocket.WebSocketClient;
import com.azure.messaging.webpubsub.client.models.WebPubSubClientCredential;
import com.azure.messaging.webpubsub.client.models.WebPubSubJsonReliableProtocol;
import com.azure.messaging.webpubsub.client.models.WebPubSubProtocol;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * The builder of WebPubSub client.
 * <p>
 * The following information must be provided on this builder:
 * Either the credential through {@link #credential(WebPubSubClientCredential)},
 * or the client access URL through {@link #clientAccessUrl(String)}.
 */
@ServiceClientBuilder(serviceClients = {WebPubSubAsyncClient.class, WebPubSubClient.class})
public class WebPubSubClientBuilder implements ConfigurationTrait<WebPubSubClientBuilder> {

    private static final ClientLogger LOGGER = new ClientLogger(WebPubSubClientBuilder.class);

    private static final String PROPERTIES = "azure-messaging-webpubsub-client.properties";
    private static final String SDK_NAME = "name";
    private static final String SDK_VERSION = "version";

    private WebPubSubClientCredential credential;
    private String clientAccessUrl;

    // default protocol be WebPubSubJsonReliableProtocol
    private WebPubSubProtocol webPubSubProtocol = new WebPubSubJsonReliableProtocol();

    private ClientOptions clientOptions;
    private Configuration configuration;
    private final Map<String, String> properties;

    private RetryOptions retryOptions = null;
    private boolean autoReconnect = true;
    private boolean autoRestoreGroup = true;

    WebSocketClient webSocketClient;

    /**
     * Creates a new instance of WebPubSubClientBuilder.
     */
    public WebPubSubClientBuilder() {
        properties = CoreUtils.getProperties(PROPERTIES);
    }

    /**
     * Sets the credential as the provider for client access URL.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed readme-sample-createClientFromCredential -->
     * <pre>
     * &#47;&#47; WebPubSubServiceAsyncClient is from com.azure:azure-messaging-webpubsub
     * &#47;&#47; create WebPubSub service client
     * WebPubSubServiceAsyncClient serverClient = new WebPubSubServiceClientBuilder&#40;&#41;
     *     .connectionString&#40;&quot;&lt;connection-string&gt;&quot;&#41;
     *     .hub&#40;&quot;&lt;hub&gt;&gt;&quot;&#41;
     *     .buildAsyncClient&#40;&#41;;
     *
     * &#47;&#47; wrap WebPubSubServiceAsyncClient.getClientAccessToken as WebPubSubClientCredential
     * WebPubSubClientCredential clientCredential = new WebPubSubClientCredential&#40;Mono.defer&#40;&#40;&#41; -&gt;
     *     serverClient.getClientAccessToken&#40;new GetClientAccessTokenOptions&#40;&#41;
     *             .setUserId&#40;&quot;&lt;user-name&gt;&quot;&#41;
     *             .addRole&#40;&quot;webpubsub.joinLeaveGroup&quot;&#41;
     *             .addRole&#40;&quot;webpubsub.sendToGroup&quot;&#41;&#41;
     *         .map&#40;WebPubSubClientAccessToken::getUrl&#41;&#41;&#41;;
     *
     * &#47;&#47; create WebPubSub client
     * WebPubSubClient client = new WebPubSubClientBuilder&#40;&#41;
     *     .credential&#40;clientCredential&#41;
     *     .buildClient&#40;&#41;;
     * </pre>
     * <!-- end readme-sample-createClientFromCredential -->
     *
     * @param credential the credential as the provider for client access URL.
     * @return itself.
     */
    public WebPubSubClientBuilder credential(WebPubSubClientCredential credential) {
        this.credential = credential;
        return this;
    }

    /**
     * Sets the credential as the provider for client access URL.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed readme-sample-createClientFromUrl -->
     * <pre>
     * WebPubSubClient client = new WebPubSubClientBuilder&#40;&#41;
     *     .clientAccessUrl&#40;&quot;&lt;client-access-url&gt;&quot;&#41;
     *     .buildClient&#40;&#41;;
     * </pre>
     * <!-- end readme-sample-createClientFromUrl -->
     *
     * @param clientAccessUrl the client access URL.
     * @return itself.
     */
    public WebPubSubClientBuilder clientAccessUrl(String clientAccessUrl) {
        this.clientAccessUrl = clientAccessUrl;
        return this;
    }

    /**
     * Sets the protocol.
     *
     * @param webPubSubProtocol the protocol.
     * @return itself.
     */
    public WebPubSubClientBuilder protocol(WebPubSubProtocol webPubSubProtocol) {
        this.webPubSubProtocol = webPubSubProtocol;
        return this;
    }

    /**
     * Sets the retry options when sending messages.
     *
     * @param retryOptions the retry options.
     * @return itself.
     */
    public WebPubSubClientBuilder retryOptions(RetryOptions retryOptions) {
        this.retryOptions = retryOptions;
        return this;
    }

    /**
     * Allows for setting common properties such as application ID, headers, proxy configuration, etc.
     *
     * @param clientOptions A configured instance of {@link HttpClientOptions}.
     * @return Returns the same concrete type with the appropriate properties updated, to allow for fluent chaining of
     *      operations.
     */
    public WebPubSubClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebPubSubClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets whether automatically reconnect after disconnect.
     *
     * @param autoReconnect whether automatically reconnect after disconnect.
     * @return itself.
     */
    public WebPubSubClientBuilder autoReconnect(boolean autoReconnect) {
        this.autoReconnect = autoReconnect;
        return this;
    }

    /**
     * Sets whether automatically restore joined groups after reconnect.
     *
     * @param autoRestoreGroup whether automatically restore joined groups after reconnect.
     * @return itself.
     */
    public WebPubSubClientBuilder autoRestoreGroup(boolean autoRestoreGroup) {
        this.autoRestoreGroup = autoRestoreGroup;
        return this;
    }

    /**
     * Builds the client.
     *
     * @return the client.
     */
    public WebPubSubClient buildClient() {
        return new WebPubSubClient(this.buildAsyncClient());
    }

    /**
     * Builds the asynchronous client.
     *
     * @return the asynchronous client.
     */
    WebPubSubAsyncClient buildAsyncClient() {
        // retryOptions
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

        // credential
        Mono<String> clientAccessUrlProvider;
        if (credential != null && clientAccessUrl != null) {
            throw LOGGER.logExceptionAsError(
                new IllegalStateException("Both credential and clientAccessUrl have been set. "
                    + "Set null to one of them to clear that option."));
        } else if (credential != null) {
            clientAccessUrlProvider = credential.getClientAccessUrl();
        } else if (clientAccessUrl != null) {
            clientAccessUrlProvider = Mono.just(clientAccessUrl);
        } else {
            throw LOGGER.logExceptionAsError(
                new IllegalStateException("Credentials have not been set. "
                    + "They can be set using: clientAccessUrl(String), credential(WebPubSubClientCredential)"));
        }

        // user-agent
        final String clientName = properties.getOrDefault(SDK_NAME, "UnknownName");
        final String clientVersion = properties.getOrDefault(SDK_VERSION, "UnknownVersion");
        String applicationId = CoreUtils.getApplicationId(clientOptions, null);
        String userAgent = UserAgentUtil.toUserAgentString(applicationId, clientName, clientVersion,
            configuration == null ? Configuration.getGlobalConfiguration() : configuration);

        return new WebPubSubAsyncClient(
            webSocketClient, clientAccessUrlProvider, webPubSubProtocol,
            applicationId, userAgent,
            retryStrategy, autoReconnect, autoRestoreGroup);
    }
}
