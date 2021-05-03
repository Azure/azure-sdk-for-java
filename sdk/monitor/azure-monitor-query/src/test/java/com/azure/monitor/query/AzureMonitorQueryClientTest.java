// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.rest.Response;
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
import com.azure.monitor.query.models.Metrics;
import com.azure.monitor.query.models.MetricsQueryOptions;
import com.azure.monitor.query.models.MetricsQueryResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link AzureMonitorQueryClient}
 */
public class AzureMonitorQueryClientTest extends TestBase {

    private AzureMonitorQueryClient client;

    @BeforeEach
    public void setup() {
        AzureMonitorQueryClientBuilder clientBuilder = new AzureMonitorQueryClientBuilder();
        if (getTestMode() == TestMode.PLAYBACK) {
            clientBuilder
                .credential(new TokenCredential() {
                    @Override
                    public Mono<AccessToken> getToken(TokenRequestContext request) {
                        return Mono.just(new AccessToken("fakeToken", OffsetDateTime.now().plusDays(1)));
                    }
                })
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

    @Test
    public void testMetricsQuery() {
        Response<MetricsQueryResult> metricsResponse = client
            .queryMetricsWithResponse(
                "/subscriptions/faa080af-c1d8-40ad-9cce-e1a450ca5b57/resourceGroups/srnagar-azuresdkgroup/providers/"
                    + "Microsoft.CognitiveServices/accounts/srnagara-textanalytics",
                Arrays.asList("SuccessfulCalls"),
                new MetricsQueryOptions()
                    .setMetricsNamespace("Microsoft.CognitiveServices/accounts")
                    .setTimespan(Duration.ofDays(30).toString())
                    .setInterval(Duration.ofHours(1))
                    .setTop(100)
                    .setAggregation("1,2,3,4,5"),
                Context.NONE);

        MetricsQueryResult metricsQueryResult = metricsResponse.getValue();
        List<Metrics> metrics = metricsQueryResult.getMetrics();

        assertEquals(1, metrics.size());
        Metrics successfulCallsMetrics = metrics.get(0);
        assertEquals("SuccessfulCalls", successfulCallsMetrics.getMetricsName());
        assertEquals("Microsoft.Insights/metrics", successfulCallsMetrics.getType());
        assertEquals(1, successfulCallsMetrics.getTimeSeries().size());
        assertEquals(720, successfulCallsMetrics.getTimeSeries().get(0).getData().size());

        Assertions.assertTrue(successfulCallsMetrics.getTimeSeries()
            .stream()
            .flatMap(timeSeriesElement -> timeSeriesElement.getData().stream())
            .anyMatch(metricsValue -> Double.compare(0.0, metricsValue.getAverage().doubleValue()) != 0));

        Assertions.assertTrue(successfulCallsMetrics.getTimeSeries()
            .stream()
            .flatMap(timeSeriesElement -> timeSeriesElement.getData().stream())
            .anyMatch(metricsValue -> Double.compare(0.0, metricsValue.getMaximum().doubleValue()) == 0));

        Assertions.assertTrue(successfulCallsMetrics.getTimeSeries()
            .stream()
            .flatMap(timeSeriesElement -> timeSeriesElement.getData().stream())
            .anyMatch(metricsValue -> Double.compare(0.0, metricsValue.getMinimum().doubleValue()) == 0));

        Assertions.assertTrue(successfulCallsMetrics.getTimeSeries()
            .stream()
            .flatMap(timeSeriesElement -> timeSeriesElement.getData().stream())
            .anyMatch(metricsValue -> Double.compare(0.0, metricsValue.getCount().doubleValue()) == 0));

        Assertions.assertTrue(successfulCallsMetrics.getTimeSeries()
            .stream()
            .flatMap(timeSeriesElement -> timeSeriesElement.getData().stream())
            .anyMatch(metricsValue -> Double.compare(0.0, metricsValue.getTotal().doubleValue()) == 0));
    }
}
