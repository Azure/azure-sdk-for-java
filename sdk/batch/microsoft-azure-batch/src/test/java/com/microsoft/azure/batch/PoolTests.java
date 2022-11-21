// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.batch;

import org.junit.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.microsoft.azure.batch.protocol.models.*;

public class PoolTests extends BatchIntegrationTestBase {
    private static CloudPool livePool;
    private static String poolId;
    private static NetworkConfiguration networkConfiguration;

    @BeforeClass
    public static void setup() throws Exception {
        poolId = getStringIdWithUserNamePrefix("-testpool");
        if(isRecordMode()) {
            createClient(AuthMode.AAD);
            livePool = createIfNotExistIaaSPool(poolId);
            Assert.assertNotNull(livePool);
        }
        // Need VNet to allow security to inject NSGs
        networkConfiguration = createNetworkConfiguration();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        try {
            // batchClient.poolOperations().deletePool(livePool.id());
        } catch (Exception e) {
            // ignore any clean up exception
        }
    }

    @Test
    public void testPoolOData() throws Exception {
        CloudPool pool = batchClient.poolOperations().getPool(poolId,
                new DetailLevel.Builder().withExpandClause("stats").build());

        //Temporarily Disabling the stats check, REST API doesn't provide the stats consistently for newly created pools
        // Will be enabled back soon.
        //Assert.assertNotNull(pool.stats());

        List<CloudPool> pools = batchClient.poolOperations()
                .listPools(new DetailLevel.Builder().withSelectClause("id, state").build());
        Assert.assertTrue(pools.size() > 0);
        Assert.assertNotNull(pools.get(0).id());
        Assert.assertNull(pools.get(0).vmSize());

        // When tests are being ran in parallel, there may be a previous pool delete still in progress
        pools = batchClient.poolOperations()
                .listPools(new DetailLevel.Builder().withFilterClause("state eq 'deleting'").build());
        Assert.assertTrue(pools.size() < 2);

    }

