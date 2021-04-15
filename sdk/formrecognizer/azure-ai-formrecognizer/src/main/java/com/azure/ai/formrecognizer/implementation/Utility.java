// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation;

import com.azure.ai.formrecognizer.implementation.models.ContentType;
import com.azure.ai.formrecognizer.implementation.models.ErrorInformation;
import com.azure.ai.formrecognizer.implementation.models.ErrorResponseException;
import com.azure.ai.formrecognizer.models.FormRecognizerErrorInformation;
import com.azure.ai.formrecognizer.models.FormRecognizerOperationResult;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.PollingContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.time.Duration;
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
    // using 4K as default buffer size: https://stackoverflow.com/a/237495/1473510
    private static final int BYTE_BUFFER_CHUNK_SIZE = 4096;
    // default time interval for polling
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
}
