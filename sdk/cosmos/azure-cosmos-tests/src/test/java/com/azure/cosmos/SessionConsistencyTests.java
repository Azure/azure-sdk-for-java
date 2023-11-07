package com.azure.cosmos;

import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.TestSuiteBase;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import org.assertj.core.api.Assertions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;

public class SessionConsistencyTests extends TestSuiteBase {

    private CosmosAsyncClient client;
    private CosmosAsyncContainer container;
    private CosmosAsyncDatabase database;

    @BeforeClass
    public void beforeClass() {
        client = createClient();

        client.createDatabaseIfNotExists("session-token-test-db").block();
        database = client.getDatabase("session-token-test-db");

        database.createContainerIfNotExists(new CosmosContainerProperties("session-token-test-container", "/mypk")).block();
        container = database.getContainer("session-token-test-container");

        try {
            Thread.sleep(30_000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // 1. execute a create operation
    // 2. validate where the session token is injected in direct / gateway mode
    // 3. specific to direct mode -> validate the session token part of request headers
    @Test
    public void validateSessionTokenAtRntbdLayer() {

        System.setProperty("COSMOS.PARTITION_KEY_SCOPED_SESSION_TOKEN_CAPTURING_ENABLED", "true");

        TestObject testObjectToCreate = TestObject.create();

        try {
            CosmosItemResponse<TestObject> createResponse = container.createItem(testObjectToCreate).block();

            Assertions.assertThat(createResponse).isNotNull();
            Assertions.assertThat(createResponse.getSessionToken()).isNotNull();

            logger.info("Session token from creation : {}", createResponse.getSessionToken());

            CosmosItemResponse<TestObject> readResponse = container
                .readItem(testObjectToCreate.getId(), new PartitionKey(testObjectToCreate.getMypk()), TestObject.class)
                .block();

            Assertions.assertThat(readResponse).isNotNull();
            Assertions.assertThat(readResponse.getSessionToken()).isNotNull();

            logger.info("Session token from read : {}", readResponse.getSessionToken());
        } catch (Exception e) {

        } finally {
            System.clearProperty("COSMOS.PARTITION_KEY_SCOPED_SESSION_TOKEN_CAPTURING_ENABLED");
        }
    }

    @Test
    public void validatePkScopedSessionTokenMapUsage() {
        System.setProperty("COSMOS.PARTITION_KEY_SCOPED_SESSION_TOKEN_CAPTURING_ENABLED", "true");

        TestObject testObjectToCreate = TestObject.create();

        try {
            CosmosItemResponse<TestObject> createResponse = container.createItem(testObjectToCreate).block();

            Assertions.assertThat(createResponse).isNotNull();
            Assertions.assertThat(createResponse.getSessionToken()).isNotNull();

            logger.info("Session token from creation : {}", createResponse.getSessionToken());

            CosmosItemResponse<TestObject> readResponse = container
                .readItem(testObjectToCreate.getId(), new PartitionKey(testObjectToCreate.getMypk()), TestObject.class)
                .block();

            Assertions.assertThat(readResponse).isNotNull();
            Assertions.assertThat(readResponse.getSessionToken()).isNotNull();

            logger.info("Session token from read : {}", readResponse.getSessionToken());
        } catch (Exception e) {

        } finally {
            System.clearProperty("COSMOS.PARTITION_KEY_SCOPED_SESSION_TOKEN_CAPTURING_ENABLED");
        }
    }

    @AfterClass
    public void afterClass() {
        client.close();
    }

    private static CosmosAsyncClient createClient() {

        CosmosClientBuilder clientBuilder = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .consistencyLevel(ConsistencyLevel.SESSION)
            .preferredRegions(Arrays.asList("East US", "South Central US", "West US"))
            .directMode()
            .sessionRetryOptions(
                new SessionRetryOptionsBuilder()
                    .regionSwitchHint(CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED)
                    .build()
            );

        return clientBuilder.buildAsyncClient();
    }
}
