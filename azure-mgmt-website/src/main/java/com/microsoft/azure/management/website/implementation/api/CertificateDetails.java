/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import org.joda.time.DateTime;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;
import com.microsoft.azure.Resource;

/**
 * Certificate Details.
 */
@JsonFlatten
public class CertificateDetails extends Resource {
    /**
     * Version.
     */
    @JsonProperty(value = "properties.version")
    private Integer version;

    /**
     * Serial Number.
     */
    @JsonProperty(value = "properties.serialNumber")
    private String serialNumber;

    /**
     * Thumbprint.
     */
    @JsonProperty(value = "properties.thumbprint")
    private String thumbprint;

    /**
     * Subject.
     */
    @JsonProperty(value = "properties.subject")
    private String subject;

    /**
     * Valid from.
     */
    @JsonProperty(value = "properties.notBefore")
    private DateTime notBefore;

    /**
     * Valid to.
     */
    @JsonProperty(value = "properties.notAfter")
    private DateTime notAfter;

    /**
     * Signature Algorithm.
     */
    @JsonProperty(value = "properties.signatureAlgorithm")
    private String signatureAlgorithm;

    /**
     * Issuer.
     */
    @JsonProperty(value = "properties.issuer")
    private String issuer;

    /**
     * Raw certificate data.
     */
    @JsonProperty(value = "properties.rawData")
    private String rawData;

    /**
     * Get the version value.
     *
     * @return the version value
     */
    public Integer version() {
        return this.version;
    }

    /**
     * Set the version value.
     *
     * @param version the version value to set
     * @return the CertificateDetails object itself.
     */
    public CertificateDetails withVersion(Integer version) {
        this.version = version;
        return this;
    }

    /**
     * Get the serialNumber value.
     *
     * @return the serialNumber value
     */
    public String serialNumber() {
        return this.serialNumber;
    }

    /**
     * Set the serialNumber value.
     *
     * @param serialNumber the serialNumber value to set
     * @return the CertificateDetails object itself.
     */
    public CertificateDetails withSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
        return this;
    }

    /**
     * Get the thumbprint value.
     *
     * @return the thumbprint value
     */
    public String thumbprint() {
        return this.thumbprint;
    }

    /**
     * Set the thumbprint value.
     *
     * @param thumbprint the thumbprint value to set
     * @return the CertificateDetails object itself.
     */
    public CertificateDetails withThumbprint(String thumbprint) {
        this.thumbprint = thumbprint;
        return this;
    }

    /**
     * Get the subject value.
     *
     * @return the subject value
     */
    public String subject() {
        return this.subject;
    }

    /**
     * Set the subject value.
     *
     * @param subject the subject value to set
     * @return the CertificateDetails object itself.
     */
    public CertificateDetails withSubject(String subject) {
        this.subject = subject;
        return this;
    }

    /**
     * Get the notBefore value.
     *
     * @return the notBefore value
     */
    public DateTime notBefore() {
        return this.notBefore;
    }

    /**
     * Set the notBefore value.
     *
     * @param notBefore the notBefore value to set
     * @return the CertificateDetails object itself.
     */
    public CertificateDetails withNotBefore(DateTime notBefore) {
        this.notBefore = notBefore;
        return this;
    }

    /**
     * Get the notAfter value.
     *
     * @return the notAfter value
     */
    public DateTime notAfter() {
        return this.notAfter;
    }

    /**
     * Set the notAfter value.
     *
     * @param notAfter the notAfter value to set
     * @return the CertificateDetails object itself.
     */
    public CertificateDetails withNotAfter(DateTime notAfter) {
        this.notAfter = notAfter;
        return this;
    }

    /**
     * Get the signatureAlgorithm value.
     *
     * @return the signatureAlgorithm value
     */
    public String signatureAlgorithm() {
        return this.signatureAlgorithm;
    }

    /**
     * Set the signatureAlgorithm value.
     *
     * @param signatureAlgorithm the signatureAlgorithm value to set
     * @return the CertificateDetails object itself.
     */
    public CertificateDetails withSignatureAlgorithm(String signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
        return this;
    }

    /**
     * Get the issuer value.
     *
     * @return the issuer value
     */
    public String issuer() {
        return this.issuer;
    }

    /**
     * Set the issuer value.
     *
     * @param issuer the issuer value to set
     * @return the CertificateDetails object itself.
     */
    public CertificateDetails withIssuer(String issuer) {
        this.issuer = issuer;
        return this;
    }

    /**
     * Get the rawData value.
     *
     * @return the rawData value
     */
    public String rawData() {
        return this.rawData;
    }

    /**
     * Set the rawData value.
     *
     * @param rawData the rawData value to set
     * @return the CertificateDetails object itself.
     */
    public CertificateDetails withRawData(String rawData) {
        this.rawData = rawData;
        return this;
    }

}
