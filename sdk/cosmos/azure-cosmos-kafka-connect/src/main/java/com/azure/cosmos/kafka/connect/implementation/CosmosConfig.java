// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation;

import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.kafka.connect.implementation.source.CosmosSourceContainersConfig;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.common.config.ConfigValue;

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
    private static final String ACCOUNT_ENDPOINT = CONFIG_PREFIX + "accountEndpoint";
    private static final String ACCOUNT_ENDPOINT_DOC = "Cosmos DB Account Endpoint Uri.";
    private static final String ACCOUNT_ENDPOINT_DISPLAY = "Cosmos DB Account Endpoint Uri.";

    private static final String ACCOUNT_KEY = CONFIG_PREFIX + "accountKey";
    private static final String ACCOUNT_KEY_DOC = "Cosmos DB Account Key.";
    private static final String ACCOUNT_KEY_DISPLAY = "Cosmos DB Account Key.";
    private static final String DEFAULT_ACCOUNT_KEY = Strings.Emtpy;

    // AAD config
    private static final String ACCOUNT_TENANT_ID = CONFIG_PREFIX + "account.tenantId";
    private static final String ACCOUNT_TENANT_ID_DOC = "The tenantId of the CosmosDB account. Required for `ServicePrincipal` authentication.";
    private static final String ACCOUNT_TENANT_ID_DISPLAY = "The tenantId of the CosmosDB account.";
    private static final String DEFAULT_ACCOUNT_TENANT_ID = Strings.Emtpy;

    private static final String AUTH_TYPE = CONFIG_PREFIX + "auth.type";
    private static final String AUTH_TYPE_DOC = "There are two auth types are supported currently: "
        + "`MasterKey`(PrimaryReadWriteKeys, SecondReadWriteKeys, PrimaryReadOnlyKeys, SecondReadWriteKeys), `ServicePrincipal`";
    private static final String AUTH_TYPE_DISPLAY = "Cosmos Auth type.";
    private static final String DEFAULT_AUTH_TYPE = CosmosAuthTypes.MASTER_KEY.getName();

    private static final String AAD_CLIENT_ID = CONFIG_PREFIX + "auth.aad.clientId";
    private static final String AAD_CLIENT_ID_DOC = "The clientId/ApplicationId of the service principal. Required for `ServicePrincipal` authentication.";
    private static final String AAD_CLIENT_ID_DISPLAY = "The clientId/ApplicationId of the service principal.";
    private static final String DEFAULT_AAD_CLIENT_ID = Strings.Emtpy;

    private static final String AAD_CLIENT_SECRET = CONFIG_PREFIX + "auth.aad.clientSecret";
    private static final String AAD_CLIENT_SECRET_DOC = "The client secret/password of the service principal. Required for `ServicePrincipal` authentication.";
    private static final String AAD_CLIENT_SECRET_DISPLAY = "The client secret/password of the service principal.";
    private static final String DEFAULT_AAD_CLIENT_SECRET = Strings.Emtpy;

    private static final String USE_GATEWAY_MODE = CONFIG_PREFIX + "useGatewayMode";
    private static final String USE_GATEWAY_MODE_DOC = "Flag to indicate whether to use gateway mode. By default it is false.";
    private static final String USE_GATEWAY_MODE_DISPLAY = "Use gateway mode.";
    private static final boolean DEFAULT_USE_GATEWAY_MODE = false;

    private static final String PREFERRED_REGIONS_LIST = CONFIG_PREFIX + "preferredRegionsList";
    private static final String PREFERRED_REGIONS_LIST_DOC = "Preferred regions list to be used for a multi region Cosmos DB account. "
        + "This is a comma separated value (e.g., `[East US, West US]` or `East US, West US`) provided preferred regions will be used as hint. "
        + "You should use a collocated kafka cluster with your Cosmos DB account and pass the kafka cluster region as preferred region. "
        + "See list of azure regions [here](https://docs.microsoft.com/dotnet/api/microsoft.azure.documents.locationnames?view=azure-dotnet&preserve-view=true).";
    private static final String PREFERRED_REGIONS_LIST_DISPLAY = "Preferred regions list.";

    private static final String APPLICATION_NAME = CONFIG_PREFIX + "applicationName";
    private static final String APPLICATION_NAME_DOC = "Application name. Will be added as the userAgent suffix.";
    private static final String APPLICATION_NAME_DISPLAY = "Application name.";

    // Throughput control config
    private static final String THROUGHPUT_CONTROL_ENABLED = CONFIG_PREFIX + "throughputControl.enabled";
    private static final String THROUGHPUT_CONTROL_ENABLED_DOC = "A flag to indicate whether throughput control is enabled.";
    private static final String THROUGHPUT_CONTROL_ENABLED_DISPLAY = "A flag to indicate whether throughput control is enabled.";
    private static final boolean DEFAULT_THROUGHPUT_CONTROL_ENABLED = false;

    private static final String THROUGHPUT_CONTROL_ACCOUNT_ENDPOINT = CONFIG_PREFIX + "throughputControl.accountEndpoint";
    private static final String THROUGHPUT_CONTROL_ACCOUNT_ENDPOINT_DOC = "Cosmos DB Throughput Control Account Endpoint Uri.";
    private static final String THROUGHPUT_CONTROL_ACCOUNT_ENDPOINT_DISPLAY = "Cosmos DB Throughput Control Account Endpoint Uri.";
    private static final String DEFAULT_THROUGHPUT_CONTROL_ACCOUNT_ENDPOINT = Strings.Emtpy;

    private static final String THROUGHPUT_CONTROL_ACCOUNT_KEY = CONFIG_PREFIX + "throughputControl.accountKey";
    private static final String THROUGHPUT_CONTROL_ACCOUNT_KEY_DOC = "Cosmos DB Throughput Control Account Key.";
    private static final String THROUGHPUT_CONTROL_ACCOUNT_KEY_DISPLAY = "Cosmos DB Throughput Control Account Key.";
    private static final String DEFAULT_THROUGHPUT_CONTROL_ACCOUNT_KEY = Strings.Emtpy;

    // Throughput control AAD config
    private static final String THROUGHPUT_CONTROL_ACCOUNT_TENANT_ID = CONFIG_PREFIX + "throughputControl.account.tenantId";
    private static final String THROUGHPUT_CONTROL_ACCOUNT_TENANT_ID_DOC = "The tenantId of the CosmosDB account. Required for `ServicePrincipal` authentication.";
    private static final String THROUGHPUT_CONTROL_ACCOUNT_TENANT_ID_DISPLAY = "The tenantId of the CosmosDB account.";
    private static final String DEFAULT_THROUGHPUT_CONTROL_ACCOUNT_TENANT_ID = Strings.Emtpy;

    private static final String THROUGHPUT_CONTROL_AUTH_TYPE = CONFIG_PREFIX + "throughputControl.auth.type";
    private static final String THROUGHPUT_CONTROL_AUTH_TYPE_DOC = "There are two auth types are supported currently: "
        + "`MasterKey`(PrimaryReadWriteKeys, SecondReadWriteKeys, PrimaryReadOnlyKeys, SecondReadWriteKeys), `ServicePrincipal`";
    private static final String THROUGHPUT_CONTROL_AUTH_TYPE_DISPLAY = "Cosmos Auth type.";
    private static final String DEFAULT_THROUGHPUT_CONTROL_AUTH_TYPE = CosmosAuthTypes.MASTER_KEY.getName();

    private static final String THROUGHPUT_CONTROL_AAD_CLIENT_ID = CONFIG_PREFIX + "throughputControl.auth.aad.clientId";
    private static final String THROUGHPUT_CONTROL_AAD_CLIENT_ID_DOC = "The clientId/ApplicationId of the service principal. Required for `ServicePrincipal` authentication.";
    private static final String THROUGHPUT_CONTROL_AAD_CLIENT_ID_DISPLAY = "The clientId/ApplicationId of the service principal.";
    private static final String DEFAULT_THROUGHPUT_CONTROL_AAD_CLIENT_ID = Strings.Emtpy;

    private static final String THROUGHPUT_CONTROL_AAD_CLIENT_SECRET = CONFIG_PREFIX + "throughputControl.auth.aad.clientSecret";
    private static final String THROUGHPUT_CONTROL_AAD_CLIENT_SECRET_DOC = "The client secret/password of the service principal. Required for `ServicePrincipal` authentication.";
    private static final String THROUGHPUT_CONTROL_AAD_CLIENT_SECRET_DISPLAY = "The client secret/password of the service principal.";
    private static final String DEFAULT_THROUGHPUT_CONTROL_AAD_CLIENT_SECRET = Strings.Emtpy;

    private static final String THROUGHPUT_CONTROL_PREFERRED_REGIONS_LIST = CONFIG_PREFIX + "throughputControl.preferredRegionsList";
    private static final String THROUGHPUT_CONTROL_PREFERRED_REGIONS_LIST_DOC = "Preferred regions list to be used for a multi region Cosmos DB account. "
        + "This is a comma separated value (e.g., `[East US, West US]` or `East US, West US`) provided preferred regions will be used as hint. "
        + "You should use a collocated kafka cluster with your Cosmos DB account and pass the kafka cluster region as preferred region. "
        + "See list of azure regions [here](https://docs.microsoft.com/dotnet/api/microsoft.azure.documents.locationnames?view=azure-dotnet&preserve-view=true).";
    private static final String THROUGHPUT_CONTROL_PREFERRED_REGIONS_LIST_DISPLAY = "Preferred regions list for throughput control database account";
    private static final String DEFAULT_THROUGHPUT_CONTROL_PREFERRED_REGIONS_LIST = Strings.Emtpy;

    private static final String THROUGHPUT_CONTROL_USE_GATEWAY_MODE = CONFIG_PREFIX + "throughputControl.useGatewayMode";
    private static final String THROUGHPUT_CONTROL_USE_GATEWAY_MODE_DOC = "Flag to indicate whether to use gateway mode for throughput control. By default it is false.";
    private static final String THROUGHPUT_CONTROL_USE_GATEWAY_MODE_DISPLAY = "Use gateway mode for throughput control";
    private static final boolean DEFAULT_THROUGHPUT_CONTROL_USE_GATEWAY_MODE = false;

    private static final String THROUGHPUT_CONTROL_GROUP_NAME = CONFIG_PREFIX + "throughputControl.name";
    private static final String THROUGHPUT_CONTROL_GROUP_NAME_DOC =
        "Throughput control group name. Since customer is allowed to create many groups for a container, the name should be unique.";
    private static final String THROUGHPUT_CONTROL_GROUP_NAME_DISPLAY = "Throughput control group name.";
    private static final String DEFAULT_THROUGHPUT_CONTROL_GROUP_NAME = Strings.Emtpy;

    private static final String THROUGHPUT_CONTROL_TARGET_THROUGHPUT = CONFIG_PREFIX + "throughputControl.targetThroughput";
    private static final String THROUGHPUT_CONTROL_TARGET_THROUGHPUT_DOC = "Throughput control group target throughput. The value should be larger than 0.";
    private static final String THROUGHPUT_CONTROL_TARGET_THROUGHPUT_DISPLAY = "Throughput control group target throughput. The value should be larger than 0.";
    private static final int DEFAULT_THROUGHPUT_CONTROL_TARGET_THROUGHPUT = -1;

    private static final String THROUGHPUT_CONTROL_TARGET_THROUGHPUT_THRESHOLD = CONFIG_PREFIX + "throughputControl.targetThroughputThreshold";
    private static final String THROUGHPUT_CONTROL_TARGET_THROUGHPUT_THRESHOLD_DOC = "Throughput control group target throughput threshold. The value should be between (0,1].";
    private static final String THROUGHPUT_CONTROL_TARGET_THROUGHPUT_THRESHOLD_DISPLAY = "Throughput control group target throughput threshold. The value should be between (0,1].";
    private static final double DEFAULT_THROUGHPUT_CONTROL_TARGET_THROUGHPUT_THRESHOLD = -1d;

    private static final String THROUGHPUT_CONTROL_PRIORITY_LEVEL = CONFIG_PREFIX + "throughputControl.priorityLevel";
    private static final String THROUGHPUT_CONTROL_PRIORITY_LEVEL_DOC = "Throughput control group priority level. The value can be None, High or Low.";
    private static final String THROUGHPUT_CONTROL_PRIORITY_LEVEL_DISPLAY = "Throughput control group priority level. The value can be None, High or Low.";
    private static final String DEFAULT_THROUGHPUT_CONTROL_PRIORITY_LEVEL = CosmosPriorityLevel.NONE.getName();

    private static final String THROUGHPUT_CONTROL_GLOBAL_CONTROL_DATABASE = CONFIG_PREFIX + "throughputControl.globalControl.database";
    private static final String THROUGHPUT_CONTROL_GLOBAL_CONTROL_DATABASE_DOC = "Database which will be used for throughput global control.";
    private static final String THROUGHPUT_CONTROL_GLOBAL_CONTROL_DATABASE_DISPLAY = "Database which will be used for throughput global control.";
    private static final String DEFAULT_THROUGHPUT_CONTROL_GLOBAL_CONTROL_DATABASE = Strings.Emtpy;

    private static final String THROUGHPUT_CONTROL_GLOBAL_CONTROL_CONTAINER = CONFIG_PREFIX + "throughputControl.globalControl.container";
    private static final String THROUGHPUT_CONTROL_GLOBAL_CONTROL_CONTAINER_DOC = "Container which will be used for throughput global control.";
    private static final String THROUGHPUT_CONTROL_GLOBAL_CONTROL_CONTAINER_DISPLAY = "Container which will be used for throughput global control.";
    private static final String DEFAULT_THROUGHPUT_CONTROL_GLOBAL_CONTROL_CONTAINER = Strings.Emtpy;

    private static final String THROUGHPUT_CONTROL_GLOBAL_CONTROL_RENEW_INTERVAL_IN_MS = CONFIG_PREFIX + "throughputControl.globalControl.renewIntervalInMS";
    private static final String THROUGHPUT_CONTROL_GLOBAL_CONTROL_RENEW_INTERVAL_IN_MS_DOC =
        "This controls how often the client is going to update the throughput usage of itself "
            + "and adjust its own throughput share based on the throughput usage of other clients. "
            + "Default is 5s, the allowed min value is 5s.";
    private static final String THROUGHPUT_CONTROL_GLOBAL_CONTROL_RENEW_INTERVAL_IN_MS_DISPLAY =
        "Throughput control client RU usage update interval";
    private static final int DEFAULT_THROUGHPUT_CONTROL_GLOBAL_CONTROL_RENEW_INTERVAL_IN_MS = -1;

    private static final String THROUGHPUT_CONTROL_GLOBAL_CONTROL_EXPIRE_INTERVAL_IN_MS = CONFIG_PREFIX + "throughputControl.globalControl.expireIntervalInMS";
    private static final String THROUGHPUT_CONTROL_GLOBAL_CONTROL_EXPIRE_INTERVAL_IN_MS_DOC =
        "This controls how quickly we will detect the client has been offline "
            + "and hence allow its throughput share to be taken by other clients. "
            + "Default is 11s, the allowed min value is 2 * renewIntervalInMS + 1";
    private static final String THROUGHPUT_CONTROL_GLOBAL_CONTROL_EXPIRE_INTERVAL_IN_MS_DISPLAY =
        "Throughput control client expire interval";
    private static final int DEFAULT_THROUGHPUT_CONTROL_GLOBAL_CONTROL_EXPIRE_INTERVAL_IN_MS = -1;

    private final CosmosAccountConfig accountConfig;
    private final CosmosThroughputControlConfig throughputControlConfig;

    public CosmosConfig(ConfigDef config, Map<String, ?> parsedConfig) {
        super(config, parsedConfig);
        this.accountConfig = this.parseAccountConfig();
        this.throughputControlConfig = this.parseThroughputControlConfig();
    }

    private CosmosAccountConfig parseAccountConfig() {
        return parseAccountConfigCore(
            ACCOUNT_ENDPOINT,
            ACCOUNT_TENANT_ID,
            AUTH_TYPE,
            ACCOUNT_KEY,
            AAD_CLIENT_ID,
            AAD_CLIENT_SECRET,
            APPLICATION_NAME,
            USE_GATEWAY_MODE,
            PREFERRED_REGIONS_LIST);
    }

    private CosmosAccountConfig parseAccountConfigCore(
        String accountEndpointConfig,
        String accountTenantIdConfig,
        String authTypeConfig,
        String accountKeyConfig,
        String clientIdConfig,
        String clientSecretConfig,
        String applicationNameConfig,
        String useGatewayModeConfig,
        String preferredRegionListConfig) {

        String endpoint = this.getString(accountEndpointConfig);
        String tenantId = this.getString(accountTenantIdConfig);
        CosmosAuthTypes authType = this.parseCosmosAuthType(authTypeConfig);
        String masterKey = this.getString(accountKeyConfig);
        String clientId = this.getString(clientIdConfig);
        String clientSecret = this.getString(clientSecretConfig);
        CosmosAuthConfig authConfig = getAuthConfig(tenantId, authType, masterKey, clientId, clientSecret);

        String applicationName = this.getString(applicationNameConfig);
        boolean useGatewayMode = this.getBoolean(useGatewayModeConfig);
        List<String> preferredRegionList = this.getPreferredRegionList(preferredRegionListConfig);

        return new CosmosAccountConfig(
            endpoint,
            tenantId,
            authConfig,
            applicationName,
            useGatewayMode,
            preferredRegionList);
    }

    private CosmosAuthConfig getAuthConfig(
        String tenantId,
        CosmosAuthTypes authType,
        String masterKey,
        String clientId,
        String clientSecret) {

        switch (authType) {
            case MASTER_KEY:
                return new CosmosMasterKeyAuthConfig(masterKey);
            case SERVICE_PRINCIPAL:
                return new CosmosAadAuthConfig(clientId, clientSecret, tenantId);
            default:
                throw new IllegalArgumentException("AuthType " + authType + " is not supported");
        }
    }

    private CosmosAuthTypes parseCosmosAuthType(String configName) {
        String authType = this.getString(configName);
        return CosmosAuthTypes.fromName(authType);
    }

    private CosmosThroughputControlConfig parseThroughputControlConfig() {
        boolean enabled = this.getBoolean(THROUGHPUT_CONTROL_ENABLED);
        String accountEndpoint = this.getString(THROUGHPUT_CONTROL_ACCOUNT_ENDPOINT);

        CosmosAccountConfig throughputControlAccountConfig = null;
        if (enabled && StringUtils.isNotEmpty(accountEndpoint)) {
            throughputControlAccountConfig = parseAccountConfigCore(
                THROUGHPUT_CONTROL_ACCOUNT_ENDPOINT,
                THROUGHPUT_CONTROL_ACCOUNT_TENANT_ID,
                THROUGHPUT_CONTROL_AUTH_TYPE,
                THROUGHPUT_CONTROL_ACCOUNT_KEY,
                THROUGHPUT_CONTROL_AAD_CLIENT_ID,
                THROUGHPUT_CONTROL_AAD_CLIENT_SECRET,
                APPLICATION_NAME,
                THROUGHPUT_CONTROL_USE_GATEWAY_MODE,
                THROUGHPUT_CONTROL_PREFERRED_REGIONS_LIST);
        }

        String throughputControlGroupName = this.getString(THROUGHPUT_CONTROL_GROUP_NAME);
        int targetThroughput = this.getInt(THROUGHPUT_CONTROL_TARGET_THROUGHPUT);
        double targetThroughputThreshold = this.getDouble(THROUGHPUT_CONTROL_TARGET_THROUGHPUT_THRESHOLD);
        CosmosPriorityLevel priorityLevel = this.parsePriorityLevel();
        String globalControlDatabaseName = this.getString(THROUGHPUT_CONTROL_GLOBAL_CONTROL_DATABASE);
        String globalControlContainerName = this.getString(THROUGHPUT_CONTROL_GLOBAL_CONTROL_CONTAINER);
        int globalThroughputControlRenewInterval = this.getInt(THROUGHPUT_CONTROL_GLOBAL_CONTROL_RENEW_INTERVAL_IN_MS);
        int globalThroughputControlExpireInterval = this.getInt(THROUGHPUT_CONTROL_GLOBAL_CONTROL_EXPIRE_INTERVAL_IN_MS);

        return new CosmosThroughputControlConfig(
            enabled,
            throughputControlAccountConfig,
            throughputControlGroupName,
            targetThroughput,
            targetThroughputThreshold,
            priorityLevel,
            globalControlDatabaseName,
            globalControlContainerName,
            globalThroughputControlRenewInterval,
            globalThroughputControlExpireInterval);
    }

    private List<String> getPreferredRegionList(String preferredRegionListConfig) {
        return convertToList(this.getString(preferredRegionListConfig));
    }

    private List<String> getThroughputControlPreferredRegionList() {
        return convertToList(this.getString(THROUGHPUT_CONTROL_PREFERRED_REGIONS_LIST));
    }

    private CosmosPriorityLevel parsePriorityLevel() {
        String priorityLevel = this.getString(THROUGHPUT_CONTROL_PRIORITY_LEVEL);
        return CosmosPriorityLevel.fromName(priorityLevel);
    }

    public static ConfigDef getConfigDef() {
        ConfigDef configDef = new ConfigDef();

        defineAccountConfig(configDef);
        defineThroughputControlConfig(configDef);

        return configDef;
    }

    private static void defineAccountConfig(ConfigDef result) {
        final String accountGroupName = "account";
        int accountGroupOrder = 0;

        // For optional config, need to provide a default value
        result
            .define(
                ACCOUNT_ENDPOINT,
                ConfigDef.Type.STRING,
                ConfigDef.NO_DEFAULT_VALUE,
                new AccountEndpointValidator(),
                ConfigDef.Importance.HIGH,
                ACCOUNT_ENDPOINT_DOC,
                accountGroupName,
                accountGroupOrder++,
                ConfigDef.Width.LONG,
                ACCOUNT_ENDPOINT_DISPLAY
            )
            .define(
                ACCOUNT_KEY,
                ConfigDef.Type.PASSWORD,
                DEFAULT_ACCOUNT_KEY,
                ConfigDef.Importance.HIGH,
                ACCOUNT_KEY_DOC,
                accountGroupName,
                accountGroupOrder++,
                ConfigDef.Width.LONG,
                ACCOUNT_KEY_DISPLAY
            )
            .define(
                ACCOUNT_TENANT_ID,
                ConfigDef.Type.STRING,
                DEFAULT_ACCOUNT_TENANT_ID,
                ConfigDef.Importance.MEDIUM,
                ACCOUNT_TENANT_ID_DOC,
                accountGroupName,
                accountGroupOrder++,
                ConfigDef.Width.LONG,
                ACCOUNT_TENANT_ID_DISPLAY
            )
            .define(
                AUTH_TYPE,
                ConfigDef.Type.STRING,
                DEFAULT_AUTH_TYPE,
                new AuthTypeValidator(),
                ConfigDef.Importance.MEDIUM,
                AUTH_TYPE_DOC,
                accountGroupName,
                accountGroupOrder++,
                ConfigDef.Width.MEDIUM,
                AUTH_TYPE_DISPLAY
            )
            .define(
                AAD_CLIENT_ID,
                ConfigDef.Type.STRING,
                DEFAULT_AAD_CLIENT_ID,
                ConfigDef.Importance.MEDIUM,
                AAD_CLIENT_ID_DOC,
                accountGroupName,
                accountGroupOrder++,
                ConfigDef.Width.MEDIUM,
                AAD_CLIENT_ID_DISPLAY
            )
            .define(
                AAD_CLIENT_SECRET,
                ConfigDef.Type.PASSWORD,
                DEFAULT_AAD_CLIENT_SECRET,
                ConfigDef.Importance.MEDIUM,
                AAD_CLIENT_SECRET_DOC,
                accountGroupName,
                accountGroupOrder++,
                ConfigDef.Width.MEDIUM,
                AAD_CLIENT_SECRET_DISPLAY
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

    private static void defineThroughputControlConfig(ConfigDef result) {
        final String throughputControlGroupName = "throughput control";
        int throughputControlGroupOrder = 0;

        // For optional config, need to provide a default value
        result
            .define(
                THROUGHPUT_CONTROL_ENABLED,
                ConfigDef.Type.BOOLEAN,
                DEFAULT_THROUGHPUT_CONTROL_ENABLED,
                ConfigDef.Importance.MEDIUM,
                THROUGHPUT_CONTROL_ENABLED_DOC,
                throughputControlGroupName,
                throughputControlGroupOrder++,
                ConfigDef.Width.MEDIUM,
                THROUGHPUT_CONTROL_ENABLED_DISPLAY
            )
            .define(
                THROUGHPUT_CONTROL_ACCOUNT_ENDPOINT,
                ConfigDef.Type.STRING,
                DEFAULT_THROUGHPUT_CONTROL_ACCOUNT_ENDPOINT,
                ConfigDef.Importance.LOW,
                THROUGHPUT_CONTROL_ACCOUNT_ENDPOINT_DOC,
                throughputControlGroupName,
                throughputControlGroupOrder++,
                ConfigDef.Width.LONG,
                THROUGHPUT_CONTROL_ACCOUNT_ENDPOINT_DISPLAY
            )
            .define(
                THROUGHPUT_CONTROL_ACCOUNT_KEY,
                ConfigDef.Type.PASSWORD,
                DEFAULT_THROUGHPUT_CONTROL_ACCOUNT_KEY,
                ConfigDef.Importance.LOW,
                THROUGHPUT_CONTROL_ACCOUNT_KEY_DOC,
                throughputControlGroupName,
                throughputControlGroupOrder++,
                ConfigDef.Width.LONG,
                THROUGHPUT_CONTROL_ACCOUNT_KEY_DISPLAY
            )
            .define(
                THROUGHPUT_CONTROL_ACCOUNT_TENANT_ID,
                ConfigDef.Type.STRING,
                DEFAULT_THROUGHPUT_CONTROL_ACCOUNT_TENANT_ID,
                ConfigDef.Importance.LOW,
                THROUGHPUT_CONTROL_ACCOUNT_TENANT_ID_DOC,
                throughputControlGroupName,
                throughputControlGroupOrder++,
                ConfigDef.Width.LONG,
                THROUGHPUT_CONTROL_ACCOUNT_TENANT_ID_DISPLAY
            )
            .define(
                THROUGHPUT_CONTROL_AUTH_TYPE,
                ConfigDef.Type.STRING,
                DEFAULT_THROUGHPUT_CONTROL_AUTH_TYPE,
                new AuthTypeValidator(),
                ConfigDef.Importance.LOW,
                THROUGHPUT_CONTROL_AUTH_TYPE_DOC,
                throughputControlGroupName,
                throughputControlGroupOrder++,
                ConfigDef.Width.MEDIUM,
                THROUGHPUT_CONTROL_AUTH_TYPE_DISPLAY
            )
            .define(
                THROUGHPUT_CONTROL_AAD_CLIENT_ID,
                ConfigDef.Type.STRING,
                DEFAULT_THROUGHPUT_CONTROL_AAD_CLIENT_ID,
                ConfigDef.Importance.LOW,
                THROUGHPUT_CONTROL_AAD_CLIENT_ID_DOC,
                throughputControlGroupName,
                throughputControlGroupOrder++,
                ConfigDef.Width.MEDIUM,
                THROUGHPUT_CONTROL_AAD_CLIENT_ID_DISPLAY
            )
            .define(
                THROUGHPUT_CONTROL_AAD_CLIENT_SECRET,
                ConfigDef.Type.PASSWORD,
                DEFAULT_THROUGHPUT_CONTROL_AAD_CLIENT_SECRET,
                ConfigDef.Importance.LOW,
                THROUGHPUT_CONTROL_AAD_CLIENT_SECRET_DOC,
                throughputControlGroupName,
                throughputControlGroupOrder++,
                ConfigDef.Width.MEDIUM,
                THROUGHPUT_CONTROL_AAD_CLIENT_SECRET_DISPLAY
            )
            .define(
                THROUGHPUT_CONTROL_PREFERRED_REGIONS_LIST,
                ConfigDef.Type.STRING,
                DEFAULT_THROUGHPUT_CONTROL_PREFERRED_REGIONS_LIST,
                ConfigDef.Importance.LOW,
                THROUGHPUT_CONTROL_PREFERRED_REGIONS_LIST_DOC,
                throughputControlGroupName,
                throughputControlGroupOrder++,
                ConfigDef.Width.LONG,
                THROUGHPUT_CONTROL_PREFERRED_REGIONS_LIST_DISPLAY
            )
            .define(
                THROUGHPUT_CONTROL_USE_GATEWAY_MODE,
                ConfigDef.Type.BOOLEAN,
                DEFAULT_THROUGHPUT_CONTROL_USE_GATEWAY_MODE,
                ConfigDef.Importance.LOW,
                THROUGHPUT_CONTROL_USE_GATEWAY_MODE_DOC,
                throughputControlGroupName,
                throughputControlGroupOrder++,
                ConfigDef.Width.MEDIUM,
                THROUGHPUT_CONTROL_USE_GATEWAY_MODE_DISPLAY
            )
            .define(
                THROUGHPUT_CONTROL_GROUP_NAME,
                ConfigDef.Type.STRING,
                DEFAULT_THROUGHPUT_CONTROL_GROUP_NAME,
                ConfigDef.Importance.MEDIUM,
                THROUGHPUT_CONTROL_GROUP_NAME_DOC,
                throughputControlGroupName,
                throughputControlGroupOrder++,
                ConfigDef.Width.LONG,
                THROUGHPUT_CONTROL_GROUP_NAME_DISPLAY
            )
            .define(
                THROUGHPUT_CONTROL_TARGET_THROUGHPUT,
                ConfigDef.Type.INT,
                DEFAULT_THROUGHPUT_CONTROL_TARGET_THROUGHPUT,
                ConfigDef.Importance.MEDIUM,
                THROUGHPUT_CONTROL_TARGET_THROUGHPUT_DOC,
                throughputControlGroupName,
                throughputControlGroupOrder++,
                ConfigDef.Width.MEDIUM,
                THROUGHPUT_CONTROL_TARGET_THROUGHPUT_DISPLAY
            )
            .define(
                THROUGHPUT_CONTROL_TARGET_THROUGHPUT_THRESHOLD,
                ConfigDef.Type.DOUBLE,
                DEFAULT_THROUGHPUT_CONTROL_TARGET_THROUGHPUT_THRESHOLD,
                ConfigDef.Importance.MEDIUM,
                THROUGHPUT_CONTROL_TARGET_THROUGHPUT_THRESHOLD_DOC,
                throughputControlGroupName,
                throughputControlGroupOrder++,
                ConfigDef.Width.MEDIUM,
                THROUGHPUT_CONTROL_TARGET_THROUGHPUT_THRESHOLD_DISPLAY
            )
            .define(
                THROUGHPUT_CONTROL_GLOBAL_CONTROL_DATABASE,
                ConfigDef.Type.STRING,
                DEFAULT_THROUGHPUT_CONTROL_GLOBAL_CONTROL_DATABASE,
                ConfigDef.Importance.MEDIUM,
                THROUGHPUT_CONTROL_GLOBAL_CONTROL_DATABASE_DOC,
                throughputControlGroupName,
                throughputControlGroupOrder++,
                ConfigDef.Width.LONG,
                THROUGHPUT_CONTROL_GLOBAL_CONTROL_DATABASE_DISPLAY
            )
            .define(
                THROUGHPUT_CONTROL_GLOBAL_CONTROL_CONTAINER,
                ConfigDef.Type.STRING,
                DEFAULT_THROUGHPUT_CONTROL_GLOBAL_CONTROL_CONTAINER,
                ConfigDef.Importance.MEDIUM,
                THROUGHPUT_CONTROL_GLOBAL_CONTROL_CONTAINER_DOC,
                throughputControlGroupName,
                throughputControlGroupOrder++,
                ConfigDef.Width.LONG,
                THROUGHPUT_CONTROL_GLOBAL_CONTROL_CONTAINER_DISPLAY
            )
            .define(
                THROUGHPUT_CONTROL_PRIORITY_LEVEL,
                ConfigDef.Type.STRING,
                DEFAULT_THROUGHPUT_CONTROL_PRIORITY_LEVEL,
                ConfigDef.Importance.MEDIUM,
                THROUGHPUT_CONTROL_PRIORITY_LEVEL_DOC,
                throughputControlGroupName,
                throughputControlGroupOrder++,
                ConfigDef.Width.MEDIUM,
                THROUGHPUT_CONTROL_PRIORITY_LEVEL_DISPLAY
            )
            .define(
                THROUGHPUT_CONTROL_GLOBAL_CONTROL_RENEW_INTERVAL_IN_MS,
                ConfigDef.Type.INT,
                DEFAULT_THROUGHPUT_CONTROL_GLOBAL_CONTROL_RENEW_INTERVAL_IN_MS,
                ConfigDef.Importance.LOW,
                THROUGHPUT_CONTROL_GLOBAL_CONTROL_RENEW_INTERVAL_IN_MS_DOC,
                throughputControlGroupName,
                throughputControlGroupOrder++,
                ConfigDef.Width.MEDIUM,
                THROUGHPUT_CONTROL_GLOBAL_CONTROL_RENEW_INTERVAL_IN_MS_DISPLAY
            )
            .define(
                THROUGHPUT_CONTROL_GLOBAL_CONTROL_EXPIRE_INTERVAL_IN_MS,
                ConfigDef.Type.INT,
                DEFAULT_THROUGHPUT_CONTROL_GLOBAL_CONTROL_EXPIRE_INTERVAL_IN_MS,
                ConfigDef.Importance.LOW,
                THROUGHPUT_CONTROL_GLOBAL_CONTROL_EXPIRE_INTERVAL_IN_MS_DOC,
                throughputControlGroupName,
                throughputControlGroupOrder++,
                ConfigDef.Width.MEDIUM,
                THROUGHPUT_CONTROL_GLOBAL_CONTROL_EXPIRE_INTERVAL_IN_MS_DISPLAY
            );
    }

    public CosmosAccountConfig getAccountConfig() {
        return accountConfig;
    }

    public CosmosThroughputControlConfig getThroughputControlConfig() {
        return throughputControlConfig;
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

    public static void validateThroughputControlConfig(
        Map<String, String> connectorConfigs,
        Map<String, ConfigValue> configValueMap) {

        boolean throughputControlEnabled = Boolean.parseBoolean(connectorConfigs.get(THROUGHPUT_CONTROL_ENABLED));
        if (!throughputControlEnabled) {
            return;
        }

        // throughput control enabled, validate required configs
        // throughput control group name is required
        String throughputControlGroupName = connectorConfigs.get(THROUGHPUT_CONTROL_GROUP_NAME);
        if (StringUtils.isEmpty(throughputControlGroupName)) {
            configValueMap
                .get(THROUGHPUT_CONTROL_GROUP_NAME)
                .addErrorMessage("ThroughputControl is enabled, group name can not be null or empty");
        }

        // one of targetThroughput, targetThroughputThreshold, priorityLevel should be defined
        int targetThroughput = Integer.parseInt(connectorConfigs.get(THROUGHPUT_CONTROL_TARGET_THROUGHPUT));
        double targetThroughputThreshold = Double.parseDouble(connectorConfigs.get(THROUGHPUT_CONTROL_TARGET_THROUGHPUT_THRESHOLD));
        String priorityLevel = connectorConfigs.get(THROUGHPUT_CONTROL_PRIORITY_LEVEL);

        if (targetThroughput <= 0 && targetThroughputThreshold <= 0
            && priorityLevel.equalsIgnoreCase(CosmosPriorityLevel.NONE.getName())) {
            configValueMap
                .get(THROUGHPUT_CONTROL_TARGET_THROUGHPUT)
                .addErrorMessage("ThroughputControl is enabled, targetThroughput, targetThroughputThreshold and priorityLevel cannot all be null or empty.");
            configValueMap
                .get(THROUGHPUT_CONTROL_TARGET_THROUGHPUT_THRESHOLD)
                .addErrorMessage("ThroughputControl is enabled, targetThroughput, targetThroughputThreshold and priorityLevel cannot all be null or empty.");
            configValueMap
                .get(THROUGHPUT_CONTROL_PRIORITY_LEVEL)
                .addErrorMessage("ThroughputControl is enabled, targetThroughput, targetThroughputThreshold and priorityLevel cannot all be null or empty.");
        }

        // throughput control databaseName is required
        String throughputControlDatabaseName = connectorConfigs.get(THROUGHPUT_CONTROL_GLOBAL_CONTROL_DATABASE);
        if (StringUtils.isEmpty(throughputControlDatabaseName)) {
            configValueMap
                .get(THROUGHPUT_CONTROL_GLOBAL_CONTROL_DATABASE)
                .addErrorMessage("ThroughputControl is enabled, throughput control database name can not be null or empty");
        }

        // throughput control containerName is required
        String throughputControlContainerName = connectorConfigs.get(THROUGHPUT_CONTROL_GLOBAL_CONTROL_CONTAINER);
        if (StringUtils.isEmpty(throughputControlContainerName)) {
            configValueMap
                .get(THROUGHPUT_CONTROL_GLOBAL_CONTROL_CONTAINER)
                .addErrorMessage("ThroughputControl is enabled, throughput control container name can not be null or empty");
        }

        String throughputControlAccountEndpoint = connectorConfigs.get(THROUGHPUT_CONTROL_ACCOUNT_ENDPOINT);
        if (StringUtils.isNotEmpty(throughputControlAccountEndpoint)) {
            validateAccountAuthConfigCore(
                connectorConfigs,
                configValueMap,
                THROUGHPUT_CONTROL_ACCOUNT_TENANT_ID,
                THROUGHPUT_CONTROL_AUTH_TYPE,
                THROUGHPUT_CONTROL_ACCOUNT_KEY,
                THROUGHPUT_CONTROL_AAD_CLIENT_ID,
                THROUGHPUT_CONTROL_AAD_CLIENT_SECRET);
        }
    }

    public static void validateCosmosAccountAuthConfig(
        Map<String, String> connectorConfigs,
        Map<String, ConfigValue> configValueMap) {

        validateAccountAuthConfigCore(
            connectorConfigs,
            configValueMap,
            ACCOUNT_TENANT_ID,
            AUTH_TYPE,
            ACCOUNT_KEY,
            AAD_CLIENT_ID,
            AAD_CLIENT_SECRET);
    }

    public static void validateAccountAuthConfigCore(
        Map<String, String> connectorConfigs,
        Map<String, ConfigValue> configValueMap,
        String accountTenantIdConfig,
        String authTypeConfig,
        String accountKeyConfig,
        String clientIdConfig,
        String clientSecretConfig) {

        // separate throughput account endpoint has been defined, verify all required configs exists
        CosmosAuthTypes authType = CosmosAuthTypes.fromName(authTypeConfig);
        switch (authType) {
            case MASTER_KEY:
                String masterKey = connectorConfigs.get(accountKeyConfig);
                if (StringUtils.isEmpty(masterKey)) {
                    configValueMap
                        .get(accountKeyConfig)
                        .addErrorMessage("ThroughputControl is enabled and use different account config, masterKey is required for masterKey auth type");
                }
                break;
            case SERVICE_PRINCIPAL:
                String tenantId = connectorConfigs.get(accountTenantIdConfig);
                if (StringUtils.isEmpty(tenantId)) {
                    configValueMap
                        .get(accountTenantIdConfig)
                        .addErrorMessage("ThroughputControl is enabled and use different account config, tenantId is required for Service Principal auth type");
                }

                String clientId = connectorConfigs.get(clientIdConfig);
                if (StringUtils.isEmpty(clientId)) {
                    configValueMap
                        .get(clientIdConfig)
                        .addErrorMessage("ThroughputControl is enabled and use different account config, clientId is required for Service Principal auth type");
                }

                String clientSecret = connectorConfigs.get(clientSecretConfig);
                if (StringUtils.isEmpty(clientSecret)) {
                    configValueMap
                        .get(clientSecretConfig)
                        .addErrorMessage("ThroughputControl is enabled and use different account config, clientSecret is required for Service Principal auth type");
                }
                break;
            default:
                throw new IllegalArgumentException("AuthType " + authType + " is not supported");
        }
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

    public static class ContainersTopicMapValidator implements ConfigDef.Validator {
        private static final String INVALID_TOPIC_MAP_FORMAT =
            "Invalid entry for topic-container map. The topic-container map should be a comma-delimited "
                + "list of Kafka topic to Cosmos containers. Each mapping should be a pair of Kafka "
                + "topic and Cosmos container separated by '#'. For example: topic1#con1,topic2#con2.";

        @Override
        @SuppressWarnings("unchecked")
        public void ensureValid(String name, Object o) {
            String configValue = (String) o;
            if (StringUtils.isEmpty(configValue)) {
                return;
            }

            List<String> containerTopicMapList = convertToList(configValue);

            // validate each item should be in topic#container format
            boolean invalidFormatExists =
                containerTopicMapList
                    .stream()
                    .anyMatch(containerTopicMap ->
                        containerTopicMap
                            .split(CosmosSourceContainersConfig.CONTAINER_TOPIC_MAP_SEPARATOR)
                            .length != 2);

            if (invalidFormatExists) {
                throw new ConfigException(name, o, INVALID_TOPIC_MAP_FORMAT);
            }
        }

        @Override
        public String toString() {
            return "Containers topic map";
        }
    }

    public static class AuthTypeValidator implements ConfigDef.Validator {
        @Override
        @SuppressWarnings("unchecked")
        public void ensureValid(String name, Object o) {
            String authTypeString = (String) o;
            if (StringUtils.isEmpty(authTypeString)) {
                throw new ConfigException(name, o, "AuthType can not be empty or null");
            }

            CosmosAuthTypes authType = CosmosAuthTypes.fromName(authTypeString);
            if (authType == null) {
                throw new ConfigException(name, o, "Invalid AuthType, only allow MasterKey or ServicePrincipal");
            }
        }

        @Override
        public String toString() {
            return "AuthType. Only allow " + CosmosAuthTypes.values();
        }
    }
}
