/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol;

import com.google.common.reflect.TypeToken;
import com.microsoft.azure.AzureServiceResponseBuilder;
import com.microsoft.azure.batch.protocol.models.BatchErrorException;
import com.microsoft.azure.batch.protocol.models.CloudTask;
import com.microsoft.azure.batch.protocol.models.CloudTaskListSubtasksResult;
import com.microsoft.azure.batch.protocol.models.PageImpl;
import com.microsoft.azure.batch.protocol.models.TaskAddHeaders;
import com.microsoft.azure.batch.protocol.models.TaskAddOptions;
import com.microsoft.azure.batch.protocol.models.TaskAddParameter;
import com.microsoft.azure.batch.protocol.models.TaskConstraints;
import com.microsoft.azure.batch.protocol.models.TaskDeleteHeaders;
import com.microsoft.azure.batch.protocol.models.TaskDeleteOptions;
import com.microsoft.azure.batch.protocol.models.TaskGetHeaders;
import com.microsoft.azure.batch.protocol.models.TaskGetOptions;
import com.microsoft.azure.batch.protocol.models.TaskListHeaders;
import com.microsoft.azure.batch.protocol.models.TaskListNextOptions;
import com.microsoft.azure.batch.protocol.models.TaskListOptions;
import com.microsoft.azure.batch.protocol.models.TaskListSubtasksHeaders;
import com.microsoft.azure.batch.protocol.models.TaskListSubtasksOptions;
import com.microsoft.azure.batch.protocol.models.TaskTerminateHeaders;
import com.microsoft.azure.batch.protocol.models.TaskTerminateOptions;
import com.microsoft.azure.batch.protocol.models.TaskUpdateHeaders;
import com.microsoft.azure.batch.protocol.models.TaskUpdateOptions;
import com.microsoft.azure.batch.protocol.models.TaskUpdateParameter;
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
import retrofit2.http.Path;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;
import retrofit2.http.Url;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * An instance of this class provides access to all the operations defined
 * in Tasks.
 */
public final class TasksImpl implements Tasks {
    /** The Retrofit service to perform REST calls. */
    private TasksService service;
    /** The service client containing this operation class. */
    private BatchServiceClient client;

