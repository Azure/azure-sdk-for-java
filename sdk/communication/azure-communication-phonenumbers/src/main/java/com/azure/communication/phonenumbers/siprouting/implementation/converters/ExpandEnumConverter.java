// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.phonenumbers.siprouting.implementation.converters;

import com.azure.communication.phonenumbers.siprouting.implementation.models.ExpandEnum;

/**
 * A converter between {@link com.azure.communication.phonenumbers.siprouting.implementation.models.ExpandEnum} and
 * {@link ExpandEnum}.
 */
public final class ExpandEnumConverter {

    /**
     * Maps from {@link com.azure.communication.phonenumbers.siprouting.implementation.models.SipTrunk} to {@link SipTrunk}.
     * @param obj Sip configuration expand. Optional.
     * @return Expand enum
     */
    public static ExpandEnum convertExpandEnum(com.azure.communication.phonenumbers.siprouting.models.ExpandEnum obj) {
        if (obj == null) {
            return null;
        }
        ExpandEnum expandEnum = ExpandEnum.TRUNKS_HEALTH;
        return expandEnum;
    }

    private ExpandEnumConverter() {
    }
}
