// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.keys.models;

import com.azure.v2.security.keyvault.keys.KeyClient;
import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;
import io.clientcore.core.instrumentation.logging.ClientLogger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

/**
 * Information about a {@link KeyVaultKey} parsed from the key URL. You can use this information when calling
 * methods of {@link KeyClient}.
 */
@Metadata(properties = { MetadataProperties.IMMUTABLE })
public final class KeyVaultKeyIdentifier {
    private static final ClientLogger LOGGER = new ClientLogger(KeyVaultKeyIdentifier.class);

    private final String sourceId;
    private final String endpoint;
    private final String name;
    private final String version;

    /**
     * Create a new {@link KeyVaultKeyIdentifier} from a given Key Vault identifier.
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
     * @param sourceId The identifier to extract information from.
     *
     * @throws IllegalArgumentException If {@code sourceId} is an invalid Key Vault Key identifier.
     * @throws NullPointerException If {@code sourceId} is {@code null}.
     */
    public KeyVaultKeyIdentifier(String sourceId) {
        Objects.requireNonNull(sourceId, "'sourceId' cannot be null.");

        try {
            final URI uri = new URI(sourceId);
            // We expect an sourceId with either 3 or 4 path segments: key vault + collection + name + "pending"/version
            final String[] pathSegments = uri.getPath().split("/");

            // More or less segments in the URI than expected.
            if (pathSegments.length != 3 && pathSegments.length != 4) {
                throw LOGGER.throwableAtError()
                    .log("'sourceId' is not a valid Key Vault identifier.", IllegalArgumentException::new);
            }

            this.sourceId = sourceId;
            this.endpoint = uri.getScheme() + "://" + uri.getHost();
            this.name = pathSegments[2];
            this.version = pathSegments.length == 4 ? pathSegments[3] : null;
        } catch (URISyntaxException e) {
            throw LOGGER.throwableAtError()
                .log("'sourceId' is not a valid Key Vault identifier.", e, IllegalArgumentException::new);
        }
    }

    /**
     * Gets the key identifier used to create this object
     *
     * @return The key identifier.
     */
    public String getSourceId() {
        return sourceId;
    }

    /**
     * Gets the endpoint of the key vault.
     *
     * @return The key vault endpoint.
     */
    public String getEndpoint() {
        return endpoint;
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
