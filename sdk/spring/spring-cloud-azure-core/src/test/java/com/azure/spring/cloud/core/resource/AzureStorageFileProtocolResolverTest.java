// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.resource;

import com.azure.storage.file.share.ShareClient;
import com.azure.storage.file.share.ShareFileClient;
import com.azure.storage.file.share.ShareServiceClient;
import com.azure.storage.file.share.StorageFileInputStream;
import com.azure.storage.file.share.StorageFileOutputStream;
import com.azure.storage.file.share.models.ShareFileProperties;
import com.azure.storage.file.share.models.ShareFileRange;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.io.ProtocolResolver;
import org.springframework.core.io.Resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
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

        // azure-file://container/blob
        // mock sdk client to return an existing blob information
        this.shareServiceClient = mock(ShareServiceClient.class);

        shareClient = mock(ShareClient.class);
        shareFileClient = mock(ShareFileClient.class);

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
}
