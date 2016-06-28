package com.microsoft.azure.management.compute;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import java.util.List;

public class VirtualMachineOperationsTests extends ComputeManagementTestBase {
    private static final String RG_NAME = "javacsmrg";
    private static final String LOCATION = "southcentralus";
    private static final String VMNAME = "javavm";

    @BeforeClass
    public static void setup() throws Exception {
        createClients();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        resourceManager.resourceGroups().delete(RG_NAME);
    }

    @Test
    public void canCreateVirtualMachine() throws Exception {
        // Create
        VirtualMachine vm = computeManager.virtualMachines()
                .define(VMNAME)
                .withRegion(LOCATION)
                .withNewResourceGroup(RG_NAME)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIpAddressDynamic()
                .withoutPrimaryPublicIpAddress()
                .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2012_DATACENTER)
                .withAdminUserName("Foo12")
                .withPassword("BaR@12")
                .withSize(VirtualMachineSizeTypes.STANDARD_D3)
                .withOsDiskCaching(CachingTypes.READ_WRITE)
                .withOsDiskName("javatest")
                .create();

        VirtualMachine foundedVM = null;
        List<VirtualMachine> vms = computeManager.virtualMachines().listByGroup(RG_NAME);
        for (VirtualMachine vm1 : vms) {
            if (vm1.name().equals(VMNAME)) {
                foundedVM = vm1;
                break;
            }
        }
        Assert.assertNotNull(foundedVM);
        Assert.assertEquals(LOCATION, foundedVM.regionName());
        // Get
        foundedVM = computeManager.virtualMachines().getByGroup(RG_NAME, VMNAME);
        Assert.assertNotNull(foundedVM);
        Assert.assertEquals(LOCATION, foundedVM.regionName());

        // Fetch instance view
        PowerState powerState = foundedVM.powerState();
        Assert.assertTrue(powerState == PowerState.RUNNING);
        VirtualMachineInstanceView instanceView = foundedVM.instanceView();
        Assert.assertNotNull(instanceView);
        Assert.assertNotNull(instanceView.statuses().size() > 0);

        // Delete VM
        computeManager.virtualMachines().delete(foundedVM.id());
    }
}
