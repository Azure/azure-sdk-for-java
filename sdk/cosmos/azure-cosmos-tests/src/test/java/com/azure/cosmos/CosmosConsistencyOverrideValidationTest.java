/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.azure.cosmos;

import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.directconnectivity.Protocol;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.rx.TestSuiteBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosConsistencyOverrideValidationTest extends TestSuiteBase {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String MULTIPLE_WRITE_REGIONS_PROPERTY =
        "COSMOS.CONSISTENCY_OVERRIDE_MULTIPLE_WRITE_REGIONS_ENABLED";
    private static final String EMULATOR_VNEXT_ENABLED_PROPERTY = "COSMOS.EMULATOR_VNEXT_ENABLED";
    private static final String HTTP2_ENABLED_PROPERTY = "COSMOS.HTTP2_ENABLED";
    private static final String THINCLIENT_ENABLED_PROPERTY = "COSMOS.THINCLIENT_ENABLED";

    private CosmosClient client;
    private CosmosContainer container;
    private final String modeLabel;
    private final boolean http2Enabled;
    private final boolean thinClientEnabled;
    private String previousHttp2Enabled;
    private String previousThinClientEnabled;
    private ConsistencyLevel accountDefaultConsistency;

    @Factory(dataProvider = "clientBuildersForConsistencyOverrides")
    public CosmosConsistencyOverrideValidationTest(
        CosmosClientBuilder clientBuilder,
        String modeLabel,
        boolean http2Enabled,
        boolean thinClientEnabled) {

        super(clientBuilder);
        this.modeLabel = modeLabel;
        this.http2Enabled = http2Enabled;
        this.thinClientEnabled = thinClientEnabled;
    }

    @Override
    public String resolveTestNameSuffix(Object[] row) {
        return this.modeLabel;
    }

    @DataProvider
    public static Object[][] clientBuildersForConsistencyOverrides() {
        boolean multipleWriteRegionsEnabled = Boolean.parseBoolean(
            System.getProperty(MULTIPLE_WRITE_REGIONS_PROPERTY, "false"));
        boolean emulatorVNextRun = isEmulatorVNextRun();

        List<Object[]> providers = new ArrayList<>();
        if (!emulatorVNextRun) {
            addDirectClientBuilder(providers, multipleWriteRegionsEnabled);
        }

        addGatewayClientBuilder(
            providers,
            TestConfigurations.HOST,
            multipleWriteRegionsEnabled,
            false,
            false);

        if (TestConfigurations.HOST.contains(ROUTING_GATEWAY_EMULATOR_PORT) && !emulatorVNextRun) {
            String computeGatewayEndpoint = TestConfigurations.HOST.replace(
                ROUTING_GATEWAY_EMULATOR_PORT,
                COMPUTE_GATEWAY_EMULATOR_PORT);
            addGatewayClientBuilder(
                providers,
                computeGatewayEndpoint,
                multipleWriteRegionsEnabled,
                false,
                false);
        }

        if (!emulatorVNextRun && !isEmulatorGatewayEndpoint(TestConfigurations.HOST)) {
            addGatewayClientBuilder(
                providers,
                TestConfigurations.HOST,
                multipleWriteRegionsEnabled,
                true,
                true);
        }

        return providers.toArray(new Object[0][]);
    }

    @BeforeClass(groups = { "consistency-overrides", "emulator", "emulator-vnext" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        assertThat(this.client).isNull();

        applyTransportSystemProperties();
        this.client = getClientBuilder().buildClient();
        CosmosAsyncContainer asyncContainer = getSharedMultiPartitionCosmosContainer(this.client.asyncClient());
        this.container = this.client
            .getDatabase(asyncContainer.getDatabase().getId())
            .getContainer(asyncContainer.getId());

        DatabaseAccount databaseAccount = getLatestDatabaseAccount();
        this.accountDefaultConsistency = databaseAccount.getConsistencyPolicy().getDefaultConsistencyLevel();

        logger.info(
            "Consistency override test mode [{}], endpoint [{}], HTTP/2 enabled [{}], thin client enabled [{}], account default consistency [{}]",
            this.modeLabel,
            getClientBuilder().getEndpoint(),
            this.http2Enabled,
            this.thinClientEnabled,
            this.accountDefaultConsistency);

        logAccountMetadata(databaseAccount);
    }

    @AfterClass(groups = { "consistency-overrides", "emulator", "emulator-vnext" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeCloseSyncClient(this.client);
        this.client = null;
        this.container = null;
        this.accountDefaultConsistency = null;
        restoreTransportSystemProperties();
    }

    @Test(groups = { "consistency-overrides", "emulator", "emulator-vnext" }, timeOut = TIMEOUT)
    public void requestOptionsConsistencyUpgradeReadAndQueryShouldBeIgnored() {
        List<ConsistencyLevel> unsupportedRequestConsistencies = strongerConsistencyLevelsThan(this.accountDefaultConsistency);
        if (unsupportedRequestConsistencies.isEmpty()) {
            throw new SkipException(
                "No request-level consistency upgrade exists for account default " + this.accountDefaultConsistency);
        }

        TestItem item = createTestItem();
        List<String> unexpectedOutcomes = new ArrayList<>();
        for (ConsistencyLevel requestedConsistency : unsupportedRequestConsistencies) {
            verifyUpgradeIgnoredOrRecord(
                "readItem",
                requestedConsistency,
                () -> executeRead(item, new CosmosItemRequestOptions().setConsistencyLevel(requestedConsistency)),
                unexpectedOutcomes);

            verifyUpgradeIgnoredOrRecord(
                "pointQuery",
                requestedConsistency,
                () -> executePointQuery(item, new CosmosQueryRequestOptions().setConsistencyLevel(requestedConsistency)),
                unexpectedOutcomes);
        }

        assertThat(unexpectedOutcomes)
            .as(
                "All request-level consistency upgrades should be ignored for mode [%s], endpoint [%s], "
                    + "account default consistency [%s]",
                this.modeLabel,
                getClientBuilder().getEndpoint(),
                this.accountDefaultConsistency)
            .isEmpty();
    }

    @Test(groups = { "consistency-overrides", "emulator", "emulator-vnext" }, timeOut = TIMEOUT)
    public void latestCommittedReadConsistencyStrategyReadAndQueryShouldSucceed() {
        TestItem item = createTestItem();

        OperationResult readResult = executeRead(
            item,
            new CosmosItemRequestOptions().setReadConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED));
        assertThat(readResult.statusCode).isEqualTo(HttpConstants.StatusCodes.OK);
        assertLatestCommitted(readResult, "readItem");

        OperationResult queryResult = executePointQuery(
            item,
            new CosmosQueryRequestOptions().setReadConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED));
        assertThat(queryResult.resultCount).isEqualTo(1);
        assertLatestCommitted(queryResult, "pointQuery");

        OperationResult changeFeedResult = executeChangeFeed(
            CosmosChangeFeedRequestOptions
                .createForProcessingFromNow(FeedRange.forFullRange())
                .setReadConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED));
        assertThat(changeFeedResult.statusCode)
            .as("queryChangeFeed status code")
            .isIn(HttpConstants.StatusCodes.OK, HttpConstants.StatusCodes.NOT_MODIFIED);
        assertLatestCommitted(changeFeedResult, "queryChangeFeed");
    }

    private TestItem createTestItem() {
        String id = UUID.randomUUID().toString();
        String partitionKey = UUID.randomUUID().toString();
        ObjectNode item = OBJECT_MAPPER.createObjectNode();
        item.put("id", id);
        item.put("mypk", partitionKey);
        item.put("payload", UUID.randomUUID().toString());

        CosmosItemResponse<ObjectNode> createResponse = this.container.createItem(item);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.CREATED);
        waitIfNeededForReplicasToCatchUp();
        return new TestItem(id, partitionKey);
    }

    private void waitIfNeededForReplicasToCatchUp() {
        switch (this.accountDefaultConsistency) {
            case EVENTUAL:
            case CONSISTENT_PREFIX:
                logger.info(" additional wait in EVENTUAL mode so the replica catch up");
                try {
                    Thread.sleep(WAIT_REPLICA_CATCH_UP_IN_MILLIS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                break;

            default:
                break;
        }
    }

    private OperationResult executeRead(TestItem item, CosmosItemRequestOptions requestOptions) {
        CosmosItemResponse<ObjectNode> response = this.container.readItem(
            item.id,
            new PartitionKey(item.partitionKey),
            requestOptions,
            ObjectNode.class);

        return new OperationResult(
            response.getDiagnostics().getDiagnosticsContext(),
            1,
            0,
            response.getStatusCode());
    }

    private OperationResult executePointQuery(TestItem item, CosmosQueryRequestOptions requestOptions) {
        SqlQuerySpec querySpec = new SqlQuerySpec(
            "SELECT * FROM c WHERE c.mypk = @pk AND c.id = @id",
            new SqlParameter("@pk", item.partitionKey),
            new SqlParameter("@id", item.id));

        Iterator<FeedResponse<ObjectNode>> iterator = this.container
            .queryItems(querySpec, requestOptions, ObjectNode.class)
            .iterableByPage()
            .iterator();

        int resultCount = 0;
        int pageCount = 0;
        CosmosDiagnosticsContext diagnosticsContext = null;
        while (iterator.hasNext()) {
            FeedResponse<ObjectNode> response = iterator.next();
            pageCount++;
            resultCount += response.getResults().size();
            diagnosticsContext = response.getCosmosDiagnostics().getDiagnosticsContext();

            if (resultCount > 0) {
                break;
            }
        }

        return new OperationResult(
            diagnosticsContext,
            resultCount,
            pageCount,
            diagnosticsContext != null ? diagnosticsContext.getStatusCode() : -1);
    }

    private OperationResult executeChangeFeed(CosmosChangeFeedRequestOptions requestOptions) {
        Iterator<FeedResponse<ObjectNode>> iterator = this.container
            .queryChangeFeed(requestOptions, ObjectNode.class)
            .iterableByPage()
            .iterator();

        if (!iterator.hasNext()) {
            return new OperationResult(null, 0, 0, -1);
        }

        FeedResponse<ObjectNode> response = iterator.next();
        CosmosDiagnosticsContext diagnosticsContext = response.getCosmosDiagnostics().getDiagnosticsContext();
        return new OperationResult(
            diagnosticsContext,
            response.getResults().size(),
            1,
            diagnosticsContext != null ? diagnosticsContext.getStatusCode() : -1);
    }

    private void verifyUpgradeIgnoredOrRecord(
        String operationName,
        ConsistencyLevel requestedConsistency,
        ConsistencyOverrideOperation operation,
        List<String> unexpectedOutcomes) {

        try {
            OperationResult result = operation.execute();
            if (result.statusCode != HttpConstants.StatusCodes.OK || result.resultCount != 1) {
                logger.warn(
                    "CONSISTENCY_OVERRIDE_RESULT mode [{}], endpoint [{}], operation [{}], requested consistency [{}], "
                        + "outcome [UNEXPECTED_SUCCESS_SHAPE], status [{}], result count [{}], page count [{}], "
                        + "diagnostics [{}]",
                    this.modeLabel,
                    getClientBuilder().getEndpoint(),
                    operationName,
                    requestedConsistency,
                    result.statusCode,
                    result.resultCount,
                    result.pageCount,
                    diagnosticsSummary(result.diagnosticsContext));
                unexpectedOutcomes.add(String.format(
                    "%s returned unexpected success shape for requested consistency [%s]. Status [%s], "
                        + "result count [%s], page count [%s], diagnostics [%s]",
                    operationName,
                    requestedConsistency,
                    result.statusCode,
                    result.resultCount,
                    result.pageCount,
                    diagnosticsSummary(result.diagnosticsContext)));
                return;
            }

            if (result.diagnosticsContext == null
                || result.diagnosticsContext.getEffectiveConsistencyLevel() != this.accountDefaultConsistency) {
                logger.warn(
                    "CONSISTENCY_OVERRIDE_RESULT mode [{}], endpoint [{}], operation [{}], requested consistency [{}], "
                        + "outcome [UNEXPECTED_EFFECTIVE_CONSISTENCY], status [{}], result count [{}], "
                        + "page count [{}], account default consistency [{}], diagnostics [{}]",
                    this.modeLabel,
                    getClientBuilder().getEndpoint(),
                    operationName,
                    requestedConsistency,
                    result.statusCode,
                    result.resultCount,
                    result.pageCount,
                    this.accountDefaultConsistency,
                    diagnosticsSummary(result.diagnosticsContext));
                unexpectedOutcomes.add(String.format(
                    "%s did not ignore requested consistency [%s]. Account default consistency [%s], diagnostics [%s]",
                    operationName,
                    requestedConsistency,
                    this.accountDefaultConsistency,
                    diagnosticsSummary(result.diagnosticsContext)));
                return;
            }

            logger.info(
                "CONSISTENCY_OVERRIDE_RESULT mode [{}], endpoint [{}], operation [{}], requested consistency [{}], "
                    + "outcome [EXPECTED_IGNORED_UPGRADE], status [{}], result count [{}], page count [{}], "
                    + "diagnostics [{}]",
                this.modeLabel,
                getClientBuilder().getEndpoint(),
                operationName,
                requestedConsistency,
                result.statusCode,
                result.resultCount,
                result.pageCount,
                diagnosticsSummary(result.diagnosticsContext));
        } catch (CosmosException error) {
            logger.warn(
                "CONSISTENCY_OVERRIDE_RESULT mode [{}], endpoint [{}], operation [{}], requested consistency [{}], "
                    + "outcome [UNEXPECTED_FAILURE], status [{}], substatus [{}], diagnostics [{}]",
                this.modeLabel,
                getClientBuilder().getEndpoint(),
                operationName,
                requestedConsistency,
                error.getStatusCode(),
                error.getSubStatusCode(),
                diagnosticsSummary(error.getDiagnostics() != null
                    ? error.getDiagnostics().getDiagnosticsContext()
                    : null));
            unexpectedOutcomes.add(String.format(
                "%s failed for requested consistency [%s]. Status [%s], substatus [%s], diagnostics [%s], "
                    + "message [%s]",
                operationName,
                requestedConsistency,
                error.getStatusCode(),
                error.getSubStatusCode(),
                diagnosticsSummary(error.getDiagnostics() != null
                    ? error.getDiagnostics().getDiagnosticsContext()
                    : null),
                error.getMessage()));
        }
    }

    private void assertLatestCommitted(OperationResult result, String operationName) {
        assertThat(result.diagnosticsContext)
            .as("%s diagnostics should not be null", operationName)
            .isNotNull();
        assertThat(result.diagnosticsContext.getEffectiveReadConsistencyStrategy())
            .as("%s effective read consistency strategy", operationName)
            .isEqualTo(ReadConsistencyStrategy.LATEST_COMMITTED);
    }

    private DatabaseAccount getLatestDatabaseAccount() {
        GlobalEndpointManager globalEndpointManager = ReflectionUtils.getGlobalEndpointManager(
            (RxDocumentClientImpl) this.client.asyncClient().getContextClient());
        return globalEndpointManager.getLatestDatabaseAccount();
    }

    private void logAccountMetadata(DatabaseAccount databaseAccount) {
        logger.info(
            "Consistency override test account metadata: multiple write locations enabled [{}], writable regions [{}], readable regions [{}]",
            databaseAccount.getEnableMultipleWriteLocations(),
            locationNames(databaseAccount.getWritableLocations()),
            locationNames(databaseAccount.getReadableLocations()));
    }

    private static List<String> locationNames(Iterable<DatabaseAccountLocation> locations) {
        List<String> names = new ArrayList<>();
        for (DatabaseAccountLocation location : locations) {
            names.add(location.getName());
        }

        return names;
    }

    private static List<ConsistencyLevel> strongerConsistencyLevelsThan(ConsistencyLevel accountDefaultConsistency) {
        return Arrays.stream(ConsistencyLevel.values())
            .filter(consistencyLevel -> strength(consistencyLevel) > strength(accountDefaultConsistency))
            .collect(Collectors.toList());
    }

    private static int strength(ConsistencyLevel consistencyLevel) {
        switch (consistencyLevel) {
            case STRONG:
                return 5;
            case BOUNDED_STALENESS:
                return 4;
            case SESSION:
                return 3;
            case CONSISTENT_PREFIX:
                return 2;
            case EVENTUAL:
                return 1;
            default:
                throw new IllegalArgumentException("Unknown consistency level " + consistencyLevel);
        }
    }

    private static String diagnosticsSummary(CosmosDiagnosticsContext diagnosticsContext) {
        if (diagnosticsContext == null) {
            return "null";
        }

        return String.format(
            "status=%s, subStatus=%s, effectiveConsistency=%s, effectiveReadConsistencyStrategy=%s",
            diagnosticsContext.getStatusCode(),
            diagnosticsContext.getSubStatusCode(),
            diagnosticsContext.getEffectiveConsistencyLevel(),
            diagnosticsContext.getEffectiveReadConsistencyStrategy());
    }

    private static void addGatewayClientBuilder(
        List<Object[]> providers,
        String endpoint,
        boolean multipleWriteRegionsEnabled,
        boolean http2Enabled,
        boolean thinClientEnabled) {

        providers.add(new Object[] {
            createGatewayBuilder(endpoint, multipleWriteRegionsEnabled, http2Enabled, thinClientEnabled),
            modeLabel(endpoint, multipleWriteRegionsEnabled, http2Enabled, thinClientEnabled),
            http2Enabled,
            thinClientEnabled
        });
    }

    private static void addDirectClientBuilder(List<Object[]> providers, boolean multipleWriteRegionsEnabled) {
        CosmosClientBuilder builder = createDirectRxDocumentClient(
            null,
            Protocol.TCP,
            multipleWriteRegionsEnabled,
            preferredLocations,
            true,
            true);
        builder.multipleWriteRegionsEnabled(multipleWriteRegionsEnabled);

        providers.add(new Object[] {
            builder,
            "DirectTcp" + (multipleWriteRegionsEnabled ? "-MultiWrite" : "-SingleWrite"),
            false,
            false
        });
    }

    private static CosmosClientBuilder createGatewayBuilder(
        String endpoint,
        boolean multipleWriteRegionsEnabled,
        boolean http2Enabled,
        boolean thinClientEnabled) {

        String previousHttp2Enabled = System.getProperty(HTTP2_ENABLED_PROPERTY);
        String previousThinClientEnabled = System.getProperty(THINCLIENT_ENABLED_PROPERTY);

        try {
            setTransportSystemProperties(http2Enabled, thinClientEnabled);
            return createGatewayRxDocumentClient(
                endpoint,
                null,
                multipleWriteRegionsEnabled,
                preferredLocations,
                true,
                true,
                http2Enabled);
        } finally {
            restoreSystemProperty(HTTP2_ENABLED_PROPERTY, previousHttp2Enabled);
            restoreSystemProperty(THINCLIENT_ENABLED_PROPERTY, previousThinClientEnabled);
        }
    }

    private void applyTransportSystemProperties() {
        this.previousHttp2Enabled = System.getProperty(HTTP2_ENABLED_PROPERTY);
        this.previousThinClientEnabled = System.getProperty(THINCLIENT_ENABLED_PROPERTY);
        setTransportSystemProperties(this.http2Enabled, this.thinClientEnabled);
    }

    private void restoreTransportSystemProperties() {
        restoreSystemProperty(HTTP2_ENABLED_PROPERTY, this.previousHttp2Enabled);
        restoreSystemProperty(THINCLIENT_ENABLED_PROPERTY, this.previousThinClientEnabled);
        this.previousHttp2Enabled = null;
        this.previousThinClientEnabled = null;
    }

    private static void setTransportSystemProperties(boolean http2Enabled, boolean thinClientEnabled) {
        System.setProperty(HTTP2_ENABLED_PROPERTY, Boolean.toString(http2Enabled));
        System.setProperty(THINCLIENT_ENABLED_PROPERTY, Boolean.toString(thinClientEnabled));
    }

    private static void restoreSystemProperty(String propertyName, String previousValue) {
        if (previousValue == null) {
            System.clearProperty(propertyName);
        } else {
            System.setProperty(propertyName, previousValue);
        }
    }

    private static boolean isEmulatorGatewayEndpoint(String endpoint) {
        return endpoint.contains(ROUTING_GATEWAY_EMULATOR_PORT)
            || endpoint.contains(COMPUTE_GATEWAY_EMULATOR_PORT);
    }

    private static boolean isEmulatorVNextRun() {
        return Boolean.parseBoolean(System.getProperty(EMULATOR_VNEXT_ENABLED_PROPERTY, "false"))
            || containsEmulatorVNextGroup(System.getProperty("test.groups"))
            || containsEmulatorVNextGroup(System.getProperty("groups"))
            || containsEmulatorVNextGroup(System.getProperty("includedGroups"))
            || TestConfigurations.HOST.startsWith("http://");
    }

    private static boolean containsEmulatorVNextGroup(String groups) {
        return groups != null && Arrays.stream(groups.split(","))
            .map(String::trim)
            .anyMatch("emulator-vnext"::equals);
    }

    private static String modeLabel(
        String endpoint,
        boolean multipleWriteRegionsEnabled,
        boolean http2Enabled,
        boolean thinClientEnabled) {

        String gatewayLabel;
        if (endpoint.contains(COMPUTE_GATEWAY_EMULATOR_PORT)) {
            gatewayLabel = "ComputeGateway";
        } else if (endpoint.contains(ROUTING_GATEWAY_EMULATOR_PORT)) {
            gatewayLabel = "RoutingGateway";
        } else if (thinClientEnabled) {
            gatewayLabel = "GatewayV2ThinClient";
        } else if (http2Enabled) {
            gatewayLabel = "GatewayV2Http2";
        } else {
            gatewayLabel = "GatewayV1";
        }

        return gatewayLabel + (multipleWriteRegionsEnabled ? "-MultiWrite" : "-SingleWrite");
    }

    @FunctionalInterface
    private interface ConsistencyOverrideOperation {
        OperationResult execute();
    }

    private static final class OperationResult {
        private final CosmosDiagnosticsContext diagnosticsContext;
        private final int resultCount;
        private final int pageCount;
        private final int statusCode;

        private OperationResult(
            CosmosDiagnosticsContext diagnosticsContext,
            int resultCount,
            int pageCount,
            int statusCode) {

            this.diagnosticsContext = diagnosticsContext;
            this.resultCount = resultCount;
            this.pageCount = pageCount;
            this.statusCode = statusCode;
        }
    }

    private static final class TestItem {
        private final String id;
        private final String partitionKey;

        private TestItem(String id, String partitionKey) {
            this.id = id;
            this.partitionKey = partitionKey;
        }
    }
}
