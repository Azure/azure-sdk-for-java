package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.compute.VirtualMachineExtensionInstanceView;
import com.microsoft.azure.management.compute.VirtualMachineScaleSetVM;
import com.microsoft.azure.management.compute.VirtualMachineScaleSetVMInstanceExtension;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Implementation of {@link VirtualMachineScaleSetVMInstanceExtension}.
 */
@LangDefinition
class VirtualMachineScaleSetVMInstanceExtensionImpl extends
        ChildResourceImpl<VirtualMachineExtensionInner, VirtualMachineScaleSetVMImpl, VirtualMachineScaleSetVM>
        implements VirtualMachineScaleSetVMInstanceExtension {

    private HashMap<String, Object> publicSettings;
    private HashMap<String, Object> protectedSettings;

    VirtualMachineScaleSetVMInstanceExtensionImpl(VirtualMachineExtensionInner inner,
                                                  VirtualMachineScaleSetVMImpl parent) {
        super(inner, parent);
        initializeSettings();
    }

    @Override
    public String name() {
        return this.inner().name();
    }

    @Override
    public String publisherName() {
        return this.inner().publisher();
    }

    @Override
    public String typeName() {
        return this.inner().type();
    }

    @Override
    public String versionName() {
        return this.inner().typeHandlerVersion();
    }

    @Override
    public boolean autoUpgradeMinorVersionEnabled() {
        return this.inner().autoUpgradeMinorVersion();
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
        return this.inner().provisioningState();
    }

    @Override
    public VirtualMachineExtensionInstanceView instanceView() {
        return this.inner().instanceView();
    }

    @Override
    public Map<String, String> tags() {
        if (this.inner().getTags() == null) {
            return Collections.unmodifiableMap(new LinkedHashMap<String, String>());
        }
        return Collections.unmodifiableMap(this.inner().getTags());
    }

    private void initializeSettings() {
        if (this.inner().settings() == null) {
            this.publicSettings = new LinkedHashMap<>();
            this.inner().withSettings(this.publicSettings);
        } else {
            this.publicSettings = (LinkedHashMap<String, Object>) this.inner().settings();
        }

        if (this.inner().protectedSettings() == null) {
            this.protectedSettings = new LinkedHashMap<>();
            this.inner().withProtectedSettings(this.protectedSettings);
        } else {
            this.protectedSettings = (LinkedHashMap<String, Object>) this.inner().protectedSettings();
        }
    }
}
