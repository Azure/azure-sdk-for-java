// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch.generated;

import com.azure.compute.batch.BatchServiceClientBuilder;
import com.azure.compute.batch.PoolClient;
import com.azure.compute.batch.models.BatchPool;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.api.Test;

import java.io.Console;

public class RandomTest extends BatchServiceClientTestBase {
    @Test
    public void testFoo() throws Exception {
        DefaultAzureCredential azureCredential = new DefaultAzureCredentialBuilder().build();
        BatchServiceClientBuilder batchServiceClient =
            new BatchServiceClientBuilder()
                .endpoint(Configuration.getGlobalConfiguration().get("AZURE_BATCH_ENDPOINT", "endpoint"))
                .httpClient(HttpClient.createDefault())
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC));

        batchServiceClient.credential(azureCredential);
        PoolClient poolOps = batchServiceClient.buildPoolClient();
        BatchPool pool = poolOps.get("BatchUser-testIaaSpool");
        System.out.println(pool.getId());

        System.out.println("In test file");
        assert true;
    }
}
