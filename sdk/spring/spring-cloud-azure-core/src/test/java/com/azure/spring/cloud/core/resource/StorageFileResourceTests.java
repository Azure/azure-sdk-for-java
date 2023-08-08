// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.resource;

import com.azure.storage.file.share.ShareClient;
import com.azure.storage.file.share.ShareFileClient;
import com.azure.storage.file.share.ShareServiceClient;
import com.azure.storage.file.share.StorageFileOutputStream;
import com.azure.storage.file.share.models.ShareErrorCode;
import com.azure.storage.file.share.models.ShareStorageException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StorageFileResourceTests {
    private ShareServiceClient shareServiceClient;

    @BeforeEach
    void init() {
        this.shareServiceClient = mock(ShareServiceClient.class);
    }

    @Test
    void getInputStreamThrowFileNotFoundExceptionWhenShareNotFound() {
        ShareClient shareClient = mock(ShareClient.class);
        ShareFileClient shareFileClient = mock(ShareFileClient.class);
        when(this.shareServiceClient.getShareClient("some-share")).thenReturn(shareClient);
        when(shareClient.getFileClient("some-file.txt")).thenReturn(shareFileClient);

        StorageFileResource storageFileResource = new StorageFileResource(this.shareServiceClient, "azure-file://some-share/some-file.txt");

        ShareStorageException shareStorageException = mock(ShareStorageException.class);
        when(shareStorageException.getErrorCode()).thenReturn(ShareErrorCode.SHARE_NOT_FOUND);
        when(shareFileClient.openInputStream()).thenThrow(shareStorageException);

        Assertions.assertThrows(FileNotFoundException.class, () -> storageFileResource.getInputStream());
    }

    @Test
    void getInputStreamThrowFileNotFoundExceptionWhenFileNotFound() {
        ShareClient shareClient = mock(ShareClient.class);
        ShareFileClient shareFileClient = mock(ShareFileClient.class);
        when(this.shareServiceClient.getShareClient("some-share")).thenReturn(shareClient);
        when(shareClient.getFileClient("some-file.txt")).thenReturn(shareFileClient);

        StorageFileResource storageFileResource = new StorageFileResource(this.shareServiceClient, "azure-file://some-share/some-file.txt");

        ShareStorageException shareStorageException = mock(ShareStorageException.class);
        when(shareStorageException.getErrorCode()).thenReturn(ShareErrorCode.RESOURCE_NOT_FOUND);
        when(shareFileClient.openInputStream()).thenThrow(shareStorageException);

        Assertions.assertThrows(FileNotFoundException.class, () -> storageFileResource.getInputStream());
    }

    @Test
    void getInputStreamThrowIOExceptionOnOtherStorageException() {
        ShareClient shareClient = mock(ShareClient.class);
        ShareFileClient shareFileClient = mock(ShareFileClient.class);
        when(this.shareServiceClient.getShareClient("some-share")).thenReturn(shareClient);
        when(shareClient.getFileClient("some-file.txt")).thenReturn(shareFileClient);

        StorageFileResource storageFileResource = new StorageFileResource(this.shareServiceClient, "azure-file://some-share/some-file.txt");

        ShareStorageException shareStorageException = mock(ShareStorageException.class);
        when(shareStorageException.getErrorCode()).thenReturn(ShareErrorCode.INVALID_INPUT);
        when(shareFileClient.openInputStream()).thenThrow(shareStorageException);

        Assertions.assertThrows(IOException.class, () -> storageFileResource.getInputStream());
    }

    @Test
    void getOutputStreamWhenShareExistsWithoutAutoCreate() {
        ShareClient shareClient = mock(ShareClient.class);
        ShareFileClient shareFileClient = mock(ShareFileClient.class);
        when(this.shareServiceClient.getShareClient("some-share")).thenReturn(shareClient);
        when(shareClient.getFileClient("some-file.txt")).thenReturn(shareFileClient);

        StorageFileResource storageFileResource = new StorageFileResource(this.shareServiceClient, "azure-file://some-share/some-file.txt", false);

        StorageFileOutputStream storageFileOutputStream = mock(StorageFileOutputStream.class);
        when(shareFileClient.getFileOutputStream()).thenReturn(storageFileOutputStream);

        ShareStorageException shareStorageException = mock(ShareStorageException.class);
        when(shareStorageException.getErrorCode()).thenReturn(ShareErrorCode.SHARE_NOT_FOUND);
        doThrow(shareStorageException).when(storageFileOutputStream).write(any());

        Assertions.assertThrows(ShareStorageException.class, () -> storageFileResource.getOutputStream().write("some-text".getBytes()));
    }

}
