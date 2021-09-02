// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config;

/**
 * Constants used for processing Azure App Configuration Config info.
 */
public class Constants {

    public static final String FEATURE_FLAG_CONTENT_TYPE = "application/vnd.microsoft.appconfig.ff+json;charset=utf-8";

    public static final String KEY_VAULT_CONTENT_TYPE =
        "application/vnd.microsoft.appconfig.keyvaultref+json;charset=utf-8";

    public static final String FEATURE_MANAGEMENT_KEY = "feature-management.featureManagement";

    public static final String FEATURE_FLAG_PREFIX = ".appconfig.featureflag/";

    public static final String FEATURE_STORE_SUFFIX = ".appconfig";

    public static final String FEATURE_STORE_WATCH_KEY = FEATURE_STORE_SUFFIX + "*";
}
