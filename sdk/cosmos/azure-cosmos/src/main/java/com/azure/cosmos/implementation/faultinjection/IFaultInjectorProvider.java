// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.faultinjection;

import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdEndpoint;

/***
 * Fault injection provider.
 */
public interface IFaultInjectorProvider {

    /***
     * Get the server error injector.
     * @return the server error injector.
     */
    IServerErrorInjector getServerErrorInjector();

    /***
     * Register the rntbd connection error injector.
     * @param provider the rntbd endpoint provider.
     */
    void registerConnectionErrorInjector(RntbdEndpoint.Provider provider);
}
