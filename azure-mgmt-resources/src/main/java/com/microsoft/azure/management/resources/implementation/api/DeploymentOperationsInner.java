/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation.api;

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
import java.io.IOException;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;
import retrofit2.Response;

/**
 * An instance of this class provides access to all the operations defined
 * in DeploymentOperations.
 */
public final class DeploymentOperationsInner {
    /** The Retrofit service to perform REST calls. */
    private DeploymentOperationsService service;
    /** The service client containing this operation class. */
    private ResourceManagementClientImpl client;

    /**
     * Initializes an instance of DeploymentOperationsInner.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public DeploymentOperationsInner(Retrofit retrofit, ResourceManagementClientImpl client) {
        this.service = retrofit.create(DeploymentOperationsService.class);
        this.client = client;
    }

    /**
     * The interface defining all the services for DeploymentOperations to be
     * used by Retrofit to perform actually REST calls.
     */
    interface DeploymentOperationsService {
        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourcegroups/{resourceGroupName}/deployments/{deploymentName}/operations/{operationId}")
        Call<ResponseBody> get(@Path("resourceGroupName") String resourceGroupName, @Path("deploymentName") String deploymentName, @Path("operationId") String operationId, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourcegroups/{resourceGroupName}/deployments/{deploymentName}/operations")
        Call<ResponseBody> list(@Path("resourceGroupName") String resourceGroupName, @Path("deploymentName") String deploymentName, @Path("subscriptionId") String subscriptionId, @Query("$top") Integer top, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET
        Call<ResponseBody> listNext(@Url String nextPageLink, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

    }

    /**
     * Get a list of deployments operations.
     *
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param deploymentName The name of the deployment.
     * @param operationId Operation Id.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the DeploymentOperationInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<DeploymentOperationInner> get(String resourceGroupName, String deploymentName, String operationId) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (deploymentName == null) {
            throw new IllegalArgumentException("Parameter deploymentName is required and cannot be null.");
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
        Call<ResponseBody> call = service.get(resourceGroupName, deploymentName, operationId, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getDelegate(call.execute());
    }

    /**
     * Get a list of deployments operations.
     *
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param deploymentName The name of the deployment.
     * @param operationId Operation Id.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getAsync(String resourceGroupName, String deploymentName, String operationId, final ServiceCallback<DeploymentOperationInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (deploymentName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter deploymentName is required and cannot be null."));
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
        Call<ResponseBody> call = service.get(resourceGroupName, deploymentName, operationId, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<DeploymentOperationInner>(serviceCallback) {
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

    private ServiceResponse<DeploymentOperationInner> getDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<DeploymentOperationInner, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<DeploymentOperationInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Gets a list of deployments operations.
     *
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param deploymentName The name of the deployment.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;DeploymentOperationInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<DeploymentOperationInner>> list(final String resourceGroupName, final String deploymentName) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (deploymentName == null) {
            throw new IllegalArgumentException("Parameter deploymentName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final Integer top = null;
        Call<ResponseBody> call = service.list(resourceGroupName, deploymentName, this.client.subscriptionId(), top, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        ServiceResponse<PageImpl<DeploymentOperationInner>> response = listDelegate(call.execute());
        PagedList<DeploymentOperationInner> result = new PagedList<DeploymentOperationInner>(response.getBody()) {
            @Override
            public Page<DeploymentOperationInner> nextPage(String nextPageLink) throws CloudException, IOException {
                return listNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Gets a list of deployments operations.
     *
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param deploymentName The name of the deployment.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listAsync(final String resourceGroupName, final String deploymentName, final ListOperationCallback<DeploymentOperationInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (deploymentName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter deploymentName is required and cannot be null."));
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
        final Integer top = null;
        Call<ResponseBody> call = service.list(resourceGroupName, deploymentName, this.client.subscriptionId(), top, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<DeploymentOperationInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<DeploymentOperationInner>> result = listDelegate(response);
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
     * Gets a list of deployments operations.
     *
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param deploymentName The name of the deployment.
     * @param top Query parameters.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;DeploymentOperationInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<DeploymentOperationInner>> list(final String resourceGroupName, final String deploymentName, final Integer top) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (deploymentName == null) {
            throw new IllegalArgumentException("Parameter deploymentName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.list(resourceGroupName, deploymentName, this.client.subscriptionId(), top, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        ServiceResponse<PageImpl<DeploymentOperationInner>> response = listDelegate(call.execute());
        PagedList<DeploymentOperationInner> result = new PagedList<DeploymentOperationInner>(response.getBody()) {
            @Override
            public Page<DeploymentOperationInner> nextPage(String nextPageLink) throws CloudException, IOException {
                return listNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Gets a list of deployments operations.
     *
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param deploymentName The name of the deployment.
     * @param top Query parameters.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listAsync(final String resourceGroupName, final String deploymentName, final Integer top, final ListOperationCallback<DeploymentOperationInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (deploymentName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter deploymentName is required and cannot be null."));
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
        Call<ResponseBody> call = service.list(resourceGroupName, deploymentName, this.client.subscriptionId(), top, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<DeploymentOperationInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<DeploymentOperationInner>> result = listDelegate(response);
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

    private ServiceResponse<PageImpl<DeploymentOperationInner>> listDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<DeploymentOperationInner>, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<PageImpl<DeploymentOperationInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Gets a list of deployments operations.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;DeploymentOperationInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl<DeploymentOperationInner>> listNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        return listNextDelegate(call.execute());
    }

    /**
     * Gets a list of deployments operations.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<DeploymentOperationInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<DeploymentOperationInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<DeploymentOperationInner>> result = listNextDelegate(response);
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

    private ServiceResponse<PageImpl<DeploymentOperationInner>> listNextDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<DeploymentOperationInner>, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<PageImpl<DeploymentOperationInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

}
