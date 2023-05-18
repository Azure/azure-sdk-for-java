// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.policy.ExponentialBackoffOptions;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
import com.azure.core.implementation.AsynchronousByteChannelWriteSubscriber;
import com.azure.core.implementation.ByteBufferCollector;
import com.azure.core.implementation.OutputStreamWriteSubscriber;
import com.azure.core.implementation.RetriableDownloadFlux;
import com.azure.core.implementation.TypeUtil;
import com.azure.core.util.io.IOUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LoggingEventBuilder;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Operators;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.ContextView;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Utility type exposing methods to deal with {@link Flux}.
 */
public final class FluxUtil {
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    private static final ClientLogger LOGGER = new ClientLogger(FluxUtil.class);

    /**
     * Checks if a type is Flux&lt;ByteBuffer&gt;.
     *
     * @param entityType the type to check
     * @return whether the type represents a Flux that emits ByteBuffer
     */
    public static boolean isFluxByteBuffer(Type entityType) {
        if (TypeUtil.isTypeOrSubTypeOf(entityType, Flux.class)) {
            final Type innerType = TypeUtil.getTypeArguments(entityType)[0];
            return TypeUtil.isTypeOrSubTypeOf(innerType, ByteBuffer.class);
        }
        return false;
    }

    /**
     * Adds progress reporting to the provided {@link Flux} of {@link ByteBuffer}.
     *
     * <p>
     *     Each {@link ByteBuffer} that's emitted from the {@link Flux} will report {@link ByteBuffer#remaining()}.
     * </p>
     * <p>
     *     When {@link Flux} is resubscribed the progress is reset. If the flux is not replayable, resubscribing
     *     can result in empty or partial data then progress reporting might not be accurate.
     * </p>
     * <p>
     *     If {@link ProgressReporter} is not provided, i.e. is {@code null},
     *     then this method returns unmodified {@link Flux}.
     * </p>
     *
     * @param flux A {@link Flux} to report progress on.
     * @param progressReporter Optional {@link ProgressReporter}.
     * @return A {@link Flux} that reports progress, or original {@link Flux} if {@link ProgressReporter} is not
     * provided.
     */
    public static Flux<ByteBuffer> addProgressReporting(Flux<ByteBuffer> flux, ProgressReporter progressReporter) {
        if (progressReporter == null) {
            return flux;
        }

        return Mono.just(progressReporter).flatMapMany(reporter -> {
            /*
             * Each time there is a new subscription, we will rewind the progress. This is desirable specifically for
             * retries, which resubscribe on each try. The first time this flowable is subscribed to, the reset will be
             * a noop as there will have been no progress made. Subsequent rewinds will work as expected.
             */
            reporter.reset();

            /*
             * Every time we emit some data, report it to the Tracker, which will pass it on to the end user.
             */
            return flux.doOnNext(buffer -> reporter.reportProgress(buffer.remaining()));
        });
    }

    /**
     * Collects ByteBuffers emitted by a Flux into a byte array.
     *
     * @param stream A stream which emits ByteBuffer instances.
     * @return A Mono which emits the concatenation of all the ByteBuffer instances given by the source Flux.
     * @throws IllegalStateException If the combined size of the emitted ByteBuffers is greater than {@link
     * Integer#MAX_VALUE}.
     */
    public static Mono<byte[]> collectBytesInByteBufferStream(Flux<ByteBuffer> stream) {
        return stream.collect(ByteBufferCollector::new, ByteBufferCollector::write)
            .map(ByteBufferCollector::toByteArray);
    }

    /**
     * Collects ByteBuffers emitted by a Flux into a byte array.
     * <p>
     * Unlike {@link #collectBytesInByteBufferStream(Flux)}, this method accepts a second parameter {@code sizeHint}.
     * This size hint allows for optimizations when creating the initial buffer to reduce the number of times it needs
     * to be resized while concatenating emitted ByteBuffers.
     *
     * @param stream A stream which emits ByteBuffer instances.
     * @param sizeHint A hint about the expected stream size.
     * @return A Mono which emits the concatenation of all the ByteBuffer instances given by the source Flux.
     * @throws IllegalArgumentException If {@code sizeHint} is equal to or less than {@code 0}.
     * @throws IllegalStateException If the combined size of the emitted ByteBuffers is greater than {@link
     * Integer#MAX_VALUE}.
     */
    public static Mono<byte[]> collectBytesInByteBufferStream(Flux<ByteBuffer> stream, int sizeHint) {
        return stream.collect(() -> new ByteBufferCollector(sizeHint), ByteBufferCollector::write)
            .map(ByteBufferCollector::toByteArray);
    }

