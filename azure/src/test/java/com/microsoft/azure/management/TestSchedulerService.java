/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.scheduler.HttpRequest;
import com.microsoft.azure.management.scheduler.Job;
import com.microsoft.azure.management.scheduler.JobAction;
import com.microsoft.azure.management.scheduler.JobActionType;
import com.microsoft.azure.management.scheduler.JobCollection;
import com.microsoft.azure.management.scheduler.JobCollectionState;
import com.microsoft.azure.management.scheduler.JobCollections;
import com.microsoft.azure.management.scheduler.JobHistory;
import com.microsoft.azure.management.scheduler.JobRecurrence;
import com.microsoft.azure.management.scheduler.JobScheduleDay;
import com.microsoft.azure.management.scheduler.JobScheduleMonthlyWeekDay;
import com.microsoft.azure.management.scheduler.JobState;
import com.microsoft.azure.management.scheduler.RecurrenceFrequency;
import com.microsoft.azure.management.scheduler.RetryPolicy;
import com.microsoft.azure.management.scheduler.RetryType;
import com.microsoft.azure.management.scheduler.SkuDefinition;
import org.joda.time.DateTime;
import org.junit.Assert;

import java.util.List;

public class TestSchedulerService {
    /**
     * Scheduler service job collection test.
     */
    public static class JobCollectionMultipleSkuTest extends TestTemplate<JobCollection, JobCollections> {

        @Override
        public JobCollection createResource(JobCollections jobCollections) throws Exception {
            final String newName = "jobs" + this.testId;
            String rgName = "rgjobc" + this.testId;

            JobCollection jobCollection = jobCollections.define(newName)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(rgName)
                .withSku(SkuDefinition.P10PREMIUM)
                .withTag("tag1", "value1")
                .create();

            Assert.assertTrue(jobCollection.tags().containsKey("tag1"));
            Assert.assertEquals(null, jobCollection.quota());
            Assert.assertEquals(SkuDefinition.P10PREMIUM, jobCollection.sku().name());
            Assert.assertEquals(50, jobCollection.maxJobCount());
            Assert.assertTrue(jobCollection.maxRecurrenceFrequency() == RecurrenceFrequency.MINUTE);
            Assert.assertTrue(jobCollection.state() == JobCollectionState.ENABLED);

            List<JobCollection> jobCollectionList = jobCollections.list();
            Assert.assertTrue(!jobCollectionList.isEmpty());

            jobCollectionList = jobCollections.listByResourceGroup(rgName);
            Assert.assertEquals(1, jobCollectionList.size());
            Assert.assertEquals(newName, jobCollectionList.get(0).name());

            jobCollections.disable(rgName, newName);
            jobCollection = jobCollections.getByResourceGroup(rgName, newName);
            Assert.assertTrue(jobCollection.state() == JobCollectionState.DISABLED);

            return jobCollection;
        }

        @Override
        public JobCollection updateResource(JobCollection resource) throws Exception {
            resource.enable();
            Assert.assertTrue(resource.state() == JobCollectionState.ENABLED);

            resource = resource.update()
                .withSku(SkuDefinition.STANDARD)
                .withState(JobCollectionState.SUSPENDED)
                .withTag("tag2", "value2")
                .withTag("tag3", "value3")
                .withoutTag("tag1")
                .apply();
            Assert.assertTrue(resource.tags().containsKey("tag2"));
            Assert.assertTrue(!resource.tags().containsKey("tag1"));
            Assert.assertEquals(null, resource.quota());
            Assert.assertEquals(50, resource.maxJobCount());
            Assert.assertTrue(resource.maxRecurrenceFrequency() == RecurrenceFrequency.MINUTE);
            Assert.assertTrue(resource.state() == JobCollectionState.SUSPENDED);

            resource = resource.update()
                .withJobCollectionQuota(2, 10000, RecurrenceFrequency.HOUR, 4)
                .withState(JobCollectionState.DISABLED)
                .apply();
            Assert.assertNotNull(resource.quota());
            Assert.assertEquals(2, resource.maxJobCount());
            Assert.assertTrue(resource.maxRecurrenceFrequency() == RecurrenceFrequency.HOUR);
            Assert.assertTrue(resource.state() == JobCollectionState.DISABLED);

            return resource;
        }

        @Override
        public void print(JobCollection resource) {
            TestSchedulerService.print(resource, "Job Collection with multiple SKUs and states: ");
        }
    }

    /**
     * Scheduler service job and job collection test.
     */
    public static class JobAndJobCollectionTest extends TestTemplate<JobCollection, JobCollections> {

