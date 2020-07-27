// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.integration;

import com.azure.cosmos.models.PartitionKey;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.domain.LongIdDomainPartition;
import com.azure.spring.data.cosmos.exception.CosmosAccessException;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.ReactiveLongIdDomainPartitionRepository;
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
import java.util.Objects;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class ReactiveLongIdDomainPartitionPartitionRepositoryIT {

    private static final Long ID_1 = 12345L;
    private static final String NAME_1 = "moary";

    private static final Long ID_2 = 67890L;
    private static final String NAME_2 = "camille";

    private static final LongIdDomainPartition DOMAIN_1 = new LongIdDomainPartition(ID_1, NAME_1);
    private static final LongIdDomainPartition DOMAIN_2 = new LongIdDomainPartition(ID_2, NAME_2);

    private static final CosmosEntityInformation<LongIdDomainPartition, Integer> entityInformation =
            new CosmosEntityInformation<>(LongIdDomainPartition.class);

    private static CosmosTemplate staticTemplate;
    private static boolean isSetupDone;

    @Autowired
    private CosmosTemplate template;

    @Autowired
    private ReactiveLongIdDomainPartitionRepository repository;

    @Before
    public void setUp() {
        if (!isSetupDone) {
            staticTemplate = template;
            template.createContainerIfNotExists(entityInformation);
        }
        Flux<LongIdDomainPartition> savedAllFlux = this.repository.saveAll(Arrays.asList(DOMAIN_1, DOMAIN_2));
        StepVerifier.create(savedAllFlux).thenConsumeWhile(domain -> true).expectComplete().verify();
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
    public void testLongIdDomainPartition() {
        Mono<Void> deletedMono = this.repository.deleteAll();
        StepVerifier.create(deletedMono).thenAwait().verifyComplete();

        Mono<LongIdDomainPartition> idMono = this.repository.findById(ID_1,
            new PartitionKey(entityInformation.getPartitionKeyFieldValue(DOMAIN_1)));
        StepVerifier.create(idMono).expectNextCount(0).verifyComplete();

        Mono<LongIdDomainPartition> saveMono = this.repository.save(DOMAIN_1);
        StepVerifier.create(saveMono).expectNext(DOMAIN_1).expectComplete().verify();

        Mono<LongIdDomainPartition> findIdMono = this.repository.findById(ID_1,
            new PartitionKey(entityInformation.getPartitionKeyFieldValue(DOMAIN_1)));
        StepVerifier.create(findIdMono).expectNext(DOMAIN_1).expectComplete().verify();

        Mono<Void> deleteMono = this.repository.delete(DOMAIN_1);
        StepVerifier.create(deleteMono).verifyComplete();

        Mono<LongIdDomainPartition> afterDelIdMono = this.repository.findById(ID_1,
            new PartitionKey(entityInformation.getPartitionKeyFieldValue(DOMAIN_1)));
        StepVerifier.create(afterDelIdMono).expectNextCount(0).verifyComplete();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidDomain() {
        new CosmosEntityInformation<InvalidDomain, Long>(InvalidDomain.class);
    }

    @Test
    public void testSaveAllAndFindAll() {
        final Mono<Void> deletedMono = repository.deleteAll();
        StepVerifier.create(deletedMono).thenAwait().verifyComplete();

        Flux<LongIdDomainPartition> savedAllFlux = this.repository.saveAll(Arrays.asList(DOMAIN_1, DOMAIN_2));
        StepVerifier.create(savedAllFlux).expectNextCount(2).verifyComplete();

        final Flux<LongIdDomainPartition> allFlux = repository.findAll();
        StepVerifier.create(allFlux).expectNextCount(2).verifyComplete();
    }

    @Test
    public void testCount() {
        Mono<Long> countMono = repository.count();
        StepVerifier.create(countMono).expectNext(2L).verifyComplete();
    }

    @Test
    public void testDeleteByIdWithoutPartitionKey() {
        final Mono<Void> deleteMono = repository.deleteById(DOMAIN_1.getNumber());
        StepVerifier.create(deleteMono).expectError(CosmosAccessException.class).verify();
    }

    @Test
    public void testDeleteByIdAndPartitionKey() {
        final Mono<Void> deleteMono = repository.deleteById(DOMAIN_1.getNumber(),
            new PartitionKey(entityInformation.getPartitionKeyFieldValue(DOMAIN_1)));
        StepVerifier.create(deleteMono).verifyComplete();

        Mono<LongIdDomainPartition> findIdMono = this.repository.findById(ID_1,
            new PartitionKey(entityInformation.getPartitionKeyFieldValue(DOMAIN_1)));
        StepVerifier.create(findIdMono).expectNextCount(0).verifyComplete();
    }

    @Test
    public void testDeleteByIdShouldFailIfNothingToDelete() {
        final Mono<Void> deletedMono = repository.deleteAll();
        StepVerifier.create(deletedMono).thenAwait().verifyComplete();

        final Mono<Void> deleteIdMono = repository.deleteById(DOMAIN_1.getNumber(),
            new PartitionKey(entityInformation.getPartitionKeyFieldValue(DOMAIN_1)));
        StepVerifier.create(deleteIdMono).expectError(CosmosAccessException.class).verify();
    }

    @Test
    public void testDelete() {
        Mono<LongIdDomainPartition> saveMono = this.repository.save(DOMAIN_1);
        StepVerifier.create(saveMono).expectNext(DOMAIN_1).expectComplete().verify();

        Mono<Void> deleteMono = this.repository.delete(DOMAIN_1);
        StepVerifier.create(deleteMono).verifyComplete();

        Mono<Long> countMono = repository.count();
        StepVerifier.create(countMono).expectNext(1L).verifyComplete();
    }

    @Test
    public void testDeleteShouldFailIfNothingToDelete() {
        final Mono<Void> deletedMono = repository.deleteAll();
        StepVerifier.create(deletedMono).thenAwait().verifyComplete();

        Mono<Void> deleteIdMono = this.repository.delete(DOMAIN_1);
        StepVerifier.create(deleteIdMono).expectError(CosmosAccessException.class).verify();
    }

    @Test
    public void testDeleteAll() {
        Flux<LongIdDomainPartition> savedAllFlux = this.repository.saveAll(Arrays.asList(DOMAIN_1, DOMAIN_2));
        StepVerifier.create(savedAllFlux).expectNextCount(2).verifyComplete();

        final Mono<Void> deletedMono = repository.deleteAll();
        StepVerifier.create(deletedMono).thenAwait().verifyComplete();

        Mono<Long> countMono = repository.count();
        StepVerifier.create(countMono).expectNext(0L).verifyComplete();
    }

    @Test
    public void testExistsById() {
        Mono<LongIdDomainPartition> saveMono = this.repository.save(DOMAIN_1);
        StepVerifier.create(saveMono).expectNext(DOMAIN_1).expectComplete().verify();

        Mono<Boolean> booleanMono = this.repository.existsById(DOMAIN_1.getNumber());
        StepVerifier.create(booleanMono).expectNext(true).expectComplete().verify();
    }

    @Test
    public void testFindAllSort() {
        final LongIdDomainPartition other = new LongIdDomainPartition(
            DOMAIN_1.getNumber() + 1, "other-name");
        Flux<LongIdDomainPartition> savedAllFlux = this.repository.saveAll(Arrays.asList(DOMAIN_1, other));
        StepVerifier.create(savedAllFlux).thenConsumeWhile(domain -> true).expectComplete().verify();

        final Sort ascSort = Sort.by(Sort.Direction.ASC, "number");
        Flux<LongIdDomainPartition> ascAllFlux = this.repository.findAll(ascSort);
        StepVerifier.create(ascAllFlux).expectNext(DOMAIN_1, other, DOMAIN_2).verifyComplete();

        final Sort descSort = Sort.by(Sort.Direction.DESC, "number");
        Flux<LongIdDomainPartition> descAllFlux = this.repository.findAll(descSort);
        StepVerifier.create(descAllFlux).expectNext(DOMAIN_2, other, DOMAIN_1).verifyComplete();
    }

    private static class InvalidDomain {

        private long count;

        private String location;

        InvalidDomain() {
        }

        InvalidDomain(long count, String location) {
            this.count = count;
            this.location = location;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            InvalidDomain that = (InvalidDomain) o;
            return count == that.count
                && Objects.equals(location, that.location);
        }

        @Override
        public int hashCode() {
            return Objects.hash(count, location);
        }

        @Override
        public String toString() {
            return "InvalidDomain{"
                + "count="
                + count
                + ", location='"
                + location
                + '\''
                + '}';
        }
    }
}
