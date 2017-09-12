/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure;

import com.microsoft.rest.ServiceResponse;
import com.microsoft.rest.ServiceResponseWithHeaders;
import com.microsoft.rest.v2.RestProxy;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Url;
import rx.Observable;
import rx.Single;
import rx.exceptions.Exceptions;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * An instance of this class defines a ServiceClient that handles polling and
 * retrying for long running operations when accessing Azure resources.
 */
public final class AzureClient extends AzureServiceClient {
    /**
     * The interval time between two long running operation polls. Default is 30 seconds.
     */
    private int longRunningOperationRetryTimeout = -1;

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
    public Integer longRunningOperationRetryTimeout() {
        return longRunningOperationRetryTimeout;
    }

    /**
     * Sets the interval time between two long running operation polls. Default is 30 seconds.
     * Set to any negative value to let AzureClient ignore this setting.
     *
     * @param longRunningOperationRetryTimeout the time in seconds. Set to any negative value to let AzureClient ignore this setting.
     */
    public void setLongRunningOperationRetryTimeout(int longRunningOperationRetryTimeout) {
        if (longRunningOperationRetryTimeout < 0) {
            throw new IllegalArgumentException("Invalid timeout for long running operations : " + longRunningOperationRetryTimeout);
        }
        this.longRunningOperationRetryTimeout = longRunningOperationRetryTimeout;
    }

    /**
     * Handles an initial response from a PUT or PATCH operation response by polling
     * the status of the operation until the long running operation terminates.
     *
     * @param observable  the initial observable from the PUT or PATCH operation.
     * @param <T>       the return type of the caller
     * @param resourceType the java.lang.reflect.Type of the resource.
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
     * @param resourceType the java.lang.reflect.Type of the resource.
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
     * Handles an initial response from a PUT or PATCH operation response by polling the status of the operation
     * asynchronously, once the operation finishes emits the final response.
     *
     * @param observable the initial observable from the PUT or PATCH operation.
     * @param resourceType the java.lang.reflect.Type of the resource.
     * @param <T>       the return type of the caller.
     * @return          the observable of which a subscription will lead to a final response.
     */
    public <T> Observable<ServiceResponse<T>> getPutOrPatchResultAsync(Observable<Response<ResponseBody>> observable, final Type resourceType) {
        return this.<T>beginPutOrPatchAsync(observable, resourceType)
                .toObservable()
                .flatMap(new Func1<PollingState<T>, Observable<PollingState<T>>>() {
                    @Override
                    public Observable<PollingState<T>> call(PollingState<T> pollingState) {
                        return pollPutOrPatchAsync(pollingState, resourceType);
                    }
                })
                .last()
                .map(new Func1<PollingState<T>, ServiceResponse<T>>() {
                    @Override
                    public ServiceResponse<T> call(PollingState<T> pollingState) {
                        return new ServiceResponse<>(pollingState.resource(), pollingState.response());
                    }
                });
    }

    /**
     * Given an observable representing a deferred PUT or PATCH action, this method returns {@link Single} object,
     * when subscribed to it, the deferred action will be performed and emits the polling state containing information
     * to track the progress of the action.
     *
     * Note: this method does not implicitly introduce concurrency, by default the deferred action will be executed
     * in scheduler (if any) set for the provided observable.
     *
     * @param observable an observable representing a deferred PUT or PATCH operation.
     * @param resourceType the java.lang.reflect.Type of the resource.
     * @param <T> the type of the resource
     * @return the observable of which a subscription will lead PUT or PATCH action.
     */
    public <T> Single<PollingState<T>> beginPutOrPatchAsync(Observable<Response<ResponseBody>> observable, final Type resourceType) {
        return observable.map(new Func1<Response<ResponseBody>, PollingState<T>>() {
            @Override
            public PollingState<T> call(Response<ResponseBody> response) {
                RuntimeException exception = createExceptionFromResponse(response, 200, 201, 202);
                if (exception != null) {
                    throw  exception;
                }
                try {
                    final PollingState<T> pollingState = PollingState.create(response, longRunningOperationRetryTimeout(), resourceType, restClient().serializerAdapter());
                    pollingState.withPollingUrlFromResponse(response);
                    pollingState.withPollingRetryTimeoutFromResponse(response);
                    pollingState.withPutOrPatchResourceUri(response.raw().request().url().toString());
                    return pollingState;
                } catch (IOException ioException) {
                    throw Exceptions.propagate(ioException);
                }
            }
        }).toSingle();
    }

