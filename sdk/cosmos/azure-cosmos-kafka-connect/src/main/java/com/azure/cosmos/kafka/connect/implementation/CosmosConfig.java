// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation;

import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Common Configuration for Cosmos DB Kafka source connector and sink connector.
 */
public class CosmosConfig extends AbstractConfig {
    protected static final ConfigDef.Validator NON_EMPTY_STRING = new ConfigDef.NonEmptyString();
    private static final String CONFIG_PREFIX = "kafka.connect.cosmos.";

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

    private final CosmosAccountConfig accountConfig;

    public CosmosConfig(ConfigDef config, Map<String, ?> parsedConfig) {
        super(config, parsedConfig);
        this.accountConfig = this.parseAccountConfig();
    }

    private CosmosAccountConfig parseAccountConfig() {
        String endpoint = this.getString(ACCOUNT_ENDPOINT_CONFIG);
        String accountKey = this.getPassword(ACCOUNT_KEY_CONFIG).value();
        String applicationName = this.getString(APPLICATION_NAME);
        boolean useGatewayMode = this.getBoolean(USE_GATEWAY_MODE);
        List<String> preferredRegionList = this.getPreferredRegionList();

        return new CosmosAccountConfig(
            endpoint,
            accountKey,
            applicationName,
            useGatewayMode,
            preferredRegionList);
    }

    private List<String> getPreferredRegionList() {
        return convertToList(this.getString(PREFERRED_REGIONS_LIST));
    }

    public static ConfigDef getConfigDef() {
        ConfigDef configDef = new ConfigDef();

        defineAccountConfig(configDef);

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
                new AccountEndpointValidator(),
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
                ConfigDef.Type.STRING,
                Strings.Emtpy,
                ConfigDef.Importance.HIGH,
                PREFERRED_REGIONS_LIST_DOC,
                accountGroupName,
                accountGroupOrder++,
                ConfigDef.Width.LONG,
                PREFERRED_REGIONS_LIST_DISPLAY
            );
    }

    public CosmosAccountConfig getAccountConfig() {
        return accountConfig;
    }

    public static class AccountEndpointValidator implements ConfigDef.Validator {
        @Override
        @SuppressWarnings("unchecked")
        public void ensureValid(String name, Object o) {
            String accountEndpointUriString = (String) o;
            if (StringUtils.isEmpty(accountEndpointUriString)) {
                throw new ConfigException(name, o, "Account endpoint can not be empty");
            }

            try {
                new URL(accountEndpointUriString);
            } catch (MalformedURLException e) {
                throw new ConfigException(name, o, "Invalid account endpoint.");
            }
        }

        @Override
        public String toString() {
            return "Account endpoint";
        }
    }

    protected static List<String> convertToList(String configValue) {
        if (StringUtils.isNotEmpty(configValue)) {
            if (configValue.startsWith("[") && configValue.endsWith("]")) {
                configValue = configValue.substring(1, configValue.length() - 1);
            }

            return Arrays.stream(configValue.split(",")).map(String::trim).collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}
