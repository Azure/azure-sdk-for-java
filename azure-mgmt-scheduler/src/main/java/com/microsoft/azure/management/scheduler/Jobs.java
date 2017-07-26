/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.scheduler;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByNameAsync;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingByName;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.scheduler.implementation.JobsInner;
import com.microsoft.azure.management.scheduler.implementation.ScheduleServiceManager;
import rx.Completable;
import rx.Observable;

/**
 * Entry point to Job management API for Scheduler service in Azure.
 */
@Fluent()
@Beta(Beta.SinceVersion.V1_2_0)
public interface Jobs extends
    SupportsCreating<Job.DefinitionStages.Blank>,
    SupportsGettingByNameAsync<Job>,
    SupportsDeletingByName,
    SupportsListing<Job>,
    HasManager<ScheduleServiceManager>,
    HasInner<JobsInner> {

    /**
     * Runs a job from the current job collection.
     *
     * @param jobName the job name
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    void run(String jobName);

    /**
     * Runs a job from the current job collection.
     *
     * @param jobName the job name
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return a representation of the future computation of this call
     */
    Completable runAsync(String jobName);

    /**
     * Lists job history.
     *
     * @param jobName the job name
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return a paged list of all the job history resources
     */
    PagedList<JobHistory> listJobHistory(String jobName);

    /**
     * Lists job history.
     *
     * @param jobName the job name
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return a representation of the future computation of this call
     */
    Observable<JobHistory> listJobHistoryAsync(String jobName);
}
