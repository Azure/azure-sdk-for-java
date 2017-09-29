/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.compute.CachingTypes;
import com.microsoft.azure.management.compute.DataDisk;
import com.microsoft.azure.management.compute.DiagnosticsProfile;
import com.microsoft.azure.management.compute.ImageReference;
import com.microsoft.azure.management.compute.OSProfile;
import com.microsoft.azure.management.compute.OperatingSystemTypes;
import com.microsoft.azure.management.compute.PowerState;
import com.microsoft.azure.management.compute.Sku;
import com.microsoft.azure.management.compute.StorageProfile;
import com.microsoft.azure.management.compute.VirtualMachineCustomImage;
import com.microsoft.azure.management.compute.VirtualMachineDataDisk;
import com.microsoft.azure.management.compute.VirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachineInstanceView;
import com.microsoft.azure.management.compute.VirtualMachineScaleSet;
import com.microsoft.azure.management.compute.VirtualMachineScaleSetVM;
import com.microsoft.azure.management.compute.VirtualMachineScaleSetVMInstanceExtension;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.compute.VirtualMachineUnmanagedDataDisk;
import com.microsoft.azure.management.network.VirtualMachineScaleSetNetworkInterface;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import rx.Completable;
import rx.Observable;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link VirtualMachineScaleSetVM}.
 */
