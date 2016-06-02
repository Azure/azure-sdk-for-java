/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation.api;

import retrofit2.Retrofit;
import com.google.common.reflect.TypeToken;
import com.microsoft.azure.AzureServiceResponseBuilder;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.ListOperationCallback;
import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;
import com.microsoft.rest.ServiceResponseCallback;
import com.microsoft.rest.Validator;
import java.io.IOException;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
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
 * in VirtualNetworkGatewayConnections.
 */
public final class VirtualNetworkGatewayConnectionsInner {
    /** The Retrofit service to perform REST calls. */
    private VirtualNetworkGatewayConnectionsService service;
    /** The service client containing this operation class. */
    private NetworkManagementClientImpl client;

    /**
     * Initializes an instance of VirtualNetworkGatewayConnectionsInner.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public VirtualNetworkGatewayConnectionsInner(Retrofit retrofit, NetworkManagementClientImpl client) {
        this.service = retrofit.create(VirtualNetworkGatewayConnectionsService.class);
        this.client = client;
    }

    /**
     * The interface defining all the services for VirtualNetworkGatewayConnections to be
     * used by Retrofit to perform actually REST calls.
     */
    interface VirtualNetworkGatewayConnectionsService {
        @Headers("Content-Type: application/json; charset=utf-8")
        @PUT("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Network/connections/{virtualNetworkGatewayConnectionName}")
        Call<ResponseBody> createOrUpdate(@Path("resourceGroupName") String resourceGroupName, @Path("virtualNetworkGatewayConnectionName") String virtualNetworkGatewayConnectionName, @Path("subscriptionId") String subscriptionId, @Body VirtualNetworkGatewayConnectionInner parameters, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @PUT("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Network/connections/{virtualNetworkGatewayConnectionName}")
        Call<ResponseBody> beginCreateOrUpdate(@Path("resourceGroupName") String resourceGroupName, @Path("virtualNetworkGatewayConnectionName") String virtualNetworkGatewayConnectionName, @Path("subscriptionId") String subscriptionId, @Body VirtualNetworkGatewayConnectionInner parameters, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Network/connections/{virtualNetworkGatewayConnectionName}")
        Call<ResponseBody> get(@Path("resourceGroupName") String resourceGroupName, @Path("virtualNetworkGatewayConnectionName") String virtualNetworkGatewayConnectionName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @HTTP(path = "subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Network/connections/{virtualNetworkGatewayConnectionName}", method = "DELETE", hasBody = true)
        Call<ResponseBody> delete(@Path("resourceGroupName") String resourceGroupName, @Path("virtualNetworkGatewayConnectionName") String virtualNetworkGatewayConnectionName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @HTTP(path = "subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Network/connections/{virtualNetworkGatewayConnectionName}", method = "DELETE", hasBody = true)
        Call<ResponseBody> beginDelete(@Path("resourceGroupName") String resourceGroupName, @Path("virtualNetworkGatewayConnectionName") String virtualNetworkGatewayConnectionName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Network/connections/{connectionSharedKeyName}/sharedkey")
        Call<ResponseBody> getSharedKey(@Path("resourceGroupName") String resourceGroupName, @Path("connectionSharedKeyName") String connectionSharedKeyName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Network/connections")
        Call<ResponseBody> list(@Path("resourceGroupName") String resourceGroupName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @POST("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Network/connections/{virtualNetworkGatewayConnectionName}/sharedkey/reset")
        Call<ResponseBody> resetSharedKey(@Path("resourceGroupName") String resourceGroupName, @Path("virtualNetworkGatewayConnectionName") String virtualNetworkGatewayConnectionName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Body ConnectionResetSharedKeyInner parameters, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @POST("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Network/connections/{virtualNetworkGatewayConnectionName}/sharedkey/reset")
        Call<ResponseBody> beginResetSharedKey(@Path("resourceGroupName") String resourceGroupName, @Path("virtualNetworkGatewayConnectionName") String virtualNetworkGatewayConnectionName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Body ConnectionResetSharedKeyInner parameters, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @PUT("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Network/connections/{virtualNetworkGatewayConnectionName}/sharedkey")
        Call<ResponseBody> setSharedKey(@Path("resourceGroupName") String resourceGroupName, @Path("virtualNetworkGatewayConnectionName") String virtualNetworkGatewayConnectionName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Body ConnectionSharedKeyInner parameters, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @PUT("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Network/connections/{virtualNetworkGatewayConnectionName}/sharedkey")
        Call<ResponseBody> beginSetSharedKey(@Path("resourceGroupName") String resourceGroupName, @Path("virtualNetworkGatewayConnectionName") String virtualNetworkGatewayConnectionName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Body ConnectionSharedKeyInner parameters, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET
        Call<ResponseBody> listNext(@Url String nextPageLink, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

    }

    /**
     * The Put VirtualNetworkGatewayConnection operation creates/updates a virtual network gateway connection in the specified resource group through Network resource provider.
     *
     * @param resourceGroupName The name of the resource group.
     * @param virtualNetworkGatewayConnectionName The name of the virtual network gateway conenction.
     * @param parameters Parameters supplied to the Begin Create or update Virtual Network Gateway connection operation through Network resource provider.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @throws InterruptedException exception thrown when long running operation is interrupted
     * @return the VirtualNetworkGatewayConnectionInner object wrapped in ServiceResponse if successful.
     */
    public ServiceResponse<VirtualNetworkGatewayConnectionInner> createOrUpdate(String resourceGroupName, String virtualNetworkGatewayConnectionName, VirtualNetworkGatewayConnectionInner parameters) throws CloudException, IOException, IllegalArgumentException, InterruptedException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (virtualNetworkGatewayConnectionName == null) {
            throw new IllegalArgumentException("Parameter virtualNetworkGatewayConnectionName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (parameters == null) {
            throw new IllegalArgumentException("Parameter parameters is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(parameters);
        Response<ResponseBody> result = service.createOrUpdate(resourceGroupName, virtualNetworkGatewayConnectionName, this.client.subscriptionId(), parameters, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent()).execute();
        return client.getAzureClient().getPutOrPatchResult(result, new TypeToken<VirtualNetworkGatewayConnectionInner>() { }.getType());
    }

    /**
     * The Put VirtualNetworkGatewayConnection operation creates/updates a virtual network gateway connection in the specified resource group through Network resource provider.
     *
     * @param resourceGroupName The name of the resource group.
     * @param virtualNetworkGatewayConnectionName The name of the virtual network gateway conenction.
     * @param parameters Parameters supplied to the Begin Create or update Virtual Network Gateway connection operation through Network resource provider.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    public ServiceCall createOrUpdateAsync(String resourceGroupName, String virtualNetworkGatewayConnectionName, VirtualNetworkGatewayConnectionInner parameters, final ServiceCallback<VirtualNetworkGatewayConnectionInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
        }
        if (virtualNetworkGatewayConnectionName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter virtualNetworkGatewayConnectionName is required and cannot be null."));
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
        }
        if (parameters == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter parameters is required and cannot be null."));
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
        }
        Validator.validate(parameters, serviceCallback);
        Call<ResponseBody> call = service.createOrUpdate(resourceGroupName, virtualNetworkGatewayConnectionName, this.client.subscriptionId(), parameters, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                serviceCallback.failure(t);
            }
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                client.getAzureClient().getPutOrPatchResultAsync(response, new TypeToken<VirtualNetworkGatewayConnectionInner>() { }.getType(), serviceCall, serviceCallback);
            }
        });
        return serviceCall;
    }

    /**
     * The Put VirtualNetworkGatewayConnection operation creates/updates a virtual network gateway connection in the specified resource group through Network resource provider.
     *
     * @param resourceGroupName The name of the resource group.
     * @param virtualNetworkGatewayConnectionName The name of the virtual network gateway conenction.
     * @param parameters Parameters supplied to the Begin Create or update Virtual Network Gateway connection operation through Network resource provider.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the VirtualNetworkGatewayConnectionInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<VirtualNetworkGatewayConnectionInner> beginCreateOrUpdate(String resourceGroupName, String virtualNetworkGatewayConnectionName, VirtualNetworkGatewayConnectionInner parameters) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (virtualNetworkGatewayConnectionName == null) {
            throw new IllegalArgumentException("Parameter virtualNetworkGatewayConnectionName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (parameters == null) {
            throw new IllegalArgumentException("Parameter parameters is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(parameters);
        Call<ResponseBody> call = service.beginCreateOrUpdate(resourceGroupName, virtualNetworkGatewayConnectionName, this.client.subscriptionId(), parameters, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return beginCreateOrUpdateDelegate(call.execute());
    }

    /**
     * The Put VirtualNetworkGatewayConnection operation creates/updates a virtual network gateway connection in the specified resource group through Network resource provider.
     *
     * @param resourceGroupName The name of the resource group.
     * @param virtualNetworkGatewayConnectionName The name of the virtual network gateway conenction.
     * @param parameters Parameters supplied to the Begin Create or update Virtual Network Gateway connection operation through Network resource provider.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall beginCreateOrUpdateAsync(String resourceGroupName, String virtualNetworkGatewayConnectionName, VirtualNetworkGatewayConnectionInner parameters, final ServiceCallback<VirtualNetworkGatewayConnectionInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (virtualNetworkGatewayConnectionName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter virtualNetworkGatewayConnectionName is required and cannot be null."));
            return null;
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
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
        Call<ResponseBody> call = service.beginCreateOrUpdate(resourceGroupName, virtualNetworkGatewayConnectionName, this.client.subscriptionId(), parameters, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<VirtualNetworkGatewayConnectionInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(beginCreateOrUpdateDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<VirtualNetworkGatewayConnectionInner> beginCreateOrUpdateDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<VirtualNetworkGatewayConnectionInner, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<VirtualNetworkGatewayConnectionInner>() { }.getType())
                .register(201, new TypeToken<VirtualNetworkGatewayConnectionInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * The Get VirtualNetworkGatewayConnection operation retrieves information about the specified virtual network gateway connection through Network resource provider.
     *
     * @param resourceGroupName The name of the resource group.
     * @param virtualNetworkGatewayConnectionName The name of the virtual network gateway connection.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the VirtualNetworkGatewayConnectionInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<VirtualNetworkGatewayConnectionInner> get(String resourceGroupName, String virtualNetworkGatewayConnectionName) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (virtualNetworkGatewayConnectionName == null) {
            throw new IllegalArgumentException("Parameter virtualNetworkGatewayConnectionName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.get(resourceGroupName, virtualNetworkGatewayConnectionName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getDelegate(call.execute());
    }

    /**
     * The Get VirtualNetworkGatewayConnection operation retrieves information about the specified virtual network gateway connection through Network resource provider.
     *
     * @param resourceGroupName The name of the resource group.
     * @param virtualNetworkGatewayConnectionName The name of the virtual network gateway connection.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getAsync(String resourceGroupName, String virtualNetworkGatewayConnectionName, final ServiceCallback<VirtualNetworkGatewayConnectionInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (virtualNetworkGatewayConnectionName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter virtualNetworkGatewayConnectionName is required and cannot be null."));
            return null;
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.get(resourceGroupName, virtualNetworkGatewayConnectionName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<VirtualNetworkGatewayConnectionInner>(serviceCallback) {
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

    private ServiceResponse<VirtualNetworkGatewayConnectionInner> getDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<VirtualNetworkGatewayConnectionInner, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<VirtualNetworkGatewayConnectionInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * The Delete VirtualNetworkGatewayConnection operation deletes the specifed virtual network Gateway connection through Network resource provider.
     *
     * @param resourceGroupName The name of the resource group.
     * @param virtualNetworkGatewayConnectionName The name of the virtual network gateway connection.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @throws InterruptedException exception thrown when long running operation is interrupted
     * @return the ServiceResponse object if successful.
     */
    public ServiceResponse<Void> delete(String resourceGroupName, String virtualNetworkGatewayConnectionName) throws CloudException, IOException, IllegalArgumentException, InterruptedException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (virtualNetworkGatewayConnectionName == null) {
            throw new IllegalArgumentException("Parameter virtualNetworkGatewayConnectionName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Response<ResponseBody> result = service.delete(resourceGroupName, virtualNetworkGatewayConnectionName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent()).execute();
        return client.getAzureClient().getPostOrDeleteResult(result, new TypeToken<Void>() { }.getType());
    }

    /**
     * The Delete VirtualNetworkGatewayConnection operation deletes the specifed virtual network Gateway connection through Network resource provider.
     *
     * @param resourceGroupName The name of the resource group.
     * @param virtualNetworkGatewayConnectionName The name of the virtual network gateway connection.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    public ServiceCall deleteAsync(String resourceGroupName, String virtualNetworkGatewayConnectionName, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
        }
        if (virtualNetworkGatewayConnectionName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter virtualNetworkGatewayConnectionName is required and cannot be null."));
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
        }
        Call<ResponseBody> call = service.delete(resourceGroupName, virtualNetworkGatewayConnectionName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                serviceCallback.failure(t);
            }
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                client.getAzureClient().getPostOrDeleteResultAsync(response, new TypeToken<Void>() { }.getType(), serviceCall, serviceCallback);
            }
        });
        return serviceCall;
    }

    /**
     * The Delete VirtualNetworkGatewayConnection operation deletes the specifed virtual network Gateway connection through Network resource provider.
     *
     * @param resourceGroupName The name of the resource group.
     * @param virtualNetworkGatewayConnectionName The name of the virtual network gateway connection.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> beginDelete(String resourceGroupName, String virtualNetworkGatewayConnectionName) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (virtualNetworkGatewayConnectionName == null) {
            throw new IllegalArgumentException("Parameter virtualNetworkGatewayConnectionName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.beginDelete(resourceGroupName, virtualNetworkGatewayConnectionName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return beginDeleteDelegate(call.execute());
    }

    /**
     * The Delete VirtualNetworkGatewayConnection operation deletes the specifed virtual network Gateway connection through Network resource provider.
     *
     * @param resourceGroupName The name of the resource group.
     * @param virtualNetworkGatewayConnectionName The name of the virtual network gateway connection.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall beginDeleteAsync(String resourceGroupName, String virtualNetworkGatewayConnectionName, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (virtualNetworkGatewayConnectionName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter virtualNetworkGatewayConnectionName is required and cannot be null."));
            return null;
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.beginDelete(resourceGroupName, virtualNetworkGatewayConnectionName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(beginDeleteDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<Void> beginDeleteDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .register(202, new TypeToken<Void>() { }.getType())
                .register(204, new TypeToken<Void>() { }.getType())
                .build(response);
    }

    /**
     * The Get VirtualNetworkGatewayConnectionSharedKey operation retrieves information about the specified virtual network gateway connection shared key through Network resource provider.
     *
     * @param resourceGroupName The name of the resource group.
     * @param connectionSharedKeyName The virtual network gateway connection shared key name.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the ConnectionSharedKeyResultInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<ConnectionSharedKeyResultInner> getSharedKey(String resourceGroupName, String connectionSharedKeyName) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (connectionSharedKeyName == null) {
            throw new IllegalArgumentException("Parameter connectionSharedKeyName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.getSharedKey(resourceGroupName, connectionSharedKeyName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getSharedKeyDelegate(call.execute());
    }

    /**
     * The Get VirtualNetworkGatewayConnectionSharedKey operation retrieves information about the specified virtual network gateway connection shared key through Network resource provider.
     *
     * @param resourceGroupName The name of the resource group.
     * @param connectionSharedKeyName The virtual network gateway connection shared key name.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getSharedKeyAsync(String resourceGroupName, String connectionSharedKeyName, final ServiceCallback<ConnectionSharedKeyResultInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (connectionSharedKeyName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter connectionSharedKeyName is required and cannot be null."));
            return null;
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.getSharedKey(resourceGroupName, connectionSharedKeyName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<ConnectionSharedKeyResultInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getSharedKeyDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<ConnectionSharedKeyResultInner> getSharedKeyDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<ConnectionSharedKeyResultInner, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<ConnectionSharedKeyResultInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * The List VirtualNetworkGatewayConnections operation retrieves all the virtual network gateways connections created.
     *
     * @param resourceGroupName The name of the resource group.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;VirtualNetworkGatewayConnectionInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<VirtualNetworkGatewayConnectionInner>> list(final String resourceGroupName) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.list(resourceGroupName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        ServiceResponse<PageImpl<VirtualNetworkGatewayConnectionInner>> response = listDelegate(call.execute());
        PagedList<VirtualNetworkGatewayConnectionInner> result = new PagedList<VirtualNetworkGatewayConnectionInner>(response.getBody()) {
            @Override
            public Page<VirtualNetworkGatewayConnectionInner> nextPage(String nextPageLink) throws CloudException, IOException {
                return listNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * The List VirtualNetworkGatewayConnections operation retrieves all the virtual network gateways connections created.
     *
     * @param resourceGroupName The name of the resource group.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listAsync(final String resourceGroupName, final ListOperationCallback<VirtualNetworkGatewayConnectionInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.list(resourceGroupName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<VirtualNetworkGatewayConnectionInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<VirtualNetworkGatewayConnectionInner>> result = listDelegate(response);
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

    private ServiceResponse<PageImpl<VirtualNetworkGatewayConnectionInner>> listDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<VirtualNetworkGatewayConnectionInner>, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<VirtualNetworkGatewayConnectionInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * The VirtualNetworkGatewayConnectionResetSharedKey operation resets the virtual network gateway connection shared key for passed virtual network gateway connection in the specified resource group through Network resource provider.
     *
     * @param resourceGroupName The name of the resource group.
     * @param virtualNetworkGatewayConnectionName The virtual network gateway connection reset shared key Name.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @throws InterruptedException exception thrown when long running operation is interrupted
     * @return the ConnectionResetSharedKeyInner object wrapped in ServiceResponse if successful.
     */
    public ServiceResponse<ConnectionResetSharedKeyInner> resetSharedKey(String resourceGroupName, String virtualNetworkGatewayConnectionName) throws CloudException, IOException, IllegalArgumentException, InterruptedException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (virtualNetworkGatewayConnectionName == null) {
            throw new IllegalArgumentException("Parameter virtualNetworkGatewayConnectionName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final Long keyLength = null;
        ConnectionResetSharedKeyInner parameters = new ConnectionResetSharedKeyInner();
        parameters.withKeyLength(null);
        Response<ResponseBody> result = service.resetSharedKey(resourceGroupName, virtualNetworkGatewayConnectionName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), parameters, this.client.userAgent()).execute();
        return client.getAzureClient().getPostOrDeleteResult(result, new TypeToken<ConnectionResetSharedKeyInner>() { }.getType());
    }

    /**
     * The VirtualNetworkGatewayConnectionResetSharedKey operation resets the virtual network gateway connection shared key for passed virtual network gateway connection in the specified resource group through Network resource provider.
     *
     * @param resourceGroupName The name of the resource group.
     * @param virtualNetworkGatewayConnectionName The virtual network gateway connection reset shared key Name.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    public ServiceCall resetSharedKeyAsync(String resourceGroupName, String virtualNetworkGatewayConnectionName, final ServiceCallback<ConnectionResetSharedKeyInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
        }
        if (virtualNetworkGatewayConnectionName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter virtualNetworkGatewayConnectionName is required and cannot be null."));
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
        }
        final Long keyLength = null;
        ConnectionResetSharedKeyInner parameters = new ConnectionResetSharedKeyInner();
        parameters.withKeyLength(null);
        Call<ResponseBody> call = service.resetSharedKey(resourceGroupName, virtualNetworkGatewayConnectionName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), parameters, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                serviceCallback.failure(t);
            }
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                client.getAzureClient().getPostOrDeleteResultAsync(response, new TypeToken<ConnectionResetSharedKeyInner>() { }.getType(), serviceCall, serviceCallback);
            }
        });
        return serviceCall;
    }
    /**
     * The VirtualNetworkGatewayConnectionResetSharedKey operation resets the virtual network gateway connection shared key for passed virtual network gateway connection in the specified resource group through Network resource provider.
     *
     * @param resourceGroupName The name of the resource group.
     * @param virtualNetworkGatewayConnectionName The virtual network gateway connection reset shared key Name.
     * @param keyLength The virtual network connection reset shared key length
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @throws InterruptedException exception thrown when long running operation is interrupted
     * @return the ConnectionResetSharedKeyInner object wrapped in ServiceResponse if successful.
     */
    public ServiceResponse<ConnectionResetSharedKeyInner> resetSharedKey(String resourceGroupName, String virtualNetworkGatewayConnectionName, Long keyLength) throws CloudException, IOException, IllegalArgumentException, InterruptedException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (virtualNetworkGatewayConnectionName == null) {
            throw new IllegalArgumentException("Parameter virtualNetworkGatewayConnectionName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        ConnectionResetSharedKeyInner parameters = new ConnectionResetSharedKeyInner();
        parameters.withKeyLength(keyLength);
        Response<ResponseBody> result = service.resetSharedKey(resourceGroupName, virtualNetworkGatewayConnectionName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), parameters, this.client.userAgent()).execute();
        return client.getAzureClient().getPostOrDeleteResult(result, new TypeToken<ConnectionResetSharedKeyInner>() { }.getType());
    }

    /**
     * The VirtualNetworkGatewayConnectionResetSharedKey operation resets the virtual network gateway connection shared key for passed virtual network gateway connection in the specified resource group through Network resource provider.
     *
     * @param resourceGroupName The name of the resource group.
     * @param virtualNetworkGatewayConnectionName The virtual network gateway connection reset shared key Name.
     * @param keyLength The virtual network connection reset shared key length
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    public ServiceCall resetSharedKeyAsync(String resourceGroupName, String virtualNetworkGatewayConnectionName, Long keyLength, final ServiceCallback<ConnectionResetSharedKeyInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
        }
        if (virtualNetworkGatewayConnectionName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter virtualNetworkGatewayConnectionName is required and cannot be null."));
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
        }
        ConnectionResetSharedKeyInner parameters = new ConnectionResetSharedKeyInner();
        parameters.withKeyLength(keyLength);
        Call<ResponseBody> call = service.resetSharedKey(resourceGroupName, virtualNetworkGatewayConnectionName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), parameters, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                serviceCallback.failure(t);
            }
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                client.getAzureClient().getPostOrDeleteResultAsync(response, new TypeToken<ConnectionResetSharedKeyInner>() { }.getType(), serviceCall, serviceCallback);
            }
        });
        return serviceCall;
    }

    /**
     * The VirtualNetworkGatewayConnectionResetSharedKey operation resets the virtual network gateway connection shared key for passed virtual network gateway connection in the specified resource group through Network resource provider.
     *
     * @param resourceGroupName The name of the resource group.
     * @param virtualNetworkGatewayConnectionName The virtual network gateway connection reset shared key Name.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the ConnectionResetSharedKeyInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<ConnectionResetSharedKeyInner> beginResetSharedKey(String resourceGroupName, String virtualNetworkGatewayConnectionName) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (virtualNetworkGatewayConnectionName == null) {
            throw new IllegalArgumentException("Parameter virtualNetworkGatewayConnectionName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final Long keyLength = null;
        ConnectionResetSharedKeyInner parameters = new ConnectionResetSharedKeyInner();
        parameters.withKeyLength(null);
        Call<ResponseBody> call = service.beginResetSharedKey(resourceGroupName, virtualNetworkGatewayConnectionName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), parameters, this.client.userAgent());
        return beginResetSharedKeyDelegate(call.execute());
    }

    /**
     * The VirtualNetworkGatewayConnectionResetSharedKey operation resets the virtual network gateway connection shared key for passed virtual network gateway connection in the specified resource group through Network resource provider.
     *
     * @param resourceGroupName The name of the resource group.
     * @param virtualNetworkGatewayConnectionName The virtual network gateway connection reset shared key Name.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall beginResetSharedKeyAsync(String resourceGroupName, String virtualNetworkGatewayConnectionName, final ServiceCallback<ConnectionResetSharedKeyInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (virtualNetworkGatewayConnectionName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter virtualNetworkGatewayConnectionName is required and cannot be null."));
            return null;
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final Long keyLength = null;
        ConnectionResetSharedKeyInner parameters = new ConnectionResetSharedKeyInner();
        parameters.withKeyLength(null);
        Call<ResponseBody> call = service.beginResetSharedKey(resourceGroupName, virtualNetworkGatewayConnectionName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), parameters, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<ConnectionResetSharedKeyInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(beginResetSharedKeyDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * The VirtualNetworkGatewayConnectionResetSharedKey operation resets the virtual network gateway connection shared key for passed virtual network gateway connection in the specified resource group through Network resource provider.
     *
     * @param resourceGroupName The name of the resource group.
     * @param virtualNetworkGatewayConnectionName The virtual network gateway connection reset shared key Name.
     * @param keyLength The virtual network connection reset shared key length
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the ConnectionResetSharedKeyInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<ConnectionResetSharedKeyInner> beginResetSharedKey(String resourceGroupName, String virtualNetworkGatewayConnectionName, Long keyLength) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (virtualNetworkGatewayConnectionName == null) {
            throw new IllegalArgumentException("Parameter virtualNetworkGatewayConnectionName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        ConnectionResetSharedKeyInner parameters = new ConnectionResetSharedKeyInner();
        parameters.withKeyLength(keyLength);
        Call<ResponseBody> call = service.beginResetSharedKey(resourceGroupName, virtualNetworkGatewayConnectionName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), parameters, this.client.userAgent());
        return beginResetSharedKeyDelegate(call.execute());
    }

    /**
     * The VirtualNetworkGatewayConnectionResetSharedKey operation resets the virtual network gateway connection shared key for passed virtual network gateway connection in the specified resource group through Network resource provider.
     *
     * @param resourceGroupName The name of the resource group.
     * @param virtualNetworkGatewayConnectionName The virtual network gateway connection reset shared key Name.
     * @param keyLength The virtual network connection reset shared key length
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall beginResetSharedKeyAsync(String resourceGroupName, String virtualNetworkGatewayConnectionName, Long keyLength, final ServiceCallback<ConnectionResetSharedKeyInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (virtualNetworkGatewayConnectionName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter virtualNetworkGatewayConnectionName is required and cannot be null."));
            return null;
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        ConnectionResetSharedKeyInner parameters = new ConnectionResetSharedKeyInner();
        parameters.withKeyLength(keyLength);
        Call<ResponseBody> call = service.beginResetSharedKey(resourceGroupName, virtualNetworkGatewayConnectionName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), parameters, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<ConnectionResetSharedKeyInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(beginResetSharedKeyDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<ConnectionResetSharedKeyInner> beginResetSharedKeyDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<ConnectionResetSharedKeyInner, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<ConnectionResetSharedKeyInner>() { }.getType())
                .register(202, new TypeToken<Void>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * The Put VirtualNetworkGatewayConnectionSharedKey operation sets the virtual network gateway connection shared key for passed virtual network gateway connection in the specified resource group through Network resource provider.
     *
     * @param resourceGroupName The name of the resource group.
     * @param virtualNetworkGatewayConnectionName The virtual network gateway connection name.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @throws InterruptedException exception thrown when long running operation is interrupted
     * @return the ConnectionSharedKeyInner object wrapped in ServiceResponse if successful.
     */
    public ServiceResponse<ConnectionSharedKeyInner> setSharedKey(String resourceGroupName, String virtualNetworkGatewayConnectionName) throws CloudException, IOException, IllegalArgumentException, InterruptedException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (virtualNetworkGatewayConnectionName == null) {
            throw new IllegalArgumentException("Parameter virtualNetworkGatewayConnectionName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String value = null;
        ConnectionSharedKeyInner parameters = new ConnectionSharedKeyInner();
        parameters.withValue(null);
        Response<ResponseBody> result = service.setSharedKey(resourceGroupName, virtualNetworkGatewayConnectionName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), parameters, this.client.userAgent()).execute();
        return client.getAzureClient().getPutOrPatchResult(result, new TypeToken<ConnectionSharedKeyInner>() { }.getType());
    }

    /**
     * The Put VirtualNetworkGatewayConnectionSharedKey operation sets the virtual network gateway connection shared key for passed virtual network gateway connection in the specified resource group through Network resource provider.
     *
     * @param resourceGroupName The name of the resource group.
     * @param virtualNetworkGatewayConnectionName The virtual network gateway connection name.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    public ServiceCall setSharedKeyAsync(String resourceGroupName, String virtualNetworkGatewayConnectionName, final ServiceCallback<ConnectionSharedKeyInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
        }
        if (virtualNetworkGatewayConnectionName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter virtualNetworkGatewayConnectionName is required and cannot be null."));
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
        }
        final String value = null;
        ConnectionSharedKeyInner parameters = new ConnectionSharedKeyInner();
        parameters.withValue(null);
        Call<ResponseBody> call = service.setSharedKey(resourceGroupName, virtualNetworkGatewayConnectionName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), parameters, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                serviceCallback.failure(t);
            }
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                client.getAzureClient().getPutOrPatchResultAsync(response, new TypeToken<ConnectionSharedKeyInner>() { }.getType(), serviceCall, serviceCallback);
            }
        });
        return serviceCall;
    }
    /**
     * The Put VirtualNetworkGatewayConnectionSharedKey operation sets the virtual network gateway connection shared key for passed virtual network gateway connection in the specified resource group through Network resource provider.
     *
     * @param resourceGroupName The name of the resource group.
     * @param virtualNetworkGatewayConnectionName The virtual network gateway connection name.
     * @param value The virtual network connection shared key value
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @throws InterruptedException exception thrown when long running operation is interrupted
     * @return the ConnectionSharedKeyInner object wrapped in ServiceResponse if successful.
     */
    public ServiceResponse<ConnectionSharedKeyInner> setSharedKey(String resourceGroupName, String virtualNetworkGatewayConnectionName, String value) throws CloudException, IOException, IllegalArgumentException, InterruptedException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (virtualNetworkGatewayConnectionName == null) {
            throw new IllegalArgumentException("Parameter virtualNetworkGatewayConnectionName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        ConnectionSharedKeyInner parameters = new ConnectionSharedKeyInner();
        parameters.withValue(value);
        Response<ResponseBody> result = service.setSharedKey(resourceGroupName, virtualNetworkGatewayConnectionName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), parameters, this.client.userAgent()).execute();
        return client.getAzureClient().getPutOrPatchResult(result, new TypeToken<ConnectionSharedKeyInner>() { }.getType());
    }

    /**
     * The Put VirtualNetworkGatewayConnectionSharedKey operation sets the virtual network gateway connection shared key for passed virtual network gateway connection in the specified resource group through Network resource provider.
     *
     * @param resourceGroupName The name of the resource group.
     * @param virtualNetworkGatewayConnectionName The virtual network gateway connection name.
     * @param value The virtual network connection shared key value
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    public ServiceCall setSharedKeyAsync(String resourceGroupName, String virtualNetworkGatewayConnectionName, String value, final ServiceCallback<ConnectionSharedKeyInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
        }
        if (virtualNetworkGatewayConnectionName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter virtualNetworkGatewayConnectionName is required and cannot be null."));
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
        }
        ConnectionSharedKeyInner parameters = new ConnectionSharedKeyInner();
        parameters.withValue(value);
        Call<ResponseBody> call = service.setSharedKey(resourceGroupName, virtualNetworkGatewayConnectionName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), parameters, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                serviceCallback.failure(t);
            }
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                client.getAzureClient().getPutOrPatchResultAsync(response, new TypeToken<ConnectionSharedKeyInner>() { }.getType(), serviceCall, serviceCallback);
            }
        });
        return serviceCall;
    }

    /**
     * The Put VirtualNetworkGatewayConnectionSharedKey operation sets the virtual network gateway connection shared key for passed virtual network gateway connection in the specified resource group through Network resource provider.
     *
     * @param resourceGroupName The name of the resource group.
     * @param virtualNetworkGatewayConnectionName The virtual network gateway connection name.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the ConnectionSharedKeyInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<ConnectionSharedKeyInner> beginSetSharedKey(String resourceGroupName, String virtualNetworkGatewayConnectionName) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (virtualNetworkGatewayConnectionName == null) {
            throw new IllegalArgumentException("Parameter virtualNetworkGatewayConnectionName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String value = null;
        ConnectionSharedKeyInner parameters = new ConnectionSharedKeyInner();
        parameters.withValue(null);
        Call<ResponseBody> call = service.beginSetSharedKey(resourceGroupName, virtualNetworkGatewayConnectionName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), parameters, this.client.userAgent());
        return beginSetSharedKeyDelegate(call.execute());
    }

    /**
     * The Put VirtualNetworkGatewayConnectionSharedKey operation sets the virtual network gateway connection shared key for passed virtual network gateway connection in the specified resource group through Network resource provider.
     *
     * @param resourceGroupName The name of the resource group.
     * @param virtualNetworkGatewayConnectionName The virtual network gateway connection name.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall beginSetSharedKeyAsync(String resourceGroupName, String virtualNetworkGatewayConnectionName, final ServiceCallback<ConnectionSharedKeyInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (virtualNetworkGatewayConnectionName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter virtualNetworkGatewayConnectionName is required and cannot be null."));
            return null;
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final String value = null;
        ConnectionSharedKeyInner parameters = new ConnectionSharedKeyInner();
        parameters.withValue(null);
        Call<ResponseBody> call = service.beginSetSharedKey(resourceGroupName, virtualNetworkGatewayConnectionName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), parameters, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<ConnectionSharedKeyInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(beginSetSharedKeyDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * The Put VirtualNetworkGatewayConnectionSharedKey operation sets the virtual network gateway connection shared key for passed virtual network gateway connection in the specified resource group through Network resource provider.
     *
     * @param resourceGroupName The name of the resource group.
     * @param virtualNetworkGatewayConnectionName The virtual network gateway connection name.
     * @param value The virtual network connection shared key value
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the ConnectionSharedKeyInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<ConnectionSharedKeyInner> beginSetSharedKey(String resourceGroupName, String virtualNetworkGatewayConnectionName, String value) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (virtualNetworkGatewayConnectionName == null) {
            throw new IllegalArgumentException("Parameter virtualNetworkGatewayConnectionName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        ConnectionSharedKeyInner parameters = new ConnectionSharedKeyInner();
        parameters.withValue(value);
        Call<ResponseBody> call = service.beginSetSharedKey(resourceGroupName, virtualNetworkGatewayConnectionName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), parameters, this.client.userAgent());
        return beginSetSharedKeyDelegate(call.execute());
    }

    /**
     * The Put VirtualNetworkGatewayConnectionSharedKey operation sets the virtual network gateway connection shared key for passed virtual network gateway connection in the specified resource group through Network resource provider.
     *
     * @param resourceGroupName The name of the resource group.
     * @param virtualNetworkGatewayConnectionName The virtual network gateway connection name.
     * @param value The virtual network connection shared key value
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall beginSetSharedKeyAsync(String resourceGroupName, String virtualNetworkGatewayConnectionName, String value, final ServiceCallback<ConnectionSharedKeyInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (virtualNetworkGatewayConnectionName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter virtualNetworkGatewayConnectionName is required and cannot be null."));
            return null;
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        ConnectionSharedKeyInner parameters = new ConnectionSharedKeyInner();
        parameters.withValue(value);
        Call<ResponseBody> call = service.beginSetSharedKey(resourceGroupName, virtualNetworkGatewayConnectionName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), parameters, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<ConnectionSharedKeyInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(beginSetSharedKeyDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<ConnectionSharedKeyInner> beginSetSharedKeyDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<ConnectionSharedKeyInner, CloudException>(this.client.restClient().mapperAdapter())
                .register(201, new TypeToken<ConnectionSharedKeyInner>() { }.getType())
                .register(200, new TypeToken<ConnectionSharedKeyInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * The List VirtualNetworkGatewayConnections operation retrieves all the virtual network gateways connections created.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;VirtualNetworkGatewayConnectionInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl<VirtualNetworkGatewayConnectionInner>> listNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        return listNextDelegate(call.execute());
    }

    /**
     * The List VirtualNetworkGatewayConnections operation retrieves all the virtual network gateways connections created.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<VirtualNetworkGatewayConnectionInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<VirtualNetworkGatewayConnectionInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<VirtualNetworkGatewayConnectionInner>> result = listNextDelegate(response);
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

    private ServiceResponse<PageImpl<VirtualNetworkGatewayConnectionInner>> listNextDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<VirtualNetworkGatewayConnectionInner>, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<VirtualNetworkGatewayConnectionInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

}
