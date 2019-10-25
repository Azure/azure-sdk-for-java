// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.cognitiveservices.inkrecognizer;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * The ApplicationKind enum allows an application to identify its domain (when it has one). Specifying a domain allows
 * the application to inform the service of its contents. This can facilitate faster processing as the service will skip
 * some classification steps. Applications that don't have a specific domain can simply specify ApplicationKind.MIXED
 * which is the default.
 * @author Microsoft
 * @version 1.0
 */
public enum ApplicationKind {

    /**
     * The application can have strokes of different kinds
     */
    MIXED("mixed"),

    /**
     * The application can only have drawing strokes.
     */
    DRAWING("drawing"),

    /**
     * The application can only have writing strokes.
     */
    WRITING("writing");

    private String applicationKindString;
    private static final Map<String, ApplicationKind> MAP = new HashMap<>();

    static {
        for (ApplicationKind applicationKind : ApplicationKind.values()) {
            MAP.put(applicationKind.toString().toLowerCase(Locale.getDefault()), applicationKind);
        }
    }

    ApplicationKind(String applicationKindString) {
        this.applicationKindString = applicationKindString;
    }

    static ApplicationKind getApplicationKindOrDefault(String applicationKindString) {
        if (applicationKindString != null && MAP.containsKey(applicationKindString.toLowerCase(Locale.getDefault()))) {
            return MAP.get(applicationKindString.toLowerCase(Locale.getDefault()));
        } else {
            return ApplicationKind.MIXED;
        }
    }

    @Override
    public String toString() {
        return applicationKindString;
    }

}