@LangDefinition
class VirtualMachineScaleSetVMImpl
        extends ChildResourceImpl<VirtualMachineScaleSetVMInner,
            VirtualMachineScaleSetImpl,
            VirtualMachineScaleSet>
        implements VirtualMachineScaleSetVM {

    private VirtualMachineInstanceView virtualMachineInstanceView;
    private final VirtualMachineScaleSetVMsInner client;
    private final ComputeManager computeManager;

    VirtualMachineScaleSetVMImpl(VirtualMachineScaleSetVMInner inner,
                                 final VirtualMachineScaleSetImpl parent,
                                 final VirtualMachineScaleSetVMsInner client,
                                 final ComputeManager computeManager) {
        super(inner, parent);
        this.client = client;
        this.computeManager = computeManager;
        this.virtualMachineInstanceView = this.inner().instanceView();
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
        if (this.inner().getTags() == null) {
            return Collections.unmodifiableMap(new LinkedHashMap<String, String>());
        }
        return Collections.unmodifiableMap(this.inner().getTags());
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
            return new VirtualMachineSizeTypes(this.sku().name());
        }
        return null;
    }

    @Override
    public boolean isLatestScaleSetUpdateApplied() {
        return this.inner().latestModelApplied();
    }

    @Override
    public boolean isOSBasedOnPlatformImage() {
        ImageReferenceInner imageReference = this.inner().storageProfile().imageReference();
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
    public  boolean isOSBasedOnCustomImage() {
        ImageReferenceInner imageReference = this.inner().storageProfile().imageReference();
        if (imageReference != null
                && imageReference.id() != null) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isOSBasedOnStoredImage() {
        if (this.inner().storageProfile().osDisk() != null
                && this.inner().storageProfile().osDisk().image() != null) {
            return this.inner().storageProfile().osDisk().image().uri() != null;
        }
        return false;
    }

    @Override
    public ImageReference platformImageReference() {
        if (isOSBasedOnPlatformImage()) {
            return new ImageReference(this.inner().storageProfile().imageReference());
        }
        return null;
    }
    
    @Override
    public VirtualMachineImage getOSPlatformImage() {
        if (this.isOSBasedOnPlatformImage()) {
            ImageReference imageReference = this.platformImageReference();
            return this.computeManager.virtualMachineImages().getImage(this.region(),
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
            ImageReferenceInner imageReference = this.inner().storageProfile().imageReference();
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
            return !Utils.toPrimitiveBoolean(this.inner().osProfile().linuxConfiguration().disablePasswordAuthentication());
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
        if (this.inner().diagnosticsProfile() != null
                && this.inner().diagnosticsProfile().bootDiagnostics() != null) {
            return Utils.toPrimitiveBoolean(this.inner().diagnosticsProfile().bootDiagnostics().enabled());
        }
        return false;
    }

    @Override
    public String bootDiagnosticStorageAccountUri() {
        if (this.inner().diagnosticsProfile() != null
                && this.inner().diagnosticsProfile().bootDiagnostics() != null) {
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
        for (NetworkInterfaceReferenceInner reference : this.inner().networkProfile().networkInterfaces()) {
            resourceIds.add(reference.id());
        }
        return Collections.unmodifiableList(resourceIds);
    }

    @Override
    public String primaryNetworkInterfaceId() {
        for (NetworkInterfaceReferenceInner reference : this.inner().networkProfile().networkInterfaces()) {
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
                extensions.put(extensionInner.name(), new VirtualMachineScaleSetVMInstanceExtensionImpl(extensionInner, this));
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
        return refreshInstanceViewAsync().toBlocking().last();
    }

    public Observable<VirtualMachineInstanceView> refreshInstanceViewAsync() {
        return this.client.getInstanceViewAsync(this.parent().resourceGroupName(),
            this.parent().name(),
            this.instanceId())
            .map(new Func1<VirtualMachineScaleSetVMInstanceViewInner, VirtualMachineInstanceView>() {
                @Override
                public VirtualMachineInstanceView call(VirtualMachineScaleSetVMInstanceViewInner instanceViewInner) {
                    if (instanceViewInner != null) {
                        virtualMachineInstanceView = new VirtualMachineInstanceView()
                            .withBootDiagnostics(instanceViewInner.bootDiagnostics())
                            .withDisks(instanceViewInner.disks())
                            .withExtensions(instanceViewInner.extensions())
                            .withPlatformFaultDomain(instanceViewInner.platformFaultDomain())
                            .withPlatformUpdateDomain(instanceViewInner.platformUpdateDomain())
                            .withRdpThumbPrint(instanceViewInner.rdpThumbPrint())
                            .withStatuses(instanceViewInner.statuses())
                            .withVmAgent(instanceViewInner.vmAgent());
                    }
                    return virtualMachineInstanceView;
                }
            });
    }

    @Override
    public PowerState powerState() {
        return PowerState.fromInstanceView(this.instanceView());
    }

    @Override
    public void reimage() {
        this.reimageAsync().await();
    }

    @Override
    public Completable reimageAsync() {
        return this.client.reimageAsync(this.parent().resourceGroupName(),
                this.parent().name(),
                this.instanceId()).toCompletable();
    }

    @Override
    public void deallocate() {
        this.deallocateAsync().await();
    }

    @Override
    public Completable deallocateAsync() {
        return this.client.deallocateAsync(this.parent().resourceGroupName(),
                this.parent().name(),
                this.instanceId()).toCompletable();
    }

    @Override
    public void powerOff() {
        this.powerOffAsync().await();
    }

    @Override
    public Completable powerOffAsync() {
        return this.client.powerOffAsync(this.parent().resourceGroupName(),
                this.parent().name(),
                this.instanceId()).toCompletable();
    }

    @Override
    public void start() {
        this.startAsync().await();
    }

    @Override
    public Completable startAsync() {
        return this.client.startAsync(this.parent().resourceGroupName(),
                this.parent().name(),
                this.instanceId()).toCompletable();
    }

    @Override
    public void restart() {
        this.restartAsync().await();
    }

    @Override
    public Completable restartAsync() {
        return this.client.restartAsync(this.parent().resourceGroupName(),
                this.parent().name(),
                this.instanceId())
                .toCompletable();
    }

    @Override
    public void delete() {
        deleteAsync().await();
    }

    @Override
    public Completable deleteAsync() {
        return this.client.deleteAsync(this.parent().resourceGroupName(),
                this.parent().name(),
                this.instanceId()).toCompletable();
    }

    @Override
    public VirtualMachineScaleSetVM refresh() {
        return this.refreshAsync().toBlocking().last();
    }

    @Override
    public Observable<VirtualMachineScaleSetVM> refreshAsync() {
        final VirtualMachineScaleSetVMImpl self = this;
        return this.client.getAsync(this.parent().resourceGroupName(),
                this.parent().name(),
                this.instanceId()).map(new Func1<VirtualMachineScaleSetVMInner, VirtualMachineScaleSetVM>() {
            @Override
            public VirtualMachineScaleSetVM call(VirtualMachineScaleSetVMInner virtualMachineScaleSetVMInner) {
                self.setInner(virtualMachineScaleSetVMInner);
                self.clearCachedRelatedResources();
                return self;
            }
        });
    }

    @Override
    public VirtualMachineScaleSetNetworkInterface getNetworkInterface(String name) {
        return this.parent().getNetworkInterfaceByInstanceId(this.instanceId(), name);
    }

    @Override
    public PagedList<VirtualMachineScaleSetNetworkInterface> listNetworkInterfaces() {
        return this.parent().listNetworkInterfacesByInstanceId(this.instanceId());
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

}
