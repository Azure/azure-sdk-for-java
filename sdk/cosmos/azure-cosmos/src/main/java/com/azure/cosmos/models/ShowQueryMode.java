// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines whether to print db.statement in tracing.
 */
public enum ShowQueryMode {

    /**
     * Do not show query.
     */
    NONE("None"),

    /**
     * Print parameterized query only.
     */
    PARAMETERIZED_ONLY("ParameterizedOnly"),

    /**
     *  Print both parameterized and non parameterized query.
     */
    ALL("All");

    private static Map<String, ShowQueryMode> showQueryOptionsHashMap = initializeMap();
    
    private static Map<String, ShowQueryMode> initializeMap() {
	    Map<String, ShowQueryMode> showQueryOptionsHashMap = new HashMap<>();
        for(ShowQueryMode showQueryOptions : ShowQueryMode.values()) {
	    	showQueryOptionsHashMap.put(showQueryOptions.toString(), showQueryOptions);
	    }

        return showQueryOptionsHashMap;
    }
    
    private final String value;
	
    ShowQueryMode(String value) {
        this.value = value;
    }
	
    static ShowQueryMode fromServiceSerializedFormat(String showQueryOptions) {
        return showQueryOptionsHashMap.get(showQueryOptions);
    }

    @JsonValue
    @Override
    public String toString() {
        return this.value;
    }
}
