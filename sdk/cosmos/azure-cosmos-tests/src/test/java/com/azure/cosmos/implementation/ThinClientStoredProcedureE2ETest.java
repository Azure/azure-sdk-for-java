// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.models.CosmosStoredProcedureProperties;
import com.azure.cosmos.models.CosmosStoredProcedureRequestOptions;
import com.azure.cosmos.models.CosmosStoredProcedureResponse;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.Fail.fail;

/**
 * Thin client E2E tests for stored procedure execution.
 */
public class ThinClientStoredProcedureE2ETest extends ThinClientTestBase {

    @Factory(dataProvider = "clientBuildersWithGatewayAndHttp2")
    public ThinClientStoredProcedureE2ETest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testThinClientStoredProcedure() {
        String sprocId = "createDocSproc_" + UUID.randomUUID().toString();
        String pkValue = UUID.randomUUID().toString();
        String docId = UUID.randomUUID().toString();
        try {
            CosmosStoredProcedureProperties storedProcedureDef = new CosmosStoredProcedureProperties(
                sprocId,
                "function createDocument(docToCreate) {" +
                    "    var context = getContext();" +
                    "    var container = context.getCollection();" +
                    "    var response = context.getResponse();" +
                    "    var accepted = container.createDocument(" +
                    "        container.getSelfLink()," +
                    "        docToCreate," +
                    "        function(err, docCreated) {" +
                    "            if (err) throw new Error('Error creating document: ' + err.message);" +
                    "            response.setBody(docCreated);" +
                    "        }" +
                    "    );" +
                    "    if (!accepted) throw new Error('Document creation was not accepted');" +
                    "}"
            );

            CosmosStoredProcedureResponse createResponse = container.getScripts()
                .createStoredProcedure(storedProcedureDef).block();
            assertThat(createResponse).isNotNull();
            assertThat(createResponse.getStatusCode()).isEqualTo(201);

            CosmosStoredProcedureRequestOptions options = new CosmosStoredProcedureRequestOptions();
            options.setPartitionKey(new PartitionKey(pkValue));

            String docToCreate = String.format("{\"%s\": \"%s\", \"%s\": \"%s\"}", ID_FIELD, docId, PARTITION_KEY_FIELD, pkValue);

            CosmosStoredProcedureResponse executeResponse = container.getScripts()
                .getStoredProcedure(sprocId)
                .execute(Arrays.asList(docToCreate), options).block();

            assertThat(executeResponse).isNotNull();
            assertThat(executeResponse.getStatusCode()).isEqualTo(200);
            assertThat(executeResponse.getRequestCharge()).isGreaterThan(0.0);
            assertThinClientEndpointUsed(executeResponse.getDiagnostics());

            CosmosItemResponse<ObjectNode> readResponse = container.readItem(docId, new PartitionKey(pkValue), ObjectNode.class).block();
            assertThat(readResponse).isNotNull();
            assertThat(readResponse.getItem().get(ID_FIELD).asText()).isEqualTo(docId);
        } finally {
            try { container.deleteItem(docId, new PartitionKey(pkValue)).block(); } catch (Exception e) { logger.warn("Cleanup failed", e); }
            try { container.getScripts().getStoredProcedure(sprocId).delete().block(); } catch (Exception e) { logger.warn("Cleanup failed", e); }
        }
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testStoredProcedureExecutionWithoutPartitionKeyThrows() {
        String sprocId = "noPartitionKeySproc_" + UUID.randomUUID().toString();
        try {
            CosmosStoredProcedureProperties storedProcedureDef = new CosmosStoredProcedureProperties(
                sprocId, "function() { getContext().getResponse().setBody('Hello'); }");

            container.getScripts().createStoredProcedure(storedProcedureDef).block();

            CosmosStoredProcedureRequestOptions options = new CosmosStoredProcedureRequestOptions();

            try {
                container.getScripts().getStoredProcedure(sprocId).execute(null, options).block();
                fail("Expected UnsupportedOperationException for sproc execution without partition key");
            } catch (UnsupportedOperationException e) {
                assertThat(e.getMessage()).contains("PartitionKey value must be supplied");
                logger.info("Confirmed: V4 SDK throws UnsupportedOperationException for sproc without PK: {}", e.getMessage());
            }
        } finally {
            try { container.getScripts().getStoredProcedure(sprocId).delete().block(); } catch (Exception e) { logger.warn("Cleanup failed", e); }
        }
    }

    @Test(groups = {"thinclient"}, timeOut = TIMEOUT)
    public void testThinClientStoredProcedureWithPartitionKeyNone() {
        String sprocId = "pkNoneSproc_" + UUID.randomUUID().toString();
        try {
            CosmosStoredProcedureProperties storedProcedureDef = new CosmosStoredProcedureProperties(
                sprocId, "function() { getContext().getResponse().setBody('Hello from PK.NONE'); }");

            container.getScripts().createStoredProcedure(storedProcedureDef).block();

            CosmosStoredProcedureRequestOptions options = new CosmosStoredProcedureRequestOptions();
            options.setPartitionKey(PartitionKey.NONE);

            CosmosStoredProcedureResponse executeResponse = container.getScripts()
                .getStoredProcedure(sprocId).execute(null, options).block();

            assertThat(executeResponse).isNotNull();
            assertThat(executeResponse.getStatusCode()).isEqualTo(200);
            assertThat(executeResponse.getRequestCharge()).isGreaterThan(0.0);
            assertThinClientEndpointUsed(executeResponse.getDiagnostics());
        } finally {
            try { container.getScripts().getStoredProcedure(sprocId).delete().block(); } catch (Exception e) { logger.warn("Cleanup failed", e); }
        }
    }
}
