// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml;

public enum MessagingSku {
    BASIC("Basic"), STANDARD("Standard"), PREMIUM("Premium");

    private final String sku;

    MessagingSku(String sku) {
        this.sku = sku;
    }

    public static MessagingSku fromString(String sku) {
        if (sku == null) {
            return null;
        }

        switch (sku) {
            case "Basic":
                return BASIC;

            case "Standard":
                return STANDARD;

            case "Premium":
                return PREMIUM;

            default:
                return null;
        }
    }

    @Override
    public String toString() {
        return sku;
    }
}
