/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.website.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.website.Certificate;
import com.microsoft.azure.management.website.DomainContact;
import com.microsoft.azure.management.website.HostNameSslBinding;
import com.microsoft.azure.management.website.HostNameSslState;
import com.microsoft.azure.management.website.SslState;
import com.microsoft.azure.management.website.WebApp;

import java.io.File;

/**
 *  Implementation for {@link DomainContact} and its create and update interfaces.
 */
@LangDefinition
class HostNameSslBindingImpl
    extends ChildResourceImpl<HostNameSslState, WebAppImpl, WebApp>
    implements
        HostNameSslBinding,
        HostNameSslBinding.Definition<WebApp.DefinitionStages.WithHostNameSslBinding>,
        HostNameSslBinding.UpdateDefinition<WebApp.Update> {

    private Creatable<Certificate> newCertificate;
    private final AppServiceManager manager;

    HostNameSslBindingImpl(HostNameSslState inner, WebAppImpl parent, AppServiceManager manager) {
        super(inner, parent);
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
    public WebAppImpl attach() {
        parent().withNewHostNameSslBinding(this);
        return parent();
    }

    @Override
    public HostNameSslBindingImpl withPfxCertificateToUpload(File pfxFile, String password) {
        newCertificate = manager.certificates().define(name() + "cert")
                .withRegion(parent().region())
                .withNewResourceGroup(parent().resourceGroupName())
                .withPfxFile(pfxFile)
                .withPfxFilePassword(password);
        return this;
    }

    HostNameSslBindingImpl withCertificateThumbprint(String thumbprint) {
        inner().withThumbprint(thumbprint);
        return this;
    }

    @Override
    public HostNameSslBindingImpl withSniSSL() {
        inner().withSslState(SslState.SNI_ENABLED);
        return this;
    }

    @Override
    public HostNameSslBindingImpl withIpBasedSSL() {
        inner().withSslState(SslState.IP_BASED_ENABLED);
        return this;
    }

    Creatable<Certificate> newCertificate() {
        return newCertificate;
    }
}
