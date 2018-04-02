/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azure.cosmosdb;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.microsoft.azure.cosmosdb.internal.Constants;

/**
 * Represents the offer for a resource in the Azure Cosmos DB database service.
 */
@SuppressWarnings("serial")
public class Offer extends Resource {
    /**
     * Initialize an offer object.
     */
    public Offer() {
        super();
        this.setOfferVersion(Constants.Properties.OFFER_VERSION_V1);
    }

    /**
     * Initialize an offer object and copy all properties from the other offer.
     *
     * @param otherOffer the Offer object whose properties to copy over.
     */
    public Offer(Offer otherOffer) {
        super();
        String serializedString = otherOffer.toJson();
        this.propertyBag = new Offer(serializedString).propertyBag;
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
     * Initialize an offer object from json object.
     *
     * @param jsonObject the json object that represents the offer.
     */
    public Offer(JSONObject jsonObject) {
        super(jsonObject);
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
        super.set(Constants.Properties.RESOURCE_LINK, resourceLink);
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
        super.set(Constants.Properties.OFFER_RESOURCE_ID, resourceId);
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
        super.set(Constants.Properties.OFFER_TYPE, offerType);
        if (StringUtils.isNotEmpty(offerType)) {
            // OfferType is only supported for V2 offers.
            this.setOfferVersion(Constants.Properties.OFFER_VERSION_V1);
        }
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
        super.set(Constants.Properties.OFFER_VERSION, offerVersion);
    }

    /**
     * Gets the content object that contains the details of the offer.
     *
     * @return the offer content.
     */
    public JSONObject getContent() {
        return super.getObject(Constants.Properties.OFFER_CONTENT);
    }

    /**
     * Sets the offer content that contains the details of the offer.
     *
     * @param offerContent the content object.
     */
    public void setContent(JSONObject offerContent) {
        super.set(Constants.Properties.OFFER_CONTENT, offerContent);
    }
}