// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.implementation;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.CookiePolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.http.swagger.OperationDescription;
import com.azure.core.http.swagger.RestProxy;
import com.azure.core.http.swagger.SwaggerInterfaceParser;
import com.azure.core.http.swagger.SwaggerMethodParser;
import com.azure.core.implementation.exception.InvalidReturnTypeException;
import com.azure.core.implementation.serializer.HttpResponseDecoder;
import com.azure.core.implementation.serializer.HttpResponseDecoder.HttpDecodedResponse;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.core.implementation.util.TypeUtil;
import com.azure.core.management.CloudException;
import com.azure.core.management.OperationState;
import com.azure.core.management.annotations.AzureHost;
import com.azure.core.management.serializer.AzureJacksonAdapter;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
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
 * This class can be used to create an Azure specific proxy implementation for a provided Swagger generated interface.
 */
public final class AzureProxy extends RestProxy {
    private static long defaultPollingDelayInMilliseconds = 30 * 1000;
    private final ClientLogger logger = new ClientLogger(AzureProxy.class);

    /**
     * Create a new instance of RestProxy.
     *
     * @param httpPipeline The HttpPipeline that will be used by this AzureProxy to send HttpRequests.
     * @param interfaceParser The parser that contains information about the swagger interface that this RestProxy
     * "implements".
     */
    private AzureProxy(HttpPipeline httpPipeline, SwaggerInterfaceParser interfaceParser) {
        super(httpPipeline, createDefaultSerializer(), interfaceParser);
    }

    /**
     * @return The millisecond delay that will occur by default between long running operation polls.
     */
    public static long getDefaultDelayInMilliseconds() {
        return AzureProxy.defaultPollingDelayInMilliseconds;
    }

    /**
     * Set the millisecond delay that will occur by default between long running operation polls.
     *
     * @param defaultPollingDelayInMilliseconds The number of milliseconds to delay before sending the next long running
     * operation status poll.
     */
    public static void setDefaultPollingDelayInMilliseconds(long defaultPollingDelayInMilliseconds) {
        AzureProxy.defaultPollingDelayInMilliseconds = defaultPollingDelayInMilliseconds;
    }

