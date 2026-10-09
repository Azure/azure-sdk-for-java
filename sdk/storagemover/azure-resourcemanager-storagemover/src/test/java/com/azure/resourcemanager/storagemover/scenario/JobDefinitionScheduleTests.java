// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storagemover.scenario;

import com.azure.resourcemanager.storagemover.models.AzureStorageBlobContainerEndpointProperties;
import com.azure.resourcemanager.storagemover.models.CopyMode;
import com.azure.resourcemanager.storagemover.models.DataIntegrityValidation;
import com.azure.resourcemanager.storagemover.models.Endpoint;
import com.azure.resourcemanager.storagemover.models.Frequency;
import com.azure.resourcemanager.storagemover.models.JobDefinition;
import com.azure.resourcemanager.storagemover.models.NfsMountEndpointProperties;
import com.azure.resourcemanager.storagemover.models.Project;
import com.azure.resourcemanager.storagemover.models.ScheduleInfo;
import com.azure.resourcemanager.storagemover.models.SchedulerTime;
import com.azure.resourcemanager.storagemover.models.StorageMover;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;

/**
 * Mirrors {@code JobDefinitionScheduleTests.cs} from the .NET source-of-truth.
 *
 * <p>All schedule {@code startDate}/{@code endDate} values are derived from
 * {@link #scheduleStartDate()} (a {@code Z}-offset {@code OffsetDateTime} of
 * {@code now() + 1 day} captured via {@code testResourceNamer.now()} so the
 * value is recorded once and replayed deterministically) so playback recordings
 * stay portable and the server does not see the {@code +00:00}-suffix that
 * triggers an RP bug — see the cross-language playbook.
 */
public class JobDefinitionScheduleTests extends StorageMoverManagementTestBase {

    @Test
    public void createJobDefinitionWithWeeklySchedule() {
        TestContext ctx = provisionParents();
        String jobDefinitionName = generateRandomResourceName("jobdef-sched-", 24);

        OffsetDateTime start = scheduleStartDate();
        ScheduleInfo schedule = new ScheduleInfo().withFrequency(Frequency.WEEKLY)
            .withIsActive(true)
            .withExecutionTime(new SchedulerTime().withHour(2))
            .withStartDate(start)
            .withEndDate(start.plusDays(30))
            .withDaysOfWeek(Arrays.asList("Monday", "Wednesday", "Friday"));

        JobDefinition jobDefinition = storageMoverManager.jobDefinitions()
            .define(jobDefinitionName)
            .withExistingProject(resourceGroupName, ctx.storageMoverName, ctx.projectName)
            .withCopyMode(CopyMode.ADDITIVE)
            .withSourceName(ctx.sourceName)
            .withTargetName(ctx.targetName)
            .withDescription("Job definition with weekly schedule")
            .withDataIntegrityValidation(DataIntegrityValidation.SAVE_VERIFY_FILE_MD5)
            .withSchedule(schedule)
            .create();

        Assertions.assertEquals(jobDefinitionName, jobDefinition.name());
        Assertions.assertEquals(ctx.sourceName, jobDefinition.sourceName());
        Assertions.assertEquals(ctx.targetName, jobDefinition.targetName());
        Assertions.assertEquals(CopyMode.ADDITIVE, jobDefinition.copyMode());
        Assertions.assertEquals("Job definition with weekly schedule", jobDefinition.description());

        Assertions.assertNotNull(jobDefinition.schedule());
        Assertions.assertEquals(Frequency.WEEKLY, jobDefinition.schedule().frequency());
        Assertions.assertEquals(Boolean.TRUE, jobDefinition.schedule().isActive());
        Assertions.assertEquals(2, jobDefinition.schedule().executionTime().hour());
        Assertions.assertEquals(3, jobDefinition.schedule().daysOfWeek().size());

        JobDefinition refetched = storageMoverManager.jobDefinitions()
            .get(resourceGroupName, ctx.storageMoverName, ctx.projectName, jobDefinitionName);
        Assertions.assertEquals(jobDefinitionName, refetched.name());
        Assertions.assertNotNull(refetched.schedule());
        Assertions.assertEquals(Frequency.WEEKLY, refetched.schedule().frequency());

        storageMoverManager.jobDefinitions()
            .delete(resourceGroupName, ctx.storageMoverName, ctx.projectName, jobDefinitionName);
        assertNotFound(() -> storageMoverManager.jobDefinitions()
            .get(resourceGroupName, ctx.storageMoverName, ctx.projectName, jobDefinitionName));
    }

