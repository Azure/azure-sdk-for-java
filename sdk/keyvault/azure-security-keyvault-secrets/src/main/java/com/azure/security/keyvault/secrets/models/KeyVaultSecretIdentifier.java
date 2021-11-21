// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.logging.ClientLogger;
import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClient;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Information about a {@link KeyVaultSecret} parsed from the secret URL. You can use this information when calling
 * methods of {@link SecretClient} or {@link SecretAsyncClient}.
 */
@Immutable
public final class KeyVaultSecretIdentifier {
    private final ClientLogger logger = new ClientLogger(KeyVaultSecretIdentifier.class);
    private final String sourceId, vaultUrl, name, version;

    /**
     * Create a new {@link KeyVaultSecretIdentifier} from a given Key Vault identifier.
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
     * @param sourceId The identifier to extract information from.
     *
     * @throws IllegalArgumentException If {@code sourceId} is an invalid Key Vault Secret identifier.
     * @throws NullPointerException If {@code sourceId} is {@code null}.
     */
    public KeyVaultSecretIdentifier(String sourceId) {
        if (sourceId == null) {
            throw logger.logExceptionAsError(new NullPointerException("'sourceId' cannot be null."));
        }

        try {
            final URL url = new URL(sourceId);
            // We expect an sourceId with either 3 or 4 path segments: key vault + collection + name + "pending"/version
            final String[] pathSegments = url.getPath().split("/");

            // More or less segments in the URI than expected.
            if (pathSegments.length != 3 && pathSegments.length != 4) {
                throw logger.logExceptionAsError(
                    new IllegalArgumentException("'sourceId' is not a valid Key Vault identifier."));
            }

            this.sourceId = sourceId;
            this.vaultUrl = String.format("%s://%s", url.getProtocol(), url.getHost());
            this.name = pathSegments[2];
            this.version = pathSegments.length == 4 ? pathSegments[3] : null;
        } catch (MalformedURLException e) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("'sourceId' is not a valid Key Vault identifier.", e));
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
