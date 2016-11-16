/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.website.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.keyvault.SecretPermissions;
import com.microsoft.azure.management.keyvault.Vault;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.IndexableWrapperImpl;
import com.microsoft.azure.management.website.AppServiceCertificate;
import com.microsoft.azure.management.website.AppServiceCertificateKeyVaultBinding;
import com.microsoft.azure.management.website.AppServiceCertificateOrder;
import com.microsoft.azure.management.website.CertificateProductType;
import com.microsoft.azure.management.website.HostNameSslBinding;
import com.microsoft.azure.management.website.HostNameSslState;
import com.microsoft.azure.management.website.SslState;
import com.microsoft.azure.management.website.WebAppBase;
import rx.Observable;
import rx.functions.Func1;

import java.io.File;

/**
 *  Implementation for {@link HostNameSslBinding} and its create and update interfaces.
 */
@LangDefinition
class HostNameSslBindingImpl<
        FluentT extends WebAppBase<FluentT>,
        FluentImplT extends WebAppBaseImpl<FluentT, FluentImplT>>
    extends IndexableWrapperImpl<HostNameSslState>
    implements
        HostNameSslBinding,
        HostNameSslBinding.Definition<WebAppBase.DefinitionStages.WithHostNameSslBinding<FluentT>>,
        HostNameSslBinding.UpdateDefinition<WebAppBase.Update<FluentT>> {

    private CreatableUpdatableImpl<AppServiceCertificate, CertificateInner, AppServiceCertificateImpl> newCertificate;
    private CreatableUpdatableImpl<AppServiceCertificateOrder, AppServiceCertificateOrderInner, AppServiceCertificateOrderImpl> newCertificateOrder;
    private final AppServiceManager manager;
    private final FluentImplT parent;

    HostNameSslBindingImpl(HostNameSslState inner, FluentImplT parent, AppServiceManager manager) {
        super(inner);
        this.parent = parent;
        this.manager = manager;
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
        final HostNameSslBindingImpl<FluentT, FluentImplT> self = this;
        final AppServiceCertificateImpl raw = (AppServiceCertificateImpl) manager.certificates().define(name() + "cert")
                .withRegion(parent().region())
                .withExistingResourceGroup(parent().resourceGroupName())
                .withPfxFile(pfxFile)
                .withPfxFilePassword(password);
        newCertificate = new CreatableUpdatableImpl<AppServiceCertificate, CertificateInner, AppServiceCertificateImpl>(raw.name(), raw.inner()) {
            @Override
            public Observable<AppServiceCertificate> createResourceAsync() {
                AppServiceCertificate certificate = (AppServiceCertificate) createdResource(raw.key());
                withCertificateThumbprint(certificate.thumbprint());
                return Observable.just(certificate);
            }

            @Override
            public boolean isInCreateMode() {
                return raw.isInCreateMode();
            }

            @Override
            public AppServiceCertificate refresh() {
                return raw.refresh();
            }
        };
        raw.creatorUpdatorTaskGroup().merge(newCertificate.creatorUpdatorTaskGroup());
        return this;
    }

    @Override
    public HostNameSslBindingImpl<FluentT, FluentImplT> withNewAppServiceCertificateOrder(CertificateProductType productType, int validYears) {
        this.newCertificateOrder = new CompleteAppServiceCertificateOrder(name(), new AppServiceCertificateOrderInner(), productType, validYears);
        return this;
    }

    private HostNameSslBindingImpl<FluentT, FluentImplT> withCertificateThumbprint(String thumbprint) {
        inner().withThumbprint(thumbprint);
        return this;
    }

    @Override
    public HostNameSslBindingImpl<FluentT, FluentImplT> withSniSsl() {
        inner().withSslState(SslState.SNI_ENABLED);
        return this;
    }

    @Override
    public HostNameSslBindingImpl<FluentT, FluentImplT> withIpBasedSsl() {
        inner().withSslState(SslState.IP_BASED_ENABLED);
        return this;
    }

    Creatable<AppServiceCertificate> newCertificate() {
        return newCertificate;
    }

    Creatable<AppServiceCertificateOrder> newCertificateOrder() {
        return newCertificateOrder;
    }

    @Override
    public WebAppBase<FluentT> parent() {
        return parent;
    }

    private class CompleteAppServiceCertificateOrder
            extends CreatableUpdatableImpl<
            AppServiceCertificateOrder,
            AppServiceCertificateOrderInner,
            AppServiceCertificateOrderImpl> {
        private AppServiceCertificateOrderImpl certCreatable;
        private Creatable<Vault> vaultCreatable;

        protected CompleteAppServiceCertificateOrder(String name, AppServiceCertificateOrderInner innerObject, CertificateProductType productType, int validYears) {
            super(name.replaceAll("[-.]", ""), innerObject);
            final String certificateName = name.replaceAll("[-.]", "");
            certCreatable = (AppServiceCertificateOrderImpl) manager.certificateOrders().define(certificateName)
                    .withExistingResourceGroup(parent().resourceGroupName())
                    .withHostName(name)
                    .withSku(productType)
                    .withValidYears(validYears);
            vaultCreatable = manager.keyVaultManager().vaults().define(certificateName)
                    .withRegion(parent().region())
                    .withExistingResourceGroup(parent().resourceGroupName())
                    .defineAccessPolicy()
                        .forServicePrincipal("f3c21649-0979-4721-ac85-b0216b2cf413")
                        .allowSecretPermissions(SecretPermissions.GET, SecretPermissions.SET, SecretPermissions.DELETE)
                        .attach()
                    .defineAccessPolicy()
                        .forServicePrincipal("abfa0a7c-a6b6-4736-8310-5855508787cd")
                        .allowSecretPermissions(SecretPermissions.GET)
                        .attach();
            addCreatableDependency(certCreatable);
            addCreatableDependency(vaultCreatable);
        }

        @Override
        public AppServiceCertificateOrder refresh() {
            return certCreatable.refresh();
        }

        @Override
        public Observable<AppServiceCertificateOrder> createResourceAsync() {
            final AppServiceCertificateOrder order = (AppServiceCertificateOrder) createdResource(certCreatable.key());
            Vault vault = (Vault) createdResource(vaultCreatable.key());
            return order.createKeyVaultBindingAsync(order.name(), vault)
                    .map(new Func1<AppServiceCertificateKeyVaultBinding, AppServiceCertificateOrder>() {
                        @Override
                        public AppServiceCertificateOrder call(AppServiceCertificateKeyVaultBinding appServiceCertificate) {
                            return order;
                        }
                    });
        }

        @Override
        public boolean isInCreateMode() {
            return certCreatable.isInCreateMode();
        }
    }
}
