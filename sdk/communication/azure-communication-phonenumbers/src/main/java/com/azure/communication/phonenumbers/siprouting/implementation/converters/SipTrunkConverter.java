// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.phonenumbers.siprouting.implementation.converters;

import com.azure.communication.phonenumbers.siprouting.models.SipTrunk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A converter between {@link com.azure.communication.phonenumbers.siprouting.implementation.models.SipTrunk} and
 * {@link SipTrunk}.
 */
public final class SipTrunkConverter {
    /**
     * Maps from {@link Map} to {@link List}.
     */
    public static List<SipTrunk> convertFromApi(Map<String, com.azure.communication.phonenumbers.siprouting.implementation.models.SipTrunk> obj) {
        if (obj == null) {
            return null;
        }

        List<SipTrunk> list = new ArrayList<>();
        for (Map.Entry<String, com.azure.communication.phonenumbers.siprouting.implementation.models.SipTrunk> entry : obj.entrySet()) {
            list.add(convertFromApi(entry.getValue(), entry.getKey()));
        }
        return list;
    }

    /**
     * Maps from {@link com.azure.communication.phonenumbers.siprouting.implementation.models.SipTrunk} to {@link SipTrunk}.
     */
    public static SipTrunk convertFromApi(com.azure.communication.phonenumbers.siprouting.implementation.models.SipTrunk obj,
                                          String fqdn) {
        if (obj == null) {
            return null;
        }

        return new SipTrunk(fqdn, obj.getSipSignalingPort());
    }

    /**
     * Maps from {@link List} to {@link Map}.
     */
    public static Map<String, com.azure.communication.phonenumbers.siprouting.implementation.models.SipTrunk>
        convertToApi(List<SipTrunk> obj) {
        if (obj == null) {
            return null;
        }

        Map<String, com.azure.communication.phonenumbers.siprouting.implementation.models.SipTrunk> map = new HashMap<>();
        for (SipTrunk trunk : obj) {
            map.put(trunk.getFqdn(), convertToApi(trunk));
        }
        return map;
    }

    /**
     * Maps from {@link SipTrunk} to {@link com.azure.communication.phonenumbers.siprouting.implementation.models.SipTrunk}.
     */
    public static com.azure.communication.phonenumbers.siprouting.implementation.models.SipTrunk convertToApi(SipTrunk obj) {
        if (obj == null) {
            return null;
        }

        return new com.azure.communication.phonenumbers.siprouting.implementation.models.SipTrunk()
            .setSipSignalingPort(obj.getSipSignalingPort());
    }

    private SipTrunkConverter() {
    }
}
