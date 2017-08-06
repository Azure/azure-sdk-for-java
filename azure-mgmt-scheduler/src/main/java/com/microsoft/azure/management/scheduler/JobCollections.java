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
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.scheduler.implementation.JobCollectionsInner;
import com.microsoft.azure.management.scheduler.implementation.ScheduleServiceManager;
import rx.Completable;
import rx.Observable;

/**
 * Entry point to Job Collection management API for Scheduler service in Azure.
 */
@Fluent()
@Beta(Beta.SinceVersion.V1_2_0)
public interface JobCollections extends
    SupportsCreating<JobCollection.DefinitionStages.Blank>,
    HasManager<ScheduleServiceManager>,
    HasInner<JobCollectionsInner>,
    SupportsBatchCreation<JobCollection>,
    SupportsGettingById<JobCollection>,
    SupportsDeletingById,
    SupportsDeletingByResourceGroup,
    SupportsListingByResourceGroup<JobCollection>,
    SupportsGettingByResourceGroup<JobCollection>,
    SupportsListing<JobCollection> {

    /***********************************************************
     * Actions
     ***********************************************************/

    /**
     * Enables all of the jobs in the job collection.
     *
     * @param resourceGroupName the resource group name
     * @param jobCollectionName the job collection name
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    void enable(String resourceGroupName, String jobCollectionName);

    /**
     * Enables all of the jobs in the job collection.
     *
     * @param resourceGroupName the resource group name
     * @param jobCollectionName the job collection name
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return a representation of the future computation of this call
     */
    Completable enableAsync(String resourceGroupName, String jobCollectionName);

    /**
     * Disables all of the jobs in the job collection.
     *
     * @param resourceGroupName the resource group name containing the job collection
     * @param jobCollectionName the job collection name
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    void disable(String resourceGroupName, String jobCollectionName);

    /**
     * Disables all of the jobs in the job collection.
     *
     * @param resourceGroupName the resource group name containing the job collection
     * @param jobCollectionName the job collection
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return a representation of the future computation of this call
     */
    Completable disableAsync(String resourceGroupName, String jobCollectionName);

    /**
     * Runs a job from the specified job collection.
     *
     * @param resourceGroupName the resource group name containing the job collection
     * @param jobCollectionName the job collection name containing the job
     * @param jobName the job name
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    void runJobByName(String resourceGroupName, String jobCollectionName, String jobName);

    /**
     * Runs a job from the specified job collection.
     *
     * @param resourceGroupName the resource group name containing the job collection
     * @param jobCollectionName the job collection name containing the job
     * @param jobName the job name
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return a representation of the future computation of this call
     */
    Completable runJobByNameAsync(String resourceGroupName, String jobCollectionName, String jobName);

    /**
     * Gets a job from the specified job collection.
     *
     * @param resourceGroupName the resource group name containing the job collection
     * @param jobCollectionName the job collection name containing the job
     * @param jobName the job name
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the Job object if successful
     */
    Job getJobByName(String resourceGroupName, String jobCollectionName, String jobName);

    /**
     * Gets a job from the specified job collection.
     *
     * @param resourceGroupName The resource group name containing the job collection
     * @param jobCollectionName The job collection name containing the job
     * @param jobName The job name.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the Job object
     */
    Observable<Job> getJobByNameAsync(String resourceGroupName, String jobCollectionName, String jobName);

    /**
     * Deletes a job from the specified job collection.
     *
     * @param resourceGroupName the resource group name containing the job collection
     * @param jobCollectionName the job collection name containing the job
     * @param jobName the job name
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    void deleteJobByName(String resourceGroupName, String jobCollectionName, String jobName);

    /**
     * Deletes a job from the specified job collection.
     *
     * @param resourceGroupName the resource group name containing the job collection
     * @param jobCollectionName the job collection name containing the job
     * @param jobName the job name
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return a representation of the future computation of this call
     */
    Completable deleteJobByNameAsync(String resourceGroupName, String jobCollectionName, String jobName);

    /**
     * Lists job history from the specified job collection.
     *
     * @param resourceGroupName the resource group name containing the job collection
     * @param jobCollectionName the job collection name containing the job
     * @param jobName the job name
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return a representation of the future computation of this call
     */
    PagedList<JobHistory> listJobHistory(String resourceGroupName, String jobCollectionName, String jobName);

    /**
     * Lists job history from the specified job collection.
     *
     * @param resourceGroupName the resource group name containing the job collection
     * @param jobCollectionName the job collection name containing the job
     * @param jobName the job name
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return a representation of the future computation of this call
     */
    Observable<JobHistory> listJobHistoryAsync(String resourceGroupName, String jobCollectionName, String jobName);
}
