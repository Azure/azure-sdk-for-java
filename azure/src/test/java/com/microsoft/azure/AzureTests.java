/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure;

import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.compute.Offer;
import com.microsoft.azure.management.compute.Publisher;
import com.microsoft.azure.management.compute.VirtualMachineImage;
import com.microsoft.azure.management.resources.GenericResource;
import com.microsoft.azure.management.resources.Subscriptions;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.management.storage.implementation.api.SkuName;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class AzureTests {
    private static final ServiceClientCredentials CREDENTIALS = new ApplicationTokenCredentials(
            System.getenv("client-id"),
            System.getenv("domain"),
            System.getenv("secret"),
            AzureEnvironment.AZURE);
    private static final String SUBSCRIPTION_ID = System.getenv("arm.subscriptionid");
    private Subscriptions subscriptions;
    private Azure azure, azure2;

    public static void main(String[] args) throws IOException, CloudException {
        final File credFile = new File("my.azureauth");
        Azure azure = Azure.authenticate(credFile).withDefaultSubscription();

        System.out.println(String.valueOf(azure.resourceGroups().list().size()));

        Azure.configure().withLogLevel(Level.BASIC).authenticate(credFile);
        System.out.println("Selected subscription: " + azure.subscriptionId());
        System.out.println(String.valueOf(azure.resourceGroups().list().size()));

        final File authFileNoSubscription = new File("nosub.azureauth");
        azure = Azure.authenticate(authFileNoSubscription).withDefaultSubscription();
        System.out.println("Selected subscription: " + azure.subscriptionId());
        System.out.println(String.valueOf(azure.resourceGroups().list().size()));
    }

    @Before
    public void setup() throws Exception {
        // Authenticate based on credentials instance
        Azure.Authenticated azureAuthed = Azure.configure()
                .withLogLevel(Level.BODY)
                .withUserAgent("AzureTests")
                .authenticate(CREDENTIALS);

        subscriptions = azureAuthed.subscriptions();
        azure = azureAuthed.withSubscription(SUBSCRIPTION_ID);

        // Authenticate based on file
        File authFile = new File("my.azureauth");
        if (authFile.exists()) {
            this.azure2 = Azure.authenticate(new File("my.azureauth"))
                    .withDefaultSubscription();
        } else {
            this.azure2 = azure;
        }
    }

    /**
     * Tests basic generic resources retrieval
     * @throws Exception
     */
    @Test public void testGenericResources() throws Exception {
        azure2.genericResources().listByGroup("sdkpriv");
        GenericResource resourceByGroup = azure2.genericResources().getByGroup("sdkpriv", "sdkpriv");
        GenericResource resourceById = azure2.genericResources().getById(resourceByGroup.id());
        Assert.assertTrue(resourceById.id().equalsIgnoreCase(resourceByGroup.id()));
    }

    /**
     * Tests VM images
     * @throws IOException
     * @throws CloudException
     */
    @Test public void testVMImages() throws CloudException, IOException {
        List<Publisher> publishers = azure2.virtualMachineImages().publishers().listByRegion(Region.US_WEST);
        Assert.assertTrue(publishers.size() > 0);
        for (Publisher p : publishers) {
            System.out.println(String.format("Publisher name: %s, region: %s", p.name(), p.region()));
            for (Offer o : p.offers().list()) {
                System.out.println(String.format("\tOffer name: %s", o.name()));
            }
        }
        List<VirtualMachineImage> images = azure2.virtualMachineImages().listByRegion(Region.US_WEST);
        Assert.assertTrue(images.size() > 0);
    }

    /**
     * Tests the network security group implementation
     * @throws Exception
     */
    @Test public void testNetworkSecurityGroups() throws Exception {
        new TestNSG().runTest(azure2.networkSecurityGroups(), azure2.resourceGroups());
    }

    /**
     * Tests the public IP address implementation
     * @throws Exception
     */
    @Test public void testPublicIpAddresses() throws Exception {
        new TestPublicIpAddress().runTest(azure2.publicIpAddresses(), azure2.resourceGroups());
    }

    /**
     * Tests the availability set implementation
     * @throws Exception
     */
    @Test public void testAvailabilitySets() throws Exception {
        new TestAvailabilitySet().runTest(azure2.availabilitySets(), azure2.resourceGroups());
    }

    /**
     * Tests the virtual network implementation
     * @throws Exception
     */
    @Test public void testNetworks() throws Exception {
        new TestNetwork().runTest(azure2.networks(), azure2.resourceGroups());
    }

    /**
     * Tests the network interface implementation
     * @throws Exception
     */
    @Test public void testNetworkInterfaces() throws Exception {
        new TestNetworkInterface().runTest(azure2.networkInterfaces(), azure2.resourceGroups());
    }

    @Test public void testVirtualMachines() throws Exception {
        // Future: This method needs to have a better specific name since we are going to include unit test for
        // different vm scenarios.
        new TestVirtualMachine().runTest(azure2.virtualMachines(), azure2.resourceGroups());
    }

    /**
     * Tests the virtual machine data disk implementation
     * @throws Exception
     */
    @Test public void testVirtualMachineDataDisk() throws Exception {
        new TestVirtualMachineDataDisk().runTest(azure.virtualMachines(), azure.resourceGroups());
    }

    /**
     * Tests the virtual machine network interface implementation
     * @throws Exception
     */
    @Test public void testVirtualMachineNics() throws Exception {
        new TestVirtualMachineNics(azure2.resourceGroups(),
                    azure2.networks(),
                    azure2.networkInterfaces())
                .runTest(azure2.virtualMachines(), azure2.resourceGroups());
    }

    @Test public void testVirtualMachineSSh() throws Exception {
        new TestVirtualMachineSsh()
                .runTest(azure.virtualMachines(), azure.resourceGroups());
    }

    @Test public void testVirtualMachineSizes() throws Exception {
        new TestVirtualMachineSizes()
                .runTest(azure2.virtualMachines(), azure2.resourceGroups());
    }

    @Test
    public void listSubscriptions() throws Exception {
        Assert.assertTrue(0 < subscriptions.list().size());
    }

    @Test
    public void listResourceGroups() throws Exception {
        Assert.assertTrue(0 < azure.resourceGroups().list().size());
    }

    @Test
    public void listStorageAccounts() throws Exception {
        Assert.assertTrue(0 < azure.storageAccounts().list().size());
    }

    @Test
    public void createStorageAccount() throws Exception {
        StorageAccount storageAccount = azure.storageAccounts().define("my-stg1")
                .withRegion(Region.ASIA_EAST)
                .withNewGroup()
                .withSku(SkuName.PREMIUM_LRS)
                .create();

        Assert.assertSame(storageAccount.name(), "my-stg1");
    }
}
