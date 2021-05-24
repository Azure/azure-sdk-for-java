// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

/**
 * Enum to define the authority host uri for public Azure Cloud environments: Azure, Azure China, Azure US Government 
 * and Azure Germany.
 */
public enum AzureAuthorityHost {

    Azure("https://login.microsoftonline.com/"),
    AzureChina("https://login.chinacloudapi.cn/"),
    AzureUSGovernment("https://login.microsoftonline.us/"),
    AzureGermany("https://login.microsoftonline.de/");

    private final String authorityHost;

    AzureAuthorityHost(String authorityHost) {
        this.authorityHost = authorityHost;
    }

    public String getAuthorityHost() {
        return this.authorityHost;
    }
}
