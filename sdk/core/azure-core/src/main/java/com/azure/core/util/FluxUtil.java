// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
import com.azure.core.implementation.TypeUtil;
import com.azure.core.util.logging.ClientLogger;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Operators;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utility type exposing methods to deal with {@link Flux}.
 */
public final class FluxUtil {
    /**
     * Checks if a type is Flux&lt;ByteBuffer&gt;.
     *
     * @param entityType the type to check
     * @return whether the type represents a Flux that emits ByteBuffer
     */
    public static boolean isFluxByteBuffer(Type entityType) {
        if (TypeUtil.isTypeOrSubTypeOf(entityType, Flux.class)) {
            final Type innerType = TypeUtil.getTypeArguments(entityType)[0];
            if (TypeUtil.isTypeOrSubTypeOf(innerType, ByteBuffer.class)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Collects ByteBuffer emitted by a Flux into a byte array.
     *
     * @param stream A stream which emits ByteBuffer instances.
     * @return A Mono which emits the concatenation of all the ByteBuffer instances given by the source Flux.
     */
    public static Mono<byte[]> collectBytesInByteBufferStream(Flux<ByteBuffer> stream) {
        return stream
            .collect(ByteArrayOutputStream::new, FluxUtil::accept)
            .map(ByteArrayOutputStream::toByteArray);
    }

    private static void accept(ByteArrayOutputStream byteOutputStream, ByteBuffer byteBuffer) {
        try {
            byteOutputStream.write(byteBufferToArray(byteBuffer));
        } catch (IOException e) {
            throw new RuntimeException("Error occurred writing ByteBuffer to ByteArrayOutputStream.", e);
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
     * This method converts the incoming {@code subscriberContext} from {@link reactor.util.context.Context Reactor
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
        return Mono.subscriberContext()
            .map(FluxUtil::toAzureContext)
            .flatMap(serviceCall);
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
     * This method converts the incoming {@code subscriberContext} from {@link reactor.util.context.Context Reactor
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
        return Mono.subscriberContext()
            .map(FluxUtil::toAzureContext)
            .flatMapMany(serviceCall);
    }

    /**
     * Converts a reactor context to azure context. If the reactor context is {@code null} or empty, {@link
     * Context#NONE} will be returned.
     *
     * @param context The reactor context
     * @return The azure context
     */
    private static Context toAzureContext(reactor.util.context.Context context) {
        Map<Object, Object> keyValues = context.stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        return CoreUtils.isNullOrEmpty(keyValues) ? Context.NONE : Context.of(keyValues);
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

        Map<Object, Object> contextValues = context.getValues();

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


            CompletionHandler<Integer, Object> onWriteCompleted = new CompletionHandler<Integer, Object>() {
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
                for (;;) {
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
                        } else {
                            emitted = false;
                        }
                        if (d) {
                            if (error != null) {
                                subscriber.onError(error);
                                // exit without reducing wip so that further drains will be NOOP
                                return;
                            } else {
                                subscriber.onComplete();
                                // exit without reducing wip so that further drains will be NOOP
                                return;
                            }
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
