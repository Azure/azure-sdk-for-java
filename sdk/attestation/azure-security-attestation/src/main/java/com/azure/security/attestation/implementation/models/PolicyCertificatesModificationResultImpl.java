// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.attestation.implementation.models;

import com.azure.core.annotation.Immutable;
import com.azure.security.attestation.models.CertificateModification;

/** The result of a policy certificate modification. */
@Immutable
public final class PolicyCertificatesModificationResultImpl
    implements com.azure.security.attestation.models.PolicyCertificatesModificationResult {
    /*
     * Hex encoded SHA1 Hash of the binary representation certificate which was
     * added or removed
     */
    private final String certificateThumbprint;

    /*
     * The result of the operation
     */
    private final CertificateModification certificateResolution;

    /**
     * Get the certificateThumbprint property: Hex encoded SHA1 Hash of the binary representation certificate which was
     * added or removed.
     *
     * @return the certificateThumbprint value.
     */
    public String getCertificateThumbprint() {
        return this.certificateThumbprint;
    }

    /**
     * Get the certificateResolution property: The result of the operation.
     *
     * @return the certificateResolution value.
     */
    public CertificateModification getCertificateResolution() {
        return this.certificateResolution;
    }

    private PolicyCertificatesModificationResultImpl(CertificateModification modification,
        String certificateThumbprint) {
        this.certificateResolution = modification;
        this.certificateThumbprint = certificateThumbprint;
    }

    public static com.azure.security.attestation.models.PolicyCertificatesModificationResult
        fromGenerated(PolicyCertificatesModificationResult generated) {
        return new com.azure.security.attestation.implementation.models.PolicyCertificatesModificationResultImpl(
            generated.getCertificateResolution(), generated.getCertificateThumbprint());
    }

}
