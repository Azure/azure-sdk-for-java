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
 * Represents a certificate action.
 */
@JsonFlatten
public class CertificateOrderActionInner extends Resource {
    /**
     * Type. Possible values include: 'CertificateIssued',
     * 'CertificateOrderCanceled', 'CertificateOrderCreated',
     * 'CertificateRevoked', 'DomainValidationComplete', 'FraudDetected',
     * 'OrgNameChange', 'OrgValidationComplete', 'SanDrop'.
     */
    @JsonProperty(value = "properties.type")
    private CertificateOrderActionType certificateOrderActionType;

    /**
     * Time at which the certificate action was performed.
     */
    @JsonProperty(value = "properties.createdAt")
    private DateTime createdAt;

    /**
     * Get the certificateOrderActionType value.
     *
     * @return the certificateOrderActionType value
     */
    public CertificateOrderActionType certificateOrderActionType() {
        return this.certificateOrderActionType;
    }

    /**
     * Set the certificateOrderActionType value.
     *
     * @param certificateOrderActionType the certificateOrderActionType value to set
     * @return the CertificateOrderActionInner object itself.
     */
    public CertificateOrderActionInner withCertificateOrderActionType(CertificateOrderActionType certificateOrderActionType) {
        this.certificateOrderActionType = certificateOrderActionType;
        return this;
    }

    /**
     * Get the createdAt value.
     *
     * @return the createdAt value
     */
    public DateTime createdAt() {
        return this.createdAt;
    }

    /**
     * Set the createdAt value.
     *
     * @param createdAt the createdAt value to set
     * @return the CertificateOrderActionInner object itself.
     */
    public CertificateOrderActionInner withCreatedAt(DateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

}
