// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.monitor.ingestion.models.LogsUploadOptions;
import com.azure.monitor.ingestion.models.LogsUploadException;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link LogsIngestionAsyncClient}.
 */
public class LogsIngestionAsyncClientTest extends LogsIngestionTestBase {

    @Test
    public void testUploadLogs() {
        List<Object> logs = getObjects(10);
        LogsIngestionAsyncClient client = clientBuilder.buildAsyncClient();
        StepVerifier.create(client.upload(dataCollectionRuleId, streamName, logs))
                .verifyComplete();
    }

    @Test
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
    public void testUploadLogsPartialFailureWithErrorHandler() {
        List<Object> logs = getObjects(100000);
        AtomicInteger count = new AtomicInteger();
        AtomicLong failedLogsCount = new AtomicLong();
        LogsUploadOptions uploadLogsOptions = new LogsUploadOptions()
                .setLogsUploadErrorConsumer(error -> failedLogsCount.addAndGet(error.getFailedLogs().size()));

        LogsIngestionAsyncClient client = clientBuilder
                .addPolicy(new PartialFailurePolicy(count))
                .buildAsyncClient();

        StepVerifier.create(client.upload(dataCollectionRuleId, streamName, logs, uploadLogsOptions))
                .verifyComplete();
        assertEquals(49460, failedLogsCount.get());
        assertEquals(11, count.get());
    }

    @Test
    public void testUploadLogsStopOnFirstError() {
        List<Object> logs = getObjects(100000);
        AtomicInteger count = new AtomicInteger();
        LogsUploadOptions uploadLogsOptions = new LogsUploadOptions()
                .setLogsUploadErrorConsumer(error -> {
                    // throw on first error
                    throw error.getResponseException();
                });

        LogsIngestionAsyncClient client = clientBuilder
                .addPolicy(new PartialFailurePolicy(count))
                .buildAsyncClient();

        StepVerifier.create(client.upload(dataCollectionRuleId, streamName, logs, uploadLogsOptions))
                .verifyErrorSatisfies(ex -> assertTrue(ex instanceof HttpResponseException));
        assertEquals(2, count.get());
    }

    @Test
    public void testUploadLogsProtocolMethod() {
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
        LogsIngestionAsyncClient client = clientBuilder.buildAsyncClient();
        StepVerifier.create(client.uploadWithResponse(dataCollectionRuleId, streamName,
                        BinaryData.fromObject(logs), new RequestOptions()))
                .verifyErrorMatches(responseException -> (responseException instanceof HttpResponseException)
                        && ((HttpResponseException) responseException).getResponse().getStatusCode() == 413);
    }

}
