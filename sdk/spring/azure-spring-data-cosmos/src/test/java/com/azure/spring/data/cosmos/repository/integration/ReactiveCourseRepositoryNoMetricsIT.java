// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.integration;

import com.azure.spring.data.cosmos.ReactiveIntegrationTestCollectionManager;
import com.azure.spring.data.cosmos.common.ResponseDiagnosticsTestUtils;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.ReactiveCosmosTemplate;
import com.azure.spring.data.cosmos.domain.Course;
import com.azure.spring.data.cosmos.repository.TestRepositoryNoMetricsConfig;
import com.azure.spring.data.cosmos.repository.repository.ReactiveCourseRepository;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryNoMetricsConfig.class)
public class ReactiveCourseRepositoryNoMetricsIT {

    private static final String COURSE_ID_1 = "1";
    private static final String COURSE_ID_2 = "2";

    private static final String COURSE_NAME_1 = "Course1";
    private static final String COURSE_NAME_2 = "Course2";

    private static final String DEPARTMENT_NAME_1 = "Department1";
    private static final String DEPARTMENT_NAME_2 = "Department2";

    private static final Course COURSE_1 = new Course(COURSE_ID_1, COURSE_NAME_1, DEPARTMENT_NAME_1);
    private static final Course COURSE_2 = new Course(COURSE_ID_2, COURSE_NAME_2, DEPARTMENT_NAME_2);

    @ClassRule
    public static final ReactiveIntegrationTestCollectionManager collectionManager = new ReactiveIntegrationTestCollectionManager();

    @Autowired
    private ReactiveCosmosTemplate template;

    @Autowired
    private ReactiveCourseRepository repository;

    @Autowired
    private CosmosConfig cosmosConfig;

    @Autowired
    private ResponseDiagnosticsTestUtils responseDiagnosticsTestUtils;

    private CosmosEntityInformation<Course, ?> entityInformation;

    @Before
    public void setUp() {
        collectionManager.ensureContainersCreatedAndEmpty(template, Course.class);
        entityInformation = collectionManager.getEntityInformation(Course.class);
        final Flux<Course> savedFlux = repository.saveAll(Arrays.asList(COURSE_1, COURSE_2));
        StepVerifier.create(savedFlux).thenConsumeWhile(course -> true).expectComplete().verify();
    }

    @Test
    public void queryDatabaseWithQueryMetricsDisabled() {
        // Test flag is true
        assertThat(cosmosConfig.isQueryMetricsEnabled()).isFalse();

        // Make sure a query runs
        final Flux<Course> allFlux = repository.findAll();
        StepVerifier.create(allFlux).expectNextCount(2).verifyComplete();

        String queryDiagnostics = responseDiagnosticsTestUtils.getCosmosDiagnostics().toString();
        assertThat(queryDiagnostics).doesNotContain("retrievedDocumentCount");
        assertThat(queryDiagnostics).doesNotContain("queryPreparationTimes");
        assertThat(queryDiagnostics).doesNotContain("runtimeExecutionTimes");
        assertThat(queryDiagnostics).doesNotContain("fetchExecutionRanges");
    }

    @Test
    public void queryDatabaseWithIndexMetricsDisabled() {
        // Test flag is true
        assertThat(cosmosConfig.isIndexMetricsEnabled()).isFalse();

        // Make sure a query runs
        final Flux<Course> allFlux = repository.findAll();
        StepVerifier.create(allFlux).expectNextCount(2).verifyComplete();

        String queryDiagnostics = responseDiagnosticsTestUtils.getCosmosDiagnostics().toString();

        assertThat(queryDiagnostics).doesNotContain("\"indexUtilizationInfo\"");
        assertThat(queryDiagnostics).doesNotContain("\"UtilizedSingleIndexes\"");
        assertThat(queryDiagnostics).doesNotContain("\"PotentialSingleIndexes\"");
        assertThat(queryDiagnostics).doesNotContain("\"UtilizedCompositeIndexes\"");
        assertThat(queryDiagnostics).doesNotContain("\"PotentialCompositeIndexes\"");
    }
}
