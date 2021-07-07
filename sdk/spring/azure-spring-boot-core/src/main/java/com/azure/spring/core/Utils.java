// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core;

import com.azure.identity.AzureAuthorityHosts;

/**
 * Used internally. Constants in the Azure Spring Boot Core library.
 */
public class Utils {

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
