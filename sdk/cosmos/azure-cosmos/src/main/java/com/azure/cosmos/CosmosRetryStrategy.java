package com.azure.cosmos;

import com.azure.cosmos.implementation.OperationKind;
import com.azure.cosmos.implementation.RetryStrategyConfiguration;
import com.azure.cosmos.models.CosmosItemOperationType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class CosmosRetryStrategy {

    private final Map<OperationType, RetryStrategyConfiguration> retryStrategies;

    CosmosRetryStrategy(Map<OperationType, RetryStrategyConfiguration> retryStrategies) {
        this.retryStrategies = retryStrategies;
    }

    public static final class RegionSwitchHint {

        private final String hintRepresentation;

        private RegionSwitchHint(String hintRepresentation) {
            this.hintRepresentation = hintRepresentation;
        }

        public static final RegionSwitchHint NONE =
                new RegionSwitchHint("none");
        public static final RegionSwitchHint LOCAL_REGION_PREFERRED =
                new RegionSwitchHint("localRegionPreferred");
        public static final RegionSwitchHint REMOTE_REGION_PREFERRED =
                new RegionSwitchHint("remoteRegionPreferred");
    }

    public static final class OperationType {

        private final String operationTypeRepresentation;

        private OperationType(String operationTypeRepresentation) {
            this.operationTypeRepresentation = operationTypeRepresentation;
        }

        public static final OperationType WRITE = new OperationType("write");
        public static final OperationType READ = new OperationType("read");

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
