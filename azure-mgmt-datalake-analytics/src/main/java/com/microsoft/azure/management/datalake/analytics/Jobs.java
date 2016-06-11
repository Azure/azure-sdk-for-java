/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.ListOperationCallback;
import com.microsoft.azure.management.datalake.analytics.models.JobDataPath;
import com.microsoft.azure.management.datalake.analytics.models.JobInformation;
import com.microsoft.azure.management.datalake.analytics.models.JobStatistics;
import com.microsoft.azure.management.datalake.analytics.models.PageImpl;
import com.microsoft.azure.PagedList;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * An instance of this class provides access to all the operations defined
 * in Jobs.
 */
public interface Jobs {
    /**
     * Gets statistics of the specified job.
     *
     * @param accountName The Azure Data Lake Analytics account to execute job operations on.
     * @param jobIdentity JobInfo ID.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the JobStatistics object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<JobStatistics> getStatistics(String accountName, UUID jobIdentity) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Gets statistics of the specified job.
     *
     * @param accountName The Azure Data Lake Analytics account to execute job operations on.
     * @param jobIdentity JobInfo ID.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall getStatisticsAsync(String accountName, UUID jobIdentity, final ServiceCallback<JobStatistics> serviceCallback) throws IllegalArgumentException;

    /**
     * Gets the job debug data information specified by the job ID.
     *
     * @param accountName The Azure Data Lake Analytics account to execute job operations on.
     * @param jobIdentity JobInfo ID.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the JobDataPath object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<JobDataPath> getDebugDataPath(String accountName, UUID jobIdentity) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Gets the job debug data information specified by the job ID.
     *
     * @param accountName The Azure Data Lake Analytics account to execute job operations on.
     * @param jobIdentity JobInfo ID.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall getDebugDataPathAsync(String accountName, UUID jobIdentity, final ServiceCallback<JobDataPath> serviceCallback) throws IllegalArgumentException;

    /**
     * Builds (compiles) the specified job in the specified Data Lake Analytics account for job correctness and validation.
     *
     * @param accountName The Azure Data Lake Analytics account to execute job operations on.
     * @param parameters The parameters to build a job.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the JobInformation object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<JobInformation> build(String accountName, JobInformation parameters) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Builds (compiles) the specified job in the specified Data Lake Analytics account for job correctness and validation.
     *
     * @param accountName The Azure Data Lake Analytics account to execute job operations on.
     * @param parameters The parameters to build a job.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall buildAsync(String accountName, JobInformation parameters, final ServiceCallback<JobInformation> serviceCallback) throws IllegalArgumentException;

    /**
     * Cancels the running job specified by the job ID.
     *
     * @param accountName The Azure Data Lake Analytics account to execute job operations on.
     * @param jobIdentity JobInfo ID to cancel.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    ServiceResponse<Void> cancel(String accountName, UUID jobIdentity) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Cancels the running job specified by the job ID.
     *
     * @param accountName The Azure Data Lake Analytics account to execute job operations on.
     * @param jobIdentity JobInfo ID to cancel.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall cancelAsync(String accountName, UUID jobIdentity, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;

    /**
     * Gets the job information for the specified job ID.
     *
     * @param accountName The Azure Data Lake Analytics account to execute job operations on.
     * @param jobIdentity JobInfo ID.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the JobInformation object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<JobInformation> get(String accountName, UUID jobIdentity) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Gets the job information for the specified job ID.
     *
     * @param accountName The Azure Data Lake Analytics account to execute job operations on.
     * @param jobIdentity JobInfo ID.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall getAsync(String accountName, UUID jobIdentity, final ServiceCallback<JobInformation> serviceCallback) throws IllegalArgumentException;

    /**
     * Submits a job to the specified Data Lake Analytics account.
     *
     * @param accountName The Azure Data Lake Analytics account to execute job operations on.
     * @param jobIdentity The job ID (a GUID) for the job being submitted.
     * @param parameters The parameters to submit a job.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the JobInformation object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<JobInformation> create(String accountName, UUID jobIdentity, JobInformation parameters) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Submits a job to the specified Data Lake Analytics account.
     *
     * @param accountName The Azure Data Lake Analytics account to execute job operations on.
     * @param jobIdentity The job ID (a GUID) for the job being submitted.
     * @param parameters The parameters to submit a job.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall createAsync(String accountName, UUID jobIdentity, JobInformation parameters, final ServiceCallback<JobInformation> serviceCallback) throws IllegalArgumentException;

    /**
     * Lists the jobs, if any, associated with the specified Data Lake Analytics account. The response includes a link to the next page of results, if any.
     *
     * @param accountName The Azure Data Lake Analytics account to execute job operations on.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;JobInformation&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PagedList<JobInformation>> list(final String accountName) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Lists the jobs, if any, associated with the specified Data Lake Analytics account. The response includes a link to the next page of results, if any.
     *
     * @param accountName The Azure Data Lake Analytics account to execute job operations on.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listAsync(final String accountName, final ListOperationCallback<JobInformation> serviceCallback) throws IllegalArgumentException;
    /**
     * Lists the jobs, if any, associated with the specified Data Lake Analytics account. The response includes a link to the next page of results, if any.
     *
     * @param accountName The Azure Data Lake Analytics account to execute job operations on.
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories?$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count The Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @param search A free form search. A free-text search expression to match for whether a particular entry should be included in the feed, e.g. Categories?$search=blue OR green. Optional.
     * @param format The return format. Return the response in particular formatxii without access to request headers for standard content-type negotiation (e.g Orders?$format=json). Optional.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;JobInformation&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PagedList<JobInformation>> list(final String accountName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count, final String search, final String format) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Lists the jobs, if any, associated with the specified Data Lake Analytics account. The response includes a link to the next page of results, if any.
     *
     * @param accountName The Azure Data Lake Analytics account to execute job operations on.
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories?$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count The Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @param search A free form search. A free-text search expression to match for whether a particular entry should be included in the feed, e.g. Categories?$search=blue OR green. Optional.
     * @param format The return format. Return the response in particular formatxii without access to request headers for standard content-type negotiation (e.g Orders?$format=json). Optional.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listAsync(final String accountName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count, final String search, final String format, final ListOperationCallback<JobInformation> serviceCallback) throws IllegalArgumentException;

    /**
     * Lists the jobs, if any, associated with the specified Data Lake Analytics account. The response includes a link to the next page of results, if any.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;JobInformation&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    ServiceResponse<PageImpl<JobInformation>> listNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException;

    /**
     * Lists the jobs, if any, associated with the specified Data Lake Analytics account. The response includes a link to the next page of results, if any.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<JobInformation> serviceCallback) throws IllegalArgumentException;

}
