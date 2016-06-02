/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.implementation;

import retrofit2.Retrofit;
import com.microsoft.azure.batch.protocol.ComputeNodes;
import com.microsoft.azure.batch.protocol.BatchServiceClient;
import com.google.common.reflect.TypeToken;
import com.microsoft.azure.AzureServiceResponseBuilder;
import com.microsoft.azure.batch.protocol.models.BatchErrorException;
import com.microsoft.azure.batch.protocol.models.ComputeNode;
import com.microsoft.azure.batch.protocol.models.ComputeNodeAddUserHeaders;
import com.microsoft.azure.batch.protocol.models.ComputeNodeAddUserOptions;
import com.microsoft.azure.batch.protocol.models.ComputeNodeDeleteUserHeaders;
import com.microsoft.azure.batch.protocol.models.ComputeNodeDeleteUserOptions;
import com.microsoft.azure.batch.protocol.models.ComputeNodeDisableSchedulingHeaders;
import com.microsoft.azure.batch.protocol.models.ComputeNodeDisableSchedulingOptions;
import com.microsoft.azure.batch.protocol.models.ComputeNodeEnableSchedulingHeaders;
import com.microsoft.azure.batch.protocol.models.ComputeNodeEnableSchedulingOptions;
import com.microsoft.azure.batch.protocol.models.ComputeNodeGetHeaders;
import com.microsoft.azure.batch.protocol.models.ComputeNodeGetOptions;
import com.microsoft.azure.batch.protocol.models.ComputeNodeGetRemoteDesktopHeaders;
import com.microsoft.azure.batch.protocol.models.ComputeNodeGetRemoteDesktopOptions;
import com.microsoft.azure.batch.protocol.models.ComputeNodeGetRemoteLoginSettingsHeaders;
import com.microsoft.azure.batch.protocol.models.ComputeNodeGetRemoteLoginSettingsOptions;
import com.microsoft.azure.batch.protocol.models.ComputeNodeGetRemoteLoginSettingsResult;
import com.microsoft.azure.batch.protocol.models.ComputeNodeListHeaders;
import com.microsoft.azure.batch.protocol.models.ComputeNodeListNextOptions;
import com.microsoft.azure.batch.protocol.models.ComputeNodeListOptions;
import com.microsoft.azure.batch.protocol.models.ComputeNodeRebootHeaders;
import com.microsoft.azure.batch.protocol.models.ComputeNodeRebootOption;
import com.microsoft.azure.batch.protocol.models.ComputeNodeRebootOptions;
import com.microsoft.azure.batch.protocol.models.ComputeNodeReimageHeaders;
import com.microsoft.azure.batch.protocol.models.ComputeNodeReimageOption;
import com.microsoft.azure.batch.protocol.models.ComputeNodeReimageOptions;
import com.microsoft.azure.batch.protocol.models.ComputeNodeUpdateUserHeaders;
import com.microsoft.azure.batch.protocol.models.ComputeNodeUpdateUserOptions;
import com.microsoft.azure.batch.protocol.models.ComputeNodeUser;
import com.microsoft.azure.batch.protocol.models.DisableComputeNodeSchedulingOption;
import com.microsoft.azure.batch.protocol.models.NodeDisableSchedulingParameter;
import com.microsoft.azure.batch.protocol.models.NodeRebootParameter;
import com.microsoft.azure.batch.protocol.models.NodeReimageParameter;
import com.microsoft.azure.batch.protocol.models.NodeUpdateUserParameter;
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
public final class ComputeNodesImpl implements ComputeNodes {
    /** The Retrofit service to perform REST calls. */
    private ComputeNodesService service;
    /** The service client containing this operation class. */
    private BatchServiceClient client;

