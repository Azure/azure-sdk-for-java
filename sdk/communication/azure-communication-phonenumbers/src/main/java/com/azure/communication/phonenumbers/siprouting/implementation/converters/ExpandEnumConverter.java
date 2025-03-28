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
     * Maps from {@link ExpandEnum} to {@link com.azure.communication.phonenumbers.siprouting.implementation.models.ExpandEnum}.
     * @param obj Sip configuration expand. Optional.
     * @return com.azure.communication.phonenumbers.siprouting.implementation.models.ExpandEnum enum
     */
    public static ExpandEnum convertExpandEnum(com.azure.communication.phonenumbers.siprouting.models.ExpandEnum obj) {
        if (obj == null) {
            return null;
        }
        return ExpandEnum.fromString(obj.toString());
    }

    private ExpandEnumConverter() {
    }
}
