/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.azure.cosmos;

import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.models.CosmosCommonRequestOptions;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class OperationPoliciesTest extends TestSuiteBase {

    private CosmosClient client;
    private CosmosContainer container;

    private static Properties prop = new Properties();


    public OperationPoliciesTest() {
        super(new CosmosClientBuilder().endpoint(TestConfigurations.HOST)
        .credential(credential)
        .addPolicy((cosmosOperationDetails) -> {

                // Figure out Operation
                CosmosDiagnosticsContext cosmosDiagnosticsContext = cosmosOperationDetails.getDiagnosticsContext();
                String operationType = cosmosDiagnosticsContext.getOperationType();


                if (operationType.equals("Create") || operationType.equals("Read") || operationType.equals("Replace")
                    || operationType.equals("Delete")) {
                    CosmosCommonRequestOptions cosmosCommonRequestOptions = new CosmosCommonRequestOptions()
                        .setCosmosEndToEndLatencyPolicyConfig(new CosmosEndToEndOperationLatencyPolicyConfig(true,
                            Duration.ofSeconds(Long.parseLong(prop.getProperty("timeout.seconds"))),
                            new ThresholdBasedAvailabilityStrategy())).setThresholds(new CosmosDiagnosticsThresholds());;
                    cosmosOperationDetails.setCommonOptions(cosmosCommonRequestOptions);
                }
            }

        ));
        prop.setProperty("timeout.seconds", "20");
    }

    @BeforeClass(groups = {"fast"}, timeOut = SETUP_TIMEOUT)
    public void before_OperationPoliciesTest() {
        assertThat(this.client).isNull();
        this.client = getClientBuilder().buildClient();
        CosmosAsyncContainer asyncContainer = getSharedMultiPartitionCosmosContainer(this.client.asyncClient());
        container = client.getDatabase(asyncContainer.getDatabase().getId()).getContainer(asyncContainer.getId());
    }

    @AfterClass(groups = {"fast"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.client).isNotNull();
        this.client.close();
    }

    @Test(groups = { "fast" }, timeOut = TIMEOUT)
    public void createItem() throws Exception {
        SampleType sampleType = new SampleType("id", "A", "pk");
        CosmosItemResponse<SampleType> itemResponse = container.createItem(sampleType);

        assertThat(itemResponse.getRequestCharge()).isGreaterThan(0);
        validateItemResponse(sampleType, itemResponse);


        prop.setProperty("timeout.seconds", "10");

        SampleType sampleType2 = new SampleType("id2", "B", "pk");
        CosmosItemResponse<SampleType> itemResponse1 = container.createItem(sampleType2);
        validateItemResponse(sampleType2, itemResponse1);

    }

    @Test(groups = { "fast" }, timeOut = TIMEOUT)
    public void deleteItem() throws Exception {
        InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse<InternalObjectNode> itemResponse = container.createItem(properties);
        CosmosItemRequestOptions options = new CosmosItemRequestOptions();



        CosmosItemResponse<?> deleteResponse = container.deleteItem(properties.getId(),
            new PartitionKey(properties.get("mypk")),
            options);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(204);

        prop.setProperty("timeout.seconds", "10");


        itemResponse = container.createItem(properties);


        deleteResponse = container.deleteItem(properties.getId(),
            new PartitionKey(properties.get("mypk")),
            options);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(204);
    }

    @Test(groups = { "fast" }, timeOut = TIMEOUT)
    public void readItem() throws Exception {
        InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse<InternalObjectNode> itemResponse = container.createItem(properties);

        CosmosItemResponse<InternalObjectNode> readResponse1 = container.readItem(properties.getId(),
            new PartitionKey(properties.get("mypk")),
            new CosmosItemRequestOptions(),
            InternalObjectNode.class);
        validateItemResponse(properties, readResponse1);

        prop.setProperty("timeout.seconds", "10");

        CosmosItemResponse<InternalObjectNode> readResponse2 = container.readItem(properties.getId(),
            new PartitionKey(properties.get("mypk")),
            new CosmosItemRequestOptions(),
            InternalObjectNode.class);
        validateItemResponse(properties, readResponse2);

    }

    private InternalObjectNode getDocumentDefinition(String documentId) {
        final String uuid = UUID.randomUUID().toString();
        final InternalObjectNode properties =
            new InternalObjectNode(String.format("{ "
                    + "\"id\": \"%s\", "
                    + "\"mypk\": \"%s\", "
                    + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                    + "}"
                , documentId, uuid));
        return properties;
    }

    private void validateItemResponse(InternalObjectNode containerProperties,
                                      CosmosItemResponse<InternalObjectNode> createResponse) {
        // Basic validation
        assertThat(BridgeInternal.getProperties(createResponse).getId()).isNotNull();
        assertThat(BridgeInternal.getProperties(createResponse).getId())
            .as("check Resource Id")
            .isEqualTo(containerProperties.getId());
    }

    private void validateItemResponse(SampleType sampleType,
                                      CosmosItemResponse<SampleType> createResponse) {
        // Basic validation
        assertThat(BridgeInternal.getProperties(createResponse).getId()).isNotNull();
        assertThat(BridgeInternal.getProperties(createResponse).getId())
            .as("check Resource Id")
            .isEqualTo(sampleType.getId());
        assertThat(createResponse.getDiagnostics().getDiagnosticsContext().getEffectiveConsistencyLevel()).isEqualTo(ConsistencyLevel.SESSION);
    }

    private void validateIdOfItemResponse(String expectedId, CosmosItemResponse<ObjectNode> createResponse) {
        // Basic validation
        assertThat(BridgeInternal.getProperties(createResponse).getId()).isNotNull();
        assertThat(BridgeInternal.getProperties(createResponse).getId())
            .as("check Resource Id")
            .isEqualTo(expectedId);
    }

    private static class PartitionKeyWrapper {
        private String mypk;

        public PartitionKeyWrapper() {
        }

        public String getMypk() {
            return mypk;
        }

        public void setMypk(String mypk) {
            this.mypk = mypk;
        }
    }

    private static class SampleType {
        private String id;
        private String val;
        private String mypk;

        public SampleType() {
        }

        SampleType(String id, String val, String mypk) {
            this.id = id;
            this.val = val;
            this.mypk = mypk;
        }

        public String getId() {
            return this.id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getMypk() {
            return this.mypk;
        }

        public void setMypk(String mypk) {
            this.mypk = mypk;
        }

        public void setVal(String val) {
            this.val = val;
        }

        public String getVal() {
            return this.val;
        }
    }
}
