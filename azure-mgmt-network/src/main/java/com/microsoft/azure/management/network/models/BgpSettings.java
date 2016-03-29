/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.models;


/**
 * The BgpSettings model.
 */
public class BgpSettings {
    /**
     * Gets or sets this BGP speaker's ASN.
     */
    private Long asn;

    /**
     * Gets or sets the BGP peering address and BGP identifier of this BGP
     * speaker.
     */
    private String bgpPeeringAddress;

    /**
     * Gets or sets the weight added to routes learned from this BGP speaker.
     */
    private Integer peerWeight;

    /**
     * Get the asn value.
     *
     * @return the asn value
     */
    public Long getAsn() {
        return this.asn;
    }

    /**
     * Set the asn value.
     *
     * @param asn the asn value to set
     */
    public void setAsn(Long asn) {
        this.asn = asn;
    }

    /**
     * Get the bgpPeeringAddress value.
     *
     * @return the bgpPeeringAddress value
     */
    public String getBgpPeeringAddress() {
        return this.bgpPeeringAddress;
    }

    /**
     * Set the bgpPeeringAddress value.
     *
     * @param bgpPeeringAddress the bgpPeeringAddress value to set
     */
    public void setBgpPeeringAddress(String bgpPeeringAddress) {
        this.bgpPeeringAddress = bgpPeeringAddress;
    }

    /**
     * Get the peerWeight value.
     *
     * @return the peerWeight value
     */
    public Integer getPeerWeight() {
        return this.peerWeight;
    }

    /**
     * Set the peerWeight value.
     *
     * @param peerWeight the peerWeight value to set
     */
    public void setPeerWeight(Integer peerWeight) {
        this.peerWeight = peerWeight;
    }

}
