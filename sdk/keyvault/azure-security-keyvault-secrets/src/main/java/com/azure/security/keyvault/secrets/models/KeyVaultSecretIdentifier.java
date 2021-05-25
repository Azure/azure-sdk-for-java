// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets.models;

import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClient;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Information about a {@link KeyVaultSecret} parsed from the secret URL. You can use this information when calling
 * methods of {@link SecretClient} or {@link SecretAsyncClient}.
 */
public final class KeyVaultSecretIdentifier {
    private final String sourceId, vaultUrl, name, version;

    /**
     * Create a new {@link KeyVaultSecretIdentifier} from a given secret identifier.
     *
     * <p>Some examples:
     *
     * <ul>
     *     <li>https://{key-vault-name}.vault.azure.net/secrets/{secret-name}</li>
     *     <li>https://{key-vault-name}.vault.azure.net/secrets/{secret-name}/pending</li>
     *     <li>https://{key-vault-name}.vault.azure.net/secrets/{secret-name}/{unique-version-id}</li>
     *     <li>https://{key-vault-name}.vault.azure.net/deletedsecrets/{deleted-secret-name}</li>
     * </ul>
     *
     * @param id The identifier to extract information from.
     *
     * @throws IllegalArgumentException If {@code secretId} is an invalid Key Vault Secret identifier.
     * @throws NullPointerException If {@code secretId} is {@code null}.
     */
    public KeyVaultSecretIdentifier(String id) {
        if (id == null) {
            throw new NullPointerException("'secretId' cannot be null.");
        }

        try {
            final URL url = new URL(id);
            // We expect an identifier with either 2 or 3 path segments: collection + name [+ version]
            final String[] pathSegments = url.getPath().split("/");

            if ((pathSegments.length != 3 && pathSegments.length != 4) // More or less segments in the URI than expected.
                || !"https".equals(url.getProtocol()) // Invalid protocol.
                || ("deletedsecrets".equals(pathSegments[1]) && pathSegments.length == 4)) { // Deleted items do not include a version.

                throw new IllegalArgumentException("'secretId' is not a valid Key Vault Secret identifier.");
            }

            this.sourceId = id;
            this.vaultUrl = String.format("%s://%s", url.getProtocol(), url.getHost());
            this.name = pathSegments[2];
            this.version = pathSegments.length == 4 ? pathSegments[3] : null;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("'secretId' is not a valid Key Vault Secret identifier.", e);
        }
    }

    /**
     * Gets the key identifier used to create this object
     *
     * @return The secret identifier.
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
     * Gets the name of the secret.
     *
     * @return The secret name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the optional version of the secret.
     *
     * @return The secret version.
     */
    public String getVersion() {
        return version;
    }
}
