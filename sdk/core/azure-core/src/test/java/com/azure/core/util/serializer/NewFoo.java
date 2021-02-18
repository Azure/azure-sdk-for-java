// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import com.azure.core.annotation.JsonFlatten;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class for testing serialization.
 */
@JsonFlatten
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "$type")
@JsonTypeName("newfoo")
@JsonSubTypes({
    @JsonSubTypes.Type(name = "newfoochild", value = NewFooChild.class)
})
public class NewFoo {
    @JsonProperty(value = "properties.bar")
    private String bar;
    @JsonProperty(value = "properties.props.baz")
    private List<String>  baz;
    @JsonProperty(value = "properties.props.q.qux")
    private Map<String, String> qux;
    @JsonProperty(value = "properties.more\\.props")
    private String moreProps;
    @JsonProperty(value = "props.empty")
    private Integer empty;
    @JsonIgnore
    private Map<String, Object> additionalProperties;
    @JsonProperty(value = "additionalProperties")
    private Map<String, Object> additionalPropertiesProperty;

    public String bar() {
        return bar;
    }

    public void bar(String bar) {
        this.bar = bar;
    }

    public List<String> baz() {
        return baz;
    }

    public void baz(List<String> baz) {
        this.baz = baz;
    }

    public Map<String, String> qux() {
        return qux;
    }

    public void qux(Map<String, String> qux) {
        this.qux = qux;
    }

    public String moreProps() {
        return moreProps;
    }

    public void moreProps(String moreProps) {
        this.moreProps = moreProps;
    }

    public Integer empty() {
        return empty;
    }

    public void empty(Integer empty) {
        this.empty = empty;
    }

    @JsonAnySetter
    private void additionalProperties(String key, Object value) {
        if (additionalProperties == null) {
            additionalProperties = new HashMap<>();
        }
        additionalProperties.put(key.replace("\\.", "."), value);
    }

    @JsonAnyGetter
    public Map<String, Object> additionalProperties() {
        return additionalProperties;
    }

    public void additionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    public Map<String, Object> additionalPropertiesProperty() {
        return additionalPropertiesProperty;
    }

    public void additionalPropertiesProperty(Map<String, Object> additionalPropertiesProperty) {
        this.additionalPropertiesProperty = additionalPropertiesProperty;
    }
}
