package com.azure.spring.core.resource;

import com.azure.storage.file.share.ShareClient;
import com.azure.storage.file.share.ShareFileClient;
import com.azure.storage.file.share.ShareServiceClient;
import com.azure.storage.file.share.StorageFileInputStream;
import com.azure.storage.file.share.StorageFileOutputStream;
import com.azure.storage.file.share.models.ShareFileProperties;
import com.azure.storage.file.share.models.ShareFileRange;
import org.springframework.core.io.ProtocolResolver;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AzureStorageFileProtocolResolverTest extends AbstractAzureStorageProtocolResolverTest {

    private ShareServiceClient shareServiceClient;

    @Override
    protected ProtocolResolver createInstance() {
        return new AzureStorageFileProtocolResolver(shareServiceClient);
    }

    @Override
    protected void initializeSDKClient() {

        // azure-file://container/blob
        // mock sdk client to return an existing blob information
        this.shareServiceClient = mock(ShareServiceClient.class);

        ShareClient shareClient = mock(ShareClient.class);
        ShareFileClient shareFileClient = mock(ShareFileClient.class);

        when(shareServiceClient.getShareClient(eq(CONTAINER_NAME))).thenReturn(shareClient);
        when(shareClient.getFileClient(eq(EXISTING_ITEM_NAME))).thenReturn(shareFileClient);

        when(shareClient.exists()).thenReturn(true);
        when(shareFileClient.exists()).thenReturn(true);
        ShareFileProperties fileProperties = new ShareFileProperties(null, null, null, null, CONTENT_LENGTH, null, null,
            null, null, null, null, null, null, null, null, null, null, null);
        when(shareFileClient.getProperties()).thenReturn(fileProperties);

        when(shareFileClient.getShareName()).thenReturn(CONTAINER_NAME);
        when(shareFileClient.getFilePath()).thenReturn(EXISTING_ITEM_NAME);
//        when(blockBlobClient.getBlobName()).thenReturn(BLOB_NAME);
        when(shareFileClient.openInputStream(any(ShareFileRange.class))).thenReturn(
            mock(StorageFileInputStream.class));

        when(shareFileClient.getFileOutputStream()).thenReturn(mock(StorageFileOutputStream.class));
        // azure-blob://non-existing/non-existing
        //mock sdk to return a non-existing blob (neither container nor blob exist)
        ShareClient nanExistingShareClient = mock(ShareClient.class);
        ShareFileClient nonExistingFile = mock(ShareFileClient.class);

        when(shareClient.getFileClient(eq(NON_EXISTING))).thenReturn(nonExistingFile);


        when(shareServiceClient.getShareClient(eq(NON_EXISTING))).thenReturn(nanExistingShareClient);
        when(nanExistingShareClient.getFileClient(eq(NON_EXISTING))).thenReturn(nonExistingFile);
        when(nanExistingShareClient.getShareName()).thenReturn(NON_EXISTING);
        when(nanExistingShareClient.exists()).thenReturn(false);
        when(nonExistingFile.exists()).thenReturn(false);
        when(nonExistingFile.getShareName()).thenReturn(NON_EXISTING);
        when(nonExistingFile.getFilePath()).thenReturn(NON_EXISTING);

    }

    @Override
    protected StorageType getStorageType() {
        return StorageType.FILE;
    }
}
