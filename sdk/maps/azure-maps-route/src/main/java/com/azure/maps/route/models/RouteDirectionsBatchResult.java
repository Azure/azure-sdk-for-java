// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.route.models;

import java.util.List;

import com.azure.maps.route.implementation.helpers.RouteDirectionsBatchResultPropertiesHelper;
import com.azure.maps.route.implementation.models.RouteDirectionsBatchResultPrivate;

/**
 * Route Directions Batch Result
 *
 */
public class RouteDirectionsBatchResult {
    private List<RouteDirectionsBatchItem> batchItems;
    private String batchId;

    static {
        RouteDirectionsBatchResultPropertiesHelper.setAccessor(
            new RouteDirectionsBatchResultPropertiesHelper.RouteDirectionsBatchResultAccessor() {
                @Override
                public void setFromRouteDirectionsBatchResultPrivate(RouteDirectionsBatchResult result,
                        RouteDirectionsBatchResultPrivate privateResult) {
                    result.setFromRouteDirectionsBatchResultPrivate(privateResult);
                }
            });
    }

    /**
     * Get the batchItems property: Array containing the batch results.
     *
     * @return the batchItems value.
     */
    public List<RouteDirectionsBatchItem> getBatchItems() {
        return this.batchItems;
    }

    /**
     * Get the batch id of this request.
     *
     * @return the batch id
     */
    public String getBatchId() {
        return batchId;
    }

    /**
     * Sets the matrix id for this request.
     * @param batchId the bach id for this request.
     */
    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    // private setter
    private void setFromRouteDirectionsBatchResultPrivate(RouteDirectionsBatchResultPrivate privateResult) {
        this.batchItems = privateResult.getBatchItems();
    }
}
