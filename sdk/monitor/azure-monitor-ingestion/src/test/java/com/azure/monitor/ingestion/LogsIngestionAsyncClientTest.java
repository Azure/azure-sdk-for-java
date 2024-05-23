// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.test.annotation.RecordWithoutRequestBody;
import com.azure.core.util.BinaryData;
import com.azure.monitor.ingestion.models.LogsUploadException;
import com.azure.monitor.ingestion.models.LogsUploadOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
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
        DataValidationPolicy dataValidationPolicy = new DataValidationPolicy(logs);

        LogsIngestionAsyncClient client = clientBuilder.addPolicy(dataValidationPolicy).buildAsyncClient();
        StepVerifier.create(client.upload(dataCollectionRuleId, streamName, logs))
            .verifyComplete();
    }

    @Test
    public void testUploadLogsInBatches() {
        List<Object> logs = getObjects(10000);

        AtomicInteger count = new AtomicInteger();
        LogsCountPolicy logsCountPolicy = new LogsCountPolicy();

        LogsIngestionAsyncClient client = clientBuilder
            .addPolicy(logsCountPolicy)
            .addPolicy(new BatchCountPolicy(count))
            .buildAsyncClient();

        StepVerifier.create(client.upload(dataCollectionRuleId, streamName, logs))
            .verifyComplete();

        assertEquals(2, count.get());
        assertEquals(logs.size(), logsCountPolicy.getTotalLogsCount());
    }

    @Test
    public void testUploadLogsInBatchesConcurrently() {
        List<Object> logs = getObjects(10000);

        AtomicInteger count = new AtomicInteger();
        LogsCountPolicy logsCountPolicy = new LogsCountPolicy();
        LogsIngestionAsyncClient client = clientBuilder
            .addPolicy(new BatchCountPolicy(count))
            .addPolicy(logsCountPolicy)
            .buildAsyncClient();
        StepVerifier.create(client.upload(dataCollectionRuleId, streamName, logs, new LogsUploadOptions().setMaxConcurrency(3)))
            .verifyComplete();
        assertEquals(2, count.get());
        assertEquals(logs.size(), logsCountPolicy.getTotalLogsCount());
    }

    @Test
    @LiveOnly
    public void testUploadLogsPartialFailure() {
        // Live Only, as it times out in CI playback mode.  TODO: Re-record and update test base to exclude any sanitizers as needed.
        List<Object> logs = getObjects(100000);
        AtomicInteger count = new AtomicInteger();
        LogsCountPolicy logsCountPolicy = new LogsCountPolicy();

        LogsIngestionAsyncClient client = clientBuilder
            .addPolicy(logsCountPolicy)
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
        assertEquals(logs.size(), logsCountPolicy.getTotalLogsCount());
    }

    @Test
    @LiveOnly
    public void testUploadLogsPartialFailureWithErrorHandler() {
        // Live Only, as it times out in CI playback mode.  TODO: Re-record and update test base to exclude any sanitizers as needed.
        List<Object> logs = getObjects(100000);
        AtomicInteger count = new AtomicInteger();
        AtomicLong failedLogsCount = new AtomicLong();
        LogsUploadOptions logsUploadOptions = new LogsUploadOptions()
            .setLogsUploadErrorConsumer(error -> failedLogsCount.addAndGet(error.getFailedLogs().size()));
        LogsCountPolicy logsCountPolicy = new LogsCountPolicy();

        LogsIngestionAsyncClient client = clientBuilder
            .addPolicy(logsCountPolicy)
            .addPolicy(new PartialFailurePolicy(count))
            .buildAsyncClient();

        StepVerifier.create(client.upload(dataCollectionRuleId, streamName, logs, logsUploadOptions))
            .verifyComplete();
        assertEquals(49460, failedLogsCount.get());
        assertEquals(11, count.get());
        assertEquals(logs.size(), logsCountPolicy.getTotalLogsCount());
    }

    @Test
    @LiveOnly
    public void testUploadLogsStopOnFirstError() {
        // Live Only, as it times out in CI playback mode.  TODO: Re-record and update test base to exclude any sanitizers as needed.
        List<Object> logs = getObjects(100000);
        AtomicInteger count = new AtomicInteger();
        LogsUploadOptions logsUploadOptions = new LogsUploadOptions()
            .setLogsUploadErrorConsumer(error -> {
                // throw on first error
                throw error.getResponseException();
            });
        LogsCountPolicy logsCountPolicy = new LogsCountPolicy();

        LogsIngestionAsyncClient client = clientBuilder
            .addPolicy(logsCountPolicy)
            .addPolicy(new PartialFailurePolicy(count))
            .buildAsyncClient();

        StepVerifier.create(client.upload(dataCollectionRuleId, streamName, logs, logsUploadOptions))
            .verifyErrorSatisfies(ex -> assertTrue(ex instanceof HttpResponseException));
        assertEquals(2, count.get());
        // this should stop on first error, so, only one request should be sent that contains a subset of logs
        assertTrue(logs.size() > logsCountPolicy.getTotalLogsCount());
    }

    @Test
    public void testUploadLogsProtocolMethod() {
        List<Object> logs = getObjects(10);
        LogsIngestionAsyncClient client = clientBuilder.buildAsyncClient();
        StepVerifier.create(client.uploadWithResponse(dataCollectionRuleId, streamName,
                BinaryData.fromObject(logs), new RequestOptions()))
            .assertNext(response -> assertEquals(204, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    @RecordWithoutRequestBody
    @EnabledIfEnvironmentVariable(named = "AZURE_TEST_MODE", matches = "LIVE", disabledReason = "Test proxy network connection is timing out for this test in playback mode.")
    public void testUploadLargeLogsProtocolMethod() {
        List<Object> logs = getObjects(375000);
        LogsIngestionAsyncClient client = clientBuilder.buildAsyncClient();
        StepVerifier.create(client.uploadWithResponse(dataCollectionRuleId, streamName,
                BinaryData.fromObject(logs), new RequestOptions()))
            .verifyErrorMatches(responseException -> (responseException instanceof HttpResponseException)
                && ((HttpResponseException) responseException).getResponse().getStatusCode() == 413);
    }

}
