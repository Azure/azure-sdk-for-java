/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.windowsazure.services.blob.client;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.UUID;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.microsoft.windowsazure.services.core.storage.AuthenticationScheme;
import com.microsoft.windowsazure.services.core.storage.CloudStorageAccount;
import com.microsoft.windowsazure.services.core.storage.StorageException;

/**
 * Blob Test Base Blob test refactoring will be done in future.
 */
public class BlobTestBase
{
    public static boolean USE_DEV_FABRIC = false;
    public static final String CLOUD_ACCOUNT_HTTP = "DefaultEndpointsProtocol=http;AccountName=[ACCOUNT NAME];AccountKey=[ACCOUNT KEY]";
    public static final String CLOUD_ACCOUNT_HTTPS = "DefaultEndpointsProtocol=https;AccountName=[ACCOUNT NAME];AccountKey=[ACCOUNT KEY]";

    protected static CloudStorageAccount httpAcc;
    protected static CloudBlobClient bClient;
    protected static String testSuiteContainerName = generateRandomContainerName();
    protected static byte[] testData = new byte[] { 1, 2, 3, 4 };

    @BeforeClass
    public static void setup() throws URISyntaxException, StorageException,
            InvalidKeyException
    {
        if (USE_DEV_FABRIC)
        {
            httpAcc = CloudStorageAccount.getDevelopmentStorageAccount();
        } else
        {
            String cloudAccount = CLOUD_ACCOUNT_HTTP;
            cloudAccount = cloudAccount.replace("[ACCOUNT NAME]",
                    System.getenv("blob.accountName"));
            cloudAccount = cloudAccount.replace("[ACCOUNT KEY]",
                    System.getenv("blob.accountKey"));

            httpAcc = CloudStorageAccount.parse(cloudAccount);
        }

        bClient = httpAcc.createCloudBlobClient();
        bClient.setAuthenticationScheme(AuthenticationScheme.SHAREDKEYFULL);
        testSuiteContainerName = generateRandomContainerName();
        CloudBlobContainer container = bClient
                .getContainerReference(testSuiteContainerName);
        container.create();
    }

    @AfterClass
    public static void teardown() throws StorageException, URISyntaxException
    {
        CloudBlobContainer container = bClient
                .getContainerReference(testSuiteContainerName);
        container.delete();
    }

    protected static String generateRandomContainerName()
    {
        String containerName = "container" + UUID.randomUUID().toString();
        return containerName.replace("-", "");
    }
}
