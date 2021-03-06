/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 *
 * Code generated by Microsoft (R) AutoRest Code Generator.
 */

package com.microsoft.azure.management.features.v2015_12_01.implementation;

import com.google.common.reflect.TypeToken;
import com.microsoft.azure.AzureClient;
import com.microsoft.azure.AzureServiceClient;
import com.microsoft.azure.AzureServiceFuture;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.ListOperationCallback;
import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.ServiceFuture;
import com.microsoft.rest.ServiceResponse;
import java.io.IOException;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Query;
import retrofit2.http.Url;
import retrofit2.Response;
import rx.functions.Func1;
import rx.Observable;

/**
 * Initializes a new instance of the FeatureClientImpl class.
 */
public class FeatureClientImpl extends AzureServiceClient {
    /** The Retrofit service to perform REST calls. */
    private FeatureClientService service;
    /** the {@link AzureClient} used for long running operations. */
    private AzureClient azureClient;

    /**
     * Gets the {@link AzureClient} used for long running operations.
     * @return the azure client;
     */
    public AzureClient getAzureClient() {
        return this.azureClient;
    }

    /** The ID of the target subscription. */
    private String subscriptionId;

    /**
     * Gets The ID of the target subscription.
     *
     * @return the subscriptionId value.
     */
    public String subscriptionId() {
        return this.subscriptionId;
    }

