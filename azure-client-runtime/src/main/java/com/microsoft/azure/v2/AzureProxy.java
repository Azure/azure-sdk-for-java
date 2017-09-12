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
import rx.functions.Func1;

import java.io.IOException;

public class AzureProxy {
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
            public Object handleAsyncResponse(Single<? extends HttpResponse> asyncResponse, final SwaggerMethodParser methodParser, SerializerAdapter<?> serializer) {
                asyncResponse = asyncResponse
                        .flatMap(new Func1<HttpResponse, Single<? extends HttpResponse>>() {
                            @Override
                            public Single<? extends HttpResponse> call(HttpResponse response) {
                                Single<? extends HttpResponse> result;
                                if (response.statusCode() != 202) {
                                    result = Single.just(response);
                                }
                                else {
                                    final Value<String> location = new Value<>(response.headerValue("Location"));
                                    result = Observable.just(true)
                                            .flatMap(new Func1<Boolean, Observable<? extends HttpResponse>>() {
                                                @Override
                                                public Observable<? extends HttpResponse> call(Boolean aBoolean) {
                                                    final HttpRequest pollRequest = new HttpRequest(methodParser.fullyQualifiedMethodName(), "GET", location.get());
                                                    return httpClient.sendRequestAsync(pollRequest).toObservable();
                                                }
                                            })
                                            .repeat()
                                            .filter(new Func1<HttpResponse, Boolean>() {
                                                @Override
                                                public Boolean call(HttpResponse response) {
                                                    final boolean result = response.statusCode() != 202;
                                                    if (!result) {
                                                        location.set(response.headerValue("Location"));
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

    private static class Value<T> {
        private T value;
        private boolean hasValue;

        public Value() {
            hasValue = false;
        }

        public Value(T value) {
            this.value = value;
            hasValue = true;
        }

        public boolean hasValue() {
            return hasValue;
        }

        public T get() {
            return hasValue ? value : null;
        }

        public void set(T value) {
            this.value = value;
            hasValue = true;
        }

        public void reset() {
            hasValue = false;
            value = null;
        }
    }
}
