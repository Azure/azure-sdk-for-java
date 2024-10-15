package com.azure.ai.openai;

import com.azure.ai.openai.implementation.RealtimesImpl;
import com.azure.ai.openai.implementation.websocket.WebSocketClient;
import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.client.traits.ConfigurationTrait;
import com.azure.core.credential.KeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.ExponentialBackoff;
import com.azure.core.http.policy.FixedDelay;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.RetryStrategy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;

import java.util.Map;

@ServiceClientBuilder(serviceClients = { RealtimeAsyncClient.class, RealtimeClient.class })
public class RealtimeClientBuilder implements ConfigurationTrait<RealtimeClientBuilder> {

    private static final String OPENAI_BASE_URL = "https://api.openai.com";

    private static final ClientLogger LOGGER = new ClientLogger(RealtimeClientBuilder.class);

    private static final Map<String, String> PROPERTIES = CoreUtils.getProperties("azure-ai-openai.properties");

    // TODO jpalvarezl: figure out this. If we ship as part of Inference, we may want to distinguish realtime specific traffic
    private static final String SDK_NAME = "name";
    private static final String SDK_VERSION = "version";

    private Configuration configuration;
    private ClientOptions clientOptions;
    private String endpoint;

    private RetryOptions retryOptions;

    WebSocketClient webSocketClient;

    private TokenCredential tokenCredential;
    private KeyCredential keyCredential;

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

    private RetryStrategy createRetryOptions() {
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

    public RealtimeClientBuilder credentials(TokenCredential tokenCredential) {
        this.tokenCredential = tokenCredential;
        return this;
    }

    public RealtimeClientBuilder credentials(KeyCredential keyCredential) {
        this.keyCredential = keyCredential;
        return this;
    }

    public RealtimeClientBuilder endpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }


    // TODO jpalvarezl: remove
    private HttpPipeline createHttpPipeline() {
        return null;
    }

    public RealtimeAsyncClient buildAsync() {
        String endpoint = this.endpoint == null ? OPENAI_BASE_URL : this.endpoint;

        return new RealtimeAsyncClient(new RealtimesImpl(createHttpPipeline(), JacksonAdapter.createDefaultSerializerAdapter(), endpoint));
    }

    public RealtimeClient build() {
        String endpoint = this.endpoint == null ? OPENAI_BASE_URL : this.endpoint;

        return new RealtimeClient(new RealtimesImpl(createHttpPipeline(), JacksonAdapter.createDefaultSerializerAdapter(), endpoint));
    }

}
