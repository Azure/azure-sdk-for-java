// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.implementation.http.PagedResponseBase;
import com.azure.core.util.Context;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.ReferenceCountUtil;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class FluxUtilTests {

    @Test
    public void testCanReadSlice() throws IOException {
        File file = createFileIfNotExist("target/test1");
        FileOutputStream stream = new FileOutputStream(file);
        stream.write("hello there".getBytes(StandardCharsets.UTF_8));
        stream.close();

        try (AsynchronousFileChannel channel = AsynchronousFileChannel.open(file.toPath(), StandardOpenOption.READ)) {
            byte[] bytes = FluxUtil.byteBufStreamFromFile(channel, 1, 3)
                    .map(bb -> {
                        byte[] bt = toBytes(bb);
                        ReferenceCountUtil.release(bb);
                        return bt;
                    })
                    .collect(() -> new ByteArrayOutputStream(),
                        (bos, b) -> {
                            try {
                                bos.write(b);
                            } catch (IOException ioe) {
                                throw Exceptions.propagate(ioe);
                            }
                        })
                    .block()
                    .toByteArray();
            assertEquals("ell", new String(bytes, StandardCharsets.UTF_8));
        } catch (IOException ioe) {

        }

    }

    @Test
    public void testCanReadEmptyFile() throws IOException {
        File file = createFileIfNotExist("target/test2");

        try (AsynchronousFileChannel channel = AsynchronousFileChannel.open(file.toPath(), StandardOpenOption.READ)) {
            byte[] bytes = FluxUtil.byteBufStreamFromFile(channel, 1, 3)
                    .map(bb -> {
                        byte[] bt = toBytes(bb);
                        ReferenceCountUtil.release(bb);
                        return bt;
                    })
                    .collect(() -> new ByteArrayOutputStream(),
                        (bos, b) -> {
                            try {
                                bos.write(b);
                            } catch (IOException ioe) {
                                throw Exceptions.propagate(ioe);
                            }
                        })
                    .block().toByteArray();
            assertEquals(0, bytes.length);
        }
        assertTrue(file.delete());
    }

    @Test
    public void testAsynchronyShortInput() throws IOException {
        File file = createFileIfNotExist("target/test3");
        FileOutputStream stream = new FileOutputStream(file);
        stream.write("hello there".getBytes(StandardCharsets.UTF_8));
        stream.close();
        try (AsynchronousFileChannel channel = AsynchronousFileChannel.open(file.toPath(), StandardOpenOption.READ)) {
            byte[] bytes = FluxUtil.byteBufStreamFromFile(channel)
                    .map(bb -> {
                        byte[] bt = toBytes(bb);
                        ReferenceCountUtil.release(bb);
                        return bt;
                    })
                    .limitRequest(1)
                    .subscribeOn(reactor.core.scheduler.Schedulers.newElastic("io", 30))
                    .publishOn(reactor.core.scheduler.Schedulers.newElastic("io", 30))
                    .collect(() -> new ByteArrayOutputStream(),
                        (bos, b) -> {
                            try {
                                bos.write(b);
                            } catch (IOException ioe) {
                                throw Exceptions.propagate(ioe);
                            }
                        })
                    .block()
                    .toByteArray();
            assertEquals("hello there", new String(bytes, StandardCharsets.UTF_8));
        }
        assertTrue(file.delete());
    }

    private static final int NUM_CHUNKS_IN_LONG_INPUT = 10_000_000;

    @Test
    public void testAsynchronyLongInput() throws IOException, NoSuchAlgorithmException {
        File file = createFileIfNotExist("target/test4");
        byte[] array = "1234567690".getBytes(StandardCharsets.UTF_8);
        MessageDigest digest = MessageDigest.getInstance("MD5");
        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
            for (int i = 0; i < NUM_CHUNKS_IN_LONG_INPUT; i++) {
                out.write(array);
                digest.update(array);
            }
        }
        System.out.println("long input file size=" + file.length() / (1024 * 1024) + "MB");
        byte[] expected = digest.digest();
        digest.reset();
        try (AsynchronousFileChannel channel = AsynchronousFileChannel.open(file.toPath(), StandardOpenOption.READ)) {
            FluxUtil.byteBufStreamFromFile(channel)
                    .subscribeOn(reactor.core.scheduler.Schedulers.newElastic("io", 30))
                    .publishOn(reactor.core.scheduler.Schedulers.newElastic("io", 30))
                    .toIterable().forEach(bb -> {
                        digest.update(bb.nioBuffer());
                        ReferenceCountUtil.release(bb);
                    });

            assertArrayEquals(expected, digest.digest());
        }
        assertTrue(file.delete());
    }

    @Test
    @Ignore("Need to sync with smaldini to find equivalent for rx.test.awaitDone")
    public void testBackpressureLongInput() throws IOException, NoSuchAlgorithmException {
//        File file = new File("target/test4");
//        byte[] array = "1234567690".getBytes(StandardCharsets.UTF_8);
//        MessageDigest digest = MessageDigest.getInstance("MD5");
//        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
//            for (int i = 0; i < NUM_CHUNKS_IN_LONG_INPUT; i++) {
//                out.write(array);
//                digest.update(array);
//            }
//        }
//        byte[] expected = digest.digest();
//        digest.reset();
//
//        try (AsynchronousFileChannel channel = AsynchronousFileChannel.open(file.toPath(), StandardOpenOption.READ)) {
//            FluxUtil1.byteBufferStreamFromFile(channel)
//                    .rebatchRequests(1)
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(Schedulers.io())
//                    .doOnNext(bb -> digest.update(bb))
//                    .test(0)
//                    .assertNoValues()
//                    .requestMore(1)
//                    .awaitCount(1)
//                    .assertValueCount(1)
//                    .requestMore(1)
//                    .awaitCount(2)
//                    .assertValueCount(2)
//                    .requestMore(Long.MAX_VALUE)
//                    .awaitDone(20, TimeUnit.SECONDS)
//                    .assertComplete();
//        }
//
//        assertArrayEquals(expected, digest.digest());
//        assertTrue(file.delete());
    }

    @Test
    public void testSplitForMultipleSplitSizesFromOneTo16() throws NoSuchAlgorithmException {
        ByteBuf bb = null;
        try {
            bb = Unpooled.directBuffer(1000);
            byte[] oneByte = new byte[1];
            for (int i = 0; i < 1000; i++) {
                oneByte[0] = (byte) i;
                bb.writeBytes(oneByte);
            }
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(bb.nioBuffer());
            byte[] expected = digest.digest();
            for (int size = 1; size < 16; size++) {
                System.out.println("size=" + size);
                digest.reset();
                bb.readerIndex(0);
                //
                FluxUtil.split(bb, 3).doOnNext(b -> digest.update(b.nioBuffer()))
                        .subscribe();
//
//            StepVerifier.create(FluxUtil1.split(bb, 3).doOnNext(b -> digest.update(b)))
//                    .expectNextCount(?) // TODO: ? is Unknown. Check with smaldini - what is the Verifier way to ignore all next calls and simply check stream completes?
//                    .verifyComplete();
//
                assertArrayEquals(expected, digest.digest());
            }
        } finally {
            if (bb != null) {
                bb.release();
            }
        }
    }

    @Test
    public void testSplitOnEmptyContent() {
        ByteBuf bb = null;
        try {
            bb = Unpooled.directBuffer(16);
            StepVerifier.create(FluxUtil.split(bb, 3))
                    .expectNextCount(0)
                    .expectComplete()
                    .verify();
        } finally {
            if (bb != null) {
                bb.release();
            }
        }
    }

    @Test
    public void toByteArrayWithEmptyByteBuffer() {
        assertArrayEquals(new byte[0], FluxUtil.byteBufToArray(Unpooled.wrappedBuffer(new byte[0])));
    }

    @Test
    public void toByteArrayWithNonEmptyByteBuffer() {
        final ByteBuf byteBuffer = Unpooled.wrappedBuffer(new byte[] { 0, 1, 2, 3, 4 });
        assertEquals(5, byteBuffer.readableBytes());
        final byte[] byteArray = FluxUtil.byteBufToArray(byteBuffer);
        assertArrayEquals(new byte[] { 0, 1, 2, 3, 4 }, byteArray);
        assertEquals(5, byteBuffer.readableBytes());
    }

    @Test
    public void testCollectByteBufStream() {
        Flux<ByteBuf> byteBufFlux = Flux
            .just(Unpooled.copyInt(1), Unpooled.copyInt(255), Unpooled.copyInt(256));
        Mono<ByteBuf> result = FluxUtil.collectByteBufStream(byteBufFlux, false);
        byte[] bytes = ByteBufUtil.getBytes(result.block());
        assertEquals(12, bytes.length);
        assertArrayEquals(new byte[]{
            0, 0, 0, 1,
            0, 0, 0, (byte) 255,
            0, 0, 1, 0}, bytes);
    }

    @Test
    public void testToMono() {
        String value = "test";
        Assert.assertEquals(getMonoRestResponse(value).flatMap(FluxUtil::toMono).block(), value);
        Assert.assertEquals(getMonoRestResponse("").flatMap(FluxUtil::toMono).block(), "");
    }

    @Test
    public void testCallWithContextGetSingle() {
        String response = getSingle("Hello, ")
            .subscriberContext(reactor.util.context.Context.of("FirstName", "Foo", "LastName", "Bar"))
            .block();
        Assert.assertEquals("Hello, Foo Bar", response);
    }

    @Test
    public void testCallWithContextGetCollection() {
        List<String> expectedLines = Arrays.asList("Hello,", "Foo", "Bar");
        List<String> actualLines = new ArrayList<>();
        getCollection("Hello, ")
            .subscriberContext(reactor.util.context.Context.of("FirstName", "Foo", "LastName", "Bar"))
            .doOnNext(line -> actualLines.add(line))
            .subscribe();
        Assert.assertEquals(expectedLines, actualLines);
    }

    @Test
    public void testCallWithContextGetPagedCollection() throws Exception {
        // Simulates the customer code that includes context
        getPagedCollection()
            .subscriberContext(
                reactor.util.context.Context.of("Key1", "Val1", "Key2", "Val2"))
            .doOnNext(System.out::println)
            .subscribe();
    }

    private PagedFlux<Integer> getPagedCollection()
        throws Exception {
        // Simulates the client library API
        List<PagedResponse<Integer>> pagedResponses = getPagedResponses(4);
        return new PagedFlux<>(
            () -> FluxUtil.withContext(context -> getFirstPage(pagedResponses, context)),
            continuationToken -> FluxUtil
                .withContext(context -> getNextPage(continuationToken, pagedResponses, context)));
    }

    private List<PagedResponse<Integer>> getPagedResponses(int noOfPages)
        throws MalformedURLException {
        HttpHeaders httpHeaders = new HttpHeaders().put("header1", "value1")
            .put("header2", "value2");
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, new URL("http://localhost"));
        String deserializedHeaders = "header1,value1,header2,value2";
        return IntStream.range(0, noOfPages)
            .boxed()
            .map(i -> createPagedResponse(httpRequest, httpHeaders, deserializedHeaders, i, noOfPages))
            .collect(Collectors.toList());
    }

    private Mono<PagedResponse<Integer>> getFirstPage(List<PagedResponse<Integer>> pagedResponses,
        Context context) {
        // Simulates the service side code which should get the context provided by customer code
        Assert.assertEquals("Val1", context.getData("Key1").get());
        return pagedResponses.isEmpty() ? Mono.empty() : Mono.just(pagedResponses.get(0));
    }

    private Mono<PagedResponse<Integer>> getNextPage(String continuationToken,
        List<PagedResponse<Integer>> pagedResponses, Context context) {
        // Simulates the service side code which should get the context provided by customer code
        Assert.assertEquals("Val2", context.getData("Key2").get());
        if (continuationToken == null || continuationToken.isEmpty()) {
            return Mono.empty();
        }
        return Mono.just(pagedResponses.get(Integer.valueOf(continuationToken)));
    }

    private PagedResponseBase<String, Integer> createPagedResponse(HttpRequest httpRequest,
        HttpHeaders httpHeaders, String deserializedHeaders, int i, int noOfPages) {
        return new PagedResponseBase<>(httpRequest, HttpResponseStatus.OK.code(),
            httpHeaders,
            getItems(i),
            i < noOfPages - 1 ? String.valueOf(i + 1) : null,
            deserializedHeaders);
    }

    private List<Integer> getItems(Integer i) {
        return IntStream.range(i * 3, i * 3 + 3).boxed().collect(Collectors.toList());
    }


    private Mono<String> getSingle(String prefix) {
        return FluxUtil.withContext(context -> serviceCallSingle(prefix, context));
    }

    private Flux<String> getCollection(String prefix) {
        return FluxUtil
            .fluxContext(context -> serviceCallCollection(prefix, context));
    }

    private Mono<String> serviceCallSingle(String prefix, Context context) {
        String msg = prefix
            + context.getData("FirstName").orElse("Stranger")
            + " "
            + context.getData("LastName").orElse("");
        return Mono.just(msg);
    }

    private Flux<String> serviceCallCollection(String prefix, Context context) {
        String msg = prefix
            + context.getData("FirstName").orElse("Stranger")
            + " "
            + context.getData("LastName").orElse("");

        return Flux.just(msg.split(" "));
    }
//
    private static byte[] toBytes(ByteBuf bb) {
        byte[] bytes = new byte[bb.readableBytes()];
        bb.readBytes(bytes);
        return bytes;
    }

    private File createFileIfNotExist(String fileName) throws IOException {
        File file = new File(fileName);
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }
        file.createNewFile();
        return file;
    }

    private <T> Mono<Response<T>> getMonoRestResponse(T value) {
        Response<T> response = new Response<T>() {
            @Override
            public int statusCode() {
                return 200;
            }

            @Override
            public HttpHeaders headers() {
                return null;
            }

            @Override
            public HttpRequest request() {
                return null;
            }

            @Override
            public T value() {
                return value;
            }
        };
        return Mono.just(response);
    }

}
