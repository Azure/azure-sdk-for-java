// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.compute.batch;

import java.util.Arrays;

import org.junit.jupiter.api.Assertions;

import com.azure.compute.batch.models.AutoUserScope;
import com.azure.compute.batch.models.AutoUserSpecification;
import com.azure.compute.batch.models.BatchError;
import com.azure.compute.batch.models.BatchErrorException;
import com.azure.compute.batch.models.BatchJobActionKind;
import com.azure.compute.batch.models.BatchJobCreateParameters;
import com.azure.compute.batch.models.BatchPoolCreateParameters;
import com.azure.compute.batch.models.BatchPoolInfo;
import com.azure.compute.batch.models.BatchPoolResizeParameters;
import com.azure.compute.batch.models.BatchTaskCreateParameters;
import com.azure.compute.batch.models.BatchVmImageReference;
import com.azure.compute.batch.models.ElevationLevel;
import com.azure.compute.batch.models.ExitCodeRangeMapping;
import com.azure.compute.batch.models.ExitConditions;
import com.azure.compute.batch.models.ExitOptions;
import com.azure.compute.batch.models.UserIdentity;
import com.azure.compute.batch.models.VirtualMachineConfiguration;
import com.azure.core.credential.AzureNamedKeyCredential;
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

        // BEGIN: com.azure.compute.batch.build-sharedkey-client
        Configuration localConfig = Configuration.getGlobalConfiguration();
        String accountName = localConfig.get("AZURE_BATCH_ACCOUNT", "fakeaccount");
        String accountKey = localConfig.get("AZURE_BATCH_ACCESS_KEY", "fakekey");
        AzureNamedKeyCredential sharedKeyCreds = new AzureNamedKeyCredential(accountName, accountKey);

        BatchClientBuilder batchClientBuilder = new BatchClientBuilder();
        batchClientBuilder.credential(sharedKeyCreds);
        BatchClient batchClientWithSharedKey = batchClientBuilder.buildClient();
        // END: com.azure.compute.batch.build-sharedkey-client

        // BEGIN: com.azure.compute.batch.create-pool.creates-a-simple-pool
        batchClient.createPool(new BatchPoolCreateParameters("poolId", "STANDARD_DC2s_V2")
            .setVirtualMachineConfiguration(
                new VirtualMachineConfiguration(new BatchVmImageReference().setPublisher("Canonical")
                    .setOffer("UbuntuServer")
                    .setSku("18_04-lts-gen2")
                    .setVersion("latest"), "batch.node.ubuntu 18.04"))
            .setTargetDedicatedNodes(1), null);
        // END: com.azure.compute.batch.create-pool.creates-a-simple-pool

        // BEGIN: com.azure.compute.batch.create-job.creates-a-basic-job
        batchClient.createJob(
            new BatchJobCreateParameters("jobId", new BatchPoolInfo().setPoolId("poolId")).setPriority(0), null);
        // END: com.azure.compute.batch.create-job.creates-a-basic-job

        // BEGIN: com.azure.compute.batch.create-task.creates-a-simple-task
        String taskId = "ExampleTaskId";
        BatchTaskCreateParameters taskToCreate = new BatchTaskCreateParameters(taskId, "echo hello world");
        batchClient.createTask("jobId", taskToCreate);
        // END: com.azure.compute.batch.create-task.creates-a-simple-task

        // BEGIN: com.azure.compute.batch.create-task.creates-a-task-with-exit-conditions
        batchClient.createTask("jobId", new BatchTaskCreateParameters("taskId", "cmd /c exit 3")
            .setExitConditions(new ExitConditions().setExitCodeRanges(Arrays
                .asList(new ExitCodeRangeMapping(2, 4, new ExitOptions().setJobAction(BatchJobActionKind.TERMINATE)))))
            .setUserIdentity(new UserIdentity().setAutoUser(
                new AutoUserSpecification().setScope(AutoUserScope.TASK).setElevationLevel(ElevationLevel.NON_ADMIN))),
            null);
        // END: com.azure.compute.batch.create-task.creates-a-task-with-exit-conditions

        // BEGIN: com.azure.compute.batch.resize-pool.resize-pool-error
        try {
            BatchPoolResizeParameters resizeParams
                = new BatchPoolResizeParameters().setTargetDedicatedNodes(1).setTargetLowPriorityNodes(1);
            batchClient.resizePool("fakepool", resizeParams);
        } catch (BatchErrorException err) {
            BatchError error = err.getValue();
            Assertions.assertNotNull(error);
            Assertions.assertEquals("PoolNotFound", error.getCode());
            Assertions.assertTrue(error.getMessage().getValue().contains("The specified pool does not exist."));
            Assertions.assertNull(error.getValues());
        }
        // END: com.azure.compute.batch.resize-pool.resize-pool-error
    }
}
