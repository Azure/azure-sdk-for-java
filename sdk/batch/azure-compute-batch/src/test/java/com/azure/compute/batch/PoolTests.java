// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch;

import com.azure.compute.batch.models.*;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.test.TestMode;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;

public class PoolTests extends BatchClientTestBase {
    private static BatchPool livePool;
    private static String poolId;
    private static NetworkConfiguration networkConfiguration;

    @Override
    protected void beforeTest() {
        super.beforeTest();
        poolId = getStringIdWithUserNamePrefix("-testpool");
        if (getTestMode() == TestMode.RECORD) {
            if (livePool == null) {
                try {
                    livePool = createIfNotExistIaaSPool(poolId);
                } catch (Exception e) {
                    // TODO (NickKouds): Auto-generated catch block
                    e.printStackTrace();
                }
                Assertions.assertNotNull(livePool);
            }
        }

        // Need VNet to allow security to inject NSGs
        networkConfiguration = createNetworkConfiguration();
    }

    @Test
    public void testPoolOData() {

        // TODO (NickKouds): Looks to be an issue with Jackson desierlization of pool stats for PoolStatistics startTime and lastUpdateTime
        //        RequestOptions requestOptions = new RequestOptions();
        //        requestOptions.addQueryParam("$expand", "stats", false);
        //        poolClient.getWithResponse(poolId, requestOptions).getValue().toObject(BatchPool.class);

        // Temporarily Disabling the stats check, REST API doesn't provide the stats consistently for newly created pools
        // Will be enabled back soon.
        //        Assertions.assertNotNull(pool.stats());

        BatchPoolsListOptions selectOptions = new BatchPoolsListOptions();
        selectOptions.setSelect(Arrays.asList("id", "state"));
        PagedIterable<BatchPool> pools = batchClient.listPools(selectOptions);
        Assertions.assertNotNull(pools);
        BatchPool pool = null;

        for (BatchPool batchPool : pools) {
            if (batchPool.getId().equals(poolId)) {
                pool = batchPool;
            }
        }

        Assertions.assertNotNull(pool, String.format("Pool with ID %s was not found in list response", poolId));
        Assertions.assertNotNull(pool.getId());
        Assertions.assertNotNull(pool.getState());
        Assertions.assertNull(pool.getVmSize());

        // When tests are being ran in parallel, there may be a previous pool delete still in progress

        BatchPoolsListOptions filterOptions = new BatchPoolsListOptions();
        filterOptions.setFilter("state eq 'deleting'");
        pools = batchClient.listPools(filterOptions);
        Assertions.assertNotNull(pools);

    }

    @Test
    public void canCreateDataDisk() {
        String poolId = getStringIdWithUserNamePrefix("-testpool3");

        // Create a pool with 0 Small VMs
        String poolVmSize = "STANDARD_D1_V2";
        int poolVmCount = 0;
        int lun = 50;
        int diskSizeGB = 50;

        // Use IaaS VM with Linux
        List<DataDisk> dataDisks = new ArrayList<DataDisk>();
        dataDisks.add(new DataDisk(lun, diskSizeGB));

        BatchVmImageReference imgRef = new BatchVmImageReference().setPublisher("Canonical")
            .setOffer("UbuntuServer")
            .setSku("18.04-LTS")
            .setVersion("latest");

        VirtualMachineConfiguration configuration = new VirtualMachineConfiguration(imgRef, "batch.node.ubuntu 18.04");
        configuration.setDataDisks(dataDisks);

        BatchPoolCreateContent poolToCreate = new BatchPoolCreateContent(poolId, poolVmSize);
        poolToCreate.setNetworkConfiguration(networkConfiguration)
            .setTargetDedicatedNodes(poolVmCount)
            .setVirtualMachineConfiguration(configuration);

        try {
            batchClient.createPool(poolToCreate);

            BatchPool pool = batchClient.getPool(poolId);
            Assertions.assertEquals(lun,
                pool.getVirtualMachineConfiguration().getDataDisks().get(0).getLogicalUnitNumber());
            Assertions.assertEquals(diskSizeGB,
                pool.getVirtualMachineConfiguration().getDataDisks().get(0).getDiskSizeGb());
        } finally {
            try {
                if (poolExists(batchClient, poolId)) {
                    batchClient.deletePool(poolId);
                }
            } catch (Exception e) {
                // Ignore exception
            }
        }
    }

