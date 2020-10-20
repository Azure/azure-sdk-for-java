// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation;

import com.azure.ai.formrecognizer.FormRecognizerServiceVersion;
import com.azure.ai.formrecognizer.implementation.models.ContentType;
import com.azure.ai.formrecognizer.implementation.models.ErrorResponseException;
import com.azure.ai.formrecognizer.models.FormRecognizerErrorInformation;
import com.azure.ai.formrecognizer.models.FormRecognizerOperationResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.AddHeadersFromContextPolicy;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.AzureKeyCredentialPolicy;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.PollingContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.azure.core.http.ContentType.APPLICATION_JSON;
import static com.azure.core.util.FluxUtil.monoError;

/**
 * Utility method class.
 */
public final class Utility {
    private static final ClientLogger LOGGER = new ClientLogger(Utility.class);
    // using 4K as default buffer size: https://stackoverflow.com/a/237495/1473510
    private static final int BYTE_BUFFER_CHUNK_SIZE = 4096;
    // default time interval for polling
    public static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(5);
    private static final RetryPolicy DEFAULT_RETRY_POLICY = new RetryPolicy("retry-after-ms", ChronoUnit.MILLIS);
    private static final HttpHeaders HTTP_HEADERS = new HttpHeaders()
            .put("x-ms-return-client-request-id", "true")
            .put("Accept", APPLICATION_JSON);
    private static final String DEFAULT_SCOPE = "https://cognitiveservices.azure.com/.default";
    private static final String OCP_APIM_SUBSCRIPTION_KEY = "Ocp-Apim-Subscription-Key";

    private static final String CLIENT_NAME;
    private static final String CLIENT_VERSION;

    static {
        Map<String, String> properties = CoreUtils.getProperties("azure-search-documents.properties");
        CLIENT_NAME = properties.getOrDefault("name", "UnknownName");
        CLIENT_VERSION = properties.getOrDefault("version", "UnknownVersion");
    }

    private Utility() {
    }

    /**
     * Automatically detect byte buffer's content type.
     * <p>
     * Given the source: <a href="https://en.wikipedia.org/wiki/Magic_number_(programming)#Magic_numbers_in_files"></a>.
     *
     * @param buffer The byte buffer input.
     *
     * @return The {@link Mono} of {@link ContentType} content type.
     */
    public static Mono<ContentType> detectContentType(Flux<ByteBuffer> buffer) {
        byte[] header = new byte[4];
        int[] written = new int[]{0};
        ContentType[] contentType = {ContentType.fromString("none")};
        return buffer.map(chunk -> {
            final int len = chunk.remaining();
            for (int i = 0; i < len; i++) {
                header[written[0]] = chunk.get(i);
                written[0]++;

                if (written[0] == 4) {
                    if (isJpeg(header)) {
                        contentType[0] = ContentType.IMAGE_JPEG;
                    } else if (isPdf(header)) {
                        contentType[0] = ContentType.APPLICATION_PDF;
                    } else if (isPng(header)) {
                        contentType[0] = ContentType.IMAGE_PNG;
                    } else if (isTiff(header)) {
                        contentType[0] = ContentType.IMAGE_TIFF;
                    }
                    // Got a four bytes matching or not, either way no need to read more byte return false
                    // so that takeWhile can cut the subscription on data
                    return false;
                }
            }
            // current chunk don't have enough bytes so return true to get next Chunk if there is one.
            return true;
        })
            .takeWhile(doContinue -> doContinue)
            .then(Mono.defer(() -> {
                if (contentType[0] != null) {
                    return Mono.just(contentType[0]);
                } else {
                    return Mono.error(new RuntimeException("Content type could not be detected. "
                        + "Should use other overload API that takes content type."));
                }
            }));
    }

