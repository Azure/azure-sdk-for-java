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
import com.microsoft.azure.management.network.ApplicationGatewayBackend;
import com.microsoft.azure.management.network.ApplicationGatewayBackendHttpConfiguration;
import com.microsoft.azure.management.network.ApplicationGatewayFrontendListener;
import com.microsoft.azure.management.network.ApplicationGatewayProtocol;
import com.microsoft.azure.management.network.ApplicationGatewayRequestRoutingRule;
import com.microsoft.azure.management.network.ApplicationGatewayRequestRoutingRuleType;
import com.microsoft.azure.management.network.ApplicationGatewaySslCertificate;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceNamer;

/**
 *  Implementation for ApplicationGatewayRequestRoutingRule.
 */
@LangDefinition
class ApplicationGatewayRequestRoutingRuleImpl
    extends ChildResourceImpl<ApplicationGatewayRequestRoutingRuleInner, ApplicationGatewayImpl, ApplicationGateway>
    implements
        ApplicationGatewayRequestRoutingRule,
        ApplicationGatewayRequestRoutingRule.Definition<ApplicationGateway.DefinitionStages.WithRequestRoutingRuleOrCreate>,
        ApplicationGatewayRequestRoutingRule.UpdateDefinition<ApplicationGateway.Update>,
        ApplicationGatewayRequestRoutingRule.Update {

    ApplicationGatewayRequestRoutingRuleImpl(ApplicationGatewayRequestRoutingRuleInner inner, ApplicationGatewayImpl parent) {
        super(inner, parent);
    }

    // Getters

    @Override
    public ApplicationGatewaySslCertificate sslCertificate() {
        if (this.frontendListener() == null) {
            return null;
        } else {
            return this.frontendListener().sslCertificate();
        }
    }

    @Override
    public ApplicationGatewayProtocol protocol() {
        if (this.frontendListener() == null) {
            return null;
        } else {
            return this.frontendListener().protocol();
        }
    }

    @Override
    public String publicIpAddressId() {
        final ApplicationGatewayFrontendListener listener = this.frontendListener();
        if (listener == null) {
            return null;
        } else {
            return listener.publicIpAddressId();
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
    public ApplicationGatewayRequestRoutingRuleType ruleType() {
        return this.inner().ruleType();
    }

    @Override
    public ApplicationGatewayBackend backend() {
        SubResource backendRef = this.inner().backendAddressPool();
        if (backendRef != null) {
            String backendName = ResourceUtils.nameFromResourceId(backendRef.id());
            return this.parent().backends().get(backendName);
        } else {
            return null;
        }
    }

    @Override
    public ApplicationGatewayBackendHttpConfiguration backendHttpConfiguration() {
        SubResource configRef = this.inner().backendHttpSettings();
        if (configRef != null) {
            String configName = ResourceUtils.nameFromResourceId(configRef.id());
            return this.parent().backendHttpConfigurations().get(configName);
        } else {
            return null;
        }
    }

    @Override
    public ApplicationGatewayFrontendListener frontendListener() {
        SubResource listenerRef = this.inner().httpListener();
        if (listenerRef != null) {
            String listenerName = ResourceUtils.nameFromResourceId(listenerRef.id());
            return this.parent().frontendListeners().get(listenerName);
        } else {
            return null;
        }
    }

    // Verbs

    @Override
    public ApplicationGatewayImpl attach() {
        this.parent().withRequestRoutingRule(this);
        return this.parent();
    }

    @Override
    public ApplicationGatewayRequestRoutingRuleImpl fromFrontendListener(String name) {
        ApplicationGatewayFrontendListenerImpl listener =
                (ApplicationGatewayFrontendListenerImpl) this.parent().frontendListeners().get(name);
        if (listener == null) {
            // If no listener with this name exists, create one, assuming HTTP port 80
            this.fromFrontendPort(80, ApplicationGatewayProtocol.HTTP, name);
        }

        SubResource listenerRef = new SubResource()
                .withId(this.parent().futureResourceId() + "/HTTPListeners/" + name);
        this.inner().withHttpListener(listenerRef);
        return this;
    }

    @Override
    public ApplicationGatewayRequestRoutingRuleImpl fromFrontendHttpPort(int portNumber) {
        return this.fromFrontendPort(portNumber, ApplicationGatewayProtocol.HTTP, null);
    }

    @Override
    public ApplicationGatewayRequestRoutingRuleImpl fromFrontendHttpsPort(int portNumber) {
        return this.fromFrontendPort(portNumber, ApplicationGatewayProtocol.HTTPS, null);
    }

    private ApplicationGatewayRequestRoutingRuleImpl fromFrontendPort(int portNumber, ApplicationGatewayProtocol protocol, String name) {
        // Determine listener to use
        ApplicationGatewayFrontendListenerImpl listenerByPort =
                (ApplicationGatewayFrontendListenerImpl) this.parent().getFrontendListenerByPortNumber(portNumber);
        ApplicationGatewayFrontendListenerImpl listenerByName = null;
        if (name != null) {
            listenerByName = (ApplicationGatewayFrontendListenerImpl) this.parent().frontendListeners().get(name);
        }

        Boolean needToCreate = this.parent().needToCreate(listenerByName, listenerByPort, name);
        if (Boolean.TRUE.equals(needToCreate)) {
            // If no listener exists for the requested port number yet and the name, create one
            if (name == null) {
                name = ResourceNamer.randomResourceName("listener", 13);
            }

            listenerByPort = this.parent().defineFrontendListener(name)
                    .withFrontendPort(portNumber);
            if (ApplicationGatewayProtocol.HTTP.equals(protocol)) {
                listenerByPort.withHttp();
            } else if (ApplicationGatewayProtocol.HTTPS.equals(protocol)) {
                listenerByPort.withHttps();
            }

            listenerByPort.attach();
            return this.fromFrontendListener(listenerByPort.name());
        } else if (Boolean.FALSE.equals(needToCreate)) {
            // If matching listener already exists, then use it
            return this.fromFrontendListener(listenerByPort.name());
        } else {
            // If found listener conflicting in port number and name, then fail
            return null;
        }
    }

    @Override
    public ApplicationGatewayRequestRoutingRuleImpl withBackend(String name) {
        SubResource backendRef = new SubResource()
                .withId(this.parent().futureResourceId() + "/backendAddressPools/" + name);
        this.inner().withBackendAddressPool(backendRef);
        return this;
    }

    @Override
    public ApplicationGatewayRequestRoutingRuleImpl toBackendHttpConfiguration(String name) {
        SubResource httpConfigRef = new SubResource()
                .withId(this.parent().futureResourceId() + "/backendHttpSettingsCollection/" + name);
        this.inner().withBackendHttpSettings(httpConfigRef);
        return this;
    }

    @Override
    public ApplicationGatewayRequestRoutingRuleImpl toBackendPort(int portNumber) {
        ApplicationGatewayBackendHttpConfiguration config = this.parent().getBackendHttpConfigurationByPortNumber(portNumber);
        if (config == null) {
            return null;
        } else {
            return this.toBackendHttpConfiguration(config.name());
        }
    }

    @Override
    public ApplicationGatewayRequestRoutingRuleImpl withSslCertificate(String name) {
        // TODO do this with this.parent().frontendListener(...).update().withSslCertificate...
        ApplicationGatewayFrontendListenerImpl listener = (ApplicationGatewayFrontendListenerImpl) this.frontendListener();
        if (listener != null) {
            listener.withSslCertificate(name);
        }
        return this;
    }

    @Override
    public ApplicationGatewayRequestRoutingRuleImpl withSslCertificateFromPfxFile(File pfxFile) {
        // TODO do this with this.parent().frontendListener(...).update().withSslCertificate...
        ApplicationGatewayFrontendListenerImpl listener = (ApplicationGatewayFrontendListenerImpl) this.frontendListener();
        if (listener != null) {
            listener.withSslCertificateFromPfxFile(pfxFile);
        }
        return this;
    }

    @Override
    public ApplicationGatewayRequestRoutingRuleImpl withSslCertificateFromPfxFile(File pfxFile, String name) {
        // TODO do this with this.parent().frontendListener(...).update().withSslCertificate...
        ApplicationGatewayFrontendListenerImpl listener = (ApplicationGatewayFrontendListenerImpl) this.frontendListener();
        if (listener != null) {
            listener.withSslCertificateFromPfxFile(pfxFile, name);
        }
        return this;
    }

    @Override
    public ApplicationGatewayRequestRoutingRuleImpl withSslCertificatePassword(String password) {
        // TODO do this with this.parent().frontendListener(...).update().withSslCertificate...
        ApplicationGatewayFrontendListenerImpl listener = (ApplicationGatewayFrontendListenerImpl) this.frontendListener();
        if (listener != null) {
            listener.withSslCertificatePassword(password);
        }
        return this;
    }
}
