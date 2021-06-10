// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.monitor.query.models.LogsQueryBatch;
import com.azure.monitor.query.models.LogsQueryBatchResult;
import com.azure.monitor.query.models.LogsQueryBatchResultCollection;
import com.azure.monitor.query.models.LogsQueryResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link LogsClient}
 */
public class LogsClientTest extends TestBase {

    private LogsClient client;

    @BeforeEach
    public void setup() {
        LogsClientBuilder clientBuilder = new LogsClientBuilder();
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
        this.client = clientBuilder.buildClient();
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
        LogsQueryResult queryResults = client.queryLogs("d2d0e126-fa1e-4b0a-b647-250cdd471e68", "AppRequests", null);
        assertEquals(1, queryResults.getLogsTables().size());
        assertEquals(1148, queryResults.getLogsTables().get(0).getAllTableCells().size());
        assertEquals(28, queryResults.getLogsTables().get(0).getTableRows().size());
    }

    @Test
    public  void testLogsQueryBatch() {
        LogsQueryBatch logsQueryBatch = new LogsQueryBatch()
            .addQuery("d2d0e126-fa1e-4b0a-b647-250cdd471e68", "AppRequests | take 2", null)
            .addQuery("d2d0e126-fa1e-4b0a-b647-250cdd471e68", "AppRequests | take 3", null);

        LogsQueryBatchResultCollection batchResultCollection = client
            .queryLogsBatchWithResponse(logsQueryBatch, Context.NONE).getValue();

        List<LogsQueryBatchResult> responses = batchResultCollection.getBatchResults();

        assertEquals(2, responses.size());

        assertEquals(1, responses.get(0).getQueryResult().getLogsTables().size());
        assertEquals(82, responses.get(0).getQueryResult().getLogsTables().get(0).getAllTableCells().size());
        assertEquals(2, responses.get(0).getQueryResult().getLogsTables().get(0).getTableRows().size());

        assertEquals(1, responses.get(1).getQueryResult().getLogsTables().size());
        assertEquals(123, responses.get(1).getQueryResult().getLogsTables().get(0).getAllTableCells().size());
        assertEquals(3, responses.get(1).getQueryResult().getLogsTables().get(0).getTableRows().size());
    }
}