    /**
     * Collects ByteBuffers returned in a network response into a byte array.
     * <p>
     * The {@code headers} are inspected for containing an {@code Content-Length} which determines if a size hinted
     * collection, {@link #collectBytesInByteBufferStream(Flux, int)}, or default collection, {@link
     * #collectBytesInByteBufferStream(Flux)}, will be used.
     *
     * @param stream A network response ByteBuffer stream.
     * @param headers The HTTP headers of the response.
     * @return A Mono which emits the collected network response ByteBuffers.
     * @throws NullPointerException If {@code headers} is null.
     * @throws IllegalStateException If the size of the network response is greater than {@link Integer#MAX_VALUE}.
     */
    public static Mono<byte[]> collectBytesFromNetworkResponse(Flux<ByteBuffer> stream, HttpHeaders headers) {
        Objects.requireNonNull(headers, "'headers' cannot be null.");

        String contentLengthHeader = headers.getValue(HttpHeaderName.CONTENT_LENGTH);

        if (contentLengthHeader == null) {
            return FluxUtil.collectBytesInByteBufferStream(stream);
        } else {
            try {
                int contentLength = Integer.parseInt(contentLengthHeader);
                if (contentLength > 0) {
                    return FluxUtil.collectBytesInByteBufferStream(stream, contentLength);
                } else {
                    return Mono.just(EMPTY_BYTE_ARRAY);
                }
            } catch (NumberFormatException ex) {
                return FluxUtil.collectBytesInByteBufferStream(stream);
            }
        }
    }

    /**
     * Gets the content of the provided ByteBuffer as a byte array. This method will create a new byte array even if the
     * ByteBuffer can have optionally backing array.
     *
     * @param byteBuffer the byte buffer
     * @return the byte array
     */
    public static byte[] byteBufferToArray(ByteBuffer byteBuffer) {
        int length = byteBuffer.remaining();
        byte[] byteArray = new byte[length];
        byteBuffer.get(byteArray);
        return byteArray;
    }

    /**
     * Creates a {@link Flux} that is capable of resuming a download by applying retry logic when an error occurs.
     *
     * @param downloadSupplier Supplier of the initial download.
     * @param onDownloadErrorResume {@link BiFunction} of {@link Throwable} and {@link Long} which is used to resume
     * downloading when an error occurs.
     * @param maxRetries The maximum number of times a download can be resumed when an error occurs.
     * @return A {@link Flux} that downloads reliably.
     */
    public static Flux<ByteBuffer> createRetriableDownloadFlux(Supplier<Flux<ByteBuffer>> downloadSupplier,
        BiFunction<Throwable, Long, Flux<ByteBuffer>> onDownloadErrorResume, int maxRetries) {
        return createRetriableDownloadFlux(downloadSupplier, onDownloadErrorResume,
            createDefaultRetryOptions(maxRetries), 0L);
    }

    /**
     * Creates a {@link Flux} that is capable of resuming a download by applying retry logic when an error occurs.
     *
     * @param downloadSupplier Supplier of the initial download.
     * @param onDownloadErrorResume {@link BiFunction} of {@link Throwable} and {@link Long} which is used to resume
     * downloading when an error occurs.
     * @param maxRetries The maximum number of times a download can be resumed when an error occurs.
     * @param position The initial offset for the download.
     * @return A {@link Flux} that downloads reliably.
     */
    public static Flux<ByteBuffer> createRetriableDownloadFlux(Supplier<Flux<ByteBuffer>> downloadSupplier,
        BiFunction<Throwable, Long, Flux<ByteBuffer>> onDownloadErrorResume, int maxRetries, long position) {
        return createRetriableDownloadFlux(downloadSupplier, onDownloadErrorResume,
            createDefaultRetryOptions(maxRetries), position);
    }

    private static RetryOptions createDefaultRetryOptions(int maxRetries) {
        return new RetryOptions(new ExponentialBackoffOptions().setMaxRetries(Math.max(0, maxRetries)));
    }