    /**
     * Get the default serializer.
     *
     * @return the default serializer.
     */
    private static SerializerAdapter createDefaultSerializer() {
        return AzureJacksonAdapter.createDefaultSerializerAdapter();
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
                StringBuilder builder = new StringBuilder();
                for (byte b : hash) {
                    builder.append(String.format("%02x", b));
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
        final String packageImplementationVersion =
            swaggerInterface == null ? "" : "/" + swaggerInterface.getPackage().getImplementationVersion();
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
     *
     * @param swaggerInterface The interface that the pipeline will use to generate a user-agent string.
     * @return the default HttpPipeline.
     */
    public static HttpPipeline createDefaultPipeline(Class<?> swaggerInterface) {
        return createDefaultPipeline(swaggerInterface, (HttpPipelinePolicy) null);
    }

    /**
     * Create the default HttpPipeline.
     *
     * @param swaggerInterface The interface that the pipeline will use to generate a user-agent string.
     * @param credentials The credentials to use to apply authentication to the pipeline.
     * @return the default HttpPipeline.
     */
    public static HttpPipeline createDefaultPipeline(Class<?> swaggerInterface, TokenCredential credentials) {
        return createDefaultPipeline(swaggerInterface, new BearerTokenAuthenticationPolicy(credentials));
    }

    /**
     * Create the default HttpPipeline.
     *
     * @param swaggerInterface The interface that the pipeline will use to generate a user-agent string.
     * @param credentialsPolicy The credentials policy factory to use to apply authentication to the pipeline.
     * @return the default HttpPipeline.
     */
    public static HttpPipeline createDefaultPipeline(Class<?> swaggerInterface, HttpPipelinePolicy credentialsPolicy) {
        // Order in which policies applied will be the order in which they appear in the array
        //
        List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new UserAgentPolicy(getDefaultUserAgentString(swaggerInterface)));
        policies.add(new RetryPolicy());
        policies.add(new CookiePolicy());
        if (credentialsPolicy != null) {
            policies.add(credentialsPolicy);
        }

        return new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .build();
    }

    /**
     * Create a proxy implementation of the provided Swagger interface.
     *
     * @param swaggerInterface The Swagger interface to provide a proxy implementation for.
     * @param azureEnvironment The azure environment that the proxy implementation will target.
     * @param httpPipeline The HTTP httpPipeline will be used to make REST calls. //* @param serializer The serializer
     * that will be used to convert POJOs to and from request and response bodies.
     * @param <A> The type of the Swagger interface.
     * @return A proxy implementation of the provided Swagger interface.
     */
    @SuppressWarnings("unchecked")
    public static <A> A create(Class<A> swaggerInterface, AzureEnvironment azureEnvironment,
        HttpPipeline httpPipeline) {
        String baseUrl = null;

        if (azureEnvironment != null) {
            final AzureHost azureHost = swaggerInterface.getAnnotation(AzureHost.class);
            if (azureHost != null) {
                baseUrl = azureEnvironment.url(azureHost.endpoint());
            }
        }

        final SwaggerInterfaceParser interfaceParser =
            new SwaggerInterfaceParser(swaggerInterface, createDefaultSerializer(), baseUrl);
        final AzureProxy azureProxy = new AzureProxy(httpPipeline, interfaceParser);
        return (A) Proxy
            .newProxyInstance(swaggerInterface.getClassLoader(), new Class<?>[]{swaggerInterface}, azureProxy);
    }

    @Override
    protected Object handleHttpResponse(final HttpRequest httpRequest, Mono<HttpDecodedResponse> asyncHttpResponse,
        final SwaggerMethodParser methodParser, Type returnType, Context context) {
        if (TypeUtil.isTypeOrSubTypeOf(returnType, Flux.class)) {
            final Type operationStatusType = ((ParameterizedType) returnType).getActualTypeArguments()[0];
            if (!TypeUtil.isTypeOrSubTypeOf(operationStatusType, OperationStatus.class)) {
                throw logger.logExceptionAsError(new InvalidReturnTypeException("AzureProxy only supports swagger "
                    + "interface methods that return Flux (such as " + methodParser.getFullyQualifiedMethodName()
                    + "()) if the Flux's inner type that is OperationStatus (not " + returnType.toString() + ")."));
            } else {
                // Get ResultTypeT in OperationStatus<ResultTypeT>
                final Type operationStatusResultType =
                    ((ParameterizedType) operationStatusType).getActualTypeArguments()[0];
                //
                return asyncHttpResponse.flatMapMany(httpResponse -> createPollStrategy(httpRequest,
                    Mono.just(httpResponse), methodParser).flatMapMany(pollStrategy -> {
                        Mono<OperationStatus<Object>> first =
                            handleBodyReturnType(httpResponse, methodParser, operationStatusResultType)
                                .map(operationResult -> new OperationStatus<Object>(
                                    operationResult,
                                    pollStrategy.getStatus()))
                                .switchIfEmpty(
                                    Mono.defer((Supplier<Mono<OperationStatus<Object>>>) () -> Mono.just(
                                        new OperationStatus<>((Object) null, pollStrategy.getStatus()))));
                        Flux<OperationStatus<Object>> rest =
                            pollStrategy.pollUntilDoneWithStatusUpdates(httpRequest, methodParser,
                                operationStatusResultType, context);
                        return first.concatWith(rest);
                    }));
            }
        } else {
            final Mono<HttpResponse> lastAsyncHttpResponse =
                createPollStrategy(httpRequest, asyncHttpResponse, methodParser)
                    .flatMap((Function<PollStrategy, Mono<HttpResponse>>) PollStrategy::pollUntilDone);
            return handleRestReturnType(new HttpResponseDecoder(this.getSerializer()).decode(lastAsyncHttpResponse,
                methodParser), methodParser, returnType, context);
        }
    }

    @Override
    protected Object handleResumeOperation(final HttpRequest httpRequest, OperationDescription operationDescription,
        final SwaggerMethodParser methodParser, Type returnType, Context context) {
        final Type operationStatusType = ((ParameterizedType) returnType).getActualTypeArguments()[0];
        if (!TypeUtil.isTypeOrSubTypeOf(operationStatusType, OperationStatus.class)) {
            throw logger.logExceptionAsError(new InvalidReturnTypeException("AzureProxy only supports swagger "
                + "interface methods that return Flux (such as " + methodParser.getFullyQualifiedMethodName()
                + "()) if the Flux's inner type that is OperationStatus (not " + returnType.toString() + ")."));
        }

        PollStrategy.PollStrategyData pollStrategyData =
            (PollStrategy.PollStrategyData) operationDescription.getPollStrategyData();
        PollStrategy pollStrategy = pollStrategyData.initializeStrategy(this, methodParser);
        return pollStrategy.pollUntilDoneWithStatusUpdates(httpRequest, methodParser, operationStatusType, context);
    }

    private Mono<PollStrategy> createPollStrategy(final HttpRequest originalHttpRequest,
        final Mono<HttpDecodedResponse> asyncOriginalHttpDecodedResponse, final SwaggerMethodParser methodParser) {
        return asyncOriginalHttpDecodedResponse
            .flatMap((Function<HttpDecodedResponse, Mono<PollStrategy>>) originalHttpDecodedResponse -> {
                final int httpStatusCode = originalHttpDecodedResponse.getSourceResponse().getStatusCode();
                final HttpResponse originalHttpResponse = originalHttpDecodedResponse.getSourceResponse();
                final int[] longRunningOperationStatusCodes = new int[]{200, 201, 202};
                return ensureExpectedStatus(originalHttpDecodedResponse, methodParser, longRunningOperationStatusCodes)
                    .flatMap(response -> {
                        Mono<PollStrategy> result = null;

                        final Long parsedDelayInMilliseconds =
                            PollStrategy.delayInMillisecondsFrom(originalHttpResponse);
                        final long delayInMilliseconds =
                            parsedDelayInMilliseconds != null
                                ? parsedDelayInMilliseconds
                                : AzureProxy.getDefaultDelayInMilliseconds();

                        final HttpMethod originalHttpRequestMethod = originalHttpRequest.getHttpMethod();

                        PollStrategy pollStrategy = null;
                        if (httpStatusCode == 200) {
                            pollStrategy =
                                AzureAsyncOperationPollStrategy.tryToCreate(AzureProxy.this, methodParser,
                                    originalHttpRequest, originalHttpResponse, delayInMilliseconds);
                            if (pollStrategy != null) {
                                result = Mono.just(pollStrategy);
                            } else {
                                result =
                                    createProvisioningStateOrCompletedPollStrategy(originalHttpRequest,
                                        originalHttpResponse, methodParser, delayInMilliseconds);
                            }
                        } else if (originalHttpRequestMethod == HttpMethod.PUT
                            || originalHttpRequestMethod == HttpMethod.PATCH) {
                            if (httpStatusCode == 201) {
                                pollStrategy =
                                    AzureAsyncOperationPollStrategy.tryToCreate(AzureProxy.this, methodParser,
                                        originalHttpRequest, originalHttpResponse, delayInMilliseconds);
                                if (pollStrategy == null) {
                                    result =
                                        createProvisioningStateOrCompletedPollStrategy(originalHttpRequest,
                                            originalHttpResponse, methodParser, delayInMilliseconds);
                                }
                            } else if (httpStatusCode == 202) {
                                pollStrategy =
                                    AzureAsyncOperationPollStrategy.tryToCreate(AzureProxy.this, methodParser,
                                        originalHttpRequest, originalHttpResponse, delayInMilliseconds);
                                if (pollStrategy == null) {
                                    pollStrategy =
                                        LocationPollStrategy.tryToCreate(AzureProxy.this, methodParser,
                                            originalHttpRequest, originalHttpDecodedResponse.getSourceResponse(),
                                            delayInMilliseconds);
                                }
                            }
                        } else {
                            if (httpStatusCode == 202) {
                                pollStrategy =
                                    AzureAsyncOperationPollStrategy.tryToCreate(AzureProxy.this, methodParser,
                                        originalHttpRequest, originalHttpResponse, delayInMilliseconds);
                                if (pollStrategy == null) {
                                    pollStrategy =
                                        LocationPollStrategy.tryToCreate(AzureProxy.this, methodParser,
                                            originalHttpRequest, originalHttpResponse, delayInMilliseconds);
                                    if (pollStrategy == null) {
                                        throw logger.logExceptionAsError(new CloudException("Response does not "
                                            + "contain an Azure-AsyncOperation or Location header.",
                                            originalHttpResponse));
                                    }
                                }
                            }
                        }

                        if (pollStrategy == null && result == null) {
                            pollStrategy = new CompletedPollStrategy(
                                new CompletedPollStrategy.CompletedPollStrategyData(AzureProxy.this, methodParser,
                                    originalHttpResponse));
                        }

                        if (pollStrategy != null) {
                            result = Mono.just(pollStrategy);
                        }

                        return result;
                    });
            });
    }

    private Mono<PollStrategy> createProvisioningStateOrCompletedPollStrategy(final HttpRequest httpRequest,
        HttpResponse httpResponse, final SwaggerMethodParser methodParser, final long delayInMilliseconds) {
        Mono<PollStrategy> pollStrategyMono;

        final HttpMethod httpRequestMethod = httpRequest.getHttpMethod();
        if (httpRequestMethod == HttpMethod.DELETE
            || httpRequestMethod == HttpMethod.GET
            || httpRequestMethod == HttpMethod.HEAD
            || !methodParser.expectsResponseBody()) {
            pollStrategyMono = Mono.just(new CompletedPollStrategy(
                new CompletedPollStrategy.CompletedPollStrategyData(AzureProxy.this, methodParser, httpResponse)));
        } else {
            final HttpResponse bufferedOriginalHttpResponse = httpResponse.buffer();
            pollStrategyMono = bufferedOriginalHttpResponse.getBodyAsString()
                .map(originalHttpResponseBody -> {
                    if (originalHttpResponseBody == null || originalHttpResponseBody.isEmpty()) {
                        throw logger.logExceptionAsError(new CloudException(
                            "The HTTP response does not contain a body.", bufferedOriginalHttpResponse));
                    }
                    PollStrategy pollStrategy;
                    try {
                        final SerializerAdapter serializer = getSerializer();
                        final ResourceWithProvisioningState resource =
                            serializer.deserialize(originalHttpResponseBody, ResourceWithProvisioningState.class,
                                SerializerEncoding.JSON);
                        if (resource != null
                            && resource.getProperties() != null
                            && !OperationState.isCompleted(resource.getProperties().getProvisioningState())) {
                            pollStrategy = new ProvisioningStatePollStrategy(
                                new ProvisioningStatePollStrategy.ProvisioningStatePollStrategyData(
                                    AzureProxy.this, methodParser, httpRequest,
                                    resource.getProperties().getProvisioningState(), delayInMilliseconds));
                        } else {
                            pollStrategy = new CompletedPollStrategy(
                                new CompletedPollStrategy.CompletedPollStrategyData(
                                    AzureProxy.this, methodParser, bufferedOriginalHttpResponse));
                        }
                    } catch (IOException e) {
                        throw logger.logExceptionAsError(Exceptions.propagate(e));
                    }
                    return pollStrategy;
                });
        }
        return pollStrategyMono;
    }
}
