/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2;

import com.microsoft.rest.v2.annotations.BodyParam;
import com.microsoft.rest.v2.annotations.DELETE;
import com.microsoft.rest.v2.annotations.ExpectedResponses;
import com.microsoft.rest.v2.annotations.GET;
import com.microsoft.rest.v2.annotations.HeaderParam;
import com.microsoft.rest.v2.annotations.Host;
import com.microsoft.rest.v2.annotations.PUT;
import com.microsoft.rest.v2.annotations.PathParam;
import com.microsoft.rest.v2.http.ContentType;
import com.microsoft.rest.v2.http.HttpHeaders;
import com.microsoft.rest.v2.http.HttpPipelineBuilder;
import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import com.microsoft.rest.v2.policy.AddHeadersPolicyFactory;
import com.microsoft.rest.v2.policy.HostPolicyFactory;
import com.microsoft.rest.v2.policy.HttpLogDetailLevel;
import com.microsoft.rest.v2.policy.RequestPolicy;
import com.microsoft.rest.v2.policy.RequestPolicyFactory;
import com.microsoft.rest.v2.policy.RequestPolicyOptions;
import com.microsoft.rest.v2.util.FlowableUtil;
import io.netty.util.ResourceLeakDetector;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.internal.functions.Functions;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.lang.Thread.UncaughtExceptionHandler;
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
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class RestProxyStressTests {
    private static IOService service;
    private static Process testServer;
    // By default will spawn a test server running on the default port.
    // If JAVA_SDK_TEST_PORT is specified in the environment, we assume
    // the server is already running on that port.
    private static int port = 8080;

    @BeforeClass
    public static void beforeClass() throws IOException {
//        Assume.assumeTrue(
//                "Set the environment variable JAVA_SDK_STRESS_TESTS to \"true\" to run stress tests",
//                Boolean.parseBoolean(System.getenv("JAVA_SDK_STRESS_TESTS")));

        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);
        LoggerFactory.getLogger(RestProxyStressTests.class).info("ResourceLeakDetector level: " + ResourceLeakDetector.getLevel());


        String tempFolderPath = System.getenv("JAVA_STRESS_TEST_TEMP_PATH");
        if (tempFolderPath == null || tempFolderPath.isEmpty()) {
            tempFolderPath = "temp";
        }

        HttpHeaders headers = new HttpHeaders()
                .set("x-ms-version", "2017-04-17");
        HttpPipelineBuilder builder = new HttpPipelineBuilder()
                .withRequestPolicy(new AddDatePolicyFactory())
                .withRequestPolicy(new AddHeadersPolicyFactory(headers))
                .withRequestPolicy(new ThrottlingRetryPolicyFactory());

        String liveStressTests = System.getenv("JAVA_SDK_TEST_SAS");
        if (liveStressTests == null || liveStressTests.isEmpty()) {
            launchTestServer();
            builder.withRequestPolicy(new HostPolicyFactory("http://localhost:" + port));
        }

        builder.withHttpLoggingPolicy(HttpLogDetailLevel.BASIC);

        service = RestProxy.create(IOService.class, builder.build());

        TEMP_FOLDER_PATH = Paths.get(tempFolderPath);
        create100MFiles(false);
    }

    private static void launchTestServer() throws IOException {
        String portString = System.getenv("JAVA_SDK_TEST_PORT");
        // TODO: figure out why test server hangs only when spawned as a subprocess
        Assume.assumeTrue("JAVA_SDK_TEST_PORT must specify the port of a running local server", portString != null);
        if (portString != null) {
            port = Integer.parseInt(portString, 10);
            LoggerFactory.getLogger(RestProxyStressTests.class).warn("Attempting to connect to already-running test server on port {}", port);
        } else {
            String javaHome = System.getProperty("java.home");
            String javaExecutable = javaHome + File.separator + "bin" + File.separator + "java";
            String classpath = System.getProperty("java.class.path");
            String className = MockServer.class.getCanonicalName();

            ProcessBuilder builder = new ProcessBuilder(
                    javaExecutable, "-cp", classpath, className).redirectErrorStream(true).redirectOutput(Redirect.INHERIT);
            testServer = builder.start();
        }
    }

    @AfterClass
    public static void afterClass() throws Exception {
        if (testServer != null) {
            testServer.destroy();
        }
    }

    private static final class AddDatePolicyFactory implements RequestPolicyFactory {
        @Override
        public RequestPolicy create(RequestPolicy next, RequestPolicyOptions options) {
            return new AddDatePolicy(next);
        }

        private static final class AddDatePolicy implements RequestPolicy {
            private final DateTimeFormatter format = DateTimeFormatter
                    .ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'")
                    .withZone(ZoneId.of("UTC"))
                    .withLocale(Locale.US);

            private final RequestPolicy next;

            AddDatePolicy(RequestPolicy next) {
                this.next = next;
            }

            @Override
            public Single<HttpResponse> sendAsync(HttpRequest request) {
                request.headers().set("Date", format.format(OffsetDateTime.now()));
                return next.sendAsync(request);
            }
        }
    }

    private static final class ThrottlingRetryPolicyFactory implements RequestPolicyFactory {
        @Override
        public RequestPolicy create(RequestPolicy next, RequestPolicyOptions options) {
            return new ThrottlingRetryPolicy(next);
        }

        private static final class ThrottlingRetryPolicy implements RequestPolicy {
            private final RequestPolicy next;

            ThrottlingRetryPolicy(RequestPolicy next) {
                this.next = next;
            }

            @Override
            public Single<HttpResponse> sendAsync(HttpRequest request) {
                return sendAsync(request, 1 + ThreadLocalRandom.current().nextInt(5));
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
                            return sendAsync(request).delaySubscription(1000, TimeUnit.MILLISECONDS);
                        }
                        LoggerFactory.getLogger(getClass()).warn("Unrecoverable exception occurred: " + throwable.getMessage());
                        return Single.error(throwable);
                    }
                });
            }
        }
    }

    @Host("https://javasdktest.blob.core.windows.net")
    interface IOService {
        @ExpectedResponses({201})
        @PUT("/javasdktest/upload/100m-{id}.dat?{sas}")
        Single<VoidResponse> upload100MB(@PathParam("id") String id, @PathParam(value = "sas", encoded = true) String sas, @HeaderParam("x-ms-blob-type") String blobType, @BodyParam(ContentType.APPLICATION_OCTET_STREAM) Flowable<ByteBuffer> stream, @HeaderParam("content-length") long contentLength);

        @GET("/javasdktest/upload/100m-{id}.dat?{sas}")
        Single<StreamResponse> download100M(@PathParam("id") String id, @PathParam(value = "sas", encoded = true) String sas);

        @ExpectedResponses({201})
        @PUT("/testcontainer{id}?restype=container&{sas}")
        Single<VoidResponse> createContainer(@PathParam("id") String id, @PathParam(value = "sas", encoded = true) String sas);

        @ExpectedResponses({202})
        @DELETE("/testcontainer{id}?restype=container&{sas}")
        Single<VoidResponse> deleteContainer(@PathParam("id") String id, @PathParam(value = "sas", encoded = true) String sas);
    }

    private static Path TEMP_FOLDER_PATH;
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

    private static void create100MFiles(boolean recreate) throws IOException {
        final Flowable<ByteBuffer> contentGenerator = Flowable.generate(Random::new, (random, emitter) -> {
            ByteBuffer buf = ByteBuffer.allocate(CHUNK_SIZE);
            random.nextBytes(buf.array());
            emitter.onNext(buf);
        });

        if (recreate) {
            deleteRecursive(TEMP_FOLDER_PATH);
        }

        if (Files.exists(TEMP_FOLDER_PATH)) {
            LoggerFactory.getLogger(RestProxyStressTests.class).info("Temp files directory already exists: " + TEMP_FOLDER_PATH.toAbsolutePath());
        } else {
            LoggerFactory.getLogger(RestProxyStressTests.class).info("Generating temp files in directory: " + TEMP_FOLDER_PATH.toAbsolutePath());
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

                    Flowable<ByteBuffer> fileContent = contentGenerator
                            .take(CHUNKS_PER_FILE)
                            .doOnNext(buf -> messageDigest.update(buf.array()));

                    return FlowableUtil.writeFile(fileContent, file).andThen(Completable.defer(new Callable<CompletableSource>() {
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
    }

    @Test
    @Ignore("Should only be run manually")
    public void prepare100MFiles() throws Exception {
        create100MFiles(true);
    }

    @Test
    public void upload100MParallelTest() throws Exception {
        final String sas = System.getenv("JAVA_SDK_TEST_SAS") == null ? "" : System.getenv("JAVA_SDK_TEST_SAS");
        List<byte[]> md5s = Flowable.range(0, NUM_FILES)
                .map(integer -> {
                    final Path filePath = TEMP_FOLDER_PATH.resolve("100m-" + integer + "-md5.dat");
                    return Files.readAllBytes(filePath);
                }).toList().blockingGet();

        Instant start = Instant.now();
        Flowable.range(0, NUM_FILES)
                .zipWith(md5s, (id, md5) -> {
                    final AsynchronousFileChannel fileStream = AsynchronousFileChannel.open(TEMP_FOLDER_PATH.resolve("100m-" + id + ".dat"));
                    return service.upload100MB(String.valueOf(id), sas, "BlockBlob", FlowableUtil.readFile(fileStream), FILE_SIZE).flatMapCompletable(response -> {
                        String base64MD5 = response.rawHeaders().get("Content-MD5");
                        byte[] receivedMD5 = Base64.getDecoder().decode(base64MD5);
                        assertArrayEquals(md5, receivedMD5);
                        return Completable.complete();
                    });
                }).flatMapCompletable(Functions.identity(), false, 15).blockingAwait();
        long durationMilliseconds = Duration.between(start, Instant.now()).toMillis();
        LoggerFactory.getLogger(getClass()).info("Upload took " + durationMilliseconds + " milliseconds.");
    }

    @Test
    public void uploadMemoryMappedTest() throws Exception {
        final String sas = System.getenv("JAVA_SDK_TEST_SAS") == null ? "" : System.getenv("JAVA_SDK_TEST_SAS");

        List<byte[]> md5s = Flowable.range(0, NUM_FILES)
                .map(integer -> {
                    final Path filePath = TEMP_FOLDER_PATH.resolve("100m-" + integer + "-md5.dat");
                    return Files.readAllBytes(filePath);
                }).toList().blockingGet();

        Instant start = Instant.now();
        Flowable.range(0, NUM_FILES)
                .zipWith(md5s, (id, md5) -> {
                    final FileChannel fileStream = FileChannel.open(TEMP_FOLDER_PATH.resolve("100m-" + id + ".dat"), StandardOpenOption.READ);

                    Flowable<ByteBuffer> stream = FlowableUtil.split(fileStream.map(FileChannel.MapMode.READ_ONLY, 0, fileStream.size()), CHUNK_SIZE);
                    return service.upload100MB(String.valueOf(id), sas, "BlockBlob", stream, FILE_SIZE).flatMapCompletable(response -> {
                        String base64MD5 = response.rawHeaders().get("Content-MD5");
                        byte[] receivedMD5 = Base64.getDecoder().decode(base64MD5);
                        assertArrayEquals(md5, receivedMD5);
                        return Completable.complete();
                    });
                }).flatMapCompletable(Functions.identity(), false, 30).blockingAwait();
        long durationMilliseconds = Duration.between(start, Instant.now()).toMillis();
        LoggerFactory.getLogger(getClass()).info("Upload took " + durationMilliseconds + " milliseconds.");
    }


    /**
     * Run after running one of the corresponding upload tests.
     */
    @Test
    public void download100MParallelTest() {
        final String sas = System.getenv("JAVA_SDK_TEST_SAS") == null ? "" : System.getenv("JAVA_SDK_TEST_SAS");

        List<byte[]> md5s = Flowable.range(0, NUM_FILES)
                .map(integer -> {
                    final Path filePath = TEMP_FOLDER_PATH.resolve("100m-" + integer + "-md5.dat");
                    return Files.readAllBytes(filePath);
                }).toList().blockingGet();

        Instant downloadStart = Instant.now();
        boolean result = Flowable.range(0, NUM_FILES)
                .zipWith(md5s, (id, md5) ->
                        service.download100M(String.valueOf(id), sas).flatMapCompletable(response -> {
                            final MessageDigest messageDigest = MessageDigest.getInstance("MD5");
                            Flowable<ByteBuffer> content = response.body()
                                    .doOnNext(buf -> messageDigest.update(buf.slice()));

                            return content.lastOrError().toCompletable().doOnComplete(() -> {
                                assertArrayEquals(md5, messageDigest.digest());
                                LoggerFactory.getLogger(getClass()).info("Finished downloading and MD5 validated for " + id);
                            });
                        }))
                .flatMapCompletable(Functions.identity())
                .blockingAwait(90, TimeUnit.SECONDS);
        assertTrue(result);
        long durationMilliseconds = Duration.between(downloadStart, Instant.now()).toMillis();
        LoggerFactory.getLogger(getClass()).info("Download took " + durationMilliseconds + " milliseconds.");
    }

    @Test
    public void downloadUploadStreamingTest() {
        final String sas = System.getenv("JAVA_SDK_TEST_SAS") == null ? "" : System.getenv("JAVA_SDK_TEST_SAS");

        List<byte[]> diskMd5s = Flowable.range(0, NUM_FILES)
                .map(integer -> {
                    final Path filePath = TEMP_FOLDER_PATH.resolve("100m-" + integer + "-md5.dat");
                    return Files.readAllBytes(filePath);
                }).toList().blockingGet();

        Instant downloadStart = Instant.now();
        Flowable.range(0, NUM_FILES)
                .zipWith(diskMd5s, (final Integer integer, final byte[] diskMd5) -> {
                    final int id = integer;
                    Flowable<ByteBuffer> downloadContent = service.download100M(String.valueOf(id), sas)
                            // Ideally we would intercept this content to load an MD5 to check consistency between download and upload directly,
                            // but it's sufficient to demonstrate that no corruption occurred between preparation->upload->download->upload.
                            .flatMapPublisher(StreamResponse::body);

                    return service.upload100MB("copy-" + integer, sas, "BlockBlob", downloadContent, FILE_SIZE)
                            .flatMapCompletable(uploadResponse -> {
                                String base64MD5 = uploadResponse.rawHeaders().get("Content-MD5");
                                byte[] uploadMD5 = Base64.getDecoder().decode(base64MD5);
                                assertArrayEquals(diskMd5, uploadMD5);
                                LoggerFactory.getLogger(getClass()).info("Finished upload and validation for id " + id);
                                return Completable.complete();
                    });
                }).flatMapCompletable(Functions.identity(), false, 30).blockingAwait();
        long durationMilliseconds = Duration.between(downloadStart, Instant.now()).toMillis();
        LoggerFactory.getLogger(getClass()).info("Download/Upload took " + durationMilliseconds + " milliseconds.");
    }

    @Test
    public void cancellationTest() throws Exception {
        final String sas = System.getenv("JAVA_SDK_TEST_SAS") == null ? "" : System.getenv("JAVA_SDK_TEST_SAS");
        final Disposable d = Flowable.range(0, NUM_FILES)
                .flatMap(integer ->
                        service.download100M(String.valueOf(integer), sas)
                                .flatMapPublisher(StreamResponse::body))
                .subscribe();

        Completable.complete().delay(10, TimeUnit.SECONDS)
                .andThen(Completable.defer(() -> {
                    d.dispose();
                    return Completable.complete();
                })).blockingAwait();

        // Wait to see if any leak reports come up
        Thread.sleep(10000);
    }

    @Test
    public void testHighParallelism() throws Exception {
        final String sas = System.getenv("JAVA_SDK_TEST_SAS") == null ? "" : System.getenv("JAVA_SDK_TEST_SAS");

        HttpHeaders headers = new HttpHeaders()
                .set("x-ms-version", "2017-04-17");
        HttpPipelineBuilder builder = new HttpPipelineBuilder()
                .withRequestPolicy(new AddDatePolicyFactory())
                .withRequestPolicy(new AddHeadersPolicyFactory(headers))
                .withRequestPolicy(new ThrottlingRetryPolicyFactory());

        if (sas == null || sas.isEmpty()) {
            builder.withRequestPolicy(new HostPolicyFactory("http://localhost:" + port));
        }

        final IOService innerService = RestProxy.create(IOService.class, builder.build());

        // When running with MockServer, connections sometimes get dropped,
        // but this doesn't seem to result in any bad behavior as long as we retry.
        Flowable.range(0, 10000)
                .flatMapCompletable(integer ->
                        innerService.createContainer(integer.toString(), sas).toCompletable()
                                .onErrorResumeNext(throwable -> {
                                    if (throwable instanceof RestException) {
                                        RestException restException = (RestException) throwable;
                                        if ((restException.response().statusCode() == 409 || restException.response().statusCode() == 404)) {
                                            return Completable.complete();
                                        }
                                    }
                                    return Completable.error(throwable);
                                })
                                .andThen(innerService.deleteContainer(integer.toString(), sas).toCompletable()))
                .blockingAwait();
    }
}
