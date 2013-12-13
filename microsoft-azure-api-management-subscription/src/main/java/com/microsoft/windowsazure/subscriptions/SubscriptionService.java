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
package com.microsoft.windowsazure.subscriptions;

import com.microsoft.windowsazure.services.core.Configuration;

/**
 * 
 * Access service management functionality.
 * 
 */
public class SubscriptionService {

    private SubscriptionService() {
        // class is not instantiated
    }

    /**
     * Creates an instance of the <code>SubscriptionClient</code> API.
     * 
     */
    public static SubscriptionClient create() {
        return Configuration.getInstance().create(SubscriptionClient.class);
    }

    /**
     * Creates an instance of the <code>SubscriptionClient</code> API using the specified configuration.
     * 
     * @param config
     *            A <code>Configuration</code> object that represents the configuration for the service management.
     * 
     */
    public static SubscriptionClient create(Configuration config) {
        return config.create(SubscriptionClient.class);
    }

    /**
     * Creates an instance of the <code>SubscriptionClient</code> API.
     * 
     * @param profile
     *            A <code>String</code> object that representing the profile of the service management service.
     * 
     */
    public static SubscriptionClient create(String profile) {
        return Configuration.getInstance().create(profile, SubscriptionClient.class);
    }

    /**
     * Creates an instance of the <code>SubscriptionClient</code> API using the specified configuration.
     * 
     * @param config
     *            A <code>Configuration</code> object that represents the configuration for the service management.
     * 
     */
    public static SubscriptionClient create(String profile, Configuration config) {
        return config.create(profile, SubscriptionClient.class);
    }
}