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
public enum ContentKeyDeliveryType {

    /** None. */
    None(0),
    /** Use PlayReady License acquisition protocol. */
    PlayReadyLicense(1),
    /** Use MPEG Baseline HTTP key protocol. */
    BaselineHttp(2),
    /** Use Widevine license acquisition protocol. */
    Widevine(3),
    /** Send FairPlay SPC to Key Delivery server and get CKC back */
    FairPlay(4);
    
    /** The AssetDeliveryPolicyType code. */
    private int contentKeyDeliveryType;

    /**
     * Instantiates a new ContentKeyDeliveryType.
     * 
     * @param contentKeyDeliveryType
     *            the ContentKeyDeliveryType code
     */
    private ContentKeyDeliveryType(int contentKeyDeliveryType) {
        this.contentKeyDeliveryType = contentKeyDeliveryType;
    }

    /**
     * Gets the code.
     * 
     * @return the code
     */
    public int getCode() {
        return contentKeyDeliveryType;
    }

    /**
     * Create an AssetDeliveryPolicyConfigurationKey instance based on the given integer.
     * 
     * @param option
     *            the integer value of option
     * @return The AssetDeliveryPolicyType
     */
    public static ContentKeyDeliveryType fromCode(int option) {
        switch (option) {
        case 0:
            return ContentKeyDeliveryType.None;
        case 1:
            return ContentKeyDeliveryType.PlayReadyLicense;
        case 2:
            return ContentKeyDeliveryType.BaselineHttp;
        case 3:
            return ContentKeyDeliveryType.Widevine;
        case 4:
            return ContentKeyDeliveryType.FairPlay;
        default:
            throw new InvalidParameterException("option");
        }
    }
}
