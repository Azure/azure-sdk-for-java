/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.arm;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.azure.CloudError;
import com.microsoft.azure.CloudException;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.protocol.SerializerAdapter;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.tuple.Pair;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Url;
import rx.Observable;
import rx.exceptions.Exceptions;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * Type to monitor the progress of delete operation.
 */
public class DeleteOperationMonitor {
    private final DeletePollingState deletePollingInitialState;
    private final RestClient restClient;
    // Delete operation statuses
    //
    static final String SUCCESS_STATUS = "Succeeded";
    static final String IN_PROGRESS_STATUS = "InProgress";
    static final String FAILED_STATUS = "Failed";
    static final String CANCELED_STATUS = "Canceled";
    // Progress tracking header names
    //
    static final String AZURE_ASYNC_OPERATION_HEADER = "Azure-AsyncOperation";
    static final String LOCATION_HEADER = "Location";
    // Retry delay header name
    //
    static final String RETRY_AFTER_HEADER = "Retry-After";

    /**
     * Creates a monitor to track delete progress with the given initial state.
     *
     * @param restClient the client to use for polling
     * @param deletePollingState the delete state to start with
     * @return an observable that emits instance of monitor to track the operation progress
     */
    public static Observable<DeleteOperationMonitor> fromPollingState(final RestClient restClient,
                                                                      final DeletePollingState deletePollingState) {
        return Observable.just(new DeleteOperationMonitor(restClient, deletePollingState));
    }

    /**
     * Creates a monitor to track delete progress given the observable represents initial delete request.
     *
     * @param restClient the client to use for polling
     * @param observable observable representing initial delete request
     * @return an observable that emits instance of monitor to track the operation progress
     */
    public static Observable<DeleteOperationMonitor> fromDeleteResponse(final RestClient restClient, Observable<Response<ResponseBody>> observable) {
        return observable
                // Retrieves delete operation status.
                //
                .map(new Func1<Response<ResponseBody>, Pair<String, Response<ResponseBody>>>() {
                    @Override
                    public Pair<String, Response<ResponseBody>> call(final Response<ResponseBody> response) {
                        RuntimeException exceptionFromResponse = createExceptionIfFailed(response, restClient.serializerAdapter());
                        if (exceptionFromResponse != null) {
                            throw exceptionFromResponse;
                        }
                        try {
                            String operationStatus = tryExtractProvisioningState(response, restClient.serializerAdapter());
                            if (operationStatus != null) {
                                return Pair.of(operationStatus, response);
                            }
                        } catch (IOException ioException) {
                            throw Exceptions.propagate(ioException);
                        }
                        return Pair.of(responseCodeToOperationStatus(response.code()), response);
                    }
                })
                // Map to DeleteOperationMonitor containing delete operation state
                //
                .map(new Func1<Pair<String, Response<ResponseBody>>, DeleteOperationMonitor>() {
                    @Override
                    public DeleteOperationMonitor call(Pair<String, Response<ResponseBody>> pair) {
                        final String operationStatus = pair.getLeft();
                        final Response<ResponseBody> response = pair.getRight();
                        if (!isTerminalStatus(operationStatus) && !hasPollingUrl(response)) {
                            throw new CloudException("Response does not contain an Azure-AsyncOperation or Location header.", response);
                        }
                        // Prepare object to hold the initial state of delete operation.
                        //
                        DeletePollingState deletePollingState = new DeletePollingState();
                        deletePollingState.withStatus(operationStatus);
                        setPollingUrl(response, deletePollingState);
                        setDelayBeforeNextPoll(response, deletePollingState);
                        // Return object to track the progress of delete operation.
                        //
                        return new DeleteOperationMonitor(restClient, deletePollingState);
                    }
                });
    }

