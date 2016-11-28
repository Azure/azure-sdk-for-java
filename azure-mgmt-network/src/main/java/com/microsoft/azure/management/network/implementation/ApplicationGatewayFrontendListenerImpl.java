/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import java.io.File;

import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.ApplicationGateway;
import com.microsoft.azure.management.network.ApplicationGatewayFrontend;
import com.microsoft.azure.management.network.ApplicationGatewayFrontendListener;
import com.microsoft.azure.management.network.ApplicationGatewayProtocol;
import com.microsoft.azure.management.network.ApplicationGatewaySslCertificate;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceNamer;

/**
 *  Implementation for ApplicationGatewayFrontendListener.
 */
@LangDefinition
class ApplicationGatewayFrontendListenerImpl
    extends ChildResourceImpl<ApplicationGatewayHttpListenerInner, ApplicationGatewayImpl, ApplicationGateway>
    implements
        ApplicationGatewayFrontendListener,
        ApplicationGatewayFrontendListener.Definition<ApplicationGateway.DefinitionStages.WithListenerOrBackendHttpConfig>,
        ApplicationGatewayFrontendListener.UpdateDefinition<ApplicationGateway.Update>,
        ApplicationGatewayFrontendListener.Update {

    ApplicationGatewayFrontendListenerImpl(ApplicationGatewayHttpListenerInner inner, ApplicationGatewayImpl parent) {
        super(inner, parent);
    }

    // Getters

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
        final SubResource frontendInner = this.inner().frontendIPConfiguration();
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
        if (this.frontend() == null) {
            // If not hooked up to a frontend, hook up to the first or default frontend
            ApplicationGatewayFrontend frontend = this.parent().frontends().get(NetworkGroupableParentResourceImpl.DEFAULT);
            if (frontend == null && !this.parent().frontends().isEmpty()) {
                // If no default frontend, hook up to the first one, if available
                frontend = this.parent().frontends().values().iterator().next();
            }

            if (frontend == null) {
                // If no frontend to hook up to, fail fast
                return null;
            } else {
                this.withFrontend(frontend.name());
            }
        }

        this.parent().withHttpListener(this);
        return this.parent();
    }

    // Withers

    @Override
    public ApplicationGatewayFrontendListenerImpl withFrontend(String name) {
        SubResource frontendRef = new SubResource()
                .withId(this.parent().futureResourceId() + "/frontendIPConfigurations/" + name);
        this.inner().withFrontendIPConfiguration(frontendRef);
        return this;
    }

    @Override
    public ApplicationGatewayFrontendListenerImpl withFrontendPort(String name) {
        SubResource portRef = new SubResource()
                .withId(this.parent().futureResourceId() + "/frontendPorts/" + name);
        this.inner().withFrontendPort(portRef);
        return this;
    }

    @Override
    public ApplicationGatewayFrontendListenerImpl withSslCertificate(String name) {
        SubResource certRef = new SubResource()
                .withId(this.parent().futureResourceId() + "/sslCertificates/" + name);
        this.inner().withSslCertificate(certRef);
        return this;
    }

    private ApplicationGatewaySslCertificateImpl sslCert = null;

    @Override
    public ApplicationGatewayFrontendListenerImpl withSslCertificateFromPfxFile(File pfxFile) {
        String name = ResourceNamer.randomResourceName("cert", 10);
        return withSslCertificateFromPfxFile(pfxFile, name);
    }

    @Override
    public ApplicationGatewayFrontendListenerImpl withSslCertificateFromPfxFile(File pfxFile, String name) {
        this.sslCert = this.parent().defineSslCertificate(name)
            .withPfxFromFile(pfxFile);
        return this;
    }

    @Override
    public ApplicationGatewayFrontendListenerImpl withSslCertificatePassword(String password) {
        if (this.sslCert != null) {
            this.sslCert.withPfxPassword(password).attach();
            this.withSslCertificate(sslCert.name());
            this.sslCert = null;
            return this;
        } else {
            return null; // Fail fast as this should never happen if the internal logic is correct
        }
    }

    @Override
    public ApplicationGatewayFrontendListenerImpl withHttp() {
        this.inner().withProtocol(ApplicationGatewayProtocol.HTTP);
        return this;
    }

    @Override
    public ApplicationGatewayFrontendListenerImpl withHttps() {
        this.inner().withProtocol(ApplicationGatewayProtocol.HTTPS);
        return this;
    }

    @Override
    public ApplicationGatewayFrontendListenerImpl withFrontendPort(int portNumber) {
        // Attempt to find an existing port referencing this port number
        String portName = this.parent().frontendPortNameFromNumber(portNumber);
        if (portName == null) {
            // Existing frontend port with this number not found so create one
            portName = ResourceNamer.randomResourceName("port", 10);
            this.parent().withFrontendPort(portNumber, portName);
        }

        return this.withFrontendPort(portName);
    }
}
