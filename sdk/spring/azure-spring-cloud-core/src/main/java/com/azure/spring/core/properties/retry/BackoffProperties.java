package com.azure.spring.core.properties.retry;

public class BackoffProperties {

    private long delay;
    private long maxDelay;
    /**
     * If positive, then used as a multiplier for generating the next delay for backoff.
     * @return a multiplier to use to calculate the next backoff delay (default 0 =
     * ignored)
     */
    private double multiplier;

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public long getMaxDelay() {
        return maxDelay;
    }

    public void setMaxDelay(long maxDelay) {
        this.maxDelay = maxDelay;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }
}