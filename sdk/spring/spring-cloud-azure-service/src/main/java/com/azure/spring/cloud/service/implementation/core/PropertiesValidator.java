// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.service.implementation.core;

public final class PropertiesValidator {

    private PropertiesValidator() {
    }

    public static final String LENGTH_ERROR = "The namespace must be between 6 and 50 characters long.";
    public static final String ILLEGAL_SYMBOL_ERROR = "The namespace can contain only letters, numbers, and hyphens.";
    public static final String START_SYMBOL_ERROR = "The namespace must start with a letter.";
    public static final String END_SYMBOL_ERROR = "The namespace must end with a letter or number.";

    public static void validateNamespace(String namespace) {
        validateLength(namespace);
        validateIllegalSymbol(namespace);
        validateStartingSymbol(namespace);
        validateEndingSymbol(namespace);
    }

    private static void validateLength(String namespace) {
        if (namespace.length() < 6 || namespace.length() > 50) {
            throw new IllegalArgumentException(LENGTH_ERROR);
        }
    }

    private static void validateIllegalSymbol(String namespace) {
        if (!namespace.matches("[a-z0-9A-Z-]+")) {
            throw new IllegalArgumentException(ILLEGAL_SYMBOL_ERROR);
        }
    }

    private static void validateStartingSymbol(String namespace) {
        if (!Character.isLetter(namespace.charAt(0))) {
            throw new IllegalArgumentException(START_SYMBOL_ERROR);
        }
    }

    private static void validateEndingSymbol(String namespace) {
        if (!Character.isLetterOrDigit(namespace.charAt(namespace.length() - 1))) {
            throw new IllegalArgumentException(END_SYMBOL_ERROR);
        }
    }
}
