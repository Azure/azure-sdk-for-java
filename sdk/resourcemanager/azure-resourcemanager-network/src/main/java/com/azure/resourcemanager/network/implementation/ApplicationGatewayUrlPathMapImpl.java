// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.core.management.SubResource;
import com.azure.resourcemanager.network.models.ApplicationGateway;
import com.azure.resourcemanager.network.models.ApplicationGatewayBackend;
import com.azure.resourcemanager.network.models.ApplicationGatewayBackendHttpConfiguration;
import com.azure.resourcemanager.network.models.ApplicationGatewayPathRule;
import com.azure.resourcemanager.network.models.ApplicationGatewayRedirectConfiguration;
import com.azure.resourcemanager.network.models.ApplicationGatewayUrlPathMap;
import com.azure.resourcemanager.network.fluent.models.ApplicationGatewayPathRuleInner;
import com.azure.resourcemanager.network.fluent.models.ApplicationGatewayUrlPathMapInner;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ChildResourceImpl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Implementation for application gateway URL path map. */
class ApplicationGatewayUrlPathMapImpl
    extends ChildResourceImpl<ApplicationGatewayUrlPathMapInner, ApplicationGatewayImpl, ApplicationGateway>
    implements ApplicationGatewayUrlPathMap,
        ApplicationGatewayUrlPathMap.Definition<ApplicationGateway.DefinitionStages.WithRequestRoutingRuleOrCreate>,
        ApplicationGatewayUrlPathMap.UpdateDefinition<ApplicationGateway.Update>,
        ApplicationGatewayUrlPathMap.Update {
    private Map<String, ApplicationGatewayPathRule> pathRules;

    ApplicationGatewayUrlPathMapImpl(ApplicationGatewayUrlPathMapInner inner, ApplicationGatewayImpl parent) {
        super(inner, parent);
        initializePathRules();
    }

    private void initializePathRules() {
        pathRules = new HashMap<>();
        if (innerModel().pathRules() != null) {
            for (ApplicationGatewayPathRuleInner inner : innerModel().pathRules()) {
                pathRules.put(inner.name(), new ApplicationGatewayPathRuleImpl(inner, this));
            }
        }
    }

    // Getters
    @Override
    public String name() {
        return this.innerModel().name();
    }

    @Override
    public Map<String, ApplicationGatewayPathRule> pathRules() {
        return Collections.unmodifiableMap(pathRules);
    }

    @Override
    public ApplicationGatewayBackend defaultBackend() {
        SubResource backendRef = this.innerModel().defaultBackendAddressPool();
        return (backendRef != null)
            ? this.parent().backends().get(ResourceUtils.nameFromResourceId(backendRef.id()))
            : null;
    }

    @Override
    public ApplicationGatewayBackendHttpConfiguration defaultBackendHttpConfiguration() {
        SubResource backendHttpConfigRef = this.innerModel().defaultBackendHttpSettings();
        return (backendHttpConfigRef != null)
            ? this.parent().backendHttpConfigurations().get(ResourceUtils.nameFromResourceId(backendHttpConfigRef.id()))
            : null;
    }

    @Override
    public ApplicationGatewayRedirectConfiguration defaultRedirectConfiguration() {
        SubResource redirectRef = this.innerModel().defaultRedirectConfiguration();
        return (redirectRef != null)
            ? this.parent().redirectConfigurations().get(ResourceUtils.nameFromResourceId(redirectRef.id()))
            : null;
    }

    // Verbs

    @Override
    public ApplicationGatewayImpl attach() {
        return this.parent().withUrlPathMap(this);
    }

    @Override
    public ApplicationGatewayUrlPathMapImpl toBackendHttpConfiguration(String name) {
        SubResource httpConfigRef =
            new SubResource().withId(this.parent().futureResourceId() + "/backendHttpSettingsCollection/" + name);
        this.innerModel().withDefaultBackendHttpSettings(httpConfigRef);
        return this;
    }

    @Override
    public ApplicationGatewayUrlPathMapImpl toBackendHttpPort(int portNumber) {
        String name = this.parent().manager().resourceManager().internalContext().randomResourceName("backcfg", 12);
        this.parent().defineBackendHttpConfiguration(name).withPort(portNumber).attach();
        return this.toBackendHttpConfiguration(name);
    }

    @Override
    public ApplicationGatewayUrlPathMapImpl toBackend(String name) {
        this.innerModel().withDefaultBackendAddressPool(this.parent().ensureBackendRef(name));
        return this;
    }

    @Override
    public ApplicationGatewayUrlPathMapImpl withRedirectConfiguration(String name) {
        if (name == null) {
            this.innerModel().withDefaultRedirectConfiguration(null);
        } else {
            SubResource ref =
                new SubResource().withId(this.parent().futureResourceId() + "/redirectConfigurations/" + name);
            this
                .innerModel()
                .withDefaultRedirectConfiguration(ref)
                .withDefaultBackendAddressPool(null)
                .withDefaultBackendHttpSettings(null);
        }
        return this;
    }

    @Override
    public ApplicationGatewayPathRuleImpl definePathRule(String name) {
        return new ApplicationGatewayPathRuleImpl(new ApplicationGatewayPathRuleInner().withName(name), this);
    }

    ApplicationGatewayUrlPathMapImpl withPathRule(ApplicationGatewayPathRuleImpl pathRule) {
        if (pathRule != null) {
            if (innerModel().pathRules() == null) {
                innerModel().withPathRules(new ArrayList<ApplicationGatewayPathRuleInner>());
            }
            innerModel().pathRules().add(pathRule.innerModel());
        }
        return this;
    }

    @Override
    public ApplicationGatewayUrlPathMapImpl fromListener(String name) {
        SubResource listenerRef = new SubResource().withId(this.parent().futureResourceId() + "/HTTPListeners/" + name);
        parent().requestRoutingRules().get(this.name()).innerModel().withHttpListener(listenerRef);
        return this;
    }

    @Override
    public ApplicationGatewayUrlPathMapImpl toBackendIPAddress(String ipAddress) {
        this.parent().updateBackend(ensureBackend().name()).withIPAddress(ipAddress);
        return this;
    }

    @Override
    public ApplicationGatewayUrlPathMapImpl toBackendIPAddresses(String... ipAddresses) {
        if (ipAddresses != null) {
            for (String ipAddress : ipAddresses) {
                this.toBackendIPAddress(ipAddress);
            }
        }
        return this;
    }

    @Override
    public ApplicationGatewayUrlPathMapImpl toBackendFqdn(String fqdn) {
        this.parent().updateBackend(ensureBackend().name()).withFqdn(fqdn);
        return this;
    }

    private ApplicationGatewayBackendImpl ensureBackend() {
        ApplicationGatewayBackendImpl backend = (ApplicationGatewayBackendImpl) this.defaultBackend();
        if (backend == null) {
            backend = this.parent().ensureUniqueBackend();
            this.toBackend(backend.name());
        }
        return backend;
    }
}
