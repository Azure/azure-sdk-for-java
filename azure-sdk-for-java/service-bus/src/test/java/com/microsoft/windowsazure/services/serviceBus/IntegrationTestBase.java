/**
 * Copyright 2011 Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.microsoft.windowsazure.services.serviceBus;

import static com.microsoft.windowsazure.services.serviceBus.Util.*;

import org.junit.Before;
import org.junit.BeforeClass;

import com.microsoft.windowsazure.services.core.Configuration;
import com.microsoft.windowsazure.services.serviceBus.models.QueueInfo;
import com.microsoft.windowsazure.services.serviceBus.models.ReceiveMessageOptions;
import com.microsoft.windowsazure.services.serviceBus.models.TopicInfo;

public abstract class IntegrationTestBase {
    @BeforeClass
    public static void initializeSystem() {
        System.setProperty("http.keepAlive", "false");
    }

    @Before
    public void initialize() throws Exception {

        boolean testAlphaExists = false;
        Configuration config = createConfiguration();
        ServiceBusContract service = ServiceBusService.create(config);
        for (QueueInfo queue : iterateQueues(service)) {
            String queueName = queue.getPath();
            if (queueName.startsWith("Test") || queueName.startsWith("test")) {
                if (queueName.equalsIgnoreCase("TestAlpha")) {
                    testAlphaExists = true;
                    long count = queue.getMessageCount();
                    for (long i = 0; i != count; ++i) {
                        service.receiveQueueMessage(queueName, new ReceiveMessageOptions().setTimeout(20));
                    }
                }
                else {
                    service.deleteQueue(queueName);
                }
            }
        }
        for (TopicInfo topic : iterateTopics(service)) {
            String topicName = topic.getPath();
            if (topicName.startsWith("Test") || topicName.startsWith("test")) {
                service.deleteQueue(topicName);
            }
        }
        if (!testAlphaExists) {
            service.createQueue(new QueueInfo("TestAlpha"));
        }
    }

    protected static Configuration createConfiguration() throws Exception {
        Configuration config = Configuration.load();
        overrideWithEnv(config, ServiceBusConfiguration.URI);
        overrideWithEnv(config, ServiceBusConfiguration.WRAP_URI);
        overrideWithEnv(config, ServiceBusConfiguration.WRAP_NAME);
        overrideWithEnv(config, ServiceBusConfiguration.WRAP_PASSWORD);
        return config;
    }

    private static void overrideWithEnv(Configuration config, String key) {
        String value = System.getenv(key);
        if (value == null)
            return;

        config.setProperty(key, value);
    }
}
