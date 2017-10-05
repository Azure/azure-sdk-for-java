package com.microsoft.azure;

import com.microsoft.rest.http.HttpRequest;
import com.microsoft.rest.http.HttpResponse;
import com.microsoft.rest.protocol.SerializerAdapter;
import rx.Single;
import rx.functions.Func1;

import java.io.IOException;

public class ProvisioningStatePollStrategy extends PollStrategy {
    private final HttpRequest originalRequest;
    private final SerializerAdapter<?> serializer;

    private ProvisioningStatePollStrategy(HttpRequest originalRequest, SerializerAdapter<?> serializer, long delayInMilliseconds) {
        super(delayInMilliseconds);

        this.originalRequest = originalRequest;
        this.serializer = serializer;
    }

    @Override
    HttpRequest createPollRequest() {
        return new HttpRequest(originalRequest.callerMethod(), "GET", originalRequest.url());
    }

    @Override
    Single<HttpResponse> updateFromAsync(final HttpResponse httpPollResponse) {
        return httpPollResponse.bodyAsStringAsync()
                .flatMap(new Func1<String, Single<HttpResponse>>() {
                    @Override
                    public Single<HttpResponse> call(String responseBody) {
                        Single<HttpResponse> result;
                        try {
                            final ResourceWithProvisioningState resourceWithProvisioningState = serializer.deserialize(responseBody, ResourceWithProvisioningState.class);
                            if (resourceWithProvisioningState == null || resourceWithProvisioningState.properties() == null || resourceWithProvisioningState.properties().provisioningState() == null) {
                                setProvisioningState(ProvisioningState.IN_PROGRESS);
                            }
                            else {
                                setProvisioningState(resourceWithProvisioningState.properties().provisioningState());
                            }

                            result = Single.just(httpPollResponse);
                        } catch (IOException e) {
                            result = Single.error(e);
                        }
                        return result;
                    }
                });
    }

    @Override
    boolean isDone() {
        final String currentProvisioningState = provisioningState();
        return ProvisioningState.SUCCEEDED.equalsIgnoreCase(currentProvisioningState) ||
                ProvisioningState.FAILED.equals(currentProvisioningState);
    }

    public static Single<PollStrategy> tryToCreate(HttpRequest originalRequest, HttpResponse originalResponse, SerializerAdapter<?> serializer, long delayInMilliseconds) {
        final ProvisioningStatePollStrategy pollStrategy = new ProvisioningStatePollStrategy(originalRequest, serializer, delayInMilliseconds);
        return pollStrategy
                .updateFromAsync(originalResponse)
                .map(new Func1<HttpResponse, PollStrategy>() {
                    @Override
                    public PollStrategy call(HttpResponse httpResponse) {
                        return pollStrategy;
                    }
                });
    }
}
