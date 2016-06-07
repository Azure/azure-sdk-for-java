/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import java.util.List;
import org.joda.time.DateTime;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;
import com.microsoft.azure.Resource;

/**
 * App certificate.
 */
@JsonFlatten
public class CertificateInner extends Resource {
    /**
     * Friendly name of the certificate.
     */
    @JsonProperty(value = "properties.friendlyName")
    private String friendlyName;

    /**
     * Subject name of the certificate.
     */
    @JsonProperty(value = "properties.subjectName")
    private String subjectName;

    /**
     * Host names the certificate applies to.
     */
    @JsonProperty(value = "properties.hostNames")
    private List<String> hostNames;

    /**
     * Pfx blob.
     */
    @JsonProperty(value = "properties.pfxBlob")
    private String pfxBlob;

    /**
     * App name.
     */
    @JsonProperty(value = "properties.siteName")
    private String siteName;

    /**
     * Self link.
     */
    @JsonProperty(value = "properties.selfLink")
    private String selfLink;

    /**
     * Certificate issuer.
     */
    @JsonProperty(value = "properties.issuer")
    private String issuer;

    /**
     * Certificate issue Date.
     */
    @JsonProperty(value = "properties.issueDate")
    private DateTime issueDate;

    /**
     * Certificate expriration date.
     */
    @JsonProperty(value = "properties.expirationDate")
    private DateTime expirationDate;

    /**
     * Certificate password.
     */
    @JsonProperty(value = "properties.password")
    private String password;

    /**
     * Certificate thumbprint.
     */
    @JsonProperty(value = "properties.thumbprint")
    private String thumbprint;

    /**
     * Is the certificate valid?.
     */
    @JsonProperty(value = "properties.valid")
    private Boolean valid;

    /**
     * Raw bytes of .cer file.
     */
    @JsonProperty(value = "properties.cerBlob")
    private String cerBlob;

    /**
     * Public key hash.
     */
    @JsonProperty(value = "properties.publicKeyHash")
    private String publicKeyHash;

    /**
     * Specification for the hosting environment (App Service Environment) to
     * use for the certificate.
     */
    @JsonProperty(value = "properties.hostingEnvironmentProfile")
    private HostingEnvironmentProfile hostingEnvironmentProfile;

    /**
     * Get the friendlyName value.
     *
     * @return the friendlyName value
     */
    public String friendlyName() {
        return this.friendlyName;
    }