    /**
     * Sets The ID of the target subscription.
     *
     * @param subscriptionId the subscriptionId value.
     * @return the service client itself
     */
    public FeatureClientImpl withSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
        return this;
    }

    /** The API version to use for this operation. */
    private String apiVersion;

    /**
     * Gets The API version to use for this operation.
     *
     * @return the apiVersion value.
     */
    public String apiVersion() {
        return this.apiVersion;
    }

    /** The preferred language for the response. */
    private String acceptLanguage;

    /**
     * Gets The preferred language for the response.
     *
     * @return the acceptLanguage value.
     */
    public String acceptLanguage() {
        return this.acceptLanguage;
    }

    /**
     * Sets The preferred language for the response.
     *
     * @param acceptLanguage the acceptLanguage value.
     * @return the service client itself
     */
    public FeatureClientImpl withAcceptLanguage(String acceptLanguage) {
        this.acceptLanguage = acceptLanguage;
        return this;
    }

    /** The retry timeout in seconds for Long Running Operations. Default value is 30. */
    private int longRunningOperationRetryTimeout;

    /**
     * Gets The retry timeout in seconds for Long Running Operations. Default value is 30.
     *
     * @return the longRunningOperationRetryTimeout value.
     */
    public int longRunningOperationRetryTimeout() {
        return this.longRunningOperationRetryTimeout;
    }

    /**
     * Sets The retry timeout in seconds for Long Running Operations. Default value is 30.
     *
     * @param longRunningOperationRetryTimeout the longRunningOperationRetryTimeout value.
     * @return the service client itself
     */
    public FeatureClientImpl withLongRunningOperationRetryTimeout(int longRunningOperationRetryTimeout) {
        this.longRunningOperationRetryTimeout = longRunningOperationRetryTimeout;
        return this;
    }

    /** Whether a unique x-ms-client-request-id should be generated. When set to true a unique x-ms-client-request-id value is generated and included in each request. Default is true. */
    private boolean generateClientRequestId;

    /**
     * Gets Whether a unique x-ms-client-request-id should be generated. When set to true a unique x-ms-client-request-id value is generated and included in each request. Default is true.
     *
     * @return the generateClientRequestId value.
     */
    public boolean generateClientRequestId() {
        return this.generateClientRequestId;
    }

    /**
     * Sets Whether a unique x-ms-client-request-id should be generated. When set to true a unique x-ms-client-request-id value is generated and included in each request. Default is true.
     *
     * @param generateClientRequestId the generateClientRequestId value.
     * @return the service client itself
     */
    public FeatureClientImpl withGenerateClientRequestId(boolean generateClientRequestId) {
        this.generateClientRequestId = generateClientRequestId;
        return this;
    }

    /**
     * The FeaturesInner object to access its operations.
     */
    private FeaturesInner features;

    /**
     * Gets the FeaturesInner object to access its operations.
     * @return the FeaturesInner object.
     */
    public FeaturesInner features() {
        return this.features;
    }

    /**
     * Initializes an instance of FeatureClient client.
     *
     * @param credentials the management credentials for Azure
     */
    public FeatureClientImpl(ServiceClientCredentials credentials) {
        this("https://management.azure.com", credentials);
    }

    /**
     * Initializes an instance of FeatureClient client.
     *
     * @param baseUrl the base URL of the host
     * @param credentials the management credentials for Azure
     */
    public FeatureClientImpl(String baseUrl, ServiceClientCredentials credentials) {
        super(baseUrl, credentials);
        initialize();
    }

    /**
     * Initializes an instance of FeatureClient client.
     *
     * @param restClient the REST client to connect to Azure.
     */
    public FeatureClientImpl(RestClient restClient) {
        super(restClient);
        initialize();
    }

    protected void initialize() {
        this.apiVersion = "2015-12-01";
        this.acceptLanguage = "en-US";
        this.longRunningOperationRetryTimeout = 30;
        this.generateClientRequestId = true;
        this.features = new FeaturesInner(restClient().retrofit(), this);
        this.azureClient = new AzureClient(this);
        initializeService();
    }

    /**
     * Gets the User-Agent header for the client.
     *
     * @return the user agent string.
     */
    @Override
    public String userAgent() {
        return String.format("%s (%s, %s, auto-generated)", super.userAgent(), "FeatureClient", "2015-12-01");
    }

    private void initializeService() {
        service = restClient().retrofit().create(FeatureClientService.class);
    }

    /**
     * The interface defining all the services for FeatureClient to be
     * used by Retrofit to perform actually REST calls.
     */
    interface FeatureClientService {
        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.management.features.v2015_12_01.FeatureClient listOperations" })
        @GET("providers/Microsoft.Features/operations")
        Observable<Response<ResponseBody>> listOperations(@Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers({ "Content-Type: application/json; charset=utf-8", "x-ms-logging-context: com.microsoft.azure.management.features.v2015_12_01.FeatureClient listOperationsNext" })
        @GET
        Observable<Response<ResponseBody>> listOperationsNext(@Url String nextUrl, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

    }

    /**
     * Lists all of the available Microsoft.Features REST API operations.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the PagedList&lt;OperationInner&gt; object if successful.
     */
    public PagedList<OperationInner> listOperations() {
        ServiceResponse<Page<OperationInner>> response = listOperationsSinglePageAsync().toBlocking().single();
        return new PagedList<OperationInner>(response.body()) {
            @Override
            public Page<OperationInner> nextPage(String nextPageLink) {
                return listOperationsNextSinglePageAsync(nextPageLink).toBlocking().single().body();
            }
        };
    }

    /**
     * Lists all of the available Microsoft.Features REST API operations.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<List<OperationInner>> listOperationsAsync(final ListOperationCallback<OperationInner> serviceCallback) {
        return AzureServiceFuture.fromPageResponse(
            listOperationsSinglePageAsync(),
            new Func1<String, Observable<ServiceResponse<Page<OperationInner>>>>() {
                @Override
                public Observable<ServiceResponse<Page<OperationInner>>> call(String nextPageLink) {
                    return listOperationsNextSinglePageAsync(nextPageLink);
                }
            },
            serviceCallback);
    }

    /**
     * Lists all of the available Microsoft.Features REST API operations.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the PagedList&lt;OperationInner&gt; object
     */
    public Observable<Page<OperationInner>> listOperationsAsync() {
        return listOperationsWithServiceResponseAsync()
            .map(new Func1<ServiceResponse<Page<OperationInner>>, Page<OperationInner>>() {
                @Override
                public Page<OperationInner> call(ServiceResponse<Page<OperationInner>> response) {
                    return response.body();
                }
            });
    }

    /**
     * Lists all of the available Microsoft.Features REST API operations.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the PagedList&lt;OperationInner&gt; object
     */
    public Observable<ServiceResponse<Page<OperationInner>>> listOperationsWithServiceResponseAsync() {
        return listOperationsSinglePageAsync()
            .concatMap(new Func1<ServiceResponse<Page<OperationInner>>, Observable<ServiceResponse<Page<OperationInner>>>>() {
                @Override
                public Observable<ServiceResponse<Page<OperationInner>>> call(ServiceResponse<Page<OperationInner>> page) {
                    String nextPageLink = page.body().nextPageLink();
                    if (nextPageLink == null) {
                        return Observable.just(page);
                    }
                    return Observable.just(page).concatWith(listOperationsNextWithServiceResponseAsync(nextPageLink));
                }
            });
    }

    /**
     * Lists all of the available Microsoft.Features REST API operations.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the PagedList&lt;OperationInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public Observable<ServiceResponse<Page<OperationInner>>> listOperationsSinglePageAsync() {
        if (this.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.apiVersion() is required and cannot be null.");
        }
        return service.listOperations(this.apiVersion(), this.acceptLanguage(), this.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<Page<OperationInner>>>>() {
                @Override
                public Observable<ServiceResponse<Page<OperationInner>>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<PageImpl<OperationInner>> result = listOperationsDelegate(response);
                        return Observable.just(new ServiceResponse<Page<OperationInner>>(result.body(), result.response()));
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<PageImpl<OperationInner>> listOperationsDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return this.restClient().responseBuilderFactory().<PageImpl<OperationInner>, CloudException>newInstance(this.serializerAdapter())
                .register(200, new TypeToken<PageImpl<OperationInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Lists all of the available Microsoft.Features REST API operations.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     * @return the PagedList&lt;OperationInner&gt; object if successful.
     */
    public PagedList<OperationInner> listOperationsNext(final String nextPageLink) {
        ServiceResponse<Page<OperationInner>> response = listOperationsNextSinglePageAsync(nextPageLink).toBlocking().single();
        return new PagedList<OperationInner>(response.body()) {
            @Override
            public Page<OperationInner> nextPage(String nextPageLink) {
                return listOperationsNextSinglePageAsync(nextPageLink).toBlocking().single().body();
            }
        };
    }

    /**
     * Lists all of the available Microsoft.Features REST API operations.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceFuture the ServiceFuture object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the {@link ServiceFuture} object
     */
    public ServiceFuture<List<OperationInner>> listOperationsNextAsync(final String nextPageLink, final ServiceFuture<List<OperationInner>> serviceFuture, final ListOperationCallback<OperationInner> serviceCallback) {
        return AzureServiceFuture.fromPageResponse(
            listOperationsNextSinglePageAsync(nextPageLink),
            new Func1<String, Observable<ServiceResponse<Page<OperationInner>>>>() {
                @Override
                public Observable<ServiceResponse<Page<OperationInner>>> call(String nextPageLink) {
                    return listOperationsNextSinglePageAsync(nextPageLink);
                }
            },
            serviceCallback);
    }

    /**
     * Lists all of the available Microsoft.Features REST API operations.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the PagedList&lt;OperationInner&gt; object
     */
    public Observable<Page<OperationInner>> listOperationsNextAsync(final String nextPageLink) {
        return listOperationsNextWithServiceResponseAsync(nextPageLink)
            .map(new Func1<ServiceResponse<Page<OperationInner>>, Page<OperationInner>>() {
                @Override
                public Page<OperationInner> call(ServiceResponse<Page<OperationInner>> response) {
                    return response.body();
                }
            });
    }

    /**
     * Lists all of the available Microsoft.Features REST API operations.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable to the PagedList&lt;OperationInner&gt; object
     */
    public Observable<ServiceResponse<Page<OperationInner>>> listOperationsNextWithServiceResponseAsync(final String nextPageLink) {
        return listOperationsNextSinglePageAsync(nextPageLink)
            .concatMap(new Func1<ServiceResponse<Page<OperationInner>>, Observable<ServiceResponse<Page<OperationInner>>>>() {
                @Override
                public Observable<ServiceResponse<Page<OperationInner>>> call(ServiceResponse<Page<OperationInner>> page) {
                    String nextPageLink = page.body().nextPageLink();
                    if (nextPageLink == null) {
                        return Observable.just(page);
                    }
                    return Observable.just(page).concatWith(listOperationsNextWithServiceResponseAsync(nextPageLink));
                }
            });
    }

    /**
     * Lists all of the available Microsoft.Features REST API operations.
     *
    ServiceResponse<PageImpl<OperationInner>> * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the PagedList&lt;OperationInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public Observable<ServiceResponse<Page<OperationInner>>> listOperationsNextSinglePageAsync(final String nextPageLink) {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        String nextUrl = String.format("%s", nextPageLink);
        return service.listOperationsNext(nextUrl, this.acceptLanguage(), this.userAgent())
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<Page<OperationInner>>>>() {
                @Override
                public Observable<ServiceResponse<Page<OperationInner>>> call(Response<ResponseBody> response) {
                    try {
                        ServiceResponse<PageImpl<OperationInner>> result = listOperationsNextDelegate(response);
                        return Observable.just(new ServiceResponse<Page<OperationInner>>(result.body(), result.response()));
                    } catch (Throwable t) {
                        return Observable.error(t);
                    }
                }
            });
    }

    private ServiceResponse<PageImpl<OperationInner>> listOperationsNextDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return this.restClient().responseBuilderFactory().<PageImpl<OperationInner>, CloudException>newInstance(this.serializerAdapter())
                .register(200, new TypeToken<PageImpl<OperationInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

}
