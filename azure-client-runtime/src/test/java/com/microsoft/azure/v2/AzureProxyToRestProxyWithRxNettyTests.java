package com.microsoft.azure.v2;

import com.microsoft.rest.v2.http.ChannelHandlerConfig;
import com.microsoft.rest.v2.http.HttpClient;
import com.microsoft.rest.v2.http.RxNettyAdapter;
import com.microsoft.rest.v2.policy.RequestPolicy;

import java.util.Collections;

public class AzureProxyToRestProxyWithRxNettyTests extends AzureProxyToRestProxyTests {
    @Override
    protected HttpClient createHttpClient() {
        return new RxNettyAdapter(Collections.<RequestPolicy.Factory>emptyList(), Collections.<ChannelHandlerConfig>emptyList());
    }
}
