package com.azure.storage.blob.perfstress.core;

import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;

import java.net.InetSocketAddress;

import com.azure.core.http.HttpClient;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.perfstress.PerfStressHttpClient;
import com.azure.perfstress.PerfStressOptions;
import com.azure.perfstress.PerfStressTest;

public abstract class ServiceTest<TOptions extends PerfStressOptions> extends PerfStressTest<TOptions> {

    protected final BlobServiceClient BlobServiceClient;
    protected final BlobServiceAsyncClient BlobServiceAsyncClient;

    public ServiceTest(TOptions options) {
        super(options);

        String connectionString = System.getenv("STORAGE_CONNECTION_STRING");

        if (connectionString == null || connectionString.isEmpty()) {
            System.out.println("Environment variable STORAGE_CONNECTION_STRING must be set");
            System.exit(1);
        }

        BlobServiceClientBuilder builder = new BlobServiceClientBuilder()
            .connectionString(connectionString)
            .httpClient(PerfStressHttpClient.create(options));

        BlobServiceClient = builder.buildClient();
        BlobServiceAsyncClient = builder.buildAsyncClient();
    }
}