    /**
     * Initializes an instance of Tasks.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public TasksImpl(Retrofit retrofit, BatchServiceClient client) {
        this.service = retrofit.create(TasksService.class);
        this.client = client;
    }

    /**
     * The interface defining all the services for Tasks to be
     * used by Retrofit to perform actually REST calls.
     */
    interface TasksService {
        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @POST("jobs/{jobId}/tasks")
        Call<ResponseBody> add(@Path("jobId") String jobId, @Body TaskAddParameter task, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @GET("jobs/{jobId}/tasks")
        Call<ResponseBody> list(@Path("jobId") String jobId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("$filter") String filter, @Query("$select") String select, @Query("$expand") String expand, @Query("maxresults") Integer maxResults, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @HTTP(path = "jobs/{jobId}/tasks/{taskId}", method = "DELETE", hasBody = true)
        Call<ResponseBody> delete(@Path("jobId") String jobId, @Path("taskId") String taskId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Header("If-Match") String ifMatch, @Header("If-None-Match") String ifNoneMatch, @Header("If-Modified-Since") DateTimeRfc1123 ifModifiedSince, @Header("If-Unmodified-Since") DateTimeRfc1123 ifUnmodifiedSince);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @GET("jobs/{jobId}/tasks/{taskId}")
        Call<ResponseBody> get(@Path("jobId") String jobId, @Path("taskId") String taskId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("$select") String select, @Query("$expand") String expand, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Header("If-Match") String ifMatch, @Header("If-None-Match") String ifNoneMatch, @Header("If-Modified-Since") DateTimeRfc1123 ifModifiedSince, @Header("If-Unmodified-Since") DateTimeRfc1123 ifUnmodifiedSince);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @PUT("jobs/{jobId}/tasks/{taskId}")
        Call<ResponseBody> update(@Path("jobId") String jobId, @Path("taskId") String taskId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Header("If-Match") String ifMatch, @Header("If-None-Match") String ifNoneMatch, @Header("If-Modified-Since") DateTimeRfc1123 ifModifiedSince, @Header("If-Unmodified-Since") DateTimeRfc1123 ifUnmodifiedSince, @Body TaskUpdateParameter taskUpdateParameter);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @GET("jobs/{jobId}/tasks/{taskId}/subtasksinfo")
        Call<ResponseBody> listSubtasks(@Path("jobId") String jobId, @Path("taskId") String taskId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("$select") String select, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @POST("jobs/{jobId}/tasks/{taskId}/terminate")
        Call<ResponseBody> terminate(@Path("jobId") String jobId, @Path("taskId") String taskId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Header("If-Match") String ifMatch, @Header("If-None-Match") String ifNoneMatch, @Header("If-Modified-Since") DateTimeRfc1123 ifModifiedSince, @Header("If-Unmodified-Since") DateTimeRfc1123 ifUnmodifiedSince);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @GET
        Call<ResponseBody> listNext(@Url String nextPageLink, @Header("accept-language") String acceptLanguage, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate);

    }

    /**
     * Adds a task to the specified job.
     *
     * @param jobId The id of the job to which the task is to be added.
     * @param task Specifies the task to be added.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, TaskAddHeaders> add(String jobId, TaskAddParameter task) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (task == null) {
            throw new IllegalArgumentException("Parameter task is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        Validator.validate(task);
        final TaskAddOptions taskAddOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.add(jobId, task, this.client.getApiVersion(), this.client.getAcceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        return addDelegate(call.execute());
    }

    /**
     * Adds a task to the specified job.
     *
     * @param jobId The id of the job to which the task is to be added.
     * @param task Specifies the task to be added.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall addAsync(String jobId, TaskAddParameter task, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (jobId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter jobId is required and cannot be null."));
            return null;
        }
        if (task == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter task is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(task, serviceCallback);
        final TaskAddOptions taskAddOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.add(jobId, task, this.client.getApiVersion(), this.client.getAcceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
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
     * Adds a task to the specified job.
     *
     * @param jobId The id of the job to which the task is to be added.
     * @param task Specifies the task to be added.
     * @param taskAddOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, TaskAddHeaders> add(String jobId, TaskAddParameter task, TaskAddOptions taskAddOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (task == null) {
            throw new IllegalArgumentException("Parameter task is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        Validator.validate(task);
        Validator.validate(taskAddOptions);
        Integer timeout = null;
        if (taskAddOptions != null) {
            timeout = taskAddOptions.getTimeout();
        }
        String clientRequestId = null;
        if (taskAddOptions != null) {
            clientRequestId = taskAddOptions.getClientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (taskAddOptions != null) {
            returnClientRequestId = taskAddOptions.getReturnClientRequestId();
        }
        DateTime ocpDate = null;
        if (taskAddOptions != null) {
            ocpDate = taskAddOptions.getOcpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.add(jobId, task, this.client.getApiVersion(), this.client.getAcceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        return addDelegate(call.execute());
    }

    /**
     * Adds a task to the specified job.
     *
     * @param jobId The id of the job to which the task is to be added.
     * @param task Specifies the task to be added.
     * @param taskAddOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall addAsync(String jobId, TaskAddParameter task, TaskAddOptions taskAddOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (jobId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter jobId is required and cannot be null."));
            return null;
        }
        if (task == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter task is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(task, serviceCallback);
        Validator.validate(taskAddOptions, serviceCallback);
        Integer timeout = null;
        if (taskAddOptions != null) {
            timeout = taskAddOptions.getTimeout();
        }
        String clientRequestId = null;
        if (taskAddOptions != null) {
            clientRequestId = taskAddOptions.getClientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (taskAddOptions != null) {
            returnClientRequestId = taskAddOptions.getReturnClientRequestId();
        }
        DateTime ocpDate = null;
        if (taskAddOptions != null) {
            ocpDate = taskAddOptions.getOcpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.add(jobId, task, this.client.getApiVersion(), this.client.getAcceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
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

    private ServiceResponseWithHeaders<Void, TaskAddHeaders> addDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.getMapperAdapter())
                .register(201, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, TaskAddHeaders.class);
    }

    /**
     * Lists all of the tasks that are associated with the specified job.
     *
     * @param jobId The id of the job.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;CloudTask&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PagedList<CloudTask>, TaskListHeaders> list(final String jobId) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        final TaskListOptions taskListOptions = null;
        String filter = null;
        String select = null;
        String expand = null;
        Integer maxResults = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.list(jobId, this.client.getApiVersion(), this.client.getAcceptLanguage(), filter, select, expand, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        ServiceResponseWithHeaders<PageImpl<CloudTask>, TaskListHeaders> response = listDelegate(call.execute());
        PagedList<CloudTask> result = new PagedList<CloudTask>(response.getBody()) {
            @Override
            public Page<CloudTask> nextPage(String nextPageLink) throws BatchErrorException, IOException {
                return listNext(nextPageLink, null).getBody();
            }
        };
        return new ServiceResponseWithHeaders<>(result, response.getHeaders(), response.getResponse());
    }

    /**
     * Lists all of the tasks that are associated with the specified job.
     *
     * @param jobId The id of the job.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listAsync(final String jobId, final ListOperationCallback<CloudTask> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (jobId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter jobId is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        final TaskListOptions taskListOptions = null;
        String filter = null;
        String select = null;
        String expand = null;
        Integer maxResults = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.list(jobId, this.client.getApiVersion(), this.client.getAcceptLanguage(), filter, select, expand, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<CloudTask>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<CloudTask>, TaskListHeaders> result = listDelegate(response);
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
     * Lists all of the tasks that are associated with the specified job.
     *
     * @param jobId The id of the job.
     * @param taskListOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;CloudTask&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PagedList<CloudTask>, TaskListHeaders> list(final String jobId, final TaskListOptions taskListOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        Validator.validate(taskListOptions);
        String filter = null;
        if (taskListOptions != null) {
            filter = taskListOptions.getFilter();
        }
        String select = null;
        if (taskListOptions != null) {
            select = taskListOptions.getSelect();
        }
        String expand = null;
        if (taskListOptions != null) {
            expand = taskListOptions.getExpand();
        }
        Integer maxResults = null;
        if (taskListOptions != null) {
            maxResults = taskListOptions.getMaxResults();
        }
        Integer timeout = null;
        if (taskListOptions != null) {
            timeout = taskListOptions.getTimeout();
        }
        String clientRequestId = null;
        if (taskListOptions != null) {
            clientRequestId = taskListOptions.getClientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (taskListOptions != null) {
            returnClientRequestId = taskListOptions.getReturnClientRequestId();
        }
        DateTime ocpDate = null;
        if (taskListOptions != null) {
            ocpDate = taskListOptions.getOcpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.list(jobId, this.client.getApiVersion(), this.client.getAcceptLanguage(), filter, select, expand, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        ServiceResponseWithHeaders<PageImpl<CloudTask>, TaskListHeaders> response = listDelegate(call.execute());
        PagedList<CloudTask> result = new PagedList<CloudTask>(response.getBody()) {
            @Override
            public Page<CloudTask> nextPage(String nextPageLink) throws BatchErrorException, IOException {
                TaskListNextOptions taskListNextOptions = null;
                if (taskListOptions != null) {
                    taskListNextOptions = new TaskListNextOptions();
                    taskListNextOptions.setClientRequestId(taskListOptions.getClientRequestId());
                    taskListNextOptions.setReturnClientRequestId(taskListOptions.getReturnClientRequestId());
                    taskListNextOptions.setOcpDate(taskListOptions.getOcpDate());
                }
                return listNext(nextPageLink, taskListNextOptions).getBody();
            }
        };
        return new ServiceResponseWithHeaders<>(result, response.getHeaders(), response.getResponse());
    }

    /**
     * Lists all of the tasks that are associated with the specified job.
     *
     * @param jobId The id of the job.
     * @param taskListOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listAsync(final String jobId, final TaskListOptions taskListOptions, final ListOperationCallback<CloudTask> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (jobId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter jobId is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(taskListOptions, serviceCallback);
        String filter = null;
        if (taskListOptions != null) {
            filter = taskListOptions.getFilter();
        }
        String select = null;
        if (taskListOptions != null) {
            select = taskListOptions.getSelect();
        }
        String expand = null;
        if (taskListOptions != null) {
            expand = taskListOptions.getExpand();
        }
        Integer maxResults = null;
        if (taskListOptions != null) {
            maxResults = taskListOptions.getMaxResults();
        }
        Integer timeout = null;
        if (taskListOptions != null) {
            timeout = taskListOptions.getTimeout();
        }
        String clientRequestId = null;
        if (taskListOptions != null) {
            clientRequestId = taskListOptions.getClientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (taskListOptions != null) {
            returnClientRequestId = taskListOptions.getReturnClientRequestId();
        }
        DateTime ocpDate = null;
        if (taskListOptions != null) {
            ocpDate = taskListOptions.getOcpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.list(jobId, this.client.getApiVersion(), this.client.getAcceptLanguage(), filter, select, expand, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<CloudTask>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<CloudTask>, TaskListHeaders> result = listDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        TaskListNextOptions taskListNextOptions = null;
                        if (taskListOptions != null) {
                            taskListNextOptions = new TaskListNextOptions();
                            taskListNextOptions.setClientRequestId(taskListOptions.getClientRequestId());
                            taskListNextOptions.setReturnClientRequestId(taskListOptions.getReturnClientRequestId());
                            taskListNextOptions.setOcpDate(taskListOptions.getOcpDate());
                        }
                        listNextAsync(result.getBody().getNextPageLink(), taskListNextOptions, serviceCall, serviceCallback);
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

    private ServiceResponseWithHeaders<PageImpl<CloudTask>, TaskListHeaders> listDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<CloudTask>, BatchErrorException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<PageImpl<CloudTask>>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, TaskListHeaders.class);
    }

    /**
     * Deletes a task from the specified job.
     *
     * @param jobId The id of the job from which to delete the task.
     * @param taskId The id of the task to delete.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, TaskDeleteHeaders> delete(String jobId, String taskId) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (taskId == null) {
            throw new IllegalArgumentException("Parameter taskId is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        final TaskDeleteOptions taskDeleteOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        Call<ResponseBody> call = service.delete(jobId, taskId, this.client.getApiVersion(), this.client.getAcceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        return deleteDelegate(call.execute());
    }

    /**
     * Deletes a task from the specified job.
     *
     * @param jobId The id of the job from which to delete the task.
     * @param taskId The id of the task to delete.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall deleteAsync(String jobId, String taskId, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
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
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        final TaskDeleteOptions taskDeleteOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        Call<ResponseBody> call = service.delete(jobId, taskId, this.client.getApiVersion(), this.client.getAcceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
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
     * Deletes a task from the specified job.
     *
     * @param jobId The id of the job from which to delete the task.
     * @param taskId The id of the task to delete.
     * @param taskDeleteOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, TaskDeleteHeaders> delete(String jobId, String taskId, TaskDeleteOptions taskDeleteOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (taskId == null) {
            throw new IllegalArgumentException("Parameter taskId is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        Validator.validate(taskDeleteOptions);
        Integer timeout = null;
        if (taskDeleteOptions != null) {
            timeout = taskDeleteOptions.getTimeout();
        }
        String clientRequestId = null;
        if (taskDeleteOptions != null) {
            clientRequestId = taskDeleteOptions.getClientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (taskDeleteOptions != null) {
            returnClientRequestId = taskDeleteOptions.getReturnClientRequestId();
        }
        DateTime ocpDate = null;
        if (taskDeleteOptions != null) {
            ocpDate = taskDeleteOptions.getOcpDate();
        }
        String ifMatch = null;
        if (taskDeleteOptions != null) {
            ifMatch = taskDeleteOptions.getIfMatch();
        }
        String ifNoneMatch = null;
        if (taskDeleteOptions != null) {
            ifNoneMatch = taskDeleteOptions.getIfNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (taskDeleteOptions != null) {
            ifModifiedSince = taskDeleteOptions.getIfModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (taskDeleteOptions != null) {
            ifUnmodifiedSince = taskDeleteOptions.getIfUnmodifiedSince();
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
        Call<ResponseBody> call = service.delete(jobId, taskId, this.client.getApiVersion(), this.client.getAcceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        return deleteDelegate(call.execute());
    }

    /**
     * Deletes a task from the specified job.
     *
     * @param jobId The id of the job from which to delete the task.
     * @param taskId The id of the task to delete.
     * @param taskDeleteOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall deleteAsync(String jobId, String taskId, TaskDeleteOptions taskDeleteOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
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
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(taskDeleteOptions, serviceCallback);
        Integer timeout = null;
        if (taskDeleteOptions != null) {
            timeout = taskDeleteOptions.getTimeout();
        }
        String clientRequestId = null;
        if (taskDeleteOptions != null) {
            clientRequestId = taskDeleteOptions.getClientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (taskDeleteOptions != null) {
            returnClientRequestId = taskDeleteOptions.getReturnClientRequestId();
        }
        DateTime ocpDate = null;
        if (taskDeleteOptions != null) {
            ocpDate = taskDeleteOptions.getOcpDate();
        }
        String ifMatch = null;
        if (taskDeleteOptions != null) {
            ifMatch = taskDeleteOptions.getIfMatch();
        }
        String ifNoneMatch = null;
        if (taskDeleteOptions != null) {
            ifNoneMatch = taskDeleteOptions.getIfNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (taskDeleteOptions != null) {
            ifModifiedSince = taskDeleteOptions.getIfModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (taskDeleteOptions != null) {
            ifUnmodifiedSince = taskDeleteOptions.getIfUnmodifiedSince();
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
        Call<ResponseBody> call = service.delete(jobId, taskId, this.client.getApiVersion(), this.client.getAcceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
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

    private ServiceResponseWithHeaders<Void, TaskDeleteHeaders> deleteDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, TaskDeleteHeaders.class);
    }

    /**
     * Gets information about the specified task.
     *
     * @param jobId The id of the job that contains the task.
     * @param taskId The id of the task to get information about.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the CloudTask object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<CloudTask, TaskGetHeaders> get(String jobId, String taskId) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (taskId == null) {
            throw new IllegalArgumentException("Parameter taskId is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        final TaskGetOptions taskGetOptions = null;
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
        Call<ResponseBody> call = service.get(jobId, taskId, this.client.getApiVersion(), this.client.getAcceptLanguage(), select, expand, timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        return getDelegate(call.execute());
    }

    /**
     * Gets information about the specified task.
     *
     * @param jobId The id of the job that contains the task.
     * @param taskId The id of the task to get information about.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getAsync(String jobId, String taskId, final ServiceCallback<CloudTask> serviceCallback) throws IllegalArgumentException {
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
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        final TaskGetOptions taskGetOptions = null;
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
        Call<ResponseBody> call = service.get(jobId, taskId, this.client.getApiVersion(), this.client.getAcceptLanguage(), select, expand, timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<CloudTask>(serviceCallback) {
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
     * Gets information about the specified task.
     *
     * @param jobId The id of the job that contains the task.
     * @param taskId The id of the task to get information about.
     * @param taskGetOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the CloudTask object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<CloudTask, TaskGetHeaders> get(String jobId, String taskId, TaskGetOptions taskGetOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (taskId == null) {
            throw new IllegalArgumentException("Parameter taskId is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        Validator.validate(taskGetOptions);
        String select = null;
        if (taskGetOptions != null) {
            select = taskGetOptions.getSelect();
        }
        String expand = null;
        if (taskGetOptions != null) {
            expand = taskGetOptions.getExpand();
        }
        Integer timeout = null;
        if (taskGetOptions != null) {
            timeout = taskGetOptions.getTimeout();
        }
        String clientRequestId = null;
        if (taskGetOptions != null) {
            clientRequestId = taskGetOptions.getClientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (taskGetOptions != null) {
            returnClientRequestId = taskGetOptions.getReturnClientRequestId();
        }
        DateTime ocpDate = null;
        if (taskGetOptions != null) {
            ocpDate = taskGetOptions.getOcpDate();
        }
        String ifMatch = null;
        if (taskGetOptions != null) {
            ifMatch = taskGetOptions.getIfMatch();
        }
        String ifNoneMatch = null;
        if (taskGetOptions != null) {
            ifNoneMatch = taskGetOptions.getIfNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (taskGetOptions != null) {
            ifModifiedSince = taskGetOptions.getIfModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (taskGetOptions != null) {
            ifUnmodifiedSince = taskGetOptions.getIfUnmodifiedSince();
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
        Call<ResponseBody> call = service.get(jobId, taskId, this.client.getApiVersion(), this.client.getAcceptLanguage(), select, expand, timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        return getDelegate(call.execute());
    }

    /**
     * Gets information about the specified task.
     *
     * @param jobId The id of the job that contains the task.
     * @param taskId The id of the task to get information about.
     * @param taskGetOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getAsync(String jobId, String taskId, TaskGetOptions taskGetOptions, final ServiceCallback<CloudTask> serviceCallback) throws IllegalArgumentException {
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
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(taskGetOptions, serviceCallback);
        String select = null;
        if (taskGetOptions != null) {
            select = taskGetOptions.getSelect();
        }
        String expand = null;
        if (taskGetOptions != null) {
            expand = taskGetOptions.getExpand();
        }
        Integer timeout = null;
        if (taskGetOptions != null) {
            timeout = taskGetOptions.getTimeout();
        }
        String clientRequestId = null;
        if (taskGetOptions != null) {
            clientRequestId = taskGetOptions.getClientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (taskGetOptions != null) {
            returnClientRequestId = taskGetOptions.getReturnClientRequestId();
        }
        DateTime ocpDate = null;
        if (taskGetOptions != null) {
            ocpDate = taskGetOptions.getOcpDate();
        }
        String ifMatch = null;
        if (taskGetOptions != null) {
            ifMatch = taskGetOptions.getIfMatch();
        }
        String ifNoneMatch = null;
        if (taskGetOptions != null) {
            ifNoneMatch = taskGetOptions.getIfNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (taskGetOptions != null) {
            ifModifiedSince = taskGetOptions.getIfModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (taskGetOptions != null) {
            ifUnmodifiedSince = taskGetOptions.getIfUnmodifiedSince();
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
        Call<ResponseBody> call = service.get(jobId, taskId, this.client.getApiVersion(), this.client.getAcceptLanguage(), select, expand, timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<CloudTask>(serviceCallback) {
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

    private ServiceResponseWithHeaders<CloudTask, TaskGetHeaders> getDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<CloudTask, BatchErrorException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<CloudTask>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, TaskGetHeaders.class);
    }

    /**
     * Updates the properties of the specified task.
     *
     * @param jobId The id of the job containing the task.
     * @param taskId The id of the task to update.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, TaskUpdateHeaders> update(String jobId, String taskId) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (taskId == null) {
            throw new IllegalArgumentException("Parameter taskId is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        final TaskConstraints constraints = null;
        final TaskUpdateOptions taskUpdateOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        TaskUpdateParameter taskUpdateParameter = new TaskUpdateParameter();
        taskUpdateParameter = null;
        Call<ResponseBody> call = service.update(jobId, taskId, this.client.getApiVersion(), this.client.getAcceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted, taskUpdateParameter);
        return updateDelegate(call.execute());
    }

    /**
     * Updates the properties of the specified task.
     *
     * @param jobId The id of the job containing the task.
     * @param taskId The id of the task to update.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall updateAsync(String jobId, String taskId, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
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
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        final TaskConstraints constraints = null;
        final TaskUpdateOptions taskUpdateOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        TaskUpdateParameter taskUpdateParameter = new TaskUpdateParameter();
        taskUpdateParameter = null;
        Call<ResponseBody> call = service.update(jobId, taskId, this.client.getApiVersion(), this.client.getAcceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted, taskUpdateParameter);
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
     * Updates the properties of the specified task.
     *
     * @param jobId The id of the job containing the task.
     * @param taskId The id of the task to update.
     * @param constraints Sets constraints that apply to this task. If omitted, the task is given the default constraints.
     * @param taskUpdateOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, TaskUpdateHeaders> update(String jobId, String taskId, TaskConstraints constraints, TaskUpdateOptions taskUpdateOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (taskId == null) {
            throw new IllegalArgumentException("Parameter taskId is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        Validator.validate(constraints);
        Validator.validate(taskUpdateOptions);
        Integer timeout = null;
        if (taskUpdateOptions != null) {
            timeout = taskUpdateOptions.getTimeout();
        }
        String clientRequestId = null;
        if (taskUpdateOptions != null) {
            clientRequestId = taskUpdateOptions.getClientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (taskUpdateOptions != null) {
            returnClientRequestId = taskUpdateOptions.getReturnClientRequestId();
        }
        DateTime ocpDate = null;
        if (taskUpdateOptions != null) {
            ocpDate = taskUpdateOptions.getOcpDate();
        }
        String ifMatch = null;
        if (taskUpdateOptions != null) {
            ifMatch = taskUpdateOptions.getIfMatch();
        }
        String ifNoneMatch = null;
        if (taskUpdateOptions != null) {
            ifNoneMatch = taskUpdateOptions.getIfNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (taskUpdateOptions != null) {
            ifModifiedSince = taskUpdateOptions.getIfModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (taskUpdateOptions != null) {
            ifUnmodifiedSince = taskUpdateOptions.getIfUnmodifiedSince();
        }
        TaskUpdateParameter taskUpdateParameter = new TaskUpdateParameter();
        taskUpdateParameter.setConstraints(constraints);
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
        Call<ResponseBody> call = service.update(jobId, taskId, this.client.getApiVersion(), this.client.getAcceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted, taskUpdateParameter);
        return updateDelegate(call.execute());
    }

    /**
     * Updates the properties of the specified task.
     *
     * @param jobId The id of the job containing the task.
     * @param taskId The id of the task to update.
     * @param constraints Sets constraints that apply to this task. If omitted, the task is given the default constraints.
     * @param taskUpdateOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall updateAsync(String jobId, String taskId, TaskConstraints constraints, TaskUpdateOptions taskUpdateOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
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
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(constraints, serviceCallback);
        Validator.validate(taskUpdateOptions, serviceCallback);
        Integer timeout = null;
        if (taskUpdateOptions != null) {
            timeout = taskUpdateOptions.getTimeout();
        }
        String clientRequestId = null;
        if (taskUpdateOptions != null) {
            clientRequestId = taskUpdateOptions.getClientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (taskUpdateOptions != null) {
            returnClientRequestId = taskUpdateOptions.getReturnClientRequestId();
        }
        DateTime ocpDate = null;
        if (taskUpdateOptions != null) {
            ocpDate = taskUpdateOptions.getOcpDate();
        }
        String ifMatch = null;
        if (taskUpdateOptions != null) {
            ifMatch = taskUpdateOptions.getIfMatch();
        }
        String ifNoneMatch = null;
        if (taskUpdateOptions != null) {
            ifNoneMatch = taskUpdateOptions.getIfNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (taskUpdateOptions != null) {
            ifModifiedSince = taskUpdateOptions.getIfModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (taskUpdateOptions != null) {
            ifUnmodifiedSince = taskUpdateOptions.getIfUnmodifiedSince();
        }
        TaskUpdateParameter taskUpdateParameter = new TaskUpdateParameter();
        taskUpdateParameter.setConstraints(constraints);
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
        Call<ResponseBody> call = service.update(jobId, taskId, this.client.getApiVersion(), this.client.getAcceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted, taskUpdateParameter);
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

    private ServiceResponseWithHeaders<Void, TaskUpdateHeaders> updateDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, TaskUpdateHeaders.class);
    }

    /**
     * Lists all of the subtasks that are associated with the specified multi-instance task.
     *
     * @param jobId The id of the job.
     * @param taskId The id of the task.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the CloudTaskListSubtasksResult object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<CloudTaskListSubtasksResult, TaskListSubtasksHeaders> listSubtasks(String jobId, String taskId) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (taskId == null) {
            throw new IllegalArgumentException("Parameter taskId is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        final TaskListSubtasksOptions taskListSubtasksOptions = null;
        String select = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.listSubtasks(jobId, taskId, this.client.getApiVersion(), this.client.getAcceptLanguage(), select, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        return listSubtasksDelegate(call.execute());
    }

    /**
     * Lists all of the subtasks that are associated with the specified multi-instance task.
     *
     * @param jobId The id of the job.
     * @param taskId The id of the task.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listSubtasksAsync(String jobId, String taskId, final ServiceCallback<CloudTaskListSubtasksResult> serviceCallback) throws IllegalArgumentException {
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
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        final TaskListSubtasksOptions taskListSubtasksOptions = null;
        String select = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.listSubtasks(jobId, taskId, this.client.getApiVersion(), this.client.getAcceptLanguage(), select, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<CloudTaskListSubtasksResult>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(listSubtasksDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Lists all of the subtasks that are associated with the specified multi-instance task.
     *
     * @param jobId The id of the job.
     * @param taskId The id of the task.
     * @param taskListSubtasksOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the CloudTaskListSubtasksResult object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<CloudTaskListSubtasksResult, TaskListSubtasksHeaders> listSubtasks(String jobId, String taskId, TaskListSubtasksOptions taskListSubtasksOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (taskId == null) {
            throw new IllegalArgumentException("Parameter taskId is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        Validator.validate(taskListSubtasksOptions);
        String select = null;
        if (taskListSubtasksOptions != null) {
            select = taskListSubtasksOptions.getSelect();
        }
        Integer timeout = null;
        if (taskListSubtasksOptions != null) {
            timeout = taskListSubtasksOptions.getTimeout();
        }
        String clientRequestId = null;
        if (taskListSubtasksOptions != null) {
            clientRequestId = taskListSubtasksOptions.getClientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (taskListSubtasksOptions != null) {
            returnClientRequestId = taskListSubtasksOptions.getReturnClientRequestId();
        }
        DateTime ocpDate = null;
        if (taskListSubtasksOptions != null) {
            ocpDate = taskListSubtasksOptions.getOcpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.listSubtasks(jobId, taskId, this.client.getApiVersion(), this.client.getAcceptLanguage(), select, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        return listSubtasksDelegate(call.execute());
    }

    /**
     * Lists all of the subtasks that are associated with the specified multi-instance task.
     *
     * @param jobId The id of the job.
     * @param taskId The id of the task.
     * @param taskListSubtasksOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listSubtasksAsync(String jobId, String taskId, TaskListSubtasksOptions taskListSubtasksOptions, final ServiceCallback<CloudTaskListSubtasksResult> serviceCallback) throws IllegalArgumentException {
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
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(taskListSubtasksOptions, serviceCallback);
        String select = null;
        if (taskListSubtasksOptions != null) {
            select = taskListSubtasksOptions.getSelect();
        }
        Integer timeout = null;
        if (taskListSubtasksOptions != null) {
            timeout = taskListSubtasksOptions.getTimeout();
        }
        String clientRequestId = null;
        if (taskListSubtasksOptions != null) {
            clientRequestId = taskListSubtasksOptions.getClientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (taskListSubtasksOptions != null) {
            returnClientRequestId = taskListSubtasksOptions.getReturnClientRequestId();
        }
        DateTime ocpDate = null;
        if (taskListSubtasksOptions != null) {
            ocpDate = taskListSubtasksOptions.getOcpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.listSubtasks(jobId, taskId, this.client.getApiVersion(), this.client.getAcceptLanguage(), select, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<CloudTaskListSubtasksResult>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(listSubtasksDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<CloudTaskListSubtasksResult, TaskListSubtasksHeaders> listSubtasksDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<CloudTaskListSubtasksResult, BatchErrorException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<CloudTaskListSubtasksResult>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, TaskListSubtasksHeaders.class);
    }

    /**
     * Terminates the specified task.
     *
     * @param jobId The id of the job containing the task.
     * @param taskId The id of the task to terminate.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, TaskTerminateHeaders> terminate(String jobId, String taskId) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (taskId == null) {
            throw new IllegalArgumentException("Parameter taskId is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        final TaskTerminateOptions taskTerminateOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        Call<ResponseBody> call = service.terminate(jobId, taskId, this.client.getApiVersion(), this.client.getAcceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        return terminateDelegate(call.execute());
    }

    /**
     * Terminates the specified task.
     *
     * @param jobId The id of the job containing the task.
     * @param taskId The id of the task to terminate.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall terminateAsync(String jobId, String taskId, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
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
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        final TaskTerminateOptions taskTerminateOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        Call<ResponseBody> call = service.terminate(jobId, taskId, this.client.getApiVersion(), this.client.getAcceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
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
     * Terminates the specified task.
     *
     * @param jobId The id of the job containing the task.
     * @param taskId The id of the task to terminate.
     * @param taskTerminateOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, TaskTerminateHeaders> terminate(String jobId, String taskId, TaskTerminateOptions taskTerminateOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (taskId == null) {
            throw new IllegalArgumentException("Parameter taskId is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        Validator.validate(taskTerminateOptions);
        Integer timeout = null;
        if (taskTerminateOptions != null) {
            timeout = taskTerminateOptions.getTimeout();
        }
        String clientRequestId = null;
        if (taskTerminateOptions != null) {
            clientRequestId = taskTerminateOptions.getClientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (taskTerminateOptions != null) {
            returnClientRequestId = taskTerminateOptions.getReturnClientRequestId();
        }
        DateTime ocpDate = null;
        if (taskTerminateOptions != null) {
            ocpDate = taskTerminateOptions.getOcpDate();
        }
        String ifMatch = null;
        if (taskTerminateOptions != null) {
            ifMatch = taskTerminateOptions.getIfMatch();
        }
        String ifNoneMatch = null;
        if (taskTerminateOptions != null) {
            ifNoneMatch = taskTerminateOptions.getIfNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (taskTerminateOptions != null) {
            ifModifiedSince = taskTerminateOptions.getIfModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (taskTerminateOptions != null) {
            ifUnmodifiedSince = taskTerminateOptions.getIfUnmodifiedSince();
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
        Call<ResponseBody> call = service.terminate(jobId, taskId, this.client.getApiVersion(), this.client.getAcceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        return terminateDelegate(call.execute());
    }

    /**
     * Terminates the specified task.
     *
     * @param jobId The id of the job containing the task.
     * @param taskId The id of the task to terminate.
     * @param taskTerminateOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall terminateAsync(String jobId, String taskId, TaskTerminateOptions taskTerminateOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
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
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(taskTerminateOptions, serviceCallback);
        Integer timeout = null;
        if (taskTerminateOptions != null) {
            timeout = taskTerminateOptions.getTimeout();
        }
        String clientRequestId = null;
        if (taskTerminateOptions != null) {
            clientRequestId = taskTerminateOptions.getClientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (taskTerminateOptions != null) {
            returnClientRequestId = taskTerminateOptions.getReturnClientRequestId();
        }
        DateTime ocpDate = null;
        if (taskTerminateOptions != null) {
            ocpDate = taskTerminateOptions.getOcpDate();
        }
        String ifMatch = null;
        if (taskTerminateOptions != null) {
            ifMatch = taskTerminateOptions.getIfMatch();
        }
        String ifNoneMatch = null;
        if (taskTerminateOptions != null) {
            ifNoneMatch = taskTerminateOptions.getIfNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (taskTerminateOptions != null) {
            ifModifiedSince = taskTerminateOptions.getIfModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (taskTerminateOptions != null) {
            ifUnmodifiedSince = taskTerminateOptions.getIfUnmodifiedSince();
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
        Call<ResponseBody> call = service.terminate(jobId, taskId, this.client.getApiVersion(), this.client.getAcceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
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

    private ServiceResponseWithHeaders<Void, TaskTerminateHeaders> terminateDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.getMapperAdapter())
                .register(204, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, TaskTerminateHeaders.class);
    }

    /**
     * Lists all of the tasks that are associated with the specified job.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;CloudTask&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PageImpl<CloudTask>, TaskListHeaders> listNext(final String nextPageLink) throws BatchErrorException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        final TaskListNextOptions taskListNextOptions = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.getAcceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted);
        return listNextDelegate(call.execute());
    }

    /**
     * Lists all of the tasks that are associated with the specified job.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<CloudTask> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        final TaskListNextOptions taskListNextOptions = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.getAcceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted);
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<CloudTask>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<CloudTask>, TaskListHeaders> result = listNextDelegate(response);
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
     * Lists all of the tasks that are associated with the specified job.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param taskListNextOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;CloudTask&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PageImpl<CloudTask>, TaskListHeaders> listNext(final String nextPageLink, final TaskListNextOptions taskListNextOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        Validator.validate(taskListNextOptions);
        String clientRequestId = null;
        if (taskListNextOptions != null) {
            clientRequestId = taskListNextOptions.getClientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (taskListNextOptions != null) {
            returnClientRequestId = taskListNextOptions.getReturnClientRequestId();
        }
        DateTime ocpDate = null;
        if (taskListNextOptions != null) {
            ocpDate = taskListNextOptions.getOcpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.getAcceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted);
        return listNextDelegate(call.execute());
    }

    /**
     * Lists all of the tasks that are associated with the specified job.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param taskListNextOptions Additional parameters for the operation
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listNextAsync(final String nextPageLink, final TaskListNextOptions taskListNextOptions, final ServiceCall serviceCall, final ListOperationCallback<CloudTask> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        Validator.validate(taskListNextOptions, serviceCallback);
        String clientRequestId = null;
        if (taskListNextOptions != null) {
            clientRequestId = taskListNextOptions.getClientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (taskListNextOptions != null) {
            returnClientRequestId = taskListNextOptions.getReturnClientRequestId();
        }
        DateTime ocpDate = null;
        if (taskListNextOptions != null) {
            ocpDate = taskListNextOptions.getOcpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.getAcceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted);
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<CloudTask>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<CloudTask>, TaskListHeaders> result = listNextDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listNextAsync(result.getBody().getNextPageLink(), taskListNextOptions, serviceCall, serviceCallback);
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

    private ServiceResponseWithHeaders<PageImpl<CloudTask>, TaskListHeaders> listNextDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<CloudTask>, BatchErrorException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<PageImpl<CloudTask>>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, TaskListHeaders.class);
    }

}
