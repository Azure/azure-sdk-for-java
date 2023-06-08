package com.azure.cosmos;

import com.azure.cosmos.implementation.RetryStrategyConfiguration;

public final class CosmosRetryStrategy {

    private final RetryStrategyConfiguration retryStrategyConfiguration;

    CosmosRetryStrategy(RetryStrategyConfiguration retryStrategyConfiguration) {
        this.retryStrategyConfiguration = retryStrategyConfiguration;
    }

    public RetryStrategyConfiguration getRetryStrategyConfiguration() {
        return retryStrategyConfiguration;
    }

    public static final class RegionSwitchHint {

        private final String hintRepresentation;

        public static final RegionSwitchHint NONE =
                new RegionSwitchHint("none");
        public static final RegionSwitchHint LOCAL_REGION_PREFERRED =
                new RegionSwitchHint("localRegionPreferred");
        public static final RegionSwitchHint REMOTE_REGION_PREFERRED =
                new RegionSwitchHint("remoteRegionPreferred");

        private RegionSwitchHint(String hintRepresentation) {
            this.hintRepresentation = hintRepresentation;
        }

        public String getHintRepresentation() {
            return hintRepresentation;
        }
    }
}
