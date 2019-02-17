/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.v3;

import com.microsoft.azure.v3.annotations.AzureHost;
import com.microsoft.azure.v3.policy.AsyncCredentialsPolicy;
import com.microsoft.azure.v3.serializer.AzureJacksonAdapter;
import com.microsoft.rest.v3.InvalidReturnTypeException;
import com.microsoft.rest.v3.OperationDescription;
import com.microsoft.rest.v3.RestProxy;
import com.microsoft.rest.v3.SwaggerInterfaceParser;
import com.microsoft.rest.v3.SwaggerMethodParser;
import com.microsoft.azure.v3.credentials.AsyncServiceClientCredentials;
import com.microsoft.rest.v3.credentials.ServiceClientCredentials;
import com.microsoft.rest.v3.http.HttpMethod;
import com.microsoft.rest.v3.http.HttpPipeline;
import com.microsoft.rest.v3.http.policy.HttpPipelinePolicy;
import com.microsoft.rest.v3.http.HttpRequest;
import com.microsoft.rest.v3.http.HttpResponse;
import com.microsoft.rest.v3.http.policy.CookiePolicy;
import com.microsoft.rest.v3.http.policy.CredentialsPolicy;
import com.microsoft.rest.v3.http.policy.DecodingPolicy;
import com.microsoft.rest.v3.http.policy.RetryPolicy;
import com.microsoft.rest.v3.http.policy.UserAgentPolicy;
import com.microsoft.rest.v3.serializer.SerializerAdapter;
import com.microsoft.rest.v3.serializer.SerializerEncoding;
import com.microsoft.rest.v3.util.TypeUtil;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

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
    private AzureProxy(HttpPipeline httpPipeline, SerializerAdapter serializer, SwaggerInterfaceParser interfaceParser) {
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
    public static SerializerAdapter createDefaultSerializer() {
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

                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(macBytes);
                StringBuffer builder = new StringBuffer();
                for (int i = 0; i < hash.length; i++) {
                    builder.append(String.format("%02x", hash[i]));
                }
                macAddressHash = builder.toString();
            } catch (Throwable t) {
                // It's okay ignore mac address hash telemetry
            }

            if (macBytes == null) {
                macAddressHash = "Unknown";
            }

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

    private static String getDefaultUserAgentString(Class<?> swaggerInterface) {
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
        return createDefaultPipeline(swaggerInterface, (HttpPipelinePolicy) null);
    }

    /**
     * Create the default HttpPipeline.
     * @param swaggerInterface The interface that the pipeline will use to generate a user-agent
     *                         string.
     * @param credentials The credentials to use to apply authentication to the pipeline.
     * @return the default HttpPipeline.
     */
    public static HttpPipeline createDefaultPipeline(Class<?> swaggerInterface, ServiceClientCredentials credentials) {
        return createDefaultPipeline(swaggerInterface, new CredentialsPolicy(credentials));
    }

    /**
     * Create the default HttpPipeline.
     * @param swaggerInterface The interface that the pipeline will use to generate a user-agent
     *                         string.
     * @param credentials The credentials to use to apply authentication to the pipeline.
     * @return the default HttpPipeline.
     */
    public static HttpPipeline createDefaultPipeline(Class<?> swaggerInterface, AsyncServiceClientCredentials credentials) {
        return createDefaultPipeline(swaggerInterface, new AsyncCredentialsPolicy(credentials));
    }

    /**
     * Create the default HttpPipeline.
     * @param swaggerInterface The interface that the pipeline will use to generate a user-agent
     *                         string.
     * @param credentialsPolicy The credentials policy factory to use to apply authentication to the
     *                          pipeline.
     * @return the default HttpPipeline.
     */
    public static HttpPipeline createDefaultPipeline(Class<?> swaggerInterface, HttpPipelinePolicy credentialsPolicy) {
        // Order in which policies applied will be the order in which they appear in the array
        //
        List<HttpPipelinePolicy> policies = new ArrayList<HttpPipelinePolicy>();
        policies.add(new UserAgentPolicy(getDefaultUserAgentString(swaggerInterface)));
        policies.add(new RetryPolicy());
        policies.add(new DecodingPolicy());
        policies.add(new CookiePolicy());
        if (credentialsPolicy != null) {
            policies.add(credentialsPolicy);
        }
        return new HttpPipeline(policies.toArray(new HttpPipelinePolicy[policies.size()]));
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
    public static <A> A create(Class<A> swaggerInterface, HttpPipeline httpPipeline, SerializerAdapter serializer) {
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
    public static <A> A create(Class<A> swaggerInterface, AzureEnvironment azureEnvironment, HttpPipeline httpPipeline, SerializerAdapter serializer) {
        String baseUrl = null;

        if (azureEnvironment != null) {
            final AzureHost azureHost = swaggerInterface.getAnnotation(AzureHost.class);
            if (azureHost != null) {
                baseUrl = azureEnvironment.url(azureHost.endpoint());
            }
        }

        final SwaggerInterfaceParser interfaceParser = new SwaggerInterfaceParser(swaggerInterface, serializer, baseUrl);
        final AzureProxy azureProxy = new AzureProxy(httpPipeline, serializer, interfaceParser);
        return (A) Proxy.newProxyInstance(swaggerInterface.getClassLoader(), new Class[]{swaggerInterface}, azureProxy);
    }

    @Override
    protected Object handleHttpResponse(final HttpRequest httpRequest, Mono<HttpResponse> asyncHttpResponse, final SwaggerMethodParser methodParser, Type returnType) {
        //
        if (TypeUtil.isTypeOrSubTypeOf(returnType, Flux.class)) {
            final Type operationStatusType = ((ParameterizedType) returnType).getActualTypeArguments()[0];
            if (!TypeUtil.isTypeOrSubTypeOf(operationStatusType, OperationStatus.class)) {
                throw new InvalidReturnTypeException("AzureProxy only supports swagger interface methods that return Flux (such as " + methodParser.fullyQualifiedMethodName() + "()) if the Flux's inner type that is OperationStatus (not " + returnType.toString() + ").");
            } else {
                final Type operationStatusResultType = ((ParameterizedType) operationStatusType).getActualTypeArguments()[0];
                //
                return asyncHttpResponse.flatMapMany(httpResponse -> {
                    final HttpResponse bufferedHttpResponse = httpResponse.buffer();
                    return createPollStrategy(httpRequest, Mono.just(bufferedHttpResponse), methodParser)
                            .flatMapMany(pollStrategy -> {
                                Mono<OperationStatus<Object>> first = handleBodyReturnType(bufferedHttpResponse, methodParser, operationStatusResultType)
                                        .map(operationResult -> new OperationStatus<Object>(operationResult, pollStrategy.status()))
                                        .switchIfEmpty(Mono.defer((Supplier<Mono<OperationStatus<Object>>>) () -> Mono.just(new OperationStatus<Object>((Object) null, pollStrategy.status()))));
                                Flux<OperationStatus<Object>> rest = pollStrategy.pollUntilDoneWithStatusUpdates(httpRequest, methodParser, operationStatusResultType);
                                return first.concatWith(rest);
                            });
                });
            }
        } else {
            final Mono<HttpResponse> lastAsyncHttpResponse = createPollStrategy(httpRequest, asyncHttpResponse, methodParser)
                    .flatMap((Function<PollStrategy, Mono<HttpResponse>>) pollStrategy -> pollStrategy.pollUntilDone());
            return handleRestReturnType(httpRequest, lastAsyncHttpResponse, methodParser, returnType);
        }
    }

    @Override
    protected Object handleResumeOperation(final HttpRequest httpRequest,
                                           OperationDescription operationDescription,
                                           final SwaggerMethodParser methodParser,
                                           Type returnType) {
        final Type operationStatusType = ((ParameterizedType) returnType).getActualTypeArguments()[0];
        if (!TypeUtil.isTypeOrSubTypeOf(operationStatusType, OperationStatus.class)) {
            throw new InvalidReturnTypeException("AzureProxy only supports swagger interface methods that return Flux (such as " + methodParser.fullyQualifiedMethodName() + "()) if the Flux's inner type that is OperationStatus (not " + returnType.toString() + ").");
        }

        PollStrategy.PollStrategyData pollStrategyData =
                (PollStrategy.PollStrategyData) operationDescription.pollStrategyData();
        PollStrategy pollStrategy = pollStrategyData.initializeStrategy(this, methodParser);
        return pollStrategy.pollUntilDoneWithStatusUpdates(httpRequest, methodParser, operationStatusType);
    }

    private Mono<PollStrategy> createPollStrategy(final HttpRequest originalHttpRequest, final Mono<HttpResponse> asyncOriginalHttpResponse, final SwaggerMethodParser methodParser) {
        return asyncOriginalHttpResponse
                .flatMap((Function<HttpResponse, Mono<PollStrategy>>) originalHttpResponse -> {
                    final int httpStatusCode = originalHttpResponse.statusCode();
                    final int[] longRunningOperationStatusCodes = new int[] {200, 201, 202};
                    return ensureExpectedStatus(originalHttpResponse, methodParser, longRunningOperationStatusCodes)
                            .flatMap(response -> {
                                Mono<PollStrategy> result = null;

                                final Long parsedDelayInMilliseconds = PollStrategy.delayInMillisecondsFrom(originalHttpResponse);
                                final long delayInMilliseconds = parsedDelayInMilliseconds != null ? parsedDelayInMilliseconds : AzureProxy.defaultDelayInMilliseconds();

                                final HttpMethod originalHttpRequestMethod = originalHttpRequest.httpMethod();

                                PollStrategy pollStrategy = null;
                                if (httpStatusCode == 200) {
                                    pollStrategy = AzureAsyncOperationPollStrategy.tryToCreate(AzureProxy.this, methodParser, originalHttpRequest, originalHttpResponse, delayInMilliseconds);
                                    if (pollStrategy != null) {
                                        result = Mono.just(pollStrategy);
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
                                    pollStrategy = new CompletedPollStrategy(
                                            new CompletedPollStrategy.CompletedPollStrategyData(AzureProxy.this, methodParser, originalHttpResponse));
                                }

                                if (pollStrategy != null) {
                                    result = Mono.just(pollStrategy);
                                }

                                return result;
                            });
                });
    }

    private Mono<PollStrategy> createProvisioningStateOrCompletedPollStrategy(final HttpRequest httpRequest, HttpResponse httpResponse, final SwaggerMethodParser methodParser, final long delayInMilliseconds) {
        Mono<PollStrategy> pollStrategyMono;

        final HttpMethod httpRequestMethod = httpRequest.httpMethod();
        if (httpRequestMethod == HttpMethod.DELETE
                || httpRequestMethod == HttpMethod.GET
                || httpRequestMethod == HttpMethod.HEAD
                || !methodParser.expectsResponseBody()) {
            pollStrategyMono = Mono.<PollStrategy>just(new CompletedPollStrategy(
                    new CompletedPollStrategy.CompletedPollStrategyData(AzureProxy.this, methodParser, httpResponse)));
        } else {
            final HttpResponse bufferedOriginalHttpResponse = httpResponse.buffer();
            pollStrategyMono = bufferedOriginalHttpResponse.bodyAsString()
                    .map(originalHttpResponseBody -> {
                        if (originalHttpResponseBody == null || originalHttpResponseBody.isEmpty()) {
                            throw new CloudException("The HTTP response does not contain a body.", bufferedOriginalHttpResponse);
                        }
                        PollStrategy pollStrategy;
                        try {
                            final SerializerAdapter serializer = serializer();
                            final ResourceWithProvisioningState resource = serializer.deserialize(originalHttpResponseBody, ResourceWithProvisioningState.class, SerializerEncoding.JSON);
                            if (resource != null && resource.properties() != null && !OperationState.isCompleted(resource.properties().provisioningState())) {
                                pollStrategy = new ProvisioningStatePollStrategy(
                                        new ProvisioningStatePollStrategy.ProvisioningStatePollStrategyData(
                                                AzureProxy.this, methodParser, httpRequest, resource.properties().provisioningState(), delayInMilliseconds));
                            } else {
                                pollStrategy = new CompletedPollStrategy(
                                        new CompletedPollStrategy.CompletedPollStrategyData(
                                                AzureProxy.this, methodParser, bufferedOriginalHttpResponse));
                            }
                        } catch (IOException e) {
                            throw Exceptions.propagate(e);
                        }
                        return pollStrategy;
                    });
        }
        return pollStrategyMono;
    }
}