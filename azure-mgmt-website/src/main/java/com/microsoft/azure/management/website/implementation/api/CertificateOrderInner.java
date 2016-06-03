/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import java.util.Map;
import org.joda.time.DateTime;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;
import com.microsoft.azure.Resource;

/**
 * Certificate purchase order.
 */
@JsonFlatten
public class CertificateOrderInner extends Resource {
    /**
     * State of the Key Vault secret.
     */
    @JsonProperty(value = "properties.certificates")
    private Map<String, CertificateOrderCertificateInner> certificates;

    /**
     * Certificate distinguished name.
     */
    @JsonProperty(value = "properties.distinguishedName")
    private String distinguishedName;

    /**
     * Domain Verification Token.
     */
    @JsonProperty(value = "properties.domainVerificationToken")
    private String domainVerificationToken;

    /**
     * Duration in years (must be between 1 and 3).
     */
    @JsonProperty(value = "properties.validityInYears")
    private Integer validityInYears;

    /**
     * Certificate Key Size.
     */
    @JsonProperty(value = "properties.keySize")
    private Integer keySize;

    /**
     * Certificate product type. Possible values include:
     * 'StandardDomainValidatedSsl', 'StandardDomainValidatedWildCardSsl'.
     */
    @JsonProperty(value = "properties.productType")
    private CertificateProductType productType;

    /**
     * Auto renew.
     */
    @JsonProperty(value = "properties.autoRenew")
    private Boolean autoRenew;

    /**
     * Status of certificate order. Possible values include: 'Succeeded',
     * 'Failed', 'Canceled', 'InProgress', 'Deleting'.
     */
    @JsonProperty(value = "properties.provisioningState")
    private ProvisioningState provisioningState;

    /**
     * Current order status. Possible values include: 'Pendingissuance',
     * 'Issued', 'Revoked', 'Canceled', 'Denied', 'Pendingrevocation',
     * 'PendingRekey', 'Unused', 'Expired', 'NotSubmitted'.
     */
    @JsonProperty(value = "properties.status")
    private CertificateOrderStatus status;

    /**
     * Signed certificate.
     */
    @JsonProperty(value = "properties.signedCertificate")
    private CertificateDetails signedCertificate;

    /**
     * Last CSR that was created for this order.
     */
    @JsonProperty(value = "properties.csr")
    private String csr;

    /**
     * Intermediate certificate.
     */
    @JsonProperty(value = "properties.intermediate")
    private CertificateDetails intermediate;

    /**
     * Root certificate.
     */
    @JsonProperty(value = "properties.root")
    private CertificateDetails root;

    /**
     * Current serial number of the certificate.
     */
    @JsonProperty(value = "properties.serialNumber")
    private String serialNumber;

    /**
     * Certificate last issuance time.
     */
    @JsonProperty(value = "properties.lastCertificateIssuanceTime")
    private DateTime lastCertificateIssuanceTime;

    /**
     * Certificate expiration time.
     */
    @JsonProperty(value = "properties.expirationTime")
    private DateTime expirationTime;

    /**
     * Get the certificates value.
     *
     * @return the certificates value
     */
    public Map<String, CertificateOrderCertificateInner> certificates() {
        return this.certificates;
    }

    /**
     * Set the certificates value.
     *
     * @param certificates the certificates value to set
     * @return the CertificateOrderInner object itself.
     */
    public CertificateOrderInner withCertificates(Map<String, CertificateOrderCertificateInner> certificates) {
        this.certificates = certificates;
        return this;
    }

    /**
     * Get the distinguishedName value.
     *
     * @return the distinguishedName value
     */
    public String distinguishedName() {
        return this.distinguishedName;
    }

    /**
     * Set the distinguishedName value.
     *
     * @param distinguishedName the distinguishedName value to set
     * @return the CertificateOrderInner object itself.
     */
    public CertificateOrderInner withDistinguishedName(String distinguishedName) {
        this.distinguishedName = distinguishedName;
        return this;
    }

    /**
     * Get the domainVerificationToken value.
     *
     * @return the domainVerificationToken value
     */
    public String domainVerificationToken() {
        return this.domainVerificationToken;
    }

    /**
     * Set the domainVerificationToken value.
     *
     * @param domainVerificationToken the domainVerificationToken value to set
     * @return the CertificateOrderInner object itself.
     */
    public CertificateOrderInner withDomainVerificationToken(String domainVerificationToken) {
        this.domainVerificationToken = domainVerificationToken;
        return this;
    }

    /**
     * Get the validityInYears value.
     *
     * @return the validityInYears value
     */
    public Integer validityInYears() {
        return this.validityInYears;
    }

    /**
     * Set the validityInYears value.
     *
     * @param validityInYears the validityInYears value to set
     * @return the CertificateOrderInner object itself.
     */
    public CertificateOrderInner withValidityInYears(Integer validityInYears) {
        this.validityInYears = validityInYears;
        return this;
    }