    @Test
    public void canCRUDLowPriIaaSPool() throws Exception {
        // CREATE
        String poolId = getStringIdWithUserNamePrefix("-canCRUDLowPri-testPool");

        // Create a pool with 3 Small VMs
        String POOL_VM_SIZE = "STANDARD_D1_V2";
        int POOL_VM_COUNT = 2;
        int POOL_LOW_PRI_VM_COUNT = 2;

        // 10 minutes
        long POOL_STEADY_TIMEOUT_IN_MILLISECONDS = 10 * 60 * 1000;
        TimeUnit.SECONDS.toMillis(30);

        // Check if pool exists
        if (!batchClient.poolOperations().existsPool(poolId)) {
            ImageReference imgRef = new ImageReference().withPublisher("Canonical").withOffer("UbuntuServer")
                    .withSku("18.04-LTS").withVersion("latest");
            VirtualMachineConfiguration configuration = new VirtualMachineConfiguration();
            configuration.withNodeAgentSKUId("batch.node.ubuntu 18.04").withImageReference(imgRef);

            NetworkConfiguration netConfig = createNetworkConfiguration();
            PoolEndpointConfiguration endpointConfig = new PoolEndpointConfiguration();
            List<InboundNATPool> inbounds = new ArrayList<>();
            inbounds.add(new InboundNATPool().withName("testinbound").withProtocol(InboundEndpointProtocol.TCP)
                    .withBackendPort(5000).withFrontendPortRangeStart(60000).withFrontendPortRangeEnd(60040));
            endpointConfig.withInboundNATPools(inbounds);
            netConfig.withEndpointConfiguration(endpointConfig);

            PoolAddParameter addParameter = new PoolAddParameter().withId(poolId)
                    .withTargetDedicatedNodes(POOL_VM_COUNT).withTargetLowPriorityNodes(POOL_LOW_PRI_VM_COUNT)
                    .withVmSize(POOL_VM_SIZE).withVirtualMachineConfiguration(configuration)
                    .withNetworkConfiguration(netConfig)
                    .withTargetNodeCommunicationMode(NodeCommunicationMode.DEFAULT);
            batchClient.poolOperations().createPool(addParameter);
        }

        try {
            // GET
            Assert.assertTrue(batchClient.poolOperations().existsPool(poolId));

            long startTime = System.currentTimeMillis();
            long elapsedTime = 0L;

            // Wait for the VM to be allocated
            CloudPool pool = waitForPoolState(poolId, AllocationState.STEADY, POOL_STEADY_TIMEOUT_IN_MILLISECONDS);

            Assert.assertEquals(POOL_VM_COUNT, (long) pool.currentDedicatedNodes());
            Assert.assertEquals(POOL_LOW_PRI_VM_COUNT, (long) pool.currentLowPriorityNodes());
            Assert.assertNotNull("CurrentNodeCommunicationMode should be defined for pool with more than one target dedicated node", pool.currentNodeCommunicationMode());
            Assert.assertEquals(NodeCommunicationMode.DEFAULT, pool.targetNodeCommunicationMode());

            List<ComputeNode> computeNodes = batchClient.computeNodeOperations().listComputeNodes(poolId);
            List<InboundEndpoint> inboundEndpoints = computeNodes.get(0).endpointConfiguration().inboundEndpoints();
            Assert.assertEquals(2, inboundEndpoints.size());
            InboundEndpoint inboundEndpoint = inboundEndpoints.get(0);
            Assert.assertEquals(5000, inboundEndpoint.backendPort());
            Assert.assertTrue(inboundEndpoint.frontendPort() >= 60000);
            Assert.assertTrue(inboundEndpoint.frontendPort() <= 60040);
            Assert.assertTrue(inboundEndpoint.name().startsWith("testinbound."));
            Assert.assertTrue(inboundEndpoints.get(1).name().startsWith("SSHRule"));

            // CHECK POOL NODE COUNTS
            PoolNodeCounts poolNodeCount = null;
            List<PoolNodeCounts> poolNodeCounts = batchClient.accountOperations().listPoolNodeCounts();
            for (PoolNodeCounts tmp : poolNodeCounts) {
                if (tmp.poolId().equals(poolId)) {
                    poolNodeCount = tmp;
                    break;
                }
            }
            Assert.assertNotNull(poolNodeCount); // Single pool only
            Assert.assertNotNull(poolNodeCount.lowPriority());

            Assert.assertEquals(POOL_LOW_PRI_VM_COUNT, poolNodeCount.lowPriority().total());
            Assert.assertEquals(POOL_VM_COUNT, poolNodeCount.dedicated().total());

            // Update NodeCommunicationMode to Simplified
            PoolUpdatePropertiesParameter updatePropertiesParam = new PoolUpdatePropertiesParameter();
            updatePropertiesParam.withTargetNodeCommunicationMode(NodeCommunicationMode.SIMPLIFIED)
                    .withApplicationPackageReferences( new LinkedList<ApplicationPackageReference>())
                    .withMetadata(new LinkedList<MetadataItem>())
                    .withCertificateReferences(new LinkedList<CertificateReference>());

            batchClient.poolOperations().updatePoolProperties(poolId, updatePropertiesParam);
            pool = batchClient.poolOperations().getPool(poolId);
            Assert.assertNotNull("CurrentNodeCommunicationMode should be defined for pool with more than one target dedicated node", pool.currentNodeCommunicationMode());
            Assert.assertEquals(NodeCommunicationMode.SIMPLIFIED, pool.targetNodeCommunicationMode());

            // Patch NodeCommunicationMode to Classic
            PoolPatchParameter patchParam = new PoolPatchParameter();
            patchParam.withTargetNodeCommunicationMode(NodeCommunicationMode.CLASSIC);
            batchClient.poolOperations().patchPool(poolId, patchParam);
            pool = batchClient.poolOperations().getPool(poolId);
            Assert.assertNotNull("CurrentNodeCommunicationMode should be defined for pool with more than one target dedicated node", pool.currentNodeCommunicationMode());
            Assert.assertEquals(NodeCommunicationMode.CLASSIC, pool.targetNodeCommunicationMode());

            // RESIZE
            batchClient.poolOperations().resizePool(poolId, 1, 1);

            pool = batchClient.poolOperations().getPool(poolId);
            Assert.assertEquals(1, (long) pool.targetDedicatedNodes());
            Assert.assertEquals(1, (long) pool.targetLowPriorityNodes());

            // DELETE
            boolean deleted = false;
            elapsedTime = 0L;
            batchClient.poolOperations().deletePool(poolId);

            // Wait for the VM to be deallocated
            while (elapsedTime < POOL_STEADY_TIMEOUT_IN_MILLISECONDS) {
                try {
                    batchClient.poolOperations().getPool(poolId);
                } catch (BatchErrorException err) {
                    if (err.body().code().equals(BatchErrorCodeStrings.PoolNotFound)) {
                        deleted = true;
                        break;
                    } else {
                        throw err;
                    }
                }

                System.out.println("wait 15 seconds for pool delete...");
                threadSleepInRecordMode(15 * 1000);
                elapsedTime = (new Date()).getTime() - startTime;
            }
            Assert.assertTrue(deleted);

        } finally {
            try {
                if (batchClient.poolOperations().existsPool(poolId)) {
                    batchClient.poolOperations().deletePool(poolId);
                }
            } catch (Exception e) {
                // Ignore exception
            }
        }
    }

