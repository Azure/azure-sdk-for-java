// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.RetryStrategy;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.TypeReference;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.monitor.query.models.LogsBatchQuery;
import com.azure.monitor.query.models.LogsBatchQueryResult;
import com.azure.monitor.query.models.LogsBatchQueryResultCollection;
import com.azure.monitor.query.models.LogsQueryOptions;
import com.azure.monitor.query.models.LogsQueryResult;
import com.azure.monitor.query.models.LogsQueryResultStatus;
import com.azure.monitor.query.models.LogsTableCell;
import com.azure.monitor.query.models.QueryTimeInterval;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static com.azure.monitor.query.MonitorQueryTestUtils.QUERY_STRING;
import static com.azure.monitor.query.MonitorQueryTestUtils.getAdditionalLogWorkspaceId;
import static com.azure.monitor.query.MonitorQueryTestUtils.getLogResourceId;
import static com.azure.monitor.query.MonitorQueryTestUtils.getLogWorkspaceId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Unit tests for {@link LogsQueryClient}
 */
public class LogsQueryClientTest extends TestProxyTestBase {

    private LogsQueryClient client;

    private String workspaceId;

    private String additionalWorkspaceId;

    private String resourceId;

    @BeforeEach
    public void setup() {
        workspaceId = getLogWorkspaceId(interceptorManager.isPlaybackMode());
        additionalWorkspaceId = getAdditionalLogWorkspaceId(interceptorManager.isPlaybackMode());
        resourceId = getLogResourceId(interceptorManager.isPlaybackMode());
        LogsQueryClientBuilder clientBuilder = new LogsQueryClientBuilder()
                .retryPolicy(new RetryPolicy(new RetryStrategy() {
                    @Override
                    public int getMaxRetries() {
                        return 0;
                    }

                    @Override
                    public Duration calculateRetryDelay(int i) {
                        return null;
                    }
                }));
        if (getTestMode() == TestMode.PLAYBACK) {
            clientBuilder
                    .credential(request -> Mono.just(new AccessToken("fakeToken", OffsetDateTime.now().plusDays(1))))
                    .httpClient(getAssertingHttpClient(interceptorManager.getPlaybackClient()));
        } else if (getTestMode() == TestMode.RECORD) {
            clientBuilder
                    .addPolicy(interceptorManager.getRecordPolicy())
                    .credential(getCredential());
        } else if (getTestMode() == TestMode.LIVE) {
            clientBuilder.credential(getCredential());
            clientBuilder.endpoint(MonitorQueryTestUtils.getLogEndpoint());
        }

        if (!interceptorManager.isLiveMode()) {
            // Remove `$..name` and `$..id` sanitizer from the list of common sanitizers
            interceptorManager.removeSanitizers("AZSDK3493", "AZSDK3430");
        }

        this.client = clientBuilder
                .buildClient();
    }

    private HttpClient getAssertingHttpClient(HttpClient httpClient) {
        return new AssertingHttpClientBuilder(httpClient)
            .assertSync()
            .skipRequest((request, context) -> false)
            .build();
    }

    private TokenCredential getCredential() {
        return new DefaultAzureCredentialBuilder().build();
    }

    @Test
    public void testLogsQuery() {
        LogsQueryResult queryResults = client.queryWorkspace(workspaceId, QUERY_STRING,
                new QueryTimeInterval(OffsetDateTime.of(LocalDateTime.of(2021, 01, 01, 0, 0), ZoneOffset.UTC),
                        OffsetDateTime.of(LocalDateTime.of(2021, 06, 10, 0, 0), ZoneOffset.UTC)));
        assertEquals(1, queryResults.getAllTables().size());
        assertEquals(1200, queryResults.getAllTables().get(0).getAllTableCells().size());
        assertEquals(100, queryResults.getAllTables().get(0).getRows().size());
    }

