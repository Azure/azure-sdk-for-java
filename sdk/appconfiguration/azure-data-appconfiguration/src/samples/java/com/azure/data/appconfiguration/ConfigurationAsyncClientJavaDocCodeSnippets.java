// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.http.MatchConditions;
import com.azure.core.util.Configuration;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.ConfigurationSettingsFilter;
import com.azure.data.appconfiguration.models.ConfigurationSnapshot;
import com.azure.data.appconfiguration.models.SettingLabelSelector;
import com.azure.data.appconfiguration.models.SettingFields;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.data.appconfiguration.models.SnapshotFields;
import com.azure.data.appconfiguration.models.SnapshotSelector;
import reactor.util.context.Context;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Code snippets for {@link ConfigurationAsyncClient}
 */
public class ConfigurationAsyncClientJavaDocCodeSnippets {

    private static final String NO_LABEL = null;
    private String key1 = "key1";
    private String key2 = "key2";
    private String value1 = "val1";
    private String value2 = "val2";

    private ConfigurationAsyncClient client = getAsyncClient();

    /**
     * Code snippets for {@link ConfigurationAsyncClient#addConfigurationSetting(String, String, String)}
     */
    public void addConfigurationSettingsCodeSnippet() {
        ConfigurationAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.addConfigurationSetting#string-string-string
        client.addConfigurationSetting("prodDBConnection", "westUS", "db_connection")
            .subscribe(response -> System.out.printf("Key: %s, Label: %s, Value: %s",
                response.getKey(), response.getLabel(), response.getValue()));
        // END: com.azure.data.appconfiguration.configurationasyncclient.addConfigurationSetting#string-string-string

        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.addConfigurationSetting#ConfigurationSetting
        client.addConfigurationSetting(new ConfigurationSetting()
                .setKey("prodDBConnection")
                .setLabel("westUS")
                .setValue("db_connection"))
            .subscribe(response -> System.out.printf("Key: %s, Label: %s, Value: %s",
                response.getKey(), response.getLabel(), response.getValue()));
        // END: com.azure.data.appconfiguration.configurationasyncclient.addConfigurationSetting#ConfigurationSetting

        /**
         * Code snippets for {@link ConfigurationAsyncClient#addConfigurationSettingWithResponse(ConfigurationSetting)}
         */
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.addConfigurationSettingWithResponse#ConfigurationSetting
        client.addConfigurationSettingWithResponse(new ConfigurationSetting()
                .setKey("prodDBConnection")
                .setLabel("westUS")
                .setValue("db_connection"))
            .subscribe(response -> {
                ConfigurationSetting responseSetting = response.getValue();
                System.out.printf("Key: %s, Label: %s, Value: %s",
                    responseSetting.getKey(), responseSetting.getLabel(), responseSetting.getValue());
            });
        // END: com.azure.data.appconfiguration.configurationasyncclient.addConfigurationSettingWithResponse#ConfigurationSetting
    }

