// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package blob;

import com.azure.storage.blob.BlockBlobClient;
import com.azure.storage.blob.ContainerClient;
import com.azure.storage.blob.StorageClient;
import com.azure.storage.blob.StorageClientBuilder;
import com.azure.storage.common.credentials.SharedKeyCredential;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Locale;

import static blob.SampleHelper.getAccountKey;
import static blob.SampleHelper.getAccountName;

/**
 * This example shows how to start using the Azure Storage Blob SDK for Java.
 */
public class BasicExample {

    public static void main(String[] args) throws Exception {

        /*
         * From the Azure portal, get your Storage account's name and account key.
         */
        String accountName = getAccountName();
        String accountKey = getAccountKey();

        /*
         * Use your Storage account's name and key to create a credential object; this is used to access your account.
         */
        SharedKeyCredential credential = new SharedKeyCredential(accountName, accountKey);

        /*
         * From the Azure portal, get your Storage account blob service URL endpoint.
         * The URL typically looks like this:
         */
        String endpoint = String.format(Locale.ROOT, "https://%s.blob.core.windows.net", accountName);

        /*
         * Create a StorageClient object that wraps the service endpoint, credential and a request pipeline.
         */
        StorageClient storageClient = new StorageClientBuilder().endpoint(endpoint).credential(credential).buildClient();

        /*
         * This example shows several common operations just to get you started.
         */

        /*
         * Create a client that references a to-be-created container in your Azure Storage account. This returns a
         * ContainerClient object that wraps the container's endpoint, credential and a request pipeline (inherited from storageClient).
         * Note that container names require lowercase.
         */
        ContainerClient containerClient = storageClient.getContainerClient("myjavacontainerbasic" + System.currentTimeMillis());

        /*
         * Create a container in Storage blob account.
         */
        containerClient.create();

        /*
         * Create a client that references a to-be-created blob in your Azure Storage account's container.
         * This returns a BlockBlobClient object that wraps the blob's endpoint, credential and a request pipeline
         * (inherited from containerClient). Note that blob names can be mixed case.
         */
        BlockBlobClient blobClient = containerClient.getBlockBlobClient("HelloWorld.txt");

        String data = "Hello world!";
        InputStream dataStream = new ByteArrayInputStream(data.getBytes());

        /*
         * Create the blob with string (plain text) content.
         */
        blobClient.upload(dataStream, data.length());

        dataStream.close();

        /*
         * Download the blob's content to output stream.
         */
        int dataSize = (int) blobClient.getProperties().value().blobSize();
        OutputStream outputStream = new ByteArrayOutputStream(dataSize);
        blobClient.download(outputStream);
        outputStream.close();

        /*
         * Verify that the blob data round-tripped correctly.
         */
        if (!data.equals(outputStream.toString())) {
            throw new RuntimeException("The downloaded data does not match the uploaded data.");
        }

        /*
         * Create more blobs before listing.
         */
        for(int i = 0; i < 3; i++) {
            String sampleData = "Samples";
            InputStream dataInBlobs = new ByteArrayInputStream(sampleData.getBytes(Charset.defaultCharset()));
            containerClient.getBlockBlobClient("myblobsforlisting" + System.currentTimeMillis())
                    .upload(dataInBlobs, sampleData.length());
            dataInBlobs.close();
        }

        /*
         * List the blob(s) in our container.
         */
        containerClient.listBlobsFlat()
            .forEach(blobItem -> {
                System.out.println("Blob name: " + blobItem.name() + ", Snapshot: " + blobItem.snapshot());
            });;

        /*
         * Delete the blob we created earlier.
         */
        blobClient.delete();

        /*
         * Delete the container we created earlier.
         */
        containerClient.delete();
    }
}
