// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.workflows.customer;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnosticsContext;
import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.TestObject;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OverridableRequestOptions;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.models.CosmosItemIdentity;
import com.azure.cosmos.models.CosmosItemRequestOptions;
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
    private final List<CosmosItemIdentity> itemsToCleanup = Collections.synchronizedList(new ArrayList<>());

    protected CustomerWorkflowTestBase(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    protected final void initializeSharedSinglePartitionContainer(String scenarioName) {
        initializeSharedSinglePartitionContainer(scenarioName, false);
    }

    protected final void initializeSharedSinglePartitionContainer(String scenarioName, boolean forceSessionConsistency) {
        if (forceSessionConsistency) {
            skipIfAccountConsistencyWeakerThanSession(scenarioName);
        }

        CosmosAsyncClient discoveryClient = null;

        try {
            discoveryClient = getClientBuilder().buildAsyncClient();
            this.writableRegions = discoverWritableRegions(discoveryClient);
            skipIfInsufficientRegions(this.writableRegions, scenarioName);

            CosmosClientBuilder clientBuilder = getClientBuilder()
                .preferredRegions(this.writableRegions)
                .multipleWriteRegionsEnabled(true)
                .contentResponseOnWriteEnabled(true);

            if (forceSessionConsistency) {
                // Read-your-write across an excluded write region is only deterministic with session (or
                // stronger) consistency, so pin the client to session consistency for these scenarios.
                clientBuilder.consistencyLevel(ConsistencyLevel.SESSION);
            }

            this.client = clientBuilder.buildAsyncClient();
            this.container = getSharedSinglePartitionCosmosContainer(this.client);
            waitForCollectionToBeAvailableToRead(this.container, /* probeClient */ null);
        } finally {
            safeClose(discoveryClient);
        }
    }

    protected final void closeClient() {
        cleanupRegisteredItems();
        safeClose(this.client);
        this.client = null;
        this.container = null;
        this.writableRegions = null;
        this.readableRegions = null;
    }

    protected final void initializeSharedSingleWriteMultiRegionContainer(String scenarioName) {
        CosmosAsyncClient discoveryClient = null;

        try {
            discoveryClient = getClientBuilder()
                .multipleWriteRegionsEnabled(false)
                .contentResponseOnWriteEnabled(true)
                .buildAsyncClient();
            this.writableRegions = discoverWritableRegions(discoveryClient);
            this.readableRegions = discoverReadableRegions(discoveryClient);
            skipIfInsufficientReadableRegions(this.readableRegions, scenarioName);
            skipIfNotSingleWriteRegion(this.writableRegions, scenarioName);

            this.client = getClientBuilder()
                .preferredRegions(this.readableRegions)
                .multipleWriteRegionsEnabled(false)
                .contentResponseOnWriteEnabled(true)
                .buildAsyncClient();
            this.container = getSharedSinglePartitionCosmosContainer(this.client);
            waitForCollectionToBeAvailableToRead(this.container, /* probeClient */ null);
        } finally {
            safeClose(discoveryClient);
        }
    }

    /**
     * Registers an item to be best-effort deleted from the shared container when the test class finishes,
     * so the shared single-partition container does not accumulate items across runs.
     */
    protected final void registerForCleanup(TestObject item) {
        if (item != null) {
            this.itemsToCleanup.add(new CosmosItemIdentity(partitionKey(item), item.getId()));
        }
    }

    private void cleanupRegisteredItems() {
        CosmosAsyncContainer cleanupContainer = this.container;
        List<CosmosItemIdentity> snapshot;
        synchronized (this.itemsToCleanup) {
            snapshot = new ArrayList<>(this.itemsToCleanup);
            this.itemsToCleanup.clear();
        }

        if (cleanupContainer == null) {
            return;
        }

        for (CosmosItemIdentity identity : snapshot) {
            try {
                cleanupContainer
                    .deleteItem(identity.getId(), identity.getPartitionKey(), new CosmosItemRequestOptions())
                    .block();
            } catch (Exception error) {
                // best-effort cleanup - ignore (for example item already deleted by the test itself)
            }
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
                throw new AssertionError("Interrupted while waiting for condition: " + failureMessage, error);
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

        return configureServerErrorRule(targetContainer, operationType, errorType, region, currentFaultInjectionConnectionType(), hitLimit);
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
            .connectionType(currentFaultInjectionConnectionType())
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
        DatabaseAccount databaseAccount = readDatabaseAccount(client);

        List<String> writableRegions = new ArrayList<>();
        for (DatabaseAccountLocation accountLocation : databaseAccount.getWritableLocations()) {
            writableRegions.add(accountLocation.getName());
        }

        return writableRegions;
    }

    protected static List<String> discoverReadableRegions(CosmosAsyncClient client) {
        DatabaseAccount databaseAccount = readDatabaseAccount(client);

        List<String> readableRegions = new ArrayList<>();
        for (DatabaseAccountLocation accountLocation : databaseAccount.getReadableLocations()) {
            readableRegions.add(accountLocation.getName());
        }

        return readableRegions;
    }

    private static DatabaseAccount readDatabaseAccount(CosmosAsyncClient client) {
        AsyncDocumentClient asyncDocumentClient = ReflectionUtils.getAsyncDocumentClient(client);
        RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) asyncDocumentClient;
        GlobalEndpointManager globalEndpointManager = ReflectionUtils.getGlobalEndpointManager(rxDocumentClient);

        // The latest database account is populated during client initialization. Poll briefly to defend against
        // an initialization race instead of forcing a synthetic database-account read (which is not routable in
        // direct connection mode).
        DatabaseAccount databaseAccount = globalEndpointManager.getLatestDatabaseAccount();
        long deadlineNanos = System.nanoTime() + Duration.ofSeconds(10).toNanos();
        while (databaseAccount == null && System.nanoTime() < deadlineNanos) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                throw new AssertionError("Interrupted while waiting for the database account to be available.", interrupted);
            }
            databaseAccount = globalEndpointManager.getLatestDatabaseAccount();
        }

        assertThat(databaseAccount)
            .as("database account must be available for region discovery")
            .isNotNull();

        return databaseAccount;
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

    protected static void skipIfAccountConsistencyWeakerThanSession(String scenarioName) {
        if (accountConsistency == ConsistencyLevel.EVENTUAL || accountConsistency == ConsistencyLevel.CONSISTENT_PREFIX) {
            throw new SkipException(
                scenarioName + " requires an account with session or stronger default consistency for deterministic read-your-write.");
        }
    }

    protected final void skipIfNotDirectMode(String scenarioName) {
        if (getConnectionPolicy().getConnectionMode() != ConnectionMode.DIRECT) {
            throw new SkipException(scenarioName + " only applies to the direct connection mode client builder.");
        }
    }

    protected final void skipIfNotGatewayMode(String scenarioName) {
        if (getConnectionPolicy().getConnectionMode() != ConnectionMode.GATEWAY) {
            throw new SkipException(scenarioName + " only applies to the gateway connection mode client builder.");
        }
    }

    /**
     * Skips fault-injection scenarios that cannot be injected for the gateway connection type. The gateway
     * internally retries 410/0, so {@code GONE} and {@code STALED_ADDRESSES_SERVER_GONE} rules are rejected at
     * configuration time for gateway-mode clients.
     */
    protected final void skipIfFaultTypeUnsupportedOnGateway(FaultInjectionServerErrorType errorType, String scenarioName) {
        if (currentFaultInjectionConnectionType() == FaultInjectionConnectionType.GATEWAY
            && (errorType == FaultInjectionServerErrorType.GONE
                || errorType == FaultInjectionServerErrorType.STALED_ADDRESSES_SERVER_GONE)) {

            throw new SkipException(
                scenarioName + " cannot inject " + errorType + " for the gateway connection type.");
        }
    }

    /**
     * Configures the same server-error fault for both the point-read ({@code READ_ITEM}) and query
     * ({@code QUERY_ITEM}) operation types. {@code readMany} resolves to a point read for a single item in a
     * partition and to a query for multiple items, so both rules are needed for the fault to reliably apply.
     */
    protected final List<FaultInjectionRule> configureReadManyServerErrorRules(
        CosmosAsyncContainer targetContainer,
        FaultInjectionServerErrorType errorType,
        String region,
        int hitLimit) {

        List<FaultInjectionRule> rules = new ArrayList<>();
        rules.add(configureServerErrorRule(
            targetContainer, FaultInjectionOperationType.READ_ITEM, errorType, region, currentFaultInjectionConnectionType(), hitLimit));
        rules.add(configureServerErrorRule(
            targetContainer, FaultInjectionOperationType.QUERY_ITEM, errorType, region, currentFaultInjectionConnectionType(), hitLimit));
        return rules;
    }

    /**
     * Asserts that a fault-injected operation produced a real HTTP outcome and that at least one of the supplied
     * fault rules was actually hit, so the scenario cannot silently pass without exercising the injected fault.
     */
    protected static void assertFaultInjectedOperation(
        CosmosDiagnosticsContext diagnosticsContext,
        FaultInjectionRule... rules) {

        assertThat(diagnosticsContext).isNotNull();
        assertThat(diagnosticsContext.getStatusCode()).isBetween(HttpConstants.StatusCodes.OK, 599);
        assertThat(diagnosticsContext.getContactedRegionNames()).isNotNull();

        long totalHits = 0;
        for (FaultInjectionRule rule : rules) {
            totalHits += rule.getHitCount();
        }

        assertThat(totalHits)
            .as("expected at least one injected fault to be hit")
            .isGreaterThanOrEqualTo(1);
    }

    protected static void assertFaultInjectedOperation(
        CosmosDiagnosticsContext diagnosticsContext,
        List<FaultInjectionRule> rules) {

        assertFaultInjectedOperation(diagnosticsContext, rules.toArray(new FaultInjectionRule[0]));
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
            .contains(expectedKeywordIdentifier);
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
