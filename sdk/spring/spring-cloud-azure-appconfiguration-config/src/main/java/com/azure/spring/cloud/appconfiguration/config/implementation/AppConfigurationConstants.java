// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

/**
 * Constants used for processing Azure App Configuration Config info.
 */
public class AppConfigurationConstants {

    /**
     * App Configurations Feature Flag Content Type
     */
    public static final String FEATURE_FLAG_CONTENT_TYPE = "application/vnd.microsoft.appconfig.ff+json;charset=utf-8";

    /**
     * App Configurations Key Vault Reference Content Type
     */
    public static final String KEY_VAULT_CONTENT_TYPE = "application/vnd.microsoft.appconfig.keyvaultref+json;charset=utf-8";

    /**
     * Feature Management Key Prefix
     */
    public static final String FEATURE_MANAGEMENT_KEY = "feature-management.";

    /**
     * Feature Flag Prefix
     */
    public static final String FEATURE_FLAG_PREFIX = ".appconfig.featureflag/";

    /**
     * Separator for multiple labels.
     */
    public static final String LABEL_SEPARATOR = ",";

    /**
     * Key for returning all feature flags
     */
    public static final String FEATURE_STORE_WATCH_KEY = FEATURE_FLAG_PREFIX + "*";

    /**
     * Constant for tracing if the library is being used with a dev profile.
     */
    public static final String DEV_ENV_TRACING = "Dev";

    /**
     * Constant for tracing if Key Vault is configured for use.
     */
    public static final String KEY_VAULT_CONFIGURED_TRACING = "UsesKeyVault";

    /**
     * Constant for tracing for Replica Count
     */
    public static final String REPLICA_COUNT = "ReplicaCount";
    
    /**
     * Http Header User Agent
     */
    public static final String USER_AGENT_TYPE = "User-Agent";

    /**
     * Http Header Correlation Context
     */
    public static final String CORRELATION_CONTEXT = "Correlation-Context";

    /**
     * Configuration Label for loading configurations with no label.
     */
    public static final String EMPTY_LABEL = "\0";

    public static final String USERS = "users";

    public static final String USERS_CAPS = "Users";

    public static final String AUDIENCE = "Audience";

    public static final String GROUPS = "groups";

    public static final String GROUPS_CAPS = "Groups";

    public static final String TARGETING_FILTER = "targetingFilter";

    public static final String DEFAULT_ROLLOUT_PERCENTAGE = "defaultRolloutPercentage";

    public static final String DEFAULT_ROLLOUT_PERCENTAGE_CAPS = "DefaultRolloutPercentage";
}
