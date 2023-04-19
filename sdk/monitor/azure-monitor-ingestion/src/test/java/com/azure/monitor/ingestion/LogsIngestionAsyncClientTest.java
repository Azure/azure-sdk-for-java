// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion;

import com.azure.core.credential.AccessToken;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.RetryStrategy;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.test.TestMode;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.monitor.ingestion.models.LogsUploadException;
import com.azure.monitor.ingestion.models.LogsUploadOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link LogsIngestionAsyncClient}.
 */
public class LogsIngestionAsyncClientTest extends LogsIngestionTestBase {

    @BeforeEach
    public void beforeTest() {
        dataCollectionEndpoint = Configuration.getGlobalConfiguration().get("AZURE_MONITOR_DCE", "https://dce.monitor.azure.com");
        dataCollectionRuleId = Configuration.getGlobalConfiguration().get("AZURE_MONITOR_DCR_ID", "dcr-a64851bc17714f0483d1e96b5d84953b");
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
    @Test
    public void testUploadLogs() {
        System.out.println(getTestMode());
        List<Object> logs = getObjects(10);
        LogsIngestionAsyncClient client = clientBuilder.buildAsyncClient();
        StepVerifier.create(client.upload(dataCollectionRuleId, streamName, logs))
                .verifyComplete();
    }

    @Test
    @Disabled
    public void testUploadLogsInBatches() {
        List<Object> logs = getObjects(10000);

        AtomicInteger count = new AtomicInteger();
        LogsIngestionAsyncClient client = clientBuilder
                .addPolicy(new BatchCountPolicy(count))
                .buildAsyncClient();

        StepVerifier.create(client.upload(dataCollectionRuleId, streamName, logs))
                .verifyComplete();

        assertEquals(2, count.get());
    }

    @Test
    public void testUploadLogsPartialFailure() {
        List<Object> logs = getObjects(100000);
        AtomicInteger count = new AtomicInteger();

        LogsIngestionAsyncClient client = clientBuilder
                .addPolicy(new PartialFailurePolicy(count))
                .buildAsyncClient();

        StepVerifier.create(client.upload(dataCollectionRuleId, streamName, logs))
                .verifyErrorSatisfies(error -> {
                    assertTrue(error instanceof LogsUploadException);
                    if (error instanceof LogsUploadException) {
                        LogsUploadException ex = (LogsUploadException) error;
                        assertEquals(49460, ex.getFailedLogsCount());
                        assertEquals(5, ex.getLogsUploadErrors().size());
                    }
                });
    }

    @Test
    @Disabled
    public void testUploadLogsPartialFailureWithErrorHandler() {
        List<Object> logs = getObjects(100000);
        AtomicInteger count = new AtomicInteger();
        AtomicLong failedLogsCount = new AtomicLong();
        LogsUploadOptions logsUploadOptions = new LogsUploadOptions()
                .setLogsUploadErrorConsumer(error -> failedLogsCount.addAndGet(error.getFailedLogs().size()));

        LogsIngestionAsyncClient client = clientBuilder
                .addPolicy(new PartialFailurePolicy(count))
                .buildAsyncClient();

        StepVerifier.create(client.upload(dataCollectionRuleId, streamName, logs, logsUploadOptions))
                .verifyComplete();
        assertEquals(49460, failedLogsCount.get());
        assertEquals(11, count.get());
    }

    @Test
    @Disabled
    public void testUploadLogsStopOnFirstError() {
        List<Object> logs = getObjects(100000);
        AtomicInteger count = new AtomicInteger();
        LogsUploadOptions logsUploadOptions = new LogsUploadOptions()
                .setLogsUploadErrorConsumer(error -> {
                    // throw on first error
                    throw error.getResponseException();
                });

        LogsIngestionAsyncClient client = clientBuilder
                .addPolicy(new PartialFailurePolicy(count))
                .buildAsyncClient();

        StepVerifier.create(client.upload(dataCollectionRuleId, streamName, logs, logsUploadOptions))
                .verifyErrorSatisfies(ex -> assertTrue(ex instanceof HttpResponseException));
        assertEquals(2, count.get());
    }

    @Test
    @Disabled
    public void testUploadLogsProtocolMethod() {
        List<Object> logs = getObjects(10);
        LogsIngestionAsyncClient client = clientBuilder.buildAsyncClient();
        StepVerifier.create(client.uploadWithResponse(dataCollectionRuleId, streamName,
                        BinaryData.fromObject(logs), new RequestOptions()))
                .assertNext(response -> assertEquals(204, response.getStatusCode()))
                .verifyComplete();
    }

    @Test
    @Disabled
    public void testUploadLargeLogsProtocolMethod() {
        List<Object> logs = getObjects(1000000);
        LogsIngestionAsyncClient client = clientBuilder.buildAsyncClient();
        StepVerifier.create(client.uploadWithResponse(dataCollectionRuleId, streamName,
                        BinaryData.fromObject(logs), new RequestOptions()))
                .verifyErrorMatches(responseException -> (responseException instanceof HttpResponseException)
                        && ((HttpResponseException) responseException).getResponse().getStatusCode() == 413);
    }

}
