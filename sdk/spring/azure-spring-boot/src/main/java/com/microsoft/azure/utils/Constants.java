// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.utils;

public class Constants {
    /**
     * The constant used to define the prefix of all Azure Key Vault properties.
     */
    public static final String AZURE_KEYVAULT_PREFIX = "azure.keyvault.";

    public static final String AZURE_KEYVAULT_USER_AGENT = "spring-boot-starter/" + PropertyLoader.getProjectVersion();
    public static final String AZURE_KEYVAULT_CLIENT_ID = "client-id";
    public static final String AZURE_KEYVAULT_CLIENT_KEY = "client-key";
    public static final String AZURE_KEYVAULT_TENANT_ID = "tenant-id";
    public static final String AZURE_KEYVAULT_CERTIFICATE_PATH = "certificate.path";
    public static final String AZURE_KEYVAULT_CERTIFICATE_PASSWORD = "certificate.password";
    public static final String AZURE_KEYVAULT_ENABLED = "enabled";
    public static final String AZURE_KEYVAULT_VAULT_URI = "uri";
    public static final String AZURE_KEYVAULT_REFRESH_INTERVAL = "refresh-interval";
    public static final String AZURE_KEYVAULT_SECRET_KEYS = "secret.keys";
    public static final String AZURE_KEYVAULT_PROPERTYSOURCE_NAME = "azurekv";
    public static final String AZURE_TOKEN_ACQUIRE_TIMEOUT_IN_SECONDS = "token-acquire-timeout-seconds";
    public static final String AZURE_KEYVAULT_ALLOW_TELEMETRY = "allow.telemetry";

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

    /**
     * The constant used to define the order of the key vaults you are
     * delivering (comma delimited, e.g 'myvault, myvault2').
     */
    public static final String AZURE_KEYVAULT_ORDER = "order";

    /**
     * Defines the constant for the property that enables/disables case sensitive keys.
     */
    public static final String AZURE_KEYVAULT_CASE_SENSITIVE_KEYS = "azure.keyvault.case-sensitive-keys";
}
