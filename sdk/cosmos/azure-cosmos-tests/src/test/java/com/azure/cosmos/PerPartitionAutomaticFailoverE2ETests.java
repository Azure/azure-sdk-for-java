// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.DatabaseAccountManagerInternal;
import com.azure.cosmos.implementation.ForbiddenException;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.RequestTimeoutException;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.RxStoreModel;
import com.azure.cosmos.implementation.ServiceUnavailableException;
import com.azure.cosmos.implementation.StoreResponseBuilder;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.directconnectivity.ConsistencyReader;
import com.azure.cosmos.implementation.directconnectivity.ConsistencyWriter;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.directconnectivity.ReplicatedResourceClient;
import com.azure.cosmos.implementation.directconnectivity.StoreClient;
import com.azure.cosmos.implementation.directconnectivity.StoreReader;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import com.azure.cosmos.implementation.directconnectivity.TransportClient;
import com.azure.cosmos.implementation.guava25.base.Function;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpHeaders;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.implementation.http.HttpResponse;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper;
import com.azure.cosmos.implementation.routing.RegionalRoutingContext;
import com.azure.cosmos.implementation.throughputControl.TestItem;
import com.azure.cosmos.models.CosmosBatch;
import com.azure.cosmos.models.CosmosBatchResponse;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosItemIdentity;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosPatchItemRequestOptions;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.test.faultinjection.CosmosFaultInjectionHelper;
import com.azure.cosmos.test.faultinjection.FaultInjectionCondition;
import com.azure.cosmos.test.faultinjection.FaultInjectionConditionBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionConnectionType;
import com.azure.cosmos.test.faultinjection.FaultInjectionEndpointBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import com.azure.cosmos.test.faultinjection.FaultInjectionResultBuilders;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import com.azure.cosmos.test.faultinjection.FaultInjectionRuleBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorResult;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.timeout.ReadTimeoutException;
import org.assertj.core.api.Assertions;
import org.mockito.Mockito;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end test suite validating Per-Partition Automatic Failover (PPAF) behavior in the Azure Cosmos DB Java SDK.
 *
 * <p>This suite exercises and verifies:
 * <ul>
 *   <li>Automatic failover and hedged routing at the granularity of a single physical partition (PK range).</li>
 *   <li>Dynamic enablement and disablement of PPAF at runtime by reflecting and overriding
 *       {@code GlobalEndpointManager}'s {@code DatabaseAccountManagerInternal} owner to toggle
 *       {@code DatabaseAccount#isPerPartitionFailoverBehaviorEnabled}.</li>
 *   <li>Write operation failover (Create, Replace, Upsert, Delete, Patch, Batch) under multiple failover‑eligible
 *       status/sub-status combinations (410/21005, 503/21008, 403/3, 408/*, gateway read timeouts).</li>
 *   <li>Non-write hedging behavior (point Read and Query variants) under region-scoped transient faults:
 *       <ul>
 *         <li>DIRECT mode: simulated server-generated 410 (sub-status 21005) scoped to a specific partition key range.</li>
 *         <li>GATEWAY mode: injected RESPONSE_DELAY faults via fault injection rules (query plan + query + read item).</li>
 *       </ul>
 *   </li>
 *   <li>Interaction with end-to-end latency policies (E2E timeout) as a gating mechanism for enabling failover logic.</li>
 * </ul>
 *
 * <p><strong>Connection Modes Covered</strong>:
 * <ul>
 *   <li>DIRECT: Uses a mocked {@code TransportClient} to selectively throw {@code CosmosException} for a targeted
 *       (region + PK range) while returning success for others.</li>
 *   <li>GATEWAY: Uses a mocked {@code HttpClient} (or fault injection framework) to simulate service errors, network
 *       timeouts (socket/read), regional delays, or success responses.</li>
 * </ul>
 *
 * <p><strong>Phased Validation Patterns</strong>:
 * <ul>
 *   <li><em>Pre-failover / Hedging Window</em>: Verifies retries or region hedging (multi-region contacts) before
 *       PPCB-enforced failover, optionally repeated to satisfy E2E timeout activation thresholds.</li>
 *   <li><em>Post-failover / Stabilized</em>: Ensures subsequent operations route directly to a healthy region
 *       (single-region contact, zero retries) unless query semantics (e.g., query plan retrieval) require multi-region access.</li>
 *   <li><em>Dynamic Enablement Toggle</em>: For selected 503 scenarios, validates behavior transitions
 *       Disabled → Enabled → Disabled, confirming routing and diagnostics adapt immediately after
 *       {@code refreshLocationAsync(true)}.</li>
 * </ul>
 *
 * <p><strong>Diagnostics Assertions</strong>:
 * Each test inspects {@code CosmosDiagnostics} to assert:
 * <ul>
 *   <li>Contacted region count (hedged vs stabilized).</li>
 *   <li>Retry count bounds (min/max) aligned with scenario expectations.</li>
 *   <li>Final HTTP status classification (success vs expected failure when failover gated).</li>
 *   <li>Consistency across response types (item, batch, feed, or exception paths).</li>
 * </ul>
 *
 * <p><strong>Key Internal Mechanisms</strong>:
 * <ul>
 *   <li>Reflection-based access to internal SDK components (e.g., {@code RxDocumentClientImpl},
 *       {@code StoreReader}, {@code ConsistencyWriter}) to inject mocked transport layers.</li>
 *   <li>Custom delegating {@code DatabaseAccountManagerInternal} wrapper that conditionally sets
 *       the per-partition failover flag on retrieved {@code DatabaseAccount} snapshots.</li>
 *   <li>Fault injection rules in GATEWAY mode to apply controlled latency (RESPONSE_DELAY) per region and operation type.</li>
 *   <li>Reusable operation dispatch via a functional resolver mapping {@code OperationType} to execution lambdas
 *       returning a uniform {@code ResponseWrapper} abstraction.</li>
 * </ul>
 *
 * <p><strong>Query Variants (QueryFlavor)</strong>:
 * <ul>
 *   <li>{@code NONE}: Point read (readItem).</li>
 *   <li>{@code READ_ALL}: {@code readAllItems} over a single partition key.</li>
 *   <li>{@code READ_MANY}: {@code readMany} with one or more item identities.</li>
 *   <li>{@code QUERY_ITEMS}: Standard SQL query; may still contact original region for query plan acquisition even
 *       after stabilization (thus dual-region diagnostics may persist).</li>
 * </ul>
 *
 * <p><strong>End-to-End Latency Policy Integration</strong>:
 * Tests optionally apply a short-circuit latency policy to:
 * <ul>
 *   <li>Simulate threshold-based activation (e.g., property {@code COSMOS.E2E_TIMEOUT_ERROR_HIT_THRESHOLD_FOR_PPAF}).</li>
 *   <li>Differentiate pre-threshold (no failover) vs post-threshold (failover enabled) diagnostics for the same fault.</li>
 * </ul>
 *
 * <p><strong>Batch Operation Coverage</strong>:
 * Batch scenarios ensure that failover and diagnostics behaviors remain consistent with single-item operations,
 * including mock batch response materialization and hedging logic validation.</p>
 *
 * <p><strong>Safety & Cleanup</strong>:
 * Each scenario ensures:
 * <ul>
 *   <li>System properties used to gate PPAF or E2E behaviors are cleared in {@code finally} blocks.</li>
 *   <li>Clients are safely disposed to avoid cross-test interference.</li>
 * </ul>
 *
 * <p><strong>Usage Notes</strong>:
 * This suite relies on internal APIs and reflection hooks not intended for production use. It is crafted specifically
 * for validation of resilience, routing, and diagnostics fidelity across complex multi-region and transient-fault
 * conditions. Adjustments to internal SDK contracts may require corresponding test maintenance.</p>
 *
 * <p><strong>Failure Interpretation</strong>:
 * A test failure typically indicates one of:
 * <ul>
 *   <li>Unexpected retry amplification or suppression.</li>
 *   <li>Incorrect region routing (e.g., failover not triggered or not stabilized).</li>
 *   <li>Diagnostics context regression (missing region names, status codes, or retry metrics).</li>
 *   <li>Latency policy mis-integration (threshold not honored).</li>
 * </ul>
 *
 * <p><strong>Extensibility</strong>:
 * Additional scenarios (e.g., new fault types, new operation categories, multi-partition batch coverage, or read feed streaming)
 * can be added by:
 * <ol>
 *   <li>Extending the appropriate {@code @DataProvider} with new parameter rows.</li>
 *   <li>Enhancing {@code resolveDataPlaneOperation} for new operation abstractions.</li>
 *   <li>Adding new fault injection builders or transport client predicates.</li>
 * </ol>
 *
 * <p>All validations aim to ensure that PPAF delivers predictable, minimal-latency routing under regional fault pressure
 * while preserving observability through {@code CosmosDiagnostics}.</p>
 */
public class PerPartitionAutomaticFailoverE2ETests extends TestSuiteBase {

    private CosmosAsyncDatabase sharedDatabase;
    private CosmosAsyncContainer sharedSinglePartitionContainer;
    private AccountLevelLocationContext accountLevelLocationReadableLocationContext;
    private static final ImplementationBridgeHelpers.CosmosClientBuilderHelper.CosmosClientBuilderAccessor COSMOS_CLIENT_BUILDER_ACCESSOR
        = ImplementationBridgeHelpers.CosmosClientBuilderHelper.getCosmosClientBuilderAccessor();
    private static final ObjectMapper OBJECT_MAPPER = Utils.getSimpleObjectMapper();

    private static final Set<ConnectionMode> ALL_CONNECTION_MODES = new HashSet<>();
    private static final Set<ConnectionMode> ONLY_DIRECT_MODE = new HashSet<>();
    private static final Set<ConnectionMode> ONLY_GATEWAY_MODE = new HashSet<>();

    private static final CosmosEndToEndOperationLatencyPolicyConfig THREE_SEC_E2E_TIMEOUT_POLICY = new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(3)).build();

    BiConsumer<ResponseWrapper<?>, ExpectedResponseCharacteristics> validateExpectedResponseCharacteristics = (responseWrapper, expectedResponseCharacteristics) -> {
        assertThat(responseWrapper).isNotNull();

        Utils.ValueHolder<CosmosDiagnostics> cosmosDiagnosticsValueHolder = new Utils.ValueHolder<>();

        if (responseWrapper.batchResponse != null) {

            CosmosBatchResponse cosmosBatchResponse = responseWrapper.batchResponse;

            assertThat(cosmosBatchResponse.getDiagnostics()).isNotNull();
            cosmosDiagnosticsValueHolder.v = cosmosBatchResponse.getDiagnostics();
        } else if (responseWrapper.cosmosItemResponse != null) {

            CosmosItemResponse<?> cosmosItemResponse = responseWrapper.cosmosItemResponse;

            assertThat(cosmosItemResponse.getDiagnostics()).isNotNull();
            cosmosDiagnosticsValueHolder.v = cosmosItemResponse.getDiagnostics();
        } else if (responseWrapper.feedResponse != null) {

            FeedResponse<?> feedResponse = responseWrapper.feedResponse;

            assertThat(feedResponse.getCosmosDiagnostics()).isNotNull();
            cosmosDiagnosticsValueHolder.v = feedResponse.getCosmosDiagnostics();
        } else if (responseWrapper.cosmosException != null) {

            CosmosException cosmosException = responseWrapper.cosmosException;

            assertThat(cosmosException.getDiagnostics()).isNotNull();
            cosmosDiagnosticsValueHolder.v = cosmosException.getDiagnostics();
        } else {
            throw new AssertionError("One of batchResponse, cosmosItemResponse, feedResponse or cosmosException should be populated!");
        }

        assertThat(cosmosDiagnosticsValueHolder.v).isNotNull();
        CosmosDiagnostics cosmosDiagnostics = cosmosDiagnosticsValueHolder.v;

        assertThat(cosmosDiagnostics.getDiagnosticsContext()).isNotNull();
        assertThat(cosmosDiagnostics.getDiagnosticsContext().getContactedRegionNames()).isNotNull();
        assertThat(cosmosDiagnostics.getDiagnosticsContext().getContactedRegionNames()).isNotEmpty();
        assertThat(cosmosDiagnostics.getDiagnosticsContext().getContactedRegionNames().size()).isEqualTo(expectedResponseCharacteristics.expectedRegionsContactedCount);

        assertThat(cosmosDiagnostics.getDiagnosticsContext()).isNotNull();
        assertThat(cosmosDiagnostics.getDiagnosticsContext().getRetryCount()).isGreaterThanOrEqualTo(expectedResponseCharacteristics.expectedMinRetryCount);
        assertThat(cosmosDiagnostics.getDiagnosticsContext().getRetryCount()).isLessThanOrEqualTo(expectedResponseCharacteristics.expectedMaxRetryCount);

        if (expectedResponseCharacteristics.shouldFinalResponseHaveSuccess) {
            assertThat(cosmosDiagnostics.getDiagnosticsContext()).isNotNull();
            assertThat(cosmosDiagnostics.getDiagnosticsContext().getStatusCode() >= HttpConstants.StatusCodes.OK
                && cosmosDiagnostics.getDiagnosticsContext().getStatusCode() <= HttpConstants.StatusCodes.NOT_MODIFIED).isTrue();
        }
    };

    @Factory(dataProvider = "clientBuildersWithDirectSession")
    public PerPartitionAutomaticFailoverE2ETests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    // Non-write dynamic enablement scenarios: READ and QUERY (with flavors) under SERVER_GENERATED_GONE and RESPONSE_DELAY
    @DataProvider(name = "ppafNonWriteDynamicEnablementScenarios")
    public Object[][] ppafNonWriteDynamicEnablementScenarios() {

        Set<ConnectionMode> onlyDirect = new HashSet<>();
        onlyDirect.add(ConnectionMode.DIRECT);

        Set<ConnectionMode> onlyGateway = new HashSet<>();
        onlyGateway.add(ConnectionMode.GATEWAY);

        return new Object[][]{
            // GONE (DIRECT only)
            {
                "Dynamic non-write: READ with SERVER_GENERATED_GONE (DIRECT)",
                OperationType.Read,
                QueryFlavor.NONE,
                FaultKind.SERVER_GENERATED_GONE,
                HttpConstants.StatusCodes.OK,
                onlyDirect
            },
            {
                "Dynamic non-write: QUERY (readAll) with SERVER_GENERATED_GONE (DIRECT)",
                OperationType.Query,
                QueryFlavor.READ_ALL,
                FaultKind.SERVER_GENERATED_GONE,
                HttpConstants.StatusCodes.OK,
                onlyDirect
            },
            {
                "Dynamic non-write: QUERY (readMany) with SERVER_GENERATED_GONE (DIRECT)",
                OperationType.Query,
                QueryFlavor.READ_MANY,
                FaultKind.SERVER_GENERATED_GONE,
                HttpConstants.StatusCodes.OK,
                onlyDirect
            },
            {
                "Dynamic non-write: QUERY (queryItems) with SERVER_GENERATED_GONE (DIRECT)",
                OperationType.Query,
                QueryFlavor.QUERY_ITEMS,
                FaultKind.SERVER_GENERATED_GONE,
                HttpConstants.StatusCodes.OK,
                onlyDirect
            },

            // RESPONSE_DELAY (GATEWAY only)
            {
                "Dynamic non-write: READ with RESPONSE_DELAY (GATEWAY)",
                OperationType.Read,
                QueryFlavor.NONE,
                FaultKind.RESPONSE_DELAY,
                HttpConstants.StatusCodes.OK,
                onlyGateway
            },
            {
                "Dynamic non-write: QUERY (readAll) with RESPONSE_DELAY (GATEWAY)",
                OperationType.Query,
                QueryFlavor.READ_ALL,
                FaultKind.RESPONSE_DELAY,
                HttpConstants.StatusCodes.OK,
                onlyGateway
            },
            {
                "Dynamic non-write: QUERY (readMany) with RESPONSE_DELAY (GATEWAY)",
                OperationType.Query,
                QueryFlavor.READ_MANY,
                FaultKind.RESPONSE_DELAY,
                HttpConstants.StatusCodes.OK,
                onlyGateway
            },
            {
                "Dynamic non-write: QUERY (queryItems) with RESPONSE_DELAY (GATEWAY)",
                OperationType.Query,
                QueryFlavor.QUERY_ITEMS,
                FaultKind.RESPONSE_DELAY,
                HttpConstants.StatusCodes.OK,
                onlyGateway
            }
        };
    }

    @DataProvider(name = "ppafDynamicEnablement503Only")
    public Object[][] ppafDynamicEnablement503Only() {

        // When PPAF is disabled -> expect no success, single region contacted (no failover)
        ExpectedResponseCharacteristics expectedWhenDisabled = new ExpectedResponseCharacteristics()
            .setExpectedMinRetryCount(0)
            .setShouldFinalResponseHaveSuccess(false)
            .setExpectedRegionsContactedCount(1);

        // When PPAF is enabled -> expect success, single region contacted (directly routed to healthy)
        ExpectedResponseCharacteristics expectedWhenEnabled = new ExpectedResponseCharacteristics()
            .setExpectedMinRetryCount(1)
            .setShouldFinalResponseHaveSuccess(true)
            .setExpectedRegionsContactedCount(2);

        return new Object[][]{
            {
                "Dynamic enablement: CREATE with SERVICE_UNAVAILABLE/503",
                OperationType.Create,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                HttpConstants.StatusCodes.CREATED,
                expectedWhenDisabled,
                expectedWhenEnabled,
                false,
                false,
                false,
                ALL_CONNECTION_MODES
            },
            {
                "Dynamic enablement: REPLACE with SERVICE_UNAVAILABLE/503",
                OperationType.Replace,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                HttpConstants.StatusCodes.OK,
                expectedWhenDisabled,
                expectedWhenEnabled,
                false,
                false,
                false,
                ALL_CONNECTION_MODES
            },
            {
                "Dynamic enablement: UPSERT with SERVICE_UNAVAILABLE/503",
                OperationType.Upsert,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                HttpConstants.StatusCodes.OK,
                expectedWhenDisabled,
                expectedWhenEnabled,
                false,
                false,
                false,
                ALL_CONNECTION_MODES
            },
            {
                "Dynamic enablement: DELETE with SERVICE_UNAVAILABLE/503",
                OperationType.Delete,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                HttpConstants.StatusCodes.NOT_MODIFIED,
                expectedWhenDisabled,
                expectedWhenEnabled,
                false,
                false,
                false,
                ALL_CONNECTION_MODES
            },
            {
                "Dynamic enablement: PATCH with SERVICE_UNAVAILABLE/503",
                OperationType.Patch,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                HttpConstants.StatusCodes.OK,
                expectedWhenDisabled,
                expectedWhenEnabled,
                false,
                false,
                false,
                ALL_CONNECTION_MODES
            },
            {
                "Dynamic enablement: BATCH with SERVICE_UNAVAILABLE/503",
                OperationType.Batch,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                HttpConstants.StatusCodes.OK,
                expectedWhenDisabled,
                expectedWhenEnabled,
                false,
                false,
                false,
                ALL_CONNECTION_MODES
            }
        };
    }

    @BeforeClass(groups = {"multi-region"})
    public void beforeClass() {
        CosmosAsyncClient cosmosAsyncClient = getClientBuilder().buildAsyncClient();

        this.sharedDatabase = getSharedCosmosDatabase(cosmosAsyncClient);
        this.sharedSinglePartitionContainer = getSharedSinglePartitionCosmosContainer(cosmosAsyncClient);

        ONLY_GATEWAY_MODE.add(ConnectionMode.GATEWAY);
        ONLY_DIRECT_MODE.add(ConnectionMode.DIRECT);

        ALL_CONNECTION_MODES.add(ConnectionMode.DIRECT);
        ALL_CONNECTION_MODES.add(ConnectionMode.GATEWAY);

        RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) ReflectionUtils.getAsyncDocumentClient(cosmosAsyncClient);
        GlobalEndpointManager globalEndpointManager = ReflectionUtils.getGlobalEndpointManager(rxDocumentClient);
        DatabaseAccount databaseAccountSnapshot = globalEndpointManager.getLatestDatabaseAccount();

        this.accountLevelLocationReadableLocationContext = getAccountLevelLocationContext(databaseAccountSnapshot, false);
    }

    @DataProvider(name = "ppafTestConfigsWithWriteOps")
    public Object[][] ppafTestConfigsWithWriteOps() {

        ExpectedResponseCharacteristics expectedResponseCharacteristicsBeforeFailover = new ExpectedResponseCharacteristics()
            .setExpectedMinRetryCount(1)
            .setShouldFinalResponseHaveSuccess(true)
            .setExpectedRegionsContactedCount(2);

        ExpectedResponseCharacteristics expectedResponseCharacteristicsBeforeFailoverWhenE2ETimeoutSet = new ExpectedResponseCharacteristics()
            .setExpectedMinRetryCount(0)
            .setShouldFinalResponseHaveSuccess(false)
            .setExpectedRegionsContactedCount(1);

        ExpectedResponseCharacteristics expectedResponseCharacteristicsBeforeFailoverForRequestTimeout = new ExpectedResponseCharacteristics()
            .setExpectedMinRetryCount(1)
            .setShouldFinalResponseHaveSuccess(true)
            .setExpectedRegionsContactedCount(2);

        ExpectedResponseCharacteristics expectedResponseCharacteristicsBeforeFailoverForGatewayEndpointReadTimeout = new ExpectedResponseCharacteristics()
            .setExpectedMinRetryCount(1)
            .setShouldFinalResponseHaveSuccess(true)
            .setExpectedRegionsContactedCount(2);

        ExpectedResponseCharacteristics expectedResponseCharacteristicsBeforeFailoverForGatewayEndpointUnavailable = new ExpectedResponseCharacteristics()
            .setExpectedMinRetryCount(1)
            .setShouldFinalResponseHaveSuccess(true)
            .setExpectedRegionsContactedCount(2);

        ExpectedResponseCharacteristics expectedResponseCharacteristicsAfterFailover = new ExpectedResponseCharacteristics()
            .setExpectedMinRetryCount(0)
            .setExpectedMaxRetryCount(0)
            .setShouldFinalResponseHaveSuccess(true)
            .setExpectedRegionsContactedCount(1);

        return new Object[][]{
            {
                "Test failover handling for CREATE when GONE / SERVER_GENERATED_410 is injected into first preferred region for a specific server partition.",
                OperationType.Create,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_410,
                HttpConstants.StatusCodes.CREATED,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                false,
                ONLY_DIRECT_MODE
            },
            {
                "Test failover handling for REPLACE when GONE / SERVER_GENERATED_410 is injected into first preferred region for a specific server partition.",
                OperationType.Replace,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_410,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                false,
                ONLY_DIRECT_MODE
            },
            {
                "Test failover handling for UPSERT when GONE / SERVER_GENERATED_410 is injected into first preferred region for a specific server partition.",
                OperationType.Upsert,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_410,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                false,
                ONLY_DIRECT_MODE
            },
            {
                "Test failover handling for DELETE when GONE / SERVER_GENERATED_410 is injected into first preferred region for a specific server partition.",
                OperationType.Delete,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_410,
                HttpConstants.StatusCodes.NOT_MODIFIED,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                false,
                ONLY_DIRECT_MODE
            },
            {
                "Test failover handling for PATCH when GONE / SERVER_GENERATED_410 is injected into first preferred region for a specific server partition.",
                OperationType.Patch,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_410,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                false,
                ONLY_DIRECT_MODE
            },
            {
                "Test failover handling for BATCH when GONE / SERVER_GENERATED_410 is injected into first preferred region for a specific server partition.",
                OperationType.Batch,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_410,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                false,
                ONLY_DIRECT_MODE
            },
            {
                "Test failover handling for CREATE when SERVICE_UNAVAILABLE / SERVER_GENERATED_503 is injected into first preferred region for a specific server partition.",
                OperationType.Create,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                HttpConstants.StatusCodes.CREATED,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                false,
                ALL_CONNECTION_MODES
            },
            {
                "Test failover handling for REPLACE when SERVICE_UNAVAILABLE / SERVER_GENERATED_503 is injected into first preferred region for a specific server partition.",
                OperationType.Replace,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                false,
                ALL_CONNECTION_MODES
            },
            {
                "Test failover handling for UPSERT when SERVICE_UNAVAILABLE / SERVER_GENERATED_503 is injected into first preferred region for a specific server partition.",
                OperationType.Upsert,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                false,
                ALL_CONNECTION_MODES
            },
            {
                "Test failover handling for DELETE when SERVICE_UNAVAILABLE / SERVER_GENERATED_503 is injected into first preferred region for a specific server partition.",
                OperationType.Delete,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                HttpConstants.StatusCodes.NOT_MODIFIED,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                false,
                ALL_CONNECTION_MODES
            },
            {
                "Test failover handling for PATCH when SERVICE_UNAVAILABLE / SERVER_GENERATED_503 is injected into first preferred region for a specific server partition.",
                OperationType.Patch,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                false,
                ALL_CONNECTION_MODES
            },
            {
                "Test failover handling for BATCH when SERVICE_UNAVAILABLE / SERVER_GENERATED_503 is injected into first preferred region for a specific server partition.",
                OperationType.Batch,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                false,
                ALL_CONNECTION_MODES
            },
            {
                "Test failover handling for CREATE when FORBIDDEN / FORBIDDEN_WRITEFORBIDDEN is injected into first preferred region for a specific server partition.",
                OperationType.Create,
                HttpConstants.StatusCodes.FORBIDDEN,
                HttpConstants.SubStatusCodes.FORBIDDEN_WRITEFORBIDDEN,
                HttpConstants.StatusCodes.CREATED,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                false,
                ALL_CONNECTION_MODES
            },
            {
                "Test failover handling for REPLACE when FORBIDDEN / FORBIDDEN_WRITEFORBIDDEN is injected into first preferred region for a specific server partition.",
                OperationType.Replace,
                HttpConstants.StatusCodes.FORBIDDEN,
                HttpConstants.SubStatusCodes.FORBIDDEN_WRITEFORBIDDEN,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                false,
                ALL_CONNECTION_MODES
            },
            {
                "Test failover handling for UPSERT when FORBIDDEN / FORBIDDEN_WRITEFORBIDDEN is injected into first preferred region for a specific server partition.",
                OperationType.Upsert,
                HttpConstants.StatusCodes.FORBIDDEN,
                HttpConstants.SubStatusCodes.FORBIDDEN_WRITEFORBIDDEN,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                false,
                ALL_CONNECTION_MODES
            },
            {
                "Test failover handling for DELETE when FORBIDDEN / FORBIDDEN_WRITEFORBIDDEN is injected into first preferred region for a specific server partition.",
                OperationType.Delete,
                HttpConstants.StatusCodes.FORBIDDEN,
                HttpConstants.SubStatusCodes.FORBIDDEN_WRITEFORBIDDEN,
                HttpConstants.StatusCodes.NOT_MODIFIED,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                false,
                ALL_CONNECTION_MODES
            },
            {
                "Test failover handling for PATCH when FORBIDDEN / FORBIDDEN_WRITEFORBIDDEN is injected into first preferred region for a specific server partition.",
                OperationType.Patch,
                HttpConstants.StatusCodes.FORBIDDEN,
                HttpConstants.SubStatusCodes.FORBIDDEN_WRITEFORBIDDEN,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                false,
                ALL_CONNECTION_MODES
            },
            {
                "Test failover handling for BATCH when FORBIDDEN / FORBIDDEN_WRITEFORBIDDEN is injected into first preferred region for a specific server partition.",
                OperationType.Batch,
                HttpConstants.StatusCodes.FORBIDDEN,
                HttpConstants.SubStatusCodes.FORBIDDEN_WRITEFORBIDDEN,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                false,
                ALL_CONNECTION_MODES
            },
            {
                "Test failover handling for DELETE when REQUEST_TIMEOUT / UNKNOWN is injected into first preferred region for a specific server partition.",
                OperationType.Create,
                HttpConstants.StatusCodes.REQUEST_TIMEOUT,
                HttpConstants.SubStatusCodes.UNKNOWN,
                HttpConstants.StatusCodes.CREATED,
                expectedResponseCharacteristicsBeforeFailoverForRequestTimeout,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                false,
                ALL_CONNECTION_MODES
            },
            {
                "Test failover handling for REPLACE when REQUEST_TIMEOUT / UNKNOWN is injected into first preferred region for a specific server partition.",
                OperationType.Replace,
                HttpConstants.StatusCodes.REQUEST_TIMEOUT,
                HttpConstants.SubStatusCodes.UNKNOWN,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverForRequestTimeout,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                false,
                ALL_CONNECTION_MODES
            },
            {
                "Test failover handling for UPSERT when REQUEST_TIMEOUT / UNKNOWN is injected into first preferred region for a specific server partition.",
                OperationType.Upsert,
                HttpConstants.StatusCodes.REQUEST_TIMEOUT,
                HttpConstants.SubStatusCodes.UNKNOWN,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverForRequestTimeout,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                false,
                ALL_CONNECTION_MODES
            },
            {
                "Test failover handling for DELETE when REQUEST_TIMEOUT / UNKNOWN is injected into first preferred region for a specific server partition.",
                OperationType.Delete,
                HttpConstants.StatusCodes.REQUEST_TIMEOUT,
                HttpConstants.SubStatusCodes.UNKNOWN,
                HttpConstants.StatusCodes.NOT_MODIFIED,
                expectedResponseCharacteristicsBeforeFailoverForRequestTimeout,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                false,
                ALL_CONNECTION_MODES
            },
            {
                "Test failover handling for PATCH when REQUEST_TIMEOUT / UNKNOWN is injected into first preferred region for a specific server partition.",
                OperationType.Patch,
                HttpConstants.StatusCodes.REQUEST_TIMEOUT,
                HttpConstants.SubStatusCodes.UNKNOWN,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverForRequestTimeout,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                false,
                ALL_CONNECTION_MODES
            },
            {
                "Test failover handling for BATCH when REQUEST_TIMEOUT / UNKNOWN is injected into first preferred region for a specific server partition.",
                OperationType.Batch,
                HttpConstants.StatusCodes.REQUEST_TIMEOUT,
                HttpConstants.SubStatusCodes.UNKNOWN,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverForRequestTimeout,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                false,
                ALL_CONNECTION_MODES
            },
            {
                "Test failover handling for DELETE when REQUEST_TIMEOUT / GATEWAY_ENDPOINT_READ_TIMEOUT is injected into first preferred region for a specific server partition.",
                OperationType.Create,
                HttpConstants.StatusCodes.REQUEST_TIMEOUT,
                HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT,
                HttpConstants.StatusCodes.CREATED,
                expectedResponseCharacteristicsBeforeFailoverForGatewayEndpointReadTimeout,
                expectedResponseCharacteristicsAfterFailover,
                true,
                true,
                false,
                ONLY_GATEWAY_MODE
            },
            {
                "Test failover handling for REPLACE when REQUEST_TIMEOUT / GATEWAY_ENDPOINT_READ_TIMEOUT is injected into first preferred region for a specific server partition.",
                OperationType.Replace,
                HttpConstants.StatusCodes.REQUEST_TIMEOUT,
                HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverForGatewayEndpointReadTimeout,
                expectedResponseCharacteristicsAfterFailover,
                true,
                true,
                false,
                ONLY_GATEWAY_MODE
            },
            {
                "Test failover handling for UPSERT when REQUEST_TIMEOUT / GATEWAY_ENDPOINT_READ_TIMEOUT is injected into first preferred region for a specific server partition.",
                OperationType.Upsert,
                HttpConstants.StatusCodes.REQUEST_TIMEOUT,
                HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverForGatewayEndpointReadTimeout,
                expectedResponseCharacteristicsAfterFailover,
                true,
                true,
                false,
                ONLY_GATEWAY_MODE
            },
            {
                "Test failover handling for DELETE when REQUEST_TIMEOUT / GATEWAY_ENDPOINT_READ_TIMEOUT is injected into first preferred region for a specific server partition.",
                OperationType.Delete,
                HttpConstants.StatusCodes.REQUEST_TIMEOUT,
                HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT,
                HttpConstants.StatusCodes.NOT_MODIFIED,
                expectedResponseCharacteristicsBeforeFailoverForGatewayEndpointReadTimeout,
                expectedResponseCharacteristicsAfterFailover,
                true,
                true,
                false,
                ONLY_GATEWAY_MODE
            },
            {
                "Test failover handling for PATCH when REQUEST_TIMEOUT / GATEWAY_ENDPOINT_READ_TIMEOUT is injected into first preferred region for a specific server partition.",
                OperationType.Patch,
                HttpConstants.StatusCodes.REQUEST_TIMEOUT,
                HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverForGatewayEndpointReadTimeout,
                expectedResponseCharacteristicsAfterFailover,
                true,
                true,
                false,
                ONLY_GATEWAY_MODE
            },
            {
                "Test failover handling for BATCH when REQUEST_TIMEOUT / GATEWAY_ENDPOINT_READ_TIMEOUT is injected into first preferred region for a specific server partition.",
                OperationType.Batch,
                HttpConstants.StatusCodes.REQUEST_TIMEOUT,
                HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverForGatewayEndpointReadTimeout,
                expectedResponseCharacteristicsAfterFailover,
                true,
                true,
                false,
                ONLY_GATEWAY_MODE
            },
            {
                "Test failover handling for DELETE when REQUEST_TIMEOUT / GATEWAY_ENDPOINT_READ_TIMEOUT is injected into first preferred region for a specific server partition.",
                OperationType.Create,
                HttpConstants.StatusCodes.REQUEST_TIMEOUT,
                HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT,
                HttpConstants.StatusCodes.CREATED,
                expectedResponseCharacteristicsBeforeFailoverForGatewayEndpointReadTimeout,
                expectedResponseCharacteristicsAfterFailover,
                true,
                true,
                false,
                ONLY_GATEWAY_MODE
            },
            {
                "Test failover handling for REPLACE when REQUEST_TIMEOUT / GATEWAY_ENDPOINT_READ_TIMEOUT is injected into first preferred region for a specific server partition.",
                OperationType.Replace,
                HttpConstants.StatusCodes.REQUEST_TIMEOUT,
                HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverForGatewayEndpointReadTimeout,
                expectedResponseCharacteristicsAfterFailover,
                true,
                true,
                false,
                ONLY_GATEWAY_MODE
            },
            {
                "Test failover handling for UPSERT when REQUEST_TIMEOUT / GATEWAY_ENDPOINT_READ_TIMEOUT is injected into first preferred region for a specific server partition.",
                OperationType.Upsert,
                HttpConstants.StatusCodes.REQUEST_TIMEOUT,
                HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverForGatewayEndpointReadTimeout,
                expectedResponseCharacteristicsAfterFailover,
                true,
                true,
                false,
                ONLY_GATEWAY_MODE
            },
            {
                "Test failover handling for DELETE when REQUEST_TIMEOUT / GATEWAY_ENDPOINT_READ_TIMEOUT is injected into first preferred region for a specific server partition.",
                OperationType.Delete,
                HttpConstants.StatusCodes.REQUEST_TIMEOUT,
                HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT,
                HttpConstants.StatusCodes.NOT_MODIFIED,
                expectedResponseCharacteristicsBeforeFailoverForGatewayEndpointReadTimeout,
                expectedResponseCharacteristicsAfterFailover,
                true,
                true,
                false,
                ONLY_GATEWAY_MODE
            },
            {
                "Test failover handling for PATCH when REQUEST_TIMEOUT / GATEWAY_ENDPOINT_READ_TIMEOUT is injected into first preferred region for a specific server partition.",
                OperationType.Patch,
                HttpConstants.StatusCodes.REQUEST_TIMEOUT,
                HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverForGatewayEndpointReadTimeout,
                expectedResponseCharacteristicsAfterFailover,
                true,
                true,
                false,
                ONLY_GATEWAY_MODE
            },
            {
                "Test failover handling for BATCH when REQUEST_TIMEOUT / GATEWAY_ENDPOINT_READ_TIMEOUT is injected into first preferred region for a specific server partition.",
                OperationType.Batch,
                HttpConstants.StatusCodes.REQUEST_TIMEOUT,
                HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverForGatewayEndpointReadTimeout,
                expectedResponseCharacteristicsAfterFailover,
                true,
                true,
                false,
                ONLY_GATEWAY_MODE
            },
            {
                "Test failover handling for CREATE when SERVICE_UNAVAILABLE / GATEWAY_ENDPOINT_UNAVAILABLE is injected into first preferred region for a specific server partition.",
                OperationType.Create,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_UNAVAILABLE,
                HttpConstants.StatusCodes.CREATED,
                expectedResponseCharacteristicsBeforeFailoverForGatewayEndpointUnavailable,
                expectedResponseCharacteristicsAfterFailover,
                true,
                false,
                false,
                ONLY_GATEWAY_MODE
            },
            {
                "Test failover handling for REPLACE when SERVICE_UNAVAILABLE / GATEWAY_ENDPOINT_UNAVAILABLE is injected into first preferred region for a specific server partition.",
                OperationType.Replace,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_UNAVAILABLE,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverForGatewayEndpointUnavailable,
                expectedResponseCharacteristicsAfterFailover,
                true,
                false,
                false,
                ONLY_GATEWAY_MODE
            },
            {
                "Test failover handling for UPSERT when SERVICE_UNAVAILABLE / GATEWAY_ENDPOINT_UNAVAILABLE is injected into first preferred region for a specific server partition.",
                OperationType.Upsert,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_UNAVAILABLE,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverForGatewayEndpointUnavailable,
                expectedResponseCharacteristicsAfterFailover,
                true,
                false,
                false,
                ONLY_GATEWAY_MODE
            },
            {
                "Test failover handling for DELETE when SERVICE_UNAVAILABLE / GATEWAY_ENDPOINT_UNAVAILABLE is injected into first preferred region for a specific server partition.",
                OperationType.Delete,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_UNAVAILABLE,
                HttpConstants.StatusCodes.NOT_MODIFIED,
                expectedResponseCharacteristicsBeforeFailoverForGatewayEndpointUnavailable,
                expectedResponseCharacteristicsAfterFailover,
                true,
                false,
                false,
                ONLY_GATEWAY_MODE
            },
            {
                "Test failover handling for PATCH when SERVICE_UNAVAILABLE / GATEWAY_ENDPOINT_UNAVAILABLE is injected into first preferred region for a specific server partition.",
                OperationType.Patch,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_UNAVAILABLE,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverForGatewayEndpointUnavailable,
                expectedResponseCharacteristicsAfterFailover,
                true,
                false,
                false,
                ONLY_GATEWAY_MODE
            },
            {
                "Test failover handling for BATCH when SERVICE_UNAVAILABLE / GATEWAY_ENDPOINT_UNAVAILABLE is injected into first preferred region for a specific server partition.",
                OperationType.Batch,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_UNAVAILABLE,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverForGatewayEndpointUnavailable,
                expectedResponseCharacteristicsAfterFailover,
                true,
                false,
                false,
                ONLY_GATEWAY_MODE
            },
            {
                "Test failover handling for CREATE with e2e timeout and when GONE / SERVER_GENERATED_410 is injected into first preferred region for a specific server partition.",
                OperationType.Create,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_410,
                HttpConstants.StatusCodes.CREATED,
                expectedResponseCharacteristicsBeforeFailoverWhenE2ETimeoutSet,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                true,
                ONLY_DIRECT_MODE
            },
            {
                "Test failover handling for REPLACE with e2e timeout and when GONE / SERVER_GENERATED_410 is injected into first preferred region for a specific server partition.",
                OperationType.Replace,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_410,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverWhenE2ETimeoutSet,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                true,
                ONLY_DIRECT_MODE
            },
            {
                "Test failover handling for UPSERT with e2e timeout and when GONE / SERVER_GENERATED_410 is injected into first preferred region for a specific server partition.",
                OperationType.Upsert,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_410,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverWhenE2ETimeoutSet,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                true,
                ONLY_DIRECT_MODE
            },
            {
                "Test failover handling for DELETE with e2e timeout and when GONE / SERVER_GENERATED_410 is injected into first preferred region for a specific server partition.",
                OperationType.Delete,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_410,
                HttpConstants.StatusCodes.NOT_MODIFIED,
                expectedResponseCharacteristicsBeforeFailoverWhenE2ETimeoutSet,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                true,
                ONLY_DIRECT_MODE
            },
            {
                "Test failover handling for PATCH with e2e timeout and when GONE / SERVER_GENERATED_410 is injected into first preferred region for a specific server partition.",
                OperationType.Patch,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_410,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverWhenE2ETimeoutSet,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                true,
                ONLY_DIRECT_MODE
            },
            {
                "Test failover handling for BATCH with e2e timeout and when GONE / SERVER_GENERATED_410 is injected into first preferred region for a specific server partition.",
                OperationType.Batch,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_410,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverWhenE2ETimeoutSet,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                true,
                ONLY_DIRECT_MODE
            },
            {
                "Test failover handling for CREATE with e2e timeout and when SERVICE_UNAVAILABLE / SERVER_GENERATED_503 is injected into first preferred region for a specific server partition.",
                OperationType.Create,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                HttpConstants.StatusCodes.CREATED,
                expectedResponseCharacteristicsBeforeFailoverWhenE2ETimeoutSet,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                true,
                ONLY_GATEWAY_MODE
            },
            {
                "Test failover handling for REPLACE with e2e timeout and when SERVICE_UNAVAILABLE / SERVER_GENERATED_503 and delay too is injected into first preferred region for a specific server partition.",
                OperationType.Replace,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverWhenE2ETimeoutSet,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                true,
                ONLY_GATEWAY_MODE
            },
            {
                "Test failover handling for UPSERT with e2e timeout and when SERVICE_UNAVAILABLE / SERVER_GENERATED_503 and delay too is injected into first preferred region for a specific server partition.",
                OperationType.Upsert,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverWhenE2ETimeoutSet,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                true,
                ONLY_GATEWAY_MODE
            },
            {
                "Test failover handling for DELETE with e2e timeout and when SERVICE_UNAVAILABLE / SERVER_GENERATED_503 and delay too is injected into first preferred region for a specific server partition.",
                OperationType.Delete,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                HttpConstants.StatusCodes.NOT_MODIFIED,
                expectedResponseCharacteristicsBeforeFailoverWhenE2ETimeoutSet,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                true,
                ONLY_GATEWAY_MODE
            },
            {
                "Test failover handling for PATCH with e2e timeout and when SERVICE_UNAVAILABLE / SERVER_GENERATED_503 is and delay too injected into first preferred region for a specific server partition.",
                OperationType.Patch,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverWhenE2ETimeoutSet,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                true,
                ONLY_GATEWAY_MODE
            },
            {
                "Test failover handling for BATCH with e2e timeout and when SERVICE_UNAVAILABLE / SERVER_GENERATED_503 is and delay too injected into first preferred region for a specific server partition.",
                OperationType.Batch,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverWhenE2ETimeoutSet,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                true,
                ONLY_GATEWAY_MODE
            }
        };
    }

    /**
     * End-to-end validation of Per-Partition Automatic Failover (PPAF) for write operations
     * (Create, Replace, Upsert, Delete, Patch, Batch) when a failover-eligible fault is injected
     * for one partition key range in the first preferred region.
     *
     * <p>Phases:</p>
     * <ul>
     *   <li>Pre-failover: injected error surfaces; request retries and/or hedges (unless gated by E2E timeout threshold).</li>
     *   <li>Post-failover: subsequent request routes directly to healthy region (single region, zero retries) unless
     *       E2E timeout gating still accumulating threshold.</li>
     * </ul>
     *
     * <p>Mechanics:</p>
     * <ul>
     *   <li>DIRECT: TransportClient mocked; targeted (region + PK range) throws configured CosmosException.</li>
     *   <li>GATEWAY: HttpClient mocked; targeted region URI throws CosmosException or network exception (read/socket timeout) or delayed fault.</li>
     *   <li>GlobalEndpointManager owner replaced with delegating manager to surface dynamic PPAF enablement flag.</li>
     *   <li>E2E latency policy optionally applied; threshold (COSMOS.E2E_TIMEOUT_ERROR_HIT_THRESHOLD_FOR_PPAF) controls activation.</li>
     * </ul>
     *
     * <p>Assertions:</p>
     * <ul>
     *   <li>Regions contacted count (before vs after failover).</li>
     *   <li>Retry count bounds.</li>
     *   <li>Success vs failure based on phase and configuration.</li>
     * </ul>
     */
    @Test(groups = {"multi-region"}, dataProvider = "ppafTestConfigsWithWriteOps")
    public void testPpafWithWriteFailoverWithEligibleErrorStatusCodes(
        String testType,
        OperationType operationType,
        int errorStatusCodeToMockFromPartitionInUnhealthyRegion,
        int errorSubStatusCodeToMockFromPartitionInUnhealthyRegion,
        int successStatusCode,
        ExpectedResponseCharacteristics expectedResponseCharacteristicsBeforeFailover,
        ExpectedResponseCharacteristics expectedResponseCharacteristicsAfterFailover,
        boolean shouldThrowNetworkError,
        boolean shouldThrowReadTimeoutExceptionWhenNetworkError,
        boolean shouldUseE2ETimeout,
        Set<ConnectionMode> allowedConnectionModes) {

        ConnectionPolicy connectionPolicy = COSMOS_CLIENT_BUILDER_ACCESSOR.getConnectionPolicy(getClientBuilder());
        ConnectionMode connectionMode = connectionPolicy.getConnectionMode();

        if (!allowedConnectionModes.contains(connectionMode)) {
            throw new SkipException(String.format("Test with type : %s not eligible for specified connection mode %s.", testType, connectionMode));
        }

        if (connectionMode == ConnectionMode.DIRECT) {

            TransportClient transportClientMock = Mockito.mock(TransportClient.class);
            List<String> preferredRegions = this.accountLevelLocationReadableLocationContext.serviceOrderedReadableRegions;
            Map<String, String> readableRegionNameToEndpoint = this.accountLevelLocationReadableLocationContext.regionNameToEndpoint;
            Utils.ValueHolder<CosmosAsyncClient> cosmosAsyncClientValueHolder = new Utils.ValueHolder<>();

            try {

                if (shouldUseE2ETimeout) {
                    System.setProperty("COSMOS.E2E_TIMEOUT_ERROR_HIT_THRESHOLD_FOR_PPAF", "2");
                }

                CosmosClientBuilder cosmosClientBuilder = getClientBuilder();

                // todo: evaluate whether Batch operation needs op-level e2e timeout and availability strategy
                if (operationType.equals(OperationType.Batch) && shouldUseE2ETimeout) {
                    cosmosClientBuilder.endToEndOperationLatencyPolicyConfig(THREE_SEC_E2E_TIMEOUT_POLICY);
                }

                CosmosAsyncClient asyncClient = cosmosClientBuilder.buildAsyncClient();
                cosmosAsyncClientValueHolder.v = asyncClient;

                CosmosAsyncContainer asyncContainer = asyncClient
                    .getDatabase(this.sharedDatabase.getId())
                    .getContainer(this.sharedSinglePartitionContainer.getId());

                RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) ReflectionUtils.getAsyncDocumentClient(asyncClient);

                StoreClient storeClient = ReflectionUtils.getStoreClient(rxDocumentClient);
                ReplicatedResourceClient replicatedResourceClient = ReflectionUtils.getReplicatedResourceClient(storeClient);
                ConsistencyReader consistencyReader = ReflectionUtils.getConsistencyReader(replicatedResourceClient);
                StoreReader storeReader = ReflectionUtils.getStoreReader(consistencyReader);

                ConsistencyWriter consistencyWriter = ReflectionUtils.getConsistencyWriter(replicatedResourceClient);
                Utils.ValueHolder<List<PartitionKeyRange>> partitionKeyRangesForContainer
                    = getPartitionKeyRangesForContainer(asyncContainer, rxDocumentClient).block();

                assertThat(partitionKeyRangesForContainer).isNotNull();
                assertThat(partitionKeyRangesForContainer.v).isNotNull();
                assertThat(partitionKeyRangesForContainer.v.size()).isGreaterThanOrEqualTo(1);

                PartitionKeyRange partitionKeyRangeWithIssues = partitionKeyRangesForContainer.v.get(0);

                assertThat(preferredRegions).isNotNull();
                assertThat(preferredRegions.size()).isGreaterThanOrEqualTo(1);

                String regionWithIssues = preferredRegions.get(0);
                RegionalRoutingContext regionalRoutingContextWithIssues = new RegionalRoutingContext(new URI(readableRegionNameToEndpoint.get(regionWithIssues)));

                ReflectionUtils.setTransportClient(storeReader, transportClientMock);
                ReflectionUtils.setTransportClient(consistencyWriter, transportClientMock);

                setupTransportClientToReturnSuccessResponse(transportClientMock, constructStoreResponse(operationType, successStatusCode));

                CosmosException cosmosException = createCosmosException(
                    errorStatusCodeToMockFromPartitionInUnhealthyRegion,
                    errorSubStatusCodeToMockFromPartitionInUnhealthyRegion);

                setupTransportClientToThrowCosmosException(
                    transportClientMock,
                    partitionKeyRangeWithIssues,
                    regionalRoutingContextWithIssues,
                    cosmosException);

                // Swap GlobalEndpointManager.owner to a delegating wrapper that toggles PPAF flag on DatabaseAccount
                GlobalEndpointManager globalEndpointManager = ReflectionUtils.getGlobalEndpointManager(rxDocumentClient);
                DatabaseAccountManagerInternal originalOwner = ReflectionUtils.getGlobalEndpointManagerOwner(globalEndpointManager);

                AtomicReference<Boolean> ppafEnabledRef = new AtomicReference<>(Boolean.TRUE);
                DatabaseAccountManagerInternal overridingOwner = new DelegatingDatabaseAccountManagerInternal(originalOwner, ppafEnabledRef);
                ReflectionUtils.setGlobalEndpointManagerOwner(globalEndpointManager, overridingOwner);

                DatabaseAccount latestDatabaseAccountSnapshot = globalEndpointManager.getLatestDatabaseAccount();
                globalEndpointManager.refreshLocationAsync(latestDatabaseAccountSnapshot, true).block();

                TestItem testItem = TestItem.createNewItem();

                Function<OperationInvocationParamsWrapper, ResponseWrapper<?>> dataPlaneOperation = resolveDataPlaneOperation(operationType);

                OperationInvocationParamsWrapper operationInvocationParamsWrapper = new OperationInvocationParamsWrapper();
                operationInvocationParamsWrapper.asyncContainer = asyncContainer;
                operationInvocationParamsWrapper.createdTestItem = testItem;
                operationInvocationParamsWrapper.itemRequestOptions = shouldUseE2ETimeout ? new CosmosItemRequestOptions().setCosmosEndToEndOperationLatencyPolicyConfig(THREE_SEC_E2E_TIMEOUT_POLICY) : new CosmosItemRequestOptions();
                operationInvocationParamsWrapper.patchItemRequestOptions = shouldUseE2ETimeout ? new CosmosPatchItemRequestOptions().setCosmosEndToEndOperationLatencyPolicyConfig(THREE_SEC_E2E_TIMEOUT_POLICY) : new CosmosPatchItemRequestOptions();

                if (shouldUseE2ETimeout) {

                    int iterationsToRun = Configs.getAllowedE2ETimeoutHitCountForPPAF();

                    for (int i = 1; i <= iterationsToRun + 1; i++) {
                        ResponseWrapper<?> responseBeforeFailover = dataPlaneOperation.apply(operationInvocationParamsWrapper);
                        this.validateExpectedResponseCharacteristics.accept(responseBeforeFailover, expectedResponseCharacteristicsBeforeFailover);
                    }
                } else {
                    ResponseWrapper<?> responseBeforeFailover = dataPlaneOperation.apply(operationInvocationParamsWrapper);
                    this.validateExpectedResponseCharacteristics.accept(responseBeforeFailover, expectedResponseCharacteristicsBeforeFailover);
                }

                ResponseWrapper<?> responseAfterFailover = dataPlaneOperation.apply(operationInvocationParamsWrapper);
                this.validateExpectedResponseCharacteristics.accept(responseAfterFailover, expectedResponseCharacteristicsAfterFailover);
            } catch (Exception e) {
                Assertions.fail("The test ran into an exception {}", e);
            } finally {
                System.clearProperty("COSMOS.E2E_TIMEOUT_ERROR_HIT_THRESHOLD_FOR_PPAF");
                System.clearProperty("COSMOS.IS_PER_PARTITION_AUTOMATIC_FAILOVER_ENABLED");
                safeClose(cosmosAsyncClientValueHolder.v);
            }
        }

        if (connectionMode == ConnectionMode.GATEWAY) {
            HttpClient mockedHttpClient = Mockito.mock(HttpClient.class);
            List<String> preferredRegions = this.accountLevelLocationReadableLocationContext.serviceOrderedReadableRegions;
            Map<String, String> readableRegionNameToEndpoint = this.accountLevelLocationReadableLocationContext.regionNameToEndpoint;
            Utils.ValueHolder<CosmosAsyncClient> cosmosAsyncClientValueHolder = new Utils.ValueHolder<>();

            try {

                if (shouldUseE2ETimeout) {
                    System.setProperty("COSMOS.E2E_TIMEOUT_ERROR_HIT_THRESHOLD_FOR_PPAF", "2");
                }

                CosmosClientBuilder cosmosClientBuilder = getClientBuilder();

                // todo: evaluate whether Batch operation needs op-level e2e timeout and availability strategy
                if (operationType.equals(OperationType.Batch) && shouldUseE2ETimeout) {
                    cosmosClientBuilder.endToEndOperationLatencyPolicyConfig(THREE_SEC_E2E_TIMEOUT_POLICY);
                }

                CosmosAsyncClient asyncClient = cosmosClientBuilder.buildAsyncClient();
                cosmosAsyncClientValueHolder.v = asyncClient;

                CosmosAsyncContainer asyncContainer = asyncClient
                    .getDatabase(this.sharedDatabase.getId())
                    .getContainer(this.sharedSinglePartitionContainer.getId());

                // populates collection cache and pkrange cache
                asyncContainer.getFeedRanges().block();

                RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) ReflectionUtils.getAsyncDocumentClient(asyncClient);

                RxStoreModel rxStoreModel = ReflectionUtils.getGatewayProxy(rxDocumentClient);

                GlobalEndpointManager globalEndpointManager = ReflectionUtils.getGlobalEndpointManager(rxDocumentClient);
                DatabaseAccount databaseAccount = globalEndpointManager.getLatestDatabaseAccount();

                // Swap GlobalEndpointManager.owner to a delegating wrapper that toggles PPAF flag on DatabaseAccount
                DatabaseAccountManagerInternal originalOwner = ReflectionUtils.getGlobalEndpointManagerOwner(globalEndpointManager);

                AtomicReference<Boolean> ppafEnabledRef = new AtomicReference<>(Boolean.TRUE);
                DatabaseAccountManagerInternal overridingOwner = new DelegatingDatabaseAccountManagerInternal(originalOwner, ppafEnabledRef);
                ReflectionUtils.setGlobalEndpointManagerOwner(globalEndpointManager, overridingOwner);

                DatabaseAccount latestDatabaseAccountSnapshot = globalEndpointManager.getLatestDatabaseAccount();
                globalEndpointManager.refreshLocationAsync(latestDatabaseAccountSnapshot, true).block();

                assertThat(preferredRegions).isNotNull();
                assertThat(preferredRegions.size()).isGreaterThanOrEqualTo(1);

                String regionWithIssues = preferredRegions.get(0);
                URI locationEndpointWithIssues = new URI(readableRegionNameToEndpoint.get(regionWithIssues) + "dbs/" + this.sharedDatabase.getId() + "/colls/" + this.sharedSinglePartitionContainer.getId() + "/docs");

                ReflectionUtils.setGatewayHttpClient(rxStoreModel, mockedHttpClient);

                setupHttpClientToReturnSuccessResponse(mockedHttpClient, operationType, databaseAccount, successStatusCode);

                CosmosException cosmosException = createCosmosException(
                    errorStatusCodeToMockFromPartitionInUnhealthyRegion,
                    errorSubStatusCodeToMockFromPartitionInUnhealthyRegion);

                setupHttpClientToThrowCosmosException(
                    mockedHttpClient,
                    locationEndpointWithIssues,
                    cosmosException,
                    shouldThrowNetworkError,
                    shouldThrowReadTimeoutExceptionWhenNetworkError,
                    shouldUseE2ETimeout);

                TestItem testItem = TestItem.createNewItem();

                Function<OperationInvocationParamsWrapper, ResponseWrapper<?>> dataPlaneOperation = resolveDataPlaneOperation(operationType);

                OperationInvocationParamsWrapper operationInvocationParamsWrapper = new OperationInvocationParamsWrapper();
                operationInvocationParamsWrapper.asyncContainer = asyncContainer;
                operationInvocationParamsWrapper.createdTestItem = testItem;
                operationInvocationParamsWrapper.itemRequestOptions = shouldUseE2ETimeout ? new CosmosItemRequestOptions().setCosmosEndToEndOperationLatencyPolicyConfig(THREE_SEC_E2E_TIMEOUT_POLICY) : new CosmosItemRequestOptions();
                operationInvocationParamsWrapper.patchItemRequestOptions = shouldUseE2ETimeout ? new CosmosPatchItemRequestOptions().setCosmosEndToEndOperationLatencyPolicyConfig(THREE_SEC_E2E_TIMEOUT_POLICY) : new CosmosPatchItemRequestOptions();

                if (shouldUseE2ETimeout) {

                    int iterationsToRun = Configs.getAllowedE2ETimeoutHitCountForPPAF();

                    for (int i = 1; i <= iterationsToRun + 1; i++) {
                        ResponseWrapper<?> responseBeforeFailover = dataPlaneOperation.apply(operationInvocationParamsWrapper);
                        this.validateExpectedResponseCharacteristics.accept(responseBeforeFailover, expectedResponseCharacteristicsBeforeFailover);
                    }
                } else {
                    ResponseWrapper<?> responseBeforeFailover = dataPlaneOperation.apply(operationInvocationParamsWrapper);

                    assertThat(responseBeforeFailover).isNotNull();
                    this.validateExpectedResponseCharacteristics.accept(responseBeforeFailover, expectedResponseCharacteristicsBeforeFailover);
                }

                ResponseWrapper<?> responseAfterFailover = dataPlaneOperation.apply(operationInvocationParamsWrapper);
                this.validateExpectedResponseCharacteristics.accept(responseAfterFailover, expectedResponseCharacteristicsAfterFailover);
            } catch (Exception e) {
                Assertions.fail("The test ran into an exception {}", e);
            } finally {
                System.clearProperty("COSMOS.E2E_TIMEOUT_ERROR_HIT_THRESHOLD_FOR_PPAF");
                System.clearProperty("COSMOS.IS_PER_PARTITION_AUTOMATIC_FAILOVER_ENABLED");
                safeClose(cosmosAsyncClientValueHolder.v);
            }
        }
    }

    /**
     * Verifies per-partition automatic failover (PPAF) dynamic enablement by toggling
     * DatabaseAccount#isPerPartitionFailoverBehaviorEnabled at runtime via a reflected override
     * of GlobalEndpointManager.owner (DatabaseAccountManagerInternal).
     *
     * <p><strong>Test strategy</strong></p>
     * <ul>
     *   <li>Build a CosmosAsyncClient from the provided builder.</li>
     *   <li>Use ReflectionUtils to obtain GlobalEndpointManager from the underlying RxDocumentClient.</li>
     *   <li>Replace its private owner with a delegating DatabaseAccountManagerInternal that injects
     *       DatabaseAccount#setIsPerPartitionFailoverBehaviorEnabled(enabledRef.get()).</li>
     *   <li>Mock transport (DIRECT) or HttpClient (GATEWAY) to simulate a 503 on the primary region
     *       and success elsewhere, mirroring the base PPAF test.</li>
     *   <li>Run in phases:</li>
     * </ul>
     * <ol>
     *   <li>PPAF disabled — expect failure characteristics (no success).</li>
     *   <li>PPAF enabled — expect success characteristics (routes to healthy).</li>
     *   <li>PPAF disabled again — expect failure again (toggle verified).</li>
     * </ol>
     *
     * <p>After each toggle, call refreshLocationAsync(forceRefresh=true) so GlobalEndpointManager
     * observes the updated DatabaseAccount flags immediately.</p>
     *
     * <p>Expectations are provided by the data provider: when disabled, the request should not succeed;
     * when enabled, it should succeed. Works for both DIRECT and GATEWAY connection modes.</p>
     */
    @Test(groups = {"multi-region"}, dataProvider = "ppafDynamicEnablement503Only")
    public void testPpafWithWriteFailoverWithEligibleErrorStatusCodesWithPpafDynamicEnablement(
        String testType,
        OperationType operationType,
        int errorStatusCodeToMockFromPartitionInUnhealthyRegion,
        int errorSubStatusCodeToMockFromPartitionInUnhealthyRegion,
        int successStatusCode,
        ExpectedResponseCharacteristics expectedResponseCharacteristicsWhenPpafIsDisabled,
        ExpectedResponseCharacteristics expectedResponseCharacteristicsWhenPpafIsEnabled,
        boolean shouldThrowNetworkError,
        boolean shouldThrowReadTimeoutExceptionWhenNetworkError,
        boolean shouldUseE2ETimeout,
        Set<ConnectionMode> allowedConnectionModes) {

        ConnectionPolicy connectionPolicy = COSMOS_CLIENT_BUILDER_ACCESSOR.getConnectionPolicy(getClientBuilder());
        ConnectionMode connectionMode = connectionPolicy.getConnectionMode();

        if (!allowedConnectionModes.contains(connectionMode)) {
            throw new SkipException(String.format("Test with type : %s not eligible for specified connection mode %s.", testType, connectionMode));
        }

        // DIRECT flow: swap transport client, inject error for primary region/PK range, and verify phase-by-phase
        if (connectionMode == ConnectionMode.DIRECT) {
            TransportClient transportClientMock = Mockito.mock(TransportClient.class);
            List<String> preferredRegions = this.accountLevelLocationReadableLocationContext.serviceOrderedReadableRegions;
            Map<String, String> readableRegionNameToEndpoint = this.accountLevelLocationReadableLocationContext.regionNameToEndpoint;
            Utils.ValueHolder<CosmosAsyncClient> cosmosAsyncClientValueHolder = new Utils.ValueHolder<>();

            try {
                CosmosClientBuilder cosmosClientBuilder = getClientBuilder();

                if (operationType.equals(OperationType.Batch) && shouldUseE2ETimeout) {
                    cosmosClientBuilder.endToEndOperationLatencyPolicyConfig(THREE_SEC_E2E_TIMEOUT_POLICY);
                }

                CosmosAsyncClient asyncClient = cosmosClientBuilder.buildAsyncClient();
                cosmosAsyncClientValueHolder.v = asyncClient;

                CosmosAsyncContainer asyncContainer = asyncClient
                    .getDatabase(this.sharedDatabase.getId())
                    .getContainer(this.sharedSinglePartitionContainer.getId());

                RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) ReflectionUtils.getAsyncDocumentClient(asyncClient);

                // Swap GlobalEndpointManager.owner to a delegating wrapper that toggles PPAF flag on DatabaseAccount
                GlobalEndpointManager globalEndpointManager = ReflectionUtils.getGlobalEndpointManager(rxDocumentClient);
                DatabaseAccountManagerInternal originalOwner = ReflectionUtils.getGlobalEndpointManagerOwner(globalEndpointManager);

                AtomicReference<Boolean> ppafEnabledRef = new AtomicReference<>(Boolean.FALSE);
                DatabaseAccountManagerInternal overridingOwner = new DelegatingDatabaseAccountManagerInternal(originalOwner, ppafEnabledRef);
                ReflectionUtils.setGlobalEndpointManagerOwner(globalEndpointManager, overridingOwner);

                StoreClient storeClient = ReflectionUtils.getStoreClient(rxDocumentClient);
                ReplicatedResourceClient replicatedResourceClient = ReflectionUtils.getReplicatedResourceClient(storeClient);
                ConsistencyReader consistencyReader = ReflectionUtils.getConsistencyReader(replicatedResourceClient);
                StoreReader storeReader = ReflectionUtils.getStoreReader(consistencyReader);

                ConsistencyWriter consistencyWriter = ReflectionUtils.getConsistencyWriter(replicatedResourceClient);
                Utils.ValueHolder<List<PartitionKeyRange>> partitionKeyRangesForContainer
                    = getPartitionKeyRangesForContainer(asyncContainer, rxDocumentClient).block();

                assertThat(partitionKeyRangesForContainer).isNotNull();
                assertThat(partitionKeyRangesForContainer.v).isNotNull();
                assertThat(partitionKeyRangesForContainer.v.size()).isGreaterThanOrEqualTo(1);

                PartitionKeyRange partitionKeyRangeWithIssues = partitionKeyRangesForContainer.v.get(0);

                assertThat(preferredRegions).isNotNull();
                assertThat(preferredRegions.size()).isGreaterThanOrEqualTo(1);

                String regionWithIssues = preferredRegions.get(0);
                RegionalRoutingContext regionalRoutingContextWithIssues = new RegionalRoutingContext(new URI(readableRegionNameToEndpoint.get(regionWithIssues)));

                // Redirect all store calls through our mocked transport client
                ReflectionUtils.setTransportClient(storeReader, transportClientMock);
                ReflectionUtils.setTransportClient(consistencyWriter, transportClientMock);

                setupTransportClientToReturnSuccessResponse(transportClientMock, constructStoreResponse(operationType, successStatusCode));

                CosmosException cosmosException = createCosmosException(
                    errorStatusCodeToMockFromPartitionInUnhealthyRegion,
                    errorSubStatusCodeToMockFromPartitionInUnhealthyRegion);

                setupTransportClientToThrowCosmosException(
                    transportClientMock,
                    partitionKeyRangeWithIssues,
                    regionalRoutingContextWithIssues,
                    cosmosException);

                TestItem testItem = TestItem.createNewItem();

                Function<OperationInvocationParamsWrapper, ResponseWrapper<?>> dataPlaneOperation = resolveDataPlaneOperation(operationType);

                OperationInvocationParamsWrapper operationInvocationParamsWrapper = new OperationInvocationParamsWrapper();
                operationInvocationParamsWrapper.asyncContainer = asyncContainer;
                operationInvocationParamsWrapper.createdTestItem = testItem;
                operationInvocationParamsWrapper.itemRequestOptions = shouldUseE2ETimeout ? new CosmosItemRequestOptions().setCosmosEndToEndOperationLatencyPolicyConfig(THREE_SEC_E2E_TIMEOUT_POLICY) : new CosmosItemRequestOptions();
                operationInvocationParamsWrapper.patchItemRequestOptions = shouldUseE2ETimeout ? new CosmosPatchItemRequestOptions().setCosmosEndToEndOperationLatencyPolicyConfig(THREE_SEC_E2E_TIMEOUT_POLICY) : new CosmosPatchItemRequestOptions();

                // Phase 1: PPAF disabled -> expect characteristics provided for DISABLED
                ppafEnabledRef.set(Boolean.FALSE);
                globalEndpointManager.refreshLocationAsync(null, true).block();
                ResponseWrapper<?> responseWithPpafDisabled = dataPlaneOperation.apply(operationInvocationParamsWrapper);
                this.validateExpectedResponseCharacteristics.accept(responseWithPpafDisabled, expectedResponseCharacteristicsWhenPpafIsDisabled);

                // Phase 2: PPAF enabled -> expect characteristics provided for ENABLED
                ppafEnabledRef.set(Boolean.TRUE);
                globalEndpointManager.refreshLocationAsync(null, true).block();
                ResponseWrapper<?> responseWithPpafEnabled = dataPlaneOperation.apply(operationInvocationParamsWrapper);
                this.validateExpectedResponseCharacteristics.accept(responseWithPpafEnabled, expectedResponseCharacteristicsWhenPpafIsEnabled);

                // Phase 3: PPAF disabled again -> confirm behavior reverts
                ppafEnabledRef.set(Boolean.FALSE);
                globalEndpointManager.refreshLocationAsync(null, true).block();
                responseWithPpafDisabled = dataPlaneOperation.apply(operationInvocationParamsWrapper);
                this.validateExpectedResponseCharacteristics.accept(responseWithPpafDisabled, expectedResponseCharacteristicsWhenPpafIsDisabled);
            } catch (Exception e) {
                Assertions.fail("The test ran into an exception {}", e);
            } finally {
                safeClose(cosmosAsyncClientValueHolder.v);
            }
        }

        // GATEWAY flow: swap RxGatewayStoreModel HttpClient, inject 503 on primary region and verify phases
        if (connectionMode == ConnectionMode.GATEWAY) {
            HttpClient mockedHttpClient = Mockito.mock(HttpClient.class);
            List<String> preferredRegions = this.accountLevelLocationReadableLocationContext.serviceOrderedReadableRegions;
            Map<String, String> readableRegionNameToEndpoint = this.accountLevelLocationReadableLocationContext.regionNameToEndpoint;
            Utils.ValueHolder<CosmosAsyncClient> cosmosAsyncClientValueHolder = new Utils.ValueHolder<>();

            try {
                CosmosClientBuilder cosmosClientBuilder = getClientBuilder();

                if (operationType.equals(OperationType.Batch) && shouldUseE2ETimeout) {
                    cosmosClientBuilder.endToEndOperationLatencyPolicyConfig(THREE_SEC_E2E_TIMEOUT_POLICY);
                }

                CosmosAsyncClient asyncClient = cosmosClientBuilder.buildAsyncClient();
                cosmosAsyncClientValueHolder.v = asyncClient;

                CosmosAsyncContainer asyncContainer = asyncClient
                    .getDatabase(this.sharedDatabase.getId())
                    .getContainer(this.sharedSinglePartitionContainer.getId());

                // Populate collection and PK range caches to ensure routing is initialized
                asyncContainer.getFeedRanges().block();

                RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) ReflectionUtils.getAsyncDocumentClient(asyncClient);
                RxStoreModel rxStoreModel = ReflectionUtils.getGatewayProxy(rxDocumentClient);

                // Swap GlobalEndpointManager.owner to a delegating wrapper that toggles PPAF flag on DatabaseAccount
                GlobalEndpointManager globalEndpointManager = ReflectionUtils.getGlobalEndpointManager(rxDocumentClient);
                DatabaseAccountManagerInternal originalOwner = ReflectionUtils.getGlobalEndpointManagerOwner(globalEndpointManager);

                AtomicReference<Boolean> ppafEnabledRef = new AtomicReference<>(Boolean.FALSE);
                DatabaseAccountManagerInternal overridingOwner = new DelegatingDatabaseAccountManagerInternal(originalOwner, ppafEnabledRef);
                ReflectionUtils.setGlobalEndpointManagerOwner(globalEndpointManager, overridingOwner);

                DatabaseAccount databaseAccountForResponses = globalEndpointManager.getLatestDatabaseAccount();
                if (databaseAccountForResponses == null) {
                    // Ensure we have an initial snapshot
                    globalEndpointManager.refreshLocationAsync(null, true).block();
                    databaseAccountForResponses = globalEndpointManager.getLatestDatabaseAccount();
                }

                assertThat(preferredRegions).isNotNull();
                assertThat(preferredRegions.size()).isGreaterThanOrEqualTo(1);

                String regionWithIssues = preferredRegions.get(0);
                URI locationEndpointWithIssues = new URI(readableRegionNameToEndpoint.get(regionWithIssues) + "dbs/" + this.sharedDatabase.getId() + "/colls/" + this.sharedSinglePartitionContainer.getId() + "/docs");

                // Redirect gateway calls through our mocked HttpClient
                ReflectionUtils.setGatewayHttpClient(rxStoreModel, mockedHttpClient);

                setupHttpClientToReturnSuccessResponse(mockedHttpClient, operationType, databaseAccountForResponses, successStatusCode);

                CosmosException cosmosException = createCosmosException(
                    errorStatusCodeToMockFromPartitionInUnhealthyRegion,
                    errorSubStatusCodeToMockFromPartitionInUnhealthyRegion);

                setupHttpClientToThrowCosmosException(
                    mockedHttpClient,
                    locationEndpointWithIssues,
                    cosmosException,
                    shouldThrowNetworkError,
                    shouldThrowReadTimeoutExceptionWhenNetworkError,
                    shouldUseE2ETimeout);

                TestItem testItem = TestItem.createNewItem();

                Function<OperationInvocationParamsWrapper, ResponseWrapper<?>> dataPlaneOperation = resolveDataPlaneOperation(operationType);

                OperationInvocationParamsWrapper operationInvocationParamsWrapper = new OperationInvocationParamsWrapper();
                operationInvocationParamsWrapper.asyncContainer = asyncContainer;
                operationInvocationParamsWrapper.createdTestItem = testItem;
                operationInvocationParamsWrapper.itemRequestOptions = shouldUseE2ETimeout ? new CosmosItemRequestOptions().setCosmosEndToEndOperationLatencyPolicyConfig(THREE_SEC_E2E_TIMEOUT_POLICY) : new CosmosItemRequestOptions();
                operationInvocationParamsWrapper.patchItemRequestOptions = shouldUseE2ETimeout ? new CosmosPatchItemRequestOptions().setCosmosEndToEndOperationLatencyPolicyConfig(THREE_SEC_E2E_TIMEOUT_POLICY) : new CosmosPatchItemRequestOptions();

                // Phase 1: PPAF disabled -> expect characteristics provided for DISABLED
                ppafEnabledRef.set(Boolean.FALSE);
                globalEndpointManager.refreshLocationAsync(null, true).block();
                ResponseWrapper<?> responseWithPpafDisabled = dataPlaneOperation.apply(operationInvocationParamsWrapper);
                this.validateExpectedResponseCharacteristics.accept(responseWithPpafDisabled, expectedResponseCharacteristicsWhenPpafIsDisabled);

                // Phase 2: PPAF enabled -> expect characteristics provided for ENABLED
                ppafEnabledRef.set(Boolean.TRUE);
                globalEndpointManager.refreshLocationAsync(null, true).block();
                ResponseWrapper<?> responseWithPpafEnabled = dataPlaneOperation.apply(operationInvocationParamsWrapper);
                this.validateExpectedResponseCharacteristics.accept(responseWithPpafEnabled, expectedResponseCharacteristicsWhenPpafIsEnabled);

                // Phase 3: PPAF disabled again -> confirm behavior reverts
                ppafEnabledRef.set(Boolean.FALSE);
                globalEndpointManager.refreshLocationAsync(null, true).block();
                responseWithPpafDisabled = dataPlaneOperation.apply(operationInvocationParamsWrapper);
                this.validateExpectedResponseCharacteristics.accept(responseWithPpafDisabled, expectedResponseCharacteristicsWhenPpafIsDisabled);
            } catch (Exception e) {
                Assertions.fail("The test ran into an exception {}", e);
            } finally {
                safeClose(cosmosAsyncClientValueHolder.v);
            }
        }
    }

    /**
     * Validates dynamic Per-Partition Automatic Failover (PPAF) hedging behavior for non-write operations
     * (point Read and Query variants).
     *
     * <p>Fault models:</p>
     * <ul>
     *   <li>DIRECT: SERVER_GENERATED_GONE (HTTP 410 / substatus 21005) for a targeted partition key range
     *       in the first preferred region.</li>
     *   <li>GATEWAY: RESPONSE_DELAY injected (via fault injection rules) for the first preferred region
     *       (applied to read item, query plan, and query operations).</li>
     * </ul>
     *
     * <p>QueryFlavor mapping:</p>
     * <ul>
     *   <li>NONE: point read (readItem).</li>
     *   <li>READ_ALL: readAllItems.</li>
     *   <li>READ_MANY: readMany with supplied identities.</li>
     *   <li>QUERY_ITEMS: queryItems (requires query plan; may still contact original region post-stabilization).</li>
     * </ul>
     *
     * <p>Phases asserted:</p>
     * <ol>
     *   <li>Hedging window (multiple consecutive injected faults):
     *       <ul>
     *         <li>DIRECT (410): expect >=1 retry and 2 contacted regions.</li>
     *         <li>GATEWAY (delay): expect 0 retries and 2 contacted regions (hedged).</li>
     *       </ul>
     *   </li>
     *   <li>Post-window stabilization:
     *       <ul>
     *         <li>Routes directly to healthy region (1 contacted region) except QUERY_ITEMS
     *             which may still require original region for query plan (thus 2).</li>
     *         <li>Expect 0 retries.</li>
     *       </ul>
     *   </li>
     * </ol>
     *
     * <p>Behavior is parameterized by the ppafNonWriteDynamicEnablementScenarios data provider:
     * test type description, operationType (Read/Query), queryFlavor, faultKind, expected success
     * status code, and allowed connection modes.</p>
     *
     * <p>Dynamic enablement is achieved by overriding GlobalEndpointManager's owner to
     * inject the PPAF flag into DatabaseAccount snapshots.</p>
     */
    @Test(groups = {"multi-region"}, dataProvider = "ppafNonWriteDynamicEnablementScenarios")
    public void testFailoverBehaviorForNonWriteOperationsWithPpafDynamicEnablement(
        String testType,
        OperationType operationType,
        QueryFlavor queryFlavor,
        FaultKind faultKind,
        int successStatusCode,
        Set<ConnectionMode> allowedConnectionModes) {

        ConnectionPolicy connectionPolicy = COSMOS_CLIENT_BUILDER_ACCESSOR.getConnectionPolicy(getClientBuilder());
        ConnectionMode connectionMode = connectionPolicy.getConnectionMode();

        if (!allowedConnectionModes.contains(connectionMode)) {
            throw new SkipException(String.format("Test with type : %s not eligible for specified connection mode %s.", testType, connectionMode));
        }

        final int consecutiveFaults = 10;

        // ===================== DIRECT MODE PATH =====================
        if (connectionMode == ConnectionMode.DIRECT) {

            // Build expectations (hedging window vs stabilized post-window)
            ExpectedResponseCharacteristics expectedDuringWindow = new ExpectedResponseCharacteristics()
                .setExpectedMinRetryCount(1)                 // At least one retry due to first region failure
                .setShouldFinalResponseHaveSuccess(true)
                .setExpectedRegionsContactedCount(2);        // Hedging to healthy region

            ExpectedResponseCharacteristics expectedAfterWindow = new ExpectedResponseCharacteristics()
                .setExpectedMinRetryCount(0)                 // Stable routing
                .setExpectedMaxRetryCount(0)
                .setShouldFinalResponseHaveSuccess(true)
                // QUERY_ITEMS still requires query plan from original region -> 2 regions contacted
                .setExpectedRegionsContactedCount(queryFlavor.equals(QueryFlavor.QUERY_ITEMS) ? 2 : 1);

            TransportClient transportClientMock = Mockito.mock(TransportClient.class);
            List<String> preferredRegions = this.accountLevelLocationReadableLocationContext.serviceOrderedReadableRegions;
            Map<String, String> readableRegionNameToEndpoint = this.accountLevelLocationReadableLocationContext.regionNameToEndpoint;
            Utils.ValueHolder<CosmosAsyncClient> cosmosAsyncClientValueHolder = new Utils.ValueHolder<>();

            try {
                // Build client and container
                CosmosClientBuilder cosmosClientBuilder = getClientBuilder();
                CosmosAsyncClient asyncClient = cosmosClientBuilder.buildAsyncClient();
                cosmosAsyncClientValueHolder.v = asyncClient;

                CosmosAsyncContainer asyncContainer = asyncClient
                    .getDatabase(this.sharedDatabase.getId())
                    .getContainer(this.sharedSinglePartitionContainer.getId());

                // Reflection plumbing for internal components
                RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) ReflectionUtils.getAsyncDocumentClient(asyncClient);
                GlobalEndpointManager globalEndpointManager = ReflectionUtils.getGlobalEndpointManager(rxDocumentClient);
                Mockito.when(transportClientMock.getGlobalEndpointManager()).thenReturn(globalEndpointManager);

                // Enable dynamic PPAF via delegating owner
                DatabaseAccountManagerInternal originalOwner = ReflectionUtils.getGlobalEndpointManagerOwner(globalEndpointManager);
                AtomicReference<Boolean> ppafEnabledRef = new AtomicReference<>(Boolean.TRUE);
                DatabaseAccountManagerInternal overridingOwner =
                    new DelegatingDatabaseAccountManagerInternal(originalOwner, ppafEnabledRef);
                ReflectionUtils.setGlobalEndpointManagerOwner(globalEndpointManager, overridingOwner);

                // Internal store clients
                StoreClient storeClient = ReflectionUtils.getStoreClient(rxDocumentClient);
                ReplicatedResourceClient replicatedResourceClient = ReflectionUtils.getReplicatedResourceClient(storeClient);
                ConsistencyReader consistencyReader = ReflectionUtils.getConsistencyReader(replicatedResourceClient);
                StoreReader storeReader = ReflectionUtils.getStoreReader(consistencyReader);
                ConsistencyWriter consistencyWriter = ReflectionUtils.getConsistencyWriter(replicatedResourceClient);

                // Identify a PK range + first preferred region to fault
                Utils.ValueHolder<List<PartitionKeyRange>> partitionKeyRangesForContainer =
                    getPartitionKeyRangesForContainer(asyncContainer, rxDocumentClient).block();
                assertThat(partitionKeyRangesForContainer).isNotNull();
                assertThat(partitionKeyRangesForContainer.v).isNotNull();
                assertThat(partitionKeyRangesForContainer.v.size()).isGreaterThanOrEqualTo(1);
                PartitionKeyRange partitionKeyRangeWithIssues = partitionKeyRangesForContainer.v.get(0);

                assertThat(preferredRegions).isNotNull();
                assertThat(preferredRegions.size()).isGreaterThanOrEqualTo(1);
                String regionWithIssues = preferredRegions.get(0);
                RegionalRoutingContext regionalRoutingContextWithIssues =
                    new RegionalRoutingContext(new URI(readableRegionNameToEndpoint.get(regionWithIssues)));

                // Wire mock transport client into reader + writer paths
                ReflectionUtils.setTransportClient(storeReader, transportClientMock);
                ReflectionUtils.setTransportClient(consistencyWriter, transportClientMock);

                // Success response when routed to healthy region
                setupTransportClientToReturnSuccessResponse(
                    transportClientMock,
                    constructStoreResponse(operationType, successStatusCode));

                if (faultKind != FaultKind.SERVER_GENERATED_GONE) {
                    throw new SkipException("DIRECT path only supports SERVER_GENERATED_GONE for this test.");
                }

                // Inject 410/21005 for unhealthy region
                CosmosException cosmosException = createCosmosException(
                    HttpConstants.StatusCodes.GONE,
                    HttpConstants.SubStatusCodes.SERVER_GENERATED_410);

                setupTransportClientToThrowCosmosException(
                    transportClientMock,
                    partitionKeyRangeWithIssues,
                    regionalRoutingContextWithIssues,
                    cosmosException);

                // Prepare operation invocation
                TestItem testItem = TestItem.createNewItem();
                Function<OperationInvocationParamsWrapper, ResponseWrapper<?>> dataPlaneOperation =
                    resolveDataPlaneOperation(operationType);

                OperationInvocationParamsWrapper params = new OperationInvocationParamsWrapper();
                params.asyncContainer = asyncContainer;
                params.createdTestItem = testItem;
                applyQueryFlavor(params, queryFlavor, testItem);

                // Force initial refresh so DatabaseAccount is loaded with PPAF flag
                DatabaseAccount dbAccountSnapshot = globalEndpointManager.getLatestDatabaseAccount();
                if (dbAccountSnapshot == null) {
                    globalEndpointManager.refreshLocationAsync(null, true).block();
                } else {
                    globalEndpointManager.refreshLocationAsync(dbAccountSnapshot, true).block();
                }

                // Execute hedging + stabilization phases
                runHedgingPhasesForNonWrite(
                    consecutiveFaults,
                    dataPlaneOperation,
                    params,
                    expectedDuringWindow,
                    expectedAfterWindow);

            } catch (Exception e) {
                Assertions.fail("The test ran into an exception {}", e);
            } finally {
                safeClose(cosmosAsyncClientValueHolder.v);
            }
        }

        // ===================== GATEWAY MODE PATH =====================
        if (connectionMode == ConnectionMode.GATEWAY) {

            ExpectedResponseCharacteristics expectedDuringWindow = new ExpectedResponseCharacteristics()
                .setExpectedMinRetryCount(0)     // Delay fault causes hedging without retries
                .setExpectedMaxRetryCount(0)
                .setShouldFinalResponseHaveSuccess(true)
                .setExpectedRegionsContactedCount(2);

            ExpectedResponseCharacteristics expectedAfterWindow = new ExpectedResponseCharacteristics()
                .setExpectedMinRetryCount(0)
                .setExpectedMaxRetryCount(0)
                .setShouldFinalResponseHaveSuccess(true)
                .setExpectedRegionsContactedCount(queryFlavor.equals(QueryFlavor.QUERY_ITEMS) ? 2 : 1);

            List<String> preferredRegions = this.accountLevelLocationReadableLocationContext.serviceOrderedReadableRegions;
            Utils.ValueHolder<CosmosAsyncClient> cosmosAsyncClientValueHolder = new Utils.ValueHolder<>();

            try {
                // Build client + container
                CosmosClientBuilder cosmosClientBuilder = getClientBuilder();
                CosmosAsyncClient asyncClient = cosmosClientBuilder.buildAsyncClient();
                cosmosAsyncClientValueHolder.v = asyncClient;

                CosmosAsyncContainer asyncContainer = asyncClient
                    .getDatabase(this.sharedDatabase.getId())
                    .getContainer(this.sharedSinglePartitionContainer.getId());
                // Warm caches
                asyncContainer.getFeedRanges().block();

                RxDocumentClientImpl rxDocumentClient =
                    (RxDocumentClientImpl) ReflectionUtils.getAsyncDocumentClient(asyncClient);

                GlobalEndpointManager globalEndpointManager =
                    ReflectionUtils.getGlobalEndpointManager(rxDocumentClient);

                // Enable PPAF dynamically
                DatabaseAccountManagerInternal originalOwner =
                    ReflectionUtils.getGlobalEndpointManagerOwner(globalEndpointManager);
                AtomicReference<Boolean> ppafEnabledRef = new AtomicReference<>(Boolean.TRUE);
                DatabaseAccountManagerInternal overridingOwner =
                    new DelegatingDatabaseAccountManagerInternal(originalOwner, ppafEnabledRef);
                ReflectionUtils.setGlobalEndpointManagerOwner(globalEndpointManager, overridingOwner);

                assertThat(preferredRegions).isNotNull();
                assertThat(preferredRegions.size()).isGreaterThanOrEqualTo(1);
                String regionWithIssues = preferredRegions.get(0);

                // Refresh DB account snapshot
                DatabaseAccount dbAccountSnapshot = globalEndpointManager.getLatestDatabaseAccount();
                if (dbAccountSnapshot == null) {
                    globalEndpointManager.refreshLocationAsync(null, true).block();
                } else {
                    globalEndpointManager.refreshLocationAsync(dbAccountSnapshot, true).block();
                }

                if (faultKind != FaultKind.RESPONSE_DELAY) {
                    throw new SkipException("GATEWAY path only supports RESPONSE_DELAY for this test.");
                }

                // Inject RESPONSE_DELAY faults using FIR (read item + query + query plan)
                FeedRange fullRange = FeedRange.forFullRange();

                FaultInjectionServerErrorResult responseDelayError = FaultInjectionResultBuilders
                    .getResultBuilder(FaultInjectionServerErrorType.RESPONSE_DELAY)
                    .delay(Duration.ofSeconds(10))          // long enough to trigger hedging
                    .suppressServiceRequests(false)
                    .build();

                FaultInjectionCondition conditionForReadItem = new FaultInjectionConditionBuilder()
                    .connectionType(FaultInjectionConnectionType.GATEWAY)
                    .endpoints(new FaultInjectionEndpointBuilder(fullRange).build())
                    .operationType(FaultInjectionOperationType.READ_ITEM)
                    .region(regionWithIssues)
                    .build();

                FaultInjectionCondition conditionForQueryPlan = new FaultInjectionConditionBuilder()
                    .connectionType(FaultInjectionConnectionType.GATEWAY)
                    .endpoints(new FaultInjectionEndpointBuilder(fullRange).build())
                    .operationType(FaultInjectionOperationType.METADATA_REQUEST_QUERY_PLAN)
                    .region(regionWithIssues)
                    .build();

                FaultInjectionCondition conditionForQuery = new FaultInjectionConditionBuilder()
                    .connectionType(FaultInjectionConnectionType.GATEWAY)
                    .endpoints(new FaultInjectionEndpointBuilder(fullRange).build())
                    .operationType(FaultInjectionOperationType.QUERY_ITEM)
                    .region(regionWithIssues)
                    .build();

                String ruleId = String.format("response-delay-%s", UUID.randomUUID());

                FaultInjectionRule queryPlanResponseDelayFIRule = new FaultInjectionRuleBuilder(ruleId + "-qp")
                    .condition(conditionForQueryPlan)
                    .result(responseDelayError)
                    .build();

                FaultInjectionRule queryResponseDelayFIRule = new FaultInjectionRuleBuilder(ruleId + "-q")
                    .condition(conditionForQuery)
                    .result(responseDelayError)
                    .build();

                FaultInjectionRule readItemResponseDelayFIRule = new FaultInjectionRuleBuilder(ruleId + "-r")
                    .condition(conditionForReadItem)
                    .result(responseDelayError)
                    .build();

                CosmosFaultInjectionHelper
                    .configureFaultInjectionRules(
                        asyncContainer,
                        Arrays.asList(queryPlanResponseDelayFIRule, queryResponseDelayFIRule, readItemResponseDelayFIRule))
                    .block();

                // Seed item for read/readMany scenarios
                TestItem testItem = TestItem.createNewItem();
                asyncContainer.createItem(testItem).block();

                // Prepare params + operation
                Function<OperationInvocationParamsWrapper, ResponseWrapper<?>> dataPlaneOperation =
                    resolveDataPlaneOperation(operationType);
                OperationInvocationParamsWrapper params = new OperationInvocationParamsWrapper();
                params.asyncContainer = asyncContainer;
                params.createdTestItem = testItem;
                applyQueryFlavor(params, queryFlavor, testItem);

                // Execute hedging + stabilization phases
                runHedgingPhasesForNonWrite(
                    consecutiveFaults,
                    dataPlaneOperation,
                    params,
                    expectedDuringWindow,
                    expectedAfterWindow);

            } catch (Exception e) {
                Assertions.fail("The test ran into an exception {}", e);
            } finally {
                safeClose(cosmosAsyncClientValueHolder.v);
            }
        }
    }

    /**
     * Helper: Executes the hedging window (multiple consecutive fault attempts) followed by a single post-window verification.
     */
    private void runHedgingPhasesForNonWrite(
        int consecutiveFaults,
        Function<OperationInvocationParamsWrapper, ResponseWrapper<?>> dataPlaneOperation,
        OperationInvocationParamsWrapper params,
        ExpectedResponseCharacteristics expectedDuringWindow,
        ExpectedResponseCharacteristics expectedAfterWindow) {

        // Hedging window iterations
        for (int i = 0; i < consecutiveFaults; i++) {
            ResponseWrapper<?> response = dataPlaneOperation.apply(params);
            this.validateExpectedResponseCharacteristics.accept(response, expectedDuringWindow);
        }

        // Stabilized post-window request
        ResponseWrapper<?> postWindow = dataPlaneOperation.apply(params);
        this.validateExpectedResponseCharacteristics.accept(postWindow, expectedAfterWindow);
    }

    private static class DelegatingDatabaseAccountManagerInternal implements DatabaseAccountManagerInternal {
        private final DatabaseAccountManagerInternal delegate;
        private final AtomicReference<Boolean> ppafEnabledRef;

        DelegatingDatabaseAccountManagerInternal(DatabaseAccountManagerInternal delegate, AtomicReference<Boolean> ppafEnabledRef) {
            this.delegate = delegate;
            this.ppafEnabledRef = ppafEnabledRef;
        }

        @Override
        public Flux<DatabaseAccount> getDatabaseAccountFromEndpoint(URI endpoint) {
            return delegate.getDatabaseAccountFromEndpoint(endpoint)
                .map(dbAccount -> {
                    Boolean enabled = ppafEnabledRef.get();
                    dbAccount.setIsPerPartitionFailoverBehaviorEnabled(enabled);
                    return dbAccount;
                });
        }

        @Override
        public ConnectionPolicy getConnectionPolicy() {
            return delegate.getConnectionPolicy();
        }

        @Override
        public URI getServiceEndpoint() {
            return delegate.getServiceEndpoint();
        }
    }

    private void setupTransportClientToThrowCosmosException(
        TransportClient transportClientMock,
        PartitionKeyRange partitionKeyRange,
        RegionalRoutingContext regionalRoutingContextToRoute,
        CosmosException cosmosException) {

        Mockito.when(
                transportClientMock.invokeResourceOperationAsync(
                    Mockito.any(),
                    Mockito.argThat(argument ->
                        argument.requestContext.resolvedPartitionKeyRange
                            .getId()
                            .equals(partitionKeyRange.getId()) &&
                            argument.requestContext.regionalRoutingContextToRoute.equals(regionalRoutingContextToRoute))))
            .thenReturn(Mono.error(cosmosException));
    }

    private void setupHttpClientToThrowCosmosException(
        HttpClient httpClientMock,
        URI locationEndpointToRoute,
        CosmosException cosmosException,
        boolean shouldThrowNetworkError,
        boolean shouldThrowReadTimeoutExceptionWhenNetworkError,
        boolean shouldForceE2ETimeout) {

        if (shouldForceE2ETimeout) {
            Mockito.when(
                    httpClientMock.send(
                        Mockito.argThat(argument -> {
                            URI uri = argument.uri();
                            return uri.toString().contains(locationEndpointToRoute.toString());
                        }), Mockito.any(Duration.class)))
                .thenReturn(Mono.delay(Duration.ofSeconds(10)).flatMap(aLong -> Mono.error(cosmosException)));

            return;
        }

        if (shouldThrowNetworkError) {
            if (shouldThrowReadTimeoutExceptionWhenNetworkError) {
                Mockito.when(
                        httpClientMock.send(
                            Mockito.argThat(argument -> {
                                URI uri = argument.uri();
                                return uri.toString().contains(locationEndpointToRoute.toString());
                            }), Mockito.any(Duration.class)))
                    .thenReturn(Mono.error(new ReadTimeoutException()));
            } else {
                Mockito.when(
                        httpClientMock.send(
                            Mockito.argThat(argument -> {
                                URI uri = argument.uri();
                                return uri.toString().contains(locationEndpointToRoute.toString());
                            }), Mockito.any(Duration.class)))
                    .thenReturn(Mono.error(new SocketTimeoutException()));
            }

            return;
        }

        // simulates regional failover with error being bubbled up by RxGatewayStoreModel which uses the mocked HttpClient
        Mockito.when(
                httpClientMock.send(
                    Mockito.argThat(argument -> {
                        URI uri = argument.uri();
                        return uri.toString().contains(locationEndpointToRoute.toString());
                    }), Mockito.any(Duration.class)))
            .thenReturn(Mono.error(cosmosException));
    }

    private void setupTransportClientToReturnSuccessResponse(
        TransportClient transportClientMock,
        StoreResponse storeResponse) {

        Mockito.when(transportClientMock.invokeResourceOperationAsync(Mockito.any(), Mockito.any())).thenReturn(Mono.just(storeResponse));
    }

    private void setupHttpClientToReturnSuccessResponse(HttpClient httpClientMock, OperationType operationType, DatabaseAccount databaseAccount, int statusCode) {

        Mockito
            .when(httpClientMock.send(Mockito.argThat(argument -> {

                if (argument == null) {
                    return false;
                }

            URI uri = argument.uri();
            String uriStr = uri.toString();

            // basically a DatabaseAccount call
            return !uriStr.contains("docs") &&
                !uriStr.contains("dbs") &&
                !uriStr.contains("colls") &&
                !uri.toString().contains("pkranges");
        }), Mockito.any(Duration.class)))
            .thenReturn(Mono.just(createResponse(statusCode, operationType, ResourceType.DatabaseAccount, databaseAccount, getTestPojoObject())));

        Mockito
            .when(httpClientMock.send(Mockito.argThat(argument -> {

                if (argument == null) {
                    return false;
                }

                URI uri = argument.uri();
                String uriStr = uri.toString();

                // basically a Document call
                return uriStr.contains("docs");
            }), Mockito.any(Duration.class)))
            .thenReturn(Mono.just(createResponse(statusCode, operationType, ResourceType.Document, databaseAccount, getTestPojoObject())));
    }

    private Mono<Utils.ValueHolder<List<PartitionKeyRange>>> getPartitionKeyRangesForContainer(
        CosmosAsyncContainer cosmosAsyncContainer, RxDocumentClientImpl rxDocumentClient) {
        return Mono.just(cosmosAsyncContainer)
            .flatMap(CosmosAsyncContainer::read)
            .flatMap(containerResponse -> rxDocumentClient
                .getPartitionKeyRangeCache()
                .tryGetOverlappingRangesAsync(
                    null,
                    containerResponse.getProperties().getResourceId(),
                    PartitionKeyInternalHelper.FullRange,
                    false,
                    null));
    }

    private AccountLevelLocationContext getAccountLevelLocationContext(DatabaseAccount databaseAccount, boolean writeOnly) {
        Iterator<DatabaseAccountLocation> locationIterator =
            writeOnly ? databaseAccount.getWritableLocations().iterator() : databaseAccount.getReadableLocations().iterator();

        List<String> serviceOrderedReadableRegions = new ArrayList<>();
        List<String> serviceOrderedWriteableRegions = new ArrayList<>();
        Map<String, String> regionMap = new ConcurrentHashMap<>();

        while (locationIterator.hasNext()) {
            DatabaseAccountLocation accountLocation = locationIterator.next();
            regionMap.put(accountLocation.getName(), accountLocation.getEndpoint());

            if (writeOnly) {
                serviceOrderedWriteableRegions.add(accountLocation.getName());
            } else {
                serviceOrderedReadableRegions.add(accountLocation.getName());
            }
        }

        return new AccountLevelLocationContext(
            serviceOrderedReadableRegions,
            serviceOrderedWriteableRegions,
            regionMap);
    }

    private StoreResponse constructStoreResponse(OperationType operationType, int statusCode) throws JsonProcessingException {

        StoreResponseBuilder storeResponseBuilder = StoreResponseBuilder.create()
            .withContent(OBJECT_MAPPER.writeValueAsString(getTestPojoObject()))
            .withStatus(statusCode);

        if (operationType == OperationType.ReadFeed) {
            return storeResponseBuilder
                .withHeader(HttpConstants.HttpHeaders.CONTINUATION, "1")
                .withHeader(HttpConstants.HttpHeaders.E_TAG, "1")
                .build();
        } else if (operationType == OperationType.Batch) {

            FakeBatchResponse fakeBatchResponse = new FakeBatchResponse();

            fakeBatchResponse
                .seteTag("1")
                .setStatusCode(HttpConstants.StatusCodes.OK)
                .setSubStatusCode(HttpConstants.SubStatusCodes.UNKNOWN)
                .setRequestCharge(1.0d)
                .setResourceBody(getTestPojoObject())
                .setRetryAfterMilliseconds("1");

            return storeResponseBuilder
                .withContent(OBJECT_MAPPER.writeValueAsString(Arrays.asList(fakeBatchResponse)))
                .build();
        } else {
            return storeResponseBuilder.build();
        }
    }


    private static class AccountLevelLocationContext {
    private final List<String> serviceOrderedReadableRegions;
    @SuppressWarnings("unused")
    private final List<String> serviceOrderedWriteableRegions;
    private final Map<String, String> regionNameToEndpoint;

        public AccountLevelLocationContext(
            List<String> serviceOrderedReadableRegions,
            List<String> serviceOrderedWriteableRegions,
            Map<String, String> regionNameToEndpoint) {

            this.serviceOrderedReadableRegions = serviceOrderedReadableRegions;
            this.serviceOrderedWriteableRegions = serviceOrderedWriteableRegions;
            this.regionNameToEndpoint = regionNameToEndpoint;
        }
    }

    private TestPojo getTestPojoObject() {
        TestPojo testPojo = new TestPojo();
        String uuid = UUID.randomUUID().toString();
        testPojo.setId(uuid);
        testPojo.setMypk(uuid);
        return testPojo;
    }

    private CosmosException createCosmosException(int statusCode, int subStatusCode) {

        switch (statusCode) {
            case HttpConstants.StatusCodes.GONE:
                return new GoneException("", subStatusCode);
            case HttpConstants.StatusCodes.SERVICE_UNAVAILABLE:
                return new ServiceUnavailableException(null, null, null, null, subStatusCode);
            case HttpConstants.StatusCodes.FORBIDDEN:
                ForbiddenException forbiddenException = new ForbiddenException(null, -1, null, new HashMap<>());
                BridgeInternal.setSubStatusCode(forbiddenException, subStatusCode);
                return forbiddenException;
            case HttpConstants.StatusCodes.REQUEST_TIMEOUT:
                return new RequestTimeoutException("", null, subStatusCode);
            default:
                throw new UnsupportedOperationException(String.format("Uncovered erroneous status code %d", statusCode));
        }
    }

    private Function<OperationInvocationParamsWrapper, ResponseWrapper<?>> resolveDataPlaneOperation(OperationType operationType) {

        switch (operationType) {
            case Read:
                return (paramsWrapper) -> {

                    CosmosAsyncContainer asyncContainer = paramsWrapper.asyncContainer;
                    TestItem createdTestObject = paramsWrapper.createdTestItem;
                    CosmosItemRequestOptions itemRequestOptions = paramsWrapper.itemRequestOptions;

                    try {

                        CosmosItemResponse<TestObject> readItemResponse = asyncContainer.readItem(
                                createdTestObject.getId(),
                                new PartitionKey(createdTestObject.getMypk()),
                                itemRequestOptions,
                                TestObject.class)
                            .block();

                        return new ResponseWrapper<>(readItemResponse);
                    } catch (Exception ex) {

                        if (ex instanceof CosmosException) {
                            CosmosException cosmosException = Utils.as(ex, CosmosException.class);
                            return new ResponseWrapper<>(cosmosException);
                        }

                        throw ex;
                    }
                };
            case Upsert:
                return (paramsWrapper) -> {

                    CosmosAsyncContainer asyncContainer = paramsWrapper.asyncContainer;
                    TestItem createdTestObject = paramsWrapper.createdTestItem;
                    CosmosItemRequestOptions itemRequestOptions = paramsWrapper.itemRequestOptions;

                    try {

                        CosmosItemResponse<TestItem> upsertItemResponse = asyncContainer.upsertItem(
                                createdTestObject,
                                new PartitionKey(createdTestObject.getMypk()),
                                itemRequestOptions)
                            .block();

                        return new ResponseWrapper<>(upsertItemResponse);
                    } catch (Exception ex) {

                        if (ex instanceof CosmosException) {
                            CosmosException cosmosException = Utils.as(ex, CosmosException.class);
                            return new ResponseWrapper<>(cosmosException);
                        }

                        throw ex;
                    }
                };
            case Create:
                return (paramsWrapper) -> {

                    CosmosAsyncContainer asyncContainer = paramsWrapper.asyncContainer;
                    TestItem createdTestObject = TestItem.createNewItem();
                    CosmosItemRequestOptions itemRequestOptions = paramsWrapper.itemRequestOptions;

                    try {

                        CosmosItemResponse<TestItem> createItemResponse = asyncContainer.createItem(
                                createdTestObject,
                                new PartitionKey(createdTestObject.getMypk()),
                                itemRequestOptions)
                            .block();

                        return new ResponseWrapper<>(createItemResponse);
                    } catch (Exception ex) {

                        if (ex instanceof CosmosException) {
                            CosmosException cosmosException = Utils.as(ex, CosmosException.class);
                            return new ResponseWrapper<>(cosmosException);
                        }

                        throw ex;
                    }
                };
            case Delete:
                return (paramsWrapper) -> {

                    CosmosAsyncContainer asyncContainer = paramsWrapper.asyncContainer;
                    TestItem createdTestObject = paramsWrapper.createdTestItem;
                    CosmosItemRequestOptions itemRequestOptions = paramsWrapper.itemRequestOptions;

                    try {

                        CosmosItemResponse<Object> deleteItemResponse = asyncContainer.deleteItem(
                                createdTestObject.getId(),
                                new PartitionKey(createdTestObject.getId()),
                                itemRequestOptions)
                            .block();

                        return new ResponseWrapper<>(deleteItemResponse);
                    } catch (Exception ex) {

                        if (ex instanceof CosmosException) {
                            CosmosException cosmosException = Utils.as(ex, CosmosException.class);
                            return new ResponseWrapper<>(cosmosException);
                        }

                        throw ex;
                    }
                };
            case Patch:
                return (paramsWrapper) -> {

                    CosmosAsyncContainer asyncContainer = paramsWrapper.asyncContainer;
                    TestItem createdTestObject = paramsWrapper.createdTestItem;
                    CosmosPatchItemRequestOptions patchItemRequestOptions = (CosmosPatchItemRequestOptions) paramsWrapper.patchItemRequestOptions;

                    CosmosPatchOperations patchOperations = CosmosPatchOperations.create().add("/number", 555);

                    try {

                        CosmosItemResponse<TestItem> patchItemResponse = asyncContainer.patchItem(
                                createdTestObject.getId(),
                                new PartitionKey(createdTestObject.getMypk()),
                                patchOperations,
                                patchItemRequestOptions,
                                TestItem.class)
                            .block();

                        return new ResponseWrapper<>(patchItemResponse);
                    } catch (Exception ex) {

                        if (ex instanceof CosmosException) {
                            CosmosException cosmosException = Utils.as(ex, CosmosException.class);
                            return new ResponseWrapper<>(cosmosException);
                        }

                        throw ex;
                    }
                };
            case Query:
                return (paramsWrapper) -> {

                    CosmosAsyncContainer asyncContainer = paramsWrapper.asyncContainer;
                    CosmosQueryRequestOptions queryRequestOptions = paramsWrapper.queryRequestOptions == null ? new CosmosQueryRequestOptions() : paramsWrapper.queryRequestOptions;
                    queryRequestOptions = paramsWrapper.feedRangeForQuery == null ? queryRequestOptions.setFeedRange(FeedRange.forFullRange()) : queryRequestOptions.setFeedRange(paramsWrapper.feedRangeForQuery);
                    String sql = paramsWrapper.querySql != null ? paramsWrapper.querySql : "SELECT * FROM c";

                    try {
                        // If applyQueryFlavor requested readAllItems or readMany, use those operations instead of query
                        if (paramsWrapper.readAllPartitionKey != null) {
                            FeedResponse<TestObject> readAllResponse = asyncContainer
                                .readAllItems(paramsWrapper.readAllPartitionKey, TestObject.class)
                                .byPage()
                                .blockLast();
                            return new ResponseWrapper<>(readAllResponse);
                        }

                        if (paramsWrapper.readManyIdentities != null && !paramsWrapper.readManyIdentities.isEmpty()) {
                            FeedResponse<TestObject> readManyResponse = asyncContainer
                                .readMany(paramsWrapper.readManyIdentities, TestObject.class)
                                .block();
                            return new ResponseWrapper<>(readManyResponse);
                        }

                        // Fallback: regular queryItems
                        FeedResponse<TestObject> queryItemResponse = asyncContainer
                            .queryItems(sql, queryRequestOptions, TestObject.class)
                            .byPage()
                            .blockLast();

                        return new ResponseWrapper<>(queryItemResponse);
                    } catch (Exception ex) {

                        if (ex instanceof CosmosException) {
                            CosmosException cosmosException = Utils.as(ex, CosmosException.class);
                            return new ResponseWrapper<>(cosmosException);
                        }

                        throw ex;
                    }
                };
            case Replace:
                return (paramsWrapper) -> {

                    CosmosAsyncContainer asyncContainer = paramsWrapper.asyncContainer;
                    TestItem createdTestObject = paramsWrapper.createdTestItem;
                    CosmosItemRequestOptions itemRequestOptions = paramsWrapper.itemRequestOptions;

                    try {

                        CosmosItemResponse<TestItem> deleteItemResponse = asyncContainer.replaceItem(
                                createdTestObject,
                                createdTestObject.getId(),
                                new PartitionKey(createdTestObject.getId()),
                                itemRequestOptions)
                            .block();

                        return new ResponseWrapper<>(deleteItemResponse);
                    } catch (Exception ex) {

                        if (ex instanceof CosmosException) {
                            CosmosException cosmosException = Utils.as(ex, CosmosException.class);
                            return new ResponseWrapper<>(cosmosException);
                        }

                        throw ex;
                    }
                };
            case Batch:
                return (paramsWrapper) -> {

                    TestItem testObject = TestItem.createNewItem();
                    CosmosBatch batch = CosmosBatch.createCosmosBatch(new PartitionKey(testObject.getId()));
                    CosmosAsyncContainer asyncContainer = paramsWrapper.asyncContainer;

                    batch.createItemOperation(testObject);

                    try {
                        CosmosBatchResponse batchResponse = asyncContainer.executeCosmosBatch(batch).block();
                        return new ResponseWrapper<>(batchResponse);
                    } catch (Exception ex) {
                        if (ex instanceof CosmosException) {
                            CosmosException cosmosException = Utils.as(ex, CosmosException.class);
                            return new ResponseWrapper<>(cosmosException);
                        }

                        throw ex;
                    }
                };
            case ReadFeed:
                return (paramsWrapper) -> {

                    CosmosAsyncContainer asyncContainer = paramsWrapper.asyncContainer;

                    try {

                        FeedResponse<TestItem> feedResponseFromChangeFeed = asyncContainer.queryChangeFeed(
                                CosmosChangeFeedRequestOptions.createForProcessingFromBeginning(paramsWrapper.feedRangeToDrainForChangeFeed == null ? FeedRange.forFullRange() : paramsWrapper.feedRangeToDrainForChangeFeed),
                                TestItem.class)
                            .byPage()
                            .blockLast();

                        return new ResponseWrapper<>(feedResponseFromChangeFeed);
                    } catch (Exception ex) {

                        if (ex instanceof CosmosException) {
                            CosmosException cosmosException = Utils.as(ex, CosmosException.class);
                            return new ResponseWrapper<>(cosmosException);
                        }

                        throw ex;
                    }
                };
            default:
                throw new UnsupportedOperationException(String.format("Operation of type : %s is not supported", operationType));
        }
    }

    private static class ResponseWrapper<T> {

        private final CosmosItemResponse<T> cosmosItemResponse;
        private final CosmosException cosmosException;
        private final FeedResponse<T> feedResponse;
        private final CosmosBatchResponse batchResponse;

        ResponseWrapper(FeedResponse<T> feedResponse) {
            this.feedResponse = feedResponse;
            this.cosmosException = null;
            this.cosmosItemResponse = null;
            this.batchResponse = null;
        }

        ResponseWrapper(CosmosItemResponse<T> cosmosItemResponse) {
            this.cosmosItemResponse = cosmosItemResponse;
            this.cosmosException = null;
            this.feedResponse = null;
            this.batchResponse = null;
        }

        ResponseWrapper(CosmosException cosmosException) {
            this.cosmosException = cosmosException;
            this.cosmosItemResponse = null;
            this.feedResponse = null;
            this.batchResponse = null;
        }

        ResponseWrapper(CosmosBatchResponse batchResponse) {
            this.cosmosException = null;
            this.cosmosItemResponse = null;
            this.feedResponse = null;
            this.batchResponse = batchResponse;
        }
    }

    private static class OperationInvocationParamsWrapper {
        public CosmosAsyncContainer asyncContainer;
        public TestItem createdTestItem;
        public CosmosItemRequestOptions itemRequestOptions;
        public CosmosQueryRequestOptions queryRequestOptions;
        public CosmosItemRequestOptions patchItemRequestOptions;
        public FeedRange feedRangeToDrainForChangeFeed;
        public FeedRange feedRangeForQuery;
        public String querySql;
        // For QueryFlavor.READ_ALL
        public PartitionKey readAllPartitionKey;
        // For QueryFlavor.READ_MANY
        public List<CosmosItemIdentity> readManyIdentities;
    }

    private static class ExpectedResponseCharacteristics {

        int expectedRegionsContactedCount = 0;

        int expectedMaxRetryCount = Integer.MAX_VALUE;

        int expectedMinRetryCount = 0;

        boolean shouldFinalResponseHaveSuccess = false;

        public ExpectedResponseCharacteristics setExpectedRegionsContactedCount(int expectedRegionsContactedCount) {
            this.expectedRegionsContactedCount = expectedRegionsContactedCount;
            return this;
        }

        public ExpectedResponseCharacteristics setExpectedMaxRetryCount(int expectedMaxRetryCount) {
            this.expectedMaxRetryCount = expectedMaxRetryCount;
            return this;
        }

        public ExpectedResponseCharacteristics setExpectedMinRetryCount(int expectedMinRetryCount) {
            this.expectedMinRetryCount = expectedMinRetryCount;
            return this;
        }

        public ExpectedResponseCharacteristics setShouldFinalResponseHaveSuccess(boolean shouldFinalResponseHaveSuccess) {
            this.shouldFinalResponseHaveSuccess = shouldFinalResponseHaveSuccess;
            return this;
        }
    }

    private static class FakeBatchResponse {

        private int statusCode;

        private int subStatusCode;

        private double requestCharge;

        private String eTag;

        private Object resourceBody;

        private String retryAfterMilliseconds;

    @SuppressWarnings("unused")
    public int getStatusCode() {
            return statusCode;
        }

        public FakeBatchResponse setStatusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

    @SuppressWarnings("unused")
    public int getSubStatusCode() {
            return subStatusCode;
        }

        public FakeBatchResponse setSubStatusCode(int subStatusCode) {
            this.subStatusCode = subStatusCode;
            return this;
        }

    @SuppressWarnings("unused")
    public double getRequestCharge() {
            return requestCharge;
        }

        public FakeBatchResponse setRequestCharge(double requestCharge) {
            this.requestCharge = requestCharge;
            return this;
        }

    @SuppressWarnings("unused")
    public String geteTag() {
            return eTag;
        }

        public FakeBatchResponse seteTag(String eTag) {
            this.eTag = eTag;
            return this;
        }

    @SuppressWarnings("unused")
    public Object getResourceBody() {
            return resourceBody;
        }

        public FakeBatchResponse setResourceBody(Object resourceBody) {
            this.resourceBody = resourceBody;
            return this;
        }

    @SuppressWarnings("unused")
    public String getRetryAfterMilliseconds() {
            return retryAfterMilliseconds;
        }

        public FakeBatchResponse setRetryAfterMilliseconds(String retryAfterMilliseconds) {
            this.retryAfterMilliseconds = retryAfterMilliseconds;
            return this;
        }
    }

    private enum FaultKind {
        SERVER_GENERATED_GONE,
        RESPONSE_DELAY
    }

    private enum QueryFlavor {
        NONE,        // Not a query
        READ_ALL,    // SELECT * FROM c
        READ_MANY,   // Simulate with IN clause
        QUERY_ITEMS  // Arbitrary filter
    }

    private void applyQueryFlavor(OperationInvocationParamsWrapper params, QueryFlavor flavor, TestItem seed) {
        if (flavor == QueryFlavor.NONE) {
            // Do not set CosmosQueryRequestOptions explicitly
            params.querySql = null;
            params.readAllPartitionKey = null;
            params.readManyIdentities = null;
            return;
        }

        // Do not set CosmosQueryRequestOptions explicitly; default behavior will be used

        switch (flavor) {
            case READ_ALL:
                // Map to readAllItems on the container using the seed's partition key
                String pkReadAll = seed != null ? seed.getMypk() : UUID.randomUUID().toString();
                params.readAllPartitionKey = new PartitionKey(pkReadAll);
                params.querySql = null;
                params.readManyIdentities = null;
                break;
            case READ_MANY:
                // Map to readMany with one or more identities using the seed
                String id = seed != null ? seed.getId() : UUID.randomUUID().toString();
                String pkReadMany = seed != null ? seed.getMypk() : UUID.randomUUID().toString();
                PartitionKey pkValue = new PartitionKey(pkReadMany);
                List<CosmosItemIdentity> identities = new ArrayList<>();
                identities.add(new CosmosItemIdentity(pkValue, id));
                params.readManyIdentities = identities;
                params.readAllPartitionKey = null;
                params.querySql = null;
                break;
            case QUERY_ITEMS:
                params.querySql = "SELECT * FROM c WHERE IS_DEFINED(c.mypk)";
                params.readAllPartitionKey = null;
                params.readManyIdentities = null;
                break;
            default:
                params.querySql = "SELECT * FROM c";
                params.readAllPartitionKey = null;
                params.readManyIdentities = null;
        }
    }

    private HttpResponse createResponse(int statusCode, OperationType operationType, ResourceType resourceType, DatabaseAccount databaseAccount, TestPojo testPojo) {
        HttpResponse httpResponse = new HttpResponse() {
            @Override
            public int statusCode() {
                return statusCode;
            }

            @Override
            public String headerValue(String name) {
                return null;
            }

            @Override
            public HttpHeaders headers() {
                return new HttpHeaders();
            }

            @Override
            public Mono<ByteBuf> body() {
                try {

                    if (resourceType == ResourceType.DatabaseAccount) {
                        return Mono.just(ByteBufUtil.writeUtf8(ByteBufAllocator.DEFAULT, databaseAccount.toJson()));
                    }

                    if (operationType == OperationType.Batch) {
                        FakeBatchResponse fakeBatchResponse = new FakeBatchResponse();

                        fakeBatchResponse
                            .seteTag("1")
                            .setStatusCode(HttpConstants.StatusCodes.OK)
                            .setSubStatusCode(HttpConstants.SubStatusCodes.UNKNOWN)
                            .setRequestCharge(1.0d)
                            .setResourceBody(getTestPojoObject())
                            .setRetryAfterMilliseconds("1");

                        return Mono.just(ByteBufUtil.writeUtf8(ByteBufAllocator.DEFAULT,
                            OBJECT_MAPPER.writeValueAsString(Arrays.asList(fakeBatchResponse))));
                    }

                    return Mono.just(ByteBufUtil.writeUtf8(ByteBufAllocator.DEFAULT,
                        OBJECT_MAPPER.writeValueAsString(testPojo)));
                } catch (JsonProcessingException e) {
                    return Mono.error(e);
                }
            }

            @Override
            public Mono<String> bodyAsString() {
                try {

                    if (resourceType == ResourceType.DatabaseAccount) {
                        return Mono.just(databaseAccount.toJson());
                    }

                    if (operationType == OperationType.Batch) {
                        FakeBatchResponse fakeBatchResponse = new FakeBatchResponse();

                        fakeBatchResponse
                            .seteTag("1")
                            .setStatusCode(HttpConstants.StatusCodes.OK)
                            .setSubStatusCode(HttpConstants.SubStatusCodes.UNKNOWN)
                            .setRequestCharge(1.0d)
                            .setResourceBody(getTestPojoObject())
                            .setRetryAfterMilliseconds("1");

                        return Mono.just(OBJECT_MAPPER.writeValueAsString(Arrays.asList(fakeBatchResponse)));
                    }

                    return Mono.just(OBJECT_MAPPER.writeValueAsString(testPojo));
                } catch (JsonProcessingException e) {
                    return Mono.error(e);
                }
            }
        };

        try {
            return httpResponse.withRequest(new HttpRequest(HttpMethod.POST, TestConfigurations.HOST, 443));
        } catch (URISyntaxException e) {
            return httpResponse;
        }
    }
}
