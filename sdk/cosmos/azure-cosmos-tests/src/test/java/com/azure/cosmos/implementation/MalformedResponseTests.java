// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.TestObject;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Factory;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

@Ignore("MalformedResponseTests is safe to run in isolation as it leverages Reflection to override the deserializer.")
public class MalformedResponseTests extends TestSuiteBase {

    @Factory(dataProvider = "clientBuildersWithSessionConsistency")
    public MalformedResponseTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeSuite(groups = {"emulator"}, alwaysRun = true)
    public void beforeSuite() {
        System.setProperty("COSMOS.CHARSET_DECODER_ERROR_ACTION_ON_MALFORMED_INPUT", "IGNORE");
        super.beforeSuite();
    }

    @Test(groups = { "emulator" })
    public void validateCosmosExceptionThrownOnMalformedResponse() throws NoSuchFieldException, IllegalAccessException {

        CosmosAsyncClient cosmosAsyncClient = null;
        ObjectMapper originalMapper = null;

        try {
            cosmosAsyncClient = getClientBuilder()
                .key(TestConfigurations.MASTER_KEY)
                .endpoint(TestConfigurations.HOST)
                .buildAsyncClient();
            CosmosAsyncContainer cosmosAsyncContainer = getSharedSinglePartitionCosmosContainer(cosmosAsyncClient);

            TestObject testObject = TestObject.create();
            cosmosAsyncContainer.createItem(testObject).block();

            Field field = Utils.class.getDeclaredField("simpleObjectMapper");
            field.setAccessible(true);

            // Save original
            originalMapper = (ObjectMapper) field.get(null);

            // Create a bad ObjectMapper
            ObjectMapper badMapper = new FailingObjectMapper();
            // Override
            field.set(null, badMapper);

            cosmosAsyncContainer.readItem(testObject.getId(), new PartitionKey(testObject.getMypk()), TestObject.class).block();
            fail("The read operation should have failed");
        } catch (CosmosException cosmosException) {
            validate(cosmosException);
        } catch (IllegalAccessException e) {
            fail("An IllegalAccessException shouldn't have occurred", e);
        } finally {
            // Restore original
            Field field = Utils.class.getDeclaredField("simpleObjectMapper");
            field.setAccessible(true);
            field.set(null, originalMapper);
            field.setAccessible(false);
        }
    }

    @Test(groups = {"emulator"})
    public void validateCosmosExceptionThrownOnMalformedResponseWhenFallbackDecoderSet() throws NoSuchFieldException, IllegalAccessException {

        CosmosAsyncClient cosmosAsyncClient = null;
        ObjectMapper originalMapper = null;

        try {

            cosmosAsyncClient = getClientBuilder()
                .key(TestConfigurations.MASTER_KEY)
                .endpoint(TestConfigurations.HOST)
                .buildAsyncClient();
            CosmosAsyncContainer cosmosAsyncContainer = getSharedSinglePartitionCosmosContainer(cosmosAsyncClient);

            TestObject testObject = TestObject.create();
            cosmosAsyncContainer.createItem(testObject).block();

            Field field = Utils.class.getDeclaredField("simpleObjectMapper");
            field.setAccessible(true);

            // Save original
            originalMapper = (ObjectMapper) field.get(null);

            // Create a bad ObjectMapper
            ObjectMapper badMapper = new FailingObjectMapper();
            // Override
            field.set(null, badMapper);

            cosmosAsyncContainer.readItem(testObject.getId(), new PartitionKey(testObject.getMypk()), TestObject.class).block();
            fail("The read operation should have failed");
        } catch (CosmosException cosmosException) {
            validate(cosmosException);
        } catch (IllegalAccessException e) {
            fail("An IllegalAccessException shouldn't have occurred", e);
        } finally {
            // Restore original
            Field field = Utils.class.getDeclaredField("simpleObjectMapper");
            field.setAccessible(true);
            field.set(null, originalMapper);
            field.setAccessible(false);
        }
    }

    private class FailingObjectMapper extends ObjectMapper {
        @Override
        public JsonNode readTree(byte[] bytes) throws IOException {
            throw new IOException("Simulated failure");
        }

        @Override
        public JsonNode readTree(String content) throws JsonProcessingException {
            throw new JsonParseException("Simulated failure");
        }
    }

    @AfterSuite(groups = {"emulator"}, alwaysRun = true)
    public void afterSuite() {
        System.setProperty("COSMOS.CHARSET_DECODER_ERROR_ACTION_ON_MALFORMED_INPUT", "IGNORE");
        super.afterSuite();
    }

    private static void validate(CosmosException cosmosException) {
        assertThat(cosmosException.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR);
        assertThat(cosmosException.getSubStatusCode()).isEqualTo(HttpConstants.SubStatusCodes.FAILED_TO_PARSE_SERVER_RESPONSE);
        assertThat(cosmosException.getDiagnostics()).isNotNull();
        assertThat(cosmosException.getResponseHeaders()).isNotNull();
        assertThat(cosmosException.getResponseHeaders()).isNotEmpty();
    }
}
