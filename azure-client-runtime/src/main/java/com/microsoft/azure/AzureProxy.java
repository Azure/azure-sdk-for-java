/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure;

import com.google.common.reflect.TypeToken;
import com.microsoft.azure.annotations.AzureHost;
import com.microsoft.rest.protocol.SerializerAdapter;
import com.microsoft.rest.InvalidReturnTypeException;
import com.microsoft.rest.RestProxy;
import com.microsoft.rest.SwaggerInterfaceParser;
import com.microsoft.rest.SwaggerMethodParser;
import com.microsoft.rest.http.HttpClient;
import com.microsoft.rest.http.HttpRequest;
import com.microsoft.rest.http.HttpResponse;
import rx.Completable;
import rx.Observable;
import rx.Single;
import rx.functions.Func0;
import rx.functions.Func1;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;

/**
 * This class can be used to create an Azure specific proxy implementation for a provided Swagger
 * generated interface.
 */
public final class AzureProxy extends RestProxy {
    private static long defaultDelayInMilliseconds = 30 * 1000;

    /**
     * Create a new instance of RestProxy.
     * @param httpClient The HttpClient that will be used by this RestProxy to send HttpRequests.
     * @param serializer The serializer that will be used to convert response bodies to POJOs.
     * @param interfaceParser The parser that contains information about the swagger interface that
     *                        this RestProxy "implements".
     */
    private AzureProxy(HttpClient httpClient, SerializerAdapter<?> serializer, SwaggerInterfaceParser interfaceParser) {
        super(httpClient, serializer, interfaceParser);
    }

    /**
     * @return The millisecond delay that will occur by default between long running operation polls.
     */
    public static long defaultDelayInMilliseconds() {
        return AzureProxy.defaultDelayInMilliseconds;
    }

    /**
     * Set the millisecond delay that will occur by default between long running operation polls.
     * @param defaultDelayInMilliseconds The number of milliseconds to delay before sending the next
     *                                   long running operation status poll.
     */
    public static void setDefaultDelayInMilliseconds(long defaultDelayInMilliseconds) {
        AzureProxy.defaultDelayInMilliseconds = defaultDelayInMilliseconds;
    }

    /**
     * Create a proxy implementation of the provided Swagger interface.
     * @param swaggerInterface The Swagger interface to provide a proxy implementation for.
     * @param azureEnvironment The azure environment that the proxy implementation will target.
     * @param httpClient The internal HTTP client that will be used to make REST calls.
     * @param serializer The serializer that will be used to convert POJOs to and from request and
     *                   response bodies.
     * @param <A> The type of the Swagger interface.
     * @return A proxy implementation of the provided Swagger interface.
     */
    @SuppressWarnings("unchecked")
    public static <A> A create(Class<A> swaggerInterface, AzureEnvironment azureEnvironment, final HttpClient httpClient, SerializerAdapter<?> serializer) {
        String baseUrl = null;

        if (azureEnvironment != null) {
            final AzureHost azureHost = swaggerInterface.getAnnotation(AzureHost.class);
            if (azureHost != null) {
                baseUrl = azureEnvironment.url(azureHost.endpoint());
            }
        }

        return AzureProxy.create(swaggerInterface, baseUrl, httpClient, serializer);
    }

    /**
     * Create a proxy implementation of the provided Swagger interface.
     * @param swaggerInterface The Swagger interface to provide a proxy implementation for.
     * @param baseUrl The base URL (protocol and host) that the proxy implementation will target.
     * @param httpClient The internal HTTP client that will be used to make REST calls.
     * @param serializer The serializer that will be used to convert POJOs to and from request and
     *                   response bodies.
     * @param <A> The type of the Swagger interface.
     * @return A proxy implementation of the provided Swagger interface.
     */
    @SuppressWarnings("unchecked")
    public static <A> A create(Class<A> swaggerInterface, String baseUrl, final HttpClient httpClient, SerializerAdapter<?> serializer) {
        final SwaggerInterfaceParser interfaceParser = new SwaggerInterfaceParser(swaggerInterface, baseUrl);
        final AzureProxy azureProxy = new AzureProxy(httpClient, serializer, interfaceParser);
        return (A) Proxy.newProxyInstance(swaggerInterface.getClassLoader(), new Class[]{swaggerInterface}, azureProxy);
    }

