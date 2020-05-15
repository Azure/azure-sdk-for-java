package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.AzureActiveDirectoryApplicationCredentials;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if
 * {@link com.azure.search.documents.models.AzureActiveDirectoryApplicationCredentials} and
 * {@link AzureActiveDirectoryApplicationCredentials} mismatch.
 */
public final class AzureActiveDirectoryApplicationCredentialsConverter {
    public static AzureActiveDirectoryApplicationCredentials convert(com.azure.search.documents.models.AzureActiveDirectoryApplicationCredentials obj) {
        return DefaultConverter.convert(obj, AzureActiveDirectoryApplicationCredentials.class);
    }

    public static com.azure.search.documents.models.AzureActiveDirectoryApplicationCredentials convert(AzureActiveDirectoryApplicationCredentials obj) {
        return DefaultConverter.convert(obj,
            com.azure.search.documents.models.AzureActiveDirectoryApplicationCredentials.class);
    }
}
