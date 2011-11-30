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
