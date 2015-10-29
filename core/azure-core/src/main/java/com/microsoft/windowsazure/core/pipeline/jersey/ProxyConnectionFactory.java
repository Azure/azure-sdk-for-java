/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.windowsazure.core.pipeline.jersey;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

import com.sun.jersey.client.urlconnection.HttpURLConnectionFactory;

/**
 * Extends <code>HttpURLConnectionFactory</code> so that an HTTP Proxy can be used when connecting to the
 * remote resource.
 *
 * @author Ed Hillmann
 */
public class ProxyConnectionFactory implements HttpURLConnectionFactory {

    private final String proxyHost;
    private final int proxyPort;
    
    public ProxyConnectionFactory(String proxyHost, int proxyPort) {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
    }
    
    @Override
    public HttpURLConnection getHttpURLConnection(URL url) throws IOException {
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
        return (HttpURLConnection) url.openConnection(proxy);
    }

}
