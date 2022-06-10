// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.compute.implementation;

import com.azure.resourcemanager.compute.models.VirtualMachineExtensionImage;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSet;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetExtension;
import com.azure.resourcemanager.compute.fluent.models.VirtualMachineScaleSetExtensionInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ChildResourceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/** Implementation of {@link VirtualMachineScaleSetExtension}. */
public class VirtualMachineScaleSetExtensionImpl
    extends ChildResourceImpl<VirtualMachineScaleSetExtensionInner, VirtualMachineScaleSetImpl, VirtualMachineScaleSet>
    implements VirtualMachineScaleSetExtension,
        VirtualMachineScaleSetExtension.Definition<VirtualMachineScaleSet.DefinitionStages.WithCreate>,
        VirtualMachineScaleSetExtension.UpdateDefinition<VirtualMachineScaleSet.UpdateStages.WithApply>,
        VirtualMachineScaleSetExtension.Update {

    protected VirtualMachineScaleSetExtensionImpl(
        VirtualMachineScaleSetExtensionInner inner, VirtualMachineScaleSetImpl parent) {
        super(inner, parent);
    }

    // Getters
    //
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
    @SuppressWarnings("unchecked")
    public Map<String, Object> publicSettings() {
        if (this.innerModel().settings() == null) {
            return Collections.unmodifiableMap(new LinkedHashMap<String, Object>());
        }
        return Collections.unmodifiableMap((LinkedHashMap<String, Object>) this.innerModel().settings());
    }

    @Override
    public String publicSettingsAsJsonString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this.publicSettings());
        } catch (JsonProcessingException jex) {
            return null;
        }
    }

    @Override
    public String provisioningState() {
        return this.innerModel().provisioningState();
    }

    // Withers
    //

    @Override
    public VirtualMachineScaleSetExtensionImpl withMinorVersionAutoUpgrade() {
        this.innerModel().withAutoUpgradeMinorVersion(true);
        return this;
    }

    @Override
    public VirtualMachineScaleSetExtensionImpl withoutMinorVersionAutoUpgrade() {
        this.innerModel().withAutoUpgradeMinorVersion(false);
        return this;
    }

    @Override
    public VirtualMachineScaleSetExtensionImpl withImage(VirtualMachineExtensionImage image) {
        this
            .innerModel()
            .withPublisher(image.publisherName())
            .withTypePropertiesType(image.typeName())
            .withTypeHandlerVersion(image.versionName());
        return this;
    }

    @Override
    public VirtualMachineScaleSetExtensionImpl withPublisher(String extensionImagePublisherName) {
        this.innerModel().withPublisher(extensionImagePublisherName);
        return this;
    }

    @Override
    public VirtualMachineScaleSetExtensionImpl withPublicSetting(String key, Object value) {
        this.ensurePublicSettings().put(key, value);
        return this;
    }

    @Override
    public VirtualMachineScaleSetExtensionImpl withProtectedSetting(String key, Object value) {
        this.ensureProtectedSettings().put(key, value);
        return this;
    }

    @Override
    public VirtualMachineScaleSetExtensionImpl withPublicSettings(HashMap<String, Object> settings) {
        this.ensurePublicSettings().clear();
        this.ensurePublicSettings().putAll(settings);
        return this;
    }

    @Override
    public VirtualMachineScaleSetExtensionImpl withProtectedSettings(HashMap<String, Object> settings) {
        this.ensureProtectedSettings().clear();
        this.ensureProtectedSettings().putAll(settings);
        return this;
    }

    @Override
    public VirtualMachineScaleSetExtensionImpl withType(String extensionImageTypeName) {
        this.innerModel().withTypePropertiesType(extensionImageTypeName);
        return this;
    }

    @Override
    public VirtualMachineScaleSetExtensionImpl withVersion(String extensionImageVersionName) {
        this.innerModel().withTypeHandlerVersion(extensionImageVersionName);
        return this;
    }

    @Override
    public VirtualMachineScaleSetImpl attach() {
        return this.parent().withExtension(this);
    }

    //
    // Note: Internal handling of VMSS extensions are different from VM extension.
    //       VM extensions are external child resources so only new, added or updated extensions will be committed.
    //
    //       VMSS extensions are inline child resources hence all extensions are always part of VMSS PUT payload
    //       i.e including the one that user didn't choose to update. ensurePublicSettings and ensureProtectedSettings
    //       are used to ensure we initialize settings/protectedSettings of an extension only if user choose to update
    // it.
    //
    @SuppressWarnings("unchecked")
    private HashMap<String, Object> ensurePublicSettings() {
        if (this.innerModel().settings() == null) {
            this.innerModel().withSettings(new LinkedHashMap<String, Object>());
        }
        return (LinkedHashMap<String, Object>) this.innerModel().settings();
    }

    @SuppressWarnings("unchecked")
    private HashMap<String, Object> ensureProtectedSettings() {
        if (this.innerModel().protectedSettings() == null) {
            this.innerModel().withProtectedSettings(new LinkedHashMap<String, Object>());
        }
        return (LinkedHashMap<String, Object>) this.innerModel().protectedSettings();
    }
}
