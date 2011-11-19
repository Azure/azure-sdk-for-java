package com.microsoft.windowsazure.services.queue;

import java.util.Map;

import com.microsoft.windowsazure.common.Builder;
import com.microsoft.windowsazure.services.blob.implementation.HttpURLConnectionClient;
import com.microsoft.windowsazure.services.queue.implementation.QueueExceptionProcessor;
import com.microsoft.windowsazure.services.queue.implementation.QueueRestProxy;
import com.microsoft.windowsazure.services.queue.implementation.SharedKeyFilter;
import com.microsoft.windowsazure.services.queue.implementation.SharedKeyLiteFilter;
import com.sun.jersey.api.client.config.ClientConfig;

public class Exports implements Builder.Exports {
    public void register(Builder.Registry registry) {
        registry.add(QueueContract.class, QueueExceptionProcessor.class);
        registry.add(QueueService.class);
        registry.add(QueueExceptionProcessor.class);
        registry.add(QueueRestProxy.class);
        registry.add(SharedKeyLiteFilter.class);
        registry.add(SharedKeyFilter.class);

        registry.add(new Builder.Factory<HttpURLConnectionClient>() {
            public HttpURLConnectionClient create(String profile, Builder builder, Map<String, Object> properties) {
                ClientConfig clientConfig = (ClientConfig) properties.get("ClientConfig");
                HttpURLConnectionClient client = HttpURLConnectionClient.create(clientConfig);
                //client.addFilter(new LoggingFilter());
                return client;
            }
        });
    }
}
