// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


package com.azure.cosmos.benchmark;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

public class PojoizedJson {
    private final Map<String, Object> instanceProps = new HashMap<>();

    @JsonAnyGetter
    public Map<String, Object> getInstance() {
        return instanceProps;
    }

    @JsonAnySetter
    public void setProperty(String name, Object value) {
        this.instanceProps.put(name, value);
    }

    @JsonIgnore
    public String getId() {
        return (String) instanceProps.get("id");
    }

    @JsonIgnore
    public Object getProperty(String propName) {
        return instanceProps.get(propName);
    }
}
