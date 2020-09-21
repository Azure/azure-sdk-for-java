// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.gremlin;

import com.microsoft.spring.data.gremlin.annotation.Vertex;
import org.springframework.data.annotation.Id;

@Vertex
public class Person {

    @Id
    private String id;

    private String name;

    private int level;

    public Person() {

    }

    public Person(String id, String name, int level) {
        this.id = id;
        this.name = name;
        this.level = level;
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

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public String toString() {
        return "Person{" +
            "id='" + id + '\'' +
            ", name='" + name + '\'' +
            ", level=" + level +
            '}';
    }
}
