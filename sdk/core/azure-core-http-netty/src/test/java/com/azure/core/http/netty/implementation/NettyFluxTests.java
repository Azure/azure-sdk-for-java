// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.ReferenceCountUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NettyFluxTests {

    @Test
    public void testCanReadSlice() throws IOException {
        File file = createFileIfNotExist("target/test1");
        FileOutputStream stream = new FileOutputStream(file);
        stream.write("hello there".getBytes(StandardCharsets.UTF_8));
        stream.close();

        try (AsynchronousFileChannel channel = AsynchronousFileChannel.open(file.toPath(), StandardOpenOption.READ)) {
            byte[] bytes = NettyFluxTestUtils.byteBufStreamFromFile(channel, 1, 3)
                .map(bb -> {
                    byte[] bt = toBytes(bb);
                    ReferenceCountUtil.release(bb);
                    return bt;
                })
                .collect(ByteArrayOutputStream::new,
                    (bos, b) -> {
                        try {
                            bos.write(b);
                        } catch (IOException ioe) {
                            throw Exceptions.propagate(ioe);
                        }
                    })
                .map(ByteArrayOutputStream::toByteArray)
                .block();

            assertEquals("ell", new String(bytes, StandardCharsets.UTF_8));
        }

        assertTrue(file.delete());
    }

    @Test
    public void testCanReadEmptyFile() throws IOException {
        File file = createFileIfNotExist("target/test2");

        try (AsynchronousFileChannel channel = AsynchronousFileChannel.open(file.toPath(), StandardOpenOption.READ)) {
            byte[] bytes = NettyFluxTestUtils.byteBufStreamFromFile(channel, 1, 3)
                .map(bb -> {
                    byte[] bt = bb.array();
                    ReferenceCountUtil.release(bb);
                    return bt;
                })
                .collect(ByteArrayOutputStream::new,
                    (bos, b) -> {
                        try {
                            bos.write(b);
                        } catch (IOException ioe) {
                            throw Exceptions.propagate(ioe);
                        }
                    })
                .map(ByteArrayOutputStream::toByteArray)
                .block();

            assertTrue(bytes != null && bytes.length == 0);
        }

        assertTrue(file.delete());
    }

    @Test
    public void testAsynchronousShortInput() throws IOException {
        File file = createFileIfNotExist("target/test3");
        FileOutputStream stream = new FileOutputStream(file);
        stream.write("hello there".getBytes(StandardCharsets.UTF_8));
        stream.close();
        try (AsynchronousFileChannel channel = AsynchronousFileChannel.open(file.toPath(), StandardOpenOption.READ)) {
            byte[] bytes = FluxUtil.readFile(channel)
                .map(bb -> {
                    byte[] bt = new byte[bb.remaining()];
                    bb.get(bt);
                    return bt;
                })
                .limitRequest(1)
                .subscribeOn(Schedulers.newBoundedElastic(30, 1024, "io"))
                .publishOn(Schedulers.newBoundedElastic(30, 1024, "io"))
                .collect(ByteArrayOutputStream::new,
                    (bos, b) -> {
                        try {
                            bos.write(b);
                        } catch (IOException ioe) {
                            throw Exceptions.propagate(ioe);
                        }
                    })
                .map(ByteArrayOutputStream::toByteArray)
                .block();
            assertEquals("hello there", new String(bytes, StandardCharsets.UTF_8));
        }

        assertTrue(file.delete());
    }

    private static final int NUM_CHUNKS_IN_LONG_INPUT = 10_000_000;

    @Test
    public void testAsynchronousLongInput() throws IOException, NoSuchAlgorithmException {
        File file = createFileIfNotExist("target/test5");
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
            StepVerifier.create(FluxUtil.readFile(channel)
                .doOnNext(buffer -> digest.update(buffer.duplicate())))
                .thenConsumeWhile(ByteBuffer::hasRemaining)
                .verifyComplete();

            assertArrayEquals(expected, digest.digest());
        }

        assertTrue(file.delete());
    }

    @Test
    public void testBackpressureLongInput() throws IOException, NoSuchAlgorithmException {
        File file = new File("target/test4");
        byte[] array = "1234567690".getBytes(StandardCharsets.UTF_8);
        MessageDigest digest = MessageDigest.getInstance("MD5");
        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
            for (int i = 0; i < NUM_CHUNKS_IN_LONG_INPUT; i++) {
                out.write(array);
                digest.update(array);
            }
        }

        byte[] expected = digest.digest();
        digest.reset();

        try (AsynchronousFileChannel channel = AsynchronousFileChannel.open(file.toPath(), StandardOpenOption.READ)) {
            StepVerifier.create(FluxUtil.readFile(channel)
                .subscribeOn(Schedulers.boundedElastic())
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(buffer -> digest.update(buffer.duplicate())))
                .thenRequest(1)
                .expectNextCount(1)
                .thenRequest(2)
                .expectNextCount(2)
                .thenRequest(Long.MAX_VALUE)
                .thenConsumeWhile(ByteBuffer::hasRemaining)
                .expectComplete()
                .verify(Duration.ofSeconds(20));
        }

        assertArrayEquals(expected, digest.digest());
        assertTrue(file.delete());
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
                NettyFluxTestUtils.split(bb, 3)
                    .doOnNext(b -> digest.update(b.nioBuffer()))
                    .subscribe();

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
            StepVerifier.create(NettyFluxTestUtils.split(bb, 3))
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
    public void testCallWithContextGetPagedCollection() throws Exception {
        // Simulates the customer code that includes context
        getPagedCollection()
            .contextWrite(reactor.util.context.Context.of("Key1", "Val1", "Key2", "Val2"))
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
        Assertions.assertEquals("Val1", context.getData("Key1").get());
        return pagedResponses.isEmpty() ? Mono.empty() : Mono.just(pagedResponses.get(0));
    }

    private Mono<PagedResponse<Integer>> getNextPage(String continuationToken,
        List<PagedResponse<Integer>> pagedResponses, Context context) {
        // Simulates the service side code which should get the context provided by customer code
        Assertions.assertEquals("Val2", context.getData("Key2").get());
        if (continuationToken == null || continuationToken.isEmpty()) {
            return Mono.empty();
        }
        return Mono.just(pagedResponses.get(Integer.parseInt(continuationToken)));
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
            public int getStatusCode() {
                return 200;
            }

            @Override
            public HttpHeaders getHeaders() {
                return null;
            }

            @Override
            public HttpRequest getRequest() {
                return null;
            }

            @Override
            public T getValue() {
                return value;
            }
        };
        return Mono.just(response);
    }
}
