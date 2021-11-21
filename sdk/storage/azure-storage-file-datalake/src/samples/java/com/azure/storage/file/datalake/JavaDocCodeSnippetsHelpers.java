// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.storage.file.datalake.models.UserDelegationKey;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.OffsetDateTime;

final class JavaDocCodeSnippetsHelpers {
    static DataLakeFileSystemAsyncClient getFileSystemAsyncClient() {
        return new DataLakeFileSystemClientBuilder().buildAsyncClient();
    }

    static DataLakeFileSystemClient getFileSystemClient() {
        return new DataLakeFileSystemClientBuilder().buildClient();
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

    static UserDelegationKey getUserDelegationKey() {
        return getDataLakeServiceClient().getUserDelegationKey(null, OffsetDateTime.now().plusDays(30));
    }
}
