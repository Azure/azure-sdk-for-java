package com.microsoft.windowsazure.services.blob;

import java.util.Map;

import com.microsoft.windowsazure.common.Builder;
import com.microsoft.windowsazure.services.blob.implementation.BlobExceptionProcessor;
import com.microsoft.windowsazure.services.blob.implementation.BlobRestProxy;
import com.microsoft.windowsazure.services.blob.implementation.HttpURLConnectionClient;
import com.microsoft.windowsazure.services.blob.implementation.SharedKeyFilter;
import com.microsoft.windowsazure.services.blob.implementation.SharedKeyLiteFilter;
import com.sun.jersey.api.client.config.ClientConfig;

public class Exports implements Builder.Exports {
    public void register(Builder.Registry registry) {
        registry.add(BlobContract.class, BlobExceptionProcessor.class);
        registry.add(BlobService.class);
        registry.add(BlobExceptionProcessor.class);
        registry.add(BlobRestProxy.class);
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