    /**
     * Get the keySize value.
     *
     * @return the keySize value
     */
    public Integer keySize() {
        return this.keySize;
    }

    /**
     * Set the keySize value.
     *
     * @param keySize the keySize value to set
     * @return the CertificateOrderInner object itself.
     */
    public CertificateOrderInner withKeySize(Integer keySize) {
        this.keySize = keySize;
        return this;
    }

    /**
     * Get the productType value.
     *
     * @return the productType value
     */
    public CertificateProductType productType() {
        return this.productType;
    }

    /**
     * Set the productType value.
     *
     * @param productType the productType value to set
     * @return the CertificateOrderInner object itself.
     */
    public CertificateOrderInner withProductType(CertificateProductType productType) {
        this.productType = productType;
        return this;
    }

    /**
     * Get the autoRenew value.
     *
     * @return the autoRenew value
     */
    public Boolean autoRenew() {
        return this.autoRenew;
    }

    /**
     * Set the autoRenew value.
     *
     * @param autoRenew the autoRenew value to set
     * @return the CertificateOrderInner object itself.
     */
    public CertificateOrderInner withAutoRenew(Boolean autoRenew) {
        this.autoRenew = autoRenew;
        return this;
    }

    /**
     * Get the provisioningState value.
     *
     * @return the provisioningState value
     */
    public ProvisioningState provisioningState() {
        return this.provisioningState;
    }

    /**
     * Set the provisioningState value.
     *
     * @param provisioningState the provisioningState value to set
     * @return the CertificateOrderInner object itself.
     */
    public CertificateOrderInner withProvisioningState(ProvisioningState provisioningState) {
        this.provisioningState = provisioningState;
        return this;
    }

    /**
     * Get the status value.
     *
     * @return the status value
     */
    public CertificateOrderStatus status() {
        return this.status;
    }

    /**
     * Set the status value.
     *
     * @param status the status value to set
     * @return the CertificateOrderInner object itself.
     */
    public CertificateOrderInner withStatus(CertificateOrderStatus status) {
        this.status = status;
        return this;
    }

    /**
     * Get the signedCertificate value.
     *
     * @return the signedCertificate value
     */
    public CertificateDetails signedCertificate() {
        return this.signedCertificate;
    }

    /**
     * Set the signedCertificate value.
     *
     * @param signedCertificate the signedCertificate value to set
     * @return the CertificateOrderInner object itself.
     */
    public CertificateOrderInner withSignedCertificate(CertificateDetails signedCertificate) {
        this.signedCertificate = signedCertificate;
        return this;
    }

    /**
     * Get the csr value.
     *
     * @return the csr value
     */
    public String csr() {
        return this.csr;
    }

    /**
     * Set the csr value.
     *
     * @param csr the csr value to set
     * @return the CertificateOrderInner object itself.
     */
    public CertificateOrderInner withCsr(String csr) {
        this.csr = csr;
        return this;
    }

    /**
     * Get the intermediate value.
     *
     * @return the intermediate value
     */
    public CertificateDetails intermediate() {
        return this.intermediate;
    }

    /**
     * Set the intermediate value.
     *
     * @param intermediate the intermediate value to set
     * @return the CertificateOrderInner object itself.
     */
    public CertificateOrderInner withIntermediate(CertificateDetails intermediate) {
        this.intermediate = intermediate;
        return this;
    }

    /**
     * Get the root value.
     *
     * @return the root value
     */
    public CertificateDetails root() {
        return this.root;
    }

    /**
     * Set the root value.
     *
     * @param root the root value to set
     * @return the CertificateOrderInner object itself.
     */
    public CertificateOrderInner withRoot(CertificateDetails root) {
        this.root = root;
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
     * @return the CertificateOrderInner object itself.
     */
    public CertificateOrderInner withSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
        return this;
    }

    /**
     * Get the lastCertificateIssuanceTime value.
     *
     * @return the lastCertificateIssuanceTime value
     */
    public DateTime lastCertificateIssuanceTime() {
        return this.lastCertificateIssuanceTime;
    }

    /**
     * Set the lastCertificateIssuanceTime value.
     *
     * @param lastCertificateIssuanceTime the lastCertificateIssuanceTime value to set
     * @return the CertificateOrderInner object itself.
     */
    public CertificateOrderInner withLastCertificateIssuanceTime(DateTime lastCertificateIssuanceTime) {
        this.lastCertificateIssuanceTime = lastCertificateIssuanceTime;
        return this;
    }

    /**
     * Get the expirationTime value.
     *
     * @return the expirationTime value
     */
    public DateTime expirationTime() {
        return this.expirationTime;
    }

    /**
     * Set the expirationTime value.
     *
     * @param expirationTime the expirationTime value to set
     * @return the CertificateOrderInner object itself.
     */
    public CertificateOrderInner withExpirationTime(DateTime expirationTime) {
        this.expirationTime = expirationTime;
        return this;
    }

}
