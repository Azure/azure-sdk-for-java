/*
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.windowsazure.services.media.models;

import java.security.InvalidParameterException;

/**
 * Specifies the types of Encoding Reserved Units.
 */
public enum EncodingReservedUnitType {

    /** The Basic. */
    Basic(0),
    /** The Standard. */
    Standard (1),
    /** The Premium . */
    Premium (2);

    /** The Encoding Reserved Unit type code. */
    private int encodingReservedUnitType;

    /**
     * Instantiates a new asset state.
     * 
     * @param encodingReservedUnitType
     *            the EncodingReservedUnitType code
     */
    private EncodingReservedUnitType(int encodingReservedUnitType) {
        this.encodingReservedUnitType = encodingReservedUnitType;
    }

    /**
     * Gets the code.
     * 
     * @return the code
     */
    public int getCode() {
        return encodingReservedUnitType;
    }

    /**
     * Create an EncodingReservedUnitType instance from the corresponding int.
     * 
     * @param type
     *            type as integer
     * @return new EncodingReservedUnitType instance
     */
    public static EncodingReservedUnitType fromCode(int state) {
        switch (state) {
        case 0:
            return EncodingReservedUnitType.Basic;
        case 1:
            return EncodingReservedUnitType.Standard;
        case 2:
            return EncodingReservedUnitType.Premium;
        default:
            throw new InvalidParameterException("state");
        }
    }
}
