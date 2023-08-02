package com.azure.compute.batch;

import com.azure.compute.batch.auth.BatchSharedKeyCredentials;
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
import java.util.LinkedList;
import java.util.List;

import static java.time.OffsetDateTime.now;

public class SharedKeyTests extends BatchServiceClientTestBase {
    private static PoolClient poolClientWithSharedKey;
    private final String sharedKeyPoolId = "SharedKey-testpool";
    private final String vmSize = "STANDARD_D1_V2";
    private final String nodeAgentSkuId = "batch.node.ubuntu 18.04";

    @Override
    protected void beforeTest() {
        super.beforeTest();
        BatchSharedKeyCredentials sharedKeyCred = getSharedKeyCredentials();
        batchClientBuilder.credential(sharedKeyCred);
        poolClientWithSharedKey = batchClientBuilder.buildPoolClient();
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

            BatchPoolCreateParameters poolCreateParameters = new BatchPoolCreateParameters(sharedKeyPoolId, vmSize);
            poolCreateParameters.setTargetDedicatedNodes(2)
                                .setVirtualMachineConfiguration(configuration)
                                .setTargetNodeCommunicationMode(NodeCommunicationMode.DEFAULT);

            poolClientWithSharedKey.create(poolCreateParameters);

            /*
             * Getting Pool
             */
            Assertions.assertTrue(poolExists(poolClientWithSharedKey, sharedKeyPoolId));
            BatchPool pool = poolClientWithSharedKey.get(sharedKeyPoolId);
            Assertions.assertEquals(pool.getId(), sharedKeyPoolId);
            Assertions.assertEquals(pool.getVirtualMachineConfiguration().getNodeAgentSKUId(), nodeAgentSkuId);
            Assertions.assertEquals(vmSize.toLowerCase(), pool.getVmSize().toLowerCase());

            /*
             * Updating Pool
             */
            BatchPoolUpdateParameters poolUpdateParameters = new BatchPoolUpdateParameters(new LinkedList<>(),
                    new LinkedList<>(),
                    new LinkedList<>(List.of(new MetadataItem("foo", "bar"))));

            poolUpdateParameters.setTargetNodeCommunicationMode(NodeCommunicationMode.SIMPLIFIED);

            poolClientWithSharedKey.updateProperties(sharedKeyPoolId, poolUpdateParameters);

            pool = poolClientWithSharedKey.get(sharedKeyPoolId);
            Assertions.assertEquals(NodeCommunicationMode.SIMPLIFIED, pool.getTargetNodeCommunicationMode());
            List<MetadataItem> metadata = pool.getMetadata();
            Assertions.assertTrue(metadata.size() == 1 && metadata.get(0).getName().equals("foo"));

            /*
             * Patch Pool
             */
            BatchPoolPatchParameters poolPatchParameters = new BatchPoolPatchParameters().setMetadata(new ArrayList<>(List.of(new MetadataItem("key1", "value1")))).setTargetNodeCommunicationMode(NodeCommunicationMode.CLASSIC);
            Response<Void> patchPoolResponse = poolClientWithSharedKey.patchWithResponse(sharedKeyPoolId, BinaryData.fromObject(poolPatchParameters), null);
            HttpRequest patchPoolRequest = patchPoolResponse.getRequest();
            HttpHeader ocpDateHeader = patchPoolRequest.getHeaders().get(HttpHeaderName.fromString("ocp-date"));
            Assertions.assertNull(ocpDateHeader);
            HttpHeader dateHeader = patchPoolRequest.getHeaders().get(HttpHeaderName.DATE);
            Assertions.assertNotNull(dateHeader);

            /*
            * Get Pool With ocp-Date header
            * */
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.setHeader(HttpHeaderName.fromString("ocp-date"), new DateTimeRfc1123(now()).toString());
            Response<BinaryData> poolGetResponse = poolClientWithSharedKey.getWithResponse(sharedKeyPoolId, requestOptions);

            HttpRequest getPoolRequest = poolGetResponse.getRequest();
            ocpDateHeader = getPoolRequest.getHeaders().get(HttpHeaderName.fromString("ocp-date"));
            Assertions.assertNotNull(ocpDateHeader);
            Assertions.assertTrue(!ocpDateHeader.getValue().isEmpty());
            pool = poolGetResponse.getValue().toObject(BatchPool.class);

            Assertions.assertEquals(NodeCommunicationMode.CLASSIC, pool.getTargetNodeCommunicationMode());
            metadata = pool.getMetadata();
            Assertions.assertTrue(metadata.size() == 1 && metadata.get(0).getName().equals("key1"));

        }
        finally {
            /*
             * Deleting Pool
             * */

            poolClientWithSharedKey.delete(sharedKeyPoolId);
        }

    }
}
