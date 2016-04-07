/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation.api;

import com.google.common.reflect.TypeToken;
import com.microsoft.azure.AzureServiceResponseBuilder;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.ListOperationCallback;
import com.microsoft.azure.management.resources.models.implementation.api.PageImpl;
import com.microsoft.azure.management.resources.models.implementation.api.RoleDefinitionFilterInner;
import com.microsoft.azure.management.resources.models.implementation.api.RoleDefinitionInner;
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
 * in RoleDefinitions.
 */
public final class RoleDefinitionsInner {
    /** The Retrofit service to perform REST calls. */
    private RoleDefinitionsService service;
    /** The service client containing this operation class. */
    private AuthorizationManagementClientImpl client;

    /**
     * Initializes an instance of RoleDefinitionsInner.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public RoleDefinitionsInner(Retrofit retrofit, AuthorizationManagementClientImpl client) {
        this.service = retrofit.create(RoleDefinitionsService.class);
        this.client = client;
    }

    /**
     * The interface defining all the services for RoleDefinitions to be
     * used by Retrofit to perform actually REST calls.
     */
    interface RoleDefinitionsService {
        @Headers("Content-Type: application/json; charset=utf-8")
        @HTTP(path = "{scope}/providers/Microsoft.Authorization/roleDefinitions/{roleDefinitionId}", method = "DELETE", hasBody = true)
        Call<ResponseBody> delete(@Path("scope") String scope, @Path("roleDefinitionId") String roleDefinitionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("{scope}/providers/Microsoft.Authorization/roleDefinitions/{roleDefinitionId}")
        Call<ResponseBody> get(@Path("scope") String scope, @Path("roleDefinitionId") String roleDefinitionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage);

        @Headers("Content-Type: application/json; charset=utf-8")
        @PUT("{scope}/providers/Microsoft.Authorization/roleDefinitions/{roleDefinitionId}")
        Call<ResponseBody> createOrUpdate(@Path("scope") String scope, @Path("roleDefinitionId") String roleDefinitionId, @Body RoleDefinitionInner roleDefinition, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("{roleDefinitionId}")
        Call<ResponseBody> getById(@Path("roleDefinitionId") String roleDefinitionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("{scope}/providers/Microsoft.Authorization/roleDefinitions")
        Call<ResponseBody> list(@Path("scope") String scope, @Query("$filter") RoleDefinitionFilterInner filter, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET
        Call<ResponseBody> listNext(@Url String nextPageLink, @Header("accept-language") String acceptLanguage);

    }

    /**
     * Deletes the role definition.
     *
     * @param scope Scope
     * @param roleDefinitionId Role definition id.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the RoleDefinitionInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<RoleDefinitionInner> delete(String scope, String roleDefinitionId) throws CloudException, IOException, IllegalArgumentException {
        if (scope == null) {
            throw new IllegalArgumentException("Parameter scope is required and cannot be null.");
        }
        if (roleDefinitionId == null) {
            throw new IllegalArgumentException("Parameter roleDefinitionId is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.delete(scope, roleDefinitionId, this.client.getApiVersion(), this.client.getAcceptLanguage());
        return deleteDelegate(call.execute());
    }

    /**
     * Deletes the role definition.
     *
     * @param scope Scope
     * @param roleDefinitionId Role definition id.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall deleteAsync(String scope, String roleDefinitionId, final ServiceCallback<RoleDefinitionInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (scope == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter scope is required and cannot be null."));
            return null;
        }
        if (roleDefinitionId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter roleDefinitionId is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.delete(scope, roleDefinitionId, this.client.getApiVersion(), this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<RoleDefinitionInner>(serviceCallback) {
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

    private ServiceResponse<RoleDefinitionInner> deleteDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<RoleDefinitionInner, CloudException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<RoleDefinitionInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Get role definition by name (GUID).
     *
     * @param scope Scope
     * @param roleDefinitionId Role definition Id
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the RoleDefinitionInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<RoleDefinitionInner> get(String scope, String roleDefinitionId) throws CloudException, IOException, IllegalArgumentException {
        if (scope == null) {
            throw new IllegalArgumentException("Parameter scope is required and cannot be null.");
        }
        if (roleDefinitionId == null) {
            throw new IllegalArgumentException("Parameter roleDefinitionId is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.get(scope, roleDefinitionId, this.client.getApiVersion(), this.client.getAcceptLanguage());
        return getDelegate(call.execute());
    }

    /**
     * Get role definition by name (GUID).
     *
     * @param scope Scope
     * @param roleDefinitionId Role definition Id
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getAsync(String scope, String roleDefinitionId, final ServiceCallback<RoleDefinitionInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (scope == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter scope is required and cannot be null."));
            return null;
        }
        if (roleDefinitionId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter roleDefinitionId is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.get(scope, roleDefinitionId, this.client.getApiVersion(), this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<RoleDefinitionInner>(serviceCallback) {
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

    private ServiceResponse<RoleDefinitionInner> getDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<RoleDefinitionInner, CloudException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<RoleDefinitionInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Creates or updates a role definition.
     *
     * @param scope Scope
     * @param roleDefinitionId Role definition id.
     * @param roleDefinition Role definition.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the RoleDefinitionInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<RoleDefinitionInner> createOrUpdate(String scope, String roleDefinitionId, RoleDefinitionInner roleDefinition) throws CloudException, IOException, IllegalArgumentException {
        if (scope == null) {
            throw new IllegalArgumentException("Parameter scope is required and cannot be null.");
        }
        if (roleDefinitionId == null) {
            throw new IllegalArgumentException("Parameter roleDefinitionId is required and cannot be null.");
        }
        if (roleDefinition == null) {
            throw new IllegalArgumentException("Parameter roleDefinition is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        Validator.validate(roleDefinition);
        Call<ResponseBody> call = service.createOrUpdate(scope, roleDefinitionId, roleDefinition, this.client.getApiVersion(), this.client.getAcceptLanguage());
        return createOrUpdateDelegate(call.execute());
    }

    /**
     * Creates or updates a role definition.
     *
     * @param scope Scope
     * @param roleDefinitionId Role definition id.
     * @param roleDefinition Role definition.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall createOrUpdateAsync(String scope, String roleDefinitionId, RoleDefinitionInner roleDefinition, final ServiceCallback<RoleDefinitionInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (scope == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter scope is required and cannot be null."));
            return null;
        }
        if (roleDefinitionId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter roleDefinitionId is required and cannot be null."));
            return null;
        }
        if (roleDefinition == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter roleDefinition is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(roleDefinition, serviceCallback);
        Call<ResponseBody> call = service.createOrUpdate(scope, roleDefinitionId, roleDefinition, this.client.getApiVersion(), this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<RoleDefinitionInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(createOrUpdateDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<RoleDefinitionInner> createOrUpdateDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<RoleDefinitionInner, CloudException>(this.client.getMapperAdapter())
                .register(201, new TypeToken<RoleDefinitionInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Get role definition by name (GUID).
     *
     * @param roleDefinitionId Fully qualified role definition Id
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the RoleDefinitionInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<RoleDefinitionInner> getById(String roleDefinitionId) throws CloudException, IOException, IllegalArgumentException {
        if (roleDefinitionId == null) {
            throw new IllegalArgumentException("Parameter roleDefinitionId is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.getById(roleDefinitionId, this.client.getApiVersion(), this.client.getAcceptLanguage());
        return getByIdDelegate(call.execute());
    }

    /**
     * Get role definition by name (GUID).
     *
     * @param roleDefinitionId Fully qualified role definition Id
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getByIdAsync(String roleDefinitionId, final ServiceCallback<RoleDefinitionInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (roleDefinitionId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter roleDefinitionId is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.getById(roleDefinitionId, this.client.getApiVersion(), this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<RoleDefinitionInner>(serviceCallback) {
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

    private ServiceResponse<RoleDefinitionInner> getByIdDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<RoleDefinitionInner, CloudException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<RoleDefinitionInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Get all role definitions that are applicable at scope and above. Use atScopeAndBelow filter to search below the given scope as well.
     *
     * @param scope Scope
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;RoleDefinitionInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<RoleDefinitionInner>> list(final String scope) throws CloudException, IOException, IllegalArgumentException {
        if (scope == null) {
            throw new IllegalArgumentException("Parameter scope is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        final RoleDefinitionFilterInner filter = null;
        Call<ResponseBody> call = service.list(scope, filter, this.client.getApiVersion(), this.client.getAcceptLanguage());
        ServiceResponse<PageImpl<RoleDefinitionInner>> response = listDelegate(call.execute());
        PagedList<RoleDefinitionInner> result = new PagedList<RoleDefinitionInner>(response.getBody()) {
            @Override
            public Page<RoleDefinitionInner> nextPage(String nextPageLink) throws CloudException, IOException {
                return listNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Get all role definitions that are applicable at scope and above. Use atScopeAndBelow filter to search below the given scope as well.
     *
     * @param scope Scope
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listAsync(final String scope, final ListOperationCallback<RoleDefinitionInner> serviceCallback) throws IllegalArgumentException {
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
        final RoleDefinitionFilterInner filter = null;
        Call<ResponseBody> call = service.list(scope, filter, this.client.getApiVersion(), this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<RoleDefinitionInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<RoleDefinitionInner>> result = listDelegate(response);
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
     * Get all role definitions that are applicable at scope and above. Use atScopeAndBelow filter to search below the given scope as well.
     *
     * @param scope Scope
     * @param filter The filter to apply on the operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;RoleDefinitionInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<RoleDefinitionInner>> list(final String scope, final RoleDefinitionFilterInner filter) throws CloudException, IOException, IllegalArgumentException {
        if (scope == null) {
            throw new IllegalArgumentException("Parameter scope is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        Validator.validate(filter);
        Call<ResponseBody> call = service.list(scope, filter, this.client.getApiVersion(), this.client.getAcceptLanguage());
        ServiceResponse<PageImpl<RoleDefinitionInner>> response = listDelegate(call.execute());
        PagedList<RoleDefinitionInner> result = new PagedList<RoleDefinitionInner>(response.getBody()) {
            @Override
            public Page<RoleDefinitionInner> nextPage(String nextPageLink) throws CloudException, IOException {
                return listNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Get all role definitions that are applicable at scope and above. Use atScopeAndBelow filter to search below the given scope as well.
     *
     * @param scope Scope
     * @param filter The filter to apply on the operation.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listAsync(final String scope, final RoleDefinitionFilterInner filter, final ListOperationCallback<RoleDefinitionInner> serviceCallback) throws IllegalArgumentException {
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
        Call<ResponseBody> call = service.list(scope, filter, this.client.getApiVersion(), this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<RoleDefinitionInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<RoleDefinitionInner>> result = listDelegate(response);
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

    private ServiceResponse<PageImpl<RoleDefinitionInner>> listDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<RoleDefinitionInner>, CloudException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<PageImpl<RoleDefinitionInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Get all role definitions that are applicable at scope and above. Use atScopeAndBelow filter to search below the given scope as well.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;RoleDefinitionInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl<RoleDefinitionInner>> listNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.getAcceptLanguage());
        return listNextDelegate(call.execute());
    }

    /**
     * Get all role definitions that are applicable at scope and above. Use atScopeAndBelow filter to search below the given scope as well.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<RoleDefinitionInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.getAcceptLanguage());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<RoleDefinitionInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<RoleDefinitionInner>> result = listNextDelegate(response);
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

    private ServiceResponse<PageImpl<RoleDefinitionInner>> listNextDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<RoleDefinitionInner>, CloudException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<PageImpl<RoleDefinitionInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

}
