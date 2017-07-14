/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.scheduler.JobCollection;
import com.microsoft.azure.management.scheduler.JobCollections;
import com.microsoft.azure.management.scheduler.SkuDefinition;
import org.junit.Assert;

public class TestSchedulerService {
    /**
     * Scheduler service test.
     */
    public static class JobCollectionAnySku extends TestTemplate<JobCollection, JobCollections> {

        @Override
        public JobCollection createResource(JobCollections jobCollections) throws Exception {
            final String newName = "jobs" + this.testId;
            String rgName = "rg" + this.testId;

            JobCollection jobCollection = jobCollections.define(newName)
                .withRegion(Region.UK_WEST)
                .withNewResourceGroup(rgName)
                .withSku(SkuDefinition.STANDARD)
                .withTag("tag1", "value1")
                .create();

            Assert.assertTrue(jobCollection.tags().containsKey("tag1"));

            return jobCollection;
        }

        @Override
        public JobCollection updateResource(JobCollection resource) throws Exception {
            resource = resource.update()
                .withTag("tag2", "value2")
                .withTag("tag3", "value3")
                .withoutTag("tag1")
                .apply();
            Assert.assertTrue(resource.tags().containsKey("tag2"));
            Assert.assertTrue(!resource.tags().containsKey("tag1"));

            return resource;
        }

        @Override
        public void print(JobCollection resource) {
            TestSchedulerService.print(resource, "Job Collection with default SKU: ");
        }
    }

    /**
     * Common print method.
     *
     * @param resource Job Collection resource
     * @param header String to be printed first
     */
    public static void print(JobCollection resource, String header) {
        StringBuilder stringBuilder = new StringBuilder().append(header).append(resource.id())
            .append("Name: ").append(resource.name())
            .append("\n\tResource group: ").append(resource.resourceGroupName())
            .append("\n\tRegion: ").append(resource.region())
            .append("\n\tTags: ").append(resource.tags());

        System.out.println(stringBuilder);
    }
}
