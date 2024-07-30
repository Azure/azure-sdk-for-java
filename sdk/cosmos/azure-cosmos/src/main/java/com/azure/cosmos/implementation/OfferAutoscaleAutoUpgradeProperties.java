// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosItemSerializer;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class OfferAutoscaleAutoUpgradeProperties extends JsonSerializable {

    private AutoscaleThroughputProperties autoscaleThroughputProperties;

    OfferAutoscaleAutoUpgradeProperties(int maxThroughputIncrementPercentage) {
        this.autoscaleThroughputProperties = new AutoscaleThroughputProperties(maxThroughputIncrementPercentage);
        super.set(
            Constants.Properties.AUTOPILOT_AUTO_THROUGHPUT_POLICY,
            autoscaleThroughputProperties,
            CosmosItemSerializer.DEFAULT_SERIALIZER);
    }

    OfferAutoscaleAutoUpgradeProperties(ObjectNode objectNode) {
        super(objectNode);
    }

    AutoscaleThroughputProperties getAutoscaleThroughputProperties() {
        return autoscaleThroughputProperties;
    }

    public static class AutoscaleThroughputProperties extends JsonSerializable {

        AutoscaleThroughputProperties(int maxThroughputIncrementPercentage) {
            super.set(
                Constants.Properties.AUTOPILOT_THROUGHPUT_POLICY_INCREMENT_PERCENT,
                maxThroughputIncrementPercentage,
                CosmosItemSerializer.DEFAULT_SERIALIZER);
        }

        /**
         * Getter for property 'incrementPercent'.
         *
         * @return Value for property 'incrementPercent'.
         */
        int getIncrementPercent() {
            return this.getInt(Constants.Properties.AUTOPILOT_THROUGHPUT_POLICY_INCREMENT_PERCENT);
        }
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
