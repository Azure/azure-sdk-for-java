// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.childresource;

import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.ExternalChildResourcesCachedImpl;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.dag.TaskGroup;
import com.azure.resourcemanager.resources.fluentcore.dag.TaskItem;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * Type representing top level school collection using which
 * 1. new schools can be created with associated teachers and students
 * 2. new teachers and students can be created independently.
 */
class SchoolsImpl {
    private IndependentTeachersImpl independentTeachers;
    private IndependentStudentsImpl independentStudents;

    SchoolsImpl() {
        this.independentTeachers = new IndependentTeachersImpl();
        this.independentStudents = new IndependentStudentsImpl();
    }

    public SchoolImpl define(String name) {
        return new SchoolImpl(name);
    }

    // Accessor to teachers collection from schools that can be created independently
    //
    public IndependentTeachersImpl independentTeachers() {
        return this.independentTeachers;
    }

    // Accessor to students collection from schools that can be created independently
    //
    public IndependentStudentsImpl independentStudents() {
        return this.independentStudents;
    }

    /**
     * Type representing an instance of School.
     */
    class SchoolImpl implements TaskItem, Indexable {
        private final TaskGroup taskGroup;
        private final String name;
        private final String key;
        private boolean isInvoked;
        private InlineTeachersImpl inlineTeachers;
        private IndependentTeachersImpl independentTeachers;
        private InlineStudentsImpl inlineStudents;
        private IndependentStudentsImpl independentStudents;

        SchoolImpl(String name) {
            this.name = name;
            this.key = name;
            this.taskGroup = new TaskGroup(this.key, this);
            this.inlineTeachers = new InlineTeachersImpl(this, this.taskGroup, "InlineTeacher");
            this.independentTeachers = new IndependentTeachersImpl();

            this.inlineStudents = new InlineStudentsImpl(this, this.taskGroup, "InlineStudent");
            this.independentStudents = new IndependentStudentsImpl();
        }

        public boolean isInvoked() {
            return this.isInvoked;
        }

        @Override
        public String key() {
            return this.key;
        }

        public SchoolImpl withAddress(String address) {
            return this;
        }

        // Define an inline teacher that can be associated to school by invoking attach()
        //
        public TeacherImpl defineTeacher(String name) {
            return this.inlineTeachers.define(name);
        }

        // Teachers in the school that can be created and updated independently
        //
        public IndependentTeachersImpl independentTeachers() {
            return this.independentTeachers;
        }

        // Define an inline student that can be associated to school by invoking attach()
        //
        public StudentImpl defineStudent(String name) {
            return this.inlineStudents.define(name);
        }

        // Students in the school that can be created and updated independently
        //
        public IndependentStudentsImpl independentStudents() {
            return this.independentStudents;
        }

        public Flux<Indexable> createAsync() {
            return taskGroup.invokeAsync(this.taskGroup.newInvocationContext());
        }

        @Override
        public Indexable result() {
            return this;
        }

        @Override
        public void beforeGroupInvoke() {
            // NOP
        }

        @Override
        public boolean isHot() {
            return true;
        }

        @Override
        public Mono<Indexable> invokeAsync(TaskGroup.InvocationContext context) {
            return Mono.<Indexable>just(this)
                    .map(indexable -> {
                        isInvoked = true;
                        return indexable;
                    });
        }

        @Override
        public Mono<Void> invokeAfterPostRunAsync(boolean isGroupFaulted) {
            return Mono.empty();
        }

        public SchoolImpl withTeacher(TeacherImpl teacher) {
            this.inlineTeachers.withTeacher(teacher);
            return this;
        }

        public SchoolImpl withStudent(StudentImpl student) {
            this.inlineStudents.withStudent(student);
            return this;
        }
    }

    /**
     * Type representing an instance of Teacher.
     */
    class TeacherImpl
            extends ExternalChildResourceImpl<TeacherImpl, Object, SchoolImpl, Object>
            implements TaskGroup.HasTaskGroup, Creatable<TeacherImpl> {

        private boolean isInvoked;

        protected TeacherImpl(String name, SchoolImpl parent, Object inner) {
            super(name, name, parent, inner);
        }

        TeacherImpl withSubject(String subjectName) {
            return this;
        }

        @Override
        public String id() {
            return name();
        }

        public boolean isInvoked() {
            return this.isInvoked;
        }

        @Override
        public Mono<TeacherImpl> createResourceAsync() {
            return Mono.just(this)
                    .map(teacher -> {
                        isInvoked = true;
                        return teacher;
                    });
        }

        @Override
        public Mono<TeacherImpl> updateResourceAsync() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Mono<Void> deleteResourceAsync() {
            throw new UnsupportedOperationException();
        }

        @Override
        protected Mono<Object> getInnerAsync() {
            throw new UnsupportedOperationException();
        }

        public SchoolImpl attach() {
            return this.parent().withTeacher(this);
        }
    }

