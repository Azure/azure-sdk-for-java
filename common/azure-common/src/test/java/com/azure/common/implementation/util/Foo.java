/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.common.implementation.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.azure.common.implementation.serializer.JsonFlatten;

import java.util.List;
import java.util.Map;

/**
 * Class for testing serialization.
 */
@JsonFlatten
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "$type")
@JsonTypeName("foo")
@JsonSubTypes({
        @JsonSubTypes.Type(name = "foochild", value = FooChild.class)
})
public class Foo {
    @JsonProperty(value = "properties.bar")
    public String bar;
    @JsonProperty(value = "properties.props.baz")
    public List<String> baz;
    @JsonProperty(value = "properties.props.q.qux")
    public Map<String, String> qux;
    @JsonProperty(value = "properties.more\\.props")
    public String moreProps;
    @JsonProperty(value = "props.empty")
    public Integer empty;
    @JsonProperty(value = "")
    public Map<String, Object> additionalProperties;
}