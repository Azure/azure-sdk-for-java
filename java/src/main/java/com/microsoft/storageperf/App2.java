package com.microsoft.storageperf;

import com.azure.storage.blob.BlockBlobAsyncClient;
import com.microsoft.azure.storage.blob.*;
import com.microsoft.azure.storage.blob.models.BlockBlobUploadResponse;
import com.microsoft.rest.v2.Context;
import com.microsoft.rest.v2.http.HttpPipeline;
import io.reactivex.Flowable;
import io.reactivex.Single;
import org.apache.commons.cli.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.time.Duration;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class App2 {
    private static final String _containerName = "testcontainer";
    private static final String _blobName = "testblob";

    public static void main(String[] args) throws InterruptedException, IOException {
        Options options = new Options();

        Option debugOption = new Option("g", "debug", false, "Enable debug mode");
        options.addOption(debugOption);

        Option durationOption = new Option("d", "duration", true, "Duration in seconds");
        options.addOption(durationOption);

        Option parallelOption = new Option("p", "parallel", true, "Number of tasks to execute in parallel");
        options.addOption(parallelOption);

        Option sizeOption = new Option("s", "size", true, "Size of message (in bytes)");
        options.addOption(sizeOption);

        Option uploadOption = new Option("u", "upload", false, "Upload before download");
        options.addOption(uploadOption);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("storageperf", options);
            System.exit(1);
        }

        int duration = Integer.parseInt(cmd.getOptionValue("duration", "10"));
        int parallel = Integer.parseInt(cmd.getOptionValue("parallel", "20"));
        int size = Integer.parseInt(cmd.getOptionValue("size", "1024000"));
        boolean upload = true; //cmd.hasOption("upload");
        boolean debug = cmd.hasOption("debug");

        String connectionString = System.getenv("STORAGE_CONNECTION_STRING");
        if (connectionString == null || connectionString.isEmpty()) {
            System.out.println("Environment variable STORAGE_CONNECTION_STRING must be set");
            System.exit(1);
        }

        Run(connectionString, debug, upload, duration, parallel, size);
    }

    static void Run(String connectionString, Boolean debug, Boolean upload, int duration, int parallel,
                          int size) {
        // From the Azure portal, get your Storage account's name and account key.
        String accountName = "";
        String accountKey = "";

        // Use your Storage account's name and key to create a credential object; this is used to access your account.
        SharedKeyCredentials credential = null;
        try {
            credential = new SharedKeyCredentials(accountName, accountKey);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        HttpPipeline pipeline = StorageURL.createPipeline(credential, new PipelineOptions());

        URL u = null;
        try {
            u = new URL(String.format(Locale.ROOT, "https://%s.blob.core.windows.net", accountName));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        ServiceURL serviceURL = new ServiceURL(u, pipeline);

        ContainerURL containerURL = serviceURL.createContainerURL(_containerName);

        BlockBlobURL blobURL = containerURL.createBlockBlobURL(_blobName + 1);

        if (upload) {
            UploadAndVerifyDownload(blobURL, size);
        }

        System.out.printf("Downloading blob '%s/%s' with %d parallel task(s) for %d second(s)...%n", _containerName,
                _blobName, parallel, duration);
        System.out.println();


        Long startTime = System.nanoTime();
        Long count = DownloadLoop(blobURL, duration, parallel, startTime);
        Long endTime = System.nanoTime();

        Long timeTaken = (endTime - startTime) / 1000000000;

        System.out.println("Final Count: " + count);
        System.out.println("Time Taken: " + timeTaken);
        Long downloadsPerSecond = count / timeTaken.intValue();
        Long megabytesPerSecond = (downloadsPerSecond * size) / (1024l * 1024l);
        System.out.printf("Downloads per second: %d %n", downloadsPerSecond);
        System.out.printf("MB/s per second: %d %n", megabytesPerSecond);
    }

    static Long DownloadLoop(BlockBlobURL client, Integer duration, Integer parallel, Long startTime) {
        return
//                Flowable.range(1, parallel)
//                .flatMap(a -> client.download(null, null, false, Context.NONE) // Mono<Flux<ByteBuffer>
//                .repeat() // Flux<Flux<ByteBuffer>>
//                .concatMap(f -> {
//                    return f.body(null).flatMap(b -> {
//                        int remaining = b.remaining();
//                        b.get(new byte[b.remaining()]);
//                        return Flowable.just(remaining);
//                    });
//                })) // Flux<Mono<Integer>>
//                .take(duration, TimeUnit.SECONDS)
//                .reduce((x1, x2) -> x1 + x2)
//                .blockingGet();
//
        Flowable.range(1, parallel)
                .flatMap(a -> client.download(null, null, false, Context.NONE) // Mono<Flux<ByteBuffer>
                                 .repeat() // Flux<Flux<ByteBuffer>>
                .concatMap(f -> {
                    return f.body(null).flatMap(b -> {
                        int remaining = b.remaining();
                        b.get(new byte[b.remaining()]);
                        return Flowable.just(remaining);
                    });
                }))
                //.repeat()// Flux<Mono<Integer>>
                .take(duration, TimeUnit.SECONDS)
                .count()
              //  .reduce((x1, x2) -> x1 + x2)
                .blockingGet();
                        // Flux<Mono<Integer>>
    }

    static Flux<Integer> downloadParallel(BlockBlobAsyncClient client, int parallel) {
        return Flux.just(1).repeat().flatMap(i -> downloadOneBlob(client), parallel);
    }
    static Mono<Integer> downloadOneBlob(BlockBlobAsyncClient client) {
        return client
                .download()
                .flatMap(f -> f
                        .reduce(0, (i, b) -> {
                            int remaining = b.remaining();
                            b.get(new byte[remaining]);
                            return i + remaining;
                        }));
    }


    static BlockBlobUploadResponse UploadAndVerifyDownload(BlockBlobURL blobURL, int size) {
        byte[] payload = new byte[size];
        (new Random(0)).nextBytes(payload);

        return blobURL.upload(Flowable.just(ByteBuffer.wrap(payload)), payload.length).blockingGet();
        // TODO: Verify download
    }
}