    /**
     * Creates an Observable, when subscribed emits events intermittently indicating the current state of
     * the delete operation.
     *
     * @return the observable
     */
    public Observable<DeletePollingState> toObservable() {
        final DeletePollingState mutableState = new DeletePollingState();
        mutableState.setFrom(deletePollingInitialState);

        final boolean[] doPoll = new boolean[1];
        doPoll[0] = false;
        return Observable.just(true)
                .flatMap(new Func1<Boolean, Observable<DeletePollingState>>() {
                    @Override
                    public Observable<DeletePollingState> call(Boolean aBoolean) {
                        return pollAsync(mutableState, doPoll[0]);
                    }
                })
                .doOnNext(new Action1<DeletePollingState>() {
                    @Override
                    public void call(DeletePollingState currentState) {
                        mutableState.setFrom(currentState);
                        doPoll[0] = true;
                    }
                })
                .repeatWhen(new Func1<Observable<? extends Void>, Observable<?>>() {
                    public Observable<?> call(Observable<? extends Void> observable) {
                        return observable
                                .flatMap(new Func1<Void, Observable<Long>>() {
                                    public Observable<Long> call(Void aVoid) {
                                        return Observable.timer(mutableState.delayBeforeNextPoll(),
                                                TimeUnit.MILLISECONDS,
                                                Schedulers.io());
                                    }
                                });
                    }
                })
                .takeUntil(new Func1<DeletePollingState, Boolean>() {
                    @Override
                    public Boolean call(DeletePollingState state) {
                        return isTerminalStatus(state.status());
                    }
                });
    }

    /**
     * Creates DeleteOperationMonitor.
     *
     * @param restClient the rest client to poll the delete progress
     * @param deletePollingState the current state
     */
    private DeleteOperationMonitor(RestClient restClient, DeletePollingState deletePollingState) {
        this.restClient = restClient;
        this.deletePollingInitialState = deletePollingState;
    }

    /**
     * Polls the current state of the delete operation and returns an observable that emit once with state.
     *
     * @param mutableState state from the previous poll, the same object will be updated with the current polling state
     *                     and emitted by the returned observable.
     * @param doPoll if false then the previous poll state will be returned as it is, true to poll server for current state
     * @return the observable
     */
    private Observable<DeletePollingState> pollAsync(DeletePollingState mutableState, final boolean doPoll) {
        if (!doPoll) {
            return Observable.just(mutableState);
        }
        if (!isNullOrEmpty(mutableState.azureAsyncOperationPollUrl())) {
            return pollUsingAzureOperationUrlAsync(mutableState);
        }
        if (!isNullOrEmpty(mutableState.locationHeaderPollUrl())) {
            return pollUsingLocationUrlAsync(mutableState);
        }
        mutableState.withStatus(FAILED_STATUS);
        return Observable.error(new Exception("State does not contain an Azure-AsyncOperation or Location poll url."));
    }

