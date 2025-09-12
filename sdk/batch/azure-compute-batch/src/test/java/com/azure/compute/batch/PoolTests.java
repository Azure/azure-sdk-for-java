// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch;

import com.azure.compute.batch.models.*;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.test.SyncAsyncExtension;
import com.azure.core.test.annotation.SyncAsyncTest;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;

import reactor.core.publisher.Mono;

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
        if (livePool == null) {
            try {
                livePool = createIfNotExistIaaSPool(poolId);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Assertions.assertNotNull(livePool);
        }

        // Need VNet to allow security to inject NSGs
        networkConfiguration = createNetworkConfiguration();
    }

    @SyncAsyncTest
    public void testPoolOData() {
        // SELECT subset of fields
        BatchPoolsListOptions selectOptions = new BatchPoolsListOptions().setSelect(Arrays.asList("id", "state"));

        Iterable<BatchPool> pools = SyncAsyncExtension.execute(() -> batchClient.listPools(selectOptions),
            () -> Mono.fromCallable(() -> batchAsyncClient.listPools(selectOptions).toIterable()));

        Assertions.assertNotNull(pools);

        BatchPool foundPool = null;
        for (BatchPool pool : pools) {
            if (pool.getId().equals(poolId)) {
                foundPool = pool;
            }
        }

        Assertions.assertNotNull(foundPool, String.format("Pool with ID %s was not found in list response", poolId));
        Assertions.assertNotNull(foundPool.getId());
        Assertions.assertNotNull(foundPool.getState());
        Assertions.assertNull(foundPool.getVmSize()); // Because we selected only "id" and "state"

        // FILTER by state
        BatchPoolsListOptions filterOptions = new BatchPoolsListOptions().setFilter("state eq 'deleting'");

        pools = SyncAsyncExtension.execute(() -> batchClient.listPools(filterOptions),
            () -> Mono.fromCallable(() -> batchAsyncClient.listPools(filterOptions).toIterable()));

        Assertions.assertNotNull(pools);
    }

    @SyncAsyncTest
    public void canCreateDataDisk() {
        String testModeSuffix = SyncAsyncExtension.execute(() -> "sync", () -> Mono.just("async"));
        String poolId = getStringIdWithUserNamePrefix("-testpool3" + testModeSuffix);

        // Create a pool with 0 Small VMs
        String poolVmSize = "STANDARD_D1_V2";
        int poolVmCount = 0;
        int lun = 50;
        int diskSizeGB = 50;

        // Use IaaS VM with Linux
        List<DataDisk> dataDisks = new ArrayList<DataDisk>();
        dataDisks.add(new DataDisk(lun, diskSizeGB));

        BatchVmImageReference imgRef = new BatchVmImageReference().setPublisher("microsoftwindowsserver")
            .setOffer("windowsserver")
            .setSku("2022-datacenter-smalldisk");

        VirtualMachineConfiguration configuration = new VirtualMachineConfiguration(imgRef, "batch.node.windows amd64");
        configuration.setDataDisks(dataDisks);

        BatchPoolCreateParameters poolToCreate = new BatchPoolCreateParameters(poolId, poolVmSize);
        poolToCreate.setNetworkConfiguration(networkConfiguration)
            .setTargetDedicatedNodes(poolVmCount)
            .setVirtualMachineConfiguration(configuration);

        try {
            SyncAsyncExtension.execute(() -> batchClient.createPool(poolToCreate),
                () -> batchAsyncClient.createPool(poolToCreate));

            BatchPool pool
                = SyncAsyncExtension.execute(() -> batchClient.getPool(poolId), () -> batchAsyncClient.getPool(poolId));
            Assertions.assertEquals(lun,
                pool.getVirtualMachineConfiguration().getDataDisks().get(0).getLogicalUnitNumber());
            Assertions.assertEquals(diskSizeGB,
                pool.getVirtualMachineConfiguration().getDataDisks().get(0).getDiskSizeGb());
        } finally {
            // DELETE
            try {
                SyncPoller<BatchPool, Void> deletePoller = setPlaybackSyncPollerPollInterval(
                    SyncAsyncExtension.execute(() -> batchClient.beginDeletePool(poolId),
                        () -> Mono.fromCallable(() -> batchAsyncClient.beginDeletePool(poolId).getSyncPoller())));

                deletePoller.waitForCompletion();
            } catch (Exception e) {
                System.err.println("Cleanup failed for pool: " + poolId);
                e.printStackTrace();
            }
        }
    }

    @SyncAsyncTest
    public void canCRUDLowPriIaaSPool() {
        String testModeSuffix = SyncAsyncExtension.execute(() -> "sync", () -> Mono.just("async"));
        // CREATE
        String poolId = getStringIdWithUserNamePrefix("-canCRUDLowPri-testPool" + testModeSuffix);

        // Create a pool with 3 Small VMs
        String poolVmSize = "STANDARD_D1_V2";
        int poolVmCount = 2;
        int poolLowPriVmCount = 2;

        // 10 minutes
        long poolSteadyTimeoutInMilliseconds = 10 * 60 * 1000;

        // Check if pool exists
        boolean exists = SyncAsyncExtension.execute(() -> poolExists(batchClient, poolId),
            () -> poolExists(batchAsyncClient, poolId));
        if (!exists) {
            BatchVmImageReference imgRef = new BatchVmImageReference().setPublisher("microsoftwindowsserver")
                .setOffer("windowsserver")
                .setSku("2022-datacenter-smalldisk");

            VirtualMachineConfiguration configuration
                = new VirtualMachineConfiguration(imgRef, "batch.node.windows amd64");

            NetworkConfiguration netConfig = createNetworkConfiguration();
            List<BatchInboundNatPool> inbounds = new ArrayList<>();
            inbounds.add(new BatchInboundNatPool("testinbound", InboundEndpointProtocol.TCP, 5000, 60000, 60040));
            inbounds.add(new BatchInboundNatPool("SSHRule", InboundEndpointProtocol.TCP, 22, 60100, 60140));

            BatchPoolEndpointConfiguration endpointConfig = new BatchPoolEndpointConfiguration(inbounds);
            netConfig.setEndpointConfiguration(endpointConfig);

            BatchPoolCreateParameters poolToCreate = new BatchPoolCreateParameters(poolId, poolVmSize);
            poolToCreate.setTargetDedicatedNodes(poolVmCount)
                .setTargetLowPriorityNodes(poolLowPriVmCount)
                .setVirtualMachineConfiguration(configuration)
                .setNetworkConfiguration(netConfig)
                .setTargetNodeCommunicationMode(BatchNodeCommunicationMode.DEFAULT);

            SyncAsyncExtension.execute(() -> batchClient.createPool(poolToCreate),
                () -> batchAsyncClient.createPool(poolToCreate));
        }

        try {
            // GET
            boolean poolExists = SyncAsyncExtension.execute(() -> poolExists(batchClient, poolId),
                () -> poolExists(batchAsyncClient, poolId));
            Assertions.assertTrue(poolExists, "Pool should exist after creation");

            // Wait for the VM to be allocated
            BatchPool pool = SyncAsyncExtension.execute(
                () -> waitForPoolState(poolId, AllocationState.STEADY, poolSteadyTimeoutInMilliseconds),
                () -> Mono.fromCallable(
                    () -> waitForPoolStateAsync(poolId, AllocationState.STEADY, poolSteadyTimeoutInMilliseconds)));

            Assertions.assertEquals(poolVmCount, (long) pool.getCurrentDedicatedNodes());
            Assertions.assertEquals(poolLowPriVmCount, (long) pool.getCurrentLowPriorityNodes());
            Assertions.assertNotNull(pool.getCurrentNodeCommunicationMode(),
                "CurrentNodeCommunicationMode should be defined for pool with more than one target dedicated node");
            Assertions.assertEquals(BatchNodeCommunicationMode.DEFAULT, pool.getTargetNodeCommunicationMode());

            Iterable<BatchNode> nodeListIterator = SyncAsyncExtension.execute(() -> batchClient.listNodes(poolId),
                () -> Mono.fromCallable(() -> batchAsyncClient.listNodes(poolId).toIterable()));
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
            Iterable<BatchPoolNodeCounts> poolNodeCountIterator
                = SyncAsyncExtension.execute(() -> batchClient.listPoolNodeCounts(),
                    () -> Mono.fromCallable(() -> batchAsyncClient.listPoolNodeCounts().toIterable()));

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

            BatchPoolUpdateParameters poolUpdateParameters = new BatchPoolUpdateParameters();
            poolUpdateParameters.setApplicationPackageReferences(new LinkedList<BatchApplicationPackageReference>())
                .setMetadata(new LinkedList<BatchMetadataItem>());

            poolUpdateParameters.setTargetNodeCommunicationMode(BatchNodeCommunicationMode.SIMPLIFIED);

            SyncAsyncExtension.execute(() -> batchClient.updatePool(poolId, poolUpdateParameters),
                () -> batchAsyncClient.updatePool(poolId, poolUpdateParameters));

            pool = SyncAsyncExtension.execute(() -> batchClient.getPool(poolId),
                () -> batchAsyncClient.getPool(poolId));
            Assertions.assertNotNull(pool.getCurrentNodeCommunicationMode(),
                "CurrentNodeCommunicationMode should be defined for pool with more than one target dedicated node");
            Assertions.assertEquals(BatchNodeCommunicationMode.SIMPLIFIED, pool.getTargetNodeCommunicationMode());

            // Patch NodeCommunicationMode to Classic

            BatchPoolUpdateParameters poolUpdateParameters2 = new BatchPoolUpdateParameters();
            poolUpdateParameters2.setTargetNodeCommunicationMode(BatchNodeCommunicationMode.CLASSIC);
            SyncAsyncExtension.execute(() -> batchClient.updatePool(poolId, poolUpdateParameters2),
                () -> batchAsyncClient.updatePool(poolId, poolUpdateParameters2));

            pool = SyncAsyncExtension.execute(() -> batchClient.getPool(poolId),
                () -> batchAsyncClient.getPool(poolId));
            Assertions.assertNotNull(pool.getCurrentNodeCommunicationMode(),
                "CurrentNodeCommunicationMode should be defined for pool with more than one target dedicated node");
            Assertions.assertEquals(BatchNodeCommunicationMode.CLASSIC, pool.getTargetNodeCommunicationMode());

            // RESIZE
            BatchPoolResizeParameters resizeParameters
                = new BatchPoolResizeParameters().setTargetDedicatedNodes(1).setTargetLowPriorityNodes(1);

            SyncPoller<BatchPool, BatchPool> resizePoller = setPlaybackSyncPollerPollInterval(
                SyncAsyncExtension.execute(() -> batchClient.beginResizePool(poolId, resizeParameters), () -> Mono
                    .fromCallable(() -> batchAsyncClient.beginResizePool(poolId, resizeParameters).getSyncPoller())));

            // Inspect first poll
            PollResponse<BatchPool> resizeFirst = resizePoller.poll();
            if (resizeFirst.getStatus() == LongRunningOperationStatus.IN_PROGRESS) {
                BatchPool poolDuringResize = resizeFirst.getValue();
                Assertions.assertNotNull(poolDuringResize);
                Assertions.assertEquals(AllocationState.RESIZING, poolDuringResize.getAllocationState());
            }

            // Wait for completion
            resizePoller.waitForCompletion();

            // Final pool after resize
            BatchPool resizedPool = resizePoller.getFinalResult();
            Assertions.assertNotNull(resizedPool);
            Assertions.assertEquals(AllocationState.STEADY, resizedPool.getAllocationState());
            Assertions.assertEquals(1, (long) resizedPool.getTargetDedicatedNodes());
            Assertions.assertEquals(1, (long) resizedPool.getTargetLowPriorityNodes());

            // DELETE using LRO
            SyncPoller<BatchPool, Void> poller = setPlaybackSyncPollerPollInterval(
                SyncAsyncExtension.execute(() -> batchClient.beginDeletePool(poolId),
                    () -> Mono.fromCallable(() -> batchAsyncClient.beginDeletePool(poolId).getSyncPoller())));

            // Validate initial poll result (pool should be in DELETING state)
            PollResponse<BatchPool> initialResponse = poller.poll();
            if (initialResponse.getStatus() == LongRunningOperationStatus.IN_PROGRESS) {
                BatchPool poolDuringPoll = initialResponse.getValue();
                Assertions.assertNotNull(poolDuringPoll, "Expected pool data during polling");
                Assertions.assertEquals(poolId, poolDuringPoll.getId());
                Assertions.assertEquals(BatchPoolState.DELETING, poolDuringPoll.getState());
            }

            // Wait for LRO to finish
            poller.waitForCompletion();

            // Final result should be null after successful deletion
            PollResponse<BatchPool> finalResponse = poller.poll();
            Assertions.assertNull(finalResponse.getValue(),
                "Expected final result to be null after successful deletion");

        } finally {
            // Confirm pool is no longer retrievable
            try {
                SyncAsyncExtension.execute(() -> batchClient.getPool(poolId), () -> batchAsyncClient.getPool(poolId));
                Assertions.fail("Expected pool to be deleted.");
            } catch (HttpResponseException ex) {
                Assertions.assertEquals(404, ex.getResponse().getStatusCode());
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

    @SyncAsyncTest
    public void canCreatePoolWithConfidentialVM() throws Exception {
        String testModeSuffix = SyncAsyncExtension.execute(() -> "sync", () -> Mono.just("async"));
        String poolId = getStringIdWithUserNamePrefix("ConfidentialVMPool" + testModeSuffix);

        boolean exists = SyncAsyncExtension.execute(() -> poolExists(batchClient, poolId),
            () -> poolExists(batchAsyncClient, poolId));
        if (!exists) {
            // Define the image reference
            BatchVmImageReference imageReference = new BatchVmImageReference().setPublisher("microsoftwindowsserver")
                .setOffer("windowsserver")
                .setSku("2022-datacenter-smalldisk-g2");

            // Set the security profile for the Confidential VM
            SecurityProfile securityProfile = new SecurityProfile();
            securityProfile.setEncryptionAtHost(true);
            securityProfile.setSecurityType(SecurityTypes.CONFIDENTIAL_VM);
            securityProfile.setUefiSettings(new BatchUefiSettings().setSecureBootEnabled(true).setVTpmEnabled(true));

            // Set the VM disk security profile
            BatchVmDiskSecurityProfile diskSecurityProfile = new BatchVmDiskSecurityProfile()
                .setSecurityEncryptionType(SecurityEncryptionTypes.VMGUEST_STATE_ONLY);

            ManagedDisk managedDisk = new ManagedDisk().setSecurityProfile(diskSecurityProfile);

            // Set the OS disk configuration
            BatchOsDisk osDisk = new BatchOsDisk().setManagedDisk(managedDisk);

            // Define the virtual machine configuration
            VirtualMachineConfiguration vmConfiguration
                = new VirtualMachineConfiguration(imageReference, "batch.node.windows amd64")
                    .setSecurityProfile(securityProfile)
                    .setOsDisk(osDisk);

            // Create the pool
            BatchPoolCreateParameters poolCreateParameters = new BatchPoolCreateParameters(poolId, "STANDARD_D2S_V3")
                .setVirtualMachineConfiguration(vmConfiguration)
                .setTargetDedicatedNodes(0);

            SyncAsyncExtension.execute(() -> batchClient.createPool(poolCreateParameters),
                () -> batchAsyncClient.createPool(poolCreateParameters));
        }

        try {
            BatchPool pool
                = SyncAsyncExtension.execute(() -> batchClient.getPool(poolId), () -> batchAsyncClient.getPool(poolId));
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
            // DELETE
            try {
                SyncPoller<BatchPool, Void> deletePoller = setPlaybackSyncPollerPollInterval(
                    SyncAsyncExtension.execute(() -> batchClient.beginDeletePool(poolId),
                        () -> Mono.fromCallable(() -> batchAsyncClient.beginDeletePool(poolId).getSyncPoller())));

                deletePoller.waitForCompletion();
            } catch (Exception e) {
                System.err.println("Cleanup failed for pool: " + poolId);
                e.printStackTrace();
            }
        }
    }

    @SyncAsyncTest
    public void canDeallocateAndStartComputeNode() throws Exception {
        String testModeSuffix = SyncAsyncExtension.execute(() -> "sync", () -> Mono.just("async"));
        String poolId = getStringIdWithUserNamePrefix("-deallocateStartNodePool" + testModeSuffix);
        // Define the VM size and node count
        String poolVmSize = "STANDARD_D2s_V3";
        int poolVmCount = 1;

        // Check if the pool exists, if not, create it
        boolean exists = SyncAsyncExtension.execute(() -> poolExists(batchClient, poolId),
            () -> poolExists(batchAsyncClient, poolId));
        if (!exists) {
            BatchVmImageReference imgRef = new BatchVmImageReference().setPublisher("microsoftwindowsserver")
                .setOffer("windowsserver")
                .setSku("2022-datacenter-smalldisk");

            VirtualMachineConfiguration vmConfiguration
                = new VirtualMachineConfiguration(imgRef, "batch.node.windows amd64");

            BatchPoolCreateParameters poolCreateParameters
                = new BatchPoolCreateParameters(poolId, poolVmSize).setTargetDedicatedNodes(poolVmCount)
                    .setVirtualMachineConfiguration(vmConfiguration);

            SyncAsyncExtension.execute(() -> batchClient.createPool(poolCreateParameters),
                () -> batchAsyncClient.createPool(poolCreateParameters));
        }

        try {
            // Wait for the pool to become steady and nodes to become idle
            BatchPool pool = SyncAsyncExtension.execute(
                () -> waitForPoolState(poolId, AllocationState.STEADY, 15 * 60 * 1000),
                () -> Mono.fromCallable(() -> waitForPoolStateAsync(poolId, AllocationState.STEADY, 15 * 60 * 1000)));

            Assertions.assertNotNull(pool);
            Assertions.assertEquals(AllocationState.STEADY, pool.getAllocationState());

            // Retrieve the nodes
            Iterable<BatchNode> nodesPaged = SyncAsyncExtension.execute(() -> batchClient.listNodes(poolId),
                () -> Mono.fromCallable(() -> batchAsyncClient.listNodes(poolId).toIterable()));

            BatchNode firstNode = null;
            for (BatchNode node : nodesPaged) {
                firstNode = node;  // Get the first node
                break;
            }
            Assertions.assertNotNull(firstNode, "Expected at least one compute node in pool");

            String nodeId = firstNode.getId();

            sleepIfRunningAgainstService(15000);

            // DEALLOCATE using LRO
            BatchNodeDeallocateParameters deallocateParams
                = new BatchNodeDeallocateParameters().setNodeDeallocateOption(BatchNodeDeallocateOption.TERMINATE);

            BatchNodeDeallocateOptions deallocateOptions
                = new BatchNodeDeallocateOptions().setTimeOutInSeconds(Duration.ofSeconds(30))
                    .setParameters(deallocateParams);

            SyncPoller<BatchNode, BatchNode> deallocatePoller = setPlaybackSyncPollerPollInterval(
                SyncAsyncExtension.execute(() -> batchClient.beginDeallocateNode(poolId, nodeId, deallocateOptions),
                    () -> Mono
                        .fromCallable(() -> batchAsyncClient.beginDeallocateNode(poolId, nodeId, deallocateOptions)
                            .getSyncPoller())));

            // Validate first poll
            PollResponse<BatchNode> firstPoll = deallocatePoller.poll();
            if (firstPoll.getStatus() == LongRunningOperationStatus.IN_PROGRESS) {
                BatchNode nodeDuringPoll = firstPoll.getValue();
                Assertions.assertNotNull(nodeDuringPoll);
                Assertions.assertEquals(nodeId, nodeDuringPoll.getId());
                Assertions.assertEquals(BatchNodeState.DEALLOCATING, nodeDuringPoll.getState());
            }

            // Wait for completion and validate final state
            deallocatePoller.waitForCompletion();
            BatchNode deallocatedNode = deallocatePoller.getFinalResult();

            Assertions.assertNotNull(deallocatedNode, "Final result should contain the node object");
            Assertions.assertEquals(BatchNodeState.DEALLOCATED, deallocatedNode.getState());

            // Start the node
            SyncPoller<BatchNode, BatchNode> startPoller = setPlaybackSyncPollerPollInterval(
                SyncAsyncExtension.execute(() -> batchClient.beginStartNode(poolId, nodeId),
                    () -> Mono.fromCallable(() -> batchAsyncClient.beginStartNode(poolId, nodeId).getSyncPoller())));

            // First poll
            PollResponse<BatchNode> startFirst = startPoller.poll();
            BatchNode firstVal = startFirst.getValue();

            if (startFirst.getStatus() == LongRunningOperationStatus.IN_PROGRESS) {
                // Only possible while the node is STARTING
                Assertions.assertNotNull(firstVal, "Expected node payload during polling");
                Assertions.assertEquals(BatchNodeState.STARTING, firstVal.getState(),
                    "When IN_PROGRESS the node must be in STARTING state");
            } else {
                // Operation completed in a single poll
                Assertions.assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, startFirst.getStatus());
                Assertions.assertNotNull(firstVal);
                Assertions.assertNotEquals(BatchNodeState.STARTING, firstVal.getState(),
                    "Node should have left STARTING when operation already completed");
            }

            startPoller.waitForCompletion();
            BatchNode startedNode = startPoller.getFinalResult();
            Assertions.assertNotNull(startedNode, "Final result of beginStartNode should not be null");
            Assertions.assertEquals(BatchNodeState.IDLE, startedNode.getState(),
                "Node should reach IDLE once it has started");

        } finally {
            // DELETE
            try {
                SyncPoller<BatchPool, Void> deletePoller = setPlaybackSyncPollerPollInterval(
                    SyncAsyncExtension.execute(() -> batchClient.beginDeletePool(poolId),
                        () -> Mono.fromCallable(() -> batchAsyncClient.beginDeletePool(poolId).getSyncPoller())));

                deletePoller.waitForCompletion();
            } catch (Exception e) {
                System.err.println("Cleanup failed for pool: " + poolId);
                e.printStackTrace();
            }
        }
    }

    @SyncAsyncTest
    public void canRebootReimageRemoveNodesAndStopResize() throws Exception {
        String modeSuffix = SyncAsyncExtension.execute(() -> "sync", () -> Mono.just("async"));
        String poolId = getStringIdWithUserNamePrefix("-nodeOpsPool" + modeSuffix);

        // Create or ensure a pool with 2 dedicated nodes
        final int startingDedicated = 2;

        boolean exists = SyncAsyncExtension.execute(() -> poolExists(batchClient, poolId),
            () -> poolExists(batchAsyncClient, poolId));

        if (!exists) {
            BatchVmImageReference imgRef = new BatchVmImageReference().setPublisher("microsoftwindowsserver")
                .setOffer("windowsserver")
                .setSku("2022-datacenter-smalldisk");

            VirtualMachineConfiguration vmCfg = new VirtualMachineConfiguration(imgRef, "batch.node.windows amd64");

            BatchPoolCreateParameters createParams
                = new BatchPoolCreateParameters(poolId, "STANDARD_D2S_V3").setTargetDedicatedNodes(startingDedicated)
                    .setVirtualMachineConfiguration(vmCfg);

            SyncAsyncExtension.execute(() -> batchClient.createPool(createParams),
                () -> batchAsyncClient.createPool(createParams));
        }

        try {
            // Wait for pool to reach steady state
            BatchPool pool = SyncAsyncExtension.execute(
                () -> waitForPoolState(poolId, AllocationState.STEADY, 15 * 60 * 1000),
                () -> Mono.fromCallable(() -> waitForPoolStateAsync(poolId, AllocationState.STEADY, 15 * 60 * 1000)));

            // Grab two node IDs
            List<BatchNode> nodes = new ArrayList<>();
            SyncAsyncExtension
                .execute(() -> batchClient.listNodes(poolId),
                    () -> Mono.fromCallable(() -> batchAsyncClient.listNodes(poolId).toIterable()))
                .forEach(nodes::add);

            Assertions.assertTrue(nodes.size() >= 2, "Need at least two nodes for this test.");
            String nodeIdA = nodes.get(0).getId();
            String nodeIdB = nodes.get(1).getId();

            // Reboot node
            SyncPoller<BatchNode, BatchNode> rebootPoller = setPlaybackSyncPollerPollInterval(
                SyncAsyncExtension.execute(() -> batchClient.beginRebootNode(poolId, nodeIdA),
                    () -> Mono.fromCallable(() -> batchAsyncClient.beginRebootNode(poolId, nodeIdA).getSyncPoller())));

            // Validate first poll (node should be rebooting)
            PollResponse<BatchNode> rebootFirst = rebootPoller.poll();
            if (rebootFirst.getStatus() == LongRunningOperationStatus.IN_PROGRESS) {
                BatchNode nodeDuringReboot = rebootFirst.getValue();
                Assertions.assertNotNull(nodeDuringReboot);
                Assertions.assertEquals(nodeIdA, nodeDuringReboot.getId());
                Assertions.assertEquals(BatchNodeState.REBOOTING, nodeDuringReboot.getState(),
                    "When in progress the node must be REBOOTING");
            }

            rebootPoller.waitForCompletion();
            BatchNode rebootedNode = rebootPoller.getFinalResult();
            Assertions.assertNotNull(rebootedNode, "Final result of beginRebootNode should not be null");

            // Reimage node
            SyncPoller<BatchNode, BatchNode> reimagePoller = setPlaybackSyncPollerPollInterval(
                SyncAsyncExtension.execute(() -> batchClient.beginReimageNode(poolId, nodeIdB),
                    () -> Mono.fromCallable(() -> batchAsyncClient.beginReimageNode(poolId, nodeIdB).getSyncPoller())));

            // First poll – should still be re-imaging OR may already have finished
            PollResponse<BatchNode> reimageFirst = reimagePoller.poll();
            BatchNode nodeDuringReimage = reimageFirst.getValue();

            if (reimageFirst.getStatus() == LongRunningOperationStatus.IN_PROGRESS) {
                // Only possible when state is REIMAGING
                Assertions.assertNotNull(nodeDuringReimage);
                Assertions.assertEquals(BatchNodeState.REIMAGING, nodeDuringReimage.getState(),
                    "When IN_PROGRESS the node must be REIMAGING");
            } else {
                // Operation finished in a single poll
                Assertions.assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, reimageFirst.getStatus());
                Assertions.assertNotNull(nodeDuringReimage);
                Assertions.assertNotEquals(BatchNodeState.REIMAGING, nodeDuringReimage.getState(),
                    "Node should have left REIMAGING when operation already completed");
            }

            // Wait until the OS has been re-applied and the node is usable
            reimagePoller.waitForCompletion();
            BatchNode reimagedNode = reimagePoller.getFinalResult();
            Assertions.assertNotNull(reimagedNode, "Final result of beginReimageNode should not be null");

            Assertions.assertNotEquals(BatchNodeState.REIMAGING, reimagedNode.getState(),
                "Node should have left the REIMAGING state once the operation completes");

            // Shrink pool by one node
            BatchNodeRemoveParameters removeParams = new BatchNodeRemoveParameters(Collections.singletonList(nodeIdB))
                .setNodeDeallocationOption(BatchNodeDeallocationOption.TASK_COMPLETION);

            SyncPoller<BatchPool, BatchPool> removePoller = setPlaybackSyncPollerPollInterval(
                SyncAsyncExtension.execute(() -> batchClient.beginRemoveNodes(poolId, removeParams), () -> Mono
                    .fromCallable(() -> batchAsyncClient.beginRemoveNodes(poolId, removeParams).getSyncPoller())));

            // First poll – pool should have entered RESIZING (value may be null on the first activation in playback mode)
            PollResponse<BatchPool> removeFirst = removePoller.poll();
            if (removeFirst.getStatus() == LongRunningOperationStatus.IN_PROGRESS && removeFirst.getValue() != null) {
                Assertions.assertEquals(AllocationState.RESIZING, removeFirst.getValue().getAllocationState(),
                    "Pool should be in RESIZING immediately after removeNodes starts.");
            }

            // Wait for the LRO to finish and grab the final pool object
            removePoller.waitForCompletion();
            BatchPool poolAfterRemove = removePoller.getFinalResult();

            Assertions.assertNotNull(poolAfterRemove, "Final result of beginRemoveNodes should be the updated pool.");
            Assertions.assertEquals(AllocationState.STEADY, poolAfterRemove.getAllocationState(),
                "Pool must return to STEADY after node removal.");
            Assertions.assertEquals(Integer.valueOf(1), poolAfterRemove.getTargetDedicatedNodes(),
                "Pool should have shrunk to one dedicated node after beginRemoveNodes.");

            // Wait again for STEADY after auto-resize
            pool = SyncAsyncExtension.execute(() -> waitForPoolState(poolId, AllocationState.STEADY, 15 * 60 * 1000),
                () -> Mono.fromCallable(() -> waitForPoolStateAsync(poolId, AllocationState.STEADY, 15 * 60 * 1000)));

            Assertions.assertEquals(Integer.valueOf(1), pool.getTargetDedicatedNodes(),
                "Pool should have shrunk to one dedicated node after removeNodes.");

            // Start a resize, then stop pool resize
            BatchPoolResizeParameters grow = new BatchPoolResizeParameters().setTargetDedicatedNodes(2);

            // Start the resize as an LRO
            SyncPoller<BatchPool, BatchPool> growPoller = setPlaybackSyncPollerPollInterval(
                SyncAsyncExtension.execute(() -> batchClient.beginResizePool(poolId, grow),
                    () -> Mono.fromCallable(() -> batchAsyncClient.beginResizePool(poolId, grow).getSyncPoller())));

            // Validate the very first poll – pool should be RESIZING
            PollResponse<BatchPool> growFirst = growPoller.poll();
            if (growFirst.getStatus() == LongRunningOperationStatus.IN_PROGRESS && growFirst.getValue() != null) {
                Assertions.assertEquals(AllocationState.RESIZING, growFirst.getValue().getAllocationState(),
                    "Pool should enter RESIZING when beginResizePool starts.");
            }

            // Immediately stop it
            SyncPoller<BatchPool, BatchPool> stopPoller = setPlaybackSyncPollerPollInterval(
                SyncAsyncExtension.execute(() -> batchClient.beginStopPoolResize(poolId),
                    () -> Mono.fromCallable(() -> batchAsyncClient.beginStopPoolResize(poolId).getSyncPoller())));

            // First poll – allocation state should be STOPPING or still RESIZING
            PollResponse<BatchPool> stopFirst = stopPoller.poll();
            if (stopFirst.getStatus() == LongRunningOperationStatus.IN_PROGRESS && stopFirst.getValue() != null) {
                AllocationState interim = stopFirst.getValue().getAllocationState();
                Assertions.assertTrue(interim == AllocationState.STOPPING || interim == AllocationState.RESIZING,
                    "Unexpected interim allocation state: " + interim);
            }

            // Wait for completion
            stopPoller.waitForCompletion();
            BatchPool stoppedPool = stopPoller.getFinalResult();

            Assertions.assertNotNull(stoppedPool, "Final result of beginStopPoolResize should be the updated pool.");
            Assertions.assertEquals(AllocationState.STEADY, stoppedPool.getAllocationState(),
                "Pool should return to STEADY after stop-resize.");

            pool = SyncAsyncExtension.execute(() -> waitForPoolState(poolId, AllocationState.STEADY, 15 * 60 * 1000),
                () -> Mono.fromCallable(() -> waitForPoolStateAsync(poolId, AllocationState.STEADY, 15 * 60 * 1000)));

            Assertions.assertNotEquals(AllocationState.RESIZING, pool.getAllocationState(),
                "Pool should not remain in RESIZING after stopPoolResize.");

        } finally {
            // Clean-up
            try {
                SyncPoller<BatchPool, Void> deletePoller = setPlaybackSyncPollerPollInterval(
                    SyncAsyncExtension.execute(() -> batchClient.beginDeletePool(poolId),
                        () -> Mono.fromCallable(() -> batchAsyncClient.beginDeletePool(poolId).getSyncPoller())));
                deletePoller.waitForCompletion();
            } catch (Exception e) {
                System.err.println("Cleanup failed for pool: " + poolId);
                e.printStackTrace();
            }
        }
    }
}
