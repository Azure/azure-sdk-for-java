// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.*;
import com.azure.cosmos.implementation.directconnectivity.HttpUtils;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import com.azure.cosmos.implementation.directconnectivity.WFConstants;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.rx.TestSuiteBase;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ConnectTimeoutException;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.azure.cosmos.implementation.HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_UNAVAILABLE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * E2E testing to verify the handling of barrier requests.
 */
public class BarrierRequestTests  extends TestSuiteBase {
    // eg. "Central US", case matters
    String primaryRegion = "Central US";
    String secondaryRegion = "East US";

    @Factory(dataProvider = "clientBuildersWithDirectTcpSession")
    public BarrierRequestTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test
    public void AssertHandleBarriersForStrongConsistencyWriteDuringFailover() {
        AtomicBoolean simulateAddressRefreshFailures = new AtomicBoolean(false);
        AtomicBoolean failoverTriggered = new AtomicBoolean(false);

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
                request.requestContext.regionalRoutingContextToRoute.getRegion().equalsIgnoreCase(this.primaryRegion)) // Target the primary region
            {
                logger.info("request operationType: " + request.getOperationType());
                logger.info("request resourceType: " + request.getResourceType());
                logger.info("Simulating network failure for address resolution for region " + this.primaryRegion);
                logger.info("failoverTriggered: " + failoverTriggered.get());
                failoverTriggered.compareAndSet(false, true);
                logger.info("failoverTriggered: " + failoverTriggered.get());
                Map<String, String> headers = new HashMap<>();
                headers.put(HttpConstants.HttpHeaders.SUB_STATUS, Integer.toString(GATEWAY_ENDPOINT_UNAVAILABLE));
                throw new CosmosException(HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_UNAVAILABLE, "Simulating network failure for address resolution for region", headers, new ConnectTimeoutException());
            }

            // Once the failover is triggered, intercept the subsequent metadata refresh call.
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

            // Track barrier requests (Head operations on a collection)
            if (request.getOperationType() == OperationType.Head && request.getResourceType() == ResourceType.DocumentCollection)
            {
                // If the barrier request is in the secondary region, allow it to succeed.
                logger.info("Barrier request detected for region: {}", request.requestContext.regionalRoutingContextToRoute.getRegion());
                if (request.requestContext.regionalRoutingContextToRoute.getRegion().equalsIgnoreCase(this.secondaryRegion))
                {
                    // Satisfy the barrier condition by setting GCLSN >= LSN
                    storeResponse.setHeaderValue(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN, String.valueOf(storeResponse.getLSN()));
                }
                    else
                {
                    // For any other region (initially the primary), keep the barrier condition unmet.
                    long lsn = storeResponse.getLSN() - 2;
                    storeResponse.setHeaderValue(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN, String.valueOf(lsn));
                }
            }
            return storeResponse;
        });

        CosmosAsyncClient client = clientBuilder.buildAsyncClient();
        CosmosAsyncContainer container = getSharedSinglePartitionCosmosContainer(client);

        CosmosItemResponse<CosmosDiagnosticsTest.TestItem> response = container.createItem(CosmosDiagnosticsTest.TestItem.createNewItem()).block();
        logger.info("Item created");
        validateDiagnosticsIsPresent(response);

        CosmosDiagnosticsContext diagnosticsContext = response.getDiagnostics().getDiagnosticsContext();
        System.out.println(diagnosticsContext);
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
            ":\"Central US\",\"databaseAccountEndpoint\":\"https://neha-test-account4-centralus.documents.azure.com:443/\"},{\"name\"" +
            ":\"East US 2\",\"databaseAccountEndpoint\":\"https://neha-test-account4-eastus2.documents.azure.com:443/\"}]," +
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
}
