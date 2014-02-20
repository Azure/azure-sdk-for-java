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
package com.microsoft.windowsazure.storage.queue;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.UUID;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.microsoft.windowsazure.storage.AuthenticationScheme;
import com.microsoft.windowsazure.storage.StorageException;
import com.microsoft.windowsazure.storage.TestBase;

/**
 * Queue Test Base
 */
public class QueueTestBase extends TestBase {

    protected static CloudQueueClient qClient;
    protected static String testSuiteQueueName = generateRandomQueueName();
    protected static CloudQueue queue;

    @BeforeClass
    public static void setupQueueTestBase() throws URISyntaxException, StorageException, InvalidKeyException {
        qClient = createCloudQueueClient();
        qClient.setAuthenticationScheme(AuthenticationScheme.SHAREDKEYFULL);
        testSuiteQueueName = generateRandomQueueName();
        queue = qClient.getQueueReference(testSuiteQueueName);
        queue.create();
    }

    @AfterClass
    public static void teardown() throws StorageException, URISyntaxException {
        CloudQueue queue = qClient.getQueueReference(testSuiteQueueName);
        queue.delete();
    }

    public static String generateRandomQueueName() {
        String queueName = "queue" + UUID.randomUUID().toString();
        return queueName.replace("-", "");
    }

    static String AppendQueueName(URI baseURI, String queueName) throws URISyntaxException {
        if (baseURI == null)
            return queueName;

        String baseAddress = baseURI.toString();
        if (baseAddress.endsWith("/")) {
            return baseAddress + queueName;
        }
        else {
            return baseAddress + "/" + queueName;
        }
    }
}
