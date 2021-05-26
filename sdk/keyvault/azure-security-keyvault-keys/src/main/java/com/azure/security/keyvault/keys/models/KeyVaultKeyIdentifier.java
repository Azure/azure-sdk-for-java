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
    private final String sourceId, vaultUrl, name, version;

    /**
     * Create a new {@link KeyVaultKeyIdentifier} from a given key identifier.
     *
     * <p>Some examples:
     *
     * <ul>
     *     <li>https://{key-vault-name}.vault.azure.net/keys/{key-name}</li>
     *     <li>https://{key-vault-name}.vault.azure.net/keys/{key-name}/pending</li>
     *     <li>https://{key-vault-name}.vault.azure.net/keys/{key-name}/{unique-version-id}</li>
     *     <li>https://{key-vault-name}.vault.azure.net/deletedkeys/{deleted-key-name}</li>
     * </ul>
     *
     * @param id The identifier to extract information from.
     *
     * @throws IllegalArgumentException If {@code keyId} is an invalid Key Vault Key identifier.
     * @throws NullPointerException If {@code keyId} is {@code null}.
     */
    public KeyVaultKeyIdentifier(String id) {
        if (id == null) {
            throw new NullPointerException("'keyId' cannot be null.");
        }

        try {
            final URL url = new URL(id);
            // We expect an identifier with either 2 or 3 path segments: collection + name [+ version]
            final String[] pathSegments = url.getPath().split("/");

            if ((pathSegments.length != 3 && pathSegments.length != 4) // More or less segments in the URI than expected.
                || !"https".equals(url.getProtocol()) // Invalid protocol.
                || ("deletedkeys".equals(pathSegments[1]) && pathSegments.length == 4)) { // Deleted items do not include a version.

                throw new IllegalArgumentException("'keyId' is not a valid Key Vault Key identifier.");
            }

            this.sourceId = id;
            this.vaultUrl = String.format("%s://%s", url.getProtocol(), url.getHost());
            this.name = pathSegments[2];
            this.version = pathSegments.length == 4 ? pathSegments[3] : null;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("'keyId' is not a valid Key Vault Key identifier.", e);
        }
    }

    /**
     * Gets the key identifier used to create this object.
     *
     * @return The key identifier.
     */
    public String getSourceId() {
        return sourceId;
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
}
