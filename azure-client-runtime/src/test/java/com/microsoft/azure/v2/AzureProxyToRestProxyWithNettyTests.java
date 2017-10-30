package com.microsoft.azure.v2;

import com.microsoft.rest.v2.http.HttpClient;
import com.microsoft.rest.v2.http.HttpClient.Configuration;
import com.microsoft.rest.v2.http.NettyClient;
import com.microsoft.rest.v2.policy.RequestPolicy;

import java.util.Collections;

public class AzureProxyToRestProxyWithNettyTests extends AzureProxyToRestProxyTests {
    private final NettyClient.Factory nettyClientFactory = new NettyClient.Factory();

    @Override
    protected HttpClient createHttpClient() {
        return nettyClientFactory.create(new Configuration(Collections.<RequestPolicy.Factory>emptyList(), null));
    }
}
