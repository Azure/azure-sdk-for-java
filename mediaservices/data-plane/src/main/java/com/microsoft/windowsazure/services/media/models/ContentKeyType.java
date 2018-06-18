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
 * 
 * Specifies the type of a content key.
 * 
 */
public enum ContentKeyType {

    /** The Common encryption. */
    CommonEncryption(0),
    /** The Storage encryption. */
    StorageEncryption(1),
    /** The Configuration encryption. */
    ConfigurationEncryption(2),
    /** The Envelope encryption. */
    EnvelopeEncryption(4),
    /** Specifies a content key for common encryption with CBCS. */
    CommonEncryptionCbcs(6),
    /** Application Secret key for FairPlay. */
    FairPlayASk (7),
    /** Password for FairPlay application certificate. */
    FairPlayPfxPassword (8);

    /** The content key type code. */
    private int contentKeyTypeCode;

    /**
     * Instantiates a new content key type.
     * 
     * @param contentKeyTypeCode
     *            the content key type code
     */
    private ContentKeyType(int contentKeyTypeCode) {
        this.contentKeyTypeCode = contentKeyTypeCode;
    }

    /**
     * Gets the code.
     * 
     * @return the code
     */
    public int getCode() {
        return contentKeyTypeCode;
    }

    /**
     * From code.
     * 
     * @param code
     *            the code
     * @return the content key type
     */
    public static ContentKeyType fromCode(int code) {
        switch (code) {
        case 0:
            return ContentKeyType.CommonEncryption;
        case 1:
            return ContentKeyType.StorageEncryption;
        case 2:
            return ContentKeyType.ConfigurationEncryption;
        case 4: 
            return ContentKeyType.EnvelopeEncryption;
        case 6: 
            return ContentKeyType.CommonEncryptionCbcs;
        case 7: 
            return ContentKeyType.FairPlayASk;
        case 8: 
            return ContentKeyType.FairPlayPfxPassword;
        default:
            throw new InvalidParameterException("code");
        }
    }
}
