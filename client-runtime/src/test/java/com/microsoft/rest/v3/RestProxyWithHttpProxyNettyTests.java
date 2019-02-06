package com.microsoft.rest.v3;

import com.microsoft.rest.v3.http.HttpClient;
import com.microsoft.rest.v3.http.HttpClientConfiguration;
import com.microsoft.rest.v3.http.NettyClient;
import com.microsoft.rest.v3.http.ProxyOptions;
import com.microsoft.rest.v3.http.ProxyOptions.Type;
import org.junit.Ignore;

import java.net.InetSocketAddress;
import java.net.Proxy;

@Ignore("Should only be run manually when a local proxy server (e.g. Fiddler) is running")
public class RestProxyWithHttpProxyNettyTests extends RestProxyTests {
    private static NettyClient.Factory nettyClientFactory = new NettyClient.Factory();

    @Override
    protected HttpClient createHttpClient() {
        InetSocketAddress address = new InetSocketAddress("127.0.0.1", 8888);
        ProxyOptions proxy = new ProxyOptions(Type.HTTP, address);
        return nettyClientFactory.create(new HttpClientConfiguration().withProxy(proxy));
    }
}
