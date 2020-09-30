// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.ISessionToken;
import com.azure.cosmos.implementation.SessionTokenHelper;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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

    void createJsonTestDocsAsync(CosmosContainer container) {
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

    TestDoc getTestDocCopy(TestDoc testDoc) {
        return new TestDoc(testDoc.getId(), testDoc.getCost(), testDoc.getDescription(), testDoc.getStatus());
    }

    void verifyByReadAsync(CosmosContainer container, TestDoc doc) {
        verifyByReadAsync(container, doc, null);
    }

    void verifyByReadAsync(CosmosContainer container, TestDoc doc, String eTag) {
        PartitionKey partitionKey = this.getPartitionKey(doc.getStatus());

        CosmosItemResponse<TestDoc> response = container.readItem(doc.getId(), partitionKey, TestDoc.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());
        assertThat(response.getItem()).isEqualTo(doc);

        if (eTag != null) {
            assertThat(response.getETag()).isEqualTo(eTag);
        }
    }

    void verifyNotFoundAsync(CosmosContainer container, TestDoc doc) {
        String id = doc.getId();
        PartitionKey partitionKey = this.getPartitionKey(doc.getStatus());

        try {
            CosmosItemResponse<TestDoc> response =  container.readItem(id, partitionKey, TestDoc.class);

            // Gateway returns response instead of exception
            assertThat(response.getStatusCode()).isEqualTo(HttpResponseStatus.NOT_FOUND.code());
        } catch (CosmosException ex) {
            assertThat(ex.getStatusCode()).isEqualTo(HttpResponseStatus.NOT_FOUND.code());
        }
    }

    PartitionKey getPartitionKey(String partitionKey) {
        return new PartitionKey(partitionKey);
    }

    private TestDoc createJsonTestDocAsync(CosmosContainer container, String partitionKey) {
        return createJsonTestDocAsync(container, partitionKey, 20);
    }

    private TestDoc createJsonTestDocAsync(CosmosContainer container, String partitionKey, int minDesiredSize) {
        TestDoc doc = this.populateTestDoc(partitionKey, minDesiredSize);
        CosmosItemResponse<TestDoc> createResponse = container.createItem(doc, this.getPartitionKey(partitionKey), null);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpResponseStatus.CREATED.code());
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
