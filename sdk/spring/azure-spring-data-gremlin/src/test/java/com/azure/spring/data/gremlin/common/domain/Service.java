// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.common.domain;

import com.azure.spring.data.gremlin.annotation.Vertex;
import org.springframework.data.annotation.Id;

import java.util.Date;
import java.util.Map;

@Vertex
public class Service {

    @Id
    private String id;

    private int instanceCount;

    private boolean active;

    private String name;

    private ServiceType type;

    private Date createAt;

    private Map<String, Object> properties;

    public Service() {
    }

    public Service(String id, int instanceCount, boolean active, String name, ServiceType type, Date createAt, Map<String, Object> properties) {
        this.id = id;
        this.instanceCount = instanceCount;
        this.active = active;
        this.name = name;
        this.type = type;
        this.createAt = createAt;
        this.properties = properties;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getInstanceCount() {
        return instanceCount;
    }

    public void setInstanceCount(int instanceCount) {
        this.instanceCount = instanceCount;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ServiceType getType() {
        return type;
    }

    public void setType(ServiceType type) {
        this.type = type;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
}
