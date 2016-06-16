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
import retrofit2.Response;

/**
 * An instance of this class provides access to all the operations defined
 * in HostingEnvironments.
 */
public final class HostingEnvironmentsInner {
    /** The Retrofit service to perform REST calls. */
    private HostingEnvironmentsService service;
    /** The service client containing this operation class. */
    private WebSiteManagementClientImpl client;

    /**
     * Initializes an instance of HostingEnvironmentsInner.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public HostingEnvironmentsInner(Retrofit retrofit, WebSiteManagementClientImpl client) {
        this.service = retrofit.create(HostingEnvironmentsService.class);
        this.client = client;
    }

    /**
     * The interface defining all the services for HostingEnvironments to be
     * used by Retrofit to perform actually REST calls.
     */
    interface HostingEnvironmentsService {
        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}")
        Call<ResponseBody> getHostingEnvironment(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @PUT("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}")
        Call<ResponseBody> createOrUpdateHostingEnvironment(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("subscriptionId") String subscriptionId, @Body HostingEnvironmentInner hostingEnvironmentEnvelope, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @PUT("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}")
        Call<ResponseBody> beginCreateOrUpdateHostingEnvironment(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("subscriptionId") String subscriptionId, @Body HostingEnvironmentInner hostingEnvironmentEnvelope, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @HTTP(path = "subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}", method = "DELETE", hasBody = true)
        Call<ResponseBody> deleteHostingEnvironment(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("subscriptionId") String subscriptionId, @Query("forceDelete") Boolean forceDelete, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @HTTP(path = "subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}", method = "DELETE", hasBody = true)
        Call<ResponseBody> beginDeleteHostingEnvironment(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("subscriptionId") String subscriptionId, @Query("forceDelete") Boolean forceDelete, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/diagnostics")
        Call<ResponseBody> getHostingEnvironmentDiagnostics(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/diagnostics/{diagnosticsName}")
        Call<ResponseBody> getHostingEnvironmentDiagnosticsItem(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("diagnosticsName") String diagnosticsName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/capacities/compute")
        Call<ResponseBody> getHostingEnvironmentCapacities(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/capacities/virtualip")
        Call<ResponseBody> getHostingEnvironmentVips(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments")
        Call<ResponseBody> getHostingEnvironments(@Path("resourceGroupName") String resourceGroupName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @POST("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/reboot")
        Call<ResponseBody> rebootHostingEnvironment(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/operations")
        Call<ResponseBody> getHostingEnvironmentOperations(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/operations/{operationId}")
        Call<ResponseBody> getHostingEnvironmentOperation(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("operationId") String operationId, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/metrics")
        Call<ResponseBody> getHostingEnvironmentMetrics(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("subscriptionId") String subscriptionId, @Query("details") Boolean details, @Query("$filter") String filter, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/metricdefinitions")
        Call<ResponseBody> getHostingEnvironmentMetricDefinitions(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/usages")
        Call<ResponseBody> getHostingEnvironmentUsages(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("subscriptionId") String subscriptionId, @Query("$filter") String filter, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/multiRolePools/default/metrics")
        Call<ResponseBody> getHostingEnvironmentMultiRoleMetrics(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("subscriptionId") String subscriptionId, @Query("startTime") String startTime, @Query("endTime") String endTime, @Query("timeGrain") String timeGrain, @Query("details") Boolean details, @Query("$filter") String filter, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/workerPools/{workerPoolName}/metrics")
        Call<ResponseBody> getHostingEnvironmentWebWorkerMetrics(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("workerPoolName") String workerPoolName, @Path("subscriptionId") String subscriptionId, @Query("details") Boolean details, @Query("$filter") String filter, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/multiRolePools/default/metricdefinitions")
        Call<ResponseBody> getHostingEnvironmentMultiRoleMetricDefinitions(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/workerPools/{workerPoolName}/metricdefinitions")
        Call<ResponseBody> getHostingEnvironmentWebWorkerMetricDefinitions(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("workerPoolName") String workerPoolName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/multiRolePools/default/usages")
        Call<ResponseBody> getHostingEnvironmentMultiRoleUsages(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/workerPools/{workerPoolName}/usages")
        Call<ResponseBody> getHostingEnvironmentWebWorkerUsages(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("workerPoolName") String workerPoolName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/sites")
        Call<ResponseBody> getHostingEnvironmentSites(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("subscriptionId") String subscriptionId, @Query("propertiesToInclude") String propertiesToInclude, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/webhostingplans")
        Call<ResponseBody> getHostingEnvironmentWebHostingPlans(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/serverfarms")
        Call<ResponseBody> getHostingEnvironmentServerFarms(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/multiRolePools")
        Call<ResponseBody> getMultiRolePools(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/multiRolePools/default")
        Call<ResponseBody> getMultiRolePool(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @PUT("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/multiRolePools/default")
        Call<ResponseBody> createOrUpdateMultiRolePool(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("subscriptionId") String subscriptionId, @Body WorkerPoolInner multiRolePoolEnvelope, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @PUT("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/multiRolePools/default")
        Call<ResponseBody> beginCreateOrUpdateMultiRolePool(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("subscriptionId") String subscriptionId, @Body WorkerPoolInner multiRolePoolEnvelope, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/multiRolePools/default/skus")
        Call<ResponseBody> getMultiRolePoolSkus(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/workerPools")
        Call<ResponseBody> getWorkerPools(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/workerPools/{workerPoolName}")
        Call<ResponseBody> getWorkerPool(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("workerPoolName") String workerPoolName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @PUT("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/workerPools/{workerPoolName}")
        Call<ResponseBody> createOrUpdateWorkerPool(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("workerPoolName") String workerPoolName, @Path("subscriptionId") String subscriptionId, @Body WorkerPoolInner workerPoolEnvelope, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @PUT("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/workerPools/{workerPoolName}")
        Call<ResponseBody> beginCreateOrUpdateWorkerPool(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("workerPoolName") String workerPoolName, @Path("subscriptionId") String subscriptionId, @Body WorkerPoolInner workerPoolEnvelope, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/workerPools/{workerPoolName}/skus")
        Call<ResponseBody> getWorkerPoolSkus(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("workerPoolName") String workerPoolName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/workerPools/{workerPoolName}/instances/{instance}/metrics")
        Call<ResponseBody> getWorkerPoolInstanceMetrics(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("workerPoolName") String workerPoolName, @Path("instance") String instance, @Path("subscriptionId") String subscriptionId, @Query("details") Boolean details, @Query("$filter") String filter, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/workerPools/{workerPoolName}/instances/{instance}/metricdefinitions")
        Call<ResponseBody> getWorkerPoolInstanceMetricDefinitions(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("workerPoolName") String workerPoolName, @Path("instance") String instance, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/multiRolePools/default/instances/{instance}/metrics")
        Call<ResponseBody> getMultiRolePoolInstanceMetrics(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("instance") String instance, @Path("subscriptionId") String subscriptionId, @Query("details") Boolean details, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/multiRolePools/default/instances/{instance}/metricdefinitions")
        Call<ResponseBody> getMultiRolePoolInstanceMetricDefinitions(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("instance") String instance, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @POST("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/suspend")
        Call<ResponseBody> suspendHostingEnvironment(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @POST("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/suspend")
        Call<ResponseBody> beginSuspendHostingEnvironment(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @POST("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/resume")
        Call<ResponseBody> resumeHostingEnvironment(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @POST("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/resume")
        Call<ResponseBody> beginResumeHostingEnvironment(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

    }

    /**
     * Get properties of hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the HostingEnvironmentInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<HostingEnvironmentInner> getHostingEnvironment(String resourceGroupName, String name) throws CloudException, IOException, IllegalArgumentException {
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
        Call<ResponseBody> call = service.getHostingEnvironment(resourceGroupName, name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getHostingEnvironmentDelegate(call.execute());
    }

    /**
     * Get properties of hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getHostingEnvironmentAsync(String resourceGroupName, String name, final ServiceCallback<HostingEnvironmentInner> serviceCallback) throws IllegalArgumentException {
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
        Call<ResponseBody> call = service.getHostingEnvironment(resourceGroupName, name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<HostingEnvironmentInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getHostingEnvironmentDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<HostingEnvironmentInner> getHostingEnvironmentDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<HostingEnvironmentInner, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<HostingEnvironmentInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Create or update a hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param hostingEnvironmentEnvelope Properties of hostingEnvironment (App Service Environment)
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @throws InterruptedException exception thrown when long running operation is interrupted
     * @return the HostingEnvironmentInner object wrapped in ServiceResponse if successful.
     */
    public ServiceResponse<HostingEnvironmentInner> createOrUpdateHostingEnvironment(String resourceGroupName, String name, HostingEnvironmentInner hostingEnvironmentEnvelope) throws CloudException, IOException, IllegalArgumentException, InterruptedException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (hostingEnvironmentEnvelope == null) {
            throw new IllegalArgumentException("Parameter hostingEnvironmentEnvelope is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(hostingEnvironmentEnvelope);
        Response<ResponseBody> result = service.createOrUpdateHostingEnvironment(resourceGroupName, name, this.client.subscriptionId(), hostingEnvironmentEnvelope, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent()).execute();
        return client.getAzureClient().getPutOrPatchResult(result, new TypeToken<HostingEnvironmentInner>() { }.getType());
    }

    /**
     * Create or update a hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param hostingEnvironmentEnvelope Properties of hostingEnvironment (App Service Environment)
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    public ServiceCall createOrUpdateHostingEnvironmentAsync(String resourceGroupName, String name, HostingEnvironmentInner hostingEnvironmentEnvelope, final ServiceCallback<HostingEnvironmentInner> serviceCallback) throws IllegalArgumentException {
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
        if (hostingEnvironmentEnvelope == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter hostingEnvironmentEnvelope is required and cannot be null."));
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
        }
        Validator.validate(hostingEnvironmentEnvelope, serviceCallback);
        Call<ResponseBody> call = service.createOrUpdateHostingEnvironment(resourceGroupName, name, this.client.subscriptionId(), hostingEnvironmentEnvelope, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                serviceCallback.failure(t);
            }
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                client.getAzureClient().getPutOrPatchResultAsync(response, new TypeToken<HostingEnvironmentInner>() { }.getType(), serviceCall, serviceCallback);
            }
        });
        return serviceCall;
    }

    /**
     * Create or update a hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param hostingEnvironmentEnvelope Properties of hostingEnvironment (App Service Environment)
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the HostingEnvironmentInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<HostingEnvironmentInner> beginCreateOrUpdateHostingEnvironment(String resourceGroupName, String name, HostingEnvironmentInner hostingEnvironmentEnvelope) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (hostingEnvironmentEnvelope == null) {
            throw new IllegalArgumentException("Parameter hostingEnvironmentEnvelope is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(hostingEnvironmentEnvelope);
        Call<ResponseBody> call = service.beginCreateOrUpdateHostingEnvironment(resourceGroupName, name, this.client.subscriptionId(), hostingEnvironmentEnvelope, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return beginCreateOrUpdateHostingEnvironmentDelegate(call.execute());
    }

    /**
     * Create or update a hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param hostingEnvironmentEnvelope Properties of hostingEnvironment (App Service Environment)
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall beginCreateOrUpdateHostingEnvironmentAsync(String resourceGroupName, String name, HostingEnvironmentInner hostingEnvironmentEnvelope, final ServiceCallback<HostingEnvironmentInner> serviceCallback) throws IllegalArgumentException {
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
        if (hostingEnvironmentEnvelope == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter hostingEnvironmentEnvelope is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(hostingEnvironmentEnvelope, serviceCallback);
        Call<ResponseBody> call = service.beginCreateOrUpdateHostingEnvironment(resourceGroupName, name, this.client.subscriptionId(), hostingEnvironmentEnvelope, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<HostingEnvironmentInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(beginCreateOrUpdateHostingEnvironmentDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<HostingEnvironmentInner> beginCreateOrUpdateHostingEnvironmentDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<HostingEnvironmentInner, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<HostingEnvironmentInner>() { }.getType())
                .register(202, new TypeToken<HostingEnvironmentInner>() { }.getType())
                .register(400, new TypeToken<Void>() { }.getType())
                .register(404, new TypeToken<Void>() { }.getType())
                .register(409, new TypeToken<Void>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Delete a hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @throws InterruptedException exception thrown when long running operation is interrupted
     * @return the Object object wrapped in ServiceResponse if successful.
     */
    public ServiceResponse<Object> deleteHostingEnvironment(String resourceGroupName, String name) throws CloudException, IOException, IllegalArgumentException, InterruptedException {
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
        final Boolean forceDelete = null;
        Response<ResponseBody> result = service.deleteHostingEnvironment(resourceGroupName, name, this.client.subscriptionId(), forceDelete, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent()).execute();
        return client.getAzureClient().getPostOrDeleteResult(result, new TypeToken<Object>() { }.getType());
    }

    /**
     * Delete a hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    public ServiceCall deleteHostingEnvironmentAsync(String resourceGroupName, String name, final ServiceCallback<Object> serviceCallback) throws IllegalArgumentException {
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
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
        }
        final Boolean forceDelete = null;
        Call<ResponseBody> call = service.deleteHostingEnvironment(resourceGroupName, name, this.client.subscriptionId(), forceDelete, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                serviceCallback.failure(t);
            }
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                client.getAzureClient().getPostOrDeleteResultAsync(response, new TypeToken<Object>() { }.getType(), serviceCall, serviceCallback);
            }
        });
        return serviceCall;
    }
    /**
     * Delete a hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param forceDelete Delete even if the hostingEnvironment (App Service Environment) contains resources
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @throws InterruptedException exception thrown when long running operation is interrupted
     * @return the Object object wrapped in ServiceResponse if successful.
     */
    public ServiceResponse<Object> deleteHostingEnvironment(String resourceGroupName, String name, Boolean forceDelete) throws CloudException, IOException, IllegalArgumentException, InterruptedException {
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
        Response<ResponseBody> result = service.deleteHostingEnvironment(resourceGroupName, name, this.client.subscriptionId(), forceDelete, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent()).execute();
        return client.getAzureClient().getPostOrDeleteResult(result, new TypeToken<Object>() { }.getType());
    }

    /**
     * Delete a hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param forceDelete Delete even if the hostingEnvironment (App Service Environment) contains resources
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    public ServiceCall deleteHostingEnvironmentAsync(String resourceGroupName, String name, Boolean forceDelete, final ServiceCallback<Object> serviceCallback) throws IllegalArgumentException {
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
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
        }
        Call<ResponseBody> call = service.deleteHostingEnvironment(resourceGroupName, name, this.client.subscriptionId(), forceDelete, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                serviceCallback.failure(t);
            }
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                client.getAzureClient().getPostOrDeleteResultAsync(response, new TypeToken<Object>() { }.getType(), serviceCall, serviceCallback);
            }
        });
        return serviceCall;
    }

    /**
     * Delete a hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the Object object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<Object> beginDeleteHostingEnvironment(String resourceGroupName, String name) throws CloudException, IOException, IllegalArgumentException {
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
        final Boolean forceDelete = null;
        Call<ResponseBody> call = service.beginDeleteHostingEnvironment(resourceGroupName, name, this.client.subscriptionId(), forceDelete, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return beginDeleteHostingEnvironmentDelegate(call.execute());
    }

    /**
     * Delete a hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall beginDeleteHostingEnvironmentAsync(String resourceGroupName, String name, final ServiceCallback<Object> serviceCallback) throws IllegalArgumentException {
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
        final Boolean forceDelete = null;
        Call<ResponseBody> call = service.beginDeleteHostingEnvironment(resourceGroupName, name, this.client.subscriptionId(), forceDelete, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Object>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(beginDeleteHostingEnvironmentDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Delete a hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param forceDelete Delete even if the hostingEnvironment (App Service Environment) contains resources
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the Object object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<Object> beginDeleteHostingEnvironment(String resourceGroupName, String name, Boolean forceDelete) throws CloudException, IOException, IllegalArgumentException {
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
        Call<ResponseBody> call = service.beginDeleteHostingEnvironment(resourceGroupName, name, this.client.subscriptionId(), forceDelete, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return beginDeleteHostingEnvironmentDelegate(call.execute());
    }

    /**
     * Delete a hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param forceDelete Delete even if the hostingEnvironment (App Service Environment) contains resources
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall beginDeleteHostingEnvironmentAsync(String resourceGroupName, String name, Boolean forceDelete, final ServiceCallback<Object> serviceCallback) throws IllegalArgumentException {
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
        Call<ResponseBody> call = service.beginDeleteHostingEnvironment(resourceGroupName, name, this.client.subscriptionId(), forceDelete, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Object>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(beginDeleteHostingEnvironmentDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<Object> beginDeleteHostingEnvironmentDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Object, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<Object>() { }.getType())
                .register(202, new TypeToken<Object>() { }.getType())
                .register(400, new TypeToken<Void>() { }.getType())
                .register(404, new TypeToken<Void>() { }.getType())
                .register(409, new TypeToken<Void>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Get diagnostic information for hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;HostingEnvironmentDiagnosticsInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<List<HostingEnvironmentDiagnosticsInner>> getHostingEnvironmentDiagnostics(String resourceGroupName, String name) throws CloudException, IOException, IllegalArgumentException {
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
        Call<ResponseBody> call = service.getHostingEnvironmentDiagnostics(resourceGroupName, name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getHostingEnvironmentDiagnosticsDelegate(call.execute());
    }

    /**
     * Get diagnostic information for hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getHostingEnvironmentDiagnosticsAsync(String resourceGroupName, String name, final ServiceCallback<List<HostingEnvironmentDiagnosticsInner>> serviceCallback) throws IllegalArgumentException {
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
        Call<ResponseBody> call = service.getHostingEnvironmentDiagnostics(resourceGroupName, name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<HostingEnvironmentDiagnosticsInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getHostingEnvironmentDiagnosticsDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<List<HostingEnvironmentDiagnosticsInner>> getHostingEnvironmentDiagnosticsDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<List<HostingEnvironmentDiagnosticsInner>, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<List<HostingEnvironmentDiagnosticsInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Get diagnostic information for hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param diagnosticsName Name of the diagnostics
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the HostingEnvironmentDiagnosticsInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<HostingEnvironmentDiagnosticsInner> getHostingEnvironmentDiagnosticsItem(String resourceGroupName, String name, String diagnosticsName) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
        }
        if (diagnosticsName == null) {
            throw new IllegalArgumentException("Parameter diagnosticsName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.getHostingEnvironmentDiagnosticsItem(resourceGroupName, name, diagnosticsName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getHostingEnvironmentDiagnosticsItemDelegate(call.execute());
    }

    /**
     * Get diagnostic information for hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param diagnosticsName Name of the diagnostics
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getHostingEnvironmentDiagnosticsItemAsync(String resourceGroupName, String name, String diagnosticsName, final ServiceCallback<HostingEnvironmentDiagnosticsInner> serviceCallback) throws IllegalArgumentException {
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
        if (diagnosticsName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter diagnosticsName is required and cannot be null."));
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
        Call<ResponseBody> call = service.getHostingEnvironmentDiagnosticsItem(resourceGroupName, name, diagnosticsName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<HostingEnvironmentDiagnosticsInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getHostingEnvironmentDiagnosticsItemDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<HostingEnvironmentDiagnosticsInner> getHostingEnvironmentDiagnosticsItemDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<HostingEnvironmentDiagnosticsInner, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<HostingEnvironmentDiagnosticsInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Get used, available, and total worker capacity for hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the StampCapacityCollectionInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<StampCapacityCollectionInner> getHostingEnvironmentCapacities(String resourceGroupName, String name) throws CloudException, IOException, IllegalArgumentException {
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
        Call<ResponseBody> call = service.getHostingEnvironmentCapacities(resourceGroupName, name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getHostingEnvironmentCapacitiesDelegate(call.execute());
    }

    /**
     * Get used, available, and total worker capacity for hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getHostingEnvironmentCapacitiesAsync(String resourceGroupName, String name, final ServiceCallback<StampCapacityCollectionInner> serviceCallback) throws IllegalArgumentException {
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
        Call<ResponseBody> call = service.getHostingEnvironmentCapacities(resourceGroupName, name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<StampCapacityCollectionInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getHostingEnvironmentCapacitiesDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<StampCapacityCollectionInner> getHostingEnvironmentCapacitiesDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<StampCapacityCollectionInner, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<StampCapacityCollectionInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Get IP addresses assigned to the hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the AddressResponseInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<AddressResponseInner> getHostingEnvironmentVips(String resourceGroupName, String name) throws CloudException, IOException, IllegalArgumentException {
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
        Call<ResponseBody> call = service.getHostingEnvironmentVips(resourceGroupName, name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getHostingEnvironmentVipsDelegate(call.execute());
    }

    /**
     * Get IP addresses assigned to the hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getHostingEnvironmentVipsAsync(String resourceGroupName, String name, final ServiceCallback<AddressResponseInner> serviceCallback) throws IllegalArgumentException {
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
        Call<ResponseBody> call = service.getHostingEnvironmentVips(resourceGroupName, name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<AddressResponseInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getHostingEnvironmentVipsDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<AddressResponseInner> getHostingEnvironmentVipsDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<AddressResponseInner, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<AddressResponseInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Get all hostingEnvironments (App Service Environments) in a resource group.
     *
     * @param resourceGroupName Name of resource group
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the HostingEnvironmentCollectionInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<HostingEnvironmentCollectionInner> getHostingEnvironments(String resourceGroupName) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.getHostingEnvironments(resourceGroupName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getHostingEnvironmentsDelegate(call.execute());
    }

    /**
     * Get all hostingEnvironments (App Service Environments) in a resource group.
     *
     * @param resourceGroupName Name of resource group
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getHostingEnvironmentsAsync(String resourceGroupName, final ServiceCallback<HostingEnvironmentCollectionInner> serviceCallback) throws IllegalArgumentException {
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
        Call<ResponseBody> call = service.getHostingEnvironments(resourceGroupName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<HostingEnvironmentCollectionInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getHostingEnvironmentsDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<HostingEnvironmentCollectionInner> getHostingEnvironmentsDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<HostingEnvironmentCollectionInner, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<HostingEnvironmentCollectionInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Reboots all machines in a hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the Object object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<Object> rebootHostingEnvironment(String resourceGroupName, String name) throws CloudException, IOException, IllegalArgumentException {
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
        Call<ResponseBody> call = service.rebootHostingEnvironment(resourceGroupName, name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return rebootHostingEnvironmentDelegate(call.execute());
    }

    /**
     * Reboots all machines in a hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall rebootHostingEnvironmentAsync(String resourceGroupName, String name, final ServiceCallback<Object> serviceCallback) throws IllegalArgumentException {
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
        Call<ResponseBody> call = service.rebootHostingEnvironment(resourceGroupName, name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Object>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(rebootHostingEnvironmentDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<Object> rebootHostingEnvironmentDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Object, CloudException>(this.client.mapperAdapter())
                .register(202, new TypeToken<Object>() { }.getType())
                .register(400, new TypeToken<Void>() { }.getType())
                .register(404, new TypeToken<Void>() { }.getType())
                .register(409, new TypeToken<Void>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * List all currently running operations on the hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the Object object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<Object> getHostingEnvironmentOperations(String resourceGroupName, String name) throws CloudException, IOException, IllegalArgumentException {
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
        Call<ResponseBody> call = service.getHostingEnvironmentOperations(resourceGroupName, name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getHostingEnvironmentOperationsDelegate(call.execute());
    }

    /**
     * List all currently running operations on the hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getHostingEnvironmentOperationsAsync(String resourceGroupName, String name, final ServiceCallback<Object> serviceCallback) throws IllegalArgumentException {
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
        Call<ResponseBody> call = service.getHostingEnvironmentOperations(resourceGroupName, name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Object>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getHostingEnvironmentOperationsDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<Object> getHostingEnvironmentOperationsDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Object, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<Object>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Get status of an operation on a hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param operationId operation identifier GUID
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the Object object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<Object> getHostingEnvironmentOperation(String resourceGroupName, String name, String operationId) throws CloudException, IOException, IllegalArgumentException {
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
        Call<ResponseBody> call = service.getHostingEnvironmentOperation(resourceGroupName, name, operationId, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getHostingEnvironmentOperationDelegate(call.execute());
    }

    /**
     * Get status of an operation on a hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param operationId operation identifier GUID
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getHostingEnvironmentOperationAsync(String resourceGroupName, String name, String operationId, final ServiceCallback<Object> serviceCallback) throws IllegalArgumentException {
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
        Call<ResponseBody> call = service.getHostingEnvironmentOperation(resourceGroupName, name, operationId, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Object>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getHostingEnvironmentOperationDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<Object> getHostingEnvironmentOperationDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Object, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<Object>() { }.getType())
                .register(202, new TypeToken<Object>() { }.getType())
                .register(404, new TypeToken<Void>() { }.getType())
                .register(500, new TypeToken<Void>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Get global metrics of hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the ResourceMetricCollectionInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<ResourceMetricCollectionInner> getHostingEnvironmentMetrics(String resourceGroupName, String name) throws CloudException, IOException, IllegalArgumentException {
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
        Call<ResponseBody> call = service.getHostingEnvironmentMetrics(resourceGroupName, name, this.client.subscriptionId(), details, filter, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getHostingEnvironmentMetricsDelegate(call.execute());
    }

    /**
     * Get global metrics of hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getHostingEnvironmentMetricsAsync(String resourceGroupName, String name, final ServiceCallback<ResourceMetricCollectionInner> serviceCallback) throws IllegalArgumentException {
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
        Call<ResponseBody> call = service.getHostingEnvironmentMetrics(resourceGroupName, name, this.client.subscriptionId(), details, filter, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<ResourceMetricCollectionInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getHostingEnvironmentMetricsDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Get global metrics of hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param details Include instance details
     * @param filter Return only usages/metrics specified in the filter. Filter conforms to odata syntax. Example: $filter=(name.value eq 'Metric1' or name.value eq 'Metric2') and startTime eq '2014-01-01T00:00:00Z' and endTime eq '2014-12-31T23:59:59Z' and timeGrain eq duration'[Hour|Minute|Day]'.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the ResourceMetricCollectionInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<ResourceMetricCollectionInner> getHostingEnvironmentMetrics(String resourceGroupName, String name, Boolean details, String filter) throws CloudException, IOException, IllegalArgumentException {
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
        Call<ResponseBody> call = service.getHostingEnvironmentMetrics(resourceGroupName, name, this.client.subscriptionId(), details, filter, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getHostingEnvironmentMetricsDelegate(call.execute());
    }

    /**
     * Get global metrics of hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param details Include instance details
     * @param filter Return only usages/metrics specified in the filter. Filter conforms to odata syntax. Example: $filter=(name.value eq 'Metric1' or name.value eq 'Metric2') and startTime eq '2014-01-01T00:00:00Z' and endTime eq '2014-12-31T23:59:59Z' and timeGrain eq duration'[Hour|Minute|Day]'.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getHostingEnvironmentMetricsAsync(String resourceGroupName, String name, Boolean details, String filter, final ServiceCallback<ResourceMetricCollectionInner> serviceCallback) throws IllegalArgumentException {
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
        Call<ResponseBody> call = service.getHostingEnvironmentMetrics(resourceGroupName, name, this.client.subscriptionId(), details, filter, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<ResourceMetricCollectionInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getHostingEnvironmentMetricsDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<ResourceMetricCollectionInner> getHostingEnvironmentMetricsDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<ResourceMetricCollectionInner, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<ResourceMetricCollectionInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Get global metric definitions of hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the MetricDefinitionInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<MetricDefinitionInner> getHostingEnvironmentMetricDefinitions(String resourceGroupName, String name) throws CloudException, IOException, IllegalArgumentException {
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
        Call<ResponseBody> call = service.getHostingEnvironmentMetricDefinitions(resourceGroupName, name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getHostingEnvironmentMetricDefinitionsDelegate(call.execute());
    }

    /**
     * Get global metric definitions of hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getHostingEnvironmentMetricDefinitionsAsync(String resourceGroupName, String name, final ServiceCallback<MetricDefinitionInner> serviceCallback) throws IllegalArgumentException {
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
        Call<ResponseBody> call = service.getHostingEnvironmentMetricDefinitions(resourceGroupName, name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<MetricDefinitionInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getHostingEnvironmentMetricDefinitionsDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<MetricDefinitionInner> getHostingEnvironmentMetricDefinitionsDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<MetricDefinitionInner, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<MetricDefinitionInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Get global usages of hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the CsmUsageQuotaCollectionInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<CsmUsageQuotaCollectionInner> getHostingEnvironmentUsages(String resourceGroupName, String name) throws CloudException, IOException, IllegalArgumentException {
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
        final String filter = null;
        Call<ResponseBody> call = service.getHostingEnvironmentUsages(resourceGroupName, name, this.client.subscriptionId(), filter, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getHostingEnvironmentUsagesDelegate(call.execute());
    }

    /**
     * Get global usages of hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getHostingEnvironmentUsagesAsync(String resourceGroupName, String name, final ServiceCallback<CsmUsageQuotaCollectionInner> serviceCallback) throws IllegalArgumentException {
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
        final String filter = null;
        Call<ResponseBody> call = service.getHostingEnvironmentUsages(resourceGroupName, name, this.client.subscriptionId(), filter, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<CsmUsageQuotaCollectionInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getHostingEnvironmentUsagesDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Get global usages of hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param filter Return only usages/metrics specified in the filter. Filter conforms to odata syntax. Example: $filter=(name.value eq 'Metric1' or name.value eq 'Metric2') and startTime eq '2014-01-01T00:00:00Z' and endTime eq '2014-12-31T23:59:59Z' and timeGrain eq duration'[Hour|Minute|Day]'.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the CsmUsageQuotaCollectionInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<CsmUsageQuotaCollectionInner> getHostingEnvironmentUsages(String resourceGroupName, String name, String filter) throws CloudException, IOException, IllegalArgumentException {
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
        Call<ResponseBody> call = service.getHostingEnvironmentUsages(resourceGroupName, name, this.client.subscriptionId(), filter, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getHostingEnvironmentUsagesDelegate(call.execute());
    }

    /**
     * Get global usages of hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param filter Return only usages/metrics specified in the filter. Filter conforms to odata syntax. Example: $filter=(name.value eq 'Metric1' or name.value eq 'Metric2') and startTime eq '2014-01-01T00:00:00Z' and endTime eq '2014-12-31T23:59:59Z' and timeGrain eq duration'[Hour|Minute|Day]'.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getHostingEnvironmentUsagesAsync(String resourceGroupName, String name, String filter, final ServiceCallback<CsmUsageQuotaCollectionInner> serviceCallback) throws IllegalArgumentException {
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
        Call<ResponseBody> call = service.getHostingEnvironmentUsages(resourceGroupName, name, this.client.subscriptionId(), filter, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<CsmUsageQuotaCollectionInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getHostingEnvironmentUsagesDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<CsmUsageQuotaCollectionInner> getHostingEnvironmentUsagesDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<CsmUsageQuotaCollectionInner, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<CsmUsageQuotaCollectionInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Get metrics for a multiRole pool of a hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the ResourceMetricCollectionInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<ResourceMetricCollectionInner> getHostingEnvironmentMultiRoleMetrics(String resourceGroupName, String name) throws CloudException, IOException, IllegalArgumentException {
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
        final String startTime = null;
        final String endTime = null;
        final String timeGrain = null;
        final Boolean details = null;
        final String filter = null;
        Call<ResponseBody> call = service.getHostingEnvironmentMultiRoleMetrics(resourceGroupName, name, this.client.subscriptionId(), startTime, endTime, timeGrain, details, filter, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getHostingEnvironmentMultiRoleMetricsDelegate(call.execute());
    }

    /**
     * Get metrics for a multiRole pool of a hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getHostingEnvironmentMultiRoleMetricsAsync(String resourceGroupName, String name, final ServiceCallback<ResourceMetricCollectionInner> serviceCallback) throws IllegalArgumentException {
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
        final String startTime = null;
        final String endTime = null;
        final String timeGrain = null;
        final Boolean details = null;
        final String filter = null;
        Call<ResponseBody> call = service.getHostingEnvironmentMultiRoleMetrics(resourceGroupName, name, this.client.subscriptionId(), startTime, endTime, timeGrain, details, filter, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<ResourceMetricCollectionInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getHostingEnvironmentMultiRoleMetricsDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Get metrics for a multiRole pool of a hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param startTime Beginning time of metrics query
     * @param endTime End time of metrics query
     * @param timeGrain Time granularity of metrics query
     * @param details Include instance details
     * @param filter Return only usages/metrics specified in the filter. Filter conforms to odata syntax. Example: $filter=(name.value eq 'Metric1' or name.value eq 'Metric2') and startTime eq '2014-01-01T00:00:00Z' and endTime eq '2014-12-31T23:59:59Z' and timeGrain eq duration'[Hour|Minute|Day]'.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the ResourceMetricCollectionInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<ResourceMetricCollectionInner> getHostingEnvironmentMultiRoleMetrics(String resourceGroupName, String name, String startTime, String endTime, String timeGrain, Boolean details, String filter) throws CloudException, IOException, IllegalArgumentException {
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
        Call<ResponseBody> call = service.getHostingEnvironmentMultiRoleMetrics(resourceGroupName, name, this.client.subscriptionId(), startTime, endTime, timeGrain, details, filter, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getHostingEnvironmentMultiRoleMetricsDelegate(call.execute());
    }

    /**
     * Get metrics for a multiRole pool of a hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param startTime Beginning time of metrics query
     * @param endTime End time of metrics query
     * @param timeGrain Time granularity of metrics query
     * @param details Include instance details
     * @param filter Return only usages/metrics specified in the filter. Filter conforms to odata syntax. Example: $filter=(name.value eq 'Metric1' or name.value eq 'Metric2') and startTime eq '2014-01-01T00:00:00Z' and endTime eq '2014-12-31T23:59:59Z' and timeGrain eq duration'[Hour|Minute|Day]'.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getHostingEnvironmentMultiRoleMetricsAsync(String resourceGroupName, String name, String startTime, String endTime, String timeGrain, Boolean details, String filter, final ServiceCallback<ResourceMetricCollectionInner> serviceCallback) throws IllegalArgumentException {
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
        Call<ResponseBody> call = service.getHostingEnvironmentMultiRoleMetrics(resourceGroupName, name, this.client.subscriptionId(), startTime, endTime, timeGrain, details, filter, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<ResourceMetricCollectionInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getHostingEnvironmentMultiRoleMetricsDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<ResourceMetricCollectionInner> getHostingEnvironmentMultiRoleMetricsDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<ResourceMetricCollectionInner, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<ResourceMetricCollectionInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Get metrics for a worker pool of a hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param workerPoolName Name of worker pool
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the ResourceMetricCollectionInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<ResourceMetricCollectionInner> getHostingEnvironmentWebWorkerMetrics(String resourceGroupName, String name, String workerPoolName) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
        }
        if (workerPoolName == null) {
            throw new IllegalArgumentException("Parameter workerPoolName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final Boolean details = null;
        final String filter = null;
        Call<ResponseBody> call = service.getHostingEnvironmentWebWorkerMetrics(resourceGroupName, name, workerPoolName, this.client.subscriptionId(), details, filter, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getHostingEnvironmentWebWorkerMetricsDelegate(call.execute());
    }

    /**
     * Get metrics for a worker pool of a hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param workerPoolName Name of worker pool
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getHostingEnvironmentWebWorkerMetricsAsync(String resourceGroupName, String name, String workerPoolName, final ServiceCallback<ResourceMetricCollectionInner> serviceCallback) throws IllegalArgumentException {
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
        if (workerPoolName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter workerPoolName is required and cannot be null."));
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
        Call<ResponseBody> call = service.getHostingEnvironmentWebWorkerMetrics(resourceGroupName, name, workerPoolName, this.client.subscriptionId(), details, filter, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<ResourceMetricCollectionInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getHostingEnvironmentWebWorkerMetricsDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Get metrics for a worker pool of a hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param workerPoolName Name of worker pool
     * @param details Include instance details
     * @param filter Return only usages/metrics specified in the filter. Filter conforms to odata syntax. Example: $filter=(name.value eq 'Metric1' or name.value eq 'Metric2') and startTime eq '2014-01-01T00:00:00Z' and endTime eq '2014-12-31T23:59:59Z' and timeGrain eq duration'[Hour|Minute|Day]'.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the ResourceMetricCollectionInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<ResourceMetricCollectionInner> getHostingEnvironmentWebWorkerMetrics(String resourceGroupName, String name, String workerPoolName, Boolean details, String filter) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
        }
        if (workerPoolName == null) {
            throw new IllegalArgumentException("Parameter workerPoolName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.getHostingEnvironmentWebWorkerMetrics(resourceGroupName, name, workerPoolName, this.client.subscriptionId(), details, filter, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getHostingEnvironmentWebWorkerMetricsDelegate(call.execute());
    }

    /**
     * Get metrics for a worker pool of a hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param workerPoolName Name of worker pool
     * @param details Include instance details
     * @param filter Return only usages/metrics specified in the filter. Filter conforms to odata syntax. Example: $filter=(name.value eq 'Metric1' or name.value eq 'Metric2') and startTime eq '2014-01-01T00:00:00Z' and endTime eq '2014-12-31T23:59:59Z' and timeGrain eq duration'[Hour|Minute|Day]'.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getHostingEnvironmentWebWorkerMetricsAsync(String resourceGroupName, String name, String workerPoolName, Boolean details, String filter, final ServiceCallback<ResourceMetricCollectionInner> serviceCallback) throws IllegalArgumentException {
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
        if (workerPoolName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter workerPoolName is required and cannot be null."));
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
        Call<ResponseBody> call = service.getHostingEnvironmentWebWorkerMetrics(resourceGroupName, name, workerPoolName, this.client.subscriptionId(), details, filter, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<ResourceMetricCollectionInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getHostingEnvironmentWebWorkerMetricsDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<ResourceMetricCollectionInner> getHostingEnvironmentWebWorkerMetricsDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<ResourceMetricCollectionInner, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<ResourceMetricCollectionInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Get metric definitions for a multiRole pool of a hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the MetricDefinitionCollectionInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<MetricDefinitionCollectionInner> getHostingEnvironmentMultiRoleMetricDefinitions(String resourceGroupName, String name) throws CloudException, IOException, IllegalArgumentException {
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
        Call<ResponseBody> call = service.getHostingEnvironmentMultiRoleMetricDefinitions(resourceGroupName, name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getHostingEnvironmentMultiRoleMetricDefinitionsDelegate(call.execute());
    }

    /**
     * Get metric definitions for a multiRole pool of a hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getHostingEnvironmentMultiRoleMetricDefinitionsAsync(String resourceGroupName, String name, final ServiceCallback<MetricDefinitionCollectionInner> serviceCallback) throws IllegalArgumentException {
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
        Call<ResponseBody> call = service.getHostingEnvironmentMultiRoleMetricDefinitions(resourceGroupName, name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<MetricDefinitionCollectionInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getHostingEnvironmentMultiRoleMetricDefinitionsDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<MetricDefinitionCollectionInner> getHostingEnvironmentMultiRoleMetricDefinitionsDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<MetricDefinitionCollectionInner, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<MetricDefinitionCollectionInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Get metric definitions for a worker pool of a hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param workerPoolName Name of worker pool
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the MetricDefinitionCollectionInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<MetricDefinitionCollectionInner> getHostingEnvironmentWebWorkerMetricDefinitions(String resourceGroupName, String name, String workerPoolName) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
        }
        if (workerPoolName == null) {
            throw new IllegalArgumentException("Parameter workerPoolName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.getHostingEnvironmentWebWorkerMetricDefinitions(resourceGroupName, name, workerPoolName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getHostingEnvironmentWebWorkerMetricDefinitionsDelegate(call.execute());
    }

    /**
     * Get metric definitions for a worker pool of a hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param workerPoolName Name of worker pool
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getHostingEnvironmentWebWorkerMetricDefinitionsAsync(String resourceGroupName, String name, String workerPoolName, final ServiceCallback<MetricDefinitionCollectionInner> serviceCallback) throws IllegalArgumentException {
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
        if (workerPoolName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter workerPoolName is required and cannot be null."));
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
        Call<ResponseBody> call = service.getHostingEnvironmentWebWorkerMetricDefinitions(resourceGroupName, name, workerPoolName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<MetricDefinitionCollectionInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getHostingEnvironmentWebWorkerMetricDefinitionsDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<MetricDefinitionCollectionInner> getHostingEnvironmentWebWorkerMetricDefinitionsDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<MetricDefinitionCollectionInner, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<MetricDefinitionCollectionInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Get usages for a multiRole pool of a hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the UsageCollectionInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<UsageCollectionInner> getHostingEnvironmentMultiRoleUsages(String resourceGroupName, String name) throws CloudException, IOException, IllegalArgumentException {
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
        Call<ResponseBody> call = service.getHostingEnvironmentMultiRoleUsages(resourceGroupName, name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getHostingEnvironmentMultiRoleUsagesDelegate(call.execute());
    }

    /**
     * Get usages for a multiRole pool of a hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getHostingEnvironmentMultiRoleUsagesAsync(String resourceGroupName, String name, final ServiceCallback<UsageCollectionInner> serviceCallback) throws IllegalArgumentException {
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
        Call<ResponseBody> call = service.getHostingEnvironmentMultiRoleUsages(resourceGroupName, name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<UsageCollectionInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getHostingEnvironmentMultiRoleUsagesDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<UsageCollectionInner> getHostingEnvironmentMultiRoleUsagesDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<UsageCollectionInner, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<UsageCollectionInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Get usages for a worker pool of a hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param workerPoolName Name of worker pool
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the UsageCollectionInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<UsageCollectionInner> getHostingEnvironmentWebWorkerUsages(String resourceGroupName, String name, String workerPoolName) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
        }
        if (workerPoolName == null) {
            throw new IllegalArgumentException("Parameter workerPoolName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.getHostingEnvironmentWebWorkerUsages(resourceGroupName, name, workerPoolName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getHostingEnvironmentWebWorkerUsagesDelegate(call.execute());
    }

    /**
     * Get usages for a worker pool of a hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param workerPoolName Name of worker pool
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getHostingEnvironmentWebWorkerUsagesAsync(String resourceGroupName, String name, String workerPoolName, final ServiceCallback<UsageCollectionInner> serviceCallback) throws IllegalArgumentException {
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
        if (workerPoolName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter workerPoolName is required and cannot be null."));
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
        Call<ResponseBody> call = service.getHostingEnvironmentWebWorkerUsages(resourceGroupName, name, workerPoolName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<UsageCollectionInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getHostingEnvironmentWebWorkerUsagesDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<UsageCollectionInner> getHostingEnvironmentWebWorkerUsagesDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<UsageCollectionInner, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<UsageCollectionInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Get all sites on the hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the SiteCollectionInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<SiteCollectionInner> getHostingEnvironmentSites(String resourceGroupName, String name) throws CloudException, IOException, IllegalArgumentException {
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
        final String propertiesToInclude = null;
        Call<ResponseBody> call = service.getHostingEnvironmentSites(resourceGroupName, name, this.client.subscriptionId(), propertiesToInclude, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getHostingEnvironmentSitesDelegate(call.execute());
    }

    /**
     * Get all sites on the hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getHostingEnvironmentSitesAsync(String resourceGroupName, String name, final ServiceCallback<SiteCollectionInner> serviceCallback) throws IllegalArgumentException {
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
        final String propertiesToInclude = null;
        Call<ResponseBody> call = service.getHostingEnvironmentSites(resourceGroupName, name, this.client.subscriptionId(), propertiesToInclude, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<SiteCollectionInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getHostingEnvironmentSitesDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Get all sites on the hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param propertiesToInclude Comma separated list of site properties to include
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the SiteCollectionInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<SiteCollectionInner> getHostingEnvironmentSites(String resourceGroupName, String name, String propertiesToInclude) throws CloudException, IOException, IllegalArgumentException {
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
        Call<ResponseBody> call = service.getHostingEnvironmentSites(resourceGroupName, name, this.client.subscriptionId(), propertiesToInclude, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getHostingEnvironmentSitesDelegate(call.execute());
    }

    /**
     * Get all sites on the hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param propertiesToInclude Comma separated list of site properties to include
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getHostingEnvironmentSitesAsync(String resourceGroupName, String name, String propertiesToInclude, final ServiceCallback<SiteCollectionInner> serviceCallback) throws IllegalArgumentException {
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
        Call<ResponseBody> call = service.getHostingEnvironmentSites(resourceGroupName, name, this.client.subscriptionId(), propertiesToInclude, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<SiteCollectionInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getHostingEnvironmentSitesDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<SiteCollectionInner> getHostingEnvironmentSitesDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<SiteCollectionInner, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<SiteCollectionInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Get all serverfarms (App Service Plans) on the hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the ServerFarmCollectionInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<ServerFarmCollectionInner> getHostingEnvironmentWebHostingPlans(String resourceGroupName, String name) throws CloudException, IOException, IllegalArgumentException {
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
        Call<ResponseBody> call = service.getHostingEnvironmentWebHostingPlans(resourceGroupName, name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getHostingEnvironmentWebHostingPlansDelegate(call.execute());
    }

    /**
     * Get all serverfarms (App Service Plans) on the hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getHostingEnvironmentWebHostingPlansAsync(String resourceGroupName, String name, final ServiceCallback<ServerFarmCollectionInner> serviceCallback) throws IllegalArgumentException {
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
        Call<ResponseBody> call = service.getHostingEnvironmentWebHostingPlans(resourceGroupName, name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<ServerFarmCollectionInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getHostingEnvironmentWebHostingPlansDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<ServerFarmCollectionInner> getHostingEnvironmentWebHostingPlansDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<ServerFarmCollectionInner, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<ServerFarmCollectionInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Get all serverfarms (App Service Plans) on the hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the ServerFarmCollectionInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<ServerFarmCollectionInner> getHostingEnvironmentServerFarms(String resourceGroupName, String name) throws CloudException, IOException, IllegalArgumentException {
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
        Call<ResponseBody> call = service.getHostingEnvironmentServerFarms(resourceGroupName, name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getHostingEnvironmentServerFarmsDelegate(call.execute());
    }

    /**
     * Get all serverfarms (App Service Plans) on the hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getHostingEnvironmentServerFarmsAsync(String resourceGroupName, String name, final ServiceCallback<ServerFarmCollectionInner> serviceCallback) throws IllegalArgumentException {
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
        Call<ResponseBody> call = service.getHostingEnvironmentServerFarms(resourceGroupName, name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<ServerFarmCollectionInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getHostingEnvironmentServerFarmsDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<ServerFarmCollectionInner> getHostingEnvironmentServerFarmsDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<ServerFarmCollectionInner, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<ServerFarmCollectionInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Get all multi role pools.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the WorkerPoolCollectionInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<WorkerPoolCollectionInner> getMultiRolePools(String resourceGroupName, String name) throws CloudException, IOException, IllegalArgumentException {
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
        Call<ResponseBody> call = service.getMultiRolePools(resourceGroupName, name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getMultiRolePoolsDelegate(call.execute());
    }

    /**
     * Get all multi role pools.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getMultiRolePoolsAsync(String resourceGroupName, String name, final ServiceCallback<WorkerPoolCollectionInner> serviceCallback) throws IllegalArgumentException {
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
        Call<ResponseBody> call = service.getMultiRolePools(resourceGroupName, name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<WorkerPoolCollectionInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getMultiRolePoolsDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<WorkerPoolCollectionInner> getMultiRolePoolsDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<WorkerPoolCollectionInner, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<WorkerPoolCollectionInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Get properties of a multiRool pool.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the WorkerPoolInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<WorkerPoolInner> getMultiRolePool(String resourceGroupName, String name) throws CloudException, IOException, IllegalArgumentException {
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
        Call<ResponseBody> call = service.getMultiRolePool(resourceGroupName, name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getMultiRolePoolDelegate(call.execute());
    }

    /**
     * Get properties of a multiRool pool.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getMultiRolePoolAsync(String resourceGroupName, String name, final ServiceCallback<WorkerPoolInner> serviceCallback) throws IllegalArgumentException {
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
        Call<ResponseBody> call = service.getMultiRolePool(resourceGroupName, name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<WorkerPoolInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getMultiRolePoolDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<WorkerPoolInner> getMultiRolePoolDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<WorkerPoolInner, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<WorkerPoolInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Create or update a multiRole pool.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param multiRolePoolEnvelope Properties of multiRole pool
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @throws InterruptedException exception thrown when long running operation is interrupted
     * @return the WorkerPoolInner object wrapped in ServiceResponse if successful.
     */
    public ServiceResponse<WorkerPoolInner> createOrUpdateMultiRolePool(String resourceGroupName, String name, WorkerPoolInner multiRolePoolEnvelope) throws CloudException, IOException, IllegalArgumentException, InterruptedException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (multiRolePoolEnvelope == null) {
            throw new IllegalArgumentException("Parameter multiRolePoolEnvelope is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(multiRolePoolEnvelope);
        Response<ResponseBody> result = service.createOrUpdateMultiRolePool(resourceGroupName, name, this.client.subscriptionId(), multiRolePoolEnvelope, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent()).execute();
        return client.getAzureClient().getPutOrPatchResult(result, new TypeToken<WorkerPoolInner>() { }.getType());
    }

    /**
     * Create or update a multiRole pool.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param multiRolePoolEnvelope Properties of multiRole pool
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    public ServiceCall createOrUpdateMultiRolePoolAsync(String resourceGroupName, String name, WorkerPoolInner multiRolePoolEnvelope, final ServiceCallback<WorkerPoolInner> serviceCallback) throws IllegalArgumentException {
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
        if (multiRolePoolEnvelope == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter multiRolePoolEnvelope is required and cannot be null."));
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
        }
        Validator.validate(multiRolePoolEnvelope, serviceCallback);
        Call<ResponseBody> call = service.createOrUpdateMultiRolePool(resourceGroupName, name, this.client.subscriptionId(), multiRolePoolEnvelope, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                serviceCallback.failure(t);
            }
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                client.getAzureClient().getPutOrPatchResultAsync(response, new TypeToken<WorkerPoolInner>() { }.getType(), serviceCall, serviceCallback);
            }
        });
        return serviceCall;
    }

    /**
     * Create or update a multiRole pool.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param multiRolePoolEnvelope Properties of multiRole pool
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the WorkerPoolInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<WorkerPoolInner> beginCreateOrUpdateMultiRolePool(String resourceGroupName, String name, WorkerPoolInner multiRolePoolEnvelope) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (multiRolePoolEnvelope == null) {
            throw new IllegalArgumentException("Parameter multiRolePoolEnvelope is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(multiRolePoolEnvelope);
        Call<ResponseBody> call = service.beginCreateOrUpdateMultiRolePool(resourceGroupName, name, this.client.subscriptionId(), multiRolePoolEnvelope, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return beginCreateOrUpdateMultiRolePoolDelegate(call.execute());
    }

    /**
     * Create or update a multiRole pool.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param multiRolePoolEnvelope Properties of multiRole pool
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall beginCreateOrUpdateMultiRolePoolAsync(String resourceGroupName, String name, WorkerPoolInner multiRolePoolEnvelope, final ServiceCallback<WorkerPoolInner> serviceCallback) throws IllegalArgumentException {
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
        if (multiRolePoolEnvelope == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter multiRolePoolEnvelope is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(multiRolePoolEnvelope, serviceCallback);
        Call<ResponseBody> call = service.beginCreateOrUpdateMultiRolePool(resourceGroupName, name, this.client.subscriptionId(), multiRolePoolEnvelope, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<WorkerPoolInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(beginCreateOrUpdateMultiRolePoolDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<WorkerPoolInner> beginCreateOrUpdateMultiRolePoolDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<WorkerPoolInner, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<WorkerPoolInner>() { }.getType())
                .register(202, new TypeToken<WorkerPoolInner>() { }.getType())
                .register(400, new TypeToken<Void>() { }.getType())
                .register(404, new TypeToken<Void>() { }.getType())
                .register(409, new TypeToken<Void>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Get available skus for scaling a multiRole pool.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the SkuInfoCollectionInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<SkuInfoCollectionInner> getMultiRolePoolSkus(String resourceGroupName, String name) throws CloudException, IOException, IllegalArgumentException {
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
        Call<ResponseBody> call = service.getMultiRolePoolSkus(resourceGroupName, name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getMultiRolePoolSkusDelegate(call.execute());
    }

    /**
     * Get available skus for scaling a multiRole pool.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getMultiRolePoolSkusAsync(String resourceGroupName, String name, final ServiceCallback<SkuInfoCollectionInner> serviceCallback) throws IllegalArgumentException {
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
        Call<ResponseBody> call = service.getMultiRolePoolSkus(resourceGroupName, name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<SkuInfoCollectionInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getMultiRolePoolSkusDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<SkuInfoCollectionInner> getMultiRolePoolSkusDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<SkuInfoCollectionInner, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<SkuInfoCollectionInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Get all worker pools.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the WorkerPoolCollectionInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<WorkerPoolCollectionInner> getWorkerPools(String resourceGroupName, String name) throws CloudException, IOException, IllegalArgumentException {
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
        Call<ResponseBody> call = service.getWorkerPools(resourceGroupName, name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getWorkerPoolsDelegate(call.execute());
    }

    /**
     * Get all worker pools.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getWorkerPoolsAsync(String resourceGroupName, String name, final ServiceCallback<WorkerPoolCollectionInner> serviceCallback) throws IllegalArgumentException {
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
        Call<ResponseBody> call = service.getWorkerPools(resourceGroupName, name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<WorkerPoolCollectionInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getWorkerPoolsDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<WorkerPoolCollectionInner> getWorkerPoolsDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<WorkerPoolCollectionInner, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<WorkerPoolCollectionInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Get properties of a worker pool.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param workerPoolName Name of worker pool
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the WorkerPoolInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<WorkerPoolInner> getWorkerPool(String resourceGroupName, String name, String workerPoolName) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
        }
        if (workerPoolName == null) {
            throw new IllegalArgumentException("Parameter workerPoolName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.getWorkerPool(resourceGroupName, name, workerPoolName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getWorkerPoolDelegate(call.execute());
    }

    /**
     * Get properties of a worker pool.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param workerPoolName Name of worker pool
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getWorkerPoolAsync(String resourceGroupName, String name, String workerPoolName, final ServiceCallback<WorkerPoolInner> serviceCallback) throws IllegalArgumentException {
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
        if (workerPoolName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter workerPoolName is required and cannot be null."));
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
        Call<ResponseBody> call = service.getWorkerPool(resourceGroupName, name, workerPoolName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<WorkerPoolInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getWorkerPoolDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<WorkerPoolInner> getWorkerPoolDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<WorkerPoolInner, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<WorkerPoolInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Create or update a worker pool.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param workerPoolName Name of worker pool
     * @param workerPoolEnvelope Properties of worker pool
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @throws InterruptedException exception thrown when long running operation is interrupted
     * @return the WorkerPoolInner object wrapped in ServiceResponse if successful.
     */
    public ServiceResponse<WorkerPoolInner> createOrUpdateWorkerPool(String resourceGroupName, String name, String workerPoolName, WorkerPoolInner workerPoolEnvelope) throws CloudException, IOException, IllegalArgumentException, InterruptedException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
        }
        if (workerPoolName == null) {
            throw new IllegalArgumentException("Parameter workerPoolName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (workerPoolEnvelope == null) {
            throw new IllegalArgumentException("Parameter workerPoolEnvelope is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(workerPoolEnvelope);
        Response<ResponseBody> result = service.createOrUpdateWorkerPool(resourceGroupName, name, workerPoolName, this.client.subscriptionId(), workerPoolEnvelope, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent()).execute();
        return client.getAzureClient().getPutOrPatchResult(result, new TypeToken<WorkerPoolInner>() { }.getType());
    }

    /**
     * Create or update a worker pool.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param workerPoolName Name of worker pool
     * @param workerPoolEnvelope Properties of worker pool
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    public ServiceCall createOrUpdateWorkerPoolAsync(String resourceGroupName, String name, String workerPoolName, WorkerPoolInner workerPoolEnvelope, final ServiceCallback<WorkerPoolInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
        }
        if (name == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter name is required and cannot be null."));
        }
        if (workerPoolName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter workerPoolName is required and cannot be null."));
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
        }
        if (workerPoolEnvelope == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter workerPoolEnvelope is required and cannot be null."));
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
        }
        Validator.validate(workerPoolEnvelope, serviceCallback);
        Call<ResponseBody> call = service.createOrUpdateWorkerPool(resourceGroupName, name, workerPoolName, this.client.subscriptionId(), workerPoolEnvelope, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                serviceCallback.failure(t);
            }
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                client.getAzureClient().getPutOrPatchResultAsync(response, new TypeToken<WorkerPoolInner>() { }.getType(), serviceCall, serviceCallback);
            }
        });
        return serviceCall;
    }

    /**
     * Create or update a worker pool.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param workerPoolName Name of worker pool
     * @param workerPoolEnvelope Properties of worker pool
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the WorkerPoolInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<WorkerPoolInner> beginCreateOrUpdateWorkerPool(String resourceGroupName, String name, String workerPoolName, WorkerPoolInner workerPoolEnvelope) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
        }
        if (workerPoolName == null) {
            throw new IllegalArgumentException("Parameter workerPoolName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (workerPoolEnvelope == null) {
            throw new IllegalArgumentException("Parameter workerPoolEnvelope is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(workerPoolEnvelope);
        Call<ResponseBody> call = service.beginCreateOrUpdateWorkerPool(resourceGroupName, name, workerPoolName, this.client.subscriptionId(), workerPoolEnvelope, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return beginCreateOrUpdateWorkerPoolDelegate(call.execute());
    }

    /**
     * Create or update a worker pool.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param workerPoolName Name of worker pool
     * @param workerPoolEnvelope Properties of worker pool
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall beginCreateOrUpdateWorkerPoolAsync(String resourceGroupName, String name, String workerPoolName, WorkerPoolInner workerPoolEnvelope, final ServiceCallback<WorkerPoolInner> serviceCallback) throws IllegalArgumentException {
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
        if (workerPoolName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter workerPoolName is required and cannot be null."));
            return null;
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
            return null;
        }
        if (workerPoolEnvelope == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter workerPoolEnvelope is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(workerPoolEnvelope, serviceCallback);
        Call<ResponseBody> call = service.beginCreateOrUpdateWorkerPool(resourceGroupName, name, workerPoolName, this.client.subscriptionId(), workerPoolEnvelope, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<WorkerPoolInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(beginCreateOrUpdateWorkerPoolDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<WorkerPoolInner> beginCreateOrUpdateWorkerPoolDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<WorkerPoolInner, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<WorkerPoolInner>() { }.getType())
                .register(202, new TypeToken<WorkerPoolInner>() { }.getType())
                .register(400, new TypeToken<Void>() { }.getType())
                .register(404, new TypeToken<Void>() { }.getType())
                .register(409, new TypeToken<Void>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Get available skus for scaling a worker pool.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param workerPoolName Name of worker pool
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the SkuInfoCollectionInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<SkuInfoCollectionInner> getWorkerPoolSkus(String resourceGroupName, String name, String workerPoolName) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
        }
        if (workerPoolName == null) {
            throw new IllegalArgumentException("Parameter workerPoolName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.getWorkerPoolSkus(resourceGroupName, name, workerPoolName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getWorkerPoolSkusDelegate(call.execute());
    }

    /**
     * Get available skus for scaling a worker pool.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param workerPoolName Name of worker pool
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getWorkerPoolSkusAsync(String resourceGroupName, String name, String workerPoolName, final ServiceCallback<SkuInfoCollectionInner> serviceCallback) throws IllegalArgumentException {
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
        if (workerPoolName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter workerPoolName is required and cannot be null."));
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
        Call<ResponseBody> call = service.getWorkerPoolSkus(resourceGroupName, name, workerPoolName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<SkuInfoCollectionInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getWorkerPoolSkusDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<SkuInfoCollectionInner> getWorkerPoolSkusDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<SkuInfoCollectionInner, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<SkuInfoCollectionInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Get metrics for a specific instance of a worker pool of a hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param workerPoolName Name of worker pool
     * @param instance Name of instance in the worker pool
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the Object object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<Object> getWorkerPoolInstanceMetrics(String resourceGroupName, String name, String workerPoolName, String instance) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
        }
        if (workerPoolName == null) {
            throw new IllegalArgumentException("Parameter workerPoolName is required and cannot be null.");
        }
        if (instance == null) {
            throw new IllegalArgumentException("Parameter instance is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final Boolean details = null;
        final String filter = null;
        Call<ResponseBody> call = service.getWorkerPoolInstanceMetrics(resourceGroupName, name, workerPoolName, instance, this.client.subscriptionId(), details, filter, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getWorkerPoolInstanceMetricsDelegate(call.execute());
    }

    /**
     * Get metrics for a specific instance of a worker pool of a hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param workerPoolName Name of worker pool
     * @param instance Name of instance in the worker pool
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getWorkerPoolInstanceMetricsAsync(String resourceGroupName, String name, String workerPoolName, String instance, final ServiceCallback<Object> serviceCallback) throws IllegalArgumentException {
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
        if (workerPoolName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter workerPoolName is required and cannot be null."));
            return null;
        }
        if (instance == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter instance is required and cannot be null."));
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
        Call<ResponseBody> call = service.getWorkerPoolInstanceMetrics(resourceGroupName, name, workerPoolName, instance, this.client.subscriptionId(), details, filter, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Object>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getWorkerPoolInstanceMetricsDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Get metrics for a specific instance of a worker pool of a hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param workerPoolName Name of worker pool
     * @param instance Name of instance in the worker pool
     * @param details Include instance details
     * @param filter Return only usages/metrics specified in the filter. Filter conforms to odata syntax. Example: $filter=(name.value eq 'Metric1' or name.value eq 'Metric2') and startTime eq '2014-01-01T00:00:00Z' and endTime eq '2014-12-31T23:59:59Z' and timeGrain eq duration'[Hour|Minute|Day]'.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the Object object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<Object> getWorkerPoolInstanceMetrics(String resourceGroupName, String name, String workerPoolName, String instance, Boolean details, String filter) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
        }
        if (workerPoolName == null) {
            throw new IllegalArgumentException("Parameter workerPoolName is required and cannot be null.");
        }
        if (instance == null) {
            throw new IllegalArgumentException("Parameter instance is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.getWorkerPoolInstanceMetrics(resourceGroupName, name, workerPoolName, instance, this.client.subscriptionId(), details, filter, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getWorkerPoolInstanceMetricsDelegate(call.execute());
    }

    /**
     * Get metrics for a specific instance of a worker pool of a hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param workerPoolName Name of worker pool
     * @param instance Name of instance in the worker pool
     * @param details Include instance details
     * @param filter Return only usages/metrics specified in the filter. Filter conforms to odata syntax. Example: $filter=(name.value eq 'Metric1' or name.value eq 'Metric2') and startTime eq '2014-01-01T00:00:00Z' and endTime eq '2014-12-31T23:59:59Z' and timeGrain eq duration'[Hour|Minute|Day]'.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getWorkerPoolInstanceMetricsAsync(String resourceGroupName, String name, String workerPoolName, String instance, Boolean details, String filter, final ServiceCallback<Object> serviceCallback) throws IllegalArgumentException {
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
        if (workerPoolName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter workerPoolName is required and cannot be null."));
            return null;
        }
        if (instance == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter instance is required and cannot be null."));
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
        Call<ResponseBody> call = service.getWorkerPoolInstanceMetrics(resourceGroupName, name, workerPoolName, instance, this.client.subscriptionId(), details, filter, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Object>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getWorkerPoolInstanceMetricsDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<Object> getWorkerPoolInstanceMetricsDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Object, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<Object>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Get metric definitions for a specific instance of a worker pool of a hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param workerPoolName Name of worker pool
     * @param instance Name of instance in the worker pool
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the Object object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<Object> getWorkerPoolInstanceMetricDefinitions(String resourceGroupName, String name, String workerPoolName, String instance) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
        }
        if (workerPoolName == null) {
            throw new IllegalArgumentException("Parameter workerPoolName is required and cannot be null.");
        }
        if (instance == null) {
            throw new IllegalArgumentException("Parameter instance is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.getWorkerPoolInstanceMetricDefinitions(resourceGroupName, name, workerPoolName, instance, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getWorkerPoolInstanceMetricDefinitionsDelegate(call.execute());
    }

    /**
     * Get metric definitions for a specific instance of a worker pool of a hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param workerPoolName Name of worker pool
     * @param instance Name of instance in the worker pool
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getWorkerPoolInstanceMetricDefinitionsAsync(String resourceGroupName, String name, String workerPoolName, String instance, final ServiceCallback<Object> serviceCallback) throws IllegalArgumentException {
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
        if (workerPoolName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter workerPoolName is required and cannot be null."));
            return null;
        }
        if (instance == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter instance is required and cannot be null."));
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
        Call<ResponseBody> call = service.getWorkerPoolInstanceMetricDefinitions(resourceGroupName, name, workerPoolName, instance, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Object>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getWorkerPoolInstanceMetricDefinitionsDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<Object> getWorkerPoolInstanceMetricDefinitionsDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Object, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<Object>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Get metrics for a specific instance of a multiRole pool of a hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param instance Name of instance in the multiRole pool
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the Object object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<Object> getMultiRolePoolInstanceMetrics(String resourceGroupName, String name, String instance) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
        }
        if (instance == null) {
            throw new IllegalArgumentException("Parameter instance is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final Boolean details = null;
        Call<ResponseBody> call = service.getMultiRolePoolInstanceMetrics(resourceGroupName, name, instance, this.client.subscriptionId(), details, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getMultiRolePoolInstanceMetricsDelegate(call.execute());
    }

    /**
     * Get metrics for a specific instance of a multiRole pool of a hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param instance Name of instance in the multiRole pool
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getMultiRolePoolInstanceMetricsAsync(String resourceGroupName, String name, String instance, final ServiceCallback<Object> serviceCallback) throws IllegalArgumentException {
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
        if (instance == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter instance is required and cannot be null."));
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
        Call<ResponseBody> call = service.getMultiRolePoolInstanceMetrics(resourceGroupName, name, instance, this.client.subscriptionId(), details, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Object>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getMultiRolePoolInstanceMetricsDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Get metrics for a specific instance of a multiRole pool of a hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param instance Name of instance in the multiRole pool
     * @param details Include instance details
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the Object object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<Object> getMultiRolePoolInstanceMetrics(String resourceGroupName, String name, String instance, Boolean details) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
        }
        if (instance == null) {
            throw new IllegalArgumentException("Parameter instance is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.getMultiRolePoolInstanceMetrics(resourceGroupName, name, instance, this.client.subscriptionId(), details, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getMultiRolePoolInstanceMetricsDelegate(call.execute());
    }

    /**
     * Get metrics for a specific instance of a multiRole pool of a hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param instance Name of instance in the multiRole pool
     * @param details Include instance details
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getMultiRolePoolInstanceMetricsAsync(String resourceGroupName, String name, String instance, Boolean details, final ServiceCallback<Object> serviceCallback) throws IllegalArgumentException {
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
        if (instance == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter instance is required and cannot be null."));
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
        Call<ResponseBody> call = service.getMultiRolePoolInstanceMetrics(resourceGroupName, name, instance, this.client.subscriptionId(), details, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Object>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getMultiRolePoolInstanceMetricsDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<Object> getMultiRolePoolInstanceMetricsDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Object, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<Object>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Get metric definitions for a specific instance of a multiRole pool of a hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param instance Name of instance in the multiRole pool&amp;gt;
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the Object object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<Object> getMultiRolePoolInstanceMetricDefinitions(String resourceGroupName, String name, String instance) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
        }
        if (instance == null) {
            throw new IllegalArgumentException("Parameter instance is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.getMultiRolePoolInstanceMetricDefinitions(resourceGroupName, name, instance, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getMultiRolePoolInstanceMetricDefinitionsDelegate(call.execute());
    }

    /**
     * Get metric definitions for a specific instance of a multiRole pool of a hostingEnvironment (App Service Environment).
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param instance Name of instance in the multiRole pool&amp;gt;
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getMultiRolePoolInstanceMetricDefinitionsAsync(String resourceGroupName, String name, String instance, final ServiceCallback<Object> serviceCallback) throws IllegalArgumentException {
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
        if (instance == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter instance is required and cannot be null."));
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
        Call<ResponseBody> call = service.getMultiRolePoolInstanceMetricDefinitions(resourceGroupName, name, instance, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Object>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getMultiRolePoolInstanceMetricDefinitionsDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<Object> getMultiRolePoolInstanceMetricDefinitionsDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Object, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<Object>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Suspends the hostingEnvironment.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @throws InterruptedException exception thrown when long running operation is interrupted
     * @return the SiteCollectionInner object wrapped in ServiceResponse if successful.
     */
    public ServiceResponse<SiteCollectionInner> suspendHostingEnvironment(String resourceGroupName, String name) throws CloudException, IOException, IllegalArgumentException, InterruptedException {
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
        Response<ResponseBody> result = service.suspendHostingEnvironment(resourceGroupName, name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent()).execute();
        return client.getAzureClient().getPostOrDeleteResult(result, new TypeToken<SiteCollectionInner>() { }.getType());
    }

    /**
     * Suspends the hostingEnvironment.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    public ServiceCall suspendHostingEnvironmentAsync(String resourceGroupName, String name, final ServiceCallback<SiteCollectionInner> serviceCallback) throws IllegalArgumentException {
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
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
        }
        Call<ResponseBody> call = service.suspendHostingEnvironment(resourceGroupName, name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                serviceCallback.failure(t);
            }
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                client.getAzureClient().getPostOrDeleteResultAsync(response, new TypeToken<SiteCollectionInner>() { }.getType(), serviceCall, serviceCallback);
            }
        });
        return serviceCall;
    }

    /**
     * Suspends the hostingEnvironment.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the SiteCollectionInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<SiteCollectionInner> beginSuspendHostingEnvironment(String resourceGroupName, String name) throws CloudException, IOException, IllegalArgumentException {
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
        Call<ResponseBody> call = service.beginSuspendHostingEnvironment(resourceGroupName, name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return beginSuspendHostingEnvironmentDelegate(call.execute());
    }

    /**
     * Suspends the hostingEnvironment.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall beginSuspendHostingEnvironmentAsync(String resourceGroupName, String name, final ServiceCallback<SiteCollectionInner> serviceCallback) throws IllegalArgumentException {
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
        Call<ResponseBody> call = service.beginSuspendHostingEnvironment(resourceGroupName, name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<SiteCollectionInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(beginSuspendHostingEnvironmentDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<SiteCollectionInner> beginSuspendHostingEnvironmentDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<SiteCollectionInner, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<SiteCollectionInner>() { }.getType())
                .register(202, new TypeToken<SiteCollectionInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Resumes the hostingEnvironment.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @throws InterruptedException exception thrown when long running operation is interrupted
     * @return the SiteCollectionInner object wrapped in ServiceResponse if successful.
     */
    public ServiceResponse<SiteCollectionInner> resumeHostingEnvironment(String resourceGroupName, String name) throws CloudException, IOException, IllegalArgumentException, InterruptedException {
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
        Response<ResponseBody> result = service.resumeHostingEnvironment(resourceGroupName, name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent()).execute();
        return client.getAzureClient().getPostOrDeleteResult(result, new TypeToken<SiteCollectionInner>() { }.getType());
    }

    /**
     * Resumes the hostingEnvironment.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    public ServiceCall resumeHostingEnvironmentAsync(String resourceGroupName, String name, final ServiceCallback<SiteCollectionInner> serviceCallback) throws IllegalArgumentException {
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
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
        }
        Call<ResponseBody> call = service.resumeHostingEnvironment(resourceGroupName, name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                serviceCallback.failure(t);
            }
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                client.getAzureClient().getPostOrDeleteResultAsync(response, new TypeToken<SiteCollectionInner>() { }.getType(), serviceCall, serviceCallback);
            }
        });
        return serviceCall;
    }

    /**
     * Resumes the hostingEnvironment.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the SiteCollectionInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<SiteCollectionInner> beginResumeHostingEnvironment(String resourceGroupName, String name) throws CloudException, IOException, IllegalArgumentException {
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
        Call<ResponseBody> call = service.beginResumeHostingEnvironment(resourceGroupName, name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return beginResumeHostingEnvironmentDelegate(call.execute());
    }

    /**
     * Resumes the hostingEnvironment.
     *
     * @param resourceGroupName Name of resource group
     * @param name Name of hostingEnvironment (App Service Environment)
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall beginResumeHostingEnvironmentAsync(String resourceGroupName, String name, final ServiceCallback<SiteCollectionInner> serviceCallback) throws IllegalArgumentException {
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
        Call<ResponseBody> call = service.beginResumeHostingEnvironment(resourceGroupName, name, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<SiteCollectionInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(beginResumeHostingEnvironmentDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<SiteCollectionInner> beginResumeHostingEnvironmentDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<SiteCollectionInner, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<SiteCollectionInner>() { }.getType())
                .register(202, new TypeToken<SiteCollectionInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

}
