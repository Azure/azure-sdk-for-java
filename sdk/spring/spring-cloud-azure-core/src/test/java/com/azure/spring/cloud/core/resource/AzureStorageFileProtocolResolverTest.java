// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.resource;

import com.azure.storage.file.share.ShareClient;
import com.azure.storage.file.share.ShareFileClient;
import com.azure.storage.file.share.ShareServiceClient;
import com.azure.storage.file.share.StorageFileInputStream;
import com.azure.storage.file.share.StorageFileOutputStream;
import com.azure.storage.file.share.models.ShareErrorCode;
import com.azure.storage.file.share.models.ShareFileProperties;
import com.azure.storage.file.share.models.ShareFileRange;
import com.azure.storage.file.share.models.ShareStorageException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.io.ProtocolResolver;
import org.springframework.core.io.Resource;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AzureStorageFileProtocolResolverTest extends AbstractAzureStorageProtocolResolverTests {

    private ShareServiceClient shareServiceClient;
    private ConfigurableListableBeanFactory beanFactory;
    private ShareClient shareClient;
    private ShareFileClient shareFileClient;

    @Override
    protected ProtocolResolver createInstance() {
        beanFactory = mock(ConfigurableListableBeanFactory.class);
        when(beanFactory.getBean(ShareServiceClient.class)).thenReturn(shareServiceClient);
        AzureStorageFileProtocolResolver protocolResolver = new AzureStorageFileProtocolResolver();
        protocolResolver.postProcessBeanFactory(beanFactory);
        return protocolResolver;
    }

    @Override
    protected void initializeSDKClient() {
        this.shareServiceClient = mock(ShareServiceClient.class);

        Tuple2<ShareClient, ShareFileClient> clientTuple2 = mockClientsForExistingShareAndFile(this.shareServiceClient, CONTAINER_NAME, EXISTING_ITEM_NAME);
        this.shareClient = clientTuple2.getT1();
        this.shareFileClient = clientTuple2.getT2();

        mockClientsForNonExistingShareAndFile(this.shareServiceClient, NON_EXISTING_CONTAINER_NAME, NON_EXISTING_ITEM_NAME);
        mockClientsForNonExistingFile(this.shareClient, CONTAINER_NAME, NON_EXISTING_ITEM_NAME);
    }

    @Override
    protected StorageType getStorageType() {
        return StorageType.FILE;
    }

    @Test
    void protocolPatternMatched() {
        when(shareServiceClient.getShareClient(anyString())).thenReturn(shareClient);
        when(shareClient.getFileClient(anyString())).thenReturn(shareFileClient);

        String[] locations = new String[] {"azure-file://test/test", "azure-file://test/test2"};
        Resource[] resources = getResources(locations);
        assertEquals(locations.length, resources.length, "Correct number of resources found");
    }

    @Test
    void protocolPatternNotMatched() {
        when(shareServiceClient.getShareClient(anyString())).thenReturn(shareClient);
        when(shareClient.getFileClient(anyString())).thenReturn(shareFileClient);

        String[] locations = new String[]{"azurefile:test/test", "otherfile:test/test2"};
        Resource[] resources = getResources(locations);
        assertEquals(0, resources.length, "No resolved resources found");
    }

    private Tuple2<ShareClient, ShareFileClient> mockClientsForExistingShareAndFile(
        ShareServiceClient shareServiceClient, String shareName, String fileName) {
        ShareClient shareClient = mock(ShareClient.class);
        ShareFileClient shareFileClient = mock(ShareFileClient.class);

        when(shareServiceClient.getShareClient(eq(shareName))).thenReturn(shareClient);
        when(shareClient.getFileClient(eq(fileName))).thenReturn(shareFileClient);
        when(shareClient.exists()).thenReturn(true);

        when(shareFileClient.exists()).thenReturn(true);
        when(shareFileClient.getShareName()).thenReturn(shareName);
        when(shareFileClient.getFilePath()).thenReturn(fileName);
        when(shareFileClient.openInputStream(any(ShareFileRange.class))).thenReturn(mock(StorageFileInputStream.class));
        when(shareFileClient.getFileOutputStream()).thenReturn(mock(StorageFileOutputStream.class));

        ShareFileProperties fileProperties = new ShareFileProperties(null, null, null, null, CONTENT_LENGTH, null, null,
            null, null, null, null, null, null, null, null, null, null, null);
        when(shareFileClient.getProperties()).thenReturn(fileProperties);
        return Tuples.of(shareClient, shareFileClient);
    }

    private void mockClientsForNonExistingShareAndFile(ShareServiceClient shareServiceClient, String shareName,
                                                       String fileName) {
        ShareClient nonExistingShareClient = mock(ShareClient.class);
        ShareFileClient nonExistingShareFileClient = mock(ShareFileClient.class);

        when(shareServiceClient.getShareClient(shareName)).thenReturn(nonExistingShareClient);
        when(nonExistingShareClient.getFileClient(fileName)).thenReturn(nonExistingShareFileClient);

        when(nonExistingShareFileClient.exists()).thenReturn(false);
        when(nonExistingShareFileClient.getShareName()).thenReturn(shareName);
        when(nonExistingShareFileClient.getFilePath()).thenReturn(fileName);

        ShareStorageException shareStorageException = mock(ShareStorageException.class);
        when(shareStorageException.getErrorCode()).thenReturn(ShareErrorCode.SHARE_NOT_FOUND);
        when(nonExistingShareFileClient.openInputStream()).thenThrow(shareStorageException);
    }

    private void mockClientsForNonExistingFile(ShareClient existingShareClient, String shareName, String fileName) {
        ShareFileClient nonExistingFileClient = mock(ShareFileClient.class);
        when(existingShareClient.getFileClient(fileName)).thenReturn(nonExistingFileClient);

        when(nonExistingFileClient.exists()).thenReturn(false);
        when(nonExistingFileClient.getShareName()).thenReturn(shareName);
        when(nonExistingFileClient.getFilePath()).thenReturn(fileName);

        ShareStorageException shareStorageException = mock(ShareStorageException.class);
        when(shareStorageException.getErrorCode()).thenReturn(ShareErrorCode.RESOURCE_NOT_FOUND);
        when(nonExistingFileClient.openInputStream()).thenThrow(shareStorageException);
    }
}
