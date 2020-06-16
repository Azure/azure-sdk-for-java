// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import com.azure.resourcemanager.appservice.AppServiceManager;
import com.azure.resourcemanager.appservice.models.AppServiceDomain;
import com.azure.resourcemanager.appservice.models.Contact;
import com.azure.resourcemanager.appservice.models.DnsType;
import com.azure.resourcemanager.appservice.models.DomainPurchaseConsent;
import com.azure.resourcemanager.appservice.models.DomainStatus;
import com.azure.resourcemanager.appservice.models.Hostname;
import com.azure.resourcemanager.appservice.models.TopLevelDomainAgreementOption;
import com.azure.resourcemanager.appservice.fluent.inner.DomainInner;
import com.azure.resourcemanager.appservice.fluent.inner.DomainOwnershipIdentifierInner;
import com.azure.resourcemanager.appservice.fluent.DomainsClient;
import com.azure.resourcemanager.appservice.fluent.inner.TldLegalAgreementInner;
import com.azure.resourcemanager.dns.models.DnsZone;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** The implementation for AppServiceDomain. */
class AppServiceDomainImpl
    extends GroupableResourceImpl<AppServiceDomain, DomainInner, AppServiceDomainImpl, AppServiceManager>
    implements AppServiceDomain, AppServiceDomain.Definition, AppServiceDomain.Update {

    private Map<String, Hostname> hostNameMap;

    private Creatable<DnsZone> dnsZoneCreatable;

    AppServiceDomainImpl(String name, DomainInner innerObject, AppServiceManager manager) {
        super(name, innerObject, manager);
        inner().withLocation("global");
        if (inner().managedHostNames() != null) {
            this.hostNameMap =
                inner().managedHostNames().stream().collect(Collectors.toMap(Hostname::name, Function.identity()));
        }
    }

    @Override
    public Flux<Indexable> createAsync() {
        if (this.isInCreateMode()) {
            // create a default DNS zone, if not specified
            if (this.inner().dnsZoneId() == null && dnsZoneCreatable == null) {
                this.withNewDnsZone(name());
            }
        }
        return super.createAsync();
    }
    @Override
    public Mono<AppServiceDomain> createResourceAsync() {
        if (this.dnsZoneCreatable != null) {
            DnsZone dnsZone = this.taskResult(dnsZoneCreatable.key());
            inner().withDnsZoneId(dnsZone.id());
        }

        String[] domainParts = this.name().split("\\.");
        String topLevel = domainParts[domainParts.length - 1];
        final DomainsClient client = this.manager().inner().getDomains();
        return this
            .manager()
            .inner()
            .getTopLevelDomains()
            .listAgreementsAsync(topLevel, new TopLevelDomainAgreementOption())
            // Step 1: Consent to agreements
            .mapPage(TldLegalAgreementInner::agreementKey)
            .collectList()
            // Step 2: Create domain
            .flatMap(
                keys -> {
                    try {
                        inner()
                            .withConsent(
                                new DomainPurchaseConsent()
                                    .withAgreedAt(OffsetDateTime.now())
                                    .withAgreedBy(Inet4Address.getLocalHost().getHostAddress())
                                    .withAgreementKeys(keys));
                    } catch (UnknownHostException e) {
                        return Mono.error(e);
                    }
                    return client.createOrUpdateAsync(resourceGroupName(), name(), inner());
                })
            .map(innerToFluentMap(this))
            .doOnSuccess(ignored -> dnsZoneCreatable = null);
    }

    @Override
    protected Mono<DomainInner> getInnerAsync() {
        return this.manager().inner().getDomains().getByResourceGroupAsync(resourceGroupName(), name());
    }

    @Override
    public Contact adminContact() {
        return inner().contactAdmin();
    }

    @Override
    public Contact billingContact() {
        return inner().contactBilling();
    }

    @Override
    public Contact registrantContact() {
        return inner().contactRegistrant();
    }

    @Override
    public Contact techContact() {
        return inner().contactTech();
    }

    @Override
    public DomainStatus registrationStatus() {
        return inner().registrationStatus();
    }

    @Override
    public List<String> nameServers() {
        return Collections.unmodifiableList(inner().nameServers());
    }

    @Override
    public boolean privacy() {
        return Utils.toPrimitiveBoolean(inner().privacy());
    }

    @Override
    public OffsetDateTime createdTime() {
        return inner().createdTime();
    }

    @Override
    public OffsetDateTime expirationTime() {
        return inner().expirationTime();
    }

    @Override
    public OffsetDateTime lastRenewedTime() {
        return inner().lastRenewedTime();
    }

    @Override
    public boolean autoRenew() {
        return Utils.toPrimitiveBoolean(inner().autoRenew());
    }

    @Override
    public boolean readyForDnsRecordManagement() {
        return Utils.toPrimitiveBoolean(inner().readyForDnsRecordManagement());
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
        return inner().consent();
    }

    @Override
    public DnsType dnsType() {
        return inner().dnsType();
    }

    @Override
    public String dnsZoneId() {
        return inner().dnsZoneId();
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
            .inner()
            .getDomains()
            .createOrUpdateOwnershipIdentifierAsync(resourceGroupName(), name(), certificateOrderName, identifierInner)
            .then(Mono.empty());
    }

    @Override
    public AppServiceDomainImpl withAdminContact(Contact contact) {
        inner().withContactAdmin(contact);
        return this;
    }

    @Override
    public AppServiceDomainImpl withBillingContact(Contact contact) {
        inner().withContactBilling(contact);
        return this;
    }

    @Override
    public AppServiceDomainImpl withRegistrantContact(Contact contact) {
        inner().withContactAdmin(contact);
        inner().withContactBilling(contact);
        inner().withContactRegistrant(contact);
        inner().withContactTech(contact);
        return this;
    }

    @Override
    public DomainContactImpl defineRegistrantContact() {
        return new DomainContactImpl(new Contact(), this);
    }

    @Override
    public AppServiceDomainImpl withTechContact(Contact contact) {
        inner().withContactTech(contact);
        return this;
    }

    @Override
    public AppServiceDomainImpl withDomainPrivacyEnabled(boolean domainPrivacy) {
        inner().withPrivacy(domainPrivacy);
        return this;
    }

    @Override
    public AppServiceDomainImpl withAutoRenewEnabled(boolean autoRenew) {
        inner().withAutoRenew(autoRenew);
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
        inner().withDnsType(DnsType.AZURE_DNS);
        dnsZoneCreatable = dnsZone;
        this.addDependency(dnsZoneCreatable);
        return this;
    }

    @Override
    public AppServiceDomainImpl withExistingDnsZone(String dnsZoneId) {
        inner().withDnsType(DnsType.AZURE_DNS);
        inner().withDnsZoneId(dnsZoneId);
        return this;
    }

    @Override
    public AppServiceDomainImpl withExistingDnsZone(DnsZone dnsZone) {
        return withExistingDnsZone(dnsZone.id());
    }
}
