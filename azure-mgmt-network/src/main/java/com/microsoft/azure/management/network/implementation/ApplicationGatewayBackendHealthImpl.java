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
import com.microsoft.azure.management.network.ApplicationGateway;
import com.microsoft.azure.management.network.ApplicationGatewayBackend;
import com.microsoft.azure.management.network.ApplicationGatewayBackendHealth;
import com.microsoft.azure.management.network.ApplicationGatewayBackendHealthHttpSettings;
import com.microsoft.azure.management.network.ApplicationGatewayBackendHealthPool;
import com.microsoft.azure.management.network.ApplicationGatewayBackendHttpConfigurationHealth;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;

/**
 * Implementation of application gateway backend health information.
 */
@LangDefinition
public class ApplicationGatewayBackendHealthImpl implements ApplicationGatewayBackendHealth {

    private final ApplicationGatewayBackendHealthPool inner;
    private final NetworkManager manager;
    private final Map<String, ApplicationGatewayBackendHttpConfigurationHealth> httpConfigHealths = new TreeMap<>();

    ApplicationGatewayBackendHealthImpl(ApplicationGatewayBackendHealthPool inner, NetworkManager manager) {
        this.inner = inner;
        this.manager = manager;
        if (inner != null) {
            for (ApplicationGatewayBackendHealthHttpSettings httpConfigInner : inner.backendHttpSettingsCollection()) {
                ApplicationGatewayBackendHttpConfigurationHealthImpl httpConfigHealth  = new ApplicationGatewayBackendHttpConfigurationHealthImpl(httpConfigInner, this);
                this.httpConfigHealths.put(httpConfigHealth.name(), httpConfigHealth);
            }
        }
    }

    @Override
    public ApplicationGatewayBackendHealthPool inner() {
        return this.inner;
    }

    @Override
    public String name() {
        if (this.inner.backendAddressPool() != null) {
            return ResourceUtils.nameFromResourceId(this.inner.backendAddressPool().id());
        } else {
            return null;
        }
    }

    @Override
    public ApplicationGatewayBackend getBackend() {
        if (this.inner.backendAddressPool() == null) {
            return null;
        }

        String appGatewayId = ResourceUtils.parentResourceIdFromResourceId(this.inner.backendAddressPool().id());
        String backendName = ResourceUtils.nameFromResourceId(this.inner.backendAddressPool().id());

        ApplicationGateway appGateway = this.manager().applicationGateways().getById(appGatewayId);
        if (appGateway == null) {
            return null;
        }

        return appGateway.backends().get(backendName);
    }

    @Override
    public NetworkManager manager() {
        return this.manager;
    }

    @Override
    public Map<String, ApplicationGatewayBackendHttpConfigurationHealth> httpConfigurationHealths() {
        return Collections.unmodifiableMap(this.httpConfigHealths);
    }
}
