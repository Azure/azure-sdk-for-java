// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch;

import com.azure.compute.batch.models.BatchMetadataItem;
import com.azure.compute.batch.models.BatchNodeCommunicationMode;
import com.azure.compute.batch.models.BatchPool;
import com.azure.compute.batch.models.BatchPoolCreateParameters;
import com.azure.compute.batch.models.BatchPoolReplaceParameters;
import com.azure.compute.batch.models.BatchPoolUpdateParameters;
import com.azure.compute.batch.models.BatchVmImageReference;
import com.azure.compute.batch.models.VirtualMachineConfiguration;
import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.test.SyncAsyncExtension;
import com.azure.core.test.annotation.SyncAsyncTest;
import com.azure.core.util.BinaryData;
import com.azure.core.util.DateTimeRfc1123;

import reactor.core.publisher.Mono;

import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.List;

import static java.time.OffsetDateTime.now;

public class SharedKeyTests extends BatchClientTestBase {
    private static BatchClient batchClientWithSharedKey;
    private static BatchAsyncClient batchAsyncClientWithSharedKey;
    private final String sharedKeyPoolPrefix = "SharedKey-testpool";
    private final String vmSize = "STANDARD_D1_V2";
    private final String nodeAgentSkuId = "batch.node.windows amd64";

    @Override
    protected void beforeTest() {
        super.beforeTest();
        AzureNamedKeyCredential sharedKeyCred = getSharedKeyCredentials();
        batchClientBuilder.credential(sharedKeyCred);
        batchClientWithSharedKey = batchClientBuilder.buildClient();
        batchAsyncClientWithSharedKey = batchClientBuilder.buildAsyncClient();
    }

