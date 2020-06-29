// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin;

import com.microsoft.spring.data.gremlin.annotation.Vertex;
import org.springframework.data.annotation.Id;


@Vertex
public class Person {

    @Id
    private String id;

    private String name;

    private String age;

    public Person() {

    }

    public Person(String id, String name, String age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }
}
