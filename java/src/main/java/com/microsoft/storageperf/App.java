package com.microsoft.storageperf;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.spi.SelectorProvider;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.netty.NettyAsyncHttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.BlockBlobAsyncClient;

import io.netty.channel.DefaultSelectStrategyFactory;
import io.netty.channel.SelectStrategyFactory;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.apache.commons.cli.*;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.tcp.ProxyProvider;

public class App {
    private static final String _containerName = "testcontainer";
    private static final String _blobName = "testblob";

    public static void main(String[] args) throws InterruptedException, IOException {
      //  Thread.sleep(10000);
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
        int parallel = Integer.parseInt(cmd.getOptionValue("parallel", "32"));
        int size = Integer.parseInt(cmd.getOptionValue("size", "10240"));
        boolean upload = true; //cmd.hasOption("upload");
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

        int numThreads =  Runtime.getRuntime().availableProcessors() * 2;
        NioEventLoopGroup group = new NioEventLoopGroup(parallel*16);
        HttpClient htclient = new NettyAsyncHttpClientBuilder()
                .nioEventLoopGroup(group)
                .build();

        BlockBlobAsyncClient client = new BlobClientBuilder().connectionString(connectionString)
                .containerName(_containerName).blobName(_blobName).httpClient(htclient).buildBlockBlobAsyncClient();

        BenchmarkingTool<BlockBlobAsyncClient, Integer> benchmarkingTool = new BenchmarkingTool<>(client);

        if (upload) {
            UploadAndVerifyDownload(client, size).block();
        }

        System.out.printf("Downloading blob '%s/%s' with %d parallel task(s) for %d second(s)...%n", _containerName,
                _blobName, parallel, duration);
        System.out.println();


        BenchmarkResults results = benchmarkingTool.runJobAsync((client1 -> downloadOneBlob(client1)), parallel, Duration.ofSeconds(duration));

        System.out.println("Final Count: " + results.getRequests());
        System.out.println("Time Taken: " + results.getTimeTaken().getSeconds());
        Float megabytesPerSecond = (results.getRequestsPerSecond() * size) / (1024f * 1024f);
        System.out.printf("Downloads per second: %f %n", results.getRequestsPerSecond());
        System.out.printf("MB/s per second: %f %n", megabytesPerSecond);

        return Mono.empty();
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

    static Mono<Void> UploadAndVerifyDownload(BlockBlobAsyncClient client, int size) {
        byte[] payload = new byte[size];
        (new Random(0)).nextBytes(payload);
        Flux<ByteBuffer> payloadStream = Flux.just(ByteBuffer.wrap(payload));

        client.upload(payloadStream, size).block();

        return Mono.empty();
        // TODO: Verify download
    }
}
