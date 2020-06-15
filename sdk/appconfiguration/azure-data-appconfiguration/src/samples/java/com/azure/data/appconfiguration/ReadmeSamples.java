// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.appconfiguration;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.AddHeadersFromContextPolicy;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 *
 * Code samples for the README.md
 */
public class ReadmeSamples {
    private String endpoint = "endpoint";
    private String connectionString = "connection string";
    private String urlKey = "url key";
    private String urlLabel = "url label";
    private String periodicUpdateLabel = "periodic update label";
    private ConfigurationClient configurationClient = new ConfigurationClientBuilder().buildClient();

    public void createHttpClient() {
        HttpClient client = new NettyAsyncHttpClientBuilder()
            .port(8080)
            .wiretap(true)
            .build();
    }

    public void createClient() {
        ConfigurationClient configurationClient = new ConfigurationClientBuilder()
            .connectionString(connectionString)
            .buildClient();
    }

    public void createAsyncClient() {
        ConfigurationAsyncClient configurationClient = new ConfigurationClientBuilder()
            .connectionString(connectionString)
            .buildAsyncClient();
    }

    public void aadAuthentication() {
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
        ConfigurationClient configurationClient = new ConfigurationClientBuilder()
            .credential(credential)
            .endpoint(endpoint)
            .buildClient();
    }

    public void sqlExample() {
        ConfigurationClient configurationClient = new ConfigurationClientBuilder()
            .connectionString(connectionString)
            .buildClient();

        // urlLabel is optional
        String url = configurationClient.getConfigurationSetting(urlKey, urlLabel).getValue();
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException ex) {
            System.out.printf("Failed to get connection using url %s", url);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    System.out.printf("Failed to close connection, url %s", url);
                }
            }
        }
    }

    public void listConfigurationsExample() {
        ConfigurationAsyncClient configurationClient = new ConfigurationClientBuilder()
            .connectionString(connectionString)
            .buildAsyncClient();

        configurationClient.listConfigurationSettings(new SettingSelector().setLabelFilter(periodicUpdateLabel))
            .subscribe(setting -> updateConfiguration(setting));
    }

    public void addConfigurationSetting() {
        ConfigurationSetting setting = configurationClient.addConfigurationSetting("new_key", "new_label", "new_value");
    }

    public void setConfigurationSetting() {
        ConfigurationSetting setting = configurationClient.setConfigurationSetting("some_key", "some_label", "some_value");
    }

    public void getConfigurationSetting() {
        ConfigurationSetting setting = configurationClient.setConfigurationSetting("some_key", "some_label", "some_value");
        ConfigurationSetting retrievedSetting = configurationClient.getConfigurationSetting("some_key", "some_label");
    }

    public void getConfigurationSettingConditionally() {
        ConfigurationSetting setting = configurationClient.setConfigurationSetting("some_key", "some_label", "some_value");
        Response<ConfigurationSetting> settingResponse = configurationClient.getConfigurationSettingWithResponse(setting, null, true, Context.NONE);
    }

    public void updateConfigurationSetting() {
        ConfigurationSetting setting = configurationClient.setConfigurationSetting("some_key", "some_label", "some_value");
        ConfigurationSetting updatedSetting = configurationClient.setConfigurationSetting("some_key", "some_label", "new_value");
    }

    public void updateConfigurationSettingConditionally() {
        ConfigurationSetting setting = configurationClient.setConfigurationSetting("some_key", "some_label", "some_value");
        Response<ConfigurationSetting> settingResponse = configurationClient.setConfigurationSettingWithResponse(setting, true, Context.NONE);
    }

    public void deleteConfigurationSetting() {
        ConfigurationSetting setting = configurationClient.setConfigurationSetting("some_key", "some_label", "some_value");
        ConfigurationSetting deletedSetting = configurationClient.deleteConfigurationSetting("some_key", "some_label");
    }

    public void deleteConfigurationSettingConditionally() {
        ConfigurationSetting setting = configurationClient.setConfigurationSetting("some_key", "some_label", "some_value");
        Response<ConfigurationSetting> settingResponse = configurationClient.deleteConfigurationSettingWithResponse(setting, true, Context.NONE);
    }

    public void listConfigurationSetting() {
        String key = "some_key";
        String key2 = "new_key";
        configurationClient.setConfigurationSetting(key, "some_label", "some_value");
        configurationClient.setConfigurationSetting(key2, "new_label", "new_value");
        SettingSelector selector = new SettingSelector().setKeyFilter(key + "," + key2);
        PagedIterable<ConfigurationSetting> settings = configurationClient.listConfigurationSettings(selector);
    }

    public void listRevisions() {
        String key = "revisionKey";
        configurationClient.setConfigurationSetting(key, "some_label", "some_value");
        configurationClient.setConfigurationSetting(key, "new_label", "new_value");
        SettingSelector selector = new SettingSelector().setKeyFilter(key);
        PagedIterable<ConfigurationSetting> settings = configurationClient.listRevisions(selector);
    }

    public void setReadOnly() {
        configurationClient.setConfigurationSetting("some_key", "some_label", "some_value");
        ConfigurationSetting setting = configurationClient.setReadOnly("some_key", "some_label", true);
    }

    public void clearReadOnly() {
        ConfigurationSetting setting = configurationClient.setReadOnly("some_key", "some_label", false);
    }

    public void customHeaders() {
        // Add your headers
        HttpHeaders headers = new HttpHeaders();
        headers.put("my-header1", "my-header1-value");
        headers.put("my-header2", "my-header2-value");
        headers.put("my-header3", "my-header3-value");
        // Call API by passing headers in Context.
        configurationClient.addConfigurationSettingWithResponse(
            new ConfigurationSetting().setKey("key").setValue("value"),
            new Context(AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY, headers));
        // Above three HttpHeader will be added in outgoing HttpRequest.
    }

    public void createClientWithProxyOption() {
        // Proxy options
        final String hostname = "{your-host-name}";
        final int port = 447; // your port number

        ProxyOptions proxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP,
            new InetSocketAddress(hostname, port));
        HttpClient httpClient = new NettyAsyncHttpClientBuilder()
            .proxy(proxyOptions)
            .build();
        ConfigurationAsyncClient configurationAsyncClient = new ConfigurationClientBuilder()
            .connectionString("{your_connection_string}")
            .httpClient(httpClient)
            .buildAsyncClient();
    }

    private void updateConfiguration(ConfigurationSetting setting) {
        // do something on the given setting.
    }
}
