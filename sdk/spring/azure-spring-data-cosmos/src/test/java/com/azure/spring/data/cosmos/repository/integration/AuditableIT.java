// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.integration;

import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.spring.data.cosmos.IntegrationTestCollectionManager;
import com.azure.spring.data.cosmos.common.TestUtils;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.core.generator.FindQuerySpecGenerator;
import com.azure.spring.data.cosmos.core.query.CosmosQuery;
import com.azure.spring.data.cosmos.core.query.Criteria;
import com.azure.spring.data.cosmos.core.query.CriteriaType;
import com.azure.spring.data.cosmos.domain.AuditableEntity;
import com.azure.spring.data.cosmos.domain.AuditableIdGeneratedEntity;
import com.azure.spring.data.cosmos.repository.StubAuditorProvider;
import com.azure.spring.data.cosmos.repository.StubDateTimeProvider;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.AuditableIdGeneratedRepository;
import com.azure.spring.data.cosmos.repository.repository.AuditableRepository;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class AuditableIT {

    @ClassRule
    public static final IntegrationTestCollectionManager collectionManager = new IntegrationTestCollectionManager();

    @Autowired
    private CosmosTemplate cosmosTemplate;
    @Autowired
    private AuditableRepository auditableRepository;
    @Autowired
    private AuditableIdGeneratedRepository auditableIdGeneratedRepository;
    @Autowired
    private StubDateTimeProvider stubDateTimeProvider;
    @Autowired
    private StubAuditorProvider stubAuditorProvider;

    @Before
    public void setup() {
        collectionManager.ensureContainersCreatedAndEmpty(cosmosTemplate, AuditableEntity.class, AuditableIdGeneratedEntity.class);
    }

    @Test
    public void testInsertShouldSetAuditableEntries() {
        final AuditableEntity entity = new AuditableEntity();
        entity.setId(UUID.randomUUID().toString());
        final OffsetDateTime now = OffsetDateTime.now(ZoneId.of("UTC")).truncatedTo(ChronoUnit.MICROS);

        stubDateTimeProvider.setNow(now);
        stubAuditorProvider.setCurrentAuditor("created-by");
        final AuditableEntity savedEntity = auditableRepository.save(entity);

        assertThat(savedEntity.getCreatedBy()).isEqualTo("created-by");
        assertThat(savedEntity.getCreatedDate()).isEqualTo(now);
        assertThat(savedEntity.getLastModifiedBy()).isEqualTo("created-by");
        assertThat(savedEntity.getLastModifiedByDate()).isEqualTo(now);
    }

    @Test
    public void testInsertAllShouldSetAuditableEntries() {
        final AuditableEntity entity1 = new AuditableEntity();
        String UUID_1 = UUID.randomUUID().toString();
        entity1.setId(UUID_1);
        final AuditableEntity entity2 = new AuditableEntity();
        String UUID_2 = UUID.randomUUID().toString();
        entity2.setId(UUID_2);
        final OffsetDateTime now = OffsetDateTime.now(ZoneId.of("UTC")).truncatedTo(ChronoUnit.MICROS);

        stubDateTimeProvider.setNow(now);
        stubAuditorProvider.setCurrentAuditor("created-by");
        final List<AuditableEntity> savedEntities =
            TestUtils.toList(auditableRepository.saveAll(Lists.newArrayList(entity1, entity2)));

        assertThat(savedEntities.get(0).getId()).isEqualTo(UUID_1);
        assertThat(savedEntities.get(0).getCreatedBy()).isEqualTo("created-by");
        assertThat(savedEntities.get(0).getCreatedDate()).isEqualTo(now);
        assertThat(savedEntities.get(0).getLastModifiedBy()).isEqualTo("created-by");
        assertThat(savedEntities.get(0).getLastModifiedByDate()).isEqualTo(now);
        assertThat(savedEntities.get(1).getId()).isEqualTo(UUID_2);
        assertThat(savedEntities.get(1).getCreatedBy()).isEqualTo("created-by");
        assertThat(savedEntities.get(1).getCreatedDate()).isEqualTo(now);
        assertThat(savedEntities.get(1).getLastModifiedBy()).isEqualTo("created-by");
        assertThat(savedEntities.get(1).getLastModifiedByDate()).isEqualTo(now);
    }

    @Test
    public void testUpdateShouldNotOverwriteCreatedEntries() {
        final AuditableEntity entity = new AuditableEntity();
        entity.setId(UUID.randomUUID().toString());
        final OffsetDateTime createdOn = OffsetDateTime.now(ZoneId.of("UTC")).truncatedTo(ChronoUnit.MICROS);

        stubDateTimeProvider.setNow(createdOn);
        stubAuditorProvider.setCurrentAuditor("created-by");
        final AuditableEntity savedEntity = auditableRepository.save(entity);

        final OffsetDateTime modifiedOn = createdOn.plusMinutes(1);
        stubDateTimeProvider.setNow(modifiedOn);
        stubAuditorProvider.setCurrentAuditor("modified-by");
        final AuditableEntity modifiedEntity = auditableRepository.save(savedEntity);

        assertThat(modifiedEntity.getCreatedBy()).isEqualTo("created-by");
        assertThat(modifiedEntity.getCreatedDate()).isEqualTo(createdOn);
        assertThat(modifiedEntity.getLastModifiedBy()).isEqualTo("modified-by");
        assertThat(modifiedEntity.getLastModifiedByDate()).isEqualTo(modifiedOn);
    }

    @Test
    public void testInsertShouldSetAuditableEntriesIfIdAutoGenerated() {
        final AuditableIdGeneratedEntity entity = new AuditableIdGeneratedEntity();
        final OffsetDateTime now = OffsetDateTime.now(ZoneId.of("UTC")).truncatedTo(ChronoUnit.MICROS);

        stubDateTimeProvider.setNow(now);
        stubAuditorProvider.setCurrentAuditor("created-by");
        final AuditableIdGeneratedEntity savedEntity = auditableIdGeneratedRepository.save(entity);

        assertThat(savedEntity.getCreatedBy()).isEqualTo("created-by");
        assertThat(savedEntity.getCreatedDate()).isEqualTo(now);
        assertThat(savedEntity.getLastModifiedBy()).isEqualTo("created-by");
        assertThat(savedEntity.getLastModifiedByDate()).isEqualTo(now);
    }

    @Test
    public void testRunQueryWithReturnTypeContainingLocalDateTime() {
        final AuditableEntity entity = new AuditableEntity();
        entity.setId(UUID.randomUUID().toString());

        auditableRepository.save(entity);

        Criteria equals = Criteria.getInstance(CriteriaType.IS_EQUAL, "id", Collections.singletonList(entity.getId()), Part.IgnoreCaseType.NEVER);
        final SqlQuerySpec sqlQuerySpec = new FindQuerySpecGenerator().generateCosmos(new CosmosQuery(equals));
        List<AuditableEntity> results = TestUtils.toList(cosmosTemplate.runQuery(sqlQuerySpec, AuditableEntity.class, AuditableEntity.class));
        assertEquals(results.size(), 1);
        AuditableEntity foundEntity = results.get(0);
        assertEquals(entity.getId(), foundEntity.getId());
        assertNotNull(foundEntity.getCreatedDate());
        assertNotNull(foundEntity.getLastModifiedByDate());
    }

}
