package com.azure.compute.batch;

import com.azure.compute.batch.models.*;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.test.TestMode;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static java.time.OffsetDateTime.now;

public class JobScheduleTests extends BatchServiceClientTestBase {
    static BatchPool livePool;
    static String poolId;

    @Override
    protected void beforeTest() {
        super.beforeTest();
        poolId = getStringIdWithUserNamePrefix("-testpool");
        if(getTestMode() == TestMode.RECORD) {
            try {
                livePool = createIfNotExistIaaSPool(poolId);
            } catch (Exception e) {
                // TODO Auto-generated catch block
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

        Schedule schedule = new Schedule().setDoNotRunUntil(now()).setDoNotRunAfter(now().plusHours(5)).setStartWindow(Duration.ofDays(5));
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
            if(getTestMode() == TestMode.RECORD) {
                Assertions.assertTrue(jobSchedule.getSchedule().getDoNotRunAfter().compareTo(now()) > 0);
            }

            // LIST
            ListBatchJobSchedulesOptions listOptions = new ListBatchJobSchedulesOptions();
            listOptions.setFilter(String.format("id eq '%s'", jobScheduleId));
            PagedIterable<BatchJobSchedule> jobSchedules = batchClient.listJobSchedules(listOptions);
            Assert.assertNotNull(jobSchedules);

            boolean found = false;
            for (BatchJobSchedule batchJobSchedule: jobSchedules) {
                if (batchJobSchedule.getId().equals(jobScheduleId)) {
                    found = true;
                }
            }
            Assert.assertTrue(found);

            // REPLACE
            List<MetadataItem> metadataList = new ArrayList<>();
            metadataList.add(new MetadataItem("name1", "value1"));
            metadataList.add(new MetadataItem("name2", "value2"));

            jobSchedule.setMetadata(metadataList);
            batchClient.replaceJobSchedule(jobScheduleId, jobSchedule);

            jobSchedule = batchClient.getJobSchedule(jobScheduleId);
            Assert.assertTrue(jobSchedule.getMetadata().size() == 2);
            Assert.assertTrue(jobSchedule.getMetadata().get(1).getValue().equals("value2"));

            // UPDATE
            LinkedList<MetadataItem> metadata = new LinkedList<MetadataItem>();
            metadata.add((new MetadataItem("key1", "value1")));
            BatchJobScheduleUpdateParameters jobScheduleUpdateOptions = new BatchJobScheduleUpdateParameters();
            jobScheduleUpdateOptions.setMetadata(metadata);
            batchClient.updateJobSchedule(jobScheduleId, jobScheduleUpdateOptions);

            jobSchedule = batchClient.getJobSchedule(jobScheduleId);
            Assert.assertTrue(jobSchedule.getMetadata().size() == 1);
            Assert.assertTrue(jobSchedule.getMetadata().get(0).getName().equals("key1"));
            Assert.assertEquals((Integer) 100, jobSchedule.getJobSpecification().getPriority());

            // DELETE
            batchClient.deleteJobSchedule(jobScheduleId);
            try {
                jobSchedule = batchClient.getJobSchedule(jobScheduleId);
                Assert.assertTrue("Shouldn't be here, the jobschedule should be deleted", true);
            } catch (HttpResponseException err) {
                if (err.getResponse().getStatusCode() != 404) {
                    throw err;
                }
            }

            Thread.sleep(1* 1000);
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
        Schedule schedule = new Schedule().setDoNotRunUntil(now()).setDoNotRunAfter(now().plusHours(5)).setStartWindow(Duration.ofDays(5));
        batchClient.createJobSchedule(new BatchJobScheduleCreateParameters(jobScheduleId, schedule, spec));

        try {
            // GET
            BatchJobSchedule jobSchedule = batchClient.getJobSchedule(jobScheduleId);
            Assert.assertEquals(BatchJobScheduleState.ACTIVE, jobSchedule.getState());

            batchClient.disableJobSchedule(jobScheduleId);
            jobSchedule = batchClient.getJobSchedule(jobScheduleId);
            Assert.assertEquals(BatchJobScheduleState.DISABLED, jobSchedule.getState());

            batchClient.enableJobSchedule(jobScheduleId);
            jobSchedule = batchClient.getJobSchedule(jobScheduleId);
            Assert.assertEquals(BatchJobScheduleState.ACTIVE, jobSchedule.getState());

            batchClient.terminateJobSchedule(jobScheduleId);
            jobSchedule = batchClient.getJobSchedule(jobScheduleId);
            Assert.assertTrue(jobSchedule.getState() == BatchJobScheduleState.TERMINATING || jobSchedule.getState() == BatchJobScheduleState.COMPLETED);

            Thread.sleep(2 * 1000);
            jobSchedule = batchClient.getJobSchedule(jobScheduleId);
            Assert.assertEquals(BatchJobScheduleState.COMPLETED, jobSchedule.getState());

            batchClient.deleteJobSchedule(jobScheduleId);
            try {
                jobSchedule = batchClient.getJobSchedule(jobScheduleId);
                Assert.assertTrue("Shouldn't be here, the jobschedule should be deleted", true);
            } catch (HttpResponseException err) {
                if (err.getResponse().getStatusCode() != 404) {
                    throw err;
                }
            }
        }
        finally {
            try {
                batchClient.deleteJobSchedule(jobScheduleId);
            }
            catch (Exception e) {
                // Ignore here
            }
        }
    }

}
