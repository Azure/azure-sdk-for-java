package com.azure.core.implementation;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;

public class FluxTestUtils {

    /**
     * Collects byte buffers emitted by a Flux into a ByteBuf.
     *
     * @param stream A stream which emits ByteBuf instances.
     * @param autoReleaseEnabled if ByteBuf instances in stream gets automatically released as they consumed
     * @return A Mono which emits the concatenation of all the byte buffers given by the source Flux.
     */
    public static Mono<ByteBuffer> collectByteBufStream(Flux<ByteBuffer> stream, boolean autoReleaseEnabled) {
//        if (autoReleaseEnabled) {
//            Mono<ByteBuffer> mergedCbb = Mono.using(
//                // Resource supplier
//                () -> {
//                    CompositeByteBuf initialCbb = Unpooled.compositeBuffer();
//                    return initialCbb;
//                },
//                // source Mono creator
//                (CompositeByteBuf initialCbb) -> {
//                    Mono<CompositeByteBuf> reducedCbb = stream.reduce(initialCbb, (CompositeByteBuf currentCbb, ByteBuf nextBb) -> {
//                        CompositeByteBuf updatedCbb = currentCbb.addComponent(nextBb.retain());
//                        return updatedCbb;
//                    });
//                    //
//                    return reducedCbb
//                               .doOnNext((CompositeByteBuf cbb) -> cbb.writerIndex(cbb.capacity()))
//                               .filter((CompositeByteBuf cbb) -> cbb.isReadable());
//                },
//                // Resource cleaner
//                (CompositeByteBuf finalCbb) -> finalCbb.release());
//            return mergedCbb;
//        } else {
//            return stream.collect(Unpooled::compositeBuffer,
//                (cbb1, buffer) -> cbb1.addComponent(true, Unpooled.wrappedBuffer(buffer)))
//                       .filter((CompositeByteBuf cbb) -> cbb.isReadable())
//                       .map(bb -> bb);
//        }

        // TODO
        throw new IllegalStateException("This method is not yet re-implemented");
    }

    private static final int DEFAULT_CHUNK_SIZE = 1024 * 64;

    /**
     * Writes the bytes emitted by a Flux to an AsynchronousFileChannel.
     *
     * @param content the Flux content
     * @param outFile the file channel
     * @return a Completable which performs the write operation when subscribed
     */
    public static Mono<Void> bytebufStreamToFile(Flux<ByteBuffer> content, AsynchronousFileChannel outFile) {
        return bytebufStreamToFile(content, outFile, 0);
    }

    /**
     * Writes the bytes emitted by a Flux to an AsynchronousFileChannel
     * starting at the given position in the file.
     *
     * @param content the Flux content
     * @param outFile the file channel
     * @param position the position in the file to begin writing
     * @return a Mono&lt;Void&gt; which performs the write operation when subscribed
     */
    public static Mono<Void> bytebufStreamToFile(Flux<ByteBuffer> content, AsynchronousFileChannel outFile, long position) {
        return Mono.create(emitter -> content.subscribe(new ByteBufToFileSubscriber(outFile, position, emitter)));
    }

    private static class ByteBufToFileSubscriber implements Subscriber<ByteBuffer> {
        private ByteBufToFileSubscriber(AsynchronousFileChannel outFile, long position, MonoSink<Void> emitter) {
            this.outFile = outFile;
            this.pos = position;
            this.emitter = emitter;
        }

        // volatile ensures that writes to these fields by one thread will be immediately visible to other threads.
        // An I/O pool thread will write to isWriting and read isCompleted,
        // while another thread may read isWriting and write to isCompleted.
        volatile boolean isWriting = false;
        volatile boolean isCompleted = false;
        volatile Subscription subscription;
        volatile long pos;
        AsynchronousFileChannel outFile;
        MonoSink<Void> emitter;

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
                if (subscription != null) {
                    subscription.request(1);
                }
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                if (subscription != null) {
                    subscription.cancel();
                }
                emitter.error(exc);
            }
        };

        @Override
        public void onError(Throwable throwable) {
            if (subscription != null) {
                subscription.cancel();
            }
            emitter.error(throwable);
        }

        @Override
        public void onComplete() {
            isCompleted = true;
            if (!isWriting) {
                emitter.success();
            }
        }
    }

    /**
     * Creates a {@link Flux} from an {@link AsynchronousFileChannel}
     * which reads the entire file.
     *
     * @param fileChannel The file channel.
     * @return The AsyncInputStream.
     */
    public static Flux<ByteBuffer> byteBufStreamFromFile(AsynchronousFileChannel fileChannel) {
//        try {
//            long size = fileChannel.size();
//            return byteBufStreamFromFile(fileChannel, DEFAULT_CHUNK_SIZE, 0, size);
//        } catch (IOException e) {
//            return Flux.error(e);
//        }

        // TODO
        throw new IllegalStateException("This method is not yet re-implemented");
    }
}
