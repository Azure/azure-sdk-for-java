/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol;

import com.microsoft.azure.batch.protocol.models.BatchErrorException;
import com.microsoft.azure.batch.protocol.models.CloudJobSchedule;
import com.microsoft.azure.batch.protocol.models.JobScheduleAddHeaders;
import com.microsoft.azure.batch.protocol.models.JobScheduleAddOptions;
import com.microsoft.azure.batch.protocol.models.JobScheduleAddParameter;
import com.microsoft.azure.batch.protocol.models.JobScheduleDeleteHeaders;
import com.microsoft.azure.batch.protocol.models.JobScheduleDeleteOptions;
import com.microsoft.azure.batch.protocol.models.JobScheduleDisableHeaders;
import com.microsoft.azure.batch.protocol.models.JobScheduleDisableOptions;
import com.microsoft.azure.batch.protocol.models.JobScheduleEnableHeaders;
import com.microsoft.azure.batch.protocol.models.JobScheduleEnableOptions;
import com.microsoft.azure.batch.protocol.models.JobScheduleExistsHeaders;
import com.microsoft.azure.batch.protocol.models.JobScheduleExistsOptions;
import com.microsoft.azure.batch.protocol.models.JobScheduleGetHeaders;
import com.microsoft.azure.batch.protocol.models.JobScheduleGetOptions;
import com.microsoft.azure.batch.protocol.models.JobScheduleListHeaders;
import com.microsoft.azure.batch.protocol.models.JobScheduleListNextOptions;
import com.microsoft.azure.batch.protocol.models.JobScheduleListOptions;
import com.microsoft.azure.batch.protocol.models.JobSchedulePatchHeaders;
import com.microsoft.azure.batch.protocol.models.JobSchedulePatchOptions;
import com.microsoft.azure.batch.protocol.models.JobSchedulePatchParameter;
import com.microsoft.azure.batch.protocol.models.JobScheduleTerminateHeaders;
import com.microsoft.azure.batch.protocol.models.JobScheduleTerminateOptions;
import com.microsoft.azure.batch.protocol.models.JobScheduleUpdateHeaders;
import com.microsoft.azure.batch.protocol.models.JobScheduleUpdateOptions;
import com.microsoft.azure.batch.protocol.models.JobScheduleUpdateParameter;
import com.microsoft.azure.batch.protocol.models.PageImpl;
import com.microsoft.azure.ListOperationCallback;
import com.microsoft.azure.PagedList;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponseWithHeaders;
import java.io.IOException;

/**
 * An instance of this class provides access to all the operations defined
 * in JobSchedules.
 */
