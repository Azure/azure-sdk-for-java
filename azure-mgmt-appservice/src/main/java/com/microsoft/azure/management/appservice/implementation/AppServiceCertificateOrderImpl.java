/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice.implementation;

import com.microsoft.azure.Page;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.appservice.CertificateDetails;
import com.microsoft.azure.management.keyvault.SecretPermissions;
import com.microsoft.azure.management.keyvault.Vault;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import com.microsoft.azure.management.appservice.AppServiceCertificateKeyVaultBinding;
import com.microsoft.azure.management.appservice.AppServiceCertificateOrder;
import com.microsoft.azure.management.appservice.AppServiceDomain;
import com.microsoft.azure.management.appservice.AppServicePlan;
import com.microsoft.azure.management.appservice.CertificateOrderStatus;
import com.microsoft.azure.management.appservice.CertificateProductType;
import com.microsoft.azure.management.appservice.WebAppBase;
import org.joda.time.DateTime;
import rx.Completable;
import rx.Observable;
import rx.functions.Func1;

/**
 * The implementation for {@link AppServicePlan}.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.AppService.Fluent")
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
    private Observable<Vault> bindingVault;

    AppServiceCertificateOrderImpl(
            String key,
            AppServiceCertificateOrderInner innerObject,
            AppServiceManager manager) {
        super(key, innerObject, manager);
        this.withRegion("global").withValidYears(1);
    }

    @Override
    protected Observable<AppServiceCertificateOrderInner> getInnerAsync() {
        return this.manager().inner().appServiceCertificateOrders().getByResourceGroupAsync(resourceGroupName(), name());
    }

    @Override
    public AppServiceCertificateKeyVaultBinding getKeyVaultBinding() {
        return getKeyVaultBindingAsync().toBlocking().single();
    }

    @Override
    public Observable<AppServiceCertificateKeyVaultBinding> getKeyVaultBindingAsync() {
        final AppServiceCertificateOrderImpl self = this;
        return this.manager().inner().appServiceCertificateOrders().listCertificatesAsync(resourceGroupName(), name())
                .map(new Func1<Page<AppServiceCertificateResourceInner>, AppServiceCertificateKeyVaultBinding>() {
                    @Override
                    public AppServiceCertificateKeyVaultBinding call(Page<AppServiceCertificateResourceInner> appServiceCertificateInnerPage) {
                        // There can only be one binding associated with an order
                        if (appServiceCertificateInnerPage.items() == null || appServiceCertificateInnerPage.items().isEmpty()) {
                            return null;
                        } else {
                            return new AppServiceCertificateKeyVaultBindingImpl(appServiceCertificateInnerPage.items().get(0), self);
                        }
                    }
                });
    }

    @Override
    public void verifyDomainOwnership(AppServiceDomain domain) {
        verifyDomainOwnershipAsync(domain).toObservable().toBlocking().subscribe();
    }

    @Override
    public Completable verifyDomainOwnershipAsync(AppServiceDomain domain) {
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
    public DateTime lastCertificateIssuanceTime() {
        return inner().lastCertificateIssuanceTime();
    }

    @Override
    public DateTime expirationTime() {
        return inner().expirationTime();
    }

    @Override
    public AppServiceCertificateKeyVaultBinding createKeyVaultBinding(String certificateName, Vault vault) {
        return createKeyVaultBindingAsync(certificateName, vault).toBlocking().single();
    }

    @Override
    public Observable<AppServiceCertificateKeyVaultBinding> createKeyVaultBindingAsync(String certificateName, Vault vault) {
        AppServiceCertificateResourceInner certInner = new AppServiceCertificateResourceInner();
        certInner.withLocation(vault.regionName());
        certInner.withKeyVaultId(vault.id());
        certInner.withKeyVaultSecretName(certificateName);
        final AppServiceCertificateOrderImpl self = this;
        return this.manager().inner().appServiceCertificateOrders().createOrUpdateCertificateAsync(
                resourceGroupName(), name(), certificateName, certInner)
                .map(new Func1<AppServiceCertificateResourceInner, AppServiceCertificateKeyVaultBinding>() {
                    @Override
                    public AppServiceCertificateKeyVaultBinding call(AppServiceCertificateResourceInner appServiceCertificateInner) {
                        return new AppServiceCertificateKeyVaultBindingImpl(appServiceCertificateInner, self);
                    }
                });
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
    public Observable<AppServiceCertificateOrder> createResourceAsync() {
        final AppServiceCertificateOrder self = this;
        return this.manager().inner().appServiceCertificateOrders().createOrUpdateAsync(
                resourceGroupName(), name(), inner())
                .map(innerToFluentMap(this))
                .flatMap(new Func1<AppServiceCertificateOrder, Observable<Void>>() {
                    @Override
                    public Observable<Void> call(AppServiceCertificateOrder certificateOrder) {
                        if (domainVerifyWebApp != null) {
                            return domainVerifyWebApp.verifyDomainOwnershipAsync(name(), domainVerificationToken()).toObservable();
                        } else if (domainVerifyDomain != null) {
                            return domainVerifyDomain.verifyDomainOwnershipAsync(name(), domainVerificationToken()).toObservable();
                        } else {
                            throw new IllegalArgumentException(
                                    "Please specify a non-null web app or domain to verify the domain ownership "
                                            + "for hostname " + distinguishedName());
                        }
                    }
                })
                .flatMap(new Func1<Void, Observable<AppServiceCertificateKeyVaultBinding>>() {
                    @Override
                    public Observable<AppServiceCertificateKeyVaultBinding> call(Void aVoid) {
                        return bindingVault.flatMap(new Func1<Vault, Observable<AppServiceCertificateKeyVaultBinding>>() {
                            @Override
                            public Observable<AppServiceCertificateKeyVaultBinding> call(Vault vault) {
                                return createKeyVaultBindingAsync(name(), vault);
                            }
                        });
                    }
                })
                .map(new Func1<AppServiceCertificateKeyVaultBinding, AppServiceCertificateOrder>() {
                    @Override
                    public AppServiceCertificateOrder call(AppServiceCertificateKeyVaultBinding appServiceCertificateKeyVaultBinding) {
                        return self;
                    }
                });
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
        this.bindingVault = Observable.just(vault);
        return this;
    }

    @Override
    public AppServiceCertificateOrderImpl withNewKeyVault(String vaultName, Region region) {
        Observable<Indexable> resourceStream = myManager.keyVaultManager().vaults().define(vaultName)
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
                .createAsync();
        this.bindingVault = Utils.rootResource(resourceStream);
        return this;
    }
}