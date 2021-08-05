// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.core.management.SubResource;
import com.azure.resourcemanager.network.models.ApplicationGateway;
import com.azure.resourcemanager.network.models.ApplicationGatewayBackend;
import com.azure.resourcemanager.network.models.ApplicationGatewayBackendAddress;
import com.azure.resourcemanager.network.models.ApplicationGatewayBackendHttpConfiguration;
import com.azure.resourcemanager.network.models.ApplicationGatewayListener;
import com.azure.resourcemanager.network.models.ApplicationGatewayProtocol;
import com.azure.resourcemanager.network.models.ApplicationGatewayRedirectConfiguration;
import com.azure.resourcemanager.network.models.ApplicationGatewayRequestRoutingRule;
import com.azure.resourcemanager.network.models.ApplicationGatewayRequestRoutingRuleType;
import com.azure.resourcemanager.network.models.ApplicationGatewaySslCertificate;
import com.azure.resourcemanager.network.models.ApplicationGatewayUrlPathMap;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.azure.resourcemanager.network.fluent.models.ApplicationGatewayRequestRoutingRuleInner;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ChildResourceImpl;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/** Implementation for ApplicationGatewayRequestRoutingRule. */
class ApplicationGatewayRequestRoutingRuleImpl
    extends ChildResourceImpl<ApplicationGatewayRequestRoutingRuleInner, ApplicationGatewayImpl, ApplicationGateway>
    implements ApplicationGatewayRequestRoutingRule,
        ApplicationGatewayRequestRoutingRule.Definition<
            ApplicationGateway.DefinitionStages.WithRequestRoutingRuleOrCreate>,
        ApplicationGatewayRequestRoutingRule.UpdateDefinition<ApplicationGateway.Update>,
        ApplicationGatewayRequestRoutingRule.Update {

    ApplicationGatewayRequestRoutingRuleImpl(
        ApplicationGatewayRequestRoutingRuleInner inner, ApplicationGatewayImpl parent) {
        super(inner, parent);
    }

    private Boolean associateWithPublicFrontend = null;

    // Getters

    @Override
    public Collection<ApplicationGatewayBackendAddress> backendAddresses() {
        Collection<ApplicationGatewayBackendAddress> addresses = new ArrayList<>();
        ApplicationGatewayBackend backend = this.backend();
        if (backend != null && backend.addresses() != null) {
            addresses = backend.addresses();
        }
        return Collections.unmodifiableCollection(addresses);
    }

    @Override
    public ApplicationGatewayUrlPathMap urlPathMap() {
        SubResource urlMapRef = this.innerModel().urlPathMap();
        if (urlMapRef != null) {
            String urlMapName = ResourceUtils.nameFromResourceId(urlMapRef.id());
            return this.parent().urlPathMaps().get(urlMapName);
        } else {
            return null;
        }
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
    public String hostname() {
        final ApplicationGatewayListener listener = this.listener();
        return (listener != null) ? listener.hostname() : null;
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
    public ApplicationGatewayRequestRoutingRuleType ruleType() {
        return this.innerModel().ruleType();
    }

    @Override
    public ApplicationGatewayBackend backend() {
        SubResource backendRef = this.innerModel().backendAddressPool();
        if (backendRef != null) {
            String backendName = ResourceUtils.nameFromResourceId(backendRef.id());
            return this.parent().backends().get(backendName);
        } else {
            return null;
        }
    }

    @Override
    public ApplicationGatewayBackendHttpConfigurationImpl backendHttpConfiguration() {
        SubResource configRef = this.innerModel().backendHttpSettings();
        if (configRef != null) {
            String configName = ResourceUtils.nameFromResourceId(configRef.id());
            return (ApplicationGatewayBackendHttpConfigurationImpl)
                this.parent().backendHttpConfigurations().get(configName);
        } else {
            return null;
        }
    }

    @Override
    public ApplicationGatewayListenerImpl listener() {
        SubResource listenerRef = this.innerModel().httpListener();
        if (listenerRef != null) {
            String listenerName = ResourceUtils.nameFromResourceId(listenerRef.id());
            return (ApplicationGatewayListenerImpl) this.parent().listeners().get(listenerName);
        } else {
            return null;
        }
    }

    @Override
    public ApplicationGatewayRedirectConfiguration redirectConfiguration() {
        SubResource ref = this.innerModel().redirectConfiguration();
        if (ref == null) {
            return null;
        } else {
            return this.parent().redirectConfigurations().get(ResourceUtils.nameFromResourceId(ref.id()));
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
        SubResource httpConfigRef =
            new SubResource().withId(this.parent().futureResourceId() + "/backendHttpSettingsCollection/" + name);
        this.innerModel().withBackendHttpSettings(httpConfigRef);
        return this;
    }

    private ApplicationGatewayBackendHttpConfigurationImpl ensureBackendHttpConfig() {
        ApplicationGatewayBackendHttpConfigurationImpl config = this.backendHttpConfiguration();
        if (config == null) {
            final String name = this.parent().manager().resourceManager().internalContext()
                .randomResourceName("bckcfg", 11);
            config = this.parent().defineBackendHttpConfiguration(name);
            config.attach();
            this.toBackendHttpConfiguration(name);
        }
        return config;
    }

    @Override
    public ApplicationGatewayRequestRoutingRuleImpl toBackendHttpPort(int portNumber) {
        String name = this.parent().manager().resourceManager().internalContext().randomResourceName("backcfg", 12);
        this.parent().defineBackendHttpConfiguration(name).withPort(portNumber).attach();
        return this.toBackendHttpConfiguration(name);
    }

    @Override
    public ApplicationGatewayRequestRoutingRuleImpl withCookieBasedAffinity() {
        this.parent().updateBackendHttpConfiguration(ensureBackendHttpConfig().name()).withCookieBasedAffinity();
        return this;
    }

    @Override
    public ApplicationGatewayRequestRoutingRuleImpl withoutCookieBasedAffinity() {
        this.parent().updateBackendHttpConfiguration(ensureBackendHttpConfig().name()).withoutCookieBasedAffinity();
        return this;
    }

    // --- Listener handling

    @Override
    public ApplicationGatewayRequestRoutingRuleImpl fromListener(String name) {
        SubResource listenerRef = new SubResource().withId(this.parent().futureResourceId() + "/HTTPListeners/" + name);
        this.innerModel().withHttpListener(listenerRef);
        return this;
    }

    private ApplicationGatewayRequestRoutingRuleImpl fromFrontendPort(
        int portNumber, ApplicationGatewayProtocol protocol, String name) {
        // Verify no conflicting listener exists
        ApplicationGatewayListenerImpl listenerByPort =
            (ApplicationGatewayListenerImpl) this.parent().listenerByPortNumber(portNumber);
        ApplicationGatewayListenerImpl listenerByName = null;
        if (name != null) {
            listenerByName = (ApplicationGatewayListenerImpl) this.parent().listeners().get(name);
        }

        ApplicationGatewayImpl.CreationState needToCreate =
            this.parent().needToCreate(listenerByName, listenerByPort, name);
        if (needToCreate == ApplicationGatewayImpl.CreationState.NeedToCreate) {
            // If no listener exists for the requested port number yet and the name, create one
            if (name == null) {
                name = this.parent().manager().resourceManager().internalContext().randomResourceName("listener", 13);
            }

            listenerByPort = this.parent().defineListener(name).withFrontendPort(portNumber);

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
            final String name = this.parent().manager().resourceManager().internalContext()
                .randomResourceName("listener", 13);
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
    public ApplicationGatewayRequestRoutingRuleImpl withSslCertificateFromKeyVaultSecretId(String keyVaultSecretId) {
        this.parent().updateListener(ensureListener().name()).withSslCertificateFromKeyVaultSecretId(keyVaultSecretId);
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
    public ApplicationGatewayRequestRoutingRuleImpl withHostname(String hostName) {
        this.parent().updateListener(ensureListener().name()).withHostname(hostName);
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
            backend = this.parent().ensureUniqueBackend();
            this.toBackend(backend.name());
        }

        return backend;
    }

    @Override
    public ApplicationGatewayRequestRoutingRuleImpl toBackend(String name) {
        this.innerModel().withBackendAddressPool(this.parent().ensureBackendRef(name));
        return this;
    }

    @Override
    public ApplicationGatewayRequestRoutingRuleImpl toBackendIPAddress(String ipAddress) {
        this.parent().updateBackend(ensureBackend().name()).withIPAddress(ipAddress);
        return this;
    }

    @Override
    public ApplicationGatewayRequestRoutingRuleImpl toBackendIPAddresses(String... ipAddresses) {
        if (ipAddresses != null) {
            for (String ipAddress : ipAddresses) {
                this.toBackendIPAddress(ipAddress);
            }
        }
        return this;
    }

    @Override
    public ApplicationGatewayRequestRoutingRuleImpl toBackendFqdn(String fqdn) {
        this.parent().updateBackend(ensureBackend().name()).withFqdn(fqdn);
        return this;
    }

    @Override
    public ApplicationGatewayRequestRoutingRuleImpl withRedirectConfiguration(String name) {
        if (name == null) {
            this.innerModel().withRedirectConfiguration(null);
        } else {
            SubResource ref =
                new SubResource().withId(this.parent().futureResourceId() + "/redirectConfigurations/" + name);
            this.innerModel().withRedirectConfiguration(ref).withBackendAddressPool(null).withBackendHttpSettings(null);
        }
        return this;
    }

    @Override
    public ApplicationGatewayRequestRoutingRuleImpl withoutRedirectConfiguration() {
        this.innerModel().withRedirectConfiguration(null);
        return this;
    }

    @Override
    public DefinitionStages.WithAttach<ApplicationGateway.DefinitionStages.WithRequestRoutingRuleOrCreate>
        withUrlPathMap(String urlPathMapName) {
        if (urlPathMapName == null) {
            this.innerModel().withUrlPathMap(null);
        } else {
            SubResource ref =
                new SubResource().withId(this.parent().futureResourceId() + "/urlPathMaps/" + urlPathMapName);
            this.innerModel().withUrlPathMap(ref);
        }
        return this;
    }
}
