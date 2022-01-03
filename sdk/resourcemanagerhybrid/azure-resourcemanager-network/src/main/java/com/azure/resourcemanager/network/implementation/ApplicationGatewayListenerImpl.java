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
import reactor.core.publisher.Mono;

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
        if (this.innerModel().requireServerNameIndication() != null) {
            return this.innerModel().requireServerNameIndication();
        } else {
            return false;
        }
    }

    @Override
    public String hostname() {
        return this.innerModel().hostname();
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
        return this.getPublicIpAddressAsync().block();
    }

    @Override
    public Mono<PublicIpAddress> getPublicIpAddressAsync() {
        String pipId = this.publicIpAddressId();
        return pipId == null ? Mono.empty() : this.parent().manager().publicIpAddresses().getByIdAsync(pipId);
    }

    @Override
    public String name() {
        return this.innerModel().name();
    }

    @Override
    public ApplicationGatewaySslCertificate sslCertificate() {
        SubResource certRef = this.innerModel().sslCertificate();
        if (certRef == null) {
            return null;
        }

        String name = ResourceUtils.nameFromResourceId(certRef.id());
        return this.parent().sslCertificates().get(name);
    }

    @Override
    public ApplicationGatewayProtocol protocol() {
        return this.innerModel().protocol();
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
        if (this.innerModel().frontendPort() != null) {
            return ResourceUtils.nameFromResourceId(this.innerModel().frontendPort().id());
        } else {
            return null;
        }
    }

    @Override
    public ApplicationGatewayFrontend frontend() {
        final SubResource frontendInner = this.innerModel().frontendIpConfiguration();
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
        this.innerModel().withFrontendIpConfiguration(frontendRef);
        return this;
    }

    // Withers

    @Override
    public ApplicationGatewayListenerImpl withFrontendPort(String name) {
        SubResource portRef = new SubResource().withId(this.parent().futureResourceId() + "/frontendPorts/" + name);
        this.innerModel().withFrontendPort(portRef);
        return this;
    }

    @Override
    public ApplicationGatewayListenerImpl withFrontendPort(int portNumber) {
        // Attempt to find an existing port referencing this port number
        String portName = this.parent().frontendPortNameFromNumber(portNumber);
        if (portName == null) {
            // Existing frontend port with this number not found so create one
            portName = this.parent().manager().resourceManager().internalContext()
                .randomResourceName("port", 9);
            this.parent().withFrontendPort(portNumber, portName);
        }

        return this.withFrontendPort(portName);
    }

    @Override
    public ApplicationGatewayListenerImpl withSslCertificate(String name) {
        SubResource certRef = new SubResource().withId(this.parent().futureResourceId() + "/sslCertificates/" + name);
        this.innerModel().withSslCertificate(certRef);
        return this;
    }

    @Override
    public ApplicationGatewayListenerImpl withSslCertificateFromKeyVaultSecretId(String keyVaultSecretId) {
        return withSslCertificateFromKeyVaultSecretId(keyVaultSecretId, null);
    }

    private ApplicationGatewayListenerImpl withSslCertificateFromKeyVaultSecretId(
        String keyVaultSecretId, String name) {
        if (name == null) {
            name = this.parent().manager().resourceManager().internalContext()
                .randomResourceName("cert", 10);
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
            name = this.parent().manager().resourceManager().internalContext()
                .randomResourceName("cert", 10);
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
        this.innerModel().withProtocol(ApplicationGatewayProtocol.HTTP);
        return this;
    }

    @Override
    public ApplicationGatewayListenerImpl withHttps() {
        this.innerModel().withProtocol(ApplicationGatewayProtocol.HTTPS);
        return this;
    }

    @Override
    public ApplicationGatewayListenerImpl withHostname(String hostname) {
        this.innerModel().withHostname(hostname);
        return this;
    }

    @Override
    public ApplicationGatewayListenerImpl withServerNameIndication() {
        this.innerModel().withRequireServerNameIndication(true);
        return this;
    }

    @Override
    public ApplicationGatewayListenerImpl withoutServerNameIndication() {
        this.innerModel().withRequireServerNameIndication(false);
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
