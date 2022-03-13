// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.logging.ClientLogger;
import com.azure.security.keyvault.certificates.CertificateAsyncClient;
import com.azure.security.keyvault.certificates.CertificateClient;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Information about a {@link KeyVaultCertificate} parsed from the certificate URL. You can use this information when
 * calling methods of {@link CertificateClient} or {@link CertificateAsyncClient}.
 */
@Immutable
public final class KeyVaultCertificateIdentifier {
    private final ClientLogger logger = new ClientLogger(KeyVaultCertificateIdentifier.class);
    private final String sourceId, vaultUrl, name, version;

    /**
     * Create a new {@link KeyVaultCertificateIdentifier} from a given Key Vault identifier.
     *
     * <p>Some examples:
     *
     * <ul>
     *     <li>https://{key-vault-name}.vault.azure.net/certificates/{certificate-name}</li>
     *     <li>https://{key-vault-name}.vault.azure.net/certificates/{certificate-name}/pending</li>
     *     <li>https://{key-vault-name}.vault.azure.net/certificates/{certificate-name}/{unique-version-id}</li>
     *     <li>https://{key-vault-name}.vault.azure.net/deletedcertificates/{deleted-certificate-name}</li>
     * </ul>
     *
     * @param sourceId The identifier to extract information from.
     *
     * @throws IllegalArgumentException If {@code sourceId} is an invalid Key Vault Certificate identifier.
     * @throws NullPointerException If {@code sourceId} is {@code null}.
     */
    public KeyVaultCertificateIdentifier(String sourceId) {
        if (sourceId == null) {
            throw logger.logExceptionAsError(new NullPointerException("'sourceId' cannot be null"));
        }

        try {
            final URL url = new URL(sourceId);
            // We expect a sourceId with either 3 or 4 path segments: key vault + collection + name [+ "pending"/version]
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
     * @return The certificate identifier.
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
     * Gets the name of the certificate.
     *
     * @return The certificate name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the optional version of the certificate.
     *
     * @return The certificate version.
     */
    public String getVersion() {
        return version;
    }
}
