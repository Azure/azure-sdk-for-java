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
@SuppressWarnings("unused")
public class ReadmeSamples {
    private String endpoint = "endpoint";
    private String connectionString = "connection string";
    private String urlKey = "url key";
    private String urlLabel = "url label";
    private String periodicUpdateLabel = "periodic update label";
    private ConfigurationClient configurationClient = new ConfigurationClientBuilder().buildClient();

    public void createClient() {
        // BEGIN: readme-sample-createClient
        ConfigurationClient configurationClient = new ConfigurationClientBuilder()
            .connectionString(connectionString)
            .buildClient();
        // END: readme-sample-createClient
    }

    public void createAsyncClient() {
        // BEGIN: readme-sample-createAsyncClient
        ConfigurationAsyncClient configurationClient = new ConfigurationClientBuilder()
            .connectionString(connectionString)
            .buildAsyncClient();
        // END: readme-sample-createAsyncClient
    }

    public void aadAuthentication() {
        // BEGIN: readme-sample-aadAuthentication
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
        ConfigurationClient configurationClient = new ConfigurationClientBuilder()
            .credential(credential)
            .endpoint(endpoint)
            .buildClient();
        // END: readme-sample-aadAuthentication
    }

    public void sqlExample() {
        // BEGIN: readme-sample-sqlExample
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
        // END: readme-sample-sqlExample
    }

    public void listConfigurationsExample() {
        // BEGIN: readme-sample-listConfigurationsExample
        ConfigurationAsyncClient configurationClient = new ConfigurationClientBuilder()
            .connectionString(connectionString)
            .buildAsyncClient();

        configurationClient.listConfigurationSettings(new SettingSelector().setLabelFilter(periodicUpdateLabel))
            .subscribe(setting -> updateConfiguration(setting));
        // END: readme-sample-listConfigurationsExample
    }

    public void addConfigurationSetting() {
        // BEGIN: readme-sample-addConfigurationSetting
        ConfigurationSetting setting = configurationClient.addConfigurationSetting("new_key", "new_label", "new_value");
        // END: readme-sample-addConfigurationSetting
    }

    public void setConfigurationSetting() {
        // BEGIN: readme-sample-setConfigurationSetting
        ConfigurationSetting setting = configurationClient.setConfigurationSetting("some_key", "some_label", "some_value");
        // END: readme-sample-setConfigurationSetting
    }

    public void getConfigurationSetting() {
        // BEGIN: readme-sample-getConfigurationSetting
        ConfigurationSetting setting = configurationClient.setConfigurationSetting("some_key", "some_label", "some_value");
        ConfigurationSetting retrievedSetting = configurationClient.getConfigurationSetting("some_key", "some_label");
        // END: readme-sample-getConfigurationSetting
    }

    public void getConfigurationSettingConditionally() {
        // BEGIN: readme-sample-getConfigurationSettingConditionally
        ConfigurationSetting setting = configurationClient.setConfigurationSetting("some_key", "some_label", "some_value");
        Response<ConfigurationSetting> settingResponse = configurationClient.getConfigurationSettingWithResponse(setting, null, true, Context.NONE);
        // END: readme-sample-getConfigurationSettingConditionally
    }

    public void updateConfigurationSetting() {
        // BEGIN: readme-sample-updateConfigurationSetting
        ConfigurationSetting setting = configurationClient.setConfigurationSetting("some_key", "some_label", "some_value");
        ConfigurationSetting updatedSetting = configurationClient.setConfigurationSetting("some_key", "some_label", "new_value");
        // END: readme-sample-updateConfigurationSetting
    }

    public void updateConfigurationSettingConditionally() {
        // BEGIN: readme-sample-updateConfigurationSettingConditionally
        ConfigurationSetting setting = configurationClient.setConfigurationSetting("some_key", "some_label", "some_value");
        Response<ConfigurationSetting> settingResponse = configurationClient.setConfigurationSettingWithResponse(setting, true, Context.NONE);
        // END: readme-sample-updateConfigurationSettingConditionally
    }

    public void deleteConfigurationSetting() {
        // BEGIN: readme-sample-deleteConfigurationSetting
        ConfigurationSetting setting = configurationClient.setConfigurationSetting("some_key", "some_label", "some_value");
        ConfigurationSetting deletedSetting = configurationClient.deleteConfigurationSetting("some_key", "some_label");
        // END: readme-sample-deleteConfigurationSetting
    }

    public void deleteConfigurationSettingConditionally() {
        // BEGIN: readme-sample-deleteConfigurationSettingConditionally
        ConfigurationSetting setting = configurationClient.setConfigurationSetting("some_key", "some_label", "some_value");
        Response<ConfigurationSetting> settingResponse = configurationClient.deleteConfigurationSettingWithResponse(setting, true, Context.NONE);
        // END: readme-sample-deleteConfigurationSettingConditionally
    }

    public void listConfigurationSetting() {
        // BEGIN: readme-sample-listConfigurationSetting
        String key = "some_key";
        String key2 = "new_key";
        configurationClient.setConfigurationSetting(key, "some_label", "some_value");
        configurationClient.setConfigurationSetting(key2, "new_label", "new_value");
        SettingSelector selector = new SettingSelector().setKeyFilter(key + "," + key2);
        PagedIterable<ConfigurationSetting> settings = configurationClient.listConfigurationSettings(selector);
        // END: readme-sample-listConfigurationSetting
    }

    public void listRevisions() {
        // BEGIN: readme-sample-listRevisions
        String key = "revisionKey";
        configurationClient.setConfigurationSetting(key, "some_label", "some_value");
        configurationClient.setConfigurationSetting(key, "new_label", "new_value");
        SettingSelector selector = new SettingSelector().setKeyFilter(key);
        PagedIterable<ConfigurationSetting> settings = configurationClient.listRevisions(selector);
        // END: readme-sample-listRevisions
    }

    public void setReadOnly() {
        // BEGIN: readme-sample-setReadOnly
        configurationClient.setConfigurationSetting("some_key", "some_label", "some_value");
        ConfigurationSetting setting = configurationClient.setReadOnly("some_key", "some_label", true);
        // END: readme-sample-setReadOnly
    }

    public void clearReadOnly() {
        // BEGIN: readme-sample-clearReadOnly
        ConfigurationSetting setting = configurationClient.setReadOnly("some_key", "some_label", false);
        // END: readme-sample-clearReadOnly
    }

    public void customHeaders() {
        // BEGIN: readme-sample-customHeaders
        // Add your headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("my-header1", "my-header1-value");
        headers.set("my-header2", "my-header2-value");
        headers.set("my-header3", "my-header3-value");
        // Call API by passing headers in Context.
        configurationClient.addConfigurationSettingWithResponse(
            new ConfigurationSetting().setKey("key").setValue("value"),
            new Context(AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY, headers));
        // Above three HttpHeader will be added in outgoing HttpRequest.
        // END: readme-sample-customHeaders
    }

    public void createClientWithProxyOption() {
        // BEGIN: readme-sample-createClientWithProxyOption
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
        // END: readme-sample-createClientWithProxyOption
    }

    private void updateConfiguration(ConfigurationSetting setting) {
        // do something on the given setting.
    }
}
