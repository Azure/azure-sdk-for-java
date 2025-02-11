package com.azure.v2.storage.blob;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

public class BlobClientTest {

    @Test
    @Disabled
    public void testDownload() throws IOException {

        BlobClient blobClient = new AzureBlobStorageBuilder()
                .url("sas-url")
                .buildBlobClient();

        InputStream sampleText = blobClient.download("testcontainer", "sample.txt", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        byte[] bytes = new byte[2048];
        sampleText.read(bytes);
        String text = new String(bytes);
        System.out.println(text);


    }
}
