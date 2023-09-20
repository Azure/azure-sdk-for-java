// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository;

import com.azure.cosmos.models.PartitionKey;
import com.azure.spring.data.cosmos.ReactiveIntegrationTestCollectionManager;
import com.azure.spring.data.cosmos.core.ReactiveCosmosTemplate;
import com.azure.spring.data.cosmos.domain.UUIDIdDomain;
import com.azure.spring.data.cosmos.exception.CosmosAccessException;
import com.azure.spring.data.cosmos.repository.repository.ReactiveUUIDIdDomainRepository;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
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

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class ReactiveUUIDIdDomainRepositoryIT {

    private static final UUID ID_1 = UUID.randomUUID();
    private static final String NAME_1 = "moary";

    private static final UUID ID_2 = UUID.randomUUID();
    private static final String NAME_2 = "camille";

    private static final UUIDIdDomain DOMAIN_1 = new UUIDIdDomain(ID_1, NAME_1);
    private static final UUIDIdDomain DOMAIN_2 = new UUIDIdDomain(ID_2, NAME_2);

    @ClassRule
    public static final ReactiveIntegrationTestCollectionManager collectionManager = new ReactiveIntegrationTestCollectionManager();

    @Autowired
    private ReactiveCosmosTemplate template;

    @Autowired
    private ReactiveUUIDIdDomainRepository repository;

    private CosmosEntityInformation<UUIDIdDomain, ?> entityInformation;

    @Before
    public void setUp() {
        collectionManager.ensureContainersCreatedAndEmpty(template, UUIDIdDomain.class);
        entityInformation = collectionManager.getEntityInformation(UUIDIdDomain.class);
        Flux<UUIDIdDomain> savedAllFlux = this.repository.saveAll(Arrays.asList(DOMAIN_1, DOMAIN_2));
        StepVerifier.create(savedAllFlux).thenConsumeWhile(domain -> true).expectComplete().verify();
    }

    @Test
    public void testUUIDIdDomain() {
        Mono<Void> deletedMono = this.repository.deleteAll();
        StepVerifier.create(deletedMono).thenAwait().verifyComplete();

        Mono<UUIDIdDomain> idMono = this.repository.findById(ID_1);
        StepVerifier.create(idMono).expectNextCount(0).verifyComplete();

        Mono<UUIDIdDomain> saveMono = this.repository.save(DOMAIN_1);
        StepVerifier.create(saveMono).expectNext(DOMAIN_1).expectComplete().verify();

        Mono<UUIDIdDomain> findIdMono = this.repository.findById(ID_1);
        StepVerifier.create(findIdMono).expectNext(DOMAIN_1).expectComplete().verify();

        Mono<Void> deleteMono = this.repository.delete(DOMAIN_1);
        StepVerifier.create(deleteMono).verifyComplete();

        Mono<UUIDIdDomain> afterDelIdMono = this.repository.findById(ID_1);
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

        Flux<UUIDIdDomain> savedAllFlux = this.repository.saveAll(Arrays.asList(DOMAIN_1, DOMAIN_2));
        StepVerifier.create(savedAllFlux).expectNextCount(2).verifyComplete();

        final Flux<UUIDIdDomain> allFlux = repository.findAll();
        StepVerifier.create(allFlux).expectNextCount(2).verifyComplete();
    }

    @Test
    public void testCount() {
        Mono<Long> countMono = repository.count();
        StepVerifier.create(countMono).expectNext(2L).verifyComplete();
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
        Mono<UUIDIdDomain> saveMono = this.repository.save(DOMAIN_1);
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
        Flux<UUIDIdDomain> savedAllFlux = this.repository.saveAll(Arrays.asList(DOMAIN_1, DOMAIN_2));
        StepVerifier.create(savedAllFlux).expectNextCount(2).verifyComplete();

        final Mono<Void> deletedMono = repository.deleteAll();
        StepVerifier.create(deletedMono).thenAwait().verifyComplete();

        Mono<Long> countMono = repository.count();
        StepVerifier.create(countMono).expectNext(0L).verifyComplete();
    }

    @Test
    public void testExistsById() {
        Mono<UUIDIdDomain> saveMono = this.repository.save(DOMAIN_1);
        StepVerifier.create(saveMono).expectNext(DOMAIN_1).expectComplete().verify();

        Mono<Boolean> booleanMono = this.repository.existsById(DOMAIN_1.getNumber());
        StepVerifier.create(booleanMono).expectNext(true).expectComplete().verify();
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
