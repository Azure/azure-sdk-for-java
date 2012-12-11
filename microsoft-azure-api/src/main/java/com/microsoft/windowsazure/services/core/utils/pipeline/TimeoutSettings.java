/**
 * Copyright 2012 Microsoft Corporation
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

package com.microsoft.windowsazure.services.core.utils.pipeline;

import com.sun.jersey.api.client.config.ClientConfig;

/**
 * Class used for injecting timeout settings into the various places that need it.
 * 
 */
public class TimeoutSettings {
    private static final int DEFAULT_TIMEOUT_MS = 90 * 1000;

    private final Integer connectTimeout;
    private final Integer readTimeout;

    /**
     * Construct a {@link TimeoutSettings} object with the default
     * timeout.
     */
    public TimeoutSettings() {
        connectTimeout = Integer.valueOf(null);
        readTimeout = Integer.valueOf(null);
    }

    public TimeoutSettings(Object connectTimeout, Object readTimeout) {
        this.connectTimeout = getTimeout(connectTimeout);
        this.readTimeout = getTimeout(readTimeout);
    }

    public void applyTimeout(ClientConfig clientConfig) {
        clientConfig.getProperties().put(ClientConfig.PROPERTY_CONNECT_TIMEOUT, connectTimeout);
        clientConfig.getProperties().put(ClientConfig.PROPERTY_READ_TIMEOUT, readTimeout);
    }

    private Integer getTimeout(Object timeoutValue) {
        if (timeoutValue == null) {
            return new Integer(DEFAULT_TIMEOUT_MS);
        }

        if (timeoutValue instanceof Integer) {
            return (Integer) timeoutValue;
        }

        if (timeoutValue instanceof String) {
            return Integer.valueOf((String) timeoutValue);
        }

        throw new IllegalArgumentException("timeoutValue");
    }
}
