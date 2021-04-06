// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.azure.security.keyvault.certificates.CertificateAsyncClient;
import com.azure.security.keyvault.certificates.CertificateClient;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Information about a {@link KeyVaultCertificate} parsed from the certificate URL. You can use this information when
 * calling methods of {@link CertificateClient} or {@link CertificateAsyncClient}.
 */
public final class KeyVaultCertificateIdentifier {
    private final String certificateId, vaultUrl, name, version;

    /**
     * Create a new {@link KeyVaultCertificateIdentifier} from a given certificate identifier.
     *
     * <p>Valid examples are:
     *
     * <ul>
     *     <li>https://{key-vault-name}.vault.azure.net/certificates/{certificate-name}</li>
     *     <li>https://{key-vault-name}.vault.azure.net/certificates/{certificate-name}/pending</li>
     *     <li>https://{key-vault-name}.vault.azure.net/certificates/{certificate-name}/{unique-version-id}</li>
     *     <li>https://{key-vault-name}.vault.azure.net/deletedcertificates/{deleted-certificate-name}</li>
     * </ul>
     *
     * @param certificateId The certificate identifier to extract information from.
     *
     * @throws IllegalArgumentException If {@code certificateId} is an invalid Key Vault Certificate identifier.
     * @throws NullPointerException If {@code certificateId} is {@code null}.
     */
    public KeyVaultCertificateIdentifier(String certificateId) {
        if (certificateId == null) {
            throw new NullPointerException("'certificateId' cannot be null");
        }

        try {
            final URL url = new URL(certificateId);
            // We expect an identifier with either 2 or 3 path segments: collection + name [+ version]
            final String[] pathSegments = url.getPath().split("/");

            if ((pathSegments.length != 3 && pathSegments.length != 4) // More or less segments in the URI than expected.
                || !"https".equals(url.getProtocol()) // Invalid protocol.
                || ("deletedcertificates".equals(pathSegments[1]) && pathSegments.length == 4)) { // Deleted items do not include a version.

                throw new IllegalArgumentException("certificateId is not a valid Key Vault Certificate identifier");
            }

            this.certificateId = certificateId;
            this.vaultUrl = String.format("%s://%s", url.getProtocol(), url.getHost());
            this.name = pathSegments[2];
            this.version = pathSegments.length == 4 ? pathSegments[3] : null;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Could not parse certificateId", e);
        }
    }

    /**
     * Gets the key identifier used to create this object
     *
     * @return The certificate identifier.
     */
    public String getCertificateId() {
        return certificateId;
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
