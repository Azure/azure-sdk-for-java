// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.http.HttpClient;
import com.azure.core.http.MatchConditions;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.util.Configuration;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import javax.net.ssl.SSLException;
import javax.net.ssl.X509TrustManager;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConditionalRequestForSettingsPagination {
    // The connection string value can be obtained by going to your App Configuration instance in the Azure portal
    // and navigating to the "Access Keys" page under the "Settings" section.
    public static String connectionString = Configuration.getGlobalConfiguration().get("AZURE_APPCONFIG_CONNECTION_STRING");

    // Instantiate a client that will be used to call the service.
    public static final ConfigurationClient client = new ConfigurationClientBuilder()
        .connectionString(connectionString)
        .buildClient();

    public static void main(String[] args) throws MalformedURLException {
        List<PagedResponse<ConfigurationSetting>> pagedResponses = client.listConfigurationSettings(null)
            .streamByPage()
            .collect(Collectors.toList());

        Map<URL, MatchConditions> urlMatchConditionsMap = collectUrlMatchConditions(pagedResponses);

        urlMatchConditionsMap.forEach((url, matchConditions) -> {
            System.out.println("URL = " + url.toString());
            System.out.println("eTag = " + matchConditions.getIfNoneMatch());
            PagedIterable<ConfigurationSetting> settings = client.listConfigurationSettings(
                    new SettingSelector().setPageLink(url).setMatchConditions(matchConditions));
            for (ConfigurationSetting setting : settings) {
                System.out.println(setting.getKey());
            }
        });
    }

    private static Map<URL, MatchConditions> collectUrlMatchConditions(List<PagedResponse<ConfigurationSetting>> pagedResponses) throws MalformedURLException {
        Map<URL, MatchConditions> urlMatchConditionsMap = new HashMap<>();

        for (int i = 0; i < pagedResponses.size(); i++) {
            final PagedResponse<ConfigurationSetting> pagedResponse = pagedResponses.get(i);
            final String pagedETagInResponseHeader = pagedResponse.getHeaders().getValue("Etag");
            final String continuationToken = pagedResponse.getContinuationToken();
            if (continuationToken == null) {
                continue;
            }
            urlMatchConditionsMap.put(new URL(client.getEndpoint() + pagedResponse.getContinuationToken()),
                    new MatchConditions().setIfNoneMatch(pagedETagInResponseHeader));
        }
        return urlMatchConditionsMap;
    }
}