public interface JobSchedules {
    /**
     * Checks the specified job schedule exists.
     *
     * @param jobScheduleId The id of the job schedule which you want to check.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the boolean object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<Boolean, JobScheduleExistsHeaders> exists(String jobScheduleId) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Checks the specified job schedule exists.
     *
     * @param jobScheduleId The id of the job schedule which you want to check.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall existsAsync(String jobScheduleId, final ServiceCallback<Boolean> serviceCallback) throws IllegalArgumentException;
    /**
     * Checks the specified job schedule exists.
     *
     * @param jobScheduleId The id of the job schedule which you want to check.
     * @param jobScheduleExistsOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the boolean object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<Boolean, JobScheduleExistsHeaders> exists(String jobScheduleId, JobScheduleExistsOptions jobScheduleExistsOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Checks the specified job schedule exists.
     *
     * @param jobScheduleId The id of the job schedule which you want to check.
     * @param jobScheduleExistsOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall existsAsync(String jobScheduleId, JobScheduleExistsOptions jobScheduleExistsOptions, final ServiceCallback<Boolean> serviceCallback) throws IllegalArgumentException;

    /**
     * Deletes a job schedule from the specified account.
     *
     * @param jobScheduleId The id of the job schedule to delete.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, JobScheduleDeleteHeaders> delete(String jobScheduleId) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Deletes a job schedule from the specified account.
     *
     * @param jobScheduleId The id of the job schedule to delete.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall deleteAsync(String jobScheduleId, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;
    /**
     * Deletes a job schedule from the specified account.
     *
     * @param jobScheduleId The id of the job schedule to delete.
     * @param jobScheduleDeleteOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, JobScheduleDeleteHeaders> delete(String jobScheduleId, JobScheduleDeleteOptions jobScheduleDeleteOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Deletes a job schedule from the specified account.
     *
     * @param jobScheduleId The id of the job schedule to delete.
     * @param jobScheduleDeleteOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall deleteAsync(String jobScheduleId, JobScheduleDeleteOptions jobScheduleDeleteOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;

    /**
     * Gets information about the specified job schedule.
     *
     * @param jobScheduleId The id of the job schedule to get.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the CloudJobSchedule object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<CloudJobSchedule, JobScheduleGetHeaders> get(String jobScheduleId) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Gets information about the specified job schedule.
     *
     * @param jobScheduleId The id of the job schedule to get.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall getAsync(String jobScheduleId, final ServiceCallback<CloudJobSchedule> serviceCallback) throws IllegalArgumentException;
    /**
     * Gets information about the specified job schedule.
     *
     * @param jobScheduleId The id of the job schedule to get.
     * @param jobScheduleGetOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the CloudJobSchedule object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<CloudJobSchedule, JobScheduleGetHeaders> get(String jobScheduleId, JobScheduleGetOptions jobScheduleGetOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Gets information about the specified job schedule.
     *
     * @param jobScheduleId The id of the job schedule to get.
     * @param jobScheduleGetOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall getAsync(String jobScheduleId, JobScheduleGetOptions jobScheduleGetOptions, final ServiceCallback<CloudJobSchedule> serviceCallback) throws IllegalArgumentException;

    /**
     * Updates the properties of the specified job schedule.
     *
     * @param jobScheduleId The id of the job schedule to update.
     * @param jobSchedulePatchParameter The parameters for the request.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, JobSchedulePatchHeaders> patch(String jobScheduleId, JobSchedulePatchParameter jobSchedulePatchParameter) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Updates the properties of the specified job schedule.
     *
     * @param jobScheduleId The id of the job schedule to update.
     * @param jobSchedulePatchParameter The parameters for the request.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall patchAsync(String jobScheduleId, JobSchedulePatchParameter jobSchedulePatchParameter, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;
    /**
     * Updates the properties of the specified job schedule.
     *
     * @param jobScheduleId The id of the job schedule to update.
     * @param jobSchedulePatchParameter The parameters for the request.
     * @param jobSchedulePatchOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, JobSchedulePatchHeaders> patch(String jobScheduleId, JobSchedulePatchParameter jobSchedulePatchParameter, JobSchedulePatchOptions jobSchedulePatchOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Updates the properties of the specified job schedule.
     *
     * @param jobScheduleId The id of the job schedule to update.
     * @param jobSchedulePatchParameter The parameters for the request.
     * @param jobSchedulePatchOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall patchAsync(String jobScheduleId, JobSchedulePatchParameter jobSchedulePatchParameter, JobSchedulePatchOptions jobSchedulePatchOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;

    /**
     * Updates the properties of the specified job schedule.
     *
     * @param jobScheduleId The id of the job schedule to update.
     * @param jobScheduleUpdateParameter The parameters for the request.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, JobScheduleUpdateHeaders> update(String jobScheduleId, JobScheduleUpdateParameter jobScheduleUpdateParameter) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Updates the properties of the specified job schedule.
     *
     * @param jobScheduleId The id of the job schedule to update.
     * @param jobScheduleUpdateParameter The parameters for the request.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall updateAsync(String jobScheduleId, JobScheduleUpdateParameter jobScheduleUpdateParameter, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;
    /**
     * Updates the properties of the specified job schedule.
     *
     * @param jobScheduleId The id of the job schedule to update.
     * @param jobScheduleUpdateParameter The parameters for the request.
     * @param jobScheduleUpdateOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, JobScheduleUpdateHeaders> update(String jobScheduleId, JobScheduleUpdateParameter jobScheduleUpdateParameter, JobScheduleUpdateOptions jobScheduleUpdateOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Updates the properties of the specified job schedule.
     *
     * @param jobScheduleId The id of the job schedule to update.
     * @param jobScheduleUpdateParameter The parameters for the request.
     * @param jobScheduleUpdateOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall updateAsync(String jobScheduleId, JobScheduleUpdateParameter jobScheduleUpdateParameter, JobScheduleUpdateOptions jobScheduleUpdateOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;

    /**
     * Disables a job schedule.
     *
     * @param jobScheduleId The id of the job schedule to disable.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, JobScheduleDisableHeaders> disable(String jobScheduleId) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Disables a job schedule.
     *
     * @param jobScheduleId The id of the job schedule to disable.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall disableAsync(String jobScheduleId, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;
    /**
     * Disables a job schedule.
     *
     * @param jobScheduleId The id of the job schedule to disable.
     * @param jobScheduleDisableOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, JobScheduleDisableHeaders> disable(String jobScheduleId, JobScheduleDisableOptions jobScheduleDisableOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Disables a job schedule.
     *
     * @param jobScheduleId The id of the job schedule to disable.
     * @param jobScheduleDisableOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall disableAsync(String jobScheduleId, JobScheduleDisableOptions jobScheduleDisableOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;

    /**
     * Enables a job schedule.
     *
     * @param jobScheduleId The id of the job schedule to enable.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, JobScheduleEnableHeaders> enable(String jobScheduleId) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Enables a job schedule.
     *
     * @param jobScheduleId The id of the job schedule to enable.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall enableAsync(String jobScheduleId, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;
    /**
     * Enables a job schedule.
     *
     * @param jobScheduleId The id of the job schedule to enable.
     * @param jobScheduleEnableOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, JobScheduleEnableHeaders> enable(String jobScheduleId, JobScheduleEnableOptions jobScheduleEnableOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Enables a job schedule.
     *
     * @param jobScheduleId The id of the job schedule to enable.
     * @param jobScheduleEnableOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall enableAsync(String jobScheduleId, JobScheduleEnableOptions jobScheduleEnableOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;

    /**
     * Terminates a job schedule.
     *
     * @param jobScheduleId The id of the job schedule to terminates.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, JobScheduleTerminateHeaders> terminate(String jobScheduleId) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Terminates a job schedule.
     *
     * @param jobScheduleId The id of the job schedule to terminates.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall terminateAsync(String jobScheduleId, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;
    /**
     * Terminates a job schedule.
     *
     * @param jobScheduleId The id of the job schedule to terminates.
     * @param jobScheduleTerminateOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, JobScheduleTerminateHeaders> terminate(String jobScheduleId, JobScheduleTerminateOptions jobScheduleTerminateOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Terminates a job schedule.
     *
     * @param jobScheduleId The id of the job schedule to terminates.
     * @param jobScheduleTerminateOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall terminateAsync(String jobScheduleId, JobScheduleTerminateOptions jobScheduleTerminateOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;

    /**
     * Adds a job schedule to the specified account.
     *
     * @param cloudJobSchedule The job schedule to be added.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, JobScheduleAddHeaders> add(JobScheduleAddParameter cloudJobSchedule) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Adds a job schedule to the specified account.
     *
     * @param cloudJobSchedule The job schedule to be added.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall addAsync(JobScheduleAddParameter cloudJobSchedule, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;
    /**
     * Adds a job schedule to the specified account.
     *
     * @param cloudJobSchedule The job schedule to be added.
     * @param jobScheduleAddOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    ServiceResponseWithHeaders<Void, JobScheduleAddHeaders> add(JobScheduleAddParameter cloudJobSchedule, JobScheduleAddOptions jobScheduleAddOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Adds a job schedule to the specified account.
     *
     * @param cloudJobSchedule The job schedule to be added.
     * @param jobScheduleAddOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall addAsync(JobScheduleAddParameter cloudJobSchedule, JobScheduleAddOptions jobScheduleAddOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException;

    /**
     * Lists all of the job schedules in the specified account.
     *
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;CloudJobSchedule&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<PagedList<CloudJobSchedule>, JobScheduleListHeaders> list() throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Lists all of the job schedules in the specified account.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listAsync(final ListOperationCallback<CloudJobSchedule> serviceCallback) throws IllegalArgumentException;
    /**
     * Lists all of the job schedules in the specified account.
     *
     * @param jobScheduleListOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;CloudJobSchedule&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<PagedList<CloudJobSchedule>, JobScheduleListHeaders> list(final JobScheduleListOptions jobScheduleListOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Lists all of the job schedules in the specified account.
     *
     * @param jobScheduleListOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listAsync(final JobScheduleListOptions jobScheduleListOptions, final ListOperationCallback<CloudJobSchedule> serviceCallback) throws IllegalArgumentException;

    /**
     * Lists all of the job schedules in the specified account.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;CloudJobSchedule&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<PageImpl<CloudJobSchedule>, JobScheduleListHeaders> listNext(final String nextPageLink) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Lists all of the job schedules in the specified account.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<CloudJobSchedule> serviceCallback) throws IllegalArgumentException;
    /**
     * Lists all of the job schedules in the specified account.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param jobScheduleListNextOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;CloudJobSchedule&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    ServiceResponseWithHeaders<PageImpl<CloudJobSchedule>, JobScheduleListHeaders> listNext(final String nextPageLink, final JobScheduleListNextOptions jobScheduleListNextOptions) throws BatchErrorException, IOException, IllegalArgumentException;

    /**
     * Lists all of the job schedules in the specified account.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param jobScheduleListNextOptions Additional parameters for the operation
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    ServiceCall listNextAsync(final String nextPageLink, final JobScheduleListNextOptions jobScheduleListNextOptions, final ServiceCall serviceCall, final ListOperationCallback<CloudJobSchedule> serviceCallback) throws IllegalArgumentException;

}
