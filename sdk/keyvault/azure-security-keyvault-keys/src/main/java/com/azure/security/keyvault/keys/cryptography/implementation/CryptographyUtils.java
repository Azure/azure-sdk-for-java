// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.keys.cryptography.implementation;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.security.keyvault.keys.implementation.KeyClientImpl;
import com.azure.security.keyvault.keys.implementation.SecretMinClientImpl;
import com.azure.security.keyvault.keys.models.JsonWebKey;
import com.azure.security.keyvault.keys.models.KeyOperation;
import com.azure.security.keyvault.keys.models.KeyType;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static com.azure.security.keyvault.keys.models.KeyType.EC;
import static com.azure.security.keyvault.keys.models.KeyType.EC_HSM;
import static com.azure.security.keyvault.keys.models.KeyType.OCT;
import static com.azure.security.keyvault.keys.models.KeyType.OCT_HSM;
import static com.azure.security.keyvault.keys.models.KeyType.RSA;
import static com.azure.security.keyvault.keys.models.KeyType.RSA_HSM;

/**
 * Utility methods for the Cryptography portion of KeyVault Keys.
 */
public final class CryptographyUtils {
    public static final String SECRETS_COLLECTION = "secrets";

    public static List<String> unpackAndValidateId(String keyId, ClientLogger logger) {
        if (CoreUtils.isNullOrEmpty(keyId)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'keyId' cannot be null or empty."));
        }

        try {
            URL url = new URL(keyId);
            String[] tokens = url.getPath().split("/");
            String vaultUrl = url.getProtocol() + "://" + url.getHost();

            if (url.getPort() != -1) {
                vaultUrl += ":" + url.getPort();
            }

            String keyCollection = (tokens.length >= 2 ? tokens[1] : null);
            String keyName = (tokens.length >= 3 ? tokens[2] : null);
            String keyVersion = (tokens.length >= 4 ? tokens[3] : null);

            if (CoreUtils.isNullOrEmpty(vaultUrl)) {
                throw logger.logExceptionAsError(
                    new IllegalArgumentException("Key endpoint in key identifier is invalid."));
            } else if (CoreUtils.isNullOrEmpty(keyName)) {
                throw logger.logExceptionAsError(
                    new IllegalArgumentException("Key name in key identifier is invalid."));
            }

            return Arrays.asList(vaultUrl, keyCollection, keyName, keyVersion);
        } catch (MalformedURLException e) {
            throw logger.logExceptionAsError(new IllegalArgumentException("The key identifier is malformed.", e));
        }
    }

    public static LocalKeyCryptographyClient initializeCryptoClient(JsonWebKey jsonWebKey, KeyClientImpl keyClient,
        SecretMinClientImpl secretClient, String vaultUrl, String keyCollection, String keyName, String keyVersion,
        ClientLogger logger) {
        if (!KeyType.values().contains(jsonWebKey.getKeyType())) {
            throw logger.logExceptionAsError(new IllegalArgumentException(String.format(
                "The JSON Web Key type: %s is not supported.", jsonWebKey.getKeyType().toString())));
        }

        try {
            if (jsonWebKey.getKeyType().equals(RSA) || jsonWebKey.getKeyType().equals(RSA_HSM)) {
                return new RsaKeyCryptographyClient(jsonWebKey, keyClient, secretClient, vaultUrl, keyCollection,
                    keyName, keyVersion);
            } else if (jsonWebKey.getKeyType().equals(EC) || jsonWebKey.getKeyType().equals(EC_HSM)) {
                return new EcKeyCryptographyClient(jsonWebKey, keyClient, secretClient, vaultUrl, keyCollection,
                    keyName, keyVersion);
            } else if (jsonWebKey.getKeyType().equals(OCT) || jsonWebKey.getKeyType().equals(OCT_HSM)) {
                return new AesKeyCryptographyClient(jsonWebKey, keyClient, secretClient, vaultUrl, keyCollection,
                    keyName, keyVersion);
            }
        } catch (RuntimeException e) {
            throw logger.logExceptionAsError(new RuntimeException("Could not initialize local cryptography client.",
                e));
        }

        // Should not reach here.
        return null;
    }

    public static boolean checkKeyPermissions(List<KeyOperation> operations, KeyOperation keyOperation) {
        return operations.contains(keyOperation);
    }

    /*
     * Determines whether the key is valid and of required size.
     *
     * @param key The key to be checked.
     * @param keySizeInBytes The minimum size required for the key
     */
    static void validate(byte[] key, int keySizeInBytes) {
        if (key == null) {
            throw new IllegalArgumentException("key must not be null");
        }

        if (key.length < keySizeInBytes) {
            throw new IllegalArgumentException(String.format("key must be at least %d bits long", keySizeInBytes << 3));
        }
    }

    /*
     * Compares two byte arrays in constant time.
     *
     * @param self
     *      The first byte array to compare
     * @param other
     *      The second byte array to compare
     * @return
     *      True if the two byte arrays are equal.
     */
    static boolean sequenceEqualConstantTime(byte[] self, byte[] other) {
        if (self == null) {
            throw new IllegalArgumentException("self");
        }

        if (other == null) {
            throw new IllegalArgumentException("other");
        }

        // Constant time comparison of two byte arrays
        long difference = (self.length & 0xffffffffL) ^ (other.length & 0xffffffffL);

        for (int i = 0; i < self.length && i < other.length; i++) {
            difference |= (self[i] ^ other[i]) & 0xffffffffL;
        }

        return difference == 0;
    }
}
