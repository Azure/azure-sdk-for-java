/**
 * Copyright 2012 Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.windowsazure.services.media;

import java.util.List;

import com.microsoft.windowsazure.services.core.FilterableService;
import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.media.models.AccessPolicyInfo;
import com.microsoft.windowsazure.services.media.models.AssetInfo;
import com.microsoft.windowsazure.services.media.models.CreateAccessPolicyOptions;
import com.microsoft.windowsazure.services.media.models.CreateAssetOptions;
import com.microsoft.windowsazure.services.media.models.CreateJobOptions;
import com.microsoft.windowsazure.services.media.models.CreateLocatorOptions;
import com.microsoft.windowsazure.services.media.models.JobInfo;
import com.microsoft.windowsazure.services.media.models.ListAccessPolicyOptions;
import com.microsoft.windowsazure.services.media.models.ListAssetsOptions;
import com.microsoft.windowsazure.services.media.models.ListJobsOptions;
import com.microsoft.windowsazure.services.media.models.ListJobsResult;
import com.microsoft.windowsazure.services.media.models.ListLocatorsOptions;
import com.microsoft.windowsazure.services.media.models.ListLocatorsResult;
import com.microsoft.windowsazure.services.media.models.ListMediaProcessorsOptions;
import com.microsoft.windowsazure.services.media.models.ListMediaProcessorsResult;
import com.microsoft.windowsazure.services.media.models.ListTasksOptions;
import com.microsoft.windowsazure.services.media.models.ListTasksResult;
import com.microsoft.windowsazure.services.media.models.LocatorInfo;
import com.microsoft.windowsazure.services.media.models.LocatorType;
import com.microsoft.windowsazure.services.media.models.UpdateAssetOptions;
import com.microsoft.windowsazure.services.media.models.UpdateLocatorOptions;
import com.sun.jersey.api.client.UniformInterfaceException;

/**
 * Defines the methods available for Windows Azure Media Services.
 */
public interface MediaContract extends FilterableService<MediaContract> {

    /**
     * Creates the asset.
     * 
     * @return the asset info
     * @throws ServiceException
     *             the service exception
     */
    public AssetInfo createAsset() throws ServiceException;

    /**
     * Creates the asset.
     * 
     * @param createAssetOptions
     *            the create asset options
     * @return the asset info
     * @throws ServiceException
     *             the service exception
     */
    public AssetInfo createAsset(CreateAssetOptions createAssetOptions) throws ServiceException;

    /**
     * Delete asset.
     * 
     * @param assetId
     *            the asset id
     * @throws ServiceException
     *             the service exception
     */
    public void deleteAsset(String assetId) throws ServiceException;

    /**
     * Gets the asset.
     * 
     * @param assetId
     *            the asset id
     * @return the asset
     * @throws ServiceException
     *             the service exception
     */
    public AssetInfo getAsset(String assetId) throws ServiceException;

    /**
     * List assets.
     * 
     * @return the list
     * @throws ServiceException
     *             the service exception
     */
    public List<AssetInfo> listAssets() throws ServiceException;

    /**
     * List assets.
     * 
     * @param listAssetsOptions
     *            the list assets options
     * @return the list
     * @throws ServiceException
     *             the service exception
     */
    public List<AssetInfo> listAssets(ListAssetsOptions listAssetsOptions) throws ServiceException;

    /**
     * Update asset.
     * 
     * @param assetId
     *            the asset id
     * @param updateAssetOptions
     *            the update asset options
     * @throws ServiceException
     *             the service exception
     */
    public void updateAsset(String assetId, UpdateAssetOptions updateAssetOptions) throws ServiceException;

    /**
     * Create the access policy.
     * 
     * @param accessPolicyName
     *            name of access policy
     * @param durationInMinutes
     *            Duration in minutes that blob access will be granted when using this access policy
     * @return Created access policy
     * @throws ServiceException
     *             the service exception
     */
    AccessPolicyInfo createAccessPolicy(String accessPolicyName, double durationInMinutes) throws ServiceException;

    /**
     * Create the access policy with the given options.
     * 
     * @param accessPolicyName
     *            name of access policy
     * @param durationInMinutes
     *            Duration in minutes that blob access will be granted when using this access policy
     * @param options
     *            options for creation
     * @return the created access policy
     * @throws ServiceException
     *             the service exception
     */
    AccessPolicyInfo createAccessPolicy(String accessPolicyName, double durationInMinutes,
            CreateAccessPolicyOptions options) throws ServiceException;

    /**
     * Delete the access policy with the given id.
     * 
     * @param accessPolicyId
     *            of access policy to delete
     * @throws ServiceException
     *             the service exception
     */
    void deleteAccessPolicy(String accessPolicyId) throws ServiceException;

    /**
     * Get a single access policy.
     * 
     * @param accessPolicyId
     *            the id of the asset to retrieve
     * @return the asset
     * @throws ServiceException
     *             the service exception
     */
    AccessPolicyInfo getAccessPolicy(String accessPolicyId) throws ServiceException;

    /**
     * List access policies.
     * 
     * @return the list
     * @throws ServiceException
     *             the service exception
     */
    List<AccessPolicyInfo> listAccessPolicies() throws ServiceException;

