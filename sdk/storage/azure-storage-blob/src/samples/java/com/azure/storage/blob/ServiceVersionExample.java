// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.storage.common.StorageSharedKeyCredential;

import java.util.Locale;

public class ServiceVersionExample {

    public static void main(String[] args) {

        // More information on these properties can be found in BasicExample.java
        String accountName = SampleHelper.getAccountName();
        String accountKey = SampleHelper.getAccountKey();
        StorageSharedKeyCredential credential = new StorageSharedKeyCredential(accountName, accountKey);
        String endpoint = String.format(Locale.ROOT, "https://%s.blob.core.windows.net", accountName);

        // Select the desired version and set it on the builder
        BlobServiceVersion serviceVersion = BlobServiceVersion.V2019_02_02;
        BlobServiceClient storageClient = new BlobServiceClientBuilder()
            .serviceVersion(serviceVersion)
            .endpoint(endpoint).credential(credential).buildClient();

        // This service client may now be used as desired, and any service requests will target the specified version.

        /*
        If the workload includes generating new SAS tokens using an older format, the version of the sas generation can
        also be configured. Before starting the jvm, set the environment variable AZURE_STORAGE_SAS_SERVICE_VERSION or
        the JVM system propert -DAZURE_STORAGE_SAS_SERVICE_VERSION to the service version which corresponds to the
        desired SAS format to configure this behavior.
         */
    }
}
