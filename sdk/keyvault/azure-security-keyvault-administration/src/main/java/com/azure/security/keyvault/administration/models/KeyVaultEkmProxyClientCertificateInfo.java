// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration.models;

import com.azure.core.annotation.Immutable;

import java.util.List;

/**
 * External Key Manager (EKM) proxy client certificate information.
 */
@Immutable
public final class KeyVaultEkmProxyClientCertificateInfo {
    private final List<byte[]> caCertificates;
    private final String subjectCommonName;

    /**
     * Creates a new {@link KeyVaultEkmProxyClientCertificateInfo} with the specified details.
     *
     * @param caCertificates The client root CA certificate chain to authenticate to the EKM proxy. A list of
     * certificates in the certificate chain, each in DER format.
     * @param subjectCommonName The subject common name of the client certificate used to authenticate to the EKM proxy.
     */
    public KeyVaultEkmProxyClientCertificateInfo(List<byte[]> caCertificates, String subjectCommonName) {
        this.caCertificates = caCertificates;
        this.subjectCommonName = subjectCommonName;
    }

    /**
     * Get the client root CA certificate chain to authenticate to the EKM proxy. A list of certificates in the
     * certificate chain, each in DER format.
     *
     * @return The CA certificates.
     */
    public List<byte[]> getCaCertificates() {
        return this.caCertificates;
    }

    /**
     * Get the subject common name of the client certificate used to authenticate to the EKM proxy.
     *
     * @return The subject common name.
     */
    public String getSubjectCommonName() {
        return this.subjectCommonName;
    }
}
