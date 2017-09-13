package com.microsoft.azure.v2;

import com.microsoft.rest.RestException;
import com.microsoft.rest.protocol.SerializerAdapter;
import com.microsoft.rest.v2.RestProxy;
import com.microsoft.rest.v2.SwaggerMethodParser;
import com.microsoft.rest.v2.http.HttpClient;
import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import rx.Observable;
import rx.Single;
import rx.functions.Func0;
import rx.functions.Func1;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class AzureProxy {

    /**
     * Empty constructor.
     */
    AzureProxy() {
    }

    /**
     * Create a proxy implementation of the provided Swagger interface.
     * @param swaggerInterface The Swagger interface to provide a proxy implementation for.
     * @param httpClient The internal HTTP client that will be used to make REST calls.
     * @param serializer The serializer that will be used to convert POJOs to and from request and
     *                   response bodies.
     * @param <A> The type of the Swagger interface.
     * @return A proxy implementation of the provided Swagger interface.
     */
    @SuppressWarnings("unchecked")
    public static <A> A create(Class<A> swaggerInterface, final HttpClient httpClient, SerializerAdapter<?> serializer) {
        return RestProxy.create(swaggerInterface, httpClient, serializer, new RestProxy.ResponseHandler() {
            @Override
            public Object handleSyncResponse(HttpResponse response, SwaggerMethodParser methodParser, SerializerAdapter<?> serializer) throws IOException, RestException {
                while (response.statusCode() == 202) {
                    final String location = response.headerValue("Location");
                    final HttpRequest pollRequest = new HttpRequest(methodParser.fullyQualifiedMethodName(), "GET", location);
                    response = httpClient.sendRequest(pollRequest);
                }
                return RestProxy.defaultResponseHandler.handleSyncResponse(response, methodParser, serializer);
            }

            @Override
            public Object handleAsyncResponse(Single<HttpResponse> asyncResponse, final SwaggerMethodParser methodParser, SerializerAdapter<?> serializer) {
                asyncResponse = asyncResponse
                        .flatMap(new Func1<HttpResponse, Single<? extends HttpResponse>>() {
                            @Override
                            public Single<? extends HttpResponse> call(HttpResponse response) {
                                Single<? extends HttpResponse> result;
                                if (response.statusCode() != 202) {
                                    result = Single.just(response);
                                }
                                else {
                                    final Value<String> pollUrl = new Value<>(getPollUrl(response, null));
                                    final Value<Long> retryAfterSeconds = new Value<>(getRetryAfterSeconds(response, null));

                                    result = Observable.defer(new Func0<Observable<HttpResponse>>() {
                                                @Override
                                                public Observable<HttpResponse> call() {
                                                    final HttpRequest pollRequest = new HttpRequest(methodParser.fullyQualifiedMethodName(), "GET", pollUrl.get());
                                                    return httpClient.sendRequestAsync(pollRequest).toObservable();
                                                }
                                            })
                                            .repeatWhen(new Func1<Observable<? extends Void>, Observable<?>>() {
                                                @Override
                                                public Observable<?> call(Observable<? extends Void> observable) {
                                                    return retryAfterSeconds.get() == null ? observable : observable.delay(retryAfterSeconds.get(), TimeUnit.SECONDS);
                                                }
                                            })
                                            .filter(new Func1<HttpResponse, Boolean>() {
                                                @Override
                                                public Boolean call(HttpResponse response) {
                                                    final boolean result = response.statusCode() != 202;
                                                    if (!result) {
                                                        pollUrl.set(getPollUrl(response, pollUrl.get()));
                                                        retryAfterSeconds.set(getRetryAfterSeconds(response, retryAfterSeconds.get()));
                                                    }
                                                    return result;
                                                }
                                            })
                                            .first()
                                            .toSingle();
                                }
                                return result;
                            }
                        });
                return RestProxy.defaultResponseHandler.handleAsyncResponse(asyncResponse, methodParser, serializer);
            }
        });
    }

    static String getPollUrl(HttpResponse response, String currentPollUrl) {
        String pollUrl = currentPollUrl;

        final String location = response.headerValue("Location");
        if (location != null && !location.isEmpty()) {
            pollUrl = location;
        }

        return pollUrl;
    }

    static Long getRetryAfterSeconds(HttpResponse response, Long currentRetryAfterSeconds) {
        Long retryAfterSeconds = currentRetryAfterSeconds;

        final String retryAfterSecondsString = response.headerValue("Retry-After");
        if (retryAfterSecondsString != null && !retryAfterSecondsString.isEmpty()) {
            try {
                retryAfterSeconds = Long.valueOf(retryAfterSecondsString);
            } catch (Exception ignored) {
            }
        }

        return retryAfterSeconds;
    }

    private static class Value<T> {
        private T value;

        public Value(T value) {
            set(value);
        }

        public T get() {
            return value;
        }

        public void set(T value) {
            this.value = value;
        }
    }
}
