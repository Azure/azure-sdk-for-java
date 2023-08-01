// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Offer;
import com.azure.cosmos.implementation.OfferAutoscaleSettings;
import com.azure.cosmos.implementation.Resource;

import java.time.Instant;

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
     * Create auto-scale throughput properties.
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
     * Gets offer auto-scale properties.
     *
     * @return the offer autoscale properties
     */
    OfferAutoscaleSettings getOfferAutoscaleProperties() {
        return this.offer.getOfferAutoScaleSettings();
    }

    /**
     * Gets max auto-scale throughput.
     *
     * @return the max auto-scale throughput
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

    Resource getResource() {
        return this.offer;
    }

    /**
     * Gets the name of the resource.
     * This is only relevant when getting response from the server.
     *
     * @return the name of the resource.
     */
    public String getId() {
        return this.offer.getId();
    }

    /**
     * Sets the id
     *
     * @param id the name of the resource.
     * @return the current instance of {@link ThroughputProperties}.
     */
    ThroughputProperties setId(String id) {
        this.offer.setId(id);
        return this;
    }

    /**
     * Gets the ID associated with the resource.
     *
     * @return the ID associated with the resource.
     */
    String getResourceId() {
        return this.offer.getResourceId();
    }

    /**
     * Get the last modified timestamp associated with the resource.
     * This is only relevant when getting response from the server.
     *
     * @return the timestamp.
     */
    public Instant getTimestamp() {
        return this.offer.getTimestamp();
    }

    /**
     * Get the entity tag associated with the resource.
     * This is only relevant when getting response from the server.
     *
     * @return the e tag.
     */
    public String getETag() {
        return this.offer.getETag();
    }
}
