/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network.implementation;

import com.azure.management.network.ApplicationGatewayBackendHealthStatus;
import com.azure.management.network.ApplicationGatewayBackendHttpConfigurationHealth;
import com.azure.management.network.ApplicationGatewayBackendServerHealth;
import com.azure.management.network.NetworkInterface;
import com.azure.management.network.NicIPConfiguration;
import com.azure.management.network.models.ApplicationGatewayBackendHealthServerInner;
import com.azure.management.resources.fluentcore.arm.ResourceUtils;

/**
 * Implementation of application gateway backend server health information.
 */
public class ApplicationGatewayBackendServerHealthImpl implements ApplicationGatewayBackendServerHealth {

    private final ApplicationGatewayBackendHealthServerInner inner;
    private final ApplicationGatewayBackendHttpConfigurationHealthImpl httpConfigHealth;

    ApplicationGatewayBackendServerHealthImpl(ApplicationGatewayBackendHealthServerInner inner, ApplicationGatewayBackendHttpConfigurationHealthImpl httpConfigHealth) {
        this.inner = inner;
        this.httpConfigHealth = httpConfigHealth;
    }

    @Override
    public ApplicationGatewayBackendHealthServerInner inner() {
        return this.inner;
    }

    @Override
    public ApplicationGatewayBackendHttpConfigurationHealth parent() {
        return this.httpConfigHealth;
    }

    @Override
    public String ipAddress() {
        return this.inner().address();
    }

    @Override
    public NicIPConfiguration getNetworkInterfaceIPConfiguration() {
        if (this.inner().ipConfiguration() == null) {
            return null;
        }
        String nicIPConfigId = this.inner().ipConfiguration().getId();
        if (nicIPConfigId == null) {
            return null;
        }

        String nicIPConfigName = ResourceUtils.nameFromResourceId(nicIPConfigId);
        String nicId = ResourceUtils.parentResourceIdFromResourceId(nicIPConfigId);
        NetworkInterface nic = this.parent().parent().parent().manager().networkInterfaces().getById(nicId);
        if (nic == null) {
            return null;
        } else {
            return nic.ipConfigurations().get(nicIPConfigName);
        }
    }

    @Override
    public ApplicationGatewayBackendHealthStatus status() {
        return ApplicationGatewayBackendHealthStatus.fromString(this.inner.health().toString());
    }
}