    @Test
    public void canCreateDataDisk() throws Exception {
        String poolId = getStringIdWithUserNamePrefix("-testpool3");

        // Create a pool with 0 Small VMs
        String POOL_VM_SIZE = "STANDARD_D1_V2";
        int POOL_VM_COUNT = 0;
        int lun = 50;
        int diskSizeGB = 50;

        // Use IaaS VM with Linux
        List<DataDisk> dataDisks = new ArrayList<DataDisk>();
        dataDisks.add(new DataDisk().withLun(lun).withDiskSizeGB(diskSizeGB));
        VirtualMachineConfiguration configuration = new VirtualMachineConfiguration();
        configuration
                .withImageReference(
                        new ImageReference().withPublisher("Canonical").withOffer("UbuntuServer").withSku("18.04-LTS"))
                .withNodeAgentSKUId("batch.node.ubuntu 18.04").withDataDisks(dataDisks);
        PoolAddParameter poolConfig =  new PoolAddParameter()
            .withId(poolId)
            .withNetworkConfiguration(networkConfiguration)
            .withTargetDedicatedNodes(POOL_VM_COUNT)
            .withVmSize(POOL_VM_SIZE)
            .withVirtualMachineConfiguration(configuration);
        try {
            batchClient.poolOperations().createPool(poolConfig);

            CloudPool pool = batchClient.poolOperations().getPool(poolId);
            Assert.assertEquals(lun, pool.virtualMachineConfiguration().dataDisks().get(0).lun());
            Assert.assertEquals(diskSizeGB, pool.virtualMachineConfiguration().dataDisks().get(0).diskSizeGB());
        } finally {
            try {
                if (batchClient.poolOperations().existsPool(poolId)) {
                    batchClient.poolOperations().deletePool(poolId);
                }
            } catch (Exception e) {
                // Ignore exception
            }
        }
    }

    @Test
    public void canCreateCustomImageWithExpectedError() throws Exception {
        String poolId = getStringIdWithUserNamePrefix("-customImageExpErr");

        // Create a pool with 0 Small VMs
        String POOL_VM_SIZE = "STANDARD_D1_V2";
        int POOL_VM_COUNT = 0;

        // Use IaaS VM with Linux
        VirtualMachineConfiguration configuration = new VirtualMachineConfiguration();
        configuration.withImageReference(new ImageReference().withVirtualMachineImageId(String.format(
            "/subscriptions/%s/resourceGroups/batchexp/providers/Microsoft.Compute/images/FakeImage",
            System.getenv("SUBSCRIPTION_ID"))))
            .withNodeAgentSKUId("batch.node.ubuntu 16.04");
        PoolAddParameter poolConfig = new PoolAddParameter()
            .withId(poolId)
            .withVmSize(POOL_VM_SIZE)
            .withTargetDedicatedNodes(POOL_VM_COUNT)
            .withVirtualMachineConfiguration(configuration)
            .withNetworkConfiguration(networkConfiguration);
        try {
            batchClient.poolOperations().createPool(poolConfig);
            throw new Exception("Expect exception, but not got it.");
        } catch (BatchErrorException err) {
            if (err.body().code().equals("InsufficientPermissions")) {
                // Accepted Error
                Assert.assertTrue(err.body().values().get(0).value().contains(
                        "The user identity used for this operation does not have the required privilege Microsoft.Compute/images/read on the specified resource"));
            } else {
                if (!err.body().code().equals("InvalidPropertyValue")) {
                    throw err;
                }
            }
        } finally {
            try {
                if (batchClient.poolOperations().existsPool(poolId)) {
                    batchClient.poolOperations().deletePool(poolId);
                }
            } catch (Exception e) {
                // Ignore exception
            }
        }
    }

