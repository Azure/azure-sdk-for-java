// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.workflows.customer;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnosticsContext;
import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.TestObject;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OverridableRequestOptions;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.test.faultinjection.CosmosFaultInjectionHelper;
import com.azure.cosmos.test.faultinjection.FaultInjectionCondition;
import com.azure.cosmos.test.faultinjection.FaultInjectionConditionBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionConnectionType;
import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import com.azure.cosmos.test.faultinjection.FaultInjectionResultBuilders;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import com.azure.cosmos.test.faultinjection.FaultInjectionRuleBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorType;
import com.azure.cosmos.test.faultinjection.IFaultInjectionResult;
import org.testng.SkipException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class CustomerWorkflowTestBase extends TestSuiteBase {
    protected CosmosAsyncClient client;
    protected CosmosAsyncContainer container;
    protected List<String> writableRegions;
    protected List<String> readableRegions;

    protected CustomerWorkflowTestBase(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    protected final void initializeSharedSinglePartitionContainer(String scenarioName) {
        CosmosAsyncClient discoveryClient = null;

        try {
            discoveryClient = getClientBuilder().buildAsyncClient();
            this.writableRegions = discoverWritableRegions(discoveryClient);
            skipIfInsufficientRegions(this.writableRegions, scenarioName);

            this.client = getClientBuilder()
                .preferredRegions(this.writableRegions)
                .multipleWriteRegionsEnabled(true)
                .contentResponseOnWriteEnabled(true)
                .buildAsyncClient();
            this.container = getSharedSinglePartitionCosmosContainer(this.client);
        } finally {
            safeClose(discoveryClient);
        }
    }

    protected final void closeClient() {
        safeClose(this.client);
        this.client = null;
        this.container = null;
        this.writableRegions = null;
        this.readableRegions = null;
    }

    protected final void initializeSharedSingleWriteMultiRegionContainer(String scenarioName) {
        CosmosAsyncClient discoveryClient = null;

        try {
            CosmosClientBuilder clientBuilder = getClientBuilder()
                .multipleWriteRegionsEnabled(false)
                .contentResponseOnWriteEnabled(true);

            discoveryClient = clientBuilder.buildAsyncClient();
            this.writableRegions = discoverWritableRegions(discoveryClient);
            this.readableRegions = discoverReadableRegions(discoveryClient);
            skipIfInsufficientReadableRegions(this.readableRegions, scenarioName);
            skipIfNotSingleWriteRegion(this.writableRegions, scenarioName);

            this.client = clientBuilder
                .preferredRegions(this.readableRegions)
                .multipleWriteRegionsEnabled(false)
                .buildAsyncClient();
            this.container = getSharedSinglePartitionCosmosContainer(this.client);
        } finally {
            safeClose(discoveryClient);
        }
    }

    protected final List<String> excludeFirstWritableRegion() {
        return Collections.singletonList(this.writableRegions.get(0));
    }

    protected final List<String> excludeFirstReadableRegion() {
        return Collections.singletonList(this.readableRegions.get(0));
    }

    protected static com.azure.cosmos.models.PartitionKey partitionKey(TestObject item) {
        return new com.azure.cosmos.models.PartitionKey(item.getMypk());
    }

    protected final CosmosAsyncContainer createTemporaryContainer(String prefix, String partitionKeyPath) {
        CosmosAsyncDatabase database = getSharedCosmosDatabase(this.client);
        String containerId = prefix + "-" + UUID.randomUUID();

        database
            .createContainerIfNotExists(containerId, partitionKeyPath, ThroughputProperties.createManualThroughput(400))
            .block();

        return database.getContainer(containerId);
    }

    protected static void deleteTemporaryContainer(CosmosAsyncContainer container) {
        safeDeleteCollection(container);
    }

    protected static void awaitCondition(BooleanSupplier condition, Duration timeout, String failureMessage) {
        long deadline = System.nanoTime() + timeout.toNanos();

        while (System.nanoTime() < deadline) {
            if (condition.getAsBoolean()) {
                return;
            }

            try {
                Thread.sleep(250);
            } catch (InterruptedException error) {
                Thread.currentThread().interrupt();
                throw new AssertionError(failureMessage, error);
            }
        }

        throw new AssertionError(failureMessage);
    }

    protected final FaultInjectionRule configureServerErrorRule(
        CosmosAsyncContainer targetContainer,
        FaultInjectionOperationType operationType,
        FaultInjectionServerErrorType errorType,
        int hitLimit) {

        return configureServerErrorRule(targetContainer, operationType, errorType, this.writableRegions.get(0), hitLimit);
    }

    protected final FaultInjectionRule configureServerErrorRule(
        CosmosAsyncContainer targetContainer,
        FaultInjectionOperationType operationType,
        FaultInjectionServerErrorType errorType,
        String region,
        int hitLimit) {

        return configureServerErrorRule(targetContainer, operationType, errorType, region, FaultInjectionConnectionType.DIRECT, hitLimit);
    }

    protected final FaultInjectionRule configureServerErrorRule(
        CosmosAsyncContainer targetContainer,
        FaultInjectionOperationType operationType,
        FaultInjectionServerErrorType errorType,
        String region,
        FaultInjectionConnectionType connectionType,
        int hitLimit) {

        FaultInjectionConditionBuilder conditionBuilder = new FaultInjectionConditionBuilder()
            .operationType(operationType)
            .connectionType(connectionType);

        if (region != null) {
            conditionBuilder.region(region);
        }

        FaultInjectionRule rule = new FaultInjectionRuleBuilder("customer-workflow-" + errorType + "-" + UUID.randomUUID())
            .condition(conditionBuilder.build())
            .result(FaultInjectionResultBuilders.getResultBuilder(errorType).build())
            .duration(Duration.ofMinutes(5))
            .hitLimit(hitLimit)
            .build();

        CosmosFaultInjectionHelper.configureFaultInjectionRules(targetContainer, Collections.singletonList(rule)).block();
        return rule;
    }

    protected final FaultInjectionConnectionType currentFaultInjectionConnectionType() {
        if (getConnectionPolicy().getConnectionMode() == ConnectionMode.GATEWAY) {
            return FaultInjectionConnectionType.GATEWAY;
        }

        return FaultInjectionConnectionType.DIRECT;
    }

    protected final FaultInjectionRule configureResponseDelayRule(
        CosmosAsyncContainer targetContainer,
        FaultInjectionOperationType operationType,
        Duration delay,
        int hitLimit) {

        FaultInjectionCondition condition = new FaultInjectionConditionBuilder()
            .operationType(operationType)
            .connectionType(FaultInjectionConnectionType.DIRECT)
            .build();

        IFaultInjectionResult result = FaultInjectionResultBuilders
            .getResultBuilder(FaultInjectionServerErrorType.RESPONSE_DELAY)
            .delay(delay)
            .times(hitLimit)
            .build();

        FaultInjectionRule rule = new FaultInjectionRuleBuilder("customer-workflow-response-delay-" + UUID.randomUUID())
            .condition(condition)
            .result(result)
            .duration(Duration.ofMinutes(5))
            .hitLimit(hitLimit)
            .build();

        CosmosFaultInjectionHelper.configureFaultInjectionRules(targetContainer, Collections.singletonList(rule)).block();
        return rule;
    }

    protected static List<String> discoverWritableRegions(CosmosAsyncClient client) {
        AsyncDocumentClient asyncDocumentClient = ReflectionUtils.getAsyncDocumentClient(client);
        RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) asyncDocumentClient;
        GlobalEndpointManager globalEndpointManager = ReflectionUtils.getGlobalEndpointManager(rxDocumentClient);
        DatabaseAccount databaseAccount = globalEndpointManager.getLatestDatabaseAccount();

        List<String> writableRegions = new ArrayList<>();
        for (DatabaseAccountLocation accountLocation : databaseAccount.getWritableLocations()) {
            writableRegions.add(accountLocation.getName());
        }

        return writableRegions;
    }

    protected static List<String> discoverReadableRegions(CosmosAsyncClient client) {
        AsyncDocumentClient asyncDocumentClient = ReflectionUtils.getAsyncDocumentClient(client);
        RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) asyncDocumentClient;
        GlobalEndpointManager globalEndpointManager = ReflectionUtils.getGlobalEndpointManager(rxDocumentClient);
        DatabaseAccount databaseAccount = globalEndpointManager.getLatestDatabaseAccount();

        List<String> readableRegions = new ArrayList<>();
        for (DatabaseAccountLocation accountLocation : databaseAccount.getReadableLocations()) {
            readableRegions.add(accountLocation.getName());
        }

        return readableRegions;
    }

    protected static void skipIfInsufficientRegions(List<String> regions, String scenarioName) {
        if (regions == null || regions.size() < 2) {
            throw new SkipException(scenarioName + " requires a live multi-region account.");
        }
    }

    protected static void skipIfInsufficientReadableRegions(List<String> regions, String scenarioName) {
        if (regions == null || regions.size() < 2) {
            throw new SkipException(scenarioName + " requires a live multi-region single-write account.");
        }
    }

    protected static void skipIfNotSingleWriteRegion(List<String> regions, String scenarioName) {
        if (regions == null || regions.size() != 1) {
            throw new SkipException(scenarioName + " requires exactly one write region.");
        }
    }

    protected static OverridableRequestOptions getRequestOptions(CosmosDiagnosticsContext diagnosticsContext) {
        assertThat(diagnosticsContext).isNotNull();
        return ImplementationBridgeHelpers
            .CosmosDiagnosticsContextHelper
            .getCosmosDiagnosticsContextAccessor()
            .getRequestOptions(diagnosticsContext);
    }

    protected static void assertKeywordIdentifier(CosmosDiagnosticsContext diagnosticsContext, String expectedKeywordIdentifier) {
        OverridableRequestOptions requestOptions = getRequestOptions(diagnosticsContext);

        assertThat(requestOptions.getKeywordIdentifiers())
            .containsExactly(expectedKeywordIdentifier);
    }

    protected static void assertExcludedRegions(
        CosmosDiagnosticsContext diagnosticsContext,
        List<String> expectedExcludedRegions) {

        OverridableRequestOptions requestOptions = getRequestOptions(diagnosticsContext);

        assertThat(requestOptions.getExcludedRegions())
            .containsExactlyElementsOf(expectedExcludedRegions);
    }

    protected static void assertDidNotContactExcludedRegions(
        CosmosDiagnosticsContext diagnosticsContext,
        Collection<String> excludedRegions) {

        Set<String> contactedRegionNames = diagnosticsContext.getContactedRegionNames();
        Set<String> normalizedExcludedRegions = excludedRegions
            .stream()
            .map(region -> region.toLowerCase(Locale.ROOT))
            .collect(Collectors.toSet());

        assertThat(contactedRegionNames).isNotNull();
        assertThat(contactedRegionNames).doesNotContainAnyElementsOf(normalizedExcludedRegions);
    }
}