    /**
     * Type representing Teacher collection where a teacher entries can be created as part of parent School.
     */
    class InlineTeachersImpl extends ExternalChildResourcesCachedImpl<TeacherImpl, TeacherImpl, Object, SchoolImpl, Object> {

        protected InlineTeachersImpl(SchoolImpl parent, TaskGroup parentTaskGroup, String childResourceName) {
            super(parent, parentTaskGroup, childResourceName);
        }

        public TeacherImpl define(String name) {
            return super.prepareInlineDefine(name);
        }

        TeacherImpl findTeacher(String teacherName) {
            return this.find(teacherName);
        }

        @Override
        protected Flux<TeacherImpl> listChildResourcesAsync() {
            return Flux.empty();
        }

        @Override
        protected List<TeacherImpl> listChildResources() {
            return new ArrayList<>();
        }

        @Override
        protected TeacherImpl newChildResource(String name) {
            return new TeacherImpl(name, this.getParent(), null);
        }

        public void withTeacher(TeacherImpl teacher) {
            this.addChildResource(teacher);
        }
    }

    /**
     * Type representing Teacher collection where a teacher entries can be created independently.
     */
    class IndependentTeachersImpl {
        public TeacherImpl define(String name) {
            TeacherImpl newTeacher = new TeacherImpl(name, null, null);
            newTeacher.setPendingOperation(ExternalChildResourceImpl.PendingOperation.ToBeCreated);
            return newTeacher;
        }
    }

    /**
     * Type representing an instance of Student.
     */
    class StudentImpl
            extends ExternalChildResourceImpl<StudentImpl, Object, SchoolImpl, Object>
            implements TaskGroup.HasTaskGroup, Creatable<StudentImpl> {
        private String teacherName;
        private boolean isInvoked;

        protected StudentImpl(String name, SchoolImpl parent, Object inner) {
            super(name, name, parent, inner);
        }

        StudentImpl withAge(int age) {
            return this;
        }

        StudentImpl withTeacher(String teacherRefName) {
            this.teacherName = teacherRefName;
            TaskGroup.HasTaskGroup teacher = this.parent().inlineTeachers.findTeacher(teacherRefName);
            if (teacher == null) {
                throw new IllegalStateException("Expected teacher not found in the inline collection");
            }
            this.addDependency(teacher);
            return this;
        }

        StudentImpl withTeacher(Creatable<TeacherImpl> newTeacher) {
            this.teacherName = newTeacher.name();
            this.addDependency(newTeacher);
            return this;
        }

        @Override
        public String id() {
            return name();
        }

        public boolean isInvoked() {
            return this.isInvoked;
        }

        @Override
        public Mono<StudentImpl> createResourceAsync() {
            Indexable teacher = this.taskGroup().taskResult(teacherName);
            if (teacher == null) {
                throw new IllegalStateException("Expected dependency teacher not found");
            }
            TeacherImpl teacherImpl = (TeacherImpl) teacher;
            if (teacherImpl == null) {
                throw new IllegalStateException("Casting Indexable to teacherImpl failed");
            }
            if (!teacherImpl.isInvoked()) {
                throw new IllegalStateException("teacherImpl.isInvoked() should be true");
            }

            return Mono.just(this).map(student -> {
                isInvoked = true;
                return student;
            });
        }

        @Override
        public Mono<StudentImpl> updateResourceAsync() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Mono<Void> deleteResourceAsync() {
            throw new UnsupportedOperationException();
        }

        @Override
        protected Mono<Object> getInnerAsync() {
            throw new UnsupportedOperationException();
        }

        public SchoolImpl attach() {
            return this.parent().withStudent(this);
        }
    }

    /**
     * Type representing Student collection where a student entries can be created as part of parent School.
     */
    class InlineStudentsImpl extends ExternalChildResourcesCachedImpl<StudentImpl, StudentImpl, Object, SchoolImpl, Object> {

        protected InlineStudentsImpl(SchoolImpl parent, TaskGroup parentTaskGroup, String childResourceName) {
            super(parent, parentTaskGroup, childResourceName);
        }

        public StudentImpl define(String name) {
            return super.prepareInlineDefine(name);
        }

        StudentImpl findStudent(String studentName) {
            return this.find(studentName);
        }

        @Override
        protected Flux<StudentImpl> listChildResourcesAsync() {
            return Flux.empty();
        }

        @Override
        protected List<StudentImpl> listChildResources() {
            return new ArrayList<>();
        }

        @Override
        protected StudentImpl newChildResource(String name) {
            return new StudentImpl(name, this.getParent(), null);
        }

        public void withStudent(StudentImpl student) {
            this.addChildResource(student);
        }
    }

    /**
     * Type representing Student collection where a student entries can be created independently.
     */
    class IndependentStudentsImpl {
        public StudentImpl define(String name) {
            StudentImpl newStudent = new StudentImpl(name, null, null);
            newStudent.setPendingOperation(ExternalChildResourceImpl.PendingOperation.ToBeCreated);
            return newStudent;
        }
    }
}
