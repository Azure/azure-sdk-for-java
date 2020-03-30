/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.appservice.implementation;

import com.azure.management.appservice.AppServiceDomain;
import com.azure.management.appservice.Contact;
import com.azure.management.appservice.DomainPurchaseConsent;
import com.azure.management.appservice.DomainStatus;
import com.azure.management.appservice.HostName;
import com.azure.management.appservice.TopLevelDomainAgreementOption;
import com.azure.management.appservice.models.DomainInner;
import com.azure.management.appservice.models.DomainOwnershipIdentifierInner;
import com.azure.management.appservice.models.DomainsInner;
import com.azure.management.appservice.models.TldLegalAgreementInner;
import com.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.azure.management.resources.fluentcore.utils.Utils;
import reactor.core.publisher.Mono;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The implementation for AppServiceDomain.
 */
class AppServiceDomainImpl
        extends
        GroupableResourceImpl<
                AppServiceDomain,
                DomainInner,
                AppServiceDomainImpl,
                AppServiceManager>
        implements
        AppServiceDomain,
        AppServiceDomain.Definition,
        AppServiceDomain.Update {

    private Map<String, HostName> hostNameMap;

    AppServiceDomainImpl(String name, DomainInner innerObject, AppServiceManager manager) {
        super(name, innerObject, manager);
        inner().setLocation("global");
        if (inner().managedHostNames() != null) {
            this.hostNameMap = inner().managedHostNames().stream()
                    .collect(Collectors.toMap(HostName::name, Function.identity()));
        }
    }

    @Override
    public Mono<AppServiceDomain> createResourceAsync() {
        String[] domainParts = this.name().split("\\.");
        String topLevel = domainParts[domainParts.length - 1];
        final DomainsInner client = this.manager().inner().domains();
        return this.manager().inner().topLevelDomains().listAgreementsAsync(topLevel, new TopLevelDomainAgreementOption())
                // Step 1: Consent to agreements
                .mapPage(TldLegalAgreementInner::agreementKey)
                .collectList()
                // Step 2: Create domain
                .flatMap(keys -> {
                    try {
                        inner().withConsent(new DomainPurchaseConsent()
                                .withAgreedAt(OffsetDateTime.now())
                                .withAgreedBy(Inet4Address.getLocalHost().getHostAddress())
                                .withAgreementKeys(keys));
                    } catch (UnknownHostException e) {
                        return Mono.error(e);
                    }
                    return client.createOrUpdateAsync(resourceGroupName(), name(), inner());
                })
                .map(innerToFluentMap(this));
    }

    @Override
    protected Mono<DomainInner> getInnerAsync() {
        return this.manager().inner().domains().getByResourceGroupAsync(resourceGroupName(), name());
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
    public Map<String, HostName> managedHostNames() {
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
    public void verifyDomainOwnership(String certificateOrderName, String domainVerificationToken) {
        verifyDomainOwnershipAsync(certificateOrderName, domainVerificationToken).block();
    }

    @Override
    public Mono<Void> verifyDomainOwnershipAsync(String certificateOrderName, String domainVerificationToken) {
        DomainOwnershipIdentifierInner identifierInner = new DomainOwnershipIdentifierInner().withOwnershipId(domainVerificationToken);
        return this.manager().inner().domains().createOrUpdateOwnershipIdentifierAsync(resourceGroupName(), name(), certificateOrderName, identifierInner)
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
}