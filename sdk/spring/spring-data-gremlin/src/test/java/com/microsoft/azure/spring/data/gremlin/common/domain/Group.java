// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.data.gremlin.common.domain;

import com.microsoft.azure.spring.data.gremlin.annotation.Edge;
import com.microsoft.azure.spring.data.gremlin.annotation.EdgeFrom;
import com.microsoft.azure.spring.data.gremlin.annotation.EdgeTo;
import com.microsoft.azure.spring.data.gremlin.annotation.GeneratedValue;
import org.springframework.data.annotation.Id;

@Edge
public class Group {

    @Id
    @GeneratedValue
    private Long id;

    @EdgeFrom
    private Student student;

    @EdgeTo
    private GroupOwner groupOwner;

    public Group(Student student, GroupOwner groupOwner) {
        this.student = student;
        this.groupOwner = groupOwner;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public GroupOwner getGroupOwner() {
        return groupOwner;
    }

    public void setGroupOwner(GroupOwner groupOwner) {
        this.groupOwner = groupOwner;
    }
}
