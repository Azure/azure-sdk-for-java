import com.google.common.io.BaseEncoding;
import com.microsoft.rest.v2.RestProxy;
import com.microsoft.rest.v2.RestResponse;
import com.microsoft.rest.v2.annotations.*;
import com.microsoft.rest.v2.http.*;
import com.microsoft.rest.v2.policy.AddHeadersPolicy;
import com.microsoft.rest.v2.policy.LoggingPolicy;
import com.microsoft.rest.v2.policy.LoggingPolicy.LogLevel;
import com.microsoft.rest.v2.policy.RequestPolicy;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Ignore;
import org.junit.Test;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
        public AddDatePolicy(RequestPolicy next) {
            this.next = next;
        }

        @Override
        public Single<HttpResponse> sendAsync(HttpRequest request) {
            request.headers().set("Date", format.print(DateTime.now()));
            return next.sendAsync(request);
        }

        static class Factory implements RequestPolicy.Factory {
            @Override
            public RequestPolicy create(RequestPolicy next, Options options) {
                return new AddDatePolicy(next);
            }
        }
    }

    @Host("https://javasdktest.blob.core.windows.net")
    interface DownloadService {
        @GET("/javasdktest/download/1k.dat?{sas}")
        Single<RestResponse<Void, Flowable<byte[]>>> download1KB(@PathParam(value = "sas", encoded = true) String sas);

        @GET("/javasdktest/download/1m.dat?{sas}")
        Single<RestResponse<Void, Flowable<byte[]>>> download1MB(@PathParam(value = "sas", encoded = true) String sas);

        @GET("/javasdktest/download/1g.dat?{sas}")
        Single<RestResponse<Void, Flowable<byte[]>>> download1GB(@PathParam(value = "sas", encoded = true) String sas);

        @ExpectedResponses({ 201 })
        @PUT("/javasdktest/upload/1m.dat?{sas}")
        Single<RestResponse<Void, Void>> upload1MB(@PathParam(value = "sas", encoded = true) String sas, @HeaderParam("x-ms-blob-type") String blobType, @BodyParam byte[] bytes);
    }

    private static final byte[] MD5_1KB = { 70, -110, 110, -84, -35, 116, 118, 2, -22, 8, 117, -65, -106, 61, -36, 58 };
    private static final byte[] MD5_1MB = { -128, 96, 94, 57, -95, -42, 40, 124, -5, 10, 124, -5, 59, -81, 122, 38 };
    private static final byte[] MD5_1GB = { 43, -104, -23, 103, 42, 34, -49, 42, 57, -127, -128, 89, -36, -81, 67, 5 };

    @Test
    public void stressTest() throws Exception {
        final String sas = System.getenv("JAVA_SDK_TEST_SAS");
        HttpHeaders headers = new HttpHeaders()
                .set("x-ms-version", "2017-04-17");

        HttpPipeline pipeline = HttpPipeline.build(
                new AddDatePolicy.Factory(),
                new AddHeadersPolicy.Factory(headers),
                new LoggingPolicy.Factory(LogLevel.BASIC));
        final DownloadService service = RestProxy.create(DownloadService.class, pipeline);

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

                        RestResponse<Void, Void> uploadResponse = service.upload1MB(sas, "BlockBlob", fileContent).blockingGet();
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
