package com.azure.security.keyvault.certificates;


import com.azure.security.keyvault.certificates.models.MergeCertificateOptions;

public class WellKnownIssuerNames {

    /**
     * Create a self-issued certificate.
     */
    public static String SELF = "Self";


    /**
     * Creates a certificate that requires merging an external X.509 certificate using
     * {@link CertificateClient#mergeCertificate(MergeCertificateOptions)} or
     * {@link CertificateAsyncClient#mergeCertificate(MergeCertificateOptions)}.
     */
    public static String UNKNOWN = "Unknown";

}
