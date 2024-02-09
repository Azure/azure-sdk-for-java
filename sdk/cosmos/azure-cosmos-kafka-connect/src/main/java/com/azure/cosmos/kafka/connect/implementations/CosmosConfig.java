// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementations;

import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Common Configuration for Cosmos DB Kafka source connector and sink connector.
 */
public class CosmosConfig extends AbstractConfig {
    private static final ConfigDef.Validator NON_EMPTY_STRING = new ConfigDef.NonEmptyString();
    private static final String CONFIG_PREFIX = "kafka.connect.cosmos";

    // Account config
    private static final String ACCOUNT_ENDPOINT_CONFIG = CONFIG_PREFIX + "accountEndpoint";
    private static final String ACCOUNT_ENDPOINT_CONFIG_DOC = "Cosmos DB Account Endpoint Uri.";
    private static final String ACCOUNT_ENDPOINT_CONFIG_DISPLAY = "Cosmos DB Account Endpoint Uri.";

    private static final String ACCOUNT_KEY_CONFIG = CONFIG_PREFIX + "accountKey";
    private static final String ACCOUNT_KEY_CONFIG_DOC = "Cosmos DB Account Key.";
    private static final String ACCOUNT_KEY_CONFIG_DISPLAY = "Cosmos DB Account Key.";

    private static final String USE_GATEWAY_MODE = CONFIG_PREFIX + "useGatewayMode";
    private static final String USE_GATEWAY_MODE_DOC = "Flag to indicate whether to use gateway mode. By default it is false.";
    private static final String USE_GATEWAY_MODE_DISPLAY = "Use gateway mode.";
    private static final boolean DEFAULT_USE_GATEWAY_MODE = false;

    private static final String PREFERRED_REGIONS_LIST = CONFIG_PREFIX + "preferredRegionsList";
    private static final String PREFERRED_REGIONS_LIST_DOC = "Preferred regions list to be used for a multi region Cosmos DB account. " +
        "This is a comma separated value (e.g., `[East US, West US]` or `East US, West US`) provided preferred regions will be used as hint. " +
        "You should use a collocated kafka cluster with your Cosmos DB account and pass the kafka cluster region as preferred region. " +
        "See list of azure regions [here](https://docs.microsoft.com/dotnet/api/microsoft.azure.documents.locationnames?view=azure-dotnet&preserve-view=true).";
    private static final String PREFERRED_REGIONS_LIST_DISPLAY = "Preferred regions list.";

    private static final String APPLICATION_NAME = CONFIG_PREFIX + "applicationName";
    private static final String APPLICATION_NAME_DOC = "Application name. Will be added as the userAgent suffix.";
    private static final String APPLICATION_NAME_DISPLAY = "Application name.";

    // Client Telemetry
    private static final String CLIENT_TELEMETRY_ENABLED = CONFIG_PREFIX + "clientTelemetry.enabled";
    private static final String CLIENT_TELEMETRY_ENABLED_DOC = "Enables Client Telemetry - NOTE: This is a preview feature - and only works with public endpoints right now.";
    private static final String CLIENT_TELEMETRY_ENABLED_DISPLAY = "Enable client telemetry.";
    private static final boolean DEFAULT_CLIENT_TELEMETRY_ENABLED = false;

    private static final String CLIENT_TELEMETRY_ENDPOINT = CONFIG_PREFIX + "clientTelemetry.endpoint";
    private static final String CLIENT_TELEMETRY_ENDPOINT_DOC = "Enables Client Telemetry to be sent to the service endpoint provided - " +
        "NOTE: This is a preview feature - and only works with public endpoints right now";
    private static final String CLIENT_TELEMETRY_ENDPOINT_DISPLAY = "Client telemetry public endpoint.";

    private final CosmosAccountConfig accountConfig;
    private final CosmosDiagnosticsConfig diagnosticsConfig;

    public CosmosConfig(ConfigDef config, Map<String, String> parsedConfig) {
        super(config, parsedConfig);
        this.accountConfig = this.parseAccountConfig();
        this.diagnosticsConfig = this.parseDiagnosticsConfig();
    }

    private CosmosAccountConfig parseAccountConfig() {
        String endpoint = this.getString(ACCOUNT_ENDPOINT_CONFIG);
        String accountKey = this.getPassword(ACCOUNT_KEY_CONFIG).value();
        String applicationName = this.getString(APPLICATION_NAME);
        boolean useGatewayMode = this.getBoolean(USE_GATEWAY_MODE);
        List<String> preferredRegionList = this.getList(PREFERRED_REGIONS_LIST);

        return new CosmosAccountConfig(
            endpoint,
            accountKey,
            applicationName,
            useGatewayMode,
            preferredRegionList);
    }

