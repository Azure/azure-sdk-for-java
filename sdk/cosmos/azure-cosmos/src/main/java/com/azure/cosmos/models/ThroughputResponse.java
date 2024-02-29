// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.Offer;
import com.azure.cosmos.implementation.ResourceResponse;

/**
 * The cosmos throughput response.
 */
public class ThroughputResponse extends CosmosResponse<ThroughputProperties> {

    private ResourceResponse<Offer> offerResourceResponse;
    private ThroughputProperties throughputProperties;

    ThroughputResponse(ResourceResponse<Offer> offerResourceResponse) {
        super(offerResourceResponse);
        this.offerResourceResponse = offerResourceResponse;
    }

    @Override
    public ThroughputProperties getProperties(){
        if (throughputProperties == null){
            Offer offer =
                new Offer(offerResourceResponse.getResource().getPropertyBag());
            throughputProperties = new ThroughputProperties(offer);
        }
        return throughputProperties;
    }

    /**
     * Gets minimum throughput in measurement of request units per second in the Azure Cosmos service.
     *
     * @return the minimun throughput
     */
    public int getMinThroughput(){
        return Integer.parseInt(offerResourceResponse
                                    .getResponseHeaders()
                                    .get(HttpConstants.HttpHeaders.OFFER_MIN_THROUGHPUT));
    }

    /**
     * Gets the status whether offer replace is successful or pending.
     *
     * @return the boolean representing the status
     */
    public boolean isReplacePending(){
        return Boolean.parseBoolean(offerResourceResponse
                                        .getResponseHeaders()
                                        .get(HttpConstants.HttpHeaders.OFFER_REPLACE_PENDING));
    }

}
