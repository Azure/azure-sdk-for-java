/**
 * Copyright 2011 Microsoft Corporation
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
package com.microsoft.windowsazure.services.media;

import com.microsoft.windowsazure.services.core.Configuration;

/**
 * 
 * Access media services functionality.
 * 
 */
public class MediaServicesService {

    private MediaServicesService() {
        // class is not instantiated
    }

    /**
     * Creates an instance of the <code>MediaServicesContract</code> API.
     * 
     */
    public static MediaServicesContract create() {
        return Configuration.getInstance().create(MediaServicesContract.class);
    }

    /**
     * Creates an instance of the <code>MediaServicesContract</code> API using the specified configuration.
     * 
     * @param config
     *            A <code>Configuration</code> object that represents the configuration for the service bus service.
     * 
     */
    public static MediaServicesContract create(Configuration config) {
        return config.create(MediaServicesContract.class);
    }

    /**
     * Creates an instance of the <code>MediaServicesContract</code> API.
     * 
     */
    public static MediaServicesContract create(String profile) {
        return Configuration.getInstance().create(profile, MediaServicesContract.class);
    }

    /**
     * Creates an instance of the <code>MediaServicesContract</code> API using the specified configuration.
     * 
     * @param config
     *            A <code>Configuration</code> object that represents the configuration for the service bus service.
     * 
     */
    public static MediaServicesContract create(String profile, Configuration config) {
        return config.create(profile, MediaServicesContract.class);
    }
}
