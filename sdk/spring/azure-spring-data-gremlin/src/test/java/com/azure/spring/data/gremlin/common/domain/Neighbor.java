// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.common.domain;

import com.azure.spring.data.gremlin.annotation.Edge;
import com.azure.spring.data.gremlin.annotation.EdgeFrom;
import com.azure.spring.data.gremlin.annotation.EdgeTo;

@Edge
public class Neighbor {

    private Long id;

    private Long distance;

    @EdgeFrom
    private Student studentFrom;

    @EdgeTo
    private Student studentTo;

    public Neighbor() {
    }

    public Neighbor(Long id, Long distance, Student studentFrom, Student studentTo) {
        this.id = id;
        this.distance = distance;
        this.studentFrom = studentFrom;
        this.studentTo = studentTo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDistance() {
        return distance;
    }

    public void setDistance(Long distance) {
        this.distance = distance;
    }

    public Student getStudentFrom() {
        return studentFrom;
    }

    public void setStudentFrom(Student studentFrom) {
        this.studentFrom = studentFrom;
    }

    public Student getStudentTo() {
        return studentTo;
    }

    public void setStudentTo(Student studentTo) {
        this.studentTo = studentTo;
    }
}
