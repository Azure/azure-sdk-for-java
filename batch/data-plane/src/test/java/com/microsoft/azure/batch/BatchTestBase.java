// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.batch;

import com.microsoft.azure.batch.auth.BatchApplicationTokenCredentials;
import com.microsoft.azure.batch.auth.BatchCredentials;
import com.microsoft.azure.batch.auth.BatchSharedKeyCredentials;
import com.microsoft.azure.batch.protocol.models.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.*;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageCredentials;
import com.microsoft.azure.storage.StorageCredentialsAccountAndKey;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.*;
import org.junit.Assert;

/**
 * The base for batch dataplane tests.
 */
abstract class BatchTestBase {
    static BatchClient batchClient;
    static int MAX_LEN_ID = 64;

    public enum AuthMode {
        AAD, SharedKey
    }

    static void createClient(AuthMode mode) {
        BatchCredentials credentials;

        if (mode == AuthMode.AAD) {
            credentials = new BatchApplicationTokenCredentials(System.getenv("AZURE_BATCH_ENDPOINT"),
                    System.getenv("CLIENT_ID"), System.getenv("APPLICATION_SECRET"), "microsoft.onmicrosoft.com", null,
                    null);
        } else {
            credentials = new BatchSharedKeyCredentials(System.getenv("AZURE_BATCH_ENDPOINT"),
                    System.getenv("AZURE_BATCH_ACCOUNT"), System.getenv("AZURE_BATCH_ACCESS_KEY"));
        }
        batchClient = BatchClient.open(credentials);
    }

    static CloudPool createIfNotExistPaaSPool(String poolId) throws Exception {
        // Create a pool with 3 Small VMs
        String POOL_VM_SIZE = "Small";
        int POOL_VM_COUNT = 3;
        String POOL_OS_FAMILY = "4";
        String POOL_OS_VERSION = "*";

        // 5 minutes
        long POOL_STEADY_TIMEOUT_IN_SECONDS = 5 * 60 * 1000;

        // Check if pool exists
        if (!batchClient.poolOperations().existsPool(poolId)) {
            // Use PaaS VM with Windows
            CloudServiceConfiguration configuration = new CloudServiceConfiguration();
            configuration.withOsFamily(POOL_OS_FAMILY).withOsVersion(POOL_OS_VERSION);

            List<UserAccount> userList = new ArrayList<>();
            userList.add(new UserAccount().withName("test-user").withPassword("kt#_gahr!@aGERDXA")
                    .withElevationLevel(ElevationLevel.ADMIN));
            PoolAddParameter addParameter = new PoolAddParameter().withId(poolId)
                    .withTargetDedicatedNodes(POOL_VM_COUNT).withVmSize(POOL_VM_SIZE)
                    .withCloudServiceConfiguration(configuration).withUserAccounts(userList);
            batchClient.poolOperations().createPool(addParameter);
        }

        long startTime = System.currentTimeMillis();
        long elapsedTime = 0L;
        boolean steady = false;
        CloudPool pool;

        // Wait for the VM to be allocated
        while (elapsedTime < POOL_STEADY_TIMEOUT_IN_SECONDS) {
            pool = batchClient.poolOperations().getPool(poolId);
            if (pool.allocationState() == AllocationState.STEADY) {
                steady = true;
                break;
            }
            System.out.println("wait 30 seconds for pool steady...");
            Thread.sleep(30 * 1000);
            elapsedTime = (new Date()).getTime() - startTime;
        }

        Assert.assertTrue("The pool did not reach a steady state in the allotted time", steady);

        return batchClient.poolOperations().getPool(poolId);
    }

    static CloudPool createIfNotExistIaaSPool(String poolId) throws Exception {
        // Create a pool with 3 Small VMs
        String POOL_VM_SIZE = "STANDARD_A1";
        int POOL_VM_COUNT = 1;

        // 5 minutes
        long POOL_STEADY_TIMEOUT_IN_SECONDS = 5 * 60 * 1000;

        // Check if pool exists
        if (!batchClient.poolOperations().existsPool(poolId)) {
            // Use IaaS VM with Ubuntu
            ImageReference imgRef = new ImageReference().withPublisher("Canonical").withOffer("UbuntuServer")
                    .withSku("16.04-LTS").withVersion("latest");
            VirtualMachineConfiguration configuration = new VirtualMachineConfiguration();
            configuration.withNodeAgentSKUId("batch.node.ubuntu 16.04").withImageReference(imgRef);

            List<UserAccount> userList = new ArrayList<>();
            userList.add(new UserAccount().withName("test-user").withPassword("kt#_gahr!@aGERDXA")
                    .withLinuxUserConfiguration(new LinuxUserConfiguration().withUid(5).withGid(5))
                    .withElevationLevel(ElevationLevel.ADMIN));
            PoolAddParameter addParameter = new PoolAddParameter().withId(poolId)
                    .withTargetDedicatedNodes(POOL_VM_COUNT).withVmSize(POOL_VM_SIZE)
                    .withVirtualMachineConfiguration(configuration).withUserAccounts(userList);
            batchClient.poolOperations().createPool(addParameter);
        }

        long startTime = System.currentTimeMillis();
        long elapsedTime = 0L;
        boolean steady = false;
        CloudPool pool;

        // Wait for the VM to be allocated
        while (elapsedTime < POOL_STEADY_TIMEOUT_IN_SECONDS) {
            pool = batchClient.poolOperations().getPool(poolId);
            if (pool.allocationState() == AllocationState.STEADY) {
                steady = true;
                break;
            }
            System.out.println("wait 30 seconds for pool steady...");
            Thread.sleep(30 * 1000);
            elapsedTime = (new Date()).getTime() - startTime;
        }

        Assert.assertTrue("The pool did not reach a steady state in the allotted time", steady);

        return batchClient.poolOperations().getPool(poolId);
    }

