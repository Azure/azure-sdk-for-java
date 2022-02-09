// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.file.datalake.models.PathItem;
import com.azure.storage.file.datalake.models.PathProperties;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 *
 * Code samples for the README.md
 */
public class ReadmeSamples {

    private final DataLakeServiceClient dataLakeServiceClient = new DataLakeServiceClientBuilder().buildClient();
    private final DataLakeFileSystemClient dataLakeFileSystemClient = new DataLakeFileSystemClientBuilder()
        .buildClient();
    private final DataLakeFileClient dataLakeFileClient = new DataLakePathClientBuilder().buildFileClient();
    private final DataLakeDirectoryClient dataLakeDirectoryClient = new DataLakePathClientBuilder()
        .buildDirectoryClient();

    public void getDataLakeServiceClient1() {
        // BEGIN: readme-sample-getDataLakeServiceClient1
        DataLakeServiceClient dataLakeServiceClient = new DataLakeServiceClientBuilder()
            .endpoint("<your-storage-account-url>")
            .sasToken("<your-sasToken>")
            .buildClient();
        // END: readme-sample-getDataLakeServiceClient1
    }

    public void getDataLakeServiceClient2() {
        // BEGIN: readme-sample-getDataLakeServiceClient2
        // Only one "?" is needed here. If the sastoken starts with "?", please removing one "?".
        DataLakeServiceClient dataLakeServiceClient = new DataLakeServiceClientBuilder()
            .endpoint("<your-storage-account-url>" + "?" + "<your-sasToken>")
            .buildClient();
        // END: readme-sample-getDataLakeServiceClient2
    }

    public void getDataLakeFileSystemClient1() {
        // BEGIN: readme-sample-getDataLakeFileSystemClient1
        DataLakeFileSystemClient dataLakeFileSystemClient = dataLakeServiceClient.getFileSystemClient("myfilesystem");
        // END: readme-sample-getDataLakeFileSystemClient1
    }

    public void getDataLakeFileSystemClient2() {
        // BEGIN: readme-sample-getDataLakeFileSystemClient2
        DataLakeFileSystemClient dataLakeFileSystemClient = new DataLakeFileSystemClientBuilder()
            .endpoint("<your-storage-account-url>")
            .sasToken("<your-sasToken>")
            .fileSystemName("myfilesystem")
            .buildClient();
        // END: readme-sample-getDataLakeFileSystemClient2
    }

    public void getDataLakeFileSystemClient3() {
        // BEGIN: readme-sample-getDataLakeFileSystemClient3
        // Only one "?" is needed here. If the sastoken starts with "?", please removing one "?".
        DataLakeFileSystemClient dataLakeFileSystemClient = new DataLakeFileSystemClientBuilder()
            .endpoint("<your-storage-account-url>" + "/" + "myfilesystem" + "?" + "<your-sasToken>")
            .buildClient();
        // END: readme-sample-getDataLakeFileSystemClient3
    }

    public void getFileClient1() {
        // BEGIN: readme-sample-getFileClient1
        DataLakeFileClient fileClient = dataLakeFileSystemClient.getFileClient("myfile");
        // END: readme-sample-getFileClient1
    }

    public void getFileClient2() {
        // BEGIN: readme-sample-getFileClient2
        DataLakeFileClient fileClient = new DataLakePathClientBuilder()
            .endpoint("<your-storage-account-url>")
            .sasToken("<your-sasToken>")
            .fileSystemName("myfilesystem")
            .pathName("myfile")
            .buildFileClient();
        // END: readme-sample-getFileClient2
    }

    public void getFileClient3() {
        // BEGIN: readme-sample-getFileClient3
        // Only one "?" is needed here. If the sastoken starts with "?", please removing one "?".
        DataLakeFileClient fileClient = new DataLakePathClientBuilder()
            .endpoint("<your-storage-account-url>" + "/" + "myfilesystem" + "/" + "myfile" + "?" + "<your-sasToken>")
            .buildFileClient();
        // END: readme-sample-getFileClient3
    }

