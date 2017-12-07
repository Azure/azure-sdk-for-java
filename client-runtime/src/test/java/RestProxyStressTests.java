import com.microsoft.rest.v2.RestProxy;
import com.microsoft.rest.v2.RestResponse;
import com.microsoft.rest.v2.annotations.GET;
import com.microsoft.rest.v2.annotations.Host;
import com.microsoft.rest.v2.annotations.PathParam;
import com.microsoft.rest.v2.http.HttpHeaders;
import com.microsoft.rest.v2.http.HttpPipeline;
import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import com.microsoft.rest.v2.policy.AddHeadersPolicy;
import com.microsoft.rest.v2.policy.LoggingPolicy;
import com.microsoft.rest.v2.policy.LoggingPolicy.LogLevel;
import com.microsoft.rest.v2.policy.RequestPolicy;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.functions.Consumer;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.*;

import static junit.framework.TestCase.fail;
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
    }

    @Test
    public void stressTest() throws Exception {
        final String sas = System.getenv("JAVA_SDK_TEST_SAS");

        HttpHeaders headers = new HttpHeaders()
                .set("x-ms-version", "2017-04-17");
//                .set("range", "bytes=0-16383");
        HttpPipeline pipeline = HttpPipeline.build(
                new AddDatePolicy.Factory(),
                new AddHeadersPolicy.Factory(headers),
                new LoggingPolicy.Factory(LogLevel.HEADERS));
        final DownloadService service = RestProxy.create(DownloadService.class, pipeline);

        ExecutorService executor = Executors.newCachedThreadPool();
        final List<Exception> threadExceptions = new ArrayList<>();
        for (int i = 0; i < 1; i++) {
            final int id = i;
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    Path path = Paths.get("out" + id + ".dat");
                    try {
                        RestResponse<Void, Flowable<byte[]>> response = service.download1GB(sas).blockingGet();
                        Files.deleteIfExists(path);
                        Files.createFile(path);
                        final FileChannel file = FileChannel.open(path, StandardOpenOption.WRITE);
                        response.body().blockingSubscribe(new Consumer<byte[]>() {
                            @Override
                            public void accept(byte[] bytes) throws Exception {
                                file.write(ByteBuffer.wrap(bytes));
                            }
                        });
                        assertEquals(1024*1024*1024, Files.size(path));
                    } catch (IOException | RuntimeException e) {
                        synchronized (threadExceptions) {
                            threadExceptions.add(e);
                        }
                    } finally {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
        executor.shutdown();
        executor.awaitTermination(15, TimeUnit.MINUTES);

        for (Exception e : threadExceptions) {
            e.printStackTrace();
        }

        if (threadExceptions.size() != 0) {
            fail();
        }
    }
}
