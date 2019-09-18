// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.azure.ai.inkrecognizer;

import java.util.HashMap;
import java.util.Map;

/**
 * The ApplicationKind enum allows an application to identify its domain (when it has one).
 * Specifying a domain allows the application to inform the service of its contents. This
 * can facilitate faster processing as the service will skip some classification steps.
 * Applications that don't have a specific domain can simply specify ApplicationKind.MIXED which is the default.
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
    private static final Map<String, ApplicationKind> map = new HashMap<>();

    static {
        for (ApplicationKind applicationKind : ApplicationKind.values()) {
            map.put(applicationKind.toString().toLowerCase(), applicationKind);
        }
    }

    ApplicationKind(String applicationKindString) {
        this.applicationKindString = applicationKindString;
    }

    static ApplicationKind getApplicationKindOrDefault(String applicationKindString) {
        if (applicationKindString != null && map.containsKey(applicationKindString.toLowerCase())) {
            return map.get(applicationKindString.toLowerCase());
        } else {
            return ApplicationKind.MIXED;
        }
    }

    @Override
    public String toString() {
        return applicationKindString;
    }

}
