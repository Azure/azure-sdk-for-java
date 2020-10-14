// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.compute.implementation;

import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineExtension;
import com.azure.resourcemanager.compute.fluent.models.VirtualMachineExtensionInner;
import com.azure.resourcemanager.compute.fluent.VirtualMachineExtensionsClient;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.ExternalChildResourcesCachedImpl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Represents a extension collection associated with a virtual machine. */
class VirtualMachineExtensionsImpl
    extends ExternalChildResourcesCachedImpl<
        VirtualMachineExtensionImpl,
        VirtualMachineExtension,
        VirtualMachineExtensionInner,
        VirtualMachineImpl,
        VirtualMachine> {
    private final VirtualMachineExtensionsClient client;

    /**
     * Creates new VirtualMachineExtensionsImpl.
     *
     * @param client the client to perform REST calls on extensions
     * @param parent the parent virtual machine of the extensions
     */
    VirtualMachineExtensionsImpl(VirtualMachineExtensionsClient client, VirtualMachineImpl parent) {
        super(parent, parent.taskGroup(), "VirtualMachineExtension");
        this.client = client;
        this.cacheCollection();
    }

    /** @return the extension as a map indexed by name. */
    public Map<String, VirtualMachineExtension> asMap() {
        return this.asMapAsync().block();
    }

    /** @return an observable emits extensions in this collection as a map indexed by name. */
    public Mono<Map<String, VirtualMachineExtension>> asMapAsync() {
        return listAsync()
            .flatMapMany(Flux::fromIterable)
            .collect(Collectors.toMap(extension -> extension.name(), extension -> extension))
            .map(map -> Collections.unmodifiableMap(map));
    }

    /** @return a Mono emits extensions in this collection */
    public Mono<List<VirtualMachineExtension>> listAsync() {
        Flux<VirtualMachineExtensionImpl> extensions = Flux.fromIterable(this.collection().values());
        // Resolve reference getExtensions
        Flux<VirtualMachineExtension> resolvedExtensionsStream =
            extensions
                .filter(extension -> extension.isReference())
                .flatMap(
                    extension ->
                        client
                            .getAsync(getParent().resourceGroupName(), getParent().name(), extension.name())
                            .map(
                                extensionInner ->
                                    new VirtualMachineExtensionImpl(
                                        extension.name(), getParent(), extensionInner, client)));
        return resolvedExtensionsStream
            .concatWith(extensions.filter(extension -> !extension.isReference()))
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
        if (getParent().innerModel().resources() != null) {
            for (VirtualMachineExtensionInner inner : getParent().innerModel().resources()) {
                if (inner.name() == null) {
                    // This extension exists in the parent VM extension collection as a reference id.
                    inner.withLocation(getParent().regionName());
                    childResources
                        .add(
                            new VirtualMachineExtensionImpl(
                                ResourceUtils.nameFromResourceId(inner.id()), this.getParent(), inner, this.client));
                } else {
                    // This extension exists in the parent VM as a fully blown object
                    childResources
                        .add(new VirtualMachineExtensionImpl(inner.name(), this.getParent(), inner, this.client));
                }
            }
        }
        return Collections.unmodifiableList(childResources);
    }

    @Override
    protected Flux<VirtualMachineExtensionImpl> listChildResourcesAsync() {
        return Flux.fromIterable(listChildResources());
    }

    @Override
    protected VirtualMachineExtensionImpl newChildResource(String name) {
        VirtualMachineExtensionImpl extension =
            VirtualMachineExtensionImpl.newVirtualMachineExtension(name, this.getParent(), this.client);
        return extension;
    }
}
