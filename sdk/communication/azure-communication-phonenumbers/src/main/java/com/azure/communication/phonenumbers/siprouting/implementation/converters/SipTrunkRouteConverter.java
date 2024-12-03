// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.phonenumbers.siprouting.implementation.converters;

import com.azure.communication.phonenumbers.siprouting.models.SipTrunkRoute;

import java.util.ArrayList;
import java.util.List;

/**
 * A converter between {@link com.azure.communication.phonenumbers.siprouting.implementation.models.SipTrunkRoute} and
 * {@link com.azure.communication.phonenumbers.siprouting.models.SipTrunkRoute}.
 */
public final class SipTrunkRouteConverter {
    /**
     * Maps from {@link com.azure.communication.phonenumbers.siprouting.implementation.models.SipTrunkRoute}
     * to {@link com.azure.communication.phonenumbers.siprouting.models.SipTrunkRoute}.
     */
    public static List<SipTrunkRoute> convertFromApi(List<com.azure.communication.phonenumbers.siprouting.implementation.models.SipTrunkRoute> apiRoutes) {
        if (apiRoutes == null) {
            return null;
        }

        List<SipTrunkRoute> result = new ArrayList<>();
        for (com.azure.communication.phonenumbers.siprouting.implementation.models.SipTrunkRoute apiRoute : apiRoutes) {
            result.add(new SipTrunkRoute(apiRoute.getName(), apiRoute.getNumberPattern())
                .setDescription(apiRoute.getDescription())
                .setTrunks(apiRoute.getTrunks()));
        }

        return result;
    }

    /**
     * Maps from {@link com.azure.communication.phonenumbers.siprouting.models.SipTrunkRoute}
     * to {@link com.azure.communication.phonenumbers.siprouting.implementation.models.SipTrunkRoute}.
     */
    public static List<com.azure.communication.phonenumbers.siprouting.implementation.models.SipTrunkRoute>
        convertToApi(List<SipTrunkRoute> routes) {
        if (routes == null) {
            return null;
        }

        List<com.azure.communication.phonenumbers.siprouting.implementation.models.SipTrunkRoute> result = new ArrayList<>();
        for (SipTrunkRoute route : routes) {
            result.add(new com.azure.communication.phonenumbers.siprouting.implementation.models.SipTrunkRoute()
                    .setName(route.getName())
                    .setNumberPattern(route.getNumberPattern())
                    .setDescription(route.getDescription())
                    .setTrunks(route.getTrunks()));
        }

        return result;
    }

    private SipTrunkRouteConverter() {
    }
}
