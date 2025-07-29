// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.clienttelemetry;

import com.azure.cosmos.implementation.Configs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public enum AttributeNamingScheme {
    PRE_V1_RELEASE,
    V1;

    private static final Logger logger = LoggerFactory.getLogger(AttributeNamingScheme.class);

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
                logger.error("Invalid AttributeNamingScheme: " + textValue + " in '" + name +
                    "' - proceeding with default value 'ALL'");

                return EnumSet.allOf(AttributeNamingScheme.class);
            }
        }

        if (schemeList.size() > 0) {
            return EnumSet.copyOf(schemeList);
        }

        return EnumSet.allOf(AttributeNamingScheme.class);
    }
}