    @Test
    public void canCRUDLowPriIaaSPool() {
        // CREATE
        String poolId = getStringIdWithUserNamePrefix("-canCRUDLowPri-testPool");

        // Create a pool with 3 Small VMs
        String poolVmSize = "STANDARD_D1_V2";
        int poolVmCount = 2;
        int poolLowPriVmCount = 2;

        // 10 minutes
        long poolSteadyTimeoutInMilliseconds = 10 * 60 * 1000;

        // Check if pool exists
        if (!poolExists(batchClient, poolId)) {
            BatchVmImageReference imgRef = new BatchVmImageReference().setPublisher("Canonical")
                .setOffer("UbuntuServer")
                .setSku("18.04-LTS")
                .setVersion("latest");

            VirtualMachineConfiguration configuration
                = new VirtualMachineConfiguration(imgRef, "batch.node.ubuntu 18.04");

            NetworkConfiguration netConfig = createNetworkConfiguration();
            List<BatchInboundNatPool> inbounds = new ArrayList<>();
            inbounds.add(new BatchInboundNatPool("testinbound", InboundEndpointProtocol.TCP, 5000, 60000, 60040));
            inbounds.add(new BatchInboundNatPool("SSHRule", InboundEndpointProtocol.TCP, 22, 60100, 60140));

            BatchPoolEndpointConfiguration endpointConfig = new BatchPoolEndpointConfiguration(inbounds);
            netConfig.setEndpointConfiguration(endpointConfig);

            BatchPoolCreateContent poolToCreate = new BatchPoolCreateContent(poolId, poolVmSize);
            poolToCreate.setTargetDedicatedNodes(poolVmCount)
                .setTargetLowPriorityNodes(poolLowPriVmCount)
                .setVirtualMachineConfiguration(configuration)
                .setNetworkConfiguration(netConfig)
                .setTargetNodeCommunicationMode(BatchNodeCommunicationMode.DEFAULT);

            batchClient.createPool(poolToCreate);
        }

        try {
            // GET
            Assertions.assertTrue(poolExists(batchClient, poolId));

            long startTime = System.currentTimeMillis();
            long elapsedTime = 0L;

            // Wait for the VM to be allocated
            BatchPool pool = waitForPoolState(poolId, AllocationState.STEADY, poolSteadyTimeoutInMilliseconds);

            Assertions.assertEquals(poolVmCount, (long) pool.getCurrentDedicatedNodes());
            Assertions.assertEquals(poolLowPriVmCount, (long) pool.getCurrentLowPriorityNodes());
            Assertions.assertNotNull(pool.getCurrentNodeCommunicationMode(),
                "CurrentNodeCommunicationMode should be defined for pool with more than one target dedicated node");
            Assertions.assertEquals(BatchNodeCommunicationMode.DEFAULT, pool.getTargetNodeCommunicationMode());

            PagedIterable<BatchNode> nodeListIterator = batchClient.listNodes(poolId);
            List<BatchNode> computeNodes = new ArrayList<BatchNode>();

            for (BatchNode node : nodeListIterator) {
                computeNodes.add(node);
            }

            List<InboundEndpoint> inboundEndpoints
                = computeNodes.get(0).getEndpointConfiguration().getInboundEndpoints();
            Assertions.assertEquals(2, inboundEndpoints.size());
            InboundEndpoint inboundEndpoint = inboundEndpoints.get(0);
            Assertions.assertEquals(5000, inboundEndpoint.getBackendPort());
            Assertions.assertTrue(inboundEndpoint.getFrontendPort() >= 60000);
            Assertions.assertTrue(inboundEndpoint.getFrontendPort() <= 60040);
            Assertions.assertTrue(inboundEndpoint.getName().startsWith("testinbound."));
            Assertions.assertTrue(inboundEndpoints.get(1).getName().startsWith("SSHRule"));

            // CHECK POOL NODE COUNTS
            BatchPoolNodeCounts poolNodeCount = null;
            PagedIterable<BatchPoolNodeCounts> poolNodeCountIterator = batchClient.listPoolNodeCounts();

            for (BatchPoolNodeCounts tmp : poolNodeCountIterator) {
                if (tmp.getPoolId().equals(poolId)) {
                    poolNodeCount = tmp;
                    break;
                }
            }
            Assertions.assertNotNull(poolNodeCount); // Single pool only
            Assertions.assertNotNull(poolNodeCount.getLowPriority());

            Assertions.assertEquals(poolLowPriVmCount, poolNodeCount.getLowPriority().getTotal());
            Assertions.assertEquals(poolVmCount, poolNodeCount.getDedicated().getTotal());

            // Update NodeCommunicationMode to Simplified

            BatchPoolUpdateContent poolUpdateContent = new BatchPoolUpdateContent();
            poolUpdateContent.setApplicationPackageReferences(new LinkedList<BatchApplicationPackageReference>())
                .setMetadata(new LinkedList<BatchMetadataItem>());

            poolUpdateContent.setTargetNodeCommunicationMode(BatchNodeCommunicationMode.SIMPLIFIED);

            batchClient.updatePool(poolId, poolUpdateContent);

            pool = batchClient.getPool(poolId);
            Assertions.assertNotNull(pool.getCurrentNodeCommunicationMode(),
                "CurrentNodeCommunicationMode should be defined for pool with more than one target dedicated node");
            Assertions.assertEquals(BatchNodeCommunicationMode.SIMPLIFIED, pool.getTargetNodeCommunicationMode());

            // Patch NodeCommunicationMode to Classic

            BatchPoolUpdateContent poolUpdateContent2 = new BatchPoolUpdateContent();
            poolUpdateContent2.setTargetNodeCommunicationMode(BatchNodeCommunicationMode.CLASSIC);
            batchClient.updatePool(poolId, poolUpdateContent2);

            pool = batchClient.getPool(poolId);
            Assertions.assertNotNull(pool.getCurrentNodeCommunicationMode(),
                "CurrentNodeCommunicationMode should be defined for pool with more than one target dedicated node");
            Assertions.assertEquals(BatchNodeCommunicationMode.CLASSIC, pool.getTargetNodeCommunicationMode());

            // RESIZE
            batchClient.resizePool(poolId,
                new BatchPoolResizeContent().setTargetDedicatedNodes(1).setTargetLowPriorityNodes(1));

            pool = batchClient.getPool(poolId);
            Assertions.assertEquals(1, (long) pool.getTargetDedicatedNodes());
            Assertions.assertEquals(1, (long) pool.getTargetLowPriorityNodes());

            // DELETE
            boolean deleted = false;
            batchClient.deletePool(poolId);

            // Wait for the VM to be deallocated
            while (elapsedTime < poolSteadyTimeoutInMilliseconds) {
                try {
                    batchClient.getPool(poolId);
                } catch (Exception err) {
                    if (!err.getMessage().contains("Status code 404")) {
                        throw err;
                    }
                    deleted = true;
                    break;
                }

                System.out.println("wait 15 seconds for pool delete...");
                sleepIfRunningAgainstService(15 * 1000);
                elapsedTime = (new Date()).getTime() - startTime;
            }
            Assertions.assertTrue(deleted);

        } finally {
            try {
                if (poolExists(batchClient, poolId)) {
                    batchClient.deletePool(poolId);
                }
            } catch (Exception e) {
                // Ignore exception
            }
        }
    }

