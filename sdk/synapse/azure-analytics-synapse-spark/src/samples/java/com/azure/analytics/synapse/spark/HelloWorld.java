// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.synapse.spark;

import com.azure.analytics.synapse.spark.models.SparkBatchJob;
import com.azure.analytics.synapse.spark.models.SparkBatchJobCollection;
import com.azure.analytics.synapse.spark.models.SparkBatchJobOptions;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.identity.AzureCliCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.Arrays;

public class HelloWorld {

    /**
     * Authenticates with the Synapse workspace and shows how to set, get, update and delete a role assignment in the workspace.
     *
     * @param args Unused. Arguments to the program.
     * @throws IllegalArgumentException when invalid workspace endpoint is passed.
     */
    public static void main(String[] args) throws  IllegalArgumentException {
        // Instantiate a Spark Batch client that will be used to call the service. Notice that the client is using default Azure
        // credentials. To make default credentials work, ensure that environment variables 'AZURE_CLIENT_ID',
        // 'AZURE_CLIENT_SECRET' and 'AZURE_TENANT_ID' are set with the service principal credentials.
        SparkBatchClient batchClient = new SparkClientBuilder()
            .endpoint("https://xysynapsetest.dev.azuresynapse.net")
            .sparkPoolName("jianghaospool")
            .credential(new AzureCliCredentialBuilder().build())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS).setPrettyPrintBody(true))
            .buildSparkBatchClient();

        String storageAccount = "xydatalake";
        String fileSystem = "xydatalakeg2";
        String name = "jianghao-sample";
        String file = String.format("abfss://%s@%s.dfs.core.windows.net/wordcount.jar", fileSystem, storageAccount);
        SparkBatchJobOptions options = new SparkBatchJobOptions()
            .setName(name)
            .setFile(file)
            .setClassName("WordCount")
            .setArguments(Arrays.asList(
                String.format("abfss://%s@%s.dfs.core.windows.net/shakespeare.txt", fileSystem, storageAccount),
                String.format("abfss://%s@%s.dfs.core.windows.net/java-result/", fileSystem, storageAccount)
            ))
            .setDriverMemory("28g")
            .setDriverCores(4)
            .setExecutorMemory("28g")
            .setExecutorCores(4)
            .setExecutorCount(2);

        SparkBatchJob jobCreated = batchClient.createSparkBatchJob(options);

        // List Spark batch jobs
        SparkBatchJobCollection jobs = batchClient.getSparkBatchJobs();
        for (SparkBatchJob job : jobs.getSessions()) {
            System.out.printf("Job is returned with name %s and state %s\n", job.getName(), job.getState());
        }

        // Cancel Spark batch job
        int jobId = jobCreated.getId();
        batchClient.cancelSparkBatchJob(0);
    }
}
