/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.ApplicationGateway;
import com.microsoft.azure.management.network.ApplicationGatewayBackendHealth;
import com.microsoft.azure.management.network.ApplicationGatewayBackendHealthHttpSettings;
import com.microsoft.azure.management.network.ApplicationGatewayBackendHttpConfiguration;
import com.microsoft.azure.management.network.ApplicationGatewayBackendHttpConfigurationHealth;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;

/**
 * Implementation of application gateway backend HTTP configuration health information.
 */
@LangDefinition
public class ApplicationGatewayBackendHttpConfigurationHealthImpl implements ApplicationGatewayBackendHttpConfigurationHealth {

    private final ApplicationGatewayBackendHealthHttpSettings inner;
    private final ApplicationGatewayBackendHealthImpl backendHealth;

    ApplicationGatewayBackendHttpConfigurationHealthImpl(ApplicationGatewayBackendHealthHttpSettings inner, ApplicationGatewayBackendHealthImpl backendHealth) {
        this.inner = inner;
        this.backendHealth = backendHealth;
    }

    @Override
    public ApplicationGatewayBackendHealthHttpSettings inner() {
        return this.inner;
    }

    @Override
    public String name() {
        if (this.inner.backendHttpSettings() != null) {
            return ResourceUtils.nameFromResourceId(this.inner.backendHttpSettings().id());
        } else {
            return null;
        }
    }

    @Override
    public ApplicationGatewayBackendHttpConfiguration getBackendHttpConfiguration() {
        if (this.inner.backendHttpSettings() == null) {
            return null;
        }

        String appGatewayId = ResourceUtils.parentResourceIdFromResourceId(this.inner.backendHttpSettings().id());
        String name = ResourceUtils.nameFromResourceId(this.inner.backendHttpSettings().id());

        ApplicationGateway appGateway = this.parent().manager().applicationGateways().getById(appGatewayId);
        if (appGateway == null) {
            return null;
        }

        return appGateway.backendHttpConfigurations().get(name);
    }

    @Override
    public ApplicationGatewayBackendHealth parent() {
        return this.backendHealth;
    }
}
