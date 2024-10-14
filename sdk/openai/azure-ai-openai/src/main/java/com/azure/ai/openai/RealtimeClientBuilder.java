package com.azure.ai.openai;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.client.traits.ConfigurationTrait;
import com.azure.core.util.Configuration;

@ServiceClientBuilder(serviceClients = { RealtimeAsyncClient.class, RealtimeClient.class })
public class RealtimeClientBuilder implements ConfigurationTrait<RealtimeClientBuilder> {

    private Configuration configuration;

    @Override
    public RealtimeClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }
}
