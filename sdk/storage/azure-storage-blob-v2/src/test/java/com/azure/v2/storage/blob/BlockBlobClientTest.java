package com.azure.v2.storage.blob;

import io.clientcore.core.util.binarydata.BinaryData;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class BlockBlobClientTest {

    @Test
    @Disabled
    public void testUpload() {
        BlockBlobClient blockBlobClient = new AzureBlobStorageBuilder()
                .url("https://srnagarstorage.blob.core.windows.net/?sv=2022-11-02&ss=bfqt&srt=sco&sp=rwdlacupiytfx&se=2024-12-23T03:55:26Z&st=2024-12-12T19:55:26Z&spr=https&sig=7Dj0MbP2z9bnXRQdoohMtCKsfzXYfUKrRTfboiRZ%2Bv8%3D")
                .buildBlockBlobClient();

        String content = "Hello World!";
        blockBlobClient.upload("testcontainer", "upload.txt", content.length(), BinaryData.fromString(content), null,
                null, null, null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null, null,
                null, null, null);

    }
}
