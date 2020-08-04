// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.integration;

import com.azure.spring.data.cosmos.domain.Course;
import com.azure.spring.data.cosmos.exception.CosmosAccessException;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.ReactiveCourseRepository;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.azure.spring.data.cosmos.common.TestConstants.COURSE_NAME;
import static com.azure.spring.data.cosmos.common.TestConstants.DEPARTMENT;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class ReactiveEtagIT {

    @Autowired
    ReactiveCourseRepository courseRepository;

    @After
    public void cleanup() {
        courseRepository.deleteAll();
    }

    private static Course createCourse() {
        return new Course(UUID.randomUUID().toString(), COURSE_NAME, DEPARTMENT);
    }

    @Test
    public void testCrudOperationsShouldApplyEtag() {
        final Mono<Course> insertedCourseMono = courseRepository.save(createCourse());
        Course insertedCourse = insertedCourseMono.block();
        Assert.assertNotNull(insertedCourse);
        Assert.assertNotNull(insertedCourse.getEtag());

        insertedCourse.setName("CHANGED");
        final Mono<Course> updatedCourseMono = courseRepository.save(insertedCourse);
        Course updatedCourse = updatedCourseMono.block();
        Assert.assertNotNull(updatedCourse);
        Assert.assertNotNull(updatedCourse.getEtag());
        Assert.assertNotEquals(updatedCourse.getEtag(), insertedCourse.getEtag());

        final Mono<Course> foundCourseMono = courseRepository.findById(insertedCourse.getCourseId());
        Course foundCourse = foundCourseMono.block();
        Assert.assertNotNull(foundCourse);
        Assert.assertNotNull(foundCourse.getEtag());
        Assert.assertEquals(foundCourse.getEtag(), updatedCourse.getEtag());
    }

    @Test
    public void testCrudListOperationsShouldApplyEtag() {
        final List<Course> courses = new ArrayList<>();
        courses.add(createCourse());
        courses.add(createCourse());

        final Flux<Course> insertedCoursesFlux = courseRepository.saveAll(courses);
        List<Course> insertedCourses = insertedCoursesFlux.collectList().block();

        Assert.assertNotNull(insertedCourses);
        insertedCourses.forEach(course -> Assert.assertNotNull(course.getEtag()));

        insertedCourses.forEach(course -> course.setName("CHANGED"));
        final Flux<Course> updatedCoursesFlux = courseRepository.saveAll(insertedCourses);
        List<Course> updatedCourses = updatedCoursesFlux.collectList().block();

        Assert.assertNotNull(updatedCourses);
        for (int i = 0; i < updatedCourses.size(); i++) {
            Course insertedCourse = insertedCourses.get(i);
            Course updatedCourse = updatedCourses.get(i);
            Assert.assertEquals(insertedCourse.getCourseId(), updatedCourse.getCourseId());
            Assert.assertNotNull(updatedCourse.getEtag());
            Assert.assertNotEquals(insertedCourse.getEtag(), updatedCourse.getEtag());
        }
    }

    @Test
    public void testShouldFailIfEtagDoesNotMatch() {
        Mono<Course> insertedCourseMono = courseRepository.save(createCourse());
        Course insertedCourse = insertedCourseMono.block();
        Assert.assertNotNull(insertedCourse);

        insertedCourse.setName("CHANGED");
        Mono<Course> updatedCourseMono = courseRepository.save(insertedCourse);
        Course updatedCourse = updatedCourseMono.block();
        Assert.assertNotNull(updatedCourse);
        updatedCourse.setEtag(insertedCourse.getEtag());


        Mono<Course> courseMono = courseRepository.save(updatedCourse);
        StepVerifier.create(courseMono).verifyError(CosmosAccessException.class);

        Mono<Void> deleteMono = courseRepository.delete(updatedCourse);
        StepVerifier.create(deleteMono).verifyError(CosmosAccessException.class);
    }

}