        @Override
        public JobCollection createResource(JobCollections jobCollections) throws Exception {
            final String jobCollectionName = "jobs" + this.testId;
            final String job1 = "job1-" + this.testId;
            String rgName = "rgjobc" + this.testId;

            JobCollection jobCollection = jobCollections.define(jobCollectionName)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(rgName)
                .withSku(SkuDefinition.STANDARD)
                .withTag("tag1", "value1")
                .create();

            Assert.assertTrue(jobCollection.tags().containsKey("tag1"));
            Assert.assertEquals(null, jobCollection.quota());
            Assert.assertEquals(SkuDefinition.STANDARD, jobCollection.sku().name());
            Assert.assertEquals(50, jobCollection.maxJobCount());
            Assert.assertTrue(jobCollection.maxRecurrenceFrequency() == RecurrenceFrequency.MINUTE);
            Assert.assertTrue(jobCollection.state() == JobCollectionState.ENABLED);

            jobCollection.jobs().define(job1)
                .startingAt(new DateTime())
                .withRecurrence(new JobRecurrence()
                    .withFrequency(RecurrenceFrequency.MINUTE)
                    .withCount(2))
                .withAction(new JobAction()
                    .withType(JobActionType.HTTP)
                    .withRequest(new HttpRequest()
                        .withMethod("GET")
                        .withUri("http://www.bing.com"))
                    .withRetryPolicy(new RetryPolicy()
                        .withRetryType(RetryType.FIXED))
                )
                .create();

            jobCollection.jobs().run(job1);

            SdkContext.sleep(65000);

            Job job = jobCollection.jobs().getByName(job1);
            job.update()
                .withState(JobState.ENABLED)
                .withRecurrence(job.recurrence()
                    .withCount(1)
                    .withFrequency(RecurrenceFrequency.HOUR))
                .apply();

            return jobCollection;
        }

