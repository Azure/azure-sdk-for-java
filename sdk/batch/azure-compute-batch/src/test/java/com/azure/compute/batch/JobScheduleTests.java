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
import java.util.LinkedList;

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

        PoolInformation poolInfo = new PoolInformation();
        poolInfo.setPoolId(poolId);

        Schedule schedule = new Schedule().setDoNotRunUntil(now()).setDoNotRunAfter(now().plusHours(5)).setStartWindow(Duration.ofDays(5));
        JobSpecification spec = new JobSpecification(poolInfo).setPriority(100);
        jobScheduleClient.create(new BatchJobScheduleCreateParameters(jobScheduleId, schedule, spec));

        try {
            // GET
            Assertions.assertTrue(jobScheduleClient.exists(jobScheduleId));

            BatchJobSchedule jobSchedule = jobScheduleClient.get(jobScheduleId);
            Assertions.assertNotNull(jobSchedule);
            Assertions.assertEquals(jobScheduleId, jobSchedule.getId());
            Assertions.assertEquals((Integer) 100, jobSchedule.getJobSpecification().getPriority());
            //This case will only hold true during live mode as recorded job schedule time will be in the past.
            //Hence, this assertion should only run in Record/Live mode.
            if(getTestMode() == TestMode.RECORD) {
                Assertions.assertTrue(jobSchedule.getSchedule().getDoNotRunAfter().compareTo(now()) > 0);
            }

            // LIST
            RequestOptions listOptions = new RequestOptions();
            listOptions.addQueryParam("$filter", String.format("id eq '%s'", jobScheduleId));
            PagedIterable<BatchJobSchedule> jobSchedules = jobScheduleClient.list(listOptions).mapPage(bodyItemValue -> bodyItemValue.toObject(BatchJobSchedule.class));
            Assert.assertNotNull(jobSchedules);

            boolean found = false;
            for (BatchJobSchedule batchJobSchedule: jobSchedules) {
                if (batchJobSchedule.getId().equals(jobScheduleId)) {
                    found = true;
                }
            }
            Assert.assertTrue(found);

            // UPDATE
            LinkedList<MetadataItem> metadata = new LinkedList<MetadataItem>();
            metadata.add((new MetadataItem("key1", "value1")));
            BatchJobScheduleUpdateParameters jobScheduleUpdateParameters = new BatchJobScheduleUpdateParameters();
            jobScheduleUpdateParameters.setMetadata(metadata);
            jobScheduleClient.patch(jobScheduleId, jobScheduleUpdateParameters);

            jobSchedule = jobScheduleClient.get(jobScheduleId);
            Assert.assertTrue(jobSchedule.getMetadata().size() == 1);
            Assert.assertTrue(jobSchedule.getMetadata().get(0).getName().equals("key1"));
            Assert.assertEquals((Integer) 100, jobSchedule.getJobSpecification().getPriority());

            // DELETE
            jobScheduleClient.delete(jobScheduleId);
            try {
                jobSchedule = jobScheduleClient.get(jobScheduleId);
                Assert.assertTrue("Shouldn't be here, the jobschedule should be deleted", true);
            } catch (HttpResponseException err) {
                if (err.getResponse().getStatusCode() != 404) {
                    throw err;
                }
            }

            Thread.sleep(1* 1000);
        } finally {
            try {
                jobScheduleClient.delete(jobScheduleId);
            } catch (Exception e) {
                // Ignore here
            }
        }
    }

    @Test
    public void canUpdateJobScheduleState() throws Exception {
        // CREATE
        String jobScheduleId = getStringIdWithUserNamePrefix("-JobSchedule-updateJobScheduleState");

        PoolInformation poolInfo = new PoolInformation();
        poolInfo.setPoolId(poolId);

        JobSpecification spec = new JobSpecification(poolInfo).setPriority(100);
        Schedule schedule = new Schedule().setDoNotRunUntil(now()).setDoNotRunAfter(now().plusHours(5)).setStartWindow(Duration.ofDays(5));
        jobScheduleClient.create(new BatchJobScheduleCreateParameters(jobScheduleId, schedule, spec));

        try {
            // GET
            BatchJobSchedule jobSchedule = jobScheduleClient.get(jobScheduleId);
            Assert.assertEquals(JobScheduleState.ACTIVE, jobSchedule.getState());

            jobScheduleClient.disable(jobScheduleId);
            jobSchedule = jobScheduleClient.get(jobScheduleId);
            Assert.assertEquals(JobScheduleState.DISABLED, jobSchedule.getState());

            jobScheduleClient.enable(jobScheduleId);
            jobSchedule = jobScheduleClient.get(jobScheduleId);
            Assert.assertEquals(JobScheduleState.ACTIVE, jobSchedule.getState());

            jobScheduleClient.terminate(jobScheduleId);
            jobSchedule = jobScheduleClient.get(jobScheduleId);
            Assert.assertTrue(jobSchedule.getState() == JobScheduleState.TERMINATING || jobSchedule.getState() == JobScheduleState.COMPLETED);

            Thread.sleep(2 * 1000);
            jobSchedule = jobScheduleClient.get(jobScheduleId);
            Assert.assertEquals(JobScheduleState.COMPLETED, jobSchedule.getState());

            jobScheduleClient.delete(jobScheduleId);
            try {
                jobSchedule = jobScheduleClient.get(jobScheduleId);
                Assert.assertTrue("Shouldn't be here, the jobschedule should be deleted", true);
            } catch (HttpResponseException err) {
                if (err.getResponse().getStatusCode() != 404) {
                    throw err;
                }
            }
        }
        finally {
            try {
                jobScheduleClient.delete(jobScheduleId);
            }
            catch (Exception e) {
                // Ignore here
            }
        }
    }

}
