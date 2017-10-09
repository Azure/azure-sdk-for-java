package com.microsoft.azure;

import com.microsoft.rest.RestProxy;
import com.microsoft.rest.SwaggerMethodParser;
import com.microsoft.rest.http.HttpRequest;
import com.microsoft.rest.http.HttpResponse;
import rx.Observable;
import rx.Single;

import java.lang.reflect.Type;

public class CompletedPollStrategy extends PollStrategy {
    private final HttpResponse bufferedOriginalHttpResponse;

    public CompletedPollStrategy(RestProxy restProxy, HttpResponse bufferedOriginalHttpResponse) {
        super(restProxy, 0);

        this.bufferedOriginalHttpResponse = bufferedOriginalHttpResponse;
        setProvisioningState(ProvisioningState.SUCCEEDED);
    }

    @Override
    HttpRequest createPollRequest() {
        return null;
    }

    @Override
    Single<HttpResponse> updateFromAsync(HttpResponse httpPollResponse) {
        return null;
    }

    @Override
    boolean isDone() {
        return true;
    }

    Observable<OperationStatus<Object>> pollUntilDoneWithStatusUpdates(final HttpRequest originalHttpRequest, final SwaggerMethodParser methodParser, final Type operationStatusResultType) {
        return createOperationStatusObservable(originalHttpRequest, bufferedOriginalHttpResponse, methodParser, operationStatusResultType);
    }

    Single<HttpResponse> pollUntilDone() {
        return Single.just(bufferedOriginalHttpResponse);
    }
}
