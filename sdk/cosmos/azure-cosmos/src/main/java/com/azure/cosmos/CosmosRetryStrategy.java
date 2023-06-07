package com.azure.cosmos;

import com.azure.cosmos.implementation.RetryStrategyConfiguration;

import java.util.Map;
import java.util.Objects;

public final class CosmosRetryStrategy {

    private final Map<OperationType, RetryStrategyConfiguration> retryStrategyConfigs;

    CosmosRetryStrategy(Map<OperationType, RetryStrategyConfiguration> retryStrategyConfigs) {
        this.retryStrategyConfigs = retryStrategyConfigs;
    }

    public Map<OperationType, RetryStrategyConfiguration> getRetryStrategyConfigs() {
        return retryStrategyConfigs;
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

    public static final class OperationType {

        private final String operationTypeRepresentation;

        private OperationType(String operationTypeRepresentation) {
            this.operationTypeRepresentation = operationTypeRepresentation;
        }

        public static final OperationType WRITE = new OperationType("write");
        public static final OperationType READ = new OperationType("read");

        public String getOperationTypeRepresentation() {
            return operationTypeRepresentation;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            OperationType that = (OperationType) o;
            return operationTypeRepresentation.equals(that.operationTypeRepresentation);
        }

        @Override
        public int hashCode() {
            return Objects.hash(operationTypeRepresentation);
        }
    }
}
