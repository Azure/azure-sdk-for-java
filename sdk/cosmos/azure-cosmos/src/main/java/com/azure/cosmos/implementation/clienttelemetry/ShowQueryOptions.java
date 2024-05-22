package com.azure.cosmos.implementation.clienttelemetry;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ShowQueryOptions {

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

    private static Map<String, ShowQueryOptions> showQueryOptionsHashMap = initializeMap();
    
    private static Map<String, ShowQueryOptions> initializeMap() {
	    Map<String, ShowQueryOptions> showQueryOptionsHashMap = new HashMap<>();
        for(ShowQueryOptions showQueryOptions : ShowQueryOptions.values()) {
	    	showQueryOptionsHashMap.put(showQueryOptions.toString(), showQueryOptions);
	    }

        return showQueryOptionsHashMap;
    }
    
    private final String value;
	
    ShowQueryOptions(String value) {
        this.value = value;
    }
	
    static ShowQueryOptions fromServiceSerializedFormat(String showQueryOptions) {
        return showQueryOptionsHashMap.get(showQueryOptions);
    }

    @JsonValue
    @Override
    public String toString() {
        return this.value;
    }
}
