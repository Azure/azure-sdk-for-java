/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.implementation;

import retrofit2.Retrofit;
import com.microsoft.azure.batch.protocol.Jobs;
import com.microsoft.azure.batch.protocol.BatchServiceClient;
import com.google.common.reflect.TypeToken;
import com.microsoft.azure.AzureServiceResponseBuilder;
import com.microsoft.azure.batch.protocol.models.BatchErrorException;
import com.microsoft.azure.batch.protocol.models.CloudJob;
import com.microsoft.azure.batch.protocol.models.DisableJobOption;
import com.microsoft.azure.batch.protocol.models.JobAddHeaders;
import com.microsoft.azure.batch.protocol.models.JobAddOptions;
import com.microsoft.azure.batch.protocol.models.JobAddParameter;
import com.microsoft.azure.batch.protocol.models.JobDeleteHeaders;
import com.microsoft.azure.batch.protocol.models.JobDeleteOptions;
import com.microsoft.azure.batch.protocol.models.JobDisableHeaders;
import com.microsoft.azure.batch.protocol.models.JobDisableOptions;
import com.microsoft.azure.batch.protocol.models.JobDisableParameter;
import com.microsoft.azure.batch.protocol.models.JobEnableHeaders;
import com.microsoft.azure.batch.protocol.models.JobEnableOptions;
import com.microsoft.azure.batch.protocol.models.JobGetAllJobsLifetimeStatisticsHeaders;
import com.microsoft.azure.batch.protocol.models.JobGetAllJobsLifetimeStatisticsOptions;
import com.microsoft.azure.batch.protocol.models.JobGetHeaders;
import com.microsoft.azure.batch.protocol.models.JobGetOptions;
import com.microsoft.azure.batch.protocol.models.JobListFromJobScheduleHeaders;
import com.microsoft.azure.batch.protocol.models.JobListFromJobScheduleNextOptions;
import com.microsoft.azure.batch.protocol.models.JobListFromJobScheduleOptions;
import com.microsoft.azure.batch.protocol.models.JobListHeaders;
import com.microsoft.azure.batch.protocol.models.JobListNextOptions;
import com.microsoft.azure.batch.protocol.models.JobListOptions;
import com.microsoft.azure.batch.protocol.models.JobListPreparationAndReleaseTaskStatusHeaders;
import com.microsoft.azure.batch.protocol.models.JobListPreparationAndReleaseTaskStatusNextOptions;
import com.microsoft.azure.batch.protocol.models.JobListPreparationAndReleaseTaskStatusOptions;
import com.microsoft.azure.batch.protocol.models.JobPatchHeaders;
import com.microsoft.azure.batch.protocol.models.JobPatchOptions;
import com.microsoft.azure.batch.protocol.models.JobPatchParameter;
import com.microsoft.azure.batch.protocol.models.JobPreparationAndReleaseTaskExecutionInformation;
import com.microsoft.azure.batch.protocol.models.JobStatistics;
import com.microsoft.azure.batch.protocol.models.JobTerminateHeaders;
import com.microsoft.azure.batch.protocol.models.JobTerminateOptions;
import com.microsoft.azure.batch.protocol.models.JobTerminateParameter;
import com.microsoft.azure.batch.protocol.models.JobUpdateHeaders;
import com.microsoft.azure.batch.protocol.models.JobUpdateOptions;
import com.microsoft.azure.batch.protocol.models.JobUpdateParameter;
import com.microsoft.azure.batch.protocol.models.PageImpl;
import com.microsoft.azure.ListOperationCallback;
import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.rest.DateTimeRfc1123;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponseCallback;
import com.microsoft.rest.ServiceResponseWithHeaders;
import com.microsoft.rest.Validator;
import java.io.IOException;
import java.util.List;
import okhttp3.ResponseBody;
import org.joda.time.DateTime;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.HTTP;
import retrofit2.http.PATCH;
import retrofit2.http.Path;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;
import retrofit2.http.Url;
import retrofit2.Response;

/**
 * An instance of this class provides access to all the operations defined
 * in Jobs.
 */
public final class JobsImpl implements Jobs {
    /** The Retrofit service to perform REST calls. */
    private JobsService service;
    /** The service client containing this operation class. */
    private BatchServiceClient client;

