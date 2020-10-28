// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.context.core.storage;

import com.microsoft.azure.AzureEnvironment;

/**
 * The builder class to build the storage endpoint string.
 */
public class StorageEndpointStringBuilder {

    public static String buildBlobEndpoint(String storageAccount, AzureEnvironment azureEnvironment,
            boolean isSecureTransfer) {
        String scheme = isSecureTransfer ? "https://" : "http://";
        return scheme + storageAccount + ".blob" + azureEnvironment.storageEndpointSuffix();
    }

    public static String buildSharesEndpoint(String storageAccount, AzureEnvironment azureEnvironment,
            boolean isSecureTransfer) {
        String scheme = isSecureTransfer ? "https://" : "http://";
        return scheme + storageAccount + ".file" + azureEnvironment.storageEndpointSuffix();
    }
}
