// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.integration;

import com.azure.spring.data.cosmos.domain.AuditableEntity;
import com.azure.spring.data.cosmos.repository.StubAuditorProvider;
import com.azure.spring.data.cosmos.repository.StubDateTimeProvider;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.ReactiveAuditableRepository;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class ReactiveAuditableIT {

    @Autowired
    private ReactiveAuditableRepository auditableRepository;
    @Autowired
    private StubDateTimeProvider stubDateTimeProvider;
    @Autowired
    private StubAuditorProvider stubAuditorProvider;

    @After
    public void cleanup() {
        this.auditableRepository.deleteAll().block();
    }

    @Test
    public void testInsertShouldSetAuditableEntries() {
        final AuditableEntity entity = new AuditableEntity();
        entity.setId(UUID.randomUUID().toString());
        final OffsetDateTime now = OffsetDateTime.now(ZoneId.of("UTC"));

        stubDateTimeProvider.setNow(now);
        stubAuditorProvider.setCurrentAuditor("created-by");
        final Mono<AuditableEntity> savedEntity = auditableRepository.save(entity);
        StepVerifier
            .create(savedEntity)
            .expectNextMatches(actual -> validateAuditableFields(actual,
                "created-by",
                now,
                "created-by",
                now))
            .verifyComplete();
    }

    @Test
    public void testUpdateShouldNotOverwriteCreatedEntries() {
        final AuditableEntity entity = new AuditableEntity();
        entity.setId(UUID.randomUUID().toString());
        final OffsetDateTime createdOn = OffsetDateTime.now(ZoneId.of("UTC"));

        stubDateTimeProvider.setNow(createdOn);
        stubAuditorProvider.setCurrentAuditor("created-by");
        final Mono<AuditableEntity> subscribe = auditableRepository.save(entity);
        final AuditableEntity saved = subscribe.block();

        final OffsetDateTime modifiedOn = createdOn.plusMinutes(1);
        stubDateTimeProvider.setNow(modifiedOn);
        stubAuditorProvider.setCurrentAuditor("modified-by");
        final Mono<AuditableEntity> modified = auditableRepository.save(saved);
        StepVerifier
            .create(modified)
            .expectNextMatches(actual -> validateAuditableFields(actual,
                "created-by",
                createdOn,
                "modified-by",
                modifiedOn))
            .verifyComplete();
    }

    private boolean validateAuditableFields(AuditableEntity entity,
                                            String expectedCreatedBy, OffsetDateTime expectedCreatedDate,
                                            String expectedModifiedBy, OffsetDateTime expectedModifiedTime) {
        assertThat(entity.getCreatedBy()).isEqualTo(expectedCreatedBy);
        assertThat(entity.getCreatedDate()).isEqualTo(expectedCreatedDate);
        assertThat(entity.getLastModifiedBy()).isEqualTo(expectedModifiedBy);
        assertThat(entity.getLastModifiedByDate()).isEqualTo(expectedModifiedTime);
        return true;
    }

}
