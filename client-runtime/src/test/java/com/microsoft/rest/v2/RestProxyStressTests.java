/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2;

import com.google.common.io.BaseEncoding;
import com.microsoft.rest.v2.annotations.*;
import com.microsoft.rest.v2.http.*;
import com.microsoft.rest.v2.policy.AddHeadersPolicy;
import com.microsoft.rest.v2.policy.LoggingPolicy;
import com.microsoft.rest.v2.policy.LoggingPolicy.LogLevel;
import com.microsoft.rest.v2.policy.RequestPolicy;
import com.microsoft.rest.v2.policy.RequestPolicyFactory;
import com.microsoft.rest.v2.policy.RequestPolicyOptions;
import com.microsoft.rest.v2.policy.RetryPolicy;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.internal.functions.Functions;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormat;
import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileChannel;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

@Ignore("Should only be run manually")
public class RestProxyStressTests {
    static class AddDatePolicy implements RequestPolicy {
        private final DateTimeFormatter format = DateTimeFormat
                .forPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'")
                .withZoneUTC()
                .withLocale(Locale.US);

        private final RequestPolicy next;
        AddDatePolicy(RequestPolicy next) {
            this.next = next;
        }

        @Override
        public Single<HttpResponse> sendAsync(HttpRequest request) {
            request.headers().set("Date", format.print(DateTime.now()));
            return next.sendAsync(request);
        }

        static class Factory implements RequestPolicyFactory {
            @Override
            public RequestPolicy create(RequestPolicy next, RequestPolicyOptions options) {
                return new AddDatePolicy(next);
            }
        }
    }

    @AfterClass
    public static void teardown() throws IOException {
        deleteRecursive(Paths.get("temp"));
    }

    @Host("https://javasdktest.blob.core.windows.net")
    interface IOService {
        @GET("/javasdktest/download/1k.dat?{sas}")
        Single<RestResponse<Void, Flowable<byte[]>>> download1KB(@PathParam(value = "sas", encoded = true) String sas);

        @GET("/javasdktest/download/1m.dat?{sas}")
        Single<RestResponse<Void, Flowable<byte[]>>> download1MB(@PathParam(value = "sas", encoded = true) String sas);

        @GET("/javasdktest/download/90m.dat?{sas}")
        Single<RestResponse<Void, Flowable<byte[]>>> download90MB(@PathParam(value = "sas", encoded = true) String sas);

        @GET("/javasdktest/download/1g.dat?{sas}")
        Single<RestResponse<Void, Flowable<byte[]>>> download1GB(@PathParam(value = "sas", encoded = true) String sas);

        @ExpectedResponses({ 201 })
        @PUT("/javasdktest/upload/1m.dat?{sas}")
        Single<RestResponse<Void, Void>> upload1MBBytes(@PathParam(value = "sas", encoded = true) String sas, @HeaderParam("x-ms-blob-type") String blobType, @BodyParam(ContentType.APPLICATION_OCTET_STREAM) byte[] bytes);

        @ExpectedResponses({ 201 })
        @PUT("/javasdktest/upload/1m-{id}.dat?{sas}")
        Single<RestResponse<Void, Void>> upload1MB(@PathParam("id") String id, @PathParam(value = "sas", encoded = true) String sas, @HeaderParam("x-ms-blob-type") String blobType, @BodyParam(ContentType.APPLICATION_OCTET_STREAM) AsyncInputStream stream);

        @ExpectedResponses({ 201 })
        @PUT("/javasdktest/upload/1m-{id}.dat?{sas}")
        Single<RestResponse<Void, Void>> upload1MBFile(@PathParam("id") String id, @PathParam(value = "sas", encoded = true) String sas, @HeaderParam("x-ms-blob-type") String blobType, @BodyParam(ContentType.APPLICATION_OCTET_STREAM) FileSegment segment);

        @ExpectedResponses({ 201 })
        @PUT("/javasdktest/upload/10m-{id}.dat?{sas}")
        Single<RestResponse<Void, Void>> upload10MB(@PathParam("id") String id, @PathParam(value = "sas", encoded = true) String sas, @HeaderParam("x-ms-blob-type") String blobType, @BodyParam(ContentType.APPLICATION_OCTET_STREAM) AsyncInputStream stream);

