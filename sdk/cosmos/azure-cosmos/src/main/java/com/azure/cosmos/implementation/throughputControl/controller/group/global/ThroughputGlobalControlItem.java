// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.controller.group.global;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class ThroughputGlobalControlItem {
    @JsonProperty(value = "id", required = true)
    private String id;

    @JsonProperty(value = "group", required = true)
    private String group;

    @JsonProperty(value = "_etag")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String etag;

    @JsonProperty(value = "ttl")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer ttl;

    public ThroughputGlobalControlItem() {

    }

    public ThroughputGlobalControlItem(String id, String partitionKeyValue) {
        this.id = id;
        this.group = partitionKeyValue;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public Integer getTtl() {
        return ttl;
    }

    public void setTtl(Integer ttl) {
        this.ttl = ttl;
    }
}