    private CosmosDiagnosticsConfig parseDiagnosticsConfig() {
        Boolean clientTelemetryEnabled = this.getBoolean(CLIENT_TELEMETRY_ENABLED);
        String clientTelemetryEndpoint = this.getString(CLIENT_TELEMETRY_ENDPOINT);

        return new CosmosDiagnosticsConfig(clientTelemetryEnabled, clientTelemetryEndpoint);
    }

    public static ConfigDef getConfigDef() {
        ConfigDef configDef = new ConfigDef();

        defineAccountConfig(configDef);
        defineDiagnosticsConfig(configDef);

        return configDef;
    }

    private static void defineAccountConfig(ConfigDef result) {
        final String accountGroupName = "account";
        int accountGroupOrder = 0;

        // For optional config, need to provide a default value
        result
            .define(
                ACCOUNT_ENDPOINT_CONFIG,
                ConfigDef.Type.STRING,
                ConfigDef.NO_DEFAULT_VALUE,
                NON_EMPTY_STRING, // TODO: add endpoint validator
                ConfigDef.Importance.HIGH,
                ACCOUNT_ENDPOINT_CONFIG_DOC,
                accountGroupName,
                accountGroupOrder++,
                ConfigDef.Width.LONG,
                ACCOUNT_ENDPOINT_CONFIG_DISPLAY
            )
            .define(
                ACCOUNT_KEY_CONFIG,
                ConfigDef.Type.PASSWORD,
                ConfigDef.NO_DEFAULT_VALUE,
                ConfigDef.Importance.HIGH,
                ACCOUNT_KEY_CONFIG_DOC,
                accountGroupName,
                accountGroupOrder++,
                ConfigDef.Width.LONG,
                ACCOUNT_KEY_CONFIG_DISPLAY
            )
            .define(
                APPLICATION_NAME,
                ConfigDef.Type.STRING,
                Strings.Emtpy,
                ConfigDef.Importance.MEDIUM,
                APPLICATION_NAME_DOC,
                accountGroupName,
                accountGroupOrder++,
                ConfigDef.Width.LONG,
                APPLICATION_NAME_DISPLAY
            )
            .define(
                USE_GATEWAY_MODE,
                ConfigDef.Type.BOOLEAN,
                DEFAULT_USE_GATEWAY_MODE,
                ConfigDef.Importance.LOW,
                USE_GATEWAY_MODE_DOC,
                accountGroupName,
                accountGroupOrder++,
                ConfigDef.Width.MEDIUM,
                USE_GATEWAY_MODE_DISPLAY
            )
            .define(
                PREFERRED_REGIONS_LIST,
                ConfigDef.Type.LIST,
                Collections.EMPTY_LIST,
                ConfigDef.Importance.HIGH,
                PREFERRED_REGIONS_LIST_DOC,
                accountGroupName,
                accountGroupOrder++,
                ConfigDef.Width.LONG,
                PREFERRED_REGIONS_LIST_DISPLAY
            );
    }

    private static void defineDiagnosticsConfig(ConfigDef result) {
        final String diagnosticsGroupName = "diagnostics";
        int diagnosticsGroupOrder = 0;

        result
            .define(
                CLIENT_TELEMETRY_ENABLED,
                ConfigDef.Type.BOOLEAN,
                DEFAULT_CLIENT_TELEMETRY_ENABLED,
                ConfigDef.Importance.LOW,
                CLIENT_TELEMETRY_ENABLED_DOC,
                diagnosticsGroupName,
                diagnosticsGroupOrder++,
                ConfigDef.Width.MEDIUM,
                CLIENT_TELEMETRY_ENABLED_DISPLAY
            )
            .define(
                CLIENT_TELEMETRY_ENDPOINT,
                ConfigDef.Type.STRING,
                Strings.Emtpy,
                ConfigDef.Importance.LOW,
                CLIENT_TELEMETRY_ENDPOINT_DOC,
                diagnosticsGroupName,
                diagnosticsGroupOrder++,
                ConfigDef.Width.LONG,
                CLIENT_TELEMETRY_ENDPOINT_DISPLAY
            );
    }

    public CosmosAccountConfig getAccountConfig() {
        return accountConfig;
    }

    public CosmosDiagnosticsConfig getDiagnosticsConfig() {
        return diagnosticsConfig;
    }
}
