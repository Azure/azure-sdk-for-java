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
 * Specifies the AssetDeliveryPolicyType.
 */
public enum AssetDeliveryPolicyType {

    /** Delivery Policy Type not set.  An invalid value. */
    None(0),
    /** The Asset should not be delivered via this AssetDeliveryProtocol. */
    Blocked(1),
    /** Do not apply dynamic encryption to the asset. */
    NoDynamicEncryption(2),
    /** Apply Dynamic Envelope encryption. */
    DynamicEnvelopeEncryption(3),
    /** Apply Dynamic Common encryption. */
    DynamicCommonEncryption(4),
    /** Apply Dynamic Common encryption with cbcs */
    DynamicCommonEncryptionCbcs(5);
    

    /** The AssetDeliveryPolicyType code. */
    private int assetDeliveryPolicyTypeCode;

    /**
     * Instantiates a new AssetDeliveryPolicyType.
     * 
     * @param assetDeliveryPolicyTypeCode
     *            the AssetDeliveryPolicyType code
     */
    private AssetDeliveryPolicyType(int assetDeliveryPolicyTypeCode) {
        this.assetDeliveryPolicyTypeCode = assetDeliveryPolicyTypeCode;
    }

    /**
     * Gets the code.
     * 
     * @return the code
     */
    public int getCode() {
        return assetDeliveryPolicyTypeCode;
    }

    /**
     * Create an AssetDeliveryPolicyType instance based on the given integer.
     * 
     * @param option
     *            the integer value of option
     * @return The AssetDeliveryPolicyType
     */
    public static AssetDeliveryPolicyType fromCode(int option) {
        switch (option) {
        case 0:
            return AssetDeliveryPolicyType.None;
        case 1:
            return AssetDeliveryPolicyType.Blocked;
        case 2:
            return AssetDeliveryPolicyType.NoDynamicEncryption;
        case 3:
            return AssetDeliveryPolicyType.DynamicEnvelopeEncryption;
        case 4:
            return AssetDeliveryPolicyType.DynamicCommonEncryption;
        case 5:
            return AssetDeliveryPolicyType.DynamicCommonEncryptionCbcs;
        default:
            throw new InvalidParameterException("option");
        }
    }
}
