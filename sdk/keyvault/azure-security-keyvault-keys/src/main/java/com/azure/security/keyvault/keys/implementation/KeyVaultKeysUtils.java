// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.keys.implementation;

import com.azure.core.http.HttpPipeline;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.security.keyvault.keys.KeyServiceVersion;
import com.azure.security.keyvault.keys.cryptography.CryptographyClientBuilder;
import com.azure.security.keyvault.keys.cryptography.CryptographyServiceVersion;

/**
 * Utility class for KeyVault Keys.
 */
public final class KeyVaultKeysUtils {
    private static final ClientLogger LOGGER = new ClientLogger(KeyVaultKeysUtils.class);

    /**
     * Creates a {@link CryptographyClientBuilder} based on the values passed from a Keys service client.
     *
     * @param keyName The name of the key.
     * @param keyVersion The version of the key.
     * @param vaultUrl The URL of the KeyVault.
     * @param httpPipeline The HttpPipeline to use for the CryptographyClient.
     * @param serviceVersion The KeyServiceVersion of the service.
     * @return A new {@link CryptographyClientBuilder} with the values passed from a Keys service client.
     */
    public static CryptographyClientBuilder getCryptographyClientBuilder(String keyName, String keyVersion,
        String vaultUrl, HttpPipeline httpPipeline, KeyServiceVersion serviceVersion) {
        if (CoreUtils.isNullOrEmpty(keyName)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'keyName' cannot be null or empty."));
        }

        return new CryptographyClientBuilder()
            .keyIdentifier(generateKeyId(keyName, keyVersion, vaultUrl))
            .pipeline(httpPipeline)
            .serviceVersion(CryptographyServiceVersion.valueOf(serviceVersion.name()));
    }

    /**
     * Generates a KeyVault Key ID from the name and version of the key and the KeyVault URL.
     *
     * @param keyName The name of the key.
     * @param keyVersion The version of the key.
     * @param vaultUrl The URL of the KeyVault.
     * @return The KeyVault Key ID.
     */
    private static String generateKeyId(String keyName, String keyVersion, String vaultUrl) {
        StringBuilder stringBuilder = new StringBuilder(vaultUrl);

        if (!vaultUrl.endsWith("/")) {
            stringBuilder.append("/");
        }

        stringBuilder.append("keys/").append(keyName);

        if (!CoreUtils.isNullOrEmpty(keyVersion)) {
            stringBuilder.append("/").append(keyVersion);
        }

        return stringBuilder.toString();
    }
}
