/**
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
package com.microsoft.azure.storage;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Represents extended error information returned by the Microsoft Azure storage service.
 */
public final class StorageExtendedErrorInformation implements Serializable {
    /**
     * The serialization version number.
     */
    private static final long serialVersionUID = 1527013626991334677L;

    /**
     * Represents additional error details, as a <code>java.util.HashMap</code> object.
     */
    private HashMap<String, String[]> additionalDetails;

    /**
     * Represents the storage service error code.
     */
    private String errorCode;

    /**
     * Represents the storage service error message.
     */
    private String errorMessage;

    /**
     * Creates an instance of the <code>StorageExtendedErrorInformation</code> class.
     */
    public StorageExtendedErrorInformation() {
        this.setAdditionalDetails(new HashMap<String, String[]>());
    }

    /**
     * Gets additional error details, as a <code>java.util.HashMap</code> object.
     * 
     * @return A <code>java.util.HashMap</code> of key/value pairs which contain the additional error details.
     */
    public HashMap<String, String[]> getAdditionalDetails() {
        return this.additionalDetails;
    }

    /**
     * Gets the storage service error code.
     * 
     * @return A <code>String</code> which contains the error code.
     */
    public String getErrorCode() {
        return this.errorCode;
    }

    /**
     * Gets the storage service error message.
     * 
     * @return A <code>String</code> which contains the error message.
     */
    public String getErrorMessage() {
        return this.errorMessage;
    }

    /**
     * RESERVED FOR INTERNAL USE.
     * 
     * Sets additional error details.
     * 
     * @param additionalDetails
     *        A <code>java.util.HashMap</code> of key/value pairs which contain the additional error details.
     */
    public void setAdditionalDetails(final HashMap<String, String[]> additionalDetails) {
        this.additionalDetails = additionalDetails;
    }

    /**
     * RESERVED FOR INTERNAL USE.
     * 
     * Sets the storage service error code.
     * 
     * @param errorCode
     *        A <code>String</code> which contains the error code.
     */
    public void setErrorCode(final String errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * RESERVED FOR INTERNAL USE.
     * 
     * Sets the storage service error message.
     * 
     * @param errorMessage
     *        A <code>String</code> which contains the error message.
     */
    public void setErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
