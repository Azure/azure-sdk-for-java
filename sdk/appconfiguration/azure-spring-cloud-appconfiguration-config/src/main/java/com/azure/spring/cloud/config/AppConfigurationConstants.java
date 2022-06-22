// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config;

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
     * App Configurations Dynamic Feature Content Type
     */
    public static final String DYNAMIC_FEATURE_CONTENT_TYPE = "application/vnd.microsoft.appconfig.df+json;charset=utf-8";
    
    /**
     * Environment variable name for the feature management schema
     */
    public static final String AZURE_APP_CONFIGURATION_FEATURE_MANAGEMENT_SCHEMA_VERSION = "AZURE_APP_CONFIGURATION_FEATURE_MANAGEMENT_SCHEMA_VERSION";
    
    /**
     * Feature Management Key Prefix V1 Schema
     */
    public static final String FEATURE_MANAGEMENT_KEY_V1 = "feature-management";

    /**
     * Feature Management Key Prefix V2 Schema
     */
    public static final String FEATURE_MANAGEMENT_KEY_V2 = "feature-management.feature-flags";
    
    /**
     * Value of Feature Management Schema V1
     */
    public static final Integer FEATURE_MANAGEMENT_V1_SCHEMA = 1;
    
    /**
     * Value of Feature Management Schema V2
     */
    public static final Integer FEATURE_MANAGEMENT_V2_SCHEMA = 2;
    
    /**
     * Prefix for Dynamic Feature property keys names
     */
    public static final String DYNAMIC_FEATURE_KEY = "feature-management.dynamic-features.";

    /**
     * Feature Flag Prefix
     */
    public static final String FEATURE_FLAG_PREFIX = ".appconfig.featureflag/";

    /**
     * Feature Store Prefix
     */
    public static final String FEATURE_STORE_SUFFIX = ".appconfig";

    /**
     * Key for returning all feature flags
     */
    public static final String FEATURE_STORE_WATCH_KEY = FEATURE_STORE_SUFFIX + "*";
    
    /**
     * Constant for tracing if the library is being used with a dev profile.
     */
    public static final String DEV_ENV_TRACING = "Dev";
    
    /**
     * Constant for tracing if Key Vault is configured for use.
     */
    public static final String KEY_VAULT_CONFIGURED_TRACING = "UsesKeyVault";
    
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
}
