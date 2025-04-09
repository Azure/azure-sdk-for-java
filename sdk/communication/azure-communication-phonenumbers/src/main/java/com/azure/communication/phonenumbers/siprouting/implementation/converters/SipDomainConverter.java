// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.phonenumbers.siprouting.implementation.converters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.azure.communication.phonenumbers.siprouting.models.SipDomain;

/**
 * A converter between {@link com.azure.communication.phonenumbers.siprouting.implementation.models.SipDomain} and
 * {@link SipDomain}.
 */
public final class SipDomainConverter {
    /**
     * Maps from {@link Map} to {@link List}.
     */
    public static List<SipDomain> convertFromApi(
        Map<String, com.azure.communication.phonenumbers.siprouting.implementation.models.SipDomain> obj) {
        if (obj == null) {
            return null;
        }

        List<SipDomain> list = new ArrayList<>();
        for (Map.Entry<String, com.azure.communication.phonenumbers.siprouting.implementation.models.SipDomain> entry : obj
            .entrySet()) {
            list.add(convertFromApi(entry.getValue(), entry.getKey()));
        }
        return list;
    }

    /**
     * Maps from {@link com.azure.communication.phonenumbers.siprouting.implementation.models.SipDomain} to {@link SipDomain}.
     */
    public static SipDomain convertFromApi(
        com.azure.communication.phonenumbers.siprouting.implementation.models.SipDomain obj, String fqdn) {
        if (obj == null) {
            return null;
        }
        SipDomain sipDomain = new SipDomain();
        sipDomain.setEnabled(obj.isEnabled());
        return sipDomain;
    }

    /**
     * Maps from {@link List} to {@link Map}.
     */
    public static Map<String, com.azure.communication.phonenumbers.siprouting.implementation.models.SipDomain>
        convertToApi(List<SipDomain> obj) {
        if (obj == null) {
            return null;
        }

        Map<String, com.azure.communication.phonenumbers.siprouting.implementation.models.SipDomain> map
            = new HashMap<>();
        for (SipDomain domain : obj) {
            map.put(domain.toString(), convertToApi(domain));
        }
        return map;
    }

    /**
     * Maps from {@link SipDomain} to {@link com.azure.communication.phonenumbers.siprouting.implementation.models.SipDomain}.
     */
    public static com.azure.communication.phonenumbers.siprouting.implementation.models.SipDomain
        convertToApi(SipDomain obj) {
        if (obj == null) {
            return null;
        }

        return new com.azure.communication.phonenumbers.siprouting.implementation.models.SipDomain()
            .setEnabled(obj.isEnabled());
    }

    private SipDomainConverter() {
    }
}
