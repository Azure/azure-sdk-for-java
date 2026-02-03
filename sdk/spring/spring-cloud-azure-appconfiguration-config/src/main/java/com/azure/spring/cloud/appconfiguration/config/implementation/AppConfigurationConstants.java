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
     * Feature Flag Prefix
     */
    public static final String FEATURE_FLAG_PREFIX = ".appconfig.featureflag/";

    /**
     * Separator for multiple labels.
     */
    public static final String LABEL_SEPARATOR = ",";

    /**
     * The key filter for selecting all feature flags.
     */
    public static final String SELECT_ALL_FEATURE_FLAGS = FEATURE_FLAG_PREFIX + "*";

    /**
     * Constant for tracing if the library is being used with a dev profile.
     */
    public static final String DEV_ENV_TRACING = "Dev";

    /**
     * Constant for tracing if Key Vault is configured for use.
     */
    public static final String KEY_VAULT_CONFIGURED_TRACING = "UsesKeyVault";
    
    /**
     * Constant for tracing if Push Refresh is enabled for the store. 
     */
    public static final String PUSH_REFRESH = "PushRefresh";

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

    public static final String CONDITIONS = "conditions";

    public static final String TELEMETRY = "telemetry";

    public static final String DEFAULT_REQUIREMENT_TYPE = "Any";

    public static final String REQUIREMENT_TYPE_SERVICE = "requirement_type";

    public static final String FEATURE_FLAG_REFERENCE = "FeatureFlagReference";

    public static final String E_TAG = "ETag";
}
