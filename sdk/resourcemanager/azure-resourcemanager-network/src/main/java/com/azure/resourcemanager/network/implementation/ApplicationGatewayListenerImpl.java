// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.core.management.SubResource;
import com.azure.resourcemanager.network.models.ApplicationGateway;
import com.azure.resourcemanager.network.models.ApplicationGatewayFrontend;
import com.azure.resourcemanager.network.models.ApplicationGatewayHttpListener;
import com.azure.resourcemanager.network.models.ApplicationGatewayListener;
import com.azure.resourcemanager.network.models.ApplicationGatewayProtocol;
import com.azure.resourcemanager.network.models.ApplicationGatewaySslCertificate;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ChildResourceImpl;
import java.io.File;
import java.io.IOException;

/** Implementation for ApplicationGatewayListener. */
class ApplicationGatewayListenerImpl
    extends ChildResourceImpl<ApplicationGatewayHttpListener, ApplicationGatewayImpl, ApplicationGateway>
    implements ApplicationGatewayListener,
        ApplicationGatewayListener.Definition<ApplicationGateway.DefinitionStages.WithCreate>,
        ApplicationGatewayListener.UpdateDefinition<ApplicationGateway.Update>,
        ApplicationGatewayListener.Update {

    ApplicationGatewayListenerImpl(ApplicationGatewayHttpListener inner, ApplicationGatewayImpl parent) {
        super(inner, parent);
    }

    // Getters
    @Override
    public String networkId() {
        ApplicationGatewayFrontend frontend = this.frontend();
        if (frontend != null) {
            return frontend.networkId();
        } else {
            return null;
        }
    }

    @Override
    public String subnetName() {
        ApplicationGatewayFrontend frontend = this.frontend();
        if (frontend != null) {
            return frontend.subnetName();
        } else {
            return null;
        }
    }

    @Override
    public boolean requiresServerNameIndication() {
        if (this.inner().requireServerNameIndication() != null) {
            return this.inner().requireServerNameIndication();
        } else {
            return false;
        }
    }

    @Override
    public String hostname() {
        return this.inner().hostname();
    }

    @Override
    public String publicIpAddressId() {
        final ApplicationGatewayFrontend frontend = this.frontend();
        if (frontend == null) {
            return null;
        } else {
            return frontend.publicIpAddressId();
        }
    }

    @Override
    public PublicIpAddress getPublicIpAddress() {
        final String pipId = this.publicIpAddressId();
        if (pipId == null) {
            return null;
        } else {
            return this.parent().manager().publicIpAddresses().getById(pipId);
        }
    }

    @Override
    public String name() {
        return this.inner().name();
    }

    @Override
    public ApplicationGatewaySslCertificate sslCertificate() {
        SubResource certRef = this.inner().sslCertificate();
        if (certRef == null) {
            return null;
        }

        String name = ResourceUtils.nameFromResourceId(certRef.id());
        return this.parent().sslCertificates().get(name);
    }

    @Override
    public ApplicationGatewayProtocol protocol() {
        return this.inner().protocol();
    }

    @Override
    public int frontendPortNumber() {
        String name = this.frontendPortName();
        if (name == null) {
            return 0;
        } else if (!this.parent().frontendPorts().containsKey(name)) {
            return 0;
        } else {
            return this.parent().frontendPorts().get(name);
        }
    }

    @Override
    public String frontendPortName() {
        if (this.inner().frontendPort() != null) {
            return ResourceUtils.nameFromResourceId(this.inner().frontendPort().id());
        } else {
            return null;
        }
    }

    @Override
    public ApplicationGatewayFrontend frontend() {
        final SubResource frontendInner = this.inner().frontendIpConfiguration();
        if (frontendInner == null) {
            return null;
        } else {
            final String frontendName = ResourceUtils.nameFromResourceId(frontendInner.id());
            return this.parent().frontends().get(frontendName);
        }
    }

    // Verbs

    @Override
    public ApplicationGatewayImpl attach() {
        this.parent().withHttpListener(this);
        return this.parent();
    }

    // Helpers

    private ApplicationGatewayListenerImpl withFrontend(String name) {
        SubResource frontendRef =
            new SubResource().withId(this.parent().futureResourceId() + "/frontendIPConfigurations/" + name);
        this.inner().withFrontendIpConfiguration(frontendRef);
        return this;
    }

    // Withers

    @Override
    public ApplicationGatewayListenerImpl withFrontendPort(String name) {
        SubResource portRef = new SubResource().withId(this.parent().futureResourceId() + "/frontendPorts/" + name);
        this.inner().withFrontendPort(portRef);
        return this;
    }

    @Override
    public ApplicationGatewayListenerImpl withFrontendPort(int portNumber) {
        // Attempt to find an existing port referencing this port number
        String portName = this.parent().frontendPortNameFromNumber(portNumber);
        if (portName == null) {
            // Existing frontend port with this number not found so create one
            portName = this.parent().manager().sdkContext().randomResourceName("port", 9);
            this.parent().withFrontendPort(portNumber, portName);
        }

        return this.withFrontendPort(portName);
    }

    @Override
    public ApplicationGatewayListenerImpl withSslCertificate(String name) {
        SubResource certRef = new SubResource().withId(this.parent().futureResourceId() + "/sslCertificates/" + name);
        this.inner().withSslCertificate(certRef);
        return this;
    }

    @Override
    public ApplicationGatewayListenerImpl withSslCertificateFromKeyVaultSecretId(String keyVaultSecretId) {
        return withSslCertificateFromKeyVaultSecretId(keyVaultSecretId, null);
    }

    private ApplicationGatewayListenerImpl withSslCertificateFromKeyVaultSecretId(
        String keyVaultSecretId, String name) {
        if (name == null) {
            name = this.parent().manager().sdkContext().randomResourceName("cert", 10);
        }
        this.parent().defineSslCertificate(name).withKeyVaultSecretId(keyVaultSecretId).attach();
        return this;
    }

    @Override
    public ApplicationGatewayListenerImpl withSslCertificateFromPfxFile(File pfxFile) throws IOException {
        return withSslCertificateFromPfxFile(pfxFile, null);
    }

    private ApplicationGatewayListenerImpl withSslCertificateFromPfxFile(File pfxFile, String name) throws IOException {
        if (name == null) {
            name = this.parent().manager().sdkContext().randomResourceName("cert", 10);
        }
        this.parent().defineSslCertificate(name).withPfxFromFile(pfxFile).attach();
        return this.withSslCertificate(name);
    }

    @Override
    public ApplicationGatewayListenerImpl withSslCertificatePassword(String password) {
        ApplicationGatewaySslCertificateImpl sslCert = (ApplicationGatewaySslCertificateImpl) this.sslCertificate();
        if (sslCert != null) {
            sslCert.withPfxPassword(password);
        }
        return this;
    }

    @Override
    public ApplicationGatewayListenerImpl withHttp() {
        this.inner().withProtocol(ApplicationGatewayProtocol.HTTP);
        return this;
    }

    @Override
    public ApplicationGatewayListenerImpl withHttps() {
        this.inner().withProtocol(ApplicationGatewayProtocol.HTTPS);
        return this;
    }

    @Override
    public ApplicationGatewayListenerImpl withHostname(String hostname) {
        this.inner().withHostname(hostname);
        return this;
    }

    @Override
    public ApplicationGatewayListenerImpl withServerNameIndication() {
        this.inner().withRequireServerNameIndication(true);
        return this;
    }

    @Override
    public ApplicationGatewayListenerImpl withoutServerNameIndication() {
        this.inner().withRequireServerNameIndication(false);
        return this;
    }

    @Override
    public ApplicationGatewayListenerImpl withPrivateFrontend() {
        this.withFrontend(this.parent().ensureDefaultPrivateFrontend().name());
        return this;
    }

    @Override
    public ApplicationGatewayListenerImpl withPublicFrontend() {
        this.withFrontend(this.parent().ensureDefaultPublicFrontend().name());
        return this;
    }
}