    /**
     * Initializes an instance of JobsImpl.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public JobsImpl(Retrofit retrofit, BatchServiceClient client) {
        this.service = retrofit.create(JobsService.class);
        this.client = client;
    }

    /**
     * The interface defining all the services for Jobs to be
     * used by Retrofit to perform actually REST calls.
     */
    interface JobsService {
        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @GET("lifetimejobstats")
        Call<ResponseBody> getAllJobsLifetimeStatistics(@Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @HTTP(path = "jobs/{jobId}", method = "DELETE", hasBody = true)
        Call<ResponseBody> delete(@Path("jobId") String jobId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Header("If-Match") String ifMatch, @Header("If-None-Match") String ifNoneMatch, @Header("If-Modified-Since") DateTimeRfc1123 ifModifiedSince, @Header("If-Unmodified-Since") DateTimeRfc1123 ifUnmodifiedSince, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @GET("jobs/{jobId}")
        Call<ResponseBody> get(@Path("jobId") String jobId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("$select") String select, @Query("$expand") String expand, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @PATCH("jobs/{jobId}")
        Call<ResponseBody> patch(@Path("jobId") String jobId, @Body JobPatchParameter jobPatchParameter, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Header("If-Match") String ifMatch, @Header("If-None-Match") String ifNoneMatch, @Header("If-Modified-Since") DateTimeRfc1123 ifModifiedSince, @Header("If-Unmodified-Since") DateTimeRfc1123 ifUnmodifiedSince, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @PUT("jobs/{jobId}")
        Call<ResponseBody> update(@Path("jobId") String jobId, @Body JobUpdateParameter jobUpdateParameter, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Header("If-Match") String ifMatch, @Header("If-None-Match") String ifNoneMatch, @Header("If-Modified-Since") DateTimeRfc1123 ifModifiedSince, @Header("If-Unmodified-Since") DateTimeRfc1123 ifUnmodifiedSince, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @POST("jobs/{jobId}/disable")
        Call<ResponseBody> disable(@Path("jobId") String jobId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Header("If-Match") String ifMatch, @Header("If-None-Match") String ifNoneMatch, @Header("If-Modified-Since") DateTimeRfc1123 ifModifiedSince, @Header("If-Unmodified-Since") DateTimeRfc1123 ifUnmodifiedSince, @Body JobDisableParameter jobDisableParameter, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @POST("jobs/{jobId}/enable")
        Call<ResponseBody> enable(@Path("jobId") String jobId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Header("If-Match") String ifMatch, @Header("If-None-Match") String ifNoneMatch, @Header("If-Modified-Since") DateTimeRfc1123 ifModifiedSince, @Header("If-Unmodified-Since") DateTimeRfc1123 ifUnmodifiedSince, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @POST("jobs/{jobId}/terminate")
        Call<ResponseBody> terminate(@Path("jobId") String jobId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Header("If-Match") String ifMatch, @Header("If-None-Match") String ifNoneMatch, @Header("If-Modified-Since") DateTimeRfc1123 ifModifiedSince, @Header("If-Unmodified-Since") DateTimeRfc1123 ifUnmodifiedSince, @Body JobTerminateParameter jobTerminateParameter, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @POST("jobs")
        Call<ResponseBody> add(@Body JobAddParameter job, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @GET("jobs")
        Call<ResponseBody> list(@Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("$filter") String filter, @Query("$select") String select, @Query("$expand") String expand, @Query("maxresults") Integer maxResults, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @GET("jobschedules/{jobScheduleId}/jobs")
        Call<ResponseBody> listFromJobSchedule(@Path("jobScheduleId") String jobScheduleId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("$filter") String filter, @Query("$select") String select, @Query("$expand") String expand, @Query("maxresults") Integer maxResults, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @GET("jobs/{jobId}/jobpreparationandreleasetaskstatus")
        Call<ResponseBody> listPreparationAndReleaseTaskStatus(@Path("jobId") String jobId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("$filter") String filter, @Query("$select") String select, @Query("maxresults") Integer maxResults, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @GET
        Call<ResponseBody> listNext(@Url String nextPageLink, @Header("accept-language") String acceptLanguage, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @GET
        Call<ResponseBody> listFromJobScheduleNext(@Url String nextPageLink, @Header("accept-language") String acceptLanguage, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @GET
        Call<ResponseBody> listPreparationAndReleaseTaskStatusNext(@Url String nextPageLink, @Header("accept-language") String acceptLanguage, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Header("User-Agent") String userAgent);

    }

    /**
     * Gets lifetime summary statistics for all of the jobs in the specified account. Statistics are aggregated across all jobs that have ever existed in the account, from account creation to the last update time of the statistics.
     *
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the JobStatistics object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<JobStatistics, JobGetAllJobsLifetimeStatisticsHeaders> getAllJobsLifetimeStatistics() throws BatchErrorException, IOException, IllegalArgumentException {
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final JobGetAllJobsLifetimeStatisticsOptions jobGetAllJobsLifetimeStatisticsOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        Call<ResponseBody> call = service.getAllJobsLifetimeStatistics(this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
        return getAllJobsLifetimeStatisticsDelegate(call.execute());
    }

    /**
     * Gets lifetime summary statistics for all of the jobs in the specified account. Statistics are aggregated across all jobs that have ever existed in the account, from account creation to the last update time of the statistics.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getAllJobsLifetimeStatisticsAsync(final ServiceCallback<JobStatistics> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final JobGetAllJobsLifetimeStatisticsOptions jobGetAllJobsLifetimeStatisticsOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        Call<ResponseBody> call = service.getAllJobsLifetimeStatistics(this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<JobStatistics>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getAllJobsLifetimeStatisticsDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Gets lifetime summary statistics for all of the jobs in the specified account. Statistics are aggregated across all jobs that have ever existed in the account, from account creation to the last update time of the statistics.
     *
     * @param jobGetAllJobsLifetimeStatisticsOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the JobStatistics object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<JobStatistics, JobGetAllJobsLifetimeStatisticsHeaders> getAllJobsLifetimeStatistics(JobGetAllJobsLifetimeStatisticsOptions jobGetAllJobsLifetimeStatisticsOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(jobGetAllJobsLifetimeStatisticsOptions);
        Integer timeout = null;
        if (jobGetAllJobsLifetimeStatisticsOptions != null) {
            timeout = jobGetAllJobsLifetimeStatisticsOptions.timeout();
        }
        String clientRequestId = null;
        if (jobGetAllJobsLifetimeStatisticsOptions != null) {
            clientRequestId = jobGetAllJobsLifetimeStatisticsOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobGetAllJobsLifetimeStatisticsOptions != null) {
            returnClientRequestId = jobGetAllJobsLifetimeStatisticsOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobGetAllJobsLifetimeStatisticsOptions != null) {
            ocpDate = jobGetAllJobsLifetimeStatisticsOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.getAllJobsLifetimeStatistics(this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
        return getAllJobsLifetimeStatisticsDelegate(call.execute());
    }

    /**
     * Gets lifetime summary statistics for all of the jobs in the specified account. Statistics are aggregated across all jobs that have ever existed in the account, from account creation to the last update time of the statistics.
     *
     * @param jobGetAllJobsLifetimeStatisticsOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getAllJobsLifetimeStatisticsAsync(JobGetAllJobsLifetimeStatisticsOptions jobGetAllJobsLifetimeStatisticsOptions, final ServiceCallback<JobStatistics> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(jobGetAllJobsLifetimeStatisticsOptions, serviceCallback);
        Integer timeout = null;
        if (jobGetAllJobsLifetimeStatisticsOptions != null) {
            timeout = jobGetAllJobsLifetimeStatisticsOptions.timeout();
        }
        String clientRequestId = null;
        if (jobGetAllJobsLifetimeStatisticsOptions != null) {
            clientRequestId = jobGetAllJobsLifetimeStatisticsOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobGetAllJobsLifetimeStatisticsOptions != null) {
            returnClientRequestId = jobGetAllJobsLifetimeStatisticsOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobGetAllJobsLifetimeStatisticsOptions != null) {
            ocpDate = jobGetAllJobsLifetimeStatisticsOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.getAllJobsLifetimeStatistics(this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<JobStatistics>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getAllJobsLifetimeStatisticsDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<JobStatistics, JobGetAllJobsLifetimeStatisticsHeaders> getAllJobsLifetimeStatisticsDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<JobStatistics, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<JobStatistics>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, JobGetAllJobsLifetimeStatisticsHeaders.class);
    }

    /**
     * Deletes a job.
     *
     * @param jobId The id of the job to delete.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, JobDeleteHeaders> delete(String jobId) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final JobDeleteOptions jobDeleteOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTime ifModifiedSince = null;
        DateTime ifUnmodifiedSince = null;
        Call<ResponseBody> call = service.delete(jobId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted, this.client.userAgent());
        return deleteDelegate(call.execute());
    }

    /**
     * Deletes a job.
     *
     * @param jobId The id of the job to delete.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall deleteAsync(String jobId, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (jobId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter jobId is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final JobDeleteOptions jobDeleteOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTime ifModifiedSince = null;
        DateTime ifUnmodifiedSince = null;
        Call<ResponseBody> call = service.delete(jobId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(deleteDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Deletes a job.
     *
     * @param jobId The id of the job to delete.
     * @param jobDeleteOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, JobDeleteHeaders> delete(String jobId, JobDeleteOptions jobDeleteOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(jobDeleteOptions);
        Integer timeout = null;
        if (jobDeleteOptions != null) {
            timeout = jobDeleteOptions.timeout();
        }
        String clientRequestId = null;
        if (jobDeleteOptions != null) {
            clientRequestId = jobDeleteOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobDeleteOptions != null) {
            returnClientRequestId = jobDeleteOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobDeleteOptions != null) {
            ocpDate = jobDeleteOptions.ocpDate();
        }
        String ifMatch = null;
        if (jobDeleteOptions != null) {
            ifMatch = jobDeleteOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (jobDeleteOptions != null) {
            ifNoneMatch = jobDeleteOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (jobDeleteOptions != null) {
            ifModifiedSince = jobDeleteOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (jobDeleteOptions != null) {
            ifUnmodifiedSince = jobDeleteOptions.ifUnmodifiedSince();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        if (ifModifiedSince != null) {
            ifModifiedSinceConverted = new DateTimeRfc1123(ifModifiedSince);
        }
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        if (ifUnmodifiedSince != null) {
            ifUnmodifiedSinceConverted = new DateTimeRfc1123(ifUnmodifiedSince);
        }
        Call<ResponseBody> call = service.delete(jobId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted, this.client.userAgent());
        return deleteDelegate(call.execute());
    }

    /**
     * Deletes a job.
     *
     * @param jobId The id of the job to delete.
     * @param jobDeleteOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall deleteAsync(String jobId, JobDeleteOptions jobDeleteOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (jobId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter jobId is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(jobDeleteOptions, serviceCallback);
        Integer timeout = null;
        if (jobDeleteOptions != null) {
            timeout = jobDeleteOptions.timeout();
        }
        String clientRequestId = null;
        if (jobDeleteOptions != null) {
            clientRequestId = jobDeleteOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobDeleteOptions != null) {
            returnClientRequestId = jobDeleteOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobDeleteOptions != null) {
            ocpDate = jobDeleteOptions.ocpDate();
        }
        String ifMatch = null;
        if (jobDeleteOptions != null) {
            ifMatch = jobDeleteOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (jobDeleteOptions != null) {
            ifNoneMatch = jobDeleteOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (jobDeleteOptions != null) {
            ifModifiedSince = jobDeleteOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (jobDeleteOptions != null) {
            ifUnmodifiedSince = jobDeleteOptions.ifUnmodifiedSince();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        if (ifModifiedSince != null) {
            ifModifiedSinceConverted = new DateTimeRfc1123(ifModifiedSince);
        }
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        if (ifUnmodifiedSince != null) {
            ifUnmodifiedSinceConverted = new DateTimeRfc1123(ifUnmodifiedSince);
        }
        Call<ResponseBody> call = service.delete(jobId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(deleteDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<Void, JobDeleteHeaders> deleteDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(202, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, JobDeleteHeaders.class);
    }

    /**
     * Gets information about the specified job.
     *
     * @param jobId The id of the job.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the CloudJob object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<CloudJob, JobGetHeaders> get(String jobId) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final JobGetOptions jobGetOptions = null;
        String select = null;
        String expand = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        Call<ResponseBody> call = service.get(jobId, this.client.apiVersion(), this.client.acceptLanguage(), select, expand, timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
        return getDelegate(call.execute());
    }

    /**
     * Gets information about the specified job.
     *
     * @param jobId The id of the job.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getAsync(String jobId, final ServiceCallback<CloudJob> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (jobId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter jobId is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final JobGetOptions jobGetOptions = null;
        String select = null;
        String expand = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        Call<ResponseBody> call = service.get(jobId, this.client.apiVersion(), this.client.acceptLanguage(), select, expand, timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<CloudJob>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Gets information about the specified job.
     *
     * @param jobId The id of the job.
     * @param jobGetOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the CloudJob object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<CloudJob, JobGetHeaders> get(String jobId, JobGetOptions jobGetOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(jobGetOptions);
        String select = null;
        if (jobGetOptions != null) {
            select = jobGetOptions.select();
        }
        String expand = null;
        if (jobGetOptions != null) {
            expand = jobGetOptions.expand();
        }
        Integer timeout = null;
        if (jobGetOptions != null) {
            timeout = jobGetOptions.timeout();
        }
        String clientRequestId = null;
        if (jobGetOptions != null) {
            clientRequestId = jobGetOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobGetOptions != null) {
            returnClientRequestId = jobGetOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobGetOptions != null) {
            ocpDate = jobGetOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.get(jobId, this.client.apiVersion(), this.client.acceptLanguage(), select, expand, timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
        return getDelegate(call.execute());
    }

    /**
     * Gets information about the specified job.
     *
     * @param jobId The id of the job.
     * @param jobGetOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getAsync(String jobId, JobGetOptions jobGetOptions, final ServiceCallback<CloudJob> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (jobId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter jobId is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(jobGetOptions, serviceCallback);
        String select = null;
        if (jobGetOptions != null) {
            select = jobGetOptions.select();
        }
        String expand = null;
        if (jobGetOptions != null) {
            expand = jobGetOptions.expand();
        }
        Integer timeout = null;
        if (jobGetOptions != null) {
            timeout = jobGetOptions.timeout();
        }
        String clientRequestId = null;
        if (jobGetOptions != null) {
            clientRequestId = jobGetOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobGetOptions != null) {
            returnClientRequestId = jobGetOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobGetOptions != null) {
            ocpDate = jobGetOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.get(jobId, this.client.apiVersion(), this.client.acceptLanguage(), select, expand, timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<CloudJob>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<CloudJob, JobGetHeaders> getDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<CloudJob, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<CloudJob>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, JobGetHeaders.class);
    }

    /**
     * Updates the properties of a job.
     *
     * @param jobId The id of the job whose properties you want to update.
     * @param jobPatchParameter The parameters for the request.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, JobPatchHeaders> patch(String jobId, JobPatchParameter jobPatchParameter) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (jobPatchParameter == null) {
            throw new IllegalArgumentException("Parameter jobPatchParameter is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(jobPatchParameter);
        final JobPatchOptions jobPatchOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTime ifModifiedSince = null;
        DateTime ifUnmodifiedSince = null;
        Call<ResponseBody> call = service.patch(jobId, jobPatchParameter, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted, this.client.userAgent());
        return patchDelegate(call.execute());
    }

    /**
     * Updates the properties of a job.
     *
     * @param jobId The id of the job whose properties you want to update.
     * @param jobPatchParameter The parameters for the request.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall patchAsync(String jobId, JobPatchParameter jobPatchParameter, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (jobId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter jobId is required and cannot be null."));
            return null;
        }
        if (jobPatchParameter == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter jobPatchParameter is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(jobPatchParameter, serviceCallback);
        final JobPatchOptions jobPatchOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTime ifModifiedSince = null;
        DateTime ifUnmodifiedSince = null;
        Call<ResponseBody> call = service.patch(jobId, jobPatchParameter, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(patchDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Updates the properties of a job.
     *
     * @param jobId The id of the job whose properties you want to update.
     * @param jobPatchParameter The parameters for the request.
     * @param jobPatchOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, JobPatchHeaders> patch(String jobId, JobPatchParameter jobPatchParameter, JobPatchOptions jobPatchOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (jobPatchParameter == null) {
            throw new IllegalArgumentException("Parameter jobPatchParameter is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(jobPatchParameter);
        Validator.validate(jobPatchOptions);
        Integer timeout = null;
        if (jobPatchOptions != null) {
            timeout = jobPatchOptions.timeout();
        }
        String clientRequestId = null;
        if (jobPatchOptions != null) {
            clientRequestId = jobPatchOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobPatchOptions != null) {
            returnClientRequestId = jobPatchOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobPatchOptions != null) {
            ocpDate = jobPatchOptions.ocpDate();
        }
        String ifMatch = null;
        if (jobPatchOptions != null) {
            ifMatch = jobPatchOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (jobPatchOptions != null) {
            ifNoneMatch = jobPatchOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (jobPatchOptions != null) {
            ifModifiedSince = jobPatchOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (jobPatchOptions != null) {
            ifUnmodifiedSince = jobPatchOptions.ifUnmodifiedSince();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        if (ifModifiedSince != null) {
            ifModifiedSinceConverted = new DateTimeRfc1123(ifModifiedSince);
        }
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        if (ifUnmodifiedSince != null) {
            ifUnmodifiedSinceConverted = new DateTimeRfc1123(ifUnmodifiedSince);
        }
        Call<ResponseBody> call = service.patch(jobId, jobPatchParameter, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted, this.client.userAgent());
        return patchDelegate(call.execute());
    }

    /**
     * Updates the properties of a job.
     *
     * @param jobId The id of the job whose properties you want to update.
     * @param jobPatchParameter The parameters for the request.
     * @param jobPatchOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall patchAsync(String jobId, JobPatchParameter jobPatchParameter, JobPatchOptions jobPatchOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (jobId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter jobId is required and cannot be null."));
            return null;
        }
        if (jobPatchParameter == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter jobPatchParameter is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(jobPatchParameter, serviceCallback);
        Validator.validate(jobPatchOptions, serviceCallback);
        Integer timeout = null;
        if (jobPatchOptions != null) {
            timeout = jobPatchOptions.timeout();
        }
        String clientRequestId = null;
        if (jobPatchOptions != null) {
            clientRequestId = jobPatchOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobPatchOptions != null) {
            returnClientRequestId = jobPatchOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobPatchOptions != null) {
            ocpDate = jobPatchOptions.ocpDate();
        }
        String ifMatch = null;
        if (jobPatchOptions != null) {
            ifMatch = jobPatchOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (jobPatchOptions != null) {
            ifNoneMatch = jobPatchOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (jobPatchOptions != null) {
            ifModifiedSince = jobPatchOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (jobPatchOptions != null) {
            ifUnmodifiedSince = jobPatchOptions.ifUnmodifiedSince();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        if (ifModifiedSince != null) {
            ifModifiedSinceConverted = new DateTimeRfc1123(ifModifiedSince);
        }
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        if (ifUnmodifiedSince != null) {
            ifUnmodifiedSinceConverted = new DateTimeRfc1123(ifUnmodifiedSince);
        }
        Call<ResponseBody> call = service.patch(jobId, jobPatchParameter, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(patchDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<Void, JobPatchHeaders> patchDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, JobPatchHeaders.class);
    }

    /**
     * Updates the properties of a job.
     *
     * @param jobId The id of the job whose properties you want to update.
     * @param jobUpdateParameter The parameters for the request.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, JobUpdateHeaders> update(String jobId, JobUpdateParameter jobUpdateParameter) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (jobUpdateParameter == null) {
            throw new IllegalArgumentException("Parameter jobUpdateParameter is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(jobUpdateParameter);
        final JobUpdateOptions jobUpdateOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTime ifModifiedSince = null;
        DateTime ifUnmodifiedSince = null;
        Call<ResponseBody> call = service.update(jobId, jobUpdateParameter, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted, this.client.userAgent());
        return updateDelegate(call.execute());
    }

    /**
     * Updates the properties of a job.
     *
     * @param jobId The id of the job whose properties you want to update.
     * @param jobUpdateParameter The parameters for the request.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall updateAsync(String jobId, JobUpdateParameter jobUpdateParameter, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (jobId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter jobId is required and cannot be null."));
            return null;
        }
        if (jobUpdateParameter == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter jobUpdateParameter is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(jobUpdateParameter, serviceCallback);
        final JobUpdateOptions jobUpdateOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTime ifModifiedSince = null;
        DateTime ifUnmodifiedSince = null;
        Call<ResponseBody> call = service.update(jobId, jobUpdateParameter, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(updateDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Updates the properties of a job.
     *
     * @param jobId The id of the job whose properties you want to update.
     * @param jobUpdateParameter The parameters for the request.
     * @param jobUpdateOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, JobUpdateHeaders> update(String jobId, JobUpdateParameter jobUpdateParameter, JobUpdateOptions jobUpdateOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (jobUpdateParameter == null) {
            throw new IllegalArgumentException("Parameter jobUpdateParameter is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(jobUpdateParameter);
        Validator.validate(jobUpdateOptions);
        Integer timeout = null;
        if (jobUpdateOptions != null) {
            timeout = jobUpdateOptions.timeout();
        }
        String clientRequestId = null;
        if (jobUpdateOptions != null) {
            clientRequestId = jobUpdateOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobUpdateOptions != null) {
            returnClientRequestId = jobUpdateOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobUpdateOptions != null) {
            ocpDate = jobUpdateOptions.ocpDate();
        }
        String ifMatch = null;
        if (jobUpdateOptions != null) {
            ifMatch = jobUpdateOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (jobUpdateOptions != null) {
            ifNoneMatch = jobUpdateOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (jobUpdateOptions != null) {
            ifModifiedSince = jobUpdateOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (jobUpdateOptions != null) {
            ifUnmodifiedSince = jobUpdateOptions.ifUnmodifiedSince();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        if (ifModifiedSince != null) {
            ifModifiedSinceConverted = new DateTimeRfc1123(ifModifiedSince);
        }
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        if (ifUnmodifiedSince != null) {
            ifUnmodifiedSinceConverted = new DateTimeRfc1123(ifUnmodifiedSince);
        }
        Call<ResponseBody> call = service.update(jobId, jobUpdateParameter, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted, this.client.userAgent());
        return updateDelegate(call.execute());
    }

    /**
     * Updates the properties of a job.
     *
     * @param jobId The id of the job whose properties you want to update.
     * @param jobUpdateParameter The parameters for the request.
     * @param jobUpdateOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall updateAsync(String jobId, JobUpdateParameter jobUpdateParameter, JobUpdateOptions jobUpdateOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (jobId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter jobId is required and cannot be null."));
            return null;
        }
        if (jobUpdateParameter == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter jobUpdateParameter is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(jobUpdateParameter, serviceCallback);
        Validator.validate(jobUpdateOptions, serviceCallback);
        Integer timeout = null;
        if (jobUpdateOptions != null) {
            timeout = jobUpdateOptions.timeout();
        }
        String clientRequestId = null;
        if (jobUpdateOptions != null) {
            clientRequestId = jobUpdateOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobUpdateOptions != null) {
            returnClientRequestId = jobUpdateOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobUpdateOptions != null) {
            ocpDate = jobUpdateOptions.ocpDate();
        }
        String ifMatch = null;
        if (jobUpdateOptions != null) {
            ifMatch = jobUpdateOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (jobUpdateOptions != null) {
            ifNoneMatch = jobUpdateOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (jobUpdateOptions != null) {
            ifModifiedSince = jobUpdateOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (jobUpdateOptions != null) {
            ifUnmodifiedSince = jobUpdateOptions.ifUnmodifiedSince();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        if (ifModifiedSince != null) {
            ifModifiedSinceConverted = new DateTimeRfc1123(ifModifiedSince);
        }
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        if (ifUnmodifiedSince != null) {
            ifUnmodifiedSinceConverted = new DateTimeRfc1123(ifUnmodifiedSince);
        }
        Call<ResponseBody> call = service.update(jobId, jobUpdateParameter, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(updateDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<Void, JobUpdateHeaders> updateDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, JobUpdateHeaders.class);
    }

    /**
     * Disables the specified job, preventing new tasks from running.
     *
     * @param jobId The id of the job to disable.
     * @param disableTasks What to do with active tasks associated with the job. Possible values include: 'requeue', 'terminate', 'wait'
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, JobDisableHeaders> disable(String jobId, DisableJobOption disableTasks) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        if (disableTasks == null) {
            throw new IllegalArgumentException("Parameter disableTasks is required and cannot be null.");
        }
        final JobDisableOptions jobDisableOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTime ifModifiedSince = null;
        DateTime ifUnmodifiedSince = null;
        JobDisableParameter jobDisableParameter = new JobDisableParameter();
        jobDisableParameter.withDisableTasks(disableTasks);
        Call<ResponseBody> call = service.disable(jobId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted, jobDisableParameter, this.client.userAgent());
        return disableDelegate(call.execute());
    }

    /**
     * Disables the specified job, preventing new tasks from running.
     *
     * @param jobId The id of the job to disable.
     * @param disableTasks What to do with active tasks associated with the job. Possible values include: 'requeue', 'terminate', 'wait'
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall disableAsync(String jobId, DisableJobOption disableTasks, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (jobId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter jobId is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        if (disableTasks == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter disableTasks is required and cannot be null."));
            return null;
        }
        final JobDisableOptions jobDisableOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTime ifModifiedSince = null;
        DateTime ifUnmodifiedSince = null;
        JobDisableParameter jobDisableParameter = new JobDisableParameter();
        jobDisableParameter.withDisableTasks(disableTasks);
        Call<ResponseBody> call = service.disable(jobId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted, jobDisableParameter, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(disableDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Disables the specified job, preventing new tasks from running.
     *
     * @param jobId The id of the job to disable.
     * @param disableTasks What to do with active tasks associated with the job. Possible values include: 'requeue', 'terminate', 'wait'
     * @param jobDisableOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, JobDisableHeaders> disable(String jobId, DisableJobOption disableTasks, JobDisableOptions jobDisableOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        if (disableTasks == null) {
            throw new IllegalArgumentException("Parameter disableTasks is required and cannot be null.");
        }
        Validator.validate(jobDisableOptions);
        Integer timeout = null;
        if (jobDisableOptions != null) {
            timeout = jobDisableOptions.timeout();
        }
        String clientRequestId = null;
        if (jobDisableOptions != null) {
            clientRequestId = jobDisableOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobDisableOptions != null) {
            returnClientRequestId = jobDisableOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobDisableOptions != null) {
            ocpDate = jobDisableOptions.ocpDate();
        }
        String ifMatch = null;
        if (jobDisableOptions != null) {
            ifMatch = jobDisableOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (jobDisableOptions != null) {
            ifNoneMatch = jobDisableOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (jobDisableOptions != null) {
            ifModifiedSince = jobDisableOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (jobDisableOptions != null) {
            ifUnmodifiedSince = jobDisableOptions.ifUnmodifiedSince();
        }
        JobDisableParameter jobDisableParameter = new JobDisableParameter();
        jobDisableParameter.withDisableTasks(disableTasks);
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        if (ifModifiedSince != null) {
            ifModifiedSinceConverted = new DateTimeRfc1123(ifModifiedSince);
        }
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        if (ifUnmodifiedSince != null) {
            ifUnmodifiedSinceConverted = new DateTimeRfc1123(ifUnmodifiedSince);
        }
        Call<ResponseBody> call = service.disable(jobId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted, jobDisableParameter, this.client.userAgent());
        return disableDelegate(call.execute());
    }

    /**
     * Disables the specified job, preventing new tasks from running.
     *
     * @param jobId The id of the job to disable.
     * @param disableTasks What to do with active tasks associated with the job. Possible values include: 'requeue', 'terminate', 'wait'
     * @param jobDisableOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall disableAsync(String jobId, DisableJobOption disableTasks, JobDisableOptions jobDisableOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (jobId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter jobId is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        if (disableTasks == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter disableTasks is required and cannot be null."));
            return null;
        }
        Validator.validate(jobDisableOptions, serviceCallback);
        Integer timeout = null;
        if (jobDisableOptions != null) {
            timeout = jobDisableOptions.timeout();
        }
        String clientRequestId = null;
        if (jobDisableOptions != null) {
            clientRequestId = jobDisableOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobDisableOptions != null) {
            returnClientRequestId = jobDisableOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobDisableOptions != null) {
            ocpDate = jobDisableOptions.ocpDate();
        }
        String ifMatch = null;
        if (jobDisableOptions != null) {
            ifMatch = jobDisableOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (jobDisableOptions != null) {
            ifNoneMatch = jobDisableOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (jobDisableOptions != null) {
            ifModifiedSince = jobDisableOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (jobDisableOptions != null) {
            ifUnmodifiedSince = jobDisableOptions.ifUnmodifiedSince();
        }
        JobDisableParameter jobDisableParameter = new JobDisableParameter();
        jobDisableParameter.withDisableTasks(disableTasks);
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        if (ifModifiedSince != null) {
            ifModifiedSinceConverted = new DateTimeRfc1123(ifModifiedSince);
        }
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        if (ifUnmodifiedSince != null) {
            ifUnmodifiedSinceConverted = new DateTimeRfc1123(ifUnmodifiedSince);
        }
        Call<ResponseBody> call = service.disable(jobId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted, jobDisableParameter, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(disableDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<Void, JobDisableHeaders> disableDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(202, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, JobDisableHeaders.class);
    }

    /**
     * Enables the specified job, allowing new tasks to run.
     *
     * @param jobId The id of the job to enable.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, JobEnableHeaders> enable(String jobId) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final JobEnableOptions jobEnableOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTime ifModifiedSince = null;
        DateTime ifUnmodifiedSince = null;
        Call<ResponseBody> call = service.enable(jobId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted, this.client.userAgent());
        return enableDelegate(call.execute());
    }

    /**
     * Enables the specified job, allowing new tasks to run.
     *
     * @param jobId The id of the job to enable.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall enableAsync(String jobId, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (jobId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter jobId is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final JobEnableOptions jobEnableOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTime ifModifiedSince = null;
        DateTime ifUnmodifiedSince = null;
        Call<ResponseBody> call = service.enable(jobId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(enableDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Enables the specified job, allowing new tasks to run.
     *
     * @param jobId The id of the job to enable.
     * @param jobEnableOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, JobEnableHeaders> enable(String jobId, JobEnableOptions jobEnableOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(jobEnableOptions);
        Integer timeout = null;
        if (jobEnableOptions != null) {
            timeout = jobEnableOptions.timeout();
        }
        String clientRequestId = null;
        if (jobEnableOptions != null) {
            clientRequestId = jobEnableOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobEnableOptions != null) {
            returnClientRequestId = jobEnableOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobEnableOptions != null) {
            ocpDate = jobEnableOptions.ocpDate();
        }
        String ifMatch = null;
        if (jobEnableOptions != null) {
            ifMatch = jobEnableOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (jobEnableOptions != null) {
            ifNoneMatch = jobEnableOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (jobEnableOptions != null) {
            ifModifiedSince = jobEnableOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (jobEnableOptions != null) {
            ifUnmodifiedSince = jobEnableOptions.ifUnmodifiedSince();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        if (ifModifiedSince != null) {
            ifModifiedSinceConverted = new DateTimeRfc1123(ifModifiedSince);
        }
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        if (ifUnmodifiedSince != null) {
            ifUnmodifiedSinceConverted = new DateTimeRfc1123(ifUnmodifiedSince);
        }
        Call<ResponseBody> call = service.enable(jobId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted, this.client.userAgent());
        return enableDelegate(call.execute());
    }

    /**
     * Enables the specified job, allowing new tasks to run.
     *
     * @param jobId The id of the job to enable.
     * @param jobEnableOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall enableAsync(String jobId, JobEnableOptions jobEnableOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (jobId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter jobId is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(jobEnableOptions, serviceCallback);
        Integer timeout = null;
        if (jobEnableOptions != null) {
            timeout = jobEnableOptions.timeout();
        }
        String clientRequestId = null;
        if (jobEnableOptions != null) {
            clientRequestId = jobEnableOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobEnableOptions != null) {
            returnClientRequestId = jobEnableOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobEnableOptions != null) {
            ocpDate = jobEnableOptions.ocpDate();
        }
        String ifMatch = null;
        if (jobEnableOptions != null) {
            ifMatch = jobEnableOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (jobEnableOptions != null) {
            ifNoneMatch = jobEnableOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (jobEnableOptions != null) {
            ifModifiedSince = jobEnableOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (jobEnableOptions != null) {
            ifUnmodifiedSince = jobEnableOptions.ifUnmodifiedSince();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        if (ifModifiedSince != null) {
            ifModifiedSinceConverted = new DateTimeRfc1123(ifModifiedSince);
        }
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        if (ifUnmodifiedSince != null) {
            ifUnmodifiedSinceConverted = new DateTimeRfc1123(ifUnmodifiedSince);
        }
        Call<ResponseBody> call = service.enable(jobId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(enableDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<Void, JobEnableHeaders> enableDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(202, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, JobEnableHeaders.class);
    }

    /**
     * Terminates the specified job, marking it as completed.
     *
     * @param jobId The id of the job to terminate.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, JobTerminateHeaders> terminate(String jobId) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String terminateReason = null;
        final JobTerminateOptions jobTerminateOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTime ifModifiedSince = null;
        DateTime ifUnmodifiedSince = null;
        JobTerminateParameter jobTerminateParameter = new JobTerminateParameter();
        jobTerminateParameter.withTerminateReason(null);
        Call<ResponseBody> call = service.terminate(jobId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted, jobTerminateParameter, this.client.userAgent());
        return terminateDelegate(call.execute());
    }

    /**
     * Terminates the specified job, marking it as completed.
     *
     * @param jobId The id of the job to terminate.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall terminateAsync(String jobId, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (jobId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter jobId is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final String terminateReason = null;
        final JobTerminateOptions jobTerminateOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTime ifModifiedSince = null;
        DateTime ifUnmodifiedSince = null;
        JobTerminateParameter jobTerminateParameter = new JobTerminateParameter();
        jobTerminateParameter.withTerminateReason(null);
        Call<ResponseBody> call = service.terminate(jobId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted, jobTerminateParameter, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(terminateDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Terminates the specified job, marking it as completed.
     *
     * @param jobId The id of the job to terminate.
     * @param terminateReason The text you want to appear as the job's TerminateReason. The default is 'UserTerminate'.
     * @param jobTerminateOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, JobTerminateHeaders> terminate(String jobId, String terminateReason, JobTerminateOptions jobTerminateOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(jobTerminateOptions);
        Integer timeout = null;
        if (jobTerminateOptions != null) {
            timeout = jobTerminateOptions.timeout();
        }
        String clientRequestId = null;
        if (jobTerminateOptions != null) {
            clientRequestId = jobTerminateOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobTerminateOptions != null) {
            returnClientRequestId = jobTerminateOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobTerminateOptions != null) {
            ocpDate = jobTerminateOptions.ocpDate();
        }
        String ifMatch = null;
        if (jobTerminateOptions != null) {
            ifMatch = jobTerminateOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (jobTerminateOptions != null) {
            ifNoneMatch = jobTerminateOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (jobTerminateOptions != null) {
            ifModifiedSince = jobTerminateOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (jobTerminateOptions != null) {
            ifUnmodifiedSince = jobTerminateOptions.ifUnmodifiedSince();
        }
        JobTerminateParameter jobTerminateParameter = null;
        if (terminateReason != null) {
            jobTerminateParameter = new JobTerminateParameter();
            jobTerminateParameter.withTerminateReason(terminateReason);
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        if (ifModifiedSince != null) {
            ifModifiedSinceConverted = new DateTimeRfc1123(ifModifiedSince);
        }
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        if (ifUnmodifiedSince != null) {
            ifUnmodifiedSinceConverted = new DateTimeRfc1123(ifUnmodifiedSince);
        }
        Call<ResponseBody> call = service.terminate(jobId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted, jobTerminateParameter, this.client.userAgent());
        return terminateDelegate(call.execute());
    }

    /**
     * Terminates the specified job, marking it as completed.
     *
     * @param jobId The id of the job to terminate.
     * @param terminateReason The text you want to appear as the job's TerminateReason. The default is 'UserTerminate'.
     * @param jobTerminateOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall terminateAsync(String jobId, String terminateReason, JobTerminateOptions jobTerminateOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (jobId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter jobId is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(jobTerminateOptions, serviceCallback);
        Integer timeout = null;
        if (jobTerminateOptions != null) {
            timeout = jobTerminateOptions.timeout();
        }
        String clientRequestId = null;
        if (jobTerminateOptions != null) {
            clientRequestId = jobTerminateOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobTerminateOptions != null) {
            returnClientRequestId = jobTerminateOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobTerminateOptions != null) {
            ocpDate = jobTerminateOptions.ocpDate();
        }
        String ifMatch = null;
        if (jobTerminateOptions != null) {
            ifMatch = jobTerminateOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (jobTerminateOptions != null) {
            ifNoneMatch = jobTerminateOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (jobTerminateOptions != null) {
            ifModifiedSince = jobTerminateOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (jobTerminateOptions != null) {
            ifUnmodifiedSince = jobTerminateOptions.ifUnmodifiedSince();
        }
        JobTerminateParameter jobTerminateParameter = null;
        if (terminateReason != null) {
            jobTerminateParameter = new JobTerminateParameter();
            jobTerminateParameter.withTerminateReason(terminateReason);
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        if (ifModifiedSince != null) {
            ifModifiedSinceConverted = new DateTimeRfc1123(ifModifiedSince);
        }
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        if (ifUnmodifiedSince != null) {
            ifUnmodifiedSinceConverted = new DateTimeRfc1123(ifUnmodifiedSince);
        }
        Call<ResponseBody> call = service.terminate(jobId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted, jobTerminateParameter, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(terminateDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<Void, JobTerminateHeaders> terminateDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(202, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, JobTerminateHeaders.class);
    }

    /**
     * Adds a job to the specified account.
     *
     * @param job The job to be added.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, JobAddHeaders> add(JobAddParameter job) throws BatchErrorException, IOException, IllegalArgumentException {
        if (job == null) {
            throw new IllegalArgumentException("Parameter job is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(job);
        final JobAddOptions jobAddOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        Call<ResponseBody> call = service.add(job, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
        return addDelegate(call.execute());
    }

    /**
     * Adds a job to the specified account.
     *
     * @param job The job to be added.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall addAsync(JobAddParameter job, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (job == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter job is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(job, serviceCallback);
        final JobAddOptions jobAddOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        Call<ResponseBody> call = service.add(job, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(addDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Adds a job to the specified account.
     *
     * @param job The job to be added.
     * @param jobAddOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, JobAddHeaders> add(JobAddParameter job, JobAddOptions jobAddOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (job == null) {
            throw new IllegalArgumentException("Parameter job is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(job);
        Validator.validate(jobAddOptions);
        Integer timeout = null;
        if (jobAddOptions != null) {
            timeout = jobAddOptions.timeout();
        }
        String clientRequestId = null;
        if (jobAddOptions != null) {
            clientRequestId = jobAddOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobAddOptions != null) {
            returnClientRequestId = jobAddOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobAddOptions != null) {
            ocpDate = jobAddOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.add(job, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
        return addDelegate(call.execute());
    }

    /**
     * Adds a job to the specified account.
     *
     * @param job The job to be added.
     * @param jobAddOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall addAsync(JobAddParameter job, JobAddOptions jobAddOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (job == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter job is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(job, serviceCallback);
        Validator.validate(jobAddOptions, serviceCallback);
        Integer timeout = null;
        if (jobAddOptions != null) {
            timeout = jobAddOptions.timeout();
        }
        String clientRequestId = null;
        if (jobAddOptions != null) {
            clientRequestId = jobAddOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobAddOptions != null) {
            returnClientRequestId = jobAddOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobAddOptions != null) {
            ocpDate = jobAddOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.add(job, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(addDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<Void, JobAddHeaders> addDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(201, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, JobAddHeaders.class);
    }

    /**
     * Lists all of the jobs in the specified account.
     *
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;CloudJob&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PagedList<CloudJob>, JobListHeaders> list() throws BatchErrorException, IOException, IllegalArgumentException {
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final JobListOptions jobListOptions = null;
        String filter = null;
        String select = null;
        String expand = null;
        Integer maxResults = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        Call<ResponseBody> call = service.list(this.client.apiVersion(), this.client.acceptLanguage(), filter, select, expand, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
        ServiceResponseWithHeaders<PageImpl<CloudJob>, JobListHeaders> response = listDelegate(call.execute());
        PagedList<CloudJob> result = new PagedList<CloudJob>(response.getBody()) {
            @Override
            public Page<CloudJob> nextPage(String nextPageLink) throws BatchErrorException, IOException {
                return listNext(nextPageLink, null).getBody();
            }
        };
        return new ServiceResponseWithHeaders<>(result, response.getHeaders(), response.getResponse());
    }

    /**
     * Lists all of the jobs in the specified account.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listAsync(final ListOperationCallback<CloudJob> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final JobListOptions jobListOptions = null;
        String filter = null;
        String select = null;
        String expand = null;
        Integer maxResults = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        Call<ResponseBody> call = service.list(this.client.apiVersion(), this.client.acceptLanguage(), filter, select, expand, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<CloudJob>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<CloudJob>, JobListHeaders> result = listDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listNextAsync(result.getBody().getNextPageLink(), null, serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponseWithHeaders<>(serviceCallback.get(), result.getHeaders(), result.getResponse()));
                    }
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Lists all of the jobs in the specified account.
     *
     * @param jobListOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;CloudJob&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PagedList<CloudJob>, JobListHeaders> list(final JobListOptions jobListOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(jobListOptions);
        String filter = null;
        if (jobListOptions != null) {
            filter = jobListOptions.filter();
        }
        String select = null;
        if (jobListOptions != null) {
            select = jobListOptions.select();
        }
        String expand = null;
        if (jobListOptions != null) {
            expand = jobListOptions.expand();
        }
        Integer maxResults = null;
        if (jobListOptions != null) {
            maxResults = jobListOptions.maxResults();
        }
        Integer timeout = null;
        if (jobListOptions != null) {
            timeout = jobListOptions.timeout();
        }
        String clientRequestId = null;
        if (jobListOptions != null) {
            clientRequestId = jobListOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobListOptions != null) {
            returnClientRequestId = jobListOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobListOptions != null) {
            ocpDate = jobListOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.list(this.client.apiVersion(), this.client.acceptLanguage(), filter, select, expand, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
        ServiceResponseWithHeaders<PageImpl<CloudJob>, JobListHeaders> response = listDelegate(call.execute());
        PagedList<CloudJob> result = new PagedList<CloudJob>(response.getBody()) {
            @Override
            public Page<CloudJob> nextPage(String nextPageLink) throws BatchErrorException, IOException {
                JobListNextOptions jobListNextOptions = null;
                if (jobListOptions != null) {
                    jobListNextOptions = new JobListNextOptions();
                    jobListNextOptions.withClientRequestId(jobListOptions.clientRequestId());
                    jobListNextOptions.withReturnClientRequestId(jobListOptions.returnClientRequestId());
                    jobListNextOptions.withOcpDate(jobListOptions.ocpDate());
                }
                return listNext(nextPageLink, jobListNextOptions).getBody();
            }
        };
        return new ServiceResponseWithHeaders<>(result, response.getHeaders(), response.getResponse());
    }

    /**
     * Lists all of the jobs in the specified account.
     *
     * @param jobListOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listAsync(final JobListOptions jobListOptions, final ListOperationCallback<CloudJob> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(jobListOptions, serviceCallback);
        String filter = null;
        if (jobListOptions != null) {
            filter = jobListOptions.filter();
        }
        String select = null;
        if (jobListOptions != null) {
            select = jobListOptions.select();
        }
        String expand = null;
        if (jobListOptions != null) {
            expand = jobListOptions.expand();
        }
        Integer maxResults = null;
        if (jobListOptions != null) {
            maxResults = jobListOptions.maxResults();
        }
        Integer timeout = null;
        if (jobListOptions != null) {
            timeout = jobListOptions.timeout();
        }
        String clientRequestId = null;
        if (jobListOptions != null) {
            clientRequestId = jobListOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobListOptions != null) {
            returnClientRequestId = jobListOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobListOptions != null) {
            ocpDate = jobListOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.list(this.client.apiVersion(), this.client.acceptLanguage(), filter, select, expand, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<CloudJob>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<CloudJob>, JobListHeaders> result = listDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        JobListNextOptions jobListNextOptions = null;
                        if (jobListOptions != null) {
                            jobListNextOptions = new JobListNextOptions();
                            jobListNextOptions.withClientRequestId(jobListOptions.clientRequestId());
                            jobListNextOptions.withReturnClientRequestId(jobListOptions.returnClientRequestId());
                            jobListNextOptions.withOcpDate(jobListOptions.ocpDate());
                        }
                        listNextAsync(result.getBody().getNextPageLink(), jobListNextOptions, serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponseWithHeaders<>(serviceCallback.get(), result.getHeaders(), result.getResponse()));
                    }
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<PageImpl<CloudJob>, JobListHeaders> listDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<CloudJob>, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<CloudJob>>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, JobListHeaders.class);
    }

    /**
     * Lists the jobs that have been created under the specified job schedule.
     *
     * @param jobScheduleId The id of the job schedule from which you want to get a list of jobs.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;CloudJob&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PagedList<CloudJob>, JobListFromJobScheduleHeaders> listFromJobSchedule(final String jobScheduleId) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobScheduleId == null) {
            throw new IllegalArgumentException("Parameter jobScheduleId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final JobListFromJobScheduleOptions jobListFromJobScheduleOptions = null;
        String filter = null;
        String select = null;
        String expand = null;
        Integer maxResults = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        Call<ResponseBody> call = service.listFromJobSchedule(jobScheduleId, this.client.apiVersion(), this.client.acceptLanguage(), filter, select, expand, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
        ServiceResponseWithHeaders<PageImpl<CloudJob>, JobListFromJobScheduleHeaders> response = listFromJobScheduleDelegate(call.execute());
        PagedList<CloudJob> result = new PagedList<CloudJob>(response.getBody()) {
            @Override
            public Page<CloudJob> nextPage(String nextPageLink) throws BatchErrorException, IOException {
                return listFromJobScheduleNext(nextPageLink, null).getBody();
            }
        };
        return new ServiceResponseWithHeaders<>(result, response.getHeaders(), response.getResponse());
    }

    /**
     * Lists the jobs that have been created under the specified job schedule.
     *
     * @param jobScheduleId The id of the job schedule from which you want to get a list of jobs.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listFromJobScheduleAsync(final String jobScheduleId, final ListOperationCallback<CloudJob> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (jobScheduleId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter jobScheduleId is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final JobListFromJobScheduleOptions jobListFromJobScheduleOptions = null;
        String filter = null;
        String select = null;
        String expand = null;
        Integer maxResults = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        Call<ResponseBody> call = service.listFromJobSchedule(jobScheduleId, this.client.apiVersion(), this.client.acceptLanguage(), filter, select, expand, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<CloudJob>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<CloudJob>, JobListFromJobScheduleHeaders> result = listFromJobScheduleDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listFromJobScheduleNextAsync(result.getBody().getNextPageLink(), null, serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponseWithHeaders<>(serviceCallback.get(), result.getHeaders(), result.getResponse()));
                    }
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Lists the jobs that have been created under the specified job schedule.
     *
     * @param jobScheduleId The id of the job schedule from which you want to get a list of jobs.
     * @param jobListFromJobScheduleOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;CloudJob&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PagedList<CloudJob>, JobListFromJobScheduleHeaders> listFromJobSchedule(final String jobScheduleId, final JobListFromJobScheduleOptions jobListFromJobScheduleOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobScheduleId == null) {
            throw new IllegalArgumentException("Parameter jobScheduleId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(jobListFromJobScheduleOptions);
        String filter = null;
        if (jobListFromJobScheduleOptions != null) {
            filter = jobListFromJobScheduleOptions.filter();
        }
        String select = null;
        if (jobListFromJobScheduleOptions != null) {
            select = jobListFromJobScheduleOptions.select();
        }
        String expand = null;
        if (jobListFromJobScheduleOptions != null) {
            expand = jobListFromJobScheduleOptions.expand();
        }
        Integer maxResults = null;
        if (jobListFromJobScheduleOptions != null) {
            maxResults = jobListFromJobScheduleOptions.maxResults();
        }
        Integer timeout = null;
        if (jobListFromJobScheduleOptions != null) {
            timeout = jobListFromJobScheduleOptions.timeout();
        }
        String clientRequestId = null;
        if (jobListFromJobScheduleOptions != null) {
            clientRequestId = jobListFromJobScheduleOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobListFromJobScheduleOptions != null) {
            returnClientRequestId = jobListFromJobScheduleOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobListFromJobScheduleOptions != null) {
            ocpDate = jobListFromJobScheduleOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.listFromJobSchedule(jobScheduleId, this.client.apiVersion(), this.client.acceptLanguage(), filter, select, expand, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
        ServiceResponseWithHeaders<PageImpl<CloudJob>, JobListFromJobScheduleHeaders> response = listFromJobScheduleDelegate(call.execute());
        PagedList<CloudJob> result = new PagedList<CloudJob>(response.getBody()) {
            @Override
            public Page<CloudJob> nextPage(String nextPageLink) throws BatchErrorException, IOException {
                JobListFromJobScheduleNextOptions jobListFromJobScheduleNextOptions = null;
                if (jobListFromJobScheduleOptions != null) {
                    jobListFromJobScheduleNextOptions = new JobListFromJobScheduleNextOptions();
                    jobListFromJobScheduleNextOptions.withClientRequestId(jobListFromJobScheduleOptions.clientRequestId());
                    jobListFromJobScheduleNextOptions.withReturnClientRequestId(jobListFromJobScheduleOptions.returnClientRequestId());
                    jobListFromJobScheduleNextOptions.withOcpDate(jobListFromJobScheduleOptions.ocpDate());
                }
                return listFromJobScheduleNext(nextPageLink, jobListFromJobScheduleNextOptions).getBody();
            }
        };
        return new ServiceResponseWithHeaders<>(result, response.getHeaders(), response.getResponse());
    }

    /**
     * Lists the jobs that have been created under the specified job schedule.
     *
     * @param jobScheduleId The id of the job schedule from which you want to get a list of jobs.
     * @param jobListFromJobScheduleOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listFromJobScheduleAsync(final String jobScheduleId, final JobListFromJobScheduleOptions jobListFromJobScheduleOptions, final ListOperationCallback<CloudJob> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (jobScheduleId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter jobScheduleId is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(jobListFromJobScheduleOptions, serviceCallback);
        String filter = null;
        if (jobListFromJobScheduleOptions != null) {
            filter = jobListFromJobScheduleOptions.filter();
        }
        String select = null;
        if (jobListFromJobScheduleOptions != null) {
            select = jobListFromJobScheduleOptions.select();
        }
        String expand = null;
        if (jobListFromJobScheduleOptions != null) {
            expand = jobListFromJobScheduleOptions.expand();
        }
        Integer maxResults = null;
        if (jobListFromJobScheduleOptions != null) {
            maxResults = jobListFromJobScheduleOptions.maxResults();
        }
        Integer timeout = null;
        if (jobListFromJobScheduleOptions != null) {
            timeout = jobListFromJobScheduleOptions.timeout();
        }
        String clientRequestId = null;
        if (jobListFromJobScheduleOptions != null) {
            clientRequestId = jobListFromJobScheduleOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobListFromJobScheduleOptions != null) {
            returnClientRequestId = jobListFromJobScheduleOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobListFromJobScheduleOptions != null) {
            ocpDate = jobListFromJobScheduleOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.listFromJobSchedule(jobScheduleId, this.client.apiVersion(), this.client.acceptLanguage(), filter, select, expand, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<CloudJob>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<CloudJob>, JobListFromJobScheduleHeaders> result = listFromJobScheduleDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        JobListFromJobScheduleNextOptions jobListFromJobScheduleNextOptions = null;
                        if (jobListFromJobScheduleOptions != null) {
                            jobListFromJobScheduleNextOptions = new JobListFromJobScheduleNextOptions();
                            jobListFromJobScheduleNextOptions.withClientRequestId(jobListFromJobScheduleOptions.clientRequestId());
                            jobListFromJobScheduleNextOptions.withReturnClientRequestId(jobListFromJobScheduleOptions.returnClientRequestId());
                            jobListFromJobScheduleNextOptions.withOcpDate(jobListFromJobScheduleOptions.ocpDate());
                        }
                        listFromJobScheduleNextAsync(result.getBody().getNextPageLink(), jobListFromJobScheduleNextOptions, serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponseWithHeaders<>(serviceCallback.get(), result.getHeaders(), result.getResponse()));
                    }
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<PageImpl<CloudJob>, JobListFromJobScheduleHeaders> listFromJobScheduleDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<CloudJob>, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<CloudJob>>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, JobListFromJobScheduleHeaders.class);
    }

    /**
     * Lists the execution status of the Job Preparation and Job Release task for the specified job across the compute nodes where the job has run.
     *
     * @param jobId The id of the job.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;JobPreparationAndReleaseTaskExecutionInformation&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PagedList<JobPreparationAndReleaseTaskExecutionInformation>, JobListPreparationAndReleaseTaskStatusHeaders> listPreparationAndReleaseTaskStatus(final String jobId) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final JobListPreparationAndReleaseTaskStatusOptions jobListPreparationAndReleaseTaskStatusOptions = null;
        String filter = null;
        String select = null;
        Integer maxResults = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        Call<ResponseBody> call = service.listPreparationAndReleaseTaskStatus(jobId, this.client.apiVersion(), this.client.acceptLanguage(), filter, select, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
        ServiceResponseWithHeaders<PageImpl<JobPreparationAndReleaseTaskExecutionInformation>, JobListPreparationAndReleaseTaskStatusHeaders> response = listPreparationAndReleaseTaskStatusDelegate(call.execute());
        PagedList<JobPreparationAndReleaseTaskExecutionInformation> result = new PagedList<JobPreparationAndReleaseTaskExecutionInformation>(response.getBody()) {
            @Override
            public Page<JobPreparationAndReleaseTaskExecutionInformation> nextPage(String nextPageLink) throws BatchErrorException, IOException {
                return listPreparationAndReleaseTaskStatusNext(nextPageLink, null).getBody();
            }
        };
        return new ServiceResponseWithHeaders<>(result, response.getHeaders(), response.getResponse());
    }

    /**
     * Lists the execution status of the Job Preparation and Job Release task for the specified job across the compute nodes where the job has run.
     *
     * @param jobId The id of the job.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listPreparationAndReleaseTaskStatusAsync(final String jobId, final ListOperationCallback<JobPreparationAndReleaseTaskExecutionInformation> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (jobId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter jobId is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final JobListPreparationAndReleaseTaskStatusOptions jobListPreparationAndReleaseTaskStatusOptions = null;
        String filter = null;
        String select = null;
        Integer maxResults = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        Call<ResponseBody> call = service.listPreparationAndReleaseTaskStatus(jobId, this.client.apiVersion(), this.client.acceptLanguage(), filter, select, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<JobPreparationAndReleaseTaskExecutionInformation>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<JobPreparationAndReleaseTaskExecutionInformation>, JobListPreparationAndReleaseTaskStatusHeaders> result = listPreparationAndReleaseTaskStatusDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listPreparationAndReleaseTaskStatusNextAsync(result.getBody().getNextPageLink(), null, serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponseWithHeaders<>(serviceCallback.get(), result.getHeaders(), result.getResponse()));
                    }
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Lists the execution status of the Job Preparation and Job Release task for the specified job across the compute nodes where the job has run.
     *
     * @param jobId The id of the job.
     * @param jobListPreparationAndReleaseTaskStatusOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;JobPreparationAndReleaseTaskExecutionInformation&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PagedList<JobPreparationAndReleaseTaskExecutionInformation>, JobListPreparationAndReleaseTaskStatusHeaders> listPreparationAndReleaseTaskStatus(final String jobId, final JobListPreparationAndReleaseTaskStatusOptions jobListPreparationAndReleaseTaskStatusOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(jobListPreparationAndReleaseTaskStatusOptions);
        String filter = null;
        if (jobListPreparationAndReleaseTaskStatusOptions != null) {
            filter = jobListPreparationAndReleaseTaskStatusOptions.filter();
        }
        String select = null;
        if (jobListPreparationAndReleaseTaskStatusOptions != null) {
            select = jobListPreparationAndReleaseTaskStatusOptions.select();
        }
        Integer maxResults = null;
        if (jobListPreparationAndReleaseTaskStatusOptions != null) {
            maxResults = jobListPreparationAndReleaseTaskStatusOptions.maxResults();
        }
        Integer timeout = null;
        if (jobListPreparationAndReleaseTaskStatusOptions != null) {
            timeout = jobListPreparationAndReleaseTaskStatusOptions.timeout();
        }
        String clientRequestId = null;
        if (jobListPreparationAndReleaseTaskStatusOptions != null) {
            clientRequestId = jobListPreparationAndReleaseTaskStatusOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobListPreparationAndReleaseTaskStatusOptions != null) {
            returnClientRequestId = jobListPreparationAndReleaseTaskStatusOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobListPreparationAndReleaseTaskStatusOptions != null) {
            ocpDate = jobListPreparationAndReleaseTaskStatusOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.listPreparationAndReleaseTaskStatus(jobId, this.client.apiVersion(), this.client.acceptLanguage(), filter, select, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
        ServiceResponseWithHeaders<PageImpl<JobPreparationAndReleaseTaskExecutionInformation>, JobListPreparationAndReleaseTaskStatusHeaders> response = listPreparationAndReleaseTaskStatusDelegate(call.execute());
        PagedList<JobPreparationAndReleaseTaskExecutionInformation> result = new PagedList<JobPreparationAndReleaseTaskExecutionInformation>(response.getBody()) {
            @Override
            public Page<JobPreparationAndReleaseTaskExecutionInformation> nextPage(String nextPageLink) throws BatchErrorException, IOException {
                JobListPreparationAndReleaseTaskStatusNextOptions jobListPreparationAndReleaseTaskStatusNextOptions = null;
                if (jobListPreparationAndReleaseTaskStatusOptions != null) {
                    jobListPreparationAndReleaseTaskStatusNextOptions = new JobListPreparationAndReleaseTaskStatusNextOptions();
                    jobListPreparationAndReleaseTaskStatusNextOptions.withClientRequestId(jobListPreparationAndReleaseTaskStatusOptions.clientRequestId());
                    jobListPreparationAndReleaseTaskStatusNextOptions.withReturnClientRequestId(jobListPreparationAndReleaseTaskStatusOptions.returnClientRequestId());
                    jobListPreparationAndReleaseTaskStatusNextOptions.withOcpDate(jobListPreparationAndReleaseTaskStatusOptions.ocpDate());
                }
                return listPreparationAndReleaseTaskStatusNext(nextPageLink, jobListPreparationAndReleaseTaskStatusNextOptions).getBody();
            }
        };
        return new ServiceResponseWithHeaders<>(result, response.getHeaders(), response.getResponse());
    }

    /**
     * Lists the execution status of the Job Preparation and Job Release task for the specified job across the compute nodes where the job has run.
     *
     * @param jobId The id of the job.
     * @param jobListPreparationAndReleaseTaskStatusOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listPreparationAndReleaseTaskStatusAsync(final String jobId, final JobListPreparationAndReleaseTaskStatusOptions jobListPreparationAndReleaseTaskStatusOptions, final ListOperationCallback<JobPreparationAndReleaseTaskExecutionInformation> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (jobId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter jobId is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(jobListPreparationAndReleaseTaskStatusOptions, serviceCallback);
        String filter = null;
        if (jobListPreparationAndReleaseTaskStatusOptions != null) {
            filter = jobListPreparationAndReleaseTaskStatusOptions.filter();
        }
        String select = null;
        if (jobListPreparationAndReleaseTaskStatusOptions != null) {
            select = jobListPreparationAndReleaseTaskStatusOptions.select();
        }
        Integer maxResults = null;
        if (jobListPreparationAndReleaseTaskStatusOptions != null) {
            maxResults = jobListPreparationAndReleaseTaskStatusOptions.maxResults();
        }
        Integer timeout = null;
        if (jobListPreparationAndReleaseTaskStatusOptions != null) {
            timeout = jobListPreparationAndReleaseTaskStatusOptions.timeout();
        }
        String clientRequestId = null;
        if (jobListPreparationAndReleaseTaskStatusOptions != null) {
            clientRequestId = jobListPreparationAndReleaseTaskStatusOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobListPreparationAndReleaseTaskStatusOptions != null) {
            returnClientRequestId = jobListPreparationAndReleaseTaskStatusOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobListPreparationAndReleaseTaskStatusOptions != null) {
            ocpDate = jobListPreparationAndReleaseTaskStatusOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.listPreparationAndReleaseTaskStatus(jobId, this.client.apiVersion(), this.client.acceptLanguage(), filter, select, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<JobPreparationAndReleaseTaskExecutionInformation>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<JobPreparationAndReleaseTaskExecutionInformation>, JobListPreparationAndReleaseTaskStatusHeaders> result = listPreparationAndReleaseTaskStatusDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        JobListPreparationAndReleaseTaskStatusNextOptions jobListPreparationAndReleaseTaskStatusNextOptions = null;
                        if (jobListPreparationAndReleaseTaskStatusOptions != null) {
                            jobListPreparationAndReleaseTaskStatusNextOptions = new JobListPreparationAndReleaseTaskStatusNextOptions();
                            jobListPreparationAndReleaseTaskStatusNextOptions.withClientRequestId(jobListPreparationAndReleaseTaskStatusOptions.clientRequestId());
                            jobListPreparationAndReleaseTaskStatusNextOptions.withReturnClientRequestId(jobListPreparationAndReleaseTaskStatusOptions.returnClientRequestId());
                            jobListPreparationAndReleaseTaskStatusNextOptions.withOcpDate(jobListPreparationAndReleaseTaskStatusOptions.ocpDate());
                        }
                        listPreparationAndReleaseTaskStatusNextAsync(result.getBody().getNextPageLink(), jobListPreparationAndReleaseTaskStatusNextOptions, serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponseWithHeaders<>(serviceCallback.get(), result.getHeaders(), result.getResponse()));
                    }
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<PageImpl<JobPreparationAndReleaseTaskExecutionInformation>, JobListPreparationAndReleaseTaskStatusHeaders> listPreparationAndReleaseTaskStatusDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<JobPreparationAndReleaseTaskExecutionInformation>, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<JobPreparationAndReleaseTaskExecutionInformation>>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, JobListPreparationAndReleaseTaskStatusHeaders.class);
    }

    /**
     * Lists all of the jobs in the specified account.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;CloudJob&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PageImpl<CloudJob>, JobListHeaders> listNext(final String nextPageLink) throws BatchErrorException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        final JobListNextOptions jobListNextOptions = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.acceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
        return listNextDelegate(call.execute());
    }

    /**
     * Lists all of the jobs in the specified account.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<CloudJob> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        final JobListNextOptions jobListNextOptions = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.acceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<CloudJob>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<CloudJob>, JobListHeaders> result = listNextDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listNextAsync(result.getBody().getNextPageLink(), null, serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponseWithHeaders<>(serviceCallback.get(), result.getHeaders(), result.getResponse()));
                    }
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Lists all of the jobs in the specified account.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param jobListNextOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;CloudJob&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PageImpl<CloudJob>, JobListHeaders> listNext(final String nextPageLink, final JobListNextOptions jobListNextOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        Validator.validate(jobListNextOptions);
        String clientRequestId = null;
        if (jobListNextOptions != null) {
            clientRequestId = jobListNextOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobListNextOptions != null) {
            returnClientRequestId = jobListNextOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobListNextOptions != null) {
            ocpDate = jobListNextOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.acceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
        return listNextDelegate(call.execute());
    }

    /**
     * Lists all of the jobs in the specified account.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param jobListNextOptions Additional parameters for the operation
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listNextAsync(final String nextPageLink, final JobListNextOptions jobListNextOptions, final ServiceCall serviceCall, final ListOperationCallback<CloudJob> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        Validator.validate(jobListNextOptions, serviceCallback);
        String clientRequestId = null;
        if (jobListNextOptions != null) {
            clientRequestId = jobListNextOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobListNextOptions != null) {
            returnClientRequestId = jobListNextOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobListNextOptions != null) {
            ocpDate = jobListNextOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.acceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<CloudJob>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<CloudJob>, JobListHeaders> result = listNextDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listNextAsync(result.getBody().getNextPageLink(), jobListNextOptions, serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponseWithHeaders<>(serviceCallback.get(), result.getHeaders(), result.getResponse()));
                    }
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<PageImpl<CloudJob>, JobListHeaders> listNextDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<CloudJob>, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<CloudJob>>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, JobListHeaders.class);
    }

    /**
     * Lists the jobs that have been created under the specified job schedule.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;CloudJob&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PageImpl<CloudJob>, JobListFromJobScheduleHeaders> listFromJobScheduleNext(final String nextPageLink) throws BatchErrorException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        final JobListFromJobScheduleNextOptions jobListFromJobScheduleNextOptions = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        Call<ResponseBody> call = service.listFromJobScheduleNext(nextPageLink, this.client.acceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
        return listFromJobScheduleNextDelegate(call.execute());
    }

    /**
     * Lists the jobs that have been created under the specified job schedule.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listFromJobScheduleNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<CloudJob> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        final JobListFromJobScheduleNextOptions jobListFromJobScheduleNextOptions = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        Call<ResponseBody> call = service.listFromJobScheduleNext(nextPageLink, this.client.acceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<CloudJob>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<CloudJob>, JobListFromJobScheduleHeaders> result = listFromJobScheduleNextDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listFromJobScheduleNextAsync(result.getBody().getNextPageLink(), null, serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponseWithHeaders<>(serviceCallback.get(), result.getHeaders(), result.getResponse()));
                    }
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Lists the jobs that have been created under the specified job schedule.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param jobListFromJobScheduleNextOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;CloudJob&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PageImpl<CloudJob>, JobListFromJobScheduleHeaders> listFromJobScheduleNext(final String nextPageLink, final JobListFromJobScheduleNextOptions jobListFromJobScheduleNextOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        Validator.validate(jobListFromJobScheduleNextOptions);
        String clientRequestId = null;
        if (jobListFromJobScheduleNextOptions != null) {
            clientRequestId = jobListFromJobScheduleNextOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobListFromJobScheduleNextOptions != null) {
            returnClientRequestId = jobListFromJobScheduleNextOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobListFromJobScheduleNextOptions != null) {
            ocpDate = jobListFromJobScheduleNextOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.listFromJobScheduleNext(nextPageLink, this.client.acceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
        return listFromJobScheduleNextDelegate(call.execute());
    }

    /**
     * Lists the jobs that have been created under the specified job schedule.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param jobListFromJobScheduleNextOptions Additional parameters for the operation
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listFromJobScheduleNextAsync(final String nextPageLink, final JobListFromJobScheduleNextOptions jobListFromJobScheduleNextOptions, final ServiceCall serviceCall, final ListOperationCallback<CloudJob> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        Validator.validate(jobListFromJobScheduleNextOptions, serviceCallback);
        String clientRequestId = null;
        if (jobListFromJobScheduleNextOptions != null) {
            clientRequestId = jobListFromJobScheduleNextOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobListFromJobScheduleNextOptions != null) {
            returnClientRequestId = jobListFromJobScheduleNextOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobListFromJobScheduleNextOptions != null) {
            ocpDate = jobListFromJobScheduleNextOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.listFromJobScheduleNext(nextPageLink, this.client.acceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<CloudJob>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<CloudJob>, JobListFromJobScheduleHeaders> result = listFromJobScheduleNextDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listFromJobScheduleNextAsync(result.getBody().getNextPageLink(), jobListFromJobScheduleNextOptions, serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponseWithHeaders<>(serviceCallback.get(), result.getHeaders(), result.getResponse()));
                    }
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<PageImpl<CloudJob>, JobListFromJobScheduleHeaders> listFromJobScheduleNextDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<CloudJob>, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<CloudJob>>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, JobListFromJobScheduleHeaders.class);
    }

    /**
     * Lists the execution status of the Job Preparation and Job Release task for the specified job across the compute nodes where the job has run.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;JobPreparationAndReleaseTaskExecutionInformation&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PageImpl<JobPreparationAndReleaseTaskExecutionInformation>, JobListPreparationAndReleaseTaskStatusHeaders> listPreparationAndReleaseTaskStatusNext(final String nextPageLink) throws BatchErrorException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        final JobListPreparationAndReleaseTaskStatusNextOptions jobListPreparationAndReleaseTaskStatusNextOptions = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        Call<ResponseBody> call = service.listPreparationAndReleaseTaskStatusNext(nextPageLink, this.client.acceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
        return listPreparationAndReleaseTaskStatusNextDelegate(call.execute());
    }

    /**
     * Lists the execution status of the Job Preparation and Job Release task for the specified job across the compute nodes where the job has run.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listPreparationAndReleaseTaskStatusNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<JobPreparationAndReleaseTaskExecutionInformation> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        final JobListPreparationAndReleaseTaskStatusNextOptions jobListPreparationAndReleaseTaskStatusNextOptions = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        Call<ResponseBody> call = service.listPreparationAndReleaseTaskStatusNext(nextPageLink, this.client.acceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<JobPreparationAndReleaseTaskExecutionInformation>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<JobPreparationAndReleaseTaskExecutionInformation>, JobListPreparationAndReleaseTaskStatusHeaders> result = listPreparationAndReleaseTaskStatusNextDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listPreparationAndReleaseTaskStatusNextAsync(result.getBody().getNextPageLink(), null, serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponseWithHeaders<>(serviceCallback.get(), result.getHeaders(), result.getResponse()));
                    }
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Lists the execution status of the Job Preparation and Job Release task for the specified job across the compute nodes where the job has run.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param jobListPreparationAndReleaseTaskStatusNextOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;JobPreparationAndReleaseTaskExecutionInformation&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PageImpl<JobPreparationAndReleaseTaskExecutionInformation>, JobListPreparationAndReleaseTaskStatusHeaders> listPreparationAndReleaseTaskStatusNext(final String nextPageLink, final JobListPreparationAndReleaseTaskStatusNextOptions jobListPreparationAndReleaseTaskStatusNextOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        Validator.validate(jobListPreparationAndReleaseTaskStatusNextOptions);
        String clientRequestId = null;
        if (jobListPreparationAndReleaseTaskStatusNextOptions != null) {
            clientRequestId = jobListPreparationAndReleaseTaskStatusNextOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobListPreparationAndReleaseTaskStatusNextOptions != null) {
            returnClientRequestId = jobListPreparationAndReleaseTaskStatusNextOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobListPreparationAndReleaseTaskStatusNextOptions != null) {
            ocpDate = jobListPreparationAndReleaseTaskStatusNextOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.listPreparationAndReleaseTaskStatusNext(nextPageLink, this.client.acceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
        return listPreparationAndReleaseTaskStatusNextDelegate(call.execute());
    }

    /**
     * Lists the execution status of the Job Preparation and Job Release task for the specified job across the compute nodes where the job has run.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param jobListPreparationAndReleaseTaskStatusNextOptions Additional parameters for the operation
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listPreparationAndReleaseTaskStatusNextAsync(final String nextPageLink, final JobListPreparationAndReleaseTaskStatusNextOptions jobListPreparationAndReleaseTaskStatusNextOptions, final ServiceCall serviceCall, final ListOperationCallback<JobPreparationAndReleaseTaskExecutionInformation> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        Validator.validate(jobListPreparationAndReleaseTaskStatusNextOptions, serviceCallback);
        String clientRequestId = null;
        if (jobListPreparationAndReleaseTaskStatusNextOptions != null) {
            clientRequestId = jobListPreparationAndReleaseTaskStatusNextOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobListPreparationAndReleaseTaskStatusNextOptions != null) {
            returnClientRequestId = jobListPreparationAndReleaseTaskStatusNextOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobListPreparationAndReleaseTaskStatusNextOptions != null) {
            ocpDate = jobListPreparationAndReleaseTaskStatusNextOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.listPreparationAndReleaseTaskStatusNext(nextPageLink, this.client.acceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<JobPreparationAndReleaseTaskExecutionInformation>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<JobPreparationAndReleaseTaskExecutionInformation>, JobListPreparationAndReleaseTaskStatusHeaders> result = listPreparationAndReleaseTaskStatusNextDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listPreparationAndReleaseTaskStatusNextAsync(result.getBody().getNextPageLink(), jobListPreparationAndReleaseTaskStatusNextOptions, serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponseWithHeaders<>(serviceCallback.get(), result.getHeaders(), result.getResponse()));
                    }
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<PageImpl<JobPreparationAndReleaseTaskExecutionInformation>, JobListPreparationAndReleaseTaskStatusHeaders> listPreparationAndReleaseTaskStatusNextDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<JobPreparationAndReleaseTaskExecutionInformation>, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<JobPreparationAndReleaseTaskExecutionInformation>>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, JobListPreparationAndReleaseTaskStatusHeaders.class);
    }

}
