/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.azure.cosmos;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.assertj.core.api.Fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import reactor.core.Exceptions;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class CosmosItemIdEncodingTest extends TestSuiteBase {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private CosmosClient client;
    private CosmosContainer container;

    @Factory(dataProvider = "clientBuildersWithDirectSession")
    public CosmosItemIdEncodingTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder.contentResponseOnWriteEnabled(true));
    }

    @BeforeClass(groups = {"simple"}, timeOut = SETUP_TIMEOUT)
    public void before_CosmosItemTest() {
        assertThat(this.client).isNull();
        this.client = getClientBuilder().buildClient();
        CosmosAsyncContainer asyncContainer = getSharedMultiPartitionCosmosContainer(this.client.asyncClient());
        container = client.getDatabase(asyncContainer.getDatabase().getId()).getContainer(asyncContainer.getId());
    }

    @AfterClass(groups = {"simple"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.client).isNotNull();
        this.client.close();
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void plainVanillaId() {
        TestScenario scenario = new TestScenario(
            "PlainVanillaId",
            "Test" + UUID.randomUUID(),
            new TestScenarioExpectations(
                ConnectionMode.GATEWAY,
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT),
            new TestScenarioExpectations(
                ConnectionMode.DIRECT,
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT));

        this.executeTestCase(scenario);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void idWithWhitespaces() {
        TestScenario scenario = new TestScenario(
            "IdWithWhitespaces",
            "This is a test" + UUID.randomUUID(),
            new TestScenarioExpectations(
                ConnectionMode.GATEWAY,
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT),
            new TestScenarioExpectations(
                ConnectionMode.DIRECT,
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT));

        this.executeTestCase(scenario);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void idStartingWithWhitespace() {
        TestScenario scenario = new TestScenario(
            "IdStartingWithWhitespace",
            " Test" + UUID.randomUUID(),
            new TestScenarioExpectations(
                ConnectionMode.GATEWAY,
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT),
            new TestScenarioExpectations(
                ConnectionMode.DIRECT,
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT));

        this.executeTestCase(scenario);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void idStartingWithWhitespaces() {
        TestScenario scenario = new TestScenario(
            "IdStartingWithWhitespaces",
            "  Test" + UUID.randomUUID(),
            new TestScenarioExpectations(
                ConnectionMode.GATEWAY,
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT),
            new TestScenarioExpectations(
                ConnectionMode.DIRECT,
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT));

        this.executeTestCase(scenario);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void idEndingWithWhitespace() {
        TestScenario scenario = new TestScenario(
            "IdEndingWithWhitespace",
             UUID.randomUUID() + "Test ",
            new TestScenarioExpectations(
                ConnectionMode.GATEWAY,
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.UNAUTHORIZED,
                HttpConstants.StatusCodes.UNAUTHORIZED,
                HttpConstants.StatusCodes.UNAUTHORIZED),
            new TestScenarioExpectations(
                ConnectionMode.DIRECT,
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT));

        this.executeTestCase(scenario);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void idEndingWithWhitespaces() {
        TestScenario scenario = new TestScenario(
            "IdEndingWithWhitespaces",
             UUID.randomUUID() + "Test   ",
            new TestScenarioExpectations(
                ConnectionMode.GATEWAY,
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.UNAUTHORIZED,
                HttpConstants.StatusCodes.UNAUTHORIZED,
                HttpConstants.StatusCodes.UNAUTHORIZED),
            new TestScenarioExpectations(
                ConnectionMode.DIRECT,
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT));

        this.executeTestCase(scenario);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void idWithUnicodeCharacters() {
        TestScenario scenario = new TestScenario(
            "IdWithUnicodeCharacters",
            "WithUnicode鱀" + UUID.randomUUID(),
            new TestScenarioExpectations(
                ConnectionMode.GATEWAY,
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.UNAUTHORIZED,
                HttpConstants.StatusCodes.UNAUTHORIZED,
                HttpConstants.StatusCodes.UNAUTHORIZED),
            new TestScenarioExpectations(
                ConnectionMode.DIRECT,
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT));

        this.executeTestCase(scenario);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void idWithAllowedSpecialCharacters() {
        TestScenario scenario = new TestScenario(
            "IdWithAllowedSpecialCharacters",
            "WithAllowedSpecial,=.:~+-@()^${}[]!_Chars" + UUID.randomUUID(),
            new TestScenarioExpectations(
                ConnectionMode.GATEWAY,
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT),
            new TestScenarioExpectations(
                ConnectionMode.DIRECT,
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT));

        this.executeTestCase(scenario);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void idWithBase64EncodedIdCharacters() {
        String base64EncodedId = "BQE1D3PdG4N4bzU9TKaCIM3qc0TVcZ2/Y3jnsRfwdHC1ombkX3F1dot/SG0/UTq9AbgdX3kOWoP6qL6lJqWeKgV3zwWWPZO/t5X0ehJzv9LGkWld07LID2rhWhGT6huBM6Q=";
        String safeBase64EncodedId = base64EncodedId.replace("/", "-");

        TestScenario scenario = new TestScenario(
            "IdWithBase64EncodedIdCharacters",
            safeBase64EncodedId + UUID.randomUUID(),
            new TestScenarioExpectations(
                ConnectionMode.GATEWAY,
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT),
            new TestScenarioExpectations(
                ConnectionMode.DIRECT,
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT));

        this.executeTestCase(scenario);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void idEndingWithPercentEncodedWhitespace() {
        TestScenario scenario = new TestScenario(
            "IdEndingWithPercentEncodedWhitespace",
            "IdEndingWithPercentEncodedWhitespace%20" + UUID.randomUUID(),
            new TestScenarioExpectations(
                ConnectionMode.GATEWAY,
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.UNAUTHORIZED,
                HttpConstants.StatusCodes.UNAUTHORIZED,
                HttpConstants.StatusCodes.UNAUTHORIZED),
            new TestScenarioExpectations(
                ConnectionMode.DIRECT,
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT));

        this.executeTestCase(scenario);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void idWithPercentEncodedSpecialChar() {
        TestScenario scenario = new TestScenario(
            "IdWithPercentEncodedSpecialChar",
            "WithPercentEncodedSpecialChar%E9%B1%80" + UUID.randomUUID(),
            new TestScenarioExpectations(
                ConnectionMode.GATEWAY,
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.UNAUTHORIZED,
                HttpConstants.StatusCodes.UNAUTHORIZED,
                HttpConstants.StatusCodes.UNAUTHORIZED),
            new TestScenarioExpectations(
                ConnectionMode.DIRECT,
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT));

        this.executeTestCase(scenario);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void idWithDisallowedCharQuestionMark() {
        TestScenario scenario = new TestScenario(
            "IdWithDisallowedCharQuestionMark",
            "Disallowed?Chars" + UUID.randomUUID(),
            new TestScenarioExpectations(
                ConnectionMode.GATEWAY,
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT),
            new TestScenarioExpectations(
                ConnectionMode.DIRECT,
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT));

        this.executeTestCase(scenario);
    }

    @Ignore("Throws IllegalArgumentException instead of CosmosException")
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void idWithDisallowedCharForwardSlash() {
        TestScenario scenario = new TestScenario(
            "IdWithDisallowedCharForwardSlash",
            "Disallowed/Chars" + UUID.randomUUID(),
            new TestScenarioExpectations(
                ConnectionMode.GATEWAY,
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.NOTFOUND,
                HttpConstants.StatusCodes.NOTFOUND,
                HttpConstants.StatusCodes.NOTFOUND),
            new TestScenarioExpectations(
                ConnectionMode.DIRECT,
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.NOTFOUND,
                HttpConstants.StatusCodes.NOTFOUND,
                HttpConstants.StatusCodes.NOTFOUND));

        this.executeTestCase(scenario);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void idWithDisallowedCharBackSlash() {
        TestScenario scenario = new TestScenario(
            "IdWithDisallowedCharBackSlash",
            "Disallowed\\\\Chars" + UUID.randomUUID(),
            new TestScenarioExpectations(
                ConnectionMode.GATEWAY,
                HttpConstants.StatusCodes.BADREQUEST,
                HttpConstants.StatusCodes.BADREQUEST,
                HttpConstants.StatusCodes.BADREQUEST,
                HttpConstants.StatusCodes.BADREQUEST),
            new TestScenarioExpectations(
                ConnectionMode.DIRECT,
                HttpConstants.StatusCodes.BADREQUEST,
                HttpConstants.StatusCodes.BADREQUEST,
                HttpConstants.StatusCodes.BADREQUEST,
                HttpConstants.StatusCodes.BADREQUEST));

        this.executeTestCase(scenario);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void idWithDisallowedCharPoundSign() {
        TestScenario scenario = new TestScenario(
            "IdWithDisallowedCharPoundSign",
            "Disallowed#Chars" + UUID.randomUUID(),
            new TestScenarioExpectations(
                ConnectionMode.GATEWAY,
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.UNAUTHORIZED,
                HttpConstants.StatusCodes.UNAUTHORIZED,
                HttpConstants.StatusCodes.UNAUTHORIZED),
            new TestScenarioExpectations(
                ConnectionMode.DIRECT,
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT));

        this.executeTestCase(scenario);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void idWithCarriageReturn() {
        TestScenario scenario = new TestScenario(
            "IdWithCarriageReturn",
            "With\\rCarriageReturn" + UUID.randomUUID(),
            new TestScenarioExpectations(
                ConnectionMode.GATEWAY,
                HttpConstants.StatusCodes.BADREQUEST,
                HttpConstants.StatusCodes.BADREQUEST,
                HttpConstants.StatusCodes.BADREQUEST,
                HttpConstants.StatusCodes.BADREQUEST),
            new TestScenarioExpectations(
                ConnectionMode.DIRECT,
                HttpConstants.StatusCodes.BADREQUEST,
                HttpConstants.StatusCodes.BADREQUEST,
                HttpConstants.StatusCodes.BADREQUEST,
                HttpConstants.StatusCodes.BADREQUEST));

        this.executeTestCase(scenario);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void idWithTab() {
        TestScenario scenario = new TestScenario(
            "IdWithTab",
            "With\\tTab" + UUID.randomUUID(),
            new TestScenarioExpectations(
                ConnectionMode.GATEWAY,
                HttpConstants.StatusCodes.BADREQUEST,
                HttpConstants.StatusCodes.BADREQUEST,
                HttpConstants.StatusCodes.BADREQUEST,
                HttpConstants.StatusCodes.BADREQUEST),
            new TestScenarioExpectations(
                ConnectionMode.DIRECT,
                HttpConstants.StatusCodes.BADREQUEST,
                HttpConstants.StatusCodes.BADREQUEST,
                HttpConstants.StatusCodes.BADREQUEST,
                HttpConstants.StatusCodes.BADREQUEST));

        this.executeTestCase(scenario);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void idWithLineFeed() {
        TestScenario scenario = new TestScenario(
            "IdWithLineFeed",
            "With\\nLineFeed" + UUID.randomUUID(),
            new TestScenarioExpectations(
                ConnectionMode.GATEWAY,
                HttpConstants.StatusCodes.BADREQUEST,
                HttpConstants.StatusCodes.BADREQUEST,
                HttpConstants.StatusCodes.BADREQUEST,
                HttpConstants.StatusCodes.BADREQUEST),
            new TestScenarioExpectations(
                ConnectionMode.DIRECT,
                HttpConstants.StatusCodes.BADREQUEST,
                HttpConstants.StatusCodes.BADREQUEST,
                HttpConstants.StatusCodes.BADREQUEST,
                HttpConstants.StatusCodes.BADREQUEST));

        this.executeTestCase(scenario);
    }

    private void executeTestCase(TestScenario scenario) {
        TestScenarioExpectations expected =
            this.getConnectionPolicy().getConnectionMode() == ConnectionMode.DIRECT ?
                scenario.Direct : scenario.Gateway;

        this.logger.info("Scenario: {}, Id: \"{}\"", scenario.Name, scenario.Id);

        try {
            CosmosItemResponse<ObjectNode> response = this.container.createItem(
                getDocumentDefinition(scenario.Id),
                new PartitionKey(scenario.Id),
                null);

            deserializeAndValidatePayload(response, scenario.Id, expected.ExpectedCreateStatusCode);
        } catch (Throwable throwable) {
            CosmosException cosmosError = Utils.as(Exceptions.unwrap(throwable), CosmosException.class);
            if (cosmosError == null) {
                Fail.fail(
                    "Unexpected exception type " + Exceptions.unwrap(throwable).getClass().getName(),
                    throwable);
            }

            assertThat(cosmosError.getStatusCode()).isEqualTo(expected.ExpectedCreateStatusCode);
            return;
        }

        try {
            CosmosItemResponse<ObjectNode> response = this.container.readItem(
                scenario.Id,
                new PartitionKey(scenario.Id),
                ObjectNode.class);

            deserializeAndValidatePayload(response, scenario.Id, expected.ExpectedReadStatusCode);
        } catch (Throwable throwable) {
            CosmosException cosmosError = Utils.as(Exceptions.unwrap(throwable), CosmosException.class);
            if (cosmosError == null) {
                Fail.fail(
                    "Unexpected exception type " + Exceptions.unwrap(throwable).getClass().getName(),
                    throwable);
            }
            assertThat(cosmosError.getStatusCode()).isEqualTo(expected.ExpectedReadStatusCode);
        }

        try {
            CosmosItemResponse<ObjectNode> response = this.container.replaceItem(
                getDocumentDefinition(scenario.Id),
                scenario.Id,
                new PartitionKey(scenario.Id),
                null);

            deserializeAndValidatePayload(response, scenario.Id, expected.ExpectedReplaceStatusCode);
        } catch (Throwable throwable) {
            CosmosException cosmosError = Utils.as(Exceptions.unwrap(throwable), CosmosException.class);
            if (cosmosError == null) {
                Fail.fail(
                    "Unexpected exception type " + Exceptions.unwrap(throwable).getClass().getName(),
                    throwable);
            }
            assertThat(cosmosError.getStatusCode()).isEqualTo(expected.ExpectedReplaceStatusCode);
        }

        try {
            CosmosItemResponse<Object> response = this.container.deleteItem(
                scenario.Id,
                new PartitionKey(scenario.Id),
                (CosmosItemRequestOptions)null);

            assertThat(response.getStatusCode()).isEqualTo(expected.ExpectedDeleteStatusCode);
        } catch (Throwable throwable) {
            CosmosException cosmosError = Utils.as(Exceptions.unwrap(throwable), CosmosException.class);
            if (cosmosError == null) {
                Fail.fail(
                    "Unexpected exception type " + Exceptions.unwrap(throwable).getClass().getName(),
                    throwable);
            }
            assertThat(cosmosError.getStatusCode()).isEqualTo(expected.ExpectedDeleteStatusCode);
        }
    }

    private void deserializeAndValidatePayload(
        CosmosItemResponse<ObjectNode> response,
        String expectedId,
        int expectedStatusCode) {

        assertThat(response.getStatusCode()).isEqualTo(expectedStatusCode);
        assertThat(response.getItem().get("id").asText()).isEqualTo(expectedId);
        assertThat(response.getItem().get("mypk").asText()).isEqualTo(expectedId);
    }

    private ObjectNode getDocumentDefinition(String documentId) {
        String json = String.format(
            "{ \"id\": \"%s\", \"mypk\": \"%s\" }",
            documentId,
            documentId);

        try {
            return
                OBJECT_MAPPER.readValue(json, ObjectNode.class);
        } catch (JsonProcessingException jsonError) {
            fail("No json processing error expected", jsonError);

            throw new IllegalStateException("No json processing error expected", jsonError);
        }
    }

    private static class TestScenarioExpectations {

        public TestScenarioExpectations(
            ConnectionMode connectionMode,
            int expectedCreateStatusCode,
            int expectedReadStatusCode,
            int expectedReplaceStatusCode,
            int expectedDeleteStatusCode
        ) {
            this.ConnectionMode = connectionMode;
            this.ExpectedCreateStatusCode = expectedCreateStatusCode;
            this.ExpectedReadStatusCode = expectedReadStatusCode;
            this.ExpectedReplaceStatusCode = expectedReplaceStatusCode;
            this.ExpectedDeleteStatusCode = expectedDeleteStatusCode;
        }

        public ConnectionMode ConnectionMode;

        public int ExpectedCreateStatusCode;

        public int ExpectedReadStatusCode;

        public int ExpectedReplaceStatusCode;

        public int ExpectedDeleteStatusCode;
    }

    private static class TestScenario {

        public TestScenario(
            String name,
            String id,
            TestScenarioExpectations gateway,
            TestScenarioExpectations direct) {

            this.Name = name;
            this.Id = id;
            this.Gateway = gateway;
            this.Direct = direct;
        }

        public String Name;

        public String Id;

        public TestScenarioExpectations Gateway;

        public TestScenarioExpectations Direct;
    }
}
