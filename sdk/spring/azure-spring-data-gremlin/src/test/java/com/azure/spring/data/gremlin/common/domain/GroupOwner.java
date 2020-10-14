// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.common.domain;

import com.azure.spring.data.gremlin.annotation.Vertex;
import org.springframework.data.annotation.Id;

@Vertex
public class GroupOwner {

    @Id
    private String name;

    private Integer expireDays;

    public GroupOwner() {
    }

    public GroupOwner(String name, Integer expireDays) {
        this.name = name;
        this.expireDays = expireDays;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getExpireDays() {
        return expireDays;
    }

    public void setExpireDays(Integer expireDays) {
        this.expireDays = expireDays;
    }
}
