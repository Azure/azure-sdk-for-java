/**
 * Copyright Microsoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.azure.utility.compute;

import com.microsoft.azure.management.compute.models.VirtualMachine;
import com.microsoft.azure.management.compute.models.VirtualMachineSize;
import com.microsoft.azure.management.compute.models.VirtualMachineSizeListResponse;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

public class ComputeTestHelper {
    public final static String Subscriptions = "subscriptions";
    public final static String ResourceGroups = "resourceGroups";
    public final static String Providers = "providers";
    public final static String AvailabilitySets = "availabilitySets";
    public final static String ResourceProviderNamespace = "Microsoft.Compute";
    public final static String VirtualMachines = "virtualMachines";

    private static String getEntityReferenceId(
            String subId, String resourceGrpName, String controllerName, String entityName)
    {
        return String.format("/%s/%s/%s/%s/%s/%s/%s/%s",
                Subscriptions, subId, ResourceGroups, resourceGrpName,
                Providers, ResourceProviderNamespace, controllerName,
                entityName);
    }

    public static String getAvailabilitySetRef(String subId, String resourceGrpName, String availabilitySetName) {
        return getEntityReferenceId(subId, resourceGrpName, AvailabilitySets, availabilitySetName);
    }

    public static String getVMReferenceId(String subId, String rgName, String vmName) {
        return getEntityReferenceId(subId, rgName, VirtualMachines, vmName);
    }

    public static void validateVirtualMachineSizeListResponse(VirtualMachineSizeListResponse vmSizeListResponse)
    {
        VirtualMachineSize size1 = new VirtualMachineSize();
        size1.setName("Standard_A0");
        size1.setMemoryInMB(768);
        size1.setNumberOfCores(1);
        size1.setOSDiskSizeInMB(130048);
        size1.setResourceDiskSizeInMB(20480);
        size1.setMaxDataDiskCount(1);

        VirtualMachineSize size2 = new VirtualMachineSize();
        size2.setName("Standard_A1");
        size2.setMemoryInMB(1792);
        size2.setNumberOfCores(1);
        size2.setOSDiskSizeInMB(130048);
        size2.setResourceDiskSizeInMB(71680);
        size2.setMaxDataDiskCount(2);

        List<VirtualMachineSize> vmSizesPropertyList = vmSizeListResponse.getVirtualMachineSizes();
        Assert.assertNotNull(vmSizesPropertyList);
        Assert.assertTrue("ListVMSizes should return more than 1 VM sizes", vmSizesPropertyList.size() > 1);

        VirtualMachineSize vmSizeProperties1 = null, vmSizeProperties2 = null;
        for (VirtualMachineSize size : vmSizesPropertyList) {
            if (size.getName().equals(size1.getName())) {
                vmSizeProperties1 = size;
            } else if (size.getName().equals(size2.getName())) {
                vmSizeProperties2 = size;
            }
        }

        Assert.assertNotNull(vmSizeProperties1);
        Assert.assertNotNull(vmSizeProperties2);
        compareVMSizes(size1, vmSizeProperties1);
        compareVMSizes(size2, vmSizeProperties2);
    }

    private static void compareVMSizes(VirtualMachineSize expectedVMSize, VirtualMachineSize vmSize)
    {
        Assert.assertTrue(
                String.format("memoryInMB is not correct for VMSize: %s", expectedVMSize.getName()),
                expectedVMSize.getMemoryInMB() == vmSize.getMemoryInMB());
        Assert.assertTrue(
                String.format("numberOfCores is not correct for VMSize: %s", expectedVMSize.getName()),
                expectedVMSize.getNumberOfCores() == vmSize.getNumberOfCores());
        // TODO: Will re-enable after CRP rollout
/*        Assert.assertTrue(
                String.format("osDiskSizeInMB is not correct for VMSize: %s", expectedVMSize.getName()),
                expectedVMSize.getOSDiskSizeInMB() == vmSize.getOSDiskSizeInMB());*/
        Assert.assertTrue(
                String.format("resourceDiskSizeInMB is not correct for VMSize: %s", expectedVMSize.getName()),
                expectedVMSize.getResourceDiskSizeInMB() == vmSize.getResourceDiskSizeInMB());
        Assert.assertTrue(
                String.format("maxDataDiskCount is not correct for VMSize: %s", expectedVMSize.getName()),
                expectedVMSize.getMaxDataDiskCount().equals(vmSize.getMaxDataDiskCount()));
    }
}
