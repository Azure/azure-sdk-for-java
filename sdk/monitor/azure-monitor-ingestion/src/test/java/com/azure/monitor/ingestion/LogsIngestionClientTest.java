// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.test.annotation.RecordWithoutRequestBody;
import com.azure.core.util.BinaryData;
import com.azure.monitor.ingestion.models.LogsUploadException;
import com.azure.monitor.ingestion.models.LogsUploadOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for {@link LogsIngestionClient}.
 */
public class LogsIngestionClientTest extends LogsIngestionTestBase {

    @Test
    public void testUploadLogs() {
        List<Object> logs = getObjects(10);
        DataValidationPolicy dataValidationPolicy = new DataValidationPolicy(logs);
        LogsIngestionClient client = clientBuilder.addPolicy(dataValidationPolicy).buildClient();
        client.upload(dataCollectionRuleId, streamName, logs);
    }

    @Test
    public void testUploadLogsInBatches() {
        List<Object> logs = getObjects(10000);

        AtomicInteger count = new AtomicInteger();
        LogsCountPolicy logsCountPolicy = new LogsCountPolicy();
        LogsIngestionClient client = clientBuilder
            .addPolicy(new BatchCountPolicy(count))
            .addPolicy(logsCountPolicy)
            .buildClient();
        client.upload(dataCollectionRuleId, streamName, logs);
        assertEquals(2, count.get());
        assertEquals(logs.size(), logsCountPolicy.getTotalLogsCount());
    }

    @Test
    public void testUploadLogsInBatchesConcurrently() {
        List<Object> logs = getObjects(10000);

        AtomicInteger count = new AtomicInteger();
        LogsCountPolicy logsCountPolicy = new LogsCountPolicy();
        LogsIngestionClient client = clientBuilder
            .addPolicy(new BatchCountPolicy(count))
            .addPolicy(logsCountPolicy)
            .buildClient();
        client.upload(dataCollectionRuleId, streamName, logs, new LogsUploadOptions().setMaxConcurrency(3));
        assertEquals(2, count.get());
        assertEquals(logs.size(), logsCountPolicy.getTotalLogsCount());
    }

    @Test
    public void testUploadLogsPartialFailure() {
        List<Object> logs = getObjects(100000);
        AtomicInteger count = new AtomicInteger();
        LogsCountPolicy logsCountPolicy = new LogsCountPolicy();

        LogsIngestionClient client = clientBuilder
            .addPolicy(new PartialFailurePolicy(count))
            .addPolicy(logsCountPolicy)
            .buildClient();

        LogsUploadException uploadLogsException = assertThrows(LogsUploadException.class, () -> {
            client.upload(dataCollectionRuleId, streamName, logs);
        });
        assertEquals(49460, uploadLogsException.getFailedLogsCount());
        assertEquals(5, uploadLogsException.getLogsUploadErrors().size());
        assertEquals(logs.size(), logsCountPolicy.getTotalLogsCount());

    }

    @Test
    public void testUploadLogsPartialFailureWithErrorHandler() {
        List<Object> logs = getObjects(100000);
        AtomicInteger count = new AtomicInteger();
        AtomicLong failedLogsCount = new AtomicLong();
        LogsUploadOptions logsUploadOptions = new LogsUploadOptions()
            .setLogsUploadErrorConsumer(error -> failedLogsCount.addAndGet(error.getFailedLogs().size()));
        LogsCountPolicy logsCountPolicy = new LogsCountPolicy();

        LogsIngestionClient client = clientBuilder
            .addPolicy(new PartialFailurePolicy(count))
            .addPolicy(logsCountPolicy)
            .buildClient();

        client.upload(dataCollectionRuleId, streamName, logs, logsUploadOptions);
        assertEquals(11, count.get());
        assertEquals(49460, failedLogsCount.get());
        assertEquals(logs.size(), logsCountPolicy.getTotalLogsCount());

    }

    @Test
    public void testUploadLogsStopOnFirstError() {
        List<Object> logs = getObjects(100000);
        AtomicInteger count = new AtomicInteger();
        LogsUploadOptions logsUploadOptions = new LogsUploadOptions()
            .setLogsUploadErrorConsumer(error -> {
                // throw on first error
                throw error.getResponseException();
            });
        LogsCountPolicy logsCountPolicy = new LogsCountPolicy();

        LogsIngestionClient client = clientBuilder
            .addPolicy(new PartialFailurePolicy(count))
            .addPolicy(logsCountPolicy)
            .buildClient();

        assertThrows(HttpResponseException.class, () -> client.upload(dataCollectionRuleId, streamName, logs,
            logsUploadOptions));
        assertEquals(2, count.get());

        // only a subset of logs should be sent
        assertTrue(logs.size() > logsCountPolicy.getTotalLogsCount());
    }

    @Test
    public void testUploadLogsProtocolMethod() {
        List<Object> logs = getObjects(10);
        LogsIngestionClient client = clientBuilder.buildClient();
        Response<Void> response = client.uploadWithResponse(dataCollectionRuleId, streamName,
            BinaryData.fromObject(logs), new RequestOptions());
        assertEquals(204, response.getStatusCode());
    }

    @Test
    @RecordWithoutRequestBody
    @EnabledIfEnvironmentVariable(named = "AZURE_TEST_MODE", matches = "LIVE", disabledReason = "Test proxy network connection is timing out for this test in playback mode.")
    public void testUploadLargeLogsProtocolMethod() {
        List<Object> logs = getObjects(375000);
        LogsIngestionClient client = clientBuilder.buildClient();

        HttpResponseException responseException = assertThrows(HttpResponseException.class,
            () -> client.uploadWithResponse(dataCollectionRuleId, streamName, BinaryData.fromObject(logs), new RequestOptions()));
        assertEquals(413, responseException.getResponse().getStatusCode());
    }
}
