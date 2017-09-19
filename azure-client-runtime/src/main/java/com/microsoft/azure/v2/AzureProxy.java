/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.v2;

import com.google.common.reflect.TypeToken;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.v2.annotations.AzureHost;
import com.microsoft.rest.protocol.SerializerAdapter;
import com.microsoft.rest.v2.InvalidReturnTypeException;
import com.microsoft.rest.v2.RestProxy;
import com.microsoft.rest.v2.SwaggerInterfaceParser;
import com.microsoft.rest.v2.SwaggerMethodParser;
import com.microsoft.rest.v2.http.HttpClient;
import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import rx.Completable;
import rx.Observable;
import rx.Single;
import rx.functions.Func0;
import rx.functions.Func1;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

/**
 * This class can be used to create an Azure specific proxy implementation for a provided Swagger
 * generated interface.
 */
public final class AzureProxy extends RestProxy {
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

        final SwaggerInterfaceParser interfaceParser = new SwaggerInterfaceParser(swaggerInterface, baseUrl);
        final AzureProxy azureProxy = new AzureProxy(httpClient, serializer, interfaceParser);
        return (A) Proxy.newProxyInstance(swaggerInterface.getClassLoader(), new Class[]{swaggerInterface}, azureProxy);
    }

    @Override
    protected Object handleSyncHttpResponse(HttpResponse httpResponse, SwaggerMethodParser methodParser) throws IOException, InterruptedException {
        String pollUrl = null;
        Long retryAfterSeconds = null;
        while (!isDonePolling(httpResponse)) {
            pollUrl = getPollUrl(httpResponse, pollUrl);

            retryAfterSeconds = getRetryAfterSeconds(httpResponse, retryAfterSeconds);
            if (retryAfterSeconds != null && retryAfterSeconds > 0) {
                Thread.sleep(retryAfterSeconds * 1000);
            }

            final HttpRequest pollRequest = createPollRequest(methodParser.fullyQualifiedMethodName(), pollUrl);
            httpResponse = sendHttpRequest(pollRequest);
        }

        return super.handleSyncHttpResponse(httpResponse, methodParser);
    }

    @Override
    protected Object handleAsyncHttpResponse(Single<HttpResponse> asyncHttpResponse, final SwaggerMethodParser methodParser) {
        Object result = null;

        final Type returnType = methodParser.returnType();
        final TypeToken returnTypeToken = TypeToken.of(returnType);

        if (returnTypeToken.isSubtypeOf(Completable.class) || returnTypeToken.isSubtypeOf(Single.class)) {
            asyncHttpResponse = asyncHttpResponse
                    .flatMap(new Func1<HttpResponse, Single<? extends HttpResponse>>() {
                        @Override
                        public Single<? extends HttpResponse> call(HttpResponse response) {
                            Single<HttpResponse> result;
                            if (isDonePolling(response)) {
                                result = Single.just(response);
                            }
                            else {
                                final Value<String> pollUrl = new Value<>(getPollUrl(response, null));
                                final Value<Long> retryAfterSeconds = new Value<>(getRetryAfterSeconds(response, null));

                                result = sendPollRequestWithDelay(methodParser, pollUrl, retryAfterSeconds)
                                        .repeat()
                                        .takeUntil(new Func1<HttpResponse, Boolean>() {
                                            @Override
                                            public Boolean call(HttpResponse response) {
                                                pollUrl.set(getPollUrl(response, pollUrl.get()));
                                                retryAfterSeconds.set(getRetryAfterSeconds(response, retryAfterSeconds.get()));
                                                return isDonePolling(response);
                                            }
                                        })
                                        .last()
                                        .toSingle();
                            }
                            return result;
                        }
                    });
            result = super.handleAsyncHttpResponse(asyncHttpResponse, methodParser);
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
                            public Observable<OperationStatus<Object>> call(HttpResponse response) {
                                Observable<OperationStatus<Object>> result;
                                if (isDonePolling(response)) {
                                    return toOperationStatusObservable(response, methodParser, operationStatusResultType);
                                } else {
                                    final Value<String> pollUrl = new Value<>(getPollUrl(response, null));
                                    final Value<Long> retryAfterSeconds = new Value<>(getRetryAfterSeconds(response, null));
                                    final Value<OperationStatus<Object>> lastOperationStatus = new Value<>();

                                    result = sendPollRequestWithDelay(methodParser, pollUrl, retryAfterSeconds)
                                            .flatMap(new Func1<HttpResponse, Observable<OperationStatus<Object>>>() {
                                                @Override
                                                public Observable<OperationStatus<Object>> call(HttpResponse httpResponse) {
                                                    pollUrl.set(getPollUrl(httpResponse, pollUrl.get()));
                                                    retryAfterSeconds.set(getRetryAfterSeconds(httpResponse, retryAfterSeconds.get()));

                                                    Observable<OperationStatus<Object>> result;
                                                    if (isDonePolling(httpResponse)) {
                                                        result = toOperationStatusObservable(httpResponse, methodParser, operationStatusResultType);
                                                    }
                                                    else {
                                                        result = Observable.just(OperationStatus.inProgress());
                                                    }
                                                    return result;
                                                }
                                            })
                                            .repeat()
                                            .takeUntil(new Func1<OperationStatus<Object>, Boolean>() {
                                                @Override
                                                public Boolean call(OperationStatus<Object> operationStatus) {
                                                    final boolean stop = operationStatus.isDone();
                                                    if (stop) {
                                                        // Take until will not return the operationStatus that is
                                                        // marked as done, so we set it here and then concatWith() it
                                                        // later to force the Observable to return the operationStatus
                                                        // that is done.
                                                        lastOperationStatus.set(operationStatus);
                                                    }
                                                    return stop;
                                                }
                                            })
                                            .concatWith(Observable.defer(new Func0<Observable<OperationStatus<Object>>>() {
                                                @Override
                                                public Observable<OperationStatus<Object>> call() {
                                                    return Observable.just(lastOperationStatus.get());
                                                }
                                            }));
                                }
                                return result;
                            }
                        });
            }
        }

        return result;
    }

    private Observable<OperationStatus<Object>> toOperationStatusObservable(HttpResponse httpResponse, SwaggerMethodParser methodParser, Type operationStatusResultType) {
        Observable<OperationStatus<Object>> result;
        try {
            final Object resultObject = super.handleSyncHttpResponse(httpResponse, methodParser, operationStatusResultType);
            final OperationStatus<Object> operationStatus = OperationStatus.completed(resultObject);
            result = Observable.just(operationStatus);
        } catch (IOException e) {
            result = Observable.error(e);
        }
        return result;
    }

    /**
     * Get the URL from the provided HttpResponse that should be requested in order to poll the
     * status of a long running operation.
     * @param response The HttpResponse that contains the poll URL.
     * @param currentPollUrl The poll URL that was previously used. Sometimes a HttpResponse may not
     *                       have an updated poll URL, so in those cases we should just use the
     *                       previous one.
     * @return The URL that should be requested in order to poll the status of a long running
     * operation.
     */
    static String getPollUrl(HttpResponse response, String currentPollUrl) {
        String pollUrl = currentPollUrl;

        final String location = response.headerValue("Location");
        if (location != null && !location.isEmpty()) {
            pollUrl = location;
        }

        return pollUrl;
    }

    private Observable<HttpResponse> sendPollRequestWithDelay(SwaggerMethodParser methodParser, final Value<String> pollUrl, final Value<Long> retryAfterSeconds) {
        final String fullyQualifiedMethodName = methodParser.fullyQualifiedMethodName();

        return Observable.defer(new Func0<Observable<HttpResponse>>() {
            @Override
            public Observable<HttpResponse> call() {
                final HttpRequest pollRequest = createPollRequest(fullyQualifiedMethodName, pollUrl.get());

                Single<HttpResponse> pollResponse = sendHttpRequestAsync(pollRequest);
                if (retryAfterSeconds.get() != null) {
                    pollResponse = pollResponse.delay(retryAfterSeconds.get(), TimeUnit.SECONDS);
                }
                return pollResponse.toObservable();
            }
        });
    }

    private static HttpRequest createPollRequest(String fullyQualifiedMethodName, String pollUrl) {
        return new HttpRequest(fullyQualifiedMethodName, "GET", pollUrl);
    }

    private static boolean isDonePolling(HttpResponse response) {
        return response.statusCode() != 202;
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
}
