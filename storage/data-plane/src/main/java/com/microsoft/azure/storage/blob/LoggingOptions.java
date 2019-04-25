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
 * Options to configure the {@link LoggingFactory}. Please refer to the Factory for more information.
 */
public final class LoggingOptions {

    /**
     * Default logging options. {@code MinDurationToLogSlowRequestsInMs} is set to 3000;
     */
    public static final long defaultMinDurationToLogSlowRequests = 3000;

    private final long minDurationToLogSlowRequestsInMs;

    private final boolean disableDefaultLogging;

    public LoggingOptions() {
        this(defaultMinDurationToLogSlowRequests);
    }


    /**
     * Creates a new {@link LoggingOptions} object.
     *
     * @param minDurationToLogSlowRequestsInMs
     *         The duration after which a tried operation will be logged as a warning.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=pipeline_options "Sample code for LoggingOptions constructor")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public LoggingOptions(long minDurationToLogSlowRequestsInMs) {
        this(minDurationToLogSlowRequestsInMs, false);
    }

    /**
     * Creates a new {@link LoggingOptions} object.
     *
     * @param minDurationToLogSlowRequestsInMs
     *         The duration after which a tried operation will be logged as a warning.
     * @param disableDefaultLogging
     *         By default, this library will automatically log warnings and errors to some files in the system's temp
     *         directory. The size of these files is bounded to a few dozen MB and should not impose a burden on the
     *         system. It is strongly recommended to leave these logs enabled for customer support reasons, but if
     *         the user desires a different logging story and enables logging via the HttpPipelineLogger or SLF4J, then
     *         it should be safe to disable default logging.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=pipeline_options "Sample code for LoggingOptions constructor")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public LoggingOptions(long minDurationToLogSlowRequestsInMs, boolean disableDefaultLogging) {
        this.minDurationToLogSlowRequestsInMs = minDurationToLogSlowRequestsInMs;
        this.disableDefaultLogging = disableDefaultLogging;
    }

    /**
     * @return The duration after which a tried operation will be logged as a warning.
     */
    public long minDurationToLogSlowRequestsInMs() {
        return minDurationToLogSlowRequestsInMs;
    }

    public boolean disableDefaultLogging() {
        return disableDefaultLogging;
    }
}
