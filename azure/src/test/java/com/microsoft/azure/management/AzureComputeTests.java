/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.compute.CachingTypes;
import com.microsoft.azure.management.compute.KnownLinuxVirtualMachineImage;
import com.microsoft.azure.management.compute.PowerState;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachineOffer;
import com.microsoft.azure.management.compute.VirtualMachinePublisher;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.compute.VirtualMachineSku;
import com.microsoft.azure.management.resources.core.TestBase;
import com.microsoft.azure.management.resources.fluentcore.arm.CountryIsoCode;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.rest.RestClient;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AzureComputeTests extends TestBase {
    private Azure azure;

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        Azure.Authenticated azureAuthed = Azure.authenticate(restClient, defaultSubscription, domain);
        azure = azureAuthed.withSubscription(defaultSubscription);
    }

    @Override
    protected void cleanUpResources() {

    }

    /**
     * Stress-tests the resilience of ExpandableEnum to multi-threaded access
     * @throws Exception
     */
    @Test
    public void testExpandableEnum() throws Exception {

        // Define some threads that read from enum
        Runnable reader1 = new Runnable() {
            @Override
            public void run() {
                Assert.assertEquals(CountryIsoCode.AFGHANISTAN, CountryIsoCode.fromString("AF"));
                Assert.assertEquals(CountryIsoCode.ANTARCTICA, CountryIsoCode.fromString("AQ"));
                Assert.assertEquals(CountryIsoCode.ANDORRA, CountryIsoCode.fromString("AD"));
                Assert.assertEquals(CountryIsoCode.ARGENTINA, CountryIsoCode.fromString("AR"));
                Assert.assertEquals(CountryIsoCode.ALBANIA, CountryIsoCode.fromString("AL"));
                Assert.assertEquals(CountryIsoCode.ALGERIA, CountryIsoCode.fromString("DZ"));
                Assert.assertEquals(CountryIsoCode.AMERICAN_SAMOA, CountryIsoCode.fromString("AS"));
                Assert.assertEquals(CountryIsoCode.ANGOLA, CountryIsoCode.fromString("AO"));
                Assert.assertEquals(CountryIsoCode.ANGUILLA, CountryIsoCode.fromString("AI"));
                Assert.assertEquals(CountryIsoCode.ANTIGUA_AND_BARBUDA, CountryIsoCode.fromString("AG"));
                Assert.assertEquals(CountryIsoCode.ARMENIA, CountryIsoCode.fromString("AM"));
                Assert.assertEquals(CountryIsoCode.ARUBA, CountryIsoCode.fromString("AW"));
                Assert.assertEquals(CountryIsoCode.AUSTRALIA, CountryIsoCode.fromString("AU"));
                Assert.assertEquals(CountryIsoCode.AUSTRIA, CountryIsoCode.fromString("AT"));
                Assert.assertEquals(CountryIsoCode.AZERBAIJAN, CountryIsoCode.fromString("AZ"));
                Assert.assertEquals(PowerState.DEALLOCATED, PowerState.fromString("PowerState/deallocated"));
                Assert.assertEquals(PowerState.DEALLOCATING, PowerState.fromString("PowerState/deallocating"));
                Assert.assertEquals(PowerState.RUNNING, PowerState.fromString("PowerState/running"));
            }
        };

        Runnable reader2 = new Runnable() {
            @Override
            public void run() {
                Assert.assertEquals(CountryIsoCode.BAHAMAS, CountryIsoCode.fromString("BS"));
                Assert.assertEquals(CountryIsoCode.BAHRAIN, CountryIsoCode.fromString("BH"));
                Assert.assertEquals(CountryIsoCode.BANGLADESH, CountryIsoCode.fromString("BD"));
                Assert.assertEquals(CountryIsoCode.BARBADOS, CountryIsoCode.fromString("BB"));
                Assert.assertEquals(CountryIsoCode.BELARUS, CountryIsoCode.fromString("BY"));
                Assert.assertEquals(CountryIsoCode.BELGIUM, CountryIsoCode.fromString("BE"));
                Assert.assertEquals(PowerState.STARTING, PowerState.fromString("PowerState/starting"));
                Assert.assertEquals(PowerState.STOPPED, PowerState.fromString("PowerState/stopped"));
                Assert.assertEquals(PowerState.STOPPING, PowerState.fromString("PowerState/stopping"));
                Assert.assertEquals(PowerState.UNKNOWN, PowerState.fromString("PowerState/unknown"));
            }
        };

        // Define some threads that write to enum
        Runnable writer1 = new Runnable() {
            @Override
            public void run() {
                for (int i = 1; i <= 10; i++) {
                    CountryIsoCode.fromString("CountryIsoCode" + i);
                    PowerState.fromString("PowerState" + i);
                }
            }
        };

        Runnable writer2 = new Runnable() {
            @Override
            public void run() {
                for (int i = 1; i <= 20; i++) {
                    CountryIsoCode.fromString("CountryIsoCode" + i);
                    PowerState.fromString("PowerState" + i);
                }
            }
        };

        // Start the threads and repeat a few times
        ExecutorService threadPool = Executors.newFixedThreadPool(4);
        for (int repeat = 0; repeat < 10; repeat++) {
            threadPool.submit(reader1);
            threadPool.submit(reader2);
            threadPool.submit(writer1);
            threadPool.submit(writer2);
        }

        // Give the test a fixed amount of time to finish
        threadPool.awaitTermination(10, TimeUnit.SECONDS);

        // Verify country ISO codes
        Collection<CountryIsoCode> countryIsoCodes = CountryIsoCode.values();
        System.out.println("\n## Country ISO codes: " + countryIsoCodes.size());
        for (CountryIsoCode value : countryIsoCodes) {
            System.out.println(value.toString());
        }
        Assert.assertEquals(257, countryIsoCodes.size());

        // Verify power states
        Collection<PowerState> powerStates = PowerState.values();
        System.out.println("\n## Power states: " + powerStates.size());
        for (PowerState value : powerStates) {
            System.out.println(value.toString());
        }
        Assert.assertEquals(27, powerStates.size());
    }

    /**
     * Tests VM images.
     * @throws IOException
     * @throws CloudException
     */
    @Test
    public void testVMImages() throws CloudException, IOException {
        List<VirtualMachinePublisher> publishers = azure.virtualMachineImages().publishers().listByRegion(Region.US_WEST);
        Assert.assertTrue(publishers.size() > 0);
        for (VirtualMachinePublisher p : publishers) {
            System.out.println(String.format("Publisher name: %s, region: %s", p.name(), p.region()));
            try {
                for (VirtualMachineOffer o : p.offers().list()) {
                    System.out.println(String.format("\tOffer name: %s", o.name()));
                    try {
                        for (VirtualMachineSku s : o.skus().list()) {
                            System.out.println(String.format("\t\tSku name: %s", s.name()));
                        }
                    } catch (com.microsoft.rest.RestException e) {
                        e.printStackTrace();
                    }
                }
            } catch (com.microsoft.rest.RestException e) {
                e.printStackTrace();
            }
        }
        List<VirtualMachineImage> images = azure.virtualMachineImages().listByRegion(Region.US_WEST);
        Assert.assertTrue(images.size() > 0);
        try {
            // Seems to help avoid connection refused error on subsequent mock test
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * Tests updating managed disks for a virtual machine
     * @throws Exception
     */
    @Test
    public void testManagedDiskVMUpdate() throws Exception {
        final String rgName = SdkContext.randomResourceName("rg", 13);
        final String linuxVM2Name = SdkContext.randomResourceName("vm" + "-", 10);
        final String linuxVM2Pip = SdkContext.randomResourceName("pip" + "-", 18);
        VirtualMachine linuxVM2 = azure.virtualMachines().define(linuxVM2Name)
            .withRegion(Region.US_EAST)
            .withNewResourceGroup(rgName)
            .withNewPrimaryNetwork("10.0.0.0/28")
            .withPrimaryPrivateIPAddressDynamic()
            .withNewPrimaryPublicIPAddress(linuxVM2Pip)
            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
            .withRootUsername("tester")
            .withRootPassword("Abcdef.123456!")
            // Begin: Managed data disks
            .withNewDataDisk(100)
            .withNewDataDisk(100, 1, CachingTypes.READ_WRITE)
            // End: Managed data disks
            .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
            .create();

        linuxVM2.deallocate();
        linuxVM2.update()
            .withoutDataDisk(2)
            .withNewDataDisk(200)
            .apply();
        azure.resourceGroups().beginDeleteByName(rgName);
    }

    /**
     * Tests virtual machines.
     * @throws Exception
     */
    @Test
    public void testVirtualMachines() throws Exception {
        // Future: This method needs to have a better specific name since we are going to include unit test for
        // different vm scenarios.
        new TestVirtualMachine().runTest(azure.virtualMachines(), azure.resourceGroups());
    }

    /**
     * Tests the virtual machine data disk implementation.
     * @throws Exception
     */
    @Test
    public void testVirtualMachineDataDisk() throws Exception {
        new TestVirtualMachineDataDisk().runTest(azure.virtualMachines(), azure.resourceGroups());
    }

    /**
     * Tests the virtual machine network interface implementation.
     * @throws Exception
     */
    @Test
    public void testVirtualMachineNics() throws Exception {
        new TestVirtualMachineNics(azure.networks().manager())
            .runTest(azure.virtualMachines(), azure.resourceGroups());
    }

    /**
     * Tests virtual machine support for SSH.
     * @throws Exception
     */
    @Test
    public void testVirtualMachineSSh() throws Exception {
        new TestVirtualMachineSsh(azure.publicIPAddresses())
            .runTest(azure.virtualMachines(), azure.resourceGroups());
    }

    /**
     * Tests virtual machine sizes.
     * @throws Exception
     */
    @Test
    public void testVirtualMachineSizes() throws Exception {
        new TestVirtualMachineSizes()
            .runTest(azure.virtualMachines(), azure.resourceGroups());
    }

    @Test
    public void testVirtualMachineCustomData() throws Exception {
        new TestVirtualMachineCustomData(azure.publicIPAddresses())
            .runTest(azure.virtualMachines(), azure.resourceGroups());
    }

    @Test
    public void testVirtualMachineInAvailabilitySet() throws Exception {
        new TestVirtualMachineInAvailabilitySet().runTest(azure.virtualMachines(), azure.resourceGroups());
    }

}
