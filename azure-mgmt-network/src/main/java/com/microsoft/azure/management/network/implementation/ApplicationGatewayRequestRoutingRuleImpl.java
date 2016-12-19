/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.ApplicationGateway;
import com.microsoft.azure.management.network.ApplicationGatewayBackend;
import com.microsoft.azure.management.network.ApplicationGatewayBackendAddress;
import com.microsoft.azure.management.network.ApplicationGatewayBackendHttpConfiguration;
import com.microsoft.azure.management.network.ApplicationGatewayListener;
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

    private Boolean associateWithPublicFrontend = null;

    // Getters

    @Override
    public List<ApplicationGatewayBackendAddress> backendAddresses() {
        List<ApplicationGatewayBackendAddress> addresses = new ArrayList<>();
        ApplicationGatewayBackend backend = this.backend();
        if (backend != null && backend.addresses() != null) {
            addresses = backend.addresses();
        }
        return Collections.unmodifiableList(addresses);
    }

    @Override
    public boolean cookieBasedAffinity() {
        final ApplicationGatewayBackendHttpConfiguration backendConfig = this.backendHttpConfiguration();
        return (backendConfig != null) ? backendConfig.cookieBasedAffinity() : false;
    }

    @Override
    public int backendPort() {
        final ApplicationGatewayBackendHttpConfiguration backendConfig = this.backendHttpConfiguration();
        return (backendConfig != null) ? backendConfig.port() : 0;
    }

    @Override
    public boolean requiresServerNameIndication() {
        final ApplicationGatewayListener listener = this.listener();
        return (listener != null) ? listener.requiresServerNameIndication() : false;
    }

    @Override
    public String hostName() {
        final ApplicationGatewayListener listener = this.listener();
        return (listener != null) ? listener.hostName() : null;
    }

    @Override
    public int frontendPort() {
        final ApplicationGatewayListener listener = this.listener();
        return (listener != null) ? listener.frontendPortNumber() : 0;
    }

    @Override
    public ApplicationGatewaySslCertificate sslCertificate() {
        final ApplicationGatewayListener listener = this.listener();
        return (listener != null) ? listener.sslCertificate() : null;
    }

    @Override
    public ApplicationGatewayProtocol frontendProtocol() {
        final ApplicationGatewayListener listener = this.listener();
        return (listener != null) ? listener.protocol() : null;
    }

    @Override
    public String publicIpAddressId() {
        final ApplicationGatewayListener listener = this.listener();
        return (listener != null) ? listener.publicIpAddressId() : null;
    }

    @Override
    public PublicIpAddress getPublicIpAddress() {
        final String pipId = this.publicIpAddressId();
        return (pipId != null) ? this.parent().manager().publicIpAddresses().getById(pipId) : null;
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
    public ApplicationGatewayBackendHttpConfigurationImpl backendHttpConfiguration() {
        SubResource configRef = this.inner().backendHttpSettings();
        if (configRef != null) {
            String configName = ResourceUtils.nameFromResourceId(configRef.id());
            return (ApplicationGatewayBackendHttpConfigurationImpl) this.parent().backendHttpConfigurations().get(configName);
        } else {
            return null;
        }
    }

    @Override
    public ApplicationGatewayListenerImpl listener() {
        SubResource listenerRef = this.inner().httpListener();
        if (listenerRef != null) {
            String listenerName = ResourceUtils.nameFromResourceId(listenerRef.id());
            return (ApplicationGatewayListenerImpl) this.parent().listeners().get(listenerName);
        } else {
            return null;
        }
    }

    // Verbs

    @Override
    public ApplicationGatewayImpl attach() {
        return this.parent().withRequestRoutingRule(this);
    }

    // Withers

    // --- Frontend handling

    @Override
    public ApplicationGatewayRequestRoutingRuleImpl fromPublicFrontend() {
        this.associateWithPublicFrontend = true;
        return this;
    }

    @Override
    public ApplicationGatewayRequestRoutingRuleImpl fromPrivateFrontend() {
        this.associateWithPublicFrontend = false;
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

    // --- Backend HTTP config handling

    @Override
    public ApplicationGatewayRequestRoutingRuleImpl toBackendHttpConfiguration(String name) {
        SubResource httpConfigRef = new SubResource()
                .withId(this.parent().futureResourceId() + "/backendHttpSettingsCollection/" + name);
        this.inner().withBackendHttpSettings(httpConfigRef);
        return this;
    }

    private ApplicationGatewayBackendHttpConfigurationImpl ensureBackendHttpConfig() {
        ApplicationGatewayBackendHttpConfigurationImpl config = this.backendHttpConfiguration();
        if (config == null) {
            final String name = ResourceNamer.randomResourceName("bckcfg", 11);
            config = this.parent().defineBackendHttpConfiguration(name);
            config.attach();
            this.toBackendHttpConfiguration(name);
        }
        return config;
    }

    @Override
    public ApplicationGatewayRequestRoutingRuleImpl toBackendHttpPort(int portNumber) {
        String name = ResourceNamer.randomResourceName("backcfg", 12);
        this.parent().defineBackendHttpConfiguration(name)
            .withPort(portNumber)
            .attach();
        return this.toBackendHttpConfiguration(name);
    }

    @Override
    public ApplicationGatewayRequestRoutingRuleImpl withCookieBasedAffinity() {
        this.parent().updateBackendHttpConfiguration(ensureBackendHttpConfig().name())
            .withCookieBasedAffinity();
        return this;
    }

    @Override
    public ApplicationGatewayRequestRoutingRuleImpl withoutCookieBasedAffinity() {
        this.parent().updateBackendHttpConfiguration(ensureBackendHttpConfig().name())
            .withoutCookieBasedAffinity();
        return this;
    }

    // --- Listener handling

    @Override
    public ApplicationGatewayRequestRoutingRuleImpl fromListener(String name) {
        SubResource listenerRef = new SubResource()
                .withId(this.parent().futureResourceId() + "/HTTPListeners/" + name);
        this.inner().withHttpListener(listenerRef);
        return this;
    }

    private ApplicationGatewayRequestRoutingRuleImpl fromFrontendPort(int portNumber, ApplicationGatewayProtocol protocol, String name) {
        // Verify no conflicting listener exists
        ApplicationGatewayListenerImpl listenerByPort =
                (ApplicationGatewayListenerImpl) this.parent().listenerByPortNumber(portNumber);
        ApplicationGatewayListenerImpl listenerByName = null;
        if (name != null) {
            listenerByName = (ApplicationGatewayListenerImpl) this.parent().listeners().get(name);
        }

        Boolean needToCreate = this.parent().needToCreate(listenerByName, listenerByPort, name);
        if (Boolean.TRUE.equals(needToCreate)) {
            // If no listener exists for the requested port number yet and the name, create one
            if (name == null) {
                name = ResourceNamer.randomResourceName("listener", 13);
            }

            listenerByPort = this.parent().defineListener(name)
                    .withFrontendPort(portNumber);

            // Determine protocol
            if (ApplicationGatewayProtocol.HTTP.equals(protocol)) {
                listenerByPort.withHttp();
            } else if (ApplicationGatewayProtocol.HTTPS.equals(protocol)) {
                listenerByPort.withHttps();
            }

            // Determine frontend
            if (Boolean.TRUE.equals(this.associateWithPublicFrontend)) {
                listenerByPort.withPublicFrontend();
                this.parent().withNewPublicIpAddress();
            } else if (Boolean.FALSE.equals(this.associateWithPublicFrontend)) {
                listenerByPort.withPrivateFrontend();
            }
            this.associateWithPublicFrontend = null;

            listenerByPort.attach();
            return this.fromListener(listenerByPort.name());
        } else {
            // If matching listener already exists then fail
            return null;
        }
    }

    private ApplicationGatewayListenerImpl ensureListener() {
        ApplicationGatewayListenerImpl listener = this.listener();
        if (listener == null) {
            final String name = ResourceNamer.randomResourceName("listener", 13);
            listener = this.parent().defineListener(name);
            listener.attach();
            this.fromListener(name);
        }
        return listener;
    }

    @Override
    public ApplicationGatewayRequestRoutingRuleImpl withSslCertificate(String name) {
        this.parent().updateListener(ensureListener().name()).withSslCertificate(name);
        return this;
    }

    @Override
    public ApplicationGatewayRequestRoutingRuleImpl withSslCertificateFromPfxFile(File pfxFile) throws IOException {
        this.parent().updateListener(ensureListener().name()).withSslCertificateFromPfxFile(pfxFile);
        return this;
    }

    @Override
    public ApplicationGatewayRequestRoutingRuleImpl withSslCertificatePassword(String password) {
        this.parent().updateListener(ensureListener().name()).withSslCertificatePassword(password);
        return this;
    }

    @Override
    public ApplicationGatewayRequestRoutingRuleImpl withHostName(String hostName) {
        this.parent().updateListener(ensureListener().name()).withHostName(hostName);
        return this;
    }

    @Override
    public ApplicationGatewayRequestRoutingRuleImpl withServerNameIndication() {
        this.parent().updateListener(ensureListener().name()).withServerNameIndication();
        return this;
    }

    @Override
    public ApplicationGatewayRequestRoutingRuleImpl withoutServerNameIndication() {
        this.parent().updateListener(ensureListener().name()).withoutServerNameIndication();
        return this;
    }

    // --- Backend handling

    private ApplicationGatewayBackendImpl ensureBackend() {
        ApplicationGatewayBackendImpl backend = (ApplicationGatewayBackendImpl) this.backend();
        if (backend == null) {
            String name = ResourceNamer.randomResourceName("backend", 12);
            backend = this.parent().defineBackend(name);
            backend.attach();
            this.toBackend(name);
        }

        return backend;
    }

    @Override
    public ApplicationGatewayRequestRoutingRuleImpl toBackend(String name) {
        SubResource backendRef = new SubResource()
                .withId(this.parent().futureResourceId() + "/backendAddressPools/" + name);
        this.inner().withBackendAddressPool(backendRef);
        return this;
    }

    @Override
    public ApplicationGatewayRequestRoutingRuleImpl toBackendIpAddress(String ipAddress) {
        this.parent().updateBackend(ensureBackend().name()).withIpAddress(ipAddress);
        return this;
    }

    @Override
    public ApplicationGatewayRequestRoutingRuleImpl toBackendFqdn(String fqdn) {
        this.parent().updateBackend(ensureBackend().name()).withFqdn(fqdn);
        return this;
    }
}
