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
    public static ThroughputProperties createManualThroughput(int throughput) {
        return new ThroughputProperties(Offer.createManualOffer(throughput));
    }

    /**
     * Create auto scale throughput properties.
     *
     * @param autoScaleMaxThroughput the max auto scale throughput
     * @param autoUpgradethroughputIncrementPercentage the auto upgrade max throughput increment percentage
     * @return the throughput properties
     */
    static ThroughputProperties createAutoscaledThroughput(
        int autoScaleMaxThroughput,
        int autoUpgradethroughputIncrementPercentage) {
        return new ThroughputProperties(Offer.createAutoscaleOffer(autoScaleMaxThroughput,
                                                                   autoUpgradethroughputIncrementPercentage));
    }

    /**
     * Create auto scaled provisioned throughput throughput properties.
     *
     * @param autoScaleMaxThroughput the max auto scale throughput
     * @return the throughput properties
     */
    public static ThroughputProperties createAutoscaledThroughput(int autoScaleMaxThroughput) {
        return new ThroughputProperties(Offer.createAutoscaleOffer(autoScaleMaxThroughput,
                                                                   0));
    }

    /**
     * Gets offer throughput.
     *
     * @return the offer throughput
     */
    public Integer getManualThroughput() {
        return offer.getThroughput();
    }

    /**
     * Gets offer autoscale properties.
     *
     * @return the offer autoscale properties
     */
    OfferAutoscaleSettings getOfferAutoscaleProperties() {
        return this.offer.getOfferAutoScaleSettings();
    }

    /**
     * Gets max autoscale throughput.
     *
     * @return the max autoscale throughput
     */
    public int getAutoscaleMaxThroughput() {
        return this.offer.getAutoscaleMaxThroughput();
    }


    Offer getOffer() {
        return this.offer;
    }

    /**
     * Get an updated offer based on the properties.
     */
    Offer updateOfferFromProperties(Offer oldOffer) {
        oldOffer.updateContent(this.offer);
        return oldOffer;
    }

}