    @Test
    public void shouldFailOnCreateContainerPoolWithRegularImage() throws Exception {
        String poolId = getStringIdWithUserNamePrefix("-createContainerRegImage");

        // Create a pool with 0 Small VMs
        String POOL_VM_SIZE = "STANDARD_D1_V2";
        int POOL_VM_COUNT = 0;

        // Use IaaS VM with Linux
        List<String> images = new ArrayList<String>();
        images.add("ubuntu");
        VirtualMachineConfiguration configuration = new VirtualMachineConfiguration();
        configuration
                .withImageReference(
                        new ImageReference().withPublisher("Canonical").withOffer("UbuntuServer").withSku("18.04-LTS"))
                .withNodeAgentSKUId("batch.node.ubuntu 18.04")
                .withContainerConfiguration(new ContainerConfiguration().withContainerImageNames(images));
        PoolAddParameter poolConfig = new PoolAddParameter()
            .withId(poolId)
            .withVmSize(POOL_VM_SIZE)
            .withTargetDedicatedNodes(POOL_VM_COUNT)
            .withVirtualMachineConfiguration(configuration)
            .withNetworkConfiguration(networkConfiguration);
        try {
            batchClient.poolOperations().createPool(poolConfig);
            throw new Exception("The test case should throw exception here");
        } catch (BatchErrorException err) {
            if (err.body().code().equals("InvalidPropertyValue")) {
                // Accepted Error
                for (int i = 0; i < err.body().values().size(); i++) {
                    if (err.body().values().get(i).key().equals("Reason")) {
                        Assert.assertEquals(
                                "The specified imageReference with publisher Canonical offer UbuntuServer sku 18.04-LTS does not support container feature.",
                                err.body().values().get(i).value());
                        return;
                    }
                }
                throw new Exception("Couldn't find expect error reason");
            } else {
                throw err;
            }
        } finally {
            try {
                if (batchClient.poolOperations().existsPool(poolId)) {
                    batchClient.poolOperations().deletePool(poolId);
                }
            } catch (Exception e) {
                // Ignore exception
            }
        }
    }

    //Temporarily disabling this test - REST API is missing the logic for this case.
    @Test
    public void shouldFailOnCreateLinuxPoolWithWindowsConfig() throws Exception {
        String poolId = getStringIdWithUserNamePrefix("-createLinuxPool");

        // Create a pool with 0 Small VMs
        String POOL_VM_SIZE = "STANDARD_D1_V2";
        int POOL_VM_COUNT = 0;

        // Use IaaS VM with Linux
        List<String> images = new ArrayList<String>();
        images.add("ubuntu");
        VirtualMachineConfiguration configuration = new VirtualMachineConfiguration();
        configuration
                .withImageReference(
                        new ImageReference().withPublisher("Canonical").withOffer("UbuntuServer").withSku("16.04-LTS"))
                .withNodeAgentSKUId("batch.node.ubuntu 16.04");
        UserAccount windowsUser = new UserAccount();
        windowsUser.withWindowsUserConfiguration(new WindowsUserConfiguration().withLoginMode(LoginMode.INTERACTIVE))
                .withName("testaccount")
                .withPassword("password");
        ArrayList<UserAccount> users = new ArrayList<UserAccount>();
        users.add(windowsUser);
        PoolAddParameter pool = new PoolAddParameter().withId(poolId)
                .withVirtualMachineConfiguration(configuration)
                .withTargetDedicatedNodes(POOL_VM_COUNT)
                .withTargetLowPriorityNodes(0)
                .withVmSize(POOL_VM_SIZE)
                .withUserAccounts(users)
                .withNetworkConfiguration(networkConfiguration);

        try {
            batchClient.poolOperations().createPool(pool);
            throw new Exception("The test case should throw exception here");
        } catch (BatchErrorException err) {
            if (err.body().code().equals("InvalidPropertyValue")) {
                // Accepted Error
                for (int i = 0; i < err.body().values().size(); i++) {
                    if (err.body().values().get(i).key().equals("Reason")) {
                        Assert.assertEquals(
                                "The user configuration for user account 'testaccount' has a mismatch with the OS (Windows/Linux) configuration specified in VirtualMachineConfiguration",
                                err.body().values().get(i).value());
                        return;
                    }
                }
                throw new Exception("Couldn't find expect error reason");
            } else {
                throw err;
            }
        } finally {
            try {
                if (batchClient.poolOperations().existsPool(poolId)) {
                    batchClient.poolOperations().deletePool(poolId);
                }
            } catch (Exception e) {
                // Ignore exception
            }
        }
    }

