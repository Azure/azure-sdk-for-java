// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.cloud.config;

/**
 * Constants used for processing Azure App Configuration Config info.
 */
public class Constants {
    /**
     * App Configurations Feature Flag Content Type
     */
    public static final String FEATURE_FLAG_CONTENT_TYPE = "application/vnd.microsoft.appconfig.ff+json;charset=utf-8";

    /**
     * App Configurations Key Vault Reference Content Type
     */
    public static final String KEY_VAULT_CONTENT_TYPE =
            "application/vnd.microsoft.appconfig.keyvaultref+json;charset=utf-8";

    /**
     * Feature Management Key Prefix
     */
    public static final String FEATURE_MANAGEMENT_KEY = "feature-management.featureManagement";

    /**
     * Feature Flag Prefix
     */
    public static final String FEATURE_FLAG_PREFIX = ".appconfig.featureflag/";

    /**
     * Configuration suffix
     */
    public static final String CONFIGURATION_SUFFIX = "_configuration";

    /**
     * Feature suffix
     */
    public static final String FEATURE_SUFFIX = "_feature";

    /**
     * Feature Store Prefix
     */
    public static final String FEATURE_STORE_SUFFIX = ".appconfig";

    /**
     * Key for returning all feature flags
     */
    public static final String FEATURE_STORE_WATCH_KEY = FEATURE_STORE_SUFFIX + "*";
}
