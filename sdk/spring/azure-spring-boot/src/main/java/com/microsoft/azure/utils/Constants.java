// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.utils;

public class Constants {

    public static final String AZURE_KEYVAULT_PROPERTYSOURCE_NAME = "azurekv";

    public static final long DEFAULT_REFRESH_INTERVAL_MS = 1800000L;
    public static final long TOKEN_ACQUIRE_TIMEOUT_SECS = 60L;

    // for the User-Agent header set in track2 SDKs
    private static final String SNAPSHOT_VERSION = "snapshot";
    private static final String AZURE = "az";
    private static final String SPRING = "sp";
    private static final String KEY_VAULT = "kv";

    public static final String SPRINGBOOT_VERSION = SNAPSHOT_VERSION;
    // the max length of application id is 24
    public static final String SPRINGBOOT_KEY_VAULT_APPLICATION_ID =
            String.join("-", AZURE, SPRING, KEY_VAULT) + "/" + SPRINGBOOT_VERSION;
}
