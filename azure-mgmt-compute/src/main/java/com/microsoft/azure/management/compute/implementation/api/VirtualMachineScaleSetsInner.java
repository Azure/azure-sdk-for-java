/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation.api;

import retrofit2.Retrofit;
import com.google.common.reflect.TypeToken;
import com.microsoft.azure.AzureServiceResponseBuilder;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.ListOperationCallback;
import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.rest.serializer.CollectionFormat;
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
 * in VirtualMachineScaleSets.
 */
public final class VirtualMachineScaleSetsInner {
    /** The Retrofit service to perform REST calls. */
    private VirtualMachineScaleSetsService service;
    /** The service client containing this operation class. */
    private ComputeManagementClientImpl client;

    /**
     * Initializes an instance of VirtualMachineScaleSetsInner.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public VirtualMachineScaleSetsInner(Retrofit retrofit, ComputeManagementClientImpl client) {
        this.service = retrofit.create(VirtualMachineScaleSetsService.class);
        this.client = client;
    }

    /**
     * The interface defining all the services for VirtualMachineScaleSets to be
     * used by Retrofit to perform actually REST calls.
     */
    interface VirtualMachineScaleSetsService {
        @Headers("Content-Type: application/json; charset=utf-8")
        @PUT("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/virtualMachineScaleSets/{name}")
        Call<ResponseBody> createOrUpdate(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("subscriptionId") String subscriptionId, @Body VirtualMachineScaleSetInner parameters, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @PUT("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/virtualMachineScaleSets/{name}")
        Call<ResponseBody> beginCreateOrUpdate(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("subscriptionId") String subscriptionId, @Body VirtualMachineScaleSetInner parameters, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @POST("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/virtualMachineScaleSets/{vmScaleSetName}/deallocate")
        Call<ResponseBody> deallocate(@Path("resourceGroupName") String resourceGroupName, @Path("vmScaleSetName") String vmScaleSetName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Body VirtualMachineScaleSetVMInstanceIDs vmInstanceIDs, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @POST("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/virtualMachineScaleSets/{vmScaleSetName}/deallocate")
        Call<ResponseBody> beginDeallocate(@Path("resourceGroupName") String resourceGroupName, @Path("vmScaleSetName") String vmScaleSetName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Body VirtualMachineScaleSetVMInstanceIDs vmInstanceIDs, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @HTTP(path = "subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/virtualMachineScaleSets/{vmScaleSetName}", method = "DELETE", hasBody = true)
        Call<ResponseBody> delete(@Path("resourceGroupName") String resourceGroupName, @Path("vmScaleSetName") String vmScaleSetName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @HTTP(path = "subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/virtualMachineScaleSets/{vmScaleSetName}", method = "DELETE", hasBody = true)
        Call<ResponseBody> beginDelete(@Path("resourceGroupName") String resourceGroupName, @Path("vmScaleSetName") String vmScaleSetName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/virtualMachineScaleSets/{vmScaleSetName}")
        Call<ResponseBody> get(@Path("resourceGroupName") String resourceGroupName, @Path("vmScaleSetName") String vmScaleSetName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @POST("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/virtualMachineScaleSets/{vmScaleSetName}/delete")
        Call<ResponseBody> deleteInstances(@Path("resourceGroupName") String resourceGroupName, @Path("vmScaleSetName") String vmScaleSetName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Body VirtualMachineScaleSetVMInstanceRequiredIDs vmInstanceIDs, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @POST("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/virtualMachineScaleSets/{vmScaleSetName}/delete")
        Call<ResponseBody> beginDeleteInstances(@Path("resourceGroupName") String resourceGroupName, @Path("vmScaleSetName") String vmScaleSetName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Body VirtualMachineScaleSetVMInstanceRequiredIDs vmInstanceIDs, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/virtualMachineScaleSets/{vmScaleSetName}/instanceView")
        Call<ResponseBody> getInstanceView(@Path("resourceGroupName") String resourceGroupName, @Path("vmScaleSetName") String vmScaleSetName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/virtualMachineScaleSets")
        Call<ResponseBody> list(@Path("resourceGroupName") String resourceGroupName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/providers/Microsoft.Compute/virtualMachineScaleSets")
        Call<ResponseBody> listAll(@Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/virtualMachineScaleSets/{vmScaleSetName}/skus")
        Call<ResponseBody> listSkus(@Path("resourceGroupName") String resourceGroupName, @Path("vmScaleSetName") String vmScaleSetName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @POST("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/virtualMachineScaleSets/{vmScaleSetName}/poweroff")
        Call<ResponseBody> powerOff(@Path("resourceGroupName") String resourceGroupName, @Path("vmScaleSetName") String vmScaleSetName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Body VirtualMachineScaleSetVMInstanceIDs vmInstanceIDs, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @POST("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/virtualMachineScaleSets/{vmScaleSetName}/poweroff")
        Call<ResponseBody> beginPowerOff(@Path("resourceGroupName") String resourceGroupName, @Path("vmScaleSetName") String vmScaleSetName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Body VirtualMachineScaleSetVMInstanceIDs vmInstanceIDs, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @POST("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/virtualMachineScaleSets/{vmScaleSetName}/restart")
        Call<ResponseBody> restart(@Path("resourceGroupName") String resourceGroupName, @Path("vmScaleSetName") String vmScaleSetName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Body VirtualMachineScaleSetVMInstanceIDs vmInstanceIDs, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @POST("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/virtualMachineScaleSets/{vmScaleSetName}/restart")
        Call<ResponseBody> beginRestart(@Path("resourceGroupName") String resourceGroupName, @Path("vmScaleSetName") String vmScaleSetName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Body VirtualMachineScaleSetVMInstanceIDs vmInstanceIDs, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @POST("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/virtualMachineScaleSets/{vmScaleSetName}/start")
        Call<ResponseBody> start(@Path("resourceGroupName") String resourceGroupName, @Path("vmScaleSetName") String vmScaleSetName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Body VirtualMachineScaleSetVMInstanceIDs vmInstanceIDs, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @POST("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/virtualMachineScaleSets/{vmScaleSetName}/start")
        Call<ResponseBody> beginStart(@Path("resourceGroupName") String resourceGroupName, @Path("vmScaleSetName") String vmScaleSetName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Body VirtualMachineScaleSetVMInstanceIDs vmInstanceIDs, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @POST("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/virtualMachineScaleSets/{vmScaleSetName}/manualupgrade")
        Call<ResponseBody> updateInstances(@Path("resourceGroupName") String resourceGroupName, @Path("vmScaleSetName") String vmScaleSetName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Body VirtualMachineScaleSetVMInstanceRequiredIDs vmInstanceIDs, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @POST("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/virtualMachineScaleSets/{vmScaleSetName}/manualupgrade")
        Call<ResponseBody> beginUpdateInstances(@Path("resourceGroupName") String resourceGroupName, @Path("vmScaleSetName") String vmScaleSetName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Body VirtualMachineScaleSetVMInstanceRequiredIDs vmInstanceIDs, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @POST("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/virtualMachineScaleSets/{vmScaleSetName}/reimage")
        Call<ResponseBody> reimage(@Path("resourceGroupName") String resourceGroupName, @Path("vmScaleSetName") String vmScaleSetName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @POST("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/virtualMachineScaleSets/{vmScaleSetName}/reimage")
        Call<ResponseBody> beginReimage(@Path("resourceGroupName") String resourceGroupName, @Path("vmScaleSetName") String vmScaleSetName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET
        Call<ResponseBody> listNext(@Url String nextPageLink, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET
        Call<ResponseBody> listAllNext(@Url String nextPageLink, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET
        Call<ResponseBody> listSkusNext(@Url String nextPageLink, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

    }

    /**
     * The operation to create or update a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param name Parameters supplied to the Create Virtual Machine Scale Set operation.
     * @param parameters Parameters supplied to the Create Virtual Machine Scale Set operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @throws InterruptedException exception thrown when long running operation is interrupted
     * @return the VirtualMachineScaleSetInner object wrapped in ServiceResponse if successful.
     */
    public ServiceResponse<VirtualMachineScaleSetInner> createOrUpdate(String resourceGroupName, String name, VirtualMachineScaleSetInner parameters) throws CloudException, IOException, IllegalArgumentException, InterruptedException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
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
        Response<ResponseBody> result = service.createOrUpdate(resourceGroupName, name, this.client.subscriptionId(), parameters, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent()).execute();
        return client.getAzureClient().getPutOrPatchResult(result, new TypeToken<VirtualMachineScaleSetInner>() { }.getType());
    }

    /**
     * The operation to create or update a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param name Parameters supplied to the Create Virtual Machine Scale Set operation.
     * @param parameters Parameters supplied to the Create Virtual Machine Scale Set operation.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    public ServiceCall createOrUpdateAsync(String resourceGroupName, String name, VirtualMachineScaleSetInner parameters, final ServiceCallback<VirtualMachineScaleSetInner> serviceCallback) throws IllegalArgumentException {
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
        if (parameters == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter parameters is required and cannot be null."));
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
        }
        Validator.validate(parameters, serviceCallback);
        Call<ResponseBody> call = service.createOrUpdate(resourceGroupName, name, this.client.subscriptionId(), parameters, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                serviceCallback.failure(t);
            }
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                client.getAzureClient().getPutOrPatchResultAsync(response, new TypeToken<VirtualMachineScaleSetInner>() { }.getType(), serviceCall, serviceCallback);
            }
        });
        return serviceCall;
    }

    /**
     * The operation to create or update a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param name Parameters supplied to the Create Virtual Machine Scale Set operation.
     * @param parameters Parameters supplied to the Create Virtual Machine Scale Set operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the VirtualMachineScaleSetInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<VirtualMachineScaleSetInner> beginCreateOrUpdate(String resourceGroupName, String name, VirtualMachineScaleSetInner parameters) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
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
        Call<ResponseBody> call = service.beginCreateOrUpdate(resourceGroupName, name, this.client.subscriptionId(), parameters, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return beginCreateOrUpdateDelegate(call.execute());
    }

    /**
     * The operation to create or update a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param name Parameters supplied to the Create Virtual Machine Scale Set operation.
     * @param parameters Parameters supplied to the Create Virtual Machine Scale Set operation.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall beginCreateOrUpdateAsync(String resourceGroupName, String name, VirtualMachineScaleSetInner parameters, final ServiceCallback<VirtualMachineScaleSetInner> serviceCallback) throws IllegalArgumentException {
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
        if (parameters == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter parameters is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(parameters, serviceCallback);
        Call<ResponseBody> call = service.beginCreateOrUpdate(resourceGroupName, name, this.client.subscriptionId(), parameters, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<VirtualMachineScaleSetInner>(serviceCallback) {
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

    private ServiceResponse<VirtualMachineScaleSetInner> beginCreateOrUpdateDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<VirtualMachineScaleSetInner, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<VirtualMachineScaleSetInner>() { }.getType())
                .register(201, new TypeToken<VirtualMachineScaleSetInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * The operation to deallocate virtual machines in a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @throws InterruptedException exception thrown when long running operation is interrupted
     * @return the ServiceResponse object if successful.
     */
    public ServiceResponse<Void> deallocate(String resourceGroupName, String vmScaleSetName) throws CloudException, IOException, IllegalArgumentException, InterruptedException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (vmScaleSetName == null) {
            throw new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String instanceIdsConverted = null;
        VirtualMachineScaleSetVMInstanceIDs vmInstanceIDs = new VirtualMachineScaleSetVMInstanceIDs();
        vmInstanceIDs.withInstanceIds(null);
        Response<ResponseBody> result = service.deallocate(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), vmInstanceIDs, this.client.userAgent()).execute();
        return client.getAzureClient().getPostOrDeleteResult(result, new TypeToken<Void>() { }.getType());
    }

    /**
     * The operation to deallocate virtual machines in a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    public ServiceCall deallocateAsync(String resourceGroupName, String vmScaleSetName, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
        }
        if (vmScaleSetName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null."));
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
        }
        final String instanceIdsConverted = null;
        VirtualMachineScaleSetVMInstanceIDs vmInstanceIDs = new VirtualMachineScaleSetVMInstanceIDs();
        vmInstanceIDs.withInstanceIds(null);
        Call<ResponseBody> call = service.deallocate(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), vmInstanceIDs, this.client.userAgent());
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
     * The operation to deallocate virtual machines in a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @param instanceIds Gets or sets the virtual machine scale set instance ids.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @throws InterruptedException exception thrown when long running operation is interrupted
     * @return the ServiceResponse object if successful.
     */
    public ServiceResponse<Void> deallocate(String resourceGroupName, String vmScaleSetName, List<String> instanceIds) throws CloudException, IOException, IllegalArgumentException, InterruptedException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (vmScaleSetName == null) {
            throw new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(instanceIds);
        VirtualMachineScaleSetVMInstanceIDs vmInstanceIDs = null;
        if (instanceIds != null) {
            vmInstanceIDs = new VirtualMachineScaleSetVMInstanceIDs();
            vmInstanceIDs.withInstanceIds(instanceIds);
        }
        Response<ResponseBody> result = service.deallocate(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), vmInstanceIDs, this.client.userAgent()).execute();
        return client.getAzureClient().getPostOrDeleteResult(result, new TypeToken<Void>() { }.getType());
    }

    /**
     * The operation to deallocate virtual machines in a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @param instanceIds Gets or sets the virtual machine scale set instance ids.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    public ServiceCall deallocateAsync(String resourceGroupName, String vmScaleSetName, List<String> instanceIds, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
        }
        if (vmScaleSetName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null."));
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
        }
        Validator.validate(instanceIds, serviceCallback);
        VirtualMachineScaleSetVMInstanceIDs vmInstanceIDs = null;
        if (instanceIds != null) {
            vmInstanceIDs = new VirtualMachineScaleSetVMInstanceIDs();
            vmInstanceIDs.withInstanceIds(instanceIds);
        }
        Call<ResponseBody> call = service.deallocate(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), vmInstanceIDs, this.client.userAgent());
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
     * The operation to deallocate virtual machines in a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> beginDeallocate(String resourceGroupName, String vmScaleSetName) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (vmScaleSetName == null) {
            throw new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String instanceIdsConverted = null;
        VirtualMachineScaleSetVMInstanceIDs vmInstanceIDs = new VirtualMachineScaleSetVMInstanceIDs();
        vmInstanceIDs.withInstanceIds(null);
        Call<ResponseBody> call = service.beginDeallocate(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), vmInstanceIDs, this.client.userAgent());
        return beginDeallocateDelegate(call.execute());
    }

    /**
     * The operation to deallocate virtual machines in a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall beginDeallocateAsync(String resourceGroupName, String vmScaleSetName, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (vmScaleSetName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null."));
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
        final String instanceIdsConverted = null;
        VirtualMachineScaleSetVMInstanceIDs vmInstanceIDs = new VirtualMachineScaleSetVMInstanceIDs();
        vmInstanceIDs.withInstanceIds(null);
        Call<ResponseBody> call = service.beginDeallocate(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), vmInstanceIDs, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(beginDeallocateDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * The operation to deallocate virtual machines in a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @param instanceIds Gets or sets the virtual machine scale set instance ids.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> beginDeallocate(String resourceGroupName, String vmScaleSetName, List<String> instanceIds) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (vmScaleSetName == null) {
            throw new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(instanceIds);
        VirtualMachineScaleSetVMInstanceIDs vmInstanceIDs = null;
        if (instanceIds != null) {
            vmInstanceIDs = new VirtualMachineScaleSetVMInstanceIDs();
            vmInstanceIDs.withInstanceIds(instanceIds);
        }
        Call<ResponseBody> call = service.beginDeallocate(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), vmInstanceIDs, this.client.userAgent());
        return beginDeallocateDelegate(call.execute());
    }

    /**
     * The operation to deallocate virtual machines in a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @param instanceIds Gets or sets the virtual machine scale set instance ids.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall beginDeallocateAsync(String resourceGroupName, String vmScaleSetName, List<String> instanceIds, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (vmScaleSetName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null."));
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
        Validator.validate(instanceIds, serviceCallback);
        VirtualMachineScaleSetVMInstanceIDs vmInstanceIDs = null;
        if (instanceIds != null) {
            vmInstanceIDs = new VirtualMachineScaleSetVMInstanceIDs();
            vmInstanceIDs.withInstanceIds(instanceIds);
        }
        Call<ResponseBody> call = service.beginDeallocate(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), vmInstanceIDs, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(beginDeallocateDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<Void> beginDeallocateDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, CloudException>(this.client.restClient().mapperAdapter())
                .register(202, new TypeToken<Void>() { }.getType())
                .build(response);
    }

    /**
     * The operation to delete a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @throws InterruptedException exception thrown when long running operation is interrupted
     * @return the ServiceResponse object if successful.
     */
    public ServiceResponse<Void> delete(String resourceGroupName, String vmScaleSetName) throws CloudException, IOException, IllegalArgumentException, InterruptedException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (vmScaleSetName == null) {
            throw new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Response<ResponseBody> result = service.delete(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent()).execute();
        return client.getAzureClient().getPostOrDeleteResult(result, new TypeToken<Void>() { }.getType());
    }

    /**
     * The operation to delete a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    public ServiceCall deleteAsync(String resourceGroupName, String vmScaleSetName, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
        }
        if (vmScaleSetName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null."));
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
        }
        Call<ResponseBody> call = service.delete(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
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
     * The operation to delete a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> beginDelete(String resourceGroupName, String vmScaleSetName) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (vmScaleSetName == null) {
            throw new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.beginDelete(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return beginDeleteDelegate(call.execute());
    }

    /**
     * The operation to delete a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall beginDeleteAsync(String resourceGroupName, String vmScaleSetName, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (vmScaleSetName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null."));
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
        Call<ResponseBody> call = service.beginDelete(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
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
     * The operation to get a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the VirtualMachineScaleSetInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<VirtualMachineScaleSetInner> get(String resourceGroupName, String vmScaleSetName) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (vmScaleSetName == null) {
            throw new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.get(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getDelegate(call.execute());
    }

    /**
     * The operation to get a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getAsync(String resourceGroupName, String vmScaleSetName, final ServiceCallback<VirtualMachineScaleSetInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (vmScaleSetName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null."));
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
        Call<ResponseBody> call = service.get(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<VirtualMachineScaleSetInner>(serviceCallback) {
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

    private ServiceResponse<VirtualMachineScaleSetInner> getDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<VirtualMachineScaleSetInner, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<VirtualMachineScaleSetInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * The operation to delete virtual machines in a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @param instanceIds Gets or sets the virtual machine scale set instance ids.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @throws InterruptedException exception thrown when long running operation is interrupted
     * @return the ServiceResponse object if successful.
     */
    public ServiceResponse<Void> deleteInstances(String resourceGroupName, String vmScaleSetName, List<String> instanceIds) throws CloudException, IOException, IllegalArgumentException, InterruptedException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (vmScaleSetName == null) {
            throw new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        if (instanceIds == null) {
            throw new IllegalArgumentException("Parameter instanceIds is required and cannot be null.");
        }
        Validator.validate(instanceIds);
        VirtualMachineScaleSetVMInstanceRequiredIDs vmInstanceIDs = new VirtualMachineScaleSetVMInstanceRequiredIDs();
        vmInstanceIDs.withInstanceIds(instanceIds);
        Response<ResponseBody> result = service.deleteInstances(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), vmInstanceIDs, this.client.userAgent()).execute();
        return client.getAzureClient().getPostOrDeleteResult(result, new TypeToken<Void>() { }.getType());
    }

    /**
     * The operation to delete virtual machines in a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @param instanceIds Gets or sets the virtual machine scale set instance ids.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    public ServiceCall deleteInstancesAsync(String resourceGroupName, String vmScaleSetName, List<String> instanceIds, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
        }
        if (vmScaleSetName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null."));
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
        }
        if (instanceIds == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter instanceIds is required and cannot be null."));
        }
        Validator.validate(instanceIds, serviceCallback);
        VirtualMachineScaleSetVMInstanceRequiredIDs vmInstanceIDs = new VirtualMachineScaleSetVMInstanceRequiredIDs();
        vmInstanceIDs.withInstanceIds(instanceIds);
        Call<ResponseBody> call = service.deleteInstances(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), vmInstanceIDs, this.client.userAgent());
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
     * The operation to delete virtual machines in a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @param instanceIds Gets or sets the virtual machine scale set instance ids.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> beginDeleteInstances(String resourceGroupName, String vmScaleSetName, List<String> instanceIds) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (vmScaleSetName == null) {
            throw new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        if (instanceIds == null) {
            throw new IllegalArgumentException("Parameter instanceIds is required and cannot be null.");
        }
        Validator.validate(instanceIds);
        VirtualMachineScaleSetVMInstanceRequiredIDs vmInstanceIDs = new VirtualMachineScaleSetVMInstanceRequiredIDs();
        vmInstanceIDs.withInstanceIds(instanceIds);
        Call<ResponseBody> call = service.beginDeleteInstances(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), vmInstanceIDs, this.client.userAgent());
        return beginDeleteInstancesDelegate(call.execute());
    }

    /**
     * The operation to delete virtual machines in a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @param instanceIds Gets or sets the virtual machine scale set instance ids.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall beginDeleteInstancesAsync(String resourceGroupName, String vmScaleSetName, List<String> instanceIds, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (vmScaleSetName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null."));
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
        if (instanceIds == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter instanceIds is required and cannot be null."));
            return null;
        }
        Validator.validate(instanceIds, serviceCallback);
        VirtualMachineScaleSetVMInstanceRequiredIDs vmInstanceIDs = new VirtualMachineScaleSetVMInstanceRequiredIDs();
        vmInstanceIDs.withInstanceIds(instanceIds);
        Call<ResponseBody> call = service.beginDeleteInstances(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), vmInstanceIDs, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(beginDeleteInstancesDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<Void> beginDeleteInstancesDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, CloudException>(this.client.restClient().mapperAdapter())
                .register(202, new TypeToken<Void>() { }.getType())
                .build(response);
    }

    /**
     * The operation to get a virtual machine scale set instance view.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the VirtualMachineScaleSetInstanceViewInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<VirtualMachineScaleSetInstanceViewInner> getInstanceView(String resourceGroupName, String vmScaleSetName) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (vmScaleSetName == null) {
            throw new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.getInstanceView(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getInstanceViewDelegate(call.execute());
    }

    /**
     * The operation to get a virtual machine scale set instance view.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getInstanceViewAsync(String resourceGroupName, String vmScaleSetName, final ServiceCallback<VirtualMachineScaleSetInstanceViewInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (vmScaleSetName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null."));
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
        Call<ResponseBody> call = service.getInstanceView(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<VirtualMachineScaleSetInstanceViewInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getInstanceViewDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<VirtualMachineScaleSetInstanceViewInner> getInstanceViewDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<VirtualMachineScaleSetInstanceViewInner, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<VirtualMachineScaleSetInstanceViewInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * The operation to list virtual machine scale sets under a resource group.
     *
     * @param resourceGroupName The name of the resource group.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;VirtualMachineScaleSetInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<VirtualMachineScaleSetInner>> list(final String resourceGroupName) throws CloudException, IOException, IllegalArgumentException {
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
        ServiceResponse<PageImpl1<VirtualMachineScaleSetInner>> response = listDelegate(call.execute());
        PagedList<VirtualMachineScaleSetInner> result = new PagedList<VirtualMachineScaleSetInner>(response.getBody()) {
            @Override
            public Page<VirtualMachineScaleSetInner> nextPage(String nextPageLink) throws CloudException, IOException {
                return listNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * The operation to list virtual machine scale sets under a resource group.
     *
     * @param resourceGroupName The name of the resource group.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listAsync(final String resourceGroupName, final ListOperationCallback<VirtualMachineScaleSetInner> serviceCallback) throws IllegalArgumentException {
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
        call.enqueue(new ServiceResponseCallback<List<VirtualMachineScaleSetInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl1<VirtualMachineScaleSetInner>> result = listDelegate(response);
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

    private ServiceResponse<PageImpl1<VirtualMachineScaleSetInner>> listDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl1<VirtualMachineScaleSetInner>, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl1<VirtualMachineScaleSetInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Gets the list of Virtual Machine Scale Sets in the subscription. Use nextLink property in the response to get the next page of Virtual Machine Scale Sets. Do this till nextLink is not null to fetch all the Virtual Machine Scale Sets.
     *
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;VirtualMachineScaleSetInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<VirtualMachineScaleSetInner>> listAll() throws CloudException, IOException, IllegalArgumentException {
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.listAll(this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        ServiceResponse<PageImpl1<VirtualMachineScaleSetInner>> response = listAllDelegate(call.execute());
        PagedList<VirtualMachineScaleSetInner> result = new PagedList<VirtualMachineScaleSetInner>(response.getBody()) {
            @Override
            public Page<VirtualMachineScaleSetInner> nextPage(String nextPageLink) throws CloudException, IOException {
                return listAllNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Gets the list of Virtual Machine Scale Sets in the subscription. Use nextLink property in the response to get the next page of Virtual Machine Scale Sets. Do this till nextLink is not null to fetch all the Virtual Machine Scale Sets.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listAllAsync(final ListOperationCallback<VirtualMachineScaleSetInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.listAll(this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<VirtualMachineScaleSetInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl1<VirtualMachineScaleSetInner>> result = listAllDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listAllNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
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

    private ServiceResponse<PageImpl1<VirtualMachineScaleSetInner>> listAllDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl1<VirtualMachineScaleSetInner>, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl1<VirtualMachineScaleSetInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * The operation to list available skus for a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;VirtualMachineScaleSetSkuInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<VirtualMachineScaleSetSkuInner>> listSkus(final String resourceGroupName, final String vmScaleSetName) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (vmScaleSetName == null) {
            throw new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.listSkus(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        ServiceResponse<PageImpl1<VirtualMachineScaleSetSkuInner>> response = listSkusDelegate(call.execute());
        PagedList<VirtualMachineScaleSetSkuInner> result = new PagedList<VirtualMachineScaleSetSkuInner>(response.getBody()) {
            @Override
            public Page<VirtualMachineScaleSetSkuInner> nextPage(String nextPageLink) throws CloudException, IOException {
                return listSkusNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * The operation to list available skus for a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listSkusAsync(final String resourceGroupName, final String vmScaleSetName, final ListOperationCallback<VirtualMachineScaleSetSkuInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (vmScaleSetName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null."));
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
        Call<ResponseBody> call = service.listSkus(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<VirtualMachineScaleSetSkuInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl1<VirtualMachineScaleSetSkuInner>> result = listSkusDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listSkusNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
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

    private ServiceResponse<PageImpl1<VirtualMachineScaleSetSkuInner>> listSkusDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl1<VirtualMachineScaleSetSkuInner>, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl1<VirtualMachineScaleSetSkuInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * The operation to power off (stop) virtual machines in a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @throws InterruptedException exception thrown when long running operation is interrupted
     * @return the ServiceResponse object if successful.
     */
    public ServiceResponse<Void> powerOff(String resourceGroupName, String vmScaleSetName) throws CloudException, IOException, IllegalArgumentException, InterruptedException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (vmScaleSetName == null) {
            throw new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String instanceIdsConverted = null;
        VirtualMachineScaleSetVMInstanceIDs vmInstanceIDs = new VirtualMachineScaleSetVMInstanceIDs();
        vmInstanceIDs.withInstanceIds(null);
        Response<ResponseBody> result = service.powerOff(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), vmInstanceIDs, this.client.userAgent()).execute();
        return client.getAzureClient().getPostOrDeleteResult(result, new TypeToken<Void>() { }.getType());
    }

    /**
     * The operation to power off (stop) virtual machines in a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    public ServiceCall powerOffAsync(String resourceGroupName, String vmScaleSetName, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
        }
        if (vmScaleSetName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null."));
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
        }
        final String instanceIdsConverted = null;
        VirtualMachineScaleSetVMInstanceIDs vmInstanceIDs = new VirtualMachineScaleSetVMInstanceIDs();
        vmInstanceIDs.withInstanceIds(null);
        Call<ResponseBody> call = service.powerOff(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), vmInstanceIDs, this.client.userAgent());
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
     * The operation to power off (stop) virtual machines in a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @param instanceIds Gets or sets the virtual machine scale set instance ids.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @throws InterruptedException exception thrown when long running operation is interrupted
     * @return the ServiceResponse object if successful.
     */
    public ServiceResponse<Void> powerOff(String resourceGroupName, String vmScaleSetName, List<String> instanceIds) throws CloudException, IOException, IllegalArgumentException, InterruptedException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (vmScaleSetName == null) {
            throw new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(instanceIds);
        VirtualMachineScaleSetVMInstanceIDs vmInstanceIDs = null;
        if (instanceIds != null) {
            vmInstanceIDs = new VirtualMachineScaleSetVMInstanceIDs();
            vmInstanceIDs.withInstanceIds(instanceIds);
        }
        Response<ResponseBody> result = service.powerOff(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), vmInstanceIDs, this.client.userAgent()).execute();
        return client.getAzureClient().getPostOrDeleteResult(result, new TypeToken<Void>() { }.getType());
    }

    /**
     * The operation to power off (stop) virtual machines in a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @param instanceIds Gets or sets the virtual machine scale set instance ids.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    public ServiceCall powerOffAsync(String resourceGroupName, String vmScaleSetName, List<String> instanceIds, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
        }
        if (vmScaleSetName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null."));
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
        }
        Validator.validate(instanceIds, serviceCallback);
        VirtualMachineScaleSetVMInstanceIDs vmInstanceIDs = null;
        if (instanceIds != null) {
            vmInstanceIDs = new VirtualMachineScaleSetVMInstanceIDs();
            vmInstanceIDs.withInstanceIds(instanceIds);
        }
        Call<ResponseBody> call = service.powerOff(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), vmInstanceIDs, this.client.userAgent());
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
     * The operation to power off (stop) virtual machines in a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> beginPowerOff(String resourceGroupName, String vmScaleSetName) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (vmScaleSetName == null) {
            throw new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String instanceIdsConverted = null;
        VirtualMachineScaleSetVMInstanceIDs vmInstanceIDs = new VirtualMachineScaleSetVMInstanceIDs();
        vmInstanceIDs.withInstanceIds(null);
        Call<ResponseBody> call = service.beginPowerOff(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), vmInstanceIDs, this.client.userAgent());
        return beginPowerOffDelegate(call.execute());
    }

    /**
     * The operation to power off (stop) virtual machines in a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall beginPowerOffAsync(String resourceGroupName, String vmScaleSetName, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (vmScaleSetName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null."));
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
        final String instanceIdsConverted = null;
        VirtualMachineScaleSetVMInstanceIDs vmInstanceIDs = new VirtualMachineScaleSetVMInstanceIDs();
        vmInstanceIDs.withInstanceIds(null);
        Call<ResponseBody> call = service.beginPowerOff(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), vmInstanceIDs, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(beginPowerOffDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * The operation to power off (stop) virtual machines in a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @param instanceIds Gets or sets the virtual machine scale set instance ids.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> beginPowerOff(String resourceGroupName, String vmScaleSetName, List<String> instanceIds) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (vmScaleSetName == null) {
            throw new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(instanceIds);
        VirtualMachineScaleSetVMInstanceIDs vmInstanceIDs = null;
        if (instanceIds != null) {
            vmInstanceIDs = new VirtualMachineScaleSetVMInstanceIDs();
            vmInstanceIDs.withInstanceIds(instanceIds);
        }
        Call<ResponseBody> call = service.beginPowerOff(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), vmInstanceIDs, this.client.userAgent());
        return beginPowerOffDelegate(call.execute());
    }

    /**
     * The operation to power off (stop) virtual machines in a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @param instanceIds Gets or sets the virtual machine scale set instance ids.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall beginPowerOffAsync(String resourceGroupName, String vmScaleSetName, List<String> instanceIds, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (vmScaleSetName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null."));
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
        Validator.validate(instanceIds, serviceCallback);
        VirtualMachineScaleSetVMInstanceIDs vmInstanceIDs = null;
        if (instanceIds != null) {
            vmInstanceIDs = new VirtualMachineScaleSetVMInstanceIDs();
            vmInstanceIDs.withInstanceIds(instanceIds);
        }
        Call<ResponseBody> call = service.beginPowerOff(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), vmInstanceIDs, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(beginPowerOffDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<Void> beginPowerOffDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, CloudException>(this.client.restClient().mapperAdapter())
                .register(202, new TypeToken<Void>() { }.getType())
                .build(response);
    }

    /**
     * The operation to restart virtual machines in a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @throws InterruptedException exception thrown when long running operation is interrupted
     * @return the ServiceResponse object if successful.
     */
    public ServiceResponse<Void> restart(String resourceGroupName, String vmScaleSetName) throws CloudException, IOException, IllegalArgumentException, InterruptedException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (vmScaleSetName == null) {
            throw new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String instanceIdsConverted = null;
        VirtualMachineScaleSetVMInstanceIDs vmInstanceIDs = new VirtualMachineScaleSetVMInstanceIDs();
        vmInstanceIDs.withInstanceIds(null);
        Response<ResponseBody> result = service.restart(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), vmInstanceIDs, this.client.userAgent()).execute();
        return client.getAzureClient().getPostOrDeleteResult(result, new TypeToken<Void>() { }.getType());
    }

    /**
     * The operation to restart virtual machines in a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    public ServiceCall restartAsync(String resourceGroupName, String vmScaleSetName, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
        }
        if (vmScaleSetName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null."));
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
        }
        final String instanceIdsConverted = null;
        VirtualMachineScaleSetVMInstanceIDs vmInstanceIDs = new VirtualMachineScaleSetVMInstanceIDs();
        vmInstanceIDs.withInstanceIds(null);
        Call<ResponseBody> call = service.restart(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), vmInstanceIDs, this.client.userAgent());
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
     * The operation to restart virtual machines in a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @param instanceIds Gets or sets the virtual machine scale set instance ids.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @throws InterruptedException exception thrown when long running operation is interrupted
     * @return the ServiceResponse object if successful.
     */
    public ServiceResponse<Void> restart(String resourceGroupName, String vmScaleSetName, List<String> instanceIds) throws CloudException, IOException, IllegalArgumentException, InterruptedException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (vmScaleSetName == null) {
            throw new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(instanceIds);
        VirtualMachineScaleSetVMInstanceIDs vmInstanceIDs = null;
        if (instanceIds != null) {
            vmInstanceIDs = new VirtualMachineScaleSetVMInstanceIDs();
            vmInstanceIDs.withInstanceIds(instanceIds);
        }
        Response<ResponseBody> result = service.restart(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), vmInstanceIDs, this.client.userAgent()).execute();
        return client.getAzureClient().getPostOrDeleteResult(result, new TypeToken<Void>() { }.getType());
    }

    /**
     * The operation to restart virtual machines in a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @param instanceIds Gets or sets the virtual machine scale set instance ids.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    public ServiceCall restartAsync(String resourceGroupName, String vmScaleSetName, List<String> instanceIds, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
        }
        if (vmScaleSetName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null."));
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
        }
        Validator.validate(instanceIds, serviceCallback);
        VirtualMachineScaleSetVMInstanceIDs vmInstanceIDs = null;
        if (instanceIds != null) {
            vmInstanceIDs = new VirtualMachineScaleSetVMInstanceIDs();
            vmInstanceIDs.withInstanceIds(instanceIds);
        }
        Call<ResponseBody> call = service.restart(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), vmInstanceIDs, this.client.userAgent());
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
     * The operation to restart virtual machines in a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> beginRestart(String resourceGroupName, String vmScaleSetName) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (vmScaleSetName == null) {
            throw new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String instanceIdsConverted = null;
        VirtualMachineScaleSetVMInstanceIDs vmInstanceIDs = new VirtualMachineScaleSetVMInstanceIDs();
        vmInstanceIDs.withInstanceIds(null);
        Call<ResponseBody> call = service.beginRestart(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), vmInstanceIDs, this.client.userAgent());
        return beginRestartDelegate(call.execute());
    }

    /**
     * The operation to restart virtual machines in a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall beginRestartAsync(String resourceGroupName, String vmScaleSetName, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (vmScaleSetName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null."));
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
        final String instanceIdsConverted = null;
        VirtualMachineScaleSetVMInstanceIDs vmInstanceIDs = new VirtualMachineScaleSetVMInstanceIDs();
        vmInstanceIDs.withInstanceIds(null);
        Call<ResponseBody> call = service.beginRestart(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), vmInstanceIDs, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(beginRestartDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * The operation to restart virtual machines in a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @param instanceIds Gets or sets the virtual machine scale set instance ids.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> beginRestart(String resourceGroupName, String vmScaleSetName, List<String> instanceIds) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (vmScaleSetName == null) {
            throw new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(instanceIds);
        VirtualMachineScaleSetVMInstanceIDs vmInstanceIDs = null;
        if (instanceIds != null) {
            vmInstanceIDs = new VirtualMachineScaleSetVMInstanceIDs();
            vmInstanceIDs.withInstanceIds(instanceIds);
        }
        Call<ResponseBody> call = service.beginRestart(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), vmInstanceIDs, this.client.userAgent());
        return beginRestartDelegate(call.execute());
    }

    /**
     * The operation to restart virtual machines in a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @param instanceIds Gets or sets the virtual machine scale set instance ids.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall beginRestartAsync(String resourceGroupName, String vmScaleSetName, List<String> instanceIds, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (vmScaleSetName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null."));
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
        Validator.validate(instanceIds, serviceCallback);
        VirtualMachineScaleSetVMInstanceIDs vmInstanceIDs = null;
        if (instanceIds != null) {
            vmInstanceIDs = new VirtualMachineScaleSetVMInstanceIDs();
            vmInstanceIDs.withInstanceIds(instanceIds);
        }
        Call<ResponseBody> call = service.beginRestart(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), vmInstanceIDs, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(beginRestartDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<Void> beginRestartDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, CloudException>(this.client.restClient().mapperAdapter())
                .register(202, new TypeToken<Void>() { }.getType())
                .build(response);
    }

    /**
     * The operation to start virtual machines in a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @throws InterruptedException exception thrown when long running operation is interrupted
     * @return the ServiceResponse object if successful.
     */
    public ServiceResponse<Void> start(String resourceGroupName, String vmScaleSetName) throws CloudException, IOException, IllegalArgumentException, InterruptedException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (vmScaleSetName == null) {
            throw new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String instanceIdsConverted = null;
        VirtualMachineScaleSetVMInstanceIDs vmInstanceIDs = new VirtualMachineScaleSetVMInstanceIDs();
        vmInstanceIDs.withInstanceIds(null);
        Response<ResponseBody> result = service.start(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), vmInstanceIDs, this.client.userAgent()).execute();
        return client.getAzureClient().getPostOrDeleteResult(result, new TypeToken<Void>() { }.getType());
    }

    /**
     * The operation to start virtual machines in a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    public ServiceCall startAsync(String resourceGroupName, String vmScaleSetName, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
        }
        if (vmScaleSetName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null."));
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
        }
        final String instanceIdsConverted = null;
        VirtualMachineScaleSetVMInstanceIDs vmInstanceIDs = new VirtualMachineScaleSetVMInstanceIDs();
        vmInstanceIDs.withInstanceIds(null);
        Call<ResponseBody> call = service.start(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), vmInstanceIDs, this.client.userAgent());
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
     * The operation to start virtual machines in a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @param instanceIds Gets or sets the virtual machine scale set instance ids.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @throws InterruptedException exception thrown when long running operation is interrupted
     * @return the ServiceResponse object if successful.
     */
    public ServiceResponse<Void> start(String resourceGroupName, String vmScaleSetName, List<String> instanceIds) throws CloudException, IOException, IllegalArgumentException, InterruptedException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (vmScaleSetName == null) {
            throw new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(instanceIds);
        VirtualMachineScaleSetVMInstanceIDs vmInstanceIDs = null;
        if (instanceIds != null) {
            vmInstanceIDs = new VirtualMachineScaleSetVMInstanceIDs();
            vmInstanceIDs.withInstanceIds(instanceIds);
        }
        Response<ResponseBody> result = service.start(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), vmInstanceIDs, this.client.userAgent()).execute();
        return client.getAzureClient().getPostOrDeleteResult(result, new TypeToken<Void>() { }.getType());
    }

    /**
     * The operation to start virtual machines in a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @param instanceIds Gets or sets the virtual machine scale set instance ids.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    public ServiceCall startAsync(String resourceGroupName, String vmScaleSetName, List<String> instanceIds, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
        }
        if (vmScaleSetName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null."));
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
        }
        Validator.validate(instanceIds, serviceCallback);
        VirtualMachineScaleSetVMInstanceIDs vmInstanceIDs = null;
        if (instanceIds != null) {
            vmInstanceIDs = new VirtualMachineScaleSetVMInstanceIDs();
            vmInstanceIDs.withInstanceIds(instanceIds);
        }
        Call<ResponseBody> call = service.start(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), vmInstanceIDs, this.client.userAgent());
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
     * The operation to start virtual machines in a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> beginStart(String resourceGroupName, String vmScaleSetName) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (vmScaleSetName == null) {
            throw new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String instanceIdsConverted = null;
        VirtualMachineScaleSetVMInstanceIDs vmInstanceIDs = new VirtualMachineScaleSetVMInstanceIDs();
        vmInstanceIDs.withInstanceIds(null);
        Call<ResponseBody> call = service.beginStart(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), vmInstanceIDs, this.client.userAgent());
        return beginStartDelegate(call.execute());
    }

    /**
     * The operation to start virtual machines in a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall beginStartAsync(String resourceGroupName, String vmScaleSetName, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (vmScaleSetName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null."));
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
        final String instanceIdsConverted = null;
        VirtualMachineScaleSetVMInstanceIDs vmInstanceIDs = new VirtualMachineScaleSetVMInstanceIDs();
        vmInstanceIDs.withInstanceIds(null);
        Call<ResponseBody> call = service.beginStart(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), vmInstanceIDs, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(beginStartDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * The operation to start virtual machines in a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @param instanceIds Gets or sets the virtual machine scale set instance ids.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> beginStart(String resourceGroupName, String vmScaleSetName, List<String> instanceIds) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (vmScaleSetName == null) {
            throw new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(instanceIds);
        VirtualMachineScaleSetVMInstanceIDs vmInstanceIDs = null;
        if (instanceIds != null) {
            vmInstanceIDs = new VirtualMachineScaleSetVMInstanceIDs();
            vmInstanceIDs.withInstanceIds(instanceIds);
        }
        Call<ResponseBody> call = service.beginStart(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), vmInstanceIDs, this.client.userAgent());
        return beginStartDelegate(call.execute());
    }

    /**
     * The operation to start virtual machines in a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @param instanceIds Gets or sets the virtual machine scale set instance ids.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall beginStartAsync(String resourceGroupName, String vmScaleSetName, List<String> instanceIds, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (vmScaleSetName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null."));
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
        Validator.validate(instanceIds, serviceCallback);
        VirtualMachineScaleSetVMInstanceIDs vmInstanceIDs = null;
        if (instanceIds != null) {
            vmInstanceIDs = new VirtualMachineScaleSetVMInstanceIDs();
            vmInstanceIDs.withInstanceIds(instanceIds);
        }
        Call<ResponseBody> call = service.beginStart(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), vmInstanceIDs, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(beginStartDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<Void> beginStartDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, CloudException>(this.client.restClient().mapperAdapter())
                .register(202, new TypeToken<Void>() { }.getType())
                .build(response);
    }

    /**
     * The operation to manually upgrade virtual machines in a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @param instanceIds Gets or sets the virtual machine scale set instance ids.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @throws InterruptedException exception thrown when long running operation is interrupted
     * @return the ServiceResponse object if successful.
     */
    public ServiceResponse<Void> updateInstances(String resourceGroupName, String vmScaleSetName, List<String> instanceIds) throws CloudException, IOException, IllegalArgumentException, InterruptedException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (vmScaleSetName == null) {
            throw new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        if (instanceIds == null) {
            throw new IllegalArgumentException("Parameter instanceIds is required and cannot be null.");
        }
        Validator.validate(instanceIds);
        VirtualMachineScaleSetVMInstanceRequiredIDs vmInstanceIDs = new VirtualMachineScaleSetVMInstanceRequiredIDs();
        vmInstanceIDs.withInstanceIds(instanceIds);
        Response<ResponseBody> result = service.updateInstances(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), vmInstanceIDs, this.client.userAgent()).execute();
        return client.getAzureClient().getPostOrDeleteResult(result, new TypeToken<Void>() { }.getType());
    }

    /**
     * The operation to manually upgrade virtual machines in a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @param instanceIds Gets or sets the virtual machine scale set instance ids.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    public ServiceCall updateInstancesAsync(String resourceGroupName, String vmScaleSetName, List<String> instanceIds, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
        }
        if (vmScaleSetName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null."));
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
        }
        if (instanceIds == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter instanceIds is required and cannot be null."));
        }
        Validator.validate(instanceIds, serviceCallback);
        VirtualMachineScaleSetVMInstanceRequiredIDs vmInstanceIDs = new VirtualMachineScaleSetVMInstanceRequiredIDs();
        vmInstanceIDs.withInstanceIds(instanceIds);
        Call<ResponseBody> call = service.updateInstances(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), vmInstanceIDs, this.client.userAgent());
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
     * The operation to manually upgrade virtual machines in a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @param instanceIds Gets or sets the virtual machine scale set instance ids.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> beginUpdateInstances(String resourceGroupName, String vmScaleSetName, List<String> instanceIds) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (vmScaleSetName == null) {
            throw new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        if (instanceIds == null) {
            throw new IllegalArgumentException("Parameter instanceIds is required and cannot be null.");
        }
        Validator.validate(instanceIds);
        VirtualMachineScaleSetVMInstanceRequiredIDs vmInstanceIDs = new VirtualMachineScaleSetVMInstanceRequiredIDs();
        vmInstanceIDs.withInstanceIds(instanceIds);
        Call<ResponseBody> call = service.beginUpdateInstances(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), vmInstanceIDs, this.client.userAgent());
        return beginUpdateInstancesDelegate(call.execute());
    }

    /**
     * The operation to manually upgrade virtual machines in a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @param instanceIds Gets or sets the virtual machine scale set instance ids.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall beginUpdateInstancesAsync(String resourceGroupName, String vmScaleSetName, List<String> instanceIds, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (vmScaleSetName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null."));
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
        if (instanceIds == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter instanceIds is required and cannot be null."));
            return null;
        }
        Validator.validate(instanceIds, serviceCallback);
        VirtualMachineScaleSetVMInstanceRequiredIDs vmInstanceIDs = new VirtualMachineScaleSetVMInstanceRequiredIDs();
        vmInstanceIDs.withInstanceIds(instanceIds);
        Call<ResponseBody> call = service.beginUpdateInstances(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), vmInstanceIDs, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(beginUpdateInstancesDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<Void> beginUpdateInstancesDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, CloudException>(this.client.restClient().mapperAdapter())
                .register(202, new TypeToken<Void>() { }.getType())
                .build(response);
    }

    /**
     * The operation to re-image virtual machines in a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @throws InterruptedException exception thrown when long running operation is interrupted
     * @return the ServiceResponse object if successful.
     */
    public ServiceResponse<Void> reimage(String resourceGroupName, String vmScaleSetName) throws CloudException, IOException, IllegalArgumentException, InterruptedException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (vmScaleSetName == null) {
            throw new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Response<ResponseBody> result = service.reimage(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent()).execute();
        return client.getAzureClient().getPostOrDeleteResult(result, new TypeToken<Void>() { }.getType());
    }

    /**
     * The operation to re-image virtual machines in a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    public ServiceCall reimageAsync(String resourceGroupName, String vmScaleSetName, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
        }
        if (vmScaleSetName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null."));
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
        }
        Call<ResponseBody> call = service.reimage(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
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
     * The operation to re-image virtual machines in a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> beginReimage(String resourceGroupName, String vmScaleSetName) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (vmScaleSetName == null) {
            throw new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.beginReimage(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return beginReimageDelegate(call.execute());
    }

    /**
     * The operation to re-image virtual machines in a virtual machine scale set.
     *
     * @param resourceGroupName The name of the resource group.
     * @param vmScaleSetName The name of the virtual machine scale set.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall beginReimageAsync(String resourceGroupName, String vmScaleSetName, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (vmScaleSetName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter vmScaleSetName is required and cannot be null."));
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
        Call<ResponseBody> call = service.beginReimage(resourceGroupName, vmScaleSetName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(beginReimageDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<Void> beginReimageDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, CloudException>(this.client.restClient().mapperAdapter())
                .register(202, new TypeToken<Void>() { }.getType())
                .build(response);
    }

    /**
     * The operation to list virtual machine scale sets under a resource group.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;VirtualMachineScaleSetInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl1<VirtualMachineScaleSetInner>> listNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        return listNextDelegate(call.execute());
    }

    /**
     * The operation to list virtual machine scale sets under a resource group.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<VirtualMachineScaleSetInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<VirtualMachineScaleSetInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl1<VirtualMachineScaleSetInner>> result = listNextDelegate(response);
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

    private ServiceResponse<PageImpl1<VirtualMachineScaleSetInner>> listNextDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl1<VirtualMachineScaleSetInner>, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl1<VirtualMachineScaleSetInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Gets the list of Virtual Machine Scale Sets in the subscription. Use nextLink property in the response to get the next page of Virtual Machine Scale Sets. Do this till nextLink is not null to fetch all the Virtual Machine Scale Sets.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;VirtualMachineScaleSetInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl1<VirtualMachineScaleSetInner>> listAllNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        Call<ResponseBody> call = service.listAllNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        return listAllNextDelegate(call.execute());
    }

    /**
     * Gets the list of Virtual Machine Scale Sets in the subscription. Use nextLink property in the response to get the next page of Virtual Machine Scale Sets. Do this till nextLink is not null to fetch all the Virtual Machine Scale Sets.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listAllNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<VirtualMachineScaleSetInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.listAllNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<VirtualMachineScaleSetInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl1<VirtualMachineScaleSetInner>> result = listAllNextDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listAllNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
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

    private ServiceResponse<PageImpl1<VirtualMachineScaleSetInner>> listAllNextDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl1<VirtualMachineScaleSetInner>, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl1<VirtualMachineScaleSetInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * The operation to list available skus for a virtual machine scale set.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;VirtualMachineScaleSetSkuInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl1<VirtualMachineScaleSetSkuInner>> listSkusNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        Call<ResponseBody> call = service.listSkusNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        return listSkusNextDelegate(call.execute());
    }

    /**
     * The operation to list available skus for a virtual machine scale set.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listSkusNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<VirtualMachineScaleSetSkuInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.listSkusNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<VirtualMachineScaleSetSkuInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl1<VirtualMachineScaleSetSkuInner>> result = listSkusNextDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listSkusNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
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

    private ServiceResponse<PageImpl1<VirtualMachineScaleSetSkuInner>> listSkusNextDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl1<VirtualMachineScaleSetSkuInner>, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl1<VirtualMachineScaleSetSkuInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

}
