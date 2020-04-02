/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.appservice.implementation;

import com.azure.management.appservice.AppServiceCertificateKeyVaultBinding;
import com.azure.management.appservice.AppServiceCertificateOrder;
import com.azure.management.appservice.AppServiceDomain;
import com.azure.management.appservice.AppServicePlan;
import com.azure.management.appservice.CertificateDetails;
import com.azure.management.appservice.CertificateOrderStatus;
import com.azure.management.appservice.CertificateProductType;
import com.azure.management.appservice.WebAppBase;
import com.azure.management.appservice.models.AppServiceCertificateOrderInner;
import com.azure.management.appservice.models.AppServiceCertificateResourceInner;
import com.azure.management.keyvault.SecretPermissions;
import com.azure.management.keyvault.Vault;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.azure.management.resources.fluentcore.model.Indexable;
import com.azure.management.resources.fluentcore.utils.Utils;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

/**
 * The implementation for {@link AppServicePlan}.
 */
class AppServiceCertificateOrderImpl
        extends
        GroupableResourceImpl<
                AppServiceCertificateOrder,
                AppServiceCertificateOrderInner,
                AppServiceCertificateOrderImpl,
                AppServiceManager>
        implements
        AppServiceCertificateOrder,
        AppServiceCertificateOrder.Definition,
        AppServiceCertificateOrder.Update {

    private WebAppBase domainVerifyWebApp;
    private AppServiceDomain domainVerifyDomain;
    private Mono<Vault> bindingVault;

    AppServiceCertificateOrderImpl(
            String key,
            AppServiceCertificateOrderInner innerObject,
            AppServiceManager manager) {
        super(key, innerObject, manager);
        this.withRegion("global").withValidYears(1);
    }

    @Override
    protected Mono<AppServiceCertificateOrderInner> getInnerAsync() {
        return this.manager().inner().appServiceCertificateOrders().getByResourceGroupAsync(resourceGroupName(), name());
    }

    @Override
    public AppServiceCertificateKeyVaultBinding getKeyVaultBinding() {
        return getKeyVaultBindingAsync().block();
    }

    @Override
    public Mono<AppServiceCertificateKeyVaultBinding> getKeyVaultBindingAsync() {
        return this.manager().inner().appServiceCertificateOrders().listCertificatesAsync(resourceGroupName(), name())
                .switchIfEmpty(Mono.empty())
                .take(1).singleOrEmpty()
                .map(inner -> new AppServiceCertificateKeyVaultBindingImpl(inner, this));
    }

    @Override
    public void verifyDomainOwnership(AppServiceDomain domain) {
        verifyDomainOwnershipAsync(domain).block();
    }

    @Override
    public Mono<Void> verifyDomainOwnershipAsync(AppServiceDomain domain) {
        return domain.verifyDomainOwnershipAsync(name(), domainVerificationToken());
    }

    @Override
    public String distinguishedName() {
        return inner().distinguishedName();
    }

    @Override
    public String domainVerificationToken() {
        return inner().domainVerificationToken();
    }

    @Override
    public int validityInYears() {
        return Utils.toPrimitiveInt(inner().validityInYears());
    }

    @Override
    public int keySize() {
        return Utils.toPrimitiveInt(inner().keySize());
    }

    @Override
    public CertificateProductType productType() {
        return inner().productType();
    }

    @Override
    public boolean autoRenew() {
        return Utils.toPrimitiveBoolean(inner().autoRenew());
    }

    @Override
    public CertificateOrderStatus status() {
        return inner().status();
    }

    @Override
    public CertificateDetails signedCertificate() {
        return inner().signedCertificate();
    }

    @Override
    public String certificateSigningRequest() {
        return inner().csr();
    }

    @Override
    public CertificateDetails intermediate() {
        return inner().intermediate();
    }

    @Override
    public CertificateDetails root() {
        return inner().root();
    }

    @Override
    public String serialNumber() {
        return inner().serialNumber();
    }

    @Override
    public OffsetDateTime lastCertificateIssuanceTime() {
        return inner().lastCertificateIssuanceTime();
    }

    @Override
    public OffsetDateTime expirationTime() {
        return inner().expirationTime();
    }

    @Override
    public AppServiceCertificateKeyVaultBinding createKeyVaultBinding(String certificateName, Vault vault) {
        return createKeyVaultBindingAsync(certificateName, vault).block();
    }

    @Override
    public Mono<AppServiceCertificateKeyVaultBinding> createKeyVaultBindingAsync(String certificateName, Vault vault) {
        AppServiceCertificateResourceInner certInner = new AppServiceCertificateResourceInner();
        certInner.setLocation(vault.regionName());
        certInner.withKeyVaultId(vault.id());
        certInner.withKeyVaultSecretName(certificateName);
        return this.manager().inner().appServiceCertificateOrders().createOrUpdateCertificateAsync(
                resourceGroupName(), name(), certificateName, certInner)
                .map(appServiceCertificateInner -> new AppServiceCertificateKeyVaultBindingImpl(appServiceCertificateInner, this));
    }

    @Override
    public AppServiceCertificateOrderImpl withHostName(String hostName) {
        inner().withDistinguishedName("CN=" + hostName);
        return this;
    }

    @Override
    public AppServiceCertificateOrderImpl withStandardSku() {
        inner().withProductType(CertificateProductType.STANDARD_DOMAIN_VALIDATED_SSL);
        return this;
    }

    @Override
    public AppServiceCertificateOrderImpl withWildcardSku() {
        inner().withProductType(CertificateProductType.STANDARD_DOMAIN_VALIDATED_WILD_CARD_SSL);
        return this;
    }

    @Override
    public AppServiceCertificateOrderImpl withValidYears(int years) {
        inner().withValidityInYears(years);
        return this;
    }

    @Override
    public Mono<AppServiceCertificateOrder> createResourceAsync() {
        return this.manager().inner().appServiceCertificateOrders().createOrUpdateAsync(
                resourceGroupName(), name(), inner())
                .map(innerToFluentMap(this))
                .then(Mono.defer(() -> {
                    if (domainVerifyWebApp != null) {
                        return domainVerifyWebApp.verifyDomainOwnershipAsync(name(), domainVerificationToken());
                    } else if (domainVerifyDomain != null) {
                        return domainVerifyDomain.verifyDomainOwnershipAsync(name(), domainVerificationToken());
                    } else {
                        return Mono.error(new IllegalArgumentException(
                                "Please specify a non-null web app or domain to verify the domain ownership "
                                        + "for hostname " + distinguishedName()));
                    }
                }))
                .then(bindingVault.flatMap(vault -> createKeyVaultBindingAsync(name(), vault)))
                .then(Mono.just(this));
    }

    @Override
    public AppServiceCertificateOrderImpl withAutoRenew(boolean enabled) {
        inner().withAutoRenew(enabled);
        return this;
    }

    @Override
    public AppServiceCertificateOrderImpl withDomainVerification(AppServiceDomain domain) {
        this.domainVerifyDomain = domain;
        return this;
    }

    @Override
    public AppServiceCertificateOrderImpl withWebAppVerification(WebAppBase webApp) {
        this.domainVerifyWebApp = webApp;
        return this;
    }

    @Override
    public AppServiceCertificateOrderImpl withExistingKeyVault(Vault vault) {
        this.bindingVault = Mono.just(vault);
        return this;
    }

    @Override
    public AppServiceCertificateOrderImpl withNewKeyVault(String vaultName, Region region) {
        Mono<Indexable> resourceStream = myManager.keyVaultManager().vaults().define(vaultName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroupName())
                .defineAccessPolicy()
                    .forServicePrincipal("f3c21649-0979-4721-ac85-b0216b2cf413")
                    .allowSecretPermissions(SecretPermissions.GET, SecretPermissions.SET, SecretPermissions.DELETE)
                    .attach()
                .defineAccessPolicy()
                    .forServicePrincipal("abfa0a7c-a6b6-4736-8310-5855508787cd")
                    .allowSecretPermissions(SecretPermissions.GET)
                    .attach()
                .createAsync().last();
        this.bindingVault = Utils.rootResource(resourceStream);
        return this;
    }
}