    @SyncAsyncTest
    public void testPoolCRUD() {
        // Generate a jobId that is unique per test mode (sync vs async)
        String testModeSuffix = SyncAsyncExtension.execute(() -> "sync", () -> Mono.just("async"));
        String poolId = sharedKeyPoolPrefix + "-" + testModeSuffix;
        try {
            /*
             * Creating Pool
             */
            BatchVmImageReference imgRef = new BatchVmImageReference().setPublisher("microsoftwindowsserver")
                .setOffer("windowsserver")
                .setSku("2022-datacenter-smalldisk");

            VirtualMachineConfiguration configuration = new VirtualMachineConfiguration(imgRef, nodeAgentSkuId);

            BatchPoolCreateParameters poolCreateParameters = new BatchPoolCreateParameters(poolId, vmSize);
            poolCreateParameters.setTargetDedicatedNodes(2)
                .setVirtualMachineConfiguration(configuration)
                .setTargetNodeCommunicationMode(BatchNodeCommunicationMode.DEFAULT);

            Response<Void> response = SyncAsyncExtension.execute(
                () -> batchClientWithSharedKey.createPoolWithResponse(BinaryData.fromObject(poolCreateParameters),
                    null),
                () -> batchAsyncClientWithSharedKey.createPoolWithResponse(BinaryData.fromObject(poolCreateParameters),
                    null));

            String authorizationValue = response.getRequest().getHeaders().getValue(HttpHeaderName.AUTHORIZATION);
            Assertions.assertTrue(authorizationValue.contains("SharedKey"),
                "Test is not using SharedKey authentication");

            /*
             * Getting Pool
             */
            boolean exists = SyncAsyncExtension.execute(() -> poolExists(batchClientWithSharedKey, poolId),
                () -> poolExists(batchAsyncClientWithSharedKey, poolId));
            Assertions.assertTrue(exists, "Pool should exist after creation");

            BatchPool pool = SyncAsyncExtension.execute(() -> batchClientWithSharedKey.getPool(poolId),
                () -> batchAsyncClientWithSharedKey.getPool(poolId));
            Assertions.assertEquals(pool.getId(), poolId);
            Assertions.assertEquals(pool.getVirtualMachineConfiguration().getNodeAgentSkuId(), nodeAgentSkuId);
            Assertions.assertEquals(vmSize.toLowerCase(), pool.getVmSize().toLowerCase());

            /*
             * Replacing Pool Properties
             */
            ArrayList<BatchMetadataItem> updatedMetadata = new ArrayList<BatchMetadataItem>();
            updatedMetadata.add(new BatchMetadataItem("foo", "bar"));

            BatchPoolReplaceParameters poolReplaceParameters
                = new BatchPoolReplaceParameters(new ArrayList<>(), new ArrayList<>(), updatedMetadata);

            poolReplaceParameters.setTargetNodeCommunicationMode(BatchNodeCommunicationMode.SIMPLIFIED);

            SyncAsyncExtension.execute(
                () -> batchClientWithSharedKey.replacePoolProperties(poolId, poolReplaceParameters),
                () -> batchAsyncClientWithSharedKey.replacePoolProperties(poolId, poolReplaceParameters));

            pool = SyncAsyncExtension.execute(() -> batchClientWithSharedKey.getPool(poolId),
                () -> batchAsyncClientWithSharedKey.getPool(poolId));
            Assertions.assertEquals(BatchNodeCommunicationMode.SIMPLIFIED, pool.getTargetNodeCommunicationMode());
            List<BatchMetadataItem> metadata = pool.getMetadata();
            Assertions.assertTrue(metadata.size() == 1 && metadata.get(0).getName().equals("foo"));

            /*
             * Update Pool
             */
            updatedMetadata.clear();
            updatedMetadata.add(new BatchMetadataItem("key1", "value1"));
            BatchPoolUpdateParameters poolUpdateParameters
                = new BatchPoolUpdateParameters().setMetadata(updatedMetadata)
                    .setTargetNodeCommunicationMode(BatchNodeCommunicationMode.CLASSIC);
            Response<Void> updatePoolResponse = SyncAsyncExtension.execute(
                () -> batchClientWithSharedKey.updatePoolWithResponse(poolId,
                    BinaryData.fromObject(poolUpdateParameters), null),
                () -> batchAsyncClientWithSharedKey.updatePoolWithResponse(poolId,
                    BinaryData.fromObject(poolUpdateParameters), null));
            HttpRequest updatePoolRequest = updatePoolResponse.getRequest();
            HttpHeader ocpDateHeader = updatePoolRequest.getHeaders().get(HttpHeaderName.fromString("ocp-date"));
            Assertions.assertNull(ocpDateHeader);
            HttpHeader dateHeader = updatePoolRequest.getHeaders().get(HttpHeaderName.DATE);
            Assertions.assertNotNull(dateHeader);
            authorizationValue = updatePoolRequest.getHeaders().getValue(HttpHeaderName.AUTHORIZATION);
            Assertions.assertTrue(authorizationValue.contains("SharedKey"),
                "Test is not using SharedKey authentication");

            /*
            * Get Pool With ocp-Date header
            */
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.setHeader(HttpHeaderName.fromString("ocp-date"), new DateTimeRfc1123(now()).toString());
            Response<BinaryData> poolGetResponse
                = SyncAsyncExtension.execute(() -> batchClientWithSharedKey.getPoolWithResponse(poolId, requestOptions),
                    () -> batchAsyncClientWithSharedKey.getPoolWithResponse(poolId, requestOptions));
            HttpRequest getPoolRequest = poolGetResponse.getRequest();
            ocpDateHeader = getPoolRequest.getHeaders().get(HttpHeaderName.fromString("ocp-date"));
            Assertions.assertNotNull(ocpDateHeader);
            Assertions.assertFalse(ocpDateHeader.getValue().isEmpty());
            pool = poolGetResponse.getValue().toObject(BatchPool.class);

            authorizationValue = getPoolRequest.getHeaders().getValue(HttpHeaderName.AUTHORIZATION);
            Assertions.assertTrue(authorizationValue.contains("SharedKey"),
                "Test is not using SharedKey authentication");

            Assertions.assertEquals(BatchNodeCommunicationMode.CLASSIC, pool.getTargetNodeCommunicationMode());
            metadata = pool.getMetadata();
            Assertions.assertTrue(metadata.size() == 1 && metadata.get(0).getName().equals("key1"));

        } finally {
            /*
             * Deleting Pool
             */
            try {
                SyncAsyncExtension.execute(() -> batchClientWithSharedKey.deletePool(poolId),
                    () -> batchAsyncClientWithSharedKey.deletePool(poolId));
            } catch (Exception e) {
                System.err.println("Cleanup failed for pool: " + poolId);
                e.printStackTrace();
            }
        }
    }
}
