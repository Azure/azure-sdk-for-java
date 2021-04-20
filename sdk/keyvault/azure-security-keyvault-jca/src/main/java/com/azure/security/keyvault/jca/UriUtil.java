// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

/**
 * Constants used for Key Vault related URLs.
 */
public class UriUtil {

    public static final String KEY_VAULT_BASE_URI_GLOBAL = "https://vault.azure.net";
    public static final String KEY_VAULT_BASE_URI_CN = "https://vault.azure.cn";
    public static final String KEY_VAULT_BASE_URI_US = "https://vault.usgovcloudapi.net";
    public static final String KEY_VAULT_BASE_URI_DE = "https://vault.microsoftazure.de";

    public static final String AAD_LOGIN_URI_GLOBAL = "https://login.microsoftonline.com/";
    public static final String AAD_LOGIN_URI_CN = "https://login.partner.microsoftonline.cn/";
    public static final String AAD_LOGIN_URI_US = "https://login.microsoftonline.us/";
    public static final String AAD_LOGIN_URI_DE = "https://login.microsoftonline.de/";

    static String getAADLoginURIByKeyVaultBaseUri(String keyVaultBaseUri) {
        String aadAuthenticationUrl;
        switch (keyVaultBaseUri) {
            case KEY_VAULT_BASE_URI_GLOBAL :
                aadAuthenticationUrl = AAD_LOGIN_URI_GLOBAL;
                break;
            case KEY_VAULT_BASE_URI_CN :
                aadAuthenticationUrl = AAD_LOGIN_URI_CN;
                break;
            case KEY_VAULT_BASE_URI_US :
                aadAuthenticationUrl = AAD_LOGIN_URI_US;
                break;
            case KEY_VAULT_BASE_URI_DE:
                aadAuthenticationUrl = AAD_LOGIN_URI_DE;
                break;
            default:
                throw new IllegalArgumentException("Property of azure.keyvault.uri is illegal.");
        }
        return aadAuthenticationUrl;
    }
}
