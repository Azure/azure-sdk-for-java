package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineExtension;
import com.microsoft.azure.management.compute.VirtualMachineExtensionImage;
import com.microsoft.azure.management.compute.VirtualMachineExtensionInstanceView;
import rx.Observable;
import rx.functions.Func1;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Implementation of {@link VirtualMachineExtension}.
 */
class VirtualMachineExtensionImpl
        extends ExternalChildResourceImpl<VirtualMachineExtension,
        VirtualMachineExtensionInner,
        VirtualMachineImpl>
        implements VirtualMachineExtension,
        VirtualMachineExtension.Definition<VirtualMachine.DefinitionStages.WithCreate>,
        VirtualMachineExtension.UpdateDefinition<VirtualMachine.Update>,
        VirtualMachineExtension.Update {
    private final VirtualMachineExtensionsInner client;
    private HashMap<String, Object> publicSettings;
    private HashMap<String, Object> protectedSettings;

    VirtualMachineExtensionImpl(String name,
                                VirtualMachineImpl parent,
                                VirtualMachineExtensionInner inner,
                                VirtualMachineExtensionsInner client) {
        super(name, parent, inner);
        this.client = client;
        initializeSettings();
    }

    protected static VirtualMachineExtensionImpl newVirtualMachineExtension(String name,
                                                                            VirtualMachineImpl parent,
                                                                            VirtualMachineExtensionsInner client) {
        VirtualMachineExtensionInner inner = new VirtualMachineExtensionInner();
        inner.withLocation(parent.regionName());
        VirtualMachineExtensionImpl extension = new VirtualMachineExtensionImpl(name,
                parent,
                inner,
                client);
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
    public VirtualMachineExtensionInstanceView instanceView() {
        return this.inner().instanceView();
    }

    @Override
    public Map<String, String> tags() {
        Map<String, String> tags = this.inner().getTags();
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
    public VirtualMachineExtensionImpl withAutoUpgradeMinorVersionEnabled() {
        this.inner().withAutoUpgradeMinorVersion(true);
        return this;
    }

    @Override
    public VirtualMachineExtensionImpl withAutoUpgradeMinorVersionDisabled() {
        this.inner().withAutoUpgradeMinorVersion(false);
        return this;
    }

    @Override
    public VirtualMachineExtensionImpl withImage(VirtualMachineExtensionImage image) {
        this.inner().withPublisher(image.publisherName())
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
        this.inner().getTags().put(key, value);
        return this;
    }

    @Override
    public final VirtualMachineExtensionImpl withoutTag(String key) {
        this.inner().getTags().remove(key);
        return this;
    }

    @Override
    public VirtualMachineImpl parent() {
        this.nullifySettingsIfEmpty();
        return this.parent;
    }

    @Override
    public VirtualMachineImpl attach() {
        this.nullifySettingsIfEmpty();
        return this.parent.withExtension(this);
    }

    @Override
    public VirtualMachineExtensionImpl refresh() throws Exception {
        VirtualMachineExtensionInner inner =
                this.client.get(this.parent.resourceGroupName(), this.parent.name(), this.name());
        this.setInner(inner);
        return this;
    }

    // Implementation of ExternalChildResourceImpl createAsync,  updateAsync and deleteAsync
    //
    @Override
    public Observable<VirtualMachineExtension> createAsync() {
        final VirtualMachineExtensionImpl self = this;
        return this.client.createOrUpdateAsync(this.parent.resourceGroupName(),
                this.parent.name(),
                this.name(),
                this.inner())
                .map(new Func1<VirtualMachineExtensionInner, VirtualMachineExtension>() {
                    @Override
                    public VirtualMachineExtension call(VirtualMachineExtensionInner inner) {
                        self.setInner(inner);
                        self.initializeSettings();
                        return self;
                    }
                });
    }

    @Override
    public Observable<VirtualMachineExtension> updateAsync() {
        return this.createAsync();
    }

    @Override
    public Observable<Void> deleteAsync() {
        return this.client.deleteAsync(this.parent.resourceGroupName(),
                this.parent.name(),
                this.name()).map(new Func1<Void, Void>() {
            @Override
            public Void call(Void result) {
                return result;
            }
        });
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
}