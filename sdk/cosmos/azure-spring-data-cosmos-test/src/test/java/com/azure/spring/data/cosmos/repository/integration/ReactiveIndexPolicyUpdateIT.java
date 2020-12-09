package com.azure.spring.data.cosmos.repository.integration;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.ExcludedPath;
import com.azure.cosmos.models.IncludedPath;
import com.azure.cosmos.models.IndexingMode;
import com.azure.cosmos.models.IndexingPolicy;
import com.azure.spring.data.cosmos.core.ReactiveCosmosTemplate;
import com.azure.spring.data.cosmos.domain.IndexPolicyEntity;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.ReactiveIndexPolicyRepository;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import com.azure.spring.data.cosmos.repository.support.SimpleReactiveCosmosRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class ReactiveIndexPolicyUpdateIT {

    @Autowired
    CosmosAsyncClient cosmosAsyncClient;

    @Autowired
    ReactiveIndexPolicyRepository indexPolicyRepository;

    @Autowired
    ReactiveCosmosTemplate template;

    @Autowired
    ApplicationContext context;

    CosmosEntityInformation<IndexPolicyEntity, String> entityInformation = new CosmosEntityInformation<>(IndexPolicyEntity.class);

    @Test
    public void testIndexPolicyUpdatesOnRepoInitialization() {
        // get original index policy
       CosmosContainerProperties properties = template.getContainerProperties(entityInformation.getContainerName()).block();

        // assert
        assertThat(properties.getIndexingPolicy().getIncludedPaths().size()).isEqualTo(1);
        assertThat(properties.getIndexingPolicy().getIncludedPaths().get(0).getPath()).isEqualTo("/*");
        assertThat(properties.getIndexingPolicy().getExcludedPaths().size()).isEqualTo(1);
        assertThat(properties.getIndexingPolicy().getExcludedPaths().get(0).getPath()).isEqualTo("/\"_etag\"/?");
        assertThat(properties.getIndexingPolicy().isAutomatic()).isEqualTo(true);
        assertThat(properties.getIndexingPolicy().getIndexingMode()).isEqualTo(IndexingMode.CONSISTENT);

        // set new index policy
        IndexingPolicy newIndexPolicy = new IndexingPolicy();
        newIndexPolicy.setIncludedPaths(Collections.singletonList(new IncludedPath("/field/?")));
        newIndexPolicy.setExcludedPaths(Collections.singletonList(new ExcludedPath("/*")));

        // apply new index policy
        CosmosEntityInformation<IndexPolicyEntity, String> spyEntityInformation = Mockito.spy(entityInformation);
        Mockito.doReturn(newIndexPolicy).when(spyEntityInformation).getIndexingPolicy();
        new SimpleReactiveCosmosRepository<>(spyEntityInformation, template);

        // retrieve updated index policy
        properties = template.getContainerProperties(entityInformation.getContainerName()).block();

        // assert
        assertThat(properties.getIndexingPolicy().getIncludedPaths().size()).isEqualTo(1);
        assertThat(properties.getIndexingPolicy().getIncludedPaths().get(0).getPath()).isEqualTo("/field/?");
        assertThat(properties.getIndexingPolicy().getExcludedPaths().size()).isEqualTo(2);
        assertThat(properties.getIndexingPolicy().getExcludedPaths().get(0).getPath()).isEqualTo("/*");
        assertThat(properties.getIndexingPolicy().getExcludedPaths().get(1).getPath()).isEqualTo("/\"_etag\"/?");
        assertThat(properties.getIndexingPolicy().isAutomatic()).isEqualTo(true);
        assertThat(properties.getIndexingPolicy().getIndexingMode()).isEqualTo(IndexingMode.CONSISTENT);
    }

}
