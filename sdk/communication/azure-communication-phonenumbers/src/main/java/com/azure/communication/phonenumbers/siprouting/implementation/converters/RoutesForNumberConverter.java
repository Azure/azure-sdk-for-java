// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.phonenumbers.siprouting.implementation.converters;

import java.util.List;

import com.azure.communication.phonenumbers.siprouting.implementation.models.SipTrunkRoute;
import com.azure.communication.phonenumbers.siprouting.models.RoutesForNumber;

/**
 * A converter between {@link com.azure.communication.phonenumbers.siprouting.implementation.models.RoutesForNumber} and
 * {@link com.azure.communication.phonenumbers.siprouting.models.RoutesForNumber}.
 */
public class RoutesForNumberConverter {

    /**
     * Maps from {@link com.azure.communication.phonenumbers.siprouting.implementation.models.RoutesForNumber} to {@link RoutesForNumber}.
     * @param obj the list of routes matching the target phone number, ordered by priority {@link com.azure.communication.phonenumbers.siprouting.implementation.models.RoutesForNumber}.
     * @return the list of routes matching the target phone number, ordered by priority {@link RoutesForNumber}.
     */
    public static RoutesForNumber convertRoutesForNumber(
        com.azure.communication.phonenumbers.siprouting.implementation.models.RoutesForNumber obj) {
        if (obj == null) {
            return null;
        }
        RoutesForNumber routesForNumber = new RoutesForNumber();
        List<SipTrunkRoute> matchingRoutes = obj.getMatchingRoutes();
        routesForNumber.setMatchingRoutes(matchingRoutes);

        return routesForNumber;

    }
}