    /**
     * Initializes an instance of ComputeNodesImpl.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public ComputeNodesImpl(Retrofit retrofit, BatchServiceClient client) {
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
        Call<ResponseBody> addUser(@Path("poolId") String poolId, @Path("nodeId") String nodeId, @Body ComputeNodeUser user, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @HTTP(path = "pools/{poolId}/nodes/{nodeId}/users/{userName}", method = "DELETE", hasBody = true)
        Call<ResponseBody> deleteUser(@Path("poolId") String poolId, @Path("nodeId") String nodeId, @Path("userName") String userName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @PUT("pools/{poolId}/nodes/{nodeId}/users/{userName}")
        Call<ResponseBody> updateUser(@Path("poolId") String poolId, @Path("nodeId") String nodeId, @Path("userName") String userName, @Body NodeUpdateUserParameter nodeUpdateUserParameter, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @GET("pools/{poolId}/nodes/{nodeId}")
        Call<ResponseBody> get(@Path("poolId") String poolId, @Path("nodeId") String nodeId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("$select") String select, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @POST("pools/{poolId}/nodes/{nodeId}/reboot")
        Call<ResponseBody> reboot(@Path("poolId") String poolId, @Path("nodeId") String nodeId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Body NodeRebootParameter nodeRebootParameter, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @POST("pools/{poolId}/nodes/{nodeId}/reimage")
        Call<ResponseBody> reimage(@Path("poolId") String poolId, @Path("nodeId") String nodeId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Body NodeReimageParameter nodeReimageParameter, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @POST("pools/{poolId}/nodes/{nodeId}/disablescheduling")
        Call<ResponseBody> disableScheduling(@Path("poolId") String poolId, @Path("nodeId") String nodeId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Body NodeDisableSchedulingParameter nodeDisableSchedulingParameter, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @POST("pools/{poolId}/nodes/{nodeId}/enablescheduling")
        Call<ResponseBody> enableScheduling(@Path("poolId") String poolId, @Path("nodeId") String nodeId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @GET("pools/{poolId}/nodes/{nodeId}/remoteloginsettings")
        Call<ResponseBody> getRemoteLoginSettings(@Path("poolId") String poolId, @Path("nodeId") String nodeId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @GET("pools/{poolId}/nodes/{nodeId}/rdp")
        @Streaming
        Call<ResponseBody> getRemoteDesktop(@Path("poolId") String poolId, @Path("nodeId") String nodeId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @GET("pools/{poolId}/nodes")
        Call<ResponseBody> list(@Path("poolId") String poolId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("$filter") String filter, @Query("$select") String select, @Query("maxresults") Integer maxResults, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @GET
        Call<ResponseBody> listNext(@Url String nextPageLink, @Header("accept-language") String acceptLanguage, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Header("User-Agent") String userAgent);

    }

    /**
     * Adds a user account to the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the machine on which you want to create a user account.
     * @param user The user account to be created.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, ComputeNodeAddUserHeaders> addUser(String poolId, String nodeId, ComputeNodeUser user) throws BatchErrorException, IOException, IllegalArgumentException {
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
        final ComputeNodeAddUserOptions computeNodeAddUserOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        Call<ResponseBody> call = service.addUser(poolId, nodeId, user, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
        return addUserDelegate(call.execute());
    }

    /**
     * Adds a user account to the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the machine on which you want to create a user account.
     * @param user The user account to be created.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall addUserAsync(String poolId, String nodeId, ComputeNodeUser user, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
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
        final ComputeNodeAddUserOptions computeNodeAddUserOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        Call<ResponseBody> call = service.addUser(poolId, nodeId, user, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
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
     * @param user The user account to be created.
     * @param computeNodeAddUserOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, ComputeNodeAddUserHeaders> addUser(String poolId, String nodeId, ComputeNodeUser user, ComputeNodeAddUserOptions computeNodeAddUserOptions) throws BatchErrorException, IOException, IllegalArgumentException {
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
        Call<ResponseBody> call = service.addUser(poolId, nodeId, user, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
        return addUserDelegate(call.execute());
    }

    /**
     * Adds a user account to the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the machine on which you want to create a user account.
     * @param user The user account to be created.
     * @param computeNodeAddUserOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall addUserAsync(String poolId, String nodeId, ComputeNodeUser user, ComputeNodeAddUserOptions computeNodeAddUserOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
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
        Call<ResponseBody> call = service.addUser(poolId, nodeId, user, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
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

    private ServiceResponseWithHeaders<Void, ComputeNodeAddUserHeaders> addUserDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(201, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, ComputeNodeAddUserHeaders.class);
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
    public ServiceResponseWithHeaders<Void, ComputeNodeDeleteUserHeaders> deleteUser(String poolId, String nodeId, String userName) throws BatchErrorException, IOException, IllegalArgumentException {
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
        final ComputeNodeDeleteUserOptions computeNodeDeleteUserOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        Call<ResponseBody> call = service.deleteUser(poolId, nodeId, userName, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
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
        final ComputeNodeDeleteUserOptions computeNodeDeleteUserOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        Call<ResponseBody> call = service.deleteUser(poolId, nodeId, userName, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
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
    public ServiceResponseWithHeaders<Void, ComputeNodeDeleteUserHeaders> deleteUser(String poolId, String nodeId, String userName, ComputeNodeDeleteUserOptions computeNodeDeleteUserOptions) throws BatchErrorException, IOException, IllegalArgumentException {
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
        Call<ResponseBody> call = service.deleteUser(poolId, nodeId, userName, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
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
    public ServiceCall deleteUserAsync(String poolId, String nodeId, String userName, ComputeNodeDeleteUserOptions computeNodeDeleteUserOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
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
        Call<ResponseBody> call = service.deleteUser(poolId, nodeId, userName, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
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

    private ServiceResponseWithHeaders<Void, ComputeNodeDeleteUserHeaders> deleteUserDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, ComputeNodeDeleteUserHeaders.class);
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
    public ServiceResponseWithHeaders<Void, ComputeNodeUpdateUserHeaders> updateUser(String poolId, String nodeId, String userName, NodeUpdateUserParameter nodeUpdateUserParameter) throws BatchErrorException, IOException, IllegalArgumentException {
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
        final ComputeNodeUpdateUserOptions computeNodeUpdateUserOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        Call<ResponseBody> call = service.updateUser(poolId, nodeId, userName, nodeUpdateUserParameter, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
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
    public ServiceCall updateUserAsync(String poolId, String nodeId, String userName, NodeUpdateUserParameter nodeUpdateUserParameter, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
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
        final ComputeNodeUpdateUserOptions computeNodeUpdateUserOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        Call<ResponseBody> call = service.updateUser(poolId, nodeId, userName, nodeUpdateUserParameter, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
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
    public ServiceResponseWithHeaders<Void, ComputeNodeUpdateUserHeaders> updateUser(String poolId, String nodeId, String userName, NodeUpdateUserParameter nodeUpdateUserParameter, ComputeNodeUpdateUserOptions computeNodeUpdateUserOptions) throws BatchErrorException, IOException, IllegalArgumentException {
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
        Call<ResponseBody> call = service.updateUser(poolId, nodeId, userName, nodeUpdateUserParameter, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
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
    public ServiceCall updateUserAsync(String poolId, String nodeId, String userName, NodeUpdateUserParameter nodeUpdateUserParameter, ComputeNodeUpdateUserOptions computeNodeUpdateUserOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
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
        Call<ResponseBody> call = service.updateUser(poolId, nodeId, userName, nodeUpdateUserParameter, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
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

    private ServiceResponseWithHeaders<Void, ComputeNodeUpdateUserHeaders> updateUserDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, ComputeNodeUpdateUserHeaders.class);
    }

    /**
     * Gets information about the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that you want to get information about.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the ComputeNode object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<ComputeNode, ComputeNodeGetHeaders> get(String poolId, String nodeId) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (nodeId == null) {
            throw new IllegalArgumentException("Parameter nodeId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final ComputeNodeGetOptions computeNodeGetOptions = null;
        String select = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        Call<ResponseBody> call = service.get(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), select, timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
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
    public ServiceCall getAsync(String poolId, String nodeId, final ServiceCallback<ComputeNode> serviceCallback) throws IllegalArgumentException {
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
        final ComputeNodeGetOptions computeNodeGetOptions = null;
        String select = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        Call<ResponseBody> call = service.get(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), select, timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<ComputeNode>(serviceCallback) {
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
     * @return the ComputeNode object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<ComputeNode, ComputeNodeGetHeaders> get(String poolId, String nodeId, ComputeNodeGetOptions computeNodeGetOptions) throws BatchErrorException, IOException, IllegalArgumentException {
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
        Call<ResponseBody> call = service.get(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), select, timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
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
    public ServiceCall getAsync(String poolId, String nodeId, ComputeNodeGetOptions computeNodeGetOptions, final ServiceCallback<ComputeNode> serviceCallback) throws IllegalArgumentException {
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
        Call<ResponseBody> call = service.get(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), select, timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<ComputeNode>(serviceCallback) {
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

    private ServiceResponseWithHeaders<ComputeNode, ComputeNodeGetHeaders> getDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<ComputeNode, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<ComputeNode>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, ComputeNodeGetHeaders.class);
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
    public ServiceResponseWithHeaders<Void, ComputeNodeRebootHeaders> reboot(String poolId, String nodeId) throws BatchErrorException, IOException, IllegalArgumentException {
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
        final ComputeNodeRebootOptions computeNodeRebootOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        NodeRebootParameter nodeRebootParameter = new NodeRebootParameter();
        nodeRebootParameter.withNodeRebootOption(null);
        Call<ResponseBody> call = service.reboot(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, nodeRebootParameter, this.client.userAgent());
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
        final ComputeNodeRebootOptions computeNodeRebootOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        NodeRebootParameter nodeRebootParameter = new NodeRebootParameter();
        nodeRebootParameter.withNodeRebootOption(null);
        Call<ResponseBody> call = service.reboot(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, nodeRebootParameter, this.client.userAgent());
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
     * @param nodeRebootOption When to reboot the compute node and what to do with currently running tasks. The default value is requeue. Possible values include: 'requeue', 'terminate', 'taskcompletion', 'retaineddata'
     * @param computeNodeRebootOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, ComputeNodeRebootHeaders> reboot(String poolId, String nodeId, ComputeNodeRebootOption nodeRebootOption, ComputeNodeRebootOptions computeNodeRebootOptions) throws BatchErrorException, IOException, IllegalArgumentException {
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
            nodeRebootParameter.withNodeRebootOption(nodeRebootOption);
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.reboot(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, nodeRebootParameter, this.client.userAgent());
        return rebootDelegate(call.execute());
    }

    /**
     * Restarts the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that you want to restart.
     * @param nodeRebootOption When to reboot the compute node and what to do with currently running tasks. The default value is requeue. Possible values include: 'requeue', 'terminate', 'taskcompletion', 'retaineddata'
     * @param computeNodeRebootOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall rebootAsync(String poolId, String nodeId, ComputeNodeRebootOption nodeRebootOption, ComputeNodeRebootOptions computeNodeRebootOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
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
            nodeRebootParameter.withNodeRebootOption(nodeRebootOption);
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.reboot(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, nodeRebootParameter, this.client.userAgent());
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

    private ServiceResponseWithHeaders<Void, ComputeNodeRebootHeaders> rebootDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(202, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, ComputeNodeRebootHeaders.class);
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
    public ServiceResponseWithHeaders<Void, ComputeNodeReimageHeaders> reimage(String poolId, String nodeId) throws BatchErrorException, IOException, IllegalArgumentException {
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
        final ComputeNodeReimageOptions computeNodeReimageOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        NodeReimageParameter nodeReimageParameter = new NodeReimageParameter();
        nodeReimageParameter.withNodeReimageOption(null);
        Call<ResponseBody> call = service.reimage(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, nodeReimageParameter, this.client.userAgent());
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
        final ComputeNodeReimageOptions computeNodeReimageOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        NodeReimageParameter nodeReimageParameter = new NodeReimageParameter();
        nodeReimageParameter.withNodeReimageOption(null);
        Call<ResponseBody> call = service.reimage(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, nodeReimageParameter, this.client.userAgent());
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
     * @param nodeReimageOption When to reimage the compute node and what to do with currently running tasks. The default value is requeue. Possible values include: 'requeue', 'terminate', 'taskcompletion', 'retaineddata'
     * @param computeNodeReimageOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, ComputeNodeReimageHeaders> reimage(String poolId, String nodeId, ComputeNodeReimageOption nodeReimageOption, ComputeNodeReimageOptions computeNodeReimageOptions) throws BatchErrorException, IOException, IllegalArgumentException {
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
            nodeReimageParameter.withNodeReimageOption(nodeReimageOption);
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.reimage(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, nodeReimageParameter, this.client.userAgent());
        return reimageDelegate(call.execute());
    }

    /**
     * Reinstalls the operating system on the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that you want to restart.
     * @param nodeReimageOption When to reimage the compute node and what to do with currently running tasks. The default value is requeue. Possible values include: 'requeue', 'terminate', 'taskcompletion', 'retaineddata'
     * @param computeNodeReimageOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall reimageAsync(String poolId, String nodeId, ComputeNodeReimageOption nodeReimageOption, ComputeNodeReimageOptions computeNodeReimageOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
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
            nodeReimageParameter.withNodeReimageOption(nodeReimageOption);
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.reimage(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, nodeReimageParameter, this.client.userAgent());
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

    private ServiceResponseWithHeaders<Void, ComputeNodeReimageHeaders> reimageDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(202, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, ComputeNodeReimageHeaders.class);
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
    public ServiceResponseWithHeaders<Void, ComputeNodeDisableSchedulingHeaders> disableScheduling(String poolId, String nodeId) throws BatchErrorException, IOException, IllegalArgumentException {
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
        final ComputeNodeDisableSchedulingOptions computeNodeDisableSchedulingOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        NodeDisableSchedulingParameter nodeDisableSchedulingParameter = new NodeDisableSchedulingParameter();
        nodeDisableSchedulingParameter.withNodeDisableSchedulingOption(null);
        Call<ResponseBody> call = service.disableScheduling(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, nodeDisableSchedulingParameter, this.client.userAgent());
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
        final ComputeNodeDisableSchedulingOptions computeNodeDisableSchedulingOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        NodeDisableSchedulingParameter nodeDisableSchedulingParameter = new NodeDisableSchedulingParameter();
        nodeDisableSchedulingParameter.withNodeDisableSchedulingOption(null);
        Call<ResponseBody> call = service.disableScheduling(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, nodeDisableSchedulingParameter, this.client.userAgent());
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
     * @param nodeDisableSchedulingOption What to do with currently running tasks when disable task scheduling on the compute node. The default value is requeue. Possible values include: 'requeue', 'terminate', 'taskcompletion'
     * @param computeNodeDisableSchedulingOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, ComputeNodeDisableSchedulingHeaders> disableScheduling(String poolId, String nodeId, DisableComputeNodeSchedulingOption nodeDisableSchedulingOption, ComputeNodeDisableSchedulingOptions computeNodeDisableSchedulingOptions) throws BatchErrorException, IOException, IllegalArgumentException {
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
            nodeDisableSchedulingParameter.withNodeDisableSchedulingOption(nodeDisableSchedulingOption);
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.disableScheduling(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, nodeDisableSchedulingParameter, this.client.userAgent());
        return disableSchedulingDelegate(call.execute());
    }

    /**
     * Disable task scheduling of the specified compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node that you want to disable task scheduling.
     * @param nodeDisableSchedulingOption What to do with currently running tasks when disable task scheduling on the compute node. The default value is requeue. Possible values include: 'requeue', 'terminate', 'taskcompletion'
     * @param computeNodeDisableSchedulingOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall disableSchedulingAsync(String poolId, String nodeId, DisableComputeNodeSchedulingOption nodeDisableSchedulingOption, ComputeNodeDisableSchedulingOptions computeNodeDisableSchedulingOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
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
            nodeDisableSchedulingParameter.withNodeDisableSchedulingOption(nodeDisableSchedulingOption);
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.disableScheduling(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, nodeDisableSchedulingParameter, this.client.userAgent());
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

    private ServiceResponseWithHeaders<Void, ComputeNodeDisableSchedulingHeaders> disableSchedulingDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, ComputeNodeDisableSchedulingHeaders.class);
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
    public ServiceResponseWithHeaders<Void, ComputeNodeEnableSchedulingHeaders> enableScheduling(String poolId, String nodeId) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (nodeId == null) {
            throw new IllegalArgumentException("Parameter nodeId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final ComputeNodeEnableSchedulingOptions computeNodeEnableSchedulingOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        Call<ResponseBody> call = service.enableScheduling(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
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
        final ComputeNodeEnableSchedulingOptions computeNodeEnableSchedulingOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        Call<ResponseBody> call = service.enableScheduling(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
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
    public ServiceResponseWithHeaders<Void, ComputeNodeEnableSchedulingHeaders> enableScheduling(String poolId, String nodeId, ComputeNodeEnableSchedulingOptions computeNodeEnableSchedulingOptions) throws BatchErrorException, IOException, IllegalArgumentException {
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
        Call<ResponseBody> call = service.enableScheduling(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
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
    public ServiceCall enableSchedulingAsync(String poolId, String nodeId, ComputeNodeEnableSchedulingOptions computeNodeEnableSchedulingOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
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
        Call<ResponseBody> call = service.enableScheduling(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
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

    private ServiceResponseWithHeaders<Void, ComputeNodeEnableSchedulingHeaders> enableSchedulingDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, ComputeNodeEnableSchedulingHeaders.class);
    }

    /**
     * Gets the settings required for remote login to a compute node.
     *
     * @param poolId The id of the pool that contains the compute node.
     * @param nodeId The id of the compute node for which to obtain the remote login settings.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the ComputeNodeGetRemoteLoginSettingsResult object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<ComputeNodeGetRemoteLoginSettingsResult, ComputeNodeGetRemoteLoginSettingsHeaders> getRemoteLoginSettings(String poolId, String nodeId) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (nodeId == null) {
            throw new IllegalArgumentException("Parameter nodeId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final ComputeNodeGetRemoteLoginSettingsOptions computeNodeGetRemoteLoginSettingsOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        Call<ResponseBody> call = service.getRemoteLoginSettings(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
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
    public ServiceCall getRemoteLoginSettingsAsync(String poolId, String nodeId, final ServiceCallback<ComputeNodeGetRemoteLoginSettingsResult> serviceCallback) throws IllegalArgumentException {
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
        final ComputeNodeGetRemoteLoginSettingsOptions computeNodeGetRemoteLoginSettingsOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        Call<ResponseBody> call = service.getRemoteLoginSettings(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<ComputeNodeGetRemoteLoginSettingsResult>(serviceCallback) {
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
     * @return the ComputeNodeGetRemoteLoginSettingsResult object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<ComputeNodeGetRemoteLoginSettingsResult, ComputeNodeGetRemoteLoginSettingsHeaders> getRemoteLoginSettings(String poolId, String nodeId, ComputeNodeGetRemoteLoginSettingsOptions computeNodeGetRemoteLoginSettingsOptions) throws BatchErrorException, IOException, IllegalArgumentException {
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
        Call<ResponseBody> call = service.getRemoteLoginSettings(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
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
    public ServiceCall getRemoteLoginSettingsAsync(String poolId, String nodeId, ComputeNodeGetRemoteLoginSettingsOptions computeNodeGetRemoteLoginSettingsOptions, final ServiceCallback<ComputeNodeGetRemoteLoginSettingsResult> serviceCallback) throws IllegalArgumentException {
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
        Call<ResponseBody> call = service.getRemoteLoginSettings(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<ComputeNodeGetRemoteLoginSettingsResult>(serviceCallback) {
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

    private ServiceResponseWithHeaders<ComputeNodeGetRemoteLoginSettingsResult, ComputeNodeGetRemoteLoginSettingsHeaders> getRemoteLoginSettingsDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<ComputeNodeGetRemoteLoginSettingsResult, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<ComputeNodeGetRemoteLoginSettingsResult>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, ComputeNodeGetRemoteLoginSettingsHeaders.class);
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
    public ServiceResponseWithHeaders<InputStream, ComputeNodeGetRemoteDesktopHeaders> getRemoteDesktop(String poolId, String nodeId) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (nodeId == null) {
            throw new IllegalArgumentException("Parameter nodeId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final ComputeNodeGetRemoteDesktopOptions computeNodeGetRemoteDesktopOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        Call<ResponseBody> call = service.getRemoteDesktop(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
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
        final ComputeNodeGetRemoteDesktopOptions computeNodeGetRemoteDesktopOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        Call<ResponseBody> call = service.getRemoteDesktop(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
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
    public ServiceResponseWithHeaders<InputStream, ComputeNodeGetRemoteDesktopHeaders> getRemoteDesktop(String poolId, String nodeId, ComputeNodeGetRemoteDesktopOptions computeNodeGetRemoteDesktopOptions) throws BatchErrorException, IOException, IllegalArgumentException {
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
        Call<ResponseBody> call = service.getRemoteDesktop(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
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
    public ServiceCall getRemoteDesktopAsync(String poolId, String nodeId, ComputeNodeGetRemoteDesktopOptions computeNodeGetRemoteDesktopOptions, final ServiceCallback<InputStream> serviceCallback) throws IllegalArgumentException {
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
        Call<ResponseBody> call = service.getRemoteDesktop(poolId, nodeId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
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

    private ServiceResponseWithHeaders<InputStream, ComputeNodeGetRemoteDesktopHeaders> getRemoteDesktopDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<InputStream, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<InputStream>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, ComputeNodeGetRemoteDesktopHeaders.class);
    }

    /**
     * Lists the compute nodes in the specified pool.
     *
     * @param poolId The id of the pool from which you want to list nodes.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;ComputeNode&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PagedList<ComputeNode>, ComputeNodeListHeaders> list(final String poolId) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final ComputeNodeListOptions computeNodeListOptions = null;
        String filter = null;
        String select = null;
        Integer maxResults = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        Call<ResponseBody> call = service.list(poolId, this.client.apiVersion(), this.client.acceptLanguage(), filter, select, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
        ServiceResponseWithHeaders<PageImpl<ComputeNode>, ComputeNodeListHeaders> response = listDelegate(call.execute());
        PagedList<ComputeNode> result = new PagedList<ComputeNode>(response.getBody()) {
            @Override
            public Page<ComputeNode> nextPage(String nextPageLink) throws BatchErrorException, IOException {
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
    public ServiceCall listAsync(final String poolId, final ListOperationCallback<ComputeNode> serviceCallback) throws IllegalArgumentException {
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
        final ComputeNodeListOptions computeNodeListOptions = null;
        String filter = null;
        String select = null;
        Integer maxResults = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        Call<ResponseBody> call = service.list(poolId, this.client.apiVersion(), this.client.acceptLanguage(), filter, select, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<ComputeNode>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<ComputeNode>, ComputeNodeListHeaders> result = listDelegate(response);
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
     * @return the List&lt;ComputeNode&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PagedList<ComputeNode>, ComputeNodeListHeaders> list(final String poolId, final ComputeNodeListOptions computeNodeListOptions) throws BatchErrorException, IOException, IllegalArgumentException {
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
        Call<ResponseBody> call = service.list(poolId, this.client.apiVersion(), this.client.acceptLanguage(), filter, select, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
        ServiceResponseWithHeaders<PageImpl<ComputeNode>, ComputeNodeListHeaders> response = listDelegate(call.execute());
        PagedList<ComputeNode> result = new PagedList<ComputeNode>(response.getBody()) {
            @Override
            public Page<ComputeNode> nextPage(String nextPageLink) throws BatchErrorException, IOException {
                ComputeNodeListNextOptions computeNodeListNextOptions = null;
                if (computeNodeListOptions != null) {
                    computeNodeListNextOptions = new ComputeNodeListNextOptions();
                    computeNodeListNextOptions.withClientRequestId(computeNodeListOptions.clientRequestId());
                    computeNodeListNextOptions.withReturnClientRequestId(computeNodeListOptions.returnClientRequestId());
                    computeNodeListNextOptions.withOcpDate(computeNodeListOptions.ocpDate());
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
    public ServiceCall listAsync(final String poolId, final ComputeNodeListOptions computeNodeListOptions, final ListOperationCallback<ComputeNode> serviceCallback) throws IllegalArgumentException {
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
        Call<ResponseBody> call = service.list(poolId, this.client.apiVersion(), this.client.acceptLanguage(), filter, select, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<ComputeNode>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<ComputeNode>, ComputeNodeListHeaders> result = listDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        ComputeNodeListNextOptions computeNodeListNextOptions = null;
                        if (computeNodeListOptions != null) {
                            computeNodeListNextOptions = new ComputeNodeListNextOptions();
                            computeNodeListNextOptions.withClientRequestId(computeNodeListOptions.clientRequestId());
                            computeNodeListNextOptions.withReturnClientRequestId(computeNodeListOptions.returnClientRequestId());
                            computeNodeListNextOptions.withOcpDate(computeNodeListOptions.ocpDate());
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

    private ServiceResponseWithHeaders<PageImpl<ComputeNode>, ComputeNodeListHeaders> listDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<ComputeNode>, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<ComputeNode>>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, ComputeNodeListHeaders.class);
    }

    /**
     * Lists the compute nodes in the specified pool.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;ComputeNode&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PageImpl<ComputeNode>, ComputeNodeListHeaders> listNext(final String nextPageLink) throws BatchErrorException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        final ComputeNodeListNextOptions computeNodeListNextOptions = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.acceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
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
    public ServiceCall listNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<ComputeNode> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        final ComputeNodeListNextOptions computeNodeListNextOptions = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTime ocpDate = null;
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.acceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<ComputeNode>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<ComputeNode>, ComputeNodeListHeaders> result = listNextDelegate(response);
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
     * @return the List&lt;ComputeNode&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PageImpl<ComputeNode>, ComputeNodeListHeaders> listNext(final String nextPageLink, final ComputeNodeListNextOptions computeNodeListNextOptions) throws BatchErrorException, IOException, IllegalArgumentException {
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
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.acceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
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
    public ServiceCall listNextAsync(final String nextPageLink, final ComputeNodeListNextOptions computeNodeListNextOptions, final ServiceCall serviceCall, final ListOperationCallback<ComputeNode> serviceCallback) throws IllegalArgumentException {
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
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.acceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted, this.client.userAgent());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<ComputeNode>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<ComputeNode>, ComputeNodeListHeaders> result = listNextDelegate(response);
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

    private ServiceResponseWithHeaders<PageImpl<ComputeNode>, ComputeNodeListHeaders> listNextDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<ComputeNode>, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<ComputeNode>>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, ComputeNodeListHeaders.class);
    }

}
