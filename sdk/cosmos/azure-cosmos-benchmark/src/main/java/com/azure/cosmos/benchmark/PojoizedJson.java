// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


package com.azure.cosmos.benchmark;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.Map;

public class PojoizedJson {
    private final Map<String, String> instanceProps = new HashMap<>();

    @JsonAnyGetter
    public Map<String, String> getInstance() {
        return instanceProps;
    }

    @JsonAnySetter
    public void setProperty(String name, String value) {
        this.instanceProps.put(name, value);
    }

    @JsonIgnore
    public String getId() {
        return instanceProps.get("id");
    }

    @JsonIgnore
    public String getProperty(String propName) {
        return instanceProps.get(propName);
    }
}
