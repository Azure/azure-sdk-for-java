// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Represents an offer in the Azure Cosmos DB database service.
 */
public class Offer extends Resource {

    /**
     * Initialize an new instance of the Offer object.
     *
     * @param offerThroughput the throughput value for this offer.
     */
    public Offer(int offerThroughput) {
        super();
        this.setOfferVersion(Constants.Properties.OFFER_VERSION_V2);
        this.setOfferType("");
        ObjectNode content = Utils.getSimpleObjectMapper().createObjectNode();
        content.put(Constants.Properties.OFFER_THROUGHPUT, offerThroughput);
        this.setContent(content);
    }

    Offer(OfferAutoscaleSettings offerAutoscaleSettings) {
        super();
        this.setOfferVersion(Constants.Properties.OFFER_VERSION_V2);
        this.setOfferType("");
        ObjectNode content = Utils.getSimpleObjectMapper().createObjectNode();
//        content.put(Constants.Properties.OFFER_THROUGHPUT, null);
        content.replace(Constants.Properties.AUTOPILOT_SETTINGS, ModelBridgeInternal
                                                                     .getPropertyBagFromJsonSerializable(offerAutoscaleSettings));
        this.setContent(content);
    }


    /**
     * Initialize an offer object from json string.
     *
     * @param jsonString the json string that represents the offer.
     */
    public Offer(String jsonString) {
        super(jsonString);
    }

    /**
     * Instantiates a new Offer from object node.
     *
     * @param objectNode the object node
     */
    public Offer(ObjectNode objectNode) {
        super(objectNode);
    }

    /**
     * Create fixed offer.
     *
     * @param throughput the throughput
     * @return the offer
     */
    public static Offer createManualOffer(int throughput) {
        return new Offer(throughput);
    }

    /**
     * Create autoscale offer.
     *
     * @param startingMaxThroughput the starting max throughput
     * @param autoUpgradeMaxThroughputIncrementPercentage the auto upgrade max throughput increment percentage
     * @return the offer
     */
    public static Offer createAutoscaleOffer(
        int startingMaxThroughput,
        int autoUpgradeMaxThroughputIncrementPercentage) {
        return new Offer(new OfferAutoscaleSettings(startingMaxThroughput,
                                                    autoUpgradeMaxThroughputIncrementPercentage));
    }

    /**
     * Gets offer auto scale settings.
     *
     * @return the offer auto scale settings
     */
    public OfferAutoscaleSettings getOfferAutoScaleSettings() {
        if (this.getContent().hasNonNull(Constants.Properties.AUTOPILOT_SETTINGS)) {
            return new OfferAutoscaleSettings((ObjectNode) this.getContent()
                                                               .get(Constants.Properties.AUTOPILOT_SETTINGS));
        } else {
            return null;
        }
    }

    /**
     * Gets max autoscale throughput.
     *
     * @return the max autoscale throughput
     */
    public int getAutoscaleMaxThroughput() {
        OfferAutoscaleSettings offerAutoscaleSettings = this.getOfferAutoScaleSettings();
        if (offerAutoscaleSettings != null) {
            return offerAutoscaleSettings.getMaxThroughput();
        } else {
            return 0;
        }
    }

    /**
     * Sets max autoscale throughput.
     *
     * @param autoscaleMaxThroughput the max autoscale throughput
     */
    public void setAutoscaleMaxThroughput(int autoscaleMaxThroughput) {
        OfferAutoscaleSettings offerAutoscaleSettings = this.getOfferAutoScaleSettings();
        if (offerAutoscaleSettings != null) {
            offerAutoscaleSettings.setMaxThroughput(autoscaleMaxThroughput);
        }
    }

    /**
     * Gets the self-link of a resource to which the resource offer applies.
     *
     * @return the resource link.
     */
    public String getResourceLink() {
        return super.getString(Constants.Properties.RESOURCE_LINK);
    }

    /**
     * Sets the self-link of a resource to which the resource offer applies.
     *
     * @param resourceLink the resource link.
     */
    void setResourceLink(String resourceLink) {
        this.set(Constants.Properties.RESOURCE_LINK, resourceLink);
    }

    /**
     * Sets the target resource id of a resource to which this offer applies.
     *
     * @return the resource id.
     */
    public String getOfferResourceId() {
        return super.getString(Constants.Properties.OFFER_RESOURCE_ID);
    }

    /**
     * Sets the target resource id of a resource to which this offer applies.
     *
     * @param resourceId the resource id.
     */
    void setOfferResourceId(String resourceId) {
        this.set(Constants.Properties.OFFER_RESOURCE_ID, resourceId);
    }

    /**
     * Gets the OfferType for the resource offer.
     *
     * @return the offer type.
     */
    public String getOfferType() {
        return super.getString(Constants.Properties.OFFER_TYPE);
    }

    /**
     * Sets the OfferType for the resource offer.
     *
     * @param offerType the offer type.
     */
    public void setOfferType(String offerType) {
        this.set(Constants.Properties.OFFER_TYPE, offerType);
    }

    /**
     * Gets the version of the current offer.
     *
     * @return the offer version.
     */
    public String getOfferVersion() {
        return super.getString(Constants.Properties.OFFER_VERSION);
    }

    /**
     * Sets the offer version.
     *
     * @param offerVersion the version of the offer.
     */
    public void setOfferVersion(String offerVersion) {
        this.set(Constants.Properties.OFFER_VERSION, offerVersion);
    }

    /**
     * Gets the offer throughput for this offer.
     *
     * @return the offer throughput.
     */
    public int getThroughput() {
        return this.getContent().get(Constants.Properties.OFFER_THROUGHPUT).asInt();
    }

    /**
     * Has offer throughput.
     *
     * @return the if the offer has throughput
     */
    public boolean hasOfferThroughput(){
        return this.getContent().hasNonNull(Constants.Properties.OFFER_THROUGHPUT);
    }

    /**
     * Sets the offer throughput for this offer.
     *
     * @param throughput the throughput of this offer.
     */
    public void setThroughput(int throughput) {
        this.getContent().put(Constants.Properties.OFFER_THROUGHPUT, throughput);
    }

    private ObjectNode getContent() {
        return this.getObject(Constants.Properties.OFFER_CONTENT);
    }

    private void setContent(ObjectNode offerContent) {
        this.set(Constants.Properties.OFFER_CONTENT, offerContent);
    }

    @Override
    public String getString(String propertyName) {
        return super.getString(propertyName);
    }

    @Override
    public Integer getInt(String propertyName) {
        return super.getInt(propertyName);
    }

    public void updateAutoscaleThroughput(int maxAutoscaleThroughput) {
        this.getOfferAutoScaleSettings().setMaxThroughput(maxAutoscaleThroughput);
    }

    public void updateContent(Offer offer) {
        this.setContent(offer.getContent());
    }
}
