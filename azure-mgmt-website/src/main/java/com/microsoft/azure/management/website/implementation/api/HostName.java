/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import java.util.List;

/**
 * Details of a hostname derived from a domain.
 */
public class HostName {
    /**
     * Name of the hostname.
     */
    private String name;

    /**
     * List of sites the hostname is assigned to. This list will have more
     * than one site only if the hostname is pointing to a Traffic Manager.
     */
    private List<String> siteNames;

    /**
     * Name of the Azure resource the hostname is assigned to. If it is
     * assigned to a traffic manager then it will be the traffic manager name
     * otherwise it will be the website name.
     */
    private String azureResourceName;

    /**
     * Type of the Azure resource the hostname is assigned to. Possible values
     * include: 'Website', 'TrafficManager'.
     */
    private AzureResourceType azureResourceType;

    /**
     * Type of the Dns record. Possible values include: 'CName', 'A'.
     */
    private CustomHostNameDnsRecordType customHostNameDnsRecordType;

    /**
     * Type of the hostname. Possible values include: 'Verified', 'Managed'.
     */
    private HostNameType hostNameType;

    /**
     * Get the name value.
     *
     * @return the name value
     */
    public String name() {
        return this.name;
    }

    /**
     * Set the name value.
     *
     * @param name the name value to set
     * @return the HostName object itself.
     */
    public HostName withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the siteNames value.
     *
     * @return the siteNames value
     */
    public List<String> siteNames() {
        return this.siteNames;
    }

    /**
     * Set the siteNames value.
     *
     * @param siteNames the siteNames value to set
     * @return the HostName object itself.
     */
    public HostName withSiteNames(List<String> siteNames) {
        this.siteNames = siteNames;
        return this;
    }

    /**
     * Get the azureResourceName value.
     *
     * @return the azureResourceName value
     */
    public String azureResourceName() {
        return this.azureResourceName;
    }

    /**
     * Set the azureResourceName value.
     *
     * @param azureResourceName the azureResourceName value to set
     * @return the HostName object itself.
     */
    public HostName withAzureResourceName(String azureResourceName) {
        this.azureResourceName = azureResourceName;
        return this;
    }

    /**
     * Get the azureResourceType value.
     *
     * @return the azureResourceType value
     */
    public AzureResourceType azureResourceType() {
        return this.azureResourceType;
    }

    /**
     * Set the azureResourceType value.
     *
     * @param azureResourceType the azureResourceType value to set
     * @return the HostName object itself.
     */
    public HostName withAzureResourceType(AzureResourceType azureResourceType) {
        this.azureResourceType = azureResourceType;
        return this;
    }

    /**
     * Get the customHostNameDnsRecordType value.
     *
     * @return the customHostNameDnsRecordType value
     */
    public CustomHostNameDnsRecordType customHostNameDnsRecordType() {
        return this.customHostNameDnsRecordType;
    }

    /**
     * Set the customHostNameDnsRecordType value.
     *
     * @param customHostNameDnsRecordType the customHostNameDnsRecordType value to set
     * @return the HostName object itself.
     */
    public HostName withCustomHostNameDnsRecordType(CustomHostNameDnsRecordType customHostNameDnsRecordType) {
        this.customHostNameDnsRecordType = customHostNameDnsRecordType;
        return this;
    }

    /**
     * Get the hostNameType value.
     *
     * @return the hostNameType value
     */
    public HostNameType hostNameType() {
        return this.hostNameType;
    }

    /**
     * Set the hostNameType value.
     *
     * @param hostNameType the hostNameType value to set
     * @return the HostName object itself.
     */
    public HostName withHostNameType(HostNameType hostNameType) {
        this.hostNameType = hostNameType;
        return this;
    }

}
