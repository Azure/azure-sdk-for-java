// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.domain;

import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import org.springframework.data.annotation.Id;

import java.util.Objects;

@Container
public class Course {

    @Id
    private String courseId;
    private String name;
    @PartitionKey
    private String department;

    public Course(String courseId, String name, String department) {
        this.courseId = courseId;
        this.name = name;
        this.department = department;
    }

    public Course() {
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Course course = (Course) o;
        return courseId.equals(course.courseId)
            && name.equals(course.name)
            && department.equals(course.department);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseId, name, department);
    }

    @Override
    public String toString() {
        return "Course{"
            + "courseId='"
            + courseId
            + '\''
            + ", name='"
            + name
            + '\''
            + ", department='"
            + department
            + '\''
            + '}';
    }
}
