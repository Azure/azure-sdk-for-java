/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.implementation;

import retrofit2.Retrofit;
import com.microsoft.azure.management.datalake.analytics.Jobs;
import com.google.common.base.Joiner;
import com.google.common.reflect.TypeToken;
import com.microsoft.azure.AzureServiceResponseBuilder;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.ListOperationCallback;
import com.microsoft.azure.management.datalake.analytics.models.JobDataPath;
import com.microsoft.azure.management.datalake.analytics.models.JobInformation;
import com.microsoft.azure.management.datalake.analytics.models.JobStatistics;
import com.microsoft.azure.management.datalake.analytics.models.PageImpl;
import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;
import com.microsoft.rest.ServiceResponseCallback;
import com.microsoft.rest.Validator;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
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
    private DataLakeAnalyticsJobManagementClientImpl client;

    /**
     * Initializes an instance of JobsImpl.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public JobsImpl(Retrofit retrofit, DataLakeAnalyticsJobManagementClientImpl client) {
        this.service = retrofit.create(JobsService.class);
        this.client = client;
    }

    /**
     * The interface defining all the services for Jobs to be
     * used by Retrofit to perform actually REST calls.
     */
    interface JobsService {
        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("Jobs/{jobIdentity}/GetStatistics")
        Call<ResponseBody> getStatistics(@Path("jobIdentity") UUID jobIdentity, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @POST("Jobs/{jobIdentity}/GetDebugDataPath")
        Call<ResponseBody> getDebugDataPath(@Path("jobIdentity") UUID jobIdentity, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @POST("BuildJob")
        Call<ResponseBody> build(@Body JobInformation parameters, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @POST("Jobs/{jobIdentity}/CancelJob")
        Call<ResponseBody> cancel(@Path("jobIdentity") UUID jobIdentity, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("Jobs/{jobIdentity}")
        Call<ResponseBody> get(@Path("jobIdentity") UUID jobIdentity, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @PUT("Jobs/{jobIdentity}")
        Call<ResponseBody> create(@Path("jobIdentity") UUID jobIdentity, @Body JobInformation parameters, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("Jobs")
        Call<ResponseBody> list(@Query("$filter") String filter, @Query("$top") Integer top, @Query("$skip") Integer skip, @Query("$expand") String expand, @Query("$select") String select, @Query("$orderby") String orderby, @Query("$count") Boolean count, @Query("$search") String search, @Query("$format") String format, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET
        Call<ResponseBody> listNext(@Url String nextPageLink, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

    }

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
    public ServiceResponse<JobStatistics> getStatistics(String accountName, UUID jobIdentity) throws CloudException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlaJobDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlaJobDnsSuffix() is required and cannot be null.");
        }
        if (jobIdentity == null) {
            throw new IllegalArgumentException("Parameter jobIdentity is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaJobDnsSuffix}", this.client.adlaJobDnsSuffix());
        Call<ResponseBody> call = service.getStatistics(jobIdentity, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        return getStatisticsDelegate(call.execute());
    }

    /**
     * Gets statistics of the specified job.
     *
     * @param accountName The Azure Data Lake Analytics account to execute job operations on.
     * @param jobIdentity JobInfo ID.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getStatisticsAsync(String accountName, UUID jobIdentity, final ServiceCallback<JobStatistics> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlaJobDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlaJobDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (jobIdentity == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter jobIdentity is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaJobDnsSuffix}", this.client.adlaJobDnsSuffix());
        Call<ResponseBody> call = service.getStatistics(jobIdentity, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<JobStatistics>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getStatisticsDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<JobStatistics> getStatisticsDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<JobStatistics, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<JobStatistics>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

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
    public ServiceResponse<JobDataPath> getDebugDataPath(String accountName, UUID jobIdentity) throws CloudException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlaJobDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlaJobDnsSuffix() is required and cannot be null.");
        }
        if (jobIdentity == null) {
            throw new IllegalArgumentException("Parameter jobIdentity is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaJobDnsSuffix}", this.client.adlaJobDnsSuffix());
        Call<ResponseBody> call = service.getDebugDataPath(jobIdentity, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        return getDebugDataPathDelegate(call.execute());
    }

    /**
     * Gets the job debug data information specified by the job ID.
     *
     * @param accountName The Azure Data Lake Analytics account to execute job operations on.
     * @param jobIdentity JobInfo ID.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getDebugDataPathAsync(String accountName, UUID jobIdentity, final ServiceCallback<JobDataPath> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlaJobDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlaJobDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (jobIdentity == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter jobIdentity is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaJobDnsSuffix}", this.client.adlaJobDnsSuffix());
        Call<ResponseBody> call = service.getDebugDataPath(jobIdentity, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<JobDataPath>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getDebugDataPathDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<JobDataPath> getDebugDataPathDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<JobDataPath, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<JobDataPath>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

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
    public ServiceResponse<JobInformation> build(String accountName, JobInformation parameters) throws CloudException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlaJobDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlaJobDnsSuffix() is required and cannot be null.");
        }
        if (parameters == null) {
            throw new IllegalArgumentException("Parameter parameters is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(parameters);
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaJobDnsSuffix}", this.client.adlaJobDnsSuffix());
        Call<ResponseBody> call = service.build(parameters, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        return buildDelegate(call.execute());
    }

    /**
     * Builds (compiles) the specified job in the specified Data Lake Analytics account for job correctness and validation.
     *
     * @param accountName The Azure Data Lake Analytics account to execute job operations on.
     * @param parameters The parameters to build a job.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall buildAsync(String accountName, JobInformation parameters, final ServiceCallback<JobInformation> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlaJobDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlaJobDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (parameters == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter parameters is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(parameters, serviceCallback);
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaJobDnsSuffix}", this.client.adlaJobDnsSuffix());
        Call<ResponseBody> call = service.build(parameters, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<JobInformation>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(buildDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<JobInformation> buildDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<JobInformation, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<JobInformation>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

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
    public ServiceResponse<Void> cancel(String accountName, UUID jobIdentity) throws CloudException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlaJobDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlaJobDnsSuffix() is required and cannot be null.");
        }
        if (jobIdentity == null) {
            throw new IllegalArgumentException("Parameter jobIdentity is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaJobDnsSuffix}", this.client.adlaJobDnsSuffix());
        Call<ResponseBody> call = service.cancel(jobIdentity, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        return cancelDelegate(call.execute());
    }

    /**
     * Cancels the running job specified by the job ID.
     *
     * @param accountName The Azure Data Lake Analytics account to execute job operations on.
     * @param jobIdentity JobInfo ID to cancel.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall cancelAsync(String accountName, UUID jobIdentity, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlaJobDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlaJobDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (jobIdentity == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter jobIdentity is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaJobDnsSuffix}", this.client.adlaJobDnsSuffix());
        Call<ResponseBody> call = service.cancel(jobIdentity, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(cancelDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<Void> cancelDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .build(response);
    }

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
    public ServiceResponse<JobInformation> get(String accountName, UUID jobIdentity) throws CloudException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlaJobDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlaJobDnsSuffix() is required and cannot be null.");
        }
        if (jobIdentity == null) {
            throw new IllegalArgumentException("Parameter jobIdentity is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaJobDnsSuffix}", this.client.adlaJobDnsSuffix());
        Call<ResponseBody> call = service.get(jobIdentity, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        return getDelegate(call.execute());
    }

    /**
     * Gets the job information for the specified job ID.
     *
     * @param accountName The Azure Data Lake Analytics account to execute job operations on.
     * @param jobIdentity JobInfo ID.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getAsync(String accountName, UUID jobIdentity, final ServiceCallback<JobInformation> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlaJobDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlaJobDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (jobIdentity == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter jobIdentity is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaJobDnsSuffix}", this.client.adlaJobDnsSuffix());
        Call<ResponseBody> call = service.get(jobIdentity, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<JobInformation>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<JobInformation> getDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<JobInformation, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<JobInformation>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

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
    public ServiceResponse<JobInformation> create(String accountName, UUID jobIdentity, JobInformation parameters) throws CloudException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlaJobDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlaJobDnsSuffix() is required and cannot be null.");
        }
        if (jobIdentity == null) {
            throw new IllegalArgumentException("Parameter jobIdentity is required and cannot be null.");
        }
        if (parameters == null) {
            throw new IllegalArgumentException("Parameter parameters is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(parameters);
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaJobDnsSuffix}", this.client.adlaJobDnsSuffix());
        Call<ResponseBody> call = service.create(jobIdentity, parameters, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        return createDelegate(call.execute());
    }

    /**
     * Submits a job to the specified Data Lake Analytics account.
     *
     * @param accountName The Azure Data Lake Analytics account to execute job operations on.
     * @param jobIdentity The job ID (a GUID) for the job being submitted.
     * @param parameters The parameters to submit a job.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall createAsync(String accountName, UUID jobIdentity, JobInformation parameters, final ServiceCallback<JobInformation> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlaJobDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlaJobDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (jobIdentity == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter jobIdentity is required and cannot be null."));
            return null;
        }
        if (parameters == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter parameters is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(parameters, serviceCallback);
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaJobDnsSuffix}", this.client.adlaJobDnsSuffix());
        Call<ResponseBody> call = service.create(jobIdentity, parameters, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<JobInformation>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(createDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<JobInformation> createDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<JobInformation, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<JobInformation>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Lists the jobs, if any, associated with the specified Data Lake Analytics account. The response includes a link to the next page of results, if any.
     *
     * @param accountName The Azure Data Lake Analytics account to execute job operations on.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;JobInformation&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<JobInformation>> list(final String accountName) throws CloudException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlaJobDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlaJobDnsSuffix() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String filter = null;
        final Integer top = null;
        final Integer skip = null;
        final String expand = null;
        final String select = null;
        final String orderby = null;
        final Boolean count = null;
        final String search = null;
        final String format = null;
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaJobDnsSuffix}", this.client.adlaJobDnsSuffix());
        Call<ResponseBody> call = service.list(filter, top, skip, expand, select, orderby, count, search, format, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        ServiceResponse<PageImpl<JobInformation>> response = listDelegate(call.execute());
        PagedList<JobInformation> result = new PagedList<JobInformation>(response.getBody()) {
            @Override
            public Page<JobInformation> nextPage(String nextPageLink) throws CloudException, IOException {
                return listNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Lists the jobs, if any, associated with the specified Data Lake Analytics account. The response includes a link to the next page of results, if any.
     *
     * @param accountName The Azure Data Lake Analytics account to execute job operations on.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listAsync(final String accountName, final ListOperationCallback<JobInformation> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlaJobDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlaJobDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final String filter = null;
        final Integer top = null;
        final Integer skip = null;
        final String expand = null;
        final String select = null;
        final String orderby = null;
        final Boolean count = null;
        final String search = null;
        final String format = null;
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaJobDnsSuffix}", this.client.adlaJobDnsSuffix());
        Call<ResponseBody> call = service.list(filter, top, skip, expand, select, orderby, count, search, format, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<JobInformation>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<JobInformation>> result = listDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponse<>(serviceCallback.get(), result.getResponse()));
                    }
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

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
    public ServiceResponse<PagedList<JobInformation>> list(final String accountName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count, final String search, final String format) throws CloudException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlaJobDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlaJobDnsSuffix() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaJobDnsSuffix}", this.client.adlaJobDnsSuffix());
        Call<ResponseBody> call = service.list(filter, top, skip, expand, select, orderby, count, search, format, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        ServiceResponse<PageImpl<JobInformation>> response = listDelegate(call.execute());
        PagedList<JobInformation> result = new PagedList<JobInformation>(response.getBody()) {
            @Override
            public Page<JobInformation> nextPage(String nextPageLink) throws CloudException, IOException {
                return listNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

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
     * @return the {@link Call} object
     */
    public ServiceCall listAsync(final String accountName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count, final String search, final String format, final ListOperationCallback<JobInformation> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlaJobDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlaJobDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaJobDnsSuffix}", this.client.adlaJobDnsSuffix());
        Call<ResponseBody> call = service.list(filter, top, skip, expand, select, orderby, count, search, format, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<JobInformation>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<JobInformation>> result = listDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponse<>(serviceCallback.get(), result.getResponse()));
                    }
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<PageImpl<JobInformation>> listDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<JobInformation>, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<PageImpl<JobInformation>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Lists the jobs, if any, associated with the specified Data Lake Analytics account. The response includes a link to the next page of results, if any.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;JobInformation&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl<JobInformation>> listNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        return listNextDelegate(call.execute());
    }

    /**
     * Lists the jobs, if any, associated with the specified Data Lake Analytics account. The response includes a link to the next page of results, if any.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<JobInformation> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<JobInformation>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<JobInformation>> result = listNextDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponse<>(serviceCallback.get(), result.getResponse()));
                    }
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<PageImpl<JobInformation>> listNextDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<JobInformation>, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<PageImpl<JobInformation>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

}