        @ExpectedResponses({ 201 })
        @PUT("/javasdktest/upload/10m-{id}.dat?{sas}")
        Single<RestResponse<Void, Void>> upload10MBFile(@PathParam("id") String id, @PathParam(value = "sas", encoded = true) String sas, @HeaderParam("x-ms-blob-type") String blobType, @BodyParam(ContentType.APPLICATION_OCTET_STREAM) FileSegment segment);

        @ExpectedResponses({ 201 })
        @PUT("/javasdktest/upload/100m-{id}.dat?{sas}")
        Single<RestResponse<Void, Void>> upload100MB(@PathParam("id") String id, @PathParam(value = "sas", encoded = true) String sas, @HeaderParam("x-ms-blob-type") String blobType, @BodyParam(ContentType.APPLICATION_OCTET_STREAM) AsyncInputStream stream);

        @ExpectedResponses({ 201 })
        @PUT("/javasdktest/upload/100m-{id}.dat?{sas}")
        Single<RestResponse<Void, Void>> upload100MBFile(@PathParam("id") String id, @PathParam(value = "sas", encoded = true) String sas, @HeaderParam("x-ms-blob-type") String blobType, @BodyParam(ContentType.APPLICATION_OCTET_STREAM) FileSegment segment);
    }

    private static final byte[] MD5_1KB = { 70, -110, 110, -84, -35, 116, 118, 2, -22, 8, 117, -65, -106, 61, -36, 58 };
    private static final byte[] MD5_1MB = { -128, 96, 94, 57, -95, -42, 40, 124, -5, 10, 124, -5, 59, -81, 122, 38 };
    private static final byte[] MD5_90MB = { 44, 39, 103, 103, -88, 8, -94, 85, 53, 79, -115, -70, 14, 82, -68, -63 };
    private static final byte[] MD5_1GB = { 43, -104, -23, 103, 42, 34, -49, 42, 57, -127, -128, 89, -36, -81, 67, 5 };

    private static final Path tempFolderPath = Paths.get("temp");

    @Test
    public void upload100MTest() throws Exception {
        final String sas = System.getenv("JAVA_SDK_TEST_SAS");
        HttpHeaders headers = new HttpHeaders()
                .set("x-ms-version", "2017-04-17");

        HttpPipeline pipeline = HttpPipeline.build(
                new AddDatePolicy.Factory(),
                new AddHeadersPolicy.Factory(headers),
                new LoggingPolicy.Factory(LogLevel.HEADERS));

        final IOService service = RestProxy.create(IOService.class, pipeline);

        final Path filePath = Paths.get("100m.dat");
        try {
            Files.deleteIfExists(filePath);
            Files.createFile(filePath);
            FileChannel file = FileChannel.open(filePath, StandardOpenOption.READ, StandardOpenOption.WRITE);

            byte[] buf = new byte[1024 * 1024 * 100];
            Random random = new Random();
            random.nextBytes(buf);

            byte[] md5 = MessageDigest.getInstance("MD5").digest(buf);
            file.write(ByteBuffer.wrap(buf));
            file.close();

            final AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(filePath);
            AsyncInputStream fileStream = AsyncInputStream.create(fileChannel);
            RestResponse<Void, Void> response = service.upload100MB("single", sas, "BlockBlob", fileStream).blockingGet();
            fileChannel.close();
            String base64MD5 = response.rawHeaders().get("Content-MD5");
            byte[] receivedMD5 = BaseEncoding.base64().decode(base64MD5);

            assertArrayEquals(md5, receivedMD5);
        } finally {
            Files.deleteIfExists(filePath);
        }
    }

