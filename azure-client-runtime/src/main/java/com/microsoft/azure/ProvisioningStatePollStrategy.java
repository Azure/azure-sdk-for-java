package com.microsoft.azure;

import com.microsoft.rest.RestProxy;
import com.microsoft.rest.SwaggerMethodParser;
import com.microsoft.rest.http.HttpRequest;
import com.microsoft.rest.http.HttpResponse;
import com.microsoft.rest.protocol.SerializerAdapter;
import rx.Observable;
import rx.Single;
import rx.functions.Func1;

import java.io.IOException;
import java.lang.reflect.Type;

public class ProvisioningStatePollStrategy extends PollStrategy {
    private final HttpRequest originalRequest;
    private final HttpResponse originalResponse;
//    private final SerializerAdapter<?> serializer;
//    private Object pollResult;
//    private Type pollResultType;

    //ProvisioningStatePollStrategy(HttpRequest originalRequest, String provisioningState, SerializerAdapter<?> serializer, Type pollResultType, long delayInMilliseconds) {
    ProvisioningStatePollStrategy(RestProxy restProxy, HttpRequest originalRequest, HttpResponse originalResponse, long delayInMilliseconds) {
        super(restProxy, delayInMilliseconds);

        this.originalRequest = originalRequest;
        this.originalResponse = originalResponse;
//        this.serializer = serializer;
//        this.pollResultType = pollResultType;
        setProvisioningState(ProvisioningState.SUCCEEDED);
    }

    @Override
    HttpRequest createPollRequest() {
        return new HttpRequest(originalRequest.callerMethod(), "GET", originalRequest.url());
    }

    @Override
    Single<HttpResponse> updateFromAsync(final HttpResponse httpPollResponse) {
        return Single.just(httpPollResponse);
//        return httpPollResponse.bodyAsStringAsync()
//                .flatMap(new Func1<String, Single<HttpResponse>>() {
//                    @Override
//                    public Single<HttpResponse> call(String responseBody) {
//                        Single<HttpResponse> result;
//                        try {
//                            final ResourceWithProvisioningState resource = serializer.deserialize(responseBody, ResourceWithProvisioningState.class);
//                            if (resource == null || resource.properties() == null || resource.properties().provisioningState() == null) {
//                                setProvisioningState(ProvisioningState.IN_PROGRESS);
//                            }
//                            else {
//                                setProvisioningState(resource.properties().provisioningState());
//                            }
//
//                            if (isDone()) {
//                                pollResult = serializer.deserialize(responseBody, pollResultType);
//                            }
//
//                            result = Single.just(httpPollResponse);
//                        } catch (IOException e) {
//                            result = Single.error(e);
//                        }
//                        return result;
//                    }
//                });
    }

    @Override
    boolean isDone() {
        //return ProvisioningState.isCompleted(provisioningState());
        return true;
    }

    @Override
    Observable<OperationStatus<Object>> pollUntilDoneWithStatusUpdates(final HttpRequest originalHttpRequest, final SwaggerMethodParser methodParser, final Type operationStatusResultType) {
        return createOperationStatusObservable(originalHttpRequest, originalResponse, methodParser, operationStatusResultType);
    }

    @Override
    public Single<HttpResponse> pollUntilDone() {
        return Single.just(originalResponse);
    }
}