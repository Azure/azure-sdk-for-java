// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.data.gremlin.web.service.domain;

import com.azure.spring.data.gremlin.annotation.Edge;
import com.azure.spring.data.gremlin.annotation.EdgeFrom;
import com.azure.spring.data.gremlin.annotation.EdgeTo;
import org.springframework.data.annotation.Id;

import java.util.HashMap;
import java.util.Map;

@Edge
public class ServicesDataFlow {

    @Id
    private String id;

    @EdgeFrom
    private MicroService serviceFrom;

    @EdgeTo
    private MicroService serviceTo;

    private Map<String, Object> properties = new HashMap<>();

    public ServicesDataFlow() {
    }

    public ServicesDataFlow(String id, MicroService serviceFrom, MicroService serviceTo,
                            Map<String, Object> properties) {
        this.id = id;
        this.serviceFrom = serviceFrom;
        this.serviceTo = serviceTo;
        this.properties = properties;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MicroService getServiceFrom() {
        return serviceFrom;
    }

    public void setServiceFrom(MicroService serviceFrom) {
        this.serviceFrom = serviceFrom;
    }

    public MicroService getServiceTo() {
        return serviceTo;
    }

    public void setServiceTo(MicroService serviceTo) {
        this.serviceTo = serviceTo;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
}

