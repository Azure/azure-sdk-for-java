/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation.api;


/**
 * Contains FQDN of the DNS record associated with the public IP address.
 */
public class PublicIPAddressDnsSettings {
    /**
     * Gets or sets the Domain name label.The concatenation of the domain name
     * label and the regionalized DNS zone make up the fully qualified domain
     * name associated with the public IP address. If a domain name label is
     * specified, an A DNS record is created for the public IP in the
     * Microsoft Azure DNS system.
     */
    private String domainNameLabel;

    /**
     * Gets the FQDN, Fully qualified domain name of the A DNS record
     * associated with the public IP. This is the concatenation of the
     * domainNameLabel and the regionalized DNS zone.
     */
    private String fqdn;

    /**
     * Gets or Sests the Reverse FQDN. A user-visible, fully qualified domain
     * name that resolves to this public IP address. If the reverseFqdn is
     * specified, then a PTR DNS record is created pointing from the IP
     * address in the in-addr.arpa domain to the reverse FQDN.
     */
    private String reverseFqdn;

    /**
     * Get the domainNameLabel value.
     *
     * @return the domainNameLabel value
     */
    public String domainNameLabel() {
        return this.domainNameLabel;
    }

    /**
     * Set the domainNameLabel value.
     *
     * @param domainNameLabel the domainNameLabel value to set
     * @return the PublicIPAddressDnsSettings object itself.
     */
    public PublicIPAddressDnsSettings withDomainNameLabel(String domainNameLabel) {
        this.domainNameLabel = domainNameLabel;
        return this;
    }

    /**
     * Get the fqdn value.
     *
     * @return the fqdn value
     */
    public String fqdn() {
        return this.fqdn;
    }

    /**
     * Set the fqdn value.
     *
     * @param fqdn the fqdn value to set
     * @return the PublicIPAddressDnsSettings object itself.
     */
    public PublicIPAddressDnsSettings withFqdn(String fqdn) {
        this.fqdn = fqdn;
        return this;
    }

    /**
     * Get the reverseFqdn value.
     *
     * @return the reverseFqdn value
     */
    public String reverseFqdn() {
        return this.reverseFqdn;
    }

    /**
     * Set the reverseFqdn value.
     *
     * @param reverseFqdn the reverseFqdn value to set
     * @return the PublicIPAddressDnsSettings object itself.
     */
    public PublicIPAddressDnsSettings withReverseFqdn(String reverseFqdn) {
        this.reverseFqdn = reverseFqdn;
        return this;
    }

}
