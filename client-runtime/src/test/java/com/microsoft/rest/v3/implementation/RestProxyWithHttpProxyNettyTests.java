package com.microsoft.rest.v3.implementation;

import com.microsoft.rest.v3.http.HttpClient;
import com.microsoft.rest.v3.http.ProxyOptions;
import com.microsoft.rest.v3.http.ProxyOptions.Type;
import org.junit.Ignore;

import java.net.InetSocketAddress;

@Ignore("Should only be run manually when a local proxy server (e.g. Fiddler) is running")
public class RestProxyWithHttpProxyNettyTests extends RestProxyTests {

    @Override
    protected HttpClient createHttpClient() {
        InetSocketAddress address = new InetSocketAddress("127.0.0.1", 8888);
        return HttpClient.createDefault().proxy(() -> new ProxyOptions(Type.HTTP, address));
    }
}
