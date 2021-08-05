// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.utils;

/**
 * Util class for Azure urls
 */
public class AzureCloudUrls {
    public static String getBaseUrl(String cloudType) {
        return cloudType.equals("Global") ? "https://login.microsoftonline.com/"
            : "https://login.partner.microsoftonline.cn/";
    }

    public static String getGraphBaseUrl(String cloudType) {
        return cloudType.equals("Global") ? "https://graph.microsoft.com/"
            : "https://microsoftgraph.chinacloudapi.cn/";
    }

    public static String getServiceManagementBaseUrl(String cloudType) {
        return cloudType.equals("Global") ? "https://management.azure.com/"
            : "https://management.chinacloudapi.cn/";
    }
}
