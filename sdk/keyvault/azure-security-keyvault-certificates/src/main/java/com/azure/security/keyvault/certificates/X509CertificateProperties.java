// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.certificates;

import com.azure.security.keyvault.certificates.models.CertificateKeyUsage;
import com.azure.security.keyvault.certificates.models.CertificatePolicy;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Properties of the X509 component of a certificate.
 */
class X509CertificateProperties {

    X509CertificateProperties(CertificatePolicy certificatePolicy) {
        this.subject = certificatePolicy.getSubject();
        this.ekus = certificatePolicy.getEnhancedKeyUsage();
        this.keyUsage = certificatePolicy.getKeyUsage();
        this.subjectAlternativeNamesRequest = new SubjectAlternativeNamesRequest(certificatePolicy.getSubjectAlternativeNames());
        this.validityInMonths = certificatePolicy.getValidityInMonths();
    }

    /**
     * The subject name. Should be a valid X509 distinguished Name.
     */
    @JsonProperty(value = "subject")
    private String subject;

    /**
     * The enhanced key usage.
     */
    @JsonProperty(value = "ekus")
    private List<String> ekus;

    /**
     * The subject alternative names.
     */
    @JsonProperty(value = "sans")
    private SubjectAlternativeNamesRequest subjectAlternativeNamesRequest;

    /**
     * List of key usages.
     */
    @JsonProperty(value = "key_usage")
    private List<CertificateKeyUsage> keyUsage;

    /**
     * The duration that the certificate is valid in months.
     */
    @JsonProperty(value = "validity_months")
    private Integer validityInMonths;

    /**
     * Get the subject value.
     *
     * @return the subject value
     */
    String subject() {
        return this.subject;
    }

    /**
     * Set the subject value.
     *
     * @param subject the subject value to set
     * @return the X509CertificateProperties object itself.
     */
    X509CertificateProperties subject(String subject) {
        this.subject = subject;
        return this;
    }

    /**
     * Get the ekus value.
     *
     * @return the ekus value
     */
    List<String> ekus() {
        return this.ekus;
    }

    /**
     * Set the ekus value.
     *
     * @param ekus the ekus value to set
     * @return the X509CertificateProperties object itself.
     */
    X509CertificateProperties ekus(List<String> ekus) {
        this.ekus = ekus;
        return this;
    }

    /**
     * Get the subjectAlternativeNamesRequest value.
     *
     * @return the subjectAlternativeNamesRequest value
     */
    SubjectAlternativeNamesRequest subjectAlternativeNames() {
        return this.subjectAlternativeNamesRequest;
    }

    /**
     * Set the subjectAlternativeNamesRequest value.
     *
     * @param subjectAlternativeNamesRequest the subjectAlternativeNamesRequest value to set
     * @return the X509CertificateProperties object itself.
     */
    X509CertificateProperties subjectAlternativeNames(SubjectAlternativeNamesRequest subjectAlternativeNamesRequest) {
        this.subjectAlternativeNamesRequest = subjectAlternativeNamesRequest;
        return this;
    }

    /**
     * Get the keyUsage value.
     *
     * @return the keyUsage value
     */
    List<CertificateKeyUsage> keyUsage() {
        return this.keyUsage;
    }

    /**
     * Set the keyUsage value.
     *
     * @param keyUsage the keyUsage value to set
     * @return the X509CertificateProperties object itself.
     */
    X509CertificateProperties keyUsage(List<CertificateKeyUsage> keyUsage) {
        this.keyUsage = keyUsage;
        return this;
    }

    /**
     * Get the validityInMonths value.
     *
     * @return the validityInMonths value
     */
    Integer validityInMonths() {
        return this.validityInMonths;
    }

    /**
     * Set the validityInMonths value.
     *
     * @param validityInMonths the validityInMonths value to set
     * @return the X509CertificateProperties object itself.
     */
    X509CertificateProperties validityInMonths(Integer validityInMonths) {
        this.validityInMonths = validityInMonths;
        return this;
    }
}