    public static FormRecognizerClientImpl getFormRecognizerRestClient(String endpoint,
        FormRecognizerServiceVersion version, HttpPipeline httpPipeline, Configuration configuration,
        RetryPolicy retryPolicy, TokenCredential tokenCredential, AzureKeyCredential apiKeyCredential,
        ClientOptions clientOptions, HttpLogOptions httpLogOptions,  List<HttpPipelinePolicy> perCallPolicies,
        List<HttpPipelinePolicy> perRetryPolicies, HttpClient httpClient) {
        Objects.requireNonNull(endpoint, "'endpoint' cannot be null.");

        // Service Version
        // final FormRecognizerServiceVersion serviceVersion =
        //     version != null ? version : FormRecognizerServiceVersion.getLatest();

        HttpPipeline pipeline = httpPipeline;

        // Create a default Pipeline if it is not given
        if (pipeline == null) {
            pipeline = getDefaultHttpPipeline(configuration,
                retryPolicy,
                tokenCredential,
                apiKeyCredential,
                clientOptions,
                httpLogOptions,
                perCallPolicies,
                perRetryPolicies,
                httpClient);
        }

        return new FormRecognizerClientImplBuilder()
            .endpoint(endpoint)
            // .apiVersion(serviceVersion.getVersion())
            .pipeline(pipeline)
            .buildClient();
    }

    private static HttpPipeline getDefaultHttpPipeline(Configuration configuration, RetryPolicy retryPolicy,
        TokenCredential tokenCredential, AzureKeyCredential apiKeyCredential, ClientOptions clientOptions,
        HttpLogOptions httpLogOptions,  List<HttpPipelinePolicy> perCallPolicies,
        List<HttpPipelinePolicy> perRetryPolicies, HttpClient httpClient) {
        // Global Env configuration store
        final Configuration buildConfiguration = (configuration == null)
            ? Configuration.getGlobalConfiguration().clone() : configuration;

        ClientOptions buildClientOptions = (clientOptions == null) ? new ClientOptions() : clientOptions;
        HttpLogOptions buildLogOptions = (httpLogOptions == null) ? new HttpLogOptions() : httpLogOptions;

        String applicationId = null;
        if (!CoreUtils.isNullOrEmpty(buildClientOptions.getApplicationId())) {
            applicationId = buildClientOptions.getApplicationId();
        } else if (!CoreUtils.isNullOrEmpty(buildLogOptions.getApplicationId())) {
            applicationId = buildLogOptions.getApplicationId();
        }

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();

        policies.add(new RequestIdPolicy());
        policies.add(new UserAgentPolicy(applicationId, CLIENT_NAME, CLIENT_VERSION, buildConfiguration));
        policies.addAll(perCallPolicies);

        HttpPolicyProviders.addBeforeRetryPolicies(policies);

        policies.add(retryPolicy == null ? DEFAULT_RETRY_POLICY : retryPolicy);
        policies.add(new AddDatePolicy());
        buildClientOptions.getHeaders().forEach(header -> HTTP_HEADERS.put(header.getName(), header.getValue()));
        policies.add(new AddHeadersFromContextPolicy());
        policies.add(new AddHeadersPolicy(HTTP_HEADERS));

        // Authentications
        if (tokenCredential != null) {
            policies.add(new BearerTokenAuthenticationPolicy(tokenCredential, DEFAULT_SCOPE));
        } else if (apiKeyCredential != null) {
            policies.add(new AzureKeyCredentialPolicy(OCP_APIM_SUBSCRIPTION_KEY, apiKeyCredential));
        }

        policies.addAll(perRetryPolicies);

        HttpPolicyProviders.addAfterRetryPolicies(policies);

        policies.add(new HttpLoggingPolicy(httpLogOptions));

        return new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();
    }

    private static boolean isJpeg(byte[] header) {
        return (header[0] == (byte) 0xff && header[1] == (byte) 0xd8);
    }

    private static boolean isPdf(byte[] header) {
        return header[0] == (byte) 0x25
            && header[1] == (byte) 0x50
            && header[2] == (byte) 0x44
            && header[3] == (byte) 0x46;
    }

    private static boolean isPng(byte[] header) {
        return header[0] == (byte) 0x89
            && header[1] == (byte) 0x50
            && header[2] == (byte) 0x4e
            && header[3] == (byte) 0x47;
    }

    private static boolean isTiff(byte[] header) {
        return (header[0] == (byte) 0x49
            && header[1] == (byte) 0x49
            && header[2] == (byte) 0x2a
            && header[3] == (byte) 0x0)
            // big-endian
            || (header[0] == (byte) 0x4d
            && header[1] == (byte) 0x4d
            && header[2] == (byte) 0x0
            && header[3] == (byte) 0x2a);
    }

