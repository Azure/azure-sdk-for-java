// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.datalake;

import com.azure.core.http.rest.PagedFlux;
import com.azure.storage.file.datalake.models.DataLakeStorageException;
import com.azure.storage.file.datalake.models.FileSystemItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

public class AsyncErrorMappingTests extends DataLakeTestBase {
    private String fileSystemName;
    private DataLakeFileSystemAsyncClient fsac;

    @BeforeEach
    public void setup() {
        fileSystemName = generateFileSystemName();
        fsac = getServiceAsyncClient(ENVIRONMENT.getDataLakeAccount()).createFileSystem(fileSystemName).block();
    }

    @Test
    public void readFile() {
        StepVerifier.create(fsac.getFileAsyncClient(generatePathName()).readWithResponse(null, null, null, false))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void getFileProperties() {
        StepVerifier.create(fsac.getFileAsyncClient(generatePathName()).getPropertiesWithResponse(null))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void setFileHttpProperties() {
        StepVerifier.create(fsac.getFileAsyncClient(generatePathName()).setHttpHeadersWithResponse(null, null))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void setFileMetadata() {
        StepVerifier.create(fsac.getFileAsyncClient(generatePathName()).setMetadataWithResponse(null, null))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void getDirectoryProperties() {
        StepVerifier.create(fsac.getDirectoryAsyncClient(generatePathName()).getPropertiesWithResponse(null))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void setDirectoryHttpProperties() {
        StepVerifier.create(fsac.getDirectoryAsyncClient(generatePathName()).setHttpHeadersWithResponse(null, null))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void setDirectoryMetadata() {
        StepVerifier.create(fsac.getDirectoryAsyncClient(generatePathName()).setMetadataWithResponse(null, null))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void createFileSystem() {
        StepVerifier.create(getServiceAsyncClient(ENVIRONMENT.getDataLakeAccount())
            .getFileSystemAsyncClient(fileSystemName).createWithResponse(null, null))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void getFileSystemProperties() {
        StepVerifier.create(getFileSystemAsyncClient().getPropertiesWithResponse(null))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void setFileSystemMetadata() {
        StepVerifier.create(getFileSystemAsyncClient().setMetadataWithResponse(null, null))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void deleteFileSystem() {
        StepVerifier.create(getFileSystemAsyncClient().deleteWithResponse(null))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void getFileSystemAccessPolicy() {
        StepVerifier.create(getFileSystemAsyncClient().getAccessPolicyWithResponse(null))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void setFileSystemAccessPolicy() {
        StepVerifier.create(getFileSystemAsyncClient().setAccessPolicyWithResponse(null, null, null))
            .verifyError(DataLakeStorageException.class);
    }

    private DataLakeFileSystemAsyncClient getFileSystemAsyncClient() {
        return getServiceAsyncClient(ENVIRONMENT.getDataLakeAccount())
            .getFileSystemAsyncClient(generateFileSystemName());
    }

    @Test
    public void listFileSystems() {
        PagedFlux<FileSystemItem> items = getServiceAsyncClient(ENVIRONMENT.getDataLakeAccount()).listFileSystems();
        StepVerifier.create(items.byPage("garbage continuation token").count())
            .verifyError(DataLakeStorageException.class);
    }
}
