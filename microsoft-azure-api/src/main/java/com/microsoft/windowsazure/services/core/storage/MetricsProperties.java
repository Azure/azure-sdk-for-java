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

/**
 * Represents the metrics properties for the analytics service.
 */
public final class MetricsProperties {
    /**
     * The analytics version to use.
     */
    private String version = "1.0";

    /**
     * A {@link MetricsLevel} level used to enable Metric and API logging
     */
    private MetricsLevel metricsLevel = com.microsoft.windowsazure.services.core.storage.MetricsLevel.DISABLED;

    /**
     * The Retention policy for the Metrics data.
     */
    private Integer retentionIntervalInDays;

    /**
     * @return the metricsLevel
     */
    public MetricsLevel getMetricsLevel() {
        return this.metricsLevel;
    }

    /**
     * @return the retentionIntervalInDays
     */
    public Integer getRetentionIntervalInDays() {
        return this.retentionIntervalInDays;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * @param metricsLevel
     *            the metricsLevel to set
     */
    public void setMetricsLevel(final MetricsLevel metricsLevel) {
        this.metricsLevel = metricsLevel;
    }

    /**
     * @param retentionIntervalInDays
     *            the retentionIntervalInDays to set
     */
    public void setRetentionIntervalInDays(final Integer retentionIntervalInDays) {
        this.retentionIntervalInDays = retentionIntervalInDays;
    }

    /**
     * @param version
     *            the version to set
     */
    public void setVersion(final String version) {
        this.version = version;
    }
}
