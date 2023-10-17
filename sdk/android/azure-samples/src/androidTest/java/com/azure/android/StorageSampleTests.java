package com.azure.android;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.azure.storage.blob.specialized.BlockBlobClient;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Locale;

@RunWith(AndroidJUnit4.class)
public class StorageSampleTests {
    static final String storageAccountName = "androidazsdkstorage";

    static ClientSecretCredential clientSecretCredential;
    static BlobServiceClient storageClient;
    static BlobContainerClient blobContainerClient;
    static BlockBlobClient blobClient;
    static String endpoint;
    final String data = "Hello world!";
    @BeforeClass
    public static void setup() throws InterruptedException {
        // These are obtained by setting system environment variables
        // on the computer emulating the app
        clientSecretCredential = new ClientSecretCredentialBuilder()
            .clientId(BuildConfig.AZURE_CLIENT_ID)
            .clientSecret(BuildConfig.AZURE_CLIENT_SECRET)
            .tenantId(BuildConfig.AZURE_TENANT_ID)
            .build();

        endpoint = String.format(Locale.ROOT, "https://%s.blob.core.windows.net", storageAccountName);

        storageClient = new BlobServiceClientBuilder().endpoint(endpoint).credential(clientSecretCredential).buildClient();

        blobContainerClient = storageClient.getBlobContainerClient("myjavacontainerbasic" + System.currentTimeMillis());
        /*
         * Create a container in Storage blob account.
         */
        blobContainerClient.create();
        blobClient = blobContainerClient.getBlobClient("HelloWorld.txt").getBlockBlobClient();
    }

    @Test
    public void connectionTest() {
        // tests that connection was made
        assertTrue(blobContainerClient.exists());

    }

    @Test
    public void blobUploadDownloadTest() throws IOException {
        /*
         * Create a client that references a to-be-created blob in your Azure Storage account's container.
         * This returns a BlockBlobClient object that wraps the blob's endpoint, credential and a request pipeline
         * (inherited from containerClient). Note that blob names can be mixed case.
         */

        InputStream dataStream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
        /*
         * Create the blob with string (plain text) content.
         */
        blobClient.upload(dataStream, data.length());

        dataStream.close();

        /*
         * Download the blob's content to output stream.
         */
        int dataSize = (int) blobClient.getProperties().getBlobSize();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(dataSize);
        blobClient.downloadStream(outputStream);
        outputStream.close();

        /*
         * Verify that the blob data round-tripped correctly.
         */
        assertEquals(data, new String(outputStream.toByteArray(), StandardCharsets.UTF_8));
    }

    @Test
    public void listBlobsTest() throws IOException {

        // create comparison list
        ArrayList<String> blobList = new ArrayList<>();

        // add blobs
        long tempTime;
        for (int i = 0; i < 3; i++) {
            String sampleData = "Samples";
            InputStream dataInBlobs = new ByteArrayInputStream(sampleData.getBytes(Charset.defaultCharset()));
            tempTime = System.currentTimeMillis();
            blobContainerClient.getBlobClient("myblobsforlisting" + tempTime).getBlockBlobClient()
                .upload(dataInBlobs, sampleData.length());
            dataInBlobs.close();
            blobList.add("myblobsforlisting" + tempTime);
        }

        // Test that blobs in container are listed correctly.
        // This block does not complete in android API 26 and will time out.
        try {
            blobContainerClient.listBlobs(new ListBlobsOptions(), Duration.ofSeconds(45))
                .forEach(blobItem -> assertTrue(blobList.contains(blobItem.getName())));
        } catch (RuntimeException e) {
            // If listBlobs takes longer than 45 seconds fail the test
            fail("Listblobs operation timed out after 45 seconds");
        }
    }

    @AfterClass
    public static void cleanUp() {

        // Delete the blobContainerClient
        blobContainerClient.delete();

    }
}
