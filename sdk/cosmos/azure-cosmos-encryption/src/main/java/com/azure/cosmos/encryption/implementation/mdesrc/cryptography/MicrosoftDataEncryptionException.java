/*
 * Copyright (c) Microsoft Corporation. All rights reserved. Licensed under the MIT License.
 */

package com.azure.cosmos.encryption.implementation.mdesrc.cryptography;

/**
 * Represents the exception thrown from any point in the SDK.
 */
public class MicrosoftDataEncryptionException extends Exception {

    /**
     * Creates the custom exception for the Microsoft Data Encryption SDK.
     */
    public MicrosoftDataEncryptionException() {
        super();
    }

    /**
     * Creates the custom exception for the Microsoft Data Encryption SDK.
     *
     * @param errMessage
     *        error message
     */
    public MicrosoftDataEncryptionException(String errMessage) {
        super(errMessage);
    }

    /**
     * Retrieves a predetermined error message from the resource file
     *
     * @param errCode
     *        error code
     * @return error message
     */
    public static String getErrString(String errCode) {
        return MicrosoftDataEncryptionExceptionResource.getResource(errCode);
    }
}
