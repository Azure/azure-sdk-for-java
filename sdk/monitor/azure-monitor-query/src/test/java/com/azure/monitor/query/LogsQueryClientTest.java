// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.monitor.query.models.LogsBatchQuery;
import com.azure.monitor.query.models.LogsBatchQueryResult;
import com.azure.monitor.query.models.LogsBatchQueryResultCollection;
import com.azure.monitor.query.models.LogsQueryOptions;
import com.azure.monitor.query.models.LogsQueryResult;
import com.azure.monitor.query.models.QueryTimeSpan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link LogsQueryClient}
 */
public class LogsQueryClientTest extends TestBase {

    public static final String WORKSPACE_ID = Configuration.getGlobalConfiguration()
            .get("AZURE_MONITOR_LOGS_WORKSPACE_ID", "d2d0e126-fa1e-4b0a-b647-250cdd471e68");
    private LogsQueryClient client;

    @BeforeEach
    public void setup() {
        LogsQueryClientBuilder clientBuilder = new LogsQueryClientBuilder();
        if (getTestMode() == TestMode.PLAYBACK) {
            clientBuilder
                .credential(request -> Mono.just(new AccessToken("fakeToken", OffsetDateTime.now().plusDays(1))))
                .httpClient(interceptorManager.getPlaybackClient());
        } else if (getTestMode() == TestMode.RECORD) {
            clientBuilder
                .addPolicy(interceptorManager.getRecordPolicy())
                .credential(getCredential());
        } else if (getTestMode() == TestMode.LIVE) {
            clientBuilder.credential(new DefaultAzureCredentialBuilder().build());
        }
        this.client = clientBuilder
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                .buildClient();
    }

    private TokenCredential getCredential() {
        return new ClientSecretCredentialBuilder()
            .clientId(Configuration.getGlobalConfiguration().get("AZURE_MONITOR_CLIENT_ID"))
            .clientSecret(Configuration.getGlobalConfiguration().get("AZURE_MONITOR_CLIENT_SECRET"))
            .tenantId(Configuration.getGlobalConfiguration().get("AZURE_TENANT_ID"))
            .build();
    }

    @Test
    public void testLogsQuery() {
        LogsQueryResult queryResults = client.queryLogs(WORKSPACE_ID, "AppRequests",
                new QueryTimeSpan(OffsetDateTime.of(LocalDateTime.of(2021, 01, 01, 0, 0), ZoneOffset.UTC),
                        OffsetDateTime.of(LocalDateTime.of(2021, 06, 10, 0, 0), ZoneOffset.UTC)));
        assertEquals(1, queryResults.getLogsTables().size());
        assertEquals(902, queryResults.getLogsTables().get(0).getAllTableCells().size());
        assertEquals(22, queryResults.getLogsTables().get(0).getTableRows().size());
    }

    @Test
    public void testLogsQueryBatch() {
        LogsBatchQuery logsBatchQuery = new LogsBatchQuery()
                .addQuery(WORKSPACE_ID, "AppRequests | take 2", null)
                .addQuery(WORKSPACE_ID, "AppRequests | take 3", null);

        LogsBatchQueryResultCollection batchResultCollection = client
            .queryLogsBatchWithResponse(logsBatchQuery, Context.NONE).getValue();

        List<LogsBatchQueryResult> responses = batchResultCollection.getBatchResults();

        assertEquals(2, responses.size());

        assertEquals(1, responses.get(0).getQueryResult().getLogsTables().size());
        assertEquals(82, responses.get(0).getQueryResult().getLogsTables().get(0).getAllTableCells().size());
        assertEquals(2, responses.get(0).getQueryResult().getLogsTables().get(0).getTableRows().size());

        assertEquals(1, responses.get(1).getQueryResult().getLogsTables().size());
        assertEquals(123, responses.get(1).getQueryResult().getLogsTables().get(0).getAllTableCells().size());
        assertEquals(3, responses.get(1).getQueryResult().getLogsTables().get(0).getTableRows().size());
    }

    @Test
    public void testMultipleWorkspaces() {
        LogsQueryResult queryResults = client.queryLogsWithResponse(
                new LogsQueryOptions(WORKSPACE_ID,
                        "union * | where TimeGenerated > ago(100d) | project TenantId | summarize count() by TenantId",
                        null)
                        .setWorkspaceIds(Arrays.asList("9dad0092-fd13-403a-b367-a189a090a541")), Context.NONE)
                .getValue();
        assertEquals(1, queryResults.getLogsTables().size());
        assertEquals(2, queryResults
                .getLogsTables()
                .get(0)
                .getTableRows()
                .stream()
                .map(row -> {
                    System.out.println(row.getColumnValue("TenantId").get().getValueAsString());
                    return row.getColumnValue("TenantId").get();
                })
                .distinct()
                .count());
    }

    @Test
    public void testBatchQueryPartialSuccess() {
        LogsBatchQuery logsBatchQuery = new LogsBatchQuery()
                .addQuery(WORKSPACE_ID, "AppRequests | take 2", null)
                .addQuery(WORKSPACE_ID, "AppRequests | take", null);

        LogsBatchQueryResultCollection batchResultCollection = client
                .queryLogsBatchWithResponse(logsBatchQuery, Context.NONE).getValue();

        List<LogsBatchQueryResult> responses = batchResultCollection.getBatchResults();

        assertEquals(2, responses.size());
        assertEquals(200, responses.get(0).getStatus());
        assertNotNull(responses.get(0).getQueryResult());
        assertNull(responses.get(0).getQueryResult().getError());
        assertEquals(400, responses.get(1).getStatus());
        assertNotNull(responses.get(1).getQueryResult().getError());
        assertEquals("BadArgumentError", responses.get(1).getQueryResult().getError().getCode());
    }

    @Test
    public void testStatistics() {
        LogsQueryResult queryResults = client.queryLogsWithResponse(new LogsQueryOptions(WORKSPACE_ID,
                "AppRequests", null).setIncludeStatistics(true), Context.NONE).getValue();

        assertEquals(1, queryResults.getLogsTables().size());
        assertNotNull(queryResults.getStatistics());
    }

    @Test
    public void testBatchStatistics() {
        LogsBatchQuery logsBatchQuery = new LogsBatchQuery()
                .addQuery(WORKSPACE_ID, "AppRequests | take 2", null)
                .addQuery(new LogsQueryOptions(WORKSPACE_ID, "AppRequests | take 2", null).setIncludeStatistics(true));

        LogsBatchQueryResultCollection batchResultCollection = client
                .queryLogsBatchWithResponse(logsBatchQuery, Context.NONE).getValue();

        List<LogsBatchQueryResult> responses = batchResultCollection.getBatchResults();

        assertEquals(2, responses.size());
        assertEquals(200, responses.get(0).getStatus());
        assertNotNull(responses.get(0).getQueryResult());
        assertNull(responses.get(0).getQueryResult().getError());
        assertNull(responses.get(0).getQueryResult().getStatistics());
        assertEquals(200, responses.get(1).getStatus());
        assertNotNull(responses.get(1).getQueryResult());
        assertNull(responses.get(1).getQueryResult().getError());
        assertNotNull(responses.get(1).getQueryResult().getStatistics());
    }
}
