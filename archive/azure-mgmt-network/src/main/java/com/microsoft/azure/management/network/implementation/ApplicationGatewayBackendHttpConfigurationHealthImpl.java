/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.ApplicationGatewayBackendHealth;
import com.microsoft.azure.management.network.ApplicationGatewayBackendHealthHttpSettings;
import com.microsoft.azure.management.network.ApplicationGatewayBackendHealthServer;
import com.microsoft.azure.management.network.ApplicationGatewayBackendHttpConfiguration;
import com.microsoft.azure.management.network.ApplicationGatewayBackendHttpConfigurationHealth;
import com.microsoft.azure.management.network.ApplicationGatewayBackendServerHealth;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;

/**
 * Implementation of application gateway backend HTTP configuration health information.
 */
@LangDefinition
public class ApplicationGatewayBackendHttpConfigurationHealthImpl implements ApplicationGatewayBackendHttpConfigurationHealth {

    private final ApplicationGatewayBackendHealthHttpSettings inner;
    private final ApplicationGatewayBackendHealthImpl backendHealth;
    private final Map<String, ApplicationGatewayBackendServerHealth> serverHealths = new TreeMap<>();

    ApplicationGatewayBackendHttpConfigurationHealthImpl(ApplicationGatewayBackendHealthHttpSettings inner, ApplicationGatewayBackendHealthImpl backendHealth) {
        this.inner = inner;
        this.backendHealth = backendHealth;

        if (inner.servers() != null) {
            for (ApplicationGatewayBackendHealthServer serverHealthInner : this.inner().servers()) {
                ApplicationGatewayBackendServerHealth serverHealth = new ApplicationGatewayBackendServerHealthImpl(serverHealthInner, this);
                this.serverHealths.put(serverHealth.ipAddress(), serverHealth);
            }
        }
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
    public ApplicationGatewayBackendHttpConfiguration backendHttpConfiguration() {
        if (this.inner.backendHttpSettings() == null) {
            return null;
        }

        String name = ResourceUtils.nameFromResourceId(this.inner.backendHttpSettings().id());
        return this.parent().parent().backendHttpConfigurations().get(name);
    }

    @Override
    public ApplicationGatewayBackendHealth parent() {
        return this.backendHealth;
    }

    @Override
    public Map<String, ApplicationGatewayBackendServerHealth> serverHealths() {
        return Collections.unmodifiableMap(this.serverHealths);
    }
}
