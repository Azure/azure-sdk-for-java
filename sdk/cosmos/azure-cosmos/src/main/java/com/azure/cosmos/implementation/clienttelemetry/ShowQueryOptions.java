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
	 * Show query parameters only.
	 */
	PARAMETERIZED_ONLY("ParameterizedOnly"),
	
	/**
	 *  Show query parameters and non parameters
	 */
	PARAMETERIZED_AND_NON_PARAMETERIZED("ParameterizedAndNonParameterized");
    
    private static Map<String, ShowQueryOptions> showQueryOptionsHashMap = new HashMap<>();
    
    static {
	    for(ShowQueryOptions showQueryOptions : ShowQueryOptions.values()) {
	    	showQueryOptionsHashMap.put(showQueryOptions.toString(), showQueryOptions);
	    }
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
