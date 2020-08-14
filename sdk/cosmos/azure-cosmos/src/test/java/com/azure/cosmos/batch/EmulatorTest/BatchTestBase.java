// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch.EmulatorTest;

import com.azure.cosmos.*;
import com.azure.cosmos.implementation.ISessionToken;
import com.azure.cosmos.implementation.SessionTokenHelper;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.directconnectivity.WFConstants;
import com.azure.cosmos.models.*;
import com.azure.cosmos.rx.TestSuiteBase;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.*;

public class BatchTestBase extends TestSuiteBase {

    private CosmosAsyncClient client;
    private CosmosAsyncClient bulkClient;
    private CosmosAsyncClient gatewayClient;

    private CosmosAsyncDatabase database;
    private CosmosAsyncDatabase bulkDatabase;
    private CosmosAsyncDatabase sharedThroughputDatabase;
    private CosmosAsyncDatabase gatewayDatabase;

    CosmosAsyncContainer jsonContainer;
    CosmosAsyncContainer bulkContainer;
    CosmosAsyncContainer gatewayJsonContainer;
    CosmosAsyncContainer sharedThroughputContainer;

    private PartitionKeyDefinition partitionKeyDefinition;
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

    @BeforeClass(groups = {"simple"}, timeOut = SETUP_TIMEOUT)
    public void before_BatchTestBase() {
        initializeDirectContainers();
        initializeBulkContainers();
        initializeGatewayContainers();
        initializeSharedThroughputContainer();
    }

    @AfterClass(groups = {"simple"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.client).isNotNull();

        if (this.database != null) {
            this.database.delete().block();
        }

        if(this.bulkDatabase != null) {
            this.bulkDatabase.delete().block();
        }

        if (this.sharedThroughputDatabase != null)
        {
            this.sharedThroughputDatabase.delete().block();
        }

        this.client.close();
    }

    private void initializeDirectContainers() {

        assertThat(this.client).isNull();

        this.client = getClientBuilder()
            .directMode()
            .contentResponseOnWriteEnabled(true)
            .buildAsyncClient();

        database = client.getDatabase(client.createDatabaseIfNotExists(UUID.randomUUID().toString()).block().getProperties().getId());

        PartitionKeyDefinition partitionKeyDefinition = new PartitionKeyDefinition();
        partitionKeyDefinition.getPaths().add("/status");

        // Create a container with at least 2 physical partitions for effective cross-partition testing
        this.jsonContainer = database.getContainer(
            database.createContainerIfNotExists(
                new CosmosContainerProperties(
                    UUID.randomUUID().toString(),
                    partitionKeyDefinition),
                ThroughputProperties.createManualThroughput(12000)).block().getProperties().getId());

        this.partitionKeyDefinition = this.jsonContainer.getCollectionInfoAsync().block().getPartitionKey();
    }

    private void initializeBulkContainers() {

        assertThat(this.bulkClient).isNull();

        this.bulkClient = getClientBuilder()
            .directMode()
            .contentResponseOnWriteEnabled(true)
            .bulkExecutionEnabled(true)
            .buildAsyncClient();

        bulkDatabase = this.bulkClient.getDatabase(
            this.bulkClient.createDatabaseIfNotExists(UUID.randomUUID().toString()).block().getProperties().getId());

        this.bulkContainer = bulkDatabase.getContainer(
            bulkDatabase.createContainerIfNotExists(
                new CosmosContainerProperties(
                    "bulk_" + UUID.randomUUID().toString(),
                    this.partitionKeyDefinition),
                ThroughputProperties.createManualThroughput(10000)).block().getProperties().getId());
    }

    private void initializeGatewayContainers() {

        assertThat(this.gatewayClient).isNull();
        this.gatewayClient =  getClientBuilder()
            .contentResponseOnWriteEnabled(true)
            .gatewayMode()
            .buildAsyncClient();
        this.gatewayDatabase = this.gatewayClient.getDatabase(this.database.getId());

        this.gatewayJsonContainer = this.gatewayDatabase.getContainer(this.jsonContainer.getId());
    }

    private void initializeSharedThroughputContainer() {
        CosmosDatabaseResponse cosmosDatabaseResponse = this.client.createDatabaseIfNotExists(
            "Shared_" + UUID.randomUUID().toString(),
            ThroughputProperties.createManualThroughput(12000)).block();

        CosmosAsyncDatabase db = this.client.getDatabase(cosmosDatabaseResponse.getProperties().getId());

        for (int index = 0; index < 5; index++) {

            CosmosContainerResponse cosmosContainerResponse = db.createContainerIfNotExists(
                new CosmosContainerProperties(
                    UUID.randomUUID().toString(),
                    this.partitionKeyDefinition)).block();

            assertTrue(Boolean.parseBoolean(cosmosContainerResponse.getResponseHeaders().get(WFConstants.BackendHeaders.SHARE_THROUGHPUT)));

            if (index == 2) {
                this.sharedThroughputContainer = db.getContainer(cosmosContainerResponse.getProperties().getId());
            }
        }

        this.sharedThroughputDatabase = db;
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

    protected  TestDoc populateTestDoc(String partitionKey, int minDesiredSize) {
        String description = StringUtils.repeat("x", minDesiredSize);
        return new TestDoc(UUID.randomUUID().toString(), this.random.nextInt(), description, partitionKey);
    }

    protected TestDoc populateTestDoc(String id, String partitionKey) {
        String description = StringUtils.repeat("x", 20);
        return new TestDoc(id, this.random.nextInt(), description, partitionKey);
    }

    protected TestDoc getTestDocCopy(TestDoc testDoc) {
        return new TestDoc(testDoc.getId(), testDoc.getCost(), testDoc.getDescription(), testDoc.getStatus());
    }

    protected void verifyByReadAsync(CosmosAsyncContainer container, TestDoc doc) {
        verifyByReadAsync(container, doc, null);
    }

    protected void verifyByReadAsync(CosmosAsyncContainer container, TestDoc doc, String eTag) {
        PartitionKey partitionKey = this.getPartitionKey(doc.getStatus());

        CosmosItemResponse<TestDoc> response = container.readItem(doc.getId(), partitionKey, TestDoc.class).block();

        assertEquals(HttpResponseStatus.OK.code(), response.getStatusCode());
        assertEquals(doc, response.getItem());

        if (eTag != null) {
            assertEquals(eTag, response.getETag());
        }
    }

    protected void verifyNotFoundAsync(CosmosAsyncContainer container, TestDoc doc) {
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

    protected PartitionKey getPartitionKey(String partitionKey) {
        return new PartitionKey(partitionKey);
    }

    protected TestDoc createJsonTestDocAsync(CosmosAsyncContainer container, String partitionKey) {
        return createJsonTestDocAsync(container, partitionKey, 20);
    }

    protected TestDoc createJsonTestDocAsync(CosmosAsyncContainer container, String partitionKey, int minDesiredSize) {
        TestDoc doc = this.populateTestDoc(partitionKey, minDesiredSize);
        CosmosItemResponse<TestDoc> createResponse = container.createItem(doc, this.getPartitionKey(partitionKey), null).block();
        assertEquals(HttpResponseStatus.CREATED.code(), createResponse.getStatusCode());
        return doc;
    }

    public Random getRandom() {
        return random;
    }

    public ISessionToken getSessionToken(String sessionToken) {
        String[] tokenParts = sessionToken.split(":");
        return SessionTokenHelper.parse(tokenParts[1]);
    }

    public static class TestDoc {
        public String id;
        public int cost;
        public String description;
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
