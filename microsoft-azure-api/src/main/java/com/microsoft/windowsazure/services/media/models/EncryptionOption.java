/*
 * Copyright 2012 Microsoft Corporation
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

/**
 * Specifies the options for encryption.
 */
public enum EncryptionOption {

    /** The None. */
    None(0),
    /** The Storage encrypted. */
    StorageEncrypted(1),
    /** The Common encryption protected. */
    CommonEncryptionProtected(2);

    /** The encryption option code. */
    private int encryptionOptionCode;

    /**
     * Instantiates a new encryption option.
     * 
     * @param encryptionOptionCode
     *            the encryption option code
     */
    private EncryptionOption(int encryptionOptionCode) {
        this.encryptionOptionCode = encryptionOptionCode;
    }

    /**
     * Gets the code.
     * 
     * @return the code
     */
    public int getCode() {
        return encryptionOptionCode;
    }
}
