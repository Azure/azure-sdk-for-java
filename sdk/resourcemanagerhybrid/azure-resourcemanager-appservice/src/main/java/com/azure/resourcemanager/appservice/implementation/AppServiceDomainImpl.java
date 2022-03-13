// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import com.azure.resourcemanager.appservice.AppServiceManager;
import com.azure.resourcemanager.appservice.fluent.DomainsClient;
import com.azure.resourcemanager.appservice.fluent.models.DomainInner;
import com.azure.resourcemanager.appservice.fluent.models.DomainOwnershipIdentifierInner;
import com.azure.resourcemanager.appservice.fluent.models.TldLegalAgreementInner;
import com.azure.resourcemanager.appservice.models.AppServiceDomain;
import com.azure.resourcemanager.appservice.models.Contact;
import com.azure.resourcemanager.appservice.models.DnsType;
import com.azure.resourcemanager.appservice.models.DomainPurchaseConsent;
import com.azure.resourcemanager.appservice.models.DomainStatus;
import com.azure.resourcemanager.appservice.models.Hostname;
import com.azure.resourcemanager.appservice.models.TopLevelDomainAgreementOption;
import com.azure.resourcemanager.dns.models.DnsZone;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import reactor.core.publisher.Mono;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** The implementation for AppServiceDomain. */
class AppServiceDomainImpl
    extends GroupableResourceImpl<AppServiceDomain, DomainInner, AppServiceDomainImpl, AppServiceManager>
    implements AppServiceDomain, AppServiceDomain.Definition, AppServiceDomain.Update {

    private Map<String, Hostname> hostNameMap;

    private Creatable<DnsZone> dnsZoneCreatable;

    AppServiceDomainImpl(String name, DomainInner innerObject, AppServiceManager manager) {
        super(name, innerObject, manager);
        innerModel().withLocation("global");
        if (innerModel().managedHostNames() != null) {
            this.hostNameMap =
                innerModel().managedHostNames().stream().collect(Collectors.toMap(Hostname::name, Function.identity()));
        }
    }

    @Override
    public Mono<AppServiceDomain> createAsync() {
        if (this.isInCreateMode()) {
            // create a default DNS zone, if not specified
            if (this.innerModel().dnsZoneId() == null && dnsZoneCreatable == null) {
                this.withNewDnsZone(name());
            }
        }
        return super.createAsync();
    }
    @Override
    public Mono<AppServiceDomain> createResourceAsync() {
        if (this.dnsZoneCreatable != null) {
            DnsZone dnsZone = this.taskResult(dnsZoneCreatable.key());
            innerModel().withDnsZoneId(dnsZone.id());
        }

        String[] domainParts = this.name().split("\\.");
        String topLevel = domainParts[domainParts.length - 1];
        final DomainsClient client = this.manager().serviceClient().getDomains();
        return this
            .manager()
            .serviceClient()
            .getTopLevelDomains()
            .listAgreementsAsync(topLevel, new TopLevelDomainAgreementOption())
            // Step 1: Consent to agreements,
            .map(TldLegalAgreementInner::agreementKey)
            .collectList()
            // Step 2: Create domain
            .flatMap(
                keys -> {
                    try {
                        innerModel()
                            .withConsent(
                                new DomainPurchaseConsent()
                                    .withAgreedAt(OffsetDateTime.now())
                                    .withAgreedBy(Inet4Address.getLocalHost().getHostAddress())
                                    .withAgreementKeys(keys));
                    } catch (UnknownHostException e) {
                        return Mono.error(e);
                    }
                    return client.createOrUpdateAsync(resourceGroupName(), name(), innerModel());
                })
            .map(innerToFluentMap(this))
            .doOnSuccess(ignored -> dnsZoneCreatable = null);
    }

    @Override
    protected Mono<DomainInner> getInnerAsync() {
        return this.manager().serviceClient().getDomains().getByResourceGroupAsync(resourceGroupName(), name());
    }

    @Override
    public Contact adminContact() {
        return innerModel().contactAdmin();
    }

    @Override
    public Contact billingContact() {
        return innerModel().contactBilling();
    }

    @Override
    public Contact registrantContact() {
        return innerModel().contactRegistrant();
    }

    @Override
    public Contact techContact() {
        return innerModel().contactTech();
    }

    @Override
    public DomainStatus registrationStatus() {
        return innerModel().registrationStatus();
    }

    @Override
    public List<String> nameServers() {
        return Collections.unmodifiableList(innerModel().nameServers());
    }

    @Override
    public boolean privacy() {
        return ResourceManagerUtils.toPrimitiveBoolean(innerModel().privacy());
    }

    @Override
    public OffsetDateTime createdTime() {
        return innerModel().createdTime();
    }

    @Override
    public OffsetDateTime expirationTime() {
        return innerModel().expirationTime();
    }

    @Override
    public OffsetDateTime lastRenewedTime() {
        return innerModel().lastRenewedTime();
    }

    @Override
    public boolean autoRenew() {
        return ResourceManagerUtils.toPrimitiveBoolean(innerModel().autoRenew());
    }

    @Override
    public boolean readyForDnsRecordManagement() {
        return ResourceManagerUtils.toPrimitiveBoolean(innerModel().readyForDnsRecordManagement());
    }

    @Override
    public Map<String, Hostname> managedHostNames() {
        if (hostNameMap == null) {
            return null;
        }
        return Collections.unmodifiableMap(hostNameMap);
    }

    @Override
    public DomainPurchaseConsent consent() {
        return innerModel().consent();
    }

    @Override
    public DnsType dnsType() {
        return innerModel().dnsType();
    }

    @Override
    public String dnsZoneId() {
        return innerModel().dnsZoneId();
    }

    @Override
    public void verifyDomainOwnership(String certificateOrderName, String domainVerificationToken) {
        verifyDomainOwnershipAsync(certificateOrderName, domainVerificationToken).block();
    }

    @Override
    public Mono<Void> verifyDomainOwnershipAsync(String certificateOrderName, String domainVerificationToken) {
        DomainOwnershipIdentifierInner identifierInner =
            new DomainOwnershipIdentifierInner().withOwnershipId(domainVerificationToken);
        return this
            .manager()
            .serviceClient()
            .getDomains()
            .createOrUpdateOwnershipIdentifierAsync(resourceGroupName(), name(), certificateOrderName, identifierInner)
            .then(Mono.empty());
    }

    @Override
    public AppServiceDomainImpl withAdminContact(Contact contact) {
        innerModel().withContactAdmin(contact);
        return this;
    }

    @Override
    public AppServiceDomainImpl withBillingContact(Contact contact) {
        innerModel().withContactBilling(contact);
        return this;
    }

    @Override
    public AppServiceDomainImpl withRegistrantContact(Contact contact) {
        innerModel().withContactAdmin(contact);
        innerModel().withContactBilling(contact);
        innerModel().withContactRegistrant(contact);
        innerModel().withContactTech(contact);
        return this;
    }

    @Override
    public DomainContactImpl defineRegistrantContact() {
        return new DomainContactImpl(new Contact(), this);
    }

    @Override
    public AppServiceDomainImpl withTechContact(Contact contact) {
        innerModel().withContactTech(contact);
        return this;
    }

    @Override
    public AppServiceDomainImpl withDomainPrivacyEnabled(boolean domainPrivacy) {
        innerModel().withPrivacy(domainPrivacy);
        return this;
    }

    @Override
    public AppServiceDomainImpl withAutoRenewEnabled(boolean autoRenew) {
        innerModel().withAutoRenew(autoRenew);
        return this;
    }

    @Override
    public AppServiceDomainImpl withNewDnsZone(String dnsZoneName) {
        Creatable<DnsZone> dnsZone;
        if (creatableGroup != null && isInCreateMode()) {
            dnsZone = manager().dnsZoneManager().zones()
                .define(dnsZoneName)
                .withNewResourceGroup(creatableGroup)
                .withETagCheck();
        } else {
            dnsZone = manager().dnsZoneManager().zones()
                .define(dnsZoneName)
                .withExistingResourceGroup(resourceGroupName())
                .withETagCheck();
        }
        return this.withNewDnsZone(dnsZone);
    }

    @Override
    public AppServiceDomainImpl withNewDnsZone(Creatable<DnsZone> dnsZone) {
        innerModel().withDnsType(DnsType.AZURE_DNS);
        dnsZoneCreatable = dnsZone;
        this.addDependency(dnsZoneCreatable);
        return this;
    }

    @Override
    public AppServiceDomainImpl withExistingDnsZone(String dnsZoneId) {
        innerModel().withDnsType(DnsType.AZURE_DNS);
        innerModel().withDnsZoneId(dnsZoneId);
        return this;
    }

    @Override
    public AppServiceDomainImpl withExistingDnsZone(DnsZone dnsZone) {
        return withExistingDnsZone(dnsZone.id());
    }
}
