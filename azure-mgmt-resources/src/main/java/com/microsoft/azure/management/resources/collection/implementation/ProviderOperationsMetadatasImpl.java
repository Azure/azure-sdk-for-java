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
import com.microsoft.azure.management.resources.collection.ProviderOperationsMetadatas;
import com.microsoft.azure.management.resources.models.dto.PageImpl;
import com.microsoft.azure.management.resources.models.dto.toplevel.ProviderOperationsMetadata;
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
import retrofit2.Retrofit;

/**
 * An instance of this class provides access to all the operations defined
 * in ProviderOperationsMetadatas.
 */
public final class ProviderOperationsMetadatasImpl implements ProviderOperationsMetadatas {
    /** The Retrofit service to perform REST calls. */
    private ProviderOperationsMetadatasService service;
    /** The service client containing this operation class. */
    private AuthorizationManagementClient client;

    /**
     * Initializes an instance of ProviderOperationsMetadatas.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public ProviderOperationsMetadatasImpl(Retrofit retrofit, AuthorizationManagementClient client) {
        this.service = retrofit.create(ProviderOperationsMetadatasService.class);
        this.client = client;
    }

    /**
     * The interface defining all the services for ProviderOperationsMetadatas to be
     * used by Retrofit to perform actually REST calls.
     */
    interface ProviderOperationsMetadatasService {
        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("providers/Microsoft.Authorization/providerOperations/{resourceProviderNamespace}")
        Call<ResponseBody> get(@Path("resourceProviderNamespace") String resourceProviderNamespace, @Query("api-version") String apiVersion, @Query("$expand") String expand, @Header("accept-language") String acceptLanguage);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("providers/Microsoft.Authorization/providerOperations")
        Call<ResponseBody> list(@Query("api-version") String apiVersion, @Query("$expand") String expand, @Header("accept-language") String acceptLanguage);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET
        Call<ResponseBody> listNext(@Url String nextPageLink, @Header("accept-language") String acceptLanguage);

    }

