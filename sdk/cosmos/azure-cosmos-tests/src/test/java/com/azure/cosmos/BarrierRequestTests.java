// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.*;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import com.azure.cosmos.implementation.directconnectivity.WFConstants;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.rx.TestSuiteBase;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ConnectTimeoutException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.azure.cosmos.implementation.HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_UNAVAILABLE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * E2E testing to verify the handling of barrier requests.
 */
public class BarrierRequestTests  extends TestSuiteBase {

    private String primaryRegion;
    private String secondaryRegion;
    private String primaryRegionalEndpointAsStr;
    private String secondaryRegionalEndpointAsStr;
    private AccountLevelLocationContext accountLevelLocationReadableLocationContext;

    @Factory(dataProvider = "clientBuildersWithDirectTcpSession")
    public BarrierRequestTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"multi-region"})
    public void beforeClass() {
        CosmosAsyncClient cosmosAsyncClient = getClientBuilder().buildAsyncClient();

        try {
            RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) ReflectionUtils.getAsyncDocumentClient(cosmosAsyncClient);
            GlobalEndpointManager globalEndpointManager = ReflectionUtils.getGlobalEndpointManager(rxDocumentClient);
            DatabaseAccount databaseAccountSnapshot = globalEndpointManager.getLatestDatabaseAccount();

            this.accountLevelLocationReadableLocationContext
                = getAccountLevelLocationContext(databaseAccountSnapshot, false);

            assertThat(this.accountLevelLocationReadableLocationContext).isNotNull();
            assertThat(this.accountLevelLocationReadableLocationContext.serviceOrderedReadableRegions).isNotNull();
            assertThat(this.accountLevelLocationReadableLocationContext.serviceOrderedReadableRegions.size()).isEqualTo(2);

            this.primaryRegion = this.accountLevelLocationReadableLocationContext.serviceOrderedReadableRegions.get(0);
            this.secondaryRegion = this.accountLevelLocationReadableLocationContext.serviceOrderedReadableRegions.get(1);
            this.primaryRegionalEndpointAsStr = this.accountLevelLocationReadableLocationContext.regionNameToEndpoint.get(this.primaryRegion);
            this.secondaryRegionalEndpointAsStr = this.accountLevelLocationReadableLocationContext.regionNameToEndpoint.get(this.secondaryRegion);
        } finally {
            cosmosAsyncClient.close();
        }
    }

    @Test
    public void assertHandleBarriersForStrongConsistencyWriteDuringFailover() {

        AtomicBoolean simulateAddressRefreshFailures = new AtomicBoolean(false);
        AtomicBoolean failoverTriggered = new AtomicBoolean(false);
        AtomicReference<GlobalEndpointManager> globalEndpointManager = new AtomicReference<>(null);

        CosmosClientBuilder clientBuilder = getClientBuilder()
                .consistencyLevel(ConsistencyLevel.STRONG)
                .directMode();

        clientBuilder.httpRequestInterceptor((request) -> {
            logger.info("inside httpRequestInterceptor, simulateAddressRefreshFailures: {}, operationType: {}, resourceType: {}, uri: {}",
                simulateAddressRefreshFailures.get(), request.getOperationType(), request.getResourceType());

            // After the initial write, simulate a network failure on address resolution.
            // This will trigger the SDK's failover logic.
            if (simulateAddressRefreshFailures.get() &&
                request.isAddressRefresh() &&
                request.requestContext.regionalRoutingContextToRoute.getRegion().equalsIgnoreCase(this.primaryRegion)) {
                logger.info("request operationType: " + request.getOperationType());
                logger.info("request resourceType: " + request.getResourceType());
                logger.info("Simulating network failure for address resolution for region " + this.primaryRegion);
                logger.info("failoverTriggered: " + failoverTriggered.get());
                failoverTriggered.compareAndSet(false, true);
                logger.info("failoverTriggered: " + failoverTriggered.get());
                Map<String, String> headers = new HashMap<>();
                headers.put(HttpConstants.HttpHeaders.SUB_STATUS, Integer.toString(GATEWAY_ENDPOINT_UNAVAILABLE));
                throw new CosmosException(HttpConstants.StatusCodes.SERVICE_UNAVAILABLE, "Simulating network failure for address resolution for region", headers, new ConnectTimeoutException());
            }

            // Once the failover is triggered, trigger a subsequent metadata refresh call (intercepted in httpRequestInterceptor).
            logger.info("Checking failoverTriggered to intercept metadata refresh call: " + failoverTriggered.get());
            //logger.info("isMetadataRequest: " + request.isMetadataRequest());
            if (failoverTriggered.get() && request.getResourceType() == ResourceType.DatabaseAccount && request.getOperationType() == OperationType.Read)
            {
                // Return the modified account properties, making the SDK believe a failover has occurred.
                logger.info("Intercepting metadata call and returning modified account properties to simulate failover. New write region: " + this.secondaryRegion);

                ByteBuf byteBuf = Utils.getUTF8BytesOrNull(getDatabaseAccountJsonAfterFailover());
                StoreResponse storeResponse = new StoreResponse(
                    TestConfigurations.HOST,
                    200,
                    request.getHeaders(),
                    new ByteBufInputStream(byteBuf),
                    byteBuf.readableBytes());

                return new RxDocumentServiceResponse(null, storeResponse);
            }

            return null; // let other requests proceed normally
        });

        clientBuilder.storeResponseInterceptor((request, storeResponse) -> {
            logger.info("inside storeResponseInterceptor, operationType: {}, resourceType: {}, region: {}",
                request.getOperationType(), request.getResourceType(), request.requestContext.regionalRoutingContextToRoute.getRegion());

            if (request.getOperationType() == OperationType.Create &&
                request.getResourceType() == ResourceType.Document &&
                request.requestContext.regionalRoutingContextToRoute.getRegion().equalsIgnoreCase(this.primaryRegion)) {

                String lsn = storeResponse.getHeaderValue(WFConstants.BackendHeaders.LSN);

                // Decrement so that GCLSN < LSN to simulate the replication lag
                String manipulatedGclsn = String.valueOf(Long.parseLong(lsn) - 2L);

                storeResponse.setHeaderValue(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN, manipulatedGclsn);

                // Enable address refresh failures for subsequent barrier requests in the primary region.
                simulateAddressRefreshFailures.compareAndSet(false, true);
                logger.info("inside storeResponseInterceptor, set simulateAddressRefreshFailures to {}", simulateAddressRefreshFailures.get());
            }

            if (request.getOperationType() == OperationType.Create &&
                request.getResourceType() == ResourceType.Document &&
                request.requestContext.regionalRoutingContextToRoute.getRegion().equalsIgnoreCase(this.secondaryRegion)) {

                String lsn = storeResponse.getHeaderValue(WFConstants.BackendHeaders.LSN);

                // Decrement so that GCLSN < LSN to simulate the replication lag
                String manipulatedGclsn = String.valueOf(Long.parseLong(lsn) - 2L);

                storeResponse.setHeaderValue(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN, manipulatedGclsn);

                // Enable address refresh failures for subsequent barrier requests in the primary region.
                simulateAddressRefreshFailures.compareAndSet(false, true);
                logger.info("inside storeResponseInterceptor, set simulateAddressRefreshFailures to {}", simulateAddressRefreshFailures.get());
            }

            // Track barrier requests (Head operations on a collection)
            if (request.getOperationType() == OperationType.Head && request.getResourceType() == ResourceType.DocumentCollection) {
                logger.info("Barrier request intercepted in storeResponseInterceptor for region: {}", request.requestContext.regionalRoutingContextToRoute.getRegion());
                logger.info("Setting failoverTriggered to true");
                failoverTriggered.compareAndSet(false, true);

                if (globalEndpointManager != null) {
                    logger.info("Trigerring metadata refresh");
                    globalEndpointManager.get().refreshLocationAsync(null, true).block();
                } else {
                    logger.info("globalEndpointManager is null, cannot trigger metadata refresh");
                }

                // If the barrier request is in the secondary region, allow it to succeed.
                logger.info("Barrier request detected for region: {}", request.requestContext.regionalRoutingContextToRoute.getRegion());
                if (request.requestContext.regionalRoutingContextToRoute.getRegion().equalsIgnoreCase(this.secondaryRegion)) {
                    // Satisfy the barrier condition by setting GCLSN >= LSN
                    storeResponse.setHeaderValue(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN, String.valueOf(storeResponse.getLSN()));
                } else {
                    // For any other region (initially the primary), keep the barrier condition unmet.
                    long lsn = storeResponse.getLSN() - 2;
                    storeResponse.setHeaderValue(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN, String.valueOf(lsn));
                }
            }
            return storeResponse;
        });

        CosmosAsyncClient client = clientBuilder.buildAsyncClient();

        try {
            CosmosAsyncContainer container = getSharedSinglePartitionCosmosContainer(client);

            globalEndpointManager.set(BridgeInternal.getContextClient(client).getGlobalEndpointManager());

            try {
                CosmosItemResponse<CosmosDiagnosticsTest.TestItem> response = container.createItem(CosmosDiagnosticsTest.TestItem.createNewItem()).block();
                logger.info("Item created");
                validateDiagnosticsIsPresent(response);

                CosmosDiagnosticsContext diagnosticsContext = response.getDiagnostics().getDiagnosticsContext();
                logger.info("Diagnostics on successful Create : {}", diagnosticsContext);
            } catch (CosmosException ex) {
                CosmosDiagnosticsContext diagnosticsContext = ex.getDiagnostics().getDiagnosticsContext();
                logger.error("Diagnostics on unsuccessful Create : {}", diagnosticsContext.toJson());
            }

        } finally {
            client.close();
        }
    }

    private void validateDiagnosticsIsPresent(CosmosItemResponse<CosmosDiagnosticsTest.TestItem> response) {
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.CREATED);
        assertThat(response.getDiagnostics()).isNotNull();
    }

    private String getDatabaseAccountJsonAfterFailover() {
        String globalDatabaseAccountName = null;
        String regex = "^https?://([^.]+)\\.documents\\.azure\\.com(?::\\d+)?/?";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(TestConfigurations.HOST);
        if (matcher.find() && matcher.toMatchResult().groupCount() == 1) {
            globalDatabaseAccountName = matcher.group(1);
        } else {
            throw new IllegalArgumentException("Invalid host: " + TestConfigurations.HOST);
        }

        // To simulate a failover from the backend, we modify the account topology in the following way:
        // - writeableLocations = secondary region
        // - replace secondary region in readableLocations with primary.
        String jsonString = "{\"_self\":\"\",\"id\":\"" + globalDatabaseAccountName + "\",\"_rid\":\"" + globalDatabaseAccountName + ".documents.azure.com\"," +
            "\"media\":\"//media/\",\"addresses\":\"//addresses/\",\"_dbs\":\"//dbs/\",\"writableLocations\":[{\"name\":\"" + secondaryRegion.toLowerCase().replaceAll("\\s", "") + "\",\"" +
            "databaseAccountEndpoint\":\"https://" + globalDatabaseAccountName + "-" + secondaryRegion.toLowerCase().replaceAll("\\s", "") +
            ".documents.azure.com:443/\"}],\"readableLocations\":[{\"name\"" +
            ":\"" + this.secondaryRegion + "\",\"databaseAccountEndpoint\":\"" + this.secondaryRegionalEndpointAsStr + "\"},{\"name\"" +
            ":\"" + this.primaryRegion + "\",\"databaseAccountEndpoint\":\"" + this.primaryRegionalEndpointAsStr  + "\"}]," +
            "\"enableMultipleWriteLocations\":false,\"continuousBackupEnabled\":false,\"enableNRegionSynchronousCommit\":false," +
            "\"enablePerPartitionFailoverBehavior\":false,\"userReplicationPolicy\":{\"asyncReplication\":false,\"minReplicaSetSize\":3," +
            "\"maxReplicasetSize\":4},\"userConsistencyPolicy\":{\"defaultConsistencyLevel\":\"Strong\"},\"systemReplicationPolicy\":" +
            "{\"minReplicaSetSize\":3,\"maxReplicasetSize\":4},\"readPolicy\":{\"primaryReadCoefficient\":1,\"secondaryReadCoefficient\":1}," +
            "\"queryEngineConfiguration\":\"{\\\"allowNewKeywords\\\":true,\\\"maxJoinsPerSqlQuery\\\":10,\\\"maxQueryRequestTimeoutFraction\\\"" +
            ":0.9,\\\"maxSqlQueryInputLength\\\":524288,\\\"maxUdfRefPerSqlQuery\\\":10,\\\"queryMaxInMemorySortDocumentCount\\\":-1000,\\\"" +
            "spatialMaxGeometryPointCount\\\":256,\\\"sqlAllowNonFiniteNumbers\\\":false,\\\"sqlDisableOptimizationFlags\\\":0,\\\"" +
            "sqlQueryILDisableOptimizationFlags\\\":0,\\\"clientDisableOptimisticDirectExecution\\\":false,\\\"queryEnableFullText\\\":true,\\\"" +
            "queryEnableFullTextPreviewFeatures\\\":false,\\\"queryMaxFullTextScoreSearchTerms\\\":5,\\\"queryMaxRrfArgumentCount\\\":100,\\\"" +
            "enableSpatialIndexing\\\":true,\\\"maxInExpressionItemsCount\\\":2147483647,\\\"maxLogicalAndPerSqlQuery\\\":2147483647,\\\"" +
            "maxLogicalOrPerSqlQuery\\\":2147483647,\\\"maxSpatialQueryCells\\\":2147483647,\\\"sqlAllowAggregateFunctions\\\":true,\\\"" +
            "sqlAllowGroupByClause\\\":true,\\\"sqlAllowLike\\\":true,\\\"sqlAllowSubQuery\\\":true,\\\"sqlAllowScalarSubQuery\\\":true,\\\"" +
            "sqlAllowTop\\\":true}\"}";

        return jsonString;
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
}
