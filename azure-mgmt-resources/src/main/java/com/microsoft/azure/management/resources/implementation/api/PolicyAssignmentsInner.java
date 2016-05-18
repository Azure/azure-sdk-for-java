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
import com.microsoft.rest.Validator;
import java.io.IOException;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.HTTP;
import retrofit2.http.Path;
import retrofit2.http.PUT;
import retrofit2.http.Query;
import retrofit2.http.Url;
import retrofit2.Response;

/**
 * An instance of this class provides access to all the operations defined
 * in PolicyAssignments.
 */
public final class PolicyAssignmentsInner {
    /** The Retrofit service to perform REST calls. */
    private PolicyAssignmentsService service;
    /** The service client containing this operation class. */
    private ResourceManagementClientImpl client;

    /**
     * Initializes an instance of PolicyAssignmentsInner.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public PolicyAssignmentsInner(Retrofit retrofit, ResourceManagementClientImpl client) {
        this.service = retrofit.create(PolicyAssignmentsService.class);
        this.client = client;
    }

    /**
     * The interface defining all the services for PolicyAssignments to be
     * used by Retrofit to perform actually REST calls.
     */
    interface PolicyAssignmentsService {
        @Headers({"Content-Type: application/json; charset=utf-8", "User-Agent: (PolicyAssignments, 2015-11-01)"})
        @GET("subscriptions/{subscriptionId}/resourcegroups/{resourceGroupName}/providers/{resourceProviderNamespace}/{parentResourcePath}/{resourceType}/{resourceName}providers/Microsoft.Authorization/policyAssignments")
        Call<ResponseBody> listForResource(@Path("resourceGroupName") String resourceGroupName, @Path("resourceProviderNamespace") String resourceProviderNamespace, @Path("parentResourcePath") String parentResourcePath, @Path("resourceType") String resourceType, @Path("resourceName") String resourceName, @Path("subscriptionId") String subscriptionId, @Query("$filter") String filter, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage);

        @Headers({"Content-Type: application/json; charset=utf-8", "User-Agent: (PolicyAssignments, 2015-11-01)"})
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Authorization/policyAssignments")
        Call<ResponseBody> listForResourceGroup(@Path("resourceGroupName") String resourceGroupName, @Path("subscriptionId") String subscriptionId, @Query("$filter") String filter, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage);

        @Headers({"Content-Type: application/json; charset=utf-8", "User-Agent: (PolicyAssignments, 2015-11-01)"})
        @HTTP(path = "{scope}/providers/Microsoft.Authorization/policyAssignments/{policyAssignmentName}", method = "DELETE", hasBody = true)
        Call<ResponseBody> delete(@Path("scope") String scope, @Path("policyAssignmentName") String policyAssignmentName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage);

        @Headers({"Content-Type: application/json; charset=utf-8", "User-Agent: (PolicyAssignments, 2015-11-01)"})
        @PUT("{scope}/providers/Microsoft.Authorization/policyAssignments/{policyAssignmentName}")
        Call<ResponseBody> create(@Path("scope") String scope, @Path("policyAssignmentName") String policyAssignmentName, @Path("subscriptionId") String subscriptionId, @Body PolicyAssignmentInner parameters, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage);

        @Headers({"Content-Type: application/json; charset=utf-8", "User-Agent: (PolicyAssignments, 2015-11-01)"})
        @GET("{scope}/providers/Microsoft.Authorization/policyAssignments/{policyAssignmentName}")
        Call<ResponseBody> get(@Path("scope") String scope, @Path("policyAssignmentName") String policyAssignmentName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage);

        @Headers({"Content-Type: application/json; charset=utf-8", "User-Agent: (PolicyAssignments, 2015-11-01)"})
        @HTTP(path = "{policyAssignmentId}", method = "DELETE", hasBody = true)
        Call<ResponseBody> deleteById(@Path("policyAssignmentId") String policyAssignmentId, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage);

        @Headers({"Content-Type: application/json; charset=utf-8", "User-Agent: (PolicyAssignments, 2015-11-01)"})
        @PUT("{policyAssignmentId}")
        Call<ResponseBody> createById(@Path("policyAssignmentId") String policyAssignmentId, @Path("subscriptionId") String subscriptionId, @Body PolicyAssignmentInner parameters, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage);