    @Test
    public void upload1MParallelTest() throws Exception {
        final String sas = System.getenv("JAVA_SDK_TEST_SAS");
        HttpHeaders headers = new HttpHeaders()
                .set("x-ms-version", "2017-04-17");

        HttpPipeline pipeline = HttpPipeline.build(
                new AddHeadersPolicy.Factory(headers),
                new RetryPolicy.Factory(2),
                new AddDatePolicy.Factory(),
                new LoggingPolicy.Factory(LogLevel.BASIC));

        final IOService service = RestProxy.create(IOService.class, pipeline);
        deleteRecursive(tempFolderPath);
        Files.createDirectory(tempFolderPath);

        final byte[] buf = new byte[1024 * 1024];
        Flowable.range(0, 100)
                .flatMapCompletable(new Function<Integer, Completable>() {
                    @Override
                    public Completable apply(Integer i) throws Exception {
                        final int id = i;
                        final Path filePath = tempFolderPath.resolve("1m-" + id + ".dat");

                        Files.deleteIfExists(filePath);
                        Files.createFile(filePath);
                        FileChannel file = FileChannel.open(filePath, StandardOpenOption.READ, StandardOpenOption.WRITE);

                        Random random = new Random();
                        random.nextBytes(buf);

                        final byte[] md5 = MessageDigest.getInstance("MD5").digest(buf);
                        file.write(ByteBuffer.wrap(buf));
                        file.close();

                        AsyncInputStream fileStream = AsyncInputStream.create(AsynchronousFileChannel.open(filePath));
                        return service.upload1MB(String.valueOf(id), sas, "BlockBlob", fileStream).flatMapCompletable(new Function<RestResponse<Void, Void>, CompletableSource>() {
                            @Override
                            public CompletableSource apply(RestResponse<Void, Void> response) throws Exception {
                                String base64MD5 = response.rawHeaders().get("Content-MD5");
                                byte[] receivedMD5 = BaseEncoding.base64().decode(base64MD5);
                                assertArrayEquals(md5, receivedMD5);
                                LoggerFactory.getLogger(getClass()).info("Finished upload for id " + id);
                                return Completable.complete();
                            }
                        });
                    }
                }).blockingAwait();
    }

    @Test
    public void upload1MPooledParallelTest() throws Exception {
        final String sas = System.getenv("JAVA_SDK_TEST_SAS");
        HttpHeaders headers = new HttpHeaders()
                .set("x-ms-version", "2017-04-17");

        HttpPipeline pipeline = HttpPipeline.build(
                new AddHeadersPolicy.Factory(headers),
                new RetryPolicy.Factory(2),
                new AddDatePolicy.Factory(),
                new LoggingPolicy.Factory(LogLevel.BASIC));

        final IOService service = RestProxy.create(IOService.class, pipeline);
        deleteRecursive(tempFolderPath);
        Files.createDirectory(tempFolderPath);

        final byte[] buf = new byte[1024 * 1024];
        Flowable.range(0, 1)
                .flatMapCompletable(new Function<Integer, Completable>() {
                    @Override
                    public Completable apply(Integer i) throws Exception {
                        final int id = i;
                        final Path filePath = tempFolderPath.resolve("1m-" + id + ".dat");

                        Files.deleteIfExists(filePath);
                        Files.createFile(filePath);
                        FileChannel file = FileChannel.open(filePath, StandardOpenOption.READ, StandardOpenOption.WRITE);

                        Random random = new Random();
                        random.nextBytes(buf);

                        final byte[] md5 = MessageDigest.getInstance("MD5").digest(buf);
                        file.write(ByteBuffer.wrap(buf));
                        file.close();

                        final FileChannel fileChannel = FileChannel.open(filePath);
                        FileSegment fileSegment = new FileSegment(fileChannel, 0, fileChannel.size());
                        return service.upload1MBFile(String.valueOf(id), sas, "BlockBlob", fileSegment).flatMapCompletable(new Function<RestResponse<Void, Void>, CompletableSource>() {
                            @Override
                            public CompletableSource apply(RestResponse<Void, Void> response) throws Exception {
                                fileChannel.close();
                                String base64MD5 = response.rawHeaders().get("Content-MD5");
                                byte[] receivedMD5 = BaseEncoding.base64().decode(base64MD5);
                                assertArrayEquals(md5, receivedMD5);
                                LoggerFactory.getLogger(getClass()).info("Finished upload for id " + id);
                                return Completable.complete();
                            }
                        });
                    }
                }).blockingAwait();
    }

