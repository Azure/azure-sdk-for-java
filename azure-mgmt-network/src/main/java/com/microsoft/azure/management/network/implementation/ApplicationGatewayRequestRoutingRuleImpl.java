/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.ApplicationGateway;
import com.microsoft.azure.management.network.ApplicationGatewayBackend;
import com.microsoft.azure.management.network.ApplicationGatewayBackendHttpConfiguration;
import com.microsoft.azure.management.network.ApplicationGatewayFrontendListener;
import com.microsoft.azure.management.network.ApplicationGatewayRequestRoutingRule;
import com.microsoft.azure.management.network.ApplicationGatewayRequestRoutingRuleType;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

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
    public ApplicationGatewayFrontendListener frontendHttpListener() {
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
        SubResource listenerRef = new SubResource()
                .withId(this.parent().futureResourceId() + "/HTTPListeners/" + name);
        this.inner().withHttpListener(listenerRef);
        return this;
    }

    @Override
    public ApplicationGatewayRequestRoutingRuleImpl fromFrontendPort(int portNumber) {
        ApplicationGatewayFrontendListener listener = this.parent().getFrontendListenerByPortNumber(portNumber);
        if (listener == null) {
            return null;
        } else {
            return this.fromFrontendListener(listener.name());
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
}
