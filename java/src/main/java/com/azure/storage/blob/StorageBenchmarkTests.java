package com.azure.storage.blob;


import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.storage.blob.models.BlockBlobItem;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.tools.benchmark.BenchmarkTestBase;
import com.azure.tools.benchmark.RandomFlux;
import com.azure.tools.benchmark.RandomStream;
import io.netty.channel.nio.NioEventLoopGroup;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;

@State(Scope.Benchmark)
public class StorageBenchmarkTests extends BenchmarkTestBase {

    @State(Scope.Benchmark)
    public static class BenchmarkState {
        NioEventLoopGroup group = new NioEventLoopGroup(32 * 16);
        HttpClient htclient = new NettyAsyncHttpClientBuilder()
                .nioEventLoopGroup(group)
                .build();
        String connectionString = System.getenv("AZURE_STORAGE_CONNECTION_STRING");

        BlobContainerClientBuilder clientBuilder = new BlobContainerClientBuilder().connectionString(connectionString)
                .containerName("testcontainer").httpClient(htclient);

        BlobContainerAsyncClient asyncClient = clientBuilder.buildAsyncClient();
        BlobContainerClient client = clientBuilder.buildClient();
    }

    @Param({"8", "16", "32"})
    public int parallelThreads;

    @Param({"1024", "1048576"})
    public int size;

    @Benchmark
    @Warmup(iterations = 2)
    @Measurement(iterations = 1)
    public void testDownloadAsync(BenchmarkState state, Blackhole bh) {
        String id = generateBenchmakrId("StorageDownloadAsyncTest", size, parallelThreads);
        BlockBlobAsyncClient blockBlobAsyncClient = state.asyncClient.getBlobAsyncClient("testDownload").getBlockBlobAsyncClient();
        uploadFile(blockBlobAsyncClient, size);
        saveResults(runJobAsync(id, blockBlobAsyncClient, ((client) ->
                downloadOneBlobAsync(client, bh)), parallelThreads, Duration.ofSeconds(10), bh));
    }

    @Benchmark
    @Warmup(iterations = 2)
    @Measurement(iterations = 1)
    public void testUploadAsync(BenchmarkState state, Blackhole bh) {
        String id = generateBenchmakrId("StorageUploadAsyncTest", size, parallelThreads);
        Function<BlobContainerAsyncClient, BlockBlobAsyncClient> setupFunction = (client) -> {
            String blobName = "randomblobtest-" + UUID.randomUUID().toString();
            return client.getBlobAsyncClient(blobName).getBlockBlobAsyncClient();
        };
        saveResults(runJobAsync(id, state.asyncClient, setupFunction, ((containerClient, blockBlobAsyncClient) ->
                uploadBlockBlobAsync(blockBlobAsyncClient, size, bh)), parallelThreads, Duration.ofSeconds(10), bh));
    }

    @Benchmark
    @Warmup(iterations = 2)
    @Measurement(iterations = 1)
    public void testUpload(BenchmarkState state, Blackhole bh) {
        String id = generateBenchmakrId("StorageUploadTest", size, parallelThreads);
        Function<BlobContainerClient, BlockBlobClient> setupFunction = (client) -> {
            String blobName = "randomblobtest-" + UUID.randomUUID().toString();
            return client.getBlobClient(blobName).getBlockBlobClient();
        };
        saveResults(runJob(id, state.client, setupFunction, ((containerClient, blockBlobClient) ->
                uploadBlockBlob(blockBlobClient, size)), parallelThreads, Duration.ofSeconds(10), bh));
    }

    @Benchmark
    @Warmup(iterations = 2)
    @Measurement(iterations = 1)
    public void testDownload(BenchmarkState state, Blackhole bh) {
        String id = generateBenchmakrId("StorageDownloadTest", size, parallelThreads);
        BlockBlobClient blockBlobClient = state.client.getBlobClient("testDownload").getBlockBlobClient();
        uploadFile(blockBlobClient, size);
        saveResults(runJob(id, blockBlobClient, ((client) ->
                downloadOneBlob(client, bh)), parallelThreads, Duration.ofSeconds(10), bh));
    }


    private Mono<Integer> downloadOneBlobAsync(BlockBlobAsyncClient client, Blackhole bh) {
        return client
                .download()
                .map(f -> {
                    int remaining = f.remaining();
                    f.get(new byte[remaining]);
                    return remaining;
                }).reduce(0, (x1, x2) -> x1 + x2)
                .onErrorReturn(0);
    }


    private Mono<BlockBlobItem> uploadBlockBlobAsync(BlockBlobAsyncClient client, int size, Blackhole bh) {
        return client
                .upload(RandomFlux.create(size), size, true);
    }

    private Integer downloadOneBlob(BlockBlobClient client, Blackhole bh) {
        client.download(new OutputStream() {
            @Override
            public void write(int b) throws IOException {

            }
        });
        return 1;
    }

    private BlockBlobItem uploadBlockBlob(BlockBlobClient client, int size) {
        return client
                .upload(RandomStream.create(size), size, true);
    }

    private String generateBenchmakrId(String prefix, int size, int parallelThreads) {
        return String.format(prefix + "-Size:%d-Parallel:%d", size, parallelThreads);
    }

    private void uploadFile(BlockBlobAsyncClient client, int size) {
        byte[] payload = new byte[size];
        (new Random(0)).nextBytes(payload);
        Flux<ByteBuffer> payloadStream = Flux.just(ByteBuffer.wrap(payload));

        client.upload(payloadStream, size, true).block();
    }

    private void uploadFile(BlockBlobClient client, int size) {
        byte[] payload = new byte[size];
        (new Random(0)).nextBytes(payload);
        ByteArrayInputStream payloadStream = new ByteArrayInputStream(payload, 0, size);

        client.upload(payloadStream, size, true);
    }
}