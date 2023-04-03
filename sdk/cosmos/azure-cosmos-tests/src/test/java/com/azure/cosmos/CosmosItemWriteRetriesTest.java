/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.azure.cosmos;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.WriteRetryPolicy;
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
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorResultBuilder;
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
    private static final boolean WITH_INJECTION = true;
    private static final boolean NO_INJECTION = false;
    private static final boolean CONTENT_RESPONSE_ENABLED = true;
    private static final boolean NO_CONTENT_RESPONSE = false;
    private static final boolean WITH_PRECONDITION_CHECK = true;
    private static final boolean NO_PRECONDITION_CHECK = false;
    private static final Boolean DEFAULT_REQUEST_SUPPRESSION = null;
    private static final Boolean ENFORCED_REQUEST_SUPPRESSION = true;
    private static final Boolean NO_REQUEST_SUPPRESSION = false;
    private static final WriteRetryPolicy NO_RETRIES = WriteRetryPolicy.DISABLED;
    private static final WriteRetryPolicy RETRIES_WITHOUT_TRACKING_ID = new WriteRetryPolicy(true, false);
    private static final WriteRetryPolicy RETRIES_WITH_TRACKING_ID = new WriteRetryPolicy(true, true);

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

    public CosmosAsyncContainer createClientAndGetContainer(WriteRetryPolicy clientWideWriteRetryPolicy) {
        this.mockTracer = new TracerUnderTest();
        CosmosClientTelemetryConfig telemetryConfig = new CosmosClientTelemetryConfig()
            .enableTransportLevelTracing();
        ImplementationBridgeHelpers
            .CosmosClientTelemetryConfigHelper
            .getCosmosClientTelemetryConfigAccessor()
            .setTracer(telemetryConfig, this.mockTracer);

        CosmosClientBuilder builder = this.getClientBuilder()
            .clientTelemetryConfig(telemetryConfig);

        if (clientWideWriteRetryPolicy != null && clientWideWriteRetryPolicy.isEnabled()) {
            builder.enableNonIdempotentWriteRetries(clientWideWriteRetryPolicy.useTrackingIdProperty());
        } else {
            builder.resetNonIdempotentWriteRetryPolicy();
        }

        return getSharedMultiPartitionCosmosContainer(builder.buildAsyncClient());
    }

    public CosmosItemRequestOptions createRequestOptions(WriteRetryPolicy requestOptionsWriteRetryPolicy) {
        CosmosItemRequestOptions options = null;
        if (requestOptionsWriteRetryPolicy != null) {
            options = new CosmosItemRequestOptions();
            if (requestOptionsWriteRetryPolicy.isEnabled()) {
                options.enableNonIdempotentWriteRetriesEnabled(
                    requestOptionsWriteRetryPolicy.useTrackingIdProperty());
            } else {
                options.disableNonIdempotentWriteRetriesEnabled();
            }
        }

        return options;
    }

    public boolean shouldExpectTrackingId(WriteRetryPolicy requestPolicy, WriteRetryPolicy clientPolicy) {
        if (requestPolicy != null) {
            if (requestPolicy.isEnabled() && requestPolicy.useTrackingIdProperty()) {
                return true;
            }

            return false;
        }

        return clientPolicy != null && clientPolicy.isEnabled() && clientPolicy.useTrackingIdProperty();
    }

    @Override
    public String resolveTestNameSuffix(Object[] row) {
        if (this.getClientBuilder().isContentResponseOnWriteEnabled()) {
            return "WithResponse";
        }

        return "WithoutResponse";
    }

    @DataProvider(name = "createItemTestCaseProvider")
    private Object[][] createItemTestCaseProvider() {
        // following parameters will be set
        // - is content response on write enabled?
        // - should inject any failure?
        // - should suppress service request?
        // - client write retry policy
        // - requestOptions retry policy
        // - expected StatusCode
        return new Object[][]{
            new Object[] { NO_CONTENT_RESPONSE, WITH_INJECTION, DEFAULT_REQUEST_SUPPRESSION, null, null, HttpConstants.StatusCodes.REQUEST_TIMEOUT },
            new Object[] { NO_CONTENT_RESPONSE, WITH_INJECTION, DEFAULT_REQUEST_SUPPRESSION, null, RETRIES_WITH_TRACKING_ID, HttpConstants.StatusCodes.CREATED },
            new Object[] { CONTENT_RESPONSE_ENABLED, WITH_INJECTION, DEFAULT_REQUEST_SUPPRESSION, null, RETRIES_WITH_TRACKING_ID, HttpConstants.StatusCodes.CREATED },
            new Object[] { NO_CONTENT_RESPONSE, WITH_INJECTION, DEFAULT_REQUEST_SUPPRESSION, null, RETRIES_WITHOUT_TRACKING_ID, HttpConstants.StatusCodes.CREATED },
            new Object[] { NO_CONTENT_RESPONSE, WITH_INJECTION, ENFORCED_REQUEST_SUPPRESSION, null, RETRIES_WITH_TRACKING_ID, HttpConstants.StatusCodes.CREATED },
            new Object[] { NO_CONTENT_RESPONSE, WITH_INJECTION, ENFORCED_REQUEST_SUPPRESSION, null, RETRIES_WITHOUT_TRACKING_ID, HttpConstants.StatusCodes.CREATED },
            new Object[] { NO_CONTENT_RESPONSE, WITH_INJECTION, DEFAULT_REQUEST_SUPPRESSION, null, NO_RETRIES, HttpConstants.StatusCodes.REQUEST_TIMEOUT },
            new Object[] { NO_CONTENT_RESPONSE, WITH_INJECTION, DEFAULT_REQUEST_SUPPRESSION, RETRIES_WITHOUT_TRACKING_ID, null, HttpConstants.StatusCodes.CREATED },
            new Object[] { NO_CONTENT_RESPONSE, WITH_INJECTION, DEFAULT_REQUEST_SUPPRESSION, RETRIES_WITH_TRACKING_ID, null, HttpConstants.StatusCodes.CREATED },
            new Object[] { NO_CONTENT_RESPONSE, WITH_INJECTION, DEFAULT_REQUEST_SUPPRESSION, RETRIES_WITHOUT_TRACKING_ID, RETRIES_WITH_TRACKING_ID, HttpConstants.StatusCodes.CREATED },
            new Object[] { NO_CONTENT_RESPONSE, WITH_INJECTION, DEFAULT_REQUEST_SUPPRESSION, RETRIES_WITH_TRACKING_ID, NO_RETRIES, HttpConstants.StatusCodes.REQUEST_TIMEOUT },
            new Object[] { NO_CONTENT_RESPONSE, WITH_INJECTION, DEFAULT_REQUEST_SUPPRESSION, NO_RETRIES, NO_RETRIES, HttpConstants.StatusCodes.REQUEST_TIMEOUT },
            new Object[] { NO_CONTENT_RESPONSE, WITH_INJECTION, DEFAULT_REQUEST_SUPPRESSION, NO_RETRIES, RETRIES_WITH_TRACKING_ID, HttpConstants.StatusCodes.CREATED },
            new Object[] { NO_CONTENT_RESPONSE, WITH_INJECTION, DEFAULT_REQUEST_SUPPRESSION, RETRIES_WITH_TRACKING_ID, RETRIES_WITH_TRACKING_ID, HttpConstants.StatusCodes.CREATED },
            new Object[] { NO_CONTENT_RESPONSE, WITH_INJECTION, NO_REQUEST_SUPPRESSION, RETRIES_WITH_TRACKING_ID, RETRIES_WITHOUT_TRACKING_ID, HttpConstants.StatusCodes.CONFLICT },
            new Object[] { NO_CONTENT_RESPONSE, NO_INJECTION, DEFAULT_REQUEST_SUPPRESSION, NO_RETRIES, RETRIES_WITH_TRACKING_ID, HttpConstants.StatusCodes.CREATED },
            new Object[] { NO_CONTENT_RESPONSE, NO_INJECTION, DEFAULT_REQUEST_SUPPRESSION, RETRIES_WITH_TRACKING_ID, NO_RETRIES, HttpConstants.StatusCodes.CREATED },
            new Object[] { NO_CONTENT_RESPONSE, WITH_INJECTION, NO_REQUEST_SUPPRESSION, null, null, HttpConstants.StatusCodes.REQUEST_TIMEOUT },
            new Object[] { NO_CONTENT_RESPONSE, WITH_INJECTION, ENFORCED_REQUEST_SUPPRESSION, null, null, HttpConstants.StatusCodes.REQUEST_TIMEOUT },
            new Object[] { NO_CONTENT_RESPONSE, WITH_INJECTION, NO_REQUEST_SUPPRESSION, RETRIES_WITH_TRACKING_ID, null, HttpConstants.StatusCodes.CREATED },
            new Object[] { CONTENT_RESPONSE_ENABLED, WITH_INJECTION, NO_REQUEST_SUPPRESSION, RETRIES_WITH_TRACKING_ID, null, HttpConstants.StatusCodes.CREATED },
            new Object[] { NO_CONTENT_RESPONSE, WITH_INJECTION, NO_REQUEST_SUPPRESSION, RETRIES_WITHOUT_TRACKING_ID, null, HttpConstants.StatusCodes.CONFLICT },
            new Object[] { NO_CONTENT_RESPONSE, WITH_INJECTION, ENFORCED_REQUEST_SUPPRESSION, RETRIES_WITH_TRACKING_ID, RETRIES_WITHOUT_TRACKING_ID, HttpConstants.StatusCodes.CREATED },
        };
    }

    @DataProvider(name = "replaceItemTestCaseProvider")
    private Object[][] replaceItemTestCaseProvider() {
        // following parameters will be set
        // - is content response on write enabled?
        // - use pre-condition check?
        // - should inject any failure?
        // - should suppress service request?
        // - client write retry policy
        // - requestOptions retry policy
        // - expected StatusCode
        return new Object[][]{
            new Object[] { NO_PRECONDITION_CHECK, CONTENT_RESPONSE_ENABLED, WITH_INJECTION, DEFAULT_REQUEST_SUPPRESSION, null, null, HttpConstants.StatusCodes.REQUEST_TIMEOUT },
            new Object[] { WITH_PRECONDITION_CHECK, CONTENT_RESPONSE_ENABLED, WITH_INJECTION, DEFAULT_REQUEST_SUPPRESSION, null, null, HttpConstants.StatusCodes.REQUEST_TIMEOUT },
            new Object[] { NO_PRECONDITION_CHECK, CONTENT_RESPONSE_ENABLED, WITH_INJECTION, DEFAULT_REQUEST_SUPPRESSION, null, RETRIES_WITH_TRACKING_ID, HttpConstants.StatusCodes.OK },
            new Object[] { WITH_PRECONDITION_CHECK, CONTENT_RESPONSE_ENABLED, WITH_INJECTION, DEFAULT_REQUEST_SUPPRESSION, null, RETRIES_WITH_TRACKING_ID, HttpConstants.StatusCodes.OK },
            new Object[] { NO_PRECONDITION_CHECK, CONTENT_RESPONSE_ENABLED, WITH_INJECTION, DEFAULT_REQUEST_SUPPRESSION, null, RETRIES_WITH_TRACKING_ID, HttpConstants.StatusCodes.OK },
            new Object[] { NO_PRECONDITION_CHECK, NO_CONTENT_RESPONSE, WITH_INJECTION, DEFAULT_REQUEST_SUPPRESSION, null, RETRIES_WITHOUT_TRACKING_ID, HttpConstants.StatusCodes.OK },
            new Object[] { WITH_PRECONDITION_CHECK, NO_CONTENT_RESPONSE, WITH_INJECTION, DEFAULT_REQUEST_SUPPRESSION, null, RETRIES_WITHOUT_TRACKING_ID, HttpConstants.StatusCodes.OK },
            new Object[] { NO_PRECONDITION_CHECK, NO_CONTENT_RESPONSE, WITH_INJECTION, ENFORCED_REQUEST_SUPPRESSION, null, RETRIES_WITH_TRACKING_ID, HttpConstants.StatusCodes.OK },
            new Object[] { WITH_PRECONDITION_CHECK, NO_CONTENT_RESPONSE, WITH_INJECTION, ENFORCED_REQUEST_SUPPRESSION, null, RETRIES_WITH_TRACKING_ID, HttpConstants.StatusCodes.OK },
            new Object[] { NO_PRECONDITION_CHECK, CONTENT_RESPONSE_ENABLED, WITH_INJECTION, ENFORCED_REQUEST_SUPPRESSION, null, RETRIES_WITHOUT_TRACKING_ID, HttpConstants.StatusCodes.OK },
            new Object[] { NO_PRECONDITION_CHECK, CONTENT_RESPONSE_ENABLED, WITH_INJECTION, DEFAULT_REQUEST_SUPPRESSION, null, NO_RETRIES, HttpConstants.StatusCodes.REQUEST_TIMEOUT },
            new Object[] { WITH_PRECONDITION_CHECK, CONTENT_RESPONSE_ENABLED, WITH_INJECTION, DEFAULT_REQUEST_SUPPRESSION, null, NO_RETRIES, HttpConstants.StatusCodes.REQUEST_TIMEOUT },
            new Object[] { NO_PRECONDITION_CHECK, CONTENT_RESPONSE_ENABLED, WITH_INJECTION, DEFAULT_REQUEST_SUPPRESSION, RETRIES_WITHOUT_TRACKING_ID, null, HttpConstants.StatusCodes.OK },
            new Object[] { WITH_PRECONDITION_CHECK, CONTENT_RESPONSE_ENABLED, WITH_INJECTION, DEFAULT_REQUEST_SUPPRESSION, RETRIES_WITHOUT_TRACKING_ID, null, HttpConstants.StatusCodes.OK },
            new Object[] { NO_PRECONDITION_CHECK, CONTENT_RESPONSE_ENABLED, WITH_INJECTION, DEFAULT_REQUEST_SUPPRESSION, RETRIES_WITH_TRACKING_ID, null, HttpConstants.StatusCodes.OK },
            new Object[] { WITH_PRECONDITION_CHECK, CONTENT_RESPONSE_ENABLED, WITH_INJECTION, DEFAULT_REQUEST_SUPPRESSION, RETRIES_WITH_TRACKING_ID, null, HttpConstants.StatusCodes.OK },
            new Object[] { NO_PRECONDITION_CHECK, NO_CONTENT_RESPONSE, WITH_INJECTION, DEFAULT_REQUEST_SUPPRESSION, RETRIES_WITHOUT_TRACKING_ID, RETRIES_WITH_TRACKING_ID, HttpConstants.StatusCodes.OK },
            new Object[] { NO_PRECONDITION_CHECK, CONTENT_RESPONSE_ENABLED, WITH_INJECTION, DEFAULT_REQUEST_SUPPRESSION, RETRIES_WITH_TRACKING_ID, NO_RETRIES, HttpConstants.StatusCodes.REQUEST_TIMEOUT },
            new Object[] { NO_PRECONDITION_CHECK, CONTENT_RESPONSE_ENABLED, WITH_INJECTION, DEFAULT_REQUEST_SUPPRESSION, NO_RETRIES, NO_RETRIES, HttpConstants.StatusCodes.REQUEST_TIMEOUT },
            new Object[] { WITH_PRECONDITION_CHECK, CONTENT_RESPONSE_ENABLED, WITH_INJECTION, DEFAULT_REQUEST_SUPPRESSION, NO_RETRIES, NO_RETRIES, HttpConstants.StatusCodes.REQUEST_TIMEOUT },
            new Object[] { NO_PRECONDITION_CHECK, NO_CONTENT_RESPONSE, WITH_INJECTION, DEFAULT_REQUEST_SUPPRESSION, NO_RETRIES, RETRIES_WITH_TRACKING_ID, HttpConstants.StatusCodes.OK },
            new Object[] { NO_PRECONDITION_CHECK, NO_CONTENT_RESPONSE, WITH_INJECTION, DEFAULT_REQUEST_SUPPRESSION, RETRIES_WITH_TRACKING_ID, RETRIES_WITH_TRACKING_ID, HttpConstants.StatusCodes.OK },
            new Object[] { NO_PRECONDITION_CHECK, NO_CONTENT_RESPONSE, WITH_INJECTION, NO_REQUEST_SUPPRESSION, RETRIES_WITH_TRACKING_ID, RETRIES_WITHOUT_TRACKING_ID, HttpConstants.StatusCodes.OK },
            new Object[] { WITH_PRECONDITION_CHECK, NO_CONTENT_RESPONSE, WITH_INJECTION, NO_REQUEST_SUPPRESSION, RETRIES_WITH_TRACKING_ID, RETRIES_WITHOUT_TRACKING_ID, HttpConstants.StatusCodes.PRECONDITION_FAILED },
            new Object[] { NO_PRECONDITION_CHECK, NO_CONTENT_RESPONSE, WITH_INJECTION, NO_REQUEST_SUPPRESSION, RETRIES_WITH_TRACKING_ID, RETRIES_WITHOUT_TRACKING_ID, HttpConstants.StatusCodes.OK },
            new Object[] { NO_PRECONDITION_CHECK, CONTENT_RESPONSE_ENABLED, NO_INJECTION, DEFAULT_REQUEST_SUPPRESSION, NO_RETRIES, RETRIES_WITH_TRACKING_ID, HttpConstants.StatusCodes.OK },
            new Object[] { NO_PRECONDITION_CHECK, NO_CONTENT_RESPONSE, NO_INJECTION, DEFAULT_REQUEST_SUPPRESSION, RETRIES_WITH_TRACKING_ID, NO_RETRIES, HttpConstants.StatusCodes.OK },
            new Object[] { NO_PRECONDITION_CHECK, CONTENT_RESPONSE_ENABLED, WITH_INJECTION, NO_REQUEST_SUPPRESSION, null, null, HttpConstants.StatusCodes.REQUEST_TIMEOUT },
            new Object[] { NO_PRECONDITION_CHECK, CONTENT_RESPONSE_ENABLED, WITH_INJECTION, ENFORCED_REQUEST_SUPPRESSION, null, null, HttpConstants.StatusCodes.REQUEST_TIMEOUT },
            new Object[] { NO_PRECONDITION_CHECK, NO_CONTENT_RESPONSE, WITH_INJECTION, NO_REQUEST_SUPPRESSION, RETRIES_WITH_TRACKING_ID, null, HttpConstants.StatusCodes.OK },
            new Object[] { WITH_PRECONDITION_CHECK, NO_CONTENT_RESPONSE, WITH_INJECTION, NO_REQUEST_SUPPRESSION, RETRIES_WITH_TRACKING_ID, null, HttpConstants.StatusCodes.OK },
            new Object[] { NO_PRECONDITION_CHECK, CONTENT_RESPONSE_ENABLED, WITH_INJECTION, NO_REQUEST_SUPPRESSION, RETRIES_WITH_TRACKING_ID, null, HttpConstants.StatusCodes.OK },
            new Object[] { WITH_PRECONDITION_CHECK, CONTENT_RESPONSE_ENABLED, WITH_INJECTION, NO_REQUEST_SUPPRESSION, RETRIES_WITH_TRACKING_ID, null, HttpConstants.StatusCodes.OK },
            new Object[] { NO_PRECONDITION_CHECK, NO_CONTENT_RESPONSE, WITH_INJECTION, NO_REQUEST_SUPPRESSION, RETRIES_WITHOUT_TRACKING_ID, null, HttpConstants.StatusCodes.OK },
            new Object[] { WITH_PRECONDITION_CHECK, NO_CONTENT_RESPONSE, WITH_INJECTION, NO_REQUEST_SUPPRESSION, RETRIES_WITHOUT_TRACKING_ID, null, HttpConstants.StatusCodes.PRECONDITION_FAILED },
            new Object[] { NO_PRECONDITION_CHECK, CONTENT_RESPONSE_ENABLED, WITH_INJECTION, ENFORCED_REQUEST_SUPPRESSION, RETRIES_WITH_TRACKING_ID, RETRIES_WITHOUT_TRACKING_ID, HttpConstants.StatusCodes.OK },
        };
    }

    @Test(groups = { "simple", "emulator" }, dataProvider = "createItemTestCaseProvider", timeOut = TIMEOUT * 10000)
    public void createItem(
        boolean isContentResponseOnWriteEnabled,
        boolean injectFailure,
        Boolean suppressServiceRequests,
        WriteRetryPolicy clientWideWriteRetryPolicy,
        WriteRetryPolicy requestOptionsWriteRetryPolicy,
        int expectedStatusCode) {

        CosmosClientBuilder clientBuilder = this.getClientBuilder();
        if (requestOptionsWriteRetryPolicy == null) {
            clientBuilder.contentResponseOnWriteEnabled(isContentResponseOnWriteEnabled);
        }

        if (injectFailure &&
            clientBuilder.buildConnectionPolicy().getConnectionMode() != ConnectionMode.DIRECT) {

            throw new SkipException("Failure injection only supported for DIRECT mode");
        }

        CosmosAsyncContainer container = createClientAndGetContainer(clientWideWriteRetryPolicy);
        CosmosItemRequestOptions options = createRequestOptions(requestOptionsWriteRetryPolicy);
        if (options != null) {
            options.setContentResponseOnWriteEnabled(isContentResponseOnWriteEnabled);
        }
        FaultInjectionRule rule = null;

        if (injectFailure) {
            rule = injectFailure(container, FaultInjectionOperationType.CREATE_ITEM, suppressServiceRequests);
        }

        try {
            String id = UUID.randomUUID().toString();
            executeAndValidate(() -> container.createItem(
                getDocumentDefinition(id), new PartitionKey(id), options),
                id,
                expectedStatusCode,
                shouldExpectTrackingId(requestOptionsWriteRetryPolicy, clientWideWriteRetryPolicy),
                isContentResponseOnWriteEnabled);
        } finally {
            if (rule != null) {
                rule.disable();
                container.getDatabase().getClient().close();
                logger.info("JSON-Traces: {}", this.mockTracer.toJson());
            }
        }
    }

    private String createTestDocument(String id) {
        CosmosAsyncClient houseKeepingClient =
            this.getClientBuilder().contentResponseOnWriteEnabled(true).buildAsyncClient();
        CosmosItemResponse<ObjectNode> createResponse = getSharedMultiPartitionCosmosContainer(houseKeepingClient)
            .createItem(
                getDocumentDefinition(id),
                new PartitionKey(id),
                new CosmosItemRequestOptions().disableNonIdempotentWriteRetriesEnabled())
            .block();
        assertThat(createResponse).isNotNull();
        assertThat(createResponse.getStatusCode()).isEqualTo(201);
        assertThat(createResponse.getItem()).isNotNull();
        assertThat(createResponse.getItem().get("id")).isNotNull();
        assertThat(createResponse.getItem().get("id").textValue()).isEqualTo(id);
        assertThat(createResponse.getItem().get("_trackingId")).isNull();
        assertThat(createResponse.getItem().get("_etag")).isNotNull();
        assertThat(createResponse.getItem().get("_etag").textValue()).isNotNull();

        houseKeepingClient.close();

        return createResponse.getItem().get("_etag").textValue();
    }

    @Test(groups = { "simple", "emulator" }, dataProvider = "replaceItemTestCaseProvider", timeOut = TIMEOUT * 10000)
    public void replaceItem(
        boolean usePreconditionCheck,
        boolean isContentResponseOnWriteEnabled,
        boolean injectFailure,
        Boolean suppressServiceRequests,
        WriteRetryPolicy clientWideWriteRetryPolicy,
        WriteRetryPolicy requestOptionsWriteRetryPolicy,
        int expectedStatusCode) {

        if (injectFailure &&
            this.getClientBuilder().buildConnectionPolicy().getConnectionMode() != ConnectionMode.DIRECT) {

            throw new SkipException("Failure injection only supported for DIRECT mode");
        }

        String id = UUID.randomUUID().toString();
        String etag = this.createTestDocument(id);

        CosmosClientBuilder clientBuilder = this.getClientBuilder();
        if (requestOptionsWriteRetryPolicy == null) {
            clientBuilder.contentResponseOnWriteEnabled(isContentResponseOnWriteEnabled);
        }
        CosmosAsyncContainer container = createClientAndGetContainer(clientWideWriteRetryPolicy);
        CosmosItemRequestOptions options = createRequestOptions(requestOptionsWriteRetryPolicy);
        if (options != null) {
            options.setContentResponseOnWriteEnabled(isContentResponseOnWriteEnabled);
        }
        FaultInjectionRule rule = null;

        if (injectFailure) {
            rule = injectFailure(container, FaultInjectionOperationType.REPLACE_ITEM, suppressServiceRequests);
        }

        try {

            if (usePreconditionCheck) {
                if (options == null) {
                    options = new CosmosItemRequestOptions();
                }

                options.setIfMatchETag(etag);
            }

            final CosmosItemRequestOptions finalOptions = options;

            executeAndValidate(() -> container.replaceItem(
                    getDocumentDefinition(id), id, new PartitionKey(id), finalOptions),
                id,
                expectedStatusCode,
                shouldExpectTrackingId(requestOptionsWriteRetryPolicy, clientWideWriteRetryPolicy),
                isContentResponseOnWriteEnabled);
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
        FaultInjectionOperationType operationType,
        Boolean suppressServiceRequests) {

        FaultInjectionServerErrorResultBuilder faultInjectionResultBuilder = FaultInjectionResultBuilders
            .getResultBuilder(FaultInjectionServerErrorType.RESPONSE_DELAY)
            .delay(Duration.ofMillis(1500))
            .times(1);

        if (suppressServiceRequests != null) {
            faultInjectionResultBuilder.suppressServiceRequests(suppressServiceRequests);
        }

        IFaultInjectionResult result = faultInjectionResultBuilder.build();

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
        boolean expectTrackingId,
        boolean contentResponseOnWriteEnabled) {

        String activityId;
        if (expectedStatusCode <= 299) {
            CosmosItemResponse<ObjectNode> response = call.get().block();
            assertThat(response.getStatusCode()).isEqualTo(expectedStatusCode);
            activityId = response.getActivityId();
            assertThat(activityId).isNotNull();
            String trackingId = response.getDiagnostics().getDiagnosticsContext().getTrackingId();
            if (expectTrackingId) {
                assertThat(trackingId).isNotNull();

                if (response.getItem() != null) {
                    assertThat(response.getItem().get("_trackingId")).isNotNull();
                    assertThat(response.getItem().get("_trackingId").textValue()).isEqualTo(trackingId);
                }
            } else {
                assertThat(trackingId).isNull();
                if (response.getItem() != null) {
                    assertThat(response.getItem().get("_trackingId")).isNull();
                }
            }
            if (contentResponseOnWriteEnabled) {
                assertThat(response.getItem().get("id").asText()).isEqualTo(expectedId);
                assertThat(response.getItem().get("mypk").asText()).isEqualTo(expectedId);
                assertThat(response.getItem()).isNotNull();
            } else {
                assertThat(response.getItem()).isNull();
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
            "{ \"id\": \"%s\", \"mypk\": \"%s\", \"dummy\": \"%s\" }",
            documentId,
            documentId,
            UUID.randomUUID());

        try {
            return
                OBJECT_MAPPER.readValue(json, ObjectNode.class);
        } catch (JsonProcessingException jsonError) {
            fail("No json processing error expected", jsonError);

            throw new IllegalStateException("No json processing error expected", jsonError);
        }
    }
}