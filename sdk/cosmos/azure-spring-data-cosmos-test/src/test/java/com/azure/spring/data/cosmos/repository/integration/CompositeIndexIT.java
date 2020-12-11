package com.azure.spring.data.cosmos.repository.integration;

import com.azure.cosmos.models.CompositePath;
import com.azure.cosmos.models.CompositePathSortOrder;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.domain.ComplexIndexPolicyEntity;
import com.azure.spring.data.cosmos.domain.CompositeIndexEntity;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import com.azure.spring.data.cosmos.repository.support.SimpleCosmosRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class CompositeIndexIT {

    @Autowired
    CosmosTemplate template;

    CosmosEntityInformation<CompositeIndexEntity, String> information = new CosmosEntityInformation<>(CompositeIndexEntity.class);

    @Test
    public void canSetCompositeIndex() {
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

}