    /**
     * Creates a {@link Flux} that is capable of resuming a download by applying retry logic when an error occurs.
     *
     * @param downloadSupplier Supplier of the initial download.
     * @param onDownloadErrorResume {@link BiFunction} of {@link Throwable} and {@link Long} which is used to resume
     * downloading when an error occurs.
     * @param retryOptions The options for retrying.
     * @param position The initial offset for the download.
     * @return A {@link Flux} that downloads reliably.
     */
    public static Flux<ByteBuffer> createRetriableDownloadFlux(Supplier<Flux<ByteBuffer>> downloadSupplier,
        BiFunction<Throwable, Long, Flux<ByteBuffer>> onDownloadErrorResume, RetryOptions retryOptions,
        long position) {
        RetryOptions options = (retryOptions == null)
            ? new RetryOptions(new ExponentialBackoffOptions())
            : retryOptions;
        return new RetriableDownloadFlux(downloadSupplier, onDownloadErrorResume, options, position);
    }

    /**
     * Converts an {@link InputStream} into a {@link Flux} of {@link ByteBuffer} using a chunk size of 4096.
     * <p>
     * Given that {@link InputStream} is not guaranteed to be replayable the returned {@link Flux} should be considered
     * non-replayable as well.
     * <p>
     * If the passed {@link InputStream} is {@code null} {@link Flux#empty()} will be returned.
     *
     * @param inputStream The {@link InputStream} to convert into a {@link Flux}.
     * @return A {@link Flux} of {@link ByteBuffer ByteBuffers} that contains the contents of the stream.
     */
    public static Flux<ByteBuffer> toFluxByteBuffer(InputStream inputStream) {
        return toFluxByteBuffer(inputStream, 4096);
    }

    /**
     * Converts an {@link InputStream} into a {@link Flux} of {@link ByteBuffer}.
     * <p>
     * Given that {@link InputStream} is not guaranteed to be replayable the returned {@link Flux} should be considered
     * non-replayable as well.
     * <p>
     * If the passed {@link InputStream} is {@code null} {@link Flux#empty()} will be returned.
     *
     * @param inputStream The {@link InputStream} to convert into a {@link Flux}.
     * @param chunkSize The requested size for each {@link ByteBuffer}.
     * @return A {@link Flux} of {@link ByteBuffer ByteBuffers} that contains the contents of the stream.
     * @throws IllegalArgumentException If {@code chunkSize} is less than or equal to {@code 0}.
     */
    public static Flux<ByteBuffer> toFluxByteBuffer(InputStream inputStream, int chunkSize) {
        if (chunkSize <= 0) {
            return Flux.error(new IllegalArgumentException("'chunkSize' must be greater than 0."));
        }

        if (inputStream == null) {
            return Flux.empty();
        }

        // If the InputStream is an instance of FileInputStream we should be able to leverage the FileChannel backing
        // the FileInputStream to generated MappedByteBuffers which aren't loaded into memory until the content is
        // consumed. This at least defers the memory usage until later and may also provide downstream calls ways to
        // optimize if they have special cases for MappedByteBuffer.
        if (inputStream instanceof FileInputStream) {
            FileChannel fileChannel = ((FileInputStream) inputStream).getChannel();

            return Flux.<ByteBuffer, FileChannel>generate(() -> fileChannel, (channel, sink) -> {
                try {
                    long channelPosition = channel.position();
                    long channelSize = channel.size();

                    if (channelPosition == channelSize) {
                        // End of File has been reached, signal completion.
                        channel.close();
                        sink.complete();
                    } else {
                        // Determine the size of the next MappedByteBuffer, either the remaining File contents or the
                        // expected chunk size.
                        int nextByteBufferSize = (int) Math.min(chunkSize, channelSize - channelPosition);
                        sink.next(channel.map(FileChannel.MapMode.READ_ONLY, channelPosition, nextByteBufferSize));

                        // FileChannel.map doesn't update the FileChannel's position as reading would, so the position
                        // needs to be updated based on the number of bytes mapped.
                        channel.position(channelPosition + nextByteBufferSize);
                    }
                } catch (IOException ex) {
                    sink.error(ex);
                }

                return channel;
            });
        }

        return Flux.<ByteBuffer, InputStream>generate(() -> inputStream, (stream, sink) -> {
            byte[] buffer = new byte[chunkSize];

            try {
                int offset = 0;

                while (offset < chunkSize) {
                    int readCount = inputStream.read(buffer, offset, chunkSize - offset);

                    // We have finished reading the stream, trigger onComplete.
                    if (readCount == -1) {
                        // If there were bytes read before reaching the end emit the buffer before completing.
                        if (offset > 0) {
                            sink.next(ByteBuffer.wrap(buffer, 0, offset));
                        }
                        sink.complete();
                        return stream;
                    }

                    offset += readCount;
                }

                sink.next(ByteBuffer.wrap(buffer));
            } catch (IOException ex) {
                sink.error(ex);
            }

            return stream;
        }).filter(ByteBuffer::hasRemaining);
    }

