/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources;

import com.google.common.reflect.TypeToken;
import com.microsoft.azure.AzureServiceResponseBuilder;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.ListOperationCallback;
import com.microsoft.azure.management.resources.models.PageImpl;
import com.microsoft.azure.management.resources.models.RoleAssignment;
import com.microsoft.azure.management.resources.models.RoleAssignmentCreateParameters;
import com.microsoft.azure.management.resources.models.RoleAssignmentFilter;
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
import retrofit2.Retrofit;

/**
 * An instance of this class provides access to all the operations defined
 * in RoleAssignmentsOperations.
 */
public final class RoleAssignmentsOperationsImpl implements RoleAssignmentsOperations {
    /** The Retrofit service to perform REST calls. */
    private RoleAssignmentsService service;
    /** The service client containing this operation class. */
    private AuthorizationManagementClient client;

    /**
     * Initializes an instance of RoleAssignmentsOperations.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public RoleAssignmentsOperationsImpl(Retrofit retrofit, AuthorizationManagementClient client) {
        this.service = retrofit.create(RoleAssignmentsService.class);
        this.client = client;
    }

    /**
     * The interface defining all the services for RoleAssignmentsOperations to be
     * used by Retrofit to perform actually REST calls.
     */
    interface RoleAssignmentsService {
        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourcegroups/{resourceGroupName}/providers/{resourceProviderNamespace}/{parentResourcePath}/{resourceType}/{resourceName}providers/Microsoft.Authorization/roleAssignments")
        Call<ResponseBody> listForResource(@Path("resourceGroupName") String resourceGroupName, @Path("resourceProviderNamespace") String resourceProviderNamespace, @Path("parentResourcePath") String parentResourcePath, @Path("resourceType") String resourceType, @Path("resourceName") String resourceName, @Path("subscriptionId") String subscriptionId, @Query("$filter") RoleAssignmentFilter filter, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Authorization/roleAssignments")
        Call<ResponseBody> listForResourceGroup(@Path("resourceGroupName") String resourceGroupName, @Path("subscriptionId") String subscriptionId, @Query("$filter") RoleAssignmentFilter filter, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage);

        @Headers("Content-Type: application/json; charset=utf-8")
        @HTTP(path = "{scope}/providers/Microsoft.Authorization/roleAssignments/{roleAssignmentName}", method = "DELETE", hasBody = true)
        Call<ResponseBody> delete(@Path("scope") String scope, @Path("roleAssignmentName") String roleAssignmentName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage);

        @Headers("Content-Type: application/json; charset=utf-8")
        @PUT("{scope}/providers/Microsoft.Authorization/roleAssignments/{roleAssignmentName}")
        Call<ResponseBody> create(@Path("scope") String scope, @Path("roleAssignmentName") String roleAssignmentName, @Body RoleAssignmentCreateParameters parameters, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("{scope}/providers/Microsoft.Authorization/roleAssignments/{roleAssignmentName}")
        Call<ResponseBody> get(@Path("scope") String scope, @Path("roleAssignmentName") String roleAssignmentName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage);

        @Headers("Content-Type: application/json; charset=utf-8")
        @HTTP(path = "{roleAssignmentId}", method = "DELETE", hasBody = true)
        Call<ResponseBody> deleteById(@Path("roleAssignmentId") String roleAssignmentId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage);

        @Headers("Content-Type: application/json; charset=utf-8")
        @PUT("{roleAssignmentId}")
        Call<ResponseBody> createById(@Path("roleAssignmentId") String roleAssignmentId, @Body RoleAssignmentCreateParameters parameters, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("{roleAssignmentId}")
        Call<ResponseBody> getById(@Path("roleAssignmentId") String roleAssignmentId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/providers/Microsoft.Authorization/roleAssignments")
        Call<ResponseBody> list(@Path("subscriptionId") String subscriptionId, @Query("$filter") RoleAssignmentFilter filter, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("{scope}/providers/Microsoft.Authorization/roleAssignments")
        Call<ResponseBody> listForScope(@Path("scope") String scope, @Query("$filter") RoleAssignmentFilter filter, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET
        Call<ResponseBody> listForResourceNext(@Url String nextPageLink, @Header("accept-language") String acceptLanguage);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET
        Call<ResponseBody> listForResourceGroupNext(@Url String nextPageLink, @Header("accept-language") String acceptLanguage);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET
        Call<ResponseBody> listNext(@Url String nextPageLink, @Header("accept-language") String acceptLanguage);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET
        Call<ResponseBody> listForScopeNext(@Url String nextPageLink, @Header("accept-language") String acceptLanguage);

    }

    /**
     * Gets role assignments of the resource.
     *
     * @param resourceGroupName The name of the resource group.
     * @param resourceProviderNamespace Resource identity.
     * @param parentResourcePath Resource identity.
     * @param resourceType Resource identity.
     * @param resourceName Resource identity.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;RoleAssignment&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<RoleAssignment>> listForResource(final String resourceGroupName, final String resourceProviderNamespace, final String parentResourcePath, final String resourceType, final String resourceName) throws CloudException, IOException, IllegalArgumentException {
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
        if (this.client.getSubscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        final RoleAssignmentFilter filter = null;
        Call<ResponseBody> call = service.listForResource(resourceGroupName, resourceProviderNamespace, parentResourcePath, resourceType, resourceName, this.client.getSubscriptionId(), filter, this.client.getApiVersion(), this.client.getAcceptLanguage());
        ServiceResponse<PageImpl<RoleAssignment>> response = listForResourceDelegate(call.execute());
        PagedList<RoleAssignment> result = new PagedList<RoleAssignment>(response.getBody()) {
            @Override
            public Page<RoleAssignment> nextPage(String nextPageLink) throws CloudException, IOException {
                return listForResourceNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Gets role assignments of the resource.
     *
     * @param resourceGroupName The name of the resource group.
     * @param resourceProviderNamespace Resource identity.
     * @param parentResourcePath Resource identity.
     * @param resourceType Resource identity.
     * @param resourceName Resource identity.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listForResourceAsync(final String resourceGroupName, final String resourceProviderNamespace, final String parentResourcePath, final String resourceType, final String resourceName, final ListOperationCallback<RoleAssignment> serviceCallback) throws IllegalArgumentException {
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
        if (this.client.getSubscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        final RoleAssignmentFilter filter = null;
        Call<ResponseBody> call = service.listForResource(resourceGroupName, resourceProviderNamespace, parentResourcePath, resourceType, resourceName, this.client.getSubscriptionId(), filter, this.client.getApiVersion(), this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<RoleAssignment>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<RoleAssignment>> result = listForResourceDelegate(response);
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
     * Gets role assignments of the resource.
     *
     * @param resourceGroupName The name of the resource group.
     * @param resourceProviderNamespace Resource identity.
     * @param parentResourcePath Resource identity.
     * @param resourceType Resource identity.
     * @param resourceName Resource identity.
     * @param filter The filter to apply on the operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;RoleAssignment&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<RoleAssignment>> listForResource(final String resourceGroupName, final String resourceProviderNamespace, final String parentResourcePath, final String resourceType, final String resourceName, final RoleAssignmentFilter filter) throws CloudException, IOException, IllegalArgumentException {
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
        if (this.client.getSubscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        Validator.validate(filter);
        Call<ResponseBody> call = service.listForResource(resourceGroupName, resourceProviderNamespace, parentResourcePath, resourceType, resourceName, this.client.getSubscriptionId(), filter, this.client.getApiVersion(), this.client.getAcceptLanguage());
        ServiceResponse<PageImpl<RoleAssignment>> response = listForResourceDelegate(call.execute());
        PagedList<RoleAssignment> result = new PagedList<RoleAssignment>(response.getBody()) {
            @Override
            public Page<RoleAssignment> nextPage(String nextPageLink) throws CloudException, IOException {
                return listForResourceNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Gets role assignments of the resource.
     *
     * @param resourceGroupName The name of the resource group.
     * @param resourceProviderNamespace Resource identity.
     * @param parentResourcePath Resource identity.
     * @param resourceType Resource identity.
     * @param resourceName Resource identity.
     * @param filter The filter to apply on the operation.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listForResourceAsync(final String resourceGroupName, final String resourceProviderNamespace, final String parentResourcePath, final String resourceType, final String resourceName, final RoleAssignmentFilter filter, final ListOperationCallback<RoleAssignment> serviceCallback) throws IllegalArgumentException {
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
        if (this.client.getSubscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(filter, serviceCallback);
        Call<ResponseBody> call = service.listForResource(resourceGroupName, resourceProviderNamespace, parentResourcePath, resourceType, resourceName, this.client.getSubscriptionId(), filter, this.client.getApiVersion(), this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<RoleAssignment>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<RoleAssignment>> result = listForResourceDelegate(response);
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

    private ServiceResponse<PageImpl<RoleAssignment>> listForResourceDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<RoleAssignment>, CloudException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<PageImpl<RoleAssignment>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Gets role assignments of the resource group.
     *
     * @param resourceGroupName Resource group name.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;RoleAssignment&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<RoleAssignment>> listForResourceGroup(final String resourceGroupName) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (this.client.getSubscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        final RoleAssignmentFilter filter = null;
        Call<ResponseBody> call = service.listForResourceGroup(resourceGroupName, this.client.getSubscriptionId(), filter, this.client.getApiVersion(), this.client.getAcceptLanguage());
        ServiceResponse<PageImpl<RoleAssignment>> response = listForResourceGroupDelegate(call.execute());
        PagedList<RoleAssignment> result = new PagedList<RoleAssignment>(response.getBody()) {
            @Override
            public Page<RoleAssignment> nextPage(String nextPageLink) throws CloudException, IOException {
                return listForResourceGroupNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Gets role assignments of the resource group.
     *
     * @param resourceGroupName Resource group name.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listForResourceGroupAsync(final String resourceGroupName, final ListOperationCallback<RoleAssignment> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (this.client.getSubscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        final RoleAssignmentFilter filter = null;
        Call<ResponseBody> call = service.listForResourceGroup(resourceGroupName, this.client.getSubscriptionId(), filter, this.client.getApiVersion(), this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<RoleAssignment>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<RoleAssignment>> result = listForResourceGroupDelegate(response);
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
     * Gets role assignments of the resource group.
     *
     * @param resourceGroupName Resource group name.
     * @param filter The filter to apply on the operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;RoleAssignment&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<RoleAssignment>> listForResourceGroup(final String resourceGroupName, final RoleAssignmentFilter filter) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (this.client.getSubscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        Validator.validate(filter);
        Call<ResponseBody> call = service.listForResourceGroup(resourceGroupName, this.client.getSubscriptionId(), filter, this.client.getApiVersion(), this.client.getAcceptLanguage());
        ServiceResponse<PageImpl<RoleAssignment>> response = listForResourceGroupDelegate(call.execute());
        PagedList<RoleAssignment> result = new PagedList<RoleAssignment>(response.getBody()) {
            @Override
            public Page<RoleAssignment> nextPage(String nextPageLink) throws CloudException, IOException {
                return listForResourceGroupNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Gets role assignments of the resource group.
     *
     * @param resourceGroupName Resource group name.
     * @param filter The filter to apply on the operation.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listForResourceGroupAsync(final String resourceGroupName, final RoleAssignmentFilter filter, final ListOperationCallback<RoleAssignment> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (this.client.getSubscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(filter, serviceCallback);
        Call<ResponseBody> call = service.listForResourceGroup(resourceGroupName, this.client.getSubscriptionId(), filter, this.client.getApiVersion(), this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<RoleAssignment>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<RoleAssignment>> result = listForResourceGroupDelegate(response);
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

    private ServiceResponse<PageImpl<RoleAssignment>> listForResourceGroupDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<RoleAssignment>, CloudException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<PageImpl<RoleAssignment>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Delete role assignment.
     *
     * @param scope Scope.
     * @param roleAssignmentName Role assignment name.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the RoleAssignment object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<RoleAssignment> delete(String scope, String roleAssignmentName) throws CloudException, IOException, IllegalArgumentException {
        if (scope == null) {
            throw new IllegalArgumentException("Parameter scope is required and cannot be null.");
        }
        if (roleAssignmentName == null) {
            throw new IllegalArgumentException("Parameter roleAssignmentName is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.delete(scope, roleAssignmentName, this.client.getApiVersion(), this.client.getAcceptLanguage());
        return deleteDelegate(call.execute());
    }

    /**
     * Delete role assignment.
     *
     * @param scope Scope.
     * @param roleAssignmentName Role assignment name.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall deleteAsync(String scope, String roleAssignmentName, final ServiceCallback<RoleAssignment> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (scope == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter scope is required and cannot be null."));
            return null;
        }
        if (roleAssignmentName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter roleAssignmentName is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.delete(scope, roleAssignmentName, this.client.getApiVersion(), this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<RoleAssignment>(serviceCallback) {
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

    private ServiceResponse<RoleAssignment> deleteDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<RoleAssignment, CloudException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<RoleAssignment>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Create role assignment.
     *
     * @param scope Scope.
     * @param roleAssignmentName Role assignment name.
     * @param parameters Role assignment.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the RoleAssignment object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<RoleAssignment> create(String scope, String roleAssignmentName, RoleAssignmentCreateParameters parameters) throws CloudException, IOException, IllegalArgumentException {
        if (scope == null) {
            throw new IllegalArgumentException("Parameter scope is required and cannot be null.");
        }
        if (roleAssignmentName == null) {
            throw new IllegalArgumentException("Parameter roleAssignmentName is required and cannot be null.");
        }
        if (parameters == null) {
            throw new IllegalArgumentException("Parameter parameters is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        Validator.validate(parameters);
        Call<ResponseBody> call = service.create(scope, roleAssignmentName, parameters, this.client.getApiVersion(), this.client.getAcceptLanguage());
        return createDelegate(call.execute());
    }

    /**
     * Create role assignment.
     *
     * @param scope Scope.
     * @param roleAssignmentName Role assignment name.
     * @param parameters Role assignment.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall createAsync(String scope, String roleAssignmentName, RoleAssignmentCreateParameters parameters, final ServiceCallback<RoleAssignment> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (scope == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter scope is required and cannot be null."));
            return null;
        }
        if (roleAssignmentName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter roleAssignmentName is required and cannot be null."));
            return null;
        }
        if (parameters == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter parameters is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(parameters, serviceCallback);
        Call<ResponseBody> call = service.create(scope, roleAssignmentName, parameters, this.client.getApiVersion(), this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<RoleAssignment>(serviceCallback) {
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

    private ServiceResponse<RoleAssignment> createDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<RoleAssignment, CloudException>(this.client.getMapperAdapter())
                .register(201, new TypeToken<RoleAssignment>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Get single role assignment.
     *
     * @param scope Scope.
     * @param roleAssignmentName Role assignment name.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the RoleAssignment object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<RoleAssignment> get(String scope, String roleAssignmentName) throws CloudException, IOException, IllegalArgumentException {
        if (scope == null) {
            throw new IllegalArgumentException("Parameter scope is required and cannot be null.");
        }
        if (roleAssignmentName == null) {
            throw new IllegalArgumentException("Parameter roleAssignmentName is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.get(scope, roleAssignmentName, this.client.getApiVersion(), this.client.getAcceptLanguage());
        return getDelegate(call.execute());
    }

    /**
     * Get single role assignment.
     *
     * @param scope Scope.
     * @param roleAssignmentName Role assignment name.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getAsync(String scope, String roleAssignmentName, final ServiceCallback<RoleAssignment> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (scope == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter scope is required and cannot be null."));
            return null;
        }
        if (roleAssignmentName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter roleAssignmentName is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.get(scope, roleAssignmentName, this.client.getApiVersion(), this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<RoleAssignment>(serviceCallback) {
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

    private ServiceResponse<RoleAssignment> getDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<RoleAssignment, CloudException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<RoleAssignment>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Delete role assignment.
     *
     * @param roleAssignmentId Role assignment Id
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the RoleAssignment object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<RoleAssignment> deleteById(String roleAssignmentId) throws CloudException, IOException, IllegalArgumentException {
        if (roleAssignmentId == null) {
            throw new IllegalArgumentException("Parameter roleAssignmentId is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.deleteById(roleAssignmentId, this.client.getApiVersion(), this.client.getAcceptLanguage());
        return deleteByIdDelegate(call.execute());
    }

    /**
     * Delete role assignment.
     *
     * @param roleAssignmentId Role assignment Id
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall deleteByIdAsync(String roleAssignmentId, final ServiceCallback<RoleAssignment> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (roleAssignmentId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter roleAssignmentId is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.deleteById(roleAssignmentId, this.client.getApiVersion(), this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<RoleAssignment>(serviceCallback) {
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

    private ServiceResponse<RoleAssignment> deleteByIdDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<RoleAssignment, CloudException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<RoleAssignment>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Create role assignment by Id.
     *
     * @param roleAssignmentId Role assignment Id
     * @param parameters Role assignment.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the RoleAssignment object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<RoleAssignment> createById(String roleAssignmentId, RoleAssignmentCreateParameters parameters) throws CloudException, IOException, IllegalArgumentException {
        if (roleAssignmentId == null) {
            throw new IllegalArgumentException("Parameter roleAssignmentId is required and cannot be null.");
        }
        if (parameters == null) {
            throw new IllegalArgumentException("Parameter parameters is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        Validator.validate(parameters);
        Call<ResponseBody> call = service.createById(roleAssignmentId, parameters, this.client.getApiVersion(), this.client.getAcceptLanguage());
        return createByIdDelegate(call.execute());
    }

    /**
     * Create role assignment by Id.
     *
     * @param roleAssignmentId Role assignment Id
     * @param parameters Role assignment.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall createByIdAsync(String roleAssignmentId, RoleAssignmentCreateParameters parameters, final ServiceCallback<RoleAssignment> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (roleAssignmentId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter roleAssignmentId is required and cannot be null."));
            return null;
        }
        if (parameters == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter parameters is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(parameters, serviceCallback);
        Call<ResponseBody> call = service.createById(roleAssignmentId, parameters, this.client.getApiVersion(), this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<RoleAssignment>(serviceCallback) {
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

    private ServiceResponse<RoleAssignment> createByIdDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<RoleAssignment, CloudException>(this.client.getMapperAdapter())
                .register(201, new TypeToken<RoleAssignment>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Get single role assignment.
     *
     * @param roleAssignmentId Role assignment Id
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the RoleAssignment object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<RoleAssignment> getById(String roleAssignmentId) throws CloudException, IOException, IllegalArgumentException {
        if (roleAssignmentId == null) {
            throw new IllegalArgumentException("Parameter roleAssignmentId is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.getById(roleAssignmentId, this.client.getApiVersion(), this.client.getAcceptLanguage());
        return getByIdDelegate(call.execute());
    }

    /**
     * Get single role assignment.
     *
     * @param roleAssignmentId Role assignment Id
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getByIdAsync(String roleAssignmentId, final ServiceCallback<RoleAssignment> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (roleAssignmentId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter roleAssignmentId is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.getById(roleAssignmentId, this.client.getApiVersion(), this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<RoleAssignment>(serviceCallback) {
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

    private ServiceResponse<RoleAssignment> getByIdDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<RoleAssignment, CloudException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<RoleAssignment>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Gets role assignments of the subscription.
     *
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;RoleAssignment&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<RoleAssignment>> list() throws CloudException, IOException, IllegalArgumentException {
        if (this.client.getSubscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        final RoleAssignmentFilter filter = null;
        Call<ResponseBody> call = service.list(this.client.getSubscriptionId(), filter, this.client.getApiVersion(), this.client.getAcceptLanguage());
        ServiceResponse<PageImpl<RoleAssignment>> response = listDelegate(call.execute());
        PagedList<RoleAssignment> result = new PagedList<RoleAssignment>(response.getBody()) {
            @Override
            public Page<RoleAssignment> nextPage(String nextPageLink) throws CloudException, IOException {
                return listNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Gets role assignments of the subscription.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listAsync(final ListOperationCallback<RoleAssignment> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (this.client.getSubscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        final RoleAssignmentFilter filter = null;
        Call<ResponseBody> call = service.list(this.client.getSubscriptionId(), filter, this.client.getApiVersion(), this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<RoleAssignment>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<RoleAssignment>> result = listDelegate(response);
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
     * Gets role assignments of the subscription.
     *
     * @param filter The filter to apply on the operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;RoleAssignment&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<RoleAssignment>> list(final RoleAssignmentFilter filter) throws CloudException, IOException, IllegalArgumentException {
        if (this.client.getSubscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        Validator.validate(filter);
        Call<ResponseBody> call = service.list(this.client.getSubscriptionId(), filter, this.client.getApiVersion(), this.client.getAcceptLanguage());
        ServiceResponse<PageImpl<RoleAssignment>> response = listDelegate(call.execute());
        PagedList<RoleAssignment> result = new PagedList<RoleAssignment>(response.getBody()) {
            @Override
            public Page<RoleAssignment> nextPage(String nextPageLink) throws CloudException, IOException {
                return listNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Gets role assignments of the subscription.
     *
     * @param filter The filter to apply on the operation.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listAsync(final RoleAssignmentFilter filter, final ListOperationCallback<RoleAssignment> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (this.client.getSubscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(filter, serviceCallback);
        Call<ResponseBody> call = service.list(this.client.getSubscriptionId(), filter, this.client.getApiVersion(), this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<RoleAssignment>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<RoleAssignment>> result = listDelegate(response);
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

    private ServiceResponse<PageImpl<RoleAssignment>> listDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<RoleAssignment>, CloudException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<PageImpl<RoleAssignment>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Gets role assignments of the scope.
     *
     * @param scope Scope.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;RoleAssignment&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<RoleAssignment>> listForScope(final String scope) throws CloudException, IOException, IllegalArgumentException {
        if (scope == null) {
            throw new IllegalArgumentException("Parameter scope is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        final RoleAssignmentFilter filter = null;
        Call<ResponseBody> call = service.listForScope(scope, filter, this.client.getApiVersion(), this.client.getAcceptLanguage());
        ServiceResponse<PageImpl<RoleAssignment>> response = listForScopeDelegate(call.execute());
        PagedList<RoleAssignment> result = new PagedList<RoleAssignment>(response.getBody()) {
            @Override
            public Page<RoleAssignment> nextPage(String nextPageLink) throws CloudException, IOException {
                return listForScopeNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Gets role assignments of the scope.
     *
     * @param scope Scope.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listForScopeAsync(final String scope, final ListOperationCallback<RoleAssignment> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (scope == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter scope is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        final RoleAssignmentFilter filter = null;
        Call<ResponseBody> call = service.listForScope(scope, filter, this.client.getApiVersion(), this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<RoleAssignment>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<RoleAssignment>> result = listForScopeDelegate(response);
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
     * Gets role assignments of the scope.
     *
     * @param scope Scope.
     * @param filter The filter to apply on the operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;RoleAssignment&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<RoleAssignment>> listForScope(final String scope, final RoleAssignmentFilter filter) throws CloudException, IOException, IllegalArgumentException {
        if (scope == null) {
            throw new IllegalArgumentException("Parameter scope is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        Validator.validate(filter);
        Call<ResponseBody> call = service.listForScope(scope, filter, this.client.getApiVersion(), this.client.getAcceptLanguage());
        ServiceResponse<PageImpl<RoleAssignment>> response = listForScopeDelegate(call.execute());
        PagedList<RoleAssignment> result = new PagedList<RoleAssignment>(response.getBody()) {
            @Override
            public Page<RoleAssignment> nextPage(String nextPageLink) throws CloudException, IOException {
                return listForScopeNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Gets role assignments of the scope.
     *
     * @param scope Scope.
     * @param filter The filter to apply on the operation.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listForScopeAsync(final String scope, final RoleAssignmentFilter filter, final ListOperationCallback<RoleAssignment> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (scope == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter scope is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(filter, serviceCallback);
        Call<ResponseBody> call = service.listForScope(scope, filter, this.client.getApiVersion(), this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<RoleAssignment>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<RoleAssignment>> result = listForScopeDelegate(response);
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

    private ServiceResponse<PageImpl<RoleAssignment>> listForScopeDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<RoleAssignment>, CloudException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<PageImpl<RoleAssignment>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Gets role assignments of the resource.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;RoleAssignment&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl<RoleAssignment>> listForResourceNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        Call<ResponseBody> call = service.listForResourceNext(nextPageLink, this.client.getAcceptLanguage());
        return listForResourceNextDelegate(call.execute());
    }

    /**
     * Gets role assignments of the resource.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listForResourceNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<RoleAssignment> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.listForResourceNext(nextPageLink, this.client.getAcceptLanguage());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<RoleAssignment>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<RoleAssignment>> result = listForResourceNextDelegate(response);
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

    private ServiceResponse<PageImpl<RoleAssignment>> listForResourceNextDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<RoleAssignment>, CloudException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<PageImpl<RoleAssignment>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Gets role assignments of the resource group.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;RoleAssignment&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl<RoleAssignment>> listForResourceGroupNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        Call<ResponseBody> call = service.listForResourceGroupNext(nextPageLink, this.client.getAcceptLanguage());
        return listForResourceGroupNextDelegate(call.execute());
    }

    /**
     * Gets role assignments of the resource group.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listForResourceGroupNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<RoleAssignment> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.listForResourceGroupNext(nextPageLink, this.client.getAcceptLanguage());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<RoleAssignment>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<RoleAssignment>> result = listForResourceGroupNextDelegate(response);
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

    private ServiceResponse<PageImpl<RoleAssignment>> listForResourceGroupNextDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<RoleAssignment>, CloudException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<PageImpl<RoleAssignment>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Gets role assignments of the subscription.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;RoleAssignment&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl<RoleAssignment>> listNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.getAcceptLanguage());
        return listNextDelegate(call.execute());
    }

    /**
     * Gets role assignments of the subscription.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<RoleAssignment> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.getAcceptLanguage());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<RoleAssignment>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<RoleAssignment>> result = listNextDelegate(response);
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

    private ServiceResponse<PageImpl<RoleAssignment>> listNextDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<RoleAssignment>, CloudException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<PageImpl<RoleAssignment>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Gets role assignments of the scope.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;RoleAssignment&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl<RoleAssignment>> listForScopeNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        Call<ResponseBody> call = service.listForScopeNext(nextPageLink, this.client.getAcceptLanguage());
        return listForScopeNextDelegate(call.execute());
    }

    /**
     * Gets role assignments of the scope.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listForScopeNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<RoleAssignment> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.listForScopeNext(nextPageLink, this.client.getAcceptLanguage());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<RoleAssignment>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<RoleAssignment>> result = listForScopeNextDelegate(response);
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

    private ServiceResponse<PageImpl<RoleAssignment>> listForScopeNextDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<RoleAssignment>, CloudException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<PageImpl<RoleAssignment>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

}
