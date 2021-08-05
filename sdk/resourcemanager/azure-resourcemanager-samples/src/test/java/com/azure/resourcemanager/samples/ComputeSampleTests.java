// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.samples;

import com.azure.resourcemanager.compute.samples.CreateVirtualMachineUsingCustomImageFromVHD;
import com.azure.resourcemanager.compute.samples.CreateVirtualMachineUsingCustomImageFromVM;
import com.azure.resourcemanager.compute.samples.CreateVirtualMachineUsingSpecializedDiskFromSnapshot;
import com.azure.resourcemanager.compute.samples.CreateVirtualMachineUsingSpecializedDiskFromVhd;
import com.azure.resourcemanager.compute.samples.CreateVirtualMachinesAsyncTrackingRelatedResources;
import com.azure.resourcemanager.compute.samples.CreateVirtualMachinesInParallel;
import com.azure.resourcemanager.compute.samples.CreateVirtualMachinesUsingCustomImageOrSpecializedVHD;
import com.azure.resourcemanager.compute.samples.ListComputeSkus;
import com.azure.resourcemanager.compute.samples.ListVirtualMachineExtensionImages;
import com.azure.resourcemanager.compute.samples.ListVirtualMachineImages;
import com.azure.resourcemanager.compute.samples.ManageAvailabilitySet;
import com.azure.resourcemanager.compute.samples.ManageManagedDisks;
import com.azure.resourcemanager.compute.samples.ManageResourceFromMSIEnabledVirtualMachineBelongsToAADGroup;
import com.azure.resourcemanager.compute.samples.ManageStorageFromMSIEnabledVirtualMachine;
import com.azure.resourcemanager.compute.samples.ManageUserAssignedMSIEnabledVirtualMachine;
import com.azure.resourcemanager.compute.samples.ManageVirtualMachine;
import com.azure.resourcemanager.compute.samples.ManageVirtualMachineAsync;
import com.azure.resourcemanager.compute.samples.ManageVirtualMachineExtension;
import com.azure.resourcemanager.compute.samples.ManageVirtualMachineScaleSet;
import com.azure.resourcemanager.compute.samples.ManageVirtualMachineScaleSetAsync;
import com.azure.resourcemanager.compute.samples.ManageVirtualMachineScaleSetWithUnmanagedDisks;
import com.azure.resourcemanager.compute.samples.ManageVirtualMachineWithDisk;
import com.azure.resourcemanager.compute.samples.ManageVirtualMachineWithUnmanagedDisks;
import com.azure.resourcemanager.compute.samples.ManageVirtualMachinesInParallel;
import com.azure.resourcemanager.compute.samples.ConvertVirtualMachineToManagedDisks;
import com.azure.resourcemanager.compute.samples.ManageZonalVirtualMachine;
import com.azure.resourcemanager.compute.samples.ManageZonalVirtualMachineScaleSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ComputeSampleTests extends SamplesTestBase {

    @Test
    public void testCreateVirtualMachinesInParallel() {
        Assertions.assertTrue(CreateVirtualMachinesInParallel.runSample(azureResourceManager));
    }

    @Test
    @Disabled("Sample leverages true parallelization, which cannot be recorded, until GenericResources support deleteByIds()")
    public void testCreateVirtualMachinesAsyncTrackingRelatedResources() {
        Assertions.assertTrue(CreateVirtualMachinesAsyncTrackingRelatedResources.runSample(azureResourceManager));
    }

    @Test
    public void testCreateVirtualMachinesUsingCustomImageOrSpecializedVHD() {
        Assertions.assertTrue(CreateVirtualMachinesUsingCustomImageOrSpecializedVHD.runSample(azureResourceManager));
    }

    @Test
    public void testCreateVirtualMachineUsingCustomImageFromVHD() {
        Assertions.assertTrue(CreateVirtualMachineUsingCustomImageFromVHD.runSample(azureResourceManager));
    }

    @Test
    public void testCreateVirtualMachineUsingCustomImageFromVM() {
        Assertions.assertTrue(CreateVirtualMachineUsingCustomImageFromVM.runSample(azureResourceManager));
    }

    @Test
    public void testCreateVirtualMachineUsingSpecializedDiskFromSnapshot() {
        Assertions.assertTrue(CreateVirtualMachineUsingSpecializedDiskFromSnapshot.runSample(azureResourceManager));
    }

    @Test
    public void testCreateVirtualMachineUsingSpecializedDiskFromVhd() {
        Assertions.assertTrue(CreateVirtualMachineUsingSpecializedDiskFromVhd.runSample(azureResourceManager));
    }

    @Test
    public void testListVirtualMachineExtensionImages() {
        Assertions.assertTrue(ListVirtualMachineExtensionImages.runSample(azureResourceManager));
    }

    @Test
    public void testListVirtualMachineImages() {
        Assertions.assertTrue(ListVirtualMachineImages.runSample(azureResourceManager));
    }

    @Test
    public void testListListComputeSkus() {
        Assertions.assertTrue(ListComputeSkus.runSample(azureResourceManager));
    }

    @Test
    public void testManageAvailabilitySet() {
        Assertions.assertTrue(ManageAvailabilitySet.runSample(azureResourceManager));
    }

    @Test
    public void testManageVirtualMachineWithUnmanagedDisks() {
        Assertions.assertTrue(ManageVirtualMachineWithUnmanagedDisks.runSample(azureResourceManager));
    }

    @Test
    public void testManageVirtualMachine() {
        Assertions.assertTrue(ManageVirtualMachine.runSample(azureResourceManager));
    }

    @Test
    public void testManageVirtualMachineAsync() {
        Assertions.assertTrue(ManageVirtualMachineAsync.runSample(azureResourceManager));
    }

    @Test
    public void testManageVirtualMachineExtension() {
        Assertions.assertTrue(ManageVirtualMachineExtension.runSample(azureResourceManager));
    }

    @Test
    public void testManageVirtualMachineScaleSet() {
        Assertions.assertTrue(ManageVirtualMachineScaleSet.runSample(azureResourceManager));
    }

    @Test
    public void testManageVirtualMachineScaleSetAsync() {
        Assertions.assertTrue(ManageVirtualMachineScaleSetAsync.runSample(azureResourceManager));
    }

    @Test
    public void testManageVirtualMachineScaleSetWithUnmanagedDisks() {
        Assertions.assertTrue(ManageVirtualMachineScaleSetWithUnmanagedDisks.runSample(azureResourceManager));
    }

    @Test
    public void testManageVirtualMachinesInParallel() {
        Assertions.assertTrue(ManageVirtualMachinesInParallel.runSample(azureResourceManager));
    }

    @Test
    public void testManageVirtualMachineWithDisk() {
        Assertions.assertTrue(ManageVirtualMachineWithDisk.runSample(azureResourceManager));
    }

    @Test
    public void testConvertVirtualMachineToManagedDisks() {
        Assertions.assertTrue(ConvertVirtualMachineToManagedDisks.runSample(azureResourceManager));
    }

    @Test
    public void testManageManagedDisks() {
        Assertions.assertTrue(ManageManagedDisks.runSample(azureResourceManager));
    }

    @Test
    @Disabled("Skipping for now - looks like a service side issue")
    public void testManageStorageFromMSIEnabledVirtualMachine() {
        Assertions.assertTrue(ManageStorageFromMSIEnabledVirtualMachine.runSample(azureResourceManager));
    }

    @Test
    @Disabled("Skipping for now - looks like a service side issue")
    public void testManageResourceFromMSIEnabledVirtualMachineBelongsToAADGroup() {
        Assertions.assertTrue(ManageResourceFromMSIEnabledVirtualMachineBelongsToAADGroup.runSample(azureResourceManager));
    }

    @Test
    public void testManageUserAssignedMSIEnabledVirtualMachine() {
        Assertions.assertTrue(ManageUserAssignedMSIEnabledVirtualMachine.runSample(azureResourceManager));
    }

    @Test
    public void testManageZonalVirtualMachine() {
        Assertions.assertTrue(ManageZonalVirtualMachine.runSample(azureResourceManager));
    }

    @Test
    public void testManageZonalVirtualMachineScaleSet() {
        Assertions.assertTrue(ManageZonalVirtualMachineScaleSet.runSample(azureResourceManager));
    }
}
