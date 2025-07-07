// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.clienttelemetry;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public enum AttributeNamingScheme {
    PRE_V1_RELEASE,
    V1;

    private static AttributeNamingScheme fromStringCore(String name) {
        for (AttributeNamingScheme scheme : values()) {
            if (scheme.name().equalsIgnoreCase(name)) {
                return scheme;
            }
        }
        throw new IllegalArgumentException("Invalid AttributeNamingScheme: " + name);
    }

    public static EnumSet<AttributeNamingScheme> parse(String name) {
        if (name == null || name.isEmpty() || "All".equalsIgnoreCase(name) || "Default".equalsIgnoreCase(name)) {
            return EnumSet.allOf(AttributeNamingScheme.class);
        }

        String[] textValues = name.split(",");
        List<AttributeNamingScheme> schemeList = new ArrayList<>();
        for (String textValue : textValues) {
            boolean foundTextValue = false;
            for (AttributeNamingScheme scheme : values()) {
                if (scheme.name().equalsIgnoreCase(textValue)) {
                    foundTextValue = true;
                    schemeList.add(scheme);
                    break;
                }
            }

            if (!foundTextValue) {
                throw new IllegalArgumentException("Invalid AttributeNamingScheme: " + textValue + " in '" + name + "'");
            }
        }

        if (schemeList.size() > 0) {
            return EnumSet.copyOf(schemeList);
        }

        return EnumSet.allOf(AttributeNamingScheme.class);
    }
}