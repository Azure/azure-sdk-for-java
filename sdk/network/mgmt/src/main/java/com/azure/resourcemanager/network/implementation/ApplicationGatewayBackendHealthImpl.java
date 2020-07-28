// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.models.ApplicationGateway;
import com.azure.resourcemanager.network.models.ApplicationGatewayBackend;
import com.azure.resourcemanager.network.models.ApplicationGatewayBackendHealth;
import com.azure.resourcemanager.network.models.ApplicationGatewayBackendHealthHttpSettings;
import com.azure.resourcemanager.network.models.ApplicationGatewayBackendHealthPool;
import com.azure.resourcemanager.network.models.ApplicationGatewayBackendHttpConfigurationHealth;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/** Implementation of application gateway backend health information. */
public class ApplicationGatewayBackendHealthImpl implements ApplicationGatewayBackendHealth {

    private final ApplicationGatewayBackendHealthPool inner;
    private final ApplicationGatewayImpl appGateway;
    private final Map<String, ApplicationGatewayBackendHttpConfigurationHealth> httpConfigHealths = new TreeMap<>();

    ApplicationGatewayBackendHealthImpl(ApplicationGatewayBackendHealthPool inner, ApplicationGatewayImpl appGateway) {
        this.inner = inner;
        this.appGateway = appGateway;
        if (inner != null) {
            for (ApplicationGatewayBackendHealthHttpSettings httpConfigInner : inner.backendHttpSettingsCollection()) {
                ApplicationGatewayBackendHttpConfigurationHealthImpl httpConfigHealth =
                    new ApplicationGatewayBackendHttpConfigurationHealthImpl(httpConfigInner, this);
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
    public ApplicationGatewayBackend backend() {
        if (this.inner.backendAddressPool() == null) {
            return null;
        }

        String backendName = ResourceUtils.nameFromResourceId(this.inner.backendAddressPool().id());
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
