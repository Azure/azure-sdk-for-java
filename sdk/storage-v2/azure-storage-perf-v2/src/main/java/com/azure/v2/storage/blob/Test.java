package com.azure.v2.storage.blob;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.client.JdkHttpClientBuilder;

import java.io.IOException;
import java.io.InputStream;

public class Test {

    public static void main(String[] args) {

        final BlobClient blobClient;

        String sasURL
            = "https://vigerastgtest.blob.core.windows.net/?sv=2024-11-04&ss=b&srt=sco&sp=rwdlaciytfx&se=2025-05-30T18:30:34Z&st=2025-05-30T10:30:34Z&spr=https&sig=EuYQxXmZpVxalZy%2BivkURQKs6JeFBPQLgtqQvFJpb%2Bw%3D";

        HttpClient httpClient = new JdkHttpClientBuilder().build();
        blobClient = new AzureBlobStorageBuilder().url(sasURL).httpClient(httpClient).buildBlobClient();

        InputStream inputStream = null;
        try {
            inputStream = blobClient.download("test", "10KB", null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null);

            // Consume InputStream with minimal work
            drainInputStream(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

    }

    /**
     * Drains the InputStream as quickly as possible without processing its content.
     */
    private static void drainInputStream(InputStream inputStream) throws IOException {
        final byte[] buffer = new byte[8192];
        while (inputStream.read(buffer) != -1) {
            // Do nothing, just read and discard the data
        }
    }
}
