/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.v2;

import com.google.common.hash.Hashing;
import com.google.common.reflect.TypeToken;
import com.microsoft.azure.v2.annotations.AzureHost;
import com.microsoft.azure.v2.serializer.AzureJacksonAdapter;
import com.microsoft.rest.v2.credentials.ServiceClientCredentials;
import com.microsoft.rest.v2.http.HttpClient;
import com.microsoft.rest.v2.http.HttpMethod;
import com.microsoft.rest.v2.http.HttpPipeline;
import com.microsoft.rest.v2.http.HttpPipelineBuilder;
import com.microsoft.rest.v2.http.NettyClient;
import com.microsoft.rest.v2.policy.CookiePolicyFactory;
import com.microsoft.rest.v2.policy.CredentialsPolicyFactory;
import com.microsoft.rest.v2.policy.HttpLogDetailLevel;
import com.microsoft.rest.v2.policy.HttpLoggingPolicyFactory;
import com.microsoft.rest.v2.policy.RequestPolicyFactory;
import com.microsoft.rest.v2.policy.RetryPolicyFactory;
import com.microsoft.rest.v2.protocol.SerializerAdapter;
import com.microsoft.rest.v2.InvalidReturnTypeException;
import com.microsoft.rest.v2.RestProxy;
import com.microsoft.rest.v2.SwaggerInterfaceParser;
import com.microsoft.rest.v2.SwaggerMethodParser;
import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.functions.Function;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.concurrent.Callable;

/**
 * This class can be used to create an Azure specific proxy implementation for a provided Swagger
 * generated interface.
 */
public final class AzureProxy extends RestProxy {
    private static long defaultPollingDelayInMilliseconds = 30 * 1000;

    /**
     * Create a new instance of RestProxy.
     * @param httpPipeline The HttpPipeline that will be used by this AzureProxy to send HttpRequests.
     * @param serializer The serializer that will be used to convert response bodies to POJOs.
     * @param interfaceParser The parser that contains information about the swagger interface that
     *                        this RestProxy "implements".
     */
    private AzureProxy(HttpPipeline httpPipeline, SerializerAdapter<?> serializer, SwaggerInterfaceParser interfaceParser) {
        super(httpPipeline, serializer, interfaceParser);
    }

    /**
     * @return The millisecond delay that will occur by default between long running operation polls.
     */
    public static long defaultDelayInMilliseconds() {
        return AzureProxy.defaultPollingDelayInMilliseconds;
    }

    /**
     * Set the millisecond delay that will occur by default between long running operation polls.
     * @param defaultPollingDelayInMilliseconds The number of milliseconds to delay before sending the next
     *                                   long running operation status poll.
     */
    public static void setDefaultPollingDelayInMilliseconds(long defaultPollingDelayInMilliseconds) {
        AzureProxy.defaultPollingDelayInMilliseconds = defaultPollingDelayInMilliseconds;
    }

    /**
     * Get the default serializer.
     * @return the default serializer.
     */
    public static SerializerAdapter<?> createDefaultSerializer() {
        return new AzureJacksonAdapter();
    }

    private static String operatingSystem;
    private static String operatingSystem() {
        if (operatingSystem == null) {
            operatingSystem = System.getProperty("os.name") + "/" + System.getProperty("os.version");
        }
        return operatingSystem;
    }

    private static String macAddressHash;
    private static String macAddressHash() {
        if (macAddressHash == null) {
            byte[] macBytes = null;
            try {
                Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();
                while (networks.hasMoreElements()) {
                    NetworkInterface network = networks.nextElement();
                    macBytes = network.getHardwareAddress();

                    if (macBytes != null) {
                        break;
                    }
                }
            } catch (Throwable t) {
                // It's okay ignore mac address hash telemetry
            }

            if (macBytes == null) {
                macBytes = "Unknown".getBytes();
            }

            macAddressHash = Hashing.sha256().hashBytes(macBytes).toString();
        }
        return macAddressHash;
    }

    private static String javaVersion;
    private static String javaVersion() {
        if (javaVersion == null) {
            final String versionProperty = System.getProperty("java.version");
            javaVersion = versionProperty != null ? versionProperty : "Unknown";
        }
        return javaVersion;
    }

    private static <T> String getDefaultUserAgentString(Class<?> swaggerInterface) {
        final String packageImplementationVersion = swaggerInterface == null ? "" : "/" + swaggerInterface.getPackage().getImplementationVersion();
        final String operatingSystem = operatingSystem();
        final String macAddressHash = macAddressHash();
        final String javaVersion = javaVersion();
        return String.format("Azure-SDK-For-Java%s OS:%s MacAddressHash:%s Java:%s",
                packageImplementationVersion,
                operatingSystem,
                macAddressHash,
                javaVersion);
    }

