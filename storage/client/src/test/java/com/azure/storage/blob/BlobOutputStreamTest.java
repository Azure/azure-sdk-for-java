package com.azure.storage.blob;

import com.azure.storage.common.credentials.SharedKeyCredential;
import com.google.common.io.ByteStreams;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BlobOutputStreamTest {
    private static final Random RANDOM = new Random();
    private static StorageClient storageClient;
    private static ContainerClient containerClient;

    @BeforeClass
    public static void setup() {
        storageClient = StorageClient.storageClientBuilder()
            .endpoint("https://" + System.getenv("ACCOUNT_NAME") + ".blob.core.windows.net")
            .credential(new SharedKeyCredential(System.getenv("ACCOUNT_NAME"), System.getenv("ACCOUNT_KEY")))
//            .httpClient(HttpClient.createDefault().proxy(() -> new ProxyOptions(Type.HTTP, new InetSocketAddress("localhost", 8888))))
            .buildClient();
        String containerName = "testcontainer" + RANDOM.nextInt(1000);
        containerClient = storageClient.getContainerClient(containerName);
        if (!containerClient.exists().value()) {
            containerClient.create();
        }
    }

    @Test
    public void testBlockBlobOutputStream() throws Exception {
        String blobName = "testblob" + RANDOM.nextInt(1000);
        byte[] randomBytes = new byte[256 * Constants.MB];
        RANDOM.nextBytes(randomBytes);

        BlockBlobClient blockBlobClient = containerClient.getBlockBlobClient(blobName);
        BlobOutputStream outStream = blockBlobClient.getBlobOutputStream();
        outStream.write(randomBytes);
        outStream.close();

        BlobInputStream blobInputStream = blockBlobClient.openInputStream();
        byte[] downloaded = convertInputStreamToByteArray(blobInputStream);
        Assert.assertArrayEquals(randomBytes, downloaded);
    }

    @Test
    public void testPageBlobOutputStream() throws Exception {
        int length = 256 * Constants.MB;
        String blobName = "testblob" + RANDOM.nextInt(1000);
        byte[] randomBytes = new byte[length];
        RANDOM.nextBytes(randomBytes);

        PageBlobClient pageBlobClient = containerClient.getPageBlobClient(blobName);
        pageBlobClient.create(length);
        BlobOutputStream outStream = pageBlobClient.getBlobOutputStream(length);
        outStream.write(randomBytes);
        outStream.close();

        BlobInputStream blobInputStream = pageBlobClient.openInputStream();
        byte[] downloaded = convertInputStreamToByteArray(blobInputStream);
        Assert.assertArrayEquals(randomBytes, downloaded);
    }

    @Test
    public void testAppendBlobOutputStream() throws Exception {
        int length = 256 * Constants.MB;
        String blobName = "testblob" + RANDOM.nextInt(1000);
        List<byte[]> randomBytes = new ArrayList<>();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        for (int i = 0; i != 256; i+=4) {
            byte[] bytes = new byte[4 * Constants.MB];
            RANDOM.nextBytes(bytes);
            randomBytes.add(bytes);
            stream.write(bytes);
        }

        byte[] uploaded = stream.toByteArray();

        AppendBlobClient appendBlobClient = containerClient.getAppendBlobClient(blobName);
        appendBlobClient.create();
        BlobOutputStream outStream = appendBlobClient.getBlobOutputStream();
        for (int i = 0; i != 256/4; i++) {
            outStream.write(randomBytes.get(i));
        }
        outStream.close();

        Assert.assertEquals(length, appendBlobClient.getProperties().value().blobSize());
        BlobInputStream blobInputStream = appendBlobClient.openInputStream();
        byte[] downloaded = convertInputStreamToByteArray(blobInputStream);
        Assert.assertArrayEquals(uploaded, downloaded);
    }

    private byte[] convertInputStreamToByteArray(InputStream inputStream) {
        int b;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            while ((b = inputStream.read()) != -1) {
                outputStream.write(b);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        return outputStream.toByteArray();
    }
}
