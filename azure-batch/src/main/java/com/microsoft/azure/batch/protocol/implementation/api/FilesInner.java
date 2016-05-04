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
import java.io.InputStream;
import java.io.IOException;
import java.util.List;
import okhttp3.ResponseBody;
import org.joda.time.DateTime;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.HEAD;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.HTTP;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;
import retrofit2.Response;

/**
 * An instance of this class provides access to all the operations defined
 * in Files.
 */
public final class FilesInner {
    /** The Retrofit service to perform REST calls. */
    private FilesService service;
    /** The service client containing this operation class. */
    private BatchServiceClientImpl client;

    /**
     * Initializes an instance of FilesInner.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public FilesInner(Retrofit retrofit, BatchServiceClientImpl client) {
        this.service = retrofit.create(FilesService.class);
        this.client = client;
    }

    /**
     * The interface defining all the services for Files to be
     * used by Retrofit to perform actually REST calls.
     */
    interface FilesService {
        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @HTTP(path = "jobs/{jobId}/tasks/{taskId}/files/{fileName}", method = "DELETE", hasBody = true)
        Call<ResponseBody> deleteFromTask(@Path("jobId") String jobId, @Path("taskId") String taskId, @Path("fileName") String fileName, @Query("recursive") Boolean recursive, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @GET("jobs/{jobId}/tasks/{taskId}/files/{fileName}")
        @Streaming
        Call<ResponseBody> getFromTask(@Path("jobId") String jobId, @Path("taskId") String taskId, @Path("fileName") String fileName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Header("ocp-range") String ocpRange, @Header("If-Modified-Since") DateTimeRfc1123 ifModifiedSince, @Header("If-Unmodified-Since") DateTimeRfc1123 ifUnmodifiedSince);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @HEAD("jobs/{jobId}/tasks/{taskId}/files/{fileName}")
        Call<Void> getNodeFilePropertiesFromTask(@Path("jobId") String jobId, @Path("taskId") String taskId, @Path("fileName") String fileName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Header("If-Modified-Since") DateTimeRfc1123 ifModifiedSince, @Header("If-Unmodified-Since") DateTimeRfc1123 ifUnmodifiedSince);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @HTTP(path = "pools/{poolId}/nodes/{nodeId}/files/{fileName}", method = "DELETE", hasBody = true)
        Call<ResponseBody> deleteFromComputeNode(@Path("poolId") String poolId, @Path("nodeId") String nodeId, @Path("fileName") String fileName, @Query("recursive") Boolean recursive, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @GET("pools/{poolId}/nodes/{nodeId}/files/{fileName}")
        @Streaming
        Call<ResponseBody> getFromComputeNode(@Path("poolId") String poolId, @Path("nodeId") String nodeId, @Path("fileName") String fileName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Header("ocp-range") String ocpRange, @Header("If-Modified-Since") DateTimeRfc1123 ifModifiedSince, @Header("If-Unmodified-Since") DateTimeRfc1123 ifUnmodifiedSince);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @HEAD("pools/{poolId}/nodes/{nodeId}/files/{fileName}")
        Call<Void> getNodeFilePropertiesFromComputeNode(@Path("poolId") String poolId, @Path("nodeId") String nodeId, @Path("fileName") String fileName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Header("If-Modified-Since") DateTimeRfc1123 ifModifiedSince, @Header("If-Unmodified-Since") DateTimeRfc1123 ifUnmodifiedSince);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @GET("jobs/{jobId}/tasks/{taskId}/files")
        Call<ResponseBody> listFromTask(@Path("jobId") String jobId, @Path("taskId") String taskId, @Query("recursive") Boolean recursive, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("$filter") String filter, @Query("maxresults") Integer maxResults, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @GET("pools/{poolId}/nodes/{nodeId}/files")
        Call<ResponseBody> listFromComputeNode(@Path("poolId") String poolId, @Path("nodeId") String nodeId, @Query("recursive") Boolean recursive, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("$filter") String filter, @Query("maxresults") Integer maxResults, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @GET
        Call<ResponseBody> listFromTaskNext(@Url String nextPageLink, @Header("accept-language") String acceptLanguage, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @GET
        Call<ResponseBody> listFromComputeNodeNext(@Url String nextPageLink, @Header("accept-language") String acceptLanguage, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate);

    }

    /**
     * Deletes the specified task file from the compute node where the task ran.
     *
     * @param jobId The id of the job that contains the task.
     * @param taskId The id of the task whose file you want to delete.
     * @param fileName The path to the task file that you want to delete.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, FileDeleteFromTaskHeadersInner> deleteFromTask(String jobId, String taskId, String fileName) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (taskId == null) {
            throw new IllegalArgumentException("Parameter taskId is required and cannot be null.");
        }
        if (fileName == null) {
            throw new IllegalArgumentException("Parameter fileName is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final Boolean recursive = null;
        final FileDeleteFromTaskOptionsInner fileDeleteFromTaskOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.deleteFromTask(jobId, taskId, fileName, recursive, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        return deleteFromTaskDelegate(call.execute());
    }

    /**
     * Deletes the specified task file from the compute node where the task ran.
     *
     * @param jobId The id of the job that contains the task.
     * @param taskId The id of the task whose file you want to delete.
     * @param fileName The path to the task file that you want to delete.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall deleteFromTaskAsync(String jobId, String taskId, String fileName, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (jobId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter jobId is required and cannot be null."));
            return null;
        }
        if (taskId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter taskId is required and cannot be null."));
            return null;
        }
        if (fileName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter fileName is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final Boolean recursive = null;
        final FileDeleteFromTaskOptionsInner fileDeleteFromTaskOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.deleteFromTask(jobId, taskId, fileName, recursive, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(deleteFromTaskDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Deletes the specified task file from the compute node where the task ran.
     *
     * @param jobId The id of the job that contains the task.
     * @param taskId The id of the task whose file you want to delete.
     * @param fileName The path to the task file that you want to delete.
     * @param recursive Sets whether to delete children of a directory. If the fileName parameter represents a directory instead of a file, you can set Recursive to true to delete the directory and all of the files and subdirectories in it. If Recursive is false then the directory must be empty or deletion will fail.
     * @param fileDeleteFromTaskOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, FileDeleteFromTaskHeadersInner> deleteFromTask(String jobId, String taskId, String fileName, Boolean recursive, FileDeleteFromTaskOptionsInner fileDeleteFromTaskOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (taskId == null) {
            throw new IllegalArgumentException("Parameter taskId is required and cannot be null.");
        }
        if (fileName == null) {
            throw new IllegalArgumentException("Parameter fileName is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(fileDeleteFromTaskOptions);
        Integer timeout = null;
        if (fileDeleteFromTaskOptions != null) {
            timeout = fileDeleteFromTaskOptions.timeout();
        }
        String clientRequestId = null;
        if (fileDeleteFromTaskOptions != null) {
            clientRequestId = fileDeleteFromTaskOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (fileDeleteFromTaskOptions != null) {
            returnClientRequestId = fileDeleteFromTaskOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (fileDeleteFromTaskOptions != null) {
            ocpDate = fileDeleteFromTaskOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.deleteFromTask(jobId, taskId, fileName, recursive, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        return deleteFromTaskDelegate(call.execute());
    }

    /**
     * Deletes the specified task file from the compute node where the task ran.
     *
     * @param jobId The id of the job that contains the task.
     * @param taskId The id of the task whose file you want to delete.
     * @param fileName The path to the task file that you want to delete.
     * @param recursive Sets whether to delete children of a directory. If the fileName parameter represents a directory instead of a file, you can set Recursive to true to delete the directory and all of the files and subdirectories in it. If Recursive is false then the directory must be empty or deletion will fail.
     * @param fileDeleteFromTaskOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall deleteFromTaskAsync(String jobId, String taskId, String fileName, Boolean recursive, FileDeleteFromTaskOptionsInner fileDeleteFromTaskOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (jobId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter jobId is required and cannot be null."));
            return null;
        }
        if (taskId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter taskId is required and cannot be null."));
            return null;
        }
        if (fileName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter fileName is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(fileDeleteFromTaskOptions, serviceCallback);
        Integer timeout = null;
        if (fileDeleteFromTaskOptions != null) {
            timeout = fileDeleteFromTaskOptions.timeout();
        }
        String clientRequestId = null;
        if (fileDeleteFromTaskOptions != null) {
            clientRequestId = fileDeleteFromTaskOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (fileDeleteFromTaskOptions != null) {
            returnClientRequestId = fileDeleteFromTaskOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (fileDeleteFromTaskOptions != null) {
            ocpDate = fileDeleteFromTaskOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.deleteFromTask(jobId, taskId, fileName, recursive, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(deleteFromTaskDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<Void, FileDeleteFromTaskHeadersInner> deleteFromTaskDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, FileDeleteFromTaskHeadersInner.class);
    }

    /**
     * Returns the content of the specified task file.
     *
     * @param jobId The id of the job that contains the task.
     * @param taskId The id of the task whose file you want to retrieve.
     * @param fileName The path to the task file that you want to get the content of.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the InputStream object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<InputStream, FileGetFromTaskHeadersInner> getFromTask(String jobId, String taskId, String fileName) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (taskId == null) {
            throw new IllegalArgumentException("Parameter taskId is required and cannot be null.");
        }
        if (fileName == null) {
            throw new IllegalArgumentException("Parameter fileName is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final FileGetFromTaskOptionsInner fileGetFromTaskOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        String ocpRange = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        Call<ResponseBody> call = service.getFromTask(jobId, taskId, fileName, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ocpRange, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        return getFromTaskDelegate(call.execute());
    }

    /**
     * Returns the content of the specified task file.
     *
     * @param jobId The id of the job that contains the task.
     * @param taskId The id of the task whose file you want to retrieve.
     * @param fileName The path to the task file that you want to get the content of.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getFromTaskAsync(String jobId, String taskId, String fileName, final ServiceCallback<InputStream> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (jobId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter jobId is required and cannot be null."));
            return null;
        }
        if (taskId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter taskId is required and cannot be null."));
            return null;
        }
        if (fileName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter fileName is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final FileGetFromTaskOptionsInner fileGetFromTaskOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        String ocpRange = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        Call<ResponseBody> call = service.getFromTask(jobId, taskId, fileName, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ocpRange, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<InputStream>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getFromTaskDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Returns the content of the specified task file.
     *
     * @param jobId The id of the job that contains the task.
     * @param taskId The id of the task whose file you want to retrieve.
     * @param fileName The path to the task file that you want to get the content of.
     * @param fileGetFromTaskOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the InputStream object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<InputStream, FileGetFromTaskHeadersInner> getFromTask(String jobId, String taskId, String fileName, FileGetFromTaskOptionsInner fileGetFromTaskOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (taskId == null) {
            throw new IllegalArgumentException("Parameter taskId is required and cannot be null.");
        }
        if (fileName == null) {
            throw new IllegalArgumentException("Parameter fileName is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(fileGetFromTaskOptions);
        Integer timeout = null;
        if (fileGetFromTaskOptions != null) {
            timeout = fileGetFromTaskOptions.timeout();
        }
        String clientRequestId = null;
        if (fileGetFromTaskOptions != null) {
            clientRequestId = fileGetFromTaskOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (fileGetFromTaskOptions != null) {
            returnClientRequestId = fileGetFromTaskOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (fileGetFromTaskOptions != null) {
            ocpDate = fileGetFromTaskOptions.ocpDate();
        }
        String ocpRange = null;
        if (fileGetFromTaskOptions != null) {
            ocpRange = fileGetFromTaskOptions.ocpRange();
        }
        DateTime ifModifiedSince = null;
        if (fileGetFromTaskOptions != null) {
            ifModifiedSince = fileGetFromTaskOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (fileGetFromTaskOptions != null) {
            ifUnmodifiedSince = fileGetFromTaskOptions.ifUnmodifiedSince();
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
        Call<ResponseBody> call = service.getFromTask(jobId, taskId, fileName, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ocpRange, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        return getFromTaskDelegate(call.execute());
    }

    /**
     * Returns the content of the specified task file.
     *
     * @param jobId The id of the job that contains the task.
     * @param taskId The id of the task whose file you want to retrieve.
     * @param fileName The path to the task file that you want to get the content of.
     * @param fileGetFromTaskOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getFromTaskAsync(String jobId, String taskId, String fileName, FileGetFromTaskOptionsInner fileGetFromTaskOptions, final ServiceCallback<InputStream> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (jobId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter jobId is required and cannot be null."));
            return null;
        }
        if (taskId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter taskId is required and cannot be null."));
            return null;
        }
        if (fileName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter fileName is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(fileGetFromTaskOptions, serviceCallback);
        Integer timeout = null;
        if (fileGetFromTaskOptions != null) {
            timeout = fileGetFromTaskOptions.timeout();
        }
        String clientRequestId = null;
        if (fileGetFromTaskOptions != null) {
            clientRequestId = fileGetFromTaskOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (fileGetFromTaskOptions != null) {
            returnClientRequestId = fileGetFromTaskOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (fileGetFromTaskOptions != null) {
            ocpDate = fileGetFromTaskOptions.ocpDate();
        }
        String ocpRange = null;
        if (fileGetFromTaskOptions != null) {
            ocpRange = fileGetFromTaskOptions.ocpRange();
        }
        DateTime ifModifiedSince = null;
        if (fileGetFromTaskOptions != null) {
            ifModifiedSince = fileGetFromTaskOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (fileGetFromTaskOptions != null) {
            ifUnmodifiedSince = fileGetFromTaskOptions.ifUnmodifiedSince();
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
        Call<ResponseBody> call = service.getFromTask(jobId, taskId, fileName, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ocpRange, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<InputStream>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getFromTaskDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<InputStream, FileGetFromTaskHeadersInner> getFromTaskDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<InputStream, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<InputStream>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, FileGetFromTaskHeadersInner.class);
    }

    /**
     * Gets the properties of the specified task file.
     *
     * @param jobId The id of the job that contains the task.
     * @param taskId The id of the task whose file you want to get the properties of.
     * @param fileName The path to the task file that you want to get the properties of.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, FileGetNodeFilePropertiesFromTaskHeadersInner> getNodeFilePropertiesFromTask(String jobId, String taskId, String fileName) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (taskId == null) {
            throw new IllegalArgumentException("Parameter taskId is required and cannot be null.");
        }
        if (fileName == null) {
            throw new IllegalArgumentException("Parameter fileName is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final FileGetNodeFilePropertiesFromTaskOptionsInner fileGetNodeFilePropertiesFromTaskOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        Call<Void> call = service.getNodeFilePropertiesFromTask(jobId, taskId, fileName, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        return getNodeFilePropertiesFromTaskDelegate(call.execute());
    }

    /**
     * Gets the properties of the specified task file.
     *
     * @param jobId The id of the job that contains the task.
     * @param taskId The id of the task whose file you want to get the properties of.
     * @param fileName The path to the task file that you want to get the properties of.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getNodeFilePropertiesFromTaskAsync(String jobId, String taskId, String fileName, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (jobId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter jobId is required and cannot be null."));
            return null;
        }
        if (taskId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter taskId is required and cannot be null."));
            return null;
        }
        if (fileName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter fileName is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final FileGetNodeFilePropertiesFromTaskOptionsInner fileGetNodeFilePropertiesFromTaskOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        Call<Void> call = service.getNodeFilePropertiesFromTask(jobId, taskId, fileName, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseEmptyCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                try {
                    serviceCallback.success(getNodeFilePropertiesFromTaskDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Gets the properties of the specified task file.
     *
     * @param jobId The id of the job that contains the task.
     * @param taskId The id of the task whose file you want to get the properties of.
     * @param fileName The path to the task file that you want to get the properties of.
     * @param fileGetNodeFilePropertiesFromTaskOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, FileGetNodeFilePropertiesFromTaskHeadersInner> getNodeFilePropertiesFromTask(String jobId, String taskId, String fileName, FileGetNodeFilePropertiesFromTaskOptionsInner fileGetNodeFilePropertiesFromTaskOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (taskId == null) {
            throw new IllegalArgumentException("Parameter taskId is required and cannot be null.");
        }
        if (fileName == null) {
            throw new IllegalArgumentException("Parameter fileName is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(fileGetNodeFilePropertiesFromTaskOptions);
        Integer timeout = null;
        if (fileGetNodeFilePropertiesFromTaskOptions != null) {
            timeout = fileGetNodeFilePropertiesFromTaskOptions.timeout();
        }
        String clientRequestId = null;
        if (fileGetNodeFilePropertiesFromTaskOptions != null) {
            clientRequestId = fileGetNodeFilePropertiesFromTaskOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (fileGetNodeFilePropertiesFromTaskOptions != null) {
            returnClientRequestId = fileGetNodeFilePropertiesFromTaskOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (fileGetNodeFilePropertiesFromTaskOptions != null) {
            ocpDate = fileGetNodeFilePropertiesFromTaskOptions.ocpDate();
        }
        DateTime ifModifiedSince = null;
        if (fileGetNodeFilePropertiesFromTaskOptions != null) {
            ifModifiedSince = fileGetNodeFilePropertiesFromTaskOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (fileGetNodeFilePropertiesFromTaskOptions != null) {
            ifUnmodifiedSince = fileGetNodeFilePropertiesFromTaskOptions.ifUnmodifiedSince();
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
        Call<Void> call = service.getNodeFilePropertiesFromTask(jobId, taskId, fileName, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        return getNodeFilePropertiesFromTaskDelegate(call.execute());
    }

    /**
     * Gets the properties of the specified task file.
     *
     * @param jobId The id of the job that contains the task.
     * @param taskId The id of the task whose file you want to get the properties of.
     * @param fileName The path to the task file that you want to get the properties of.
     * @param fileGetNodeFilePropertiesFromTaskOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getNodeFilePropertiesFromTaskAsync(String jobId, String taskId, String fileName, FileGetNodeFilePropertiesFromTaskOptionsInner fileGetNodeFilePropertiesFromTaskOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (jobId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter jobId is required and cannot be null."));
            return null;
        }
        if (taskId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter taskId is required and cannot be null."));
            return null;
        }
        if (fileName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter fileName is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(fileGetNodeFilePropertiesFromTaskOptions, serviceCallback);
        Integer timeout = null;
        if (fileGetNodeFilePropertiesFromTaskOptions != null) {
            timeout = fileGetNodeFilePropertiesFromTaskOptions.timeout();
        }
        String clientRequestId = null;
        if (fileGetNodeFilePropertiesFromTaskOptions != null) {
            clientRequestId = fileGetNodeFilePropertiesFromTaskOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (fileGetNodeFilePropertiesFromTaskOptions != null) {
            returnClientRequestId = fileGetNodeFilePropertiesFromTaskOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (fileGetNodeFilePropertiesFromTaskOptions != null) {
            ocpDate = fileGetNodeFilePropertiesFromTaskOptions.ocpDate();
        }
        DateTime ifModifiedSince = null;
        if (fileGetNodeFilePropertiesFromTaskOptions != null) {
            ifModifiedSince = fileGetNodeFilePropertiesFromTaskOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (fileGetNodeFilePropertiesFromTaskOptions != null) {
            ifUnmodifiedSince = fileGetNodeFilePropertiesFromTaskOptions.ifUnmodifiedSince();
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
        Call<Void> call = service.getNodeFilePropertiesFromTask(jobId, taskId, fileName, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseEmptyCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                try {
                    serviceCallback.success(getNodeFilePropertiesFromTaskDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<Void, FileGetNodeFilePropertiesFromTaskHeadersInner> getNodeFilePropertiesFromTaskDelegate(Response<Void> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildEmptyWithHeaders(response, FileGetNodeFilePropertiesFromTaskHeadersInner.class);
    }

    /**
     * Deletes the specified task file from the compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node from which you want to delete the file.
     * @param fileName The path to the file that you want to delete.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, FileDeleteFromComputeNodeHeadersInner> deleteFromComputeNode(String poolId, String nodeId, String fileName) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (nodeId == null) {
            throw new IllegalArgumentException("Parameter nodeId is required and cannot be null.");
        }
        if (fileName == null) {
            throw new IllegalArgumentException("Parameter fileName is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final Boolean recursive = null;
        final FileDeleteFromComputeNodeOptionsInner fileDeleteFromComputeNodeOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.deleteFromComputeNode(poolId, nodeId, fileName, recursive, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        return deleteFromComputeNodeDelegate(call.execute());
    }

    /**
     * Deletes the specified task file from the compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node from which you want to delete the file.
     * @param fileName The path to the file that you want to delete.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall deleteFromComputeNodeAsync(String poolId, String nodeId, String fileName, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (poolId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter poolId is required and cannot be null."));
            return null;
        }
        if (nodeId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nodeId is required and cannot be null."));
            return null;
        }
        if (fileName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter fileName is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final Boolean recursive = null;
        final FileDeleteFromComputeNodeOptionsInner fileDeleteFromComputeNodeOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.deleteFromComputeNode(poolId, nodeId, fileName, recursive, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(deleteFromComputeNodeDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Deletes the specified task file from the compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node from which you want to delete the file.
     * @param fileName The path to the file that you want to delete.
     * @param recursive Sets whether to delete children of a directory. If the fileName parameter represents a directory instead of a file, you can set Recursive to true to delete the directory and all of the files and subdirectories in it. If Recursive is false then the directory must be empty or deletion will fail.
     * @param fileDeleteFromComputeNodeOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, FileDeleteFromComputeNodeHeadersInner> deleteFromComputeNode(String poolId, String nodeId, String fileName, Boolean recursive, FileDeleteFromComputeNodeOptionsInner fileDeleteFromComputeNodeOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (nodeId == null) {
            throw new IllegalArgumentException("Parameter nodeId is required and cannot be null.");
        }
        if (fileName == null) {
            throw new IllegalArgumentException("Parameter fileName is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(fileDeleteFromComputeNodeOptions);
        Integer timeout = null;
        if (fileDeleteFromComputeNodeOptions != null) {
            timeout = fileDeleteFromComputeNodeOptions.timeout();
        }
        String clientRequestId = null;
        if (fileDeleteFromComputeNodeOptions != null) {
            clientRequestId = fileDeleteFromComputeNodeOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (fileDeleteFromComputeNodeOptions != null) {
            returnClientRequestId = fileDeleteFromComputeNodeOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (fileDeleteFromComputeNodeOptions != null) {
            ocpDate = fileDeleteFromComputeNodeOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.deleteFromComputeNode(poolId, nodeId, fileName, recursive, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        return deleteFromComputeNodeDelegate(call.execute());
    }

    /**
     * Deletes the specified task file from the compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node from which you want to delete the file.
     * @param fileName The path to the file that you want to delete.
     * @param recursive Sets whether to delete children of a directory. If the fileName parameter represents a directory instead of a file, you can set Recursive to true to delete the directory and all of the files and subdirectories in it. If Recursive is false then the directory must be empty or deletion will fail.
     * @param fileDeleteFromComputeNodeOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall deleteFromComputeNodeAsync(String poolId, String nodeId, String fileName, Boolean recursive, FileDeleteFromComputeNodeOptionsInner fileDeleteFromComputeNodeOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (poolId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter poolId is required and cannot be null."));
            return null;
        }
        if (nodeId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nodeId is required and cannot be null."));
            return null;
        }
        if (fileName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter fileName is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(fileDeleteFromComputeNodeOptions, serviceCallback);
        Integer timeout = null;
        if (fileDeleteFromComputeNodeOptions != null) {
            timeout = fileDeleteFromComputeNodeOptions.timeout();
        }
        String clientRequestId = null;
        if (fileDeleteFromComputeNodeOptions != null) {
            clientRequestId = fileDeleteFromComputeNodeOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (fileDeleteFromComputeNodeOptions != null) {
            returnClientRequestId = fileDeleteFromComputeNodeOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (fileDeleteFromComputeNodeOptions != null) {
            ocpDate = fileDeleteFromComputeNodeOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.deleteFromComputeNode(poolId, nodeId, fileName, recursive, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(deleteFromComputeNodeDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<Void, FileDeleteFromComputeNodeHeadersInner> deleteFromComputeNodeDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, FileDeleteFromComputeNodeHeadersInner.class);
    }

    /**
     * Returns the content of the specified task file.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that contains the file.
     * @param fileName The path to the task file that you want to get the content of.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the InputStream object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<InputStream, FileGetFromComputeNodeHeadersInner> getFromComputeNode(String poolId, String nodeId, String fileName) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (nodeId == null) {
            throw new IllegalArgumentException("Parameter nodeId is required and cannot be null.");
        }
        if (fileName == null) {
            throw new IllegalArgumentException("Parameter fileName is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final FileGetFromComputeNodeOptionsInner fileGetFromComputeNodeOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        String ocpRange = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        Call<ResponseBody> call = service.getFromComputeNode(poolId, nodeId, fileName, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ocpRange, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        return getFromComputeNodeDelegate(call.execute());
    }

    /**
     * Returns the content of the specified task file.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that contains the file.
     * @param fileName The path to the task file that you want to get the content of.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getFromComputeNodeAsync(String poolId, String nodeId, String fileName, final ServiceCallback<InputStream> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (poolId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter poolId is required and cannot be null."));
            return null;
        }
        if (nodeId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nodeId is required and cannot be null."));
            return null;
        }
        if (fileName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter fileName is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final FileGetFromComputeNodeOptionsInner fileGetFromComputeNodeOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        String ocpRange = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        Call<ResponseBody> call = service.getFromComputeNode(poolId, nodeId, fileName, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ocpRange, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<InputStream>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getFromComputeNodeDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Returns the content of the specified task file.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that contains the file.
     * @param fileName The path to the task file that you want to get the content of.
     * @param fileGetFromComputeNodeOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the InputStream object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<InputStream, FileGetFromComputeNodeHeadersInner> getFromComputeNode(String poolId, String nodeId, String fileName, FileGetFromComputeNodeOptionsInner fileGetFromComputeNodeOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (nodeId == null) {
            throw new IllegalArgumentException("Parameter nodeId is required and cannot be null.");
        }
        if (fileName == null) {
            throw new IllegalArgumentException("Parameter fileName is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(fileGetFromComputeNodeOptions);
        Integer timeout = null;
        if (fileGetFromComputeNodeOptions != null) {
            timeout = fileGetFromComputeNodeOptions.timeout();
        }
        String clientRequestId = null;
        if (fileGetFromComputeNodeOptions != null) {
            clientRequestId = fileGetFromComputeNodeOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (fileGetFromComputeNodeOptions != null) {
            returnClientRequestId = fileGetFromComputeNodeOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (fileGetFromComputeNodeOptions != null) {
            ocpDate = fileGetFromComputeNodeOptions.ocpDate();
        }
        String ocpRange = null;
        if (fileGetFromComputeNodeOptions != null) {
            ocpRange = fileGetFromComputeNodeOptions.ocpRange();
        }
        DateTime ifModifiedSince = null;
        if (fileGetFromComputeNodeOptions != null) {
            ifModifiedSince = fileGetFromComputeNodeOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (fileGetFromComputeNodeOptions != null) {
            ifUnmodifiedSince = fileGetFromComputeNodeOptions.ifUnmodifiedSince();
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
        Call<ResponseBody> call = service.getFromComputeNode(poolId, nodeId, fileName, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ocpRange, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        return getFromComputeNodeDelegate(call.execute());
    }

    /**
     * Returns the content of the specified task file.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that contains the file.
     * @param fileName The path to the task file that you want to get the content of.
     * @param fileGetFromComputeNodeOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getFromComputeNodeAsync(String poolId, String nodeId, String fileName, FileGetFromComputeNodeOptionsInner fileGetFromComputeNodeOptions, final ServiceCallback<InputStream> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (poolId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter poolId is required and cannot be null."));
            return null;
        }
        if (nodeId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nodeId is required and cannot be null."));
            return null;
        }
        if (fileName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter fileName is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(fileGetFromComputeNodeOptions, serviceCallback);
        Integer timeout = null;
        if (fileGetFromComputeNodeOptions != null) {
            timeout = fileGetFromComputeNodeOptions.timeout();
        }
        String clientRequestId = null;
        if (fileGetFromComputeNodeOptions != null) {
            clientRequestId = fileGetFromComputeNodeOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (fileGetFromComputeNodeOptions != null) {
            returnClientRequestId = fileGetFromComputeNodeOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (fileGetFromComputeNodeOptions != null) {
            ocpDate = fileGetFromComputeNodeOptions.ocpDate();
        }
        String ocpRange = null;
        if (fileGetFromComputeNodeOptions != null) {
            ocpRange = fileGetFromComputeNodeOptions.ocpRange();
        }
        DateTime ifModifiedSince = null;
        if (fileGetFromComputeNodeOptions != null) {
            ifModifiedSince = fileGetFromComputeNodeOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (fileGetFromComputeNodeOptions != null) {
            ifUnmodifiedSince = fileGetFromComputeNodeOptions.ifUnmodifiedSince();
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
        Call<ResponseBody> call = service.getFromComputeNode(poolId, nodeId, fileName, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ocpRange, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<InputStream>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getFromComputeNodeDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<InputStream, FileGetFromComputeNodeHeadersInner> getFromComputeNodeDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<InputStream, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<InputStream>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, FileGetFromComputeNodeHeadersInner.class);
    }

    /**
     * Gets the properties of the specified compute node file.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that contains the file.
     * @param fileName The path to the compute node file that you want to get the properties of.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, FileGetNodeFilePropertiesFromComputeNodeHeadersInner> getNodeFilePropertiesFromComputeNode(String poolId, String nodeId, String fileName) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (nodeId == null) {
            throw new IllegalArgumentException("Parameter nodeId is required and cannot be null.");
        }
        if (fileName == null) {
            throw new IllegalArgumentException("Parameter fileName is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final FileGetNodeFilePropertiesFromComputeNodeOptionsInner fileGetNodeFilePropertiesFromComputeNodeOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        Call<Void> call = service.getNodeFilePropertiesFromComputeNode(poolId, nodeId, fileName, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        return getNodeFilePropertiesFromComputeNodeDelegate(call.execute());
    }

    /**
     * Gets the properties of the specified compute node file.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that contains the file.
     * @param fileName The path to the compute node file that you want to get the properties of.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getNodeFilePropertiesFromComputeNodeAsync(String poolId, String nodeId, String fileName, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (poolId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter poolId is required and cannot be null."));
            return null;
        }
        if (nodeId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nodeId is required and cannot be null."));
            return null;
        }
        if (fileName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter fileName is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final FileGetNodeFilePropertiesFromComputeNodeOptionsInner fileGetNodeFilePropertiesFromComputeNodeOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        Call<Void> call = service.getNodeFilePropertiesFromComputeNode(poolId, nodeId, fileName, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseEmptyCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                try {
                    serviceCallback.success(getNodeFilePropertiesFromComputeNodeDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Gets the properties of the specified compute node file.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that contains the file.
     * @param fileName The path to the compute node file that you want to get the properties of.
     * @param fileGetNodeFilePropertiesFromComputeNodeOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, FileGetNodeFilePropertiesFromComputeNodeHeadersInner> getNodeFilePropertiesFromComputeNode(String poolId, String nodeId, String fileName, FileGetNodeFilePropertiesFromComputeNodeOptionsInner fileGetNodeFilePropertiesFromComputeNodeOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (nodeId == null) {
            throw new IllegalArgumentException("Parameter nodeId is required and cannot be null.");
        }
        if (fileName == null) {
            throw new IllegalArgumentException("Parameter fileName is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(fileGetNodeFilePropertiesFromComputeNodeOptions);
        Integer timeout = null;
        if (fileGetNodeFilePropertiesFromComputeNodeOptions != null) {
            timeout = fileGetNodeFilePropertiesFromComputeNodeOptions.timeout();
        }
        String clientRequestId = null;
        if (fileGetNodeFilePropertiesFromComputeNodeOptions != null) {
            clientRequestId = fileGetNodeFilePropertiesFromComputeNodeOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (fileGetNodeFilePropertiesFromComputeNodeOptions != null) {
            returnClientRequestId = fileGetNodeFilePropertiesFromComputeNodeOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (fileGetNodeFilePropertiesFromComputeNodeOptions != null) {
            ocpDate = fileGetNodeFilePropertiesFromComputeNodeOptions.ocpDate();
        }
        DateTime ifModifiedSince = null;
        if (fileGetNodeFilePropertiesFromComputeNodeOptions != null) {
            ifModifiedSince = fileGetNodeFilePropertiesFromComputeNodeOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (fileGetNodeFilePropertiesFromComputeNodeOptions != null) {
            ifUnmodifiedSince = fileGetNodeFilePropertiesFromComputeNodeOptions.ifUnmodifiedSince();
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
        Call<Void> call = service.getNodeFilePropertiesFromComputeNode(poolId, nodeId, fileName, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        return getNodeFilePropertiesFromComputeNodeDelegate(call.execute());
    }

    /**
     * Gets the properties of the specified compute node file.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that contains the file.
     * @param fileName The path to the compute node file that you want to get the properties of.
     * @param fileGetNodeFilePropertiesFromComputeNodeOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getNodeFilePropertiesFromComputeNodeAsync(String poolId, String nodeId, String fileName, FileGetNodeFilePropertiesFromComputeNodeOptionsInner fileGetNodeFilePropertiesFromComputeNodeOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (poolId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter poolId is required and cannot be null."));
            return null;
        }
        if (nodeId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nodeId is required and cannot be null."));
            return null;
        }
        if (fileName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter fileName is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(fileGetNodeFilePropertiesFromComputeNodeOptions, serviceCallback);
        Integer timeout = null;
        if (fileGetNodeFilePropertiesFromComputeNodeOptions != null) {
            timeout = fileGetNodeFilePropertiesFromComputeNodeOptions.timeout();
        }
        String clientRequestId = null;
        if (fileGetNodeFilePropertiesFromComputeNodeOptions != null) {
            clientRequestId = fileGetNodeFilePropertiesFromComputeNodeOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (fileGetNodeFilePropertiesFromComputeNodeOptions != null) {
            returnClientRequestId = fileGetNodeFilePropertiesFromComputeNodeOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (fileGetNodeFilePropertiesFromComputeNodeOptions != null) {
            ocpDate = fileGetNodeFilePropertiesFromComputeNodeOptions.ocpDate();
        }
        DateTime ifModifiedSince = null;
        if (fileGetNodeFilePropertiesFromComputeNodeOptions != null) {
            ifModifiedSince = fileGetNodeFilePropertiesFromComputeNodeOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (fileGetNodeFilePropertiesFromComputeNodeOptions != null) {
            ifUnmodifiedSince = fileGetNodeFilePropertiesFromComputeNodeOptions.ifUnmodifiedSince();
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
        Call<Void> call = service.getNodeFilePropertiesFromComputeNode(poolId, nodeId, fileName, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseEmptyCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                try {
                    serviceCallback.success(getNodeFilePropertiesFromComputeNodeDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<Void, FileGetNodeFilePropertiesFromComputeNodeHeadersInner> getNodeFilePropertiesFromComputeNodeDelegate(Response<Void> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildEmptyWithHeaders(response, FileGetNodeFilePropertiesFromComputeNodeHeadersInner.class);
    }

    /**
     * Lists the files in a task's directory on its compute node.
     *
     * @param jobId The id of the job that contains the task.
     * @param taskId The id of the task whose files you want to list.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;NodeFileInner&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PagedList<NodeFileInner>, FileListFromTaskHeadersInner> listFromTask(final String jobId, final String taskId) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (taskId == null) {
            throw new IllegalArgumentException("Parameter taskId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final Boolean recursive = null;
        final FileListFromTaskOptionsInner fileListFromTaskOptions = null;
        String filter = null;
        Integer maxResults = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.listFromTask(jobId, taskId, recursive, this.client.apiVersion(), this.client.acceptLanguage(), filter, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        ServiceResponseWithHeaders<PageImpl<NodeFileInner>, FileListFromTaskHeadersInner> response = listFromTaskDelegate(call.execute());
        PagedList<NodeFileInner> result = new PagedList<NodeFileInner>(response.getBody()) {
            @Override
            public Page<NodeFileInner> nextPage(String nextPageLink) throws BatchErrorException, IOException {
                return listFromTaskNext(nextPageLink, null).getBody();
            }
        };
        return new ServiceResponseWithHeaders<>(result, response.getHeaders(), response.getResponse());
    }

    /**
     * Lists the files in a task's directory on its compute node.
     *
     * @param jobId The id of the job that contains the task.
     * @param taskId The id of the task whose files you want to list.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listFromTaskAsync(final String jobId, final String taskId, final ListOperationCallback<NodeFileInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (jobId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter jobId is required and cannot be null."));
            return null;
        }
        if (taskId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter taskId is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final Boolean recursive = null;
        final FileListFromTaskOptionsInner fileListFromTaskOptions = null;
        String filter = null;
        Integer maxResults = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.listFromTask(jobId, taskId, recursive, this.client.apiVersion(), this.client.acceptLanguage(), filter, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<NodeFileInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<NodeFileInner>, FileListFromTaskHeadersInner> result = listFromTaskDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listFromTaskNextAsync(result.getBody().getNextPageLink(), null, serviceCall, serviceCallback);
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
     * Lists the files in a task's directory on its compute node.
     *
     * @param jobId The id of the job that contains the task.
     * @param taskId The id of the task whose files you want to list.
     * @param recursive Sets whether to list children of a directory.
     * @param fileListFromTaskOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;NodeFileInner&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PagedList<NodeFileInner>, FileListFromTaskHeadersInner> listFromTask(final String jobId, final String taskId, final Boolean recursive, final FileListFromTaskOptionsInner fileListFromTaskOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (taskId == null) {
            throw new IllegalArgumentException("Parameter taskId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(fileListFromTaskOptions);
        String filter = null;
        if (fileListFromTaskOptions != null) {
            filter = fileListFromTaskOptions.filter();
        }
        Integer maxResults = null;
        if (fileListFromTaskOptions != null) {
            maxResults = fileListFromTaskOptions.maxResults();
        }
        Integer timeout = null;
        if (fileListFromTaskOptions != null) {
            timeout = fileListFromTaskOptions.timeout();
        }
        String clientRequestId = null;
        if (fileListFromTaskOptions != null) {
            clientRequestId = fileListFromTaskOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (fileListFromTaskOptions != null) {
            returnClientRequestId = fileListFromTaskOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (fileListFromTaskOptions != null) {
            ocpDate = fileListFromTaskOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.listFromTask(jobId, taskId, recursive, this.client.apiVersion(), this.client.acceptLanguage(), filter, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        ServiceResponseWithHeaders<PageImpl<NodeFileInner>, FileListFromTaskHeadersInner> response = listFromTaskDelegate(call.execute());
        PagedList<NodeFileInner> result = new PagedList<NodeFileInner>(response.getBody()) {
            @Override
            public Page<NodeFileInner> nextPage(String nextPageLink) throws BatchErrorException, IOException {
                FileListFromTaskNextOptionsInner fileListFromTaskNextOptions = null;
                if (fileListFromTaskOptions != null) {
                    fileListFromTaskNextOptions = new FileListFromTaskNextOptionsInner();
                    fileListFromTaskNextOptions.setClientRequestId(fileListFromTaskOptions.clientRequestId());
                    fileListFromTaskNextOptions.setReturnClientRequestId(fileListFromTaskOptions.returnClientRequestId());
                    fileListFromTaskNextOptions.setOcpDate(fileListFromTaskOptions.ocpDate());
                }
                return listFromTaskNext(nextPageLink, fileListFromTaskNextOptions).getBody();
            }
        };
        return new ServiceResponseWithHeaders<>(result, response.getHeaders(), response.getResponse());
    }

    /**
     * Lists the files in a task's directory on its compute node.
     *
     * @param jobId The id of the job that contains the task.
     * @param taskId The id of the task whose files you want to list.
     * @param recursive Sets whether to list children of a directory.
     * @param fileListFromTaskOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listFromTaskAsync(final String jobId, final String taskId, final Boolean recursive, final FileListFromTaskOptionsInner fileListFromTaskOptions, final ListOperationCallback<NodeFileInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (jobId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter jobId is required and cannot be null."));
            return null;
        }
        if (taskId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter taskId is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(fileListFromTaskOptions, serviceCallback);
        String filter = null;
        if (fileListFromTaskOptions != null) {
            filter = fileListFromTaskOptions.filter();
        }
        Integer maxResults = null;
        if (fileListFromTaskOptions != null) {
            maxResults = fileListFromTaskOptions.maxResults();
        }
        Integer timeout = null;
        if (fileListFromTaskOptions != null) {
            timeout = fileListFromTaskOptions.timeout();
        }
        String clientRequestId = null;
        if (fileListFromTaskOptions != null) {
            clientRequestId = fileListFromTaskOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (fileListFromTaskOptions != null) {
            returnClientRequestId = fileListFromTaskOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (fileListFromTaskOptions != null) {
            ocpDate = fileListFromTaskOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.listFromTask(jobId, taskId, recursive, this.client.apiVersion(), this.client.acceptLanguage(), filter, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<NodeFileInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<NodeFileInner>, FileListFromTaskHeadersInner> result = listFromTaskDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        FileListFromTaskNextOptionsInner fileListFromTaskNextOptions = null;
                        if (fileListFromTaskOptions != null) {
                            fileListFromTaskNextOptions = new FileListFromTaskNextOptionsInner();
                            fileListFromTaskNextOptions.setClientRequestId(fileListFromTaskOptions.clientRequestId());
                            fileListFromTaskNextOptions.setReturnClientRequestId(fileListFromTaskOptions.returnClientRequestId());
                            fileListFromTaskNextOptions.setOcpDate(fileListFromTaskOptions.ocpDate());
                        }
                        listFromTaskNextAsync(result.getBody().getNextPageLink(), fileListFromTaskNextOptions, serviceCall, serviceCallback);
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

    private ServiceResponseWithHeaders<PageImpl<NodeFileInner>, FileListFromTaskHeadersInner> listFromTaskDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<NodeFileInner>, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<NodeFileInner>>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, FileListFromTaskHeadersInner.class);
    }

    /**
     * Lists all of the files in task directories on the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node whose files you want to list.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;NodeFileInner&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PagedList<NodeFileInner>, FileListFromComputeNodeHeadersInner> listFromComputeNode(final String poolId, final String nodeId) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (nodeId == null) {
            throw new IllegalArgumentException("Parameter nodeId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final Boolean recursive = null;
        final FileListFromComputeNodeOptionsInner fileListFromComputeNodeOptions = null;
        String filter = null;
        Integer maxResults = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.listFromComputeNode(poolId, nodeId, recursive, this.client.apiVersion(), this.client.acceptLanguage(), filter, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        ServiceResponseWithHeaders<PageImpl<NodeFileInner>, FileListFromComputeNodeHeadersInner> response = listFromComputeNodeDelegate(call.execute());
        PagedList<NodeFileInner> result = new PagedList<NodeFileInner>(response.getBody()) {
            @Override
            public Page<NodeFileInner> nextPage(String nextPageLink) throws BatchErrorException, IOException {
                return listFromComputeNodeNext(nextPageLink, null).getBody();
            }
        };
        return new ServiceResponseWithHeaders<>(result, response.getHeaders(), response.getResponse());
    }

    /**
     * Lists all of the files in task directories on the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node whose files you want to list.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listFromComputeNodeAsync(final String poolId, final String nodeId, final ListOperationCallback<NodeFileInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (poolId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter poolId is required and cannot be null."));
            return null;
        }
        if (nodeId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nodeId is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final Boolean recursive = null;
        final FileListFromComputeNodeOptionsInner fileListFromComputeNodeOptions = null;
        String filter = null;
        Integer maxResults = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.listFromComputeNode(poolId, nodeId, recursive, this.client.apiVersion(), this.client.acceptLanguage(), filter, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<NodeFileInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<NodeFileInner>, FileListFromComputeNodeHeadersInner> result = listFromComputeNodeDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listFromComputeNodeNextAsync(result.getBody().getNextPageLink(), null, serviceCall, serviceCallback);
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
     * Lists all of the files in task directories on the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node whose files you want to list.
     * @param recursive Sets whether to list children of a directory.
     * @param fileListFromComputeNodeOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;NodeFileInner&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PagedList<NodeFileInner>, FileListFromComputeNodeHeadersInner> listFromComputeNode(final String poolId, final String nodeId, final Boolean recursive, final FileListFromComputeNodeOptionsInner fileListFromComputeNodeOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (nodeId == null) {
            throw new IllegalArgumentException("Parameter nodeId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(fileListFromComputeNodeOptions);
        String filter = null;
        if (fileListFromComputeNodeOptions != null) {
            filter = fileListFromComputeNodeOptions.filter();
        }
        Integer maxResults = null;
        if (fileListFromComputeNodeOptions != null) {
            maxResults = fileListFromComputeNodeOptions.maxResults();
        }
        Integer timeout = null;
        if (fileListFromComputeNodeOptions != null) {
            timeout = fileListFromComputeNodeOptions.timeout();
        }
        String clientRequestId = null;
        if (fileListFromComputeNodeOptions != null) {
            clientRequestId = fileListFromComputeNodeOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (fileListFromComputeNodeOptions != null) {
            returnClientRequestId = fileListFromComputeNodeOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (fileListFromComputeNodeOptions != null) {
            ocpDate = fileListFromComputeNodeOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.listFromComputeNode(poolId, nodeId, recursive, this.client.apiVersion(), this.client.acceptLanguage(), filter, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        ServiceResponseWithHeaders<PageImpl<NodeFileInner>, FileListFromComputeNodeHeadersInner> response = listFromComputeNodeDelegate(call.execute());
        PagedList<NodeFileInner> result = new PagedList<NodeFileInner>(response.getBody()) {
            @Override
            public Page<NodeFileInner> nextPage(String nextPageLink) throws BatchErrorException, IOException {
                FileListFromComputeNodeNextOptionsInner fileListFromComputeNodeNextOptions = null;
                if (fileListFromComputeNodeOptions != null) {
                    fileListFromComputeNodeNextOptions = new FileListFromComputeNodeNextOptionsInner();
                    fileListFromComputeNodeNextOptions.setClientRequestId(fileListFromComputeNodeOptions.clientRequestId());
                    fileListFromComputeNodeNextOptions.setReturnClientRequestId(fileListFromComputeNodeOptions.returnClientRequestId());
                    fileListFromComputeNodeNextOptions.setOcpDate(fileListFromComputeNodeOptions.ocpDate());
                }
                return listFromComputeNodeNext(nextPageLink, fileListFromComputeNodeNextOptions).getBody();
            }
        };
        return new ServiceResponseWithHeaders<>(result, response.getHeaders(), response.getResponse());
    }

    /**
     * Lists all of the files in task directories on the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node whose files you want to list.
     * @param recursive Sets whether to list children of a directory.
     * @param fileListFromComputeNodeOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listFromComputeNodeAsync(final String poolId, final String nodeId, final Boolean recursive, final FileListFromComputeNodeOptionsInner fileListFromComputeNodeOptions, final ListOperationCallback<NodeFileInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (poolId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter poolId is required and cannot be null."));
            return null;
        }
        if (nodeId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nodeId is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(fileListFromComputeNodeOptions, serviceCallback);
        String filter = null;
        if (fileListFromComputeNodeOptions != null) {
            filter = fileListFromComputeNodeOptions.filter();
        }
        Integer maxResults = null;
        if (fileListFromComputeNodeOptions != null) {
            maxResults = fileListFromComputeNodeOptions.maxResults();
        }
        Integer timeout = null;
        if (fileListFromComputeNodeOptions != null) {
            timeout = fileListFromComputeNodeOptions.timeout();
        }
        String clientRequestId = null;
        if (fileListFromComputeNodeOptions != null) {
            clientRequestId = fileListFromComputeNodeOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (fileListFromComputeNodeOptions != null) {
            returnClientRequestId = fileListFromComputeNodeOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (fileListFromComputeNodeOptions != null) {
            ocpDate = fileListFromComputeNodeOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.listFromComputeNode(poolId, nodeId, recursive, this.client.apiVersion(), this.client.acceptLanguage(), filter, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<NodeFileInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<NodeFileInner>, FileListFromComputeNodeHeadersInner> result = listFromComputeNodeDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        FileListFromComputeNodeNextOptionsInner fileListFromComputeNodeNextOptions = null;
                        if (fileListFromComputeNodeOptions != null) {
                            fileListFromComputeNodeNextOptions = new FileListFromComputeNodeNextOptionsInner();
                            fileListFromComputeNodeNextOptions.setClientRequestId(fileListFromComputeNodeOptions.clientRequestId());
                            fileListFromComputeNodeNextOptions.setReturnClientRequestId(fileListFromComputeNodeOptions.returnClientRequestId());
                            fileListFromComputeNodeNextOptions.setOcpDate(fileListFromComputeNodeOptions.ocpDate());
                        }
                        listFromComputeNodeNextAsync(result.getBody().getNextPageLink(), fileListFromComputeNodeNextOptions, serviceCall, serviceCallback);
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

    private ServiceResponseWithHeaders<PageImpl<NodeFileInner>, FileListFromComputeNodeHeadersInner> listFromComputeNodeDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<NodeFileInner>, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<NodeFileInner>>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, FileListFromComputeNodeHeadersInner.class);
    }

    /**
     * Lists the files in a task's directory on its compute node.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;NodeFileInner&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PageImpl<NodeFileInner>, FileListFromTaskHeadersInner> listFromTaskNext(final String nextPageLink) throws BatchErrorException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        final FileListFromTaskNextOptionsInner fileListFromTaskNextOptions = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.listFromTaskNext(nextPageLink, this.client.acceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted);
        return listFromTaskNextDelegate(call.execute());
    }

    /**
     * Lists the files in a task's directory on its compute node.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listFromTaskNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<NodeFileInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        final FileListFromTaskNextOptionsInner fileListFromTaskNextOptions = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.listFromTaskNext(nextPageLink, this.client.acceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted);
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<NodeFileInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<NodeFileInner>, FileListFromTaskHeadersInner> result = listFromTaskNextDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listFromTaskNextAsync(result.getBody().getNextPageLink(), null, serviceCall, serviceCallback);
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
     * Lists the files in a task's directory on its compute node.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param fileListFromTaskNextOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;NodeFileInner&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PageImpl<NodeFileInner>, FileListFromTaskHeadersInner> listFromTaskNext(final String nextPageLink, final FileListFromTaskNextOptionsInner fileListFromTaskNextOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        Validator.validate(fileListFromTaskNextOptions);
        String clientRequestId = null;
        if (fileListFromTaskNextOptions != null) {
            clientRequestId = fileListFromTaskNextOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (fileListFromTaskNextOptions != null) {
            returnClientRequestId = fileListFromTaskNextOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (fileListFromTaskNextOptions != null) {
            ocpDate = fileListFromTaskNextOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.listFromTaskNext(nextPageLink, this.client.acceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted);
        return listFromTaskNextDelegate(call.execute());
    }

    /**
     * Lists the files in a task's directory on its compute node.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param fileListFromTaskNextOptions Additional parameters for the operation
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listFromTaskNextAsync(final String nextPageLink, final FileListFromTaskNextOptionsInner fileListFromTaskNextOptions, final ServiceCall serviceCall, final ListOperationCallback<NodeFileInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        Validator.validate(fileListFromTaskNextOptions, serviceCallback);
        String clientRequestId = null;
        if (fileListFromTaskNextOptions != null) {
            clientRequestId = fileListFromTaskNextOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (fileListFromTaskNextOptions != null) {
            returnClientRequestId = fileListFromTaskNextOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (fileListFromTaskNextOptions != null) {
            ocpDate = fileListFromTaskNextOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.listFromTaskNext(nextPageLink, this.client.acceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted);
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<NodeFileInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<NodeFileInner>, FileListFromTaskHeadersInner> result = listFromTaskNextDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listFromTaskNextAsync(result.getBody().getNextPageLink(), fileListFromTaskNextOptions, serviceCall, serviceCallback);
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

    private ServiceResponseWithHeaders<PageImpl<NodeFileInner>, FileListFromTaskHeadersInner> listFromTaskNextDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<NodeFileInner>, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<NodeFileInner>>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, FileListFromTaskHeadersInner.class);
    }

    /**
     * Lists all of the files in task directories on the specified compute node.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;NodeFileInner&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PageImpl<NodeFileInner>, FileListFromComputeNodeHeadersInner> listFromComputeNodeNext(final String nextPageLink) throws BatchErrorException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        final FileListFromComputeNodeNextOptionsInner fileListFromComputeNodeNextOptions = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.listFromComputeNodeNext(nextPageLink, this.client.acceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted);
        return listFromComputeNodeNextDelegate(call.execute());
    }

    /**
     * Lists all of the files in task directories on the specified compute node.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listFromComputeNodeNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<NodeFileInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        final FileListFromComputeNodeNextOptionsInner fileListFromComputeNodeNextOptions = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.listFromComputeNodeNext(nextPageLink, this.client.acceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted);
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<NodeFileInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<NodeFileInner>, FileListFromComputeNodeHeadersInner> result = listFromComputeNodeNextDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listFromComputeNodeNextAsync(result.getBody().getNextPageLink(), null, serviceCall, serviceCallback);
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
     * Lists all of the files in task directories on the specified compute node.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param fileListFromComputeNodeNextOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;NodeFileInner&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PageImpl<NodeFileInner>, FileListFromComputeNodeHeadersInner> listFromComputeNodeNext(final String nextPageLink, final FileListFromComputeNodeNextOptionsInner fileListFromComputeNodeNextOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        Validator.validate(fileListFromComputeNodeNextOptions);
        String clientRequestId = null;
        if (fileListFromComputeNodeNextOptions != null) {
            clientRequestId = fileListFromComputeNodeNextOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (fileListFromComputeNodeNextOptions != null) {
            returnClientRequestId = fileListFromComputeNodeNextOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (fileListFromComputeNodeNextOptions != null) {
            ocpDate = fileListFromComputeNodeNextOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.listFromComputeNodeNext(nextPageLink, this.client.acceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted);
        return listFromComputeNodeNextDelegate(call.execute());
    }

    /**
     * Lists all of the files in task directories on the specified compute node.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param fileListFromComputeNodeNextOptions Additional parameters for the operation
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listFromComputeNodeNextAsync(final String nextPageLink, final FileListFromComputeNodeNextOptionsInner fileListFromComputeNodeNextOptions, final ServiceCall serviceCall, final ListOperationCallback<NodeFileInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        Validator.validate(fileListFromComputeNodeNextOptions, serviceCallback);
        String clientRequestId = null;
        if (fileListFromComputeNodeNextOptions != null) {
            clientRequestId = fileListFromComputeNodeNextOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (fileListFromComputeNodeNextOptions != null) {
            returnClientRequestId = fileListFromComputeNodeNextOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (fileListFromComputeNodeNextOptions != null) {
            ocpDate = fileListFromComputeNodeNextOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.listFromComputeNodeNext(nextPageLink, this.client.acceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted);
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<NodeFileInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<NodeFileInner>, FileListFromComputeNodeHeadersInner> result = listFromComputeNodeNextDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listFromComputeNodeNextAsync(result.getBody().getNextPageLink(), fileListFromComputeNodeNextOptions, serviceCall, serviceCallback);
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

    private ServiceResponseWithHeaders<PageImpl<NodeFileInner>, FileListFromComputeNodeHeadersInner> listFromComputeNodeNextDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<NodeFileInner>, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<NodeFileInner>>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, FileListFromComputeNodeHeadersInner.class);
    }

}
