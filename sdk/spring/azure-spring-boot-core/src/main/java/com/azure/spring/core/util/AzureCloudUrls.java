// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.util;

import com.azure.identity.AzureAuthorityHosts;

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

    public static String toAuthorityHost(String azureEnvironment) {
        switch (azureEnvironment) {
            case "AzureChina":
                return AzureAuthorityHosts.AZURE_CHINA;
            case "AzureGermany":
                return AzureAuthorityHosts.AZURE_GERMANY;
            case "AzureUSGovernment":
                return AzureAuthorityHosts.AZURE_GOVERNMENT;
            default:
                return AzureAuthorityHosts.AZURE_PUBLIC_CLOUD;
        }
    }
}
