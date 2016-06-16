/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

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
import retrofit2.http.PATCH;
import retrofit2.http.Path;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;
import retrofit2.http.Url;
import retrofit2.Response;

/**
 * An instance of this class provides access to all the operations defined
 * in ServerFarms.
 */
public final class ServerFarmsInner {
    /** The Retrofit service to perform REST calls. */
    private ServerFarmsService service;
    /** The service client containing this operation class. */
    private WebSiteManagementClientImpl client;

    /**
     * Initializes an instance of ServerFarmsInner.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public ServerFarmsInner(Retrofit retrofit, WebSiteManagementClientImpl client) {
        this.service = retrofit.create(ServerFarmsService.class);
        this.client = client;
    }

    /**
     * The interface defining all the services for ServerFarms to be
     * used by Retrofit to perform actually REST calls.
     */
    interface ServerFarmsService {
        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/serverfarms")
        Call<ResponseBody> getServerFarms(@Path("resourceGroupName") String resourceGroupName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/serverfarms/{name}")
        Call<ResponseBody> getServerFarm(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @PUT("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/serverfarms/{name}")
        Call<ResponseBody> createOrUpdateServerFarm(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("subscriptionId") String subscriptionId, @Body ServerFarmWithRichSkuInner serverFarmEnvelope, @Query("allowPendingState") Boolean allowPendingState, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @PUT("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/serverfarms/{name}")
        Call<ResponseBody> beginCreateOrUpdateServerFarm(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("subscriptionId") String subscriptionId, @Body ServerFarmWithRichSkuInner serverFarmEnvelope, @Query("allowPendingState") Boolean allowPendingState, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @HTTP(path = "subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/serverfarms/{name}", method = "DELETE", hasBody = true)
        Call<ResponseBody> deleteServerFarm(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/serverfarms/{name}/metrics")
        Call<ResponseBody> getServerFarmMetrics(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("subscriptionId") String subscriptionId, @Query("details") Boolean details, @Query("$filter") String filter, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/serverfarms/{name}/metricdefinitions")
        Call<ResponseBody> getServerFarmMetricDefintions(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/serverfarms/{name}/virtualNetworkConnections")
        Call<ResponseBody> getVnetsForServerFarm(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/serverfarms/{name}/virtualNetworkConnections/{vnetName}")
        Call<ResponseBody> getVnetFromServerFarm(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("vnetName") String vnetName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/serverfarms/{name}/virtualNetworkConnections/{vnetName}/routes")
        Call<ResponseBody> getRoutesForVnet(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("vnetName") String vnetName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/serverfarms/{name}/virtualNetworkConnections/{vnetName}/routes/{routeName}")
        Call<ResponseBody> getRouteForVnet(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("vnetName") String vnetName, @Path("routeName") String routeName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @PUT("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/serverfarms/{name}/virtualNetworkConnections/{vnetName}/routes/{routeName}")
        Call<ResponseBody> createOrUpdateVnetRoute(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("vnetName") String vnetName, @Path("routeName") String routeName, @Path("subscriptionId") String subscriptionId, @Body VnetRouteInner route, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @HTTP(path = "subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/serverfarms/{name}/virtualNetworkConnections/{vnetName}/routes/{routeName}", method = "DELETE", hasBody = true)
        Call<ResponseBody> deleteVnetRoute(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("vnetName") String vnetName, @Path("routeName") String routeName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @PATCH("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/serverfarms/{name}/virtualNetworkConnections/{vnetName}/routes/{routeName}")
        Call<ResponseBody> updateVnetRoute(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("vnetName") String vnetName, @Path("routeName") String routeName, @Path("subscriptionId") String subscriptionId, @Body VnetRouteInner route, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/serverfarms/{name}/virtualNetworkConnections/{vnetName}/gateways/{gatewayName}")
        Call<ResponseBody> getServerFarmVnetGateway(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("vnetName") String vnetName, @Path("gatewayName") String gatewayName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @PUT("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/serverfarms/{name}/virtualNetworkConnections/{vnetName}/gateways/{gatewayName}")
        Call<ResponseBody> updateServerFarmVnetGateway(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("vnetName") String vnetName, @Path("gatewayName") String gatewayName, @Path("subscriptionId") String subscriptionId, @Body VnetGatewayInner connectionEnvelope, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/serverfarms/{name}/sites")
        Call<ResponseBody> getServerFarmSites(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("subscriptionId") String subscriptionId, @Query("$skipToken") String skipToken, @Query("$filter") String filter, @Query("$top") String top, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @POST("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/serverfarms/{name}/workers/{workerName}/reboot")
        Call<ResponseBody> rebootWorkerForServerFarm(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("workerName") String workerName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @POST("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/serverfarms/{name}/restartSites")
        Call<ResponseBody> restartSitesForServerFarm(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("subscriptionId") String subscriptionId, @Query("softRestart") Boolean softRestart, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/serverfarms/{name}/operationresults/{operationId}")
        Call<ResponseBody> getServerFarmOperation(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("operationId") String operationId, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET
        Call<ResponseBody> getServerFarmSitesNext(@Url String nextPageLink, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

    }

    /**
     * Gets collection of App Service Plans in a resource group for a given subscription.
     *
     * @param resourceGroupName Name of resource group
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the ServerFarmCollectionInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<ServerFarmCollectionInner> getServerFarms(String resourceGroupName) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.getServerFarms(resourceGroupName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getServerFarmsDelegate(call.execute());
    }

    /**
     * Gets collection of App Service Plans in a resource group for a given subscription.
     *
     * @param resourceGroupName Name of resource group
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getServerFarmsAsync(String resourceGroupName, final ServiceCallback<ServerFarmCollectionInner> serviceCallback) throws IllegalArgumentException {
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
        Call<ResponseBody> call = service.getServerFarms(resourceGroupName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<ServerFarmCollectionInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getServerFarmsDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<ServerFarmCollectionInner> getServerFarmsDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<ServerFarmCollectionInner, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<ServerFarmCollectionInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Gets specified App Service Plan in a resource group.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of App Service Plan
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the ServerFarmWithRichSkuInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<ServerFarmWithRichSkuInner> getServerFarm(String resourceGroupName, String name) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.getServerFarm(resourceGroupName, name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getServerFarmDelegate(call.execute());
    }

    /**
     * Gets specified App Service Plan in a resource group.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of App Service Plan
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getServerFarmAsync(String resourceGroupName, String name, final ServiceCallback<ServerFarmWithRichSkuInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (name == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter name is required and cannot be null."));
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
        Call<ResponseBody> call = service.getServerFarm(resourceGroupName, name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<ServerFarmWithRichSkuInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getServerFarmDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<ServerFarmWithRichSkuInner> getServerFarmDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<ServerFarmWithRichSkuInner, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<ServerFarmWithRichSkuInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Creates or updates an App Service Plan.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of App Service Plan
     * @param serverFarmEnvelope Details of App Service Plan
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @throws InterruptedException exception thrown when long running operation is interrupted
     * @return the ServerFarmWithRichSkuInner object wrapped in ServiceResponse if successful.
     */
    public ServiceResponse<ServerFarmWithRichSkuInner> createOrUpdateServerFarm(String resourceGroupName, String name, ServerFarmWithRichSkuInner serverFarmEnvelope) throws CloudException, IOException, IllegalArgumentException, InterruptedException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (serverFarmEnvelope == null) {
            throw new IllegalArgumentException("Parameter serverFarmEnvelope is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(serverFarmEnvelope);
        final Boolean allowPendingState = null;
        Response<ResponseBody> result = service.createOrUpdateServerFarm(resourceGroupName, name, this.client.subscriptionId(), serverFarmEnvelope, allowPendingState, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent()).execute();
        return client.getAzureClient().getPutOrPatchResult(result, new TypeToken<ServerFarmWithRichSkuInner>() { }.getType());
    }

    /**
     * Creates or updates an App Service Plan.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of App Service Plan
     * @param serverFarmEnvelope Details of App Service Plan
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    public ServiceCall createOrUpdateServerFarmAsync(String resourceGroupName, String name, ServerFarmWithRichSkuInner serverFarmEnvelope, final ServiceCallback<ServerFarmWithRichSkuInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
        }
        if (name == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter name is required and cannot be null."));
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
        }
        if (serverFarmEnvelope == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter serverFarmEnvelope is required and cannot be null."));
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
        }
        Validator.validate(serverFarmEnvelope, serviceCallback);
        final Boolean allowPendingState = null;
        Call<ResponseBody> call = service.createOrUpdateServerFarm(resourceGroupName, name, this.client.subscriptionId(), serverFarmEnvelope, allowPendingState, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                serviceCallback.failure(t);
            }
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                client.getAzureClient().getPutOrPatchResultAsync(response, new TypeToken<ServerFarmWithRichSkuInner>() { }.getType(), serviceCall, serviceCallback);
            }
        });
        return serviceCall;
    }
    /**
     * Creates or updates an App Service Plan.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of App Service Plan
     * @param serverFarmEnvelope Details of App Service Plan
     * @param allowPendingState OBSOLETE: If true, allow pending state for App Service Plan
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @throws InterruptedException exception thrown when long running operation is interrupted
     * @return the ServerFarmWithRichSkuInner object wrapped in ServiceResponse if successful.
     */
    public ServiceResponse<ServerFarmWithRichSkuInner> createOrUpdateServerFarm(String resourceGroupName, String name, ServerFarmWithRichSkuInner serverFarmEnvelope, Boolean allowPendingState) throws CloudException, IOException, IllegalArgumentException, InterruptedException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (serverFarmEnvelope == null) {
            throw new IllegalArgumentException("Parameter serverFarmEnvelope is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(serverFarmEnvelope);
        Response<ResponseBody> result = service.createOrUpdateServerFarm(resourceGroupName, name, this.client.subscriptionId(), serverFarmEnvelope, allowPendingState, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent()).execute();
        return client.getAzureClient().getPutOrPatchResult(result, new TypeToken<ServerFarmWithRichSkuInner>() { }.getType());
    }

    /**
     * Creates or updates an App Service Plan.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of App Service Plan
     * @param serverFarmEnvelope Details of App Service Plan
     * @param allowPendingState OBSOLETE: If true, allow pending state for App Service Plan
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    public ServiceCall createOrUpdateServerFarmAsync(String resourceGroupName, String name, ServerFarmWithRichSkuInner serverFarmEnvelope, Boolean allowPendingState, final ServiceCallback<ServerFarmWithRichSkuInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
        }
        if (name == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter name is required and cannot be null."));
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
        }
        if (serverFarmEnvelope == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter serverFarmEnvelope is required and cannot be null."));
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
        }
        Validator.validate(serverFarmEnvelope, serviceCallback);
        Call<ResponseBody> call = service.createOrUpdateServerFarm(resourceGroupName, name, this.client.subscriptionId(), serverFarmEnvelope, allowPendingState, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                serviceCallback.failure(t);
            }
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                client.getAzureClient().getPutOrPatchResultAsync(response, new TypeToken<ServerFarmWithRichSkuInner>() { }.getType(), serviceCall, serviceCallback);
            }
        });
        return serviceCall;
    }

    /**
     * Creates or updates an App Service Plan.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of App Service Plan
     * @param serverFarmEnvelope Details of App Service Plan
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the ServerFarmWithRichSkuInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<ServerFarmWithRichSkuInner> beginCreateOrUpdateServerFarm(String resourceGroupName, String name, ServerFarmWithRichSkuInner serverFarmEnvelope) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (serverFarmEnvelope == null) {
            throw new IllegalArgumentException("Parameter serverFarmEnvelope is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(serverFarmEnvelope);
        final Boolean allowPendingState = null;
        Call<ResponseBody> call = service.beginCreateOrUpdateServerFarm(resourceGroupName, name, this.client.subscriptionId(), serverFarmEnvelope, allowPendingState, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return beginCreateOrUpdateServerFarmDelegate(call.execute());
    }

    /**
     * Creates or updates an App Service Plan.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of App Service Plan
     * @param serverFarmEnvelope Details of App Service Plan
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall beginCreateOrUpdateServerFarmAsync(String resourceGroupName, String name, ServerFarmWithRichSkuInner serverFarmEnvelope, final ServiceCallback<ServerFarmWithRichSkuInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (name == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter name is required and cannot be null."));
            return null;
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
            return null;
        }
        if (serverFarmEnvelope == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter serverFarmEnvelope is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(serverFarmEnvelope, serviceCallback);
        final Boolean allowPendingState = null;
        Call<ResponseBody> call = service.beginCreateOrUpdateServerFarm(resourceGroupName, name, this.client.subscriptionId(), serverFarmEnvelope, allowPendingState, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<ServerFarmWithRichSkuInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(beginCreateOrUpdateServerFarmDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Creates or updates an App Service Plan.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of App Service Plan
     * @param serverFarmEnvelope Details of App Service Plan
     * @param allowPendingState OBSOLETE: If true, allow pending state for App Service Plan
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the ServerFarmWithRichSkuInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<ServerFarmWithRichSkuInner> beginCreateOrUpdateServerFarm(String resourceGroupName, String name, ServerFarmWithRichSkuInner serverFarmEnvelope, Boolean allowPendingState) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (serverFarmEnvelope == null) {
            throw new IllegalArgumentException("Parameter serverFarmEnvelope is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(serverFarmEnvelope);
        Call<ResponseBody> call = service.beginCreateOrUpdateServerFarm(resourceGroupName, name, this.client.subscriptionId(), serverFarmEnvelope, allowPendingState, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return beginCreateOrUpdateServerFarmDelegate(call.execute());
    }

    /**
     * Creates or updates an App Service Plan.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of App Service Plan
     * @param serverFarmEnvelope Details of App Service Plan
     * @param allowPendingState OBSOLETE: If true, allow pending state for App Service Plan
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall beginCreateOrUpdateServerFarmAsync(String resourceGroupName, String name, ServerFarmWithRichSkuInner serverFarmEnvelope, Boolean allowPendingState, final ServiceCallback<ServerFarmWithRichSkuInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (name == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter name is required and cannot be null."));
            return null;
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
            return null;
        }
        if (serverFarmEnvelope == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter serverFarmEnvelope is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(serverFarmEnvelope, serviceCallback);
        Call<ResponseBody> call = service.beginCreateOrUpdateServerFarm(resourceGroupName, name, this.client.subscriptionId(), serverFarmEnvelope, allowPendingState, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<ServerFarmWithRichSkuInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(beginCreateOrUpdateServerFarmDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<ServerFarmWithRichSkuInner> beginCreateOrUpdateServerFarmDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<ServerFarmWithRichSkuInner, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<ServerFarmWithRichSkuInner>() { }.getType())
                .register(202, new TypeToken<ServerFarmWithRichSkuInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Deletes a App Service Plan.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of App Service Plan
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the Object object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<Object> deleteServerFarm(String resourceGroupName, String name) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.deleteServerFarm(resourceGroupName, name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return deleteServerFarmDelegate(call.execute());
    }

    /**
     * Deletes a App Service Plan.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of App Service Plan
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall deleteServerFarmAsync(String resourceGroupName, String name, final ServiceCallback<Object> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (name == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter name is required and cannot be null."));
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
        Call<ResponseBody> call = service.deleteServerFarm(resourceGroupName, name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Object>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(deleteServerFarmDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<Object> deleteServerFarmDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Object, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<Object>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Queries for App Serice Plan metrics.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of App Service Plan
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the ResourceMetricCollectionInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<ResourceMetricCollectionInner> getServerFarmMetrics(String resourceGroupName, String name) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final Boolean details = null;
        final String filter = null;
        Call<ResponseBody> call = service.getServerFarmMetrics(resourceGroupName, name, this.client.subscriptionId(), details, filter, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getServerFarmMetricsDelegate(call.execute());
    }

    /**
     * Queries for App Serice Plan metrics.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of App Service Plan
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getServerFarmMetricsAsync(String resourceGroupName, String name, final ServiceCallback<ResourceMetricCollectionInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (name == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter name is required and cannot be null."));
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
        final Boolean details = null;
        final String filter = null;
        Call<ResponseBody> call = service.getServerFarmMetrics(resourceGroupName, name, this.client.subscriptionId(), details, filter, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<ResourceMetricCollectionInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getServerFarmMetricsDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Queries for App Serice Plan metrics.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of App Service Plan
     * @param details If true, metrics are broken down per App Service Plan instance
     * @param filter Return only usages/metrics specified in the filter. Filter conforms to odata syntax. Example: $filter=(name.value eq 'Metric1' or name.value eq 'Metric2') and startTime eq '2014-01-01T00:00:00Z' and endTime eq '2014-12-31T23:59:59Z' and timeGrain eq duration'[Hour|Minute|Day]'.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the ResourceMetricCollectionInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<ResourceMetricCollectionInner> getServerFarmMetrics(String resourceGroupName, String name, Boolean details, String filter) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.getServerFarmMetrics(resourceGroupName, name, this.client.subscriptionId(), details, filter, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getServerFarmMetricsDelegate(call.execute());
    }

    /**
     * Queries for App Serice Plan metrics.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of App Service Plan
     * @param details If true, metrics are broken down per App Service Plan instance
     * @param filter Return only usages/metrics specified in the filter. Filter conforms to odata syntax. Example: $filter=(name.value eq 'Metric1' or name.value eq 'Metric2') and startTime eq '2014-01-01T00:00:00Z' and endTime eq '2014-12-31T23:59:59Z' and timeGrain eq duration'[Hour|Minute|Day]'.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getServerFarmMetricsAsync(String resourceGroupName, String name, Boolean details, String filter, final ServiceCallback<ResourceMetricCollectionInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (name == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter name is required and cannot be null."));
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
        Call<ResponseBody> call = service.getServerFarmMetrics(resourceGroupName, name, this.client.subscriptionId(), details, filter, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<ResourceMetricCollectionInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getServerFarmMetricsDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<ResourceMetricCollectionInner> getServerFarmMetricsDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<ResourceMetricCollectionInner, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<ResourceMetricCollectionInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * List of metrics that can be queried for an App Service Plan.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of App Service Plan
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the MetricDefinitionCollectionInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<MetricDefinitionCollectionInner> getServerFarmMetricDefintions(String resourceGroupName, String name) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.getServerFarmMetricDefintions(resourceGroupName, name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getServerFarmMetricDefintionsDelegate(call.execute());
    }

    /**
     * List of metrics that can be queried for an App Service Plan.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of App Service Plan
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getServerFarmMetricDefintionsAsync(String resourceGroupName, String name, final ServiceCallback<MetricDefinitionCollectionInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (name == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter name is required and cannot be null."));
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
        Call<ResponseBody> call = service.getServerFarmMetricDefintions(resourceGroupName, name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<MetricDefinitionCollectionInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getServerFarmMetricDefintionsDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<MetricDefinitionCollectionInner> getServerFarmMetricDefintionsDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<MetricDefinitionCollectionInner, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<MetricDefinitionCollectionInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Gets list of vnets associated with App Service Plan.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of App Service Plan
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;VnetInfoInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<List<VnetInfoInner>> getVnetsForServerFarm(String resourceGroupName, String name) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.getVnetsForServerFarm(resourceGroupName, name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getVnetsForServerFarmDelegate(call.execute());
    }

    /**
     * Gets list of vnets associated with App Service Plan.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of App Service Plan
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getVnetsForServerFarmAsync(String resourceGroupName, String name, final ServiceCallback<List<VnetInfoInner>> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (name == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter name is required and cannot be null."));
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
        Call<ResponseBody> call = service.getVnetsForServerFarm(resourceGroupName, name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<VnetInfoInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getVnetsForServerFarmDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<List<VnetInfoInner>> getVnetsForServerFarmDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<List<VnetInfoInner>, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<List<VnetInfoInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Gets a vnet associated with an App Service Plan.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of App Service Plan
     * @param vnetName Name of virtual network
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the VnetInfoInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<VnetInfoInner> getVnetFromServerFarm(String resourceGroupName, String name, String vnetName) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
        }
        if (vnetName == null) {
            throw new IllegalArgumentException("Parameter vnetName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.getVnetFromServerFarm(resourceGroupName, name, vnetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getVnetFromServerFarmDelegate(call.execute());
    }

    /**
     * Gets a vnet associated with an App Service Plan.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of App Service Plan
     * @param vnetName Name of virtual network
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getVnetFromServerFarmAsync(String resourceGroupName, String name, String vnetName, final ServiceCallback<VnetInfoInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (name == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter name is required and cannot be null."));
            return null;
        }
        if (vnetName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter vnetName is required and cannot be null."));
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
        Call<ResponseBody> call = service.getVnetFromServerFarm(resourceGroupName, name, vnetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<VnetInfoInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getVnetFromServerFarmDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<VnetInfoInner> getVnetFromServerFarmDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<VnetInfoInner, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<VnetInfoInner>() { }.getType())
                .register(404, new TypeToken<Void>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Gets a list of all routes associated with a vnet, in an app service plan.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of App Service Plan
     * @param vnetName Name of virtual network
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;VnetRouteInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<List<VnetRouteInner>> getRoutesForVnet(String resourceGroupName, String name, String vnetName) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
        }
        if (vnetName == null) {
            throw new IllegalArgumentException("Parameter vnetName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.getRoutesForVnet(resourceGroupName, name, vnetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getRoutesForVnetDelegate(call.execute());
    }

    /**
     * Gets a list of all routes associated with a vnet, in an app service plan.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of App Service Plan
     * @param vnetName Name of virtual network
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getRoutesForVnetAsync(String resourceGroupName, String name, String vnetName, final ServiceCallback<List<VnetRouteInner>> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (name == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter name is required and cannot be null."));
            return null;
        }
        if (vnetName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter vnetName is required and cannot be null."));
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
        Call<ResponseBody> call = service.getRoutesForVnet(resourceGroupName, name, vnetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<VnetRouteInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getRoutesForVnetDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<List<VnetRouteInner>> getRoutesForVnetDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<List<VnetRouteInner>, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<List<VnetRouteInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Gets a specific route associated with a vnet, in an app service plan.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of App Service Plan
     * @param vnetName Name of virtual network
     * @param routeName Name of the virtual network route
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;VnetRouteInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<List<VnetRouteInner>> getRouteForVnet(String resourceGroupName, String name, String vnetName, String routeName) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
        }
        if (vnetName == null) {
            throw new IllegalArgumentException("Parameter vnetName is required and cannot be null.");
        }
        if (routeName == null) {
            throw new IllegalArgumentException("Parameter routeName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.getRouteForVnet(resourceGroupName, name, vnetName, routeName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getRouteForVnetDelegate(call.execute());
    }

    /**
     * Gets a specific route associated with a vnet, in an app service plan.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of App Service Plan
     * @param vnetName Name of virtual network
     * @param routeName Name of the virtual network route
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getRouteForVnetAsync(String resourceGroupName, String name, String vnetName, String routeName, final ServiceCallback<List<VnetRouteInner>> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (name == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter name is required and cannot be null."));
            return null;
        }
        if (vnetName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter vnetName is required and cannot be null."));
            return null;
        }
        if (routeName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter routeName is required and cannot be null."));
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
        Call<ResponseBody> call = service.getRouteForVnet(resourceGroupName, name, vnetName, routeName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<VnetRouteInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getRouteForVnetDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<List<VnetRouteInner>> getRouteForVnetDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<List<VnetRouteInner>, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<List<VnetRouteInner>>() { }.getType())
                .register(404, new TypeToken<Void>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Creates a new route or updates an existing route for a vnet in an app service plan.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of App Service Plan
     * @param vnetName Name of virtual network
     * @param routeName Name of the virtual network route
     * @param route The route object
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the VnetRouteInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<VnetRouteInner> createOrUpdateVnetRoute(String resourceGroupName, String name, String vnetName, String routeName, VnetRouteInner route) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
        }
        if (vnetName == null) {
            throw new IllegalArgumentException("Parameter vnetName is required and cannot be null.");
        }
        if (routeName == null) {
            throw new IllegalArgumentException("Parameter routeName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (route == null) {
            throw new IllegalArgumentException("Parameter route is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(route);
        Call<ResponseBody> call = service.createOrUpdateVnetRoute(resourceGroupName, name, vnetName, routeName, this.client.subscriptionId(), route, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return createOrUpdateVnetRouteDelegate(call.execute());
    }

    /**
     * Creates a new route or updates an existing route for a vnet in an app service plan.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of App Service Plan
     * @param vnetName Name of virtual network
     * @param routeName Name of the virtual network route
     * @param route The route object
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall createOrUpdateVnetRouteAsync(String resourceGroupName, String name, String vnetName, String routeName, VnetRouteInner route, final ServiceCallback<VnetRouteInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (name == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter name is required and cannot be null."));
            return null;
        }
        if (vnetName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter vnetName is required and cannot be null."));
            return null;
        }
        if (routeName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter routeName is required and cannot be null."));
            return null;
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
            return null;
        }
        if (route == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter route is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(route, serviceCallback);
        Call<ResponseBody> call = service.createOrUpdateVnetRoute(resourceGroupName, name, vnetName, routeName, this.client.subscriptionId(), route, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<VnetRouteInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(createOrUpdateVnetRouteDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<VnetRouteInner> createOrUpdateVnetRouteDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<VnetRouteInner, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<VnetRouteInner>() { }.getType())
                .register(400, new TypeToken<Void>() { }.getType())
                .register(404, new TypeToken<Void>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Deletes an existing route for a vnet in an app service plan.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of App Service Plan
     * @param vnetName Name of virtual network
     * @param routeName Name of the virtual network route
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the Object object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<Object> deleteVnetRoute(String resourceGroupName, String name, String vnetName, String routeName) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
        }
        if (vnetName == null) {
            throw new IllegalArgumentException("Parameter vnetName is required and cannot be null.");
        }
        if (routeName == null) {
            throw new IllegalArgumentException("Parameter routeName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.deleteVnetRoute(resourceGroupName, name, vnetName, routeName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return deleteVnetRouteDelegate(call.execute());
    }

    /**
     * Deletes an existing route for a vnet in an app service plan.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of App Service Plan
     * @param vnetName Name of virtual network
     * @param routeName Name of the virtual network route
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall deleteVnetRouteAsync(String resourceGroupName, String name, String vnetName, String routeName, final ServiceCallback<Object> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (name == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter name is required and cannot be null."));
            return null;
        }
        if (vnetName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter vnetName is required and cannot be null."));
            return null;
        }
        if (routeName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter routeName is required and cannot be null."));
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
        Call<ResponseBody> call = service.deleteVnetRoute(resourceGroupName, name, vnetName, routeName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Object>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(deleteVnetRouteDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<Object> deleteVnetRouteDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Object, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<Object>() { }.getType())
                .register(404, new TypeToken<Void>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Creates a new route or updates an existing route for a vnet in an app service plan.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of App Service Plan
     * @param vnetName Name of virtual network
     * @param routeName Name of the virtual network route
     * @param route The route object
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the VnetRouteInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<VnetRouteInner> updateVnetRoute(String resourceGroupName, String name, String vnetName, String routeName, VnetRouteInner route) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
        }
        if (vnetName == null) {
            throw new IllegalArgumentException("Parameter vnetName is required and cannot be null.");
        }
        if (routeName == null) {
            throw new IllegalArgumentException("Parameter routeName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (route == null) {
            throw new IllegalArgumentException("Parameter route is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(route);
        Call<ResponseBody> call = service.updateVnetRoute(resourceGroupName, name, vnetName, routeName, this.client.subscriptionId(), route, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return updateVnetRouteDelegate(call.execute());
    }

    /**
     * Creates a new route or updates an existing route for a vnet in an app service plan.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of App Service Plan
     * @param vnetName Name of virtual network
     * @param routeName Name of the virtual network route
     * @param route The route object
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall updateVnetRouteAsync(String resourceGroupName, String name, String vnetName, String routeName, VnetRouteInner route, final ServiceCallback<VnetRouteInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (name == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter name is required and cannot be null."));
            return null;
        }
        if (vnetName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter vnetName is required and cannot be null."));
            return null;
        }
        if (routeName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter routeName is required and cannot be null."));
            return null;
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
            return null;
        }
        if (route == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter route is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(route, serviceCallback);
        Call<ResponseBody> call = service.updateVnetRoute(resourceGroupName, name, vnetName, routeName, this.client.subscriptionId(), route, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<VnetRouteInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(updateVnetRouteDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<VnetRouteInner> updateVnetRouteDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<VnetRouteInner, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<VnetRouteInner>() { }.getType())
                .register(400, new TypeToken<Void>() { }.getType())
                .register(404, new TypeToken<Void>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Gets the vnet gateway.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of the App Service Plan
     * @param vnetName Name of the virtual network
     * @param gatewayName Name of the gateway. Only the 'primary' gateway is supported.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the VnetGatewayInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<VnetGatewayInner> getServerFarmVnetGateway(String resourceGroupName, String name, String vnetName, String gatewayName) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
        }
        if (vnetName == null) {
            throw new IllegalArgumentException("Parameter vnetName is required and cannot be null.");
        }
        if (gatewayName == null) {
            throw new IllegalArgumentException("Parameter gatewayName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.getServerFarmVnetGateway(resourceGroupName, name, vnetName, gatewayName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getServerFarmVnetGatewayDelegate(call.execute());
    }

    /**
     * Gets the vnet gateway.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of the App Service Plan
     * @param vnetName Name of the virtual network
     * @param gatewayName Name of the gateway. Only the 'primary' gateway is supported.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getServerFarmVnetGatewayAsync(String resourceGroupName, String name, String vnetName, String gatewayName, final ServiceCallback<VnetGatewayInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (name == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter name is required and cannot be null."));
            return null;
        }
        if (vnetName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter vnetName is required and cannot be null."));
            return null;
        }
        if (gatewayName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter gatewayName is required and cannot be null."));
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
        Call<ResponseBody> call = service.getServerFarmVnetGateway(resourceGroupName, name, vnetName, gatewayName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<VnetGatewayInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getServerFarmVnetGatewayDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<VnetGatewayInner> getServerFarmVnetGatewayDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<VnetGatewayInner, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<VnetGatewayInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Updates the vnet gateway.
     *
     * @param resourceGroupName The resource group
     * @param name The name of the App Service Plan
     * @param vnetName The name of the virtual network
     * @param gatewayName The name of the gateway. Only 'primary' is supported.
     * @param connectionEnvelope The gateway entity.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the VnetGatewayInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<VnetGatewayInner> updateServerFarmVnetGateway(String resourceGroupName, String name, String vnetName, String gatewayName, VnetGatewayInner connectionEnvelope) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
        }
        if (vnetName == null) {
            throw new IllegalArgumentException("Parameter vnetName is required and cannot be null.");
        }
        if (gatewayName == null) {
            throw new IllegalArgumentException("Parameter gatewayName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (connectionEnvelope == null) {
            throw new IllegalArgumentException("Parameter connectionEnvelope is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(connectionEnvelope);
        Call<ResponseBody> call = service.updateServerFarmVnetGateway(resourceGroupName, name, vnetName, gatewayName, this.client.subscriptionId(), connectionEnvelope, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return updateServerFarmVnetGatewayDelegate(call.execute());
    }

    /**
     * Updates the vnet gateway.
     *
     * @param resourceGroupName The resource group
     * @param name The name of the App Service Plan
     * @param vnetName The name of the virtual network
     * @param gatewayName The name of the gateway. Only 'primary' is supported.
     * @param connectionEnvelope The gateway entity.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall updateServerFarmVnetGatewayAsync(String resourceGroupName, String name, String vnetName, String gatewayName, VnetGatewayInner connectionEnvelope, final ServiceCallback<VnetGatewayInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (name == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter name is required and cannot be null."));
            return null;
        }
        if (vnetName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter vnetName is required and cannot be null."));
            return null;
        }
        if (gatewayName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter gatewayName is required and cannot be null."));
            return null;
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
            return null;
        }
        if (connectionEnvelope == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter connectionEnvelope is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(connectionEnvelope, serviceCallback);
        Call<ResponseBody> call = service.updateServerFarmVnetGateway(resourceGroupName, name, vnetName, gatewayName, this.client.subscriptionId(), connectionEnvelope, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<VnetGatewayInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(updateServerFarmVnetGatewayDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<VnetGatewayInner> updateServerFarmVnetGatewayDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<VnetGatewayInner, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<VnetGatewayInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Gets list of Apps associated with an App Service Plan.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of App Service Plan
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;SiteInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<SiteInner>> getServerFarmSites(final String resourceGroupName, final String name) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String skipToken = null;
        final String filter = null;
        final String top = null;
        Call<ResponseBody> call = service.getServerFarmSites(resourceGroupName, name, this.client.subscriptionId(), skipToken, filter, top, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        ServiceResponse<PageImpl<SiteInner>> response = getServerFarmSitesDelegate(call.execute());
        PagedList<SiteInner> result = new PagedList<SiteInner>(response.getBody()) {
            @Override
            public Page<SiteInner> nextPage(String nextPageLink) throws CloudException, IOException {
                return getServerFarmSitesNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Gets list of Apps associated with an App Service Plan.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of App Service Plan
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getServerFarmSitesAsync(final String resourceGroupName, final String name, final ListOperationCallback<SiteInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (name == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter name is required and cannot be null."));
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
        final String skipToken = null;
        final String filter = null;
        final String top = null;
        Call<ResponseBody> call = service.getServerFarmSites(resourceGroupName, name, this.client.subscriptionId(), skipToken, filter, top, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<SiteInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<SiteInner>> result = getServerFarmSitesDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        getServerFarmSitesNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
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
     * Gets list of Apps associated with an App Service Plan.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of App Service Plan
     * @param skipToken Skip to of web apps in a list. If specified, the resulting list will contain web apps starting from (including) the skipToken. Else, the resulting list contains web apps from the start of the list
     * @param filter Supported filter: $filter=state eq running. Returns only web apps that are currently running
     * @param top List page size. If specified, results are paged.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;SiteInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<SiteInner>> getServerFarmSites(final String resourceGroupName, final String name, final String skipToken, final String filter, final String top) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.getServerFarmSites(resourceGroupName, name, this.client.subscriptionId(), skipToken, filter, top, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        ServiceResponse<PageImpl<SiteInner>> response = getServerFarmSitesDelegate(call.execute());
        PagedList<SiteInner> result = new PagedList<SiteInner>(response.getBody()) {
            @Override
            public Page<SiteInner> nextPage(String nextPageLink) throws CloudException, IOException {
                return getServerFarmSitesNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Gets list of Apps associated with an App Service Plan.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of App Service Plan
     * @param skipToken Skip to of web apps in a list. If specified, the resulting list will contain web apps starting from (including) the skipToken. Else, the resulting list contains web apps from the start of the list
     * @param filter Supported filter: $filter=state eq running. Returns only web apps that are currently running
     * @param top List page size. If specified, results are paged.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getServerFarmSitesAsync(final String resourceGroupName, final String name, final String skipToken, final String filter, final String top, final ListOperationCallback<SiteInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (name == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter name is required and cannot be null."));
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
        Call<ResponseBody> call = service.getServerFarmSites(resourceGroupName, name, this.client.subscriptionId(), skipToken, filter, top, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<SiteInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<SiteInner>> result = getServerFarmSitesDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        getServerFarmSitesNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
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

    private ServiceResponse<PageImpl<SiteInner>> getServerFarmSitesDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<SiteInner>, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<PageImpl<SiteInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Submit a reboot request for a worker machine in the specified server farm.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of server farm
     * @param workerName Name of worker machine, typically starts with RD
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the Object object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<Object> rebootWorkerForServerFarm(String resourceGroupName, String name, String workerName) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
        }
        if (workerName == null) {
            throw new IllegalArgumentException("Parameter workerName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.rebootWorkerForServerFarm(resourceGroupName, name, workerName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return rebootWorkerForServerFarmDelegate(call.execute());
    }

    /**
     * Submit a reboot request for a worker machine in the specified server farm.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of server farm
     * @param workerName Name of worker machine, typically starts with RD
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall rebootWorkerForServerFarmAsync(String resourceGroupName, String name, String workerName, final ServiceCallback<Object> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (name == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter name is required and cannot be null."));
            return null;
        }
        if (workerName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter workerName is required and cannot be null."));
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
        Call<ResponseBody> call = service.rebootWorkerForServerFarm(resourceGroupName, name, workerName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Object>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(rebootWorkerForServerFarmDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<Object> rebootWorkerForServerFarmDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Object, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<Object>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Restarts web apps in a specified App Service Plan.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of App Service Plan
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the Object object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<Object> restartSitesForServerFarm(String resourceGroupName, String name) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final Boolean softRestart = null;
        Call<ResponseBody> call = service.restartSitesForServerFarm(resourceGroupName, name, this.client.subscriptionId(), softRestart, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return restartSitesForServerFarmDelegate(call.execute());
    }

    /**
     * Restarts web apps in a specified App Service Plan.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of App Service Plan
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall restartSitesForServerFarmAsync(String resourceGroupName, String name, final ServiceCallback<Object> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (name == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter name is required and cannot be null."));
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
        final Boolean softRestart = null;
        Call<ResponseBody> call = service.restartSitesForServerFarm(resourceGroupName, name, this.client.subscriptionId(), softRestart, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Object>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(restartSitesForServerFarmDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Restarts web apps in a specified App Service Plan.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of App Service Plan
     * @param softRestart Soft restart applies the configuration settings and restarts the apps if necessary. Hard restart always restarts and reprovisions the apps
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the Object object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<Object> restartSitesForServerFarm(String resourceGroupName, String name, Boolean softRestart) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.restartSitesForServerFarm(resourceGroupName, name, this.client.subscriptionId(), softRestart, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return restartSitesForServerFarmDelegate(call.execute());
    }

    /**
     * Restarts web apps in a specified App Service Plan.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of App Service Plan
     * @param softRestart Soft restart applies the configuration settings and restarts the apps if necessary. Hard restart always restarts and reprovisions the apps
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall restartSitesForServerFarmAsync(String resourceGroupName, String name, Boolean softRestart, final ServiceCallback<Object> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (name == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter name is required and cannot be null."));
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
        Call<ResponseBody> call = service.restartSitesForServerFarm(resourceGroupName, name, this.client.subscriptionId(), softRestart, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Object>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(restartSitesForServerFarmDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<Object> restartSitesForServerFarmDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Object, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<Object>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Gets a server farm operation.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of server farm
     * @param operationId Id of Server farm operation"&amp;gt;
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the ServerFarmWithRichSkuInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<ServerFarmWithRichSkuInner> getServerFarmOperation(String resourceGroupName, String name, String operationId) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
        }
        if (operationId == null) {
            throw new IllegalArgumentException("Parameter operationId is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.getServerFarmOperation(resourceGroupName, name, operationId, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getServerFarmOperationDelegate(call.execute());
    }

    /**
     * Gets a server farm operation.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of server farm
     * @param operationId Id of Server farm operation"&amp;gt;
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getServerFarmOperationAsync(String resourceGroupName, String name, String operationId, final ServiceCallback<ServerFarmWithRichSkuInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (name == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter name is required and cannot be null."));
            return null;
        }
        if (operationId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter operationId is required and cannot be null."));
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
        Call<ResponseBody> call = service.getServerFarmOperation(resourceGroupName, name, operationId, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<ServerFarmWithRichSkuInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getServerFarmOperationDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<ServerFarmWithRichSkuInner> getServerFarmOperationDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<ServerFarmWithRichSkuInner, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<ServerFarmWithRichSkuInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Gets list of Apps associated with an App Service Plan.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;SiteInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl<SiteInner>> getServerFarmSitesNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        Call<ResponseBody> call = service.getServerFarmSitesNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        return getServerFarmSitesNextDelegate(call.execute());
    }

    /**
     * Gets list of Apps associated with an App Service Plan.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getServerFarmSitesNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<SiteInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.getServerFarmSitesNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<SiteInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<SiteInner>> result = getServerFarmSitesNextDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        getServerFarmSitesNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
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

    private ServiceResponse<PageImpl<SiteInner>> getServerFarmSitesNextDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<SiteInner>, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<PageImpl<SiteInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

}
