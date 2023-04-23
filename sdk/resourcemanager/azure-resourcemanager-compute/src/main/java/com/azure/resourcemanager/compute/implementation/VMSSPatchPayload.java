// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.implementation;

import com.azure.core.management.SubResource;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSet;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetIpConfiguration;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetNetworkConfiguration;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetUpdate;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetUpdateIpConfiguration;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetUpdateNetworkConfiguration;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetUpdateNetworkProfile;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetUpdateOSDisk;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetUpdateOSProfile;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetUpdatePublicIpAddressConfiguration;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetUpdateStorageProfile;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetUpdateVMProfile;
import java.util.ArrayList;

class VMSSPatchPayload {
    /*
     * Note: innerModel().virtualMachineProfile().storageProfile().osDisk() won't be set if the scale set has ephemeral OS disk.
     * This may change if backend service decides that ephemeral osDisk can be updated.
     */
    static VirtualMachineScaleSetUpdate preparePatchPayload(VirtualMachineScaleSet scaleSet) {
        VirtualMachineScaleSetUpdate updateParameter = new VirtualMachineScaleSetUpdate();
        //
        updateParameter.withIdentity(scaleSet.innerModel().identity());
        updateParameter.withOverprovision(scaleSet.innerModel().overprovision());
        //updateParameter.withPlan(scaleSet.innerModel().plan());   // update cannot change plan
        updateParameter.withSinglePlacementGroup(scaleSet.innerModel().singlePlacementGroup());
        updateParameter.withSku(scaleSet.innerModel().sku());
        updateParameter.withTags(scaleSet.innerModel().tags());
        updateParameter.withUpgradePolicy(scaleSet.innerModel().upgradePolicy());
        updateParameter.withAdditionalCapabilities(scaleSet.innerModel().additionalCapabilities());
        //
        if (scaleSet.innerModel().virtualMachineProfile() != null) {
            // --
            VirtualMachineScaleSetUpdateVMProfile updateVMProfile = new VirtualMachineScaleSetUpdateVMProfile();
            updateVMProfile.withDiagnosticsProfile(scaleSet.innerModel().virtualMachineProfile().diagnosticsProfile());
            updateVMProfile.withExtensionProfile(scaleSet.innerModel().virtualMachineProfile().extensionProfile());
            updateVMProfile.withLicenseType(scaleSet.innerModel().virtualMachineProfile().licenseType());
            updateVMProfile.withBillingProfile(scaleSet.innerModel().virtualMachineProfile().billingProfile());
            //
            if (scaleSet.innerModel().virtualMachineProfile().storageProfile() != null) {
                // -- --
                VirtualMachineScaleSetUpdateStorageProfile storageProfile =
                    new VirtualMachineScaleSetUpdateStorageProfile();
                storageProfile
                    .withDataDisks(scaleSet.innerModel().virtualMachineProfile().storageProfile().dataDisks());
                storageProfile
                    .withImageReference(
                        scaleSet.innerModel().virtualMachineProfile().storageProfile().imageReference());

                if (scaleSet.innerModel().virtualMachineProfile().storageProfile().osDisk() != null
                    // Filling patch payload with osDisk property for VMSS with ephemeral OS disks will be considered updating VM model.
                    // This may have impact if user want to update the scale set without leaving existing VMs outdated,
                    // e.g. scale out using `withCapacity`, especially if the scale set's `updatePolicy` is `Rolling` or `Automatic`, which may trigger
                    // a VM restart.
                    // Thus, we won't fill for VMSS with ephemeral OS disks.
                    && !scaleSet.isEphemeralOSDisk()
                ) {
                    VirtualMachineScaleSetUpdateOSDisk osDisk = new VirtualMachineScaleSetUpdateOSDisk();
                    osDisk
                        .withCaching(scaleSet.innerModel().virtualMachineProfile().storageProfile().osDisk().caching());
                    osDisk.withImage(scaleSet.innerModel().virtualMachineProfile().storageProfile().osDisk().image());
                    osDisk
                        .withManagedDisk(
                            scaleSet.innerModel().virtualMachineProfile().storageProfile().osDisk().managedDisk());
                    osDisk
                        .withVhdContainers(
                            scaleSet.innerModel().virtualMachineProfile().storageProfile().osDisk().vhdContainers());
                    osDisk
                        .withWriteAcceleratorEnabled(
                            scaleSet
                                .innerModel()
                                .virtualMachineProfile()
                                .storageProfile()
                                .osDisk()
                                .writeAcceleratorEnabled());
                    storageProfile.withOsDisk(osDisk);
                }
                updateVMProfile.withStorageProfile(storageProfile);
                // -- --
            }
            if (scaleSet.innerModel().virtualMachineProfile().osProfile() != null) {
                // -- --
                VirtualMachineScaleSetUpdateOSProfile osProfile = new VirtualMachineScaleSetUpdateOSProfile();
                osProfile.withCustomData(scaleSet.innerModel().virtualMachineProfile().osProfile().customData());
                osProfile
                    .withLinuxConfiguration(
                        scaleSet.innerModel().virtualMachineProfile().osProfile().linuxConfiguration());
                osProfile.withSecrets(scaleSet.innerModel().virtualMachineProfile().osProfile().secrets());
                osProfile
                    .withWindowsConfiguration(
                        scaleSet.innerModel().virtualMachineProfile().osProfile().windowsConfiguration());
                updateVMProfile.withOsProfile(osProfile);
                // -- --
            }
            if (scaleSet.innerModel().virtualMachineProfile().networkProfile() != null) {
                // -- --
                VirtualMachineScaleSetUpdateNetworkProfile networkProfile =
                    new VirtualMachineScaleSetUpdateNetworkProfile();
                networkProfile.withNetworkApiVersion(scaleSet.innerModel().virtualMachineProfile().networkProfile().networkApiVersion());
                if (scaleSet.innerModel().virtualMachineProfile().networkProfile().networkInterfaceConfigurations()
                    != null) {
                    networkProfile
                        .withNetworkInterfaceConfigurations(
                            new ArrayList<>());
                    for (VirtualMachineScaleSetNetworkConfiguration nicConfig
                        : scaleSet
                            .innerModel()
                            .virtualMachineProfile()
                            .networkProfile()
                            .networkInterfaceConfigurations()) {
                        VirtualMachineScaleSetUpdateNetworkConfiguration nicPatchConfig =
                            new VirtualMachineScaleSetUpdateNetworkConfiguration();
                        nicPatchConfig.withDnsSettings(nicConfig.dnsSettings());
                        nicPatchConfig.withEnableAcceleratedNetworking(nicConfig.enableAcceleratedNetworking());
                        nicPatchConfig.withEnableIpForwarding(nicConfig.enableIpForwarding());
                        nicPatchConfig.withName(nicConfig.name());
                        nicPatchConfig.withNetworkSecurityGroup(nicConfig.networkSecurityGroup());
                        nicPatchConfig.withPrimary(nicConfig.primary());
                        if (nicConfig.ipConfigurations() != null) {
                            nicPatchConfig
                                .withIpConfigurations(new ArrayList<>());
                            for (VirtualMachineScaleSetIpConfiguration ipConfig : nicConfig.ipConfigurations()) {
                                VirtualMachineScaleSetUpdateIpConfiguration patchIpConfig =
                                    new VirtualMachineScaleSetUpdateIpConfiguration();
                                patchIpConfig
                                    .withApplicationGatewayBackendAddressPools(
                                        ipConfig.applicationGatewayBackendAddressPools());
                                patchIpConfig
                                    .withLoadBalancerBackendAddressPools(ipConfig.loadBalancerBackendAddressPools());
                                patchIpConfig.withLoadBalancerInboundNatPools(ipConfig.loadBalancerInboundNatPools());
                                patchIpConfig.withName(ipConfig.name());
                                patchIpConfig.withPrimary(ipConfig.primary());
                                patchIpConfig.withPrivateIpAddressVersion(ipConfig.privateIpAddressVersion());
                                patchIpConfig.withSubnet(ipConfig.subnet());
                                if (ipConfig.publicIpAddressConfiguration() != null) {
                                    patchIpConfig
                                        .withPublicIpAddressConfiguration(
                                            new VirtualMachineScaleSetUpdatePublicIpAddressConfiguration());
                                    patchIpConfig
                                        .publicIpAddressConfiguration()
                                        .withDnsSettings(ipConfig.publicIpAddressConfiguration().dnsSettings());
                                    patchIpConfig
                                        .publicIpAddressConfiguration()
                                        .withIdleTimeoutInMinutes(
                                            ipConfig.publicIpAddressConfiguration().idleTimeoutInMinutes());
                                    patchIpConfig
                                        .publicIpAddressConfiguration()
                                        .withName(ipConfig.publicIpAddressConfiguration().name());
                                }
                                if (ipConfig.applicationSecurityGroups() != null) {
                                    patchIpConfig.withApplicationSecurityGroups(new ArrayList<>());
                                    for (SubResource asg : ipConfig.applicationSecurityGroups()) {
                                        patchIpConfig
                                            .applicationSecurityGroups()
                                            .add(new SubResource().withId(asg.id()));
                                    }
                                }
                                nicPatchConfig.ipConfigurations().add(patchIpConfig);
                            }
                        }
                        networkProfile.networkInterfaceConfigurations().add(nicPatchConfig);
                    }
                }
                updateVMProfile.withNetworkProfile(networkProfile);
                // -- --
            }
            updateParameter.withVirtualMachineProfile(updateVMProfile);
            // --
        }
        //
        return updateParameter;
    }
}
