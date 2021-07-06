// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core;

import java.util.Optional;

/**
 * Used internally. Constants in the Azure Spring Boot Core library.
 */
public class Utils {

    public static String toAuthorityHost(String azureEnvironment) {
        switch (azureEnvironment) {
            case "AzureChina":
                return Constants.AZURE_CHINA_AUTHORITY_HOST;
            case "AzureGermany":
                return Constants.AZURE_GERMANY_AUTHORITY_HOST;
            case "AzureUSGovernment":
                return Constants.AZURE_US_GOVERNMENT_AUTHORITY_HOST;
            default:
                return Constants.AZURE_GLOBAL_AUTHORITY_HOST;
        }
    }
}
