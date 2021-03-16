// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
import com.azure.core.implementation.ByteBufferCollector;
import com.azure.core.implementation.TypeUtil;
import com.azure.core.util.logging.ClientLogger;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Operators;
import reactor.util.context.ContextView;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utility type exposing methods to deal with {@link Flux}.
 */
public final class FluxUtil {
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

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

        String contentLengthHeader = headers.getValue("Content-Length");

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
     * {@codesnippet com.azure.core.implementation.util.fluxutil.withcontext}
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
     * {@codesnippet com.azure.core.implementation.util.fluxutil.fluxcontext}
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

        // Filter out null value entries as Reactor's context doesn't allow null values.
        Map<Object, Object> contextValues = context.getValues().entrySet().stream()
            .filter(kvp -> kvp.getValue() != null)
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        return CoreUtils.isNullOrEmpty(contextValues)
            ? reactor.util.context.Context.empty()
            : reactor.util.context.Context.of(contextValues);
    }

    /**
     * Writes the bytes emitted by a Flux to an AsynchronousFileChannel.
     *
     * @param content the Flux content
     * @param outFile the file channel
     * @return a Mono which performs the write operation when subscribed
     */
    public static Mono<Void> writeFile(Flux<ByteBuffer> content, AsynchronousFileChannel outFile) {
        return writeFile(content, outFile, 0);
    }

    /**
     * Writes the bytes emitted by a Flux to an AsynchronousFileChannel starting at the given position in the file.
     *
     * @param content the Flux content
     * @param outFile the file channel
     * @param position the position in the file to begin writing
     * @return a Mono which performs the write operation when subscribed
     */
    public static Mono<Void> writeFile(Flux<ByteBuffer> content, AsynchronousFileChannel outFile, long position) {
        return Mono.create(emitter -> content.subscribe(new Subscriber<ByteBuffer>() {
            // volatile ensures that writes to these fields by one thread will be immediately visible to other threads.
            // An I/O pool thread will write to isWriting and read isCompleted,
            // while another thread may read isWriting and write to isCompleted.
            volatile boolean isWriting = false;
            volatile boolean isCompleted = false;
            volatile Subscription subscription;
            volatile long pos = position;

            @Override
            public void onSubscribe(Subscription s) {
                subscription = s;
                s.request(1);
            }

            @Override
            public void onNext(ByteBuffer bytes) {
                isWriting = true;
                outFile.write(bytes, pos, null, onWriteCompleted);
            }


            final CompletionHandler<Integer, Object> onWriteCompleted = new CompletionHandler<Integer, Object>() {
                @Override
                public void completed(Integer bytesWritten, Object attachment) {
                    isWriting = false;
                    if (isCompleted) {
                        emitter.success();
                    }
                    //noinspection NonAtomicOperationOnVolatileField
                    pos += bytesWritten;
                    subscription.request(1);
                }

                @Override
                public void failed(Throwable exc, Object attachment) {
                    subscription.cancel();
                    emitter.error(exc);
                }
            };

            @Override
            public void onError(Throwable throwable) {
                subscription.cancel();
                emitter.error(throwable);
            }

            @Override
            public void onComplete() {
                isCompleted = true;
                if (!isWriting) {
                    emitter.success();
                }
            }
        }));
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
            @SuppressWarnings("rawtypes")
            static final AtomicIntegerFieldUpdater<FileReadSubscription> WIP =
                AtomicIntegerFieldUpdater.newUpdater(FileReadSubscription.class, "wip");
            volatile long requested;
            @SuppressWarnings("rawtypes")
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