    /**
     * List access policies.
     * 
     * @param options
     *            the list access policy options
     * @return the list
     * @throws ServiceException
     *             the service exception
     */
    List<AccessPolicyInfo> listAccessPolicies(ListAccessPolicyOptions options) throws ServiceException;

    /**
     * Creates the locator.
     * 
     * @param accessPolicyId
     *            the access policy id
     * @param assetId
     *            the asset id
     * @param locatorType
     *            the locator type
     * @return the locator info
     * @throws ServiceException
     *             the service exception
     */
    public LocatorInfo createLocator(String accessPolicyId, String assetId, LocatorType locatorType)
            throws ServiceException;

    /**
     * Creates the locator.
     * 
     * @param accessPolicyId
     *            the access policy id
     * @param assetId
     *            the asset id
     * @param locatorType
     *            the locator type
     * @param createLocatorOptions
     *            the create locator options
     * @return the locator info
     * @throws ServiceException
     *             the service exception
     */
    public LocatorInfo createLocator(String accessPolicyId, String assetId, LocatorType locatorType,
            CreateLocatorOptions createLocatorOptions) throws ServiceException;

    /**
     * Gets the locator.
     * 
     * @param locatorId
     *            the locator id
     * @return the locator
     * @throws ServiceException
     *             the service exception
     */
    public LocatorInfo getLocator(String locatorId) throws ServiceException;

    /**
     * List locators.
     * 
     * @return the list locators result
     * @throws ServiceException
     *             the service exception
     */
    public ListLocatorsResult listLocators() throws ServiceException;

    /**
     * Delete locator.
     * 
     * @param locatorId
     *            the id
     * @throws UniformInterfaceException
     *             the uniform interface exception
     * @throws ServiceException
     *             the service exception
     */
    public void deleteLocator(String locatorId) throws UniformInterfaceException, ServiceException;

    /**
     * List locators.
     * 
     * @param listLocatorOptions
     *            the list locator options
     * @return the list locators result
     * @throws ServiceException
     *             the service exception
     */
    public ListLocatorsResult listLocators(ListLocatorsOptions listLocatorOptions) throws ServiceException;

    /**
     * Update locator.
     * 
     * @param locatorId
     *            the locator id
     * @param updateLocatorOptions
     *            the update locator options
     * @throws ServiceException
     *             the service exception
     */
    public void updateLocator(String locatorId, UpdateLocatorOptions updateLocatorOptions) throws ServiceException;

    /**
     * List media processors.
     * 
     * @return the list media processors result
     * @throws ServiceException
     *             the service exception
     */
    public ListMediaProcessorsResult listMediaProcessors() throws ServiceException;

    /**
     * List media processors.
     * 
     * @param listMediaProcessorsOptions
     *            the list media processors options
     * @return the list media processors result
     * @throws ServiceException
     *             the service exception
     */
    public ListMediaProcessorsResult listMediaProcessors(ListMediaProcessorsOptions listMediaProcessorsOptions)
            throws ServiceException;

    /**
     * Creates the job.
     * 
     * @param createJobOptions
     *            the create job options
     * @return the job info
     * @throws ServiceException
     *             the service exception
     */
    public JobInfo createJob(CreateJobOptions createJobOptions) throws ServiceException;

    /**
     * Gets the job.
     * 
     * @param jobId
     *            the job id
     * @return the job
     * @throws ServiceException
     *             the service exception
     */
    public JobInfo getJob(String jobId) throws ServiceException;

    /**
     * List jobs.
     * 
     * @return the list jobs result
     * @throws ServiceException
     *             the service exception
     */
    public ListJobsResult listJobs() throws ServiceException;

    /**
     * List jobs.
     * 
     * @param listJobsOptions
     *            the list jobs options
     * @return the list jobs result
     * @throws ServiceException
     *             the service exception
     */
    public ListJobsResult listJobs(ListJobsOptions listJobsOptions) throws ServiceException;

    /**
     * Cancel job.
     * 
     * @param jobId
     *            the job id
     * @throws ServiceException
     *             the service exception
     */
    public void cancelJob(String jobId) throws ServiceException;

    /**
     * List tasks.
     * 
     * @return the list tasks result
     * @throws ServiceException
     *             the service exception
     */
    public ListTasksResult listTasks() throws ServiceException;

    /**
     * List tasks.
     * 
     * @param listTasksOptions
     *            the list tasks options
     * @return the list tasks result
     * @throws ServiceException
     *             the service exception
     */
    public ListTasksResult listTasks(ListTasksOptions listTasksOptions) throws ServiceException;

    /**
     * List job tasks.
     * 
     * @param jobId
     *            the job id
     * @return the list tasks result
     * @throws ServiceException
     *             the service exception
     */
    public ListTasksResult listJobTasks(String jobId) throws ServiceException;

    /**
     * List job tasks.
     * 
     * @param jobId
     *            the job id
     * @param listTasksOptions
     *            the list tasks options
     * @return the list tasks result
     * @throws ServiceException
     *             the service exception
     */
    public ListTasksResult listJobTasks(String jobId, ListTasksOptions listTasksOptions) throws ServiceException;

}