    /**
     * Code snippets for {@link ConfigurationAsyncClient#setConfigurationSetting(String, String, String)}
     */
    public void setConfigurationSettingsCodeSnippet() {
        ConfigurationAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.setConfigurationSetting#string-string-string
        client.setConfigurationSetting("prodDBConnection", "westUS", "db_connection")
            .subscribe(response -> System.out.printf("Key: %s, Label: %s, Value: %s",
                response.getKey(), response.getLabel(), response.getValue()));
        // Update the value of the setting to "updated_db_connection"
        client.setConfigurationSetting("prodDBConnection", "westUS", "updated_db_connection")
            .subscribe(response -> System.out.printf("Key: %s, Label: %s, Value: %s",
                response.getKey(), response.getLabel(), response.getValue()));
        // END: com.azure.data.appconfiguration.configurationasyncclient.setConfigurationSetting#string-string-string

        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.setConfigurationSetting#ConfigurationSetting
        client.setConfigurationSetting(new ConfigurationSetting()
                .setKey("prodDBConnection")
                .setLabel("westUS"))
            .subscribe(response -> System.out.printf("Key: %s, Label: %s, Value: %s",
                response.getKey(), response.getLabel(), response.getValue()));
        // Update the value of the setting to "updated_db_connection"
        client.setConfigurationSetting(new ConfigurationSetting()
                .setKey("prodDBConnection")
                .setLabel("westUS")
                .setValue("updated_db_connection"))
            .subscribe(response -> System.out.printf("Key: %s, Label: %s, Value: %s",
                response.getKey(), response.getLabel(), response.getValue()));
        // END: com.azure.data.appconfiguration.configurationasyncclient.setConfigurationSetting#ConfigurationSetting

        /**
         * Code snippets for
         * {@link ConfigurationAsyncClient#setConfigurationSettingWithResponse(ConfigurationSetting, boolean)}
         */
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.setConfigurationSettingWithResponse#ConfigurationSetting-boolean
        client.setConfigurationSettingWithResponse(new ConfigurationSetting()
                .setKey("prodDBConnection")
                .setLabel("westUS")
                .setValue("db_connection"),
                false)
            .subscribe(response -> {
                final ConfigurationSetting result = response.getValue();
                System.out.printf("Key: %s, Label: %s, Value: %s",
                    result.getKey(), result.getLabel(), result.getValue());
            });
        // Update the value of the setting to "updated_db_connection"
        client.setConfigurationSettingWithResponse(new ConfigurationSetting()
                .setKey("prodDBConnection")
                .setLabel("westUS")
                .setValue("updated_db_connection"),
                false)
            .subscribe(response -> {
                final ConfigurationSetting responseSetting = response.getValue();
                System.out.printf("Key: %s, Label: %s, Value: %s",
                    responseSetting.getKey(), responseSetting.getLabel(), responseSetting.getValue());
            });
        // END: com.azure.data.appconfiguration.configurationasyncclient.setConfigurationSettingWithResponse#ConfigurationSetting-boolean
    }

    /**
     * Code snippets for {@link ConfigurationAsyncClient#getConfigurationSetting(String, String)}
     */
    public void getConfigurationSettingsCodeSnippet() {
        ConfigurationAsyncClient client = getAsyncClient();

        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.getConfigurationSetting#string-string
        client.getConfigurationSetting("prodDBConnection", "westUS")
            .subscribe(response -> System.out.printf("Key: %s, Label: %s, Value: %s",
                response.getKey(), response.getLabel(), response.getValue()));
        // END: com.azure.data.appconfiguration.configurationasyncclient.getConfigurationSetting#string-string

        /**
         * Code snippets for {@link ConfigurationAsyncClient#getConfigurationSetting(String, String, OffsetDateTime)}
         */
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.getConfigurationSetting#string-string-OffsetDateTime
        client.getConfigurationSetting(
            "prodDBConnection", "westUS", OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(1))
            .subscribe(response -> System.out.printf("Key: %s, Label: %s, Value: %s",
                response.getKey(), response.getLabel(), response.getValue()));
        // END: com.azure.data.appconfiguration.configurationasyncclient.getConfigurationSetting#string-string-OffsetDateTime

        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.getConfigurationSetting#ConfigurationSetting
        client.getConfigurationSetting(new ConfigurationSetting()
                .setKey("prodDBConnection")
                .setLabel("westUS"))
            .subscribe(response -> System.out.printf("Key: %s, Label: %s, Value: %s",
                response.getKey(), response.getLabel(), response.getValue()));
        // END: com.azure.data.appconfiguration.configurationasyncclient.getConfigurationSetting#ConfigurationSetting

        /**
         * Code snippets for {@link ConfigurationAsyncClient#getConfigurationSettingWithResponse(
         * ConfigurationSetting, OffsetDateTime, boolean)}
         */
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.getConfigurationSettingWithResponse#ConfigurationSetting-OffsetDateTime-boolean
        client.getConfigurationSettingWithResponse(new ConfigurationSetting()
                .setKey("prodDBConnection")
                .setLabel("westUS"),
                null,
                false)
            .contextWrite(Context.of(key1, value1, key2, value2))
            .subscribe(response -> {
                ConfigurationSetting result = response.getValue();
                System.out.printf("Key: %s, Label: %s, Value: %s",
                    result.getKey(), result.getLabel(), result.getValue());
            });
        // END: com.azure.data.appconfiguration.configurationasyncclient.getConfigurationSettingWithResponse#ConfigurationSetting-OffsetDateTime-boolean
    }

