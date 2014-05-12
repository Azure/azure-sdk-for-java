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

/**
 * Represents the metrics properties for the analytics service.
 */
public final class MetricsProperties {
    /**
     * Represents the analytics version to use.
     */
    private String version = "1.0";

    /**
     * A {@link MetricsLevel} level used to enable Metric and API logging
     */
    private MetricsLevel metricsLevel = com.microsoft.azure.storage.MetricsLevel.DISABLED;

    /**
     * Represents the retention policy for the metrics data.
     */
    private Integer retentionIntervalInDays;

    /**
     * Gets the <code>{@link MetricsLevel}</code> for the analytics service.
     * 
     * @return The <code>{@link MetricsLevel}</code>.
     */
    public MetricsLevel getMetricsLevel() {
        return this.metricsLevel;
    }

    /**
     * Gets the metrics retention interval (in days).
     * 
     * @return An <code>Integer</code> which contains the retention interval.
     */
    public Integer getRetentionIntervalInDays() {
        return this.retentionIntervalInDays;
    }

    /**
     * Gets the analytics version.
     * 
     * @return A <code>String</code> which contains the analytics version.
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Sets the <code>{@link MetricsLevel}</code> for the analytics service.
     * 
     * @param metricsLevel
     *        The <code>{@link MetricsLevel}</code> to set.
     */
    public void setMetricsLevel(final MetricsLevel metricsLevel) {
        this.metricsLevel = metricsLevel;
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
     *        A <code>String</code> which contains the analytics version to set.
     */
    public void setVersion(final String version) {
        this.version = version;
    }
}
