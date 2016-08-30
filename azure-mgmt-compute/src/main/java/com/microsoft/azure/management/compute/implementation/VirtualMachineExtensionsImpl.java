package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.compute.VirtualMachineExtension;
import com.microsoft.rest.ServiceResponse;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Represents a extension collection associated with a virtual machine.
 */
class VirtualMachineExtensionsImpl {
    private final VirtualMachineExtensionsInner client;
    private final VirtualMachineImpl parent;
    private ConcurrentMap<String, VirtualMachineExtensionImpl> extensions;
    private boolean requireRefresh = false;

    VirtualMachineExtensionsImpl(VirtualMachineExtensionsInner client, VirtualMachineImpl parent) {
        this.extensions = new ConcurrentHashMap<>();
        this.client = client;
        this.parent = parent;
        this.initializeExtensionsFromParent();
    }

    Map<String, VirtualMachineExtension> asMap() {
        if (requireRefresh) {
            try {
                parent.refresh();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            initializeExtensionsFromParent();
        }
        Map<String, VirtualMachineExtension> result = new HashMap<>();
        for (Map.Entry<String, VirtualMachineExtensionImpl> extensionEntry : this.extensions.entrySet()) {
            result.put(extensionEntry.getKey(), extensionEntry.getValue());
        }
        return Collections.unmodifiableMap(result);
    }

    VirtualMachineExtensionImpl define(String name) {
        if (findExtension(name) != null) {
            throw new IllegalArgumentException("An extension with name  '" + name + "' already exists");
        }
        VirtualMachineExtensionImpl extension = VirtualMachineExtensionImpl
                .newVirtualMachineExtension(name, this.parent, this.client);
        extension.setState(VirtualMachineExtensionImpl.State.ToBeCreated);
        return extension;
    }

    VirtualMachineExtensionImpl update(String name) {
        VirtualMachineExtensionImpl extension = findExtension(name);
        if (extension == null
                || extension.state() == VirtualMachineExtensionImpl.State.ToBeCreated) {
            throw new IllegalArgumentException("An extension with name  '" + name + "' not found");
        }
        if (extension.state() == VirtualMachineExtensionImpl.State.ToBeRemoved) {
            throw new IllegalArgumentException("An extension with name  '" + name + "' is marked for deletion");
        }
        extension.setState(VirtualMachineExtensionImpl.State.ToBeUpdated);
        return extension;
    }

    void remove(String name) {
        VirtualMachineExtensionImpl extension = findExtension(name);
        if (extension == null
                || extension.state() == VirtualMachineExtensionImpl.State.ToBeCreated) {
            throw new IllegalArgumentException("An extension with name  '" + name + "' not found");
        }
        extension.setState(VirtualMachineExtensionImpl.State.ToBeRemoved);
    }

    void addExtension(VirtualMachineExtensionImpl extension) {
        this.extensions.put(extension.name(), extension);
    }

    Observable<VirtualMachineExtensionImpl> commitAsync() {
        final VirtualMachineExtensionsImpl self = this;
        List<VirtualMachineExtensionImpl> items = new ArrayList<>();
        for (VirtualMachineExtensionImpl extension : this.extensions.values()) {
            items.add(extension);
        }

        Observable<VirtualMachineExtensionImpl> deleteStream = Observable.from(items)
                .filter(new Func1<VirtualMachineExtensionImpl, Boolean>() {
                    @Override
                    public Boolean call(VirtualMachineExtensionImpl extension) {
                        return extension.state() == ExternalChildResourceImpl.State.ToBeRemoved;
                    }
                }).flatMap(new Func1<VirtualMachineExtensionImpl, Observable<VirtualMachineExtensionImpl>>() {
                    @Override
                    public Observable<VirtualMachineExtensionImpl> call(final VirtualMachineExtensionImpl extension) {

                        return self.client.deleteAsync(self.parent.resourceGroupName(),
                                self.parent.name(),
                                extension.name())
                        .map(new Func1<ServiceResponse<Void>, VirtualMachineExtensionImpl>() {
                            @Override
                            public VirtualMachineExtensionImpl call(ServiceResponse<Void> response) {
                                self.extensions.remove(extension.name());
                                return extension;
                            }
                        });
                    }
                }).doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        self.extensions.clear();
                    }
                });

        Observable<VirtualMachineExtensionImpl> setStream = Observable.from(items)
                .filter(new Func1<VirtualMachineExtensionImpl, Boolean>() {
                    @Override
                    public Boolean call(VirtualMachineExtensionImpl extension) {
                        return extension.state() == ExternalChildResourceImpl.State.ToBeUpdated
                                || extension.state() == ExternalChildResourceImpl.State.ToBeCreated;
                    }
                }).flatMap(new Func1<VirtualMachineExtensionImpl, Observable<VirtualMachineExtensionImpl>>() {
            @Override
            public Observable<VirtualMachineExtensionImpl> call(final VirtualMachineExtensionImpl extension) {
                return extension.createResourceAsync()
                        .map(new Func1<VirtualMachineExtension, VirtualMachineExtensionImpl>() {
                            @Override
                            public VirtualMachineExtensionImpl call(VirtualMachineExtension e) {
                                extension.setState(ExternalChildResourceImpl.State.None);
                                return extension;
                            }
                        });
            }
        }).doOnError(new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                self.extensions.clear();
            }
        });

        return deleteStream.mergeWith(setStream)
                .filter(new Func1<VirtualMachineExtensionImpl, Boolean>() {
                    @Override
                    public Boolean call(VirtualMachineExtensionImpl extension) {
                        return extension.state() == ExternalChildResourceImpl.State.None;
                    }
                });
    }

    private void initializeExtensionsFromParent() {
        if (parent.inner().resources() != null) {
            for (VirtualMachineExtensionInner innerExtension : this.parent.inner().resources()) {
                this.extensions.put(innerExtension.name(),
                        new VirtualMachineExtensionImpl(innerExtension.name(),
                                this.parent,
                                innerExtension,
                                this.client));
            }
        }
    }

    private VirtualMachineExtensionImpl findExtension(String name) {
        for (Map.Entry<String, VirtualMachineExtensionImpl> extensionEntry : this.extensions.entrySet()) {
            if (extensionEntry.getKey().equalsIgnoreCase(name)) {
                return extensionEntry.getValue();
            }
        }
        return null;
    }
}
