/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network.implementation;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import com.azure.management.network.ApplicationGatewayBackend;
import com.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.azure.management.network.ApplicationGateway;
import com.azure.management.network.ApplicationGatewayBackendHealth;
import com.azure.management.network.ApplicationGatewayBackendHealthHttpSettings;
import com.azure.management.network.ApplicationGatewayBackendHealthPool;
import com.azure.management.network.ApplicationGatewayBackendHttpConfigurationHealth;

/**
 * Implementation of application gateway backend health information.
 */
public class ApplicationGatewayBackendHealthImpl implements ApplicationGatewayBackendHealth {

    private final ApplicationGatewayBackendHealthPool inner;
    private final ApplicationGatewayImpl appGateway;
    private final Map<String, ApplicationGatewayBackendHttpConfigurationHealth> httpConfigHealths = new TreeMap<>();

    ApplicationGatewayBackendHealthImpl(ApplicationGatewayBackendHealthPool inner, ApplicationGatewayImpl appGateway) {
        this.inner = inner;
        this.appGateway = appGateway;
        if (inner != null) {
            for (ApplicationGatewayBackendHealthHttpSettings httpConfigInner : inner.backendHttpSettingsCollection()) {
                ApplicationGatewayBackendHttpConfigurationHealthImpl httpConfigHealth = new ApplicationGatewayBackendHttpConfigurationHealthImpl(httpConfigInner, this);
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
            return ResourceUtils.nameFromResourceId(this.inner.backendAddressPool().getId());
        } else {
            return null;
        }
    }

    @Override
    public ApplicationGatewayBackend backend() {
        if (this.inner.backendAddressPool() == null) {
            return null;
        }

        String backendName = ResourceUtils.nameFromResourceId(this.inner.backendAddressPool().getId());
        return this.appGateway.backends().get(backendName);
    }

    @Override
    public Map<String, ApplicationGatewayBackendHttpConfigurationHealth> httpConfigurationHealths() {
        return Collections.unmodifiableMap(this.httpConfigHealths);
    }

    @Override
    public ApplicationGateway parent() {
        return this.appGateway;
    }
}
