// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.integration;

import com.azure.spring.data.cosmos.domain.CourseWithEtag;
import com.azure.spring.data.cosmos.exception.CosmosAccessException;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.ReactiveCourseWithEtagRepository;
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
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static com.azure.spring.data.cosmos.common.TestConstants.COURSE_NAME;
import static com.azure.spring.data.cosmos.common.TestConstants.DEPARTMENT;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class ReactiveEtagIT {

    @Autowired
    ReactiveCourseWithEtagRepository reactiveCourseWithEtagRepository;

    @After
    public void cleanup() {
        reactiveCourseWithEtagRepository.deleteAll().block();
    }

    private static CourseWithEtag createCourseWithEtag() {
        return new CourseWithEtag(UUID.randomUUID().toString(), COURSE_NAME, DEPARTMENT);
    }

    @Test
    public void testCrudOperationsShouldApplyEtag() {
        final Mono<CourseWithEtag> insertedCourseWithEtagMono =
            reactiveCourseWithEtagRepository.save(createCourseWithEtag());
        CourseWithEtag insertedCourseWithEtag = insertedCourseWithEtagMono.block();
        Assert.assertNotNull(insertedCourseWithEtag);
        Assert.assertNotNull(insertedCourseWithEtag.getEtag());

        insertedCourseWithEtag.setName("CHANGED");
        final Mono<CourseWithEtag> updatedCourseWithEtagMono =
            reactiveCourseWithEtagRepository.save(insertedCourseWithEtag);
        CourseWithEtag updatedCourseWithEtag = updatedCourseWithEtagMono.block();
        Assert.assertNotNull(updatedCourseWithEtag);
        Assert.assertNotNull(updatedCourseWithEtag.getEtag());
        Assert.assertNotEquals(updatedCourseWithEtag.getEtag(), insertedCourseWithEtag.getEtag());

        final Mono<CourseWithEtag> foundCourseWithEtagMono =
            reactiveCourseWithEtagRepository.findById(insertedCourseWithEtag.getCourseId());
        CourseWithEtag foundCourseWithEtag = foundCourseWithEtagMono.block();
        Assert.assertNotNull(foundCourseWithEtag);
        Assert.assertNotNull(foundCourseWithEtag.getEtag());
        Assert.assertEquals(foundCourseWithEtag.getEtag(), updatedCourseWithEtag.getEtag());
    }

    @Test
    public void testCrudListOperationsShouldApplyEtag() {
        final List<CourseWithEtag> courses = new ArrayList<>();
        courses.add(createCourseWithEtag());
        courses.add(createCourseWithEtag());

        final Flux<CourseWithEtag> insertedCourseWithEtagsFlux = reactiveCourseWithEtagRepository.saveAll(courses);
        List<CourseWithEtag> insertedCourseWithEtags = insertedCourseWithEtagsFlux.collectList().block();

        Assert.assertNotNull(insertedCourseWithEtags);
        insertedCourseWithEtags.forEach(course -> Assert.assertNotNull(course.getEtag()));

        insertedCourseWithEtags.forEach(course -> course.setName("CHANGED"));
        final Flux<CourseWithEtag> updatedCourseWithEtagsFlux =
            reactiveCourseWithEtagRepository.saveAll(insertedCourseWithEtags);
        List<CourseWithEtag> updatedCourseWithEtags = updatedCourseWithEtagsFlux.collectList().block();

        Assert.assertNotNull(updatedCourseWithEtags);

        insertedCourseWithEtags.sort(Comparator.comparing(CourseWithEtag::getCourseId));
        updatedCourseWithEtags.sort(Comparator.comparing(CourseWithEtag::getCourseId));

        for (int i = 0; i < updatedCourseWithEtags.size(); i++) {
            CourseWithEtag insertedCourseWithEtag = insertedCourseWithEtags.get(i);
            CourseWithEtag updatedCourseWithEtag = updatedCourseWithEtags.get(i);
            Assert.assertEquals(insertedCourseWithEtag.getCourseId(), updatedCourseWithEtag.getCourseId());
            Assert.assertNotNull(updatedCourseWithEtag.getEtag());
            Assert.assertNotEquals(insertedCourseWithEtag.getEtag(), updatedCourseWithEtag.getEtag());
        }
    }

    @Test
    public void testShouldFailIfEtagDoesNotMatch() {
        Mono<CourseWithEtag> insertedCourseWithEtagMono = reactiveCourseWithEtagRepository.save(createCourseWithEtag());
        CourseWithEtag insertedCourseWithEtag = insertedCourseWithEtagMono.block();
        Assert.assertNotNull(insertedCourseWithEtag);

        insertedCourseWithEtag.setName("CHANGED");
        Mono<CourseWithEtag> updatedCourseWithEtagMono = reactiveCourseWithEtagRepository.save(insertedCourseWithEtag);
        CourseWithEtag updatedCourseWithEtag = updatedCourseWithEtagMono.block();
        Assert.assertNotNull(updatedCourseWithEtag);
        updatedCourseWithEtag.setEtag(insertedCourseWithEtag.getEtag());


        Mono<CourseWithEtag> courseMono = reactiveCourseWithEtagRepository.save(updatedCourseWithEtag);
        StepVerifier.create(courseMono).verifyError(CosmosAccessException.class);

        Mono<Void> deleteMono = reactiveCourseWithEtagRepository.delete(updatedCourseWithEtag);
        StepVerifier.create(deleteMono).verifyError(CosmosAccessException.class);
    }

}
