// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.azure.security.keyvault.certificates.CertificateAsyncClient;
import com.azure.security.keyvault.certificates.CertificateClient;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Information about a {@link KeyVaultCertificate} parsed from the key URL. You can use this information when calling
 * methods of {@link CertificateClient} or {@link CertificateAsyncClient}.
 */
public final class KeyVaultCertificateIdentifier {
    private final String certificateId, vaultUrl, name, version;

    private KeyVaultCertificateIdentifier(String certificateId, String vaultUrl, String name, String version) {
        this.certificateId = certificateId;
        this.vaultUrl = vaultUrl;
        this.name = name;
        this.version = version;
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

    /**
     * Create a new {@link KeyVaultCertificateIdentifier} from a given certificate identifier.
     *
     * @param certificateId The certificate identifier to extract information from.
     * @return a new instance of {@link KeyVaultCertificateIdentifier}.
     * @throws IllegalArgumentException if the given identifier is {@code null}.
     * @throws MalformedURLException if the given identifier is not a valid Key Vault Certificate identifier
     */
    public static KeyVaultCertificateIdentifier parse(String certificateId) throws IllegalArgumentException, MalformedURLException {
        if (certificateId == null) {
            throw new IllegalArgumentException("certificateId cannot be null");
        }

        URL url = new URL(certificateId);
        // We expect an identifier with either 2 or 3 path segments: collection + name [+ version]
        String[] pathSegments = url.getPath().split("/");

        if ((pathSegments.length != 2 && pathSegments.length != 3) || !"certificates".equals(pathSegments[0])) {
            throw new IllegalArgumentException("certificateId is not a valid Key Vault Certificate identifier");
        }

        return new KeyVaultCertificateIdentifier(certificateId, url.getProtocol() + "://" + url.getHost(), pathSegments[0],
            pathSegments.length == 3 ? pathSegments[2] : null);
    }
}
