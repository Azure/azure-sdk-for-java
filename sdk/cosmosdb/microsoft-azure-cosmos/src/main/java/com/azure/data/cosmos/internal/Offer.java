// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.Resource;
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

    /**
     * Initialize an offer object from json string.
     *
     * @param jsonString the json string that represents the offer.
     */
    public Offer(String jsonString) {
        super(jsonString);
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
        BridgeInternal.setProperty(this, Constants.Properties.RESOURCE_LINK, resourceLink);
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
        BridgeInternal.setProperty(this, Constants.Properties.OFFER_RESOURCE_ID, resourceId);
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
        BridgeInternal.setProperty(this, Constants.Properties.OFFER_TYPE, offerType);
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
        BridgeInternal.setProperty(this, Constants.Properties.OFFER_VERSION, offerVersion);
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
     * Sets the offer throughput for this offer.
     *
     * @param throughput the throughput of this offer.
     */
    public void setThroughput(int throughput) {
        this.getContent().put(Constants.Properties.OFFER_THROUGHPUT, throughput);
    }

    private ObjectNode getContent() {
        return BridgeInternal.getObject(this, Constants.Properties.OFFER_CONTENT);
    }

    private void setContent(ObjectNode offerContent) {
        BridgeInternal.setProperty(this, Constants.Properties.OFFER_CONTENT, offerContent);
    }
}
