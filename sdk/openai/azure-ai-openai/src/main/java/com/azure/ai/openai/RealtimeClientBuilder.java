package com.azure.ai.openai;

import com.azure.ai.openai.implementation.websocket.WebSocketClient;
import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.client.traits.ConfigurationTrait;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;

@ServiceClientBuilder(serviceClients = { RealtimeAsyncClient.class, RealtimeClient.class })
public class RealtimeClientBuilder implements ConfigurationTrait<RealtimeClientBuilder> {

    private static final ClientLogger LOGGER = new ClientLogger(RealtimeClientBuilder.class);

    // TODO jpalvarezl: figure out these
    private static final String SDK_NAME = "AzureOpenAIRealTime";
    private static final String SDK_VERSION = "0.0.1-beta1";

    WebSocketClient webSocketClient;

    private Configuration configuration;

    @Override
    public RealtimeClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

}
