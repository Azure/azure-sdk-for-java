/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.azure.cosmos;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.assertj.core.api.Fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.Exceptions;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class CosmosItemIdEncodingTest extends TestSuiteBase {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().configure(
        JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(),
        true
    );

    private CosmosClient client;
    private CosmosContainer container;

    @Factory(dataProvider = "clientBuildersWithDirectSessionIncludeComputeGateway")
    public CosmosItemIdEncodingTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder.contentResponseOnWriteEnabled(true));
    }

    @BeforeClass(groups = {"fast", "emulator"}, timeOut = SETUP_TIMEOUT)
    public void before_CosmosItemTest() {
        assertThat(this.client).isNull();
        CosmosContainerProperties containerProperties = getCollectionDefinitionWithRangeRangeIndex();
        logger.info("Creating separate container {} for ItemIdEncoding tests to prevent leaving " +
            "any left-overs with weird encoded ids in the shared container.", containerProperties.getId());
        try {
            this.client = getClientBuilder().buildClient();
            getSharedCosmosDatabase(this.client.asyncClient()).createContainer(containerProperties).block();
            CosmosAsyncContainer asyncContainer =
                getSharedCosmosDatabase(this.client.asyncClient()).getContainer(containerProperties.getId());
            this.container = client
                .getDatabase(asyncContainer.getDatabase().getId())
                .getContainer(asyncContainer.getId());
        } catch (Exception error) {
            String message = String.format(
                "Failed creating separate container %s for ItemIdEncoding tests to prevent leaving " +
                    "any left-overs with weird encoded ids in the shared container.",
                containerProperties.getId());

            logger.error(message, error);

            fail(message);
        }

        logger.info("Finished creating separate container {} for ItemIdEncoding tests to prevent leaving " +
            "any left-overs with weird encoded ids in the shared container.", containerProperties.getId());
    }

    @AfterClass(groups = {"fast", "emulator"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.client).isNotNull();
        if (this.container != null) {
            container.delete();
        }
        this.client.close();
    }

    @Test(groups = { "fast", "emulator" }, timeOut = TIMEOUT)
    public void plainVanillaId() {
        TestScenario scenario = new TestScenario(
            "PlainVanillaId",
            "Test" + UUID.randomUUID(),
            new TestScenarioExpectations(
                ConnectionMode.GATEWAY.toString(),
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT),
            new TestScenarioExpectations(
                "COMPUTE_GATEWAY",
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT),
            new TestScenarioExpectations(
                ConnectionMode.DIRECT.toString(),
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT));

        this.executeTestCase(scenario);
    }

    @Test(groups = { "fast", "emulator" }, timeOut = TIMEOUT)
    public void containerIdWithUnicodeCharacter() {
        TestScenario scenario = new TestScenario(
            "ContainerIdWithUnicode鱀",
            "Test" + UUID.randomUUID(),
            new TestScenarioExpectations(
                ConnectionMode.GATEWAY.toString(),
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT),
            new TestScenarioExpectations(
                "COMPUTE_GATEWAY",
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT),
            new TestScenarioExpectations(
                ConnectionMode.DIRECT.toString(),
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT));

        this.executeTestCase(scenario);
    }

    @Test(groups = { "fast", "emulator" }, timeOut = TIMEOUT)
    public void idWithWhitespaces() {
        TestScenario scenario = new TestScenario(
            "IdWithWhitespaces",
            "This is a test" + UUID.randomUUID(),
            new TestScenarioExpectations(
                ConnectionMode.GATEWAY.toString(),
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT),
            new TestScenarioExpectations(
                "COMPUTE_GATEWAY",
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT),
            new TestScenarioExpectations(
                ConnectionMode.DIRECT.toString(),
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT));

        this.executeTestCase(scenario);
    }

    @Test(groups = { "fast", "emulator" }, timeOut = TIMEOUT)
    public void idStartingWithWhitespace() {
        TestScenario scenario = new TestScenario(
            "IdStartingWithWhitespace",
            " Test" + UUID.randomUUID(),
            new TestScenarioExpectations(
                ConnectionMode.GATEWAY.toString(),
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT),
            new TestScenarioExpectations(
                "COMPUTE_GATEWAY",
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT),
            new TestScenarioExpectations(
                ConnectionMode.DIRECT.toString(),
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT));

        this.executeTestCase(scenario);
    }

    @Test(groups = { "fast", "emulator" }, timeOut = TIMEOUT)
    public void idStartingWithWhitespaces() {
        TestScenario scenario = new TestScenario(
            "IdStartingWithWhitespaces",
            "  Test" + UUID.randomUUID(),
            new TestScenarioExpectations(
                ConnectionMode.GATEWAY.toString(),
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT),
            new TestScenarioExpectations(
                "COMPUTE_GATEWAY",
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT),
            new TestScenarioExpectations(
                ConnectionMode.DIRECT.toString(),
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT));

        this.executeTestCase(scenario);
    }

    @Test(groups = { "fast", "emulator" }, timeOut = TIMEOUT)
    public void idEndingWithWhitespace() {
        TestScenario scenario = new TestScenario(
            "IdEndingWithWhitespace",
             UUID.randomUUID() + "Test ",
            new TestScenarioExpectations(
                ConnectionMode.GATEWAY.toString(),
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.UNAUTHORIZED,
                HttpConstants.StatusCodes.UNAUTHORIZED,
                HttpConstants.StatusCodes.UNAUTHORIZED),
            new TestScenarioExpectations(
                "COMPUTE_GATEWAY",
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT),
            new TestScenarioExpectations(
                ConnectionMode.DIRECT.toString(),
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT));

        this.executeTestCase(scenario);
    }

    @Test(groups = { "fast", "emulator" }, timeOut = TIMEOUT)
    public void idEndingWithWhitespaces() {
        TestScenario scenario = new TestScenario(
            "IdEndingWithWhitespaces",
             UUID.randomUUID() + "Test   ",
            new TestScenarioExpectations(
                ConnectionMode.GATEWAY.toString(),
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.UNAUTHORIZED,
                HttpConstants.StatusCodes.UNAUTHORIZED,
                HttpConstants.StatusCodes.UNAUTHORIZED),
            new TestScenarioExpectations(
                "COMPUTE_GATEWAY",
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT),
            new TestScenarioExpectations(
                ConnectionMode.DIRECT.toString(),
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT));

        this.executeTestCase(scenario);
    }

    @Test(groups = { "fast", "emulator" }, timeOut = TIMEOUT)
    public void idWithUnicodeCharacters() {
        TestScenario scenario = new TestScenario(
            "IdWithUnicodeCharacters",
            "WithUnicode鱀" + UUID.randomUUID(),
            new TestScenarioExpectations(
                ConnectionMode.GATEWAY.toString(),
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT),
            new TestScenarioExpectations(
                "COMPUTE_GATEWAY",
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT),
            new TestScenarioExpectations(
                ConnectionMode.DIRECT.toString(),
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT));

        this.executeTestCase(scenario);
    }

    @Test(groups = { "fast", "emulator" }, timeOut = TIMEOUT)
    public void idWithAllowedSpecialCharacters() {
        TestScenario scenario = new TestScenario(
            "IdWithAllowedSpecialCharacters",
            "WithAllowedSpecial,=.:~+-@()^${}[]!_Chars" + UUID.randomUUID(),
            new TestScenarioExpectations(
                ConnectionMode.GATEWAY.toString(),
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT),
            new TestScenarioExpectations(
                "COMPUTE_GATEWAY",
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT),
            new TestScenarioExpectations(
                ConnectionMode.DIRECT.toString(),
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT));

        this.executeTestCase(scenario);
    }

    @Test(groups = { "fast", "emulator" }, timeOut = TIMEOUT)
    public void idWithBase64EncodedIdCharacters() {
        String base64EncodedId = "BQE1D3PdG4N4bzU9TKaCIM3qc0TVcZ2/Y3jnsRfwdHC1ombkX3F1dot/SG0/UTq9AbgdX3kOWoP6qL6lJqWeKgV3zwWWPZO/t5X0ehJzv9LGkWld07LID2rhWhGT6huBM6Q=";
        String safeBase64EncodedId = base64EncodedId.replace("/", "-");

        TestScenario scenario = new TestScenario(
            "IdWithBase64EncodedIdCharacters",
            safeBase64EncodedId + UUID.randomUUID(),
            new TestScenarioExpectations(
                ConnectionMode.GATEWAY.toString(),
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT),
            new TestScenarioExpectations(
                "COMPUTE_GATEWAY",
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT),
            new TestScenarioExpectations(
                ConnectionMode.DIRECT.toString(),
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT));

        this.executeTestCase(scenario);
    }

    @Test(groups = { "fast", "emulator" }, timeOut = TIMEOUT)
    public void idEndingWithPercentEncodedWhitespace() {
        TestScenario scenario = new TestScenario(
            "IdEndingWithPercentEncodedWhitespace",
            "IdEndingWithPercentEncodedWhitespace%20" + UUID.randomUUID(),
            new TestScenarioExpectations(
                ConnectionMode.GATEWAY.toString(),
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.UNAUTHORIZED,
                HttpConstants.StatusCodes.UNAUTHORIZED,
                HttpConstants.StatusCodes.UNAUTHORIZED),
            new TestScenarioExpectations(
                "COMPUTE_GATEWAY",
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT),
            new TestScenarioExpectations(
                ConnectionMode.DIRECT.toString(),
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT));

        this.executeTestCase(scenario);
    }

    @Test(groups = { "fast", "emulator" }, timeOut = TIMEOUT)
    public void idWithPercentEncodedSpecialChar() {
        TestScenario scenario = new TestScenario(
            "IdWithPercentEncodedSpecialChar",
            "WithPercentEncodedSpecialChar%E9%B1%80" + UUID.randomUUID(),
            new TestScenarioExpectations(
                ConnectionMode.GATEWAY.toString(),
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.UNAUTHORIZED,
                HttpConstants.StatusCodes.UNAUTHORIZED,
                HttpConstants.StatusCodes.UNAUTHORIZED),
            new TestScenarioExpectations(
                "COMPUTE_GATEWAY",
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT),
            new TestScenarioExpectations(
                ConnectionMode.DIRECT.toString(),
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT));

        this.executeTestCase(scenario);
    }

    @Test(groups = { "fast", "emulator" }, timeOut = TIMEOUT)
    public void idWithDisallowedCharQuestionMark() {
        TestScenario scenario = new TestScenario(
            "IdWithDisallowedCharQuestionMark",
            "Disallowed?Chars" + UUID.randomUUID(),
            new TestScenarioExpectations(
                ConnectionMode.GATEWAY.toString(),
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT),
            new TestScenarioExpectations(
                "COMPUTE_GATEWAY",
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT),
            new TestScenarioExpectations(
                ConnectionMode.DIRECT.toString(),
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT));

        this.executeTestCase(scenario);
    }

    @Test(groups = { "fast", "emulator" }, timeOut = TIMEOUT)
    public void idWithDisallowedCharForwardSlash() {
        TestScenario scenario = new TestScenario(
            "IdWithDisallowedCharForwardSlash",
            "Disallowed/Chars" + UUID.randomUUID(),
            new TestScenarioExpectations(
                ConnectionMode.GATEWAY.toString(),
                HttpConstants.StatusCodes.CREATED,
                -1 , // Non-CosmosException - in this case IllegalArgumentException
                -1,
                -1),
            new TestScenarioExpectations(
                "COMPUTE_GATEWAY",
                HttpConstants.StatusCodes.CREATED,
                -1 , // Non-CosmosException - in this case IllegalArgumentException
                -1,
                -1),
            new TestScenarioExpectations(
                ConnectionMode.DIRECT.toString(),
                HttpConstants.StatusCodes.CREATED,
                -1 , // Non-CosmosException - in this case IllegalArgumentException
                -1,
                -1));

        this.executeTestCase(scenario);
    }

    @Test(groups = { "fast", "emulator" }, timeOut = TIMEOUT)
    public void idWithDisallowedCharForwardSlashButIdValidationEnabled() {
        TestScenario scenario = new TestScenario(
            "IdWithDisallowedCharForwardSlashButIdValidationEnabled",
            "Disallowed/Chars" + UUID.randomUUID(),
            true,
            new TestScenarioExpectations(
                ConnectionMode.GATEWAY.toString(),
                HttpConstants.StatusCodes.BADREQUEST,
                -1 , // Non-CosmosException - in this case IllegalArgumentException
                -1,
                -1),
            new TestScenarioExpectations(
                "COMPUTE_GATEWAY",
                HttpConstants.StatusCodes.BADREQUEST,
                -1 , // Non-CosmosException - in this case IllegalArgumentException
                -1,
                -1),
            new TestScenarioExpectations(
                ConnectionMode.DIRECT.toString(),
                HttpConstants.StatusCodes.BADREQUEST,
                -1 , // Non-CosmosException - in this case IllegalArgumentException
                -1,
                -1));

        this.executeTestCase(scenario);
    }

    @Test(groups = { "fast", "emulator" }, timeOut = TIMEOUT)
    public void idWithDisallowedCharBackSlash() {
        TestScenario scenario = new TestScenario(
            "IdWithDisallowedCharBackSlash",
            "Disallowed\\\\Chars" + UUID.randomUUID(),
            new TestScenarioExpectations(
                ConnectionMode.GATEWAY.toString(),
                HttpConstants.StatusCodes.BADREQUEST,
                HttpConstants.StatusCodes.BADREQUEST,
                HttpConstants.StatusCodes.BADREQUEST,
                HttpConstants.StatusCodes.BADREQUEST),
            new TestScenarioExpectations(
                "COMPUTE_GATEWAY",
                HttpConstants.StatusCodes.BADREQUEST,
                HttpConstants.StatusCodes.BADREQUEST,
                HttpConstants.StatusCodes.BADREQUEST,
                HttpConstants.StatusCodes.BADREQUEST),
            new TestScenarioExpectations(
                ConnectionMode.DIRECT.toString(),
                HttpConstants.StatusCodes.BADREQUEST,
                HttpConstants.StatusCodes.BADREQUEST,
                HttpConstants.StatusCodes.BADREQUEST,
                HttpConstants.StatusCodes.BADREQUEST));

        this.executeTestCase(scenario);
    }

    @Test(groups = { "fast", "emulator" }, timeOut = TIMEOUT)
    public void idWithDisallowedCharPoundSign() {
        TestScenario scenario = new TestScenario(
            "IdWithDisallowedCharPoundSign",
            "Disallowed#Chars" + UUID.randomUUID(),
            new TestScenarioExpectations(
                ConnectionMode.GATEWAY.toString(),
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.UNAUTHORIZED,
                HttpConstants.StatusCodes.UNAUTHORIZED,
                HttpConstants.StatusCodes.UNAUTHORIZED),
            new TestScenarioExpectations(
                "COMPUTE_GATEWAY",
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT),
            new TestScenarioExpectations(
                ConnectionMode.DIRECT.toString(),
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT));

        this.executeTestCase(scenario);
    }

    @Test(groups = { "fast", "emulator" }, timeOut = TIMEOUT)
    public void idWithCarriageReturn() {
        TestScenario scenario = new TestScenario(
            "IdWithCarriageReturn",
            "With\rCarriageReturn" + UUID.randomUUID(),
            new TestScenarioExpectations(
                ConnectionMode.GATEWAY.toString(),
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.BADREQUEST,
                HttpConstants.StatusCodes.BADREQUEST,
                HttpConstants.StatusCodes.BADREQUEST),
            new TestScenarioExpectations(
                "COMPUTE_GATEWAY",
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT),
            new TestScenarioExpectations(
                ConnectionMode.DIRECT.toString(),
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT));

        this.executeTestCase(scenario);
    }

    @Test(groups = { "fast", "emulator" }, timeOut = TIMEOUT)
    public void idWithTab() {
        TestScenario scenario = new TestScenario(
            "IdWithTab",
            "With\tTab" + UUID.randomUUID(),
            new TestScenarioExpectations(
                ConnectionMode.GATEWAY.toString(),
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.BADREQUEST,
                HttpConstants.StatusCodes.BADREQUEST,
                HttpConstants.StatusCodes.BADREQUEST),
            new TestScenarioExpectations(
                "COMPUTE_GATEWAY",
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT),
            new TestScenarioExpectations(
                ConnectionMode.DIRECT.toString(),
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT));

        this.executeTestCase(scenario);
    }

    @Test(groups = { "fast", "emulator" }, timeOut = TIMEOUT)
    public void idWithLineFeed() {
        TestScenario scenario = new TestScenario(
            "IdWithLineFeed",
            "With\nLineFeed" + UUID.randomUUID(),
            new TestScenarioExpectations(
                ConnectionMode.GATEWAY.toString(),
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.BADREQUEST,
                HttpConstants.StatusCodes.BADREQUEST,
                HttpConstants.StatusCodes.BADREQUEST),
            new TestScenarioExpectations(
                "COMPUTE_GATEWAY",
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT),
            new TestScenarioExpectations(
                ConnectionMode.DIRECT.toString(),
                HttpConstants.StatusCodes.CREATED,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.OK,
                HttpConstants.StatusCodes.NO_CONTENT));

        this.executeTestCase(scenario);
    }

    private void executeTestCase(TestScenario scenario) {
        TestScenarioExpectations expected =
            this.getConnectionPolicy().getConnectionMode() == ConnectionMode.DIRECT ?
                scenario.direct : this.getClientBuilder().getEndpoint().contains(COMPUTE_GATEWAY_EMULATOR_PORT) ?
                    scenario.computeGateway : scenario.gateway;

        logger.info("Scenario: {}, Id: \"{}\"", scenario.name, scenario.id);

        if (scenario.idValidationEnabled) {
            System.setProperty(Configs.PREVENT_INVALID_ID_CHARS, "false");
        }

        try {
            try {
                CosmosItemResponse<ObjectNode> response = this.container.createItem(
                    getDocumentDefinition(scenario.id),
                    new PartitionKey(scenario.id),
                    null);

                deserializeAndValidatePayload(response, scenario.id, expected.ExpectedCreateStatusCode);
            } catch (Throwable throwable) {
                CosmosException cosmosError = Utils.as(Exceptions.unwrap(throwable), CosmosException.class);
                if (cosmosError == null) {
                    Fail.fail(
                        "Unexpected exception type " + Exceptions.unwrap(throwable).getClass().getName(),
                        throwable);
                }

                logger.error(cosmosError.toString());

                assertThat(cosmosError.getStatusCode())
                    .isEqualTo(expected.ExpectedCreateStatusCode);

                return;
            }

            try {
                CosmosItemResponse<ObjectNode> response = this.container.readItem(
                    scenario.id,
                    new PartitionKey(scenario.id),
                    ObjectNode.class);

                deserializeAndValidatePayload(response, scenario.id, expected.ExpectedReadStatusCode);
            } catch (Throwable throwable) {
                CosmosException cosmosError = Utils.as(Exceptions.unwrap(throwable), CosmosException.class);
                if (cosmosError == null) {
                    if (expected.ExpectedReadStatusCode == -1) {
                        return;
                    }

                    Fail.fail(
                        "Unexpected exception type " + Exceptions.unwrap(throwable).getClass().getName(),
                        throwable);
                }
                if (cosmosError.getStatusCode() == 0 &&
                    cosmosError.getCause() instanceof IllegalArgumentException &&
                    cosmosError.getCause().getCause() instanceof JsonParseException &&
                    cosmosError.getCause().getCause().toString().contains("<TITLE>Bad Request</TITLE>")) {

                    logger.info("HTML BAD REQUEST", cosmosError);
                    assertThat(expected.ExpectedReadStatusCode).isEqualTo(400);
                    return;
                } else {
                    logger.info("BAD REQUEST", cosmosError);
                    assertThat(cosmosError.getStatusCode()).isEqualTo(expected.ExpectedReadStatusCode);
                }
            }

            try {
                CosmosItemResponse<ObjectNode> response = this.container.replaceItem(
                    getDocumentDefinition(scenario.id),
                    scenario.id,
                    new PartitionKey(scenario.id),
                    null);

                deserializeAndValidatePayload(response, scenario.id, expected.ExpectedReplaceStatusCode);
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
                    scenario.id,
                    new PartitionKey(scenario.id),
                    (CosmosItemRequestOptions) null);

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
        } finally {
            System.clearProperty(Configs.PREVENT_INVALID_ID_CHARS);
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
            String connectionMode,
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

        public String ConnectionMode;

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
            TestScenarioExpectations computeGateway,
            TestScenarioExpectations direct) {

            this(name, id, false, gateway, computeGateway, direct);
        }

        public TestScenario(
            String name,
            String id,
            boolean idValidationEnabled,
            TestScenarioExpectations gateway,
            TestScenarioExpectations computeGateway,
            TestScenarioExpectations direct) {

            this.name = name;
            this.id = id;
            this.gateway = gateway;
            this.computeGateway = computeGateway;
            this.direct = direct;
            this.idValidationEnabled = idValidationEnabled;
        }

        public String name;

        public String id;

        public boolean idValidationEnabled;

        public TestScenarioExpectations gateway;

        public TestScenarioExpectations computeGateway;

        public TestScenarioExpectations direct;
    }
}
