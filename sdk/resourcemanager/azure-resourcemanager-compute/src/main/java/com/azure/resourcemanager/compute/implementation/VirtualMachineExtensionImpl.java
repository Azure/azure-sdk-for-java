// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.compute.implementation;

import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineExtension;
import com.azure.resourcemanager.compute.models.VirtualMachineExtensionImage;
import com.azure.resourcemanager.compute.models.VirtualMachineExtensionInstanceView;
import com.azure.resourcemanager.compute.fluent.inner.VirtualMachineExtensionInner;
import com.azure.resourcemanager.compute.fluent.VirtualMachineExtensionsClient;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import reactor.core.publisher.Mono;

/** Implementation of VirtualMachineExtension. */
class VirtualMachineExtensionImpl
    extends ExternalChildResourceImpl<
        VirtualMachineExtension, VirtualMachineExtensionInner, VirtualMachineImpl, VirtualMachine>
    implements VirtualMachineExtension,
        VirtualMachineExtension.Definition<VirtualMachine.DefinitionStages.WithCreate>,
        VirtualMachineExtension.UpdateDefinition<VirtualMachine.Update>,
        VirtualMachineExtension.Update {
    private final VirtualMachineExtensionsClient client;
    private HashMap<String, Object> publicSettings;
    private HashMap<String, Object> protectedSettings;

    VirtualMachineExtensionImpl(
        String name,
        VirtualMachineImpl parent,
        VirtualMachineExtensionInner inner,
        VirtualMachineExtensionsClient client) {
        super(name, parent, inner);
        this.client = client;
        initializeSettings();
    }

    protected static VirtualMachineExtensionImpl newVirtualMachineExtension(
        String name, VirtualMachineImpl parent, VirtualMachineExtensionsClient client) {
        VirtualMachineExtensionInner inner = new VirtualMachineExtensionInner();
        inner.withLocation(parent.regionName());
        VirtualMachineExtensionImpl extension = new VirtualMachineExtensionImpl(name, parent, inner, client);
        return extension;
    }

    @Override
    public String id() {
        return this.inner().id();
    }

    @Override
    public String publisherName() {
        return this.inner().publisher();
    }

    @Override
    public String typeName() {
        return this.inner().virtualMachineExtensionType();
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
    public VirtualMachineExtensionInstanceView getInstanceView() {
        return getInstanceViewAsync().block();
    }

    @Override
    public Mono<VirtualMachineExtensionInstanceView> getInstanceViewAsync() {
        return this
            .client
            .getAsync(this.parent().resourceGroupName(), this.parent().name(), this.name(), "instanceView")
            .map(inner -> inner.instanceView());
    }

    @Override
    public Map<String, String> tags() {
        Map<String, String> tags = this.inner().tags();
        if (tags == null) {
            tags = new TreeMap<>();
        }
        return Collections.unmodifiableMap(tags);
    }

    @Override
    public String provisioningState() {
        return this.inner().provisioningState();
    }

    @Override
    public VirtualMachineExtensionImpl withMinorVersionAutoUpgrade() {
        this.inner().withAutoUpgradeMinorVersion(true);
        return this;
    }

    @Override
    public VirtualMachineExtensionImpl withoutMinorVersionAutoUpgrade() {
        this.inner().withAutoUpgradeMinorVersion(false);
        return this;
    }

    @Override
    public VirtualMachineExtensionImpl withImage(VirtualMachineExtensionImage image) {
        this
            .inner()
            .withPublisher(image.publisherName())
            .withVirtualMachineExtensionType(image.typeName())
            .withTypeHandlerVersion(image.versionName());
        return this;
    }

    @Override
    public VirtualMachineExtensionImpl withPublisher(String extensionImagePublisherName) {
        this.inner().withPublisher(extensionImagePublisherName);
        return this;
    }

    @Override
    public VirtualMachineExtensionImpl withPublicSetting(String key, Object value) {
        this.publicSettings.put(key, value);
        return this;
    }

    @Override
    public VirtualMachineExtensionImpl withProtectedSetting(String key, Object value) {
        this.protectedSettings.put(key, value);
        return this;
    }

    @Override
    public VirtualMachineExtensionImpl withPublicSettings(HashMap<String, Object> settings) {
        this.publicSettings.clear();
        this.publicSettings.putAll(settings);
        return this;
    }

    @Override
    public VirtualMachineExtensionImpl withProtectedSettings(HashMap<String, Object> settings) {
        this.protectedSettings.clear();
        this.protectedSettings.putAll(settings);
        return this;
    }

    @Override
    public VirtualMachineExtensionImpl withType(String extensionImageTypeName) {
        this.inner().withVirtualMachineExtensionType(extensionImageTypeName);
        return this;
    }

    @Override
    public VirtualMachineExtensionImpl withVersion(String extensionImageVersionName) {
        this.inner().withTypeHandlerVersion(extensionImageVersionName);
        return this;
    }

    @Override
    public final VirtualMachineExtensionImpl withTags(Map<String, String> tags) {
        this.inner().withTags(new HashMap<>(tags));
        return this;
    }

    @Override
    public final VirtualMachineExtensionImpl withTag(String key, String value) {
        if (this.inner().tags() == null) {
            this.inner().withTags(new HashMap<>());
        }
        this.inner().tags().put(key, value);
        return this;
    }

    @Override
    public final VirtualMachineExtensionImpl withoutTag(String key) {
        if (this.inner().tags() != null) {
            this.inner().tags().remove(key);
        }
        return this;
    }

    @Override
    public VirtualMachineImpl attach() {
        this.nullifySettingsIfEmpty();
        return this.parent().withExtension(this);
    }

    @Override
    protected Mono<VirtualMachineExtensionInner> getInnerAsync() {
        String name;
        if (this.isReference()) {
            name = ResourceUtils.nameFromResourceId(this.inner().id());
        } else {
            name = this.inner().name();
        }
        return this.client.getAsync(this.parent().resourceGroupName(), this.parent().name(), name);
    }

    // Implementation of ExternalChildResourceImpl createAsyncStreaming,  updateAsync and deleteAsync
    @Override
    public Mono<VirtualMachineExtension> createResourceAsync() {
        final VirtualMachineExtensionImpl self = this;
        return this
            .client
            .createOrUpdateAsync(this.parent().resourceGroupName(), this.parent().name(), this.name(), this.inner())
            .map(
                inner -> {
                    self.setInner(inner);
                    self.initializeSettings();
                    return self;
                });
    }

    @Override
    @SuppressWarnings("unchecked")
    public Mono<VirtualMachineExtension> updateResourceAsync() {
        this.nullifySettingsIfEmpty();
        if (this.isReference()) {
            String extensionName = ResourceUtils.nameFromResourceId(this.inner().id());
            return this
                .client
                .getAsync(this.parent().resourceGroupName(), this.parent().name(), extensionName)
                .flatMap(
                    resource -> {
                        inner()
                            .withPublisher(resource.publisher())
                            .withVirtualMachineExtensionType(resource.virtualMachineExtensionType())
                            .withTypeHandlerVersion(resource.typeHandlerVersion());
                        if (inner().autoUpgradeMinorVersion() == null) {
                            inner().withAutoUpgradeMinorVersion(resource.autoUpgradeMinorVersion());
                        }
                        LinkedHashMap<String, Object> publicSettings =
                            (LinkedHashMap<String, Object>) resource.settings();
                        if (publicSettings != null && publicSettings.size() > 0) {
                            LinkedHashMap<String, Object> innerPublicSettings =
                                (LinkedHashMap<String, Object>) inner().settings();
                            if (innerPublicSettings == null) {
                                inner().withSettings(new LinkedHashMap<String, Object>());
                                innerPublicSettings = (LinkedHashMap<String, Object>) inner().settings();
                            }
                            for (Map.Entry<String, Object> entry : publicSettings.entrySet()) {
                                if (!innerPublicSettings.containsKey(entry.getKey())) {
                                    innerPublicSettings.put(entry.getKey(), entry.getValue());
                                }
                            }
                        }
                        return createResourceAsync();
                    });
        } else {
            return this.createResourceAsync();
        }
    }

    @Override
    public Mono<Void> deleteResourceAsync() {
        return this.client.deleteAsync(this.parent().resourceGroupName(), this.parent().name(), this.name());
    }

    /**
     * @return true if this is just a reference to the extension.
     *     <p>An extension will present as a reference when the parent virtual machine was fetched using VM list, a GET
     *     on a specific VM will return fully expanded extension details.
     */
    public boolean isReference() {
        return this.inner().name() == null;
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

    @SuppressWarnings("unchecked")
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