    /**
     * This method converts the incoming {@code deferContextual} from {@link reactor.util.context.Context Reactor
     * Context} to {@link Context Azure Context} and calls the given lambda function with this context and returns a
     * single entity of type {@code T}
     * <p>
     * If the reactor context is empty, {@link Context#NONE} will be used to call the lambda function
     * </p>
     *
     * <p><strong>Code samples</strong></p>
     * <!-- src_embed com.azure.core.implementation.util.fluxutil.withcontext -->
     * <pre>
     * String prefix = &quot;Hello, &quot;;
     * Mono&lt;String&gt; response = FluxUtil
     *     .withContext&#40;context -&gt; serviceCallReturnsSingle&#40;prefix, context&#41;&#41;;
     * </pre>
     * <!-- end com.azure.core.implementation.util.fluxutil.withcontext -->
     *
     * @param serviceCall The lambda function that makes the service call into which azure context will be passed
     * @param <T> The type of response returned from the service call
     * @return The response from service call
     */
    public static <T> Mono<T> withContext(Function<Context, Mono<T>> serviceCall) {
        return withContext(serviceCall, Collections.emptyMap());
    }

    /**
     * This method converts the incoming {@code deferContextual} from {@link reactor.util.context.Context Reactor
     * Context} to {@link Context Azure Context}, adds the specified context attributes and calls the given lambda
     * function with this context and returns a single entity of type {@code T}
     * <p>
     * If the reactor context is empty, {@link Context#NONE} will be used to call the lambda function
     * </p>
     *
     * @param serviceCall serviceCall The lambda function that makes the service call into which azure context will be
     * passed
     * @param contextAttributes The map of attributes sent by the calling method to be set on {@link Context}.
     * @param <T> The type of response returned from the service call
     * @return The response from service call
     */
    public static <T> Mono<T> withContext(Function<Context, Mono<T>> serviceCall,
        Map<String, String> contextAttributes) {
        return Mono.deferContextual(context -> {
            final Context[] azureContext = new Context[]{Context.NONE};

            if (!CoreUtils.isNullOrEmpty(contextAttributes)) {
                contextAttributes.forEach((key, value) -> azureContext[0] = azureContext[0].addData(key, value));
            }

            if (!context.isEmpty()) {
                context.stream().forEach(entry ->
                    azureContext[0] = azureContext[0].addData(entry.getKey(), entry.getValue()));
            }

            return serviceCall.apply(azureContext[0]);
        });
    }

    /**
     * Converts the incoming content to Mono.
     *
     * @param <T> The type of the Response, which will be returned in the Mono.
     * @param response whose {@link Response#getValue() value} is to be converted
     * @return The converted {@link Mono}
     */
    public static <T> Mono<T> toMono(Response<T> response) {
        return Mono.justOrEmpty(response.getValue());
    }

    /**
     * Propagates a {@link RuntimeException} through the error channel of {@link Mono}.
     *
     * @param logger The {@link ClientLogger} to log the exception.
     * @param ex The {@link RuntimeException}.
     * @param <T> The return type.
     * @return A {@link Mono} that terminates with error wrapping the {@link RuntimeException}.
     */
    public static <T> Mono<T> monoError(ClientLogger logger, RuntimeException ex) {
        return Mono.error(logger.logExceptionAsError(Exceptions.propagate(ex)));
    }

    /**
     * Propagates a {@link RuntimeException} through the error channel of {@link Mono}.
     *
     * @param logBuilder The {@link LoggingEventBuilder} with context to log the exception.
     * @param ex The {@link RuntimeException}.
     * @param <T> The return type.
     * @return A {@link Mono} that terminates with error wrapping the {@link RuntimeException}.
     */
    public static <T> Mono<T> monoError(LoggingEventBuilder logBuilder, RuntimeException ex) {
        return Mono.error(logBuilder.log(Exceptions.propagate(ex)));
    }

