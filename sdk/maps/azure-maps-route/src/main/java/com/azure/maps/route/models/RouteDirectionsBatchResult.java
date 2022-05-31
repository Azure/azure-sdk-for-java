package com.azure.maps.route.models;

import java.util.List;

import com.azure.maps.route.implementation.helpers.RouteDirectionsBatchResultPropertiesHelper;
import com.azure.maps.route.implementation.models.RouteDirectionsBatchResultPrivate;

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
     */
    public String getBatchId() {
        return batchId;
    }

    /**
     * Sets the matrix id for this request.
     * @param batch
     */
    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    // private setter
    private void setFromRouteDirectionsBatchResultPrivate(RouteDirectionsBatchResultPrivate privateResult) {
        this.batchItems = privateResult.getBatchItems();
    }
}