    @Test
    public void createJobDefinitionWithDailyScheduleAndPreservePermissions() {
        TestContext ctx = provisionParents();
        String jobDefinitionName = generateRandomResourceName("jobdef-daily-", 24);

        OffsetDateTime start = scheduleStartDate();
        ScheduleInfo schedule = new ScheduleInfo().withFrequency(Frequency.DAILY)
            .withIsActive(true)
            .withExecutionTime(new SchedulerTime().withHour(0))
            .withStartDate(start)
            .withEndDate(start.plusDays(30));

        JobDefinition jobDefinition = storageMoverManager.jobDefinitions()
            .define(jobDefinitionName)
            .withExistingProject(resourceGroupName, ctx.storageMoverName, ctx.projectName)
            .withCopyMode(CopyMode.MIRROR)
            .withSourceName(ctx.sourceName)
            .withTargetName(ctx.targetName)
            .withDescription("Job definition with daily schedule")
            .withDataIntegrityValidation(DataIntegrityValidation.NONE)
            .withPreservePermissions(true)
            .withSchedule(schedule)
            .create();

        Assertions.assertEquals(jobDefinitionName, jobDefinition.name());
        Assertions.assertEquals(CopyMode.MIRROR, jobDefinition.copyMode());
        Assertions.assertNotNull(jobDefinition.schedule());
        Assertions.assertEquals(Frequency.DAILY, jobDefinition.schedule().frequency());
        Assertions.assertEquals(Boolean.TRUE, jobDefinition.schedule().isActive());

        storageMoverManager.jobDefinitions()
            .delete(resourceGroupName, ctx.storageMoverName, ctx.projectName, jobDefinitionName);
    }

    @Test
    public void createJobDefinitionWithOnetimeSchedule() {
        TestContext ctx = provisionParents();
        String jobDefinitionName = generateRandomResourceName("jobdef-once-", 24);

        // Onetime is the only frequency that may omit endDate.
        ScheduleInfo schedule = new ScheduleInfo().withFrequency(Frequency.ONETIME)
            .withIsActive(true)
            .withExecutionTime(new SchedulerTime().withHour(10))
            .withStartDate(scheduleStartDate())
            .withDaysOfWeek(Collections.<String>emptyList());

        JobDefinition jobDefinition = storageMoverManager.jobDefinitions()
            .define(jobDefinitionName)
            .withExistingProject(resourceGroupName, ctx.storageMoverName, ctx.projectName)
            .withCopyMode(CopyMode.ADDITIVE)
            .withSourceName(ctx.sourceName)
            .withTargetName(ctx.targetName)
            .withDescription("Job definition with one-time schedule")
            .withSchedule(schedule)
            .create();

        Assertions.assertEquals(jobDefinitionName, jobDefinition.name());
        Assertions.assertNotNull(jobDefinition.schedule());
        Assertions.assertEquals(Frequency.ONETIME, jobDefinition.schedule().frequency());
        Assertions.assertEquals(Boolean.TRUE, jobDefinition.schedule().isActive());

        storageMoverManager.jobDefinitions()
            .delete(resourceGroupName, ctx.storageMoverName, ctx.projectName, jobDefinitionName);
    }

    private TestContext provisionParents() {
        TestContext ctx = new TestContext();
        ctx.storageMoverName = generateRandomResourceName("stomover-sched-", 24);
        ctx.projectName = generateRandomResourceName("project-sched-", 24);
        ctx.sourceName = generateRandomResourceName("nfsep-", 24);
        ctx.targetName = generateRandomResourceName("blobep-", 24);

        StorageMover sm = storageMoverManager.storageMovers()
            .define(ctx.storageMoverName)
            .withRegion(DEFAULT_REGION)
            .withExistingResourceGroup(resourceGroupName)
            .create();

        Project project = storageMoverManager.projects()
            .define(ctx.projectName)
            .withExistingStorageMover(resourceGroupName, sm.name())
            .create();

        Endpoint sourceEndpoint = storageMoverManager.endpoints()
            .define(ctx.sourceName)
            .withExistingStorageMover(resourceGroupName, sm.name())
            .withProperties(new NfsMountEndpointProperties().withHost("10.0.0.1").withExport("/"))
            .create();

        Endpoint targetEndpoint
            = storageMoverManager.endpoints()
                .define(ctx.targetName)
                .withExistingStorageMover(resourceGroupName, sm.name())
                .withProperties(new AzureStorageBlobContainerEndpointProperties()
                    .withStorageAccountResourceId(FAKE_STORAGE_ACCOUNT_ID)
                    .withBlobContainerName("testcontainer"))
                .create();

        // Reference the created resources to avoid unused-variable warnings.
        Assertions.assertNotNull(project);
        Assertions.assertNotNull(sourceEndpoint);
        Assertions.assertNotNull(targetEndpoint);
        return ctx;
    }

    private static final class TestContext {
        String storageMoverName;
        String projectName;
        String sourceName;
        String targetName;
    }
}
