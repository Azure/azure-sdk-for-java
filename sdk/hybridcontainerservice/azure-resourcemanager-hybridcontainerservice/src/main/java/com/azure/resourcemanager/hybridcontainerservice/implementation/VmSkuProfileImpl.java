// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.hybridcontainerservice.implementation;

import com.azure.core.management.SystemData;
import com.azure.resourcemanager.hybridcontainerservice.fluent.models.VmSkuProfileInner;
import com.azure.resourcemanager.hybridcontainerservice.models.ExtendedLocation;
import com.azure.resourcemanager.hybridcontainerservice.models.VmSkuProfile;
import com.azure.resourcemanager.hybridcontainerservice.models.VmSkuProfileProperties;

public final class VmSkuProfileImpl implements VmSkuProfile {
    private VmSkuProfileInner innerObject;

    private final com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager serviceManager;

    VmSkuProfileImpl(VmSkuProfileInner innerObject,
        com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager serviceManager) {
        this.innerObject = innerObject;
        this.serviceManager = serviceManager;
    }

    public String id() {
        return this.innerModel().id();
    }

    public String name() {
        return this.innerModel().name();
    }

    public String type() {
        return this.innerModel().type();
    }

    public ExtendedLocation extendedLocation() {
        return this.innerModel().extendedLocation();
    }

    public VmSkuProfileProperties properties() {
        return this.innerModel().properties();
    }

    public SystemData systemData() {
        return this.innerModel().systemData();
    }

    public VmSkuProfileInner innerModel() {
        return this.innerObject;
    }

    private com.azure.resourcemanager.hybridcontainerservice.HybridContainerServiceManager manager() {
        return this.serviceManager;
    }
}
