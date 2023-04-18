package com.azure.compute.batch.generated;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import com.azure.compute.batch.PoolClient;
import com.azure.compute.batch.models.BatchPool;
import com.azure.compute.batch.models.NetworkConfiguration;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.test.TestMode;

public class PoolTests extends BatchServiceClientTestBase {
	 private static BatchPool livePool;
	 private static String poolId;
	 private static NetworkConfiguration networkConfiguration;
	 private static PoolClient poolClient;
	 
	 @Override
     protected void beforeTest() {
    	super.beforeTest();
    	poolClient = batchClientBulder.buildPoolClient();
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
		 //RequestOptions requestOptions = new RequestOptions();
		 //requestOptions.addQueryParam("$expand", "stats", false);
		//poolClient.getWithResponse(poolId, requestOptions).getValue().toObject(BatchPool.class);

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
 
	 

}