    /**
     * Code snippets for {@link ConfigurationAsyncClient#deleteConfigurationSetting(String, String)}
     */
    public void deleteConfigurationSettingsCodeSnippet() {
        ConfigurationAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.deleteConfigurationSetting#string-string
        client.deleteConfigurationSetting("prodDBConnection", "westUS")
            .subscribe(response -> System.out.printf("Key: %s, Label: %s, Value: %s",
                response.getKey(), response.getLabel(), response.getValue()));
        // END: com.azure.data.appconfiguration.configurationasyncclient.deleteConfigurationSetting#string-string

        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.deleteConfigurationSetting#ConfigurationSetting
        client.deleteConfigurationSetting(new ConfigurationSetting()
                .setKey("prodDBConnection")
                .setLabel("westUS"))
            .subscribe(response -> System.out.printf("Key: %s, Label: %s, Value: %s",
                response.getKey(), response.getValue()));
        // END: com.azure.data.appconfiguration.configurationasyncclient.deleteConfigurationSetting#ConfigurationSetting

        /**
         * Code snippets for
         * {@link ConfigurationAsyncClient#deleteConfigurationSettingWithResponse(ConfigurationSetting, boolean)}
         */
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.deleteConfigurationSettingWithResponse#ConfigurationSetting-boolean
        client.deleteConfigurationSettingWithResponse(new ConfigurationSetting()
                .setKey("prodDBConnection")
                .setLabel("westUS"),
                false)
            .contextWrite(Context.of(key1, value1, key2, value2))
            .subscribe(response -> {
                ConfigurationSetting responseSetting = response.getValue();
                System.out.printf("Key: %s, Label: %s, Value: %s",
                    responseSetting.getKey(), responseSetting.getLabel(), responseSetting.getValue());
            });
        // END: com.azure.data.appconfiguration.configurationasyncclient.deleteConfigurationSettingWithResponse#ConfigurationSetting-boolean
    }

    /**
     * Code snippets for {@link ConfigurationAsyncClient#setReadOnly(String, String, boolean)} set to read-only setting
     */
    public void lockSettingsCodeSnippet() {
        ConfigurationAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.setReadOnly#string-string-boolean
        client.setReadOnly("prodDBConnection", "westUS", true)
            .subscribe(response -> System.out.printf("Key: %s, Label: %s, Value: %s",
                response.getKey(), response.getLabel(), response.getValue()));
        // END: com.azure.data.appconfiguration.configurationasyncclient.setReadOnly#string-string-boolean

        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.setReadOnly#ConfigurationSetting-boolean
        client.setReadOnly(new ConfigurationSetting()
                .setKey("prodDBConnection")
                .setLabel("westUS"),
                true)
            .subscribe(response -> System.out.printf("Key: %s, Label: %s, Value: %s",
                response.getKey(), response.getLabel(), response.getValue()));
        // END: com.azure.data.appconfiguration.configurationasyncclient.setReadOnly#ConfigurationSetting-boolean

        /**
         * Code snippets for {@link ConfigurationAsyncClient#setReadOnlyWithResponse(ConfigurationSetting, Boolean)}
         */
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.setReadOnlyWithResponse#ConfigurationSetting-boolean
        client.setReadOnlyWithResponse(new ConfigurationSetting()
                .setKey("prodDBConnection")
                .setLabel("westUS"),
                true)
            .subscribe(response -> {
                ConfigurationSetting result = response.getValue();
                System.out.printf("Key: %s, Label: %s, Value: %s",
                    result.getKey(), result.getLabel(), result.getValue());
            });
        // END: com.azure.data.appconfiguration.configurationasyncclient.setReadOnlyWithResponse#ConfigurationSetting-boolean
    }

