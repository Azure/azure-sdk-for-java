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
 * The Enum ProtectionKeyType.
 */
public enum ProtectionKeyType {

    /** The X509 certificate thumbprint. */
    X509CertificateThumbprint(0);

    /** The protection key type code. */
    private int protectionKeyTypeCode;

    /**
     * Instantiates a new protection key type.
     * 
     * @param protectionKeyTypeCode
     *            the protection key type code
     */
    private ProtectionKeyType(int protectionKeyTypeCode) {
        this.protectionKeyTypeCode = protectionKeyTypeCode;
    }

    /**
     * Gets the code.
     * 
     * @return the code
     */
    public int getCode() {
        return protectionKeyTypeCode;
    }

    /**
     * Create an ProtectionKeyType instance from the corresponding int.
     * 
     * @param protectionKeyTypeCode
     *            protectionKeyTypeCode as integer
     * @return new ProtectionKeyType instance
     */
    public static ProtectionKeyType fromCode(int protectionKeyTypeCode) {
        switch (protectionKeyTypeCode) {
        case 0:
            return ProtectionKeyType.X509CertificateThumbprint;
        default:
            throw new InvalidParameterException("state");
        }
    }

}
