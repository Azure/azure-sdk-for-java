// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.core.v2.annotation.JsonFlatten;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.List;
import java.util.Map;

/**
 * Class for testing serialization.
 */
@JsonFlatten
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "$type")
@JsonTypeName("foo")
@JsonSubTypes({ @JsonSubTypes.Type(name = "foochild", value = FooChild.class) })
public class Foo {
    @JsonProperty(value = "properties.bar")
    private String bar;
    @JsonProperty(value = "properties.props.baz")
    private List<String> baz;
    @JsonProperty(value = "properties.props.q.qux")
    private Map<String, String> qux;
    @JsonProperty(value = "properties.more\\.props")
    private String moreProps;
    @JsonProperty(value = "props.empty")
    private Integer empty;
    @JsonProperty(value = "")
    private Map<String, Object> additionalProperties;

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

    public Map<String, Object> additionalProperties() {
        return additionalProperties;
    }

    public void additionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }
}