    /**
     * Code snippets for {@link ConfigurationAsyncClient#setReadOnly(String, String, boolean)} set to not read-only setting
     */
    public void unlockSettingsCodeSnippet() {
        ConfigurationAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.setReadOnly#string-string-boolean-clearReadOnly
        client.setReadOnly("prodDBConnection", "westUS", false)
            .contextWrite(Context.of(key1, value1, key2, value2))
            .subscribe(response -> System.out.printf("Key: %s, Value: %s", response.getKey(), response.getValue()));
        // END: com.azure.data.appconfiguration.configurationasyncclient.setReadOnly#string-string-boolean-clearReadOnly

        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.setReadOnly#ConfigurationSetting-boolean-clearReadOnly
        client.setReadOnly(new ConfigurationSetting()
                .setKey("prodDBConnection")
                .setLabel("westUS"),
                false)
            .subscribe(response -> System.out.printf("Key: %s, Value: %s", response.getKey(), response.getValue()));
        // END: com.azure.data.appconfiguration.configurationasyncclient.setReadOnly#ConfigurationSetting-boolean-clearReadOnly

        /**
         * Code snippets for {@link ConfigurationAsyncClient#setReadOnlyWithResponse(ConfigurationSetting, Boolean)}
         */
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.setReadOnlyWithResponse#ConfigurationSetting-boolean-clearReadOnly
        client.setReadOnlyWithResponse(new ConfigurationSetting()
                .setKey("prodDBConnection")
                .setLabel("westUS"),
                false)
            .contextWrite(Context.of(key1, value1, key2, value2))
            .subscribe(response -> {
                ConfigurationSetting result = response.getValue();
                System.out.printf("Key: %s, Value: %s", result.getKey(), result.getValue());
            });
        // END: com.azure.data.appconfiguration.configurationasyncclient.setReadOnlyWithResponse#ConfigurationSetting-boolean-clearReadOnly
    }


    /**
     * Code snippets for {@link ConfigurationAsyncClient#listConfigurationSettings(SettingSelector)}
     */
    public void listSettingCodeSnippet() {
        ConfigurationAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.listConfigurationSettings
        client.listConfigurationSettings(new SettingSelector().setKeyFilter("prodDBConnection"))
            .contextWrite(Context.of(key1, value1, key2, value2))
            .subscribe(setting ->
                System.out.printf("Key: %s, Value: %s", setting.getKey(), setting.getValue()));
        // END: com.azure.data.appconfiguration.configurationasyncclient.listConfigurationSettings
    }

    /**
     * Code snippets for {@link ConfigurationAsyncClient#listConfigurationSettingsForSnapshot(String)}
     */
    public void listConfigurationSettingsForSnapshot() {
        ConfigurationAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.listConfigurationSettingsForSnapshot
        String snapshotName = "{snapshotName}";
        client.listConfigurationSettingsForSnapshot(snapshotName)
            .subscribe(setting ->
                System.out.printf("Key: %s, Value: %s", setting.getKey(), setting.getValue()));
        // END: com.azure.data.appconfiguration.configurationasyncclient.listConfigurationSettingsForSnapshot
    }

    /**
     * Code snippets for {@link ConfigurationAsyncClient#listConfigurationSettingsForSnapshot(String, List)}
     */
    public void listConfigurationSettingsForSnapshotMaxOverload() {
        ConfigurationAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.listConfigurationSettingsForSnapshotMaxOverload
        String snapshotName = "{snapshotName}";
        List<SettingFields> fields = Arrays.asList(SettingFields.KEY);
        client.listConfigurationSettingsForSnapshot(snapshotName, fields)
            .subscribe(setting ->
                System.out.printf("Key: %s, Value: %s", setting.getKey(), setting.getValue()));
        // END: com.azure.data.appconfiguration.configurationasyncclient.listConfigurationSettingsForSnapshotMaxOverload
    }

    /**
     * Code snippets for {@link ConfigurationAsyncClient#listRevisions(SettingSelector)}
     */
    public void listRevisionsCodeSnippet() {
        ConfigurationAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.listsettingrevisions
        client.listRevisions(new SettingSelector().setKeyFilter("prodDBConnection"))
            .contextWrite(Context.of(key1, value1, key2, value2))
            .subscribe(setting ->
                System.out.printf("Key: %s, Value: %s", setting.getKey(), setting.getValue()));
        // END: com.azure.data.appconfiguration.configurationasyncclient.listsettingrevisions
    }

