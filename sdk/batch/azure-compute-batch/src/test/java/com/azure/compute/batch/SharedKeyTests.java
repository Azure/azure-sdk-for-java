// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch;

import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.compute.batch.models.*;
import com.azure.core.http.*;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.DateTimeRfc1123;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static java.time.OffsetDateTime.now;

public class SharedKeyTests extends BatchClientTestBase {
    private static BatchClient batchClientWithSharedKey;
    private final String sharedKeyPoolId = "SharedKey-testpool";
    private final String vmSize = "STANDARD_D1_V2";
    private final String nodeAgentSkuId = "batch.node.ubuntu 18.04";

    @Override
    protected void beforeTest() {
        super.beforeTest();
        AzureNamedKeyCredential sharedKeyCred = getSharedKeyCredentials();
        batchClientBuilder.credential(sharedKeyCred);
        batchClientWithSharedKey = batchClientBuilder.buildClient();
    }

    @Test
    public void testPoolCRUD() {
        try {
            /*
             * Creating Pool
             * */
            ImageReference imgRef = new ImageReference().setPublisher("Canonical").setOffer("UbuntuServer")
                    .setSku("18.04-LTS").setVersion("latest");

            VirtualMachineConfiguration configuration = new VirtualMachineConfiguration(imgRef, nodeAgentSkuId);

            BatchPoolCreateContent poolCreateContent = new BatchPoolCreateContent(sharedKeyPoolId, vmSize);
            poolCreateContent.setTargetDedicatedNodes(2)
                                .setVirtualMachineConfiguration(configuration)
                                .setTargetNodeCommunicationMode(BatchNodeCommunicationMode.DEFAULT);

            Response<Void> response = batchClientWithSharedKey.createPoolWithResponse(BinaryData.fromObject(poolCreateContent), null);
            String authorizationValue = response.getRequest().getHeaders().getValue(HttpHeaderName.AUTHORIZATION);
            Assertions.assertTrue(authorizationValue.contains("SharedKey"), "Test is not using SharedKey authentication");

            /*
             * Getting Pool
             */
            Assertions.assertTrue(poolExists(batchClientWithSharedKey, sharedKeyPoolId));
            BatchPool pool = batchClientWithSharedKey.getPool(sharedKeyPoolId);
            Assertions.assertEquals(pool.getId(), sharedKeyPoolId);
            Assertions.assertEquals(pool.getVirtualMachineConfiguration().getNodeAgentSkuId(), nodeAgentSkuId);
            Assertions.assertEquals(vmSize.toLowerCase(), pool.getVmSize().toLowerCase());

            /*
             * Replacing Pool Properties
             */
            ArrayList<MetadataItem> updatedMetadata = new ArrayList<MetadataItem>();
            updatedMetadata.add(new MetadataItem("foo", "bar"));

            BatchPoolReplaceContent poolReplaceContent = new BatchPoolReplaceContent(new ArrayList<>(), updatedMetadata);

            poolReplaceContent.setTargetNodeCommunicationMode(BatchNodeCommunicationMode.SIMPLIFIED);

            batchClientWithSharedKey.replacePoolProperties(sharedKeyPoolId, poolReplaceContent);

            pool = batchClientWithSharedKey.getPool(sharedKeyPoolId);
            Assertions.assertEquals(BatchNodeCommunicationMode.SIMPLIFIED, pool.getTargetNodeCommunicationMode());
            List<MetadataItem> metadata = pool.getMetadata();
            Assertions.assertTrue(metadata.size() == 1 && metadata.get(0).getName().equals("foo"));

            /*
             * Update Pool
             */
            updatedMetadata.clear();
            updatedMetadata.add(new MetadataItem("key1", "value1"));
            BatchPoolUpdateContent poolUpdateContent = new BatchPoolUpdateContent().setMetadata(updatedMetadata).setTargetNodeCommunicationMode(BatchNodeCommunicationMode.CLASSIC);
            Response<Void> updatePoolResponse = batchClientWithSharedKey.updatePoolWithResponse(sharedKeyPoolId, BinaryData.fromObject(poolUpdateContent), null);
            HttpRequest updatePoolRequest = updatePoolResponse.getRequest();
            HttpHeader ocpDateHeader = updatePoolRequest.getHeaders().get(HttpHeaderName.fromString("ocp-date"));
            Assertions.assertNull(ocpDateHeader);
            HttpHeader dateHeader = updatePoolRequest.getHeaders().get(HttpHeaderName.DATE);
            Assertions.assertNotNull(dateHeader);
            authorizationValue = updatePoolRequest.getHeaders().getValue(HttpHeaderName.AUTHORIZATION);
            Assertions.assertTrue(authorizationValue.contains("SharedKey"), "Test is not using SharedKey authentication");

            /*
            * Get Pool With ocp-Date header
            * */
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.setHeader(HttpHeaderName.fromString("ocp-date"), new DateTimeRfc1123(now()).toString());
            Response<BinaryData> poolGetResponse = batchClientWithSharedKey.getPoolWithResponse(sharedKeyPoolId, requestOptions);

            HttpRequest getPoolRequest = poolGetResponse.getRequest();
            ocpDateHeader = getPoolRequest.getHeaders().get(HttpHeaderName.fromString("ocp-date"));
            Assertions.assertNotNull(ocpDateHeader);
            Assertions.assertTrue(!ocpDateHeader.getValue().isEmpty());
            pool = poolGetResponse.getValue().toObject(BatchPool.class);

            authorizationValue = getPoolRequest.getHeaders().getValue(HttpHeaderName.AUTHORIZATION);
            Assertions.assertTrue(authorizationValue.contains("SharedKey"), "Test is not using SharedKey authentication");

            Assertions.assertEquals(BatchNodeCommunicationMode.CLASSIC, pool.getTargetNodeCommunicationMode());
            metadata = pool.getMetadata();
            Assertions.assertTrue(metadata.size() == 1 && metadata.get(0).getName().equals("key1"));

        } finally {
            /*
             * Deleting Pool
             * */

            batchClientWithSharedKey.deletePool(sharedKeyPoolId);
        }

    }
}
