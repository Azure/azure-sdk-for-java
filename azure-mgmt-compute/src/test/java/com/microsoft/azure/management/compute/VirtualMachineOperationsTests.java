package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.CreatedResources;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceNamer;
import com.microsoft.azure.management.storage.StorageAccount;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

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
        resourceManager.resourceGroups().deleteByName(RG_NAME);
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
                .withAdminUsername("Foo12")
                .withAdminPassword("BaR@12")
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
        computeManager.virtualMachines().deleteById(foundedVM.id());
    }

    @Test
    public void canCreateVirtualMachinesAndRelatedResourcesInParallel() throws Exception {
        String vmNamePrefix = "vmz";
        String publicIpNamePrefix = ResourceNamer.randomResourceName("pip-", 15);
        String networkNamePrefix = ResourceNamer.randomResourceName("vnet-", 15);
        Region region = Region.US_EAST;
        int count = 5;

        CreatablesInfo creatablesInfo = prepareCreatableVirtualMachines(region,
                vmNamePrefix,
                networkNamePrefix,
                publicIpNamePrefix,
                count);
        List<Creatable<VirtualMachine>> virtualMachineCreatables = creatablesInfo.virtualMachineCreatables;
        List<String> networkCreatableKeys = creatablesInfo.networkCreatableKeys;
        List<String> publicIpCreatableKeys = creatablesInfo.publicIpCreatableKeys;

        CreatedResources<VirtualMachine> createdVirtualMachines = computeManager.virtualMachines().create(virtualMachineCreatables);
        Assert.assertTrue(createdVirtualMachines.size() == count);

        Set<String> virtualMachineNames = new HashSet<>();
        for (int i = 0; i < count; i ++) {
            virtualMachineNames.add(String.format("%s-%d", vmNamePrefix, i));
        }
        for (VirtualMachine virtualMachine : createdVirtualMachines) {
            Assert.assertTrue(virtualMachineNames.contains(virtualMachine.name()));
            Assert.assertNotNull(virtualMachine.id());
        }

        Set<String> networkNames = new HashSet<>();
        for (int i = 0; i < count; i ++) {
            networkNames.add(String.format("%s-%d", networkNamePrefix, i));
        }
        for (String networkCreatableKey : networkCreatableKeys) {
            Network createdNetwork = (Network) createdVirtualMachines.createdRelatedResource(networkCreatableKey);
            Assert.assertNotNull(createdNetwork);
            Assert.assertTrue(networkNames.contains(createdNetwork.name()));
        }

        Set<String> publicIpAddressNames = new HashSet<>();
        for (int i = 0; i < count; i ++) {
            publicIpAddressNames.add(String.format("%s-%d", publicIpNamePrefix, i));
        }
        for (String publicIpCreatableKey : publicIpCreatableKeys) {
            PublicIpAddress createdPublicIpAddress = (PublicIpAddress) createdVirtualMachines.createdRelatedResource(publicIpCreatableKey);
            Assert.assertNotNull(createdPublicIpAddress);
            Assert.assertTrue(publicIpAddressNames.contains(createdPublicIpAddress.name()));
        }
    }

    @Test
    public void canStreamParallelCreatedVirtualMachinesAndRelatedResources() throws Exception {
        String vmNamePrefix = "vmz";
        String publicIpNamePrefix = ResourceNamer.randomResourceName("pip-", 15);
        String networkNamePrefix = ResourceNamer.randomResourceName("vnet-", 15);
        Region region = Region.US_EAST;
        int count = 5;

        final Set<String> virtualMachineNames = new HashSet<>();
        for (int i = 0; i < count; i ++) {
            virtualMachineNames.add(String.format("%s-%d", vmNamePrefix, i));
        }

        final Set<String> networkNames = new HashSet<>();
        for (int i = 0; i < count; i ++) {
            networkNames.add(String.format("%s-%d", networkNamePrefix, i));
        }

        final Set<String> publicIpAddressNames = new HashSet<>();
        for (int i = 0; i < count; i ++) {
            publicIpAddressNames.add(String.format("%s-%d", publicIpNamePrefix, i));
        }

        final CreatablesInfo creatablesInfo = prepareCreatableVirtualMachines(region,
                vmNamePrefix,
                networkNamePrefix,
                publicIpNamePrefix,
                count);
        final AtomicInteger resourceCount = new AtomicInteger(0);
        List<Creatable<VirtualMachine>> virtualMachineCreatables = creatablesInfo.virtualMachineCreatables;
        computeManager.virtualMachines().createAsync(virtualMachineCreatables)
                .map(new Func1<Indexable, Indexable>() {
                    @Override
                    public Indexable call(Indexable createdResource) {
                        if (createdResource instanceof Resource) {
                            Resource resource = (Resource) createdResource;
                            System.out.println("Created: " + resource.id());
                            if (resource instanceof VirtualMachine) {
                                VirtualMachine virtualMachine = (VirtualMachine) resource;
                                Assert.assertTrue(virtualMachineNames.contains(virtualMachine.name()));
                                Assert.assertNotNull(virtualMachine.id());
                            } else if (resource instanceof Network) {
                                Network network = (Network) resource;
                                Assert.assertTrue(networkNames.contains(network.name()));
                                Assert.assertNotNull(network.id());
                            } else if (resource instanceof PublicIpAddress) {
                                PublicIpAddress publicIpAddress = (PublicIpAddress) resource;
                                Assert.assertTrue(publicIpAddressNames.contains(publicIpAddress.name()));
                                Assert.assertNotNull(publicIpAddress.id());
                            }
                        }
                        resourceCount.incrementAndGet();
                        return createdResource;
                    }
                }).toBlocking().last();
        // 1 resource group, 1 storage, 5 network, 5 publicIp, 5 nic, 5 virtual machines
        Assert.assertTrue(resourceCount.get() == 22);
    }

    private CreatablesInfo prepareCreatableVirtualMachines(Region region,
                                                           String vmNamePrefix,
                                                           String networkNamePrefix,
                                                           String publicIpNamePrefix,
                                                           int vmCount) {
        String resourceGroupName = ResourceNamer.randomResourceName("rgvmtest-", 20);
        Creatable<ResourceGroup> resourceGroupCreatable = resourceManager.resourceGroups()
                .define(resourceGroupName)
                .withRegion(region);

        Creatable<StorageAccount> storageAccountCreatable = storageManager.storageAccounts()
                .define(ResourceNamer.randomResourceName("stg", 20))
                .withRegion(region)
                .withNewResourceGroup(resourceGroupCreatable);

        List<String> networkCreatableKeys = new ArrayList<>();
        List<String> publicIpCreatableKeys = new ArrayList<>();
        List<Creatable<VirtualMachine>> virtualMachineCreatables = new ArrayList<>();
        for (int i = 0; i < vmCount; i++) {
            Creatable<Network> networkCreatable = networkManager.networks()
                    .define(String.format("%s-%d", networkNamePrefix, i))
                    .withRegion(region)
                    .withNewResourceGroup(resourceGroupCreatable)
                    .withAddressSpace("10.0.0.0/28");
            networkCreatableKeys.add(networkCreatable.key());

            Creatable<PublicIpAddress> publicIpAddressCreatable = networkManager.publicIpAddresses()
                    .define(String.format("%s-%d", publicIpNamePrefix, i))
                    .withRegion(region)
                    .withNewResourceGroup(resourceGroupCreatable);
            publicIpCreatableKeys.add(publicIpAddressCreatable.key());


            Creatable<VirtualMachine> virtualMachineCreatable = computeManager.virtualMachines()
                    .define(String.format("%s-%d", vmNamePrefix, i))
                    .withRegion(region)
                    .withNewResourceGroup(resourceGroupCreatable)
                    .withNewPrimaryNetwork(networkCreatable)
                    .withPrimaryPrivateIpAddressDynamic()
                    .withNewPrimaryPublicIpAddress(publicIpAddressCreatable)
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                    .withRootUsername("tirekicker")
                    .withRootPassword("BaR@12!#")
                    .withNewStorageAccount(storageAccountCreatable);

            virtualMachineCreatables.add(virtualMachineCreatable);
        }
        CreatablesInfo creatablesInfo = new CreatablesInfo();
        creatablesInfo.virtualMachineCreatables = virtualMachineCreatables;
        creatablesInfo.networkCreatableKeys = networkCreatableKeys;
        creatablesInfo.publicIpCreatableKeys = publicIpCreatableKeys;
        return creatablesInfo;
    }

    class CreatablesInfo {
        public List<Creatable<VirtualMachine>> virtualMachineCreatables;
        List<String> networkCreatableKeys;
        List<String> publicIpCreatableKeys;
    }
 }
