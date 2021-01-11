// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class OfferAutoscaleSettings extends JsonSerializable {
    OfferAutoscaleAutoUpgradeProperties offerAutoscaleAutoUpgradeProperties;

    OfferAutoscaleSettings(final int maxThroughput, int maxThroughputIncrementPercentage) {
        super();
        offerAutoscaleAutoUpgradeProperties = new OfferAutoscaleAutoUpgradeProperties(maxThroughputIncrementPercentage);
        super.set(Constants.Properties.AUTOPILOT_MAX_THROUGHPUT, maxThroughput);
        super.set(Constants.Properties.AUTOPILOT_AUTO_UPGRADE_POLICY, offerAutoscaleAutoUpgradeProperties);
    }

    OfferAutoscaleSettings(String json) {
        super(json);
    }

    OfferAutoscaleSettings(ObjectNode objectNode) {
        super(objectNode);
    }

    /**
     * Getter for property 'maxThroughput'.
     *
     * @return Value for property 'maxThroughput'.
     */
    int getMaxThroughput() {
        return this.getInt(Constants.Properties.AUTOPILOT_MAX_THROUGHPUT);
    }

    void setMaxThroughput(int maxAutoscaleThroughput) {
        super.set(Constants.Properties.AUTOPILOT_MAX_THROUGHPUT, maxAutoscaleThroughput);
    }

    /**
     * Getter for property 'autoscaleAutoUpgradeProperties'.
     *
     * @return Value for property 'autoscaleAutoUpgradeProperties'.
     */
    OfferAutoscaleAutoUpgradeProperties getAutoscaleAutoUpgradeProperties() {
        if (offerAutoscaleAutoUpgradeProperties == null) {
            if (this.has(Constants.Properties.AUTOPILOT_AUTO_UPGRADE_POLICY))
                offerAutoscaleAutoUpgradeProperties =
                    new OfferAutoscaleAutoUpgradeProperties((ObjectNode) this.get(Constants.Properties
                                                                                      .AUTOPILOT_AUTO_UPGRADE_POLICY));
        }
        return offerAutoscaleAutoUpgradeProperties;
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
