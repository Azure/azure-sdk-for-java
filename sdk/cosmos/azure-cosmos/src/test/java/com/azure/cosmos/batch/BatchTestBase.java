// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.ISessionToken;
import com.azure.cosmos.implementation.SessionTokenHelper;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.directconnectivity.WFConstants;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.rx.TestSuiteBase;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.Random;
import java.util.UUID;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class BatchTestBase extends TestSuiteBase {

    private Random random = new Random();
    String partitionKey1 = "TBD1";

    // Documents in partitionKey1
    TestDoc TestDocPk1ExistingA;
    TestDoc TestDocPk1ExistingB ;
    TestDoc TestDocPk1ExistingC;
    TestDoc TestDocPk1ExistingD;

    public BatchTestBase(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    CosmosAsyncContainer createSharedThroughputContainer(CosmosAsyncClient client) {
        CosmosAsyncContainer sharedThroughputContainer = null;
        CosmosDatabaseResponse cosmosDatabaseResponse = client.createDatabaseIfNotExists(
            "Shared_" + UUID.randomUUID().toString(),
            ThroughputProperties.createManualThroughput(12000)).block();

        CosmosAsyncDatabase db = client.getDatabase(cosmosDatabaseResponse.getProperties().getId());

        for (int index = 0; index < 5; index++) {

            CosmosContainerResponse cosmosContainerResponse = db.createContainerIfNotExists(getCollectionDefinition()).block();
            assertTrue(Boolean.parseBoolean(cosmosContainerResponse.getResponseHeaders().get(WFConstants.BackendHeaders.SHARE_THROUGHPUT)));

            if (index == 2) {
                sharedThroughputContainer = db.getContainer(cosmosContainerResponse.getProperties().getId());
            }
        }

        return sharedThroughputContainer;
    }

    void createJsonTestDocsAsync(CosmosAsyncContainer container) {
        this.TestDocPk1ExistingA =  this.createJsonTestDocAsync(container, this.partitionKey1);
        this.TestDocPk1ExistingB =  this.createJsonTestDocAsync(container, this.partitionKey1);
        this.TestDocPk1ExistingC =  this.createJsonTestDocAsync(container, this.partitionKey1);
        this.TestDocPk1ExistingD =  this.createJsonTestDocAsync(container, this.partitionKey1);
    }

    TestDoc populateTestDoc(String partitionKey) {
        return populateTestDoc(partitionKey, 20);
    }

    TestDoc populateTestDoc(String partitionKey, int minDesiredSize) {
        String description = StringUtils.repeat("x", minDesiredSize);
        return new TestDoc(UUID.randomUUID().toString(), this.random.nextInt(), description, partitionKey);
    }

    public TestDoc populateTestDoc(String id, String partitionKey) {
        String description = StringUtils.repeat("x", 20);
        return new TestDoc(id, this.random.nextInt(), description, partitionKey);
    }

    TestDoc getTestDocCopy(TestDoc testDoc) {
        return new TestDoc(testDoc.getId(), testDoc.getCost(), testDoc.getDescription(), testDoc.getStatus());
    }

    void verifyByReadAsync(CosmosAsyncContainer container, TestDoc doc) {
        verifyByReadAsync(container, doc, null);
    }

    void verifyByReadAsync(CosmosAsyncContainer container, TestDoc doc, String eTag) {
        PartitionKey partitionKey = this.getPartitionKey(doc.getStatus());

        CosmosItemResponse<TestDoc> response = container.readItem(doc.getId(), partitionKey, TestDoc.class).block();

        assertEquals(HttpResponseStatus.OK.code(), response.getStatusCode());
        assertEquals(doc, response.getItem());

        if (eTag != null) {
            assertEquals(eTag, response.getETag());
        }
    }

    void verifyNotFoundAsync(CosmosAsyncContainer container, TestDoc doc) {
        String id = doc.getId();
        PartitionKey partitionKey = this.getPartitionKey(doc.getStatus());

        try {
            CosmosItemResponse<TestDoc> response =  container.readItem(id, partitionKey, TestDoc.class).block();

            // Gateway returns response instead of exception
            assertEquals(HttpResponseStatus.NOT_FOUND.code(), response.getStatusCode());
        } catch (CosmosException ex) {
            assertEquals(HttpResponseStatus.NOT_FOUND.code(), ex.getStatusCode());
        }
    }

    PartitionKey getPartitionKey(String partitionKey) {
        return new PartitionKey(partitionKey);
    }

    private TestDoc createJsonTestDocAsync(CosmosAsyncContainer container, String partitionKey) {
        return createJsonTestDocAsync(container, partitionKey, 20);
    }

    private TestDoc createJsonTestDocAsync(CosmosAsyncContainer container, String partitionKey, int minDesiredSize) {
        TestDoc doc = this.populateTestDoc(partitionKey, minDesiredSize);
        CosmosItemResponse<TestDoc> createResponse = container.createItem(doc, this.getPartitionKey(partitionKey), null).block();
        assertEquals(HttpResponseStatus.CREATED.code(), createResponse.getStatusCode());
        return doc;
    }

    public Random getRandom() {
        return random;
    }

    ISessionToken getSessionToken(String sessionToken) {
        String[] tokenParts = sessionToken.split(":");
        return SessionTokenHelper.parse(tokenParts[1]);
    }

    public static class TestDoc {
        public String id;
        public int cost;
        public String description;

        @JsonProperty("mypk")
        public String status;

        public TestDoc() {

        }

        public TestDoc(String id, int cost, String description, String status) {
            this.id = id;
            this.cost = cost;
            this.description = description;
            this.status = status;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            TestDoc testDoc2 = (TestDoc) obj;
            return (this.getId().equals(testDoc2.getId()) &&
                this.getCost() == testDoc2.getCost()) &&
                this.getDescription().equals(testDoc2.getDescription()) &&
                this.getStatus().equals(testDoc2.getStatus());
        }

        @Override
        public int hashCode() {
            int hashCode = 1652434776;
            hashCode = (hashCode * -1521134295) + this.id.hashCode();
            hashCode = (hashCode * -1521134295) + this.cost;
            hashCode = (hashCode * -1521134295) + this.description.hashCode();
            hashCode = (hashCode * -1521134295) + this.status.hashCode();
            return hashCode;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getCost() {
            return cost;
        }

        public void setCost(int cost) {
            this.cost = cost;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
