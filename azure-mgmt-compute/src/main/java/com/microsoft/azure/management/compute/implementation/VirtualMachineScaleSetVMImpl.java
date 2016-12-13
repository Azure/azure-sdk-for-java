package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.compute.CachingTypes;
import com.microsoft.azure.management.compute.DiagnosticsProfile;
import com.microsoft.azure.management.compute.ImageReference;
import com.microsoft.azure.management.compute.OSProfile;
import com.microsoft.azure.management.compute.OperatingSystemTypes;
import com.microsoft.azure.management.compute.PowerState;
import com.microsoft.azure.management.compute.Sku;
import com.microsoft.azure.management.compute.StorageProfile;
import com.microsoft.azure.management.compute.VirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachineInstanceView;
import com.microsoft.azure.management.compute.VirtualMachineScaleSet;
import com.microsoft.azure.management.compute.VirtualMachineScaleSetVM;
import com.microsoft.azure.management.compute.VirtualMachineScaleSetVMInstanceExtension;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import rx.Observable;

import java.util.ArrayList;
import java.util.Collections;
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
    public boolean isOsBasedOnPlatformImage() {
        return this.platformImageReference() != null;
    }

    @Override
    public ImageReference platformImageReference() {
        return this.inner().storageProfile().imageReference();
    }

    @Override
    public VirtualMachineImage getPlatformImage() {
        if (this.isOsBasedOnPlatformImage()) {
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
    public String customImageVhdUri() {
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
    public String osDiskVhdUri() {
        if (this.inner().storageProfile().osDisk().vhd() != null) {
            return this.inner().storageProfile().osDisk().vhd().uri();
        }
        return null;
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
    public boolean isWindowsVmAgentProvisioned() {
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
        VirtualMachineScaleSetVMInstanceViewInner instanceViewInner = this.client.getInstanceView(this.parent().resourceGroupName(),
                this.parent().name(),
                this.instanceId());
        if (instanceViewInner != null) {
            this.virtualMachineInstanceView = new VirtualMachineInstanceView()
                    .withBootDiagnostics(instanceViewInner.bootDiagnostics())
                    .withDisks(instanceViewInner.disks())
                    .withExtensions(instanceViewInner.extensions())
                    .withPlatformFaultDomain(instanceViewInner.platformFaultDomain())
                    .withPlatformUpdateDomain(instanceViewInner.platformUpdateDomain())
                    .withRdpThumbPrint(instanceViewInner.rdpThumbPrint())
                    .withStatuses(instanceViewInner.statuses())
                    .withVmAgent(instanceViewInner.vmAgent());
        }
        return this.virtualMachineInstanceView;
    }

    @Override
    public PowerState powerState() {
        return PowerState.fromInstanceView(this.instanceView());
    }

    @Override
    public void reimage() {
        this.reimageAsync().toBlocking().last();
    }

    @Override
    public Observable<Void> reimageAsync() {
        return this.client.reimageAsync(this.parent().resourceGroupName(),
                this.parent().name(),
                this.instanceId());
    }

    @Override
    public void deallocate() {
        this.deallocateAsync().toBlocking().last();
    }

    @Override
    public Observable<Void> deallocateAsync() {
        return this.client.deallocateAsync(this.parent().resourceGroupName(),
                this.parent().name(),
                this.instanceId());
    }

    @Override
    public void powerOff() {
        this.powerOffAsync().toBlocking().last();
    }

    @Override
    public Observable<Void> powerOffAsync() {
        return this.client.powerOffAsync(this.parent().resourceGroupName(),
                this.parent().name(),
                this.instanceId());
    }

    @Override
    public void start() {
        this.startAsync().toBlocking().last();
    }

    @Override
    public Observable<Void> startAsync() {
        return this.client.startAsync(this.parent().resourceGroupName(),
                this.parent().name(),
                this.instanceId());
    }

    @Override
    public void restart() {
        this.restartAsync().toBlocking().last();
    }

    @Override
    public Observable<Void> restartAsync() {
        return this.client.restartAsync(this.parent().resourceGroupName(),
                this.parent().name(),
                this.instanceId());
    }

    @Override
    public void delete() {
        deleteAsync().toBlocking().last();
    }

    @Override
    public Observable<Void> deleteAsync() {
        return this.client.deleteAsync(this.parent().resourceGroupName(),
                this.parent().name(),
                this.instanceId());
    }

    @Override
    public VirtualMachineScaleSetVM refresh() {
        this.setInner(this.client.get(this.parent().resourceGroupName(),
                this.parent().name(),
                this.instanceId()));
        this.clearCachedRelatedResources();
        return this;
    }

    private void clearCachedRelatedResources() {
        this.virtualMachineInstanceView = null;
    }
}
