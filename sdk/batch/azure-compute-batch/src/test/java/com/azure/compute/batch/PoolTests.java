package com.azure.compute.batch;

import com.azure.compute.batch.models.*;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.test.TestMode;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PoolTests extends BatchServiceClientTestBase {
	 private static BatchPool livePool;
	 private static String poolId;
	 private static NetworkConfiguration networkConfiguration;

	 @Override
     protected void beforeTest() {
    	super.beforeTest();
        poolId = getStringIdWithUserNamePrefix("-testpool");
        if(getTestMode() == TestMode.RECORD) {
        	if (livePool == null) {
        		try {
					livePool = createIfNotExistIaaSPool(poolId);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                Assertions.assertNotNull(livePool);
        	}
        }

         // Need VNet to allow security to inject NSGs
        networkConfiguration = createNetworkConfiguration();
    }

	 @Test
	 public void testPoolOData() throws Exception {

		//TODO: Looks to be an issue with Jackson desierlization of pool stats for PoolStatistics startTime and lastUpdateTime
//		 RequestOptions requestOptions = new RequestOptions();
//		 requestOptions.addQueryParam("$expand", "stats", false);
//		 poolClient.getWithResponse(poolId, requestOptions).getValue().toObject(BatchPool.class);

		//Temporarily Disabling the stats check, REST API doesn't provide the stats consistently for newly created pools
        // Will be enabled back soon.
        //Assertions.assertNotNull(pool.stats());

        PagedIterable<BatchPool> pools = poolClient.list(null, null, null, null, "id, state", null);
        Assertions.assertNotNull(pools);
        BatchPool pool = null;

        for (BatchPool batchPool: pools) {
        	if (batchPool.getId().equals(poolId)) {
        		pool = batchPool;
        	}
        }

        Assertions.assertNotNull(pool, String.format("Pool with ID %s was not found in list response", poolId));
        Assertions.assertNotNull(pool.getId());
        Assertions.assertNotNull(pool.getState());
        Assertions.assertNull(pool.getVmSize());


        // When tests are being ran in parallel, there may be a previous pool delete still in progress
        pools = poolClient.list(null, null, null, "state eq 'deleting'", null, null);
        Assertions.assertNotNull(pools);

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
	    dataDisks.add(new DataDisk(lun, diskSizeGB));

	    ImageReference imgRef = new ImageReference().setPublisher("Canonical").setOffer("UbuntuServer")
                .setSku("18.04-LTS").setVersion("latest");

	    VirtualMachineConfiguration configuration = new VirtualMachineConfiguration(imgRef, "batch.node.ubuntu 18.04");
	    configuration.setDataDisks(dataDisks);

	    BatchPoolCreateParameters poolCreateParameters = new BatchPoolCreateParameters(poolId, POOL_VM_SIZE);
	    poolCreateParameters.setNetworkConfiguration(networkConfiguration).setTargetDedicatedNodes(POOL_VM_COUNT)
	                        .setVirtualMachineConfiguration(configuration);

	    try {
	    	poolClient.create(poolCreateParameters);

	    	BatchPool pool = poolClient.get(poolId);
	        Assertions.assertEquals(lun, pool.getVirtualMachineConfiguration().getDataDisks().get(0).getLun());
	        Assertions.assertEquals(diskSizeGB, pool.getVirtualMachineConfiguration().getDataDisks().get(0).getDiskSizeGB());
	    } finally {
	        try {
	            if (poolExists(poolClient, poolId)) {
	                poolClient.delete(poolId);
	            }
	        } catch (Exception e) {
	            // Ignore exception
	        }
	    }
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
        if (!poolExists(poolClient, poolId)) {
        	ImageReference imgRef = new ImageReference().setPublisher("Canonical").setOffer("UbuntuServer")
                    .setSku("18.04-LTS").setVersion("latest");

    	    VirtualMachineConfiguration configuration = new VirtualMachineConfiguration(imgRef, "batch.node.ubuntu 18.04");

            NetworkConfiguration netConfig = createNetworkConfiguration();
            List<InboundNATPool> inbounds = new ArrayList<>();
            inbounds.add(new InboundNATPool("testinbound", InboundEndpointProtocol.TCP, 5000, 60000, 60040));

            PoolEndpointConfiguration endpointConfig = new PoolEndpointConfiguration(inbounds);
            netConfig.setEndpointConfiguration(endpointConfig);

            BatchPoolCreateParameters poolCreateParameters = new BatchPoolCreateParameters(poolId, POOL_VM_SIZE);
            poolCreateParameters.setTargetDedicatedNodes(POOL_VM_COUNT)
            		 .setTargetLowPriorityNodes(POOL_LOW_PRI_VM_COUNT)
            		 .setVirtualMachineConfiguration(configuration).setNetworkConfiguration(netConfig)
            		 .setTargetNodeCommunicationMode(NodeCommunicationMode.DEFAULT);


            poolClient.create(poolCreateParameters);
        }

        try {
            // GET
            Assertions.assertTrue(poolExists(poolClient, poolId));

            long startTime = System.currentTimeMillis();
            long elapsedTime = 0L;

            // Wait for the VM to be allocated
            BatchPool pool = waitForPoolState(poolId, AllocationState.STEADY, POOL_STEADY_TIMEOUT_IN_MILLISECONDS);

            Assertions.assertEquals(POOL_VM_COUNT, (long) pool.getCurrentDedicatedNodes());
            Assertions.assertEquals(POOL_LOW_PRI_VM_COUNT, (long) pool.getCurrentLowPriorityNodes());
            Assertions.assertNotNull(pool.getCurrentNodeCommunicationMode(), "CurrentNodeCommunicationMode should be defined for pool with more than one target dedicated node");
            Assertions.assertEquals(NodeCommunicationMode.DEFAULT, pool.getTargetNodeCommunicationMode());

            BatchNodesClient nodeClient = batchClientBuilder.buildBatchNodesClient();

            PagedIterable<BatchNode> nodeListIterator = nodeClient.list(poolId);
            List<BatchNode> computeNodes = new ArrayList<BatchNode>();

            for (BatchNode node: nodeListIterator) {
            	computeNodes.add(node);
            }

            List<InboundEndpoint> inboundEndpoints = computeNodes.get(0).getEndpointConfiguration().getInboundEndpoints();
            Assertions.assertEquals(2, inboundEndpoints.size());
            InboundEndpoint inboundEndpoint = inboundEndpoints.get(0);
            Assertions.assertEquals(5000, inboundEndpoint.getBackendPort());
            Assertions.assertTrue(inboundEndpoint.getFrontendPort() >= 60000);
            Assertions.assertTrue(inboundEndpoint.getFrontendPort() <= 60040);
            Assertions.assertTrue(inboundEndpoint.getName().startsWith("testinbound."));
            Assertions.assertTrue(inboundEndpoints.get(1).getName().startsWith("SSHRule"));

            // CHECK POOL NODE COUNTS
            PoolNodeCounts poolNodeCount = null;
            PagedIterable<PoolNodeCounts> poolNodeCountIterator = batchClientBuilder.buildAccountClient().listPoolNodeCounts();

            for (PoolNodeCounts tmp : poolNodeCountIterator) {
                if (tmp.getPoolId().equals(poolId)) {
                    poolNodeCount = tmp;
                    break;
                }
            }
            Assertions.assertNotNull(poolNodeCount); // Single pool only
            Assertions.assertNotNull(poolNodeCount.getLowPriority());

            Assertions.assertEquals(POOL_LOW_PRI_VM_COUNT, poolNodeCount.getLowPriority().getTotal());
            Assertions.assertEquals(POOL_VM_COUNT, poolNodeCount.getDedicated().getTotal());

            // Update NodeCommunicationMode to Simplified

            BatchPoolUpdateParameters poolUpdateParameters = new BatchPoolUpdateParameters(
                new LinkedList<CertificateReference>(),
                new LinkedList<ApplicationPackageReference>(),
                new LinkedList<MetadataItem>());

            poolUpdateParameters.setTargetNodeCommunicationMode(NodeCommunicationMode.SIMPLIFIED);

            poolClient.updateProperties(poolId, poolUpdateParameters);

            pool = poolClient.get(poolId);
            Assertions.assertNotNull(pool.getCurrentNodeCommunicationMode(), "CurrentNodeCommunicationMode should be defined for pool with more than one target dedicated node");
            Assertions.assertEquals(NodeCommunicationMode.SIMPLIFIED, pool.getTargetNodeCommunicationMode());

            // Patch NodeCommunicationMode to Classic

            BatchPoolPatchParameters poolPatchParameters = new BatchPoolPatchParameters();
            poolPatchParameters.setTargetNodeCommunicationMode(NodeCommunicationMode.CLASSIC);
            poolClient.patch(poolId, poolPatchParameters);

            pool = poolClient.get(poolId);
            Assertions.assertNotNull(pool.getCurrentNodeCommunicationMode(), "CurrentNodeCommunicationMode should be defined for pool with more than one target dedicated node");
            Assertions.assertEquals(NodeCommunicationMode.CLASSIC, pool.getTargetNodeCommunicationMode());

            // RESIZE
            poolClient.resize(poolId, new BatchPoolResizeParameters().setTargetDedicatedNodes(1).setTargetLowPriorityNodes(1));

            pool = poolClient.get(poolId);
            Assertions.assertEquals(1, (long) pool.getTargetDedicatedNodes());
            Assertions.assertEquals(1, (long) pool.getTargetLowPriorityNodes());

            // DELETE
            boolean deleted = false;
            elapsedTime = 0L;
            poolClient.delete(poolId);

            // Wait for the VM to be deallocated
            while (elapsedTime < POOL_STEADY_TIMEOUT_IN_MILLISECONDS) {
                try {
                	poolClient.get(poolId);
                } catch (Exception err) {
                	if (!err.getMessage().contains("Status code 404")) {
            			throw err;
            		}
                	deleted = true;
                	break;
                }

                System.out.println("wait 15 seconds for pool delete...");
                threadSleepInRecordMode(15 * 1000);
                elapsedTime = (new Date()).getTime() - startTime;
            }
            Assertions.assertTrue(deleted);

        } finally {
            try {
                if (poolExists(poolClient, poolId)) {
                    poolClient.delete(poolId);
                }
            } catch (Exception e) {
                // Ignore exception
            }
        }
    }
}
