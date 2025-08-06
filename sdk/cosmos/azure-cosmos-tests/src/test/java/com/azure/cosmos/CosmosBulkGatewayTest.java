// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.models.CosmosBulkExecutionOptions;
import com.azure.cosmos.models.CosmosBulkItemResponse;
import com.azure.cosmos.models.CosmosBulkOperationResponse;
import com.azure.cosmos.models.CosmosBulkOperations;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.models.ThroughputResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosBulkGatewayTest extends BatchTestBase {
    private final static Logger logger = LoggerFactory.getLogger(CosmosBulkGatewayTest.class);

    private CosmosAsyncClient bulkClient;
    private CosmosAsyncDatabase createdDatabase;

    @Factory(dataProvider = "simpleClientBuilderGatewaySession")
    public CosmosBulkGatewayTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"split"}, timeOut = SETUP_TIMEOUT)
    public void before_CosmosBulkAsyncTest() {
        assertThat(this.bulkClient).isNull();
        this.bulkClient = getClientBuilder().buildAsyncClient();
        createdDatabase = getSharedCosmosDatabase(this.bulkClient);
    }

    @AfterClass(groups = {"split"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(this.bulkClient);
    }

    @Test(groups = {"split"}, timeOut = TIMEOUT * 20, retryAnalyzer = SplitTestsRetryAnalyzer.class)
    public void createItem_withBulk_split() throws InterruptedException {
        String containerId = "bulksplittestcontainer_" + UUID.randomUUID();
        int totalRequest = getTotalRequest();
        CosmosContainerProperties containerProperties = new CosmosContainerProperties(containerId, "/mypk");
        CosmosContainerResponse containerResponse = createdDatabase.createContainer(containerProperties).block();
        CosmosAsyncContainer container = createdDatabase.getContainer(containerId);

        Flux<com.azure.cosmos.models.CosmosItemOperation> cosmosItemOperationFlux1 = Flux.range(0, totalRequest).map(i -> {
            String partitionKey = UUID.randomUUID().toString();
            TestDoc testDoc = this.populateTestDoc(partitionKey);

            return CosmosBulkOperations.getCreateItemOperation(testDoc, new PartitionKey(partitionKey));
        });

        Flux<com.azure.cosmos.models.CosmosItemOperation> cosmosItemOperationFlux2 = Flux.range(0, totalRequest).map(i -> {
            String partitionKey = UUID.randomUUID().toString();
            EventDoc eventDoc = new EventDoc(UUID.randomUUID().toString(), 2, 4, "type1", partitionKey);

            return CosmosBulkOperations.getCreateItemOperation(eventDoc, new PartitionKey(partitionKey));
        });

        CosmosBulkExecutionOptions cosmosBulkExecutionOptions = new CosmosBulkExecutionOptions();

        Flux<CosmosBulkOperationResponse<CosmosBulkAsyncTest>> responseFlux =
            container.executeBulkOperations(cosmosItemOperationFlux1, cosmosBulkExecutionOptions);

        AtomicInteger processedDoc = new AtomicInteger(0);
        responseFlux
            .flatMap(cosmosBulkOperationResponse -> {

                processedDoc.incrementAndGet();

                com.azure.cosmos.models.CosmosBulkItemResponse cosmosBulkItemResponse = cosmosBulkOperationResponse.getResponse();
                assertThat(cosmosBulkItemResponse.getStatusCode()).isEqualTo(HttpResponseStatus.CREATED.code());
                assertThat(cosmosBulkItemResponse.getRequestCharge()).isGreaterThan(0);
                assertThat(cosmosBulkItemResponse.getCosmosDiagnostics().toString()).isNotNull();
                assertThat(cosmosBulkItemResponse.getSessionToken()).isNotNull();
                assertThat(cosmosBulkItemResponse.getActivityId()).isNotNull();
                assertThat(cosmosBulkItemResponse.getRequestCharge()).isNotNull();

                return Mono.just(cosmosBulkItemResponse);
            }).blockLast();

        assertThat(processedDoc.get()).isEqualTo(totalRequest);

        // introduce a split and continue bulk operations after split. The partition key range cache has to be
        // refreshed and bulk processing should complete without errors
        List<PartitionKeyRange> partitionKeyRanges = getPartitionKeyRanges(containerId, this.bulkClient);
        // Scale up the throughput for a split
        logger.info("Scaling up throughput for split");
        ThroughputProperties throughputProperties = ThroughputProperties.createManualThroughput(16000);
        ThroughputResponse throughputResponse = container.replaceThroughput(throughputProperties).block();
        logger.info("Throughput replace request submitted for {} ",
                    throughputResponse.getProperties().getManualThroughput());
        throughputResponse = container.readThroughput().block();

        int i = 0;
        // Only wait for 10 minutes for the split to complete
        // If backend does not finish split within 10 minutes
        // something is off in the backend
        // it could be due to limits on how many splits can be executed concurrently etc.
        // nothing that can really be done in the SDK
        while (i < 60 && throughputResponse.isReplacePending()) {
            logger.info("Waiting for split to complete");
            Thread.sleep(10 * 1000);
            throughputResponse = container.readThroughput().block();
            i++;
        }

        if (throughputResponse.isReplacePending()) {
            throw new SplitTimeoutException(
                "Backend did not finish split for container '"
                    + getEndpoint() + "/"
                    + container.getDatabase().getId() + "/"
                    + container.getId()
                    + "' - skipping this test case");
        }

        // Read number of partitions. Should be greater than one
        List<PartitionKeyRange> partitionKeyRangesAfterSplit = getPartitionKeyRanges(containerId,
                                                                                     this.bulkClient);
        assertThat(partitionKeyRangesAfterSplit.size()).isGreaterThan(partitionKeyRanges.size())
            .as("Partition ranges should increase after split");
        logger.info("After split num partitions = {}", partitionKeyRangesAfterSplit.size());

        responseFlux = container.executeBulkOperations(cosmosItemOperationFlux2, cosmosBulkExecutionOptions);

        AtomicInteger processedDoc2 = new AtomicInteger(0);
        responseFlux
            .flatMap(cosmosBulkOperationResponse -> {

                processedDoc2.incrementAndGet();

                CosmosBulkItemResponse cosmosBulkItemResponse = cosmosBulkOperationResponse.getResponse();
                assertThat(cosmosBulkItemResponse.getStatusCode()).isEqualTo(HttpResponseStatus.CREATED.code());
                assertThat(cosmosBulkItemResponse.getRequestCharge()).isGreaterThan(0);
                assertThat(cosmosBulkItemResponse.getCosmosDiagnostics().toString()).isNotNull();
                assertThat(cosmosBulkItemResponse.getSessionToken()).isNotNull();
                assertThat(cosmosBulkItemResponse.getActivityId()).isNotNull();
                assertThat(cosmosBulkItemResponse.getRequestCharge()).isNotNull();

                return Mono.just(cosmosBulkItemResponse);
            }).blockLast();

        assertThat(processedDoc.get()).isEqualTo(totalRequest);
        container.delete().block();
    }

    private List<PartitionKeyRange> getPartitionKeyRanges(
        String containerId, CosmosAsyncClient asyncClient) {
        List<PartitionKeyRange> partitionKeyRanges = new ArrayList<>();
        AsyncDocumentClient asyncDocumentClient = BridgeInternal.getContextClient(asyncClient);
        List<FeedResponse<PartitionKeyRange>> partitionFeedResponseList = asyncDocumentClient
                                                                              .readPartitionKeyRanges("/dbs/" + createdDatabase.getId()
                                                                                                          + "/colls/" + containerId,
                                                                                                      new CosmosQueryRequestOptions())
                                                                              .collectList().block();
        partitionFeedResponseList.forEach(f -> partitionKeyRanges.addAll(f.getResults()));
        return partitionKeyRanges;
    }

    private int getTotalRequest() {
        int countRequest = new Random().nextInt(100) + 200;
        logger.info("Total count of request for this test case: " + countRequest);

        return countRequest;
    }
}
