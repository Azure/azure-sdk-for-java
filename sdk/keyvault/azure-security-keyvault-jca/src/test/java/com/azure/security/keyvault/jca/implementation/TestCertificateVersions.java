// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation;

/**
 * Shared test-only helper used to construct {@link CertificateVersion} instances from tests that live outside the
 * {@code com.azure.security.keyvault.jca.implementation} package, since {@link CertificateVersion} is a final class
 * with a package-private constructor.
 */
public final class TestCertificateVersions {

    private TestCertificateVersions() {
    }

    /**
     * Creates a {@link CertificateVersion} with the given alias and metadata.
     *
     * @param alias The certificate alias.
     * @param certificateData The Base64-encoded DER certificate data.
     * @param keyId The versioned Key Vault key ID.
     * @param secretId The versioned Key Vault secret ID.
     * @param exportable Whether the private key is exportable through the certificate's secret.
     * @param keyType The Key Vault key type.
     * @return A new {@link CertificateVersion} instance.
     */
    public static CertificateVersion create(String alias, String certificateData, String keyId, String secretId,
        boolean exportable, String keyType) {
        return new CertificateVersion(alias, certificateData, keyId, secretId, exportable, keyType);
    }

    /**
     * Creates a {@link CertificateVersion} for the given alias with no additional metadata populated. Each
     * invocation returns a distinct instance, which is useful for tests that need to tell "versions" apart by
     * identity.
     *
     * @param alias The certificate alias.
     * @return A new {@link CertificateVersion} instance.
     */
    public static CertificateVersion create(String alias) {
        return create(alias, null, null, null, false, null);
    }
}