    /**
     * Code snippets for {@link ConfigurationAsyncClient#beginCreateSnapshot(String, ConfigurationSnapshot)}}
     */
    public void beginCreateSnapshotMaxOverload() {
        ConfigurationAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.beginCreateSnapshotMaxOverload
        List<ConfigurationSettingsFilter> filters = new ArrayList<>();
        // Key Name also supports RegExp but only support prefix end with "*", such as "k*" and is case-sensitive.
        filters.add(new ConfigurationSettingsFilter("{keyName}"));
        String snapshotName = "{snapshotName}";
        client.beginCreateSnapshot(snapshotName, new ConfigurationSnapshot(filters)
                .setRetentionPeriod(Duration.ofHours(1)))
            .flatMap(result -> result.getFinalResult())
            .subscribe(
                snapshot -> System.out.printf("Snapshot name=%s is created at %s%n",
                    snapshot.getName(), snapshot.getCreatedAt()),
                ex -> System.out.printf("Error on creating a snapshot=%s, with error=%s.%n", snapshotName,
                    ex.getMessage()),
                () -> System.out.println("Successfully created a snapshot."));
        // END: com.azure.data.appconfiguration.configurationasyncclient.beginCreateSnapshotMaxOverload
    }

    /**
     * Code snippets for {@link ConfigurationAsyncClient#getSnapshot(String)}
     */
    public void getSnapshotByName() {
        ConfigurationAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.getSnapshotByName
        String snapshotName = "{snapshotName}";
        client.getSnapshot(snapshotName).subscribe(
            getSnapshot -> {
                System.out.printf("Snapshot name=%s is created at %s, snapshot status is %s.%n",
                    getSnapshot.getName(), getSnapshot.getCreatedAt(), getSnapshot.getStatus());
            }
        );
        // END: com.azure.data.appconfiguration.configurationasyncclient.getSnapshotByName
    }

    /**
     * Code snippets for {@link ConfigurationAsyncClient#getSnapshotWithResponse(String, List)}
     */
    public void getSnapshotByNameMaxOverload() {
        ConfigurationAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.getSnapshotByNameMaxOverload
        String snapshotName = "{snapshotName}";

        client.getSnapshotWithResponse(snapshotName, Arrays.asList(SnapshotFields.NAME, SnapshotFields.CREATED_AT,
            SnapshotFields.STATUS, SnapshotFields.FILTERS))
            .subscribe(
                response -> {
                    ConfigurationSnapshot getSnapshot = response.getValue();
                    // Only properties `name`, `createAt`, `status` and `filters` have value, and expect null or
                    // empty value other than the `fields` specified in the request.
                    System.out.printf("Snapshot name=%s is created at %s, snapshot status is %s.%n",
                        getSnapshot.getName(), getSnapshot.getCreatedAt(), getSnapshot.getStatus());
                    List<ConfigurationSettingsFilter> filters = getSnapshot.getFilters();
                    for (ConfigurationSettingsFilter filter : filters) {
                        System.out.printf("Snapshot filter key=%s, label=%s.%n", filter.getKey(), filter.getLabel());
                    }
                });
        // END: com.azure.data.appconfiguration.configurationasyncclient.getSnapshotByNameMaxOverload
    }

    /**
     * Code snippets for {@link ConfigurationAsyncClient#archiveSnapshot(String)}
     */
    public void archiveSnapshotByName() {
        ConfigurationAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.archiveSnapshotByName
        String snapshotName = "{snapshotName}";
        client.archiveSnapshot(snapshotName).subscribe(
            archivedSnapshot -> {
                System.out.printf("Archived snapshot name=%s is created at %s, snapshot status is %s.%n",
                    archivedSnapshot.getName(), archivedSnapshot.getCreatedAt(), archivedSnapshot.getStatus());
            }
        );
        // END: com.azure.data.appconfiguration.configurationasyncclient.archiveSnapshotByName
    }

