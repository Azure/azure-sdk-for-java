package com.azure.storage.blob;

import com.azure.core.http.HttpClient;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.ProxyOptions.Type;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.google.common.io.ByteStreams;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.net.InetSocketAddress;
import java.util.Random;

public class BlobOutputStreamTest {
    private static final Random RANDOM = new Random();
    private static StorageClient storageClient;

    @BeforeClass
    public static void setup() {
        storageClient = StorageClient.storageClientBuilder()
            .endpoint("https://" + System.getenv("ACCOUNT_NAME") + ".blob.core.windows.net")
            .credentials(new SharedKeyCredential(System.getenv("ACCOUNT_NAME"), System.getenv("ACCOUNT_KEY")))
//            .httpClient(HttpClient.createDefault().proxy(() -> new ProxyOptions(Type.HTTP, new InetSocketAddress("localhost", 8888))))
            .buildClient();
    }

//    @Test
    public void testBlobOutputStream() throws Exception {
        String containerName = "testcontainer" + RANDOM.nextInt(1000);
        String blobName = "testblob" + RANDOM.nextInt(1000);

        ContainerClient containerClient = storageClient.getContainerClient(containerName);
        if (!containerClient.exists().value()) {
            containerClient.create();
        }

        byte[] randomBytes = new byte[256 * Constants.MB];
        RANDOM.nextBytes(randomBytes);

        BlockBlobAsyncClient blockBlobAsyncClient = BlobAsyncClient.blobClientBuilder()
            .endpoint("https://" + System.getenv("ACCOUNT_NAME") + ".blob.core.windows.net")
            .containerName(containerName)
            .blobName(blobName)
            .credentials(new SharedKeyCredential(System.getenv("ACCOUNT_NAME"), System.getenv("ACCOUNT_KEY")))
            .httpClient(HttpClient.createDefault().proxy(() -> new ProxyOptions(Type.HTTP, new InetSocketAddress("localhost", 8888))))
            .buildAsyncClient()
            .asBlockBlobAsyncClient();
        BlobOutputStream outStream = new BlobOutputStream(blockBlobAsyncClient, null, null);
        outStream.write(randomBytes);
        outStream.close();

        BlobInputStream blobInputStream = new BlobInputStream(blockBlobAsyncClient, null, null);
        byte[] downloaded = ByteStreams.toByteArray(blobInputStream);
        Assert.assertArrayEquals(randomBytes, downloaded);
    }
}
