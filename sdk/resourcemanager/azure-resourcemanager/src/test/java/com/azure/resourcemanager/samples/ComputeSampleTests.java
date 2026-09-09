// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.samples;

import com.azure.resourcemanager.compute.samples.CreateVirtualMachineUsingCustomImageFromVM;
import com.azure.resourcemanager.compute.samples.CreateVirtualMachineUsingSpecializedDiskFromSnapshot;
import com.azure.resourcemanager.compute.samples.ManageVirtualMachinesInParallel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ComputeSampleTests extends SamplesTestBase {

    @Test
    public void testCreateVirtualMachineUsingCustomImageFromVM() {
        Assertions.assertTrue(CreateVirtualMachineUsingCustomImageFromVM.runSample(azureResourceManager));
    }

    @Test
    public void testCreateVirtualMachineUsingSpecializedDiskFromSnapshot() {
        Assertions.assertTrue(CreateVirtualMachineUsingSpecializedDiskFromSnapshot.runSample(azureResourceManager));
    }

    @Test
    public void testManageVirtualMachinesInParallel() {
        Assertions.assertTrue(ManageVirtualMachinesInParallel.runSample(azureResourceManager));
    }
}
