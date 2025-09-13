// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch;

import com.azure.compute.batch.models.BatchJobSchedule;
import com.azure.compute.batch.models.BatchJobScheduleConfiguration;
import com.azure.compute.batch.models.BatchJobScheduleCreateParameters;
import com.azure.compute.batch.models.BatchJobScheduleState;
import com.azure.compute.batch.models.BatchJobScheduleStatistics;
import com.azure.compute.batch.models.BatchJobScheduleUpdateParameters;
import com.azure.compute.batch.models.BatchJobSpecification;
import com.azure.compute.batch.models.BatchMetadataItem;
import com.azure.compute.batch.models.BatchPool;
import com.azure.compute.batch.models.BatchPoolInfo;
import com.azure.compute.batch.models.BatchJobSchedulesListOptions;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.test.SyncAsyncExtension;
import com.azure.core.test.TestMode;
import com.azure.core.test.annotation.SyncAsyncTest;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.StringReader;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static java.time.OffsetDateTime.now;

public class JobScheduleTests extends BatchClientTestBase {
    static BatchPool livePool;
    static String poolId;

    @Override
    protected void beforeTest() {
        super.beforeTest();
        poolId = getStringIdWithUserNamePrefix("-testpool");
        try {
            livePool = createIfNotExistIaaSPool(poolId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assertions.assertNotNull(livePool);
    }

    @SyncAsyncTest
    public void canCRUDJobSchedule() {
        // Generate a jobId that is unique per test mode (sync vs async)
        String testModeSuffix = SyncAsyncExtension.execute(() -> "sync", () -> Mono.just("async"));
        // CREATE
        String jobScheduleId = getStringIdWithUserNamePrefix("-JobSchedule-canCRUD" + testModeSuffix);

        BatchPoolInfo poolInfo = new BatchPoolInfo().setPoolId(poolId);

        BatchJobScheduleConfiguration schedule = new BatchJobScheduleConfiguration().setDoNotRunUntil(now())
            .setDoNotRunAfter(now().plusHours(5))
            .setStartWindow(Duration.ofDays(5));
        BatchJobSpecification spec = new BatchJobSpecification(poolInfo).setPriority(100);

        SyncAsyncExtension.execute(
            () -> batchClient.createJobSchedule(new BatchJobScheduleCreateParameters(jobScheduleId, schedule, spec)),
            () -> batchAsyncClient
                .createJobSchedule(new BatchJobScheduleCreateParameters(jobScheduleId, schedule, spec)));

        // GET
        Boolean exists = SyncAsyncExtension.execute(() -> batchClient.jobScheduleExists(jobScheduleId),
            () -> batchAsyncClient.jobScheduleExists(jobScheduleId));
        Assertions.assertTrue(exists);

        BatchJobSchedule originalJobSchedule = SyncAsyncExtension.execute(
            () -> batchClient.getJobSchedule(jobScheduleId), () -> batchAsyncClient.getJobSchedule(jobScheduleId));
        Assertions.assertNotNull(originalJobSchedule);
        Assertions.assertEquals(jobScheduleId, originalJobSchedule.getId());
        Assertions.assertEquals((Integer) 100, originalJobSchedule.getJobSpecification().getPriority());

        //This case will only hold true during live mode as recorded job schedule time will be in the past.
        //Hence, this assertion should only run in Record/Live mode.
        if (getTestMode() == TestMode.RECORD) {
            Assertions.assertTrue(originalJobSchedule.getSchedule().getDoNotRunAfter().isAfter(now()));
        }

        // LIST
        BatchJobSchedulesListOptions listOptions = new BatchJobSchedulesListOptions();
        listOptions.setFilter(String.format("id eq '%s'", jobScheduleId));

        Iterable<BatchJobSchedule> jobSchedules
            = SyncAsyncExtension.execute(() -> batchClient.listJobSchedules(listOptions),
                () -> Mono.fromCallable(() -> batchAsyncClient.listJobSchedules(listOptions).toIterable()));

        Assertions.assertNotNull(jobSchedules);

        boolean found = false;
        for (BatchJobSchedule batchJobSchedule : jobSchedules) {
            if (batchJobSchedule.getId().equals(jobScheduleId)) {
                found = true;
                break;
            }
        }
        Assertions.assertTrue(found);

        // REPLACE
        List<BatchMetadataItem> metadataList = new ArrayList<>();
        metadataList.add(new BatchMetadataItem("name1", "value1"));
        metadataList.add(new BatchMetadataItem("name2", "value2"));

        BatchJobSchedule updatedJobSchedule = originalJobSchedule;
        updatedJobSchedule.setMetadata(metadataList);

        SyncAsyncExtension.execute(() -> batchClient.replaceJobSchedule(jobScheduleId, updatedJobSchedule),
            () -> batchAsyncClient.replaceJobSchedule(jobScheduleId, updatedJobSchedule));

        BatchJobSchedule jobScheduleAfterReplace = SyncAsyncExtension.execute(
            () -> batchClient.getJobSchedule(jobScheduleId), () -> batchAsyncClient.getJobSchedule(jobScheduleId));
        Assertions.assertEquals(2, jobScheduleAfterReplace.getMetadata().size());
        Assertions.assertEquals("value2", jobScheduleAfterReplace.getMetadata().get(1).getValue());

        // UPDATE
        LinkedList<BatchMetadataItem> metadata = new LinkedList<>();
        metadata.add(new BatchMetadataItem("key1", "value1"));
        BatchJobScheduleUpdateParameters updateParams = new BatchJobScheduleUpdateParameters().setMetadata(metadata);

        SyncAsyncExtension.execute(() -> batchClient.updateJobSchedule(jobScheduleId, updateParams),
            () -> batchAsyncClient.updateJobSchedule(jobScheduleId, updateParams));

        BatchJobSchedule jobScheduleAfterUpdate = SyncAsyncExtension.execute(
            () -> batchClient.getJobSchedule(jobScheduleId), () -> batchAsyncClient.getJobSchedule(jobScheduleId));
        Assertions.assertEquals(1, jobScheduleAfterUpdate.getMetadata().size());
        Assertions.assertEquals("key1", jobScheduleAfterUpdate.getMetadata().get(0).getName());
        Assertions.assertEquals((Integer) 100, jobScheduleAfterUpdate.getJobSpecification().getPriority());

        // DELETE
        SyncPoller<BatchJobSchedule, Void> poller = setPlaybackSyncPollerPollInterval(
            SyncAsyncExtension.execute(() -> batchClient.beginDeleteJobSchedule(jobScheduleId),
                () -> Mono.fromCallable(() -> batchAsyncClient.beginDeleteJobSchedule(jobScheduleId).getSyncPoller())));

        PollResponse<BatchJobSchedule> initialResponse = poller.poll();
        if (initialResponse.getStatus() == LongRunningOperationStatus.IN_PROGRESS) {
            BatchJobSchedule jobScheduleDuringPoll = initialResponse.getValue();
            Assertions.assertNotNull(jobScheduleDuringPoll, "Expected job schedule data during polling");
            Assertions.assertEquals(jobScheduleId, jobScheduleDuringPoll.getId());
            Assertions.assertEquals(BatchJobScheduleState.DELETING, jobScheduleDuringPoll.getState());
        }

        poller.waitForCompletion();

        PollResponse<BatchJobSchedule> finalResponse = poller.poll();
        Assertions.assertNull(finalResponse.getValue(), "Expected final result to be null after successful deletion.");

        try {
            SyncAsyncExtension.execute(() -> batchClient.getJobSchedule(jobScheduleId),
                () -> batchAsyncClient.getJobSchedule(jobScheduleId));
            Assertions.fail("Expected job schedule to be deleted.");
        } catch (HttpResponseException ex) {
            Assertions.assertEquals(404, ex.getResponse().getStatusCode());
        }

    }

    @SyncAsyncTest
    public void canUpdateJobScheduleState() {
        // Generate a jobId that is unique per test mode (sync vs async)
        String testModeSuffix = SyncAsyncExtension.execute(() -> "sync", () -> Mono.just("async"));
        // CREATE
        String jobScheduleId = getStringIdWithUserNamePrefix("-JobSchedule-updateJobScheduleState" + testModeSuffix);

        BatchPoolInfo poolInfo = new BatchPoolInfo().setPoolId(poolId);
        BatchJobSpecification spec = new BatchJobSpecification(poolInfo).setPriority(100);
        BatchJobScheduleConfiguration schedule = new BatchJobScheduleConfiguration().setDoNotRunUntil(now())
            .setDoNotRunAfter(now().plusHours(5))
            .setStartWindow(Duration.ofDays(5));

        SyncAsyncExtension.execute(
            () -> batchClient.createJobSchedule(new BatchJobScheduleCreateParameters(jobScheduleId, schedule, spec)),
            () -> batchAsyncClient
                .createJobSchedule(new BatchJobScheduleCreateParameters(jobScheduleId, schedule, spec)));

        try {
            // GET
            BatchJobSchedule jobSchedule = SyncAsyncExtension.execute(() -> batchClient.getJobSchedule(jobScheduleId),
                () -> batchAsyncClient.getJobSchedule(jobScheduleId));
            Assertions.assertEquals(BatchJobScheduleState.ACTIVE, jobSchedule.getState());

            SyncAsyncExtension.execute(() -> {
                batchClient.disableJobSchedule(jobScheduleId);
                return null;
            }, () -> batchAsyncClient.disableJobSchedule(jobScheduleId).then());

            jobSchedule = SyncAsyncExtension.execute(() -> batchClient.getJobSchedule(jobScheduleId),
                () -> batchAsyncClient.getJobSchedule(jobScheduleId));
            Assertions.assertEquals(BatchJobScheduleState.DISABLED, jobSchedule.getState());

            SyncAsyncExtension.execute(() -> {
                batchClient.enableJobSchedule(jobScheduleId);
                return null;
            }, () -> batchAsyncClient.enableJobSchedule(jobScheduleId).then());

            jobSchedule = SyncAsyncExtension.execute(() -> batchClient.getJobSchedule(jobScheduleId),
                () -> batchAsyncClient.getJobSchedule(jobScheduleId));
            Assertions.assertEquals(BatchJobScheduleState.ACTIVE, jobSchedule.getState());

            // TERMINATE
            SyncPoller<BatchJobSchedule, BatchJobSchedule> terminatePoller = setPlaybackSyncPollerPollInterval(
                SyncAsyncExtension.execute(() -> batchClient.beginTerminateJobSchedule(jobScheduleId), () -> Mono
                    .fromCallable(() -> batchAsyncClient.beginTerminateJobSchedule(jobScheduleId).getSyncPoller())));

            terminatePoller.waitForCompletion();
            jobSchedule = terminatePoller.getFinalResult();
            Assertions.assertNotNull(jobSchedule);
            Assertions.assertEquals(BatchJobScheduleState.COMPLETED, jobSchedule.getState());

        } finally {
            // DELETE
            try {
                SyncPoller<BatchJobSchedule, Void> deletePoller = setPlaybackSyncPollerPollInterval(
                    SyncAsyncExtension.execute(() -> batchClient.beginDeleteJobSchedule(jobScheduleId), () -> Mono
                        .fromCallable(() -> batchAsyncClient.beginDeleteJobSchedule(jobScheduleId).getSyncPoller())));

                deletePoller.waitForCompletion();
            } catch (Exception e) {
                System.err.println("Cleanup failed for job schedule: " + jobScheduleId);
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testDeserializationOfBatchJobScheduleStatistics() {
        // Simulated JSON response with numbers as strings
        String jsonResponse = "{" + "\"url\":\"https://example.com/schedule-stats\","
            + "\"startTime\":\"2022-01-01T00:00:00Z\"," + "\"lastUpdateTime\":\"2022-01-01T01:00:00Z\","
            + "\"userCPUTime\":\"PT1H\"," + "\"kernelCPUTime\":\"PT30M\"," + "\"wallClockTime\":\"PT1H30M\","
            + "\"readIOps\":\"1000\"," + "\"writeIOps\":\"500\"," + "\"readIOGiB\":0.5," + "\"writeIOGiB\":0.25,"
            + "\"numSucceededTasks\":\"10\"," + "\"numFailedTasks\":\"2\"," + "\"numTaskRetries\":\"3\","
            + "\"waitTime\":\"PT10M\"" + "}";

        // Deserialize JSON response using JsonReader from JsonProviders
        try (JsonReader jsonReader = JsonProviders.createReader(new StringReader(jsonResponse))) {
            BatchJobScheduleStatistics stats = BatchJobScheduleStatistics.fromJson(jsonReader);

            // Assertions
            Assertions.assertNotNull(stats);
            Assertions.assertEquals("https://example.com/schedule-stats", stats.getUrl());
            Assertions.assertEquals(OffsetDateTime.parse("2022-01-01T00:00:00Z"), stats.getStartTime());
            Assertions.assertEquals(OffsetDateTime.parse("2022-01-01T01:00:00Z"), stats.getLastUpdateTime());
            Assertions.assertEquals(Duration.parse("PT1H"), stats.getUserCpuTime());
            Assertions.assertEquals(Duration.parse("PT30M"), stats.getKernelCpuTime());
            Assertions.assertEquals(Duration.parse("PT1H30M"), stats.getWallClockTime());
            Assertions.assertEquals(1000, stats.getReadIops());
            Assertions.assertEquals(500, stats.getWriteIops());
            Assertions.assertEquals(0.5, stats.getReadIoGiB());
            Assertions.assertEquals(0.25, stats.getWriteIoGiB());
            Assertions.assertEquals(10, stats.getSucceededTasksCount());
            Assertions.assertEquals(2, stats.getFailedTasksCount());
            Assertions.assertEquals(3, stats.getTaskRetriesCount());
            Assertions.assertEquals(Duration.parse("PT10M"), stats.getWaitTime());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
