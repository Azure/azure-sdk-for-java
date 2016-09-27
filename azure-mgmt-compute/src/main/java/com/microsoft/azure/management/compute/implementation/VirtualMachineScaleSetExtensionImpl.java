package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.compute.VirtualMachineExtensionImage;
import com.microsoft.azure.management.compute.VirtualMachineScaleSet;
import com.microsoft.azure.management.compute.VirtualMachineScaleSetExtension;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Implementation of {@link VirtualMachineScaleSetExtension}.
 */
@LangDefinition()
public class VirtualMachineScaleSetExtensionImpl
        extends ChildResourceImpl<VirtualMachineScaleSetExtensionInner, VirtualMachineScaleSetImpl, VirtualMachineScaleSet>
        implements
        VirtualMachineScaleSetExtension,
        VirtualMachineScaleSetExtension.Definition<VirtualMachineScaleSet.DefinitionStages.WithCreate>,
        VirtualMachineScaleSetExtension.UpdateDefinition<VirtualMachineScaleSet.UpdateStages.WithApply>,
        VirtualMachineScaleSetExtension.Update {

    private HashMap<String, Object> publicSettings;
    private HashMap<String, Object> protectedSettings;

    protected VirtualMachineScaleSetExtensionImpl(VirtualMachineScaleSetExtensionInner inner,
                                                  VirtualMachineScaleSetImpl parent) {
        super(inner, parent);
        initializeSettings();
    }

    // Getters
    //
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

    // Withers
    //

    @Override
    public VirtualMachineScaleSetExtensionImpl withMinorVersionAutoUpgrade() {
        this.inner().withAutoUpgradeMinorVersion(true);
        return this;
    }

    @Override
    public VirtualMachineScaleSetExtensionImpl withoutMinorVersionAutoUpgrade() {
        this.inner().withAutoUpgradeMinorVersion(false);
        return this;
    }

    @Override
    public VirtualMachineScaleSetExtensionImpl withImage(VirtualMachineExtensionImage image) {
        this.inner().withPublisher(image.publisherName())
                .withType(image.typeName())
                .withTypeHandlerVersion(image.versionName());
        return this;
    }

    @Override
    public VirtualMachineScaleSetExtensionImpl withPublisher(String extensionImagePublisherName) {
        this.inner().withPublisher(extensionImagePublisherName);
        return this;
    }

    @Override
    public VirtualMachineScaleSetExtensionImpl withPublicSetting(String key, Object value) {
        this.publicSettings.put(key, value);
        return this;
    }

    @Override
    public VirtualMachineScaleSetExtensionImpl withProtectedSetting(String key, Object value) {
        this.protectedSettings.put(key, value);
        return this;
    }

    @Override
    public VirtualMachineScaleSetExtensionImpl withPublicSettings(HashMap<String, Object> settings) {
        this.publicSettings.clear();
        this.publicSettings.putAll(settings);
        return this;
    }

    @Override
    public VirtualMachineScaleSetExtensionImpl withProtectedSettings(HashMap<String, Object> settings) {
        this.protectedSettings.clear();
        this.protectedSettings.putAll(settings);
        return this;
    }

    @Override
    public VirtualMachineScaleSetExtensionImpl withType(String extensionImageTypeName) {
        this.inner().withType(extensionImageTypeName);
        return this;
    }

    @Override
    public VirtualMachineScaleSetExtensionImpl withVersion(String extensionImageVersionName) {
        this.inner().withTypeHandlerVersion(extensionImageVersionName);
        return this;
    }

    // Helper methods
    //
    private void nullifySettingsIfEmpty() {
        if (this.publicSettings.size() == 0) {
            this.inner().withSettings(null);
        }
        if (this.protectedSettings.size() == 0) {
            this.inner().withProtectedSettings(null);
        }
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

    @Override
    public VirtualMachineScaleSetImpl attach() {
        nullifySettingsIfEmpty();
        return this.parent().withExtension(this);
    }
}
