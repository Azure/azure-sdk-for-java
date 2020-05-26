// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.models.AzureActiveDirectoryApplicationCredentials;

/**
 * A converter between
 * {@link com.azure.search.documents.implementation.models.AzureActiveDirectoryApplicationCredentials} and
 * {@link AzureActiveDirectoryApplicationCredentials}.
 */
public final class AzureActiveDirectoryApplicationCredentialsConverter {
    /**
     * Maps from {@link com.azure.search.documents.implementation.models.AzureActiveDirectoryApplicationCredentials} to
     * {@link AzureActiveDirectoryApplicationCredentials}.
     */
    public static AzureActiveDirectoryApplicationCredentials map(com.azure.search.documents.implementation.models.AzureActiveDirectoryApplicationCredentials obj) {
        if (obj == null) {
            return null;
        }
        AzureActiveDirectoryApplicationCredentials azureActiveDirectoryApplicationCredentials =
            new AzureActiveDirectoryApplicationCredentials();

        String applicationId = obj.getApplicationId();
        azureActiveDirectoryApplicationCredentials.setApplicationId(applicationId);

        String applicationSecret = obj.getApplicationSecret();
        azureActiveDirectoryApplicationCredentials.setApplicationSecret(applicationSecret);
        return azureActiveDirectoryApplicationCredentials;
    }

    /**
     * Maps from {@link AzureActiveDirectoryApplicationCredentials} to
     * {@link com.azure.search.documents.implementation.models.AzureActiveDirectoryApplicationCredentials}.
     */
    public static com.azure.search.documents.implementation.models.AzureActiveDirectoryApplicationCredentials map(AzureActiveDirectoryApplicationCredentials obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.AzureActiveDirectoryApplicationCredentials azureActiveDirectoryApplicationCredentials = new com.azure.search.documents.implementation.models.AzureActiveDirectoryApplicationCredentials();

        String applicationId = obj.getApplicationId();
        azureActiveDirectoryApplicationCredentials.setApplicationId(applicationId);

        String applicationSecret = obj.getApplicationSecret();
        azureActiveDirectoryApplicationCredentials.setApplicationSecret(applicationSecret);
        return azureActiveDirectoryApplicationCredentials;
    }

    private AzureActiveDirectoryApplicationCredentialsConverter() {
    }
}
