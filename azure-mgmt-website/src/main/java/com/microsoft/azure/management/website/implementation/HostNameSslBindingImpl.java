/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.website.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.IndexableWrapperImpl;
import com.microsoft.azure.management.website.AppServiceCertificate;
import com.microsoft.azure.management.website.Certificate;
import com.microsoft.azure.management.website.DomainContact;
import com.microsoft.azure.management.website.HostNameSslBinding;
import com.microsoft.azure.management.website.HostNameSslState;
import com.microsoft.azure.management.website.SslState;
import com.microsoft.azure.management.website.WebAppBase;

import java.io.File;

/**
 *  Implementation for {@link DomainContact} and its create and update interfaces.
 */
@LangDefinition
class HostNameSslBindingImpl<
        FluentT extends WebAppBase<FluentT>,
        FluentImplT extends WebAppBaseImpl<FluentT, FluentImplT>>
    extends IndexableWrapperImpl<HostNameSslState>
    implements
        HostNameSslBinding,
        HostNameSslBinding.Definition<WebAppBase.DefinitionStages.WithHostNameSslBinding<FluentT>>,
        HostNameSslBinding.UpdateDefinition<WebAppBase.Update> {

    private Creatable<Certificate> newCertificate;
    private Creatable<AppServiceCertificate> newCertificateOrder;
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
    public HostNameSslBindingImpl<FluentT, FluentImplT> withPfxCertificateToUpload(File pfxFile, String password) {
        newCertificate = manager.certificates().define(name() + "cert")
                .withRegion(parent().region())
                .withNewResourceGroup(parent().resourceGroupName())
                .withPfxFile(pfxFile)
                .withPfxFilePassword(password);
        return this;
    }

//    @Override
//    public HostNameSslBindingImpl withNewAppServiceCertificateOrder(CertificateProductType productType, int validYears) {
//        final String certificateName = name().replaceAll("[-.]", "");
//        Observable<AppServiceCertificateOrder> orderObservable = manager.certificateOrders().define(certificateName)
//                .withExistingResourceGroup(parent().resourceGroupName())
//                .withHostName(name())
//                .withSku(productType)
//                .withValidYears(1)
//                .createAsync();
//        Observable<Vault> vaultObservable = manager.keyVaultManager().vaults().define(certificateName + "vault")
//                .withRegion(parent().region())
//                .withNewResourceGroup(parent().resourceGroupName())
//                .defineAccessPolicy()
//                    .forServicePrincipal("Microsoft.Azure.CertificateRegistration")
//                    .allowSecretPermissions(SecretPermissions.GET, SecretPermissions.SET, SecretPermissions.DELETE)
//                    .attach()
//                .defineAccessPolicy()
//                    .forServicePrincipal("Microsoft.Azure.WebSites")
//                    .allowSecretPermissions(SecretPermissions.GET)
//                    .attach()
//                .createAsync();
//        return this;
//    }

    HostNameSslBindingImpl withCertificateThumbprint(String thumbprint) {
        inner().withThumbprint(thumbprint);
        return this;
    }

    @Override
    public HostNameSslBindingImpl<FluentT, FluentImplT> withSniSSL() {
        inner().withSslState(SslState.SNI_ENABLED);
        return this;
    }

    @Override
    public HostNameSslBindingImpl<FluentT, FluentImplT> withIpBasedSSL() {
        inner().withSslState(SslState.IP_BASED_ENABLED);
        return this;
    }

    Creatable<Certificate> newCertificate() {
        return newCertificate;
    }

    Creatable<AppServiceCertificate> newCertificateOrder() {
        return newCertificateOrder;
    }

    @Override
    public WebAppBase<FluentT> parent() {
        return parent;
    }
}
