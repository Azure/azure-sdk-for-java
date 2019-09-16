package com.microsoft.storageperf;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.BlockBlobAsyncClient;
import com.azure.storage.blob.BlockBlobClient;

import org.apache.commons.cli.*;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

public class App {
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
        int parallel = Integer.parseInt(cmd.getOptionValue("parallel", "1"));
        int size = Integer.parseInt(cmd.getOptionValue("size", "10240"));
        boolean upload = cmd.hasOption("upload");
        boolean debug = cmd.hasOption("debug");

        String connectionString = System.getenv("STORAGE_CONNECTION_STRING");
        if (connectionString == null || connectionString.isEmpty()) {
            System.out.println("Environment variable STORAGE_CONNECTION_STRING must be set");
            System.exit(1);
        }

        Run(connectionString, debug, upload, duration, parallel, size);
    }

    static Mono<Void> Run(String connectionString, Boolean debug, Boolean upload, int duration, int parallel,
            int size) {
        BlockBlobAsyncClient client = new BlobClientBuilder().connectionString(connectionString)
                .containerName(_containerName).blobName(_blobName).buildBlockBlobAsyncClient();

        if (upload) {
            UploadAndVerifyDownload(client, size).block();
        }

        System.out.printf("Downloading blob '%s/%s' with %d parallel task(s) for %d second(s)...%n", _containerName,
                _blobName, parallel, duration);
        System.out.println();

        AtomicInteger count = new AtomicInteger(0);
        Flux<Flux<ByteBuffer>> downloads = DownloadLoop(client);
        Disposable subscription = downloads.subscribe(f -> {
            System.out.println(".");
            count.incrementAndGet();
        });

        System.out.println("before sleep");

        try {
            Thread.sleep(duration * 1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println("after sleep");

        subscription.dispose();

        System.out.println(count.get());

        try {
            Thread.sleep(duration * 1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println(count.get());

        return Mono.empty();
    }

    static Flux<Flux<ByteBuffer>> DownloadLoop(BlockBlobAsyncClient client) {
        return Flux.generate(
            () -> client.download(),
            (state, sink) -> {
                sink.next(state.flatMapMany(response -> response));
                return client.download();
            });
    }

    static Mono<Void> UploadAndVerifyDownload(BlockBlobAsyncClient client, int size) {
        byte[] payload = new byte[size];
        (new Random(0)).nextBytes(payload);
        Flux<ByteBuffer> payloadStream = Flux.just(ByteBuffer.wrap(payload));

        client.upload(payloadStream, size).block();

        return Mono.empty();
        // TODO: Verify download
    }
}
