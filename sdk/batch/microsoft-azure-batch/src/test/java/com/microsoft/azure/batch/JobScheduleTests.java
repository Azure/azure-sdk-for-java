// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.batch;

import com.microsoft.azure.batch.protocol.models.*;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.junit.*;

import java.util.LinkedList;
import java.util.List;

public class JobScheduleTests extends BatchIntegrationTestBase {
    static CloudPool livePool;
    static String poolId;

    @BeforeClass
    public static void setup() throws Exception {
        poolId = getStringIdWithUserNamePrefix("-testpool");
        if(isRecordMode()) {
            createClient(AuthMode.AAD);
            livePool = createIfNotExistIaaSPool(poolId);
            Assert.assertNotNull(livePool);
        }
    }

    @AfterClass
    public static void cleanup() throws Exception {
        try {
            //batchClient.poolOperations().deletePool(livePool.id());
        }
        catch (Exception e) {
            // ignore any clean up exception
        }
    }

    @Test
    public void canCRUDJobSchedule() throws Exception {
        // CREATE
        String jobScheduleId = getStringIdWithUserNamePrefix("-JobSchedule-canCRUD");

        PoolInformation poolInfo = new PoolInformation();
        poolInfo.withPoolId(poolId);

        Schedule schedule = new Schedule().withDoNotRunUntil(DateTime.now()).withDoNotRunAfter(DateTime.now().plusHours(5)).withStartWindow(Period.days(5));
        JobSpecification spec = new JobSpecification().withPriority(100).withPoolInfo(poolInfo);
        batchClient.jobScheduleOperations().createJobSchedule(jobScheduleId, schedule, spec);

        try {
            // GET
            Assert.assertTrue(batchClient.jobScheduleOperations().existsJobSchedule(jobScheduleId));

            CloudJobSchedule jobSchedule = batchClient.jobScheduleOperations().getJobSchedule(jobScheduleId);
            Assert.assertNotNull(jobSchedule);
            Assert.assertEquals(jobScheduleId, jobSchedule.id());
            Assert.assertEquals((Integer) 100, jobSchedule.jobSpecification().priority());
            //This case will only hold true during live mode as recorded job schedule time will be in the past.
            //Hence, this assertion should only run in Record/Live mode.
            if(isRecordMode()) {
                Assert.assertTrue(jobSchedule.schedule().doNotRunAfter().compareTo(DateTime.now()) > 0);
            }

            // LIST
            List<CloudJobSchedule> jobSchedules = batchClient.jobScheduleOperations().listJobSchedules(new DetailLevel.Builder().withFilterClause(String.format("id eq '%s'", jobScheduleId)).build());
            Assert.assertNotNull(jobSchedules);
            Assert.assertTrue(jobSchedules.size() > 0);

            Assert.assertEquals(jobScheduleId, jobSchedules.get(0).id());

            // UPDATE
            LinkedList<MetadataItem> metadata = new LinkedList<MetadataItem>();
            metadata.add((new MetadataItem()).withName("key1").withValue("value1"));
            batchClient.jobScheduleOperations().patchJobSchedule(jobScheduleId, null, null, metadata);
            jobSchedule = batchClient.jobScheduleOperations().getJobSchedule(jobScheduleId);
            Assert.assertTrue(jobSchedule.metadata().size() == 1);
            Assert.assertTrue(jobSchedule.metadata().get(0).name().equals("key1"));
            Assert.assertEquals((Integer) 100, jobSchedule.jobSpecification().priority());

            // DELETE
            batchClient.jobScheduleOperations().deleteJobSchedule(jobScheduleId);
            try {
                jobSchedule = batchClient.jobScheduleOperations().getJobSchedule(jobScheduleId);
                Assert.assertTrue("Shouldn't be here, the jobschedule should be deleted", true);
            } catch (BatchErrorException err) {
                if (!err.body().code().equals(BatchErrorCodeStrings.JobScheduleNotFound)) {
                    throw err;
                }
            }

            Thread.sleep(1* 1000);
        } finally {
            try {
                batchClient.jobScheduleOperations().deleteJobSchedule(jobScheduleId);
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
        poolInfo.withPoolId(poolId);

        JobSpecification spec = new JobSpecification().withPriority(100).withPoolInfo(poolInfo);
        Schedule schedule = new Schedule().withDoNotRunUntil(DateTime.now()).withDoNotRunAfter(DateTime.now().plusHours(5)).withStartWindow(Period.days(5));
        batchClient.jobScheduleOperations().createJobSchedule(jobScheduleId, schedule, spec);

        try {
            // GET
            CloudJobSchedule jobSchedule = batchClient.jobScheduleOperations().getJobSchedule(jobScheduleId);
            Assert.assertEquals(JobScheduleState.ACTIVE, jobSchedule.state());

            batchClient.jobScheduleOperations().disableJobSchedule(jobScheduleId);
            jobSchedule = batchClient.jobScheduleOperations().getJobSchedule(jobScheduleId);
            Assert.assertEquals(JobScheduleState.DISABLED, jobSchedule.state());

            batchClient.jobScheduleOperations().enableJobSchedule(jobScheduleId);
            jobSchedule = batchClient.jobScheduleOperations().getJobSchedule(jobScheduleId);
            Assert.assertEquals(JobScheduleState.ACTIVE, jobSchedule.state());

            batchClient.jobScheduleOperations().terminateJobSchedule(jobScheduleId);
            jobSchedule = batchClient.jobScheduleOperations().getJobSchedule(jobScheduleId);
            Assert.assertTrue(jobSchedule.state() == JobScheduleState.TERMINATING || jobSchedule.state() == JobScheduleState.COMPLETED);

            Thread.sleep(2 * 1000);
            jobSchedule = batchClient.jobScheduleOperations().getJobSchedule(jobScheduleId);
            Assert.assertEquals(JobScheduleState.COMPLETED, jobSchedule.state());

            batchClient.jobScheduleOperations().deleteJobSchedule(jobScheduleId);
            try {
                jobSchedule = batchClient.jobScheduleOperations().getJobSchedule(jobScheduleId);
                Assert.assertTrue("Shouldn't be here, the jobschedule should be deleted", true);
            } catch (BatchErrorException err) {
                if (!err.body().code().equals(BatchErrorCodeStrings.JobScheduleNotFound)) {
                    throw err;
                }
            }
        }
        finally {
            try {
                batchClient.jobScheduleOperations().deleteJobSchedule(jobScheduleId);
            }
            catch (Exception e) {
                // Ignore here
            }
        }
    }
}
