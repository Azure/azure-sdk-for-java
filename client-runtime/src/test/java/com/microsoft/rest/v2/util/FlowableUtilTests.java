package com.microsoft.rest.v2.util;

import static com.microsoft.rest.v2.util.FlowableUtil.ensureLength;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import org.junit.Test;

import com.google.common.io.Files;

import io.reactivex.schedulers.Schedulers;

public class FlowableUtilTests {
    @Test
    public void testCountingNotEnoughBytesEmitsError() {
        Flowable<ByteBuffer> content = Flowable.just(ByteBuffer.allocate(4));
        content.compose(ensureLength(8))
                .test()
                .assertError(IllegalArgumentException.class);
    }

    @Test
    public void testCountingTooManyBytesEmitsError() {
        Flowable<ByteBuffer> content = Flowable.just(ByteBuffer.allocate(4));
        content.compose(ensureLength(1))
                .test()
                .assertError(IllegalArgumentException.class);
    }


    @Test
    public void testCountingTooManyBytesCancelsSubscription() {
        Flowable<ByteBuffer> content = Flowable.just(ByteBuffer.allocate(4)).concatWith(Flowable.never());
        content.compose(ensureLength(1))
                .test()
                .awaitDone(1, TimeUnit.SECONDS)
                .assertError(IllegalArgumentException.class);
    }

    @Test
    public void testCountingExpectedNumberOfBytesSucceeds() {
        Flowable<ByteBuffer> content = Flowable.just(ByteBuffer.allocate(4));
        content.compose(ensureLength(4))
                .test()
                .assertComplete();
    }

    @Test
    public void testCanReadSlice() throws IOException {
        File file = new File("target/test1");
        Files.write("hello there".getBytes(StandardCharsets.UTF_8), file);
        try (AsynchronousFileChannel channel = AsynchronousFileChannel.open(file.toPath(), StandardOpenOption.READ)) {
            byte[] bytes = FlowableUtil.readFile(channel, 1, 3) //
                    .map(bb -> toBytes(bb)) //
                    .collectInto(new ByteArrayOutputStream(), (bos, b) -> bos.write(b)) //
                    .blockingGet().toByteArray();
            assertEquals("ell", new String(bytes, StandardCharsets.UTF_8));
        }
    }

    @Test
    public void testCanReadEmptyFile() throws IOException {
        File file = new File("target/test2");
        file.createNewFile();
        try (AsynchronousFileChannel channel = AsynchronousFileChannel.open(file.toPath(), StandardOpenOption.READ)) {
            byte[] bytes = FlowableUtil.readFile(channel, 1, 3) //
                    .map(bb -> toBytes(bb)) //
                    .collectInto(new ByteArrayOutputStream(), OutputStream::write) //
                    .blockingGet().toByteArray();
            assertEquals(0, bytes.length);
        }
        assertTrue(file.delete());
    }

    @Test
    public void testAsynchronyShortInput() throws IOException {
        File file = new File("target/test3");
        Files.write("hello there".getBytes(StandardCharsets.UTF_8), file);
        try (AsynchronousFileChannel channel = AsynchronousFileChannel.open(file.toPath(), StandardOpenOption.READ)) {
            byte[] bytes = FlowableUtil.readFile(channel) //
                    .map(bb -> toBytes(bb)) //
                    .rebatchRequests(1) //
                    .subscribeOn(Schedulers.io()) //
                    .observeOn(Schedulers.io()) //
                    .collectInto(new ByteArrayOutputStream(), (bos, b) -> bos.write(b)) //
                    .blockingGet() //
                    .toByteArray();
            assertEquals("hello there", new String(bytes, StandardCharsets.UTF_8));
        }
        assertTrue(file.delete());
    }

    private static final int NUM_CHUNKS_IN_LONG_INPUT = 10_000_000;

    @Test
    public void testAsynchronyLongInput() throws IOException, NoSuchAlgorithmException {
        File file = new File("target/test4");
        byte[] array = "1234567690".getBytes(StandardCharsets.UTF_8);
        MessageDigest digest = MessageDigest.getInstance("MD5");
        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
            for (int i = 0; i < NUM_CHUNKS_IN_LONG_INPUT; i++) {
                out.write(array);
                digest.update(array);
            }
        }
        System.out.println("long input file size="+ file.length()/(1024*1024) + "MB");
        byte[] expected = digest.digest();
        digest.reset();
        try (AsynchronousFileChannel channel = AsynchronousFileChannel.open(file.toPath(), StandardOpenOption.READ)) {
            FlowableUtil.readFile(channel) //
                    .rebatchRequests(1) //
                    .subscribeOn(Schedulers.io()) //
                    .observeOn(Schedulers.io()) //
                    .blockingForEach(bb -> digest.update(bb));

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
            FlowableUtil.readFile(channel) //
                    .rebatchRequests(1) //
                    .subscribeOn(Schedulers.io()) //
                    .observeOn(Schedulers.io()) //
                    .doOnNext(bb -> digest.update(bb)) //
                    .test(0) //
                    .assertNoValues() //
                    .requestMore(1) //
                    .awaitCount(1) //
                    .assertValueCount(1)
                    .requestMore(1) //
                    .awaitCount(2) //
                    .assertValueCount(2) //
                    .requestMore(Long.MAX_VALUE) //
                    .awaitDone(20, TimeUnit.SECONDS) //
                    .assertComplete();
        }

        assertArrayEquals(expected, digest.digest());
        assertTrue(file.delete());
    }
    
    @Test
    public void testSplitForMultipleSplitSizesFromOneTo16() throws NoSuchAlgorithmException {
        ByteBuffer bb = ByteBuffer.allocateDirect(1000);
        for (int i = 0;i < 1000;i++) {
            bb.put((byte) i);
        }
        bb.flip();
        MessageDigest digest = MessageDigest.getInstance("MD5");
        digest.update(bb);
        byte[] expected = digest.digest();
        for (int size=1; size<16; size++) {
            System.out.println("size="+ size);
            digest.reset();
            bb.position(0);
            FlowableUtil //
                .split(bb, 3) //
                .doOnNext(b -> digest.update(b)) //
                .test()
                .assertComplete();
            assertArrayEquals(expected, digest.digest());
        }
    }
    
    @Test
    public void testSplitOnEmptyContent() {
        ByteBuffer bb = ByteBuffer.allocateDirect(16);
        bb.flip();
        FlowableUtil //
            .split(bb, 3) //
            .test() //
            .assertValueCount(0) //
            .assertComplete();
    }

    private static byte[] toBytes(ByteBuffer bb) {
        byte[] bytes = new byte[bb.remaining()];
        bb.get(bytes);
        return bytes;
    }

}
