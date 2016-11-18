/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.website.implementation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.keyvault.SecretPermissions;
import com.microsoft.azure.management.keyvault.Vault;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.IndexableWrapperImpl;
import com.microsoft.azure.management.website.AppServiceCertificate;
import com.microsoft.azure.management.website.AppServiceCertificateKeyVaultBinding;
import com.microsoft.azure.management.website.AppServiceCertificateOrder;
import com.microsoft.azure.management.website.CertificateProductType;
import com.microsoft.azure.management.website.HostNameSslBinding;
import com.microsoft.azure.management.website.HostNameSslState;
import com.microsoft.azure.management.website.SslState;
import com.microsoft.azure.management.website.WebAppBase;
import com.microsoft.rest.serializer.JsonFlatten;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;

import java.io.File;
import java.util.Map;

/**
 *  Implementation for {@link HostNameSslBinding} and its create and update interfaces.
 */
@Fluent
class HostNameSslBindingImpl<
        FluentT extends WebAppBase<FluentT>,
        FluentImplT extends WebAppBaseImpl<FluentT, FluentImplT>>
    extends IndexableWrapperImpl<HostNameSslState>
    implements
        HostNameSslBinding,
        HostNameSslBinding.Definition<WebAppBase.DefinitionStages.WithHostNameSslBinding<FluentT>>,
        HostNameSslBinding.UpdateDefinition<WebAppBase.Update<FluentT>> {

    private Observable<AppServiceCertificate> newCertificate;
    private Observable<AppServiceCertificateOrder> newCertificateOrder;
    private final AppServiceManager manager;
    private final FluentImplT parent;
    private final VerifyDomainOwnershipService verifyDomainOwnershipService;

    HostNameSslBindingImpl(HostNameSslState inner, FluentImplT parent, AppServiceManager manager) {
        super(inner);
        this.parent = parent;
        this.manager = manager;
        this.verifyDomainOwnershipService = manager.restClient().retrofit().create(VerifyDomainOwnershipService.class);
    }

    @Override
    public String name() {
        return inner().name();
    }

    @Override
    public SslState sslState() {
        return inner().sslState();
    }

    @Override
    public String virtualIP() {
        return inner().virtualIP();
    }

    @Override
    public String thumbprint() {
        return inner().thumbprint();
    }

    @Override
    public FluentImplT attach() {
        parent.withNewHostNameSslBinding(this);
        return parent;
    }

    @Override
    public HostNameSslBindingImpl<FluentT, FluentImplT> withPfxCertificateToUpload(final File pfxFile, final String password) {
        newCertificate = manager.certificates().define(name() + "cert")
                .withRegion(parent().region())
                .withExistingResourceGroup(parent().resourceGroupName())
                .withPfxFile(pfxFile)
                .withPfxPassword(password)
                .createAsync();
        return this;
    }

    @Override
    public HostNameSslBindingImpl<FluentT, FluentImplT> withNewAppServiceCertificateOrder(final String certificateOrderName, CertificateProductType productType) {
        this.newCertificateOrder = manager.certificateOrders().define(certificateOrderName)
                .withExistingResourceGroup(parent().resourceGroupName())
                .withHostName(name())
                .withSku(productType)
                .withValidYears(1)
                .createAsync()
                .flatMap(new Func1<AppServiceCertificateOrder, Observable<AppServiceCertificateOrder>>() {
                    @Override
                    public Observable<AppServiceCertificateOrder> call(final AppServiceCertificateOrder appServiceCertificateOrder) {
                        return verifyDomainOwnershipService.verifyDomainOwnership(
                                manager.subscriptionId(), parent().resourceGroupName(), parent().name(),
                                certificateOrderName, new DomainOwnershipIdentifier().withOwnershipId(appServiceCertificateOrder.domainVerificationToken()),
                                "2016-08-01")
                                .map(new Func1<DomainOwnershipIdentifier, AppServiceCertificateOrder>() {
                                    @Override
                                    public AppServiceCertificateOrder call(DomainOwnershipIdentifier domainOwnershipIdentifier) {
                                        return appServiceCertificateOrder;
                                    }
                                });
                    }
                });
        return this;
    }

    @Override
    public HostNameSslBindingImpl<FluentT, FluentImplT> withReadyToUseAppServiceCertificateOrder(AppServiceCertificateOrder certificateOrder) {
        newCertificate = certificateOrder.getKeyVaultBindingAsync()
                .flatMap(new Func1<AppServiceCertificateKeyVaultBinding, Observable<AppServiceCertificate>>() {
                    @Override
                    public Observable<AppServiceCertificate> call(AppServiceCertificateKeyVaultBinding binding) {
                        return manager.certificates().define(name() + "cert")
                                .withRegion(parent().region())
                                .withExistingResourceGroup(parent().resourceGroupName())
                                .withCertificateOrderKeyVaultBinding(binding.keyVaultId(), binding.keyVaultSecretName())
                                .createAsync();
                    }
                });
        return this;
    }

    private HostNameSslBindingImpl<FluentT, FluentImplT> withCertificateThumbprint(String thumbprint) {
        inner().withThumbprint(thumbprint);
        return this;
    }

    @Override
    public HostNameSslBindingImpl<FluentT, FluentImplT> withSniBasedSsl() {
        inner().withSslState(SslState.SNI_ENABLED);
        return this;
    }

    @Override
    public HostNameSslBindingImpl<FluentT, FluentImplT> withIpBasedSsl() {
        inner().withSslState(SslState.IP_BASED_ENABLED);
        return this;
    }

    Observable<AppServiceCertificate> newCertificate() {
        return newCertificate.doOnNext(new Action1<AppServiceCertificate>() {
            @Override
            public void call(AppServiceCertificate appServiceCertificateOrder) {
                withCertificateThumbprint(appServiceCertificateOrder.thumbprint());
            }
        });
    }

    @Override
    public WebAppBase<FluentT> parent() {
        return parent;
    }

    @Override
    public HostNameSslBindingImpl<FluentT, FluentImplT> forHostname(String hostname) {
        inner().withName(hostname);
        return this;
    }

    private Observable<AppServiceCertificate> createBindingAndCertificate(final AppServiceCertificateOrder order, final Vault vault) {
        return order.createKeyVaultBindingAsync(order.name(), vault)
        .flatMap(new Func1<AppServiceCertificateKeyVaultBinding, Observable<AppServiceCertificate>>() {
            @Override
            public Observable<AppServiceCertificate> call(AppServiceCertificateKeyVaultBinding binding) {
                return manager.certificates().define(order.name())
                        .withRegion(parent().regionName())
                        .withExistingResourceGroup(parent().resourceGroupName())
                        .withCertificateOrderKeyVaultBinding(vault.id(), order.name())
                        .createAsync();
            }
        });
    }

    @Override
    public HostNameSslBindingImpl<FluentT, FluentImplT> withExistingKeyVault(final Vault vault) {
        newCertificate = newCertificateOrder
                .flatMap(new Func1<AppServiceCertificateOrder, Observable<AppServiceCertificate>>() {
                    @Override
                    public Observable<AppServiceCertificate> call(AppServiceCertificateOrder appServiceCertificateOrder) {
                        return createBindingAndCertificate(appServiceCertificateOrder, vault);
                    }
                });
        return this;
    }

    @Override
    public HostNameSslBindingImpl<FluentT, FluentImplT> withNewKeyVault(String vaultName) {
        Observable<Vault> vaultObservable = manager.keyVaultManager().vaults().define(vaultName)
                .withRegion(parent().region())
                .withExistingResourceGroup(parent().resourceGroupName())
                .defineAccessPolicy()
                    .forServicePrincipal("f3c21649-0979-4721-ac85-b0216b2cf413")
                    .allowSecretPermissions(SecretPermissions.GET, SecretPermissions.SET, SecretPermissions.DELETE)
                    .attach()
                .defineAccessPolicy()
                    .forServicePrincipal("abfa0a7c-a6b6-4736-8310-5855508787cd")
                    .allowSecretPermissions(SecretPermissions.GET)
                    .attach()
                .createAsync();
        newCertificate = Observable.zip(newCertificateOrder, vaultObservable, new Func2<AppServiceCertificateOrder, Vault, Map.Entry<AppServiceCertificateOrder, Vault>>() {
            @Override
            public Map.Entry<AppServiceCertificateOrder, Vault> call(AppServiceCertificateOrder appServiceCertificateOrder, Vault vault) {
                return Maps.immutableEntry(appServiceCertificateOrder, vault);
            }
        }).flatMap(new Func1<Map.Entry<AppServiceCertificateOrder, Vault>, Observable<AppServiceCertificate>>() {
            @Override
            public Observable<AppServiceCertificate> call(final Map.Entry<AppServiceCertificateOrder, Vault> entry) {
                return createBindingAndCertificate(entry.getKey(), entry.getValue());
            }
        });
        return this;
    }

    private interface VerifyDomainOwnershipService {
        @Headers("Content-Type: application/json; charset=utf-8")
        @PUT("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/sites/{name}/domainOwnershipIdentifiers/{domainOwnershipIdentifierName}")
        Observable<DomainOwnershipIdentifier> verifyDomainOwnership(@Path("subscriptionId") String subscriptionId, @Path("resourceGroupName") String resourceGroupName, @Path("name") String siteName, @Path("domainOwnershipIdentifierName") String domainOwnershipIdentifierName, @Body DomainOwnershipIdentifier domainOwnershipIdentifier, @Query("api-version") String apiVersion);
    }

    @JsonFlatten
    private static class DomainOwnershipIdentifier {
        @JsonProperty(value = "properties.id")
        private String ownershipId;

        private DomainOwnershipIdentifier withOwnershipId(String ownershipId) {
            this.ownershipId = ownershipId;
            return this;
        }
    }
}
