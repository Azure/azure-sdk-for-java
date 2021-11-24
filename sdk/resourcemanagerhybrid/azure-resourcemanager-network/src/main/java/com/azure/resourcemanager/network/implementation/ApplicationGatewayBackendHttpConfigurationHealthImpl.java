// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.models.ApplicationGatewayBackendHealth;
import com.azure.resourcemanager.network.models.ApplicationGatewayBackendHealthHttpSettings;
import com.azure.resourcemanager.network.models.ApplicationGatewayBackendHttpConfiguration;
import com.azure.resourcemanager.network.models.ApplicationGatewayBackendHttpConfigurationHealth;
import com.azure.resourcemanager.network.models.ApplicationGatewayBackendServerHealth;
import com.azure.resourcemanager.network.fluent.models.ApplicationGatewayBackendHealthServerInner;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/** Implementation of application gateway backend HTTP configuration health information. */
public class ApplicationGatewayBackendHttpConfigurationHealthImpl
    implements ApplicationGatewayBackendHttpConfigurationHealth {

    private final ApplicationGatewayBackendHealthHttpSettings inner;
    private final ApplicationGatewayBackendHealthImpl backendHealth;
    private final Map<String, ApplicationGatewayBackendServerHealth> serverHealths = new TreeMap<>();

    ApplicationGatewayBackendHttpConfigurationHealthImpl(
        ApplicationGatewayBackendHealthHttpSettings inner, ApplicationGatewayBackendHealthImpl backendHealth) {
        this.inner = inner;
        this.backendHealth = backendHealth;

        if (inner.servers() != null) {
            for (ApplicationGatewayBackendHealthServerInner serverHealthInner : this.innerModel().servers()) {
                ApplicationGatewayBackendServerHealth serverHealth =
                    new ApplicationGatewayBackendServerHealthImpl(serverHealthInner, this);
                this.serverHealths.put(serverHealth.ipAddress(), serverHealth);
            }
        }
    }

    @Override
    public ApplicationGatewayBackendHealthHttpSettings innerModel() {
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
