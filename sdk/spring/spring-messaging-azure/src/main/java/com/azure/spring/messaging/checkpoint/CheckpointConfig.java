// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.checkpoint;

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

    public CheckpointConfig(CheckpointMode checkpointMode, int checkpointCount, Duration checkpointInterval) {
        this.checkpointMode = checkpointMode;
        this.checkpointCount = checkpointCount;
        this.checkpointInterval = checkpointInterval;
    }

    public static CheckpointConfigBuilder builder() {
        return new CheckpointConfigBuilder();
    }

    public CheckpointMode getCheckpointMode() {
        return checkpointMode;
    }

    public int getCheckpointCount() {
        return checkpointCount;
    }

    public Duration getCheckpointInterval() {
        return checkpointInterval;
    }

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

        public CheckpointConfigBuilder checkpointMode(CheckpointMode checkpointMode) {
            this.checkpointMode = checkpointMode;
            return this;
        }

        public CheckpointConfigBuilder checkpointCount(int checkpointCount) {
            this.checkpointCount = checkpointCount;
            return this;
        }

        public CheckpointConfigBuilder checkpointInterval(Duration checkpointInterval) {
            this.checkpointInterval = checkpointInterval;
            return this;
        }

        public CheckpointConfig build() {
            return new CheckpointConfig(checkpointMode, checkpointCount, checkpointInterval);
        }
    }

}
