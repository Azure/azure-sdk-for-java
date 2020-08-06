// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.integration;

import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.core.query.CosmosPageRequest;
import com.azure.spring.data.cosmos.domain.SortedProject;
import com.azure.spring.data.cosmos.exception.CosmosAccessException;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.SortedProjectRepository;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.azure.spring.data.cosmos.common.PageTestUtils.validateLastPage;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class ProjectRepositorySortIT {

    private static final String ID_0 = "id-0";
    private static final String ID_1 = "id-1";
    private static final String ID_2 = "id-2";
    private static final String ID_3 = "id-3";
    private static final String ID_4 = "id-4";

    private static final String NAME_0 = "name-0";
    private static final String NAME_1 = "name-1";
    private static final String NAME_2 = "name-2";
    private static final String NAME_3 = "NAME-3";
    private static final String NAME_4 = "name-4";

    private static final String CREATOR_0 = "creator-0";
    private static final String CREATOR_1 = "creator-1";
    private static final String CREATOR_2 = "creator-2";
    private static final String CREATOR_3 = "creator-3";
    private static final String CREATOR_4 = "creator-4";

    private static final Long STAR_COUNT_0 = 0L;
    private static final Long STAR_COUNT_1 = 1L;
    private static final Long STAR_COUNT_2 = 2L;
    private static final Long STAR_COUNT_3 = 3L;
    private static final Long STAR_COUNT_4 = 4L;

    private static final Long FORK_COUNT_0 = 0L;
    private static final Long FORK_COUNT_1 = 1L;
    private static final Long FORK_COUNT_2 = 2L;
    private static final Long FORK_COUNT_3 = 3L;
    private static final Long FORK_COUNT_4 = FORK_COUNT_3;

    private static final SortedProject PROJECT_0 = new SortedProject(ID_0, NAME_0, CREATOR_0,
        true, STAR_COUNT_0, FORK_COUNT_0);
    private static final SortedProject PROJECT_1 = new SortedProject(ID_1, NAME_1, CREATOR_1,
        true, STAR_COUNT_1, FORK_COUNT_1);
    private static final SortedProject PROJECT_2 = new SortedProject(ID_2, NAME_2, CREATOR_2,
        true, STAR_COUNT_2, FORK_COUNT_2);
    private static final SortedProject PROJECT_3 = new SortedProject(ID_3, NAME_3, CREATOR_3,
        true, STAR_COUNT_3, FORK_COUNT_3);
    private static final SortedProject PROJECT_4 = new SortedProject(ID_4, NAME_4, CREATOR_4,
        true, STAR_COUNT_4, FORK_COUNT_4);

    private static final List<SortedProject> PROJECTS = Arrays.asList(PROJECT_4, PROJECT_3,
        PROJECT_2, PROJECT_1, PROJECT_0);

    private static final CosmosEntityInformation<SortedProject, String> entityInformation =
        new CosmosEntityInformation<>(SortedProject.class);

    private static CosmosTemplate staticTemplate;
    private static boolean isSetupDone;

    @Autowired
    private CosmosTemplate template;

    @Autowired
    private SortedProjectRepository repository;

    @Before
    public void setUp() {
        if (!isSetupDone) {
            staticTemplate = template;
            template.createContainerIfNotExists(entityInformation);
        }
        this.repository.saveAll(PROJECTS);
        isSetupDone = true;
    }

    @After
    public void cleanup() {
        this.repository.deleteAll();
    }

    @AfterClass
    public static void afterClassCleanup() {
        staticTemplate.deleteContainer(entityInformation.getContainerName());
    }

    @Test
    public void testFindAllSortASC() {
        final Sort sort = Sort.by(Sort.Direction.ASC, "starCount");
        final List<SortedProject> projects = Lists.newArrayList(this.repository.findAll(sort));

        PROJECTS.sort(Comparator.comparing(SortedProject::getStarCount));

        Assert.assertEquals(PROJECTS.size(), projects.size());
        Assert.assertEquals(PROJECTS, projects);
    }

    @Test
    public void testFindAllSortDESC() {
        final Sort sort = Sort.by(Sort.Direction.DESC, "creator");
        final List<SortedProject> projects = Lists.newArrayList(this.repository.findAll(sort));

        PROJECTS.sort(Comparator.comparing(SortedProject::getCreator).reversed());

        Assert.assertEquals(PROJECTS.size(), projects.size());
        Assert.assertEquals(PROJECTS, projects);
    }

    @Test
    public void testFindAllUnSorted() {
        final Sort sort = Sort.unsorted();
        final List<SortedProject> projects = Lists.newArrayList(this.repository.findAll(sort));

        PROJECTS.sort(Comparator.comparing(SortedProject::getId));
        projects.sort(Comparator.comparing(SortedProject::getId));

        Assert.assertEquals(PROJECTS.size(), projects.size());
        Assert.assertEquals(PROJECTS, projects);
    }

    @Test(expected = CosmosAccessException.class)
    public void testFindAllSortMoreThanOneOrderException() {
        final Sort sort = Sort.by(Sort.Direction.ASC, "name", "creator");

        this.repository.findAll(sort).iterator().next();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindAllSortIgnoreCaseException() {
        final Sort.Order order = Sort.Order.by("name").ignoreCase();
        final Sort sort = Sort.by(order);

        this.repository.findAll(sort);
    }

    @Test(expected = CosmosAccessException.class)
    public void testFindAllSortMissMatchException() {
        final Sort sort = Sort.by(Sort.Direction.ASC, "fake-name");

        this.repository.findAll(sort).iterator().next();
    }

    public void testFindAllSortWithIdName() {
        final List<SortedProject> projectListSortedById = Lists.newArrayList(PROJECTS);
        projectListSortedById.sort(Comparator.comparing(SortedProject::getId));

        final Sort sort = Sort.by(Sort.Direction.ASC, "id");
        final List<SortedProject> results = StreamSupport.stream(this.repository.findAll(sort).spliterator(),
            false)
                                                         .collect(Collectors.toList());

        Assert.assertEquals(projectListSortedById, results);
    }

    @Test
    public void testFindSortWithOr() {
        final Sort sort = Sort.by(Sort.Direction.ASC, "starCount");
        final List<SortedProject> projects = Lists.newArrayList(this.repository.findByNameOrCreator(NAME_0, CREATOR_3,
            sort));
        final List<SortedProject> references = Arrays.asList(PROJECT_0, PROJECT_3);

        references.sort(Comparator.comparing(SortedProject::getStarCount));

        Assert.assertEquals(references.size(), projects.size());
        Assert.assertEquals(references, projects);
    }

    @Test
    public void testFindSortWithAnd() {
        final Sort sort = Sort.by(Sort.Direction.ASC, "forkCount");
        final List<SortedProject> projects = Lists.newArrayList(repository.findByNameAndCreator(NAME_0, CREATOR_0,
            sort));
        final List<SortedProject> references = Arrays.asList(PROJECT_0);

        references.sort(Comparator.comparing(SortedProject::getStarCount));

        Assert.assertEquals(references.size(), projects.size());
        Assert.assertEquals(references, projects);
    }

    @Test
    public void testFindSortWithEqual() {
        final Sort sort = Sort.by(Sort.Direction.DESC, "name");
        final List<SortedProject> projects = Lists.newArrayList(this.repository.findByForkCount(FORK_COUNT_3, sort));
        final List<SortedProject> references = Arrays.asList(PROJECT_3, PROJECT_4);

        references.sort(Comparator.comparing(SortedProject::getName).reversed());

        Assert.assertEquals(references.size(), projects.size());
        Assert.assertEquals(references, projects);
    }

    @Test
    public void testFindAllWithPageableAndSort() {
        final Sort sort = Sort.by(Sort.Direction.DESC, "name");
        final Pageable pageable = new CosmosPageRequest(0, 5, null, sort);

        final Page<SortedProject> result = this.repository.findAll(pageable);

        final List<SortedProject> references = Arrays.asList(PROJECT_0, PROJECT_1, PROJECT_2, PROJECT_3, PROJECT_4);
        references.sort(Comparator.comparing(SortedProject::getName).reversed());

        Assert.assertEquals(references.size(), result.getContent().size());
        Assert.assertEquals(references, result.getContent());
        validateLastPage(result, 5);
    }

    @Test
    public void testFindWithPageableAndSort() {
        final Sort sort = Sort.by(Sort.Direction.DESC, "name");
        final Pageable pageable = new CosmosPageRequest(0, 5, null, sort);

        final Page<SortedProject> result = this.repository.findByForkCount(FORK_COUNT_3, pageable);

        final List<SortedProject> references = Arrays.asList(PROJECT_3, PROJECT_4);

        references.sort(Comparator.comparing(SortedProject::getName).reversed());

        Assert.assertEquals(references.size(), result.getContent().size());
        Assert.assertEquals(references, result.getContent());
        validateLastPage(result, result.getContent().size());
    }
}

