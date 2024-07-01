// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.repository.integration;

import com.azure.cosmos.models.CompositePath;
import com.azure.cosmos.models.CompositePathSortOrder;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.IndexingPolicy;
import com.azure.spring.data.cosmos.IntegrationTestCollectionManager;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.core.ReactiveCosmosTemplate;
import com.azure.spring.data.cosmos.domain.CompositeIndexEntity;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import com.azure.spring.data.cosmos.repository.support.SimpleCosmosRepository;
import com.azure.spring.data.cosmos.repository.support.SimpleReactiveCosmosRepository;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class CompositeIndexIT {

    @ClassRule
    public static final IntegrationTestCollectionManager collectionManager = new IntegrationTestCollectionManager();

    @Autowired
    CosmosTemplate template;

    @Autowired
    ReactiveCosmosTemplate reactiveTemplate;

    CosmosEntityInformation<CompositeIndexEntity, String> information = new CosmosEntityInformation<>(CompositeIndexEntity.class);

    @Before
    public void setup() {
        collectionManager.ensureContainersCreatedAndEmpty(template, CompositeIndexEntity.class);
    }

    @AfterClass
    public static void cleanUp() {
        collectionManager.deleteContainer(new CosmosEntityInformation<>(CompositeIndexEntity.class));
    }

    @Test
    public void canSetCompositeIndex() {
        collectionManager.deleteContainer(information);
        new SimpleCosmosRepository<>(information, template);
        CosmosContainerProperties properties = template.getContainerProperties(information.getContainerName());
        List<List<CompositePath>> indexes = properties.getIndexingPolicy().getCompositeIndexes();

        assertThat(indexes.get(0).get(0).getPath()).isEqualTo("/fieldOne");
        assertThat(indexes.get(0).get(0).getOrder()).isEqualTo(CompositePathSortOrder.ASCENDING);
        assertThat(indexes.get(0).get(1).getPath()).isEqualTo("/fieldTwo");
        assertThat(indexes.get(0).get(1).getOrder()).isEqualTo(CompositePathSortOrder.ASCENDING);

        assertThat(indexes.get(1).get(0).getPath()).isEqualTo("/fieldThree");
        assertThat(indexes.get(1).get(0).getOrder()).isEqualTo(CompositePathSortOrder.DESCENDING);
        assertThat(indexes.get(1).get(1).getPath()).isEqualTo("/fieldFour");
        assertThat(indexes.get(1).get(1).getOrder()).isEqualTo(CompositePathSortOrder.DESCENDING);
    }

    @Test
    public void canSetCompositeIndexReactive() {
        collectionManager.deleteContainer(information);
        new SimpleReactiveCosmosRepository<>(information, reactiveTemplate);
        CosmosContainerProperties properties = reactiveTemplate.getContainerProperties(information.getContainerName()).block();
        List<List<CompositePath>> indexes = properties.getIndexingPolicy().getCompositeIndexes();

        assertThat(indexes.get(0).get(0).getPath()).isEqualTo("/fieldOne");
        assertThat(indexes.get(0).get(0).getOrder()).isEqualTo(CompositePathSortOrder.ASCENDING);
        assertThat(indexes.get(0).get(1).getPath()).isEqualTo("/fieldTwo");
        assertThat(indexes.get(0).get(1).getOrder()).isEqualTo(CompositePathSortOrder.ASCENDING);

        assertThat(indexes.get(1).get(0).getPath()).isEqualTo("/fieldThree");
        assertThat(indexes.get(1).get(0).getOrder()).isEqualTo(CompositePathSortOrder.DESCENDING);
        assertThat(indexes.get(1).get(1).getPath()).isEqualTo("/fieldFour");
        assertThat(indexes.get(1).get(1).getOrder()).isEqualTo(CompositePathSortOrder.DESCENDING);
    }


    @Test
    public void canNotUpdateCompositeIndex() {
        // initialize policy on entity
        new SimpleCosmosRepository<>(information, template);

        // set new index policy
        IndexingPolicy newIndexPolicy = new IndexingPolicy();
        List<List<CompositePath>> newCompositeIndex = new ArrayList<>();
        List<CompositePath> innerList = new ArrayList<>();
        innerList.add(new CompositePath().setPath("/fieldOne"));
        innerList.add(new CompositePath().setPath("/fieldFour"));
        newCompositeIndex.add(innerList);
        newIndexPolicy.setCompositeIndexes(newCompositeIndex);

        // apply new index policy
        CosmosEntityInformation<CompositeIndexEntity, String> spyEntityInformation = Mockito.spy(information);
        Mockito.doReturn(newIndexPolicy).when(spyEntityInformation).getIndexingPolicy();
        new SimpleCosmosRepository<>(spyEntityInformation, template);

        // retrieve new policy
        CosmosContainerProperties properties = template.getContainerProperties(information.getContainerName());
        List<List<CompositePath>> indexes = properties.getIndexingPolicy().getCompositeIndexes();

        // assert
        assertThat(indexes.size()).isEqualTo(2);
        assertThat(indexes.get(0).get(0).getPath()).isEqualTo("/fieldOne");
        assertThat(indexes.get(0).get(0).getOrder()).isEqualTo(CompositePathSortOrder.ASCENDING);
        assertThat(indexes.get(0).get(1).getPath()).isEqualTo("/fieldTwo");
        assertThat(indexes.get(0).get(1).getOrder()).isEqualTo(CompositePathSortOrder.ASCENDING);
        assertThat(indexes.get(1).get(0).getPath()).isEqualTo("/fieldThree");
        assertThat(indexes.get(1).get(0).getOrder()).isEqualTo(CompositePathSortOrder.DESCENDING);
        assertThat(indexes.get(1).get(1).getPath()).isEqualTo("/fieldFour");
        assertThat(indexes.get(1).get(1).getOrder()).isEqualTo(CompositePathSortOrder.DESCENDING);
    }

    @Test
    public void canNotUpdateCompositeIndexReactive() {
        // initialize policy on entity
        new SimpleReactiveCosmosRepository<>(information, reactiveTemplate);

        // set new index policy
        IndexingPolicy newIndexPolicy = new IndexingPolicy();
        List<List<CompositePath>> newCompositeIndex = new ArrayList<>();
        List<CompositePath> innerList = new ArrayList<>();
        innerList.add(new CompositePath().setPath("/fieldOne"));
        innerList.add(new CompositePath().setPath("/fieldFour"));
        newCompositeIndex.add(innerList);
        newIndexPolicy.setCompositeIndexes(newCompositeIndex);

        // apply new index policy
        CosmosEntityInformation<CompositeIndexEntity, String> spyEntityInformation = Mockito.spy(information);
        Mockito.doReturn(newIndexPolicy).when(spyEntityInformation).getIndexingPolicy();
        new SimpleReactiveCosmosRepository<>(spyEntityInformation, reactiveTemplate);

        // retrieve new policy
        CosmosContainerProperties properties = reactiveTemplate.getContainerProperties(information.getContainerName()).block();
        List<List<CompositePath>> indexes = properties.getIndexingPolicy().getCompositeIndexes();

        // assert
        assertThat(indexes.size()).isEqualTo(2);
        assertThat(indexes.get(0).get(0).getPath()).isEqualTo("/fieldOne");
        assertThat(indexes.get(0).get(0).getOrder()).isEqualTo(CompositePathSortOrder.ASCENDING);
        assertThat(indexes.get(0).get(1).getPath()).isEqualTo("/fieldTwo");
        assertThat(indexes.get(0).get(1).getOrder()).isEqualTo(CompositePathSortOrder.ASCENDING);
        assertThat(indexes.get(1).get(0).getPath()).isEqualTo("/fieldThree");
        assertThat(indexes.get(1).get(0).getOrder()).isEqualTo(CompositePathSortOrder.DESCENDING);
        assertThat(indexes.get(1).get(1).getPath()).isEqualTo("/fieldFour");
        assertThat(indexes.get(1).get(1).getOrder()).isEqualTo(CompositePathSortOrder.DESCENDING);
    }

}
