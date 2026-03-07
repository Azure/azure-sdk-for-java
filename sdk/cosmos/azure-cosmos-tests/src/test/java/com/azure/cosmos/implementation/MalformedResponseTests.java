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

@Ignore("MalformedResponseTests is only safe to run in isolation as it leverages Reflection to override the ObjectMapper instance responsible for deserialization.")
public class MalformedResponseTests extends TestSuiteBase {

    @Factory(dataProvider = "clientBuildersWithSessionConsistency")
    public MalformedResponseTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeSuite(groups = {"emulator"})
    public void beforeSuite() {
        System.setProperty("COSMOS.CHARSET_DECODER_ERROR_ACTION_ON_MALFORMED_INPUT", "IGNORE");
        System.setProperty("COSMOS.IS_NON_PARSEABLE_DOCUMENT_LOGGING_ENABLED", "true");
        super.beforeSuite();
    }

    /**
     * Validate that a CosmosException is thrown with the appropriate status code and sub-status code
     * when the response from the server is malformed and cannot be deserialized
     * and fallback decoder is set / not set
     * <p>
     * NOTE: Run this test with MalformedResponseTests#beforeSuite and MalformedResponseTests#afterSuite commented out for no fallback decoder.
     * NOTE: Run this test with MalformedResponseTests#beforeSuite and MalformedResponseTests#afterSuite enabled for fallback decoder.
     * */
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
            assertThat(cosmosException.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.BADREQUEST);
            assertThat(cosmosException.getSubStatusCode()).isEqualTo(HttpConstants.SubStatusCodes.FAILED_TO_PARSE_SERVER_RESPONSE);
            assertThat(cosmosException.getDiagnostics()).isNotNull();
            assertThat(cosmosException.getResponseHeaders()).isNotNull();
            assertThat(cosmosException.getResponseHeaders()).isNotEmpty();
        } catch (IllegalAccessException e) {
            fail("An IllegalAccessException shouldn't have occurred", e);
        } finally {
            // Restore original
            Field field = Utils.class.getDeclaredField("simpleObjectMapper");
            field.setAccessible(true);
            field.set(null, originalMapper);
            field.setAccessible(false);

            safeClose(cosmosAsyncClient);
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

    @AfterSuite(groups = {"emulator"})
    public void afterSuite() {
        System.clearProperty("COSMOS.CHARSET_DECODER_ERROR_ACTION_ON_MALFORMED_INPUT");
        System.clearProperty("COSMOS.IS_NON_PARSEABLE_DOCUMENT_LOGGING_ENABLED");
        super.afterSuite();
    }
}