    /**
     * Create the default HttpPipeline.
     * @param swaggerInterface The interface that the pipeline will use to generate a user-agent
     *                         string.
     * @return the default HttpPipeline.
     */
    public static HttpPipeline createDefaultPipeline(Class<?> swaggerInterface) {
        return createDefaultPipeline(swaggerInterface, (RequestPolicyFactory) null);
    }

    /**
     * Create the default HttpPipeline.
     * @param swaggerInterface The interface that the pipeline will use to generate a user-agent
     *                         string.
     * @param credentials The credentials to use to apply authentication to the pipeline.
     * @return the default HttpPipeline.
     */
    public static HttpPipeline createDefaultPipeline(Class<?> swaggerInterface, ServiceClientCredentials credentials) {
        return createDefaultPipeline(swaggerInterface, new CredentialsPolicyFactory(credentials));
    }

    /**
     * Create the default HttpPipeline.
     * @param swaggerInterface The interface that the pipeline will use to generate a user-agent
     *                         string.
     * @param credentialsPolicy The credentials policy factory to use to apply authentication to the
     *                          pipeline.
     * @return the default HttpPipeline.
     */
    public static HttpPipeline createDefaultPipeline(Class<?> swaggerInterface, RequestPolicyFactory credentialsPolicy) {
        final HttpClient httpClient = new NettyClient.Factory().create(null);
        final HttpPipelineBuilder builder = new HttpPipelineBuilder().withHttpClient(httpClient);
        builder.withUserAgent(getDefaultUserAgentString(swaggerInterface));
        builder.withRequestPolicy(new RetryPolicyFactory());
        builder.withRequestPolicy(new CookiePolicyFactory());
        if (credentialsPolicy != null) {
            builder.withRequestPolicy(credentialsPolicy);
        }
        builder.withRequestPolicy(new HttpLoggingPolicyFactory(HttpLogDetailLevel.HEADERS));
        return builder.build();
    }

    /**
     * Create a proxy implementation of the provided Swagger interface.
     * @param swaggerInterface The Swagger interface to provide a proxy implementation for.
     * @param azureServiceClient The AzureServiceClient that contains the details to use to create
     *                          the AzureProxy implementation of the swagger interface.
     * @param <A> The type of the Swagger interface.
     * @return A proxy implementation of the provided Swagger interface.
     */
    @SuppressWarnings("unchecked")
    public static <A> A create(Class<A> swaggerInterface, AzureServiceClient azureServiceClient) {
        return AzureProxy.create(swaggerInterface, azureServiceClient.azureEnvironment(), azureServiceClient.httpPipeline(), azureServiceClient.serializerAdapter());
    }

    /**
     * Create a proxy implementation of the provided Swagger interface.
     * @param swaggerInterface The Swagger interface to provide a proxy implementation for.
     * @param httpPipeline The HTTP httpPipeline will be used to make REST calls.
     * @param serializer The serializer that will be used to convert POJOs to and from request and
     *                   response bodies.
     * @param <A> The type of the Swagger interface.
     * @return A proxy implementation of the provided Swagger interface.
     */
    @SuppressWarnings("unchecked")
    public static <A> A create(Class<A> swaggerInterface, HttpPipeline httpPipeline, SerializerAdapter<?> serializer) {
        return AzureProxy.create(swaggerInterface, null, httpPipeline, serializer);
    }

    /**
     * Create a proxy implementation of the provided Swagger interface.
     * @param swaggerInterface The Swagger interface to provide a proxy implementation for.
     * @param azureEnvironment The azure environment that the proxy implementation will target.
     * @param httpPipeline The HTTP httpPipeline will be used to make REST calls.
     * @param serializer The serializer that will be used to convert POJOs to and from request and
     *                   response bodies.
     * @param <A> The type of the Swagger interface.
     * @return A proxy implementation of the provided Swagger interface.
     */
    @SuppressWarnings("unchecked")
    public static <A> A create(Class<A> swaggerInterface, AzureEnvironment azureEnvironment, HttpPipeline httpPipeline, SerializerAdapter<?> serializer) {
        String baseUrl = null;

        if (azureEnvironment != null) {
            final AzureHost azureHost = swaggerInterface.getAnnotation(AzureHost.class);
            if (azureHost != null) {
                baseUrl = azureEnvironment.url(azureHost.endpoint());
            }
        }

        final SwaggerInterfaceParser interfaceParser = new SwaggerInterfaceParser(swaggerInterface, baseUrl);
        final AzureProxy azureProxy = new AzureProxy(httpPipeline, serializer, interfaceParser);
        return (A) Proxy.newProxyInstance(swaggerInterface.getClassLoader(), new Class[]{swaggerInterface}, azureProxy);
    }

