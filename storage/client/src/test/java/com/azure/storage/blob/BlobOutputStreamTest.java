package com.azure.storage.blob;

import com.azure.core.http.HttpClient;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.ProxyOptions.Type;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.google.common.io.ByteStreams;
import com.google.common.primitives.Bytes;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
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
            .httpClient(HttpClient.createDefault().proxy(() -> new ProxyOptions(Type.HTTP, new InetSocketAddress("localhost", 8888))))
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
        byte[] downloaded = ByteStreams.toByteArray(blobInputStream);
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
        byte[] downloaded = ByteStreams.toByteArray(blobInputStream);
        Assert.assertArrayEquals(randomBytes, downloaded);
    }

    @Test
    public void testAppendBlobOutputStream() throws Exception {
        int length = 16 * Constants.MB;
        String blobName = "testblob" + RANDOM.nextInt(1000);
        List<byte[]> randomBytes = new ArrayList<>();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        for (int i = 0; i != 16; i+=4) {
            byte[] bytes = new byte[4 * Constants.MB];
            RANDOM.nextBytes(bytes);
            randomBytes.add(bytes);
            stream.write(bytes);
        }

        byte[] uploaded = stream.toByteArray();
//        for (int i = 0; i != length; i++) {
//            randomBytes[i] = (byte) (i % 255);
//        }

        AppendBlobClient appendBlobClientNormal = containerClient.getAppendBlobClient(blobName + "Normal");
        appendBlobClientNormal.create();
        AppendBlobClient appendBlobClient = containerClient.getAppendBlobClient(blobName);
        appendBlobClient.create();
        for (int i = 0; i != 4; i ++) {
            final int finali = i;
            appendBlobClientNormal.appendBlock(ByteBufFlux.fromInbound(Mono.defer(() -> Mono.just(randomBytes.get(finali)))), 4 * Constants.MB);
        }
        BlobOutputStream outStream = appendBlobClient.getBlobOutputStream();
        for (int i = 0; i != 16/4; i++) {
//            System.out.println("Byte at " + i * Constants.MB + ": " + randomBytes[i * Constants.MB]);
            outStream.write(randomBytes.get(i));
        }
        outStream.close();

        Assert.assertEquals(length, appendBlobClientNormal.getProperties().value().blobSize());
        BlobInputStream blobInputStream = appendBlobClientNormal.openInputStream();
        byte[] downloaded = ByteStreams.toByteArray(blobInputStream);
        Assert.assertArrayEquals(uploaded, downloaded);
        Assert.assertEquals(length, appendBlobClient.getProperties().value().blobSize());
        blobInputStream = appendBlobClient.openInputStream();
        downloaded = ByteStreams.toByteArray(blobInputStream);
        Assert.assertArrayEquals(uploaded, downloaded);
    }
}
