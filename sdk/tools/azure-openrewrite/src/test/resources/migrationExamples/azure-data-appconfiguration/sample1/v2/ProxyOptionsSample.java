// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.http.HttpClient;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.data.appconfiguration.models.ConfigurationSetting;

import java.net.InetSocketAddress;

/**
 * Sample for writing configuration proxy.
 */
public class ProxyOptionsSample {
    /**
     * Runs the sample algorithm and demonstrates how to add a custom policy to the HTTP pipeline.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        // The connection string value can be obtained by going to your App Configuration instance in the Azure portal
        // and navigating to "Access Keys" page under the "Settings" section.
        String connectionString = "endpoint={endpoint_value};id={id_value};secret={secret_value}";

        // Proxy options
        String hostName = "{your-host-name}";
        int port = 447; // your port number

        ProxyOptions proxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP,
            new InetSocketAddress(hostName, port));
        HttpClient httpClient = new NettyAsyncHttpClientBuilder()
            .proxy(proxyOptions)
            .build();

        ConfigurationClient client = new ConfigurationClientBuilder()
            .connectionString(connectionString)
            .httpClient(httpClient)
            .buildClient();

        // Name of the key to add to the configuration service.
        final String key = "hello";
        final String value = "world";

        ConfigurationSetting setting = client.setConfigurationSetting(key, null, value);
        System.out.printf("[SetConfigurationSetting] Key: %s, Value: %s", setting.getKey(), setting.getValue());

        setting = client.getConfigurationSetting(key, null, null);
        System.out.printf("[GetConfigurationSetting] Key: %s, Value: %s", setting.getKey(), setting.getValue());

        setting = client.deleteConfigurationSetting(key, null);
        System.out.printf("[DeleteConfigurationSetting] Key: %s, Value: %s", setting.getKey(), setting.getValue());

        System.out.println("End of synchronous sample.");
    }
}
