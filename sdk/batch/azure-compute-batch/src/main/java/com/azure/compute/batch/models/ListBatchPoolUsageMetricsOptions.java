package com.azure.compute.batch.models;

import java.time.OffsetDateTime;

public class ListBatchPoolUsageMetricsOptions extends BatchListOptions{
    private OffsetDateTime endtime;
    private OffsetDateTime starttime;

    /**
     * Gets the latest time from which to include metrics.
     *
     * <p>This property represents the end time for including metrics in an operation.
     * It must be at least two hours before the current time.
     * If not specified, it defaults to the end time of the last aggregation interval currently available.
     *
     * @return The latest time from which to include metrics.
     */
    public OffsetDateTime getEndTime() {
        return endtime;
    }

    /**
     * Sets the latest time from which to include metrics.
     *
     * <p>This property represents the end time for including metrics in an operation.
     * It must be at least two hours before the current time.
     * If not specified, it defaults to the end time of the last aggregation interval currently available.
     *
     * @param endtime The latest time from which to include metrics.
     */
    public void setEndTime(OffsetDateTime endtime) {
        this.endtime = endtime;
    }

    /**
     * Gets the earliest time from which to include metrics.
     *
     * This property represents the start time for including metrics in an operation.
     * It must be at least two and a half hours before the current time.
     * If not specified, it defaults to the start time of the last aggregation interval currently available.
     *
     * @return The earliest time from which to include metrics.
     */
    public OffsetDateTime getStartTime() {
        return starttime;
    }

    /**
     * Sets the earliest time from which to include metrics.
     *
     * This property represents the start time for including metrics in an operation.
     * It must be at least two and a half hours before the current time.
     * If not specified, it defaults to the start time of the last aggregation interval currently available.
     *
     * @param starttime The earliest time from which to include metrics.
     */
    public void setStartTime(OffsetDateTime starttime) {
        this.starttime = starttime;
    }

}
