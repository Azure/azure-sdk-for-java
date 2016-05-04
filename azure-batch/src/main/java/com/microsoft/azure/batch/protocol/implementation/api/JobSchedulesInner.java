/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.implementation.api;

import retrofit2.Retrofit;
import com.google.common.reflect.TypeToken;
import com.microsoft.azure.AzureServiceResponseBuilder;
import com.microsoft.azure.ListOperationCallback;
import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.rest.DateTimeRfc1123;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponseCallback;
import com.microsoft.rest.ServiceResponseEmptyCallback;
import com.microsoft.rest.ServiceResponseWithHeaders;
import com.microsoft.rest.Validator;
import java.io.IOException;
import java.util.List;
import okhttp3.ResponseBody;
import org.joda.time.DateTime;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HEAD;
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
 * in JobSchedules.
 */
public final class JobSchedulesInner {
    /** The Retrofit service to perform REST calls. */
    private JobSchedulesService service;
    /** The service client containing this operation class. */
    private BatchServiceClientImpl client;

    /**
     * Initializes an instance of JobSchedulesInner.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public JobSchedulesInner(Retrofit retrofit, BatchServiceClientImpl client) {
        this.service = retrofit.create(JobSchedulesService.class);
        this.client = client;
    }

    /**
     * The interface defining all the services for JobSchedules to be
     * used by Retrofit to perform actually REST calls.
     */
    interface JobSchedulesService {
        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @HEAD("jobschedules/{jobScheduleId}")
        Call<Void> exists(@Path("jobScheduleId") String jobScheduleId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("$select") String select, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Header("If-Match") String ifMatch, @Header("If-None-Match") String ifNoneMatch, @Header("If-Modified-Since") DateTimeRfc1123 ifModifiedSince, @Header("If-Unmodified-Since") DateTimeRfc1123 ifUnmodifiedSince);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @HTTP(path = "jobschedules/{jobScheduleId}", method = "DELETE", hasBody = true)
        Call<ResponseBody> delete(@Path("jobScheduleId") String jobScheduleId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Header("If-Match") String ifMatch, @Header("If-None-Match") String ifNoneMatch, @Header("If-Modified-Since") DateTimeRfc1123 ifModifiedSince, @Header("If-Unmodified-Since") DateTimeRfc1123 ifUnmodifiedSince);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @GET("jobschedules/{jobScheduleId}")
        Call<ResponseBody> get(@Path("jobScheduleId") String jobScheduleId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("$select") String select, @Query("$expand") String expand, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Header("If-Match") String ifMatch, @Header("If-None-Match") String ifNoneMatch, @Header("If-Modified-Since") DateTimeRfc1123 ifModifiedSince, @Header("If-Unmodified-Since") DateTimeRfc1123 ifUnmodifiedSince);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @PATCH("jobschedules/{jobScheduleId}")
        Call<ResponseBody> patch(@Path("jobScheduleId") String jobScheduleId, @Body JobSchedulePatchParameterInner jobSchedulePatchParameter, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Header("If-Match") String ifMatch, @Header("If-None-Match") String ifNoneMatch, @Header("If-Modified-Since") DateTimeRfc1123 ifModifiedSince, @Header("If-Unmodified-Since") DateTimeRfc1123 ifUnmodifiedSince);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @PUT("jobschedules/{jobScheduleId}")
        Call<ResponseBody> update(@Path("jobScheduleId") String jobScheduleId, @Body JobScheduleUpdateParameterInner jobScheduleUpdateParameter, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Header("If-Match") String ifMatch, @Header("If-None-Match") String ifNoneMatch, @Header("If-Modified-Since") DateTimeRfc1123 ifModifiedSince, @Header("If-Unmodified-Since") DateTimeRfc1123 ifUnmodifiedSince);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @POST("jobschedules/{jobScheduleId}/disable")
        Call<ResponseBody> disable(@Path("jobScheduleId") String jobScheduleId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Header("If-Match") String ifMatch, @Header("If-None-Match") String ifNoneMatch, @Header("If-Modified-Since") DateTimeRfc1123 ifModifiedSince, @Header("If-Unmodified-Since") DateTimeRfc1123 ifUnmodifiedSince);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @POST("jobschedules/{jobScheduleId}/enable")
        Call<ResponseBody> enable(@Path("jobScheduleId") String jobScheduleId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Header("If-Match") String ifMatch, @Header("If-None-Match") String ifNoneMatch, @Header("If-Modified-Since") DateTimeRfc1123 ifModifiedSince, @Header("If-Unmodified-Since") DateTimeRfc1123 ifUnmodifiedSince);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @POST("jobschedules/{jobScheduleId}/terminate")
        Call<ResponseBody> terminate(@Path("jobScheduleId") String jobScheduleId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Header("If-Match") String ifMatch, @Header("If-None-Match") String ifNoneMatch, @Header("If-Modified-Since") DateTimeRfc1123 ifModifiedSince, @Header("If-Unmodified-Since") DateTimeRfc1123 ifUnmodifiedSince);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @POST("jobschedules")
        Call<ResponseBody> add(@Body JobScheduleAddParameterInner cloudJobSchedule, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @GET("jobschedules")
        Call<ResponseBody> list(@Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("$filter") String filter, @Query("$select") String select, @Query("$expand") String expand, @Query("maxresults") Integer maxResults, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @GET
        Call<ResponseBody> listNext(@Url String nextPageLink, @Header("accept-language") String acceptLanguage, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate);

    }

    /**
     * Checks the specified job schedule exists.
     *
     * @param jobScheduleId The id of the job schedule which you want to check.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the boolean object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<Boolean, JobScheduleExistsHeadersInner> exists(String jobScheduleId) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobScheduleId == null) {
            throw new IllegalArgumentException("Parameter jobScheduleId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final JobScheduleExistsOptionsInner jobScheduleExistsOptions = null;
        String select = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        Call<Void> call = service.exists(jobScheduleId, this.client.apiVersion(), this.client.acceptLanguage(), select, timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        return existsDelegate(call.execute());
    }

    /**
     * Checks the specified job schedule exists.
     *
     * @param jobScheduleId The id of the job schedule which you want to check.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall existsAsync(String jobScheduleId, final ServiceCallback<Boolean> serviceCallback) throws IllegalArgumentException {
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
        final JobScheduleExistsOptionsInner jobScheduleExistsOptions = null;
        String select = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        Call<Void> call = service.exists(jobScheduleId, this.client.apiVersion(), this.client.acceptLanguage(), select, timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseEmptyCallback<Boolean>(serviceCallback) {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                try {
                    serviceCallback.success(existsDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

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
    public ServiceResponseWithHeaders<Boolean, JobScheduleExistsHeadersInner> exists(String jobScheduleId, JobScheduleExistsOptionsInner jobScheduleExistsOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobScheduleId == null) {
            throw new IllegalArgumentException("Parameter jobScheduleId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(jobScheduleExistsOptions);
        String select = null;
        if (jobScheduleExistsOptions != null) {
            select = jobScheduleExistsOptions.select();
        }
        Integer timeout = null;
        if (jobScheduleExistsOptions != null) {
            timeout = jobScheduleExistsOptions.timeout();
        }
        String clientRequestId = null;
        if (jobScheduleExistsOptions != null) {
            clientRequestId = jobScheduleExistsOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobScheduleExistsOptions != null) {
            returnClientRequestId = jobScheduleExistsOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobScheduleExistsOptions != null) {
            ocpDate = jobScheduleExistsOptions.ocpDate();
        }
        String ifMatch = null;
        if (jobScheduleExistsOptions != null) {
            ifMatch = jobScheduleExistsOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (jobScheduleExistsOptions != null) {
            ifNoneMatch = jobScheduleExistsOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (jobScheduleExistsOptions != null) {
            ifModifiedSince = jobScheduleExistsOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (jobScheduleExistsOptions != null) {
            ifUnmodifiedSince = jobScheduleExistsOptions.ifUnmodifiedSince();
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
        Call<Void> call = service.exists(jobScheduleId, this.client.apiVersion(), this.client.acceptLanguage(), select, timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        return existsDelegate(call.execute());
    }

    /**
     * Checks the specified job schedule exists.
     *
     * @param jobScheduleId The id of the job schedule which you want to check.
     * @param jobScheduleExistsOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall existsAsync(String jobScheduleId, JobScheduleExistsOptionsInner jobScheduleExistsOptions, final ServiceCallback<Boolean> serviceCallback) throws IllegalArgumentException {
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
        Validator.validate(jobScheduleExistsOptions, serviceCallback);
        String select = null;
        if (jobScheduleExistsOptions != null) {
            select = jobScheduleExistsOptions.select();
        }
        Integer timeout = null;
        if (jobScheduleExistsOptions != null) {
            timeout = jobScheduleExistsOptions.timeout();
        }
        String clientRequestId = null;
        if (jobScheduleExistsOptions != null) {
            clientRequestId = jobScheduleExistsOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobScheduleExistsOptions != null) {
            returnClientRequestId = jobScheduleExistsOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobScheduleExistsOptions != null) {
            ocpDate = jobScheduleExistsOptions.ocpDate();
        }
        String ifMatch = null;
        if (jobScheduleExistsOptions != null) {
            ifMatch = jobScheduleExistsOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (jobScheduleExistsOptions != null) {
            ifNoneMatch = jobScheduleExistsOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (jobScheduleExistsOptions != null) {
            ifModifiedSince = jobScheduleExistsOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (jobScheduleExistsOptions != null) {
            ifUnmodifiedSince = jobScheduleExistsOptions.ifUnmodifiedSince();
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
        Call<Void> call = service.exists(jobScheduleId, this.client.apiVersion(), this.client.acceptLanguage(), select, timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseEmptyCallback<Boolean>(serviceCallback) {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                try {
                    serviceCallback.success(existsDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<Boolean, JobScheduleExistsHeadersInner> existsDelegate(Response<Void> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Boolean, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .register(404, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildEmptyWithHeaders(response, JobScheduleExistsHeadersInner.class);
    }

    /**
     * Deletes a job schedule from the specified account.
     *
     * @param jobScheduleId The id of the job schedule to delete.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, JobScheduleDeleteHeadersInner> delete(String jobScheduleId) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobScheduleId == null) {
            throw new IllegalArgumentException("Parameter jobScheduleId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final JobScheduleDeleteOptionsInner jobScheduleDeleteOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        Call<ResponseBody> call = service.delete(jobScheduleId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        return deleteDelegate(call.execute());
    }

    /**
     * Deletes a job schedule from the specified account.
     *
     * @param jobScheduleId The id of the job schedule to delete.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall deleteAsync(String jobScheduleId, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
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
        final JobScheduleDeleteOptionsInner jobScheduleDeleteOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        Call<ResponseBody> call = service.delete(jobScheduleId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
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
     * Deletes a job schedule from the specified account.
     *
     * @param jobScheduleId The id of the job schedule to delete.
     * @param jobScheduleDeleteOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, JobScheduleDeleteHeadersInner> delete(String jobScheduleId, JobScheduleDeleteOptionsInner jobScheduleDeleteOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobScheduleId == null) {
            throw new IllegalArgumentException("Parameter jobScheduleId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(jobScheduleDeleteOptions);
        Integer timeout = null;
        if (jobScheduleDeleteOptions != null) {
            timeout = jobScheduleDeleteOptions.timeout();
        }
        String clientRequestId = null;
        if (jobScheduleDeleteOptions != null) {
            clientRequestId = jobScheduleDeleteOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobScheduleDeleteOptions != null) {
            returnClientRequestId = jobScheduleDeleteOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobScheduleDeleteOptions != null) {
            ocpDate = jobScheduleDeleteOptions.ocpDate();
        }
        String ifMatch = null;
        if (jobScheduleDeleteOptions != null) {
            ifMatch = jobScheduleDeleteOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (jobScheduleDeleteOptions != null) {
            ifNoneMatch = jobScheduleDeleteOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (jobScheduleDeleteOptions != null) {
            ifModifiedSince = jobScheduleDeleteOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (jobScheduleDeleteOptions != null) {
            ifUnmodifiedSince = jobScheduleDeleteOptions.ifUnmodifiedSince();
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
        Call<ResponseBody> call = service.delete(jobScheduleId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        return deleteDelegate(call.execute());
    }

    /**
     * Deletes a job schedule from the specified account.
     *
     * @param jobScheduleId The id of the job schedule to delete.
     * @param jobScheduleDeleteOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall deleteAsync(String jobScheduleId, JobScheduleDeleteOptionsInner jobScheduleDeleteOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
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
        Validator.validate(jobScheduleDeleteOptions, serviceCallback);
        Integer timeout = null;
        if (jobScheduleDeleteOptions != null) {
            timeout = jobScheduleDeleteOptions.timeout();
        }
        String clientRequestId = null;
        if (jobScheduleDeleteOptions != null) {
            clientRequestId = jobScheduleDeleteOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobScheduleDeleteOptions != null) {
            returnClientRequestId = jobScheduleDeleteOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobScheduleDeleteOptions != null) {
            ocpDate = jobScheduleDeleteOptions.ocpDate();
        }
        String ifMatch = null;
        if (jobScheduleDeleteOptions != null) {
            ifMatch = jobScheduleDeleteOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (jobScheduleDeleteOptions != null) {
            ifNoneMatch = jobScheduleDeleteOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (jobScheduleDeleteOptions != null) {
            ifModifiedSince = jobScheduleDeleteOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (jobScheduleDeleteOptions != null) {
            ifUnmodifiedSince = jobScheduleDeleteOptions.ifUnmodifiedSince();
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
        Call<ResponseBody> call = service.delete(jobScheduleId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
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

    private ServiceResponseWithHeaders<Void, JobScheduleDeleteHeadersInner> deleteDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(202, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, JobScheduleDeleteHeadersInner.class);
    }

    /**
     * Gets information about the specified job schedule.
     *
     * @param jobScheduleId The id of the job schedule to get.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the CloudJobScheduleInner object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<CloudJobScheduleInner, JobScheduleGetHeadersInner> get(String jobScheduleId) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobScheduleId == null) {
            throw new IllegalArgumentException("Parameter jobScheduleId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final JobScheduleGetOptionsInner jobScheduleGetOptions = null;
        String select = null;
        String expand = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        Call<ResponseBody> call = service.get(jobScheduleId, this.client.apiVersion(), this.client.acceptLanguage(), select, expand, timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        return getDelegate(call.execute());
    }

    /**
     * Gets information about the specified job schedule.
     *
     * @param jobScheduleId The id of the job schedule to get.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getAsync(String jobScheduleId, final ServiceCallback<CloudJobScheduleInner> serviceCallback) throws IllegalArgumentException {
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
        final JobScheduleGetOptionsInner jobScheduleGetOptions = null;
        String select = null;
        String expand = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        Call<ResponseBody> call = service.get(jobScheduleId, this.client.apiVersion(), this.client.acceptLanguage(), select, expand, timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<CloudJobScheduleInner>(serviceCallback) {
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
     * Gets information about the specified job schedule.
     *
     * @param jobScheduleId The id of the job schedule to get.
     * @param jobScheduleGetOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the CloudJobScheduleInner object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<CloudJobScheduleInner, JobScheduleGetHeadersInner> get(String jobScheduleId, JobScheduleGetOptionsInner jobScheduleGetOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobScheduleId == null) {
            throw new IllegalArgumentException("Parameter jobScheduleId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(jobScheduleGetOptions);
        String select = null;
        if (jobScheduleGetOptions != null) {
            select = jobScheduleGetOptions.select();
        }
        String expand = null;
        if (jobScheduleGetOptions != null) {
            expand = jobScheduleGetOptions.expand();
        }
        Integer timeout = null;
        if (jobScheduleGetOptions != null) {
            timeout = jobScheduleGetOptions.timeout();
        }
        String clientRequestId = null;
        if (jobScheduleGetOptions != null) {
            clientRequestId = jobScheduleGetOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobScheduleGetOptions != null) {
            returnClientRequestId = jobScheduleGetOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobScheduleGetOptions != null) {
            ocpDate = jobScheduleGetOptions.ocpDate();
        }
        String ifMatch = null;
        if (jobScheduleGetOptions != null) {
            ifMatch = jobScheduleGetOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (jobScheduleGetOptions != null) {
            ifNoneMatch = jobScheduleGetOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (jobScheduleGetOptions != null) {
            ifModifiedSince = jobScheduleGetOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (jobScheduleGetOptions != null) {
            ifUnmodifiedSince = jobScheduleGetOptions.ifUnmodifiedSince();
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
        Call<ResponseBody> call = service.get(jobScheduleId, this.client.apiVersion(), this.client.acceptLanguage(), select, expand, timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        return getDelegate(call.execute());
    }

    /**
     * Gets information about the specified job schedule.
     *
     * @param jobScheduleId The id of the job schedule to get.
     * @param jobScheduleGetOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getAsync(String jobScheduleId, JobScheduleGetOptionsInner jobScheduleGetOptions, final ServiceCallback<CloudJobScheduleInner> serviceCallback) throws IllegalArgumentException {
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
        Validator.validate(jobScheduleGetOptions, serviceCallback);
        String select = null;
        if (jobScheduleGetOptions != null) {
            select = jobScheduleGetOptions.select();
        }
        String expand = null;
        if (jobScheduleGetOptions != null) {
            expand = jobScheduleGetOptions.expand();
        }
        Integer timeout = null;
        if (jobScheduleGetOptions != null) {
            timeout = jobScheduleGetOptions.timeout();
        }
        String clientRequestId = null;
        if (jobScheduleGetOptions != null) {
            clientRequestId = jobScheduleGetOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobScheduleGetOptions != null) {
            returnClientRequestId = jobScheduleGetOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobScheduleGetOptions != null) {
            ocpDate = jobScheduleGetOptions.ocpDate();
        }
        String ifMatch = null;
        if (jobScheduleGetOptions != null) {
            ifMatch = jobScheduleGetOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (jobScheduleGetOptions != null) {
            ifNoneMatch = jobScheduleGetOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (jobScheduleGetOptions != null) {
            ifModifiedSince = jobScheduleGetOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (jobScheduleGetOptions != null) {
            ifUnmodifiedSince = jobScheduleGetOptions.ifUnmodifiedSince();
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
        Call<ResponseBody> call = service.get(jobScheduleId, this.client.apiVersion(), this.client.acceptLanguage(), select, expand, timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<CloudJobScheduleInner>(serviceCallback) {
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

    private ServiceResponseWithHeaders<CloudJobScheduleInner, JobScheduleGetHeadersInner> getDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<CloudJobScheduleInner, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<CloudJobScheduleInner>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, JobScheduleGetHeadersInner.class);
    }

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
    public ServiceResponseWithHeaders<Void, JobSchedulePatchHeadersInner> patch(String jobScheduleId, JobSchedulePatchParameterInner jobSchedulePatchParameter) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobScheduleId == null) {
            throw new IllegalArgumentException("Parameter jobScheduleId is required and cannot be null.");
        }
        if (jobSchedulePatchParameter == null) {
            throw new IllegalArgumentException("Parameter jobSchedulePatchParameter is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(jobSchedulePatchParameter);
        final JobSchedulePatchOptionsInner jobSchedulePatchOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        Call<ResponseBody> call = service.patch(jobScheduleId, jobSchedulePatchParameter, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        return patchDelegate(call.execute());
    }

    /**
     * Updates the properties of the specified job schedule.
     *
     * @param jobScheduleId The id of the job schedule to update.
     * @param jobSchedulePatchParameter The parameters for the request.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall patchAsync(String jobScheduleId, JobSchedulePatchParameterInner jobSchedulePatchParameter, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (jobScheduleId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter jobScheduleId is required and cannot be null."));
            return null;
        }
        if (jobSchedulePatchParameter == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter jobSchedulePatchParameter is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(jobSchedulePatchParameter, serviceCallback);
        final JobSchedulePatchOptionsInner jobSchedulePatchOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        Call<ResponseBody> call = service.patch(jobScheduleId, jobSchedulePatchParameter, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
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
    public ServiceResponseWithHeaders<Void, JobSchedulePatchHeadersInner> patch(String jobScheduleId, JobSchedulePatchParameterInner jobSchedulePatchParameter, JobSchedulePatchOptionsInner jobSchedulePatchOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobScheduleId == null) {
            throw new IllegalArgumentException("Parameter jobScheduleId is required and cannot be null.");
        }
        if (jobSchedulePatchParameter == null) {
            throw new IllegalArgumentException("Parameter jobSchedulePatchParameter is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(jobSchedulePatchParameter);
        Validator.validate(jobSchedulePatchOptions);
        Integer timeout = null;
        if (jobSchedulePatchOptions != null) {
            timeout = jobSchedulePatchOptions.timeout();
        }
        String clientRequestId = null;
        if (jobSchedulePatchOptions != null) {
            clientRequestId = jobSchedulePatchOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobSchedulePatchOptions != null) {
            returnClientRequestId = jobSchedulePatchOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobSchedulePatchOptions != null) {
            ocpDate = jobSchedulePatchOptions.ocpDate();
        }
        String ifMatch = null;
        if (jobSchedulePatchOptions != null) {
            ifMatch = jobSchedulePatchOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (jobSchedulePatchOptions != null) {
            ifNoneMatch = jobSchedulePatchOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (jobSchedulePatchOptions != null) {
            ifModifiedSince = jobSchedulePatchOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (jobSchedulePatchOptions != null) {
            ifUnmodifiedSince = jobSchedulePatchOptions.ifUnmodifiedSince();
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
        Call<ResponseBody> call = service.patch(jobScheduleId, jobSchedulePatchParameter, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        return patchDelegate(call.execute());
    }

    /**
     * Updates the properties of the specified job schedule.
     *
     * @param jobScheduleId The id of the job schedule to update.
     * @param jobSchedulePatchParameter The parameters for the request.
     * @param jobSchedulePatchOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall patchAsync(String jobScheduleId, JobSchedulePatchParameterInner jobSchedulePatchParameter, JobSchedulePatchOptionsInner jobSchedulePatchOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (jobScheduleId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter jobScheduleId is required and cannot be null."));
            return null;
        }
        if (jobSchedulePatchParameter == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter jobSchedulePatchParameter is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(jobSchedulePatchParameter, serviceCallback);
        Validator.validate(jobSchedulePatchOptions, serviceCallback);
        Integer timeout = null;
        if (jobSchedulePatchOptions != null) {
            timeout = jobSchedulePatchOptions.timeout();
        }
        String clientRequestId = null;
        if (jobSchedulePatchOptions != null) {
            clientRequestId = jobSchedulePatchOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobSchedulePatchOptions != null) {
            returnClientRequestId = jobSchedulePatchOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobSchedulePatchOptions != null) {
            ocpDate = jobSchedulePatchOptions.ocpDate();
        }
        String ifMatch = null;
        if (jobSchedulePatchOptions != null) {
            ifMatch = jobSchedulePatchOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (jobSchedulePatchOptions != null) {
            ifNoneMatch = jobSchedulePatchOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (jobSchedulePatchOptions != null) {
            ifModifiedSince = jobSchedulePatchOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (jobSchedulePatchOptions != null) {
            ifUnmodifiedSince = jobSchedulePatchOptions.ifUnmodifiedSince();
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
        Call<ResponseBody> call = service.patch(jobScheduleId, jobSchedulePatchParameter, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
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

    private ServiceResponseWithHeaders<Void, JobSchedulePatchHeadersInner> patchDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, JobSchedulePatchHeadersInner.class);
    }

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
    public ServiceResponseWithHeaders<Void, JobScheduleUpdateHeadersInner> update(String jobScheduleId, JobScheduleUpdateParameterInner jobScheduleUpdateParameter) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobScheduleId == null) {
            throw new IllegalArgumentException("Parameter jobScheduleId is required and cannot be null.");
        }
        if (jobScheduleUpdateParameter == null) {
            throw new IllegalArgumentException("Parameter jobScheduleUpdateParameter is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(jobScheduleUpdateParameter);
        final JobScheduleUpdateOptionsInner jobScheduleUpdateOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        Call<ResponseBody> call = service.update(jobScheduleId, jobScheduleUpdateParameter, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        return updateDelegate(call.execute());
    }

    /**
     * Updates the properties of the specified job schedule.
     *
     * @param jobScheduleId The id of the job schedule to update.
     * @param jobScheduleUpdateParameter The parameters for the request.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall updateAsync(String jobScheduleId, JobScheduleUpdateParameterInner jobScheduleUpdateParameter, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (jobScheduleId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter jobScheduleId is required and cannot be null."));
            return null;
        }
        if (jobScheduleUpdateParameter == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter jobScheduleUpdateParameter is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(jobScheduleUpdateParameter, serviceCallback);
        final JobScheduleUpdateOptionsInner jobScheduleUpdateOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        Call<ResponseBody> call = service.update(jobScheduleId, jobScheduleUpdateParameter, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
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
    public ServiceResponseWithHeaders<Void, JobScheduleUpdateHeadersInner> update(String jobScheduleId, JobScheduleUpdateParameterInner jobScheduleUpdateParameter, JobScheduleUpdateOptionsInner jobScheduleUpdateOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobScheduleId == null) {
            throw new IllegalArgumentException("Parameter jobScheduleId is required and cannot be null.");
        }
        if (jobScheduleUpdateParameter == null) {
            throw new IllegalArgumentException("Parameter jobScheduleUpdateParameter is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(jobScheduleUpdateParameter);
        Validator.validate(jobScheduleUpdateOptions);
        Integer timeout = null;
        if (jobScheduleUpdateOptions != null) {
            timeout = jobScheduleUpdateOptions.timeout();
        }
        String clientRequestId = null;
        if (jobScheduleUpdateOptions != null) {
            clientRequestId = jobScheduleUpdateOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobScheduleUpdateOptions != null) {
            returnClientRequestId = jobScheduleUpdateOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobScheduleUpdateOptions != null) {
            ocpDate = jobScheduleUpdateOptions.ocpDate();
        }
        String ifMatch = null;
        if (jobScheduleUpdateOptions != null) {
            ifMatch = jobScheduleUpdateOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (jobScheduleUpdateOptions != null) {
            ifNoneMatch = jobScheduleUpdateOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (jobScheduleUpdateOptions != null) {
            ifModifiedSince = jobScheduleUpdateOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (jobScheduleUpdateOptions != null) {
            ifUnmodifiedSince = jobScheduleUpdateOptions.ifUnmodifiedSince();
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
        Call<ResponseBody> call = service.update(jobScheduleId, jobScheduleUpdateParameter, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        return updateDelegate(call.execute());
    }

    /**
     * Updates the properties of the specified job schedule.
     *
     * @param jobScheduleId The id of the job schedule to update.
     * @param jobScheduleUpdateParameter The parameters for the request.
     * @param jobScheduleUpdateOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall updateAsync(String jobScheduleId, JobScheduleUpdateParameterInner jobScheduleUpdateParameter, JobScheduleUpdateOptionsInner jobScheduleUpdateOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (jobScheduleId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter jobScheduleId is required and cannot be null."));
            return null;
        }
        if (jobScheduleUpdateParameter == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter jobScheduleUpdateParameter is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(jobScheduleUpdateParameter, serviceCallback);
        Validator.validate(jobScheduleUpdateOptions, serviceCallback);
        Integer timeout = null;
        if (jobScheduleUpdateOptions != null) {
            timeout = jobScheduleUpdateOptions.timeout();
        }
        String clientRequestId = null;
        if (jobScheduleUpdateOptions != null) {
            clientRequestId = jobScheduleUpdateOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobScheduleUpdateOptions != null) {
            returnClientRequestId = jobScheduleUpdateOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobScheduleUpdateOptions != null) {
            ocpDate = jobScheduleUpdateOptions.ocpDate();
        }
        String ifMatch = null;
        if (jobScheduleUpdateOptions != null) {
            ifMatch = jobScheduleUpdateOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (jobScheduleUpdateOptions != null) {
            ifNoneMatch = jobScheduleUpdateOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (jobScheduleUpdateOptions != null) {
            ifModifiedSince = jobScheduleUpdateOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (jobScheduleUpdateOptions != null) {
            ifUnmodifiedSince = jobScheduleUpdateOptions.ifUnmodifiedSince();
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
        Call<ResponseBody> call = service.update(jobScheduleId, jobScheduleUpdateParameter, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
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

    private ServiceResponseWithHeaders<Void, JobScheduleUpdateHeadersInner> updateDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, JobScheduleUpdateHeadersInner.class);
    }

    /**
     * Disables a job schedule.
     *
     * @param jobScheduleId The id of the job schedule to disable.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, JobScheduleDisableHeadersInner> disable(String jobScheduleId) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobScheduleId == null) {
            throw new IllegalArgumentException("Parameter jobScheduleId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final JobScheduleDisableOptionsInner jobScheduleDisableOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        Call<ResponseBody> call = service.disable(jobScheduleId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        return disableDelegate(call.execute());
    }

    /**
     * Disables a job schedule.
     *
     * @param jobScheduleId The id of the job schedule to disable.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall disableAsync(String jobScheduleId, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
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
        final JobScheduleDisableOptionsInner jobScheduleDisableOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        Call<ResponseBody> call = service.disable(jobScheduleId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
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
     * Disables a job schedule.
     *
     * @param jobScheduleId The id of the job schedule to disable.
     * @param jobScheduleDisableOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, JobScheduleDisableHeadersInner> disable(String jobScheduleId, JobScheduleDisableOptionsInner jobScheduleDisableOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobScheduleId == null) {
            throw new IllegalArgumentException("Parameter jobScheduleId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(jobScheduleDisableOptions);
        Integer timeout = null;
        if (jobScheduleDisableOptions != null) {
            timeout = jobScheduleDisableOptions.timeout();
        }
        String clientRequestId = null;
        if (jobScheduleDisableOptions != null) {
            clientRequestId = jobScheduleDisableOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobScheduleDisableOptions != null) {
            returnClientRequestId = jobScheduleDisableOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobScheduleDisableOptions != null) {
            ocpDate = jobScheduleDisableOptions.ocpDate();
        }
        String ifMatch = null;
        if (jobScheduleDisableOptions != null) {
            ifMatch = jobScheduleDisableOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (jobScheduleDisableOptions != null) {
            ifNoneMatch = jobScheduleDisableOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (jobScheduleDisableOptions != null) {
            ifModifiedSince = jobScheduleDisableOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (jobScheduleDisableOptions != null) {
            ifUnmodifiedSince = jobScheduleDisableOptions.ifUnmodifiedSince();
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
        Call<ResponseBody> call = service.disable(jobScheduleId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        return disableDelegate(call.execute());
    }

    /**
     * Disables a job schedule.
     *
     * @param jobScheduleId The id of the job schedule to disable.
     * @param jobScheduleDisableOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall disableAsync(String jobScheduleId, JobScheduleDisableOptionsInner jobScheduleDisableOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
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
        Validator.validate(jobScheduleDisableOptions, serviceCallback);
        Integer timeout = null;
        if (jobScheduleDisableOptions != null) {
            timeout = jobScheduleDisableOptions.timeout();
        }
        String clientRequestId = null;
        if (jobScheduleDisableOptions != null) {
            clientRequestId = jobScheduleDisableOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobScheduleDisableOptions != null) {
            returnClientRequestId = jobScheduleDisableOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobScheduleDisableOptions != null) {
            ocpDate = jobScheduleDisableOptions.ocpDate();
        }
        String ifMatch = null;
        if (jobScheduleDisableOptions != null) {
            ifMatch = jobScheduleDisableOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (jobScheduleDisableOptions != null) {
            ifNoneMatch = jobScheduleDisableOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (jobScheduleDisableOptions != null) {
            ifModifiedSince = jobScheduleDisableOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (jobScheduleDisableOptions != null) {
            ifUnmodifiedSince = jobScheduleDisableOptions.ifUnmodifiedSince();
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
        Call<ResponseBody> call = service.disable(jobScheduleId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
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

    private ServiceResponseWithHeaders<Void, JobScheduleDisableHeadersInner> disableDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(204, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, JobScheduleDisableHeadersInner.class);
    }

    /**
     * Enables a job schedule.
     *
     * @param jobScheduleId The id of the job schedule to enable.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, JobScheduleEnableHeadersInner> enable(String jobScheduleId) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobScheduleId == null) {
            throw new IllegalArgumentException("Parameter jobScheduleId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final JobScheduleEnableOptionsInner jobScheduleEnableOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        Call<ResponseBody> call = service.enable(jobScheduleId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        return enableDelegate(call.execute());
    }

    /**
     * Enables a job schedule.
     *
     * @param jobScheduleId The id of the job schedule to enable.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall enableAsync(String jobScheduleId, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
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
        final JobScheduleEnableOptionsInner jobScheduleEnableOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        Call<ResponseBody> call = service.enable(jobScheduleId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
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
     * Enables a job schedule.
     *
     * @param jobScheduleId The id of the job schedule to enable.
     * @param jobScheduleEnableOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, JobScheduleEnableHeadersInner> enable(String jobScheduleId, JobScheduleEnableOptionsInner jobScheduleEnableOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobScheduleId == null) {
            throw new IllegalArgumentException("Parameter jobScheduleId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(jobScheduleEnableOptions);
        Integer timeout = null;
        if (jobScheduleEnableOptions != null) {
            timeout = jobScheduleEnableOptions.timeout();
        }
        String clientRequestId = null;
        if (jobScheduleEnableOptions != null) {
            clientRequestId = jobScheduleEnableOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobScheduleEnableOptions != null) {
            returnClientRequestId = jobScheduleEnableOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobScheduleEnableOptions != null) {
            ocpDate = jobScheduleEnableOptions.ocpDate();
        }
        String ifMatch = null;
        if (jobScheduleEnableOptions != null) {
            ifMatch = jobScheduleEnableOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (jobScheduleEnableOptions != null) {
            ifNoneMatch = jobScheduleEnableOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (jobScheduleEnableOptions != null) {
            ifModifiedSince = jobScheduleEnableOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (jobScheduleEnableOptions != null) {
            ifUnmodifiedSince = jobScheduleEnableOptions.ifUnmodifiedSince();
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
        Call<ResponseBody> call = service.enable(jobScheduleId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        return enableDelegate(call.execute());
    }

    /**
     * Enables a job schedule.
     *
     * @param jobScheduleId The id of the job schedule to enable.
     * @param jobScheduleEnableOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall enableAsync(String jobScheduleId, JobScheduleEnableOptionsInner jobScheduleEnableOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
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
        Validator.validate(jobScheduleEnableOptions, serviceCallback);
        Integer timeout = null;
        if (jobScheduleEnableOptions != null) {
            timeout = jobScheduleEnableOptions.timeout();
        }
        String clientRequestId = null;
        if (jobScheduleEnableOptions != null) {
            clientRequestId = jobScheduleEnableOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobScheduleEnableOptions != null) {
            returnClientRequestId = jobScheduleEnableOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobScheduleEnableOptions != null) {
            ocpDate = jobScheduleEnableOptions.ocpDate();
        }
        String ifMatch = null;
        if (jobScheduleEnableOptions != null) {
            ifMatch = jobScheduleEnableOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (jobScheduleEnableOptions != null) {
            ifNoneMatch = jobScheduleEnableOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (jobScheduleEnableOptions != null) {
            ifModifiedSince = jobScheduleEnableOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (jobScheduleEnableOptions != null) {
            ifUnmodifiedSince = jobScheduleEnableOptions.ifUnmodifiedSince();
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
        Call<ResponseBody> call = service.enable(jobScheduleId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
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

    private ServiceResponseWithHeaders<Void, JobScheduleEnableHeadersInner> enableDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(204, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, JobScheduleEnableHeadersInner.class);
    }

    /**
     * Terminates a job schedule.
     *
     * @param jobScheduleId The id of the job schedule to terminates.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, JobScheduleTerminateHeadersInner> terminate(String jobScheduleId) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobScheduleId == null) {
            throw new IllegalArgumentException("Parameter jobScheduleId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final JobScheduleTerminateOptionsInner jobScheduleTerminateOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        Call<ResponseBody> call = service.terminate(jobScheduleId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        return terminateDelegate(call.execute());
    }

    /**
     * Terminates a job schedule.
     *
     * @param jobScheduleId The id of the job schedule to terminates.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall terminateAsync(String jobScheduleId, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
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
        final JobScheduleTerminateOptionsInner jobScheduleTerminateOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        Call<ResponseBody> call = service.terminate(jobScheduleId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
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
     * Terminates a job schedule.
     *
     * @param jobScheduleId The id of the job schedule to terminates.
     * @param jobScheduleTerminateOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, JobScheduleTerminateHeadersInner> terminate(String jobScheduleId, JobScheduleTerminateOptionsInner jobScheduleTerminateOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobScheduleId == null) {
            throw new IllegalArgumentException("Parameter jobScheduleId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(jobScheduleTerminateOptions);
        Integer timeout = null;
        if (jobScheduleTerminateOptions != null) {
            timeout = jobScheduleTerminateOptions.timeout();
        }
        String clientRequestId = null;
        if (jobScheduleTerminateOptions != null) {
            clientRequestId = jobScheduleTerminateOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobScheduleTerminateOptions != null) {
            returnClientRequestId = jobScheduleTerminateOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobScheduleTerminateOptions != null) {
            ocpDate = jobScheduleTerminateOptions.ocpDate();
        }
        String ifMatch = null;
        if (jobScheduleTerminateOptions != null) {
            ifMatch = jobScheduleTerminateOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (jobScheduleTerminateOptions != null) {
            ifNoneMatch = jobScheduleTerminateOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (jobScheduleTerminateOptions != null) {
            ifModifiedSince = jobScheduleTerminateOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (jobScheduleTerminateOptions != null) {
            ifUnmodifiedSince = jobScheduleTerminateOptions.ifUnmodifiedSince();
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
        Call<ResponseBody> call = service.terminate(jobScheduleId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        return terminateDelegate(call.execute());
    }

    /**
     * Terminates a job schedule.
     *
     * @param jobScheduleId The id of the job schedule to terminates.
     * @param jobScheduleTerminateOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall terminateAsync(String jobScheduleId, JobScheduleTerminateOptionsInner jobScheduleTerminateOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
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
        Validator.validate(jobScheduleTerminateOptions, serviceCallback);
        Integer timeout = null;
        if (jobScheduleTerminateOptions != null) {
            timeout = jobScheduleTerminateOptions.timeout();
        }
        String clientRequestId = null;
        if (jobScheduleTerminateOptions != null) {
            clientRequestId = jobScheduleTerminateOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobScheduleTerminateOptions != null) {
            returnClientRequestId = jobScheduleTerminateOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobScheduleTerminateOptions != null) {
            ocpDate = jobScheduleTerminateOptions.ocpDate();
        }
        String ifMatch = null;
        if (jobScheduleTerminateOptions != null) {
            ifMatch = jobScheduleTerminateOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (jobScheduleTerminateOptions != null) {
            ifNoneMatch = jobScheduleTerminateOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (jobScheduleTerminateOptions != null) {
            ifModifiedSince = jobScheduleTerminateOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (jobScheduleTerminateOptions != null) {
            ifUnmodifiedSince = jobScheduleTerminateOptions.ifUnmodifiedSince();
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
        Call<ResponseBody> call = service.terminate(jobScheduleId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
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

    private ServiceResponseWithHeaders<Void, JobScheduleTerminateHeadersInner> terminateDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(202, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, JobScheduleTerminateHeadersInner.class);
    }

    /**
     * Adds a job schedule to the specified account.
     *
     * @param cloudJobSchedule Specifies the job schedule to be added.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, JobScheduleAddHeadersInner> add(JobScheduleAddParameterInner cloudJobSchedule) throws BatchErrorException, IOException, IllegalArgumentException {
        if (cloudJobSchedule == null) {
            throw new IllegalArgumentException("Parameter cloudJobSchedule is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(cloudJobSchedule);
        final JobScheduleAddOptionsInner jobScheduleAddOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.add(cloudJobSchedule, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        return addDelegate(call.execute());
    }

    /**
     * Adds a job schedule to the specified account.
     *
     * @param cloudJobSchedule Specifies the job schedule to be added.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall addAsync(JobScheduleAddParameterInner cloudJobSchedule, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (cloudJobSchedule == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter cloudJobSchedule is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(cloudJobSchedule, serviceCallback);
        final JobScheduleAddOptionsInner jobScheduleAddOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.add(cloudJobSchedule, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
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
     * Adds a job schedule to the specified account.
     *
     * @param cloudJobSchedule Specifies the job schedule to be added.
     * @param jobScheduleAddOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, JobScheduleAddHeadersInner> add(JobScheduleAddParameterInner cloudJobSchedule, JobScheduleAddOptionsInner jobScheduleAddOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (cloudJobSchedule == null) {
            throw new IllegalArgumentException("Parameter cloudJobSchedule is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(cloudJobSchedule);
        Validator.validate(jobScheduleAddOptions);
        Integer timeout = null;
        if (jobScheduleAddOptions != null) {
            timeout = jobScheduleAddOptions.timeout();
        }
        String clientRequestId = null;
        if (jobScheduleAddOptions != null) {
            clientRequestId = jobScheduleAddOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobScheduleAddOptions != null) {
            returnClientRequestId = jobScheduleAddOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobScheduleAddOptions != null) {
            ocpDate = jobScheduleAddOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.add(cloudJobSchedule, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        return addDelegate(call.execute());
    }

    /**
     * Adds a job schedule to the specified account.
     *
     * @param cloudJobSchedule Specifies the job schedule to be added.
     * @param jobScheduleAddOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall addAsync(JobScheduleAddParameterInner cloudJobSchedule, JobScheduleAddOptionsInner jobScheduleAddOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (cloudJobSchedule == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter cloudJobSchedule is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(cloudJobSchedule, serviceCallback);
        Validator.validate(jobScheduleAddOptions, serviceCallback);
        Integer timeout = null;
        if (jobScheduleAddOptions != null) {
            timeout = jobScheduleAddOptions.timeout();
        }
        String clientRequestId = null;
        if (jobScheduleAddOptions != null) {
            clientRequestId = jobScheduleAddOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobScheduleAddOptions != null) {
            returnClientRequestId = jobScheduleAddOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobScheduleAddOptions != null) {
            ocpDate = jobScheduleAddOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.add(cloudJobSchedule, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
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

    private ServiceResponseWithHeaders<Void, JobScheduleAddHeadersInner> addDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(201, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, JobScheduleAddHeadersInner.class);
    }

    /**
     * Lists all of the job schedules in the specified account.
     *
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;CloudJobScheduleInner&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PagedList<CloudJobScheduleInner>, JobScheduleListHeadersInner> list() throws BatchErrorException, IOException, IllegalArgumentException {
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final JobScheduleListOptionsInner jobScheduleListOptions = null;
        String filter = null;
        String select = null;
        String expand = null;
        Integer maxResults = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.list(this.client.apiVersion(), this.client.acceptLanguage(), filter, select, expand, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        ServiceResponseWithHeaders<PageImpl<CloudJobScheduleInner>, JobScheduleListHeadersInner> response = listDelegate(call.execute());
        PagedList<CloudJobScheduleInner> result = new PagedList<CloudJobScheduleInner>(response.getBody()) {
            @Override
            public Page<CloudJobScheduleInner> nextPage(String nextPageLink) throws BatchErrorException, IOException {
                return listNext(nextPageLink, null).getBody();
            }
        };
        return new ServiceResponseWithHeaders<>(result, response.getHeaders(), response.getResponse());
    }

    /**
     * Lists all of the job schedules in the specified account.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listAsync(final ListOperationCallback<CloudJobScheduleInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final JobScheduleListOptionsInner jobScheduleListOptions = null;
        String filter = null;
        String select = null;
        String expand = null;
        Integer maxResults = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.list(this.client.apiVersion(), this.client.acceptLanguage(), filter, select, expand, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<CloudJobScheduleInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<CloudJobScheduleInner>, JobScheduleListHeadersInner> result = listDelegate(response);
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
     * Lists all of the job schedules in the specified account.
     *
     * @param jobScheduleListOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;CloudJobScheduleInner&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PagedList<CloudJobScheduleInner>, JobScheduleListHeadersInner> list(final JobScheduleListOptionsInner jobScheduleListOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(jobScheduleListOptions);
        String filter = null;
        if (jobScheduleListOptions != null) {
            filter = jobScheduleListOptions.filter();
        }
        String select = null;
        if (jobScheduleListOptions != null) {
            select = jobScheduleListOptions.select();
        }
        String expand = null;
        if (jobScheduleListOptions != null) {
            expand = jobScheduleListOptions.expand();
        }
        Integer maxResults = null;
        if (jobScheduleListOptions != null) {
            maxResults = jobScheduleListOptions.maxResults();
        }
        Integer timeout = null;
        if (jobScheduleListOptions != null) {
            timeout = jobScheduleListOptions.timeout();
        }
        String clientRequestId = null;
        if (jobScheduleListOptions != null) {
            clientRequestId = jobScheduleListOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobScheduleListOptions != null) {
            returnClientRequestId = jobScheduleListOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobScheduleListOptions != null) {
            ocpDate = jobScheduleListOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.list(this.client.apiVersion(), this.client.acceptLanguage(), filter, select, expand, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        ServiceResponseWithHeaders<PageImpl<CloudJobScheduleInner>, JobScheduleListHeadersInner> response = listDelegate(call.execute());
        PagedList<CloudJobScheduleInner> result = new PagedList<CloudJobScheduleInner>(response.getBody()) {
            @Override
            public Page<CloudJobScheduleInner> nextPage(String nextPageLink) throws BatchErrorException, IOException {
                JobScheduleListNextOptionsInner jobScheduleListNextOptions = null;
                if (jobScheduleListOptions != null) {
                    jobScheduleListNextOptions = new JobScheduleListNextOptionsInner();
                    jobScheduleListNextOptions.setClientRequestId(jobScheduleListOptions.clientRequestId());
                    jobScheduleListNextOptions.setReturnClientRequestId(jobScheduleListOptions.returnClientRequestId());
                    jobScheduleListNextOptions.setOcpDate(jobScheduleListOptions.ocpDate());
                }
                return listNext(nextPageLink, jobScheduleListNextOptions).getBody();
            }
        };
        return new ServiceResponseWithHeaders<>(result, response.getHeaders(), response.getResponse());
    }

    /**
     * Lists all of the job schedules in the specified account.
     *
     * @param jobScheduleListOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listAsync(final JobScheduleListOptionsInner jobScheduleListOptions, final ListOperationCallback<CloudJobScheduleInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(jobScheduleListOptions, serviceCallback);
        String filter = null;
        if (jobScheduleListOptions != null) {
            filter = jobScheduleListOptions.filter();
        }
        String select = null;
        if (jobScheduleListOptions != null) {
            select = jobScheduleListOptions.select();
        }
        String expand = null;
        if (jobScheduleListOptions != null) {
            expand = jobScheduleListOptions.expand();
        }
        Integer maxResults = null;
        if (jobScheduleListOptions != null) {
            maxResults = jobScheduleListOptions.maxResults();
        }
        Integer timeout = null;
        if (jobScheduleListOptions != null) {
            timeout = jobScheduleListOptions.timeout();
        }
        String clientRequestId = null;
        if (jobScheduleListOptions != null) {
            clientRequestId = jobScheduleListOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobScheduleListOptions != null) {
            returnClientRequestId = jobScheduleListOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobScheduleListOptions != null) {
            ocpDate = jobScheduleListOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.list(this.client.apiVersion(), this.client.acceptLanguage(), filter, select, expand, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<CloudJobScheduleInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<CloudJobScheduleInner>, JobScheduleListHeadersInner> result = listDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        JobScheduleListNextOptionsInner jobScheduleListNextOptions = null;
                        if (jobScheduleListOptions != null) {
                            jobScheduleListNextOptions = new JobScheduleListNextOptionsInner();
                            jobScheduleListNextOptions.setClientRequestId(jobScheduleListOptions.clientRequestId());
                            jobScheduleListNextOptions.setReturnClientRequestId(jobScheduleListOptions.returnClientRequestId());
                            jobScheduleListNextOptions.setOcpDate(jobScheduleListOptions.ocpDate());
                        }
                        listNextAsync(result.getBody().getNextPageLink(), jobScheduleListNextOptions, serviceCall, serviceCallback);
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

    private ServiceResponseWithHeaders<PageImpl<CloudJobScheduleInner>, JobScheduleListHeadersInner> listDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<CloudJobScheduleInner>, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<CloudJobScheduleInner>>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, JobScheduleListHeadersInner.class);
    }

    /**
     * Lists all of the job schedules in the specified account.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;CloudJobScheduleInner&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PageImpl<CloudJobScheduleInner>, JobScheduleListHeadersInner> listNext(final String nextPageLink) throws BatchErrorException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        final JobScheduleListNextOptionsInner jobScheduleListNextOptions = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.acceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted);
        return listNextDelegate(call.execute());
    }

    /**
     * Lists all of the job schedules in the specified account.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<CloudJobScheduleInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        final JobScheduleListNextOptionsInner jobScheduleListNextOptions = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.acceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted);
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<CloudJobScheduleInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<CloudJobScheduleInner>, JobScheduleListHeadersInner> result = listNextDelegate(response);
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
     * Lists all of the job schedules in the specified account.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param jobScheduleListNextOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;CloudJobScheduleInner&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PageImpl<CloudJobScheduleInner>, JobScheduleListHeadersInner> listNext(final String nextPageLink, final JobScheduleListNextOptionsInner jobScheduleListNextOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        Validator.validate(jobScheduleListNextOptions);
        String clientRequestId = null;
        if (jobScheduleListNextOptions != null) {
            clientRequestId = jobScheduleListNextOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobScheduleListNextOptions != null) {
            returnClientRequestId = jobScheduleListNextOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobScheduleListNextOptions != null) {
            ocpDate = jobScheduleListNextOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.acceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted);
        return listNextDelegate(call.execute());
    }

    /**
     * Lists all of the job schedules in the specified account.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param jobScheduleListNextOptions Additional parameters for the operation
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listNextAsync(final String nextPageLink, final JobScheduleListNextOptionsInner jobScheduleListNextOptions, final ServiceCall serviceCall, final ListOperationCallback<CloudJobScheduleInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        Validator.validate(jobScheduleListNextOptions, serviceCallback);
        String clientRequestId = null;
        if (jobScheduleListNextOptions != null) {
            clientRequestId = jobScheduleListNextOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (jobScheduleListNextOptions != null) {
            returnClientRequestId = jobScheduleListNextOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (jobScheduleListNextOptions != null) {
            ocpDate = jobScheduleListNextOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.acceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted);
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<CloudJobScheduleInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<CloudJobScheduleInner>, JobScheduleListHeadersInner> result = listNextDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listNextAsync(result.getBody().getNextPageLink(), jobScheduleListNextOptions, serviceCall, serviceCallback);
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

    private ServiceResponseWithHeaders<PageImpl<CloudJobScheduleInner>, JobScheduleListHeadersInner> listNextDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<CloudJobScheduleInner>, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<CloudJobScheduleInner>>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, JobScheduleListHeadersInner.class);
    }

}
