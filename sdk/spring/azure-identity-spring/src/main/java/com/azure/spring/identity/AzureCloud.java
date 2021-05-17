// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.identity;


/**
 * Enum to define all Azure environments: Azure, Azure China...
 */
public enum AzureCloud {

    Azure("https://login.microsoftonline.com/"),
    AzureChina("https://login.chinacloudapi.cn/"),
    AzureUSGovernment("https://login.microsoftonline.us/"),
    AzureGermany("https://login.microsoftonline.de/");

    private final String authorityHost;

    AzureCloud(String authorityHost) {
        this.authorityHost = authorityHost;
    }

    public String getAuthorityHost() {
        return this.authorityHost;
    }

}