    /**
     * Polls the current state of the delete operation using azure async-operation header url and returns an observable
     * that emit once with state.
     *
     * @param mutableState state from previous poll, the same object will be updated with the current polling state
     *                     and emitted by the returned observable.
     * @return the observable
     */
    private Observable<DeletePollingState> pollUsingAzureOperationUrlAsync(final DeletePollingState mutableState) {
        final String pollUrl = mutableState.azureAsyncOperationPollUrl();
        return pollAsync(pollUrl)
                .map(new Func1<Response<ResponseBody>, Pair<String, Response<ResponseBody>>>() {
                    @Override
                    public Pair<String, Response<ResponseBody>> call(Response<ResponseBody> response) {
                        if (response.body() == null) {
                            throw new CloudException("Polling response contains null body.", response);
                        }
                        String pollingResponseStr = null;
                        PollingResponse pollingResponse = null;
                        try {
                            pollingResponseStr = response.body().string();
                            pollingResponse = restClient.serializerAdapter().deserialize(pollingResponseStr, PollingResponse.class);
                        } catch (IOException e) {
                        } finally {
                            response.body().close();
                        }
                        if (pollingResponse != null) {
                            if (pollingResponse.status != null) {
                                return Pair.of(pollingResponse.status, response);
                            } else if (pollingResponse.error != null) {
                                throw new CloudException("Polling returned error.", response, pollingResponse.error);
                            }
                        }
                        throw new CloudException(String.format("Polling response does not contain a valid body: %s", pollingResponseStr), response);
                    }
                })
                .doOnNext(new Action1<Pair<String, Response<ResponseBody>>>() {
                    @Override
                    public void call(Pair<String, Response<ResponseBody>> pair) {
                        String operationStatus = pair.getLeft();
                        Response<ResponseBody> response = pair.getRight();
                        // Update state object with the current state of delete operation.
                        //
                        mutableState.withStatus(operationStatus);
                        setPollingUrl(response, mutableState);
                        setDelayBeforeNextPoll(response, mutableState);
                    }
                })
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mutableState.withStatus(FAILED_STATUS);
                    }
                })
                .map(new Func1<Pair<?, ?>, DeletePollingState>() {
                    @Override
                    public DeletePollingState call(Pair<?, ?> pair) {
                        return mutableState;
                    }
                });
    }

    /**
     * Polls the current state of the delete operation using location header url and returns an observable that
     * emit once with state.
     *
     * @param mutableState state from previous poll, the same object will be updated with the current polling state
     *                     and emitted by the returned observable.
     * @return the observable
     */
    private Observable<DeletePollingState> pollUsingLocationUrlAsync(final DeletePollingState mutableState) {
        final String pollUrl = mutableState.locationHeaderPollUrl();
        return pollAsync(pollUrl)
                .map(new Func1<Response<ResponseBody>, Pair<String, Response<ResponseBody>>>() {
                    @Override
                    public Pair<String, Response<ResponseBody>> call(Response<ResponseBody> response) {
                        return Pair.of(responseCodeToOperationStatus(response.code()), response);
                    }
                })
                .doOnNext(new Action1<Pair<String, Response<ResponseBody>>>() {
                    @Override
                    public void call(Pair<String, Response<ResponseBody>> pair) {
                        String resourceStatus = pair.getLeft();
                        Response<ResponseBody> response = pair.getRight();
                        // Update state object with the current state of delete operation.
                        //
                        mutableState.withStatus(resourceStatus);
                        setPollingUrl(response, mutableState);
                        setDelayBeforeNextPoll(response, mutableState);
                    }
                })
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mutableState.withStatus(FAILED_STATUS);
                    }
                })
                .map(new Func1<Pair<?, ?>, DeletePollingState>() {
                    @Override
                    public DeletePollingState call(Pair<?, ?> pair) {
                        return mutableState;
                    }
                });
    }

    /**
     * Polls using given polling url and returns an observable that emit once with the polling response.
     *
     * @param pollUrl the url to poll
     * @return the observable
     */
    private Observable<Response<ResponseBody>> pollAsync(String pollUrl) {
        URL endpoint;
        try {
            endpoint = new URL(pollUrl);
        } catch (MalformedURLException e) {
            return Observable.error(e);
        }
        PollingService service = restClient.retrofit().create(PollingService.class);
        return service.get(endpoint.getFile())
                .map(new Func1<Response<ResponseBody>, Response<ResponseBody>>() {
                    @Override
                    public Response<ResponseBody> call(Response<ResponseBody> response) {
                        RuntimeException exception = createExceptionIfFailed(response, restClient.serializerAdapter());
                        if (exception != null) {
                            throw exception;
                        }
                        return response;
                    }
                });
    }

    /**
     * @param response the response to an initial delete or progress tracking request
     * @return true if the given response contains standard headers with value as url to
     * query the progress of the delete operation.
     */
    private static boolean hasPollingUrl(Response<ResponseBody> response) {
        String asyncHeader = response.headers().get(AZURE_ASYNC_OPERATION_HEADER);
        if (!isNullOrEmpty(asyncHeader)) {
            return true;
        }
        String locationHeader = response.headers().get(LOCATION_HEADER);
        if (!isNullOrEmpty(locationHeader)) {
            return true;
        }
        return false;
    }

    /**
     * Given the response object extract url to query the delete operation progress and set it
     * in the given delete state object.
     *
     * @param response the response object
     * @param mutableState the updated delete state object
     */
    private static void setPollingUrl(Response<ResponseBody> response, DeletePollingState mutableState) {
        String asyncHeader = response.headers().get(AZURE_ASYNC_OPERATION_HEADER);
        String locationHeader = response.headers().get(LOCATION_HEADER);
        if (!isNullOrEmpty(asyncHeader)) {
            mutableState.withAzureAsyncOperationPollUrl(asyncHeader);
        }
        if (!isNullOrEmpty(locationHeader)) {
            mutableState.withLocationHeaderPollUrl(locationHeader);
        }
    }

    /**
     * Given the response object extract time to wait before next poll and set it in the given delete state object.
     *
     * @param response the response object
     * @param mutableState the updated delete state object
     */
    private static void setDelayBeforeNextPoll(Response<ResponseBody> response, DeletePollingState mutableState) {
        String retryHeader = response.headers().get(RETRY_AFTER_HEADER);
        if (!isNullOrEmpty(retryHeader)) {
            mutableState.withDelayBeforeNextPoll(Integer.parseInt(retryHeader) * 1000);
        } else {
            mutableState.withDelayBeforeNextPoll(30 * 1000);
        }
    }

    /**
     * Given a response check it indicates failure (of a delete operation) if so create an exception
     * object from the response.
     *
     * @param response the response to initial delete or progress request
     * @param serializerAdapter the adapter to deserialize error details from the response
     * @return the exception if the response indicate failure, null otherwise
     */
    private static RuntimeException createExceptionIfFailed(Response<ResponseBody> response,
                                                            SerializerAdapter<?> serializerAdapter) {
        final int statusCode = response.code();
        if (isAllowedStatusCode(statusCode)) {
            return null;
        }
        try {
            final String bodyString = response.isSuccessful() ? response.body().toString() : response.errorBody().toString();
            CloudError errorBody = serializerAdapter.deserialize(bodyString, CloudError.class);
            if (errorBody != null) {
                return new CloudException(errorBody.message(), response, errorBody);
            }
            return new CloudException(String.format("Unknown error with status code %s and body %s.", statusCode, bodyString), response, errorBody);
        } catch (IOException exception) {
            return new RuntimeException(String.format("Unknown error with status code %d.", statusCode), exception);
        }
    }


    /**
     * Extract the provisioning state of the resource from the response, if available.
     *
     * @param response the response from provisioning state to be extracted
     * @param serializerAdapter the adapter to deserialize the response
     * @return provisioning state if available, null otherwise
     * @throws IOException if deserialization fails
     */
    private static String tryExtractProvisioningState(Response<ResponseBody> response, SerializerAdapter<?> serializerAdapter) throws IOException {
        if (response.body() != null) {
            final String responseContent = response.body().string();
            response.body().close();
            if (!isNullOrEmpty(responseContent)) {
                final PollingResource resource = serializerAdapter.deserialize(responseContent, PollingResource.class);
                if (resource != null
                        && resource.properties != null
                        && !isNullOrEmpty(resource.properties.provisioningState)) {
                    return resource.properties.provisioningState;
                }
            }
        }
        return null;
    }

    /**
     * Checks whether the given status code indicates a non-exception state of the delete operation.
     *
     * @param statusCode the status code to check
     * @return true if status represents non-exceptional state, false otherwise.
     */
    private static boolean isAllowedStatusCode(final int statusCode) {
        return statusCode == 200 || statusCode == 201 || statusCode == 202 || statusCode == 204;
    }

    /**
     * Maps response code to operation status.
     *
     * @param statusCode the response code
     * @return operation status
     */
    private static String responseCodeToOperationStatus(final int statusCode) {
        switch (statusCode) {
            case 202:
                return IN_PROGRESS_STATUS;
            case 200:
            case 201:
            case 204:
                return SUCCESS_STATUS;
            default:
                return FAILED_STATUS;
        }
    }

    /**
     * Given the delete operation status checks whether it indicates successful or non-successful
     * completion of the operation.
     *
     * @param status the status
     * @return true if the status is a terminal status
     */
    private static boolean isTerminalStatus(final String status) {
        if (isNullOrEmpty(status)) {
            throw new IllegalArgumentException("status argument is required");
        }
        if (status.equalsIgnoreCase(FAILED_STATUS)) {
            return true;
        }
        if (status.equalsIgnoreCase(CANCELED_STATUS)) {
            return true;
        }
        if (status.equalsIgnoreCase(SUCCESS_STATUS)) {
            return true;
        }
        return false;
    }

    /**
     * @param str the string
     * @return true if the given string is null or empty, false otherwise
     */
    private static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    private class PollingResource {
        @JsonProperty(value = "properties")
        private PollingResourceProperties properties;
    }

    private class PollingResourceProperties {
        @JsonProperty(value = "provisioningState")
        private String provisioningState;
    }

    private class PollingResponse {
        @JsonProperty(value = "status")
        private String status;
        @JsonProperty(value = "error")
        private CloudError error;
    }

    private interface PollingService {
        @GET
        Observable<Response<ResponseBody>> get(@Url String url);
    }
}
