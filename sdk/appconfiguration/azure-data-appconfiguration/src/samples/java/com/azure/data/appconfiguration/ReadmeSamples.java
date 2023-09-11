// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.appconfiguration;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.AddHeadersFromContextPolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.ConfigurationSettingSnapshot;
import com.azure.data.appconfiguration.models.CreateSnapshotOperationDetail;
import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagFilter;
import com.azure.data.appconfiguration.models.SecretReferenceConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.data.appconfiguration.models.SnapshotSelector;
import com.azure.data.appconfiguration.models.SnapshotSettingFilter;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public void createFeatureFlagConfigurationSetting() {
        // BEGIN: readme-sample-addFeatureFlagConfigurationSetting
        String key = "some_key";
        String filterName = "{filter_name}"; // such as "Microsoft.Percentage"
        String filterParameterKey = "{filter_parameter_key}"; // "Value"
        Object filterParameterValue = 30; // Any value. Could be String, primitive value, or Json Object
        FeatureFlagFilter percentageFilter = new FeatureFlagFilter(filterName)
                                                 .addParameter(filterParameterKey, filterParameterValue);
        FeatureFlagConfigurationSetting featureFlagConfigurationSetting =
            new FeatureFlagConfigurationSetting(key, true)
                .setClientFilters(Arrays.asList(percentageFilter));

        FeatureFlagConfigurationSetting setting = (FeatureFlagConfigurationSetting)
            configurationClient.addConfigurationSetting(featureFlagConfigurationSetting);
        // END: readme-sample-addFeatureFlagConfigurationSetting
    }

    public void updateFeatureFlagConfigurationSetting() {
        String key = "some_key";
        String filterName = "{filter_name}"; // such as "Microsoft.Percentage"
        String filterParameterKey = "{filter_parameter_key}"; // "Value"
        Object filterParameterValue = 30; // Any value. Could be String, primitive value, or Json Object
        FeatureFlagFilter percentageFilter = new FeatureFlagFilter(filterName)
                                                 .addParameter(filterParameterKey, filterParameterValue);
        FeatureFlagConfigurationSetting featureFlagConfigurationSetting =
            new FeatureFlagConfigurationSetting(key, true)
                .setClientFilters(Arrays.asList(percentageFilter));

        // BEGIN: readme-sample-updateFeatureFlagConfigurationSetting
        FeatureFlagConfigurationSetting setting = (FeatureFlagConfigurationSetting)
            configurationClient.setConfigurationSetting(featureFlagConfigurationSetting);
        // END: readme-sample-updateFeatureFlagConfigurationSetting
    }

    public void getFeatureFlagConfigurationSetting() {
        String key = "some_key";
        String filterName = "{filter_name}"; // such as "Microsoft.Percentage"
        String filterParameterKey = "{filter_parameter_key}"; // "Value"
        Object filterParameterValue = 30; // Any value. Could be String, primitive value, or Json Object
        FeatureFlagFilter percentageFilter = new FeatureFlagFilter(filterName)
                                                 .addParameter(filterParameterKey, filterParameterValue);
        FeatureFlagConfigurationSetting featureFlagConfigurationSetting =
            new FeatureFlagConfigurationSetting(key, true)
                .setClientFilters(Arrays.asList(percentageFilter));

        // BEGIN: readme-sample-getFeatureFlagConfigurationSetting
        FeatureFlagConfigurationSetting setting = (FeatureFlagConfigurationSetting)
            configurationClient.getConfigurationSetting(featureFlagConfigurationSetting);
        // END: readme-sample-getFeatureFlagConfigurationSetting
    }

    public void deleteFeatureFlagConfigurationSetting() {
        String key = "some_key";
        String filterName = "{filter_name}"; // such as "Microsoft.Percentage"
        String filterParameterKey = "{filter_parameter_key}"; // "Value"
        Object filterParameterValue = 30; // Any value. Could be String, primitive value, or Json Object
        FeatureFlagFilter percentageFilter = new FeatureFlagFilter(filterName)
                                                 .addParameter(filterParameterKey, filterParameterValue);
        FeatureFlagConfigurationSetting featureFlagConfigurationSetting =
            new FeatureFlagConfigurationSetting(key, true)
                .setClientFilters(Arrays.asList(percentageFilter));

        // BEGIN: readme-sample-deleteFeatureFlagConfigurationSetting
        FeatureFlagConfigurationSetting setting = (FeatureFlagConfigurationSetting)
            configurationClient.deleteConfigurationSetting(featureFlagConfigurationSetting);
        // END: readme-sample-deleteFeatureFlagConfigurationSetting
    }

    public void addSecretReferenceConfigurationSetting() {
        // BEGIN: readme-sample-addSecretReferenceConfigurationSetting
        String key = "{some_key}";
        String keyVaultReference = "{key_vault_reference}";

        SecretReferenceConfigurationSetting referenceConfigurationSetting =
            new SecretReferenceConfigurationSetting(key, keyVaultReference);

        SecretReferenceConfigurationSetting setting = (SecretReferenceConfigurationSetting)
            configurationClient.addConfigurationSetting(referenceConfigurationSetting);
        // END: readme-sample-addSecretReferenceConfigurationSetting
    }

    public void updateSecretReferenceConfigurationSetting() {
        String key = "{some_key}";
        String keyVaultReference = "{key_vault_reference}";

        SecretReferenceConfigurationSetting referenceConfigurationSetting =
            new SecretReferenceConfigurationSetting(key, keyVaultReference);

        // BEGIN: readme-sample-updateSecretReferenceConfigurationSetting
        SecretReferenceConfigurationSetting setting = (SecretReferenceConfigurationSetting)
            configurationClient.setConfigurationSetting(referenceConfigurationSetting);
        // END: readme-sample-updateSecretReferenceConfigurationSetting
    }

    public void getSecretReferenceConfigurationSetting() {
        String key = "{some_key}";
        String keyVaultReference = "{key_vault_reference}";

        SecretReferenceConfigurationSetting referenceConfigurationSetting =
            new SecretReferenceConfigurationSetting(key, keyVaultReference);

        // BEGIN: readme-sample-getSecretReferenceConfigurationSetting
        SecretReferenceConfigurationSetting setting = (SecretReferenceConfigurationSetting)
            configurationClient.getConfigurationSetting(referenceConfigurationSetting);
        // END: readme-sample-getSecretReferenceConfigurationSetting
    }

    public void deleteSecretReferenceConfigurationSetting() {
        String key = "{some_key}";
        String keyVaultReference = "{key_vault_reference}";

        SecretReferenceConfigurationSetting referenceConfigurationSetting =
            new SecretReferenceConfigurationSetting(key, keyVaultReference);

        // BEGIN: readme-sample-deleteSecretReferenceConfigurationSetting
        SecretReferenceConfigurationSetting setting = (SecretReferenceConfigurationSetting)
            configurationClient.deleteConfigurationSetting(referenceConfigurationSetting);
        // END: readme-sample-deleteSecretReferenceConfigurationSetting
    }

    public void enableHttpLogging() {
        // BEGIN: readme-sample-enablehttplogging
        ConfigurationClient configurationClient = new ConfigurationClientBuilder()
                .connectionString(connectionString)
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                .buildClient();
        // or
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
        ConfigurationClient configurationClientAad = new ConfigurationClientBuilder()
                .credential(credential)
                .endpoint(endpoint)
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                .buildClient();
        // END: readme-sample-enablehttplogging

    }

    public void troubleshootingExceptions() {
        ConfigurationClient client = new ConfigurationClientBuilder()
                                                      .buildClient();
        // BEGIN: readme-sample-troubleshootingExceptions
        try {
            ConfigurationSetting setting = new ConfigurationSetting().setKey("myKey").setValue("myValue");
            client.getConfigurationSetting(setting);
        } catch (HttpResponseException e) {
            System.out.println(e.getMessage());
            // Do something with the exception
        }
        // END: readme-sample-troubleshootingExceptions

        ConfigurationAsyncClient asyncClient = new ConfigurationClientBuilder()
                                         .buildAsyncClient();
        // BEGIN: readme-sample-troubleshootingExceptions-async
        ConfigurationSetting setting = new ConfigurationSetting().setKey("myKey").setValue("myValue");
        asyncClient.getConfigurationSetting(setting)
            .doOnSuccess(ignored -> System.out.println("Success!"))
            .doOnError(
                error -> error instanceof ResourceNotFoundException,
                error -> System.out.println("Exception: 'getConfigurationSetting' could not be performed."));
        // END: readme-sample-troubleshootingExceptions-async
    }

    public void createSnapshot() {
        // BEGIN: readme-sample-createSnapshot
        String snapshotName = "{snapshotName}";
        // Prepare the snapshot filters
        List<SnapshotSettingFilter> filters = new ArrayList<>();
        // Key Name also supports RegExp but only support prefix end with "*", such as "k*" and is case-sensitive.
        filters.add(new SnapshotSettingFilter("Test*"));
        SyncPoller<CreateSnapshotOperationDetail, ConfigurationSettingSnapshot> poller =
            configurationClient.beginCreateSnapshot(snapshotName, new ConfigurationSettingSnapshot(filters), Context.NONE);
        poller.setPollInterval(Duration.ofSeconds(10));
        poller.waitForCompletion();
        ConfigurationSettingSnapshot snapshot = poller.getFinalResult();
        System.out.printf("Snapshot name=%s is created at %s, snapshot status is %s.%n",
            snapshot.getName(), snapshot.getCreatedAt(), snapshot.getStatus());
        // END: readme-sample-createSnapshot
    }

    public void getSnapshot() {
        // BEGIN: readme-sample-getSnapshot
        String snapshotName = "{snapshotName}";
        ConfigurationSettingSnapshot getSnapshot = configurationClient.getSnapshot(snapshotName);
        System.out.printf("Snapshot name=%s is created at %s, snapshot status is %s.%n",
            getSnapshot.getName(), getSnapshot.getCreatedAt(), getSnapshot.getStatus());
        // END: readme-sample-getSnapshot
    }

    public void archiveSnapshot() {
        // BEGIN: readme-sample-archiveSnapshot
        String snapshotName = "{snapshotName}";
        ConfigurationSettingSnapshot archivedSnapshot = configurationClient.archiveSnapshot(snapshotName);
        System.out.printf("Archived snapshot name=%s is created at %s, snapshot status is %s.%n",
            archivedSnapshot.getName(), archivedSnapshot.getCreatedAt(), archivedSnapshot.getStatus());
        // END: readme-sample-archiveSnapshot
    }

    public void recoverSnapshot() {
        // BEGIN: readme-sample-recoverSnapshot
        String snapshotName = "{snapshotName}";
        ConfigurationSettingSnapshot recoveredSnapshot = configurationClient.recoverSnapshot(snapshotName);
        System.out.printf("Recovered snapshot name=%s is created at %s, snapshot status is %s.%n",
            recoveredSnapshot.getName(), recoveredSnapshot.getCreatedAt(), recoveredSnapshot.getStatus());
        // END: readme-sample-recoverSnapshot
    }

    public void listSnapshot() {
        // BEGIN: readme-sample-getAllSnapshots
        String snapshotNameProduct = "{snapshotNameInProduct}";
        SnapshotSelector snapshotSelector = new SnapshotSelector().setName(snapshotNameProduct);
        PagedIterable<ConfigurationSettingSnapshot> configurationSettingSnapshots =
            configurationClient.listSnapshots(snapshotSelector);
        for (ConfigurationSettingSnapshot snapshot : configurationSettingSnapshots) {
            System.out.printf("Listed Snapshot name = %s is created at %s, snapshot status is %s.%n",
                snapshot.getName(), snapshot.getCreatedAt(), snapshot.getStatus());
        }
        // END: readme-sample-getAllSnapshots
    }

    public void listSettingInSnapshot() {
        // BEGIN: readme-sample-listSettingsInSnapshot
        String snapshotNameProduct = "{snapshotNameInProduct}";
        PagedIterable<ConfigurationSetting> configurationSettings =
            configurationClient.listConfigurationSettingsForSnapshot(snapshotNameProduct);

        for (ConfigurationSetting setting : configurationSettings) {
            System.out.printf("[ConfigurationSetting in snapshot] Key: %s, Value: %s%n",
                setting.getKey(), setting.getValue());
        }
        // END: readme-sample-listSettingsInSnapshot

    }

    private void updateConfiguration(ConfigurationSetting setting) {
        // do something on the given setting.
    }
}
