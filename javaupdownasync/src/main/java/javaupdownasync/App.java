
package javaupdownasync;

import java.nio.ByteBuffer;
import java.util.Random;
import java.util.UUID;

import com.azure.storage.blob.*;
import com.azure.storage.blob.models.ParallelTransferOptions;
import reactor.core.publisher.Flux;

public class App {
    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
        String connectionString = System.getenv("STORAGE_CONNECTION_STRING");

        if (connectionString == null || connectionString.isEmpty()) {
            System.out.println("Environment variable STORAGE_CONNECTION_STRING must be set");
            System.exit(1);
        }

        BlobServiceClientBuilder builder = new BlobServiceClientBuilder().connectionString(connectionString);
        BlobServiceAsyncClient serviceAsyncClient = builder.buildAsyncClient();

        String containerName = "perf-" + UUID.randomUUID().toString();
        BlobContainerAsyncClient containerAsyncClient = serviceAsyncClient.getBlobContainerAsyncClient(containerName);
        containerAsyncClient.create().block();

        BlobAsyncClient blobAsyncClient = containerAsyncClient.getBlobAsyncClient("downloadtest");

        byte[] byteArray = new byte[256 * 1024 * 1024];
        (new Random(0)).nextBytes(byteArray);
        ByteBuffer byteBuffer = ByteBuffer.wrap(byteArray).asReadOnlyBuffer();

        long start = System.currentTimeMillis();
        blobAsyncClient.upload(Flux.just(byteBuffer), new ParallelTransferOptions()).block();
        long end = System.currentTimeMillis();
        System.out.println("Upload: " + (end - start) + " ms");

        start = System.currentTimeMillis();
        blobAsyncClient.download()
                .map(b -> {
                    b.get(new byte[b.remaining()]);
                    return 1;
                })
                .blockLast();
        end = System.currentTimeMillis();
        System.out.println("Download: " + (end - start) + " ms");

        containerAsyncClient.delete().block();
    }
}