    @Override
    protected Object handleSyncHttpResponse(HttpRequest httpRequest, HttpResponse httpResponse, SwaggerMethodParser methodParser) throws IOException, InterruptedException {
        final SerializerAdapter<?> serializer = serializer();

        final PollStrategy pollStrategy = createPollStrategy(httpRequest, httpResponse, serializer);
        if (pollStrategy != null) {
            while (!pollStrategy.isDone()) {
                pollStrategy.delay();

                final HttpRequest pollRequest = pollStrategy.createPollRequest();
                httpResponse = sendHttpRequest(pollRequest);

                pollStrategy.updateFrom(httpResponse);
            }
        }

        return super.handleSyncHttpResponse(httpRequest, httpResponse, methodParser);
    }

    @Override
    protected Object handleAsyncHttpResponse(final HttpRequest httpRequest, Single<HttpResponse> asyncHttpResponse, final SwaggerMethodParser methodParser) {
        final SerializerAdapter<?> serializer = serializer();

        Object result = null;

        final Type returnType = methodParser.returnType();
        final TypeToken returnTypeToken = TypeToken.of(returnType);

        if (returnTypeToken.isSubtypeOf(Completable.class) || returnTypeToken.isSubtypeOf(Single.class)) {
            asyncHttpResponse = asyncHttpResponse
                    .flatMap(new Func1<HttpResponse, Single<? extends HttpResponse>>() {
                        @Override
                        public Single<? extends HttpResponse> call(HttpResponse httpResponse) {
                            final PollStrategy pollStrategy = createPollStrategy(httpRequest, httpResponse, serializer);

                            Single<HttpResponse> result;
                            if (pollStrategy == null || pollStrategy.isDone()) {
                                result = Single.just(httpResponse);
                            }
                            else {
                                result = sendPollRequestWithDelay(pollStrategy)
                                        .repeat()
                                        .takeUntil(new Func1<HttpResponse, Boolean>() {
                                            @Override
                                            public Boolean call(HttpResponse ignored) {
                                                return pollStrategy.isDone();
                                            }
                                        })
                                        .last()
                                        .toSingle();
                            }
                            return result;
                        }
                    });
            result = super.handleAsyncHttpResponse(httpRequest, asyncHttpResponse, methodParser);
        }
        else if (returnTypeToken.isSubtypeOf(Observable.class)) {
            final Type operationStatusType = ((ParameterizedType) returnType).getActualTypeArguments()[0];
            final TypeToken operationStatusTypeToken = TypeToken.of(operationStatusType);
            if (!operationStatusTypeToken.isSubtypeOf(OperationStatus.class)) {
                throw new InvalidReturnTypeException("AzureProxy only supports swagger interface methods that return Observable (such as " + methodParser.fullyQualifiedMethodName() + "()) if the Observable's inner type that is OperationStatus (not " + returnType.toString() + ").");
            }
            else {
                final Type operationStatusResultType = ((ParameterizedType) operationStatusType).getActualTypeArguments()[0];
                result = asyncHttpResponse
                        .toObservable()
                        .flatMap(new Func1<HttpResponse, Observable<OperationStatus<Object>>>() {
                            @Override
                            public Observable<OperationStatus<Object>> call(HttpResponse httpResponse) {
                                final PollStrategy pollStrategy = createPollStrategy(httpRequest, httpResponse, serializer);

                                Observable<OperationStatus<Object>> result;
                                if (pollStrategy == null || pollStrategy.isDone()) {
                                    result = toCompletedOperationStatusObservable(httpRequest, httpResponse, methodParser, operationStatusResultType);
                                } else {
                                    result = sendPollRequestWithDelay(pollStrategy)
                                            .flatMap(new Func1<HttpResponse, Observable<OperationStatus<Object>>>() {
                                                @Override
                                                public Observable<OperationStatus<Object>> call(HttpResponse httpResponse) {
                                                    Observable<OperationStatus<Object>> result;
                                                    if (!pollStrategy.isDone()) {
                                                        result = Observable.just(new OperationStatus<>(pollStrategy));
                                                    }
                                                    else {
                                                        result = toCompletedOperationStatusObservable(httpRequest, httpResponse, methodParser, operationStatusResultType);
                                                    }
                                                    return result;
                                                }
                                            })
                                            .repeat()
                                            .takeUntil(new Func1<OperationStatus<Object>, Boolean>() {
                                                @Override
                                                public Boolean call(OperationStatus<Object> operationStatus) {
                                                    return operationStatus.isDone();
                                                }
                                            });
                                }
                                return result;
                            }
                        });
            }
        }

        return result;
    }

