/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice.implementation;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.microsoft.azure.Page;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import com.microsoft.azure.management.appservice.AppServiceDomain;
import com.microsoft.azure.management.appservice.Contact;
import com.microsoft.azure.management.appservice.DomainPurchaseConsent;
import com.microsoft.azure.management.appservice.DomainStatus;
import com.microsoft.azure.management.appservice.HostName;
import org.joda.time.DateTime;
import rx.Observable;
import rx.functions.Func1;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The implementation for {@link AppServiceDomain}.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.AppService.Fluent")
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

    private final DomainsInner client;
    private final TopLevelDomainsInner topLevelDomainsInner;

    private Map<String, HostName> hostNameMap;

    AppServiceDomainImpl(String name, DomainInner innerObject, final DomainsInner client, final TopLevelDomainsInner topLevelDomainsInner, AppServiceManager manager) {
        super(name, innerObject, manager);
        this.client = client;
        this.topLevelDomainsInner = topLevelDomainsInner;
        inner().withLocation("global");
        if (inner().managedHostNames() != null) {
            this.hostNameMap = Maps.uniqueIndex(inner().managedHostNames(), new Function<HostName, String>() {
                @Override
                public String apply(HostName input) {
                    return input.name();
                }
            });
        }
    }

    @Override
    public Observable<AppServiceDomain> createResourceAsync() {
        String[] domainParts = this.name().split("\\.");
        String topLevel = domainParts[domainParts.length - 1];
        return topLevelDomainsInner.listAgreementsAsync(topLevel)
                // Step 1: Consent to agreements
                .flatMap(new Func1<Page<TldLegalAgreementInner>, Observable<List<String>>>() {
                    @Override
                    public Observable<List<String>> call(Page<TldLegalAgreementInner> tldLegalAgreementInnerPage) {
                        List<String> agreementKeys = new ArrayList<String>();
                        for (TldLegalAgreementInner agreementInner : tldLegalAgreementInnerPage.items()) {
                            agreementKeys.add(agreementInner.agreementKey());
                        }
                        return Observable.just(agreementKeys);
                    }
                })
                // Step 2: Create domain
                .flatMap(new Func1<List<String>, Observable<DomainInner>>() {
                    @Override
                    public Observable<DomainInner> call(List<String> keys) {
                        try {
                            inner().withConsent(new DomainPurchaseConsent()
                                    .withAgreedAt(new DateTime())
                                    .withAgreedBy(Inet4Address.getLocalHost().getHostAddress())
                                    .withAgreementKeys(keys));
                        } catch (UnknownHostException e) {
                            return Observable.error(e);
                        }
                        return client.createOrUpdateAsync(resourceGroupName(), name(), inner());
                    }
                })
                .map(innerToFluentMap(this));
    }

    @Override
    public AppServiceDomainImpl refresh() {
        this.setInner(client.get(resourceGroupName(), name()));
        return this;
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
    public DateTime createdTime() {
        return inner().createdTime();
    }

    @Override
    public DateTime expirationTime() {
        return inner().expirationTime();
    }

    @Override
    public DateTime lastRenewedTime() {
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
        verifyDomainOwnershipAsync(certificateOrderName, domainVerificationToken).toBlocking().subscribe();
    }

    @Override
    public Observable<Void> verifyDomainOwnershipAsync(String certificateOrderName, String domainVerificationToken) {
        DomainOwnershipIdentifierInner identifierInner = new DomainOwnershipIdentifierInner().withOwnershipId(domainVerificationToken);
        identifierInner.withLocation("global");
        return client.createOrUpdateOwnershipIdentifierAsync(resourceGroupName(), name(), certificateOrderName, identifierInner)
                .map(new Func1<DomainOwnershipIdentifierInner, Void>() {
                    @Override
                    public Void call(DomainOwnershipIdentifierInner domainOwnershipIdentifierInner) {
                        return null;
                    }
                });
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