    /**
     * Code snippets for {@link ConfigurationAsyncClient#archiveSnapshotWithResponse(String, MatchConditions)}
     */
    public void archiveSnapshotMaxOverload() {
        ConfigurationAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.archiveSnapshotMaxOverload
        String snapshotName = "{snapshotName}";
        MatchConditions matchConditions = new MatchConditions().setIfMatch("{etag}");
        client.archiveSnapshotWithResponse(snapshotName, matchConditions)
            .subscribe(
                response -> {
                    ConfigurationSnapshot archivedSnapshot = response.getValue();
                    System.out.printf("Archived snapshot name=%s is created at %s, snapshot status is %s.%n",
                        archivedSnapshot.getName(), archivedSnapshot.getCreatedAt(), archivedSnapshot.getStatus());
                }
            );
        // END: com.azure.data.appconfiguration.configurationasyncclient.archiveSnapshotMaxOverload
    }

    /**
     * Code snippets for {@link ConfigurationAsyncClient#recoverSnapshot(String)}
     */
    public void recoverSnapshotByName() {
        ConfigurationAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.recoverSnapshotByName
        String snapshotName = "{snapshotName}";
        client.recoverSnapshot(snapshotName).subscribe(
            recoveredSnapshot -> {
                System.out.printf("Recovered snapshot name=%s is created at %s, snapshot status is %s.%n",
                    recoveredSnapshot.getName(), recoveredSnapshot.getCreatedAt(), recoveredSnapshot.getStatus());
            }
        );
        // END: com.azure.data.appconfiguration.configurationasyncclient.recoverSnapshotByName
    }

    /**
     * Code snippets for {@link ConfigurationAsyncClient#recoverSnapshotWithResponse(String, MatchConditions)}
     */
    public void recoverSnapshotMaxOverload() {
        ConfigurationAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.recoverSnapshotMaxOverload
        String snapshotName = "{snapshotName}";
        MatchConditions matchConditions = new MatchConditions().setIfMatch("{etag}");
        client.recoverSnapshotWithResponse(snapshotName, matchConditions).subscribe(
            response -> {
                ConfigurationSnapshot recoveredSnapshot = response.getValue();
                System.out.printf("Recovered snapshot name=%s is created at %s, snapshot status is %s.%n",
                    recoveredSnapshot.getName(), recoveredSnapshot.getCreatedAt(), recoveredSnapshot.getStatus());
            }
        );
        // END: com.azure.data.appconfiguration.configurationasyncclient.recoverSnapshotMaxOverload
    }


    /**
     * Code snippets for {@link ConfigurationAsyncClient#listSnapshots(SnapshotSelector)}
     */
    public void listSnapshots() {
        ConfigurationAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.listSnapshots
        String snapshotNameFilter = "{snapshotNamePrefix}*";
        client.listSnapshots(new SnapshotSelector().setNameFilter(snapshotNameFilter))
            .subscribe(recoveredSnapshot -> {
                System.out.printf("Recovered snapshot name=%s is created at %s, snapshot status is %s.%n",
                    recoveredSnapshot.getName(), recoveredSnapshot.getCreatedAt(), recoveredSnapshot.getStatus());
            });
        // END: com.azure.data.appconfiguration.configurationasyncclient.listSnapshots
    }

    /**
     * Code snippets for {@link ConfigurationAsyncClient#listLabels(SettingLabelSelector)}
     */
    public void listLabels() {
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.listLabels
        String labelNameFilter = "{labelNamePrefix}*";
        client.listLabels(new SettingLabelSelector().setNameFilter(labelNameFilter))
                .subscribe(label -> {
                    System.out.println("label name = " + label);
                });
        // END: com.azure.data.appconfiguration.configurationasyncclient.listLabels
    }

    /**
     * Implementation not provided
     *
     * @return {@code null}
     */
    private ConfigurationAsyncClient getAsyncClient() {
        return new ConfigurationClientBuilder()
            .connectionString(Configuration.getGlobalConfiguration().get("AZURE_APPCONFIG_CONNECTION_STRING"))
            .buildAsyncClient();
    }
}
