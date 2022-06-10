// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.compute.implementation;

import com.azure.resourcemanager.compute.models.VirtualMachineExtensionInstanceView;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetVM;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetVMInstanceExtension;
import com.azure.resourcemanager.compute.fluent.models.VirtualMachineExtensionInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ChildResourceImpl;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/** Implementation of {@link VirtualMachineScaleSetVMInstanceExtension}. */
class VirtualMachineScaleSetVMInstanceExtensionImpl
    extends ChildResourceImpl<VirtualMachineExtensionInner, VirtualMachineScaleSetVMImpl, VirtualMachineScaleSetVM>
    implements VirtualMachineScaleSetVMInstanceExtension {

    private HashMap<String, Object> publicSettings;
    private HashMap<String, Object> protectedSettings;

    VirtualMachineScaleSetVMInstanceExtensionImpl(
        VirtualMachineExtensionInner inner, VirtualMachineScaleSetVMImpl parent) {
        super(inner, parent);
        initializeSettings();
    }

    @Override
    public String name() {
        return this.innerModel().name();
    }

    @Override
    public String publisherName() {
        return this.innerModel().publisher();
    }

    @Override
    public String typeName() {
        return this.innerModel().type();
    }

    @Override
    public String versionName() {
        return this.innerModel().typeHandlerVersion();
    }

    @Override
    public boolean autoUpgradeMinorVersionEnabled() {
        return this.innerModel().autoUpgradeMinorVersion();
    }

    @Override
    public Map<String, Object> publicSettings() {
        return Collections.unmodifiableMap(this.publicSettings);
    }

    @Override
    public String publicSettingsAsJsonString() {
        return null;
    }

    @Override
    public String provisioningState() {
        return this.innerModel().provisioningState();
    }

    @Override
    public VirtualMachineExtensionInstanceView instanceView() {
        return this.innerModel().instanceView();
    }

    @Override
    public Map<String, String> tags() {
        if (this.innerModel().tags() == null) {
            return Collections.unmodifiableMap(new LinkedHashMap<String, String>());
        }
        return Collections.unmodifiableMap(this.innerModel().tags());
    }

    @SuppressWarnings("unchecked")
    private void initializeSettings() {
        if (this.innerModel().settings() == null) {
            this.publicSettings = new LinkedHashMap<>();
            this.innerModel().withSettings(this.publicSettings);
        } else {
            this.publicSettings = (LinkedHashMap<String, Object>) this.innerModel().settings();
        }

        if (this.innerModel().protectedSettings() == null) {
            this.protectedSettings = new LinkedHashMap<>();
            this.innerModel().withProtectedSettings(this.protectedSettings);
        } else {
            this.protectedSettings = (LinkedHashMap<String, Object>) this.innerModel().protectedSettings();
        }
    }
}
