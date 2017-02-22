/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute.implementation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    protected VirtualMachineScaleSetExtensionImpl(VirtualMachineScaleSetExtensionInner inner,
                                                  VirtualMachineScaleSetImpl parent) {
        super(inner, parent);
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
        if (this.inner().settings() == null) {
            return Collections.unmodifiableMap(new LinkedHashMap<String, Object>());
        }
        return Collections.unmodifiableMap((LinkedHashMap<String, Object>) this.inner().settings());
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
        this.inner().withType(extensionImageTypeName);
        return this;
    }

    @Override
    public VirtualMachineScaleSetExtensionImpl withVersion(String extensionImageVersionName) {
        this.inner().withTypeHandlerVersion(extensionImageVersionName);
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
    //       are used to ensure we initialize settings/protectedSettings of an extension only if user choose to update it.
    //
    private HashMap<String, Object> ensurePublicSettings() {
        if (this.inner().settings() == null) {
            this.inner().withSettings(new LinkedHashMap<String, Object>());
        }
        return (LinkedHashMap<String, Object>) this.inner().settings();
    }

    private HashMap<String, Object> ensureProtectedSettings() {
        if (this.inner().protectedSettings() == null) {
            this.inner().withProtectedSettings(new LinkedHashMap<String, Object>());
        }
        return (LinkedHashMap<String, Object>) this.inner().protectedSettings();
    }
}
