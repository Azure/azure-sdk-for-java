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
 * in Subscriptions.
 */
public final class SubscriptionsInner {
    /** The Retrofit service to perform REST calls. */
    private SubscriptionsService service;
    /** The service client containing this operation class. */
    private SubscriptionClientImpl client;

    /**
     * Initializes an instance of SubscriptionsInner.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public SubscriptionsInner(Retrofit retrofit, SubscriptionClientImpl client) {
        this.service = retrofit.create(SubscriptionsService.class);
        this.client = client;
    }

    /**
     * The interface defining all the services for Subscriptions to be
     * used by Retrofit to perform actually REST calls.
     */
    interface SubscriptionsService {
        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/locations")
        Call<ResponseBody> listLocations(@Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}")
        Call<ResponseBody> get(@Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions")
        Call<ResponseBody> list(@Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET
        Call<ResponseBody> listNext(@Url String nextPageLink, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

    }

    /**
     * Gets a list of the subscription locations.
     *
     * @param subscriptionId Id of the subscription
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;LocationInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<List<LocationInner>> listLocations(String subscriptionId) throws CloudException, IOException, IllegalArgumentException {
        if (subscriptionId == null) {
            throw new IllegalArgumentException("Parameter subscriptionId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.listLocations(subscriptionId, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        ServiceResponse<PageImpl<LocationInner>> response = listLocationsDelegate(call.execute());
        List<LocationInner> result = response.getBody().getItems();
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Gets a list of the subscription locations.
     *
     * @param subscriptionId Id of the subscription
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listLocationsAsync(String subscriptionId, final ServiceCallback<List<LocationInner>> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (subscriptionId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter subscriptionId is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.listLocations(subscriptionId, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<LocationInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<LocationInner>> result = listLocationsDelegate(response);
                    serviceCallback.success(new ServiceResponse<>(result.getBody().getItems(), result.getResponse()));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<PageImpl<LocationInner>> listLocationsDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<LocationInner>, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<PageImpl<LocationInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Gets details about particular subscription.
     *
     * @param subscriptionId Id of the subscription.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the SubscriptionInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<SubscriptionInner> get(String subscriptionId) throws CloudException, IOException, IllegalArgumentException {
        if (subscriptionId == null) {
            throw new IllegalArgumentException("Parameter subscriptionId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.get(subscriptionId, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getDelegate(call.execute());
    }

    /**
     * Gets details about particular subscription.
     *
     * @param subscriptionId Id of the subscription.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getAsync(String subscriptionId, final ServiceCallback<SubscriptionInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (subscriptionId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter subscriptionId is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.get(subscriptionId, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<SubscriptionInner>(serviceCallback) {
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

    private ServiceResponse<SubscriptionInner> getDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<SubscriptionInner, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<SubscriptionInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Gets a list of the subscriptionIds.
     *
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;SubscriptionInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<SubscriptionInner>> list() throws CloudException, IOException, IllegalArgumentException {
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.list(this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        ServiceResponse<PageImpl1<SubscriptionInner>> response = listDelegate(call.execute());
        PagedList<SubscriptionInner> result = new PagedList<SubscriptionInner>(response.getBody()) {
            @Override
            public Page<SubscriptionInner> nextPage(String nextPageLink) throws CloudException, IOException {
                return listNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Gets a list of the subscriptionIds.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listAsync(final ListOperationCallback<SubscriptionInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.list(this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<SubscriptionInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl1<SubscriptionInner>> result = listDelegate(response);
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

    private ServiceResponse<PageImpl1<SubscriptionInner>> listDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl1<SubscriptionInner>, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<PageImpl1<SubscriptionInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Gets a list of the subscriptionIds.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;SubscriptionInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl1<SubscriptionInner>> listNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        return listNextDelegate(call.execute());
    }

    /**
     * Gets a list of the subscriptionIds.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<SubscriptionInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<SubscriptionInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl1<SubscriptionInner>> result = listNextDelegate(response);
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

    private ServiceResponse<PageImpl1<SubscriptionInner>> listNextDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl1<SubscriptionInner>, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<PageImpl1<SubscriptionInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

}
