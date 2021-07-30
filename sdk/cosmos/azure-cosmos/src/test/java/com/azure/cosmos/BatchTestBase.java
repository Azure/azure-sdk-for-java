// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.ISessionToken;
import com.azure.cosmos.implementation.SessionTokenHelper;
import com.azure.cosmos.implementation.VectorSessionToken;
import com.azure.cosmos.implementation.apachecommons.collections.map.UnmodifiableMap;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.Offset;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class BatchTestBase extends TestSuiteBase {

    private Random random = new Random();
    String partitionKey1 = "TBD1";

    // items in partitionKey1
    TestDoc TestDocPk1ExistingA;
    TestDoc TestDocPk1ExistingB ;
    TestDoc TestDocPk1ExistingC;
    TestDoc TestDocPk1ExistingD;

    public BatchTestBase(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    void createJsonTestDocs(CosmosContainer container) {
        this.TestDocPk1ExistingA =  this.createJsonTestDoc(container, this.partitionKey1);
        this.TestDocPk1ExistingB =  this.createJsonTestDoc(container, this.partitionKey1);
        this.TestDocPk1ExistingC =  this.createJsonTestDoc(container, this.partitionKey1);
        this.TestDocPk1ExistingD =  this.createJsonTestDoc(container, this.partitionKey1);
    }

    void createJsonTestDocs(CosmosAsyncContainer container) {
        this.TestDocPk1ExistingA =  this.createJsonTestDoc(container, this.partitionKey1);
        this.TestDocPk1ExistingB =  this.createJsonTestDoc(container, this.partitionKey1);
        this.TestDocPk1ExistingC =  this.createJsonTestDoc(container, this.partitionKey1);
        this.TestDocPk1ExistingD =  this.createJsonTestDoc(container, this.partitionKey1);
    }

    TestDoc populateTestDoc(String partitionKey) {
        return populateTestDoc(partitionKey, 20);
    }

    TestDoc populateTestDoc(String partitionKey, int cost, int minDesiredSize) {
        String description = StringUtils.repeat("x", minDesiredSize);
        return new TestDoc(cost + UUID.randomUUID().toString(), cost, description, partitionKey);
    }

    TestDoc populateTestDoc(String partitionKey, int minDesiredSize) {
        String description = StringUtils.repeat("x", minDesiredSize);
        return new TestDoc(UUID.randomUUID().toString(), this.random.nextInt(), description, partitionKey);
    }

    TestDoc getTestDocCopy(TestDoc testDoc) {
        return new TestDoc(testDoc.getId(), testDoc.getCost(), testDoc.getDescription(), testDoc.getStatus());
    }

    void verifyByRead(CosmosAsyncContainer container, TestDoc doc) {
        verifyByRead(container, doc, null);
    }

    void verifyByRead(CosmosAsyncContainer container, TestDoc doc, String eTag) {
        PartitionKey partitionKey = this.getPartitionKey(doc.getStatus());

        CosmosItemResponse<TestDoc> response = container.readItem(doc.getId(), partitionKey, TestDoc.class).block();

        assertThat(response.getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());
        assertThat(response.getItem()).isEqualTo(doc);

        if (eTag != null) {
            assertThat(response.getETag()).isEqualTo(eTag);
        }
    }

    void verifyByRead(CosmosContainer container, TestDoc doc) {
        verifyByRead(container, doc, null);
    }

    void verifyByRead(CosmosContainer container, TestDoc doc, String eTag) {
        PartitionKey partitionKey = this.getPartitionKey(doc.getStatus());

        CosmosItemResponse<TestDoc> response = container.readItem(doc.getId(), partitionKey, TestDoc.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());
        assertThat(response.getItem()).isEqualTo(doc);

        if (eTag != null) {
            assertThat(response.getETag()).isEqualTo(eTag);
        }
    }

    void verifyNotFound(CosmosContainer container, TestDoc doc) {
        String id = doc.getId();
        PartitionKey partitionKey = this.getPartitionKey(doc.getStatus());

        try {
            CosmosItemResponse<TestDoc> response =  container.readItem(id, partitionKey, TestDoc.class);
            Assertions.fail("Should throw NOT_FOUND exception");
        } catch (CosmosException ex) {
            assertThat(ex.getStatusCode()).isEqualTo(HttpResponseStatus.NOT_FOUND.code());
        }
    }

    PartitionKey getPartitionKey(String partitionKey) {
        return new PartitionKey(partitionKey);
    }

    private TestDoc createJsonTestDoc(CosmosContainer container, String partitionKey) {
        return createJsonTestDoc(container, partitionKey, 20);
    }

    TestDoc createJsonTestDoc(CosmosContainer container, String partitionKey, int minDesiredSize) {
        TestDoc doc = this.populateTestDoc(partitionKey, minDesiredSize);
        CosmosItemResponse<TestDoc> createResponse = container.createItem(doc, this.getPartitionKey(partitionKey), null);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpResponseStatus.CREATED.code());
        return doc;
    }

    private TestDoc createJsonTestDoc(CosmosAsyncContainer container, String partitionKey) {
        return createJsonTestDoc(container, partitionKey, 20);
    }

    TestDoc createJsonTestDoc(CosmosAsyncContainer container, String partitionKey, int minDesiredSize) {
        TestDoc doc = this.populateTestDoc(partitionKey, minDesiredSize);
        CosmosItemResponse<TestDoc> createResponse = container.createItem(doc, this.getPartitionKey(partitionKey), null).block();
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpResponseStatus.CREATED.code());
        return doc;
    }

    public Random getRandom() {
        return random;
    }

    ISessionToken getSessionToken(String sessionToken) {
        String[] tokenParts = org.apache.commons.lang3.StringUtils.split(sessionToken, ':');
        return SessionTokenHelper.parse(tokenParts[1]);
    }

    String getDifferentLSNToken(String token, long lsnDifferent) throws Exception {
        String[] tokenParts = org.apache.commons.lang3.StringUtils.split(token, ':');
        ISessionToken sessionToken = SessionTokenHelper.parse(tokenParts[1]);
        ISessionToken differentSessionToken = createSessionToken(sessionToken, sessionToken.getLSN() + lsnDifferent);
        return String.format("%s:%s", tokenParts[0], differentSessionToken.convertToString());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static ISessionToken createSessionToken(ISessionToken from, long globalLSN) throws Exception {
        // Creates session token with specified GlobalLSN
        if (from instanceof VectorSessionToken) {
            VectorSessionToken fromSessionToken = (VectorSessionToken) from;
            Field fieldVersion = VectorSessionToken.class.getDeclaredField("version");
            fieldVersion.setAccessible(true);
            Long version = (Long) fieldVersion.get(fromSessionToken);

            Field fieldLocalLsnByRegion = VectorSessionToken.class.getDeclaredField("localLsnByRegion");
            fieldLocalLsnByRegion.setAccessible(true);
            UnmodifiableMap<Integer, Long> localLsnByRegion = (UnmodifiableMap<Integer, Long>) fieldLocalLsnByRegion.get(fromSessionToken);

            Constructor<VectorSessionToken> constructor = VectorSessionToken.class.getDeclaredConstructor(long.class, long.class, UnmodifiableMap.class);
            constructor.setAccessible(true);
            VectorSessionToken vectorSessionToken = constructor.newInstance(version, globalLSN, localLsnByRegion);
            return vectorSessionToken;
        } else {
            throw new IllegalArgumentException();
        }
    }

    void verifyBatchProcessed(TransactionalBatchResponse batchResponse, int numberOfOperations) {
        this.verifyBatchProcessed(batchResponse, numberOfOperations, HttpResponseStatus.OK);
    }

    void verifyBatchProcessed(TransactionalBatchResponse batchResponse, int numberOfOperations, HttpResponseStatus expectedStatusCode) {
        assertThat(batchResponse).isNotNull();
        assertThat(batchResponse.getStatusCode())
            .as("Batch server response had StatusCode {0} instead of {1} expected and had ErrorMessage {2}",
                batchResponse.getStatusCode(), expectedStatusCode.code())
            .isEqualTo(expectedStatusCode.code());

        assertThat(batchResponse.size()).isEqualTo(numberOfOperations);
        assertThat(batchResponse.getRequestCharge()).isPositive();
        assertThat(batchResponse.getDiagnostics().toString()).isNotEmpty();

        // Allow a delta since we round both the total charge and the individual operation
        // charges to 2 decimal places.
        assertThat(batchResponse.getRequestCharge())
            .isCloseTo(batchResponse.getResults().stream().mapToDouble(TransactionalBatchOperationResult::getRequestCharge).sum(),
                Offset.offset(0.1));
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

        @Override
        public String toString() {
            return "TestDoc{" +
                "id='" + id + '\'' +
                ", cost=" + cost +
                ", description='" + description + '\'' +
                ", status='" + status + '\'' +
                '}';
        }
    }

    public static class EventDoc {

        public String id;
        int clicks;
        int views;
        String type;

        @JsonProperty("mypk")
        public String partitionKey;


        public EventDoc() {

        }

        public EventDoc(String id, int clicks, int views, String type, String partitionKey) {
            this.id = id;
            this.clicks = clicks;
            this.views = views;
            this.type = type;
            this.partitionKey = partitionKey;
        }

        public String getId() {
            return id;
        }

        public int getClicks() {
            return clicks;
        }

        public int getViews() {
            return views;
        }

        public String getType() {
            return type;
        }

        public String getPartitionKey() {
            return partitionKey;
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setClicks(int clicks) {
            this.clicks = clicks;
        }

        public void setViews(int views) {
            this.views = views;
        }

        public void setType(String type) {
            this.type = type;
        }

        public void setPartitionKey(String partitionKey) {
            this.partitionKey = partitionKey;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EventDoc eventDoc = (EventDoc) o;
            return clicks == eventDoc.clicks &&
                views == eventDoc.views &&
                Objects.equals(id, eventDoc.id) &&
                Objects.equals(type, eventDoc.type) &&
                Objects.equals(partitionKey, eventDoc.partitionKey);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, clicks, views, type, partitionKey);
        }

        @Override
        public String toString() {
            return "EventDoc{" +
                "id='" + id + '\'' +
                ", clicks=" + clicks +
                ", views=" + views +
                ", type='" + type + '\'' +
                ", partitionKey='" + partitionKey + '\'' +
                '}';
        }
    }
}
