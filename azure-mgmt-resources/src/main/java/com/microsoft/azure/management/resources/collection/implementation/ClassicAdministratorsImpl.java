/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.collection.implementation;

import com.google.common.reflect.TypeToken;
import com.microsoft.azure.AzureServiceResponseBuilder;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.ListOperationCallback;
import com.microsoft.azure.management.resources.client.AuthorizationManagementClient;
import com.microsoft.azure.management.resources.collection.ClassicAdministrators;
import com.microsoft.azure.management.resources.models.dto.toplevel.ClassicAdministrator;
import com.microsoft.azure.management.resources.models.dto.PageImpl;
import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.rest.ServiceCall;
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
import retrofit2.Retrofit;

/**
 * An instance of this class provides access to all the operations defined
 * in ClassicAdministrators.
 */
public final class ClassicAdministratorsImpl implements ClassicAdministrators {
    /** The Retrofit service to perform REST calls. */
    private ClassicAdministratorsService service;
    /** The service client containing this operation class. */
    private AuthorizationManagementClient client;

    /**
     * Initializes an instance of ClassicAdministrators.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public ClassicAdministratorsImpl(Retrofit retrofit, AuthorizationManagementClient client) {
        this.service = retrofit.create(ClassicAdministratorsService.class);
        this.client = client;
    }

    /**
     * The interface defining all the services for ClassicAdministrators to be
     * used by Retrofit to perform actually REST calls.
     */
    interface ClassicAdministratorsService {
        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/providers/Microsoft.Authorization/classicAdministrators")
        Call<ResponseBody> list(@Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET
        Call<ResponseBody> listNext(@Url String nextPageLink, @Header("accept-language") String acceptLanguage);

    }

    /**
     * Gets a list of classic administrators for the subscription.
     *
     * @param apiVersion the String value
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;ClassicAdministrator&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<ClassicAdministrator>> list(final String apiVersion) throws CloudException, IOException, IllegalArgumentException {
        if (this.client.getSubscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null.");
        }
        if (apiVersion == null) {
            throw new IllegalArgumentException("Parameter apiVersion is required and cannot be null.");
        }
        Call<ResponseBody> call = service.list(this.client.getSubscriptionId(), apiVersion, this.client.getAcceptLanguage());
        ServiceResponse<PageImpl<ClassicAdministrator>> response = listDelegate(call.execute());
        PagedList<ClassicAdministrator> result = new PagedList<ClassicAdministrator>(response.getBody()) {
            @Override
            public Page<ClassicAdministrator> nextPage(String nextPageLink) throws CloudException, IOException {
                return listNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Gets a list of classic administrators for the subscription.
     *
     * @param apiVersion the String value
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listAsync(final String apiVersion, final ListOperationCallback<ClassicAdministrator> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (this.client.getSubscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null."));
            return null;
        }
        if (apiVersion == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter apiVersion is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.list(this.client.getSubscriptionId(), apiVersion, this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<ClassicAdministrator>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<ClassicAdministrator>> result = listDelegate(response);
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

    private ServiceResponse<PageImpl<ClassicAdministrator>> listDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<ClassicAdministrator>, CloudException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<PageImpl<ClassicAdministrator>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Gets a list of classic administrators for the subscription.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;ClassicAdministrator&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl<ClassicAdministrator>> listNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.getAcceptLanguage());
        return listNextDelegate(call.execute());
    }

    /**
     * Gets a list of classic administrators for the subscription.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<ClassicAdministrator> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.getAcceptLanguage());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<ClassicAdministrator>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<ClassicAdministrator>> result = listNextDelegate(response);
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

    private ServiceResponse<PageImpl<ClassicAdministrator>> listNextDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<ClassicAdministrator>, CloudException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<PageImpl<ClassicAdministrator>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

}
