package com.azure.storage.blob;

import com.azure.storage.common.credentials.SharedKeyCredential;

import java.io.File;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Random;

public class LargeFileTest {
    private static final Random RANDOM = new Random();
    private static final String filePath = "C:\\Users\\jianghlu\\10g.dat";
    private static StorageClient storageClient;
    private static ContainerClient containerClient;

    //@BeforeClass
    public static void setup() {
        storageClient = StorageClient.storageClientBuilder()
            .credential(new SharedKeyCredential(System.getenv("ACCOUNT_NAME"), System.getenv("ACCOUNT_KEY")))
            .endpoint("https://" + System.getenv("ACCOUNT_NAME") + ".blob.core.windows.net")
//            .httpClient(HttpClient.createDefault().proxy(() -> new ProxyOptions(Type.HTTP, new InetSocketAddress("localhost", 8888))))
            .buildClient();

        containerClient = storageClient.getContainerClient("testcontainer-10g");
        if (!containerClient.exists().value()) {
            containerClient.create();
        }
    }

    //@Test
    public void uploadLargeBlockBlob() throws Exception {
        BlockBlobClient blockBlobClient = containerClient.getBlockBlobClient("testblob" + RANDOM.nextInt(1000));
        blockBlobClient.uploadFromFile(filePath);
    }

    //@Test
    public void downloadLargeBlockBlob() throws Exception {
        OffsetDateTime start = OffsetDateTime.now();
        BlockBlobClient blockBlobClient = containerClient.getBlockBlobClient("testblob-10g");
//        blockBlobClient.uploadFromFile(filePath);
//        File uploadedFile = new File(filePath);
//        System.out.println("Upload " + uploadedFile.length() + " bytes took " + Duration.between(start, OffsetDateTime.now()).getSeconds() + " seconds");
//        start = OffsetDateTime.now();
        String downloadName = "D:\\10g-downloaded.dat";
        File downloaded = new File(downloadName);
        downloaded.createNewFile();
        blockBlobClient.downloadToFile(downloadName);
        System.out.println("Download " + downloaded.length() + " bytes took " + Duration.between(start, OffsetDateTime.now()).getSeconds() + " seconds");
        downloaded.delete();
    }
}