        @Override
        public JobCollection updateResource(JobCollection jobCollection) throws Exception {
            final String jobName2 = "job2-" + this.testId;
            final String jobName3 = "job3-" + this.testId;
            final String jobName4 = "job4-" + this.testId;
            final String jobName5 = "job5-" + this.testId;
            final String jobName6 = "job6-" + this.testId;
            final String jobName7 = "job7-" + this.testId;
            final String jobName8 = "job8-" + this.testId;
            Job job;

            // create a simple job that runs immediately
            jobCollection.jobs().define(jobName2)
                .startingNow()
                .withAction(new JobAction()
                        .withType(JobActionType.HTTP)
                        .withRequest(new HttpRequest()
                            .withMethod("GET")
                            .withUri("http://www.bing.com"))
                        .withRetryPolicy(new RetryPolicy()
                            .withRetryType(RetryType.FIXED)))
                .create();
            job = jobCollection.jobs().getByName(jobName2);
            Assert.assertEquals(jobName2, job.name());
            Assert.assertNull(job.startTime());
            Assert.assertNull(job.recurrence());
            Assert.assertNotNull(job.action());
            Assert.assertEquals(JobActionType.HTTP, job.action().type());
            Assert.assertEquals("GET", job.action().request().method());
            Assert.assertEquals("http://www.bing.com", job.action().request().uri());
            Assert.assertNull(job.action().request().authentication());
            Assert.assertNull(job.action().request().body());
            Assert.assertNull(job.action().request().headers());
            Assert.assertEquals(JobState.ENABLED, job.state());

            // create a simple job that runs every 3 hours ending after 5 runs
            jobCollection.jobs().define(jobName3)
                .startingAt(new DateTime())
                .runningEvery(3)
                .hours()
                .endingAfterOccurrence(5)
                .withAction(new JobAction()
                    .withType(JobActionType.HTTP)
                    .withRequest(new HttpRequest()
                        .withMethod("GET")
                        .withUri("http://www.bing.com"))
                    .withRetryPolicy(new RetryPolicy()
                        .withRetryType(RetryType.FIXED)))
                .create();
            job = jobCollection.jobs().getByName(jobName3);
            Assert.assertEquals(jobName3, job.name());
            Assert.assertNotNull(job.startTime());
            Assert.assertNotNull(job.recurrence());
            Assert.assertEquals(RecurrenceFrequency.HOUR, job.recurrence().frequency());
            Assert.assertEquals(3, job.recurrence().interval().intValue());
            Assert.assertEquals(5, job.recurrence().count().intValue());
            Assert.assertNull(job.recurrence().endTime());
            Assert.assertNotNull(job.action());
            Assert.assertEquals(JobActionType.HTTP, job.action().type());
            Assert.assertEquals("GET", job.action().request().method());
            Assert.assertEquals("http://www.bing.com", job.action().request().uri());
            Assert.assertNull(job.action().request().authentication());
            Assert.assertNull(job.action().request().body());
            Assert.assertNull(job.action().request().headers());
            Assert.assertEquals(JobState.ENABLED, job.state());


            // create a simple job that runs every day at given hours and minutes with no ending
            jobCollection.jobs().define(jobName4)
                .startingAt(new DateTime())
                .runningDaily()
                .atTheseHours(10, 22)
                .atTheseMinutes(0, 30)
                .endingNever()
                .withAction(new JobAction()
                    .withType(JobActionType.HTTP)
                    .withRequest(new HttpRequest()
                        .withMethod("GET")
                        .withUri("http://www.bing.com"))
                    .withRetryPolicy(new RetryPolicy()
                        .withRetryType(RetryType.FIXED)))
                .create();
            job = jobCollection.jobs().getByName(jobName4);
            Assert.assertEquals(jobName4, job.name());
            Assert.assertNotNull(job.startTime());
            Assert.assertNotNull(job.recurrence());
            Assert.assertEquals(RecurrenceFrequency.DAY, job.recurrence().frequency());
            Assert.assertEquals(1, job.recurrence().interval().intValue());
            Assert.assertNotNull(job.recurrence().schedule());
            Assert.assertNotNull(job.recurrence().schedule().hours());
            Assert.assertNotNull(job.recurrence().schedule().minutes());
            Assert.assertNull(job.recurrence().count());
            Assert.assertNull(job.recurrence().endTime());
            Assert.assertNotNull(job.action());
            Assert.assertEquals(JobActionType.HTTP, job.action().type());
            Assert.assertEquals("GET", job.action().request().method());
            Assert.assertEquals("http://www.bing.com", job.action().request().uri());
            Assert.assertNull(job.action().request().authentication());
            Assert.assertNull(job.action().request().body());
            Assert.assertNull(job.action().request().headers());
            Assert.assertEquals(JobState.ENABLED, job.state());

            // create a simple job that runs every month on a given day at given hours and minutes with end time
            jobCollection.jobs().define(jobName5)
                .startingAt(new DateTime())
                .runningMonthly().recurringEvery(JobScheduleMonthlyWeekDay.FIRST_MONDAY)
                .atTheseHours(10, 22)
                .atTheseMinutes(0, 30)
                .endingBy(new DateTime().plusMonths(5))
                .withAction(new JobAction()
                    .withType(JobActionType.HTTP)
                    .withRequest(new HttpRequest()
                        .withMethod("GET")
                        .withUri("http://www.bing.com"))
                    .withRetryPolicy(new RetryPolicy()
                        .withRetryType(RetryType.FIXED)))
                .create();
            job = jobCollection.jobs().getByName(jobName5);
            Assert.assertEquals(jobName5, job.name());
            Assert.assertNotNull(job.startTime());
            Assert.assertNotNull(job.recurrence());
            Assert.assertEquals(RecurrenceFrequency.MONTH, job.recurrence().frequency());
            Assert.assertEquals(1, job.recurrence().interval().intValue());
            Assert.assertNotNull(job.recurrence().schedule());
            Assert.assertNotNull(job.recurrence().schedule().monthlyOccurrences());
            Assert.assertNotNull(job.recurrence().schedule().hours());
            Assert.assertNotNull(job.recurrence().schedule().minutes());
            Assert.assertNull(job.recurrence().count());
            Assert.assertNotNull(job.recurrence().endTime());
            Assert.assertNotNull(job.action());
            Assert.assertEquals(JobActionType.HTTP, job.action().type());
            Assert.assertEquals("GET", job.action().request().method());
            Assert.assertEquals("http://www.bing.com", job.action().request().uri());
            Assert.assertNull(job.action().request().authentication());
            Assert.assertNull(job.action().request().body());
            Assert.assertNull(job.action().request().headers());
            Assert.assertEquals(JobState.ENABLED, job.state());

            // create a simple job that runs month on a given day of the week
            jobCollection.jobs().define(jobName6)
                .startingAt(new DateTime())
                .runningMonthly().recurringEvery(JobScheduleDay.SUNDAY)
                .endingBy(new DateTime().plusMonths(2))
                .withAction(new JobAction()
                    .withType(JobActionType.HTTP)
                    .withRequest(new HttpRequest()
                        .withMethod("GET")
                        .withUri("http://www.bing.com"))
                    .withRetryPolicy(new RetryPolicy()
                        .withRetryType(RetryType.FIXED)))
                .create();
            job = jobCollection.jobs().getByName(jobName6);
            Assert.assertEquals(jobName6, job.name());
            Assert.assertNotNull(job.startTime());
            Assert.assertNotNull(job.recurrence());
            Assert.assertEquals(RecurrenceFrequency.MONTH, job.recurrence().frequency());
            Assert.assertEquals(1, job.recurrence().interval().intValue());
            Assert.assertNotNull(job.recurrence().schedule());
            Assert.assertNotNull(job.recurrence().schedule().monthlyOccurrences());
            Assert.assertNull(job.recurrence().schedule().hours());
            Assert.assertNull(job.recurrence().schedule().minutes());
            Assert.assertNull(job.recurrence().count());
            Assert.assertNotNull(job.recurrence().endTime());
            Assert.assertNotNull(job.action());
            Assert.assertEquals(JobActionType.HTTP, job.action().type());
            Assert.assertEquals("GET", job.action().request().method());
            Assert.assertEquals("http://www.bing.com", job.action().request().uri());
            Assert.assertNull(job.action().request().authentication());
            Assert.assertNull(job.action().request().body());
            Assert.assertNull(job.action().request().headers());
            Assert.assertEquals(JobState.ENABLED, job.state());

            return jobCollection;
        }

