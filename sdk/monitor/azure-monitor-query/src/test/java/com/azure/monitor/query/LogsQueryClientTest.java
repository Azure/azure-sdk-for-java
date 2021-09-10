// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.experimental.models.TimeInterval;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.RetryStrategy;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.monitor.query.models.LogsBatchQuery;
import com.azure.monitor.query.models.LogsBatchQueryResult;
import com.azure.monitor.query.models.LogsBatchQueryResultCollection;
import com.azure.monitor.query.models.LogsQueryOptions;
import com.azure.monitor.query.models.LogsQueryResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

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
                    .httpClient(interceptorManager.getPlaybackClient());
        } else if (getTestMode() == TestMode.RECORD) {
            clientBuilder
                    .addPolicy(interceptorManager.getRecordPolicy())
                    .credential(getCredential());
        } else if (getTestMode() == TestMode.LIVE) {
            clientBuilder.credential(getCredential());
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
        LogsQueryResult queryResults = client.query(WORKSPACE_ID, "AppRequests",
                new TimeInterval(OffsetDateTime.of(LocalDateTime.of(2021, 01, 01, 0, 0), ZoneOffset.UTC),
                        OffsetDateTime.of(LocalDateTime.of(2021, 06, 10, 0, 0), ZoneOffset.UTC)));
        assertEquals(1, queryResults.getAllTables().size());
        assertEquals(902, queryResults.getAllTables().get(0).getAllTableCells().size());
        assertEquals(22, queryResults.getAllTables().get(0).getRows().size());
    }

    @Test
    public void testLogsQueryBatch() {
        LogsBatchQuery logsBatchQuery = new LogsBatchQuery();
        logsBatchQuery.addQuery(WORKSPACE_ID, "AppRequests | take 2", null);
        logsBatchQuery.addQuery(WORKSPACE_ID, "AppRequests | take 3", null);

        LogsBatchQueryResultCollection batchResultCollection = client
                .queryBatchWithResponse(logsBatchQuery, Context.NONE).getValue();

        List<LogsBatchQueryResult> responses = batchResultCollection.getBatchResults();

        assertEquals(2, responses.size());

        assertEquals(1, responses.get(0).getAllTables().size());
        assertEquals(82, responses.get(0).getAllTables().get(0).getAllTableCells().size());
        assertEquals(2, responses.get(0).getAllTables().get(0).getRows().size());

        assertEquals(1, responses.get(1).getAllTables().size());
        assertEquals(123, responses.get(1).getAllTables().get(0).getAllTableCells().size());
        assertEquals(3, responses.get(1).getAllTables().get(0).getRows().size());
    }

    @Test
    public void testMultipleWorkspaces() {
        LogsQueryResult queryResults = client.queryWithResponse(WORKSPACE_ID,
                "union * | where TimeGenerated > ago(100d) | project TenantId | summarize count() by TenantId", null,
                new LogsQueryOptions()
                        .setAdditionalWorkspaces(Arrays.asList("9dad0092-fd13-403a-b367-a189a090a541")), Context.NONE)
                .getValue();
        assertEquals(1, queryResults.getAllTables().size());
        assertEquals(2, queryResults
                .getAllTables()
                .get(0)
                .getRows()
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
        LogsBatchQuery logsBatchQuery = new LogsBatchQuery();
        logsBatchQuery.addQuery(WORKSPACE_ID, "AppRequests | take 2", null);
        logsBatchQuery.addQuery(WORKSPACE_ID, "AppRequests | take", null);

        LogsBatchQueryResultCollection batchResultCollection = client
                .queryBatchWithResponse(logsBatchQuery, Context.NONE).getValue();

        List<LogsBatchQueryResult> responses = batchResultCollection.getBatchResults();

        assertEquals(2, responses.size());
        assertEquals(200, responses.get(0).getStatus());
        assertNull(responses.get(0).getError());
        assertEquals(400, responses.get(1).getStatus());
        assertNotNull(responses.get(1).getError());
        assertEquals("BadArgumentError", responses.get(1).getError().getCode());
    }

    @Test
    public void testStatistics() {
        LogsQueryResult queryResults = client.queryWithResponse(WORKSPACE_ID,
                "AppRequests", null, new LogsQueryOptions().setIncludeStatistics(true), Context.NONE).getValue();

        assertEquals(1, queryResults.getAllTables().size());
        assertNotNull(queryResults.getStatistics());
    }

    @Test
    public void testBatchStatistics() {
        LogsBatchQuery logsBatchQuery = new LogsBatchQuery();
        logsBatchQuery.addQuery(WORKSPACE_ID, "AppRequests | take 2", null);
        logsBatchQuery.addQuery(WORKSPACE_ID, "AppRequests | take 2", null,
                        new LogsQueryOptions().setIncludeStatistics(true));

        LogsBatchQueryResultCollection batchResultCollection = client
                .queryBatchWithResponse(logsBatchQuery, Context.NONE).getValue();

        List<LogsBatchQueryResult> responses = batchResultCollection.getBatchResults();

        assertEquals(2, responses.size());
        assertEquals(200, responses.get(0).getStatus());
        assertNull(responses.get(0).getError());
        assertNull(responses.get(0).getStatistics());
        assertEquals(200, responses.get(1).getStatus());
        assertNull(responses.get(1).getError());
        assertNotNull(responses.get(1).getStatistics());
    }

    @Test
    public void testServerTimeout() {
        // The server does not always stop processing the request and return a 504 before the client times out
        // so, retry until a 504 response is returned
        Random random = new Random();
        while (true) {
            // add some random number to circumvent cached response from server
            long count = 1000000000000L + random.nextInt(10000);
            try {
                // this query should take more than 5 seconds usually, but the server may have cached the
                // response and may return before 5 seconds. So, retry with another query (different count value)
                client.queryWithResponse(WORKSPACE_ID, "range x from 1 to " + count + " step 1 | count", null,
                        new LogsQueryOptions()
                                .setServerTimeout(Duration.ofSeconds(5)),
                        Context.NONE);
            } catch (Exception exception) {
                if (exception instanceof HttpResponseException) {
                    HttpResponseException logsQueryException = (HttpResponseException) exception;
                    if (logsQueryException.getResponse().getStatusCode() == 504) {
                        break;
                    }
                }
            }
        }
    }
}
