// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

//  TODO: (kuthapar) - to be removed after testing.
public class ChangeFeedFullFidelityTest {
    private static final String databaseId = "SampleDatabase";
    private static final String containerId = "GreenTaxiRecords";
    private static final Logger logger = LoggerFactory.getLogger(ChangeFeedFullFidelityTest.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static CosmosAsyncDatabase database;
    private static CosmosAsyncContainer container;
    private static Map<String, List<JsonNode>> changeFeedMap = new ConcurrentHashMap<>();


    public static void main(String[] args) {
        setup();
        runChangeFeedFullFidelityFromNow();
    }

    private static void setup() {
        logger.info("Setting up");

        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        CosmosAsyncClient cosmosAsyncClient = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .contentResponseOnWriteEnabled(true)
            .buildAsyncClient();

        database = cosmosAsyncClient.getDatabase(databaseId);
        container = database.getContainer(containerId);
    }

    private static void runChangeFeedFullFidelityFromNow() {

        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1);
        executorService.scheduleAtFixedRate(ChangeFeedFullFidelityTest::checkChangeFeedMapDetails,
            30, 30, TimeUnit.SECONDS);

        CosmosChangeFeedRequestOptions options = CosmosChangeFeedRequestOptions
            .createForProcessingFromNow(FeedRange.forFullRange());
        options.fullFidelity();

        String continuationToken = "";

        CosmosPagedFlux<JsonNode> cosmosPagedFlux = container.queryChangeFeed(options, JsonNode.class);
        Iterator<FeedResponse<JsonNode>> iterator = cosmosPagedFlux.byPage().toIterable().iterator();

        do {
            while (iterator.hasNext()) {
                FeedResponse<JsonNode> next = iterator.next();
                List<JsonNode> jsonNodes = next.getResults();
                for (JsonNode item : jsonNodes) {
                    try {
                        String operationType = item.get("metadata").get("operationType").asText();
                        if (!changeFeedMap.containsKey(operationType)) {
                            changeFeedMap.put(operationType, new ArrayList<>());
                        }
                        changeFeedMap.get(operationType).add(item);
                    }
                    catch (Exception e) {
                        if (item == null)  {
                            logger.error("Received null item ", e);
                        } else {
                            logger.error("Error occurred for item : {}", item.toPrettyString(), e);
                        }
                    }
                }
                continuationToken = next.getContinuationToken();
            }
            options = CosmosChangeFeedRequestOptions.createForProcessingFromContinuation(continuationToken);
            cosmosPagedFlux = container.queryChangeFeed(options, JsonNode.class);
            iterator = cosmosPagedFlux.byPage().toIterable().iterator();
        } while (continuationToken != null);
    }

    private static void checkChangeFeedMapDetails() {
        logger.info("Change feed map details are");
        changeFeedMap.forEach((key, value) -> {
            logger.info("Operation type : {}, number of changes : {}", key, value.size());
        });
    }
}
