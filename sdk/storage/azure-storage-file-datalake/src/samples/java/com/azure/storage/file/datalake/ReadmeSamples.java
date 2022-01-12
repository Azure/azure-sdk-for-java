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

    private DataLakeServiceClient dataLakeServiceClient = new DataLakeServiceClientBuilder().buildClient();
    private DataLakeFileSystemClient dataLakeFileSystemClient = new DataLakeFileSystemClientBuilder().buildClient();
    private DataLakeFileClient dataLakeFileClient = new DataLakePathClientBuilder().buildFileClient();
    private DataLakeDirectoryClient dataLakeDirectoryClient = new DataLakePathClientBuilder().buildDirectoryClient();

    public void getDataLakeServiceClient1() {
        DataLakeServiceClient dataLakeServiceClient = new DataLakeServiceClientBuilder()
            .endpoint("<your-storage-account-url>")
            .sasToken("<your-sasToken>")
            .buildClient();
    }

    public void getDataLakeServiceClient2() {
        // Only one "?" is needed here. If the sastoken starts with "?", please removing one "?".
        DataLakeServiceClient dataLakeServiceClient = new DataLakeServiceClientBuilder()
            .endpoint("<your-storage-account-url>" + "?" + "<your-sasToken>")
            .buildClient();
    }

    public void getDataLakeFileSystemClient1() {
        DataLakeFileSystemClient dataLakeFileSystemClient = dataLakeServiceClient.getFileSystemClient("myfilesystem");
    }

    public void getDataLakeFileSystemClient2() {
        DataLakeFileSystemClient dataLakeFileSystemClient = new DataLakeFileSystemClientBuilder()
            .endpoint("<your-storage-account-url>")
            .sasToken("<your-sasToken>")
            .fileSystemName("myfilesystem")
            .buildClient();
    }

    public void getDataLakeFileSystemClient3() {
        // Only one "?" is needed here. If the sastoken starts with "?", please removing one "?".
        DataLakeFileSystemClient dataLakeFileSystemClient = new DataLakeFileSystemClientBuilder()
            .endpoint("<your-storage-account-url>" + "/" + "myfilesystem" + "?" + "<your-sasToken>")
            .buildClient();
    }

    public void getFileClient1() {
        DataLakeFileClient fileClient = dataLakeFileSystemClient.getFileClient("myfile");
    }

    public void getFileClient2() {
        DataLakeFileClient fileClient = new DataLakePathClientBuilder()
            .endpoint("<your-storage-account-url>")
            .sasToken("<your-sasToken>")
            .fileSystemName("myfilesystem")
            .pathName("myfile")
            .buildFileClient();
    }

    public void getFileClient3() {
        // Only one "?" is needed here. If the sastoken starts with "?", please removing one "?".
        DataLakeFileClient fileClient = new DataLakePathClientBuilder()
            .endpoint("<your-storage-account-url>" + "/" + "myfilesystem" + "/" + "myfile" + "?" + "<your-sasToken>")
            .buildFileClient();
    }

    public void getDirClient1() {
        DataLakeDirectoryClient directoryClient = dataLakeFileSystemClient.getDirectoryClient("mydir");
    }

    public void getDirClient2() {
        DataLakeDirectoryClient directoryClient = new DataLakePathClientBuilder()
            .endpoint("<your-storage-account-url>")
            .sasToken("<your-sasToken>")
            .fileSystemName("myfilesystem")
            .pathName("mydir")
            .buildDirectoryClient();
    }

    public void getDirClient3() {
        // Only one "?" is needed here. If the sastoken starts with "?", please removing one "?".
        DataLakeDirectoryClient directoryClient = new DataLakePathClientBuilder()
            .endpoint("<your-storage-account-url>" + "/" + "myfilesystem" + "/" + "mydir" + "?" + "<your-sasToken>")
            .buildDirectoryClient();
    }

    public void createDataLakeFileSystemClient1() {
        dataLakeServiceClient.createFileSystem("myfilesystem");
    }

    public void createDataLakeFileSystemClient2() {
        dataLakeFileSystemClient.create();
    }

    public void enumeratePaths() {
        for (PathItem pathItem : dataLakeFileSystemClient.listPaths()) {
            System.out.println("This is the path name: " + pathItem.getName());
        }
    }

    public void renameFile() {
        //Need to authenticate with azure identity and add role assignment "Storage Blob Data Contributor" to do the following operation.
        DataLakeFileClient fileClient = dataLakeFileSystemClient.getFileClient("myfile");
        fileClient.create();
        fileClient.rename("new-file-system-name", "new-file-name");
    }

    public void renameDirectory() {
        //Need to authenticate with azure identity and add role assignment "Storage Blob Data Contributor" to do the following operation.
        DataLakeDirectoryClient directoryClient = dataLakeFileSystemClient.getDirectoryClient("mydir");
        directoryClient.create();
        directoryClient.rename("new-file-system-name", "new-directory-name");
    }

    public void getPropertiesFile() {
        DataLakeFileClient fileClient = dataLakeFileSystemClient.getFileClient("myfile");
        fileClient.create();
        PathProperties properties = fileClient.getProperties();
    }

    public void getPropertiesDirectory() {
        DataLakeDirectoryClient directoryClient = dataLakeFileSystemClient.getDirectoryClient("mydir");
        directoryClient.create();
        PathProperties properties = directoryClient.getProperties();
    }

    public void authWithIdentity() {
        DataLakeServiceClient storageClient = new DataLakeServiceClientBuilder()
            .endpoint("<your-storage-account-url>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
    }

}

