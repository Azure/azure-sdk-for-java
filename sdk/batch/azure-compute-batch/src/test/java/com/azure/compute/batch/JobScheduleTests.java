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
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.test.TestMode;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
        if (getTestMode() == TestMode.RECORD) {
            try {
                livePool = createIfNotExistIaaSPool(poolId);
            } catch (Exception e) {
                // TODO (catch): Auto-generated catch block
                e.printStackTrace();
            }
            Assertions.assertNotNull(livePool);
        }
    }

    @Test
    public void canCRUDJobSchedule() throws Exception {
        // CREATE
        String jobScheduleId = getStringIdWithUserNamePrefix("-JobSchedule-canCRUD");

        BatchPoolInfo poolInfo = new BatchPoolInfo();
        poolInfo.setPoolId(poolId);

        BatchJobScheduleConfiguration schedule = new BatchJobScheduleConfiguration().setDoNotRunUntil(now())
            .setDoNotRunAfter(now().plusHours(5))
            .setStartWindow(Duration.ofDays(5));
        BatchJobSpecification spec = new BatchJobSpecification(poolInfo).setPriority(100);
        batchClient.createJobSchedule(new BatchJobScheduleCreateParameters(jobScheduleId, schedule, spec));

        try {
            // GET
            Assertions.assertTrue(batchClient.jobScheduleExists(jobScheduleId));

            BatchJobSchedule jobSchedule = batchClient.getJobSchedule(jobScheduleId);
            Assertions.assertNotNull(jobSchedule);
            Assertions.assertEquals(jobScheduleId, jobSchedule.getId());
            Assertions.assertEquals((Integer) 100, jobSchedule.getJobSpecification().getPriority());
            //This case will only hold true during live mode as recorded job schedule time will be in the past.
            //Hence, this assertion should only run in Record/Live mode.
            if (getTestMode() == TestMode.RECORD) {
                Assertions.assertTrue(jobSchedule.getSchedule().getDoNotRunAfter().isAfter(now()));
            }

            // LIST
            BatchJobSchedulesListOptions listOptions = new BatchJobSchedulesListOptions();
            listOptions.setFilter(String.format("id eq '%s'", jobScheduleId));
            PagedIterable<BatchJobSchedule> jobSchedules = batchClient.listJobSchedules(listOptions);
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

            jobSchedule.setMetadata(metadataList);
            batchClient.replaceJobSchedule(jobScheduleId, jobSchedule);

            jobSchedule = batchClient.getJobSchedule(jobScheduleId);
            Assertions.assertEquals(2, jobSchedule.getMetadata().size());
            Assertions.assertEquals("value2", jobSchedule.getMetadata().get(1).getValue());

            // UPDATE
            LinkedList<BatchMetadataItem> metadata = new LinkedList<BatchMetadataItem>();
            metadata.add((new BatchMetadataItem("key1", "value1")));
            BatchJobScheduleUpdateParameters jobScheduleUpdateParameters = new BatchJobScheduleUpdateParameters();
            jobScheduleUpdateParameters.setMetadata(metadata);
            batchClient.updateJobSchedule(jobScheduleId, jobScheduleUpdateParameters);

            jobSchedule = batchClient.getJobSchedule(jobScheduleId);
            Assertions.assertEquals(1, jobSchedule.getMetadata().size());
            Assertions.assertEquals("key1", jobSchedule.getMetadata().get(0).getName());
            Assertions.assertEquals((Integer) 100, jobSchedule.getJobSpecification().getPriority());

            // DELETE using LRO
            SyncPoller<BatchJobSchedule, Void> poller
                = setPlaybackSyncPollerPollInterval(batchClient.beginDeleteJobSchedule(jobScheduleId));

            PollResponse<BatchJobSchedule> initialResponse = poller.poll();
            if (initialResponse.getStatus() == LongRunningOperationStatus.IN_PROGRESS) {
                BatchJobSchedule jobScheduleDuringPoll = initialResponse.getValue();
                Assertions.assertNotNull(jobScheduleDuringPoll, "Expected job schedule data during polling");
                Assertions.assertEquals(jobScheduleId, jobScheduleDuringPoll.getId());
                Assertions.assertEquals(BatchJobScheduleState.DELETING, jobScheduleDuringPoll.getState());
            }

            poller.waitForCompletion();

            try {
                jobSchedule = batchClient.getJobSchedule(jobScheduleId);
                Assertions.fail("Expected job schedule to be deleted, but it was found.");
            } catch (HttpResponseException err) {
                Assertions.assertEquals(404, err.getResponse().getStatusCode());
            }

            sleepIfRunningAgainstService(1 * 1000);
        } finally {
            try {
                batchClient.deleteJobSchedule(jobScheduleId);
            } catch (Exception e) {
                // Ignore here
            }
        }
    }

    @Test
    public void canUpdateJobScheduleState() throws Exception {
        // CREATE
        String jobScheduleId = getStringIdWithUserNamePrefix("-JobSchedule-updateJobScheduleState");

        BatchPoolInfo poolInfo = new BatchPoolInfo();
        poolInfo.setPoolId(poolId);

        BatchJobSpecification spec = new BatchJobSpecification(poolInfo).setPriority(100);
        BatchJobScheduleConfiguration schedule = new BatchJobScheduleConfiguration().setDoNotRunUntil(now())
            .setDoNotRunAfter(now().plusHours(5))
            .setStartWindow(Duration.ofDays(5));
        batchClient.createJobSchedule(new BatchJobScheduleCreateParameters(jobScheduleId, schedule, spec));

        try {
            // GET
            BatchJobSchedule jobSchedule = batchClient.getJobSchedule(jobScheduleId);
            Assertions.assertEquals(BatchJobScheduleState.ACTIVE, jobSchedule.getState());

            batchClient.disableJobSchedule(jobScheduleId);
            jobSchedule = batchClient.getJobSchedule(jobScheduleId);
            Assertions.assertEquals(BatchJobScheduleState.DISABLED, jobSchedule.getState());

            batchClient.enableJobSchedule(jobScheduleId);
            jobSchedule = batchClient.getJobSchedule(jobScheduleId);
            Assertions.assertEquals(BatchJobScheduleState.ACTIVE, jobSchedule.getState());

            batchClient.terminateJobSchedule(jobScheduleId);
            jobSchedule = batchClient.getJobSchedule(jobScheduleId);
            Assertions.assertTrue(jobSchedule.getState() == BatchJobScheduleState.TERMINATING
                || jobSchedule.getState() == BatchJobScheduleState.COMPLETED);

            sleepIfRunningAgainstService(2 * 1000);
            jobSchedule = batchClient.getJobSchedule(jobScheduleId);
            Assertions.assertEquals(BatchJobScheduleState.COMPLETED, jobSchedule.getState());

            batchClient.deleteJobSchedule(jobScheduleId);
            try {
                jobSchedule = batchClient.getJobSchedule(jobScheduleId);
                Assertions.assertTrue(true, "Shouldn't be here, the jobschedule should be deleted");
            } catch (HttpResponseException err) {
                if (err.getResponse().getStatusCode() != 404) {
                    throw err;
                }
            }
        } finally {
            try {
                batchClient.deleteJobSchedule(jobScheduleId);
            } catch (Exception e) {
                // Ignore here
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
