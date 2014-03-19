/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.microsoft.windowsazure.core.pipeline.apache;

import java.util.Map;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;

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

        return httpClientBuilder;
    }
}