        @Override
        public void print(JobCollection resource) {
            TestSchedulerService.print(resource, "Job and Job Collection with STANDARD SKU: ");
        }
    }

    /**
     * Common print method for a job collection resource.
     *
     * @param resource Job Collection resource
     * @param header String to be printed first
     */
    public static void print(JobCollection resource, String header) {
        StringBuilder stringBuilder = new StringBuilder().append(header).append(resource.id())
            .append("Name: ").append(resource.name())
            .append("\n\tResource group: ").append(resource.resourceGroupName())
            .append("\n\tRegion: ").append(resource.region())
            .append("\n\tSKU: ").append(resource.sku().name())
            .append("\n\tMax job count: ").append(resource.maxJobCount())
            .append("\n\tMax recurrence frequency: ").append(resource.maxRecurrenceFrequency().name())
            .append("\n\tState: ").append(resource.state().name())
            .append("\n\tTags: ").append(resource.tags());
        List<Job> jobs = resource.jobs().list();
        if (jobs != null) {
            for (Job job : jobs) {
                print(job, stringBuilder);
                List<JobHistory> jobHistoryList = resource.jobs().listJobHistory(job.name());
                if (jobHistoryList != null) {
                    for (JobHistory jobHistory : jobHistoryList) {
                        print(jobHistory, stringBuilder);
                    }
                }
            }
        }

        System.out.println(stringBuilder);
    }

    /**
     * Common print method for a job resource.
     *
     * @param resource Job resource
     * @param stringBuilder String to be printed first
     */
    public static void print(Job resource, StringBuilder stringBuilder) {
        stringBuilder.append("\n\tPrinting job details for ").append(resource.id())
            .append("\n\t- Job name: ").append(resource.name())
            .append("\n\t      state: ").append(resource.state())
            .append("\n\t      status: ").append(resource.status())
            .append("\n\t      start time: ").append(resource.startTime())
            .append("\n\t    Job action: ")
            .append("\n\t      Action type: ").append(resource.action().type().name())
            .append("\n\t      Action URI: ").append(resource.action().request().uri())
            .append("\n\t      Action method: ").append(resource.action().request().method());
        if (resource.action().retryPolicy() != null) {
            stringBuilder.append("\n\t      Action retry policy type: ").append(resource.action().retryPolicy().retryType());
            stringBuilder.append("\n\t      Action retry policy count: ").append(resource.action().retryPolicy().retryCount());
            stringBuilder.append("\n\t      Action retry policy interval: ").append(resource.action().retryPolicy().retryInterval());
        }
        if (resource.recurrence() != null) {
            stringBuilder.append("\n\t- Job recurrence: ")
                .append("\n\t      Frequency: ").append(resource.recurrence().frequency().name())
                .append("\n\t      Count: ").append(resource.recurrence().count())
                .append("\n\t      End time: ").append(resource.recurrence().endTime())
                .append("\n\t      Interval: ").append(resource.recurrence().interval())
                .append("\n\t      Schedule: ").append(resource.recurrence().schedule());
        }
    }

    /**
     * Common print method for a job history resource.
     *
     * @param resource Job history resource
     * @param stringBuilder String to be printed first
     */
    public static void print(JobHistory resource, StringBuilder stringBuilder) {
        stringBuilder.append("\n\tPrinting job history details for ").append(resource.id())
            .append("\n\t- Job history name: ").append(resource.name())
            .append("\n\t              type: ").append(resource.type())
            .append("\n\t              status: ").append(resource.status())
            .append("\n\t              start time: ").append(resource.startTime())
            .append("\n\t              end time: ").append(resource.endTime())
            .append("\n\t              action name: ").append(resource.actionName())
            .append("\n\t              repeat count: ").append(resource.repeatCount())
            .append("\n\t              retry count: ").append(resource.retryCount())
            .append("\n\t              expected execution time: ").append(resource.expectedExecutionTime())
            .append("\n\t              message: ").append(resource.message());
    }
}
