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
package com.microsoft.azure.storage.queue;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.TestHelper;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

/**
 * Queue Test Base
 */
public class QueueTestHelper extends TestHelper {

    public static String generateRandomQueueName() {
        String queueName = "queue" + UUID.randomUUID().toString();
        return queueName.replace("-", "");
    }

    public static CloudQueue getRandomQueueReference() throws URISyntaxException, StorageException {
        String queueName = generateRandomQueueName();
        CloudQueueClient qClient = createCloudQueueClient();
        CloudQueue queue = qClient.getQueueReference(queueName);

        return queue;
    }

    static String appendQueueName(URI baseURI, String queueName) {
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