    /**
     * Set the friendlyName value.
     *
     * @param friendlyName the friendlyName value to set
     * @return the CertificateInner object itself.
     */
    public CertificateInner withFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
        return this;
    }

    /**
     * Get the subjectName value.
     *
     * @return the subjectName value
     */
    public String subjectName() {
        return this.subjectName;
    }

    /**
     * Set the subjectName value.
     *
     * @param subjectName the subjectName value to set
     * @return the CertificateInner object itself.
     */
    public CertificateInner withSubjectName(String subjectName) {
        this.subjectName = subjectName;
        return this;
    }

    /**
     * Get the hostNames value.
     *
     * @return the hostNames value
     */
    public List<String> hostNames() {
        return this.hostNames;
    }

    /**
     * Set the hostNames value.
     *
     * @param hostNames the hostNames value to set
     * @return the CertificateInner object itself.
     */
    public CertificateInner withHostNames(List<String> hostNames) {
        this.hostNames = hostNames;
        return this;
    }

    /**
     * Get the pfxBlob value.
     *
     * @return the pfxBlob value
     */
    public String pfxBlob() {
        return this.pfxBlob;
    }

    /**
     * Set the pfxBlob value.
     *
     * @param pfxBlob the pfxBlob value to set
     * @return the CertificateInner object itself.
     */
    public CertificateInner withPfxBlob(String pfxBlob) {
        this.pfxBlob = pfxBlob;
        return this;
    }

    /**
     * Get the siteName value.
     *
     * @return the siteName value
     */
    public String siteName() {
        return this.siteName;
    }

    /**
     * Set the siteName value.
     *
     * @param siteName the siteName value to set
     * @return the CertificateInner object itself.
     */
    public CertificateInner withSiteName(String siteName) {
        this.siteName = siteName;
        return this;
    }

    /**
     * Get the selfLink value.
     *
     * @return the selfLink value
     */
    public String selfLink() {
        return this.selfLink;
    }

    /**
     * Set the selfLink value.
     *
     * @param selfLink the selfLink value to set
     * @return the CertificateInner object itself.
     */
    public CertificateInner withSelfLink(String selfLink) {
        this.selfLink = selfLink;
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
     * @return the CertificateInner object itself.
     */
    public CertificateInner withIssuer(String issuer) {
        this.issuer = issuer;
        return this;
    }

    /**
     * Get the issueDate value.
     *
     * @return the issueDate value
     */
    public DateTime issueDate() {
        return this.issueDate;
    }

    /**
     * Set the issueDate value.
     *
     * @param issueDate the issueDate value to set
     * @return the CertificateInner object itself.
     */
    public CertificateInner withIssueDate(DateTime issueDate) {
        this.issueDate = issueDate;
        return this;
    }

    /**
     * Get the expirationDate value.
     *
     * @return the expirationDate value
     */
    public DateTime expirationDate() {
        return this.expirationDate;
    }

    /**
     * Set the expirationDate value.
     *
     * @param expirationDate the expirationDate value to set
     * @return the CertificateInner object itself.
     */
    public CertificateInner withExpirationDate(DateTime expirationDate) {
        this.expirationDate = expirationDate;
        return this;
    }

    /**
     * Get the password value.
     *
     * @return the password value
     */
    public String password() {
        return this.password;
    }

    /**
     * Set the password value.
     *
     * @param password the password value to set
     * @return the CertificateInner object itself.
     */
    public CertificateInner withPassword(String password) {
        this.password = password;
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
     * @return the CertificateInner object itself.
     */
    public CertificateInner withThumbprint(String thumbprint) {
        this.thumbprint = thumbprint;
        return this;
    }

    /**
     * Get the valid value.
     *
     * @return the valid value
     */
    public Boolean valid() {
        return this.valid;
    }

    /**
     * Set the valid value.
     *
     * @param valid the valid value to set
     * @return the CertificateInner object itself.
     */
    public CertificateInner withValid(Boolean valid) {
        this.valid = valid;
        return this;
    }

    /**
     * Get the cerBlob value.
     *
     * @return the cerBlob value
     */
    public String cerBlob() {
        return this.cerBlob;
    }

    /**
     * Set the cerBlob value.
     *
     * @param cerBlob the cerBlob value to set
     * @return the CertificateInner object itself.
     */
    public CertificateInner withCerBlob(String cerBlob) {
        this.cerBlob = cerBlob;
        return this;
    }

    /**
     * Get the publicKeyHash value.
     *
     * @return the publicKeyHash value
     */
    public String publicKeyHash() {
        return this.publicKeyHash;
    }

    /**
     * Set the publicKeyHash value.
     *
     * @param publicKeyHash the publicKeyHash value to set
     * @return the CertificateInner object itself.
     */
    public CertificateInner withPublicKeyHash(String publicKeyHash) {
        this.publicKeyHash = publicKeyHash;
        return this;
    }

    /**
     * Get the hostingEnvironmentProfile value.
     *
     * @return the hostingEnvironmentProfile value
     */
    public HostingEnvironmentProfile hostingEnvironmentProfile() {
        return this.hostingEnvironmentProfile;
    }

    /**
     * Set the hostingEnvironmentProfile value.
     *
     * @param hostingEnvironmentProfile the hostingEnvironmentProfile value to set
     * @return the CertificateInner object itself.
     */
    public CertificateInner withHostingEnvironmentProfile(HostingEnvironmentProfile hostingEnvironmentProfile) {
        this.hostingEnvironmentProfile = hostingEnvironmentProfile;
        return this;
    }

}