    @Override
    protected Object handleAsyncHttpResponse(final HttpRequest httpRequest, Single<HttpResponse> asyncHttpResponse, final SwaggerMethodParser methodParser, Type returnType) {
        Object result;

        final TypeToken returnTypeToken = TypeToken.of(returnType);

        if (returnTypeToken.isSubtypeOf(Observable.class)) {
            final Type operationStatusType = ((ParameterizedType) returnType).getActualTypeArguments()[0];
            final TypeToken operationStatusTypeToken = TypeToken.of(operationStatusType);
            if (!operationStatusTypeToken.isSubtypeOf(OperationStatus.class)) {
                throw new InvalidReturnTypeException("AzureProxy only supports swagger interface methods that return Observable (such as " + methodParser.fullyQualifiedMethodName() + "()) if the Observable's inner type that is OperationStatus (not " + returnType.toString() + ").");
            }
            else {
                final Type operationStatusResultType = ((ParameterizedType) operationStatusType).getActualTypeArguments()[0];
                result = asyncHttpResponse.flatMapObservable(new Function<HttpResponse, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(HttpResponse httpResponse) throws Exception {
                        final HttpResponse bufferedHttpResponse = httpResponse.buffer();
                        return createPollStrategy(httpRequest, Single.just(bufferedHttpResponse), methodParser).flatMapObservable(new Function<PollStrategy, ObservableSource<OperationStatus<?>>>() {
                            @Override
                            public ObservableSource<OperationStatus<?>> apply(final PollStrategy pollStrategy) throws Exception {
                                Observable<OperationStatus<?>> first = handleBodyReturnTypeAsync(bufferedHttpResponse, methodParser, operationStatusResultType)
                                        .map(new Function<Object, OperationStatus<?>>() {
                                            @Override
                                            public OperationStatus<?> apply(Object operationResult) throws Exception {
                                                return new OperationStatus<>(operationResult, pollStrategy.status());
                                            }
                                        }).switchIfEmpty(Single.defer(new Callable<SingleSource<? extends OperationStatus<?>>>() {
                                            @Override
                                            public SingleSource<? extends OperationStatus<?>> call() throws Exception {
                                                return Single.just(new OperationStatus<>((Object) null, pollStrategy.status()));
                                            }
                                        })).toObservable();
                                Observable<OperationStatus<Object>> rest = pollStrategy.pollUntilDoneWithStatusUpdates(httpRequest, methodParser, operationStatusResultType);
                                return first.concatWith(rest);
                            }
                        });
                    }
                });
            }
        }
        else {
            final Single<HttpResponse> lastAsyncHttpResponse = createPollStrategy(httpRequest, asyncHttpResponse, methodParser)
                    .flatMap(new Function<PollStrategy, Single<HttpResponse>>() {
                        @Override
                        public Single<HttpResponse> apply(PollStrategy pollStrategy) {
                            return pollStrategy.pollUntilDone();
                        }
                    });
            result = handleRestReturnType(httpRequest, lastAsyncHttpResponse, methodParser, returnType);
        }

