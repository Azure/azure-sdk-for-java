// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.utils;

/**
 * Util class for Azure urls
 */
public class AzureCloudUrls {
    /**
     * Gets the base URL.
     *
     * @param cloudType the cloud type
     * @return the base URL
     */
    public static String getBaseUrl(String cloudType) {
        return cloudType.equals("Global") ? "https://login.microsoftonline.com/"
            : "https://login.partner.microsoftonline.cn/";
    }

    /**
     * Gets the graph base URL.
     *
     * @param cloudType the cloud type
     * @return the graph base URL
     */
    public static String getGraphBaseUrl(String cloudType) {
        return cloudType.equals("Global") ? "https://graph.microsoft.com/"
            : "https://microsoftgraph.chinacloudapi.cn/";
    }

    /**
     * Gets the service management base URL.
     *
     * @param cloudType the cloud type
     * @return the service management base URL
     */
    public static String getServiceManagementBaseUrl(String cloudType) {
        return cloudType.equals("Global") ? "https://management.azure.com/"
            : "https://management.chinacloudapi.cn/";
    }
}
