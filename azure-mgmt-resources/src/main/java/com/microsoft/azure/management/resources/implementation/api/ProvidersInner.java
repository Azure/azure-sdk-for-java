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
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Url;
import retrofit2.Response;

/**
 * An instance of this class provides access to all the operations defined
 * in Providers.
 */
public final class ProvidersInner {
    /** The Retrofit service to perform REST calls. */
    private ProvidersService service;
    /** The service client containing this operation class. */
    private ResourceManagementClientImpl client;

    /**
     * Initializes an instance of ProvidersInner.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public ProvidersInner(Retrofit retrofit, ResourceManagementClientImpl client) {
        this.service = retrofit.create(ProvidersService.class);
        this.client = client;
    }

    /**
     * The interface defining all the services for Providers to be
     * used by Retrofit to perform actually REST calls.
     */
    interface ProvidersService {
        @Headers("Content-Type: application/json; charset=utf-8")
        @POST("subscriptions/{subscriptionId}/providers/{resourceProviderNamespace}/unregister")
        Call<ResponseBody> unregister(@Path("resourceProviderNamespace") String resourceProviderNamespace, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @POST("subscriptions/{subscriptionId}/providers/{resourceProviderNamespace}/register")
        Call<ResponseBody> register(@Path("resourceProviderNamespace") String resourceProviderNamespace, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/providers")
        Call<ResponseBody> list(@Path("subscriptionId") String subscriptionId, @Query("$top") Integer top, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/providers/{resourceProviderNamespace}")
        Call<ResponseBody> get(@Path("resourceProviderNamespace") String resourceProviderNamespace, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET
        Call<ResponseBody> listNext(@Url String nextPageLink, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

    }

    /**
     * Unregisters provider from a subscription.
     *
     * @param resourceProviderNamespace Namespace of the resource provider.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the ProviderInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<ProviderInner> unregister(String resourceProviderNamespace) throws CloudException, IOException, IllegalArgumentException {
        if (resourceProviderNamespace == null) {
            throw new IllegalArgumentException("Parameter resourceProviderNamespace is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.unregister(resourceProviderNamespace, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return unregisterDelegate(call.execute());
    }

    /**
     * Unregisters provider from a subscription.
     *
     * @param resourceProviderNamespace Namespace of the resource provider.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall unregisterAsync(String resourceProviderNamespace, final ServiceCallback<ProviderInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceProviderNamespace == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceProviderNamespace is required and cannot be null."));
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
        Call<ResponseBody> call = service.unregister(resourceProviderNamespace, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<ProviderInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(unregisterDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<ProviderInner> unregisterDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<ProviderInner, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<ProviderInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Registers provider to be used with a subscription.
     *
     * @param resourceProviderNamespace Namespace of the resource provider.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the ProviderInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<ProviderInner> register(String resourceProviderNamespace) throws CloudException, IOException, IllegalArgumentException {
        if (resourceProviderNamespace == null) {
            throw new IllegalArgumentException("Parameter resourceProviderNamespace is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.register(resourceProviderNamespace, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return registerDelegate(call.execute());
    }

    /**
     * Registers provider to be used with a subscription.
     *
     * @param resourceProviderNamespace Namespace of the resource provider.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall registerAsync(String resourceProviderNamespace, final ServiceCallback<ProviderInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceProviderNamespace == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceProviderNamespace is required and cannot be null."));
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
        Call<ResponseBody> call = service.register(resourceProviderNamespace, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<ProviderInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(registerDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<ProviderInner> registerDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<ProviderInner, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<ProviderInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Gets a list of resource providers.
     *
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;ProviderInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<ProviderInner>> list() throws CloudException, IOException, IllegalArgumentException {
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final Integer top = null;
        Call<ResponseBody> call = service.list(this.client.subscriptionId(), top, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        ServiceResponse<PageImpl<ProviderInner>> response = listDelegate(call.execute());
        PagedList<ProviderInner> result = new PagedList<ProviderInner>(response.getBody()) {
            @Override
            public Page<ProviderInner> nextPage(String nextPageLink) throws CloudException, IOException {
                return listNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Gets a list of resource providers.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listAsync(final ListOperationCallback<ProviderInner> serviceCallback) throws IllegalArgumentException {
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
        final Integer top = null;
        Call<ResponseBody> call = service.list(this.client.subscriptionId(), top, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<ProviderInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<ProviderInner>> result = listDelegate(response);
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
     * Gets a list of resource providers.
     *
     * @param top Query parameters. If null is passed returns all deployments.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;ProviderInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<ProviderInner>> list(final Integer top) throws CloudException, IOException, IllegalArgumentException {
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.list(this.client.subscriptionId(), top, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        ServiceResponse<PageImpl<ProviderInner>> response = listDelegate(call.execute());
        PagedList<ProviderInner> result = new PagedList<ProviderInner>(response.getBody()) {
            @Override
            public Page<ProviderInner> nextPage(String nextPageLink) throws CloudException, IOException {
                return listNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Gets a list of resource providers.
     *
     * @param top Query parameters. If null is passed returns all deployments.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listAsync(final Integer top, final ListOperationCallback<ProviderInner> serviceCallback) throws IllegalArgumentException {
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
        Call<ResponseBody> call = service.list(this.client.subscriptionId(), top, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<ProviderInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<ProviderInner>> result = listDelegate(response);
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

    private ServiceResponse<PageImpl<ProviderInner>> listDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<ProviderInner>, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<PageImpl<ProviderInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Gets a resource provider.
     *
     * @param resourceProviderNamespace Namespace of the resource provider.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the ProviderInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<ProviderInner> get(String resourceProviderNamespace) throws CloudException, IOException, IllegalArgumentException {
        if (resourceProviderNamespace == null) {
            throw new IllegalArgumentException("Parameter resourceProviderNamespace is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.get(resourceProviderNamespace, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getDelegate(call.execute());
    }

    /**
     * Gets a resource provider.
     *
     * @param resourceProviderNamespace Namespace of the resource provider.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getAsync(String resourceProviderNamespace, final ServiceCallback<ProviderInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceProviderNamespace == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceProviderNamespace is required and cannot be null."));
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
        Call<ResponseBody> call = service.get(resourceProviderNamespace, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<ProviderInner>(serviceCallback) {
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

    private ServiceResponse<ProviderInner> getDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<ProviderInner, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<ProviderInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Gets a list of resource providers.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;ProviderInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl<ProviderInner>> listNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        return listNextDelegate(call.execute());
    }

    /**
     * Gets a list of resource providers.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<ProviderInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<ProviderInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<ProviderInner>> result = listNextDelegate(response);
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

    private ServiceResponse<PageImpl<ProviderInner>> listNextDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<ProviderInner>, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<PageImpl<ProviderInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

}
