// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.core.api;

import java.time.Duration;

/**
 * Checkpoint related config.
 *
 * @author Warren Zhu
 */
public class CheckpointConfig {

    private final CheckpointMode checkpointMode;
    /**
     * The count of message to trigger checkpoint. Only used when {@link CheckpointMode#PARTITION_COUNT}
     */
    private final int checkpointCount;

    /**
     * The time interval to trigger checkpoint. Only used when {@link CheckpointMode#TIME}
     */
    private final Duration checkpointInterval;

    /**
     *
     * @param checkpointMode The check point mode.
     * @param checkpointCount The check point count.
     * @param checkpointInterval The check point interval.
     */
    public CheckpointConfig(CheckpointMode checkpointMode, int checkpointCount, Duration checkpointInterval) {
        this.checkpointMode = checkpointMode;
        this.checkpointCount = checkpointCount;
        this.checkpointInterval = checkpointInterval;
    }

    /**
     *
     * @return The CheckpointConfigBuilder.
     */
    public static CheckpointConfigBuilder builder() {
        return new CheckpointConfigBuilder();
    }

    /**
     *
     * @return The CheckpointMode.
     */
    public CheckpointMode getCheckpointMode() {
        return checkpointMode;
    }

    /**
     *
     * @return The check point count.
     */
    public int getCheckpointCount() {
        return checkpointCount;
    }

    /**
     *
     * @return The check point interval.
     */
    public Duration getCheckpointInterval() {
        return checkpointInterval;
    }

    /**
     *
     * @return The string.
     */
    @Override
    public String toString() {
        return "CheckpointConfig{" + "checkpointMode=" + checkpointMode
            + ", checkpointCount=" + checkpointCount
            + ", checkpointInterval=" + checkpointInterval + '}';
    }

    /**
     * Builder class for {@link CheckpointConfig}.
     */
    public static class CheckpointConfigBuilder {
        private CheckpointMode checkpointMode;
        private int checkpointCount;
        private Duration checkpointInterval;

        /**
         *
         * @param checkpointMode The check point mode.
         * @return The CheckpointConfigBuilder.
         */
        public CheckpointConfigBuilder checkpointMode(CheckpointMode checkpointMode) {
            this.checkpointMode = checkpointMode;
            return this;
        }

        /**
         *
         * @param checkpointCount The checkpoint count.
         * @return The CheckpointConfigBuilder.
         */
        public CheckpointConfigBuilder checkpointCount(int checkpointCount) {
            this.checkpointCount = checkpointCount;
            return this;
        }

        /**
         *
         * @param checkpointInterval The check point interval.
         * @return The CheckpointConfigBuilder.
         */
        public CheckpointConfigBuilder checkpointInterval(Duration checkpointInterval) {
            this.checkpointInterval = checkpointInterval;
            return this;
        }

        /**
         *
         * @return The CheckpointConfig.
         */
        public CheckpointConfig build() {
            return new CheckpointConfig(checkpointMode, checkpointCount, checkpointInterval);
        }
    }

}
