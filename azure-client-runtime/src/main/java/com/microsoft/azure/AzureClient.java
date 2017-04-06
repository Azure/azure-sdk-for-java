/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure;

import com.microsoft.rest.ServiceResponse;
import com.microsoft.rest.ServiceResponseWithHeaders;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Url;
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * An instance of this class defines a ServiceClient that handles polling and
 * retrying for long running operations when accessing Azure resources.
 */
public final class AzureClient extends AzureServiceClient {
    private static final String LOGGING_HEADER = "x-ms-logging-context";

    /**
     * The interval time between two long running operation polls. Default is
     * used if null.
     */
    private Integer longRunningOperationRetryTimeout;
    /**
     * The executor for asynchronous requests.
     */
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    /**
     * The user agent from the service client that owns this Azure Client.
     */
    private final String serviceClientUserAgent;

    /**
     * Initializes an instance of this class with customized client metadata.
     *
     * @param serviceClient the caller client that initiates the asynchronous request.
     */
    public AzureClient(AzureServiceClient serviceClient) {
        super(serviceClient.restClient());
        this.serviceClientUserAgent = serviceClient.userAgent();
    }

    /**
     * Gets the interval time between two long running operation polls.
     *
     * @return the time in seconds.
     */
    public Integer getLongRunningOperationRetryTimeout() {
        return longRunningOperationRetryTimeout;
    }

    /**
     * Sets the interval time between two long running operation polls.
     *
     * @param longRunningOperationRetryTimeout the time in seconds.
     */
    public void withLongRunningOperationRetryTimeout(Integer longRunningOperationRetryTimeout) {
        this.longRunningOperationRetryTimeout = longRunningOperationRetryTimeout;
    }

    /**
     * Handles an initial response from a PUT or PATCH operation response by polling
     * the status of the operation until the long running operation terminates.
     *
     * @param observable  the initial observable from the PUT or PATCH operation.
     * @param <T>       the return type of the caller
     * @param resourceType the type of the resource
     * @return          the terminal response for the operation.
     * @throws CloudException REST exception
     * @throws InterruptedException interrupted exception
     * @throws IOException thrown by deserialization
     */
    private <T> ServiceResponse<T> getPutOrPatchResult(Observable<Response<ResponseBody>> observable, Type resourceType) throws CloudException, InterruptedException, IOException {
        Observable<ServiceResponse<T>> asyncObservable = getPutOrPatchResultAsync(observable, resourceType);
        return asyncObservable.toBlocking().last();
    }

    /**
     * Handles an initial response from a PUT or PATCH operation response by polling
     * the status of the operation until the long running operation terminates.
     *
     * @param observable  the initial observable from the PUT or PATCH operation.
     * @param resourceType the type of the resource
     * @param headerType the type of the response header
     * @param <T>       the return type of the caller
     * @param <THeader> the type of the response header
     * @return          the terminal response for the operation.
     * @throws CloudException REST exception
     * @throws InterruptedException interrupted exception
     * @throws IOException thrown by deserialization
     */
    public <T, THeader> ServiceResponseWithHeaders<T, THeader> getPutOrPatchResultWithHeaders(Observable<Response<ResponseBody>> observable, Type resourceType, Class<THeader> headerType) throws CloudException, InterruptedException, IOException {
        ServiceResponse<T> bodyResponse = getPutOrPatchResult(observable, resourceType);
        return new ServiceResponseWithHeaders<>(
            bodyResponse.body(),
            restClient().serializerAdapter().<THeader>deserialize(restClient().serializerAdapter().serialize(bodyResponse.response().headers()), headerType),
            bodyResponse.response()
        );
    }