    public void getDirClient1() {
        // BEGIN: readme-sample-getDirClient1
        DataLakeDirectoryClient directoryClient = dataLakeFileSystemClient.getDirectoryClient("mydir");
        // END: readme-sample-getDirClient1
    }

    public void getDirClient2() {
        // BEGIN: readme-sample-getDirClient2
        DataLakeDirectoryClient directoryClient = new DataLakePathClientBuilder()
            .endpoint("<your-storage-account-url>")
            .sasToken("<your-sasToken>")
            .fileSystemName("myfilesystem")
            .pathName("mydir")
            .buildDirectoryClient();
        // END: readme-sample-getDirClient2
    }

    public void getDirClient3() {
        // BEGIN: readme-sample-getDirClient3
        // Only one "?" is needed here. If the sastoken starts with "?", please removing one "?".
        DataLakeDirectoryClient directoryClient = new DataLakePathClientBuilder()
            .endpoint("<your-storage-account-url>" + "/" + "myfilesystem" + "/" + "mydir" + "?" + "<your-sasToken>")
            .buildDirectoryClient();
        // END: readme-sample-getDirClient3
    }

    public void createDataLakeFileSystemClient1() {
        // BEGIN: readme-sample-createDataLakeFileSystemClient1
        dataLakeServiceClient.createFileSystem("myfilesystem");
        // END: readme-sample-createDataLakeFileSystemClient1
    }

    public void createDataLakeFileSystemClient2() {
        // BEGIN: readme-sample-createDataLakeFileSystemClient2
        dataLakeFileSystemClient.create();
        // END: readme-sample-createDataLakeFileSystemClient2
    }

    public void enumeratePaths() {
        // BEGIN: readme-sample-enumeratePaths
        for (PathItem pathItem : dataLakeFileSystemClient.listPaths()) {
            System.out.println("This is the path name: " + pathItem.getName());
        }
        // END: readme-sample-enumeratePaths
    }

    public void renameFile() {
        // BEGIN: readme-sample-renameFile
        //Need to authenticate with azure identity and add role assignment "Storage Blob Data Contributor" to do the following operation.
        DataLakeFileClient fileClient = dataLakeFileSystemClient.getFileClient("myfile");
        fileClient.create();
        fileClient.rename("new-file-system-name", "new-file-name");
        // END: readme-sample-renameFile
    }

    public void renameDirectory() {
        // BEGIN: readme-sample-renameDirectory
        //Need to authenticate with azure identity and add role assignment "Storage Blob Data Contributor" to do the following operation.
        DataLakeDirectoryClient directoryClient = dataLakeFileSystemClient.getDirectoryClient("mydir");
        directoryClient.create();
        directoryClient.rename("new-file-system-name", "new-directory-name");
        // END: readme-sample-renameDirectory
    }

    public void getPropertiesFile() {
        // BEGIN: readme-sample-getPropertiesFile
        DataLakeFileClient fileClient = dataLakeFileSystemClient.getFileClient("myfile");
        fileClient.create();
        PathProperties properties = fileClient.getProperties();
        // END: readme-sample-getPropertiesFile
    }

    public void getPropertiesDirectory() {
        // BEGIN: readme-sample-getPropertiesDirectory
        DataLakeDirectoryClient directoryClient = dataLakeFileSystemClient.getDirectoryClient("mydir");
        directoryClient.create();
        PathProperties properties = directoryClient.getProperties();
        // END: readme-sample-getPropertiesDirectory
    }

    public void authWithIdentity() {
        // BEGIN: readme-sample-authWithIdentity
        DataLakeServiceClient storageClient = new DataLakeServiceClientBuilder()
            .endpoint("<your-storage-account-url>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
        // END: readme-sample-authWithIdentity
    }

}

