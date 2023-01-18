// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.RetryStrategy;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.monitor.ingestion.models.UploadLogsResult;
import com.azure.monitor.ingestion.models.UploadLogsStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test cases for {@link LogsIngestionClient}.
 */
public class LogsIngestionClientTest extends TestBase {
    private LogsIngestionClientBuilder clientBuilder;
    private String dataCollectionEndpoint;
    private String dataCollectionRuleId;
    private String streamName;

    @BeforeEach
    public void setup() {
        dataCollectionEndpoint = Configuration.getGlobalConfiguration().get("AZURE_MONITOR_DCE", "https://dce.monitor.azure.com");
        dataCollectionRuleId = Configuration.getGlobalConfiguration().get("AZURE_MONITOR_DCR_ID", "dcr-adec84661d05465f8532f32a04af6f98");
        streamName = "Custom-MyTableRawData";

        LogsIngestionClientBuilder clientBuilder = new LogsIngestionClientBuilder()
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
        this.clientBuilder = clientBuilder
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                .endpoint(dataCollectionEndpoint);
    }

    private TokenCredential getCredential() {
        return new ClientSecretCredentialBuilder()
                .clientId(Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_CLIENT_ID))
                .clientSecret(Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_CLIENT_SECRET))
                .tenantId(Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_TENANT_ID))
                .build();
    }

    @Test
    public void testUploadLogs() {
        List<Object> logs = getObjects(10);
        LogsIngestionClient client = clientBuilder.buildClient();
        UploadLogsResult result = client.upload(dataCollectionRuleId, streamName, logs);
        assertEquals(UploadLogsStatus.SUCCESS, result.getStatus());
    }

    @Test
    public void testUploadLogsAsync() {
        List<Object> logs = getObjects(10);
        LogsIngestionAsyncClient client = clientBuilder.buildAsyncClient();
        StepVerifier.create(client.upload(dataCollectionRuleId, streamName, logs))
            .assertNext(result -> assertEquals(UploadLogsStatus.SUCCESS, result.getStatus()))
            .verifyComplete();
    }

    @Test
    public void testUploadLogsInBatches() {
        List<Object> logs = getObjects(10000);

        AtomicInteger count = new AtomicInteger();
        LogsIngestionClient client = clientBuilder
                .addPolicy(new BatchCountPolicy(count))
                .buildClient();
        UploadLogsResult result = client.upload(dataCollectionRuleId, streamName, logs);
        assertEquals(UploadLogsStatus.SUCCESS, result.getStatus());
        assertEquals(2, count.get());
    }

    @Test
    public void testUploadLogsInBatchesAsync() {
        List<Object> logs = getObjects(10000);

        AtomicInteger count = new AtomicInteger();
        LogsIngestionAsyncClient client = clientBuilder
            .addPolicy(new BatchCountPolicy(count))
            .buildAsyncClient();

        StepVerifier.create(client.upload(dataCollectionRuleId, streamName, logs))
            .assertNext(result -> assertEquals(UploadLogsStatus.SUCCESS, result.getStatus()))
            .verifyComplete();

        assertEquals(2, count.get());
    }

    @Test
    public void testUploadLogsPartialFailure() {
        List<Object> logs = getObjects(100000);
        AtomicInteger count = new AtomicInteger();

        LogsIngestionClient client = clientBuilder
                .addPolicy(new PartialFailurePolicy(count))
                .buildClient();

        UploadLogsResult result = client.upload(dataCollectionRuleId, streamName, logs);
        assertEquals(UploadLogsStatus.PARTIAL_FAILURE, result.getStatus());
        assertEquals(11, count.get());
        assertEquals(5, result.getErrors().size());
        result.getErrors().stream().forEach(error -> assertEquals("NotFound", error.getResponseError().getCode()));
    }

    @Test
    public void testUploadLogsPartialFailureAsync() {
        List<Object> logs = getObjects(100000);
        AtomicInteger count = new AtomicInteger();

        LogsIngestionAsyncClient client = clientBuilder
            .addPolicy(new PartialFailurePolicy(count))
            .buildAsyncClient();

        StepVerifier.create(client.upload(dataCollectionRuleId, streamName, logs))
            .assertNext(result -> {
                assertEquals(UploadLogsStatus.PARTIAL_FAILURE, result.getStatus());
                assertEquals(5, result.getErrors().size());
                result.getErrors().stream().forEach(error -> assertEquals("NotFound", error.getResponseError().getCode()));
            })
            .verifyComplete();

        assertEquals(11, count.get());
    }

    @Test
    public void testUploadLogsProtocolMethod() {
        List<Object> logs = getObjects(10);
        LogsIngestionClient client = clientBuilder.buildClient();
        Response<Void> response = client.uploadWithResponse(dataCollectionRuleId, streamName,
                BinaryData.fromObject(logs), new RequestOptions().setHeader("Content-Encoding", "gzip"));
        assertEquals(204, response.getStatusCode());
    }

    @Test
    public void testUploadLogsProtocolMethodAsync() {
        List<Object> logs = getObjects(10);
        LogsIngestionAsyncClient client = clientBuilder.buildAsyncClient();
        StepVerifier.create(client.uploadWithResponse(dataCollectionRuleId, streamName,
            BinaryData.fromObject(logs), new RequestOptions().setHeader("Content-Encoding", "gzip")))
                .assertNext(response -> assertEquals(204, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void testUploadLargeLogsProtocolMethod() {
        List<Object> logs = getObjects(1000000);
        LogsIngestionClient client = clientBuilder.buildClient();

        HttpResponseException responseException = assertThrows(HttpResponseException.class,
                () -> client.uploadWithResponse(dataCollectionRuleId, streamName, BinaryData.fromObject(logs),
                        new RequestOptions()));
        assertEquals(413, responseException.getResponse().getStatusCode());
    }

    @Test
    public void testUploadLargeLogsProtocolMethodAsync() {
        List<Object> logs = getObjects(1000000);
        LogsIngestionAsyncClient client = clientBuilder.buildAsyncClient();
        StepVerifier.create(client.uploadWithResponse(dataCollectionRuleId, streamName,
                BinaryData.fromObject(logs), new RequestOptions()))
            .verifyErrorMatches(responseException -> (responseException instanceof HttpResponseException)
                && ((HttpResponseException) responseException).getResponse().getStatusCode() == 413);
    }

    private List<Object> getObjects(int logsCount) {
        List<Object> logs = new ArrayList<>();

        for (int i = 0; i < logsCount; i++) {
            LogData logData = new LogData()
                    .setTime(OffsetDateTime.parse("2022-01-01T00:00:00+07:00"))
                    .setExtendedColumn("test" + i)
                    .setAdditionalContext("additional logs context");
            logs.add(logData);
        }
        return logs;
    }

    private static class BatchCountPolicy implements HttpPipelinePolicy {
        private final AtomicInteger counter;

        BatchCountPolicy(AtomicInteger counter) {
            this.counter = counter;
        }

        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            counter.incrementAndGet();
            return next.process();
        }

        @Override
        public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
            counter.incrementAndGet();
            return next.processSync();
        }

        @Override
        public HttpPipelinePosition getPipelinePosition() {
            return HttpPipelinePosition.PER_CALL;
        }
    }

    private class PartialFailurePolicy implements HttpPipelinePolicy {
        private final AtomicInteger counter;
        private final AtomicBoolean changeDcrId = new AtomicBoolean();

        PartialFailurePolicy(AtomicInteger counter) {
            this.counter = counter;
        }

        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            process(context);
            return next.process();
        }

        @Override
        public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
            process(context);
            return next.processSync();
        }

        private void process(HttpPipelineCallContext context) {
            counter.incrementAndGet();
            if (changeDcrId.get()) {
                String url = context.getHttpRequest().getUrl().toString()
                    .replace(dataCollectionRuleId, "dcr-id");
                context.getHttpRequest().setUrl(url);
                changeDcrId.set(false);
            } else {
                changeDcrId.set(true);
            }
        }
        @Override
        public HttpPipelinePosition getPipelinePosition() {
            return HttpPipelinePosition.PER_CALL;
        }
    }
}