    @Test
    public void upload10MParallelPooledTest() throws Exception {
        final String sas = System.getenv("JAVA_SDK_TEST_SAS");
        HttpHeaders headers = new HttpHeaders()
                .set("x-ms-version", "2017-04-17");

        HttpPipeline pipeline = HttpPipeline.build(
                new AddHeadersPolicy.Factory(headers),
                new RetryPolicy.Factory(2),
                new AddDatePolicy.Factory(),
                new LoggingPolicy.Factory(LogLevel.BASIC));

        final IOService service = RestProxy.create(IOService.class, pipeline);
        deleteRecursive(tempFolderPath);
        Files.createDirectory(tempFolderPath);

        final byte[] buf = new byte[1024 * 1024 * 10];
        Flowable.range(0, 50)
                .flatMapCompletable(new Function<Integer, Completable>() {
                    @Override
                    public Completable apply(Integer i) throws Exception {
                        final int id = i;
                        final Path filePath = tempFolderPath.resolve("10m-" + id + ".dat");

                        Files.deleteIfExists(filePath);
                        Files.createFile(filePath);
                        FileChannel file = FileChannel.open(filePath, StandardOpenOption.READ, StandardOpenOption.WRITE);

                        Random random = new Random();
                        random.nextBytes(buf);

                        final byte[] md5 = MessageDigest.getInstance("MD5").digest(buf);
                        file.write(ByteBuffer.wrap(buf));
                        file.close();

                        final FileChannel fileChannel = FileChannel.open(filePath);
                        FileSegment fileSegment = new FileSegment(fileChannel, 0, fileChannel.size());
                        return service.upload10MBFile(String.valueOf(id), sas, "BlockBlob", fileSegment).flatMapCompletable(new Function<RestResponse<Void, Void>, CompletableSource>() {
                            @Override
                            public CompletableSource apply(RestResponse<Void, Void> response) throws Exception {
                                fileChannel.close();
                                String base64MD5 = response.rawHeaders().get("Content-MD5");
                                byte[] receivedMD5 = BaseEncoding.base64().decode(base64MD5);
                                assertArrayEquals(md5, receivedMD5);
                                LoggerFactory.getLogger(getClass()).info("Finished upload for id " + id);
                                return Completable.complete();
                            }
                        });

                    }
                }).blockingAwait();
    }

    @Test
    public void upload100MParallelPooledTest() throws Exception {
        final String sas = System.getenv("JAVA_SDK_TEST_SAS");
        HttpHeaders headers = new HttpHeaders()
                .set("x-ms-version", "2017-04-17");

        HttpPipeline pipeline = HttpPipeline.build(
                new AddDatePolicy.Factory(),
                new AddHeadersPolicy.Factory(headers),
                new LoggingPolicy.Factory(LogLevel.BASIC));

        final IOService service = RestProxy.create(IOService.class, pipeline);
        deleteRecursive(tempFolderPath);
        Files.createDirectory(tempFolderPath);

        final byte[] buf = new byte[1024 * 1024 * 100];
        Flowable.range(0, 100)
                .flatMapCompletable(new Function<Integer, Completable>() {
                    @Override
                    public Completable apply(Integer i) throws Exception {
                        final int id = i;
                        final Path filePath = tempFolderPath.resolve("100m-" + id + ".dat");

                        Files.deleteIfExists(filePath);
                        Files.createFile(filePath);
                        FileChannel file = FileChannel.open(filePath, StandardOpenOption.READ, StandardOpenOption.WRITE);

                        Random random = new Random();
                        random.nextBytes(buf);

                        final byte[] md5 = MessageDigest.getInstance("MD5").digest(buf);
                        file.write(ByteBuffer.wrap(buf));
                        file.close();

                        final FileChannel fileChannel = FileChannel.open(filePath);
                        FileSegment fileSegment = new FileSegment(fileChannel, 0, fileChannel.size());
                        return service.upload100MBFile(String.valueOf(id), sas, "BlockBlob", fileSegment).flatMapCompletable(new Function<RestResponse<Void, Void>, CompletableSource>() {
                            @Override
                            public CompletableSource apply(RestResponse<Void, Void> response) throws Exception {
                                fileChannel.close();
                                String base64MD5 = response.rawHeaders().get("Content-MD5");
                                byte[] receivedMD5 = BaseEncoding.base64().decode(base64MD5);
                                assertArrayEquals(md5, receivedMD5);
                                LoggerFactory.getLogger(getClass()).info("Finished upload for id " + id);
                                return Completable.complete();
                            }
                        });

                    }
                }).blockingAwait();
    }

