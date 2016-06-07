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
 * Domain registration input for validation Api.
 */
@JsonFlatten
public class DomainRegistrationInputInner extends Resource {
    /**
     * Name of the domain.
     */
    @JsonProperty(value = "properties.name")
    private String domainRegistrationInputName;

    /**
     * Admin contact information.
     */
    @JsonProperty(value = "properties.contactAdmin")
    private Contact contactAdmin;

    /**
     * Billing contact information.
     */
    @JsonProperty(value = "properties.contactBilling")
    private Contact contactBilling;

    /**
     * Registrant contact information.
     */
    @JsonProperty(value = "properties.contactRegistrant")
    private Contact contactRegistrant;

    /**
     * Technical contact information.
     */
    @JsonProperty(value = "properties.contactTech")
    private Contact contactTech;

    /**
     * Domain registration status. Possible values include: 'Active',
     * 'Awaiting', 'Cancelled', 'Confiscated', 'Disabled', 'Excluded',
     * 'Expired', 'Failed', 'Held', 'Locked', 'Parked', 'Pending',
     * 'Reserved', 'Reverted', 'Suspended', 'Transferred', 'Unknown',
     * 'Unlocked', 'Unparked', 'Updated', 'JsonConverterFailed'.
     */
    @JsonProperty(value = "properties.registrationStatus")
    private DomainStatus registrationStatus;

    /**
     * Domain provisioning state. Possible values include: 'Succeeded',
     * 'Failed', 'Canceled', 'InProgress', 'Deleting'.
     */
    @JsonProperty(value = "properties.provisioningState")
    private ProvisioningState provisioningState;

    /**
     * Name servers.
     */
    @JsonProperty(value = "properties.nameServers")
    private List<String> nameServers;

    /**
     * If true then domain privacy is enabled for this domain.
     */
    @JsonProperty(value = "properties.privacy")
    private Boolean privacy;

    /**
     * Domain creation timestamp.
     */
    @JsonProperty(value = "properties.createdTime")
    private DateTime createdTime;

    /**
     * Domain expiration timestamp.
     */
    @JsonProperty(value = "properties.expirationTime")
    private DateTime expirationTime;

    /**
     * Timestamp when the domain was renewed last time.
     */
    @JsonProperty(value = "properties.lastRenewedTime")
    private DateTime lastRenewedTime;

    /**
     * If true then domain will renewed automatically.
     */
    @JsonProperty(value = "properties.autoRenew")
    private Boolean autoRenew;

    /**
     * If true then Azure can assign this domain to Web Apps. This value will
     * be true if domain registration status is active and it is hosted on
     * name servers Azure has programmatic access to.
     */
    @JsonProperty(value = "properties.readyForDnsRecordManagement")
    private Boolean readyForDnsRecordManagement;

    /**
     * All hostnames derived from the domain and assigned to Azure resources.
     */
    @JsonProperty(value = "properties.managedHostNames")
    private List<HostName> managedHostNames;

    /**
     * Legal agreement consent.
     */
    @JsonProperty(value = "properties.consent")
    private DomainPurchaseConsent consent;

    /**
     * Reasons why domain is not renewable.
     */
    @JsonProperty(value = "properties.domainNotRenewableReasons")
    private List<String> domainNotRenewableReasons;

    /**
     * Get the domainRegistrationInputName value.
     *
     * @return the domainRegistrationInputName value
     */
    public String domainRegistrationInputName() {
        return this.domainRegistrationInputName;
    }

    /**
     * Set the domainRegistrationInputName value.
     *
     * @param domainRegistrationInputName the domainRegistrationInputName value to set
     * @return the DomainRegistrationInputInner object itself.
     */
    public DomainRegistrationInputInner withDomainRegistrationInputName(String domainRegistrationInputName) {
        this.domainRegistrationInputName = domainRegistrationInputName;
        return this;
    }

    /**
     * Get the contactAdmin value.
     *
     * @return the contactAdmin value
     */
    public Contact contactAdmin() {
        return this.contactAdmin;
    }

    /**
     * Set the contactAdmin value.
     *
     * @param contactAdmin the contactAdmin value to set
     * @return the DomainRegistrationInputInner object itself.
     */
    public DomainRegistrationInputInner withContactAdmin(Contact contactAdmin) {
        this.contactAdmin = contactAdmin;
        return this;
    }

    /**
     * Get the contactBilling value.
     *
     * @return the contactBilling value
     */
    public Contact contactBilling() {
        return this.contactBilling;
    }

    /**
     * Set the contactBilling value.
     *
     * @param contactBilling the contactBilling value to set
     * @return the DomainRegistrationInputInner object itself.
     */
    public DomainRegistrationInputInner withContactBilling(Contact contactBilling) {
        this.contactBilling = contactBilling;
        return this;
    }

    /**
     * Get the contactRegistrant value.
     *
     * @return the contactRegistrant value
     */
    public Contact contactRegistrant() {
        return this.contactRegistrant;
    }

    /**
     * Set the contactRegistrant value.
     *
     * @param contactRegistrant the contactRegistrant value to set
     * @return the DomainRegistrationInputInner object itself.
     */
    public DomainRegistrationInputInner withContactRegistrant(Contact contactRegistrant) {
        this.contactRegistrant = contactRegistrant;
        return this;
    }

    /**
     * Get the contactTech value.
     *
     * @return the contactTech value
     */
    public Contact contactTech() {
        return this.contactTech;
    }