    @Test
    public void testLogsQueryResource() {
        LogsQueryResult queryResults = client.queryResource(resourceId, QUERY_STRING,
            new QueryTimeInterval(OffsetDateTime.of(LocalDateTime.of(2021, 01, 01, 0, 0), ZoneOffset.UTC),
                OffsetDateTime.of(LocalDateTime.of(2021, 06, 10, 0, 0), ZoneOffset.UTC)));
        assertEquals(1, queryResults.getAllTables().size());
        assertEquals(1200, queryResults.getAllTables().get(0).getAllTableCells().size());
        assertEquals(100, queryResults.getAllTables().get(0).getRows().size());
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void testLogsQueryAllowPartialSuccess() {
        // Arrange
        final String query =  "let dt = datatable (DateTime: datetime, Bool:bool, Guid: guid, Int: "
            + "int, Long:long, Double: double, String: string, Timespan: timespan, Decimal: decimal, Dynamic: dynamic)\n"
            + "[datetime(2015-12-31 23:59:59.9), false, guid(74be27de-1e4e-49d9-b579-fe0b331d3642), 12345, 1, 12345.6789,"
            + " 'string value', 10s, decimal(0.10101), dynamic({\"a\":123, \"b\":\"hello\", \"c\":[1,2,3], \"d\":{}})];"
            + "range x from 1 to 400000 step 1 | extend y=1 | join kind=fullouter dt on $left.y == $right.Long";

        final LogsQueryOptions options = new LogsQueryOptions().setAllowPartialErrors(true);
        final QueryTimeInterval interval = QueryTimeInterval.LAST_DAY;

        // Act
        final Response<LogsQueryResult> response = client.queryWorkspaceWithResponse(workspaceId, query, interval,
            options, Context.NONE);

        // Assert
        final LogsQueryResult result = response.getValue();

        assertEquals(LogsQueryResultStatus.PARTIAL_FAILURE, result.getQueryResultStatus());
        assertNotNull(result.getError());
        assertNotNull(result.getTable());
        assertTrue(result.getTable().getRows().size() > 0, "Expected there to be rows returned.");
    }

    @Test
    public void testLogsQueryBatch() {
        LogsBatchQuery logsBatchQuery = new LogsBatchQuery();
        logsBatchQuery.addWorkspaceQuery(workspaceId, QUERY_STRING + " | take 2", null);
        logsBatchQuery.addWorkspaceQuery(workspaceId, QUERY_STRING + "| take 3", null);

        LogsBatchQueryResultCollection batchResultCollection = client
                .queryBatchWithResponse(logsBatchQuery, Context.NONE).getValue();

        List<LogsBatchQueryResult> responses = batchResultCollection.getBatchResults();

        assertEquals(2, responses.size());

        assertEquals(1, responses.get(0).getAllTables().size());
        assertEquals(24, responses.get(0).getAllTables().get(0).getAllTableCells().size());
        assertEquals(2, responses.get(0).getAllTables().get(0).getRows().size());

        assertEquals(1, responses.get(1).getAllTables().size());
        assertEquals(36, responses.get(1).getAllTables().get(0).getAllTableCells().size());
        assertEquals(3, responses.get(1).getAllTables().get(0).getRows().size());
    }

    @Test
    public void testLogsQueryBatchWithServerTimeout() {

        LogsQueryClientBuilder clientBuilder = new LogsQueryClientBuilder();
        if (getTestMode() == TestMode.PLAYBACK) {
            clientBuilder
                .credential(request -> Mono.just(new AccessToken("fakeToken", OffsetDateTime.now().plusDays(1))))
                .httpClient(getAssertingHttpClient(interceptorManager.getPlaybackClient()));
        } else if (getTestMode() == TestMode.RECORD) {
            clientBuilder
                .addPolicy(interceptorManager.getRecordPolicy())
                .credential(getCredential());
        } else if (getTestMode() == TestMode.LIVE) {
            clientBuilder.credential(getCredential());
            clientBuilder.endpoint(MonitorQueryTestUtils.getLogEndpoint());
        }
        LogsQueryClient client = clientBuilder
            .addPolicy((context, next) -> {
                String requestBody = context.getHttpRequest().getBodyAsBinaryData().toString();
                Assertions.assertTrue(requestBody.contains("wait=10"));
                Assertions.assertTrue(requestBody.contains("wait=20"));
                return next.process();
            })
            .buildClient();

        LogsBatchQuery logsBatchQuery = new LogsBatchQuery();
        logsBatchQuery.addWorkspaceQuery(workspaceId, QUERY_STRING + " | take 2", null);
        logsBatchQuery.addWorkspaceQuery(workspaceId, QUERY_STRING + " | take 5", null,
                new LogsQueryOptions().setServerTimeout(Duration.ofSeconds(20)));
        logsBatchQuery.addWorkspaceQuery(workspaceId, QUERY_STRING + "| take 3", null,
                new LogsQueryOptions().setServerTimeout(Duration.ofSeconds(10)));

        LogsBatchQueryResultCollection batchResultCollection = client
                .queryBatchWithResponse(logsBatchQuery, Context.NONE).getValue();

        List<LogsBatchQueryResult> responses = batchResultCollection.getBatchResults();

        assertEquals(3, responses.size());

        assertEquals(1, responses.get(0).getAllTables().size());
        assertEquals(24, responses.get(0).getAllTables().get(0).getAllTableCells().size());
        assertEquals(2, responses.get(0).getAllTables().get(0).getRows().size());

        assertEquals(1, responses.get(1).getAllTables().size());
        assertEquals(60, responses.get(1).getAllTables().get(0).getAllTableCells().size());
        assertEquals(5, responses.get(1).getAllTables().get(0).getRows().size());

        assertEquals(1, responses.get(2).getAllTables().size());
        assertEquals(36, responses.get(2).getAllTables().get(0).getAllTableCells().size());
        assertEquals(3, responses.get(2).getAllTables().get(0).getRows().size());
    }

    @Test
    public void testMultipleWorkspaces() {
        final String multipleWorkspacesQuery = "let dt = datatable (DateTime: datetime, Bool:bool, Guid: guid, Int: "
            + "int, Long:long, Double: double, String: string, Timespan: timespan, Decimal: decimal, Dynamic: dynamic, TenantId: string)\n"
            + "[datetime(2015-12-31 23:59:59.9), false, guid(74be27de-1e4e-49d9-b579-fe0b331d3642), 12345, 1, 12345.6789,"
            + " 'string value', 10s, decimal(0.10101), dynamic({\"a\":123, \"b\":\"hello\", \"c\":[1,2,3], \"d\":{}}), \"" + workspaceId + "\""
            + ", datetime(2015-12-31 23:59:59.9), false, guid(74be27de-1e4e-49d9-b579-fe0b331d3642), 12345, 1, 12345.6789,"
            + " 'string value', 10s, decimal(0.10101), dynamic({\"a\":123, \"b\":\"hello\", \"c\":[1,2,3], \"d\":{}}), \"" + additionalWorkspaceId + "\"];"
            + "range x from 1 to 2 step 1 | extend y=1 | join kind=fullouter dt on $left.y == $right.Long";

        LogsQueryResult queryResults = client.queryWorkspaceWithResponse(workspaceId,
                multipleWorkspacesQuery, null,
                new LogsQueryOptions()
                        .setAdditionalWorkspaces(Collections.singletonList(additionalWorkspaceId)), Context.NONE)
                .getValue();
        assertEquals(1, queryResults.getAllTables().size());
        assertEquals(2, queryResults
                .getAllTables()
                .get(0)
                .getRows()
                .stream()
                .map(row -> row.getColumnValue("TenantId").get())
                .map(LogsTableCell::getValueAsString)
                .distinct()
                .count());
    }

    @Test
    public void testBatchQueryPartialSuccess() {
        LogsBatchQuery logsBatchQuery = new LogsBatchQuery();
        logsBatchQuery.addWorkspaceQuery(workspaceId,  QUERY_STRING + " | take 2", null);
        logsBatchQuery.addWorkspaceQuery(workspaceId, QUERY_STRING + " | take", null);

        LogsBatchQueryResultCollection batchResultCollection = client
                .queryBatchWithResponse(logsBatchQuery, Context.NONE).getValue();

        List<LogsBatchQueryResult> responses = batchResultCollection.getBatchResults();

        assertEquals(2, responses.size());
        assertEquals(LogsQueryResultStatus.SUCCESS, responses.get(0).getQueryResultStatus());
        assertNull(responses.get(0).getError());
        assertEquals(LogsQueryResultStatus.FAILURE, responses.get(1).getQueryResultStatus());
        assertNotNull(responses.get(1).getError());
        assertEquals("BadArgumentError", responses.get(1).getError().getCode());
    }

    @Test
    public void testStatistics() {
        LogsQueryResult queryResults = client.queryWorkspaceWithResponse(workspaceId,
                QUERY_STRING, null, new LogsQueryOptions().setIncludeStatistics(true), Context.NONE).getValue();

        BinaryData statisticsData = queryResults.getStatistics();

        try (JsonReader jsonReader = JsonProviders.createReader(statisticsData.toBytes())) {
            Map<String, Object> statisticsMap = jsonReader.readMap(JsonReader::readUntyped);
            assertNotNull(statisticsMap);

            Object query = statisticsMap.get("query");
            if (query instanceof Map<?, ?>) {
                Map<?, ?> queryMap = (Map<?, ?>) query;
                assertNotNull(queryMap.get("executionTime"));
                assertNotNull(queryMap.get("resourceUsage"));
            } else {
                Assertions.fail("Failed to read the statistics data.");
            }
        } catch (Exception e) {
            Assertions.fail("Failed to read the statistics data.");
        }

        assertEquals(1, queryResults.getAllTables().size());
        assertNotNull(queryResults.getStatistics());
    }

    @Test
    public void testStatisticsResourceQuery() {
        LogsQueryResult queryResults = client.queryResourceWithResponse(resourceId,
            QUERY_STRING, null, new LogsQueryOptions().setIncludeStatistics(true), Context.NONE)
            .getValue();
        assertEquals(1, queryResults.getAllTables().size());
        assertNotNull(queryResults.getStatistics());
    }

    @Test
    @Disabled
    public void testBatchStatistics() {
        LogsBatchQuery logsBatchQuery = new LogsBatchQuery();
        logsBatchQuery.addWorkspaceQuery(workspaceId, QUERY_STRING, null);
        logsBatchQuery.addWorkspaceQuery(workspaceId, QUERY_STRING, null,
            new LogsQueryOptions().setIncludeStatistics(true));

        LogsBatchQueryResultCollection batchResultCollection = client
                .queryBatchWithResponse(logsBatchQuery, Context.NONE).getValue();

        List<LogsBatchQueryResult> responses = batchResultCollection.getBatchResults();

        assertEquals(2, responses.size());
        assertEquals(LogsQueryResultStatus.SUCCESS, responses.get(0).getQueryResultStatus());
        assertNull(responses.get(0).getError());
        assertNull(responses.get(0).getStatistics());
        assertEquals(LogsQueryResultStatus.SUCCESS, responses.get(1).getQueryResultStatus());
        assertNull(responses.get(1).getError());
        assertNotNull(responses.get(1).getStatistics());
    }

    @Test
    public void testServerTimeout() {
        // Server timeout is not readily reproducible and because the service caches query results, the queries that require extended time
        // to complete if run the first time can return immediately if a cached result is available. So, instead of testing the server behavior,
        // this test validates that the request is sent with the correct timeout value in the Prefer header.
        LogsQueryClientBuilder clientBuilder = new LogsQueryClientBuilder();
        if (getTestMode() == TestMode.PLAYBACK) {
            clientBuilder
                .credential(request -> Mono.just(new AccessToken("fakeToken", OffsetDateTime.now().plusDays(1))))
                .httpClient(getAssertingHttpClient(interceptorManager.getPlaybackClient()));
        } else if (getTestMode() == TestMode.RECORD) {
            clientBuilder
                .addPolicy(interceptorManager.getRecordPolicy())
                .credential(getCredential());
        } else if (getTestMode() == TestMode.LIVE) {
            clientBuilder.credential(getCredential());
            clientBuilder.endpoint(MonitorQueryTestUtils.getLogEndpoint());
        }
        LogsQueryClient client = clientBuilder
            .addPolicy((context, next) -> {
                Assertions.assertTrue(context.getHttpRequest().getHeaders().get(HttpHeaderName.fromString("Prefer")).getValue().contains("wait=5"));
                return next.process();
            })
            .buildClient();
        long count = 5;
        client.queryWorkspaceWithResponse(workspaceId, "range x from 1 to " + count + " step 1 | count", null,
            new LogsQueryOptions().setServerTimeout(Duration.ofSeconds(5)), Context.NONE);
    }

    @Test
    public void testVisualization() {
        String query = "datatable (s: string, i: long) [ \"a\", 1, \"b\", 2, \"c\", 3 ] "
                + "| render columnchart with (title=\"the chart title\", xtitle=\"the x axis title\")";
        LogsQueryResult queryResults = client.queryWorkspaceWithResponse(workspaceId,
            query, null, new LogsQueryOptions().setIncludeStatistics(true).setIncludeVisualization(true),
            Context.NONE).getValue();

        assertEquals(1, queryResults.getAllTables().size());
        assertNotNull(queryResults.getVisualization());

        BinaryData visualization = queryResults.getVisualization();

        try (JsonReader reader = JsonProviders.createReader(visualization.toStream())) {
            Map<String, Object> map = reader.readMap(innerReader -> {
                return reader.readUntyped();
            });
            String title = map.get("title").toString();
            String xTitle = map.get("xTitle").toString();

            assertEquals("the chart title", title);
            assertEquals("the x axis title", xTitle);
        } catch (IOException e) {
            Assertions.fail("Failed to read the visualization data.");
        }

        LinkedHashMap<String, Object> linkedHashMap =
            queryResults.getVisualization().toObject(new TypeReference<LinkedHashMap<String, Object>>() {
            });
        String title = linkedHashMap.get("title").toString();
        String xTitle = linkedHashMap.get("xTitle").toString();

        assertEquals("the chart title", title);
        assertEquals("the x axis title", xTitle);
    }

    @Test
    public void testVisualizationResourceQuery() {
        String query = "datatable (s: string, i: long) [ \"a\", 1, \"b\", 2, \"c\", 3 ] "
            + "| render columnchart with (title=\"the chart title\", xtitle=\"the x axis title\")";
        LogsQueryResult queryResults = client.queryResourceWithResponse(resourceId,
            query, null, new LogsQueryOptions().setIncludeStatistics(true).setIncludeVisualization(true),
            Context.NONE).getValue();
        assertEquals(1, queryResults.getAllTables().size());
        assertNotNull(queryResults.getVisualization());

        BinaryData visualization = queryResults.getVisualization();

        try (JsonReader reader = JsonProviders.createReader(visualization.toStream())) {
            Map<String, Object> map = reader.readMap(innerReader -> {
                return reader.readUntyped();
            });
            String title = map.get("title").toString();
            String xTitle = map.get("xTitle").toString();

            assertEquals("the chart title", title);
            assertEquals("the x axis title", xTitle);
        } catch (IOException e) {
            Assertions.fail("Failed to read the visualization data.");
        }

        LinkedHashMap<String, Object> linkedHashMap =
            queryResults.getVisualization().toObject(new TypeReference<LinkedHashMap<String, Object>>() {
            });
        String title = linkedHashMap.get("title").toString();
        String xTitle = linkedHashMap.get("xTitle").toString();

        assertEquals("the chart title", title);
        assertEquals("the x axis title", xTitle);
    }
}
