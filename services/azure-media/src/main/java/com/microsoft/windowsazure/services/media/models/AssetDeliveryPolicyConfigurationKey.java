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
public enum AssetDeliveryPolicyConfigurationKey {

    /** No policies. */
    None(0),
    /** Exact Envelope key URL. */
    EnvelopeKeyAcquisitionUrl(1),
    /** Base key url that will have KID=<Guid> appended for Envelope. */
    EnvelopeBaseKeyAcquisitionUrl(2),
    /** The initialization vector to use for envelope encryption in Base64 format. */
    EnvelopeEncryptionIVAsBase64(3),
    /** The PlayReady License Acquisition Url to use for common encryption. */
    PlayReadyLicenseAcquisitionUrl(4),
    /** The PlayReady Custom Attributes to add to the PlayReady Content Header. */
    PlayReadyCustomAttributes(5),
    /** The initialization vector to use for envelope encryption. */
    EnvelopeEncryptionIV(6),
    /** Widevine DRM Acquisition Url to use for common encryption. */
    WidevineLicenseAcquisitionUrl(7),
    /** Base Widevine url that will have KID=<Guid> appended */
    WidevineBaseLicenseAcquisitionUrl(8),
    /** FairPlay license acquisition URL. */
    FairPlayLicenseAcquisitionUrl(9),
    /** Base FairPlay license acquisition URL that will have KID=<Guid> appended. */
    FairPlayBaseLicenseAcquisitionUrl(10),
    /** Initialization Vector that will be used for encrypting the content. Must match 
        IV in the AssetDeliveryPolicy. */
    CommonEncryptionIVForCbcs(11);    

    /** The AssetDeliveryPolicyType code. */
    private int assetDeliveryPolicyConfigurationKey;

    /**
     * Instantiates a new AssetDeliveryPolicyConfigurationKey.
     * 
     * @param assetDeliveryPolicyConfigurationKey
     *            the AssetDeliveryPolicyConfigurationKey code
     */
    private AssetDeliveryPolicyConfigurationKey(int assetDeliveryPolicyConfigurationKey) {
        this.assetDeliveryPolicyConfigurationKey = assetDeliveryPolicyConfigurationKey;
    }

    /**
     * Gets the code.
     * 
     * @return the code
     */
    public int getCode() {
        return assetDeliveryPolicyConfigurationKey;
    }

    /**
     * Create an AssetDeliveryPolicyConfigurationKey instance based on the given integer.
     * 
     * @param option
     *            the integer value of option
     * @return The AssetDeliveryPolicyType
     */
    public static AssetDeliveryPolicyConfigurationKey fromCode(int option) {
        switch (option) {
        case 0:
            return AssetDeliveryPolicyConfigurationKey.None;
        case 1:
            return AssetDeliveryPolicyConfigurationKey.EnvelopeKeyAcquisitionUrl;
        case 2:
            return AssetDeliveryPolicyConfigurationKey.EnvelopeBaseKeyAcquisitionUrl;
        case 3:
            return AssetDeliveryPolicyConfigurationKey.EnvelopeEncryptionIVAsBase64;
        case 4:
            return AssetDeliveryPolicyConfigurationKey.PlayReadyLicenseAcquisitionUrl;
        case 5:
            return AssetDeliveryPolicyConfigurationKey.PlayReadyCustomAttributes;
        case 6:
            return AssetDeliveryPolicyConfigurationKey.EnvelopeEncryptionIV;
        case 7:
            return AssetDeliveryPolicyConfigurationKey.WidevineLicenseAcquisitionUrl;
        case 8:
            return AssetDeliveryPolicyConfigurationKey.WidevineBaseLicenseAcquisitionUrl;
        case 9:
        	return AssetDeliveryPolicyConfigurationKey.FairPlayLicenseAcquisitionUrl;
        case 10:
        	return AssetDeliveryPolicyConfigurationKey.FairPlayBaseLicenseAcquisitionUrl;
        case 11:
        	return AssetDeliveryPolicyConfigurationKey.CommonEncryptionIVForCbcs;
        default:
            throw new InvalidParameterException("option");
        }
    }
}
