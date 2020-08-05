// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.integration;

import com.azure.cosmos.models.PartitionKey;
import com.azure.spring.data.cosmos.core.ReactiveCosmosTemplate;
import com.azure.spring.data.cosmos.domain.Course;
import com.azure.spring.data.cosmos.exception.CosmosAccessException;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.ReactiveCourseRepository;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class ReactiveCourseRepositoryIT {

    private static final String COURSE_ID_1 = "1";
    private static final String COURSE_ID_2 = "2";
    private static final String COURSE_ID_3 = "3";
    private static final String COURSE_ID_4 = "4";
    private static final String COURSE_ID_5 = "5";

    private static final String COURSE_NAME_1 = "Course1";
    private static final String COURSE_NAME_2 = "Course2";
    private static final String COURSE_NAME_3 = "Course3";
    private static final String COURSE_NAME_4 = "Course4";
    private static final String COURSE_NAME_5 = "Course5";

    private static final String DEPARTMENT_NAME_1 = "Department1";
    private static final String DEPARTMENT_NAME_2 = "Department2";
    private static final String DEPARTMENT_NAME_3 = "Department3";

    private static final Course COURSE_1 = new Course(COURSE_ID_1, COURSE_NAME_1, DEPARTMENT_NAME_3);
    private static final Course COURSE_2 = new Course(COURSE_ID_2, COURSE_NAME_2, DEPARTMENT_NAME_2);
    private static final Course COURSE_3 = new Course(COURSE_ID_3, COURSE_NAME_3, DEPARTMENT_NAME_2);
    private static final Course COURSE_4 = new Course(COURSE_ID_4, COURSE_NAME_4, DEPARTMENT_NAME_1);
    private static final Course COURSE_5 = new Course(COURSE_ID_5, COURSE_NAME_5, DEPARTMENT_NAME_1);

    private static final CosmosEntityInformation<Course, String> entityInformation =
        new CosmosEntityInformation<>(Course.class);

    private static ReactiveCosmosTemplate staticTemplate;
    private static boolean isSetupDone;

    @Autowired
    private ReactiveCosmosTemplate template;

    @Autowired
    private ReactiveCourseRepository repository;

    @Before
    public void setUp() {
        if (!isSetupDone) {
            staticTemplate = template;
            template.createContainerIfNotExists(entityInformation);
        }
        final Flux<Course> savedFlux = repository.saveAll(Arrays.asList(COURSE_1, COURSE_2,
            COURSE_3, COURSE_4));
        StepVerifier.create(savedFlux).thenConsumeWhile(course -> true).expectComplete().verify();
        isSetupDone = true;
    }

    @After
    public void cleanup() {
        final Mono<Void> deletedMono = repository.deleteAll();
        StepVerifier.create(deletedMono).thenAwait().verifyComplete();
    }

    @AfterClass
    public static void afterClassCleanup() {
        staticTemplate.deleteContainer(entityInformation.getContainerName());
    }

    @Test
    public void testFindById() {
        final Mono<Course> idMono = repository.findById(COURSE_ID_4);
        StepVerifier.create(idMono).expectNext(COURSE_4).expectComplete().verify();
    }

    @Test
    public void testFindByIdAndPartitionKey() {
        final Mono<Course> idMono = repository.findById(COURSE_ID_4,
            new PartitionKey(entityInformation.getPartitionKeyFieldValue(COURSE_4)));
        StepVerifier.create(idMono).expectNext(COURSE_4).expectComplete().verify();
    }

    @Test
    public void testFindByIdAsPublisher() {
        final Mono<Course> byId = repository.findById(Mono.just(COURSE_ID_1));
        StepVerifier.create(byId).expectNext(COURSE_1).verifyComplete();
    }

    @Test
    public void testFindAllWithSort() {
        final Flux<Course> sortAll = repository.findAll(Sort.by(Sort.Order.desc("name")));
        StepVerifier.create(sortAll).expectNext(COURSE_4, COURSE_3, COURSE_2, COURSE_1).verifyComplete();
    }

    @Test
    public void testFindByIdNotFound() {
        final Mono<Course> idMono = repository.findById("10");
        //  Expect an empty mono as return value
        StepVerifier.create(idMono).expectComplete().verify();
    }

    @Test
    public void testFindByIdAndPartitionKeyNotFound() {
        final Mono<Course> idMono = repository.findById("10",
            new PartitionKey(entityInformation.getPartitionKeyFieldValue(COURSE_1)));
        //  Expect an empty mono as return value
        StepVerifier.create(idMono).expectComplete().verify();
    }

    @Test
    public void testFindAll() {
        final Flux<Course> allFlux = repository.findAll();
        StepVerifier.create(allFlux).expectNextCount(4).verifyComplete();
    }

    @Test
    public void testFindOneShouldFailIfMultipleResultsReturned() {
        final Course course = new Course("unusedId", COURSE_1.getName(), COURSE_1.getDepartment());
        final Mono<Course> saveSecond = repository.save(course);
        StepVerifier.create(saveSecond).expectNext(course).verifyComplete();

        final Mono<Course> find = repository.findOneByName(COURSE_1.getName());
        StepVerifier.create(find).expectError(CosmosAccessException.class).verify();
    }

    @Test
    public void testShouldFindSingleEntity() {
        final Mono<Course> find = repository.findOneByName(COURSE_1.getName());
        StepVerifier.create(find).expectNext(COURSE_1).expectComplete().verify();
    }

    @Test
    public void testShouldReturnEmptyMonoWhenNoResults() {
        final Mono<Course> find = repository.findOneByName("unusedName");
        StepVerifier.create(find).verifyComplete();
    }

    @Test
    public void testInsert() {
        final Mono<Course> save = repository.save(COURSE_5);
        StepVerifier.create(save).expectNext(COURSE_5).verifyComplete();
    }

    @Test
    public void testUpsert() {
        Mono<Course> save = repository.save(COURSE_1);
        StepVerifier.create(save).expectNext(COURSE_1).expectComplete().verify();

        save = repository.save(COURSE_1);
        StepVerifier.create(save).expectNext(COURSE_1).expectComplete().verify();
    }

    @Test
    public void testDeleteByIdWithoutPartitionKey() {
        final Mono<Void> deleteMono = repository.deleteById(COURSE_1.getCourseId());
        StepVerifier.create(deleteMono).expectError(CosmosAccessException.class).verify();
    }

    @Test
    public void testDeleteByIdAndPartitionKey() {
        final Mono<Void> deleteMono = repository.deleteById(COURSE_1.getCourseId(),
            new PartitionKey(entityInformation.getPartitionKeyFieldValue(COURSE_1)));
        StepVerifier.create(deleteMono).verifyComplete();

        final Mono<Course> byId = repository.findById(COURSE_ID_1,
            new PartitionKey(entityInformation.getPartitionKeyFieldValue(COURSE_1)));
        //  Expect an empty mono as return value
        StepVerifier.create(byId).verifyComplete();
    }

    @Test
    public void testDeleteByEntity() {
        final Mono<Void> deleteMono = repository.delete(COURSE_4);
        StepVerifier.create(deleteMono).verifyComplete();

        final Mono<Course> byId = repository.findById(COURSE_ID_4);
        //  Expect an empty mono as return value
        StepVerifier.create(byId).expectComplete().verify();
    }

    @Test
    public void testDeleteByIdNotFound() {
        final Mono<Void> deleteMono = repository.deleteById(COURSE_ID_5);
        StepVerifier.create(deleteMono).expectError(CosmosAccessException.class).verify();
    }

    @Test
    public void testDeleteByEntityNotFound() {
        final Mono<Void> deleteMono = repository.delete(COURSE_5);
        StepVerifier.create(deleteMono).expectError(CosmosAccessException.class).verify();
    }

    @Test
    public void testCountAll() {
        final Mono<Long> countMono = repository.count();
        StepVerifier.create(countMono).expectNext(4L).verifyComplete();
    }

    @Test
    public void testFindByDepartmentIn() {
        final Flux<Course> byDepartmentIn =
            repository.findByDepartmentIn(Collections.singletonList(DEPARTMENT_NAME_2));
        StepVerifier.create(byDepartmentIn).expectNextCount(2).verifyComplete();
    }

    @Test
    public void testFindAllByPartitionKey() {
        final Mono<Course> save = repository.save(COURSE_5);
        StepVerifier.create(save).expectNext(COURSE_5).verifyComplete();

        Flux<Course> findAll = repository.findAll(new PartitionKey(DEPARTMENT_NAME_1));
        //  Since there are two courses with department_1
        final AtomicBoolean courseFound = new AtomicBoolean(false);
        StepVerifier.create(findAll).expectNextCount(2).verifyComplete();
        StepVerifier.create(findAll)
                    .expectNextMatches(course -> {
                        if (course.equals(COURSE_4)) {
                            courseFound.set(true);
                        } else if (course.equals(COURSE_5)) {
                            courseFound.set(false);
                        } else {
                            return false;
                        }
                        return true;
                    })
                    .expectNextMatches(course -> {
                        if (courseFound.get()) {
                            return course.equals(COURSE_5);
                        } else {
                            return course.equals(COURSE_4);
                        }
                    })
                    .verifyComplete();

        findAll = repository.findAll(new PartitionKey(DEPARTMENT_NAME_3));
        //  Since there are two courses with department_3
        StepVerifier.create(findAll).expectNext(COURSE_1).verifyComplete();

        findAll = repository.findAll(new PartitionKey(DEPARTMENT_NAME_2));
        //  Since there are two courses with department_2
        StepVerifier.create(findAll).expectNextCount(2).verifyComplete();
        StepVerifier.create(findAll)
                    .expectNextMatches(course -> {
                        if (course.equals(COURSE_2)) {
                            courseFound.set(true);
                        } else if (course.equals(COURSE_3)) {
                            courseFound.set(false);
                        } else {
                            return false;
                        }
                        return true;
                    })
                    .expectNextMatches(course -> {
                        if (courseFound.get()) {
                            return course.equals(COURSE_3);
                        } else {
                            return course.equals(COURSE_2);
                        }
                    })
                    .verifyComplete();
    }

    @Test
    public void testFindByNameIgnoreCase() {
        final Flux<Course> findResult = repository.findByNameIgnoreCase(COURSE_NAME_1.toLowerCase());
        StepVerifier.create(findResult).expectNext(COURSE_1).verifyComplete();
    }

    @Test
    public void testFindByNameAndDepartmentAllIgnoreCase() {
        final Flux<Course> findResult = repository.findByNameAndDepartmentAllIgnoreCase(
            COURSE_NAME_1.toLowerCase(), DEPARTMENT_NAME_3.toLowerCase());
        StepVerifier.create(findResult).expectNext(COURSE_1).verifyComplete();
    }

    @Test
    public void testFindByNameOrDepartmentAllIgnoreCase() {
        final Flux<Course> findResult = repository.findByNameOrDepartmentAllIgnoreCase(
            COURSE_NAME_1.toLowerCase(), DEPARTMENT_NAME_3.toLowerCase());
        StepVerifier.create(findResult).expectNext(COURSE_1).verifyComplete();
    }
}
