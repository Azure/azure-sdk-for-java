// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.compute.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.compute.ComputeManager;
import com.azure.resourcemanager.compute.models.CachingTypes;
import com.azure.resourcemanager.compute.models.DataDisk;
import com.azure.resourcemanager.compute.models.DiagnosticsProfile;
import com.azure.resourcemanager.compute.models.Disk;
import com.azure.resourcemanager.compute.models.DiskCreateOptionTypes;
import com.azure.resourcemanager.compute.models.DiskState;
import com.azure.resourcemanager.compute.models.ImageReference;
import com.azure.resourcemanager.compute.models.ManagedDiskParameters;
import com.azure.resourcemanager.compute.models.NetworkInterfaceReference;
import com.azure.resourcemanager.compute.models.OSProfile;
import com.azure.resourcemanager.compute.models.OperatingSystemTypes;
import com.azure.resourcemanager.compute.models.PowerState;
import com.azure.resourcemanager.compute.models.Sku;
import com.azure.resourcemanager.compute.models.StorageAccountTypes;
import com.azure.resourcemanager.compute.models.StorageProfile;
import com.azure.resourcemanager.compute.models.VirtualMachineCustomImage;
import com.azure.resourcemanager.compute.models.VirtualMachineDataDisk;
import com.azure.resourcemanager.compute.models.VirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachineInstanceView;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSet;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetVM;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetVMInstanceExtension;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetVMNetworkProfileConfiguration;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetVMProtectionPolicy;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.compute.models.VirtualMachineUnmanagedDataDisk;
import com.azure.resourcemanager.compute.fluent.inner.VirtualMachineExtensionInner;
import com.azure.resourcemanager.compute.fluent.inner.VirtualMachineInstanceViewInner;
import com.azure.resourcemanager.compute.fluent.inner.VirtualMachineScaleSetVMInner;
import com.azure.resourcemanager.compute.fluent.inner.VirtualMachineScaleSetVMInstanceViewInner;
import com.azure.resourcemanager.compute.fluent.VirtualMachineScaleSetVMsClient;
import com.azure.resourcemanager.network.models.VirtualMachineScaleSetNetworkInterface;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ChildResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Implementation of {@link VirtualMachineScaleSetVM}. */
class VirtualMachineScaleSetVMImpl
    extends ChildResourceImpl<VirtualMachineScaleSetVMInner, VirtualMachineScaleSetImpl, VirtualMachineScaleSet>
    implements VirtualMachineScaleSetVM, VirtualMachineScaleSetVM.Update {

    private VirtualMachineInstanceView virtualMachineInstanceView;
    private final VirtualMachineScaleSetVMsClient client;
    private final ComputeManager computeManager;
    private final ClientLogger logger = new ClientLogger(VirtualMachineScaleSetVMImpl.class);

    // To track the managed data disks
    private final ManagedDataDiskCollection managedDataDisks = new ManagedDataDiskCollection();

    VirtualMachineScaleSetVMImpl(
        VirtualMachineScaleSetVMInner inner,
        final VirtualMachineScaleSetImpl parent,
        final VirtualMachineScaleSetVMsClient client,
        final ComputeManager computeManager) {
        super(inner, parent);
        this.client = client;
        this.computeManager = computeManager;
        VirtualMachineScaleSetVMInstanceViewInner instanceViewInner = this.inner().instanceView();
        if (instanceViewInner != null) {
            this.virtualMachineInstanceView =
                new VirtualMachineInstanceViewImpl(
                    new VirtualMachineInstanceViewInner()
                        .withBootDiagnostics(instanceViewInner.bootDiagnostics())
                        .withDisks(instanceViewInner.disks())
                        .withExtensions(instanceViewInner.extensions())
                        .withPlatformFaultDomain(instanceViewInner.platformFaultDomain())
                        .withPlatformUpdateDomain(instanceViewInner.platformUpdateDomain())
                        .withRdpThumbPrint(instanceViewInner.rdpThumbPrint())
                        .withStatuses(instanceViewInner.statuses())
                        .withVmAgent(instanceViewInner.vmAgent()));
        } else {
            this.virtualMachineInstanceView = null;
        }
    }

    @Override
    public String id() {
        return this.inner().id();
    }

    @Override
    public String name() {
        return this.inner().name();
    }

    @Override
    public String regionName() {
        return this.inner().location();
    }

    @Override
    public Region region() {
        return Region.fromName(this.regionName());
    }

    @Override
    public String type() {
        return this.inner().type();
    }

    @Override
    public Map<String, String> tags() {
        if (this.inner().tags() == null) {
            return Collections.unmodifiableMap(new LinkedHashMap<>());
        }
        return Collections.unmodifiableMap(this.inner().tags());
    }

    @Override
    public String instanceId() {
        return this.inner().instanceId();
    }

    @Override
    public Sku sku() {
        return this.inner().sku();
    }

    @Override
    public VirtualMachineSizeTypes size() {
        if (this.inner().hardwareProfile() != null && this.inner().hardwareProfile().vmSize() != null) {
            return this.inner().hardwareProfile().vmSize();
        }
        if (this.sku() != null && this.sku().name() != null) {
            return VirtualMachineSizeTypes.fromString(this.sku().name());
        }
        return null;
    }

    @Override
    public boolean isLatestScaleSetUpdateApplied() {
        return this.inner().latestModelApplied();
    }

    @Override
    public boolean isOSBasedOnPlatformImage() {
        ImageReference imageReference = this.inner().storageProfile().imageReference();
        if (imageReference != null
            && imageReference.publisher() != null
            && imageReference.sku() != null
            && imageReference.offer() != null
            && imageReference.version() != null) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isOSBasedOnCustomImage() {
        ImageReference imageReference = this.inner().storageProfile().imageReference();
        if (imageReference != null && imageReference.id() != null) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isOSBasedOnStoredImage() {
        if (this.inner().storageProfile().osDisk() != null && this.inner().storageProfile().osDisk().image() != null) {
            return this.inner().storageProfile().osDisk().image().uri() != null;
        }
        return false;
    }

    @Override
    public ImageReference platformImageReference() {
        if (isOSBasedOnPlatformImage()) {
            return this.inner().storageProfile().imageReference();
        }
        return null;
    }

    @Override
    public VirtualMachineImage getOSPlatformImage() {
        if (this.isOSBasedOnPlatformImage()) {
            ImageReference imageReference = this.platformImageReference();
            return this
                .computeManager
                .virtualMachineImages()
                .getImage(
                    this.region(),
                    imageReference.publisher(),
                    imageReference.offer(),
                    imageReference.sku(),
                    imageReference.version());
        }
        return null;
    }

    @Override
    public VirtualMachineCustomImage getOSCustomImage() {
        if (this.isOSBasedOnCustomImage()) {
            ImageReference imageReference = this.inner().storageProfile().imageReference();
            return this.computeManager.virtualMachineCustomImages().getById(imageReference.id());
        }
        return null;
    }

    @Override
    public String storedImageUnmanagedVhdUri() {
        if (this.inner().storageProfile().osDisk().image() != null) {
            return this.inner().storageProfile().osDisk().image().uri();
        }
        return null;
    }

    @Override
    public String osDiskName() {
        return this.inner().storageProfile().osDisk().name();
    }

    @Override
    public String osUnmanagedDiskVhdUri() {
        if (this.inner().storageProfile().osDisk().vhd() != null) {
            return this.inner().storageProfile().osDisk().vhd().uri();
        }
        return null;
    }

    @Override
    public String osDiskId() {
        if (this.storageProfile().osDisk().managedDisk() != null) {
            return this.storageProfile().osDisk().managedDisk().id();
        }
        return null;
    }

    @Override
    public Map<Integer, VirtualMachineUnmanagedDataDisk> unmanagedDataDisks() {
        Map<Integer, VirtualMachineUnmanagedDataDisk> dataDisks = new HashMap<>();
        if (!isManagedDiskEnabled()) {
            List<DataDisk> innerDataDisks = this.inner().storageProfile().dataDisks();
            if (innerDataDisks != null) {
                for (DataDisk innerDataDisk : innerDataDisks) {
                    dataDisks.put(innerDataDisk.lun(), new UnmanagedDataDiskImpl(innerDataDisk, null));
                }
            }
        }
        return Collections.unmodifiableMap(dataDisks);
    }

    @Override
    public Map<Integer, VirtualMachineDataDisk> dataDisks() {
        Map<Integer, VirtualMachineDataDisk> dataDisks = new HashMap<>();
        if (isManagedDiskEnabled()) {
            List<DataDisk> innerDataDisks = this.inner().storageProfile().dataDisks();
            if (innerDataDisks != null) {
                for (DataDisk innerDataDisk : innerDataDisks) {
                    dataDisks.put(innerDataDisk.lun(), new VirtualMachineDataDiskImpl(innerDataDisk));
                }
            }
        }
        return Collections.unmodifiableMap(dataDisks);
    }

    @Override
    public CachingTypes osDiskCachingType() {
        return this.inner().storageProfile().osDisk().caching();
    }

    @Override
    public int osDiskSizeInGB() {
        return Utils.toPrimitiveInt(this.inner().storageProfile().osDisk().diskSizeGB());
    }

    @Override
    public String computerName() {
        return this.inner().osProfile().computerName();
    }

    @Override
    public String administratorUserName() {
        return this.inner().osProfile().adminUsername();
    }

    @Override
    public OperatingSystemTypes osType() {
        return this.inner().storageProfile().osDisk().osType();
    }

    @Override
    public boolean isLinuxPasswordAuthenticationEnabled() {
        if (this.inner().osProfile().linuxConfiguration() != null) {
            return !Utils
                .toPrimitiveBoolean(this.inner().osProfile().linuxConfiguration().disablePasswordAuthentication());
        }
        return false;
    }

    @Override
    public boolean isWindowsVMAgentProvisioned() {
        if (this.inner().osProfile().windowsConfiguration() != null) {
            return Utils.toPrimitiveBoolean(this.inner().osProfile().windowsConfiguration().provisionVMAgent());
        }
        return false;
    }

    @Override
    public boolean isWindowsAutoUpdateEnabled() {
        if (this.inner().osProfile().windowsConfiguration() != null) {
            return Utils.toPrimitiveBoolean(this.inner().osProfile().windowsConfiguration().enableAutomaticUpdates());
        }
        return false;
    }

    @Override
    public String windowsTimeZone() {
        if (this.inner().osProfile().windowsConfiguration() != null) {
            return this.inner().osProfile().windowsConfiguration().timeZone();
        }
        return null;
    }

    @Override
    public boolean bootDiagnosticEnabled() {
        if (this.inner().diagnosticsProfile() != null && this.inner().diagnosticsProfile().bootDiagnostics() != null) {
            return Utils.toPrimitiveBoolean(this.inner().diagnosticsProfile().bootDiagnostics().enabled());
        }
        return false;
    }

    @Override
    public String bootDiagnosticStorageAccountUri() {
        if (this.inner().diagnosticsProfile() != null && this.inner().diagnosticsProfile().bootDiagnostics() != null) {
            return this.inner().diagnosticsProfile().bootDiagnostics().storageUri();
        }
        return null;
    }

    @Override
    public String availabilitySetId() {
        if (this.inner().availabilitySet() != null) {
            return this.inner().availabilitySet().id();
        }
        return null;
    }

    @Override
    public List<String> networkInterfaceIds() {
        List<String> resourceIds = new ArrayList<>();
        for (NetworkInterfaceReference reference : this.inner().networkProfile().networkInterfaces()) {
            resourceIds.add(reference.id());
        }
        return Collections.unmodifiableList(resourceIds);
    }

    @Override
    public String primaryNetworkInterfaceId() {
        for (NetworkInterfaceReference reference : this.inner().networkProfile().networkInterfaces()) {
            if (reference.primary() != null && reference.primary()) {
                return reference.id();
            }
        }
        return null;
    }

    @Override
    public Map<String, VirtualMachineScaleSetVMInstanceExtension> extensions() {
        Map<String, VirtualMachineScaleSetVMInstanceExtension> extensions = new LinkedHashMap<>();
        if (this.inner().resources() != null) {
            for (VirtualMachineExtensionInner extensionInner : this.inner().resources()) {
                extensions
                    .put(
                        extensionInner.name(),
                        new VirtualMachineScaleSetVMInstanceExtensionImpl(extensionInner, this));
            }
        }
        return Collections.unmodifiableMap(extensions);
    }

    @Override
    public StorageProfile storageProfile() {
        return this.inner().storageProfile();
    }

    @Override
    public OSProfile osProfile() {
        return this.inner().osProfile();
    }

    @Override
    public DiagnosticsProfile diagnosticsProfile() {
        return this.inner().diagnosticsProfile();
    }

    @Override
    public VirtualMachineInstanceView instanceView() {
        if (this.virtualMachineInstanceView == null) {
            refreshInstanceView();
        }
        return this.virtualMachineInstanceView;
    }

    @Override
    public VirtualMachineInstanceView refreshInstanceView() {
        return refreshInstanceViewAsync().block();
    }

    public Mono<VirtualMachineInstanceView> refreshInstanceViewAsync() {
        return this
            .client
            .getInstanceViewAsync(this.parent().resourceGroupName(), this.parent().name(), this.instanceId())
            .map(
                instanceViewInner -> {
                    virtualMachineInstanceView =
                        new VirtualMachineInstanceViewImpl(
                            new VirtualMachineInstanceViewInner()
                                .withBootDiagnostics(instanceViewInner.bootDiagnostics())
                                .withDisks(instanceViewInner.disks())
                                .withExtensions(instanceViewInner.extensions())
                                .withPlatformFaultDomain(instanceViewInner.platformFaultDomain())
                                .withPlatformUpdateDomain(instanceViewInner.platformUpdateDomain())
                                .withRdpThumbPrint(instanceViewInner.rdpThumbPrint())
                                .withStatuses(instanceViewInner.statuses())
                                .withVmAgent(instanceViewInner.vmAgent()));
                    return virtualMachineInstanceView;
                })
            .switchIfEmpty(Mono.defer(() -> Mono.empty()));
    }

    @Override
    public PowerState powerState() {
        return PowerState.fromInstanceView(this.instanceView());
    }

    @Override
    public void reimage() {
        this.reimageAsync().block();
    }

    @Override
    public Mono<Void> reimageAsync() {
        return this
            .client
            .reimageAsync(this.parent().resourceGroupName(), this.parent().name(), this.instanceId(), null);
    }

    @Override
    public void deallocate() {
        this.deallocateAsync().block();
    }

    @Override
    public Mono<Void> deallocateAsync() {
        return this.client.deallocateAsync(this.parent().resourceGroupName(), this.parent().name(), this.instanceId());
    }

    @Override
    public void powerOff() {
        this.powerOffAsync().block();
    }

    @Override
    public Mono<Void> powerOffAsync() {
        return this
            .client
            .powerOffAsync(this.parent().resourceGroupName(), this.parent().name(), this.instanceId(), null);
    }

    @Override
    public void start() {
        this.startAsync().block();
    }

    @Override
    public Mono<Void> startAsync() {
        return this.client.startAsync(this.parent().resourceGroupName(), this.parent().name(), this.instanceId());
    }

    @Override
    public void restart() {
        this.restartAsync().block();
    }

    @Override
    public Mono<Void> restartAsync() {
        return this.client.restartAsync(this.parent().resourceGroupName(), this.parent().name(), this.instanceId());
    }

    @Override
    public void delete() {
        deleteAsync().block();
    }

    @Override
    public Mono<Void> deleteAsync() {
        return this.client.deleteAsync(this.parent().resourceGroupName(), this.parent().name(), this.instanceId());
    }

    @Override
    public VirtualMachineScaleSetVM refresh() {
        return this.refreshAsync().block();
    }

    @Override
    public Mono<VirtualMachineScaleSetVM> refreshAsync() {
        final VirtualMachineScaleSetVMImpl self = this;
        return this
            .client
            .getAsync(this.parent().resourceGroupName(), this.parent().name(), this.instanceId())
            .map(
                vmInner -> {
                    self.setInner(vmInner);
                    self.clearCachedRelatedResources();
                    self.initializeDataDisks();
                    return self;
                });
    }

    @Override
    public VirtualMachineScaleSetNetworkInterface getNetworkInterface(String name) {
        return this.parent().getNetworkInterfaceByInstanceId(this.instanceId(), name);
    }

    @Override
    public PagedIterable<VirtualMachineScaleSetNetworkInterface> listNetworkInterfaces() {
        return this.parent().listNetworkInterfacesByInstanceId(this.instanceId());
    }

    @Override
    public PagedFlux<VirtualMachineScaleSetNetworkInterface> listNetworkInterfacesAsync() {
        return this.parent().listNetworkInterfacesByInstanceIdAsync(this.instanceId());
    }

    @Override
    public String modelDefinitionApplied() {
        return this.inner().modelDefinitionApplied();
    }

    @Override
    public VirtualMachineScaleSetVMProtectionPolicy protectionPolicy() {
        return this.inner().protectionPolicy();
    }

    @Override
    public VirtualMachineScaleSetVMNetworkProfileConfiguration networkProfileConfiguration() {
        return this.inner().networkProfileConfiguration();
    }

    private void clearCachedRelatedResources() {
        this.virtualMachineInstanceView = null;
    }

    @Override
    public boolean isManagedDiskEnabled() {
        if (isOSBasedOnCustomImage()) {
            return true;
        }
        if (isOSBasedOnStoredImage()) {
            return false;
        }
        if (isOSBasedOnPlatformImage()) {
            if (this.inner().storageProfile().osDisk() != null
                && this.inner().storageProfile().osDisk().vhd() != null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Update withExistingDataDisk(Disk dataDisk, int lun, CachingTypes cachingTypes) {
        return this
            .withExistingDataDisk(
                dataDisk, lun, cachingTypes, StorageAccountTypes.fromString(dataDisk.sku().accountType().toString()));
    }

    @Override
    public Update withExistingDataDisk(
        Disk dataDisk, int lun, CachingTypes cachingTypes, StorageAccountTypes storageAccountTypes) {
        if (!this.isManagedDiskEnabled()) {
            throw logger.logExceptionAsError(new IllegalStateException(
                ManagedUnmanagedDiskErrors.VM_BOTH_UNMANAGED_AND_MANAGED_DISK_NOT_ALLOWED));
        }
        if (dataDisk.inner().diskState() != DiskState.UNATTACHED) {
            throw logger.logExceptionAsError(new IllegalStateException("Disk need to be in unattached state"));
        }

        ManagedDiskParameters managedDiskParameters =
            new ManagedDiskParameters().withStorageAccountType(storageAccountTypes);
        managedDiskParameters.withId(dataDisk.id());

        DataDisk attachDataDisk =
            new DataDisk()
                .withCreateOption(DiskCreateOptionTypes.ATTACH)
                .withLun(lun)
                .withCaching(cachingTypes)
                .withManagedDisk(managedDiskParameters);
        return this.withExistingDataDisk(attachDataDisk, lun);
    }

    private Update withExistingDataDisk(DataDisk dataDisk, int lun) {
        if (this.tryFindDataDisk(lun, this.inner().storageProfile().dataDisks()) != null) {
            throw logger.logExceptionAsError(new IllegalStateException(
                String.format("A data disk with lun '%d' already attached", lun)));
        } else if (this.tryFindDataDisk(lun, this.managedDataDisks.existingDisksToAttach) != null) {
            throw logger.logExceptionAsError(new IllegalStateException(
                String.format("A data disk with lun '%d' already scheduled to be attached", lun)));
        }
        this.managedDataDisks.existingDisksToAttach.add(dataDisk);
        return this;
    }

    @Override
    public Update withoutDataDisk(int lun) {
        DataDisk dataDisk = this.tryFindDataDisk(lun, this.inner().storageProfile().dataDisks());
        if (dataDisk == null) {
            throw logger.logExceptionAsError(new IllegalStateException(
                String.format("A data disk with lun '%d' not found", lun)));
        }
        if (dataDisk.createOption() != DiskCreateOptionTypes.ATTACH) {
            throw logger.logExceptionAsError(new IllegalStateException(
                String
                    .format(
                        "A data disk with lun '%d' cannot be detached, as it is part of Virtual Machine Scale Set"
                            + " model",
                        lun)));
        }
        this.managedDataDisks.diskLunsToRemove.add(lun);
        return this;
    }

    @Override
    public VirtualMachineScaleSetVM apply() {
        return this.applyAsync().block();
    }

    @Override
    public Mono<VirtualMachineScaleSetVM> applyAsync() {
        final VirtualMachineScaleSetVMImpl self = this;
        this.managedDataDisks.syncToVMDataDisks(this.inner().storageProfile());
        return this
            .parent()
            .virtualMachines()
            .inner()
            .updateAsync(this.parent().resourceGroupName(), this.parent().name(), this.instanceId(), this.inner())
            .map(
                vmInner -> {
                    self.setInner(vmInner);
                    self.clearCachedRelatedResources();
                    self.initializeDataDisks();
                    return self;
                });
    }

    @Override
    public VirtualMachineScaleSetVM.Update update() {
        initializeDataDisks();
        return this;
    }

    private void initializeDataDisks() {
        this.managedDataDisks.clear();
    }

    private DataDisk tryFindDataDisk(int lun, List<DataDisk> dataDisks) {
        DataDisk disk = null;
        if (dataDisks != null) {
            for (DataDisk dataDisk : dataDisks) {
                if (dataDisk.lun() == lun) {
                    disk = dataDisk;
                    break;
                }
            }
        }
        return disk;
    }

    /** Class to manage data disk collection. */
    private static class ManagedDataDiskCollection {
        private final List<DataDisk> existingDisksToAttach = new ArrayList<>();
        private final List<Integer> diskLunsToRemove = new ArrayList<>();

        void syncToVMDataDisks(StorageProfile storageProfile) {
            if (storageProfile != null && this.isPending()) {
                // remove disks from VM inner
                if (storageProfile.dataDisks() != null && !diskLunsToRemove.isEmpty()) {
                    Iterator<DataDisk> iterator = storageProfile.dataDisks().iterator();
                    while (iterator.hasNext()) {
                        DataDisk dataDisk = iterator.next();
                        if (diskLunsToRemove.contains(dataDisk.lun())) {
                            iterator.remove();
                        }
                    }
                }

                // add disks to VM inner
                if (!existingDisksToAttach.isEmpty()) {
                    for (DataDisk dataDisk : existingDisksToAttach) {
                        if (storageProfile.dataDisks() == null) {
                            storageProfile.withDataDisks(new ArrayList<DataDisk>());
                        }
                        storageProfile.dataDisks().add(dataDisk);
                    }
                }

                // clear ManagedDataDiskCollection after it is synced into VM inner
                this.clear();
            }
        }

        private void clear() {
            existingDisksToAttach.clear();
            diskLunsToRemove.clear();
        }

        private boolean isPending() {
            return !(existingDisksToAttach.isEmpty() && diskLunsToRemove.isEmpty());
        }
    }
}
