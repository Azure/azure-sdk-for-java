/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2;

import com.google.common.io.BaseEncoding;
import com.microsoft.rest.v2.annotations.*;
import com.microsoft.rest.v2.http.*;
import com.microsoft.rest.v2.http.HttpClient.Configuration;
import com.microsoft.rest.v2.policy.AddHeadersPolicy;
import com.microsoft.rest.v2.policy.LoggingPolicy;
import com.microsoft.rest.v2.policy.LoggingPolicy.LogLevel;
import com.microsoft.rest.v2.policy.RequestPolicy;
import com.microsoft.rest.v2.policy.RequestPolicyFactory;
import com.microsoft.rest.v2.policy.RequestPolicyOptions;
import com.microsoft.rest.v2.util.FlowableUtil;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Emitter;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.internal.functions.Functions;
import io.reactivex.schedulers.Schedulers;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormat;
import org.junit.Ignore;
import org.junit.Test;
import org.reactivestreams.Publisher;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
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
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.*;

import static org.junit.Assert.assertArrayEquals;

@SuppressWarnings("Duplicates")
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

    static class ThrottlingRetryPolicyFactory implements RequestPolicyFactory {
        @Override
        public RequestPolicy create(RequestPolicy next, RequestPolicyOptions options) {
            return new ThrottlingRetryPolicy(next);
        }

        static class ThrottlingRetryPolicy implements RequestPolicy {
            private final RequestPolicy next;

            ThrottlingRetryPolicy(RequestPolicy next) {
                this.next = next;
            }

            @Override
            public Single<HttpResponse> sendAsync(HttpRequest request) {
                return sendAsync(request, 5 + ThreadLocalRandom.current().nextInt(10));
            }

            Single<HttpResponse> sendAsync(final HttpRequest request, final int waitTimeSeconds) {
                return next.sendAsync(request).flatMap(new Function<HttpResponse, Single<? extends HttpResponse>>() {
                    @Override
                    public Single<? extends HttpResponse> apply(HttpResponse httpResponse) throws Exception {
                        if (httpResponse.statusCode() != 503 && httpResponse.statusCode() != 500) {
                            return Single.just(httpResponse);
                        } else {
                            LoggerFactory.getLogger(getClass()).warn("Received " + httpResponse.statusCode() + " for request. Waiting " + waitTimeSeconds + " seconds before retry.");
                            final int nextWaitTime = 5 + ThreadLocalRandom.current().nextInt(10);
                            return Completable.complete().delay(waitTimeSeconds, TimeUnit.SECONDS)
                                    .andThen(sendAsync(request, nextWaitTime));
                        }
                    }
                }).onErrorResumeNext(new Function<Throwable, SingleSource<? extends HttpResponse>>() {
                    @Override
                    public SingleSource<? extends HttpResponse> apply(Throwable throwable) throws Exception {
                        if (throwable instanceof IOException) {
                            LoggerFactory.getLogger(getClass()).warn("I/O exception occurred: " + throwable.getMessage());
                            return sendAsync(request);
                        }
                        LoggerFactory.getLogger(getClass()).warn("Unrecoverable exception occurred: " + throwable.getMessage());
                        return Single.error(throwable);
                    }
                });
            }
        }
    }

    @Host("http://javasdktest.blob.core.windows.net")
    interface IOService {
        @ExpectedResponses({ 201 })
        @PUT("/javasdktest/upload/100m-{id}.dat?{sas}")
        Single<RestResponse<Void, Void>> upload100MB(@PathParam("id") String id, @PathParam(value = "sas", encoded = true) String sas, @HeaderParam("x-ms-blob-type") String blobType, @BodyParam(ContentType.APPLICATION_OCTET_STREAM) AsyncInputStream stream);

        @ExpectedResponses({ 201 })
        @PUT("/javasdktest/upload/100m-{id}.dat?{sas}")
        Single<RestResponse<Void, Void>> upload100MBFile(@PathParam("id") String id, @PathParam(value = "sas", encoded = true) String sas, @HeaderParam("x-ms-blob-type") String blobType, @BodyParam(ContentType.APPLICATION_OCTET_STREAM) FileSegment segment);

        @GET("/javasdktest/upload/100m-{id}.dat?{sas}")
        Single<RestResponse<Void, AsyncInputStream>> download100M(@PathParam("id") String id, @PathParam(value = "sas", encoded = true) String sas);
    }

    private static final Path TEMP_FOLDER_PATH = Paths.get("temp");
    private static final int NUM_FILES = 100;
    private static final int FILE_SIZE = 1024 * 1024 * 100;
    private static final int CHUNK_SIZE = 8192;
    private static final int CHUNKS_PER_FILE = FILE_SIZE / CHUNK_SIZE;

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

    /**
     * Run before other tests that exercise upload and download scenarios.
     */
    @Test
    public void prepare100MFiles() throws Exception {
        final Flowable<byte[]> contentGenerator = Flowable.generate(new Callable<Random>() {
            @Override
            public Random call() throws Exception {
                return new Random();
            }
        }, new BiConsumer<Random, Emitter<byte[]>>() {
            @Override
            public void accept(Random random, Emitter<byte[]> emitter) throws Exception {
                byte[] buf = new byte[CHUNK_SIZE];
                random.nextBytes(buf);
                emitter.onNext(buf);
            }
        });

        deleteRecursive(TEMP_FOLDER_PATH);
        Files.createDirectory(TEMP_FOLDER_PATH);

        Flowable.range(0, NUM_FILES).flatMapCompletable(new Function<Integer, Completable>() {
            @Override
            public Completable apply(Integer integer) throws Exception {
                final int i = integer;
                final Path filePath = TEMP_FOLDER_PATH.resolve("100m-" + i + ".dat");

                Files.deleteIfExists(filePath);
                Files.createFile(filePath);
                final AsynchronousFileChannel file = AsynchronousFileChannel.open(filePath, StandardOpenOption.READ, StandardOpenOption.WRITE);
                final MessageDigest messageDigest = MessageDigest.getInstance("MD5");

                Flowable<byte[]> fileContent = contentGenerator.take(CHUNKS_PER_FILE).doOnNext(new Consumer<byte[]>() {
                    @Override
                    public void accept(byte[] bytes) throws Exception {
                        messageDigest.update(bytes);
                    }
                });

                return FlowableUtil.writeContentToFile(fileContent, file).andThen(Completable.defer(new Callable<CompletableSource>() {
                    @Override
                    public CompletableSource call() throws Exception {
                        file.close();
                        Files.write(TEMP_FOLDER_PATH.resolve("100m-" + i + "-md5.dat"), messageDigest.digest());
                        LoggerFactory.getLogger(getClass()).info("Finished writing file " + i);
                        return Completable.complete();
                    }
                }));
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
                new ThrottlingRetryPolicyFactory(),
                new LoggingPolicy.Factory(LogLevel.BASIC));

        final IOService service = RestProxy.create(IOService.class, pipeline);

        List<byte[]> md5s = Flowable.range(0, NUM_FILES)
                .map(new Function<Integer, byte[]>() {
                    @Override
                    public byte[] apply(Integer integer) throws Exception {
                        final Path filePath = TEMP_FOLDER_PATH.resolve("100m-" + integer + "-md5.dat");
                        return Files.readAllBytes(filePath);
                    }
                }).toList().blockingGet();

        Instant start = Instant.now();
        Flowable.range(0, NUM_FILES)
                .zipWith(md5s, new BiFunction<Integer, byte[], Completable>() {
                    @Override
                    public Completable apply(Integer id, final byte[] md5) throws Exception {
                        final AsynchronousFileChannel fileStream = AsynchronousFileChannel.open(TEMP_FOLDER_PATH.resolve("100m-" + id + ".dat"));
                        AsyncInputStream stream = AsyncInputStream.create(fileStream);
                        return service.upload100MB(String.valueOf(id), sas, "BlockBlob", stream).flatMapCompletable(new Function<RestResponse<Void, Void>, CompletableSource>() {
                            @Override
                            public CompletableSource apply(RestResponse<Void, Void> response) throws Exception {
                                String base64MD5 = response.rawHeaders().get("Content-MD5");
                                byte[] receivedMD5 = BaseEncoding.base64().decode(base64MD5);
                                assertArrayEquals(md5, receivedMD5);
                                return Completable.complete();
                            }
                        });
                    }
                }).flatMapCompletable(Functions.<Completable>identity(), false, 30).blockingAwait();
        String timeTakenString = PeriodFormat.getDefault().print(new Duration(start, Instant.now()).toPeriod());
        LoggerFactory.getLogger(getClass()).info("Upload took " + timeTakenString);
    }

    @Test
    public void upload100MParallelPooledTest() throws Exception {
        final String sas = System.getenv("JAVA_SDK_TEST_SAS");
        HttpHeaders headers = new HttpHeaders()
                .set("x-ms-version", "2017-04-17");

        HttpPipeline pipeline = HttpPipeline.build(
                new AddDatePolicy.Factory(),
                new AddHeadersPolicy.Factory(headers),
                new ThrottlingRetryPolicyFactory(),
                new LoggingPolicy.Factory(LogLevel.BASIC));

        final IOService service = RestProxy.create(IOService.class, pipeline);

        List<byte[]> md5s = Flowable.range(0, NUM_FILES)
                .map(new Function<Integer, byte[]>() {
                    @Override
                    public byte[] apply(Integer integer) throws Exception {
                        final Path filePath = TEMP_FOLDER_PATH.resolve("100m-" + integer + "-md5.dat");
                        return Files.readAllBytes(filePath);
                    }
                }).toList().blockingGet();

        Instant start = Instant.now();
        Flowable.range(0, NUM_FILES)
                .zipWith(md5s, new BiFunction<Integer, byte[], Completable>() {
                    @Override
                    public Completable apply(Integer integer, final byte[] md5) throws Exception {
                        final int id = integer;
                        final Path filePath = TEMP_FOLDER_PATH.resolve("100m-" + id + ".dat");
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
                }).flatMapCompletable(Functions.<Completable>identity(), false, 30).blockingAwait();

        String timeTakenString = PeriodFormat.getDefault().print(new Duration(start, Instant.now()).toPeriod());
        LoggerFactory.getLogger(getClass()).info("Upload took " + timeTakenString);
    }

    /**
     * Run after running one of the corresponding upload tests.
     */
    @Test
    public void download100MParallelTest() throws Exception {
        final String sas = System.getenv("JAVA_SDK_TEST_SAS");
        HttpHeaders headers = new HttpHeaders()
                .set("x-ms-version", "2017-04-17");

        HttpPipeline pipeline = HttpPipeline.build(
                new AddDatePolicy.Factory(),
                new AddHeadersPolicy.Factory(headers),
                new ThrottlingRetryPolicyFactory(),
                new LoggingPolicy.Factory(LogLevel.BASIC));

        final IOService service = RestProxy.create(IOService.class, pipeline);

        List<byte[]> md5s = Flowable.range(0, NUM_FILES)
                .map(new Function<Integer, byte[]>() {
                    @Override
                    public byte[] apply(Integer integer) throws Exception {
                        final Path filePath = TEMP_FOLDER_PATH.resolve("100m-" + integer + "-md5.dat");
                        return Files.readAllBytes(filePath);
                    }
                }).toList().blockingGet();

        Instant downloadStart = Instant.now();
        Flowable.range(0, NUM_FILES)
                .zipWith(md5s, new BiFunction<Integer, byte[], Completable>() {
                    @Override
                    public Completable apply(final Integer integer, final byte[] md5) throws Exception {
                        final int id = integer;
                        return service.download100M(String.valueOf(id), sas).flatMapCompletable(new Function<RestResponse<Void, AsyncInputStream>, CompletableSource>() {
                            @Override
                            public CompletableSource apply(RestResponse<Void, AsyncInputStream> response) throws Exception {
                                final MessageDigest messageDigest = MessageDigest.getInstance("MD5");
                                Flowable<byte[]> content = response.body().content().doOnNext(new Consumer<byte[]>() {
                                    @Override
                                    public void accept(byte[] bytes) throws Exception {
                                        messageDigest.update(bytes);
                                    }
                                });

                                AsynchronousFileChannel file = AsynchronousFileChannel.open(TEMP_FOLDER_PATH.resolve("100m-" + integer + ".dat"), StandardOpenOption.WRITE);
                                return FlowableUtil.writeContentToFile(content, file).doOnComplete(new Action() {
                                    @Override
                                    public void run() throws Exception {
                                        assertArrayEquals(md5, messageDigest.digest());
                                        LoggerFactory.getLogger(getClass()).info("Finished downloading and MD5 validated for " + id);
                                    }
                                });
                            }
                        });
                    }
                }).flatMapCompletable(Functions.<Completable>identity(), false, 30).blockingAwait();
        String downloadTimeTakenString = PeriodFormat.getDefault().print(new Duration(downloadStart, Instant.now()).toPeriod());
        LoggerFactory.getLogger(getClass()).info("Download took " + downloadTimeTakenString);
    }

    @Test
    public void downloadUploadStreamingTest() {
        final String sas = System.getenv("JAVA_SDK_TEST_SAS");
        HttpHeaders headers = new HttpHeaders()
                .set("x-ms-version", "2017-04-17");

        HttpPipeline pipeline = HttpPipeline.build(
                new AddDatePolicy.Factory(),
                new AddHeadersPolicy.Factory(headers),
                new ThrottlingRetryPolicyFactory(),
                new LoggingPolicy.Factory(LogLevel.BASIC));

        final IOService service = RestProxy.create(IOService.class, pipeline);

        List<byte[]> diskMd5s = Flowable.range(0, NUM_FILES)
                .map(new Function<Integer, byte[]>() {
                    @Override
                    public byte[] apply(Integer integer) throws Exception {
                        final Path filePath = TEMP_FOLDER_PATH.resolve("100m-" + integer + "-md5.dat");
                        return Files.readAllBytes(filePath);
                    }
                }).toList().blockingGet();

        Instant downloadStart = Instant.now();
        Flowable.range(0, NUM_FILES)
                .zipWith(diskMd5s, new BiFunction<Integer, byte[], Completable>() {
                    @Override
                    public Completable apply(final Integer integer, final byte[] diskMd5) throws Exception {
                        final int id = integer;
                        Flowable<byte[]> downloadContent = service.download100M(String.valueOf(id), sas).flatMapPublisher(new Function<RestResponse<Void, AsyncInputStream>, Publisher<? extends byte[]>>() {
                            @Override
                            public Publisher<? extends byte[]> apply(RestResponse<Void, AsyncInputStream> response) throws Exception {
                                // Ideally we would intercept this content to load an MD5 to check consistency between download and upload directly,
                                // but it's sufficient to demonstrate that no corruption occurred between preparation->upload->download->upload.
                                return response.body().content();
                            }
                        });

                        // A download stream which is allowed to issue an HTTP request when subscribed
                        // can't know for certain what the content length is each time a request is made.
                        AsyncInputStream toSend = new AsyncInputStream(downloadContent, FILE_SIZE, true);
                        return service.upload100MB("copy-" + integer, sas, "BlockBlob", toSend).flatMapCompletable(new Function<RestResponse<Void, Void>, CompletableSource>() {
                            @Override
                            public CompletableSource apply(RestResponse<Void, Void> uploadResponse) throws Exception {
                                String base64MD5 = uploadResponse.rawHeaders().get("Content-MD5");
                                byte[] uploadMD5 = BaseEncoding.base64().decode(base64MD5);
                                assertArrayEquals(diskMd5, uploadMD5);
                                LoggerFactory.getLogger(getClass()).info("Finished upload and validation for id " + id);
                                return Completable.complete();
                            }
                        });
                    }
                }).flatMapCompletable(Functions.<Completable>identity(), false, 30).blockingAwait();
        String downloadTimeTakenString = PeriodFormat.getDefault().print(new Duration(downloadStart, Instant.now()).toPeriod());
        LoggerFactory.getLogger(getClass()).info("Download/upload took " + downloadTimeTakenString);
    }

    @Test
    public void cancellationTest() {
        final String sas = System.getenv("JAVA_SDK_TEST_SAS");
        HttpHeaders headers = new HttpHeaders()
                .set("x-ms-version", "2017-04-17");

        HttpPipeline pipeline = HttpPipeline.build(
                new AddDatePolicy.Factory(),
                new AddHeadersPolicy.Factory(headers),
                new ThrottlingRetryPolicyFactory(),
                new LoggingPolicy.Factory(LogLevel.BASIC));

        final IOService service = RestProxy.create(IOService.class, pipeline);

        Flowable.range(0, NUM_FILES)
                .flatMap(new Function<Integer, Publisher<?>>() {
                    @Override
                    public Publisher<?> apply(Integer integer) throws Exception {
                        return service.download100M(String.valueOf(integer), sas).flatMapPublisher(new Function<RestResponse<Void, AsyncInputStream>, Publisher<?>>() {
                            @Override
                            public Publisher<?> apply(RestResponse<Void, AsyncInputStream> response) throws Exception {
                                return response.body().content().timeout(100, TimeUnit.MILLISECONDS);
                            }
                        });
                    }
                }).blockingLast();
    }
}