        return result;
    }

    private Single<PollStrategy> createPollStrategy(final HttpRequest originalHttpRequest, final Single<HttpResponse> asyncOriginalHttpResponse, final SwaggerMethodParser methodParser) {
        return asyncOriginalHttpResponse
                .flatMap(new Function<HttpResponse, Single<PollStrategy>>() {
                    @Override
                    public Single<PollStrategy> apply(final HttpResponse originalHttpResponse) {
                        final int httpStatusCode = originalHttpResponse.statusCode();
                        final int[] longRunningOperationStatusCodes = new int[] {200, 201, 202};
                        return ensureExpectedStatus(originalHttpResponse, methodParser, longRunningOperationStatusCodes)
                                .flatMap(new Function<HttpResponse, Single<? extends PollStrategy>>() {
                                    @Override
                                    public Single<? extends PollStrategy> apply(HttpResponse response) {
                                        Single<PollStrategy> result = null;

                                        final Long parsedDelayInMilliseconds = PollStrategy.delayInMillisecondsFrom(originalHttpResponse);
                                        final long delayInMilliseconds = parsedDelayInMilliseconds != null ? parsedDelayInMilliseconds : AzureProxy.defaultDelayInMilliseconds();

                                        final HttpMethod originalHttpRequestMethod = originalHttpRequest.httpMethod();

                                        PollStrategy pollStrategy = null;
                                        if (httpStatusCode == 200) {
                                            pollStrategy = AzureAsyncOperationPollStrategy.tryToCreate(AzureProxy.this, methodParser, originalHttpRequest, originalHttpResponse, delayInMilliseconds);
                                            if (pollStrategy != null) {
                                                result = Single.just(pollStrategy);
                                            }
                                            else {
                                                result = createProvisioningStateOrCompletedPollStrategy(originalHttpRequest, originalHttpResponse, methodParser, delayInMilliseconds);
                                            }
                                        }
                                        else if (originalHttpRequestMethod == HttpMethod.PUT || originalHttpRequestMethod == HttpMethod.PATCH) {
                                            if (httpStatusCode == 201) {
                                                pollStrategy = AzureAsyncOperationPollStrategy.tryToCreate(AzureProxy.this, methodParser, originalHttpRequest, originalHttpResponse, delayInMilliseconds);
                                                if (pollStrategy == null) {
                                                    result = createProvisioningStateOrCompletedPollStrategy(originalHttpRequest, originalHttpResponse, methodParser, delayInMilliseconds);
                                                }
                                            } else if (httpStatusCode == 202) {
                                                pollStrategy = AzureAsyncOperationPollStrategy.tryToCreate(AzureProxy.this, methodParser, originalHttpRequest, originalHttpResponse, delayInMilliseconds);
                                                if (pollStrategy == null) {
                                                    pollStrategy = LocationPollStrategy.tryToCreate(AzureProxy.this, methodParser, originalHttpRequest, originalHttpResponse, delayInMilliseconds);
                                                }
                                            }
                                        }
                                        else {
                                            if (httpStatusCode == 202) {
                                                pollStrategy = AzureAsyncOperationPollStrategy.tryToCreate(AzureProxy.this, methodParser, originalHttpRequest, originalHttpResponse, delayInMilliseconds);
                                                if (pollStrategy == null) {
                                                    pollStrategy = LocationPollStrategy.tryToCreate(AzureProxy.this, methodParser, originalHttpRequest, originalHttpResponse, delayInMilliseconds);
                                                    if (pollStrategy == null) {
                                                        throw new CloudException("Response does not contain an Azure-AsyncOperation or Location header.", originalHttpResponse);
                                                    }
                                                }
                                            }
                                        }

                                        if (pollStrategy == null && result == null) {
                                            pollStrategy = new CompletedPollStrategy(AzureProxy.this, methodParser, originalHttpResponse);
                                        }

                                        if (pollStrategy != null) {
                                            result = Single.just(pollStrategy);
                                        }

                                        return result;
                                    }
                                });
                    }
                });
    }

    private Single<PollStrategy> createProvisioningStateOrCompletedPollStrategy(final HttpRequest httpRequest, HttpResponse httpResponse, final SwaggerMethodParser methodParser, final long delayInMilliseconds) {
        Single<PollStrategy> result;

        final HttpMethod httpRequestMethod = httpRequest.httpMethod();
        if (httpRequestMethod == HttpMethod.DELETE
                || httpRequestMethod == HttpMethod.GET
                || httpRequestMethod == HttpMethod.HEAD
                || !methodParser.expectsResponseBody()) {
            result = Single.<PollStrategy>just(new CompletedPollStrategy(AzureProxy.this, methodParser, httpResponse));
        } else {
            final HttpResponse bufferedOriginalHttpResponse = httpResponse.buffer();
            result = bufferedOriginalHttpResponse.bodyAsStringAsync()
                    .map(new Function<String, PollStrategy>() {
                        @Override
                        public PollStrategy apply(String originalHttpResponseBody) {
                            if (originalHttpResponseBody == null || originalHttpResponseBody.isEmpty()) {
                                throw new CloudException("The HTTP response does not contain a body.", bufferedOriginalHttpResponse);
                            }

                            PollStrategy result;
                            try {
                                final SerializerAdapter<?> serializer = serializer();
                                final ResourceWithProvisioningState resource = serializer.deserialize(originalHttpResponseBody, ResourceWithProvisioningState.class, SerializerAdapter.Encoding.JSON);
                                if (resource != null && resource.properties() != null && !OperationState.isCompleted(resource.properties().provisioningState())) {
                                    result = new ProvisioningStatePollStrategy(AzureProxy.this, methodParser, httpRequest, resource.properties().provisioningState(), delayInMilliseconds);
                                } else {
                                    result = new CompletedPollStrategy(AzureProxy.this, methodParser, bufferedOriginalHttpResponse);
                                }
                            } catch (IOException e) {
                                throw Exceptions.propagate(e);
                            }

                            return result;
                        }
                    });
        }

        return result;
    }
}