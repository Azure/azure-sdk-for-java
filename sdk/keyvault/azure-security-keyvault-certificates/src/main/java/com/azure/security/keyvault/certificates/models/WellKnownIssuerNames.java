// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.azure.security.keyvault.certificates.CertificateAsyncClient;
import com.azure.security.keyvault.certificates.CertificateClient;

/**
 * Represents well known issuer names to refer in {@link CertificatePolicy}
 */
public class WellKnownIssuerNames {

    /**
     * Create a self-issued certificate.
     */
    public static final String SELF = "Self";


    /**
     * Creates a certificate that requires merging an external X.509 certificate using
     * {@link CertificateClient#mergeCertificate(MergeCertificateOptions)} or
     * {@link CertificateAsyncClient#mergeCertificate(MergeCertificateOptions)}.
     */
    public static final String UNKNOWN = "Unknown";

}
