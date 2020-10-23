// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.data.gremlin.domain;

import com.azure.spring.data.gremlin.annotation.Edge;
import com.azure.spring.data.gremlin.annotation.EdgeFrom;
import com.azure.spring.data.gremlin.annotation.EdgeTo;
import org.springframework.data.annotation.Id;

@Edge
public class Relation {

    @Id
    private String id;

    private String name;

    @EdgeFrom
    private Person personFrom;

    @EdgeTo
    private Person personTo;

    public Relation() {
    }

    public Relation(String id, String name, Person personFrom, Person personTo) {
        this.id = id;
        this.name = name;
        this.personFrom = personFrom;
        this.personTo = personTo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Person getPersonFrom() {
        return personFrom;
    }

    public void setPersonFrom(Person personFrom) {
        this.personFrom = personFrom;
    }

    public Person getPersonTo() {
        return personTo;
    }

    public void setPersonTo(Person personTo) {
        this.personTo = personTo;
    }
}

