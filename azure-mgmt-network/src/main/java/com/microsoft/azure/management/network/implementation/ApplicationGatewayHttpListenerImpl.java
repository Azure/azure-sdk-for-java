/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.ApplicationGateway;
import com.microsoft.azure.management.network.ApplicationGatewayFrontend;
import com.microsoft.azure.management.network.ApplicationGatewayHttpListener;
import com.microsoft.azure.management.network.ApplicationGatewayProtocol;
import com.microsoft.azure.management.network.ApplicationGatewaySslCertificate;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceNamer;

/**
 *  Implementation for ApplicationGatewayHttpListener.
 */
@LangDefinition
class ApplicationGatewayHttpListenerImpl
    extends ChildResourceImpl<ApplicationGatewayHttpListenerInner, ApplicationGatewayImpl, ApplicationGateway>
    implements
        ApplicationGatewayHttpListener,
        ApplicationGatewayHttpListener.Definition<ApplicationGateway.DefinitionStages.WithHttpListenerOrRequestRoutingRule>,
        ApplicationGatewayHttpListener.UpdateDefinition<ApplicationGateway.Update>,
        ApplicationGatewayHttpListener.Update {

    ApplicationGatewayHttpListenerImpl(ApplicationGatewayHttpListenerInner inner, ApplicationGatewayImpl parent) {
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

    // Verbs

    @Override
    public ApplicationGatewayImpl attach() {
        this.parent().withHttpListener(this);
        return this.parent();
    }

    // Withers

    @Override
    public ApplicationGatewayHttpListenerImpl withFrontend(String name) {
        SubResource frontendRef = new SubResource()
                .withId(this.parent().futureResourceId() + "/frontendIPConfigurations/" + name);
        this.inner().withFrontendIPConfiguration(frontendRef);
        return this;
    }

    @Override
    public ApplicationGatewayHttpListenerImpl withFrontendPort(String name) {
        SubResource portRef = new SubResource()
                .withId(this.parent().futureResourceId() + "/frontendPorts/" + name);
        this.inner().withFrontendPort(portRef);
        return this;
    }

    @Override
    public ApplicationGatewayHttpListenerImpl withSslCertificate(String name) {
        SubResource certRef = new SubResource()
                .withId(this.parent().futureResourceId() + "/sslCertificates/" + name);
        this.inner().withSslCertificate(certRef);
        return this;
    }

    @Override
    public ApplicationGatewayHttpListenerImpl withHttp() {
        this.inner().withProtocol(ApplicationGatewayProtocol.HTTP);
        return this;
    }

    @Override
    public ApplicationGatewayHttpListenerImpl withHttps() {
        this.inner().withProtocol(ApplicationGatewayProtocol.HTTPS);
        return this;
    }

    @Override
    public ApplicationGatewayHttpListenerImpl withFrontendPort(int portNumber) {
        // Attempt to find an existing port referencing this port number
        String portName = this.parent().frontendPortNameFromNumber(portNumber);
        if (portName == null) {
            // Existing frontend port with this number not found so create one
            portName = ResourceNamer.randomResourceName("port", 10);
            this.parent().withFrontendPort(portNumber, portName);
        }

        return this.withFrontendPort(portName);
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
}