    @Test
    public void testDeserializationOfBatchPoolResourceStatistics() {
        // Simulated JSON response with numbers as strings
        String jsonResponse = "{" + "\"startTime\":\"2022-01-01T00:00:00Z\","
            + "\"lastUpdateTime\":\"2022-01-01T01:00:00Z\"," + "\"avgCPUPercentage\":50.5," + "\"avgMemoryGiB\":2.5,"
            + "\"peakMemoryGiB\":3.0," + "\"avgDiskGiB\":1.5," + "\"peakDiskGiB\":2.0," + "\"diskReadIOps\":\"1000\","
            + "\"diskWriteIOps\":\"500\"," + "\"diskReadGiB\":0.5," + "\"diskWriteGiB\":0.25,"
            + "\"networkReadGiB\":1.0," + "\"networkWriteGiB\":0.75" + "}";

        // Deserialize JSON response using JsonReader from JsonProviders
        try (JsonReader jsonReader = JsonProviders.createReader(new StringReader(jsonResponse))) {
            BatchPoolResourceStatistics stats = BatchPoolResourceStatistics.fromJson(jsonReader);

            // Assertions
            Assertions.assertNotNull(stats);
            Assertions.assertEquals(OffsetDateTime.parse("2022-01-01T00:00:00Z"), stats.getStartTime());
            Assertions.assertEquals(OffsetDateTime.parse("2022-01-01T01:00:00Z"), stats.getLastUpdateTime());
            Assertions.assertEquals(50.5, stats.getAvgCpuPercentage());
            Assertions.assertEquals(2.5, stats.getAvgMemoryGiB());
            Assertions.assertEquals(3.0, stats.getPeakMemoryGiB());
            Assertions.assertEquals(1.5, stats.getAvgDiskGiB());
            Assertions.assertEquals(2.0, stats.getPeakDiskGiB());
            Assertions.assertEquals(1000, stats.getDiskReadIops());
            Assertions.assertEquals(500, stats.getDiskWriteIops());
            Assertions.assertEquals(0.5, stats.getDiskReadGiB());
            Assertions.assertEquals(0.25, stats.getDiskWriteGiB());
            Assertions.assertEquals(1.0, stats.getNetworkReadGiB());
            Assertions.assertEquals(0.75, stats.getNetworkWriteGiB());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void canCreatePoolWithConfidentialVM() throws Exception {
        String poolId = getStringIdWithUserNamePrefix("ConfidentialVMPool");

        if (!poolExists(batchClient, poolId)) {
            // Define the image reference
            BatchVmImageReference imageReference = new BatchVmImageReference().setPublisher("microsoftwindowsserver")
                .setOffer("windowsserver")
                .setSku("2022-datacenter-smalldisk-g2");

            // Set the security profile for the Confidential VM
            SecurityProfile securityProfile = new SecurityProfile(true, SecurityTypes.CONFIDENTIAL_VM,
                new BatchUefiSettings().setSecureBootEnabled(true).setVTpmEnabled(true));

            // Set the VM disk security profile
            VmDiskSecurityProfile diskSecurityProfile
                = new VmDiskSecurityProfile().setSecurityEncryptionType(SecurityEncryptionTypes.VMGUEST_STATE_ONLY);

            ManagedDisk managedDisk = new ManagedDisk().setSecurityProfile(diskSecurityProfile);

            // Set the OS disk configuration
            BatchOsDisk osDisk = new BatchOsDisk().setManagedDisk(managedDisk);

            // Define the virtual machine configuration
            VirtualMachineConfiguration vmConfiguration
                = new VirtualMachineConfiguration(imageReference, "batch.node.windows amd64")
                    .setSecurityProfile(securityProfile)
                    .setOsDisk(osDisk);

            // Create the pool
            BatchPoolCreateContent poolCreateContent
                = new BatchPoolCreateContent(poolId, "STANDARD_D2S_V3").setVirtualMachineConfiguration(vmConfiguration)
                    .setTargetDedicatedNodes(0);

            batchClient.createPool(poolCreateContent);
        }

        try {
            BatchPool pool = batchClient.getPool(poolId);
            Assertions.assertNotNull(pool);

            SecurityProfile sp = pool.getVirtualMachineConfiguration().getSecurityProfile();
            Assertions
                .assertTrue(SecurityTypes.CONFIDENTIAL_VM.toString().equalsIgnoreCase(sp.getSecurityType().toString()));
            Assertions.assertTrue(sp.isEncryptionAtHost());
            Assertions.assertTrue(sp.getUefiSettings().isSecureBootEnabled());
            Assertions.assertTrue(sp.getUefiSettings().isVTpmEnabled());

            BatchOsDisk disk = pool.getVirtualMachineConfiguration().getOsDisk();
            Assertions.assertEquals(SecurityEncryptionTypes.VMGUEST_STATE_ONLY,
                disk.getManagedDisk().getSecurityProfile().getSecurityEncryptionType());
        } finally {
            // Clean up by deleting the pool
            try {
                if (poolExists(batchClient, poolId)) {
                    batchClient.deletePool(poolId);
                }
            } catch (Exception e) {
                // Ignore exception
            }
        }
    }

    @Test
    public void canDeallocateAndStartComputeNode() throws Exception {
        String poolId = getStringIdWithUserNamePrefix("-deallocateStartNodePool");

        // Define the VM size and node count
        String poolVmSize = "STANDARD_D1_V2";
        int poolVmCount = 1;

        // Check if the pool exists, if not, create it
        if (!poolExists(batchClient, poolId)) {
            BatchVmImageReference imgRef = new BatchVmImageReference().setPublisher("Canonical")
                .setOffer("UbuntuServer")
                .setSku("18.04-LTS")
                .setVersion("latest");

            VirtualMachineConfiguration vmConfiguration
                = new VirtualMachineConfiguration(imgRef, "batch.node.ubuntu 18.04");

            BatchPoolCreateContent poolCreateContent
                = new BatchPoolCreateContent(poolId, poolVmSize).setTargetDedicatedNodes(poolVmCount)
                    .setVirtualMachineConfiguration(vmConfiguration);

            batchClient.createPool(poolCreateContent);
        }

        try {
            // Wait for the pool to become steady and nodes to become idle
            BatchPool pool = waitForPoolState(poolId, AllocationState.STEADY, 15 * 60 * 1000);
            Assertions.assertNotNull(pool);
            Assertions.assertEquals(AllocationState.STEADY, pool.getAllocationState());

            // Retrieve the nodes using PagedIterable
            PagedIterable<BatchNode> nodesPaged = batchClient.listNodes(poolId);
            BatchNode firstNode = null;
            for (BatchNode node : nodesPaged) {
                firstNode = node;  // Get the first node
                break;
            }

            Assertions.assertNotNull(firstNode); // Assert there is at least one compute node
            String nodeId = firstNode.getId();
            BatchNode computeNode = batchClient.getNode(poolId, nodeId);

            // Deallocate the node using the compute node operations
            BatchNodeDeallocateContent deallocateContent
                = new BatchNodeDeallocateContent().setNodeDeallocateOption(BatchNodeDeallocateOption.TERMINATE);
            BatchNodeDeallocateOptions options = new BatchNodeDeallocateOptions();
            options.setTimeOutInSeconds(Duration.ofSeconds(30));
            options.setParameters(deallocateContent);
            batchClient.deallocateNode(poolId, nodeId, options);

            // Wait for the node to be deallocated
            boolean isDeallocated = false;
            while (!isDeallocated) {
                computeNode = batchClient.getNode(poolId, nodeId);
                if (computeNode.getState().equals(BatchNodeState.DEALLOCATED)) {
                    isDeallocated = true;
                } else {
                    sleepIfRunningAgainstService(15 * 1000);
                }
            }
            Assertions.assertEquals(BatchNodeState.DEALLOCATED, computeNode.getState());

            // Start the node again
            batchClient.startNode(poolId, nodeId);

            // Wait for the node to become idle again
            boolean isIdle = false;
            while (!isIdle) {
                computeNode = batchClient.getNode(poolId, nodeId);
                if (computeNode.getState().equals(BatchNodeState.IDLE)) {
                    isIdle = true;
                } else {
                    sleepIfRunningAgainstService(15 * 1000);
                }
            }
            Assertions.assertEquals(BatchNodeState.IDLE, computeNode.getState());

        } finally {
            // Clean up by deleting the pool
            try {
                if (poolExists(batchClient, poolId)) {
                    batchClient.deletePool(poolId);
                }
            } catch (Exception e) {
                // Ignore exception
            }
        }
    }

}
