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
import com.microsoft.rest.ServiceResponseWithHeaders;
import com.microsoft.rest.Validator;
import java.io.InputStream;
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
import retrofit2.http.Streaming;
import retrofit2.http.Url;
import retrofit2.Response;

/**
 * An instance of this class provides access to all the operations defined
 * in ComputeNodes.
 */
public final class ComputeNodesInner {
    /** The Retrofit service to perform REST calls. */
    private ComputeNodesService service;
    /** The service client containing this operation class. */
    private BatchServiceClientImpl client;

    /**
     * Initializes an instance of ComputeNodesInner.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public ComputeNodesInner(Retrofit retrofit, BatchServiceClientImpl client) {
        this.service = retrofit.create(ComputeNodesService.class);
        this.client = client;
    }

    /**
     * The interface defining all the services for ComputeNodes to be
     * used by Retrofit to perform actually REST calls.
     */
    interface ComputeNodesService {
        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @POST("pools/{poolId}/nodes/{nodeId}/users")
        Call<ResponseBody> addUser(@Path("poolId") String poolId, @Path("nodeId") String nodeId, @Body ComputeNodeUserInner user, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @HTTP(path = "pools/{poolId}/nodes/{nodeId}/users/{userName}", method = "DELETE", hasBody = true)
        Call<ResponseBody> deleteUser(@Path("poolId") String poolId, @Path("nodeId") String nodeId, @Path("userName") String userName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @PUT("pools/{poolId}/nodes/{nodeId}/users/{userName}")
        Call<ResponseBody> updateUser(@Path("poolId") String poolId, @Path("nodeId") String nodeId, @Path("userName") String userName, @Body NodeUpdateUserParameterInner nodeUpdateUserParameter, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @GET("pools/{poolId}/nodes/{nodeId}")
        Call<ResponseBody> get(@Path("poolId") String poolId, @Path("nodeId") String nodeId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("$select") String select, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @POST("pools/{poolId}/nodes/{nodeId}/reboot")
        Call<ResponseBody> reboot(@Path("poolId") String poolId, @Path("nodeId") String nodeId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Body NodeRebootParameter nodeRebootParameter);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @POST("pools/{poolId}/nodes/{nodeId}/reimage")
        Call<ResponseBody> reimage(@Path("poolId") String poolId, @Path("nodeId") String nodeId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Body NodeReimageParameter nodeReimageParameter);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @POST("pools/{poolId}/nodes/{nodeId}/disablescheduling")
        Call<ResponseBody> disableScheduling(@Path("poolId") String poolId, @Path("nodeId") String nodeId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Body NodeDisableSchedulingParameter nodeDisableSchedulingParameter);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @POST("pools/{poolId}/nodes/{nodeId}/enablescheduling")
        Call<ResponseBody> enableScheduling(@Path("poolId") String poolId, @Path("nodeId") String nodeId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @GET("pools/{poolId}/nodes/{nodeId}/remoteloginsettings")
        Call<ResponseBody> getRemoteLoginSettings(@Path("poolId") String poolId, @Path("nodeId") String nodeId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @GET("pools/{poolId}/nodes/{nodeId}/rdp")
        @Streaming
        Call<ResponseBody> getRemoteDesktop(@Path("poolId") String poolId, @Path("nodeId") String nodeId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @GET("pools/{poolId}/nodes")
        Call<ResponseBody> list(@Path("poolId") String poolId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("$filter") String filter, @Query("$select") String select, @Query("maxresults") Integer maxResults, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @GET
        Call<ResponseBody> listNext(@Url String nextPageLink, @Header("accept-language") String acceptLanguage, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate);

    }

    /**
     * Adds a user account to the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the machine on which you want to create a user account.
     * @param user Specifies the user account to be created.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, ComputeNodeAddUserHeadersInner> addUser(String poolId, String nodeId, ComputeNodeUserInner user) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (nodeId == null) {
            throw new IllegalArgumentException("Parameter nodeId is required and cannot be null.");
        }
        if (user == null) {
            throw new IllegalArgumentException("Parameter user is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(user);
        final ComputeNodeAddUserOptionsInner computeNodeAddUserOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.addUser(poolId, nodeId, user, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        return addUserDelegate(call.execute());
    }

    /**
     * Adds a user account to the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the machine on which you want to create a user account.
     * @param user Specifies the user account to be created.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall addUserAsync(String poolId, String nodeId, ComputeNodeUserInner user, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
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
        if (user == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter user is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(user, serviceCallback);
        final ComputeNodeAddUserOptionsInner computeNodeAddUserOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.addUser(poolId, nodeId, user, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(addUserDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Adds a user account to the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the machine on which you want to create a user account.
     * @param user Specifies the user account to be created.
     * @param computeNodeAddUserOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, ComputeNodeAddUserHeadersInner> addUser(String poolId, String nodeId, ComputeNodeUserInner user, ComputeNodeAddUserOptionsInner computeNodeAddUserOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (nodeId == null) {
            throw new IllegalArgumentException("Parameter nodeId is required and cannot be null.");
        }
        if (user == null) {
            throw new IllegalArgumentException("Parameter user is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(user);
        Validator.validate(computeNodeAddUserOptions);
        Integer timeout = null;
        if (computeNodeAddUserOptions != null) {
            timeout = computeNodeAddUserOptions.timeout();
        }
        String clientRequestId = null;
        if (computeNodeAddUserOptions != null) {
            clientRequestId = computeNodeAddUserOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (computeNodeAddUserOptions != null) {
            returnClientRequestId = computeNodeAddUserOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (computeNodeAddUserOptions != null) {
            ocpDate = computeNodeAddUserOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.addUser(poolId, nodeId, user, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        return addUserDelegate(call.execute());
    }

    /**
     * Adds a user account to the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the machine on which you want to create a user account.
     * @param user Specifies the user account to be created.
     * @param computeNodeAddUserOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall addUserAsync(String poolId, String nodeId, ComputeNodeUserInner user, ComputeNodeAddUserOptionsInner computeNodeAddUserOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
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
        if (user == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter user is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(user, serviceCallback);
        Validator.validate(computeNodeAddUserOptions, serviceCallback);
        Integer timeout = null;
        if (computeNodeAddUserOptions != null) {
            timeout = computeNodeAddUserOptions.timeout();
        }
        String clientRequestId = null;
        if (computeNodeAddUserOptions != null) {
            clientRequestId = computeNodeAddUserOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (computeNodeAddUserOptions != null) {
            returnClientRequestId = computeNodeAddUserOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (computeNodeAddUserOptions != null) {
            ocpDate = computeNodeAddUserOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.addUser(poolId, nodeId, user, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(addUserDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<Void, ComputeNodeAddUserHeadersInner> addUserDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(201, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, ComputeNodeAddUserHeadersInner.class);
    }

    /**
     * Deletes a user account from the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the machine on which you want to delete a user account.
     * @param userName The name of the user account to delete.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, ComputeNodeDeleteUserHeadersInner> deleteUser(String poolId, String nodeId, String userName) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (nodeId == null) {
            throw new IllegalArgumentException("Parameter nodeId is required and cannot be null.");
        }
        if (userName == null) {
            throw new IllegalArgumentException("Parameter userName is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final ComputeNodeDeleteUserOptionsInner computeNodeDeleteUserOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.deleteUser(poolId, nodeId, userName, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        return deleteUserDelegate(call.execute());
    }

    /**
     * Deletes a user account from the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the machine on which you want to delete a user account.
     * @param userName The name of the user account to delete.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall deleteUserAsync(String poolId, String nodeId, String userName, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
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
        if (userName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter userName is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final ComputeNodeDeleteUserOptionsInner computeNodeDeleteUserOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.deleteUser(poolId, nodeId, userName, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(deleteUserDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Deletes a user account from the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the machine on which you want to delete a user account.
     * @param userName The name of the user account to delete.
     * @param computeNodeDeleteUserOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, ComputeNodeDeleteUserHeadersInner> deleteUser(String poolId, String nodeId, String userName, ComputeNodeDeleteUserOptionsInner computeNodeDeleteUserOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (nodeId == null) {
            throw new IllegalArgumentException("Parameter nodeId is required and cannot be null.");
        }
        if (userName == null) {
            throw new IllegalArgumentException("Parameter userName is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(computeNodeDeleteUserOptions);
        Integer timeout = null;
        if (computeNodeDeleteUserOptions != null) {
            timeout = computeNodeDeleteUserOptions.timeout();
        }
        String clientRequestId = null;
        if (computeNodeDeleteUserOptions != null) {
            clientRequestId = computeNodeDeleteUserOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (computeNodeDeleteUserOptions != null) {
            returnClientRequestId = computeNodeDeleteUserOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (computeNodeDeleteUserOptions != null) {
            ocpDate = computeNodeDeleteUserOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.deleteUser(poolId, nodeId, userName, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        return deleteUserDelegate(call.execute());
    }

    /**
     * Deletes a user account from the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the machine on which you want to delete a user account.
     * @param userName The name of the user account to delete.
     * @param computeNodeDeleteUserOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall deleteUserAsync(String poolId, String nodeId, String userName, ComputeNodeDeleteUserOptionsInner computeNodeDeleteUserOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
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
        if (userName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter userName is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(computeNodeDeleteUserOptions, serviceCallback);
        Integer timeout = null;
        if (computeNodeDeleteUserOptions != null) {
            timeout = computeNodeDeleteUserOptions.timeout();
        }
        String clientRequestId = null;
        if (computeNodeDeleteUserOptions != null) {
            clientRequestId = computeNodeDeleteUserOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (computeNodeDeleteUserOptions != null) {
            returnClientRequestId = computeNodeDeleteUserOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (computeNodeDeleteUserOptions != null) {
            ocpDate = computeNodeDeleteUserOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.deleteUser(poolId, nodeId, userName, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(deleteUserDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<Void, ComputeNodeDeleteUserHeadersInner> deleteUserDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, ComputeNodeDeleteUserHeadersInner.class);
    }

    /**
     * Updates the password or expiration time of a user account on the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the machine on which you want to update a user account.
     * @param userName The name of the user account to update.
     * @param nodeUpdateUserParameter The parameters for the request.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, ComputeNodeUpdateUserHeadersInner> updateUser(String poolId, String nodeId, String userName, NodeUpdateUserParameterInner nodeUpdateUserParameter) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (nodeId == null) {
            throw new IllegalArgumentException("Parameter nodeId is required and cannot be null.");
        }
        if (userName == null) {
            throw new IllegalArgumentException("Parameter userName is required and cannot be null.");
        }
        if (nodeUpdateUserParameter == null) {
            throw new IllegalArgumentException("Parameter nodeUpdateUserParameter is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(nodeUpdateUserParameter);
        final ComputeNodeUpdateUserOptionsInner computeNodeUpdateUserOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.updateUser(poolId, nodeId, userName, nodeUpdateUserParameter, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        return updateUserDelegate(call.execute());
    }

    /**
     * Updates the password or expiration time of a user account on the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the machine on which you want to update a user account.
     * @param userName The name of the user account to update.
     * @param nodeUpdateUserParameter The parameters for the request.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall updateUserAsync(String poolId, String nodeId, String userName, NodeUpdateUserParameterInner nodeUpdateUserParameter, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
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
        if (userName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter userName is required and cannot be null."));
            return null;
        }
        if (nodeUpdateUserParameter == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nodeUpdateUserParameter is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(nodeUpdateUserParameter, serviceCallback);
        final ComputeNodeUpdateUserOptionsInner computeNodeUpdateUserOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.updateUser(poolId, nodeId, userName, nodeUpdateUserParameter, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(updateUserDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Updates the password or expiration time of a user account on the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the machine on which you want to update a user account.
     * @param userName The name of the user account to update.
     * @param nodeUpdateUserParameter The parameters for the request.
     * @param computeNodeUpdateUserOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, ComputeNodeUpdateUserHeadersInner> updateUser(String poolId, String nodeId, String userName, NodeUpdateUserParameterInner nodeUpdateUserParameter, ComputeNodeUpdateUserOptionsInner computeNodeUpdateUserOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (nodeId == null) {
            throw new IllegalArgumentException("Parameter nodeId is required and cannot be null.");
        }
        if (userName == null) {
            throw new IllegalArgumentException("Parameter userName is required and cannot be null.");
        }
        if (nodeUpdateUserParameter == null) {
            throw new IllegalArgumentException("Parameter nodeUpdateUserParameter is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(nodeUpdateUserParameter);
        Validator.validate(computeNodeUpdateUserOptions);
        Integer timeout = null;
        if (computeNodeUpdateUserOptions != null) {
            timeout = computeNodeUpdateUserOptions.timeout();
        }
        String clientRequestId = null;
        if (computeNodeUpdateUserOptions != null) {
            clientRequestId = computeNodeUpdateUserOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (computeNodeUpdateUserOptions != null) {
            returnClientRequestId = computeNodeUpdateUserOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (computeNodeUpdateUserOptions != null) {
            ocpDate = computeNodeUpdateUserOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.updateUser(poolId, nodeId, userName, nodeUpdateUserParameter, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        return updateUserDelegate(call.execute());
    }

    /**
     * Updates the password or expiration time of a user account on the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the machine on which you want to update a user account.
     * @param userName The name of the user account to update.
     * @param nodeUpdateUserParameter The parameters for the request.
     * @param computeNodeUpdateUserOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall updateUserAsync(String poolId, String nodeId, String userName, NodeUpdateUserParameterInner nodeUpdateUserParameter, ComputeNodeUpdateUserOptionsInner computeNodeUpdateUserOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
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
        if (userName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter userName is required and cannot be null."));
            return null;
        }
        if (nodeUpdateUserParameter == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nodeUpdateUserParameter is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(nodeUpdateUserParameter, serviceCallback);
        Validator.validate(computeNodeUpdateUserOptions, serviceCallback);
        Integer timeout = null;
        if (computeNodeUpdateUserOptions != null) {
            timeout = computeNodeUpdateUserOptions.timeout();
        }
        String clientRequestId = null;
        if (computeNodeUpdateUserOptions != null) {
            clientRequestId = computeNodeUpdateUserOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (computeNodeUpdateUserOptions != null) {
            returnClientRequestId = computeNodeUpdateUserOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (computeNodeUpdateUserOptions != null) {
            ocpDate = computeNodeUpdateUserOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.updateUser(poolId, nodeId, userName, nodeUpdateUserParameter, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(updateUserDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<Void, ComputeNodeUpdateUserHeadersInner> updateUserDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, ComputeNodeUpdateUserHeadersInner.class);
    }

    /**
     * Gets information about the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that you want to get information about.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the ComputeNodeInner object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<ComputeNodeInner, ComputeNodeGetHeadersInner> get(String poolId, String nodeId) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (nodeId == null) {
            throw new IllegalArgumentException("Parameter nodeId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final ComputeNodeGetOptionsInner computeNodeGetOptions = null;
        String select = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.get(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), select, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        return getDelegate(call.execute());
    }

    /**
     * Gets information about the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that you want to get information about.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getAsync(String poolId, String nodeId, final ServiceCallback<ComputeNodeInner> serviceCallback) throws IllegalArgumentException {
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
        final ComputeNodeGetOptionsInner computeNodeGetOptions = null;
        String select = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.get(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), select, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<ComputeNodeInner>(serviceCallback) {
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
     * Gets information about the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that you want to get information about.
     * @param computeNodeGetOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the ComputeNodeInner object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<ComputeNodeInner, ComputeNodeGetHeadersInner> get(String poolId, String nodeId, ComputeNodeGetOptionsInner computeNodeGetOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (nodeId == null) {
            throw new IllegalArgumentException("Parameter nodeId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(computeNodeGetOptions);
        String select = null;
        if (computeNodeGetOptions != null) {
            select = computeNodeGetOptions.select();
        }
        Integer timeout = null;
        if (computeNodeGetOptions != null) {
            timeout = computeNodeGetOptions.timeout();
        }
        String clientRequestId = null;
        if (computeNodeGetOptions != null) {
            clientRequestId = computeNodeGetOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (computeNodeGetOptions != null) {
            returnClientRequestId = computeNodeGetOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (computeNodeGetOptions != null) {
            ocpDate = computeNodeGetOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.get(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), select, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        return getDelegate(call.execute());
    }

    /**
     * Gets information about the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that you want to get information about.
     * @param computeNodeGetOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getAsync(String poolId, String nodeId, ComputeNodeGetOptionsInner computeNodeGetOptions, final ServiceCallback<ComputeNodeInner> serviceCallback) throws IllegalArgumentException {
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
        Validator.validate(computeNodeGetOptions, serviceCallback);
        String select = null;
        if (computeNodeGetOptions != null) {
            select = computeNodeGetOptions.select();
        }
        Integer timeout = null;
        if (computeNodeGetOptions != null) {
            timeout = computeNodeGetOptions.timeout();
        }
        String clientRequestId = null;
        if (computeNodeGetOptions != null) {
            clientRequestId = computeNodeGetOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (computeNodeGetOptions != null) {
            returnClientRequestId = computeNodeGetOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (computeNodeGetOptions != null) {
            ocpDate = computeNodeGetOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.get(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), select, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<ComputeNodeInner>(serviceCallback) {
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

    private ServiceResponseWithHeaders<ComputeNodeInner, ComputeNodeGetHeadersInner> getDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<ComputeNodeInner, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<ComputeNodeInner>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, ComputeNodeGetHeadersInner.class);
    }

    /**
     * Restarts the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that you want to restart.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, ComputeNodeRebootHeadersInner> reboot(String poolId, String nodeId) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (nodeId == null) {
            throw new IllegalArgumentException("Parameter nodeId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final ComputeNodeRebootOption nodeRebootOption = null;
        final ComputeNodeRebootOptionsInner computeNodeRebootOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        NodeRebootParameter nodeRebootParameter = new NodeRebootParameter();
        nodeRebootParameter = null;
        Call<ResponseBody> call = service.reboot(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, nodeRebootParameter);
        return rebootDelegate(call.execute());
    }

    /**
     * Restarts the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that you want to restart.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall rebootAsync(String poolId, String nodeId, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
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
        final ComputeNodeRebootOption nodeRebootOption = null;
        final ComputeNodeRebootOptionsInner computeNodeRebootOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        NodeRebootParameter nodeRebootParameter = new NodeRebootParameter();
        nodeRebootParameter = null;
        Call<ResponseBody> call = service.reboot(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, nodeRebootParameter);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(rebootDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Restarts the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that you want to restart.
     * @param nodeRebootOption Sets when to reboot the compute node and what to do with currently running tasks. The default value is requeue. Possible values include: 'requeue', 'terminate', 'taskcompletion', 'retaineddata'
     * @param computeNodeRebootOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, ComputeNodeRebootHeadersInner> reboot(String poolId, String nodeId, ComputeNodeRebootOption nodeRebootOption, ComputeNodeRebootOptionsInner computeNodeRebootOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (nodeId == null) {
            throw new IllegalArgumentException("Parameter nodeId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(computeNodeRebootOptions);
        Integer timeout = null;
        if (computeNodeRebootOptions != null) {
            timeout = computeNodeRebootOptions.timeout();
        }
        String clientRequestId = null;
        if (computeNodeRebootOptions != null) {
            clientRequestId = computeNodeRebootOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (computeNodeRebootOptions != null) {
            returnClientRequestId = computeNodeRebootOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (computeNodeRebootOptions != null) {
            ocpDate = computeNodeRebootOptions.ocpDate();
        }
        NodeRebootParameter nodeRebootParameter = null;
        if (nodeRebootOption != null) {
            nodeRebootParameter = new NodeRebootParameter();
            nodeRebootParameter.setNodeRebootOption(nodeRebootOption);
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.reboot(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, nodeRebootParameter);
        return rebootDelegate(call.execute());
    }

    /**
     * Restarts the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that you want to restart.
     * @param nodeRebootOption Sets when to reboot the compute node and what to do with currently running tasks. The default value is requeue. Possible values include: 'requeue', 'terminate', 'taskcompletion', 'retaineddata'
     * @param computeNodeRebootOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall rebootAsync(String poolId, String nodeId, ComputeNodeRebootOption nodeRebootOption, ComputeNodeRebootOptionsInner computeNodeRebootOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
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
        Validator.validate(computeNodeRebootOptions, serviceCallback);
        Integer timeout = null;
        if (computeNodeRebootOptions != null) {
            timeout = computeNodeRebootOptions.timeout();
        }
        String clientRequestId = null;
        if (computeNodeRebootOptions != null) {
            clientRequestId = computeNodeRebootOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (computeNodeRebootOptions != null) {
            returnClientRequestId = computeNodeRebootOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (computeNodeRebootOptions != null) {
            ocpDate = computeNodeRebootOptions.ocpDate();
        }
        NodeRebootParameter nodeRebootParameter = null;
        if (nodeRebootOption != null) {
            nodeRebootParameter = new NodeRebootParameter();
            nodeRebootParameter.setNodeRebootOption(nodeRebootOption);
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.reboot(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, nodeRebootParameter);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(rebootDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<Void, ComputeNodeRebootHeadersInner> rebootDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(202, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, ComputeNodeRebootHeadersInner.class);
    }

    /**
     * Reinstalls the operating system on the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that you want to restart.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, ComputeNodeReimageHeadersInner> reimage(String poolId, String nodeId) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (nodeId == null) {
            throw new IllegalArgumentException("Parameter nodeId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final ComputeNodeReimageOption nodeReimageOption = null;
        final ComputeNodeReimageOptionsInner computeNodeReimageOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        NodeReimageParameter nodeReimageParameter = new NodeReimageParameter();
        nodeReimageParameter = null;
        Call<ResponseBody> call = service.reimage(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, nodeReimageParameter);
        return reimageDelegate(call.execute());
    }

    /**
     * Reinstalls the operating system on the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that you want to restart.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall reimageAsync(String poolId, String nodeId, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
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
        final ComputeNodeReimageOption nodeReimageOption = null;
        final ComputeNodeReimageOptionsInner computeNodeReimageOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        NodeReimageParameter nodeReimageParameter = new NodeReimageParameter();
        nodeReimageParameter = null;
        Call<ResponseBody> call = service.reimage(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, nodeReimageParameter);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(reimageDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Reinstalls the operating system on the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that you want to restart.
     * @param nodeReimageOption Sets when to reimage the compute node and what to do with currently running tasks. The default value is requeue. Possible values include: 'requeue', 'terminate', 'taskcompletion', 'retaineddata'
     * @param computeNodeReimageOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, ComputeNodeReimageHeadersInner> reimage(String poolId, String nodeId, ComputeNodeReimageOption nodeReimageOption, ComputeNodeReimageOptionsInner computeNodeReimageOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (nodeId == null) {
            throw new IllegalArgumentException("Parameter nodeId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(computeNodeReimageOptions);
        Integer timeout = null;
        if (computeNodeReimageOptions != null) {
            timeout = computeNodeReimageOptions.timeout();
        }
        String clientRequestId = null;
        if (computeNodeReimageOptions != null) {
            clientRequestId = computeNodeReimageOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (computeNodeReimageOptions != null) {
            returnClientRequestId = computeNodeReimageOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (computeNodeReimageOptions != null) {
            ocpDate = computeNodeReimageOptions.ocpDate();
        }
        NodeReimageParameter nodeReimageParameter = null;
        if (nodeReimageOption != null) {
            nodeReimageParameter = new NodeReimageParameter();
            nodeReimageParameter.setNodeReimageOption(nodeReimageOption);
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.reimage(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, nodeReimageParameter);
        return reimageDelegate(call.execute());
    }

    /**
     * Reinstalls the operating system on the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that you want to restart.
     * @param nodeReimageOption Sets when to reimage the compute node and what to do with currently running tasks. The default value is requeue. Possible values include: 'requeue', 'terminate', 'taskcompletion', 'retaineddata'
     * @param computeNodeReimageOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall reimageAsync(String poolId, String nodeId, ComputeNodeReimageOption nodeReimageOption, ComputeNodeReimageOptionsInner computeNodeReimageOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
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
        Validator.validate(computeNodeReimageOptions, serviceCallback);
        Integer timeout = null;
        if (computeNodeReimageOptions != null) {
            timeout = computeNodeReimageOptions.timeout();
        }
        String clientRequestId = null;
        if (computeNodeReimageOptions != null) {
            clientRequestId = computeNodeReimageOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (computeNodeReimageOptions != null) {
            returnClientRequestId = computeNodeReimageOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (computeNodeReimageOptions != null) {
            ocpDate = computeNodeReimageOptions.ocpDate();
        }
        NodeReimageParameter nodeReimageParameter = null;
        if (nodeReimageOption != null) {
            nodeReimageParameter = new NodeReimageParameter();
            nodeReimageParameter.setNodeReimageOption(nodeReimageOption);
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.reimage(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, nodeReimageParameter);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(reimageDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<Void, ComputeNodeReimageHeadersInner> reimageDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(202, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, ComputeNodeReimageHeadersInner.class);
    }

    /**
     * Disable task scheduling of the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that you want to disable task scheduling.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, ComputeNodeDisableSchedulingHeadersInner> disableScheduling(String poolId, String nodeId) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (nodeId == null) {
            throw new IllegalArgumentException("Parameter nodeId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final DisableComputeNodeSchedulingOption nodeDisableSchedulingOption = null;
        final ComputeNodeDisableSchedulingOptionsInner computeNodeDisableSchedulingOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        NodeDisableSchedulingParameter nodeDisableSchedulingParameter = new NodeDisableSchedulingParameter();
        nodeDisableSchedulingParameter = null;
        Call<ResponseBody> call = service.disableScheduling(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, nodeDisableSchedulingParameter);
        return disableSchedulingDelegate(call.execute());
    }

    /**
     * Disable task scheduling of the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that you want to disable task scheduling.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall disableSchedulingAsync(String poolId, String nodeId, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
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
        final DisableComputeNodeSchedulingOption nodeDisableSchedulingOption = null;
        final ComputeNodeDisableSchedulingOptionsInner computeNodeDisableSchedulingOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        NodeDisableSchedulingParameter nodeDisableSchedulingParameter = new NodeDisableSchedulingParameter();
        nodeDisableSchedulingParameter = null;
        Call<ResponseBody> call = service.disableScheduling(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, nodeDisableSchedulingParameter);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(disableSchedulingDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Disable task scheduling of the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that you want to disable task scheduling.
     * @param nodeDisableSchedulingOption Gets or sets what to do with currently running tasks when disable task scheduling on the compute node. The default value is requeue. Possible values include: 'requeue', 'terminate', 'taskcompletion'
     * @param computeNodeDisableSchedulingOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, ComputeNodeDisableSchedulingHeadersInner> disableScheduling(String poolId, String nodeId, DisableComputeNodeSchedulingOption nodeDisableSchedulingOption, ComputeNodeDisableSchedulingOptionsInner computeNodeDisableSchedulingOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (nodeId == null) {
            throw new IllegalArgumentException("Parameter nodeId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(computeNodeDisableSchedulingOptions);
        Integer timeout = null;
        if (computeNodeDisableSchedulingOptions != null) {
            timeout = computeNodeDisableSchedulingOptions.timeout();
        }
        String clientRequestId = null;
        if (computeNodeDisableSchedulingOptions != null) {
            clientRequestId = computeNodeDisableSchedulingOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (computeNodeDisableSchedulingOptions != null) {
            returnClientRequestId = computeNodeDisableSchedulingOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (computeNodeDisableSchedulingOptions != null) {
            ocpDate = computeNodeDisableSchedulingOptions.ocpDate();
        }
        NodeDisableSchedulingParameter nodeDisableSchedulingParameter = null;
        if (nodeDisableSchedulingOption != null) {
            nodeDisableSchedulingParameter = new NodeDisableSchedulingParameter();
            nodeDisableSchedulingParameter.setNodeDisableSchedulingOption(nodeDisableSchedulingOption);
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.disableScheduling(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, nodeDisableSchedulingParameter);
        return disableSchedulingDelegate(call.execute());
    }

    /**
     * Disable task scheduling of the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that you want to disable task scheduling.
     * @param nodeDisableSchedulingOption Gets or sets what to do with currently running tasks when disable task scheduling on the compute node. The default value is requeue. Possible values include: 'requeue', 'terminate', 'taskcompletion'
     * @param computeNodeDisableSchedulingOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall disableSchedulingAsync(String poolId, String nodeId, DisableComputeNodeSchedulingOption nodeDisableSchedulingOption, ComputeNodeDisableSchedulingOptionsInner computeNodeDisableSchedulingOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
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
        Validator.validate(computeNodeDisableSchedulingOptions, serviceCallback);
        Integer timeout = null;
        if (computeNodeDisableSchedulingOptions != null) {
            timeout = computeNodeDisableSchedulingOptions.timeout();
        }
        String clientRequestId = null;
        if (computeNodeDisableSchedulingOptions != null) {
            clientRequestId = computeNodeDisableSchedulingOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (computeNodeDisableSchedulingOptions != null) {
            returnClientRequestId = computeNodeDisableSchedulingOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (computeNodeDisableSchedulingOptions != null) {
            ocpDate = computeNodeDisableSchedulingOptions.ocpDate();
        }
        NodeDisableSchedulingParameter nodeDisableSchedulingParameter = null;
        if (nodeDisableSchedulingOption != null) {
            nodeDisableSchedulingParameter = new NodeDisableSchedulingParameter();
            nodeDisableSchedulingParameter.setNodeDisableSchedulingOption(nodeDisableSchedulingOption);
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.disableScheduling(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, nodeDisableSchedulingParameter);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(disableSchedulingDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<Void, ComputeNodeDisableSchedulingHeadersInner> disableSchedulingDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, ComputeNodeDisableSchedulingHeadersInner.class);
    }

    /**
     * Enable task scheduling of the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that you want to enable task scheduling.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, ComputeNodeEnableSchedulingHeadersInner> enableScheduling(String poolId, String nodeId) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (nodeId == null) {
            throw new IllegalArgumentException("Parameter nodeId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final ComputeNodeEnableSchedulingOptionsInner computeNodeEnableSchedulingOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.enableScheduling(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        return enableSchedulingDelegate(call.execute());
    }

    /**
     * Enable task scheduling of the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that you want to enable task scheduling.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall enableSchedulingAsync(String poolId, String nodeId, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
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
        final ComputeNodeEnableSchedulingOptionsInner computeNodeEnableSchedulingOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.enableScheduling(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(enableSchedulingDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Enable task scheduling of the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that you want to enable task scheduling.
     * @param computeNodeEnableSchedulingOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, ComputeNodeEnableSchedulingHeadersInner> enableScheduling(String poolId, String nodeId, ComputeNodeEnableSchedulingOptionsInner computeNodeEnableSchedulingOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (nodeId == null) {
            throw new IllegalArgumentException("Parameter nodeId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(computeNodeEnableSchedulingOptions);
        Integer timeout = null;
        if (computeNodeEnableSchedulingOptions != null) {
            timeout = computeNodeEnableSchedulingOptions.timeout();
        }
        String clientRequestId = null;
        if (computeNodeEnableSchedulingOptions != null) {
            clientRequestId = computeNodeEnableSchedulingOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (computeNodeEnableSchedulingOptions != null) {
            returnClientRequestId = computeNodeEnableSchedulingOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (computeNodeEnableSchedulingOptions != null) {
            ocpDate = computeNodeEnableSchedulingOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.enableScheduling(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        return enableSchedulingDelegate(call.execute());
    }

    /**
     * Enable task scheduling of the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that you want to enable task scheduling.
     * @param computeNodeEnableSchedulingOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall enableSchedulingAsync(String poolId, String nodeId, ComputeNodeEnableSchedulingOptionsInner computeNodeEnableSchedulingOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
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
        Validator.validate(computeNodeEnableSchedulingOptions, serviceCallback);
        Integer timeout = null;
        if (computeNodeEnableSchedulingOptions != null) {
            timeout = computeNodeEnableSchedulingOptions.timeout();
        }
        String clientRequestId = null;
        if (computeNodeEnableSchedulingOptions != null) {
            clientRequestId = computeNodeEnableSchedulingOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (computeNodeEnableSchedulingOptions != null) {
            returnClientRequestId = computeNodeEnableSchedulingOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (computeNodeEnableSchedulingOptions != null) {
            ocpDate = computeNodeEnableSchedulingOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.enableScheduling(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(enableSchedulingDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<Void, ComputeNodeEnableSchedulingHeadersInner> enableSchedulingDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, ComputeNodeEnableSchedulingHeadersInner.class);
    }

    /**
     * Gets the settings required for remote login to a compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node for which to obtain the remote login settings.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the ComputeNodeGetRemoteLoginSettingsResultInner object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<ComputeNodeGetRemoteLoginSettingsResultInner, ComputeNodeGetRemoteLoginSettingsHeadersInner> getRemoteLoginSettings(String poolId, String nodeId) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (nodeId == null) {
            throw new IllegalArgumentException("Parameter nodeId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final ComputeNodeGetRemoteLoginSettingsOptionsInner computeNodeGetRemoteLoginSettingsOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.getRemoteLoginSettings(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        return getRemoteLoginSettingsDelegate(call.execute());
    }

    /**
     * Gets the settings required for remote login to a compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node for which to obtain the remote login settings.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getRemoteLoginSettingsAsync(String poolId, String nodeId, final ServiceCallback<ComputeNodeGetRemoteLoginSettingsResultInner> serviceCallback) throws IllegalArgumentException {
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
        final ComputeNodeGetRemoteLoginSettingsOptionsInner computeNodeGetRemoteLoginSettingsOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.getRemoteLoginSettings(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<ComputeNodeGetRemoteLoginSettingsResultInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getRemoteLoginSettingsDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Gets the settings required for remote login to a compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node for which to obtain the remote login settings.
     * @param computeNodeGetRemoteLoginSettingsOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the ComputeNodeGetRemoteLoginSettingsResultInner object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<ComputeNodeGetRemoteLoginSettingsResultInner, ComputeNodeGetRemoteLoginSettingsHeadersInner> getRemoteLoginSettings(String poolId, String nodeId, ComputeNodeGetRemoteLoginSettingsOptionsInner computeNodeGetRemoteLoginSettingsOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (nodeId == null) {
            throw new IllegalArgumentException("Parameter nodeId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(computeNodeGetRemoteLoginSettingsOptions);
        Integer timeout = null;
        if (computeNodeGetRemoteLoginSettingsOptions != null) {
            timeout = computeNodeGetRemoteLoginSettingsOptions.timeout();
        }
        String clientRequestId = null;
        if (computeNodeGetRemoteLoginSettingsOptions != null) {
            clientRequestId = computeNodeGetRemoteLoginSettingsOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (computeNodeGetRemoteLoginSettingsOptions != null) {
            returnClientRequestId = computeNodeGetRemoteLoginSettingsOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (computeNodeGetRemoteLoginSettingsOptions != null) {
            ocpDate = computeNodeGetRemoteLoginSettingsOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.getRemoteLoginSettings(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        return getRemoteLoginSettingsDelegate(call.execute());
    }

    /**
     * Gets the settings required for remote login to a compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node for which to obtain the remote login settings.
     * @param computeNodeGetRemoteLoginSettingsOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getRemoteLoginSettingsAsync(String poolId, String nodeId, ComputeNodeGetRemoteLoginSettingsOptionsInner computeNodeGetRemoteLoginSettingsOptions, final ServiceCallback<ComputeNodeGetRemoteLoginSettingsResultInner> serviceCallback) throws IllegalArgumentException {
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
        Validator.validate(computeNodeGetRemoteLoginSettingsOptions, serviceCallback);
        Integer timeout = null;
        if (computeNodeGetRemoteLoginSettingsOptions != null) {
            timeout = computeNodeGetRemoteLoginSettingsOptions.timeout();
        }
        String clientRequestId = null;
        if (computeNodeGetRemoteLoginSettingsOptions != null) {
            clientRequestId = computeNodeGetRemoteLoginSettingsOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (computeNodeGetRemoteLoginSettingsOptions != null) {
            returnClientRequestId = computeNodeGetRemoteLoginSettingsOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (computeNodeGetRemoteLoginSettingsOptions != null) {
            ocpDate = computeNodeGetRemoteLoginSettingsOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.getRemoteLoginSettings(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<ComputeNodeGetRemoteLoginSettingsResultInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getRemoteLoginSettingsDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<ComputeNodeGetRemoteLoginSettingsResultInner, ComputeNodeGetRemoteLoginSettingsHeadersInner> getRemoteLoginSettingsDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<ComputeNodeGetRemoteLoginSettingsResultInner, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<ComputeNodeGetRemoteLoginSettingsResultInner>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, ComputeNodeGetRemoteLoginSettingsHeadersInner.class);
    }

    /**
     * Gets the Remote Desktop Protocol file for the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node for which you want to get the Remote Desktop Protocol file.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the InputStream object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<InputStream, ComputeNodeGetRemoteDesktopHeadersInner> getRemoteDesktop(String poolId, String nodeId) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (nodeId == null) {
            throw new IllegalArgumentException("Parameter nodeId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final ComputeNodeGetRemoteDesktopOptionsInner computeNodeGetRemoteDesktopOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.getRemoteDesktop(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        return getRemoteDesktopDelegate(call.execute());
    }

    /**
     * Gets the Remote Desktop Protocol file for the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node for which you want to get the Remote Desktop Protocol file.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getRemoteDesktopAsync(String poolId, String nodeId, final ServiceCallback<InputStream> serviceCallback) throws IllegalArgumentException {
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
        final ComputeNodeGetRemoteDesktopOptionsInner computeNodeGetRemoteDesktopOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.getRemoteDesktop(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<InputStream>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getRemoteDesktopDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Gets the Remote Desktop Protocol file for the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node for which you want to get the Remote Desktop Protocol file.
     * @param computeNodeGetRemoteDesktopOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the InputStream object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<InputStream, ComputeNodeGetRemoteDesktopHeadersInner> getRemoteDesktop(String poolId, String nodeId, ComputeNodeGetRemoteDesktopOptionsInner computeNodeGetRemoteDesktopOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (nodeId == null) {
            throw new IllegalArgumentException("Parameter nodeId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(computeNodeGetRemoteDesktopOptions);
        Integer timeout = null;
        if (computeNodeGetRemoteDesktopOptions != null) {
            timeout = computeNodeGetRemoteDesktopOptions.timeout();
        }
        String clientRequestId = null;
        if (computeNodeGetRemoteDesktopOptions != null) {
            clientRequestId = computeNodeGetRemoteDesktopOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (computeNodeGetRemoteDesktopOptions != null) {
            returnClientRequestId = computeNodeGetRemoteDesktopOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (computeNodeGetRemoteDesktopOptions != null) {
            ocpDate = computeNodeGetRemoteDesktopOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.getRemoteDesktop(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        return getRemoteDesktopDelegate(call.execute());
    }

    /**
     * Gets the Remote Desktop Protocol file for the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node for which you want to get the Remote Desktop Protocol file.
     * @param computeNodeGetRemoteDesktopOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getRemoteDesktopAsync(String poolId, String nodeId, ComputeNodeGetRemoteDesktopOptionsInner computeNodeGetRemoteDesktopOptions, final ServiceCallback<InputStream> serviceCallback) throws IllegalArgumentException {
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
        Validator.validate(computeNodeGetRemoteDesktopOptions, serviceCallback);
        Integer timeout = null;
        if (computeNodeGetRemoteDesktopOptions != null) {
            timeout = computeNodeGetRemoteDesktopOptions.timeout();
        }
        String clientRequestId = null;
        if (computeNodeGetRemoteDesktopOptions != null) {
            clientRequestId = computeNodeGetRemoteDesktopOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (computeNodeGetRemoteDesktopOptions != null) {
            returnClientRequestId = computeNodeGetRemoteDesktopOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (computeNodeGetRemoteDesktopOptions != null) {
            ocpDate = computeNodeGetRemoteDesktopOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.getRemoteDesktop(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<InputStream>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getRemoteDesktopDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<InputStream, ComputeNodeGetRemoteDesktopHeadersInner> getRemoteDesktopDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<InputStream, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<InputStream>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, ComputeNodeGetRemoteDesktopHeadersInner.class);
    }

    /**
     * Lists the compute nodes in the specified pool.
     *
     * @param poolId The id of the pool from which you want to list nodes.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;ComputeNodeInner&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PagedList<ComputeNodeInner>, ComputeNodeListHeadersInner> list(final String poolId) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final ComputeNodeListOptionsInner computeNodeListOptions = null;
        String filter = null;
        String select = null;
        Integer maxResults = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.list(poolId, this.client.apiVersion(), this.client.acceptLanguage(), filter, select, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        ServiceResponseWithHeaders<PageImpl<ComputeNodeInner>, ComputeNodeListHeadersInner> response = listDelegate(call.execute());
        PagedList<ComputeNodeInner> result = new PagedList<ComputeNodeInner>(response.getBody()) {
            @Override
            public Page<ComputeNodeInner> nextPage(String nextPageLink) throws BatchErrorException, IOException {
                return listNext(nextPageLink, null).getBody();
            }
        };
        return new ServiceResponseWithHeaders<>(result, response.getHeaders(), response.getResponse());
    }

    /**
     * Lists the compute nodes in the specified pool.
     *
     * @param poolId The id of the pool from which you want to list nodes.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listAsync(final String poolId, final ListOperationCallback<ComputeNodeInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (poolId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter poolId is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final ComputeNodeListOptionsInner computeNodeListOptions = null;
        String filter = null;
        String select = null;
        Integer maxResults = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.list(poolId, this.client.apiVersion(), this.client.acceptLanguage(), filter, select, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<ComputeNodeInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<ComputeNodeInner>, ComputeNodeListHeadersInner> result = listDelegate(response);
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
     * Lists the compute nodes in the specified pool.
     *
     * @param poolId The id of the pool from which you want to list nodes.
     * @param computeNodeListOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;ComputeNodeInner&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PagedList<ComputeNodeInner>, ComputeNodeListHeadersInner> list(final String poolId, final ComputeNodeListOptionsInner computeNodeListOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(computeNodeListOptions);
        String filter = null;
        if (computeNodeListOptions != null) {
            filter = computeNodeListOptions.filter();
        }
        String select = null;
        if (computeNodeListOptions != null) {
            select = computeNodeListOptions.select();
        }
        Integer maxResults = null;
        if (computeNodeListOptions != null) {
            maxResults = computeNodeListOptions.maxResults();
        }
        Integer timeout = null;
        if (computeNodeListOptions != null) {
            timeout = computeNodeListOptions.timeout();
        }
        String clientRequestId = null;
        if (computeNodeListOptions != null) {
            clientRequestId = computeNodeListOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (computeNodeListOptions != null) {
            returnClientRequestId = computeNodeListOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (computeNodeListOptions != null) {
            ocpDate = computeNodeListOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.list(poolId, this.client.apiVersion(), this.client.acceptLanguage(), filter, select, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        ServiceResponseWithHeaders<PageImpl<ComputeNodeInner>, ComputeNodeListHeadersInner> response = listDelegate(call.execute());
        PagedList<ComputeNodeInner> result = new PagedList<ComputeNodeInner>(response.getBody()) {
            @Override
            public Page<ComputeNodeInner> nextPage(String nextPageLink) throws BatchErrorException, IOException {
                ComputeNodeListNextOptionsInner computeNodeListNextOptions = null;
                if (computeNodeListOptions != null) {
                    computeNodeListNextOptions = new ComputeNodeListNextOptionsInner();
                    computeNodeListNextOptions.setClientRequestId(computeNodeListOptions.clientRequestId());
                    computeNodeListNextOptions.setReturnClientRequestId(computeNodeListOptions.returnClientRequestId());
                    computeNodeListNextOptions.setOcpDate(computeNodeListOptions.ocpDate());
                }
                return listNext(nextPageLink, computeNodeListNextOptions).getBody();
            }
        };
        return new ServiceResponseWithHeaders<>(result, response.getHeaders(), response.getResponse());
    }

    /**
     * Lists the compute nodes in the specified pool.
     *
     * @param poolId The id of the pool from which you want to list nodes.
     * @param computeNodeListOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listAsync(final String poolId, final ComputeNodeListOptionsInner computeNodeListOptions, final ListOperationCallback<ComputeNodeInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (poolId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter poolId is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(computeNodeListOptions, serviceCallback);
        String filter = null;
        if (computeNodeListOptions != null) {
            filter = computeNodeListOptions.filter();
        }
        String select = null;
        if (computeNodeListOptions != null) {
            select = computeNodeListOptions.select();
        }
        Integer maxResults = null;
        if (computeNodeListOptions != null) {
            maxResults = computeNodeListOptions.maxResults();
        }
        Integer timeout = null;
        if (computeNodeListOptions != null) {
            timeout = computeNodeListOptions.timeout();
        }
        String clientRequestId = null;
        if (computeNodeListOptions != null) {
            clientRequestId = computeNodeListOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (computeNodeListOptions != null) {
            returnClientRequestId = computeNodeListOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (computeNodeListOptions != null) {
            ocpDate = computeNodeListOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.list(poolId, this.client.apiVersion(), this.client.acceptLanguage(), filter, select, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<ComputeNodeInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<ComputeNodeInner>, ComputeNodeListHeadersInner> result = listDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        ComputeNodeListNextOptionsInner computeNodeListNextOptions = null;
                        if (computeNodeListOptions != null) {
                            computeNodeListNextOptions = new ComputeNodeListNextOptionsInner();
                            computeNodeListNextOptions.setClientRequestId(computeNodeListOptions.clientRequestId());
                            computeNodeListNextOptions.setReturnClientRequestId(computeNodeListOptions.returnClientRequestId());
                            computeNodeListNextOptions.setOcpDate(computeNodeListOptions.ocpDate());
                        }
                        listNextAsync(result.getBody().getNextPageLink(), computeNodeListNextOptions, serviceCall, serviceCallback);
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

    private ServiceResponseWithHeaders<PageImpl<ComputeNodeInner>, ComputeNodeListHeadersInner> listDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<ComputeNodeInner>, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<ComputeNodeInner>>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, ComputeNodeListHeadersInner.class);
    }

    /**
     * Lists the compute nodes in the specified pool.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;ComputeNodeInner&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PageImpl<ComputeNodeInner>, ComputeNodeListHeadersInner> listNext(final String nextPageLink) throws BatchErrorException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        final ComputeNodeListNextOptionsInner computeNodeListNextOptions = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.acceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted);
        return listNextDelegate(call.execute());
    }

    /**
     * Lists the compute nodes in the specified pool.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<ComputeNodeInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        final ComputeNodeListNextOptionsInner computeNodeListNextOptions = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.acceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted);
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<ComputeNodeInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<ComputeNodeInner>, ComputeNodeListHeadersInner> result = listNextDelegate(response);
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
     * Lists the compute nodes in the specified pool.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param computeNodeListNextOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;ComputeNodeInner&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PageImpl<ComputeNodeInner>, ComputeNodeListHeadersInner> listNext(final String nextPageLink, final ComputeNodeListNextOptionsInner computeNodeListNextOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        Validator.validate(computeNodeListNextOptions);
        String clientRequestId = null;
        if (computeNodeListNextOptions != null) {
            clientRequestId = computeNodeListNextOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (computeNodeListNextOptions != null) {
            returnClientRequestId = computeNodeListNextOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (computeNodeListNextOptions != null) {
            ocpDate = computeNodeListNextOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.acceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted);
        return listNextDelegate(call.execute());
    }

    /**
     * Lists the compute nodes in the specified pool.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param computeNodeListNextOptions Additional parameters for the operation
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listNextAsync(final String nextPageLink, final ComputeNodeListNextOptionsInner computeNodeListNextOptions, final ServiceCall serviceCall, final ListOperationCallback<ComputeNodeInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        Validator.validate(computeNodeListNextOptions, serviceCallback);
        String clientRequestId = null;
        if (computeNodeListNextOptions != null) {
            clientRequestId = computeNodeListNextOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (computeNodeListNextOptions != null) {
            returnClientRequestId = computeNodeListNextOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (computeNodeListNextOptions != null) {
            ocpDate = computeNodeListNextOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.acceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted);
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<ComputeNodeInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<ComputeNodeInner>, ComputeNodeListHeadersInner> result = listNextDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listNextAsync(result.getBody().getNextPageLink(), computeNodeListNextOptions, serviceCall, serviceCallback);
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

    private ServiceResponseWithHeaders<PageImpl<ComputeNodeInner>, ComputeNodeListHeadersInner> listNextDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<ComputeNodeInner>, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<ComputeNodeInner>>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, ComputeNodeListHeadersInner.class);
    }

}