    /**
     * Propagates a {@link RuntimeException} through the error channel of {@link Flux}.
     *
     * @param logger The {@link ClientLogger} to log the exception.
     * @param ex The {@link RuntimeException}.
     * @param <T> The return type.
     * @return A {@link Flux} that terminates with error wrapping the {@link RuntimeException}.
     */
    public static <T> Flux<T> fluxError(ClientLogger logger, RuntimeException ex) {
        return Flux.error(logger.logExceptionAsError(Exceptions.propagate(ex)));
    }

    /**
     * Propagates a {@link RuntimeException} through the error channel of {@link PagedFlux}.
     *
     * @param logger The {@link ClientLogger} to log the exception.
     * @param ex The {@link RuntimeException}.
     * @param <T> The return type.
     * @return A {@link PagedFlux} that terminates with error wrapping the {@link RuntimeException}.
     */
    public static <T> PagedFlux<T> pagedFluxError(ClientLogger logger, RuntimeException ex) {
        return new PagedFlux<>(() -> monoError(logger, ex));
    }

    /**
     * This method converts the incoming {@code deferContextual} from {@link reactor.util.context.Context Reactor
     * Context} to {@link Context Azure Context} and calls the given lambda function with this context and returns a
     * collection of type {@code T}
     * <p>
     * If the reactor context is empty, {@link Context#NONE} will be used to call the lambda function
     * </p>
     *
     * <p><strong>Code samples</strong></p>
     * <!-- src_embed com.azure.core.implementation.util.fluxutil.fluxcontext -->
     * <pre>
     * String prefix = &quot;Hello, &quot;;
     * Flux&lt;String&gt; response = FluxUtil
     *     .fluxContext&#40;context -&gt; serviceCallReturnsCollection&#40;prefix, context&#41;&#41;;
     * </pre>
     * <!-- end com.azure.core.implementation.util.fluxutil.fluxcontext -->
     *
     * @param serviceCall The lambda function that makes the service call into which the context will be passed
     * @param <T> The type of response returned from the service call
     * @return The response from service call
     */
    public static <T> Flux<T> fluxContext(Function<Context, Flux<T>> serviceCall) {
        return Flux.deferContextual(context -> serviceCall.apply(toAzureContext(context)));
    }

    /**
     * Converts a reactor context to azure context. If the reactor context is {@code null} or empty, {@link
     * Context#NONE} will be returned.
     *
     * @param context The reactor context
     * @return The azure context
     */
    private static Context toAzureContext(ContextView context) {
        final Context[] azureContext = new Context[]{Context.NONE};

        if (!context.isEmpty()) {
            context.stream().forEach(entry ->
                azureContext[0] = azureContext[0].addData(entry.getKey(), entry.getValue()));
        }

        return azureContext[0];
    }

    /**
     * Converts an Azure context to Reactor context. If the Azure context is {@code null} or empty, {@link
     * reactor.util.context.Context#empty()} will be returned.
     *
     * @param context The Azure context.
     * @return The Reactor context.
     */
    public static reactor.util.context.Context toReactorContext(Context context) {
        if (context == null) {
            return reactor.util.context.Context.empty();
        }

        reactor.util.context.Context returnContext = reactor.util.context.Context.empty();

        Context[] contextChain = context.getContextChain();
        for (Context toAdd : contextChain) {
            // Filter out null value entries as Reactor's context doesn't allow null values.
            if (toAdd == null || toAdd.getValue() == null) {
                continue;
            }

            returnContext = returnContext.put(toAdd.getKey(), toAdd.getValue());
        }

        return returnContext;
    }

    /**
     * Writes the {@link ByteBuffer ByteBuffers} emitted by a {@link Flux} of {@link ByteBuffer} to an {@link
     * OutputStream}.
     * <p>
     * The {@code stream} is not closed by this call, closing of the {@code stream} is managed by the caller.
     * <p>
     * The response {@link Mono} will emit an error if {@code content} or {@code stream} are null. Additionally, an
     * error will be emitted if an exception occurs while writing the {@code content} to the {@code stream}.
     *
     * @param content The {@link Flux} of {@link ByteBuffer} content.
     * @param stream The {@link OutputStream} being written into.
     * @return A {@link Mono} which emits a completion status once the {@link Flux} has been written to the {@link
     * OutputStream}, or an error status if writing fails.
     */
    public static Mono<Void> writeToOutputStream(Flux<ByteBuffer> content, OutputStream stream) {
        if (content == null && stream == null) {
            return monoError(LOGGER, new NullPointerException("'content' and 'stream' cannot be null."));
        } else if (content == null) {
            return monoError(LOGGER, new NullPointerException("'content' cannot be null."));
        } else if (stream == null) {
            return monoError(LOGGER, new NullPointerException("'stream' cannot be null."));
        }

        return Mono.create(emitter -> content.subscribe(new OutputStreamWriteSubscriber(emitter, stream, LOGGER)));
    }

