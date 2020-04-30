// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Offer;
import com.azure.cosmos.implementation.OfferAutoscaleSettings;

/**
 * Represents throughput of the resources in the Azure Cosmos DB service.
 */
public class ThroughputProperties {
    private final Offer offer;

    ThroughputProperties(Offer offer) {
        this.offer = offer;
    }

    /**
     * Create fixed throughput properties.
     *
     * @param throughput the throughput
     * @return the throughput properties
     */
    public static ThroughputProperties createFixedThroughput(int throughput) {
        return new ThroughputProperties(Offer.createFixedOffer(throughput));
    }

    /**
     * Create auto scale throughput properties.
     *
     * @param maxAutoScaleThroughput the max auto scale throughput
     * @param autoUpgradethroughputIncrementPercentage the auto upgrade max throughput increment percentage
     * @return the throughput properties
     */
    public static ThroughputProperties createAutoScaledProvisionedThroughput(
        int maxAutoScaleThroughput,
        int autoUpgradethroughputIncrementPercentage) {
        return new ThroughputProperties(Offer.createAutoscaleOffer(maxAutoScaleThroughput,
                                                                   autoUpgradethroughputIncrementPercentage));
    }

    /**
     * Create auto scaled provisioned throughput throughput properties.
     *
     * @param maxAutoScaleThroughput the max auto scale throughput
     * @return the throughput properties
     */
    public static ThroughputProperties createAutoScaledProvisionedThroughput(int maxAutoScaleThroughput) {
        return new ThroughputProperties(Offer.createAutoscaleOffer(maxAutoScaleThroughput,
                                                                   0));
    }

    /**
     * Gets offer throughput.
     *
     * @return the offer throughput
     */
    public Integer getOfferThroughput() {
        return offer.getThroughput();
    }

    /**
     * Gets offer autoscale properties.
     *
     * @return the offer autoscale properties
     */
    public OfferAutoscaleSettings getOfferAutoscaleProperties() {
        return this.offer.getOfferAutoScaleSettings();
    }

    /**
     * Gets max autoscale throughput.
     *
     * @return the max autoscale throughput
     */
    public int getMaxAutoscaleThroughput() {
        return this.offer.getMaxAutoscaleThroughput();
    }


    Offer getOffer() {
        return this.offer;
    }

    /**
     * Get an updated offer based on the properties.
     */
    Offer updateOfferFromProperties(Offer offer) {
        /*
        if autoscale is set then update autoscale values
        else update fixedthroughput
         */

        if (this.getMaxAutoscaleThroughput() > 0) {
            offer.updateAutoscaleThroughput(this.getMaxAutoscaleThroughput());
        }
        return offer;
    }

}
