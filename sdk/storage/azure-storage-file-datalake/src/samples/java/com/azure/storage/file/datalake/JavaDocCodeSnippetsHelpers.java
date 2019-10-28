// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import java.net.MalformedURLException;
import java.net.URL;

final class JavaDocCodeSnippetsHelpers {
    static FileSystemAsyncClient getFileSystemAsyncClient() {
        return new FileSystemClientBuilder().buildAsyncClient();
    }

    static FileSystemClient getFileSystemClient() {
        return new FileSystemClientBuilder().buildClient();
    }

    static DataLakeFileAsyncClient getFileAsyncClient(String fileName) {
        return getFileSystemAsyncClient().getFileAsyncClient(fileName);
    }

    static DataLakeFileClient getFileClient(String fileName) {
        return getFileSystemClient().getFileClient(fileName);
    }

    static DataLakeDirectoryAsyncClient getDirectoryAsyncClient(String directoryName) {
        return getFileSystemAsyncClient().getDirectoryAsyncClient(directoryName);
    }

    static DataLakeDirectoryClient getDirectoryClient(String directoryName) {
        return getFileSystemClient().getDirectoryClient(directoryName);
    }


    static DataLakeServiceAsyncClient getDataLakeServiceAsyncClient() {
        return new DataLakeServiceClientBuilder().buildAsyncClient();
    }

    static DataLakeServiceClient getDataLakeServiceClient() {
        return new DataLakeServiceClientBuilder().buildClient();
    }

    static URL generateURL(String urlString) {
        try {
            return new URL(urlString);
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
    }
}
