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
import com.microsoft.rest.serializer.CollectionFormat;
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

/**
 * An instance of this class provides access to all the operations defined
 * in Tasks.
 */
public final class TasksInner {
    /** The Retrofit service to perform REST calls. */
    private TasksService service;
    /** The service client containing this operation class. */
    private BatchServiceClientImpl client;

    /**
     * Initializes an instance of TasksInner.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public TasksInner(Retrofit retrofit, BatchServiceClientImpl client) {
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
        Call<ResponseBody> add(@Path("jobId") String jobId, @Body TaskAddParameterInner task, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @GET("jobs/{jobId}/tasks")
        Call<ResponseBody> list(@Path("jobId") String jobId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("$filter") String filter, @Query("$select") String select, @Query("$expand") String expand, @Query("maxresults") Integer maxResults, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @POST("jobs/{jobId}/addtaskcollection")
        Call<ResponseBody> addCollection(@Path("jobId") String jobId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Body TaskAddCollectionParameter taskCollection);

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
    public ServiceResponseWithHeaders<Void, TaskAddHeadersInner> add(String jobId, TaskAddParameterInner task) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (task == null) {
            throw new IllegalArgumentException("Parameter task is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(task);
        final TaskAddOptionsInner taskAddOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.add(jobId, task, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
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
    public ServiceCall addAsync(String jobId, TaskAddParameterInner task, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
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
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(task, serviceCallback);
        final TaskAddOptionsInner taskAddOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.add(jobId, task, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
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
    public ServiceResponseWithHeaders<Void, TaskAddHeadersInner> add(String jobId, TaskAddParameterInner task, TaskAddOptionsInner taskAddOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (task == null) {
            throw new IllegalArgumentException("Parameter task is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(task);
        Validator.validate(taskAddOptions);
        Integer timeout = null;
        if (taskAddOptions != null) {
            timeout = taskAddOptions.timeout();
        }
        String clientRequestId = null;
        if (taskAddOptions != null) {
            clientRequestId = taskAddOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (taskAddOptions != null) {
            returnClientRequestId = taskAddOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (taskAddOptions != null) {
            ocpDate = taskAddOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.add(jobId, task, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
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
    public ServiceCall addAsync(String jobId, TaskAddParameterInner task, TaskAddOptionsInner taskAddOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
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
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(task, serviceCallback);
        Validator.validate(taskAddOptions, serviceCallback);
        Integer timeout = null;
        if (taskAddOptions != null) {
            timeout = taskAddOptions.timeout();
        }
        String clientRequestId = null;
        if (taskAddOptions != null) {
            clientRequestId = taskAddOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (taskAddOptions != null) {
            returnClientRequestId = taskAddOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (taskAddOptions != null) {
            ocpDate = taskAddOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.add(jobId, task, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
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

    private ServiceResponseWithHeaders<Void, TaskAddHeadersInner> addDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(201, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, TaskAddHeadersInner.class);
    }

    /**
     * Lists all of the tasks that are associated with the specified job.
     *
     * @param jobId The id of the job.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;CloudTaskInner&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PagedList<CloudTaskInner>, TaskListHeadersInner> list(final String jobId) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final TaskListOptionsInner taskListOptions = null;
        String filter = null;
        String select = null;
        String expand = null;
        Integer maxResults = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.list(jobId, this.client.apiVersion(), this.client.acceptLanguage(), filter, select, expand, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        ServiceResponseWithHeaders<PageImpl<CloudTaskInner>, TaskListHeadersInner> response = listDelegate(call.execute());
        PagedList<CloudTaskInner> result = new PagedList<CloudTaskInner>(response.getBody()) {
            @Override
            public Page<CloudTaskInner> nextPage(String nextPageLink) throws BatchErrorException, IOException {
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
    public ServiceCall listAsync(final String jobId, final ListOperationCallback<CloudTaskInner> serviceCallback) throws IllegalArgumentException {
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
        final TaskListOptionsInner taskListOptions = null;
        String filter = null;
        String select = null;
        String expand = null;
        Integer maxResults = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.list(jobId, this.client.apiVersion(), this.client.acceptLanguage(), filter, select, expand, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<CloudTaskInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<CloudTaskInner>, TaskListHeadersInner> result = listDelegate(response);
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
     * @return the List&lt;CloudTaskInner&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PagedList<CloudTaskInner>, TaskListHeadersInner> list(final String jobId, final TaskListOptionsInner taskListOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(taskListOptions);
        String filter = null;
        if (taskListOptions != null) {
            filter = taskListOptions.filter();
        }
        String select = null;
        if (taskListOptions != null) {
            select = taskListOptions.select();
        }
        String expand = null;
        if (taskListOptions != null) {
            expand = taskListOptions.expand();
        }
        Integer maxResults = null;
        if (taskListOptions != null) {
            maxResults = taskListOptions.maxResults();
        }
        Integer timeout = null;
        if (taskListOptions != null) {
            timeout = taskListOptions.timeout();
        }
        String clientRequestId = null;
        if (taskListOptions != null) {
            clientRequestId = taskListOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (taskListOptions != null) {
            returnClientRequestId = taskListOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (taskListOptions != null) {
            ocpDate = taskListOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.list(jobId, this.client.apiVersion(), this.client.acceptLanguage(), filter, select, expand, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        ServiceResponseWithHeaders<PageImpl<CloudTaskInner>, TaskListHeadersInner> response = listDelegate(call.execute());
        PagedList<CloudTaskInner> result = new PagedList<CloudTaskInner>(response.getBody()) {
            @Override
            public Page<CloudTaskInner> nextPage(String nextPageLink) throws BatchErrorException, IOException {
                TaskListNextOptionsInner taskListNextOptions = null;
                if (taskListOptions != null) {
                    taskListNextOptions = new TaskListNextOptionsInner();
                    taskListNextOptions.setClientRequestId(taskListOptions.clientRequestId());
                    taskListNextOptions.setReturnClientRequestId(taskListOptions.returnClientRequestId());
                    taskListNextOptions.setOcpDate(taskListOptions.ocpDate());
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
    public ServiceCall listAsync(final String jobId, final TaskListOptionsInner taskListOptions, final ListOperationCallback<CloudTaskInner> serviceCallback) throws IllegalArgumentException {
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
        Validator.validate(taskListOptions, serviceCallback);
        String filter = null;
        if (taskListOptions != null) {
            filter = taskListOptions.filter();
        }
        String select = null;
        if (taskListOptions != null) {
            select = taskListOptions.select();
        }
        String expand = null;
        if (taskListOptions != null) {
            expand = taskListOptions.expand();
        }
        Integer maxResults = null;
        if (taskListOptions != null) {
            maxResults = taskListOptions.maxResults();
        }
        Integer timeout = null;
        if (taskListOptions != null) {
            timeout = taskListOptions.timeout();
        }
        String clientRequestId = null;
        if (taskListOptions != null) {
            clientRequestId = taskListOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (taskListOptions != null) {
            returnClientRequestId = taskListOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (taskListOptions != null) {
            ocpDate = taskListOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.list(jobId, this.client.apiVersion(), this.client.acceptLanguage(), filter, select, expand, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<CloudTaskInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<CloudTaskInner>, TaskListHeadersInner> result = listDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        TaskListNextOptionsInner taskListNextOptions = null;
                        if (taskListOptions != null) {
                            taskListNextOptions = new TaskListNextOptionsInner();
                            taskListNextOptions.setClientRequestId(taskListOptions.clientRequestId());
                            taskListNextOptions.setReturnClientRequestId(taskListOptions.returnClientRequestId());
                            taskListNextOptions.setOcpDate(taskListOptions.ocpDate());
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

    private ServiceResponseWithHeaders<PageImpl<CloudTaskInner>, TaskListHeadersInner> listDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<CloudTaskInner>, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<CloudTaskInner>>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, TaskListHeadersInner.class);
    }

    /**
     * Adds a collection of tasks to the specified job.
     *
     * @param jobId The id of the job to which the task collection is to be added.
     * @param value The collection of tasks to add.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the TaskAddCollectionResultInner object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<TaskAddCollectionResultInner, TaskAddCollectionHeadersInner> addCollection(String jobId, List<TaskAddParameterInner> value) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        if (value == null) {
            throw new IllegalArgumentException("Parameter value is required and cannot be null.");
        }
        Validator.validate(value);
        final TaskAddCollectionOptionsInner taskAddCollectionOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        TaskAddCollectionParameter taskCollection = new TaskAddCollectionParameter();
        taskCollection.setValue(value);
        Call<ResponseBody> call = service.addCollection(jobId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, taskCollection);
        return addCollectionDelegate(call.execute());
    }

    /**
     * Adds a collection of tasks to the specified job.
     *
     * @param jobId The id of the job to which the task collection is to be added.
     * @param value The collection of tasks to add.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall addCollectionAsync(String jobId, List<TaskAddParameterInner> value, final ServiceCallback<TaskAddCollectionResultInner> serviceCallback) throws IllegalArgumentException {
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
        if (value == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter value is required and cannot be null."));
            return null;
        }
        Validator.validate(value, serviceCallback);
        final TaskAddCollectionOptionsInner taskAddCollectionOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        TaskAddCollectionParameter taskCollection = new TaskAddCollectionParameter();
        taskCollection.setValue(value);
        Call<ResponseBody> call = service.addCollection(jobId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, taskCollection);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<TaskAddCollectionResultInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(addCollectionDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Adds a collection of tasks to the specified job.
     *
     * @param jobId The id of the job to which the task collection is to be added.
     * @param value The collection of tasks to add.
     * @param taskAddCollectionOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the TaskAddCollectionResultInner object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<TaskAddCollectionResultInner, TaskAddCollectionHeadersInner> addCollection(String jobId, List<TaskAddParameterInner> value, TaskAddCollectionOptionsInner taskAddCollectionOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        if (value == null) {
            throw new IllegalArgumentException("Parameter value is required and cannot be null.");
        }
        Validator.validate(value);
        Validator.validate(taskAddCollectionOptions);
        Integer timeout = null;
        if (taskAddCollectionOptions != null) {
            timeout = taskAddCollectionOptions.timeout();
        }
        String clientRequestId = null;
        if (taskAddCollectionOptions != null) {
            clientRequestId = taskAddCollectionOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (taskAddCollectionOptions != null) {
            returnClientRequestId = taskAddCollectionOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (taskAddCollectionOptions != null) {
            ocpDate = taskAddCollectionOptions.ocpDate();
        }
        TaskAddCollectionParameter taskCollection = new TaskAddCollectionParameter();
        taskCollection.setValue(value);
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.addCollection(jobId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, taskCollection);
        return addCollectionDelegate(call.execute());
    }

    /**
     * Adds a collection of tasks to the specified job.
     *
     * @param jobId The id of the job to which the task collection is to be added.
     * @param value The collection of tasks to add.
     * @param taskAddCollectionOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall addCollectionAsync(String jobId, List<TaskAddParameterInner> value, TaskAddCollectionOptionsInner taskAddCollectionOptions, final ServiceCallback<TaskAddCollectionResultInner> serviceCallback) throws IllegalArgumentException {
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
        if (value == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter value is required and cannot be null."));
            return null;
        }
        Validator.validate(value, serviceCallback);
        Validator.validate(taskAddCollectionOptions, serviceCallback);
        Integer timeout = null;
        if (taskAddCollectionOptions != null) {
            timeout = taskAddCollectionOptions.timeout();
        }
        String clientRequestId = null;
        if (taskAddCollectionOptions != null) {
            clientRequestId = taskAddCollectionOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (taskAddCollectionOptions != null) {
            returnClientRequestId = taskAddCollectionOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (taskAddCollectionOptions != null) {
            ocpDate = taskAddCollectionOptions.ocpDate();
        }
        TaskAddCollectionParameter taskCollection = new TaskAddCollectionParameter();
        taskCollection.setValue(value);
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.addCollection(jobId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, taskCollection);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<TaskAddCollectionResultInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(addCollectionDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<TaskAddCollectionResultInner, TaskAddCollectionHeadersInner> addCollectionDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<TaskAddCollectionResultInner, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<TaskAddCollectionResultInner>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, TaskAddCollectionHeadersInner.class);
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
    public ServiceResponseWithHeaders<Void, TaskDeleteHeadersInner> delete(String jobId, String taskId) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (taskId == null) {
            throw new IllegalArgumentException("Parameter taskId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final TaskDeleteOptionsInner taskDeleteOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        Call<ResponseBody> call = service.delete(jobId, taskId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
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
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final TaskDeleteOptionsInner taskDeleteOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        Call<ResponseBody> call = service.delete(jobId, taskId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
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
    public ServiceResponseWithHeaders<Void, TaskDeleteHeadersInner> delete(String jobId, String taskId, TaskDeleteOptionsInner taskDeleteOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (taskId == null) {
            throw new IllegalArgumentException("Parameter taskId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(taskDeleteOptions);
        Integer timeout = null;
        if (taskDeleteOptions != null) {
            timeout = taskDeleteOptions.timeout();
        }
        String clientRequestId = null;
        if (taskDeleteOptions != null) {
            clientRequestId = taskDeleteOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (taskDeleteOptions != null) {
            returnClientRequestId = taskDeleteOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (taskDeleteOptions != null) {
            ocpDate = taskDeleteOptions.ocpDate();
        }
        String ifMatch = null;
        if (taskDeleteOptions != null) {
            ifMatch = taskDeleteOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (taskDeleteOptions != null) {
            ifNoneMatch = taskDeleteOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (taskDeleteOptions != null) {
            ifModifiedSince = taskDeleteOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (taskDeleteOptions != null) {
            ifUnmodifiedSince = taskDeleteOptions.ifUnmodifiedSince();
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
        Call<ResponseBody> call = service.delete(jobId, taskId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
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
    public ServiceCall deleteAsync(String jobId, String taskId, TaskDeleteOptionsInner taskDeleteOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
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
        Validator.validate(taskDeleteOptions, serviceCallback);
        Integer timeout = null;
        if (taskDeleteOptions != null) {
            timeout = taskDeleteOptions.timeout();
        }
        String clientRequestId = null;
        if (taskDeleteOptions != null) {
            clientRequestId = taskDeleteOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (taskDeleteOptions != null) {
            returnClientRequestId = taskDeleteOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (taskDeleteOptions != null) {
            ocpDate = taskDeleteOptions.ocpDate();
        }
        String ifMatch = null;
        if (taskDeleteOptions != null) {
            ifMatch = taskDeleteOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (taskDeleteOptions != null) {
            ifNoneMatch = taskDeleteOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (taskDeleteOptions != null) {
            ifModifiedSince = taskDeleteOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (taskDeleteOptions != null) {
            ifUnmodifiedSince = taskDeleteOptions.ifUnmodifiedSince();
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
        Call<ResponseBody> call = service.delete(jobId, taskId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
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

    private ServiceResponseWithHeaders<Void, TaskDeleteHeadersInner> deleteDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, TaskDeleteHeadersInner.class);
    }

    /**
     * Gets information about the specified task.
     *
     * @param jobId The id of the job that contains the task.
     * @param taskId The id of the task to get information about.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the CloudTaskInner object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<CloudTaskInner, TaskGetHeadersInner> get(String jobId, String taskId) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (taskId == null) {
            throw new IllegalArgumentException("Parameter taskId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final TaskGetOptionsInner taskGetOptions = null;
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
        Call<ResponseBody> call = service.get(jobId, taskId, this.client.apiVersion(), this.client.acceptLanguage(), select, expand, timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
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
    public ServiceCall getAsync(String jobId, String taskId, final ServiceCallback<CloudTaskInner> serviceCallback) throws IllegalArgumentException {
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
        final TaskGetOptionsInner taskGetOptions = null;
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
        Call<ResponseBody> call = service.get(jobId, taskId, this.client.apiVersion(), this.client.acceptLanguage(), select, expand, timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<CloudTaskInner>(serviceCallback) {
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
     * @return the CloudTaskInner object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<CloudTaskInner, TaskGetHeadersInner> get(String jobId, String taskId, TaskGetOptionsInner taskGetOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (taskId == null) {
            throw new IllegalArgumentException("Parameter taskId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(taskGetOptions);
        String select = null;
        if (taskGetOptions != null) {
            select = taskGetOptions.select();
        }
        String expand = null;
        if (taskGetOptions != null) {
            expand = taskGetOptions.expand();
        }
        Integer timeout = null;
        if (taskGetOptions != null) {
            timeout = taskGetOptions.timeout();
        }
        String clientRequestId = null;
        if (taskGetOptions != null) {
            clientRequestId = taskGetOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (taskGetOptions != null) {
            returnClientRequestId = taskGetOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (taskGetOptions != null) {
            ocpDate = taskGetOptions.ocpDate();
        }
        String ifMatch = null;
        if (taskGetOptions != null) {
            ifMatch = taskGetOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (taskGetOptions != null) {
            ifNoneMatch = taskGetOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (taskGetOptions != null) {
            ifModifiedSince = taskGetOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (taskGetOptions != null) {
            ifUnmodifiedSince = taskGetOptions.ifUnmodifiedSince();
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
        Call<ResponseBody> call = service.get(jobId, taskId, this.client.apiVersion(), this.client.acceptLanguage(), select, expand, timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
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
    public ServiceCall getAsync(String jobId, String taskId, TaskGetOptionsInner taskGetOptions, final ServiceCallback<CloudTaskInner> serviceCallback) throws IllegalArgumentException {
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
        Validator.validate(taskGetOptions, serviceCallback);
        String select = null;
        if (taskGetOptions != null) {
            select = taskGetOptions.select();
        }
        String expand = null;
        if (taskGetOptions != null) {
            expand = taskGetOptions.expand();
        }
        Integer timeout = null;
        if (taskGetOptions != null) {
            timeout = taskGetOptions.timeout();
        }
        String clientRequestId = null;
        if (taskGetOptions != null) {
            clientRequestId = taskGetOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (taskGetOptions != null) {
            returnClientRequestId = taskGetOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (taskGetOptions != null) {
            ocpDate = taskGetOptions.ocpDate();
        }
        String ifMatch = null;
        if (taskGetOptions != null) {
            ifMatch = taskGetOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (taskGetOptions != null) {
            ifNoneMatch = taskGetOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (taskGetOptions != null) {
            ifModifiedSince = taskGetOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (taskGetOptions != null) {
            ifUnmodifiedSince = taskGetOptions.ifUnmodifiedSince();
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
        Call<ResponseBody> call = service.get(jobId, taskId, this.client.apiVersion(), this.client.acceptLanguage(), select, expand, timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<CloudTaskInner>(serviceCallback) {
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

    private ServiceResponseWithHeaders<CloudTaskInner, TaskGetHeadersInner> getDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<CloudTaskInner, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<CloudTaskInner>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, TaskGetHeadersInner.class);
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
    public ServiceResponseWithHeaders<Void, TaskUpdateHeadersInner> update(String jobId, String taskId) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (taskId == null) {
            throw new IllegalArgumentException("Parameter taskId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final TaskConstraintsInner constraints = null;
        final TaskUpdateOptionsInner taskUpdateOptions = null;
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
        Call<ResponseBody> call = service.update(jobId, taskId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted, taskUpdateParameter);
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
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final TaskConstraintsInner constraints = null;
        final TaskUpdateOptionsInner taskUpdateOptions = null;
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
        Call<ResponseBody> call = service.update(jobId, taskId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted, taskUpdateParameter);
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
    public ServiceResponseWithHeaders<Void, TaskUpdateHeadersInner> update(String jobId, String taskId, TaskConstraintsInner constraints, TaskUpdateOptionsInner taskUpdateOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (taskId == null) {
            throw new IllegalArgumentException("Parameter taskId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(constraints);
        Validator.validate(taskUpdateOptions);
        Integer timeout = null;
        if (taskUpdateOptions != null) {
            timeout = taskUpdateOptions.timeout();
        }
        String clientRequestId = null;
        if (taskUpdateOptions != null) {
            clientRequestId = taskUpdateOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (taskUpdateOptions != null) {
            returnClientRequestId = taskUpdateOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (taskUpdateOptions != null) {
            ocpDate = taskUpdateOptions.ocpDate();
        }
        String ifMatch = null;
        if (taskUpdateOptions != null) {
            ifMatch = taskUpdateOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (taskUpdateOptions != null) {
            ifNoneMatch = taskUpdateOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (taskUpdateOptions != null) {
            ifModifiedSince = taskUpdateOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (taskUpdateOptions != null) {
            ifUnmodifiedSince = taskUpdateOptions.ifUnmodifiedSince();
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
        Call<ResponseBody> call = service.update(jobId, taskId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted, taskUpdateParameter);
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
    public ServiceCall updateAsync(String jobId, String taskId, TaskConstraintsInner constraints, TaskUpdateOptionsInner taskUpdateOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
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
        Validator.validate(constraints, serviceCallback);
        Validator.validate(taskUpdateOptions, serviceCallback);
        Integer timeout = null;
        if (taskUpdateOptions != null) {
            timeout = taskUpdateOptions.timeout();
        }
        String clientRequestId = null;
        if (taskUpdateOptions != null) {
            clientRequestId = taskUpdateOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (taskUpdateOptions != null) {
            returnClientRequestId = taskUpdateOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (taskUpdateOptions != null) {
            ocpDate = taskUpdateOptions.ocpDate();
        }
        String ifMatch = null;
        if (taskUpdateOptions != null) {
            ifMatch = taskUpdateOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (taskUpdateOptions != null) {
            ifNoneMatch = taskUpdateOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (taskUpdateOptions != null) {
            ifModifiedSince = taskUpdateOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (taskUpdateOptions != null) {
            ifUnmodifiedSince = taskUpdateOptions.ifUnmodifiedSince();
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
        Call<ResponseBody> call = service.update(jobId, taskId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted, taskUpdateParameter);
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

    private ServiceResponseWithHeaders<Void, TaskUpdateHeadersInner> updateDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, TaskUpdateHeadersInner.class);
    }

    /**
     * Lists all of the subtasks that are associated with the specified multi-instance task.
     *
     * @param jobId The id of the job.
     * @param taskId The id of the task.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the CloudTaskListSubtasksResultInner object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<CloudTaskListSubtasksResultInner, TaskListSubtasksHeadersInner> listSubtasks(String jobId, String taskId) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (taskId == null) {
            throw new IllegalArgumentException("Parameter taskId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final TaskListSubtasksOptionsInner taskListSubtasksOptions = null;
        String select = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.listSubtasks(jobId, taskId, this.client.apiVersion(), this.client.acceptLanguage(), select, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
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
    public ServiceCall listSubtasksAsync(String jobId, String taskId, final ServiceCallback<CloudTaskListSubtasksResultInner> serviceCallback) throws IllegalArgumentException {
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
        final TaskListSubtasksOptionsInner taskListSubtasksOptions = null;
        String select = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.listSubtasks(jobId, taskId, this.client.apiVersion(), this.client.acceptLanguage(), select, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<CloudTaskListSubtasksResultInner>(serviceCallback) {
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
     * @return the CloudTaskListSubtasksResultInner object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<CloudTaskListSubtasksResultInner, TaskListSubtasksHeadersInner> listSubtasks(String jobId, String taskId, TaskListSubtasksOptionsInner taskListSubtasksOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (taskId == null) {
            throw new IllegalArgumentException("Parameter taskId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(taskListSubtasksOptions);
        String select = null;
        if (taskListSubtasksOptions != null) {
            select = taskListSubtasksOptions.select();
        }
        Integer timeout = null;
        if (taskListSubtasksOptions != null) {
            timeout = taskListSubtasksOptions.timeout();
        }
        String clientRequestId = null;
        if (taskListSubtasksOptions != null) {
            clientRequestId = taskListSubtasksOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (taskListSubtasksOptions != null) {
            returnClientRequestId = taskListSubtasksOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (taskListSubtasksOptions != null) {
            ocpDate = taskListSubtasksOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.listSubtasks(jobId, taskId, this.client.apiVersion(), this.client.acceptLanguage(), select, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
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
    public ServiceCall listSubtasksAsync(String jobId, String taskId, TaskListSubtasksOptionsInner taskListSubtasksOptions, final ServiceCallback<CloudTaskListSubtasksResultInner> serviceCallback) throws IllegalArgumentException {
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
        Validator.validate(taskListSubtasksOptions, serviceCallback);
        String select = null;
        if (taskListSubtasksOptions != null) {
            select = taskListSubtasksOptions.select();
        }
        Integer timeout = null;
        if (taskListSubtasksOptions != null) {
            timeout = taskListSubtasksOptions.timeout();
        }
        String clientRequestId = null;
        if (taskListSubtasksOptions != null) {
            clientRequestId = taskListSubtasksOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (taskListSubtasksOptions != null) {
            returnClientRequestId = taskListSubtasksOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (taskListSubtasksOptions != null) {
            ocpDate = taskListSubtasksOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.listSubtasks(jobId, taskId, this.client.apiVersion(), this.client.acceptLanguage(), select, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<CloudTaskListSubtasksResultInner>(serviceCallback) {
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

    private ServiceResponseWithHeaders<CloudTaskListSubtasksResultInner, TaskListSubtasksHeadersInner> listSubtasksDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<CloudTaskListSubtasksResultInner, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<CloudTaskListSubtasksResultInner>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, TaskListSubtasksHeadersInner.class);
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
    public ServiceResponseWithHeaders<Void, TaskTerminateHeadersInner> terminate(String jobId, String taskId) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (taskId == null) {
            throw new IllegalArgumentException("Parameter taskId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final TaskTerminateOptionsInner taskTerminateOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        Call<ResponseBody> call = service.terminate(jobId, taskId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
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
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final TaskTerminateOptionsInner taskTerminateOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        Call<ResponseBody> call = service.terminate(jobId, taskId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
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
    public ServiceResponseWithHeaders<Void, TaskTerminateHeadersInner> terminate(String jobId, String taskId, TaskTerminateOptionsInner taskTerminateOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (jobId == null) {
            throw new IllegalArgumentException("Parameter jobId is required and cannot be null.");
        }
        if (taskId == null) {
            throw new IllegalArgumentException("Parameter taskId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(taskTerminateOptions);
        Integer timeout = null;
        if (taskTerminateOptions != null) {
            timeout = taskTerminateOptions.timeout();
        }
        String clientRequestId = null;
        if (taskTerminateOptions != null) {
            clientRequestId = taskTerminateOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (taskTerminateOptions != null) {
            returnClientRequestId = taskTerminateOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (taskTerminateOptions != null) {
            ocpDate = taskTerminateOptions.ocpDate();
        }
        String ifMatch = null;
        if (taskTerminateOptions != null) {
            ifMatch = taskTerminateOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (taskTerminateOptions != null) {
            ifNoneMatch = taskTerminateOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (taskTerminateOptions != null) {
            ifModifiedSince = taskTerminateOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (taskTerminateOptions != null) {
            ifUnmodifiedSince = taskTerminateOptions.ifUnmodifiedSince();
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
        Call<ResponseBody> call = service.terminate(jobId, taskId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
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
    public ServiceCall terminateAsync(String jobId, String taskId, TaskTerminateOptionsInner taskTerminateOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
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
        Validator.validate(taskTerminateOptions, serviceCallback);
        Integer timeout = null;
        if (taskTerminateOptions != null) {
            timeout = taskTerminateOptions.timeout();
        }
        String clientRequestId = null;
        if (taskTerminateOptions != null) {
            clientRequestId = taskTerminateOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (taskTerminateOptions != null) {
            returnClientRequestId = taskTerminateOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (taskTerminateOptions != null) {
            ocpDate = taskTerminateOptions.ocpDate();
        }
        String ifMatch = null;
        if (taskTerminateOptions != null) {
            ifMatch = taskTerminateOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (taskTerminateOptions != null) {
            ifNoneMatch = taskTerminateOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (taskTerminateOptions != null) {
            ifModifiedSince = taskTerminateOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (taskTerminateOptions != null) {
            ifUnmodifiedSince = taskTerminateOptions.ifUnmodifiedSince();
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
        Call<ResponseBody> call = service.terminate(jobId, taskId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
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

    private ServiceResponseWithHeaders<Void, TaskTerminateHeadersInner> terminateDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(204, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, TaskTerminateHeadersInner.class);
    }

    /**
     * Lists all of the tasks that are associated with the specified job.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;CloudTaskInner&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PageImpl<CloudTaskInner>, TaskListHeadersInner> listNext(final String nextPageLink) throws BatchErrorException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        final TaskListNextOptionsInner taskListNextOptions = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.acceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted);
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
    public ServiceCall listNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<CloudTaskInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        final TaskListNextOptionsInner taskListNextOptions = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.acceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted);
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<CloudTaskInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<CloudTaskInner>, TaskListHeadersInner> result = listNextDelegate(response);
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
     * @return the List&lt;CloudTaskInner&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PageImpl<CloudTaskInner>, TaskListHeadersInner> listNext(final String nextPageLink, final TaskListNextOptionsInner taskListNextOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        Validator.validate(taskListNextOptions);
        String clientRequestId = null;
        if (taskListNextOptions != null) {
            clientRequestId = taskListNextOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (taskListNextOptions != null) {
            returnClientRequestId = taskListNextOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (taskListNextOptions != null) {
            ocpDate = taskListNextOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.acceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted);
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
    public ServiceCall listNextAsync(final String nextPageLink, final TaskListNextOptionsInner taskListNextOptions, final ServiceCall serviceCall, final ListOperationCallback<CloudTaskInner> serviceCallback) throws IllegalArgumentException {
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
            clientRequestId = taskListNextOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (taskListNextOptions != null) {
            returnClientRequestId = taskListNextOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (taskListNextOptions != null) {
            ocpDate = taskListNextOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.acceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted);
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<CloudTaskInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<CloudTaskInner>, TaskListHeadersInner> result = listNextDelegate(response);
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

    private ServiceResponseWithHeaders<PageImpl<CloudTaskInner>, TaskListHeadersInner> listNextDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<CloudTaskInner>, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<CloudTaskInner>>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, TaskListHeadersInner.class);
    }

}