    /**
     * Writes the {@link ByteBuffer ByteBuffers} emitted by a {@link Flux} of {@link ByteBuffer} to an {@link
     * AsynchronousFileChannel}.
     * <p>
     * The {@code outFile} is not closed by this call, closing of the {@code outFile} is managed by the caller.
     * <p>
     * The response {@link Mono} will emit an error if {@code content} or {@code outFile} are null. Additionally, an
     * error will be emitted if the {@code outFile} wasn't opened with the proper open options, such as {@link
     * StandardOpenOption#WRITE}.
     *
     * @param content The {@link Flux} of {@link ByteBuffer} content.
     * @param outFile The {@link AsynchronousFileChannel}.
     * @return A {@link Mono} which emits a completion status once the {@link Flux} has been written to the {@link
     * AsynchronousFileChannel}.
     * @throws NullPointerException When {@code content} is null.
     * @throws NullPointerException When {@code outFile} is null.
     */
    public static Mono<Void> writeFile(Flux<ByteBuffer> content, AsynchronousFileChannel outFile) {
        return writeFile(content, outFile, 0);
    }

    /**
     * Writes the {@link ByteBuffer ByteBuffers} emitted by a {@link Flux} of {@link ByteBuffer} to an {@link
     * AsynchronousFileChannel} starting at the given {@code position} in the file.
     * <p>
     * The {@code outFile} is not closed by this call, closing of the {@code outFile} is managed by the caller.
     * <p>
     * The response {@link Mono} will emit an error if {@code content} or {@code outFile} are null or {@code position}
     * is less than 0. Additionally, an error will be emitted if the {@code outFile} wasn't opened with the proper open
     * options, such as {@link StandardOpenOption#WRITE}.
     *
     * @param content The {@link Flux} of {@link ByteBuffer} content.
     * @param outFile The {@link AsynchronousFileChannel}.
     * @param position The position in the file to begin writing the {@code content}.
     * @return A {@link Mono} which emits a completion status once the {@link Flux} has been written to the {@link
     * AsynchronousFileChannel}.
     * @throws NullPointerException When {@code content} is null.
     * @throws NullPointerException When {@code outFile} is null.
     * @throws IllegalArgumentException When {@code position} is negative.
     */
    public static Mono<Void> writeFile(Flux<ByteBuffer> content, AsynchronousFileChannel outFile, long position) {
        if (content == null && outFile == null) {
            return monoError(LOGGER, new NullPointerException("'content' and 'outFile' cannot be null."));
        } else if (content == null) {
            return monoError(LOGGER, new NullPointerException("'content' cannot be null."));
        } else if (outFile == null) {
            return monoError(LOGGER, new NullPointerException("'outFile' cannot be null."));
        } else if (position < 0) {
            return monoError(LOGGER, new IllegalArgumentException("'position' cannot be less than 0."));
        }

        return writeToAsynchronousByteChannel(content, IOUtils.toAsynchronousByteChannel(outFile, position));
    }

    /**
     * Writes the {@link ByteBuffer ByteBuffers} emitted by a {@link Flux} of {@link ByteBuffer} to an {@link
     * AsynchronousByteChannel}.
     * <p>
     * The {@code channel} is not closed by this call, closing of the {@code channel} is managed by the caller.
     * <p>
     * The response {@link Mono} will emit an error if {@code content} or {@code channel} are null.
     *
     * @param content The {@link Flux} of {@link ByteBuffer} content.
     * @param channel The {@link AsynchronousByteChannel}.
     * @return A {@link Mono} which emits a completion status once the {@link Flux} has been written to the {@link
     * AsynchronousByteChannel}.
     * @throws NullPointerException When {@code content} is null.
     * @throws NullPointerException When {@code channel} is null.
     */
    public static Mono<Void> writeToAsynchronousByteChannel(Flux<ByteBuffer> content, AsynchronousByteChannel channel) {
        if (content == null && channel == null) {
            return monoError(LOGGER, new NullPointerException("'content' and 'channel' cannot be null."));
        } else if (content == null) {
            return monoError(LOGGER, new NullPointerException("'content' cannot be null."));
        } else if (channel == null) {
            return monoError(LOGGER, new NullPointerException("'channel' cannot be null."));
        }

        return Mono.create(emitter -> content.subscribe(
            new AsynchronousByteChannelWriteSubscriber(channel, emitter)));
    }