    private static void deleteRecursive(Path tempFolderPath) throws IOException {
        try {
            Files.walkFileTree(tempFolderPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (exc != null) {
                        throw exc;
                    }

                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (NoSuchFileException ignored) {
        }
    }

    @Test
    public void upload10MParallelTest() throws Exception {
        final String sas = System.getenv("JAVA_SDK_TEST_SAS");
        HttpHeaders headers = new HttpHeaders()
                .set("x-ms-version", "2017-04-17");

        HttpPipeline pipeline = HttpPipeline.build(
                new AddHeadersPolicy.Factory(headers),
                new RetryPolicy.Factory(2),
                new AddDatePolicy.Factory(),
                new LoggingPolicy.Factory(LogLevel.BASIC));

        final IOService service = RestProxy.create(IOService.class, pipeline);
        final Path tempFolderPath = Paths.get("temp");
        deleteRecursive(tempFolderPath);
        Files.createDirectory(tempFolderPath);

        final byte[] buf = new byte[1024 * 1024 * 10];
        Flowable.range(0, 50)
                .flatMapCompletable(new Function<Integer, Completable>() {
                    @Override
                    public Completable apply(Integer i) throws Exception {
                        final int id = i;
                        final Path filePath = tempFolderPath.resolve("10m-" + id + ".dat");

                        Files.deleteIfExists(filePath);
                        Files.createFile(filePath);
                        FileChannel file = FileChannel.open(filePath, StandardOpenOption.READ, StandardOpenOption.WRITE);

                        Random random = new Random();
                        random.nextBytes(buf);

                        final byte[] md5 = MessageDigest.getInstance("MD5").digest(buf);
                        file.write(ByteBuffer.wrap(buf));
                        file.close();

                        final AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(filePath);
                        AsyncInputStream fileStream = AsyncInputStream.create(fileChannel);
                        return service.upload10MB(String.valueOf(id), sas, "BlockBlob", fileStream).flatMapCompletable(new Function<RestResponse<Void, Void>, CompletableSource>() {
                            @Override
                            public CompletableSource apply(RestResponse<Void, Void> response) throws Exception {
                                fileChannel.close();
                                String base64MD5 = response.rawHeaders().get("Content-MD5");
                                byte[] receivedMD5 = BaseEncoding.base64().decode(base64MD5);
                                assertArrayEquals(md5, receivedMD5);
                                LoggerFactory.getLogger(getClass()).info("Finished upload for id " + id);
                                return Completable.complete();
                            }
                        });

                    }
                }).blockingAwait();
    }

    @Test
    public void upload100MParallelTest() throws Exception {
        final String sas = System.getenv("JAVA_SDK_TEST_SAS");
        HttpHeaders headers = new HttpHeaders()
                .set("x-ms-version", "2017-04-17");

        HttpPipeline pipeline = HttpPipeline.build(
                new AddDatePolicy.Factory(),
                new AddHeadersPolicy.Factory(headers),
                new LoggingPolicy.Factory(LogLevel.BASIC));

        final IOService service = RestProxy.create(IOService.class, pipeline);
        deleteRecursive(tempFolderPath);
        Files.createDirectory(tempFolderPath);

        final byte[] buf = new byte[1024 * 1024 * 100];
        Flowable.range(0, 100)
                .flatMapCompletable(new Function<Integer, Completable>() {
                    @Override
                    public Completable apply(Integer i) throws Exception {
                        final int id = i;
                        final Path filePath = tempFolderPath.resolve("100m-" + id + ".dat");

                        Files.deleteIfExists(filePath);
                        Files.createFile(filePath);
                        FileChannel file = FileChannel.open(filePath, StandardOpenOption.READ, StandardOpenOption.WRITE);

                        Random random = new Random();
                        random.nextBytes(buf);

                        final byte[] md5 = MessageDigest.getInstance("MD5").digest(buf);
                        file.write(ByteBuffer.wrap(buf));
                        file.close();

                        final AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(filePath);
                        AsyncInputStream fileStream = AsyncInputStream.create(fileChannel);
                        return service.upload100MB(String.valueOf(id), sas, "BlockBlob", fileStream).flatMapCompletable(new Function<RestResponse<Void, Void>, CompletableSource>() {
                            @Override
                            public CompletableSource apply(RestResponse<Void, Void> response) throws Exception {
                                fileChannel.close();
                                String base64MD5 = response.rawHeaders().get("Content-MD5");
                                byte[] receivedMD5 = BaseEncoding.base64().decode(base64MD5);
                                assertArrayEquals(md5, receivedMD5);
                                LoggerFactory.getLogger(getClass()).info("Finished upload for id " + id);
                                return Completable.complete();
                            }
                        });

                    }
                }).blockingAwait();
    }

    @Test
    public void upload100MParallelPooledPrewrittenFilesTest() throws Exception {
        final String sas = System.getenv("JAVA_SDK_TEST_SAS");
        HttpHeaders headers = new HttpHeaders()
                .set("x-ms-version", "2017-04-17");

        HttpPipeline pipeline = HttpPipeline.build(
                new AddDatePolicy.Factory(),
                new AddHeadersPolicy.Factory(headers),
                new LoggingPolicy.Factory(LogLevel.BASIC));

        final IOService service = RestProxy.create(IOService.class, pipeline);
        deleteRecursive(tempFolderPath);
        Files.createDirectory(tempFolderPath);

        final int numFiles = 100;
        List<byte[]> md5s = new ArrayList<>(numFiles);
        final byte[] buf = new byte[1024 * 1024 * 100];
        for (int i = 0; i < numFiles; i++) {

            final Path filePath = tempFolderPath.resolve("100m-" + i + ".dat");

            Files.deleteIfExists(filePath);
            Files.createFile(filePath);
            FileChannel file = FileChannel.open(filePath, StandardOpenOption.READ, StandardOpenOption.WRITE);

            Random random = new Random();
            random.nextBytes(buf);

            final byte[] md5 = MessageDigest.getInstance("MD5").digest(buf);
            file.write(ByteBuffer.wrap(buf));
            file.close();

            LoggerFactory.getLogger(getClass()).info("Wrote file id " + i + " to disk");
            md5s.add(md5);
        }

        Instant start = Instant.now();
        Flowable.range(0, numFiles)
                .zipWith(md5s, new BiFunction<Integer, byte[], Completable>() {
                    @Override
                    public Completable apply(Integer integer, final byte[] md5) throws Exception {
                        final int id = integer;
                        final Path filePath = tempFolderPath.resolve("100m-" + id + ".dat");
                        final FileChannel fileChannel = FileChannel.open(filePath);
                        FileSegment fileSegment = new FileSegment(fileChannel, 0, fileChannel.size());
                        return service.upload100MBFile(String.valueOf(id), sas, "BlockBlob", fileSegment).flatMapCompletable(new Function<RestResponse<Void, Void>, CompletableSource>() {
                            @Override
                            public CompletableSource apply(RestResponse<Void, Void> response) throws Exception {
                                fileChannel.close();
                                String base64MD5 = response.rawHeaders().get("Content-MD5");
                                byte[] receivedMD5 = BaseEncoding.base64().decode(base64MD5);
                                assertArrayEquals(md5, receivedMD5);
                                LoggerFactory.getLogger(getClass()).info("Finished upload for id " + id);
                                return Completable.complete();
                            }
                        });
                    }
                }).flatMapCompletable(Functions.<Completable>identity()).blockingAwait();

        String timeTakenString = PeriodFormat.getDefault().print(new Duration(start, Instant.now()).toPeriod());
        LoggerFactory.getLogger(getClass()).info("Upload took " + timeTakenString);
    }

    @Test
    public void download90MTest() throws Exception {
        final String sas = System.getenv("JAVA_SDK_TEST_SAS");
        assert !sas.isEmpty();
        HttpHeaders headers = new HttpHeaders()
                .set("x-ms-version", "2017-04-17");

        HttpPipeline pipeline = HttpPipeline.build(
                new AddDatePolicy.Factory(),
                new AddHeadersPolicy.Factory(headers),
                new LoggingPolicy.Factory(LogLevel.HEADERS));

        final IOService service = RestProxy.create(IOService.class, pipeline);
        RestResponse<Void, Flowable<byte[]>> response = service.download90MB(sas).blockingGet();

        final MessageDigest md5 = MessageDigest.getInstance("MD5");
        response.body()
                .blockingSubscribe(new Consumer<byte[]>() {
                    @Override
                    public void accept(byte[] bytes) throws Exception {
                        md5.update(bytes);
                    }
                });

        assertArrayEquals(MD5_90MB, md5.digest());
    }

    @Test
    public void stressTest() throws Exception {
        final String sas = System.getenv("JAVA_SDK_TEST_SAS");

        HttpHeaders headers = new HttpHeaders()
                .set("x-ms-version", "2017-04-17");

        HttpPipeline pipeline = HttpPipeline.build(
                new AddDatePolicy.Factory(),
                new AddHeadersPolicy.Factory(headers),
                new LoggingPolicy.Factory(LogLevel.BASIC));

        final IOService service = RestProxy.create(IOService.class, pipeline);

        ExecutorService executor = Executors.newCachedThreadPool();
        final List<Exception> threadExceptions = new ArrayList<>();

        Runnable downloadVerify1GB = new Runnable() {
            @Override
            public void run() {
                try {
                    RestResponse<Void, Flowable<byte[]>> response = service.download1GB(sas).blockingGet();
                    final MessageDigest md = MessageDigest.getInstance("MD5");
                    response.body().blockingSubscribe(new Consumer<byte[]>() {
                        @Override
                        public void accept(byte[] bytes) throws Exception {
                            md.update(bytes);
                        }
                    });

                    byte[] actualMD5 = md.digest();
                    assertArrayEquals(MD5_1GB, actualMD5);
                } catch (RuntimeException | NoSuchAlgorithmException e) {
                    synchronized (threadExceptions) {
                        threadExceptions.add(e);
                    }
                }
            }
        };

        executor.submit(downloadVerify1GB);

        for (int i = 0; i < 8; i++) {
            // Download, upload 1 MB
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        RestResponse<Void, Flowable<byte[]>> response = service.download1MB(sas).blockingGet();
                        int contentLength = Integer.parseInt(response.rawHeaders().get("Content-Length"));
                        final byte[] fileContent = new byte[contentLength];
                        final int[] position = { 0 };

                        final MessageDigest md = MessageDigest.getInstance("MD5");
                        response.body().blockingSubscribe(new Consumer<byte[]>() {
                            @Override
                            public void accept(byte[] bytes) throws Exception {
                                md.update(bytes);
                                System.arraycopy(bytes, 0, fileContent, position[0], bytes.length);
                                position[0] += bytes.length;
                            }
                        });

                        byte[] actualMD5 = md.digest();
                        assertArrayEquals(MD5_1MB, actualMD5);

                        RestResponse<Void, Void> uploadResponse = service.upload1MBBytes(sas, "BlockBlob", fileContent).blockingGet();
                        String base64MD5 = uploadResponse.rawHeaders().get("Content-MD5");
                        byte[] uploadedMD5 = BaseEncoding.base64().decode(base64MD5);
                        assertArrayEquals(MD5_1MB, uploadedMD5);
                    } catch (RuntimeException | NoSuchAlgorithmException e) {
                        synchronized (threadExceptions) {
                            threadExceptions.add(e);
                        }
                    }
                }
            });

