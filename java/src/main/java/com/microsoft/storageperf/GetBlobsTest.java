package com.microsoft.storageperf;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

import com.azure.storage.blob.BlockBlobClient;

public class GetBlobsTest extends ContainerTest<CountOptions> {
    public GetBlobsTest(CountOptions options) {
        super(options);
    }

    public void GlobalSetup() {
        super.GlobalSetup();

        List<String> blobNames = new ArrayList<>(Options.Count);

        for (int i = 0; i < Options.Count; i++) {
            blobNames.add("getblobstest-" + UUID.randomUUID().toString());
        }

        ForkJoinPool forkJoinPool = new ForkJoinPool(Options.Count);
        try {
            forkJoinPool.submit(() -> blobNames.parallelStream().forEach(s -> {
                BlockBlobClient blockBlobClient = ContainerClient.getBlockBlobClient(s);
                try {
                    blockBlobClient.upload(InputStream.nullInputStream(), 0);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            })).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void Run() {
        ContainerClient.listBlobsFlat().forEach(b -> {});
    }
}