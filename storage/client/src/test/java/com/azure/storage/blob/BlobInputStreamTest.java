package com.azure.storage.blob;

import com.azure.storage.common.credentials.SharedKeyCredential;
import com.google.common.io.ByteStreams;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.Random;

public class BlobInputStreamTest {
    private static final Random RANDOM = new Random();
    private static StorageClient storageClient;

    @BeforeClass
    public static void setup() {
        storageClient = StorageClient.storageClientBuilder()
            .endpoint("https://" + System.getenv("ACCOUNT_NAME") + ".blob.core.windows.net")
            .credential(new SharedKeyCredential(System.getenv("ACCOUNT_NAME"), System.getenv("ACCOUNT_KEY")))
//            .httpClient(HttpClient.createDefault().proxy(() -> new ProxyOptions(Type.HTTP, new InetSocketAddress("localhost", 8888))))
            .buildClient();
    }

//    @Test
    public void testBlobInputStream() throws Exception {
        String containerName = "testcontainer" + RANDOM.nextInt(1000);
        String blobName = "testblob" + RANDOM.nextInt(1000);

        ContainerClient containerClient = storageClient.getContainerClient(containerName);
        if (!containerClient.exists().value()) {
            containerClient.create();
        }

        BlockBlobClient blobClient = containerClient.getBlockBlobClient(blobName);
        byte[] randomBytes = new byte[256 * Constants.MB];
        RANDOM.nextBytes(randomBytes);
        blobClient.upload(new ByteArrayInputStream(randomBytes), randomBytes.length);

        BlobAsyncClient blobAsyncClient = BlobAsyncClient.blobClientBuilder()
            .endpoint("https://" + System.getenv("ACCOUNT_NAME") + ".blob.core.windows.net")
            .containerName(containerName)
            .blobName(blobName)
            .credential(new SharedKeyCredential(System.getenv("ACCOUNT_NAME"), System.getenv("ACCOUNT_KEY")))
//            .httpClient(HttpClient.createDefault().proxy(() -> new ProxyOptions(Type.HTTP, new InetSocketAddress("localhost", 8888))))
            .buildAsyncClient();

        BlobInputStream blobInputStream = new BlobInputStream(blobAsyncClient, null, null);
        Assert.assertEquals(256 * Constants.MB, blobInputStream.skip(256 * Constants.MB));
    }
}