    /**
     * Given a polling state representing state of a PUT or PATCH operation, this method returns {@link Single} object,
     * when subscribed to it, a single poll will be performed and emits the latest polling state. A poll will be
     * performed only if the current polling state is not in terminal state.
     *
     * Note: this method does not implicitly introduce concurrency, by default the deferred action will be executed
     * in scheduler (if any) set for the provided observable.
     *
     * @param pollingState the current polling state
     * @param <T> the type of the resource
     * @param resourceType the java.lang.reflect.Type of the resource.
     * @return the observable of which a subscription will lead single polling action.
     */
    private <T> Single<PollingState<T>> pollPutOrPatchSingleAsync(final PollingState<T> pollingState, final Type resourceType) {
        pollingState.withResourceType(resourceType);
        pollingState.withSerializerAdapter(restClient().serializerAdapter());
        if (pollingState.isStatusTerminal()) {
            if (pollingState.isStatusSucceeded() && pollingState.resource() == null) {
                return updateStateFromGetResourceOperationAsync(pollingState, pollingState.putOrPatchResourceUri()).toSingle();
            }
            return Single.just(pollingState);
        }
        return putOrPatchPollingDispatcher(pollingState, pollingState.putOrPatchResourceUri())
                .map(new Func1<PollingState<T>, PollingState<T>>() {
                    @Override
                    public PollingState<T> call(PollingState<T> tPollingState) {
                        tPollingState.throwCloudExceptionIfInFailedState();
                        return tPollingState;
                    }
                })
                .flatMap(new Func1<PollingState<T>, Observable<PollingState<T>>>() {
                    @Override
                    public Observable<PollingState<T>> call(PollingState<T> tPollingState) {
                        if (pollingState.isStatusSucceeded() && pollingState.resource() == null) {
                            return updateStateFromGetResourceOperationAsync(pollingState, pollingState.putOrPatchResourceUri());
                        }
                        return Observable.just(tPollingState);
                    }
                })
                .toSingle();
    }