    /**
     * Set the contactTech value.
     *
     * @param contactTech the contactTech value to set
     * @return the DomainRegistrationInputInner object itself.
     */
    public DomainRegistrationInputInner withContactTech(Contact contactTech) {
        this.contactTech = contactTech;
        return this;
    }

    /**
     * Get the registrationStatus value.
     *
     * @return the registrationStatus value
     */
    public DomainStatus registrationStatus() {
        return this.registrationStatus;
    }

    /**
     * Set the registrationStatus value.
     *
     * @param registrationStatus the registrationStatus value to set
     * @return the DomainRegistrationInputInner object itself.
     */
    public DomainRegistrationInputInner withRegistrationStatus(DomainStatus registrationStatus) {
        this.registrationStatus = registrationStatus;
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
     * @return the DomainRegistrationInputInner object itself.
     */
    public DomainRegistrationInputInner withProvisioningState(ProvisioningState provisioningState) {
        this.provisioningState = provisioningState;
        return this;
    }

    /**
     * Get the nameServers value.
     *
     * @return the nameServers value
     */
    public List<String> nameServers() {
        return this.nameServers;
    }

    /**
     * Set the nameServers value.
     *
     * @param nameServers the nameServers value to set
     * @return the DomainRegistrationInputInner object itself.
     */
    public DomainRegistrationInputInner withNameServers(List<String> nameServers) {
        this.nameServers = nameServers;
        return this;
    }

    /**
     * Get the privacy value.
     *
     * @return the privacy value
     */
    public Boolean privacy() {
        return this.privacy;
    }

    /**
     * Set the privacy value.
     *
     * @param privacy the privacy value to set
     * @return the DomainRegistrationInputInner object itself.
     */
    public DomainRegistrationInputInner withPrivacy(Boolean privacy) {
        this.privacy = privacy;
        return this;
    }

    /**
     * Get the createdTime value.
     *
     * @return the createdTime value
     */
    public DateTime createdTime() {
        return this.createdTime;
    }

    /**
     * Set the createdTime value.
     *
     * @param createdTime the createdTime value to set
     * @return the DomainRegistrationInputInner object itself.
     */
    public DomainRegistrationInputInner withCreatedTime(DateTime createdTime) {
        this.createdTime = createdTime;
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
     * @return the DomainRegistrationInputInner object itself.
     */
    public DomainRegistrationInputInner withExpirationTime(DateTime expirationTime) {
        this.expirationTime = expirationTime;
        return this;
    }

    /**
     * Get the lastRenewedTime value.
     *
     * @return the lastRenewedTime value
     */
    public DateTime lastRenewedTime() {
        return this.lastRenewedTime;
    }

    /**
     * Set the lastRenewedTime value.
     *
     * @param lastRenewedTime the lastRenewedTime value to set
     * @return the DomainRegistrationInputInner object itself.
     */
    public DomainRegistrationInputInner withLastRenewedTime(DateTime lastRenewedTime) {
        this.lastRenewedTime = lastRenewedTime;
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
     * @return the DomainRegistrationInputInner object itself.
     */
    public DomainRegistrationInputInner withAutoRenew(Boolean autoRenew) {
        this.autoRenew = autoRenew;
        return this;
    }

    /**
     * Get the readyForDnsRecordManagement value.
     *
     * @return the readyForDnsRecordManagement value
     */
    public Boolean readyForDnsRecordManagement() {
        return this.readyForDnsRecordManagement;
    }

    /**
     * Set the readyForDnsRecordManagement value.
     *
     * @param readyForDnsRecordManagement the readyForDnsRecordManagement value to set
     * @return the DomainRegistrationInputInner object itself.
     */
    public DomainRegistrationInputInner withReadyForDnsRecordManagement(Boolean readyForDnsRecordManagement) {
        this.readyForDnsRecordManagement = readyForDnsRecordManagement;
        return this;
    }

    /**
     * Get the managedHostNames value.
     *
     * @return the managedHostNames value
     */
    public List<HostName> managedHostNames() {
        return this.managedHostNames;
    }

    /**
     * Set the managedHostNames value.
     *
     * @param managedHostNames the managedHostNames value to set
     * @return the DomainRegistrationInputInner object itself.
     */
    public DomainRegistrationInputInner withManagedHostNames(List<HostName> managedHostNames) {
        this.managedHostNames = managedHostNames;
        return this;
    }

    /**
     * Get the consent value.
     *
     * @return the consent value
     */
    public DomainPurchaseConsent consent() {
        return this.consent;
    }

    /**
     * Set the consent value.
     *
     * @param consent the consent value to set
     * @return the DomainRegistrationInputInner object itself.
     */
    public DomainRegistrationInputInner withConsent(DomainPurchaseConsent consent) {
        this.consent = consent;
        return this;
    }

    /**
     * Get the domainNotRenewableReasons value.
     *
     * @return the domainNotRenewableReasons value
     */
    public List<String> domainNotRenewableReasons() {
        return this.domainNotRenewableReasons;
    }

    /**
     * Set the domainNotRenewableReasons value.
     *
     * @param domainNotRenewableReasons the domainNotRenewableReasons value to set
     * @return the DomainRegistrationInputInner object itself.
     */
    public DomainRegistrationInputInner withDomainNotRenewableReasons(List<String> domainNotRenewableReasons) {
        this.domainNotRenewableReasons = domainNotRenewableReasons;
        return this;
    }

}
