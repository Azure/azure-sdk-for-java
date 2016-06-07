/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;
import com.microsoft.azure.Resource;

/**
 * Class representing certificate reissue request.
 */
@JsonFlatten
public class ReissueCertificateOrderRequestInner extends Resource {
    /**
     * Certificate Key Size.
     */
    @JsonProperty(value = "properties.keySize")
    private Integer keySize;

    /**
     * Delay in hours to revoke existing certificate after the new certificate
     * is issued.
     */
    @JsonProperty(value = "properties.delayExistingRevokeInHours")
    private Integer delayExistingRevokeInHours;

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
     * @return the ReissueCertificateOrderRequestInner object itself.
     */
    public ReissueCertificateOrderRequestInner withKeySize(Integer keySize) {
        this.keySize = keySize;
        return this;
    }

    /**
     * Get the delayExistingRevokeInHours value.
     *
     * @return the delayExistingRevokeInHours value
     */
    public Integer delayExistingRevokeInHours() {
        return this.delayExistingRevokeInHours;
    }

    /**
     * Set the delayExistingRevokeInHours value.
     *
     * @param delayExistingRevokeInHours the delayExistingRevokeInHours value to set
     * @return the ReissueCertificateOrderRequestInner object itself.
     */
    public ReissueCertificateOrderRequestInner withDelayExistingRevokeInHours(Integer delayExistingRevokeInHours) {
        this.delayExistingRevokeInHours = delayExistingRevokeInHours;
        return this;
    }

}
