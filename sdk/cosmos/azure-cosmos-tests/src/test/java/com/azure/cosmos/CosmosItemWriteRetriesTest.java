/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.azure.cosmos;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.models.CosmosClientTelemetryConfig;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.test.faultinjection.FaultInjectionCondition;
import com.azure.cosmos.test.faultinjection.FaultInjectionConditionBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionConnectionType;
import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import com.azure.cosmos.test.faultinjection.FaultInjectionResultBuilders;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import com.azure.cosmos.test.faultinjection.FaultInjectionRuleBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorType;
import com.azure.cosmos.test.faultinjection.IFaultInjectionResult;
import com.azure.cosmos.test.implementation.faultinjection.FaultInjectorProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class CosmosItemWriteRetriesTest extends TestSuiteBase {
    private static final ObjectMapper OBJECT_MAPPER = Utils.getSimpleObjectMapper();

    private TracerUnderTest mockTracer;

    @Factory(dataProvider = "clientBuildersWithDirectTcpSession")
    public CosmosItemWriteRetriesTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilderWithReducedNetworkRequestTimeout(clientBuilder));
    }

    private static CosmosClientBuilder clientBuilderWithReducedNetworkRequestTimeout(
        CosmosClientBuilder clientBuilder) {

        clientBuilder.isContentResponseOnWriteEnabled();
        if (clientBuilder.buildConnectionPolicy().getConnectionMode() == ConnectionMode.DIRECT) {
            if (clientBuilder.getDirectConnectionConfig() == null) {
                clientBuilder.directMode();
            }
            clientBuilder.getDirectConnectionConfig().setNetworkRequestTimeout(Duration.ofSeconds(1));
        }

        return clientBuilder;
    }

    public CosmosAsyncContainer createClientAndGetContainer(Boolean nonIdempotentWriteRetriesEnabled) {
        this.mockTracer = new TracerUnderTest();
        CosmosClientBuilder builder = this.getClientBuilder()
            .clientTelemetryConfig(
                new CosmosClientTelemetryConfig()
                    .enableTransportLevelTracing()
                    .tracer(this.mockTracer)
            );

        if (nonIdempotentWriteRetriesEnabled != null && nonIdempotentWriteRetriesEnabled) {
            builder.enableNonIdempotentWriteRetries();
        }

        return getSharedMultiPartitionCosmosContainer(builder.buildAsyncClient());
    }

    public CosmosItemRequestOptions createRequestOptions(Boolean requestOptionsEnabled) {
        CosmosItemRequestOptions options = null;
        if (requestOptionsEnabled != null) {
            options = new CosmosItemRequestOptions();
            options.setNonIdempotentWriteRetriesEnabled(requestOptionsEnabled);
        }

        return options;
    }

    @Override
    public String resolveTestNameSuffix(Object[] row) {
        if (this.getClientBuilder().isContentResponseOnWriteEnabled()) {
            return "WithResponse";
        }

        return "WithoutResponse";
    }

    @DataProvider(name = "retriesEnabledTestCaseProvider")
    private Object[][] nonIdempotentWriteRetriesEnabledTestCaseProvider() {
        // following parameters will be set
        // - client default
        // - requestOptions default
        // - expected StatusCode
        return new Object[][]{
            new Object[] { true, null, null, HttpConstants.StatusCodes.REQUEST_TIMEOUT },
            new Object[] { true, null, true, HttpConstants.StatusCodes.CREATED },
            new Object[] { true, null, false, HttpConstants.StatusCodes.REQUEST_TIMEOUT },
            new Object[] { true, true, null, HttpConstants.StatusCodes.CREATED },
            new Object[] { true, true, null, HttpConstants.StatusCodes.CREATED },
            new Object[] { true, true, true, HttpConstants.StatusCodes.CREATED },
            new Object[] { true, true, false, HttpConstants.StatusCodes.REQUEST_TIMEOUT },
            new Object[] { true, false, false, HttpConstants.StatusCodes.REQUEST_TIMEOUT },
            new Object[] { true, false, true, HttpConstants.StatusCodes.CREATED },
            new Object[] { false, true, true, HttpConstants.StatusCodes.CREATED },
            new Object[] { false, false, true, HttpConstants.StatusCodes.CREATED },
            new Object[] { false, false, false, HttpConstants.StatusCodes.CREATED },
        };
    }

    @Test(groups = { "simple", "emulator" }, dataProvider = "retriesEnabledTestCaseProvider", timeOut = TIMEOUT * 10000)
    public void createItem(
        boolean injectFailure,
        Boolean clientWideEnabled,
        Boolean requestOptionsEnabled,
        int expectedStatusCode) {

        if (injectFailure &&
            this.getClientBuilder().buildConnectionPolicy().getConnectionMode() != ConnectionMode.DIRECT) {

            throw new SkipException("Failure injection only supported for DIRECT mode");
        }

        CosmosAsyncContainer container = createClientAndGetContainer(clientWideEnabled);
        CosmosItemRequestOptions options = createRequestOptions(requestOptionsEnabled);
        FaultInjectionRule rule = null;

        if (injectFailure) {
            rule = injectFailure(container, FaultInjectionOperationType.CREATE_ITEM);
        }

        try {
            String id = UUID.randomUUID().toString();
            executeAndValidate(() -> container.createItem(
                getDocumentDefinition(id), new PartitionKey(id), options),
                id,
                expectedStatusCode,
                false);
        } finally {
            if (rule != null) {
                rule.disable();
                container.getDatabase().getClient().close();
                logger.info("JSON-Traces: {}", this.mockTracer.toJson());
            }
        }
    }

    private FaultInjectionRule injectFailure(
        CosmosAsyncContainer container,
        FaultInjectionOperationType operationType) {

        IFaultInjectionResult result = FaultInjectionResultBuilders
            .getResultBuilder(FaultInjectionServerErrorType.RESPONSE_DELAY)
            .delay(Duration.ofMillis(1500))
            .times(1)
            .build();

        FaultInjectionCondition condition = new FaultInjectionConditionBuilder()
            .operationType(operationType)
            .connectionType(FaultInjectionConnectionType.DIRECT)
            .build();

        FaultInjectionRule rule = new FaultInjectionRuleBuilder("InjectedResponseDelay")
            .condition(condition)
            .result(result)
            .build();

        FaultInjectorProvider injectorProvider = (FaultInjectorProvider) container
            .getOrConfigureFaultInjectorProvider(() -> new FaultInjectorProvider(container));

        injectorProvider.configureFaultInjectionRules(Arrays.asList(rule)).block();

        return rule;
    }

    private void executeAndValidate(
        Supplier<Mono<CosmosItemResponse<ObjectNode>>> call,
        String expectedId,
        int expectedStatusCode,
        boolean expectTrackingId) {

        String activityId;
        if (expectedStatusCode <= 299) {
            CosmosItemResponse<ObjectNode> response = call.get().block();
            assertThat(response.getStatusCode()).isEqualTo(expectedStatusCode);
            assertThat(response.getItem().get("id").asText()).isEqualTo(expectedId);
            assertThat(response.getItem().get("mypk").asText()).isEqualTo(expectedId);
            activityId = response.getActivityId();
            assertThat(activityId).isNotNull();
            if (expectTrackingId) {
                assertThat(response.getItem().get("_trackingId")).isNotNull();
                assertThat(response.getItem().get("_trackingId")).isEqualTo(activityId);
            }
        } else {
            try {
                call.get().block();
                fail("Should never have reached here but seen exception");
            } catch (CosmosException cosmosError) {
                assertThat(cosmosError.getStatusCode()).isEqualTo(expectedStatusCode);
                activityId = cosmosError.getActivityId();
                //assertThat(activityId).isNotNull();
            }
        }
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
}