    /**
     * Writes the {@link ByteBuffer ByteBuffers} emitted by a {@link Flux} of {@link ByteBuffer} to an {@link
     * WritableByteChannel}.
     * <p>
     * The {@code channel} is not closed by this call, closing of the {@code channel} is managed by the caller.
     * <p>
     * The response {@link Mono} will emit an error if {@code content} or {@code channel} are null.
     *
     * @param content The {@link Flux} of {@link ByteBuffer} content.
     * @param channel The {@link WritableByteChannel}.
     * @return A {@link Mono} which emits a completion status once the {@link Flux} has been written to the {@link
     * WritableByteChannel}.
     * @throws NullPointerException When {@code content} is null.
     * @throws NullPointerException When {@code channel} is null.
     */
    public static Mono<Void> writeToWritableByteChannel(Flux<ByteBuffer> content, WritableByteChannel channel) {
        if (content == null && channel == null) {
            return monoError(LOGGER, new NullPointerException("'content' and 'channel' cannot be null."));
        } else if (content == null) {
            return monoError(LOGGER, new NullPointerException("'content' cannot be null."));
        } else if (channel == null) {
            return monoError(LOGGER, new NullPointerException("'channel' cannot be null."));
        }

        return content.publishOn(Schedulers.boundedElastic())
            .map(buffer -> {
                while (buffer.hasRemaining()) {
                    try {
                        channel.write(buffer);
                    } catch (IOException e) {
                        throw Exceptions.propagate(e);
                    }
                }
                return buffer;
            }).then();
    }

    /**
     * Creates a {@link Flux} from an {@link AsynchronousFileChannel} which reads part of a file into chunks of the
     * given size.
     *
     * @param fileChannel The file channel.
     * @param chunkSize the size of file chunks to read.
     * @param offset The offset in the file to begin reading.
     * @param length The number of bytes to read from the file.
     * @return the Flux.
     */
    public static Flux<ByteBuffer> readFile(AsynchronousFileChannel fileChannel, int chunkSize, long offset,
        long length) {
        return new FileReadFlux(fileChannel, chunkSize, offset, length);
    }

    /**
     * Creates a {@link Flux} from an {@link AsynchronousFileChannel} which reads part of a file.
     *
     * @param fileChannel The file channel.
     * @param offset The offset in the file to begin reading.
     * @param length The number of bytes to read from the file.
     * @return the Flux.
     */
    public static Flux<ByteBuffer> readFile(AsynchronousFileChannel fileChannel, long offset, long length) {
        return readFile(fileChannel, DEFAULT_CHUNK_SIZE, offset, length);
    }

    /**
     * Creates a {@link Flux} from an {@link AsynchronousFileChannel} which reads the entire file.
     *
     * @param fileChannel The file channel.
     * @return The AsyncInputStream.
     */
    public static Flux<ByteBuffer> readFile(AsynchronousFileChannel fileChannel) {
        try {
            long size = fileChannel.size();
            return readFile(fileChannel, DEFAULT_CHUNK_SIZE, 0, size);
        } catch (IOException e) {
            return Flux.error(new RuntimeException("Failed to read the file.", e));
        }
    }

    private static final int DEFAULT_CHUNK_SIZE = 1024 * 64;

    private static final class FileReadFlux extends Flux<ByteBuffer> {
        private final AsynchronousFileChannel fileChannel;
        private final int chunkSize;
        private final long offset;
        private final long length;

        FileReadFlux(AsynchronousFileChannel fileChannel, int chunkSize, long offset, long length) {
            this.fileChannel = fileChannel;
            this.chunkSize = chunkSize;
            this.offset = offset;
            this.length = length;
        }

        @Override
        public void subscribe(CoreSubscriber<? super ByteBuffer> actual) {
            FileReadSubscription subscription =
                new FileReadSubscription(actual, fileChannel, chunkSize, offset, length);
            actual.onSubscribe(subscription);
        }

