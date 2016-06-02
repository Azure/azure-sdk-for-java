/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation.api;


/**
 * Contains Stats associated with the peering.
 */
public class ExpressRouteCircuitStatsInner {
    /**
     * Gets BytesIn of the peering.
     */
    private Integer bytesIn;

    /**
     * Gets BytesOut of the peering.
     */
    private Integer bytesOut;

    /**
     * Get the bytesIn value.
     *
     * @return the bytesIn value
     */
    public Integer bytesIn() {
        return this.bytesIn;
    }

    /**
     * Set the bytesIn value.
     *
     * @param bytesIn the bytesIn value to set
     * @return the ExpressRouteCircuitStatsInner object itself.
     */
    public ExpressRouteCircuitStatsInner withBytesIn(Integer bytesIn) {
        this.bytesIn = bytesIn;
        return this;
    }

    /**
     * Get the bytesOut value.
     *
     * @return the bytesOut value
     */
    public Integer bytesOut() {
        return this.bytesOut;
    }

    /**
     * Set the bytesOut value.
     *
     * @param bytesOut the bytesOut value to set
     * @return the ExpressRouteCircuitStatsInner object itself.
     */
    public ExpressRouteCircuitStatsInner withBytesOut(Integer bytesOut) {
        this.bytesOut = bytesOut;
        return this;
    }

}
