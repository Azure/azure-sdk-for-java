package blob;

import com.azure.storage.blob.BlockBlobClient;
import com.azure.storage.blob.ContainerClient;
import com.azure.storage.blob.StorageClient;
import com.azure.storage.blob.StorageClientBuilder;
import com.azure.storage.blob.models.BlobHTTPHeaders;
import com.azure.storage.blob.models.Metadata;
import com.azure.storage.common.credentials.SharedKeyCredential;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.Locale;

import static blob.SampleHelper.getAccountKey;
import static blob.SampleHelper.getAccountName;

/**
 * This example shows how to set metadata for containers and blobs and how to set HTTPHeaders for blobs
 * using the Azure Storage Blob SDK for Java.
 */
public class SetMetadataAndHTTPHeadersExample {

    public static void main(String[] args) throws Exception {
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
         * Create a container client from storageClient.
         */
        ContainerClient containerClient = storageClient.getContainerClient("mycontainer" + System.currentTimeMillis());

        /*
         * Setup containerMetadata for container.
         */
        Metadata containerMetadata = new Metadata(Collections.singletonMap("mycontainermetadata", "sample"));

        /*
         * Create a container with the containerMetadata above.
         */
        containerClient.create(containerMetadata, null, null);

        /*
         * Create a blob client.
         */
        BlockBlobClient blobClient = containerClient.getBlockBlobClient("myblob" + System.currentTimeMillis());

        /*
         * Create a blob with blob's blobMetadata and BlobHttpHeaders.
         */
        Metadata blobMetadata = new Metadata(Collections.singletonMap("myblobmetadata", "sample"));
        BlobHTTPHeaders blobHTTPHeaders = new BlobHTTPHeaders().blobContentDisposition("attachment")
            .blobContentType("text/html; charset=utf-8");

        /*
         * Data which will upload to block blob.
         */
        String data = "Hello world!";
        InputStream dataStream = new ByteArrayInputStream(data.getBytes());
        blobClient.upload(dataStream, data.length(), blobHTTPHeaders, blobMetadata, null, null);

        /*
         * Clean up the container and blob.
         */
        blobClient.delete();
        containerClient.delete();
    }
}