    static String getStringIdWithUserNamePrefix(String name) {
        String userName = System.getProperty("user.name");
        StringBuilder out = new StringBuilder();
        int remainingSpace = MAX_LEN_ID - name.length();
        if (remainingSpace > 0){
            if(userName.length() > remainingSpace){
                out.append(userName.substring(0,remainingSpace));
            } else {
                out.append(userName);
            }
            out.append(name);
        } else {
            out.append(name.substring(0, MAX_LEN_ID));
        }
        return out.toString();
    }

    static CloudBlobContainer createBlobContainer(String storageAccountName, String storageAccountKey,
            String containerName) throws URISyntaxException, StorageException {
        // Create storage credential from name and key
        StorageCredentials credentials = new StorageCredentialsAccountAndKey(storageAccountName, storageAccountKey);

        // Create storage account
        CloudStorageAccount storageAccount = new CloudStorageAccount(credentials, true);

        // Create the blob client
        CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

        // Get a reference to a container.
        // The container name must be lower case
        return blobClient.getContainerReference(containerName);
    }

    /**
     * Upload file to blob container and return sas key
     * 
     * @param container
     *            blob container
     * @param fileName
     *            the file name of blob
     * @param filePath
     *            the local file path
     * @return SAS key for the uploaded file
     * @throws URISyntaxException
     * @throws IOException
     * @throws InvalidKeyException
     * @throws StorageException
     */
    static String uploadFileToCloud(CloudBlobContainer container, String fileName, String filePath)
            throws StorageException, URISyntaxException, IOException, InvalidKeyException {
        // Create the container if it does not exist.
        container.createIfNotExists();

        // Upload file
        CloudBlockBlob blob = container.getBlockBlobReference(fileName);
        File source = new File(filePath);
        blob.upload(new FileInputStream(source), source.length());

        // Create policy with 1 day read permission
        SharedAccessBlobPolicy policy = new SharedAccessBlobPolicy();
        EnumSet<SharedAccessBlobPermissions> perEnumSet = EnumSet.of(SharedAccessBlobPermissions.READ);
        policy.setPermissions(perEnumSet);

        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE, 1);
        policy.setSharedAccessExpiryTime(c.getTime());

        // Create SAS key
        String sas = blob.generateSharedAccessSignature(policy, null);
        return blob.getUri() + "?" + sas;
    }

    /**
     * Wait all tasks under a specified job to be completed
     * 
     * @param client
     *            batch client instance
     * @param jobId
     *            job id
     * @param expiryTimeInSeconds
     *            the waiting period
     * @return if task completed in time, return true, otherwise, return false
     * @throws BatchErrorException
     * @throws IOException
     * @throws InterruptedException
     */
    static boolean waitForTasksToComplete(BatchClient client, String jobId, int expiryTimeInSeconds)
            throws BatchErrorException, IOException, InterruptedException {
        long startTime = System.currentTimeMillis();
        long elapsedTime = 0L;

        while (elapsedTime < expiryTimeInSeconds * 1000) {
            List<CloudTask> taskCollection = client.taskOperations().listTasks(jobId,
                    new DetailLevel.Builder().withSelectClause("id, state").build());

            boolean allComplete = true;
            for (CloudTask task : taskCollection) {
                if (task.state() != TaskState.COMPLETED) {
                    allComplete = false;
                    break;
                }
            }

            if (allComplete) {
                // All tasks completed
                return true;
            }

            // Check again after 10 seconds
            Thread.sleep(10 * 1000);
            elapsedTime = (new Date()).getTime() - startTime;
        }

        // Timeout, return false
        return false;
    }

    static String generateContainerSasToken(CloudBlobContainer container) throws StorageException, InvalidKeyException {
        container.createIfNotExists();

        // Create policy with 1 day read permission
        SharedAccessBlobPolicy policy = new SharedAccessBlobPolicy();
        EnumSet<SharedAccessBlobPermissions> perEnumSet = EnumSet.of(SharedAccessBlobPermissions.WRITE,
                SharedAccessBlobPermissions.READ, SharedAccessBlobPermissions.CREATE, SharedAccessBlobPermissions.LIST,
                SharedAccessBlobPermissions.DELETE);
        policy.setPermissions(perEnumSet);

        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE, 1);
        policy.setSharedAccessExpiryTime(c.getTime());

        // Create SAS key
        String sas = container.generateSharedAccessSignature(policy, null);
        return container.getUri() + "?" + sas;
    }

    static String getContentFromContainer(CloudBlobContainer container, String fileName)
            throws URISyntaxException, StorageException, IOException {
        CloudBlockBlob blockBlobReference = container.getBlockBlobReference(fileName);
        String s = blockBlobReference.downloadText();
        return s;
    }

    static String getTestMode(){
        String testMode = System.getenv("AZURE_TEST_MODE");
        testMode = testMode != null ? testMode : "";
        return testMode;
    }
}
