// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.models;

import com.azure.security.keyvault.keys.KeyAsyncClient;
import com.azure.security.keyvault.keys.KeyClient;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Information about a {@link KeyVaultKey} parsed from the key URL. You can use this information when calling methods
 * of {@link KeyClient} or {@link KeyAsyncClient}.
 */
public final class KeyVaultKeyIdentifier {
    private final String keyId, vaultUrl, name, version;

    private KeyVaultKeyIdentifier(String keyId, String vaultUrl, String name, String version) {
        this.keyId = keyId;
        this.vaultUrl = vaultUrl;
        this.name = name;
        this.version = version;
    }

    /**
     * Gets the key identifier used to create this object.
     *
     * @return The key identifier.
     */
    public String getKeyId() {
        return keyId;
    }

    /**
     * Gets the URL of the Key Vault.
     *
     * @return The Key Vault URL.
     */
    public String getVaultUrl() {
        return vaultUrl;
    }

    /**
     * Gets the name of the key.
     *
     * @return The key name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the optional version of the key.
     *
     * @return The key version.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Create a new {@link KeyVaultKeyIdentifier} from a given key identifier.
     *
     * <p>Valid examples are:
     *
     * <ul>
     *     <li>https://{key-vault-name}.vault.azure.net/keys/{key-name}</li>
     *     <li>https://{key-vault-name}.vault.azure.net/keys/{key-name}/pending</li>
     *     <li>https://{key-vault-name}.vault.azure.net/keys/{key-name}/{unique-version-id}</li>
     *     <li>https://{key-vault-name}.vault.azure.net/deletedkeys/{deleted-key-name}</li>
     * </ul>
     *
     * @param keyId The key identifier to extract information from.
     * @return a new instance of {@link KeyVaultKeyIdentifier}.
     * @throws IllegalArgumentException if the given identifier is {@code null} or an invalid Key Vault Key identifier.
     */
    public static KeyVaultKeyIdentifier parse(String keyId) throws IllegalArgumentException {
        if (keyId == null) {
            throw new IllegalArgumentException("keyId cannot be null");
        }

        try {
            final URL url = new URL(keyId);
            // We expect an identifier with either 2 or 3 path segments: collection + name [+ version]
            final String[] pathSegments = url.getPath().split("/");

            if ((pathSegments.length != 3 && pathSegments.length != 4) // More or less segments in the URI than expected.
                || !"https".equals(url.getProtocol()) // Invalid protocol.
                || (!"keys".equals(pathSegments[1]) && !"deletedkeys".equals(pathSegments[1])) // Invalid collection.
                || ("deletedkeys".equals(pathSegments[1]) && pathSegments.length == 4)) { // Deleted items do not include a version.
                throw new IllegalArgumentException("keyId is not a valid Key Vault Key identifier");
            }

            final String vaultUrl = String.format("%s://%s", url.getProtocol(), url.getHost());
            final String keyName = pathSegments[2];
            final String keyVersion = pathSegments.length == 4 ? pathSegments[3] : null;

            return new KeyVaultKeyIdentifier(keyId, vaultUrl, keyName, keyVersion);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Could not parse keyId", e);
        }
    }
}
