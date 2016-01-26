/**
 * 
 * Copyright (c) Microsoft and contributors.  All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package com.microsoft.windowsazure.core.pipeline.apache;

import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.core.pipeline.filter.ServiceRequestFilter;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;

import java.util.Map;

public class ApacheConfigSettings {
    private final String profile;
    private final Map<String, Object> properties;

    public ApacheConfigSettings(String profile, Map<String, Object> properties) {
        this.profile = profile;
        this.properties = properties;
    }

    /**
     * Update the given {@link HttpClientBuilder} object with the appropriate
     * settings from configuration.
     * 
     * @param httpClientBuilder
     *            The object to update.
     * @return The updates httpClientBuilder
     */
    public HttpClientBuilder applyConfig(HttpClientBuilder httpClientBuilder) {
        if (properties
                .containsKey(profile
                        + ApacheConfigurationProperties.PROPERTY_SSL_CONNECTION_SOCKET_FACTORY)) {
            httpClientBuilder
                    .setSSLSocketFactory((LayeredConnectionSocketFactory) properties
                            .get(profile
                                    + ApacheConfigurationProperties.PROPERTY_SSL_CONNECTION_SOCKET_FACTORY));
        }

        if (properties.containsKey(profile
                + ApacheConfigurationProperties.PROPERTY_CONNECTION_MANAGER)) {
            httpClientBuilder
                    .setConnectionManager((HttpClientConnectionManager) properties
                            .get(profile
                                    + ApacheConfigurationProperties.PROPERTY_CONNECTION_MANAGER));
        }

        if (properties.containsKey(profile
                + ApacheConfigurationProperties.PROPERTY_PROXY_URI)) {
            httpClientBuilder
                    .setProxy(new HttpHost((String) properties.get(profile
                            + ApacheConfigurationProperties.PROPERTY_PROXY_URI)));
        }

        if (properties.containsKey(profile
                + ApacheConfigurationProperties.PROPERTY_RETRY_HANDLER)) {
            httpClientBuilder
                    .setRetryHandler((HttpRequestRetryHandler) properties
                            .get(profile
                                    + ApacheConfigurationProperties.PROPERTY_RETRY_HANDLER));
        }

        if (properties.containsKey(profile
                + ApacheConfigurationProperties.PROPERTY_HTTP_CLIENT_BUILDER)) {
            return (HttpClientBuilder) properties
                    .get(profile
                            + ApacheConfigurationProperties.PROPERTY_HTTP_CLIENT_BUILDER);
        }
        
        if (properties.containsKey(profile
                + ApacheConfigurationProperties.PROPERTY_REDIRECT_STRATEGY)) {
            httpClientBuilder
            .setRedirectStrategy((DefaultRedirectStrategy) properties
                    .get(profile
                            + ApacheConfigurationProperties.PROPERTY_REDIRECT_STRATEGY));
            
            // Currently the redirect strategy, due to what seems to be a bug,
            // fails for post requests since it tries do double
            // add the content-length header. This workaround makes sure this header is always
            // removed before it is actually processed by apache
            httpClientBuilder.addInterceptorFirst(new HttpHeaderRemovalFilter());
        }
        
        if (properties.containsKey("AuthFilter"))
        {
            @SuppressWarnings("unchecked")
            ServiceRequestFilter filter = (ServiceRequestFilter) properties.get("AuthFilter");
            httpClientBuilder.addInterceptorFirst(new FilterInterceptor(filter));
        }

        if (properties.containsKey(profile + Configuration.PROPERTY_HTTP_PROXY_HOST) &&
                properties.containsKey(profile + Configuration.PROPERTY_HTTP_PROXY_PORT)) {
            String proxyHost = (String) properties.get(profile + Configuration.PROPERTY_HTTP_PROXY_HOST);
            int proxyPort = Integer.parseInt((String) properties.get(profile + Configuration.PROPERTY_HTTP_PROXY_PORT));
            HttpHost proxy;
            if (properties.containsKey(profile + Configuration.PROPERTY_HTTP_PROXY_SCHEME)) {
                proxy = new HttpHost(proxyHost, proxyPort, (String) properties.get(profile + Configuration.PROPERTY_HTTP_PROXY_SCHEME));
            } else {
                proxy = new HttpHost(proxyHost, proxyPort);
            }
            httpClientBuilder.setProxy(proxy);
        }

        return httpClientBuilder;
    }
}
