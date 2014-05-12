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
package com.microsoft.azure.storage;

import java.util.EnumSet;

/**
 * Represents the logging properties for the analytics service.
 */
public final class LoggingProperties {

    /**
     * The analytics version to use.
     */
    private String version = "1.0";

    /**
     * An <code>EnumSet<code> of <code>LoggingOperations</code> that represents which storage operations should be logged.
     */
    private EnumSet<LoggingOperations> logOperationTypes = EnumSet.noneOf(LoggingOperations.class);

    /**
     * Represents the retention policy for the logging data.
     */
    private Integer retentionIntervalInDays;

    /**
     * Gets an <code>EnumSet<code> of <code>{@link LoggingOperations}</code> that represents which storage operations should be logged.
     * 
     * @return An <code>EnumSet<code> of <code>{@link LoggingOperations}</code>.
     */
    public EnumSet<LoggingOperations> getLogOperationTypes() {
        return this.logOperationTypes;
    }

    /**
     * Gets the retention interval (in days).
     * 
     * @return An <code>Integer</code> which contains the retention interval.
     */
    public Integer getRetentionIntervalInDays() {
        return this.retentionIntervalInDays;
    }

    /**
     * Gets the analytics version.
     * 
     * @return A <code>String</code> which contains the version.
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Sets the <code>{@link LoggingOperations}</code> for which storage operations should be logged.
     * 
     * @param logOperationTypes
     *        An <code>EnumSet<code> of <code>{@link LoggingOperations}</code> to set.
     */
    public void setLogOperationTypes(final EnumSet<LoggingOperations> logOperationTypes) {
        this.logOperationTypes = logOperationTypes;
    }

    /**
     * Sets the retention interval (in days).
     * 
     * @param retentionIntervalInDays
     *        An <code>Integer</code> which contains the retention interval to set.
     */
    public void setRetentionIntervalInDays(final Integer retentionIntervalInDays) {
        this.retentionIntervalInDays = retentionIntervalInDays;
    }

    /**
     * Sets the analytics version.
     * 
     * @param version
     *        A <code>String</code> which contains the version to set.
     */
    public void setVersion(final String version) {
        this.version = version;
    }
}
