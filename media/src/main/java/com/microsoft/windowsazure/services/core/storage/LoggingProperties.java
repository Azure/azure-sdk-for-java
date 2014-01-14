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
package com.microsoft.windowsazure.services.core.storage;

import java.util.EnumSet;

/**
 * Represents the logging properties for the analytics service.
 */
public final class LoggingProperties
{

    /**
     * The analytics version to use.
     */
    private String version = "1.0";

    /**
     * A EnumSet of <code>LoggingOperationTypes</code> that represent which
     * storage operations should be logged.
     */
    private EnumSet<LoggingOperations> logOperationTypes = EnumSet
            .noneOf(LoggingOperations.class);

    /**
     * The Retention policy for the logging data.
     */
    private Integer retentionIntervalInDays;

    /**
     * @return the logOperationTypes
     */
    public EnumSet<LoggingOperations> getLogOperationTypes()
    {
        return this.logOperationTypes;
    }

    /**
     * @return the retentionIntervalInDays
     */
    public Integer getRetentionIntervalInDays()
    {
        return this.retentionIntervalInDays;
    }

    /**
     * @return the version
     */
    public String getVersion()
    {
        return this.version;
    }

    /**
     * @param logOperationTypes
     *            the logOperationTypes to set
     */
    public void setLogOperationTypes(
            final EnumSet<LoggingOperations> logOperationTypes)
    {
        this.logOperationTypes = logOperationTypes;
    }

    /**
     * @param retentionIntervalInDays
     *            the retentionIntervalInDays to set
     */
    public void setRetentionIntervalInDays(final Integer retentionIntervalInDays)
    {
        this.retentionIntervalInDays = retentionIntervalInDays;
    }

    /**
     * @param version
     *            the version to set
     */
    public void setVersion(final String version)
    {
        this.version = version;
    }
}
