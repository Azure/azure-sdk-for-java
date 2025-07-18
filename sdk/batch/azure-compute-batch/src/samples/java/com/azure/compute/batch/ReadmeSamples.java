// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.compute.batch;

import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

public final class ReadmeSamples {
    public void readmeSamples() {
        // BEGIN: com.azure.compute.batch.readme
        // END: com.azure.compute.batch.readme

        // BEGIN: com.azure.compute.batch.build-client
        BatchClient batchClient = new BatchClientBuilder().credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT"))
            .buildClient();
        // END: com.azure.compute.batch.build-client
    }
}
