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

import java.util.Iterator;

public class ChangeFeedFullFidelityTest {
    private static final String databaseId = "SampleDatabase";
    private static final String containerId = "GreenTaxiRecords";
    private static final Logger logger = LoggerFactory.getLogger(ChangeFeedFullFidelityTest.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static CosmosAsyncDatabase database;
    private static CosmosAsyncContainer container;


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
        CosmosChangeFeedRequestOptions options = CosmosChangeFeedRequestOptions
            .createForProcessingFromNow(FeedRange.forFullRange());
        options.fullFidelity();

        String continuationToken = "";

        CosmosPagedFlux<JsonNode> jsonNodes = container.queryChangeFeed(options, JsonNode.class);
        Iterator<FeedResponse<JsonNode>> iterator = jsonNodes.byPage().toIterable().iterator();

        do {
            while (iterator.hasNext()) {
                FeedResponse<JsonNode> next = iterator.next();
                logger.info("Results are : {}", next.getResults());
                continuationToken = next.getContinuationToken();
            }
            options = CosmosChangeFeedRequestOptions.createForProcessingFromContinuation(continuationToken);
            jsonNodes = container.queryChangeFeed(options, JsonNode.class);
            iterator = jsonNodes.byPage().toIterable().iterator();
        } while (continuationToken != null);
    }
}
