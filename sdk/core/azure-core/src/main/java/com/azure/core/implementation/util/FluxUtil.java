// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import com.azure.core.util.Context;

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

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.core.publisher.Operators;

/**
 * Utility type exposing methods to deal with {@link Flux}.
 */
public final class FluxUtil {
    /**
     * Checks if a type is Flux&lt;ByteBuf&gt;.
     *
     * @param entityType the type to check
     * @return whether the type represents a Flux that emits ByteBuf
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
     * @param stream A stream which emits ByteBuf instances.
     * @param autoReleaseEnabled if ByteBuffer instances in stream gets automatically released as they consumed
     * @return A Mono which emits the concatenation of all the ByteBuf instances given by the source Flux.
     */
    public static Mono<byte[]> collectBytesInByteBufferStream(Flux<ByteBuffer> stream, boolean autoReleaseEnabled) {
//        if (autoReleaseEnabled) {
//            // A stream is auto-release enabled means - the ByteBuf chunks in the stream get
//            // released as consumer consumes each chunk.
//            return Mono.using(Unpooled::compositeBuffer,
//                cbb -> stream.collect(() -> cbb,
//                    (cbb1, buffer) -> cbb1.addComponent(true, Unpooled.wrappedBuffer(buffer).retain())),
//                    ReferenceCountUtil::release)
//                    .filter((CompositeByteBuf cbb) -> cbb.isReadable())
//                    .map(FluxUtil::byteBufferToArray);
//        } else {
//            return stream.collect(Unpooled::compositeBuffer,
//                (cbb1, buffer) -> cbb1.addComponent(true, Unpooled.wrappedBuffer(buffer)))
//                    .filter((CompositeByteBuf cbb) -> cbb.isReadable())
//                    .map(FluxUtil::byteBufferToArray);
//        }

        // TODO this is not a good implementation
        return stream
                   .collect(ByteArrayOutputStream::new, FluxUtil::accept)
                   .map(ByteArrayOutputStream::toByteArray);
    }

    private static void accept(ByteArrayOutputStream byteOutputStream, ByteBuffer byteBuffer) {
        try {
            byteOutputStream.write(byteBuffer.array());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the content of the provided ByteBuf as a byte array.
     * This method will create a new byte array even if the ByteBuf can
     * have optionally backing array.
     *
     *
     * @param byteBuf the byte buffer
     * @return the byte array
     */
    public static byte[] byteBufferToArray(ByteBuffer byteBuf) {
//        int length = byteBuf.readableBytes();
//        byte[] byteArray = new byte[length];
//        byteBuf.getBytes(byteBuf.readerIndex(), byteArray);
//        return byteArray;

        // FIXME this is not good code!
        return byteBuf.array();
    }

    /**
     * This method converts the incoming {@code subscriberContext} from {@link reactor.util.context.Context Reactor
     * Context} to {@link Context Azure Context} and calls the given lambda function with this context and returns a
     * single entity of type {@code T}
     * <p>
     *  If the reactor context is empty, {@link Context#NONE} will be used to call the lambda function
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
     * This method converts the incoming {@code subscriberContext} from {@link reactor.util.context.Context Reactor
     * Context} to {@link Context Azure Context} and calls the given lambda function with this context and returns a
     * collection of type {@code T}
     * <p>
     *  If the reactor context is empty, {@link Context#NONE} will be used to call the lambda function
     * </p>
     *
     *  <p><strong>Code samples</strong></p>
     *  {@codesnippet com.azure.core.implementation.util.fluxutil.fluxcontext}
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
     * Converts a reactor context to azure context. If the reactor context is {@code null} or empty,
     * {@link Context#NONE} will be returned.
     *
     * @param context The reactor context
     * @return The azure context
     */
    private static Context toAzureContext(reactor.util.context.Context context) {
        Map<Object, Object> keyValues = context.stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        if (ImplUtils.isNullOrEmpty(keyValues)) {
            return Context.NONE;
        }
        return Context.of(keyValues);
    }



    // Private Ctr
    private FluxUtil() {
    }
}
