package com.azure.storage.blob;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Random;

public class LargeFileTest {
    private static final Random RANDOM = new Random();
    private static final String filePath = "C:\\Users\\jianghlu\\test-downloaded.vhd";
    private static StorageClient storageClient;
    private static ContainerClient containerClient;

    @BeforeClass
    public static void setup() {
        storageClient = StorageClient.storageClientBuilder()
            .credentials(new SharedKeyCredentials(System.getenv("ACCOUNT_NAME"), System.getenv("ACCOUNT_KEY")))
            .endpoint("https://" + System.getenv("ACCOUNT_NAME") + ".blob.core.windows.net")
//            .httpClient(HttpClient.createDefault().proxy(() -> new ProxyOptions(Type.HTTP, new InetSocketAddress("localhost", 8888))))
            .buildClient();

        containerClient = storageClient.getContainerClient("testcontainer" + RANDOM.nextInt(1000));
        containerClient.create();
    }

    @Test
    public void uploadLargeBlockBlob() throws Exception {
        BlockBlobClient blockBlobClient = containerClient.getBlockBlobClient("testblob" + RANDOM.nextInt(1000));
        blockBlobClient.uploadFromFile(filePath);
    }

    @Test
    public void downloadLargeBlockBlob() throws Exception {
        OffsetDateTime start = OffsetDateTime.now();
        BlockBlobClient blockBlobClient = containerClient.getBlockBlobClient("testblob" + RANDOM.nextInt(1000));
        blockBlobClient.uploadFromFile(filePath);
        File uploadedFile = new File(filePath);
        System.out.println("Upload " + uploadedFile.length() + " bytes took " + Duration.between(start, OffsetDateTime.now()).getSeconds() + " seconds");
        start = OffsetDateTime.now();
        String downloadName = "jdk-downloaded.exe";
        File downloaded = new File(downloadName);
        downloaded.createNewFile();
        blockBlobClient.downloadToFile(downloadName);
        System.out.println("Download " + downloaded.length() + " bytes took " + Duration.between(start, OffsetDateTime.now()).getSeconds() + " seconds");
        downloaded.delete();
    }
}
