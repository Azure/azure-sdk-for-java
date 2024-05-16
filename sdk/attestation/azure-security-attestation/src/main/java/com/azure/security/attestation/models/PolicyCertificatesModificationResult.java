// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.attestation.models;

import com.azure.core.annotation.Immutable;

/**
 * Respects the result of a call to {@link com.azure.security.attestation.AttestationAdministrationAsyncClient#addPolicyManagementCertificate(PolicyManagementCertificateOptions)} or
 * {@link com.azure.security.attestation.AttestationAdministrationAsyncClient#deletePolicyManagementCertificate(PolicyManagementCertificateOptions)}.
 *
 * It contains the state of the certificate identified by {@link PolicyCertificatesModificationResult#getCertificateThumbprint()} -
 * whether the Certificate Modification API resulted in the certificate being removed or not.
 *
 * If the certificate was removed, the state will be {@link CertificateModification#IS_ABSENT}, if it is
 * present after the API call, the state will be {@link CertificateModification#IS_PRESENT}.
 */
@Immutable
public interface PolicyCertificatesModificationResult {
    /**
     * Returns the certificateThumbprint for the certificate which was modified.
     *
     * The "thumbprint" of a certificate is the upper case hex encoded SHA1 Hash of the ASN.1 DER
     * encoded binary representation certificate.
     *
     * @return the certificateThumbprint value.
     */
    String getCertificateThumbprint();

    /**
     * Returns the {@link CertificateModification} property: The result of the operation.
     *
     * @return the certificateResolution value.
     */
    CertificateModification getCertificateResolution();
}
