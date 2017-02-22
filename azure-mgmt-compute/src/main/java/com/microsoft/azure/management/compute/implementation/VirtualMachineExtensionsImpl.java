/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute.implementation;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineExtension;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ExternalChildResourcesCachedImpl;
import rx.Observable;
import rx.functions.Action2;
import rx.functions.Func0;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a extension collection associated with a virtual machine.
 */
@LangDefinition
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
        super(parent, "VirtualMachineExtension");
        this.client = client;
        this.cacheCollection();
    }

    /**
     * @return the extension as a map indexed by name.
     */
    public Map<String, VirtualMachineExtension> asMap() {
        return Collections.unmodifiableMap(this.asMapAsync().toBlocking().last());
    }

    /**
     * @return an observable emits extensions in this collection as a map indexed by name.
     */
    public Observable<Map<String, VirtualMachineExtension>> asMapAsync() {
        return listAsync()
                .collect(new Func0<Map<String, VirtualMachineExtension>>() {
                    @Override
                    public Map<String, VirtualMachineExtension> call() {
                        return new HashMap<>();
                    }
                }, new Action2<Map<String, VirtualMachineExtension>, VirtualMachineExtension>() {
                    @Override
                    public void call(Map<String, VirtualMachineExtension> map, VirtualMachineExtension extension) {
                        map.put(extension.name(), extension);
                    }
                });
    }

    /**
     * @return an observable emits extensions in this collection
     */
    public Observable<VirtualMachineExtension> listAsync() {
        Observable<VirtualMachineExtensionImpl> extensions = Observable.from(this.collection().values());
        // Resolve reference getExtensions
        //
        Observable<VirtualMachineExtension> resolvedExtensionsStream = extensions
                .filter(new Func1<VirtualMachineExtensionImpl, Boolean>() {
                    @Override
                    public Boolean call(VirtualMachineExtensionImpl extension) {
                        return extension.isReference();
                    }
                })
                .flatMap(new Func1<VirtualMachineExtensionImpl, Observable<VirtualMachineExtension>>() {
                    @Override
                    public Observable<VirtualMachineExtension> call(final VirtualMachineExtensionImpl extension) {
                        return client.getAsync(parent().resourceGroupName(), parent().name(), extension.name())
                                .map(new Func1<VirtualMachineExtensionInner, VirtualMachineExtension>() {
                                    @Override
                                    public VirtualMachineExtension call(VirtualMachineExtensionInner extensionInner) {
                                        return new VirtualMachineExtensionImpl(extension.name(), parent(), extensionInner, client);
                                    }
                                });
                    }
                });
        return resolvedExtensionsStream.concatWith(extensions.filter(new Func1<VirtualMachineExtensionImpl, Boolean>() {
            @Override
            public Boolean call(VirtualMachineExtensionImpl extension) {
                return !extension.isReference();
            }
        }));
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
    protected List<VirtualMachineExtensionImpl> listChildResources() {
        List<VirtualMachineExtensionImpl> childResources = new ArrayList<>();
        if (parent().inner().resources() != null) {
            for (VirtualMachineExtensionInner inner : parent().inner().resources()) {
                if (inner.name() == null) {
                    inner.withLocation(parent().regionName());
                    childResources.add(new VirtualMachineExtensionImpl(ResourceUtils.nameFromResourceId(inner.id()),
                            this.parent(),
                            inner,
                            this.client));
                } else {
                    childResources.add(new VirtualMachineExtensionImpl(inner.name(),
                            this.parent(),
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
                .newVirtualMachineExtension(name, this.parent(), this.client);
        return extension;
    }
}
