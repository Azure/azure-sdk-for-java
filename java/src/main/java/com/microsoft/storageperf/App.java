package com.microsoft.storageperf;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.BlockBlobAsyncClient;

import org.apache.commons.cli.*;

import io.netty.channel.nio.NioEventLoopGroup;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class App {
    private static final String _containerName = "testcontainer";
    private static final String _blobName = "testblob";

    public static void main(String[] args) throws InterruptedException, IOException {
        Options options = new Options();

        Option durationOption = new Option("d", "duration", true, "Duration in seconds");
        options.addOption(durationOption);

        Option nettyThreadsOption = new Option("n", "nettyThreads", true, "Netty threads");
        options.addOption(nettyThreadsOption);

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
        int nettyThreads = Integer.parseInt(cmd.getOptionValue("nettyThreads", "-1"));
        int size = Integer.parseInt(cmd.getOptionValue("size", "10240"));
        boolean upload = cmd.hasOption("upload");
        boolean debug = cmd.hasOption("debug");

        String connectionString = System.getenv("STORAGE_CONNECTION_STRING");
        if (connectionString == null || connectionString.isEmpty()) {
            System.out.println("Environment variable STORAGE_CONNECTION_STRING must be set");
            System.exit(1);
        }

        Run(connectionString, debug, upload, duration, parallel, nettyThreads, size);

        System.exit(0);
    }

    static void Run(String connectionString, Boolean debug, Boolean upload, int duration, int parallel, int nettyThreads,
            int size) {

        BlobClientBuilder clientBuilder = new BlobClientBuilder()
            .connectionString(connectionString)
            .containerName(_containerName).
            blobName(_blobName);

        if (nettyThreads != -1) {
            HttpClient httpClient = new NettyAsyncHttpClientBuilder()
                .setNioEventLoopGroup(new NioEventLoopGroup(nettyThreads))
                .build();
            
            clientBuilder.httpClient(httpClient);
        }

        BlockBlobAsyncClient client = clientBuilder.buildBlockBlobAsyncClient();

        if (upload) {
            UploadAndVerifyDownload(client, size).block();
        }

        System.out.printf("Downloading blob '%s/%s' with %d parallel task(s) for %d second(s)...%n", _containerName,
                _blobName, parallel, duration);
        System.out.println();

        AtomicInteger receivedSize = new AtomicInteger(-1);
        AtomicInteger counter = new AtomicInteger();
        
        Disposable printStatus = Flux.interval(Duration.ofSeconds(1)).subscribe(i -> System.out.println(counter.get()));

        long start = System.nanoTime();
        Flux<Integer> downloads = downloadParallel(client, parallel)
            .take(Duration.ofSeconds(duration))
            .doOnNext(i -> {
                if (receivedSize.get() == -1) {
                    receivedSize.set(i);
                }
                counter.incrementAndGet();
            });
        downloads.blockLast();
        long end = System.nanoTime();
        
        printStatus.dispose();

        double elapsedSeconds = 1.0 * (end - start) / 1000000000;
        double downloadsPerSecond = counter.get() / elapsedSeconds;
        double megabytesPerSecond = (downloadsPerSecond * receivedSize.get()) / (1024 * 1024);

        System.out.println();
        System.out.printf("Downloaded %d blobs of size %d in %.2fs (%.2f blobs/s, %.2f MB/s)", counter.get(), receivedSize.get(),
            elapsedSeconds, downloadsPerSecond, megabytesPerSecond);
    }

    static Flux<Integer> downloadParallel(BlockBlobAsyncClient client, int parallel) {
        return Flux.just(1).repeat().flatMap(i -> downloadOneBlob(client), parallel);
    }
    
    static Mono<Integer> downloadOneBlob(BlockBlobAsyncClient client) {
        return client
            .download() // Mono<Flux<ByteBuffer>>
            .flatMap(f -> f
                .reduce(0, (i, b) -> {
                    // System.out.println(Thread.currentThread().getName());
                    int remaining = b.remaining();
                    b.get(new byte[remaining]);
                    return i + remaining;
                }));
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
