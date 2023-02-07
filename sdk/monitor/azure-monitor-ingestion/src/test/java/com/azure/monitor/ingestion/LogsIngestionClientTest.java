// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.monitor.ingestion.models.UploadLogsException;
import com.azure.monitor.ingestion.models.UploadLogsOptions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test cases for {@link LogsIngestionClient}.
 */
public class LogsIngestionClientTest extends LogsIngestionTestBase {

    @Test
    public void testUploadLogs() {
        List<Object> logs = getObjects(10);
        LogsIngestionClient client = clientBuilder.buildClient();
        client.upload(dataCollectionRuleId, streamName, logs);
    }

    @Test
    public void testUploadLogsInBatches() {
        List<Object> logs = getObjects(10000);

        AtomicInteger count = new AtomicInteger();
        LogsIngestionClient client = clientBuilder
                .addPolicy(new BatchCountPolicy(count))
                .buildClient();
        client.upload(dataCollectionRuleId, streamName, logs);
        assertEquals(2, count.get());
    }

    @Test
    public void testUploadLogsInBatchesConcurrently() {
        List<Object> logs = getObjects(10000);

        AtomicInteger count = new AtomicInteger();
        LogsIngestionClient client = clientBuilder
                .addPolicy(new BatchCountPolicy(count))
                .buildClient();
        client.upload(dataCollectionRuleId, streamName, logs, new UploadLogsOptions().setMaxConcurrency(3));
        assertEquals(2, count.get());
    }

    @Test
    public void testUploadLogsPartialFailure() {
        List<Object> logs = getObjects(100000);
        AtomicInteger count = new AtomicInteger();

        LogsIngestionClient client = clientBuilder
                .addPolicy(new PartialFailurePolicy(count))
                .buildClient();

        UploadLogsException uploadLogsException = assertThrows(UploadLogsException.class, () -> {
            client.upload(dataCollectionRuleId, streamName, logs);
        });
        assertEquals(49460, uploadLogsException.getFailedLogsCount());
        assertEquals(5, uploadLogsException.getUploadLogsErrors().size());
    }

    @Test
    public void testUploadLogsPartialFailureWithErrorHandler() {
        List<Object> logs = getObjects(100000);
        AtomicInteger count = new AtomicInteger();
        AtomicLong failedLogsCount = new AtomicLong();
        UploadLogsOptions uploadLogsOptions = new UploadLogsOptions()
                .setUploadLogsErrorConsumer(error -> failedLogsCount.addAndGet(error.getFailedLogs().size()));

        LogsIngestionClient client = clientBuilder
                .addPolicy(new PartialFailurePolicy(count))
                .buildClient();

        client.upload(dataCollectionRuleId, streamName, logs, uploadLogsOptions);
        assertEquals(11, count.get());
        assertEquals(49460, failedLogsCount.get());
    }

    @Test
    public void testUploadLogsStopOnFirstError() {
        List<Object> logs = getObjects(100000);
        AtomicInteger count = new AtomicInteger();
        UploadLogsOptions uploadLogsOptions = new UploadLogsOptions()
                .setUploadLogsErrorConsumer(error -> {
                    // throw on first error
                    throw error.getResponseException();
                });

        LogsIngestionClient client = clientBuilder
                .addPolicy(new PartialFailurePolicy(count))
                .buildClient();

        assertThrows(HttpResponseException.class, () -> client.upload(dataCollectionRuleId, streamName, logs,
                uploadLogsOptions));
        assertEquals(2, count.get());
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
    public void testUploadLargeLogsProtocolMethod() {
        List<Object> logs = getObjects(1000000);
        LogsIngestionClient client = clientBuilder.buildClient();

        HttpResponseException responseException = assertThrows(HttpResponseException.class,
                () -> client.uploadWithResponse(dataCollectionRuleId, streamName, BinaryData.fromObject(logs),
                        new RequestOptions()));
        assertEquals(413, responseException.getResponse().getStatusCode());
    }
}
