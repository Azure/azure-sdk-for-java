// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.repository.integration;

import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.spring.data.cosmos.ReactiveIntegrationTestCollectionManager;
import com.azure.spring.data.cosmos.core.ReactiveCosmosTemplate;
import com.azure.spring.data.cosmos.domain.ReactiveTeacher;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.ReactiveTeacherRepository;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class ReactiveTeacherRepositoryIT {

    private static final String TEACHER_ID_1 = "1";

    private static final String TEACHER_FIRST_NAME_1 = "FirstName1";

    private static final String DEPARTMENT_LAST_NAME_1 = "LastName1";

    private static final ReactiveTeacher TEACHER_1 = new ReactiveTeacher(TEACHER_ID_1, TEACHER_FIRST_NAME_1, DEPARTMENT_LAST_NAME_1);

    @ClassRule
    public static final ReactiveIntegrationTestCollectionManager collectionManager = new ReactiveIntegrationTestCollectionManager();

    @Autowired
    private ReactiveCosmosTemplate template;

    @Autowired
    private ReactiveTeacherRepository repository;

    @Before
    public void setUp() {
        collectionManager.ensureContainersCreatedAndEmpty(template, ReactiveTeacher.class);
        final Flux<ReactiveTeacher> savedFlux = repository.saveAll(Arrays.asList(TEACHER_1));
        StepVerifier.create(savedFlux).thenConsumeWhile(ReactiveTeacher -> true).expectComplete().verify();
    }

    @Test
    public void testSaveWithSuppressedNullValue() {
        final Mono<Void> deletedMono = repository.deleteAll();
        StepVerifier.create(deletedMono).thenAwait().verifyComplete();
        String teacherId = TEACHER_ID_1 + "-Other";
        final ReactiveTeacher teacher = new ReactiveTeacher(teacherId, TEACHER_FIRST_NAME_1, null);
        final Mono<ReactiveTeacher> saveSecond = repository.save(teacher);
        StepVerifier.create(saveSecond).expectNext(teacher).verifyComplete();

        final Mono<ReactiveTeacher> idMono = repository.findById(teacherId);
        StepVerifier.create(idMono).expectNext(teacher).expectComplete().verify();

        final Mono<Boolean> existFirstNameMono = repository.existsByFirstNameIsNotNull();
        StepVerifier.create(existFirstNameMono).expectNext(true).expectComplete().verify();

        final Mono<Boolean> existLastNameMono = repository.existsByLastNameIsNull();
        StepVerifier.create(existLastNameMono).expectNext(false).expectComplete().verify();
    }
    @Test
    public void testFindAllItemsOnePage() {
        ReactiveTeacher TEACHER_2 = new ReactiveTeacher("2", TEACHER_FIRST_NAME_1, DEPARTMENT_LAST_NAME_1);
        ReactiveTeacher TEACHER_3 = new ReactiveTeacher("3", TEACHER_FIRST_NAME_1, DEPARTMENT_LAST_NAME_1);
        ReactiveTeacher TEACHER_4 = new ReactiveTeacher("4", TEACHER_FIRST_NAME_1, DEPARTMENT_LAST_NAME_1);
        repository.saveAll(Arrays.asList(TEACHER_2, TEACHER_3, TEACHER_4));

        final CosmosPagedFlux<ReactiveTeacher> cosmosPagedFluxResult = repository.findAllByFirstName(TEACHER_FIRST_NAME_1);
        List<ReactiveTeacher> results = cosmosPagedFluxResult.byPage().blockFirst().getResults();

        assertThat(results.size()).isEqualTo(4);
        assertThat(results).contains(TEACHER_1, TEACHER_2, TEACHER_3, TEACHER_4);
    }

    /*@Test
    public void testFindAllWithPageablePageSize2() {
        cosmosTemplate.insert(TEST_PERSON_2, new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_2))).block();
        cosmosTemplate.insert(TEST_PERSON_3, new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_3))).block();
        cosmosTemplate.insert(TEST_PERSON_4, new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_4))).block();

        final CosmosPagedFlux<Person> cosmosPagedFluxResult = cosmosTemplate.findAll(Person.class, containerName);

        int totalResultCount = 0;
        List<Person> allResults = new ArrayList<>();
        String continuationToken = null;
        do {
            Iterable<FeedResponse<Person>> feedResponseIterable = cosmosPagedFluxResult
                .byPage(continuationToken, PAGE_SIZE_2).toIterable();
            for (FeedResponse<Person> fr : feedResponseIterable) {
                List<Person> results = fr.getResults();
                for (Person person: results) {
                    allResults.add(person);
                }
                assertThat(results.size()).isLessThanOrEqualTo(PAGE_SIZE_2);
                totalResultCount += results.size();
                continuationToken = fr.getContinuationToken();
            }
        } while (continuationToken != null);

        assertThat(totalResultCount).isEqualTo(4);
        assertThat(allResults).contains(TEST_PERSON, TEST_PERSON_2, TEST_PERSON_3, TEST_PERSON_4);
    }

    @Test
    public void testFindAllWithPageablePageSize3() {
        cosmosTemplate.insert(TEST_PERSON_2, new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_2))).block();
        cosmosTemplate.insert(TEST_PERSON_3, new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_3))).block();
        cosmosTemplate.insert(TEST_PERSON_4, new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_4))).block();

        final CosmosPagedFlux<Person> cosmosPagedFluxResult = cosmosTemplate.findAll(Person.class, containerName);

        int totalResultCount = 0;
        List<Person> allResults = new ArrayList<>();
        String continuationToken = null;
        do {
            Iterable<FeedResponse<Person>> feedResponseIterable = cosmosPagedFluxResult
                .byPage(continuationToken, PAGE_SIZE_3).toIterable();
            for (FeedResponse<Person> fr : feedResponseIterable) {
                List<Person> results = fr.getResults();
                for (Person person: results) {
                    allResults.add(person);
                }
                assertThat(results.size()).isLessThanOrEqualTo(PAGE_SIZE_3);
                totalResultCount += results.size();
                continuationToken = fr.getContinuationToken();
            }
        } while (continuationToken != null);

        assertThat(totalResultCount).isEqualTo(4);
        assertThat(allResults).contains(TEST_PERSON, TEST_PERSON_2, TEST_PERSON_3, TEST_PERSON_4);
    }*/

}