        static final class FileReadSubscription implements Subscription, CompletionHandler<Integer, ByteBuffer> {
            private static final int NOT_SET = -1;
            private static final long serialVersionUID = -6831808726875304256L;
            //
            private final Subscriber<? super ByteBuffer> subscriber;
            private volatile long position;
            //
            private final AsynchronousFileChannel fileChannel;
            private final int chunkSize;
            private final long offset;
            private final long length;
            //
            private volatile boolean done;
            private Throwable error;
            private volatile ByteBuffer next;
            private volatile boolean cancelled;
            //
            volatile int wip;
            static final AtomicIntegerFieldUpdater<FileReadSubscription> WIP =
                AtomicIntegerFieldUpdater.newUpdater(FileReadSubscription.class, "wip");

            volatile long requested;
            static final AtomicLongFieldUpdater<FileReadSubscription> REQUESTED =
                AtomicLongFieldUpdater.newUpdater(FileReadSubscription.class, "requested");
            //

            FileReadSubscription(Subscriber<? super ByteBuffer> subscriber, AsynchronousFileChannel fileChannel,
                int chunkSize, long offset, long length) {
                this.subscriber = subscriber;
                //
                this.fileChannel = fileChannel;
                this.chunkSize = chunkSize;
                this.offset = offset;
                this.length = length;
                //
                this.position = NOT_SET;
            }

            //region Subscription implementation

            @Override
            public void request(long n) {
                if (Operators.validate(n)) {
                    Operators.addCap(REQUESTED, this, n);
                    drain();
                }
            }

            @Override
            public void cancel() {
                this.cancelled = true;
            }

            //endregion

            //region CompletionHandler implementation

            @Override
            public void completed(Integer bytesRead, ByteBuffer buffer) {
                if (!cancelled) {
                    if (bytesRead == -1) {
                        done = true;
                    } else {
                        // use local variable to perform fewer volatile reads
                        long pos = position;
                        int bytesWanted = Math.min(bytesRead, maxRequired(pos));
                        long position2 = pos + bytesWanted;
                        //noinspection NonAtomicOperationOnVolatileField
                        position = position2;
                        buffer.position(bytesWanted);
                        buffer.flip();
                        next = buffer;
                        if (position2 >= offset + length) {
                            done = true;
                        }
                    }
                    drain();
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                if (!cancelled) {
                    // must set error before setting done to true
                    // so that is visible in drain loop
                    error = exc;
                    done = true;
                    drain();
                }
            }

            //endregion

            private void drain() {
                if (WIP.getAndIncrement(this) != 0) {
                    return;
                }
                // on first drain (first request) we initiate the first read
                if (position == NOT_SET) {
                    position = offset;
                    doRead();
                }
                int missed = 1;
                while (true) {
                    if (cancelled) {
                        return;
                    }
                    if (REQUESTED.get(this) > 0) {
                        boolean emitted = false;
                        // read d before next to avoid race
                        boolean d = done;
                        ByteBuffer bb = next;
                        if (bb != null) {
                            next = null;
                            subscriber.onNext(bb);
                            emitted = true;
                        }
                        if (d) {
                            if (error != null) {
                                subscriber.onError(error);
                            } else {
                                subscriber.onComplete();
                            }

                            // exit without reducing wip so that further drains will be NOOP
                            return;
                        }
                        if (emitted) {
                            // do this after checking d to avoid calling read
                            // when done
                            Operators.produced(REQUESTED, this, 1);
                            //
                            doRead();
                        }
                    }
                    missed = WIP.addAndGet(this, -missed);
                    if (missed == 0) {
                        return;
                    }
                }
            }

            private void doRead() {
                // use local variable to limit volatile reads
                long pos = position;
                ByteBuffer innerBuf = ByteBuffer.allocate(Math.min(chunkSize, maxRequired(pos)));
                fileChannel.read(innerBuf, pos, innerBuf, this);
            }

            private int maxRequired(long pos) {
                long maxRequired = offset + length - pos;
                if (maxRequired <= 0) {
                    return 0;
                } else {
                    int m = (int) (maxRequired);
                    // support really large files by checking for overflow
                    if (m < 0) {
                        return Integer.MAX_VALUE;
                    } else {
                        return m;
                    }
                }
            }
        }
    }


    // Private Ctr
    private FluxUtil() {
    }
}
