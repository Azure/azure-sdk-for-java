package com.microsoft.azure;

import com.microsoft.rest.http.ChannelHandlerConfig;
import com.microsoft.rest.http.HttpClient;
import com.microsoft.rest.http.RxNettyAdapter;
import com.microsoft.rest.policy.RequestPolicy;

import java.util.Collections;

public class AzureProxyToRestProxyWithRxNettyTests extends AzureProxyToRestProxyTests {
    @Override
    protected HttpClient createHttpClient() {
        return new RxNettyAdapter(Collections.<RequestPolicy.Factory>emptyList(), Collections.<ChannelHandlerConfig>emptyList());
    }
}
