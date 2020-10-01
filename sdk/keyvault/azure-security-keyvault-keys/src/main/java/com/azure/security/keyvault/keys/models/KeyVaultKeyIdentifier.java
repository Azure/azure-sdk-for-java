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
     * @param keyId The key identifier to extract information from.
     * @return a new instance of {@link KeyVaultKeyIdentifier}.
     * @throws IllegalArgumentException if the given identifier is {@code null}.
     * @throws MalformedURLException if the given identifier is not a valid Key Vault Key identifier
     */
    public static KeyVaultKeyIdentifier parse(String keyId) throws IllegalArgumentException, MalformedURLException {
        if (keyId == null) {
            throw new IllegalArgumentException("keyId cannot be null");
        }

        URL url = new URL(keyId);
        // We expect an identifier with either 2 or 3 path segments: collection + name [+ version]
        String[] pathSegments = url.getPath().split("/");

        if ((pathSegments.length != 3 && pathSegments.length != 4) || !"keys".equals(pathSegments[1])) {
            throw new IllegalArgumentException("keyId is not a valid Key Vault Key identifier");
        }

        return new KeyVaultKeyIdentifier(keyId, url.getProtocol() + "://" + url.getHost(), pathSegments[2],
            pathSegments.length == 4 ? pathSegments[3] : null);
    }
}