    /**
     * Gets provider operations metadata.
     *
     * @param resourceProviderNamespace Namespace of the resource provider.
     * @param apiVersion the String value
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the ProviderOperationsMetadata object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<ProviderOperationsMetadata> get(String resourceProviderNamespace, String apiVersion) throws CloudException, IOException, IllegalArgumentException {
        if (resourceProviderNamespace == null) {
            throw new IllegalArgumentException("Parameter resourceProviderNamespace is required and cannot be null.");
        }
        if (apiVersion == null) {
            throw new IllegalArgumentException("Parameter apiVersion is required and cannot be null.");
        }
        final String expand = null;
        Call<ResponseBody> call = service.get(resourceProviderNamespace, apiVersion, expand, this.client.getAcceptLanguage());
        return getDelegate(call.execute());
    }

    /**
     * Gets provider operations metadata.
     *
     * @param resourceProviderNamespace Namespace of the resource provider.
     * @param apiVersion the String value
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getAsync(String resourceProviderNamespace, String apiVersion, final ServiceCallback<ProviderOperationsMetadata> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceProviderNamespace == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceProviderNamespace is required and cannot be null."));
            return null;
        }
        if (apiVersion == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter apiVersion is required and cannot be null."));
            return null;
        }
        final String expand = null;
        Call<ResponseBody> call = service.get(resourceProviderNamespace, apiVersion, expand, this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<ProviderOperationsMetadata>(serviceCallback) {
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

    /**
     * Gets provider operations metadata.
     *
     * @param resourceProviderNamespace Namespace of the resource provider.
     * @param apiVersion the String value
     * @param expand the String value
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the ProviderOperationsMetadata object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<ProviderOperationsMetadata> get(String resourceProviderNamespace, String apiVersion, String expand) throws CloudException, IOException, IllegalArgumentException {
        if (resourceProviderNamespace == null) {
            throw new IllegalArgumentException("Parameter resourceProviderNamespace is required and cannot be null.");
        }
        if (apiVersion == null) {
            throw new IllegalArgumentException("Parameter apiVersion is required and cannot be null.");
        }
        Call<ResponseBody> call = service.get(resourceProviderNamespace, apiVersion, expand, this.client.getAcceptLanguage());
        return getDelegate(call.execute());
    }

    /**
     * Gets provider operations metadata.
     *
     * @param resourceProviderNamespace Namespace of the resource provider.
     * @param apiVersion the String value
     * @param expand the String value
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getAsync(String resourceProviderNamespace, String apiVersion, String expand, final ServiceCallback<ProviderOperationsMetadata> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceProviderNamespace == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceProviderNamespace is required and cannot be null."));
            return null;
        }
        if (apiVersion == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter apiVersion is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.get(resourceProviderNamespace, apiVersion, expand, this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<ProviderOperationsMetadata>(serviceCallback) {
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

    private ServiceResponse<ProviderOperationsMetadata> getDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<ProviderOperationsMetadata, CloudException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<ProviderOperationsMetadata>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Gets provider operations metadata list.
     *
     * @param apiVersion the String value
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;ProviderOperationsMetadata&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<ProviderOperationsMetadata>> list(final String apiVersion) throws CloudException, IOException, IllegalArgumentException {
        if (apiVersion == null) {
            throw new IllegalArgumentException("Parameter apiVersion is required and cannot be null.");
        }
        final String expand = null;
        Call<ResponseBody> call = service.list(apiVersion, expand, this.client.getAcceptLanguage());
        ServiceResponse<PageImpl<ProviderOperationsMetadata>> response = listDelegate(call.execute());
        PagedList<ProviderOperationsMetadata> result = new PagedList<ProviderOperationsMetadata>(response.getBody()) {
            @Override
            public Page<ProviderOperationsMetadata> nextPage(String nextPageLink) throws CloudException, IOException {
                return listNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Gets provider operations metadata list.
     *
     * @param apiVersion the String value
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listAsync(final String apiVersion, final ListOperationCallback<ProviderOperationsMetadata> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (apiVersion == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter apiVersion is required and cannot be null."));
            return null;
        }
        final String expand = null;
        Call<ResponseBody> call = service.list(apiVersion, expand, this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<ProviderOperationsMetadata>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<ProviderOperationsMetadata>> result = listDelegate(response);
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
     * Gets provider operations metadata list.
     *
     * @param apiVersion the String value
     * @param expand the String value
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;ProviderOperationsMetadata&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<ProviderOperationsMetadata>> list(final String apiVersion, final String expand) throws CloudException, IOException, IllegalArgumentException {
        if (apiVersion == null) {
            throw new IllegalArgumentException("Parameter apiVersion is required and cannot be null.");
        }
        Call<ResponseBody> call = service.list(apiVersion, expand, this.client.getAcceptLanguage());
        ServiceResponse<PageImpl<ProviderOperationsMetadata>> response = listDelegate(call.execute());
        PagedList<ProviderOperationsMetadata> result = new PagedList<ProviderOperationsMetadata>(response.getBody()) {
            @Override
            public Page<ProviderOperationsMetadata> nextPage(String nextPageLink) throws CloudException, IOException {
                return listNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Gets provider operations metadata list.
     *
     * @param apiVersion the String value
     * @param expand the String value
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listAsync(final String apiVersion, final String expand, final ListOperationCallback<ProviderOperationsMetadata> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (apiVersion == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter apiVersion is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.list(apiVersion, expand, this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<ProviderOperationsMetadata>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<ProviderOperationsMetadata>> result = listDelegate(response);
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

    private ServiceResponse<PageImpl<ProviderOperationsMetadata>> listDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<ProviderOperationsMetadata>, CloudException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<PageImpl<ProviderOperationsMetadata>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Gets provider operations metadata list.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;ProviderOperationsMetadata&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl<ProviderOperationsMetadata>> listNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.getAcceptLanguage());
        return listNextDelegate(call.execute());
    }

    /**
     * Gets provider operations metadata list.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<ProviderOperationsMetadata> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.getAcceptLanguage());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<ProviderOperationsMetadata>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<ProviderOperationsMetadata>> result = listNextDelegate(response);
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

    private ServiceResponse<PageImpl<ProviderOperationsMetadata>> listNextDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<ProviderOperationsMetadata>, CloudException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<PageImpl<ProviderOperationsMetadata>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

}
