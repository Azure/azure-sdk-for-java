// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.spring.data.gremlin.common.domain;

import com.microsoft.spring.data.gremlin.annotation.Edge;
import com.microsoft.spring.data.gremlin.annotation.EdgeFrom;
import com.microsoft.spring.data.gremlin.annotation.EdgeTo;
import com.microsoft.spring.data.gremlin.annotation.GeneratedValue;
import org.springframework.data.annotation.Id;

import java.util.Objects;

@Edge
public class Group {

    @Id
    @GeneratedValue
    private String id;

    @EdgeFrom
    private Student student;

    @EdgeTo
    private GroupOwner groupOwner;

    public Group() {
    }

    public Group(Student student, GroupOwner groupOwner) {
        this.student = student;
        this.groupOwner = groupOwner;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Group group = (Group) o;
        return Objects.equals(id, group.id)
            && Objects.equals(student, group.student)
            && Objects.equals(groupOwner, group.groupOwner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, student, groupOwner);
    }
}