        @Headers({"Content-Type: application/json; charset=utf-8", "User-Agent: (PolicyAssignments, 2015-11-01)"})
        @GET("{policyAssignmentId}")
        Call<ResponseBody> getById(@Path("policyAssignmentId") String policyAssignmentId, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage);

        @Headers({"Content-Type: application/json; charset=utf-8", "User-Agent: (PolicyAssignments, 2015-11-01)"})
        @GET("subscriptions/{subscriptionId}/providers/Microsoft.Authorization/policyAssignments")
        Call<ResponseBody> list(@Path("subscriptionId") String subscriptionId, @Query("$filter") String filter, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage);

        @Headers({"Content-Type: application/json; charset=utf-8", "User-Agent: (PolicyAssignments, 2015-11-01)"})
        @GET("{scope}/providers/Microsoft.Authorization/policyAssignments")
        Call<ResponseBody> listForScope(@Path("scope") String scope, @Path("subscriptionId") String subscriptionId, @Query("$filter") String filter, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage);

        @Headers({"Content-Type: application/json; charset=utf-8", "User-Agent: (PolicyAssignments, 2015-11-01)"})
        @GET
        Call<ResponseBody> listForResourceNext(@Url String nextPageLink, @Header("accept-language") String acceptLanguage);

        @Headers({"Content-Type: application/json; charset=utf-8", "User-Agent: (PolicyAssignments, 2015-11-01)"})
        @GET
        Call<ResponseBody> listForResourceGroupNext(@Url String nextPageLink, @Header("accept-language") String acceptLanguage);

        @Headers({"Content-Type: application/json; charset=utf-8", "User-Agent: (PolicyAssignments, 2015-11-01)"})
        @GET
        Call<ResponseBody> listNext(@Url String nextPageLink, @Header("accept-language") String acceptLanguage);

        @Headers({"Content-Type: application/json; charset=utf-8", "User-Agent: (PolicyAssignments, 2015-11-01)"})
        @GET
        Call<ResponseBody> listForScopeNext(@Url String nextPageLink, @Header("accept-language") String acceptLanguage);

    }

