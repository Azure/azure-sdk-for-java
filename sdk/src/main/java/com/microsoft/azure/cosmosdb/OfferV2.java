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

import org.json.JSONObject;

import com.microsoft.azure.cosmosdb.internal.Constants;

/**
 * Represents an offer version 2 in the Azure Cosmos DB database service.
 */
@SuppressWarnings("serial")
public class OfferV2 extends Offer {

    /**
     * Initialize an new instance of the OfferV2 object.
     *
     * @param offerThroughput the throughput value for this offer.
     */
    public OfferV2(int offerThroughput) {
        this.setOfferVersion(Constants.Properties.OFFER_VERSION_V2);
        this.setOfferType("");
        JSONObject content = new JSONObject();
        content.put(Constants.Properties.OFFER_THROUGHPUT, offerThroughput);
        this.setContent(content);
    }

    /**
     * Initialize an new instance of the OfferV2 object, copy the base
     * properties from another Offer object and set the throughput value.
     *
     * @param otherOffer the Offer object whose base properties are to be copied.
     */
    public OfferV2(Offer otherOffer) {
        super(otherOffer);

        this.setOfferVersion(Constants.Properties.OFFER_VERSION_V2);
        this.setOfferType("");

        JSONObject content = this.getContent();
        if (content == null) {
            content = new JSONObject();
            this.setContent(content);
        }
    }

    /**
     * Gets the offer throughput for this offer.
     *
     * @return the offer throughput.
     */
    public int getOfferThroughput() {
        return this.getContent().getInt(Constants.Properties.OFFER_THROUGHPUT);
    }

    /**
     * Sets the offer throughput for this offer.
     *
     * @param throughput the throughput of this offer.
     */
    public void setOfferThroughput(int throughput) {
        this.getContent().put(Constants.Properties.OFFER_THROUGHPUT, throughput);
    }
}
