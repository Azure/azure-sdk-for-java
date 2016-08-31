package com.microsoft.azure.management.compute.implementation;
import com.microsoft.azure.management.compute.VirtualMachineExtension;
import rx.Observable;
import rx.functions.Func1;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a extension collection associated with a virtual machine.
 */
class VirtualMachineExtensionsImpl extends
        ExternalChildResourcesImpl<VirtualMachineExtensionImpl,
                VirtualMachineExtension,
                VirtualMachineExtensionInner,
                VirtualMachineImpl> {
    private final VirtualMachineExtensionsInner client;

    /**
     * Creates new VirtualMachineExtensionsImpl.
     *
     * @param client the client to perform REST calls on extensions
     * @param parent the parent virtual machine of the extensions
     */
    VirtualMachineExtensionsImpl(VirtualMachineExtensionsInner client, VirtualMachineImpl parent) {
        super(parent, "VirtualMachine", "VirtualMachineExtension");
        this.client = client;
    }

    /**
     * @return the extension as a map indexed by name.
     */
    public Map<String, VirtualMachineExtension> asMap() {
        Map<String, VirtualMachineExtension> result = new HashMap<>();
        for (Map.Entry<String, VirtualMachineExtensionImpl> entry : this.collection().entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Starts an extension definition chain.
     *
     * @param name the reference name of the extension to be added
     * @return the extension
     */
    public VirtualMachineExtensionImpl define(String name) {
        return this.prepareDefine(name);
    }

    /**
     * Starts an extension update chain.
     *
     * @param name the reference name of the extension to be updated
     * @return the extension
     */
    public VirtualMachineExtensionImpl update(String name) {
        return this.prepareUpdate(name);
    }

    /**
     * Mark the extension with given name as to be removed.
     *
     * @param name the reference name of the extension to be removed
     */
    public void remove(String name) {
        this.prepareRemove(name);
    }

    /**
     * Adds the extension to the collection.
     *
     * @param extension the extension
     */
    public void addExtension(VirtualMachineExtensionImpl extension) {
        this.addChildResource(extension);
    }

    @Override
    protected Observable<VirtualMachineExtensionImpl> listChildResourcesAsync(boolean requireRefresh) {
        if (requireRefresh) {
            try {
                parent().refresh(); // This is sync
            } catch (Exception exception) {
                return Observable.error(exception);
            }
        }

        if (parent().inner().resources() == null) {
            return Observable.empty();
        }

        final VirtualMachineExtensionsImpl self = this;
        return Observable.from(this.parent().inner().resources())
                .map(new Func1<VirtualMachineExtensionInner, VirtualMachineExtensionImpl>() {
                    @Override
                    public VirtualMachineExtensionImpl call(VirtualMachineExtensionInner inner) {
                        return new VirtualMachineExtensionImpl(inner.name(),
                                self.parent(),
                                inner,
                                self.client);
                    }
                });
    }

    @Override
    protected VirtualMachineExtensionImpl newChildResource(String name) {
        VirtualMachineExtensionImpl extension = VirtualMachineExtensionImpl
                .newVirtualMachineExtension(name, this.parent(), this.client);
        return extension;
    }
}
