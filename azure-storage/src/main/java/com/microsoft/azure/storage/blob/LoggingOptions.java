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

import java.util.logging.Level;
// TODO: Do we want to be using the HttpPipelineLoggingLevel here instead of that java.util?

/**
 * Options to configure the {@link LoggingFactory}.
 */
public final class LoggingOptions {

    /**
     * An object representing default logging options. {@code MinDurationToLogSlowRequestsInMs} is set to 3000;
     */
    public static final LoggingOptions DEFAULT = new LoggingOptions(3000);

    private final long minDurationToLogSlowRequestsInMs;



    /**
     * Creates a new {@link LoggingOptions} object.
     *
     * @param minDurationToLogSlowRequestsInMs
     *      A {@code long} representing the minimum duration for a tried operation to log a warning.
     */
    public LoggingOptions(long minDurationToLogSlowRequestsInMs) {
        this.minDurationToLogSlowRequestsInMs = minDurationToLogSlowRequestsInMs;
    }

    /**
     * @return
     *      A {@code long} representing the minimum duration for a tried operation to log a warning.
     */
    public long getMinDurationToLogSlowRequestsInMs() {
        return minDurationToLogSlowRequestsInMs;
    }


}
