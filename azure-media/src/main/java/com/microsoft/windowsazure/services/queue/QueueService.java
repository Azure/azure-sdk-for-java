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
package com.microsoft.windowsazure.services.queue;

import com.microsoft.windowsazure.Configuration;

/**
 * A class for static factory methods that return instances implementing
 * {@link com.microsoft.windowsazure.services.queue.QueueContract}.
 */
public final class QueueService {
    /**
     * Private default constructor.
     */
    private QueueService() {
    }

    /**
     * A static factory method that returns an instance implementing
     * {@link com.microsoft.windowsazure.services.queue.QueueContract} using default values for initializing a
     * {@link Configuration} instance. Note that the returned interface will not
     * work unless storage account credentials have been added to the
     * "META-INF/com.microsoft.windowsazure.properties" resource file.
     * 
     * @return An instance implementing {@link com.microsoft.windowsazure.services.queue.QueueContract} for interacting
     *         with the queue service.
     */
    public static QueueContract create() {
        return create(null, Configuration.getInstance());
    }

    /**
     * A static factory method that returns an instance implementing
     * {@link com.microsoft.windowsazure.services.queue.QueueContract} using the specified {@link Configuration} instance.
     * The {@link Configuration} instance must have storage account information
     * and credentials set before this method is called for the returned
     * interface to work.
     * 
     * @param config
     *            A {@link Configuration} instance configured with storage
     *            account information and credentials.
     * 
     * @return An instance implementing {@link com.microsoft.windowsazure.services.queue.QueueContract} for interacting
     *         with the queue service.
     */
    public static QueueContract create(Configuration config) {
        return create(null, config);
    }

    /**
     * A static factory method that returns an instance implementing
     * {@link com.microsoft.windowsazure.services.queue.QueueContract} using default values for initializing a
     * {@link Configuration} instance, and using the specified profile prefix
     * for service settings. Note that the returned interface will not work
     * unless storage account settings and credentials have been added to the
     * "META-INF/com.microsoft.windowsazure.properties" resource file with the
     * specified profile prefix.
     * 
     * @param profile
     *            A string prefix for the account name and credentials settings
     *            in the {@link Configuration} instance.
     * @return An instance implementing {@link com.microsoft.windowsazure.services.queue.QueueContract} for interacting
     *         with the queue service.
     */
    public static QueueContract create(String profile) {
        return create(profile, Configuration.getInstance());
    }

    /**
     * A static factory method that returns an instance implementing
     * {@link com.microsoft.windowsazure.services.queue.QueueContract} using the specified {@link Configuration} instance
     * and profile prefix for service settings. The {@link Configuration}
     * instance must have storage account information and credentials set with
     * the specified profile prefix before this method is called for the
     * returned interface to work.
     * 
     * @param profile
     *            A string prefix for the account name and credentials settings
     *            in the {@link Configuration} instance.
     * @param config
     *            A {@link Configuration} instance configured with storage
     *            account information and credentials.
     * 
     * @return An instance implementing {@link com.microsoft.windowsazure.services.queue.QueueContract} for interacting
     *         with the queue service.
     */
    public static QueueContract create(String profile, Configuration config) {
        return config.create(profile, QueueContract.class);
    }
}
