package com.azure.compute.batch.generated;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import com.azure.compute.batch.BatchServiceClientBuilder;
import com.azure.compute.batch.ComputeNodesClient;
import com.azure.compute.batch.PoolClient;
import com.azure.compute.batch.models.AllocationState;
import com.azure.compute.batch.models.BatchPool;
import com.azure.compute.batch.models.BatchPoolResizeParameters;
import com.azure.compute.batch.models.CertificateReference;
import com.azure.compute.batch.models.ComputeNode;
import com.azure.compute.batch.models.NetworkConfiguration;
import com.azure.compute.batch.models.NodeCommunicationMode;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.test.TestMode;
import com.azure.compute.batch.models.DataDisk;
import com.azure.compute.batch.models.ImageReference;
import com.azure.compute.batch.models.InboundEndpoint;
import com.azure.compute.batch.models.InboundEndpointProtocol;
import com.azure.compute.batch.models.VirtualMachineConfiguration;
import com.azure.compute.batch.models.PoolEndpointConfiguration;
import com.azure.compute.batch.models.PoolNodeCounts;
import com.azure.compute.batch.models.InboundNATPool;
import com.azure.compute.batch.models.MetadataItem;
import com.azure.compute.batch.models.ApplicationPackageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PoolTests extends BatchServiceClientTestBase {
	 private static BatchPool livePool;
	 private static String poolId;
	 private static NetworkConfiguration networkConfiguration;
	 private static PoolClient poolClient;
	 
	 @Override
     protected void beforeTest() {
    	super.beforeTest();
    	poolClient = batchClientBuilder.buildPoolClient();
        poolId = getStringIdWithUserNamePrefix("-testpool");
        if(getTestMode() == TestMode.RECORD) {
        	if (livePool == null) {
        		try {
					livePool = createIfNotExistIaaSPool(poolId);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                Assert.assertNotNull(livePool);
        	}
        }
        
        networkConfiguration = createNetworkConfiguration();
    }
	 
	 @Test
	 public void testPoolOData() throws Exception {

		//TODO: Looks to be an issue with Jackson desierlization of pool stats for PoolStatistics startTime and lastUpdateTime
		 RequestOptions requestOptions = new RequestOptions();
		 requestOptions.addQueryParam("$expand", "stats", false);
		 poolClient.getWithResponse(poolId, requestOptions).getValue().toObject(BatchPool.class);

		//Temporarily Disabling the stats check, REST API doesn't provide the stats consistently for newly created pools
        // Will be enabled back soon.
        //Assert.assertNotNull(pool.stats());

        PagedIterable<BatchPool> pools = poolClient.list(null, null, null, null, null, null, "id, state", null);
        Assert.assertNotNull(pools);
        BatchPool pool = null;
        
        for (BatchPool batchPool: pools) {
        	if (batchPool.getId().equals(poolId)) {
        		pool = batchPool;
        	}
        }

        Assert.assertNotNull(String.format("Pool with ID %s was not found in list response", poolId), pool);
        Assert.assertNotNull(pool.getId());
        Assert.assertNotNull(pool.getState());
        Assert.assertNull(pool.getVmSize());

        
        // When tests are being ran in parallel, there may be a previous pool delete still in progress
        pools = poolClient.list(null, null, null, null, null, "state eq 'deleting'", null, null);
        Assert.assertNotNull(pools);

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

	    BatchPool poolToAdd = new BatchPool();
	    poolToAdd.setId(poolId);
	    poolToAdd.setNetworkConfiguration(networkConfiguration);
	    poolToAdd.setTargetDedicatedNodes(POOL_VM_COUNT);
	    poolToAdd.setVmSize(POOL_VM_SIZE);
	    poolToAdd.setVirtualMachineConfiguration(configuration);

	    try {
	    	poolClient.add(poolToAdd);
	
	    	BatchPool pool = poolClient.get(poolId);
	        Assert.assertEquals(lun, pool.getVirtualMachineConfiguration().getDataDisks().get(0).getLun());
	        Assert.assertEquals(diskSizeGB, pool.getVirtualMachineConfiguration().getDataDisks().get(0).getDiskSizeGB());
	    } finally {
	        try {
	            if (poolExists(poolId)) {
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
        if (!poolExists(poolId)) {
        	ImageReference imgRef = new ImageReference().setPublisher("Canonical").setOffer("UbuntuServer")
                    .setSku("18.04-LTS").setVersion("latest");

    	    VirtualMachineConfiguration configuration = new VirtualMachineConfiguration(imgRef, "batch.node.ubuntu 18.04");

            NetworkConfiguration netConfig = createNetworkConfiguration();
            List<InboundNATPool> inbounds = new ArrayList<>();
            inbounds.add(new InboundNATPool("testinbound", InboundEndpointProtocol.TCP, 5000, 60000, 60040));

            PoolEndpointConfiguration endpointConfig = new PoolEndpointConfiguration(inbounds);
            netConfig.setEndpointConfiguration(endpointConfig);

            BatchPool poolToAdd = new BatchPool();
            poolToAdd.setId(poolId).setTargetDedicatedNodes(POOL_VM_COUNT)
            		 .setTargetLowPriorityNodes(POOL_LOW_PRI_VM_COUNT).setVmSize(POOL_VM_SIZE)
            		 .setVirtualMachineConfiguration(configuration).setNetworkConfiguration(netConfig)
            		 .setTargetNodeCommunicationMode(NodeCommunicationMode.DEFAULT);
            	
            
            poolClient.add(poolToAdd);
        }

        try {
            // GET
            Assert.assertTrue(poolExists(poolId));

            long startTime = System.currentTimeMillis();
            long elapsedTime = 0L;

            // Wait for the VM to be allocated
            BatchPool pool = waitForPoolState(poolId, AllocationState.STEADY, POOL_STEADY_TIMEOUT_IN_MILLISECONDS);

            Assert.assertEquals(POOL_VM_COUNT, (long) pool.getCurrentDedicatedNodes());
            Assert.assertEquals(POOL_LOW_PRI_VM_COUNT, (long) pool.getCurrentLowPriorityNodes());
            Assert.assertNotNull("CurrentNodeCommunicationMode should be defined for pool with more than one target dedicated node", pool.getCurrentNodeCommunicationMode());
            Assert.assertEquals(NodeCommunicationMode.DEFAULT, pool.getTargetNodeCommunicationMode());

            ComputeNodesClient nodeClient = batchClientBuilder.buildComputeNodesClient();

            PagedIterable<ComputeNode> nodeListIterator = nodeClient.list(poolId);
            List<ComputeNode> computeNodes = new ArrayList<ComputeNode>();
            
            for (ComputeNode node: nodeListIterator) {
            	computeNodes.add(node);
            }

            List<InboundEndpoint> inboundEndpoints = computeNodes.get(0).getEndpointConfiguration().getInboundEndpoints();
            Assert.assertEquals(2, inboundEndpoints.size());
            InboundEndpoint inboundEndpoint = inboundEndpoints.get(0);
            Assert.assertEquals(5000, inboundEndpoint.getBackendPort());
            Assert.assertTrue(inboundEndpoint.getFrontendPort() >= 60000);
            Assert.assertTrue(inboundEndpoint.getFrontendPort() <= 60040);
            Assert.assertTrue(inboundEndpoint.getName().startsWith("testinbound."));
            Assert.assertTrue(inboundEndpoints.get(1).getName().startsWith("SSHRule"));

            // CHECK POOL NODE COUNTS
            PoolNodeCounts poolNodeCount = null;
            PagedIterable<PoolNodeCounts> poolNodeCountIterator = batchClientBuilder.buildAccountClient().listPoolNodeCounts();

            for (PoolNodeCounts tmp : poolNodeCountIterator) {
                if (tmp.getPoolId().equals(poolId)) {
                    poolNodeCount = tmp;
                    break;
                }
            }
            Assert.assertNotNull(poolNodeCount); // Single pool only
            Assert.assertNotNull(poolNodeCount.getLowPriority());

            Assert.assertEquals(POOL_LOW_PRI_VM_COUNT, poolNodeCount.getLowPriority().getTotal());
            Assert.assertEquals(POOL_VM_COUNT, poolNodeCount.getDedicated().getTotal());

            // Update NodeCommunicationMode to Simplified

//	            //You cannot take an existing BatchPool object that has i.e. Id and vmSize properties defined and call update with it
//	            poolToUpdate.setTargetNodeCommunicationMode(NodeCommunicationMode.SIMPLIFIED)
//    			.setApplicationPackageReferences(new ArrayList<ApplicationPackageReference>())
//    			.setMetadata(new ArrayList<MetadataItem>())
//    			.setCertificateReferences(new ArrayList<CertificateReference>());

            BatchPool poolToUpdate = new BatchPool();
            poolToUpdate.setTargetNodeCommunicationMode(NodeCommunicationMode.SIMPLIFIED)
            			.setApplicationPackageReferences(new ArrayList<ApplicationPackageReference>())
            			.setMetadata(new ArrayList<MetadataItem>())
            			.setCertificateReferences(new ArrayList<CertificateReference>());

            poolClient.updateProperties(poolId, poolToUpdate);

            pool = poolClient.get(poolId);
            Assert.assertNotNull("CurrentNodeCommunicationMode should be defined for pool with more than one target dedicated node", pool.getCurrentNodeCommunicationMode());
            Assert.assertEquals(NodeCommunicationMode.SIMPLIFIED, pool.getTargetNodeCommunicationMode());

            // Patch NodeCommunicationMode to Classic
            
            //You cannot take an existing BatchPool object that has i.e. Id and vmSize properties defined and call patch with it
//	            pool.setTargetNodeCommunicationMode(NodeCommunicationMode.CLASSIC);
//	            poolClient.patch(poolId, pool);
            
            BatchPool poolToPatch = new BatchPool();
            poolToPatch.setTargetNodeCommunicationMode(NodeCommunicationMode.CLASSIC);
            poolClient.patch(poolId, poolToPatch);
            
            pool = poolClient.get(poolId);
            Assert.assertNotNull("CurrentNodeCommunicationMode should be defined for pool with more than one target dedicated node", pool.getCurrentNodeCommunicationMode());
            Assert.assertEquals(NodeCommunicationMode.CLASSIC, pool.getTargetNodeCommunicationMode());

            // RESIZE
            poolClient.resize(poolId, new BatchPoolResizeParameters().setTargetDedicatedNodes(1).setTargetLowPriorityNodes(1));

            pool = poolClient.get(poolId);
            Assert.assertEquals(1, (long) pool.getTargetDedicatedNodes());
            Assert.assertEquals(1, (long) pool.getTargetLowPriorityNodes());

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
            Assert.assertTrue(deleted);

        } finally {
            try {
                if (poolExists(poolId)) {
                    poolClient.delete(poolId);
                }
            } catch (Exception e) {
                // Ignore exception
            }
        }
    }

 
	 public BatchPool waitForPoolState(String poolId, AllocationState targetState, long poolAllocationTimeoutInMilliseconds) throws IOException, InterruptedException {
	        long startTime = System.currentTimeMillis();
	        long elapsedTime = 0L;
	        boolean allocationStateReached = false;
	        BatchPool pool = null;

	        // Wait for the VM to be allocated
	        while (elapsedTime < poolAllocationTimeoutInMilliseconds) {
	            pool = poolClient.get(poolId);
	            Assert.assertNotNull(pool);

	            if (pool.getAllocationState() == targetState) {
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

}
