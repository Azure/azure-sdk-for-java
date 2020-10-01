// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets.models;

import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClient;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Information about a {@link KeyVaultSecret} parsed from the key URL. You can use this information when calling
 * methods of {@link SecretClient} or {@link SecretAsyncClient}.
 */
public class KeyVaultSecretIdentifier {
    private final String secretId, vaultUrl, name, version;

    private KeyVaultSecretIdentifier(String secretId, String vaultUrl, String name, String version) {
        this.secretId = secretId;
        this.vaultUrl = vaultUrl;
        this.name = name;
        this.version = version;
    }

    /**
     * Gets the key identifier used to create this object
     *
     * @return The secret identifier.
     */
    public String getSecretId() {
        return secretId;
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

    /**
     * Create a new {@link KeyVaultSecretIdentifier} from a given secret identifier.
     *
     * @param secretId The secret identifier to extract information from.
     * @return a new instance of {@link KeyVaultSecretIdentifier}.
     * @throws IllegalArgumentException if the given identifier is {@code null}.
     * @throws MalformedURLException if the given identifier is not a valid Key Vault Secret identifier
     */
    public static KeyVaultSecretIdentifier parse(String secretId) throws IllegalArgumentException, MalformedURLException {
        if (secretId == null) {
            throw new IllegalArgumentException("secretId cannot be null");
        }

        URL url = new URL(secretId);
        // We expect an identifier with either 2 or 3 path segments: collection + name [+ version]
        String[] pathSegments = url.getPath().split("/");

        if ((pathSegments.length != 2 && pathSegments.length != 3) || !"secrets".equals(pathSegments[0])) {
            throw new IllegalArgumentException("secretId is not a valid Key Vault Secret identifier");
        }

        return new KeyVaultSecretIdentifier(secretId, url.getProtocol() + "://" + url.getHost(), pathSegments[0],
            pathSegments.length == 3 ? pathSegments[2] : null);
    }
}
