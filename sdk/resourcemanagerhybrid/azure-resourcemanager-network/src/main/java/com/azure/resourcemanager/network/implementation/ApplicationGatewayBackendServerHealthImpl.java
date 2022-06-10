// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.models.ApplicationGatewayBackendHealthStatus;
import com.azure.resourcemanager.network.models.ApplicationGatewayBackendHttpConfigurationHealth;
import com.azure.resourcemanager.network.models.ApplicationGatewayBackendServerHealth;
import com.azure.resourcemanager.network.models.NetworkInterface;
import com.azure.resourcemanager.network.models.NicIpConfiguration;
import com.azure.resourcemanager.network.fluent.models.ApplicationGatewayBackendHealthServerInner;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;

/** Implementation of application gateway backend server health information. */
public class ApplicationGatewayBackendServerHealthImpl implements ApplicationGatewayBackendServerHealth {

    private final ApplicationGatewayBackendHealthServerInner inner;
    private final ApplicationGatewayBackendHttpConfigurationHealthImpl httpConfigHealth;

    ApplicationGatewayBackendServerHealthImpl(
        ApplicationGatewayBackendHealthServerInner inner,
        ApplicationGatewayBackendHttpConfigurationHealthImpl httpConfigHealth) {
        this.inner = inner;
        this.httpConfigHealth = httpConfigHealth;
    }

    @Override
    public ApplicationGatewayBackendHealthServerInner innerModel() {
        return this.inner;
    }

    @Override
    public ApplicationGatewayBackendHttpConfigurationHealth parent() {
        return this.httpConfigHealth;
    }

    @Override
    public String ipAddress() {
        return this.innerModel().address();
    }

    @Override
    public NicIpConfiguration getNetworkInterfaceIPConfiguration() {
        if (this.innerModel().ipConfiguration() == null) {
            return null;
        }
        String nicIPConfigId = this.innerModel().ipConfiguration().id();
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
