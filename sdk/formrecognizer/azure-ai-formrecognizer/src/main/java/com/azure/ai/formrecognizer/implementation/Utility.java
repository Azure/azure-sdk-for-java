// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation;

import com.azure.ai.formrecognizer.implementation.models.ContentType;
import com.azure.ai.formrecognizer.implementation.models.ErrorInformation;
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
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.PollingContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.azure.core.util.FluxUtil.monoError;

/**
 * Utility method class.
 */
public final class Utility {
    private static final ClientLogger LOGGER = new ClientLogger(Utility.class);

    private static final String DEFAULT_SCOPE = "https://cognitiveservices.azure.com/.default";
    private static final String FORM_RECOGNIZER_PROPERTIES = "azure-ai-formrecognizer.properties";
    private static final String NAME = "name";
    private static final String OCP_APIM_SUBSCRIPTION_KEY = "Ocp-Apim-Subscription-Key";
    private static final String VERSION = "version";

    private static final ClientOptions DEFAULT_CLIENT_OPTIONS = new ClientOptions();
    private static final HttpHeaders DEFAULT_HTTP_HEADERS = new HttpHeaders();
    private static final HttpLogOptions DEFAULT_LOG_OPTIONS = new HttpLogOptions();

    private static final String CLIENT_NAME;
    private static final String CLIENT_VERSION;
    static {
        Map<String, String> properties = CoreUtils.getProperties(FORM_RECOGNIZER_PROPERTIES);
        CLIENT_NAME = properties.getOrDefault(NAME, "UnknownName");
        CLIENT_VERSION = properties.getOrDefault(VERSION, "UnknownVersion");
    }
    
    public static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(5);

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
                    } else if (isBmp(header)) {
                        contentType[0] = ContentType.IMAGE_BMP;
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

    private static boolean isBmp(byte[] header) {
        return (header[0] == (byte) 0x42 && header[1] == (byte) 0x4D);
    }

    /**
     * Creates a Flux of ByteBuffer, with each ByteBuffer wrapping bytes read from the given
     * InputStream.
     *
     * @param inputStream InputStream to back the Flux
     *
     * @return Flux of ByteBuffer backed by the InputStream
     * @throws NullPointerException If {@code inputStream} is null.
     */
    public static Flux<ByteBuffer> toFluxByteBuffer(InputStream inputStream) {
        Objects.requireNonNull(inputStream, "'inputStream' is required and cannot be null.");
        return FluxUtil
            .toFluxByteBuffer(inputStream)
            .cache()
            .map(ByteBuffer::duplicate);
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

    /**
     * Mapping a {@link ErrorResponseException} to {@link HttpResponseException} if exist. Otherwise, return
     * original {@link Throwable}.
     *
     * @param throwable A {@link Throwable}.
     * @return A {@link HttpResponseException} or the original throwable type.
     */
    public static Throwable mapToHttpResponseExceptionIfExists(Throwable throwable) {
        if (throwable instanceof ErrorResponseException) {
            ErrorResponseException errorResponseException = (ErrorResponseException) throwable;
            FormRecognizerErrorInformation formRecognizerErrorInformation = null;
            if (errorResponseException.getValue() != null && errorResponseException.getValue().getError() != null) {
                ErrorInformation errorInformation = errorResponseException.getValue().getError();
                formRecognizerErrorInformation =
                    new FormRecognizerErrorInformation(errorInformation.getCode(), errorInformation.getMessage());
            }
            return new HttpResponseException(
                errorResponseException.getMessage(),
                errorResponseException.getResponse(),
                formRecognizerErrorInformation
            );
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
                return activationOperation.get().onErrorMap(Utility::mapToHttpResponseExceptionIfExists);
            } catch (RuntimeException ex) {
                return monoError(logger, ex);
            }
        };
    }

    public static HttpPipeline buildHttpPipeline(ClientOptions clientOptions, HttpLogOptions logOptions,
        Configuration configuration, RetryPolicy retryPolicy, AzureKeyCredential credential,
        TokenCredential tokenCredential, List<HttpPipelinePolicy> perCallPolicies,
        List<HttpPipelinePolicy> perRetryPolicies, HttpClient httpClient) {

        Configuration buildConfiguration = (configuration == null)
                                               ? Configuration.getGlobalConfiguration()
                                               : configuration;

        ClientOptions buildClientOptions = (clientOptions == null) ? DEFAULT_CLIENT_OPTIONS : clientOptions;
        HttpLogOptions buildLogOptions = (logOptions == null) ? DEFAULT_LOG_OPTIONS : logOptions;

        String applicationId = CoreUtils.getApplicationId(buildClientOptions, buildLogOptions);

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> httpPipelinePolicies = new ArrayList<>();
        httpPipelinePolicies.add(new AddHeadersPolicy(DEFAULT_HTTP_HEADERS));
        httpPipelinePolicies.add(new AddHeadersFromContextPolicy());
        httpPipelinePolicies.add(new UserAgentPolicy(applicationId, CLIENT_NAME, CLIENT_VERSION, buildConfiguration));
        httpPipelinePolicies.add(new RequestIdPolicy());

        httpPipelinePolicies.addAll(perCallPolicies);
        HttpPolicyProviders.addBeforeRetryPolicies(httpPipelinePolicies);
        httpPipelinePolicies.add(retryPolicy == null ? new RetryPolicy() : retryPolicy);

        httpPipelinePolicies.add(new AddDatePolicy());

        // Authentications
        if (tokenCredential != null) {
            httpPipelinePolicies.add(new BearerTokenAuthenticationPolicy(tokenCredential, DEFAULT_SCOPE));
        } else if (credential != null) {
            httpPipelinePolicies.add(new AzureKeyCredentialPolicy(OCP_APIM_SUBSCRIPTION_KEY, credential));
        } else {
            // Throw exception that credential and tokenCredential cannot be null
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("Missing credential information while building a client."));
        }
        httpPipelinePolicies.addAll(perRetryPolicies);
        HttpPolicyProviders.addAfterRetryPolicies(httpPipelinePolicies);

        HttpHeaders headers = new HttpHeaders();
        buildClientOptions.getHeaders().forEach(header -> headers.set(header.getName(), header.getValue()));
        if (headers.getSize() > 0) {
            httpPipelinePolicies.add(new AddHeadersPolicy(headers));
        }

        httpPipelinePolicies.add(new HttpLoggingPolicy(buildLogOptions));

        return new HttpPipelineBuilder()
                   .clientOptions(buildClientOptions)
                   .httpClient(httpClient)
                   .policies(httpPipelinePolicies.toArray(new HttpPipelinePolicy[0]))
                   .build();
    }
}