    /**
     * Gets policy assignments of the resource.
     *
     * @param resourceGroupName The name of the resource group.
     * @param resourceProviderNamespace The name of the resource provider.
     * @param parentResourcePath The parent resource path.
     * @param resourceType The resource type.
     * @param resourceName The resource name.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;PolicyAssignmentInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<PolicyAssignmentInner>> listForResource(final String resourceGroupName, final String resourceProviderNamespace, final String parentResourcePath, final String resourceType, final String resourceName) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (resourceProviderNamespace == null) {
            throw new IllegalArgumentException("Parameter resourceProviderNamespace is required and cannot be null.");
        }
        if (parentResourcePath == null) {
            throw new IllegalArgumentException("Parameter parentResourcePath is required and cannot be null.");
        }
        if (resourceType == null) {
            throw new IllegalArgumentException("Parameter resourceType is required and cannot be null.");
        }
        if (resourceName == null) {
            throw new IllegalArgumentException("Parameter resourceName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String filter = null;
        Call<ResponseBody> call = service.listForResource(resourceGroupName, resourceProviderNamespace, parentResourcePath, resourceType, resourceName, this.client.subscriptionId(), filter, this.client.apiVersion(), this.client.acceptLanguage());
        ServiceResponse<PageImpl<PolicyAssignmentInner>> response = listForResourceDelegate(call.execute());
        PagedList<PolicyAssignmentInner> result = new PagedList<PolicyAssignmentInner>(response.getBody()) {
            @Override
            public Page<PolicyAssignmentInner> nextPage(String nextPageLink) throws CloudException, IOException {
                return listForResourceNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Gets policy assignments of the resource.
     *
     * @param resourceGroupName The name of the resource group.
     * @param resourceProviderNamespace The name of the resource provider.
     * @param parentResourcePath The parent resource path.
     * @param resourceType The resource type.
     * @param resourceName The resource name.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listForResourceAsync(final String resourceGroupName, final String resourceProviderNamespace, final String parentResourcePath, final String resourceType, final String resourceName, final ListOperationCallback<PolicyAssignmentInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (resourceProviderNamespace == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceProviderNamespace is required and cannot be null."));
            return null;
        }
        if (parentResourcePath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter parentResourcePath is required and cannot be null."));
            return null;
        }
        if (resourceType == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceType is required and cannot be null."));
            return null;
        }
        if (resourceName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceName is required and cannot be null."));
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
        Call<ResponseBody> call = service.listForResource(resourceGroupName, resourceProviderNamespace, parentResourcePath, resourceType, resourceName, this.client.subscriptionId(), filter, this.client.apiVersion(), this.client.acceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<PolicyAssignmentInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<PolicyAssignmentInner>> result = listForResourceDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listForResourceNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
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
     * Gets policy assignments of the resource.
     *
     * @param resourceGroupName The name of the resource group.
     * @param resourceProviderNamespace The name of the resource provider.
     * @param parentResourcePath The parent resource path.
     * @param resourceType The resource type.
     * @param resourceName The resource name.
     * @param filter The filter to apply on the operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;PolicyAssignmentInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<PolicyAssignmentInner>> listForResource(final String resourceGroupName, final String resourceProviderNamespace, final String parentResourcePath, final String resourceType, final String resourceName, final String filter) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (resourceProviderNamespace == null) {
            throw new IllegalArgumentException("Parameter resourceProviderNamespace is required and cannot be null.");
        }
        if (parentResourcePath == null) {
            throw new IllegalArgumentException("Parameter parentResourcePath is required and cannot be null.");
        }
        if (resourceType == null) {
            throw new IllegalArgumentException("Parameter resourceType is required and cannot be null.");
        }
        if (resourceName == null) {
            throw new IllegalArgumentException("Parameter resourceName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.listForResource(resourceGroupName, resourceProviderNamespace, parentResourcePath, resourceType, resourceName, this.client.subscriptionId(), filter, this.client.apiVersion(), this.client.acceptLanguage());
        ServiceResponse<PageImpl<PolicyAssignmentInner>> response = listForResourceDelegate(call.execute());
        PagedList<PolicyAssignmentInner> result = new PagedList<PolicyAssignmentInner>(response.getBody()) {
            @Override
            public Page<PolicyAssignmentInner> nextPage(String nextPageLink) throws CloudException, IOException {
                return listForResourceNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Gets policy assignments of the resource.
     *
     * @param resourceGroupName The name of the resource group.
     * @param resourceProviderNamespace The name of the resource provider.
     * @param parentResourcePath The parent resource path.
     * @param resourceType The resource type.
     * @param resourceName The resource name.
     * @param filter The filter to apply on the operation.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listForResourceAsync(final String resourceGroupName, final String resourceProviderNamespace, final String parentResourcePath, final String resourceType, final String resourceName, final String filter, final ListOperationCallback<PolicyAssignmentInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (resourceProviderNamespace == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceProviderNamespace is required and cannot be null."));
            return null;
        }
        if (parentResourcePath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter parentResourcePath is required and cannot be null."));
            return null;
        }
        if (resourceType == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceType is required and cannot be null."));
            return null;
        }
        if (resourceName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceName is required and cannot be null."));
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
        Call<ResponseBody> call = service.listForResource(resourceGroupName, resourceProviderNamespace, parentResourcePath, resourceType, resourceName, this.client.subscriptionId(), filter, this.client.apiVersion(), this.client.acceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<PolicyAssignmentInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<PolicyAssignmentInner>> result = listForResourceDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listForResourceNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
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

    private ServiceResponse<PageImpl<PolicyAssignmentInner>> listForResourceDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<PolicyAssignmentInner>, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<PolicyAssignmentInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Gets policy assignments of the resource group.
     *
     * @param resourceGroupName Resource group name.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;PolicyAssignmentInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<PolicyAssignmentInner>> listForResourceGroup(final String resourceGroupName) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String filter = null;
        Call<ResponseBody> call = service.listForResourceGroup(resourceGroupName, this.client.subscriptionId(), filter, this.client.apiVersion(), this.client.acceptLanguage());
        ServiceResponse<PageImpl<PolicyAssignmentInner>> response = listForResourceGroupDelegate(call.execute());
        PagedList<PolicyAssignmentInner> result = new PagedList<PolicyAssignmentInner>(response.getBody()) {
            @Override
            public Page<PolicyAssignmentInner> nextPage(String nextPageLink) throws CloudException, IOException {
                return listForResourceGroupNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Gets policy assignments of the resource group.
     *
     * @param resourceGroupName Resource group name.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listForResourceGroupAsync(final String resourceGroupName, final ListOperationCallback<PolicyAssignmentInner> serviceCallback) throws IllegalArgumentException {
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
        final String filter = null;
        Call<ResponseBody> call = service.listForResourceGroup(resourceGroupName, this.client.subscriptionId(), filter, this.client.apiVersion(), this.client.acceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<PolicyAssignmentInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<PolicyAssignmentInner>> result = listForResourceGroupDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listForResourceGroupNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
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
     * Gets policy assignments of the resource group.
     *
     * @param resourceGroupName Resource group name.
     * @param filter The filter to apply on the operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;PolicyAssignmentInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<PolicyAssignmentInner>> listForResourceGroup(final String resourceGroupName, final String filter) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.listForResourceGroup(resourceGroupName, this.client.subscriptionId(), filter, this.client.apiVersion(), this.client.acceptLanguage());
        ServiceResponse<PageImpl<PolicyAssignmentInner>> response = listForResourceGroupDelegate(call.execute());
        PagedList<PolicyAssignmentInner> result = new PagedList<PolicyAssignmentInner>(response.getBody()) {
            @Override
            public Page<PolicyAssignmentInner> nextPage(String nextPageLink) throws CloudException, IOException {
                return listForResourceGroupNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Gets policy assignments of the resource group.
     *
     * @param resourceGroupName Resource group name.
     * @param filter The filter to apply on the operation.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listForResourceGroupAsync(final String resourceGroupName, final String filter, final ListOperationCallback<PolicyAssignmentInner> serviceCallback) throws IllegalArgumentException {
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
        Call<ResponseBody> call = service.listForResourceGroup(resourceGroupName, this.client.subscriptionId(), filter, this.client.apiVersion(), this.client.acceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<PolicyAssignmentInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<PolicyAssignmentInner>> result = listForResourceGroupDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listForResourceGroupNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
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

    private ServiceResponse<PageImpl<PolicyAssignmentInner>> listForResourceGroupDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<PolicyAssignmentInner>, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<PolicyAssignmentInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Delete policy assignment.
     *
     * @param scope Scope.
     * @param policyAssignmentName Policy assignment name.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the PolicyAssignmentInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PolicyAssignmentInner> delete(String scope, String policyAssignmentName) throws CloudException, IOException, IllegalArgumentException {
        if (scope == null) {
            throw new IllegalArgumentException("Parameter scope is required and cannot be null.");
        }
        if (policyAssignmentName == null) {
            throw new IllegalArgumentException("Parameter policyAssignmentName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.delete(scope, policyAssignmentName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage());
        return deleteDelegate(call.execute());
    }

    /**
     * Delete policy assignment.
     *
     * @param scope Scope.
     * @param policyAssignmentName Policy assignment name.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall deleteAsync(String scope, String policyAssignmentName, final ServiceCallback<PolicyAssignmentInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (scope == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter scope is required and cannot be null."));
            return null;
        }
        if (policyAssignmentName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter policyAssignmentName is required and cannot be null."));
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
        Call<ResponseBody> call = service.delete(scope, policyAssignmentName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<PolicyAssignmentInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(deleteDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<PolicyAssignmentInner> deleteDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PolicyAssignmentInner, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PolicyAssignmentInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Create policy assignment.
     *
     * @param scope Scope.
     * @param policyAssignmentName Policy assignment name.
     * @param parameters Policy assignment.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the PolicyAssignmentInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PolicyAssignmentInner> create(String scope, String policyAssignmentName, PolicyAssignmentInner parameters) throws CloudException, IOException, IllegalArgumentException {
        if (scope == null) {
            throw new IllegalArgumentException("Parameter scope is required and cannot be null.");
        }
        if (policyAssignmentName == null) {
            throw new IllegalArgumentException("Parameter policyAssignmentName is required and cannot be null.");
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
        Call<ResponseBody> call = service.create(scope, policyAssignmentName, this.client.subscriptionId(), parameters, this.client.apiVersion(), this.client.acceptLanguage());
        return createDelegate(call.execute());
    }

    /**
     * Create policy assignment.
     *
     * @param scope Scope.
     * @param policyAssignmentName Policy assignment name.
     * @param parameters Policy assignment.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall createAsync(String scope, String policyAssignmentName, PolicyAssignmentInner parameters, final ServiceCallback<PolicyAssignmentInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (scope == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter scope is required and cannot be null."));
            return null;
        }
        if (policyAssignmentName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter policyAssignmentName is required and cannot be null."));
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
        Call<ResponseBody> call = service.create(scope, policyAssignmentName, this.client.subscriptionId(), parameters, this.client.apiVersion(), this.client.acceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<PolicyAssignmentInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(createDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<PolicyAssignmentInner> createDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PolicyAssignmentInner, CloudException>(this.client.restClient().mapperAdapter())
                .register(201, new TypeToken<PolicyAssignmentInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Get single policy assignment.
     *
     * @param scope Scope.
     * @param policyAssignmentName Policy assignment name.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the PolicyAssignmentInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PolicyAssignmentInner> get(String scope, String policyAssignmentName) throws CloudException, IOException, IllegalArgumentException {
        if (scope == null) {
            throw new IllegalArgumentException("Parameter scope is required and cannot be null.");
        }
        if (policyAssignmentName == null) {
            throw new IllegalArgumentException("Parameter policyAssignmentName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.get(scope, policyAssignmentName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage());
        return getDelegate(call.execute());
    }

    /**
     * Get single policy assignment.
     *
     * @param scope Scope.
     * @param policyAssignmentName Policy assignment name.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getAsync(String scope, String policyAssignmentName, final ServiceCallback<PolicyAssignmentInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (scope == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter scope is required and cannot be null."));
            return null;
        }
        if (policyAssignmentName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter policyAssignmentName is required and cannot be null."));
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
        Call<ResponseBody> call = service.get(scope, policyAssignmentName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<PolicyAssignmentInner>(serviceCallback) {
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

    private ServiceResponse<PolicyAssignmentInner> getDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PolicyAssignmentInner, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PolicyAssignmentInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Delete policy assignment.
     *
     * @param policyAssignmentId Policy assignment Id
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the PolicyAssignmentInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PolicyAssignmentInner> deleteById(String policyAssignmentId) throws CloudException, IOException, IllegalArgumentException {
        if (policyAssignmentId == null) {
            throw new IllegalArgumentException("Parameter policyAssignmentId is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.deleteById(policyAssignmentId, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage());
        return deleteByIdDelegate(call.execute());
    }

    /**
     * Delete policy assignment.
     *
     * @param policyAssignmentId Policy assignment Id
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall deleteByIdAsync(String policyAssignmentId, final ServiceCallback<PolicyAssignmentInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (policyAssignmentId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter policyAssignmentId is required and cannot be null."));
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
        Call<ResponseBody> call = service.deleteById(policyAssignmentId, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<PolicyAssignmentInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(deleteByIdDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<PolicyAssignmentInner> deleteByIdDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PolicyAssignmentInner, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PolicyAssignmentInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Create policy assignment by Id.
     *
     * @param policyAssignmentId Policy assignment Id
     * @param parameters Policy assignment.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the PolicyAssignmentInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PolicyAssignmentInner> createById(String policyAssignmentId, PolicyAssignmentInner parameters) throws CloudException, IOException, IllegalArgumentException {
        if (policyAssignmentId == null) {
            throw new IllegalArgumentException("Parameter policyAssignmentId is required and cannot be null.");
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
        Call<ResponseBody> call = service.createById(policyAssignmentId, this.client.subscriptionId(), parameters, this.client.apiVersion(), this.client.acceptLanguage());
        return createByIdDelegate(call.execute());
    }

    /**
     * Create policy assignment by Id.
     *
     * @param policyAssignmentId Policy assignment Id
     * @param parameters Policy assignment.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall createByIdAsync(String policyAssignmentId, PolicyAssignmentInner parameters, final ServiceCallback<PolicyAssignmentInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (policyAssignmentId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter policyAssignmentId is required and cannot be null."));
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
        Call<ResponseBody> call = service.createById(policyAssignmentId, this.client.subscriptionId(), parameters, this.client.apiVersion(), this.client.acceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<PolicyAssignmentInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(createByIdDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<PolicyAssignmentInner> createByIdDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PolicyAssignmentInner, CloudException>(this.client.restClient().mapperAdapter())
                .register(201, new TypeToken<PolicyAssignmentInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Get single policy assignment.
     *
     * @param policyAssignmentId Policy assignment Id
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the PolicyAssignmentInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PolicyAssignmentInner> getById(String policyAssignmentId) throws CloudException, IOException, IllegalArgumentException {
        if (policyAssignmentId == null) {
            throw new IllegalArgumentException("Parameter policyAssignmentId is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.getById(policyAssignmentId, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage());
        return getByIdDelegate(call.execute());
    }

    /**
     * Get single policy assignment.
     *
     * @param policyAssignmentId Policy assignment Id
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getByIdAsync(String policyAssignmentId, final ServiceCallback<PolicyAssignmentInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (policyAssignmentId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter policyAssignmentId is required and cannot be null."));
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
        Call<ResponseBody> call = service.getById(policyAssignmentId, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<PolicyAssignmentInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getByIdDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<PolicyAssignmentInner> getByIdDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PolicyAssignmentInner, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PolicyAssignmentInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Gets policy assignments of the subscription.
     *
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;PolicyAssignmentInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<PolicyAssignmentInner>> list() throws CloudException, IOException, IllegalArgumentException {
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String filter = null;
        Call<ResponseBody> call = service.list(this.client.subscriptionId(), filter, this.client.apiVersion(), this.client.acceptLanguage());
        ServiceResponse<PageImpl<PolicyAssignmentInner>> response = listDelegate(call.execute());
        PagedList<PolicyAssignmentInner> result = new PagedList<PolicyAssignmentInner>(response.getBody()) {
            @Override
            public Page<PolicyAssignmentInner> nextPage(String nextPageLink) throws CloudException, IOException {
                return listNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Gets policy assignments of the subscription.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listAsync(final ListOperationCallback<PolicyAssignmentInner> serviceCallback) throws IllegalArgumentException {
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
        final String filter = null;
        Call<ResponseBody> call = service.list(this.client.subscriptionId(), filter, this.client.apiVersion(), this.client.acceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<PolicyAssignmentInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<PolicyAssignmentInner>> result = listDelegate(response);
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
     * Gets policy assignments of the subscription.
     *
     * @param filter The filter to apply on the operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;PolicyAssignmentInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<PolicyAssignmentInner>> list(final String filter) throws CloudException, IOException, IllegalArgumentException {
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.list(this.client.subscriptionId(), filter, this.client.apiVersion(), this.client.acceptLanguage());
        ServiceResponse<PageImpl<PolicyAssignmentInner>> response = listDelegate(call.execute());
        PagedList<PolicyAssignmentInner> result = new PagedList<PolicyAssignmentInner>(response.getBody()) {
            @Override
            public Page<PolicyAssignmentInner> nextPage(String nextPageLink) throws CloudException, IOException {
                return listNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Gets policy assignments of the subscription.
     *
     * @param filter The filter to apply on the operation.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listAsync(final String filter, final ListOperationCallback<PolicyAssignmentInner> serviceCallback) throws IllegalArgumentException {
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
        Call<ResponseBody> call = service.list(this.client.subscriptionId(), filter, this.client.apiVersion(), this.client.acceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<PolicyAssignmentInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<PolicyAssignmentInner>> result = listDelegate(response);
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

    private ServiceResponse<PageImpl<PolicyAssignmentInner>> listDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<PolicyAssignmentInner>, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<PolicyAssignmentInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Gets policy assignments of the scope.
     *
     * @param scope Scope.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;PolicyAssignmentInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<PolicyAssignmentInner>> listForScope(final String scope) throws CloudException, IOException, IllegalArgumentException {
        if (scope == null) {
            throw new IllegalArgumentException("Parameter scope is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String filter = null;
        Call<ResponseBody> call = service.listForScope(scope, this.client.subscriptionId(), filter, this.client.apiVersion(), this.client.acceptLanguage());
        ServiceResponse<PageImpl<PolicyAssignmentInner>> response = listForScopeDelegate(call.execute());
        PagedList<PolicyAssignmentInner> result = new PagedList<PolicyAssignmentInner>(response.getBody()) {
            @Override
            public Page<PolicyAssignmentInner> nextPage(String nextPageLink) throws CloudException, IOException {
                return listForScopeNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Gets policy assignments of the scope.
     *
     * @param scope Scope.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listForScopeAsync(final String scope, final ListOperationCallback<PolicyAssignmentInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (scope == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter scope is required and cannot be null."));
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
        Call<ResponseBody> call = service.listForScope(scope, this.client.subscriptionId(), filter, this.client.apiVersion(), this.client.acceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<PolicyAssignmentInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<PolicyAssignmentInner>> result = listForScopeDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listForScopeNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
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
     * Gets policy assignments of the scope.
     *
     * @param scope Scope.
     * @param filter The filter to apply on the operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;PolicyAssignmentInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<PolicyAssignmentInner>> listForScope(final String scope, final String filter) throws CloudException, IOException, IllegalArgumentException {
        if (scope == null) {
            throw new IllegalArgumentException("Parameter scope is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.listForScope(scope, this.client.subscriptionId(), filter, this.client.apiVersion(), this.client.acceptLanguage());
        ServiceResponse<PageImpl<PolicyAssignmentInner>> response = listForScopeDelegate(call.execute());
        PagedList<PolicyAssignmentInner> result = new PagedList<PolicyAssignmentInner>(response.getBody()) {
            @Override
            public Page<PolicyAssignmentInner> nextPage(String nextPageLink) throws CloudException, IOException {
                return listForScopeNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Gets policy assignments of the scope.
     *
     * @param scope Scope.
     * @param filter The filter to apply on the operation.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listForScopeAsync(final String scope, final String filter, final ListOperationCallback<PolicyAssignmentInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (scope == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter scope is required and cannot be null."));
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
        Call<ResponseBody> call = service.listForScope(scope, this.client.subscriptionId(), filter, this.client.apiVersion(), this.client.acceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<PolicyAssignmentInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<PolicyAssignmentInner>> result = listForScopeDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listForScopeNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
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

    private ServiceResponse<PageImpl<PolicyAssignmentInner>> listForScopeDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<PolicyAssignmentInner>, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<PolicyAssignmentInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Gets policy assignments of the resource.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;PolicyAssignmentInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl<PolicyAssignmentInner>> listForResourceNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        Call<ResponseBody> call = service.listForResourceNext(nextPageLink, this.client.acceptLanguage());
        return listForResourceNextDelegate(call.execute());
    }

    /**
     * Gets policy assignments of the resource.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listForResourceNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<PolicyAssignmentInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.listForResourceNext(nextPageLink, this.client.acceptLanguage());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<PolicyAssignmentInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<PolicyAssignmentInner>> result = listForResourceNextDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listForResourceNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
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

    private ServiceResponse<PageImpl<PolicyAssignmentInner>> listForResourceNextDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<PolicyAssignmentInner>, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<PolicyAssignmentInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Gets policy assignments of the resource group.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;PolicyAssignmentInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl<PolicyAssignmentInner>> listForResourceGroupNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        Call<ResponseBody> call = service.listForResourceGroupNext(nextPageLink, this.client.acceptLanguage());
        return listForResourceGroupNextDelegate(call.execute());
    }

    /**
     * Gets policy assignments of the resource group.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listForResourceGroupNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<PolicyAssignmentInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.listForResourceGroupNext(nextPageLink, this.client.acceptLanguage());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<PolicyAssignmentInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<PolicyAssignmentInner>> result = listForResourceGroupNextDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listForResourceGroupNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
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

    private ServiceResponse<PageImpl<PolicyAssignmentInner>> listForResourceGroupNextDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<PolicyAssignmentInner>, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<PolicyAssignmentInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Gets policy assignments of the subscription.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;PolicyAssignmentInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl<PolicyAssignmentInner>> listNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.acceptLanguage());
        return listNextDelegate(call.execute());
    }

    /**
     * Gets policy assignments of the subscription.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<PolicyAssignmentInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.acceptLanguage());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<PolicyAssignmentInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<PolicyAssignmentInner>> result = listNextDelegate(response);
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

    private ServiceResponse<PageImpl<PolicyAssignmentInner>> listNextDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<PolicyAssignmentInner>, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<PolicyAssignmentInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Gets policy assignments of the scope.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;PolicyAssignmentInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl<PolicyAssignmentInner>> listForScopeNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        Call<ResponseBody> call = service.listForScopeNext(nextPageLink, this.client.acceptLanguage());
        return listForScopeNextDelegate(call.execute());
    }

    /**
     * Gets policy assignments of the scope.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listForScopeNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<PolicyAssignmentInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.listForScopeNext(nextPageLink, this.client.acceptLanguage());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<PolicyAssignmentInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<PolicyAssignmentInner>> result = listForScopeNextDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listForScopeNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
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

    private ServiceResponse<PageImpl<PolicyAssignmentInner>> listForScopeNextDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<PolicyAssignmentInner>, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<PolicyAssignmentInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

}