    /**
     * Handles an initial response from a PUT or PATCH operation response by polling
     * the status of the operation asynchronously, calling the user provided callback
     * when the operation terminates.
     *
     * @param observable  the initial observable from the PUT or PATCH operation.
     * @param <T>       the return type of the caller.
     * @param resourceType the type of the resource.
     * @return          the observable of which a subscription will lead to a final response.
     */
    public <T> Observable<ServiceResponse<T>> getPutOrPatchResultAsync(Observable<Response<ResponseBody>> observable, final Type resourceType) {
        return observable
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<T>>>() {
                @Override
                public Observable<ServiceResponse<T>> call(Response<ResponseBody> response) {
                    RuntimeException exception = createExceptionFromResponse(response, 200, 201, 202);
                    if (exception != null) {
                        return Observable.error(exception);
                    }

                    try {
                        final PollingState<T> pollingState = new PollingState<>(response, getLongRunningOperationRetryTimeout(), resourceType, restClient().serializerAdapter());
                        final String url = response.raw().request().url().toString();

                        // Task runner will take it from here
                        return Observable.just(pollingState)
                            .subscribeOn(Schedulers.io())
                            // Emit a polling task intermittently
                            .repeatWhen(new Func1<Observable<? extends Void>, Observable<?>>() {
                                @Override
                                public Observable<?> call(Observable<? extends Void> observable) {
                                    return observable.delay(pollingState.delayInMilliseconds(), TimeUnit.MILLISECONDS);
                                }
                            })
                            // Conditionally polls if it's not a terminal status
                            .flatMap(new Func1<PollingState<T>, Observable<PollingState<T>>>() {
                                @Override
                                public Observable<PollingState<T>> call(PollingState<T> pollingState) {
                                    for (String terminalStatus : AzureAsyncOperation.terminalStatuses()) {
                                        if (terminalStatus.equalsIgnoreCase(pollingState.status())) {
                                            return Observable.just(pollingState);
                                        }
                                    }
                                    return putOrPatchPollingDispatcher(pollingState, url);
                                }
                            })
                            // The above process continues until this filter passes
                            .filter(new Func1<PollingState<T>, Boolean>() {
                                @Override
                                public Boolean call(PollingState<T> pollingState) {
                                    for (String terminalStatus : AzureAsyncOperation.terminalStatuses()) {
                                        if (terminalStatus.equalsIgnoreCase(pollingState.status())) {
                                            return true;
                                        }
                                    }
                                    return false;
                                }
                            })
                            .first()
                            // Possible extra get to receive the actual resource
                            .flatMap(new Func1<PollingState<T>, Observable<PollingState<T>>>() {
                                @Override
                                public Observable<PollingState<T>> call(PollingState<T> pollingState) {
                                    if (AzureAsyncOperation.SUCCESS_STATUS.equalsIgnoreCase(pollingState.status()) && pollingState.resource() == null) {
                                        return updateStateFromGetResourceOperationAsync(pollingState, url);
                                    }
                                    for (String failedStatus : AzureAsyncOperation.failedStatuses()) {
                                        if (failedStatus.equalsIgnoreCase(pollingState.status())) {
                                            return Observable.error(new CloudException("Async operation failed with provisioning state: " + pollingState.status(), pollingState.response()));
                                        }
                                    }
                                    return Observable.just(pollingState);
                                }
                            })
                            .map(new Func1<PollingState<T>, ServiceResponse<T>>() {
                                @Override
                                public ServiceResponse<T> call(PollingState<T> pollingState) {
                                    return new ServiceResponse<>(pollingState.resource(), pollingState.response());
                                }
                            });
                    } catch (IOException e) {
                        return Observable.error(e);
                    }
                }
            });
    }

    /**
     * Handles an initial response from a PUT or PATCH operation response by polling
     * the status of the operation asynchronously, calling the user provided callback
     * when the operation terminates.
     *
     * @param observable  the initial response from the PUT or PATCH operation.
     * @param <T>       the return type of the caller
     * @param <THeader> the type of the response header
     * @param resourceType the type of the resource.
     * @param headerType the type of the response header
     * @return          the task describing the asynchronous polling.
     */
    public <T, THeader> Observable<ServiceResponseWithHeaders<T, THeader>> getPutOrPatchResultWithHeadersAsync(Observable<Response<ResponseBody>> observable, Type resourceType, final Class<THeader> headerType) {
        Observable<ServiceResponse<T>> bodyResponse = getPutOrPatchResultAsync(observable, resourceType);
        return bodyResponse
            .flatMap(new Func1<ServiceResponse<T>, Observable<ServiceResponseWithHeaders<T, THeader>>>() {
                @Override
                public Observable<ServiceResponseWithHeaders<T, THeader>> call(ServiceResponse<T> serviceResponse) {
                    try {
                        return Observable
                            .just(new ServiceResponseWithHeaders<>(serviceResponse.body(),
                                restClient().serializerAdapter().<THeader>deserialize(restClient().serializerAdapter().serialize(serviceResponse.response().headers()), headerType),
                                serviceResponse.response()));
                    } catch (IOException e) {
                        return Observable.error(e);
                    }
                }
            });
    }

    /**
     * Handles an initial response from a POST or DELETE operation response by polling
     * the status of the operation until the long running operation terminates.
     *
     * @param observable  the initial observable from the POST or DELETE operation.
     * @param <T>       the return type of the caller
     * @param resourceType the type of the resource
     * @return          the terminal response for the operation.
     * @throws CloudException REST exception
     * @throws InterruptedException interrupted exception
     * @throws IOException thrown by deserialization
     */
    private <T> ServiceResponse<T> getPostOrDeleteResult(Observable<Response<ResponseBody>> observable, Type resourceType) throws CloudException, InterruptedException, IOException {
        Observable<ServiceResponse<T>> asyncObservable = getPutOrPatchResultAsync(observable, resourceType);
        return asyncObservable.toBlocking().last();
    }

    /**
     * Handles an initial response from a POST or DELETE operation response by polling
     * the status of the operation until the long running operation terminates.
     *
     * @param observable  the initial observable from the POST or DELETE operation.
     * @param resourceType the type of the resource
     * @param headerType the type of the response header
     * @param <T>       the return type of the caller
     * @param <THeader> the type of the response header
     * @return          the terminal response for the operation.
     * @throws CloudException REST exception
     * @throws InterruptedException interrupted exception
     * @throws IOException thrown by deserialization
     */
    public <T, THeader> ServiceResponseWithHeaders<T, THeader> getPostOrDeleteResultWithHeaders(Observable<Response<ResponseBody>> observable, Type resourceType, Class<THeader> headerType) throws CloudException, InterruptedException, IOException {
        ServiceResponse<T> bodyResponse = getPostOrDeleteResult(observable, resourceType);
        return new ServiceResponseWithHeaders<>(
            bodyResponse.body(),
            restClient().serializerAdapter().<THeader>deserialize(restClient().serializerAdapter().serialize(bodyResponse.response().headers()), headerType),
            bodyResponse.response()
        );
    }

    /**
     * Handles an initial response from a POST or DELETE operation response by polling
     * the status of the operation asynchronously, calling the user provided callback
     * when the operation terminates.
     *
     * @param observable  the initial response from the POST or DELETE operation.
     * @param <T>       the return type of the caller.
     * @param resourceType the type of the resource.
     * @return          the task describing the asynchronous polling.
     */
    public <T> Observable<ServiceResponse<T>> getPostOrDeleteResultAsync(Observable<Response<ResponseBody>> observable, final Type resourceType) {
        return observable
            .flatMap(new Func1<Response<ResponseBody>, Observable<ServiceResponse<T>>>() {
                @Override
                public Observable<ServiceResponse<T>> call(Response<ResponseBody> response) {
                    RuntimeException exception = createExceptionFromResponse(response, 200, 202, 204);
                    if (exception != null) {
                        return Observable.error(exception);
                    }

                    try {
                        final PollingState<T> pollingState = new PollingState<>(response, getLongRunningOperationRetryTimeout(), resourceType, restClient().serializerAdapter());
                        return Observable.just(pollingState)
                            .subscribeOn(Schedulers.io())
                            // Emit a polling task intermittently
                            .repeatWhen(new Func1<Observable<? extends Void>, Observable<?>>() {
                                @Override
                                public Observable<?> call(Observable<? extends Void> observable) {
                                    return observable.delay(pollingState.delayInMilliseconds(), TimeUnit.MILLISECONDS);
                                }
                            })
                            // Conditionally polls if it's not a terminal status
                            .flatMap(new Func1<PollingState<T>, Observable<PollingState<T>>>() {
                                @Override
                                public Observable<PollingState<T>> call(PollingState<T> pollingState) {
                                    for (String terminalStatus : AzureAsyncOperation.terminalStatuses()) {
                                        if (terminalStatus.equalsIgnoreCase(pollingState.status())) {
                                            return Observable.just(pollingState);
                                        }
                                    }
                                    return postOrDeletePollingDispatcher(pollingState);
                                }
                            })
                            // The above process continues until this filter passes
                            .filter(new Func1<PollingState<T>, Boolean>() {
                                @Override
                                public Boolean call(PollingState<T> pollingState) {
                                    for (String terminalStatus : AzureAsyncOperation.terminalStatuses()) {
                                        if (terminalStatus.equalsIgnoreCase(pollingState.status())) {
                                            return true;
                                        }
                                    }
                                    return false;
                                }
                            })
                            .first()
                            .flatMap(new Func1<PollingState<T>, Observable<ServiceResponse<T>>>() {
                                @Override
                                public Observable<ServiceResponse<T>> call(PollingState<T> pollingState) {
                                    for (String failedStatus : AzureAsyncOperation.failedStatuses()) {
                                        if (failedStatus.equalsIgnoreCase(pollingState.status())) {
                                            return Observable.error(new CloudException("Async operation failed with provisioning state: " + pollingState.status(), pollingState.response()));
                                        }
                                    }
                                    return Observable.just(new ServiceResponse<>(pollingState.resource(), pollingState.response()));
                                }
                            });
                    } catch (IOException e) {
                        return Observable.error(e);
                    }
                }
            });
    }

    /**
     * Handles an initial response from a POST or DELETE operation response by polling
     * the status of the operation asynchronously, calling the user provided callback
     * when the operation terminates.
     *
     * @param observable  the initial observable from the POST or DELETE operation.
     * @param <T>       the return type of the caller
     * @param <THeader> the type of the response header
     * @param resourceType the type of the resource.
     * @param headerType the type of the response header
     * @return          the task describing the asynchronous polling.
     */
    public <T, THeader> Observable<ServiceResponseWithHeaders<T, THeader>> getPostOrDeleteResultWithHeadersAsync(Observable<Response<ResponseBody>> observable, Type resourceType, final Class<THeader> headerType) {
        Observable<ServiceResponse<T>> bodyResponse = getPostOrDeleteResultAsync(observable, resourceType);
        return bodyResponse
            .flatMap(new Func1<ServiceResponse<T>, Observable<ServiceResponseWithHeaders<T, THeader>>>() {
                @Override
                public Observable<ServiceResponseWithHeaders<T, THeader>> call(ServiceResponse<T> serviceResponse) {
                    try {
                        return Observable
                            .just(new ServiceResponseWithHeaders<>(serviceResponse.body(),
                                restClient().serializerAdapter().<THeader>deserialize(restClient().serializerAdapter().serialize(serviceResponse.response().headers()), headerType),
                                serviceResponse.response()));
                    } catch (IOException e) {
                        return Observable.error(e);
                    }
                }
            });
    }

    /**
     * Polls from the location header and updates the polling state with the
     * polling response for a PUT operation.
     *
     * @param pollingState the polling state for the current operation.
     * @param <T> the return type of the caller.
     */
    private <T> Observable<PollingState<T>> updateStateFromLocationHeaderOnPutAsync(final PollingState<T> pollingState) {
        return pollAsync(pollingState.locationHeaderLink(), pollingState.response().raw().request().header(LOGGING_HEADER))
            .flatMap(new Func1<Response<ResponseBody>, Observable<PollingState<T>>>() {
                @Override
                public Observable<PollingState<T>> call(Response<ResponseBody> response) {
                    int statusCode = response.code();
                    if (statusCode == 202) {
                        pollingState.withResponse(response);
                        pollingState.withStatus(AzureAsyncOperation.IN_PROGRESS_STATUS);
                    } else if (statusCode == 200 || statusCode == 201) {
                        try {
                            pollingState.updateFromResponseOnPutPatch(response);
                        } catch (CloudException | IOException e) {
                            return Observable.error(e);
                        }
                    }
                    return Observable.just(pollingState);
                }
            });
    }

    /**
     * Polls from the location header and updates the polling state with the
     * polling response for a POST or DELETE operation.
     *
     * @param pollingState the polling state for the current operation.
     * @param <T> the return type of the caller.
     */
    private <T> Observable<PollingState<T>> updateStateFromLocationHeaderOnPostOrDeleteAsync(final PollingState<T> pollingState) {
        return pollAsync(pollingState.locationHeaderLink(), pollingState.response().raw().request().header(LOGGING_HEADER))
            .flatMap(new Func1<Response<ResponseBody>, Observable<PollingState<T>>>() {
                @Override
                public Observable<PollingState<T>> call(Response<ResponseBody> response) {
                    int statusCode = response.code();
                    if (statusCode == 202) {
                        pollingState.withResponse(response);
                        pollingState.withStatus(AzureAsyncOperation.IN_PROGRESS_STATUS);
                    } else if (statusCode == 200 || statusCode == 201 || statusCode == 204) {
                        try {
                            pollingState.updateFromResponseOnDeletePost(response);
                        } catch (IOException e) {
                            return Observable.error(e);
                        }
                    }
                    return Observable.just(pollingState);
                }
            });
    }

    /**
     * Polls from the provided URL and updates the polling state with the
     * polling response.
     *
     * @param pollingState the polling state for the current operation.
     * @param url the url to poll from
     * @param <T> the return type of the caller.
     */
    private <T> Observable<PollingState<T>> updateStateFromGetResourceOperationAsync(final PollingState<T> pollingState, String url) {
        return pollAsync(url, pollingState.response().raw().request().header(LOGGING_HEADER))
            .flatMap(new Func1<Response<ResponseBody>, Observable<PollingState<T>>>() {
                @Override
                public Observable<PollingState<T>> call(Response<ResponseBody> response) {
                    try {
                        pollingState.updateFromResponseOnPutPatch(response);
                        return Observable.just(pollingState);
                    } catch (CloudException | IOException e) {
                        return Observable.error(e);
                    }
                }
            });
    }

    /**
     * Polls from the 'Azure-AsyncOperation' header and updates the polling
     * state with the polling response.
     *
     * @param pollingState the polling state for the current operation.
     * @param <T> the return type of the caller.
     */
    private <T> Observable<PollingState<T>> updateStateFromAzureAsyncOperationHeaderOnPutAsync(final PollingState<T> pollingState) {
        return pollAsync(pollingState.azureAsyncOperationHeaderLink(), pollingState.response().raw().request().header(LOGGING_HEADER))
            .flatMap(new Func1<Response<ResponseBody>, Observable<PollingState<T>>>() {
                @Override
                public Observable<PollingState<T>> call(Response<ResponseBody> response) {
                    AzureAsyncOperation body = null;
                    String bodyString = "";
                    if (response.body() != null) {
                        try {
                            bodyString = response.body().string();
                            body = restClient().serializerAdapter().deserialize(bodyString, AzureAsyncOperation.class);
                        } catch (IOException e) {
                            // null body will be handled later
                        } finally {
                            response.body().close();
                        }
                    }

                    if (body == null || body.status() == null) {
                        CloudException exception = new CloudException("polling response does not contain a valid body: " + bodyString, response);
                        return Observable.error(exception);
                    }

                    pollingState.withStatus(body.status());
                    pollingState.withResponse(response);
                    pollingState.withResource(null);
                    return Observable.just(pollingState);
                }
            });
    }

    /**
     * Polls from the 'Azure-AsyncOperation' header and updates the polling
     * state with the polling response.
     *
     * @param pollingState the polling state for the current operation.
     * @param <T> the return type of the caller.
     */
    private <T> Observable<PollingState<T>> updateStateFromAzureAsyncOperationHeaderOnPostOrDeleteAsync(final PollingState<T> pollingState) {
        return pollAsync(pollingState.azureAsyncOperationHeaderLink(), pollingState.response().raw().request().header(LOGGING_HEADER))
            .flatMap(new Func1<Response<ResponseBody>, Observable<PollingState<T>>>() {
                @Override
                public Observable<PollingState<T>> call(Response<ResponseBody> response) {
                    AzureAsyncOperation body = null;
                    String bodyString = "";
                    if (response.body() != null) {
                        try {
                            bodyString = response.body().string();
                            body = restClient().serializerAdapter().deserialize(bodyString, AzureAsyncOperation.class);
                        } catch (IOException e) {
                            // null body will be handled later
                        } finally {
                            response.body().close();
                        }
                    }

                    if (body == null || body.status() == null) {
                        CloudException exception = new CloudException("polling response does not contain a valid body: " + bodyString, response);
                        return Observable.error(exception);
                    }

                    pollingState.withStatus(body.status());
                    pollingState.withResponse(response);
                    T resource = null;
                    try {
                        resource = restClient().serializerAdapter().deserialize(bodyString, pollingState.resourceType());
                    } catch (IOException e) {
                        // Ignore and let resource be null
                    }
                    pollingState.withResource(resource);
                    return Observable.just(pollingState);
                }
            });
    }

    /**
     * Polls from the URL provided.
     *
     * @param url the URL to poll from.
     * @return the raw response.
     */
    private Observable<Response<ResponseBody>> pollAsync(String url, String loggingContext) {
        URL endpoint;
        try {
            endpoint = new URL(url);
        } catch (MalformedURLException e) {
            return Observable.error(e);
        }
        int port = endpoint.getPort();
        if (port == -1) {
            port = endpoint.getDefaultPort();
        }
        AsyncService service = restClient().retrofit().create(AsyncService.class);
        if (!loggingContext.endsWith(" (poll)")) {
            loggingContext += " (poll)";
        }
        return service.get(endpoint.getFile(), serviceClientUserAgent, loggingContext)
            .flatMap(new Func1<Response<ResponseBody>, Observable<Response<ResponseBody>>>() {
                @Override
                public Observable<Response<ResponseBody>> call(Response<ResponseBody> response) {
                    RuntimeException exception = createExceptionFromResponse(response, 200, 201, 202, 204);
                    if (exception != null) {
                        return Observable.error(exception);
                    } else {
                        return Observable.just(response);
                    }
                }
            });
    }

    private RuntimeException createExceptionFromResponse(Response<ResponseBody> response, Integer... allowedStatusCodes) {
        int statusCode = response.code();
        ResponseBody responseBody;
        if (response.isSuccessful()) {
            responseBody = response.body();
        } else {
            responseBody = response.errorBody();
        }
        if (!Arrays.asList(allowedStatusCodes).contains(statusCode)) {
            CloudException exception;
            try {
                String bodyString = responseBody.string();
                CloudError errorBody = restClient().serializerAdapter().deserialize(bodyString, CloudError.class);
                if (errorBody != null) {
                    exception = new CloudException(errorBody.message(), response, errorBody);
                } else {
                    exception = new CloudException("Unknown error with status code " + statusCode + " and body " + bodyString, response, errorBody);
                }
                return exception;
            } catch (IOException e) {
                /* ignore serialization errors on top of service errors */
                return new RuntimeException("Unknown error with status code " + statusCode, e);
            }
        }
        return null;
    }

    private <T> Observable<PollingState<T>> putOrPatchPollingDispatcher(PollingState<T> pollingState, String url) {
        if (pollingState.azureAsyncOperationHeaderLink() != null
            && !pollingState.azureAsyncOperationHeaderLink().isEmpty()) {
            return updateStateFromAzureAsyncOperationHeaderOnPutAsync(pollingState);
        } else if (pollingState.locationHeaderLink() != null
            && !pollingState.locationHeaderLink().isEmpty()) {
            return updateStateFromLocationHeaderOnPutAsync(pollingState);
        } else {
            return updateStateFromGetResourceOperationAsync(pollingState, url);
        }
    }

    private <T> Observable<PollingState<T>> postOrDeletePollingDispatcher(PollingState<T> pollingState) {
        if (pollingState.azureAsyncOperationHeaderLink() != null
            && !pollingState.azureAsyncOperationHeaderLink().isEmpty()) {
            return updateStateFromAzureAsyncOperationHeaderOnPostOrDeleteAsync(pollingState);
        } else if (pollingState.locationHeaderLink() != null
            && !pollingState.locationHeaderLink().isEmpty()) {
            return updateStateFromLocationHeaderOnPostOrDeleteAsync(pollingState);
        } else {
            CloudException exception = new CloudException("Response does not contain an Azure-AsyncOperation or Location header.", pollingState.response(), pollingState.errorBody());
            return Observable.error(exception);
        }
    }

    /**
     * The Retrofit service used for polling.
     */
    private interface AsyncService {
        @GET
        Observable<Response<ResponseBody>> get(@Url String url, @Header("User-Agent") String userAgent, @Header("x-ms-logging-context") String loggingHeader);
    }
}