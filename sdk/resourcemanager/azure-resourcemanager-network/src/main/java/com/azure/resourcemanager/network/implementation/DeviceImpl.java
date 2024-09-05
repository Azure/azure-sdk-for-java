// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.models.Device;
import com.azure.resourcemanager.network.models.DeviceProperties;
import com.azure.resourcemanager.network.models.VpnSite;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

/** Implementation for VPN device. */
public class DeviceImpl
    extends ChildResourceImpl<DeviceProperties, VpnSiteImpl, VpnSite>
    implements Device,
    Device.Definition<VpnSite.DefinitionStages.WithCreate>,
    Device.UpdateDefinition<VpnSite.Update>,
    Device.Update {

    DeviceImpl(DeviceProperties innerObject, VpnSiteImpl parent) {
        super(innerObject, parent);
    }

    @Override
    public String deviceVendor() {
        return this.innerModel().deviceVendor();
    }

    @Override
    public String deviceModel() {
        return this.innerModel().deviceModel();
    }

    @Override
    public Integer linkSpeedInMbps() {
        return this.innerModel().linkSpeedInMbps();
    }

    @Override
    public DeviceImpl withDeviceVendor(String deviceVendor) {
        this.innerModel().withDeviceVendor(deviceVendor);
        return this;
    }

    @Override
    public DeviceImpl withDeviceModel(String deviceModel) {
        this.innerModel().withDeviceModel(deviceModel);
        return this;
    }

    @Override
    public DeviceImpl withLinkSpeedInMbps(Integer linkSpeedInMbps) {
        this.innerModel().withLinkSpeedInMbps(linkSpeedInMbps);
        return this;
    }

    @Override
    public String name() {
        return "";
    }

    @Override
    public VpnSiteImpl attach() {
        this.parent().innerModel().withDeviceProperties(this.innerModel());
        return this.parent();
    }
}
