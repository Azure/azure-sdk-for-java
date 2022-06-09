/*
 * Copyright (c) Microsoft Corporation. All rights reserved. Licensed under the MIT License.
 */

package com.azure.cosmos.encryption.implementation.mdesrc.cryptography;

import java.text.MessageFormat;


/**
 * Utils class imported from the .NET implementation. Seldomly used to validate strings.
 *
 */
public class Utils {

    /**
     * Validates that the object isn't null.
     *
     * @param s
     *        the object
     * @param name
     *        name of the object
     * @throws MicrosoftDataEncryptionException
     */
    public static void validateNotNull(Object s, String name) throws MicrosoftDataEncryptionException {
        if (null == s) {
            MessageFormat form = new MessageFormat(
                    MicrosoftDataEncryptionExceptionResource.getResource("R_CannotBeNull"));
            Object[] msgArgs = {name};
            throw new MicrosoftDataEncryptionException(form.format(msgArgs));
        }
    }

    /**
     * Validates that the string isn't null and doesn't contain white spaces.
     *
     * @param s
     *        the string
     * @param name
     *        name of the string
     * @throws MicrosoftDataEncryptionException
     */
    public static void validateNotNullOrWhitespace(String s, String name) throws MicrosoftDataEncryptionException {
        if (null == s || s.isEmpty() || s.trim().isEmpty()) {
            MessageFormat form = new MessageFormat(
                    MicrosoftDataEncryptionExceptionResource.getResource("R_CannotBeNullOrWhiteSpace"));
            Object[] msgArgs = {name};
            throw new MicrosoftDataEncryptionException(form.format(msgArgs));
        }
    }
}