    @Test
    public void canCRUDLowPriPaaSPool() throws Exception {
        // CREATE
        String poolId = getStringIdWithUserNamePrefix("-testpool4");

        // Create a pool with 3 Small VMs
        String POOL_VM_SIZE = "STANDARD_D1_V2";
        int POOL_VM_COUNT = 1;
        int POOL_LOW_PRI_VM_COUNT = 2;
        String POOL_OS_FAMILY = "4";
        String POOL_OS_VERSION = "*";

        // 10 minutes
        long POOL_STEADY_TIMEOUT_IN_Milliseconds = 10 * 60 * 1000;

        // Check if pool exists
        if (!batchClient.poolOperations().existsPool(poolId)) {
            // Use PaaS VM with Windows
            CloudServiceConfiguration configuration = new CloudServiceConfiguration();
            configuration.withOsFamily(POOL_OS_FAMILY).withOsVersion(POOL_OS_VERSION);

            batchClient.poolOperations().createPool(poolId, POOL_VM_SIZE, configuration, POOL_VM_COUNT,
                    POOL_LOW_PRI_VM_COUNT);
        }

        try {
            // GET
            Assert.assertTrue(batchClient.poolOperations().existsPool(poolId));

            long startTime = System.currentTimeMillis();
            long elapsedTime = 0L;

            // Wait for the VM to be allocated
            CloudPool pool = waitForPoolState(poolId, AllocationState.STEADY, POOL_STEADY_TIMEOUT_IN_Milliseconds);

            Assert.assertEquals(POOL_VM_COUNT, (long) pool.currentDedicatedNodes());
            Assert.assertEquals(POOL_LOW_PRI_VM_COUNT, (long) pool.currentLowPriorityNodes());

            // RESIZE
            batchClient.poolOperations().resizePool(poolId, null, 1);

            pool = batchClient.poolOperations().getPool(poolId);
            Assert.assertEquals(POOL_VM_COUNT, (long) pool.targetDedicatedNodes());
            Assert.assertEquals(1, (long) pool.targetLowPriorityNodes());

            // DELETE
            boolean deleted = false;
            batchClient.poolOperations().deletePool(poolId);
            // Wait for the VM to be allocated
            while (elapsedTime < POOL_STEADY_TIMEOUT_IN_Milliseconds * 2) {
                try {
                    batchClient.poolOperations().getPool(poolId);
                } catch (BatchErrorException err) {
                    if (err.body().code().equals(BatchErrorCodeStrings.PoolNotFound)) {
                        deleted = true;
                        break;
                    } else {
                        throw err;
                    }
                }

                System.out.println("wait 15 seconds for pool delete...");
                threadSleepInRecordMode(15 * 1000);
                elapsedTime = (new Date()).getTime() - startTime;
            }
            Assert.assertTrue(deleted);
        } finally {
            try {
                if (batchClient.poolOperations().existsPool(poolId)) {
                    batchClient.poolOperations().deletePool(poolId);
                }
            } catch (Exception e) {
                // Ignore exception
            }
        }
    }

    private static CloudPool waitForPoolState(String poolId, AllocationState targetState, long poolAllocationTimeoutInMilliseconds) throws IOException, InterruptedException {
        long startTime = System.currentTimeMillis();
        long elapsedTime = 0L;
        boolean allocationStateReached = false;
        CloudPool pool = null;

        // Wait for the VM to be allocated
        while (elapsedTime < poolAllocationTimeoutInMilliseconds) {
            pool = batchClient.poolOperations().getPool(poolId);
            Assert.assertNotNull(pool);

            if (pool.allocationState() == targetState) {
                allocationStateReached = true;
                break;
            }

            System.out.println("wait 30 seconds for pool allocationStateReached...");
            threadSleepInRecordMode(30 * 1000);
            elapsedTime = (new Date()).getTime() - startTime;
        }

        Assert.assertTrue("The pool did not reach a allocationStateReached state in the allotted time", allocationStateReached);
        return pool;
    }

