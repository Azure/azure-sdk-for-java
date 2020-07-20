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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private static final Duration DEFAULT_TIME_OUT = Duration.ofSeconds(10);

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
        this.repository.save(DOMAIN_1).block(DEFAULT_TIME_OUT);
        this.repository.save(DOMAIN_2).block(DEFAULT_TIME_OUT);
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
        this.repository.deleteAll().block(DEFAULT_TIME_OUT);
        Assert.assertFalse(this.repository.findById(ID_1).blockOptional(DEFAULT_TIME_OUT).isPresent());

        this.repository.save(DOMAIN_1).block(DEFAULT_TIME_OUT);
        Optional<LongIdDomainPartition> foundOptional = this.repository.findById(ID_1).blockOptional(DEFAULT_TIME_OUT);

        Assert.assertTrue(foundOptional.isPresent());
        Assert.assertEquals(DOMAIN_1.getNumber(), foundOptional.get().getNumber());
        Assert.assertEquals(DOMAIN_1.getName(), foundOptional.get().getName());

        this.repository.delete(DOMAIN_1).block(DEFAULT_TIME_OUT);

        Assert.assertFalse(this.repository.findById(ID_1).blockOptional(DEFAULT_TIME_OUT).isPresent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidDomain() {
        new CosmosEntityInformation<InvalidDomain, Long>(InvalidDomain.class);
    }

    @Test
    public void testBasicQuery() {
        LongIdDomainPartition save = this.repository.save(DOMAIN_1).block(DEFAULT_TIME_OUT);
        Assert.assertNotNull(save);
    }

    @Test
    public void testSaveAndFindById() {
        Assert.assertNotNull(this.repository.save(DOMAIN_1).block(DEFAULT_TIME_OUT));
        Optional<LongIdDomainPartition> longIdDomainPartitionOptional = this.repository
            .findById(DOMAIN_1.getNumber()).blockOptional(DEFAULT_TIME_OUT);
        Assert.assertTrue(longIdDomainPartitionOptional.isPresent());
        Assert.assertEquals(DOMAIN_1, longIdDomainPartitionOptional.get());
    }

    @Test
    public void testSaveAllAndFindAll() {
        this.repository.deleteAll().block(DEFAULT_TIME_OUT);
        List<LongIdDomainPartition> savedEntities = Stream.of(DOMAIN_1, DOMAIN_2).collect(Collectors.toList());
        this.repository.saveAll(savedEntities).collectList().block(DEFAULT_TIME_OUT);
        List<LongIdDomainPartition> longIdDomainPartitionList = this.repository.findAll().collectList().block(DEFAULT_TIME_OUT);
        Assert.assertTrue(longIdDomainPartitionList.containsAll(savedEntities));
    }

    @Test
    public void testCount() {
        Assert.assertTrue(2 == repository.count().block(DEFAULT_TIME_OUT));
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

        final Mono<LongIdDomainPartition> byId = repository.findById(DOMAIN_1.getNumber(),
            new PartitionKey(entityInformation.getPartitionKeyFieldValue(DOMAIN_1)));
        Assert.assertNull(byId.block(DEFAULT_TIME_OUT));
    }

    @Test(expected = CosmosAccessException.class)
    public void testDeleteByIdShouldFailIfNothingToDelete() {
        this.repository.deleteAll().block(DEFAULT_TIME_OUT);
        this.repository.deleteById(DOMAIN_1.getNumber()).block(DEFAULT_TIME_OUT);
    }

    @Test
    public void testDelete() {
        this.repository.save(DOMAIN_1).block(DEFAULT_TIME_OUT);
        this.repository.delete(DOMAIN_1).block(DEFAULT_TIME_OUT);
        Assert.assertTrue(1 == this.repository.count().block(DEFAULT_TIME_OUT));
    }

    @Test(expected = CosmosAccessException.class)
    public void testDeleteShouldFailIfNothingToDelete() {
        this.repository.deleteAll().block(DEFAULT_TIME_OUT);
        this.repository.delete(DOMAIN_1).block(DEFAULT_TIME_OUT);
    }

    @Test
    public void testDeleteAll() {
        this.repository.save(DOMAIN_1).block(DEFAULT_TIME_OUT);
        this.repository.save(DOMAIN_2).block(DEFAULT_TIME_OUT);
        this.repository.deleteAll(Arrays.asList(DOMAIN_1, DOMAIN_2)).block(DEFAULT_TIME_OUT);
        Assert.assertTrue(0 == this.repository.count().block(DEFAULT_TIME_OUT));
    }

    @Test
    public void testExistsById() {
        this.repository.save(DOMAIN_1).block(DEFAULT_TIME_OUT);
        Assert.assertTrue(this.repository.existsById(DOMAIN_1.getNumber()).block(DEFAULT_TIME_OUT));
    }

    @Test
    public void testFindAllSort() {
        final LongIdDomainPartition other = new LongIdDomainPartition(DOMAIN_1.getNumber() + 1, "other-name");
        this.repository.save(other).block(DEFAULT_TIME_OUT);
        this.repository.save(DOMAIN_1).block(DEFAULT_TIME_OUT);

        final Sort ascSort = Sort.by(Sort.Direction.ASC, "number");
        final List<LongIdDomainPartition> ascending = this.repository.findAll(ascSort)
            .collectList().block(DEFAULT_TIME_OUT);
        Assert.assertEquals(3, ascending.size());
        Assert.assertEquals(DOMAIN_1, ascending.get(0));
        Assert.assertEquals(other, ascending.get(1));
        Assert.assertEquals(DOMAIN_2, ascending.get(2));

        final Sort descSort = Sort.by(Sort.Direction.DESC, "number");
        final List<LongIdDomainPartition> descending = this.repository.findAll(descSort)
            .collectList().block(DEFAULT_TIME_OUT);
        Assert.assertEquals(3, descending.size());
        Assert.assertEquals(DOMAIN_2, descending.get(0));
        Assert.assertEquals(other, descending.get(1));
        Assert.assertEquals(DOMAIN_1, descending.get(2));

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
