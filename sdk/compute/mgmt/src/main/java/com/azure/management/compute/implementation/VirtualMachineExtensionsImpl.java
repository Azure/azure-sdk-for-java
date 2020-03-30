/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.compute.implementation;
import com.azure.management.compute.VirtualMachine;
import com.azure.management.compute.VirtualMachineExtension;
import com.azure.management.compute.models.VirtualMachineExtensionInner;
import com.azure.management.compute.models.VirtualMachineExtensionsInner;
import com.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.azure.management.resources.fluentcore.arm.collection.implementation.ExternalChildResourcesCachedImpl;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents a extension collection associated with a virtual machine.
 */
class VirtualMachineExtensionsImpl extends
        ExternalChildResourcesCachedImpl<VirtualMachineExtensionImpl,
                                VirtualMachineExtension,
                                VirtualMachineExtensionInner,
                                VirtualMachineImpl,
                                VirtualMachine> {
    private final VirtualMachineExtensionsInner client;

    /**
     * Creates new VirtualMachineExtensionsImpl.
     *
     * @param client the client to perform REST calls on extensions
     * @param parent the parent virtual machine of the extensions
     */
    VirtualMachineExtensionsImpl(VirtualMachineExtensionsInner client, VirtualMachineImpl parent) {
        super(parent,  parent.taskGroup(), "VirtualMachineExtension");
        this.client = client;
        this.cacheCollection();
    }

    /**
     * @return the extension as a map indexed by name.
     */
    public Map<String, VirtualMachineExtension> asMap() {
        return this.asMapAsync().block();
    }

    /**
     * @return an observable emits extensions in this collection as a map indexed by name.
     */
    public Mono<Map<String, VirtualMachineExtension>> asMapAsync() {
        return listAsync()
                .flatMapMany(Flux::fromIterable)
                .collect(Collectors.toMap(extension -> extension.name(), extension -> extension))
                .map(map -> Collections.unmodifiableMap(map));
    }

    /**
     * @return a Mono emits extensions in this collection
     */
    public Mono<List<VirtualMachineExtension>> listAsync() {
        Flux<VirtualMachineExtensionImpl> extensions = Flux.fromIterable(this.collection().values());
        // Resolve reference getExtensions
        Flux<VirtualMachineExtension> resolvedExtensionsStream = extensions
                .filter(extension -> extension.isReference())
                .flatMap(extension -> client.getAsync(getParent().resourceGroupName(), getParent().name(), extension.name())
                        .map(extensionInner -> new VirtualMachineExtensionImpl(extension.name(), getParent(), extensionInner, client)));
        return resolvedExtensionsStream.concatWith(extensions.filter(extension -> !extension.isReference()))
                .collectList()
                .map(list -> Collections.unmodifiableList(list));
    }

    /**
     * Starts an extension definition chain.
     *
     * @param name the reference name of the extension to be added
     * @return the extension
     */
    public VirtualMachineExtensionImpl define(String name) {
        VirtualMachineExtensionImpl newExtension = this.prepareInlineDefine(name);
        return newExtension;
    }

    /**
     * Starts an extension update chain.
     *
     * @param name the reference name of the extension to be updated
     * @return the extension
     */
    public VirtualMachineExtensionImpl update(String name) {
        return this.prepareInlineUpdate(name);
    }

    /**
     * Mark the extension with given name as to be removed.
     *
     * @param name the reference name of the extension to be removed
     */
    public void remove(String name) {
        this.prepareInlineRemove(name);
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
    protected List<VirtualMachineExtensionImpl> listChildResources() {
        List<VirtualMachineExtensionImpl> childResources = new ArrayList<>();
        if (getParent().inner().resources() != null) {
            for (VirtualMachineExtensionInner inner : getParent().inner().resources()) {
                if (inner.getName() == null) {
                    // This extension exists in the parent VM extension collection as a reference id.
                    inner.setLocation(getParent().regionName());
                    childResources.add(new VirtualMachineExtensionImpl(ResourceUtils.nameFromResourceId(inner.getId()),
                            this.getParent(),
                            inner,
                            this.client));
                } else {
                    // This extension exists in the parent VM as a fully blown object
                    childResources.add(new VirtualMachineExtensionImpl(inner.getName(),
                            this.getParent(),
                            inner,
                            this.client));
                }
            }
        }
        return childResources;
    }

    @Override
    protected VirtualMachineExtensionImpl newChildResource(String name) {
        VirtualMachineExtensionImpl extension = VirtualMachineExtensionImpl
                .newVirtualMachineExtension(name, this.getParent(), this.client);
        return extension;
    }
}
