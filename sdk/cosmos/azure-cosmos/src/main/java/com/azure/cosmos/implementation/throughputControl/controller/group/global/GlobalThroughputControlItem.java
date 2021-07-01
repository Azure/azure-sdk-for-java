// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.controller.group.global;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class GlobalThroughputControlItem {
    @JsonProperty(value = "id", required = true)
    private String id;

    @JsonProperty(value = "groupId", required = true)
    private String groupId;

    @JsonProperty(value = "_etag")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String etag;

    @JsonProperty(value = "ttl")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer ttl;

    public GlobalThroughputControlItem() {

    }

    public GlobalThroughputControlItem(String id, String partitionKeyValue) {
        this.id = id;
        this.groupId = partitionKeyValue;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
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