    private static PollStrategy createPollStrategy(HttpRequest httpRequest, HttpResponse httpResponse, SerializerAdapter<?> serializer) {
        PollStrategy result = null;

        final int httpStatusCode = httpResponse.statusCode();
        if (httpStatusCode != 200) {
            final String fullyQualifiedMethodName = httpRequest.callerMethod();
            final String originalHttpRequestMethod = httpRequest.httpMethod();
            final String originalHttpRequestUrl = httpRequest.url();

            if (originalHttpRequestMethod.equalsIgnoreCase("PUT") || originalHttpRequestMethod.equalsIgnoreCase("PATCH")) {
                if (httpStatusCode == 201) {
                    result = AzureAsyncOperationPollStrategy.tryToCreate(fullyQualifiedMethodName, httpResponse, originalHttpRequestUrl, serializer);
                } else if (httpStatusCode == 202) {
                    result = AzureAsyncOperationPollStrategy.tryToCreate(fullyQualifiedMethodName, httpResponse, originalHttpRequestUrl, serializer);
                    if (result == null) {
                        result = LocationPollStrategy.tryToCreate(fullyQualifiedMethodName, httpResponse);
                    }
                }
            } else /* if (originalRequestHttpMethod.equalsIgnoreCase("DELETE") || originalRequestHttpMethod.equalsIgnoreCase("POST") */ {
                if (httpStatusCode == 202) {
                    result = AzureAsyncOperationPollStrategy.tryToCreate(fullyQualifiedMethodName, httpResponse, originalHttpRequestUrl, serializer);
                    if (result == null) {
                        result = LocationPollStrategy.tryToCreate(fullyQualifiedMethodName, httpResponse);
                    }
                }
            }
        }

        return result;
    }

    private Observable<OperationStatus<Object>> toCompletedOperationStatusObservable(HttpRequest httpRequest, HttpResponse httpResponse, SwaggerMethodParser methodParser, Type operationStatusResultType) {
        Observable<OperationStatus<Object>> result;
        try {
            final Object resultObject = super.handleSyncHttpResponse(httpRequest, httpResponse, methodParser, operationStatusResultType);
            result = Observable.just(new OperationStatus<>(resultObject));
        } catch (IOException e) {
            result = Observable.error(e);
        }
        return result;
    }

    private Observable<HttpResponse> sendPollRequestWithDelay(final PollStrategy pollStrategy) {
        return Observable.defer(new Func0<Observable<HttpResponse>>() {
            @Override
            public Observable<HttpResponse> call() {
                return pollStrategy
                        .delayAsync()
                        .flatMap(new Func1<Void, Single<HttpResponse>>() {
                            @Override
                            public Single<HttpResponse> call(Void ignored) {
                                final HttpRequest pollRequest = pollStrategy.createPollRequest();
                                return sendHttpRequestAsync(pollRequest);
                            }
                        })
                        .flatMap(new Func1<HttpResponse, Single<HttpResponse>>() {
                            @Override
                            public Single<HttpResponse> call(HttpResponse response) {
                                return pollStrategy.updateFromAsync(response);
                            }
                        })
                        .toObservable();
            }
        });
    }
}
