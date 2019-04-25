/*
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
package com.microsoft.azure.storage.blob;

/**
 * Options for configuring the {@link TelemetryFactory}. Please refer to the Factory for more information.
 */
public final class TelemetryOptions {

    private final String userAgentPrefix;

    public TelemetryOptions() {
        this(Constants.EMPTY_STRING);
    }

    /**
     * @param userAgentPrefix
     *         A string prepended to each request's User-Agent and sent to the service. The service records.
     *         the user-agent in logs for diagnostics and tracking of client requests.
     */
    public TelemetryOptions(String userAgentPrefix) {
        this.userAgentPrefix = userAgentPrefix;
    }

    /**
     * @return The user agent prefix.
     */
    public String userAgentPrefix() {
        return this.userAgentPrefix;
    }
}
