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
package com.microsoft.windowsazure.services.media;

import com.microsoft.windowsazure.Configuration;

/**
 * 
 * Access media services functionality. This class cannot be instantiated.
 * 
 */
public final class MediaService {

    private MediaService() {
    }

    /**
     * Creates an instance of the <code>MediaServicesContract</code> API.
     * 
     */
    public static MediaContract create() {
        return Configuration.getInstance().create(MediaContract.class);
    }

    /**
     * Creates an instance of the <code>MediaServicesContract</code> API using
     * the specified configuration.
     * 
     * @param config
     *            A <code>Configuration</code> object that represents the
     *            configuration for the media service account.
     * 
     */
    public static MediaContract create(Configuration config) {
        return config.create(MediaContract.class);
    }

    /**
     * Creates an instance of the <code>MediaServicesContract</code> API.
     * 
     */
    public static MediaContract create(String profile) {
        return Configuration.getInstance().create(profile, MediaContract.class);
    }

    /**
     * Creates an instance of the <code>MediaServicesContract</code> API using
     * the specified configuration.
     * 
     * @param config
     *            A <code>Configuration</code> object that represents the
     *            configuration for the media service account.
     * 
     */
    public static MediaContract create(String profile, Configuration config) {
        return config.create(profile, MediaContract.class);
    }
}
