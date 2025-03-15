// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.phonenumbers.siprouting.implementation.converters;

import com.azure.communication.phonenumbers.siprouting.implementation.models.SipConfiguration;

/**
 * A converter between {@link com.azure.communication.phonenumbers.siprouting.implementation.models.SipConfiguration} and
 * {@link com.azure.communication.phonenumbers.siprouting.models.SipConfiguration}.
 */
public final class SipConfigurationConverter {

    /**
    * Maps from {@link com.azure.communication.phonenumbers.siprouting.implementation.models.SipConfiguration} to {@link SipConfiguration}.
    * @param obj Sip configuration.
    * @return SipConfiguration
    */
    public static SipConfiguration
        convertSipConfiguration(com.azure.communication.phonenumbers.siprouting.models.SipConfigurationModel obj) {
        if (obj == null) {
            return null;
        }
        SipConfiguration sipConfiguration = new SipConfiguration();
        sipConfiguration.setDomains(obj.getDomains());
        sipConfiguration.setTrunks(obj.getTrunks());
        sipConfiguration.setRoutes(obj.getRoutes());
        return sipConfiguration;
    }

    private SipConfigurationConverter() {
    }
}
