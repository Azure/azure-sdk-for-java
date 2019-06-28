package com.azure.storage.blob;

import com.azure.storage.common.credentials.SharedKeyCredential;
import org.junit.Assert;
import org.junit.BeforeClass;

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

        BlockBlobClient blockBlobClient = containerClient.getBlockBlobClient(blobName);
        byte[] randomBytes = new byte[256 * Constants.MB];
        RANDOM.nextBytes(randomBytes);
        blockBlobClient.upload(new ByteArrayInputStream(randomBytes), randomBytes.length);

        BlobInputStream blobInputStream = blockBlobClient.openInputStream();
        Assert.assertEquals(256 * Constants.MB, blobInputStream.skip(256 * Constants.MB));
    }
}