    /**
     * Creates a Flux of ByteBuffer, with each ByteBuffer wrapping bytes read from the given
     * InputStream.
     *
     * @param inputStream InputStream to back the Flux
     *
     * @return Flux of ByteBuffer backed by the InputStream
     */
    public static Flux<ByteBuffer> toFluxByteBuffer(InputStream inputStream) {
        Pair pair = new Pair();
        return Flux.just(true)
            .repeat()
            .map(ignore -> {
                byte[] buffer = new byte[BYTE_BUFFER_CHUNK_SIZE];
                try {
                    int numBytes = inputStream.read(buffer);
                    if (numBytes > 0) {
                        return pair.buffer(ByteBuffer.wrap(buffer, 0, numBytes)).readBytes(numBytes);
                    } else {
                        return pair.buffer(null).readBytes(numBytes);
                    }
                } catch (IOException ioe) {
                    throw LOGGER.logExceptionAsError(new RuntimeException(ioe));
                }
            })
            .takeUntil(p -> p.readBytes() == -1)
            .filter(p -> p.readBytes() > 0)
            .map(Pair::buffer)
            .cache();
    }

    /**
     * Extracts the result ID from the URL.
     *
     * @param operationLocation The URL specified in the 'Operation-Location' response header containing the
     * resultId used to track the progress and obtain the result of the analyze operation.
     *
     * @return The resultId used to track the progress.
     */
    public static String parseModelId(String operationLocation) {
        if (!CoreUtils.isNullOrEmpty(operationLocation)) {
            int lastIndex = operationLocation.lastIndexOf('/');
            if (lastIndex != -1) {
                return operationLocation.substring(lastIndex + 1);
            }
        }
        throw LOGGER.logExceptionAsError(
            new RuntimeException("Failed to parse operation header for result Id from: " + operationLocation));
    }

    /**
     * Given an iterable will apply the indexing function to it and return the index and each item of the iterable.
     *
     * @param iterable the list to apply the mapping function to.
     * @param biConsumer the function which accepts the index and the each value of the iterable.
     * @param <T> the type of items being returned.
     */
    public static <T> void forEachWithIndex(Iterable<T> iterable, BiConsumer<Integer, T> biConsumer) {
        int[] index = new int[]{0};
        iterable.forEach(element -> biConsumer.accept(index[0]++, element));
    }

    private static class Pair {
        private ByteBuffer byteBuffer;
        private int readBytes;

        ByteBuffer buffer() {
            return this.byteBuffer;
        }

        int readBytes() {
            return this.readBytes;
        }

        Pair buffer(ByteBuffer byteBuffer) {
            this.byteBuffer = byteBuffer;
            return this;
        }

        Pair readBytes(int cnt) {
            this.readBytes = cnt;
            return this;
        }
    }

    /**
     * Mapping a {@link ErrorResponseException} to {@link HttpResponseException} if exist. Otherwise, return
     * original {@link Throwable}.
     *
     * @param throwable A {@link Throwable}.
     * @return A {@link HttpResponseException} or the original throwable type.
     */
    public static Throwable mapToHttpResponseExceptionIfExist(Throwable throwable) {
        if (throwable instanceof ErrorResponseException) {
            ErrorResponseException errorResponseException = (ErrorResponseException) throwable;
            return new HttpResponseException(errorResponseException.getMessage(), errorResponseException.getResponse(),
                new FormRecognizerErrorInformation(errorResponseException.getValue().getError().getCode(),
                    errorResponseException.getValue().getError().getMessage()));
        }
        return throwable;
    }

    /*
     * Poller's ACTIVATION operation that takes URL as input.
     */
    public static Function<PollingContext<FormRecognizerOperationResult>, Mono<FormRecognizerOperationResult>>
        urlActivationOperation(
        Supplier<Mono<FormRecognizerOperationResult>> activationOperation, ClientLogger logger) {
        return pollingContext -> {
            try {
                return activationOperation.get().onErrorMap(Utility::mapToHttpResponseExceptionIfExist);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }
}