            // Start downloading 1 GB and cancel
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    RestResponse<Void, Flowable<byte[]>> response = service.download1GB(sas).blockingGet();
                    final AtomicInteger count = new AtomicInteger();

                    response.body().map(new Function<byte[], byte[]>() {
                        @Override
                        public byte[] apply(byte[] bytes) throws Exception {
                            count.incrementAndGet();
                            if (count.intValue() == 3) {
                                throw new IllegalStateException("Oops, cancel the download.");
                            }
                            return bytes;
                        }
                    }).subscribe();
                }
            });

            // Download 1 KB
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        RestResponse<Void, Flowable<byte[]>> response = service.download1KB(sas).blockingGet();
                        final MessageDigest md = MessageDigest.getInstance("MD5");
                        response.body().blockingSubscribe(new Consumer<byte[]>() {
                            @Override
                            public void accept(byte[] bytes) throws Exception {
                                md.update(bytes);
                            }
                        });

                        byte[] actualMD5 = md.digest();
                        assertArrayEquals(MD5_1KB, actualMD5);
                    } catch (RuntimeException | NoSuchAlgorithmException e) {
                        synchronized (threadExceptions) {
                            threadExceptions.add(e);
                        }
                    }
                }
            });
        }

        executor.submit(downloadVerify1GB);

        executor.shutdown();
        executor.awaitTermination(15, TimeUnit.MINUTES);

        for (Exception e : threadExceptions) {
            e.printStackTrace();
        }

        assertEquals(0, threadExceptions.size());
    }
}