    /**
     * Given a polling state representing state of a PUT or PATCH operation, this method returns {@link Observable} object,
     * when subscribed to it, a series of polling will be performed and emits each polling state to downstream.
     * Polling will completes when the operation finish with success, failure or exception.
     *
     * Note: this method implicitly runs the polling on rx IO scheduler.
     *
     * @param pollingState the current polling state
     * @param <T> the type of the resource
     * @param resourceType the java.lang.reflect.Type of the resource.
     * @return the observable of which a subscription will lead multiple polling action.
     */
    private <T> Observable<PollingState<T>> pollPutOrPatchAsync(final PollingState<T> pollingState, final Type resourceType) {
        pollingState.withResourceType(resourceType);
        pollingState.withSerializerAdapter(restClient().serializerAdapter());
        return Observable.just(true)
                .flatMap(new Func1<Boolean, Observable<PollingState<T>>>() {
                    @Override
                    public Observable<PollingState<T>> call(Boolean aBoolean) {
                        return pollPutOrPatchSingleAsync(pollingState, resourceType).toObservable();
                    }
                }).repeatWhen(new Func1<Observable<? extends Void>, Observable<?>>() {
                    @Override
                    public Observable<?> call(Observable<? extends Void> observable) {
                        return observable.flatMap(new Func1<Void, Observable<Long>>() {
                            @Override
                            public Observable<Long> call(Void aVoid) {
                                return Observable.timer(pollingState.delayInMilliseconds(),
                                        TimeUnit.MILLISECONDS, Schedulers.immediate());
                            }
                        });
                    }
                }).takeUntil(new Func1<PollingState<T>, Boolean>() {
                    @Override
                    public Boolean call(PollingState<T> tPollingState) {
                        return pollingState.isStatusTerminal();
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
     * @param resourceType the java.lang.reflect.Type of the resource.
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
        Observable<ServiceResponse<T>> asyncObservable = getPostOrDeleteResultAsync(observable, resourceType);
        return asyncObservable.toBlocking().last();
    }

    /**
     * Handles an initial response from a POST or DELETE operation response by polling
     * the status of the operation until the long running operation terminates.
     *
     * @param observable  the initial observable from the POST or DELETE operation.
     * @param resourceType the java.lang.reflect.Type of the resource.
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
     * @param resourceType the java.lang.reflect.Type of the resource.
     * @return          the task describing the asynchronous polling.
     */
    public <T> Observable<ServiceResponse<T>> getPostOrDeleteResultAsync(Observable<Response<ResponseBody>> observable, final Type resourceType) {
        return this.<T>beginPostOrDeleteAsync(observable, resourceType)
                .toObservable()
                .flatMap(new Func1<PollingState<T>, Observable<PollingState<T>>>() {
                    @Override
                    public Observable<PollingState<T>> call(PollingState<T> pollingState) {
                        return pollPostOrDeleteAsync(pollingState, resourceType);
                    }
                })
                .last()
                .map(new Func1<PollingState<T>, ServiceResponse<T>>() {
                    @Override
                    public ServiceResponse<T> call(PollingState<T> pollingState) {
                        return new ServiceResponse<>(pollingState.resource(), pollingState.response());
                    }
                });
    }

    /**
     * Given an observable representing a deferred POST or DELETE action, this method returns {@link Single} object,
     * when subscribed to it, the deferred action will be performed and emits the polling state containing information
     * to track the progress of the action.
     *
     * @param observable an observable representing a deferred PUT or PATCH operation.
     * @param resourceType the java.lang.reflect.Type of the resource.
     * @param <T> the type of the resource
     * @return the observable of which a subscription will lead POST or DELETE action.
     */
    public <T> Single<PollingState<T>> beginPostOrDeleteAsync(Observable<Response<ResponseBody>> observable, final Type resourceType) {
        return observable.map(new Func1<Response<ResponseBody>, PollingState<T>>() {
            @Override
            public PollingState<T> call(Response<ResponseBody> response) {
                RuntimeException exception = createExceptionFromResponse(response, 200, 202, 204);
                if (exception != null) {
                    throw  exception;
                }
                try {
                    final PollingState<T> pollingState = PollingState.create(response, longRunningOperationRetryTimeout(), resourceType, restClient().serializerAdapter());
                    pollingState.withPollingUrlFromResponse(response);
                    pollingState.withPollingRetryTimeoutFromResponse(response);
                    return pollingState;
                } catch (IOException ioException) {
                    throw Exceptions.propagate(ioException);
                }
            }
        }).toSingle();
    }

    /**
     * Given a polling state representing state of a POST or DELETE operation, this method returns {@link Single} object,
     * when subscribed to it, a single poll will be performed and emits the latest polling state. A poll will be
     * performed only if the current polling state is not in terminal state.
     *
     * @param pollingState the current polling state
     * @param resourceType the java.lang.reflect.Type of the resource.
     * @param <T> the type of the resource
     * @return the observable of which a subscription will lead single polling action.
     */
    private <T> Single<PollingState<T>> pollPostOrDeleteSingleAsync(final PollingState<T> pollingState, final Type resourceType) {
        pollingState.withResourceType(resourceType);
        pollingState.withSerializerAdapter(restClient().serializerAdapter());
        if (pollingState.isStatusTerminal()) {
            if (pollingState.resourcePending()) {
                return updateStateFromLocationHeaderOnPostOrDeleteAsync(pollingState).toSingle();
            }
            return Single.just(pollingState);
        }
        return postOrDeletePollingDispatcher(pollingState)
                .map(new Func1<PollingState<T>, PollingState<T>>() {
                    @Override
                    public PollingState<T> call(PollingState<T> tPollingState) {
                        tPollingState.throwCloudExceptionIfInFailedState();
                        return tPollingState;
                    }
                })
                .flatMap(new Func1<PollingState<T>, Observable<PollingState<T>>>() {
                    @Override
                    public Observable<PollingState<T>> call(PollingState<T> tPollingState) {
                        if (pollingState.resourcePending()) {
                            return updateStateFromLocationHeaderOnPostOrDeleteAsync(pollingState);
                        }
                        return Observable.just(pollingState);
                    }
                })
                .toSingle();
    }

    /**
     * Given a polling state representing state of a POST or DELETE operation, this method returns {@link Observable} object,
     * when subscribed to it, a series of polling will be performed and emits each polling state to downstream.
     * Polling will completes when the operation finish with success, failure or exception.
     *
     * @param pollingState the current polling state
     * @param resourceType the java.lang.reflect.Type of the resource.
     * @param <T> the type of the resource
     * @return the observable of which a subscription will lead multiple polling action.
     */
    private <T> Observable<PollingState<T>> pollPostOrDeleteAsync(final PollingState<T> pollingState, final Type resourceType) {
        pollingState.withResourceType(resourceType);
        pollingState.withSerializerAdapter(restClient().serializerAdapter());
        return Observable.just(true)
                .flatMap(new Func1<Boolean, Observable<PollingState<T>>>() {
                    @Override
                    public Observable<PollingState<T>> call(Boolean aBoolean) {
                        return pollPostOrDeleteSingleAsync(pollingState, resourceType).toObservable();
                    }
                }).repeatWhen(new Func1<Observable<? extends Void>, Observable<?>>() {
                    @Override
                    public Observable<?> call(Observable<? extends Void> observable) {
                        return observable.flatMap(new Func1<Void, Observable<Long>>() {
                            @Override
                            public Observable<Long> call(Void aVoid) {
                                return Observable.timer(pollingState.delayInMilliseconds(),
                                        TimeUnit.MILLISECONDS, Schedulers.immediate());
                            }
                        });
                    }
                }).takeUntil(new Func1<PollingState<T>, Boolean>() {
                    @Override
                    public Boolean call(PollingState<T> tPollingState) {
                        return pollingState.isStatusTerminal();
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
     * @param resourceType the java.lang.reflect.Type of the resource.
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
     * Given a polling state representing state of a LRO operation, this method returns {@link Single} object,
     * when subscribed to it, a single poll will be performed and emits the latest polling state. A poll will be
     * performed only if the current polling state is not in terminal state.
     *
     * Note: this method does not implicitly introduce concurrency, by default the deferred action will be executed
     * in scheduler (if any) set for the provided observable.
     *
     * @param pollingState the current polling state
     * @param <T> the type of the resource
     * @param resourceType the java.lang.reflect.Type of the resource.
     * @return the observable of which a subscription will lead single polling action.
     */
    public <T> Single<PollingState<T>> pollSingleAsync(final PollingState<T> pollingState, final Type resourceType) {
        if (pollingState.initialHttpMethod().equalsIgnoreCase("PUT")
                || pollingState.initialHttpMethod().equalsIgnoreCase("PATCH")) {
            return this.pollPutOrPatchSingleAsync(pollingState, resourceType);
        }
        if (pollingState.initialHttpMethod().equalsIgnoreCase("POST")
                || pollingState.initialHttpMethod().equalsIgnoreCase("DELETE")) {
            return this.pollPostOrDeleteSingleAsync(pollingState, resourceType);
        }
        throw new IllegalArgumentException("PollingState contains unsupported http method:" + pollingState.initialHttpMethod());
    }

    /**
     * Given a polling state representing state of an LRO operation, this method returns {@link Observable} object,
     * when subscribed to it, a series of polling will be performed and emits each polling state to downstream.
     * Polling will completes when the operation finish with success, failure or exception.
     *
     * @param pollingState the current polling state
     * @param resourceType the java.lang.reflect.Type of the resource.
     * @param <T> the type of the resource
     * @return the observable of which a subscription will lead multiple polling action.
     */
    public <T> Observable<PollingState<T>> pollAsync(final PollingState<T> pollingState, final Type resourceType) {
        if (pollingState.initialHttpMethod().equalsIgnoreCase("PUT")
                || pollingState.initialHttpMethod().equalsIgnoreCase("PATCH")) {
            return this.pollPutOrPatchAsync(pollingState, resourceType);
        }
        if (pollingState.initialHttpMethod().equalsIgnoreCase("POST")
                || pollingState.initialHttpMethod().equalsIgnoreCase("DELETE")) {
            return this.pollPostOrDeleteAsync(pollingState, resourceType);
        }
        throw new IllegalArgumentException("PollingState contains unsupported http method:" + pollingState.initialHttpMethod());
    }

    /**
     * Polls from the location header and updates the polling state with the
     * polling response for a PUT operation.
     *
     * @param pollingState the polling state for the current operation.
     * @param <T> the return type of the caller.
     */
    private <T> Observable<PollingState<T>> updateStateFromLocationHeaderOnPutAsync(final PollingState<T> pollingState) {
        return pollAsync(pollingState.locationHeaderLink(), pollingState.loggingContext())
                .flatMap(new Func1<Response<ResponseBody>, Observable<PollingState<T>>>() {
                    @Override
                    public Observable<PollingState<T>> call(Response<ResponseBody> response) {
                        int statusCode = response.code();
                        if (statusCode == 202) {
                            pollingState.withResponse(response);
                            pollingState.withStatus(AzureAsyncOperation.IN_PROGRESS_STATUS, statusCode);
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
        return pollAsync(pollingState.locationHeaderLink(), pollingState.loggingContext())
                .flatMap(new Func1<Response<ResponseBody>, Observable<PollingState<T>>>() {
                    @Override
                    public Observable<PollingState<T>> call(Response<ResponseBody> response) {
                        int statusCode = response.code();
                        if (statusCode == 202) {
                            pollingState.withResponse(response);
                            pollingState.withStatus(AzureAsyncOperation.IN_PROGRESS_STATUS, statusCode);
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
        return pollAsync(url, pollingState.loggingContext())
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
        return pollAsync(pollingState.azureAsyncOperationHeaderLink(), pollingState.loggingContext())
                .flatMap(new Func1<Response<ResponseBody>, Observable<PollingState<T>>>() {
                    @Override
                    public Observable<PollingState<T>> call(Response<ResponseBody> response) {
                        final AzureAsyncOperation asyncOperation;
                        try {
                            asyncOperation = AzureAsyncOperation.fromResponse(restClient().serializerAdapter(), response);
                        } catch (CloudException exception) {
                            return Observable.error(exception);
                        }
                        pollingState.withStatus(asyncOperation.status());
                        pollingState.withErrorBody(asyncOperation.getError());
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
        return pollAsync(pollingState.azureAsyncOperationHeaderLink(), pollingState.loggingContext())
                .flatMap(new Func1<Response<ResponseBody>, Observable<PollingState<T>>>() {
                    @Override
                    public Observable<PollingState<T>> call(Response<ResponseBody> response) {
                        final AzureAsyncOperation asyncOperation;
                        try {
                            asyncOperation = AzureAsyncOperation.fromResponse(restClient().serializerAdapter(), response);
                        } catch (CloudException exception) {
                            return Observable.error(exception);
                        }
                        pollingState.withStatus(asyncOperation.status());
                        pollingState.withErrorBody(asyncOperation.getError());
                        pollingState.withResponse(response);
                        try {
                            T resource = restClient().serializerAdapter().deserialize(asyncOperation.rawString(), pollingState.resourceType());
                            pollingState.withResource(resource);
                        } catch (IOException e) {
                            // Ignore and let resource be null
                        }
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
        AsyncService service = RestProxy.create(AsyncService.class, rpHttpClient(), serializerAdapter());
        if (loggingContext != null && !loggingContext.endsWith(" (poll)")) {
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
                    exception = new CloudException("Unknown error with status code " + statusCode + " and body " + bodyString, response, null);
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
        if (pollingState.azureAsyncOperationHeaderLink() != null) {
            return updateStateFromAzureAsyncOperationHeaderOnPutAsync(pollingState);
        } else if (pollingState.locationHeaderLink() != null) {
            return updateStateFromLocationHeaderOnPutAsync(pollingState);
        } else {
            return updateStateFromGetResourceOperationAsync(pollingState, url);
        }
    }

    private <T> Observable<PollingState<T>> postOrDeletePollingDispatcher(PollingState<T> pollingState) {
        if (pollingState.azureAsyncOperationHeaderLink() != null) {
            return updateStateFromAzureAsyncOperationHeaderOnPostOrDeleteAsync(pollingState);
        } else if (pollingState.locationHeaderLink() != null) {
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