    @Test
    public void canCRUDPaaSPool() throws Exception {
        // CREATE
        String poolId = getStringIdWithUserNamePrefix("-CRUDPaaS");

        // Create a pool with 3 Small VMs
        String POOL_VM_SIZE = "STANDARD_D1_V2";
        int POOL_VM_COUNT = 3;
        String POOL_OS_FAMILY = "4";
        String POOL_OS_VERSION = "*";
        // 15 minutes
        long POOL_STEADY_TIMEOUT_IN_Milliseconds = 15 * 60 * 1000;

        // Check if pool exists
        if (!batchClient.poolOperations().existsPool(poolId)) {
            // Use PaaS VM with Windows
            CloudServiceConfiguration configuration = new CloudServiceConfiguration();
            configuration.withOsFamily(POOL_OS_FAMILY).withOsVersion(POOL_OS_VERSION);

            List<UserAccount> userList = new ArrayList<>();
            userList.add(new UserAccount().withName("test-user-1").withPassword("kt#_gahr!@aGERDXA"));
            userList.add(new UserAccount().withName("test-user-2").withPassword("kt#_gahr!@aGERDXA")
                    .withElevationLevel(ElevationLevel.ADMIN));
            PoolAddParameter addParameter = new PoolAddParameter().withId(poolId)
                    .withTargetDedicatedNodes(POOL_VM_COUNT).withVmSize(POOL_VM_SIZE)
                    .withCloudServiceConfiguration(configuration).withUserAccounts(userList);
            batchClient.poolOperations().createPool(addParameter);
        }

        try {
            // GET
            Assert.assertTrue(batchClient.poolOperations().existsPool(poolId));

            long startTime = System.currentTimeMillis();
            long elapsedTime = 0L;

            // Wait for the VM to be allocated
            CloudPool pool = waitForPoolState(poolId, AllocationState.STEADY, POOL_STEADY_TIMEOUT_IN_Milliseconds);

            Assert.assertNotNull(pool.userAccounts());
            Assert.assertEquals("test-user-1", pool.userAccounts().get(0).name());
            Assert.assertEquals(ElevationLevel.NON_ADMIN, pool.userAccounts().get(0).elevationLevel());
            Assert.assertNull(pool.userAccounts().get(0).password());
            Assert.assertEquals(ElevationLevel.ADMIN, pool.userAccounts().get(1).elevationLevel());

            // LIST
            List<CloudPool> pools = batchClient.poolOperations().listPools();
            Assert.assertTrue(pools.size() > 0);

            boolean found = false;
            for (CloudPool p : pools) {
                if (p.id().equals(poolId)) {
                    found = true;
                    break;
                }
            }

            Assert.assertTrue(found);

            // CHECK POOL NODE COUNTS
            PoolNodeCounts poolNodeCount = null;
            List<PoolNodeCounts> poolNodeCounts = batchClient.accountOperations().listPoolNodeCounts();
            for (PoolNodeCounts tmp : poolNodeCounts) {
                if (tmp.poolId().equals(poolId)) {
                    poolNodeCount = tmp;
                    break;
                }
            }
            Assert.assertNotNull(poolNodeCount); // Single pool only
            Assert.assertNotNull(poolNodeCount.lowPriority());
            Assert.assertEquals(0, poolNodeCount.lowPriority().total());
            Assert.assertEquals(3, poolNodeCount.dedicated().total());

            // UPDATE
            LinkedList<MetadataItem> metadata = new LinkedList<>();
            metadata.add((new MetadataItem()).withName("key1").withValue("value1"));
            batchClient.poolOperations().patchPool(poolId, null, null, null, metadata);

            pool = batchClient.poolOperations().getPool(poolId);
            Assert.assertTrue(pool.metadata().size() == 1);
            Assert.assertTrue(pool.metadata().get(0).name().equals("key1"));

            batchClient.poolOperations().updatePoolProperties(poolId, null, new LinkedList<CertificateReference>(),
                    new LinkedList<ApplicationPackageReference>(), new LinkedList<MetadataItem>());
            pool = batchClient.poolOperations().getPool(poolId);
            Assert.assertNull(pool.metadata());

            // DELETE
            boolean deleted = false;
            batchClient.poolOperations().deletePool(poolId);
            // Wait for the VM to be allocated
            while (elapsedTime < POOL_STEADY_TIMEOUT_IN_Milliseconds) {
                try {
                    batchClient.poolOperations().getPool(poolId);
                } catch (BatchErrorException err) {
                    if (err.body().code().equals(BatchErrorCodeStrings.PoolNotFound)) {
                        deleted = true;
                        break;
                    } else {
                        throw err;
                    }
                }

                System.out.println("wait 5 seconds for pool delete...");
                threadSleepInRecordMode(5 * 1000);
                elapsedTime = (new Date()).getTime() - startTime;
            }
            Assert.assertTrue(deleted);
        } finally {
            try {
                if (batchClient.poolOperations().existsPool(poolId)) {
                    batchClient.poolOperations().deletePool(poolId);
                }
            } catch (Exception e) {
                // Ignore exception
            }
        }
    }
}
