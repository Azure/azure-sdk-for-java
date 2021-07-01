// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.integration;

import com.azure.spring.data.cosmos.IntegrationTestCollectionManager;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.core.query.CosmosPageRequest;
import com.azure.spring.data.cosmos.domain.IntegerIdDomain;
import com.azure.spring.data.cosmos.exception.CosmosAccessException;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.IntegerIdDomainRepository;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class IntegerIdDomainRepositoryIT {

    private static final Integer ID = 231234;
    private static final String NAME = "panli";
    private static final IntegerIdDomain DOMAIN = new IntegerIdDomain(ID, NAME);

    @ClassRule
    public static final IntegrationTestCollectionManager collectionManager = new IntegrationTestCollectionManager();

    @Autowired
    private CosmosTemplate template;

    @Autowired
    private IntegerIdDomainRepository repository;

    @Before
    public void setUp() {
        collectionManager.ensureContainersCreatedAndEmpty(template, IntegerIdDomain.class);
        this.repository.save(DOMAIN);
    }

    @Test
    public void testIntegerIdDomain() {
        this.repository.deleteAll();
        Assert.assertFalse(this.repository.findById(ID).isPresent());

        this.repository.save(DOMAIN);
        final Optional<IntegerIdDomain> foundOptional = this.repository.findById(ID);

        Assert.assertTrue(foundOptional.isPresent());
        Assert.assertEquals(DOMAIN.getNumber(), foundOptional.get().getNumber());
        Assert.assertEquals(DOMAIN.getName(), foundOptional.get().getName());

        this.repository.delete(DOMAIN);

        Assert.assertFalse(this.repository.findById(ID).isPresent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidDomain() {
        new CosmosEntityInformation<InvalidDomain, Integer>(InvalidDomain.class);
    }

    @Test
    public void testBasicQuery() {
        final IntegerIdDomain save = this.repository.save(DOMAIN);
        Assert.assertNotNull(save);
    }

    @Test
    public void testSaveAndFindById() {
        Assert.assertNotNull(this.repository.save(DOMAIN));

        final Optional<IntegerIdDomain> savedEntity = this.repository.findById(DOMAIN.getNumber());
        Assert.assertTrue(savedEntity.isPresent());
        Assert.assertEquals(DOMAIN, savedEntity.get());
    }

    @Test
    public void testSaveAllAndFindAll() {
        Assert.assertTrue(this.repository.findAll().iterator().hasNext());

        final Set<IntegerIdDomain> entitiesToSave = Collections.singleton(DOMAIN);
        this.repository.saveAll(entitiesToSave);

        final Set<IntegerIdDomain> savedEntities = StreamSupport.stream(this.repository.findAll().spliterator(), false)
                                                                .collect(Collectors.toSet());

        Assert.assertTrue(entitiesToSave.containsAll(savedEntities));
    }

    @Test
    public void testFindAllById() {
        final Iterable<IntegerIdDomain> allById =
            this.repository.findAllById(Collections.singleton(DOMAIN.getNumber()));
        Assert.assertTrue(allById.iterator().hasNext());
    }

    @Test
    public void testCount() {
        Assert.assertEquals(1, repository.count());
    }

    @Test
    public void testDeleteById() {
        this.repository.save(DOMAIN);
        this.repository.deleteById(DOMAIN.getNumber());
        Assert.assertEquals(0, this.repository.count());
    }

    @Test(expected = CosmosAccessException.class)
    public void testDeleteByIdShouldFailIfNothingToDelete() {
        this.repository.deleteAll();
        this.repository.deleteById(DOMAIN.getNumber());
    }

    @Test
    public void testDelete() {
        this.repository.save(DOMAIN);
        this.repository.delete(DOMAIN);
        Assert.assertEquals(0, this.repository.count());
    }

    @Test(expected = CosmosAccessException.class)
    public void testDeleteShouldFailIfNothingToDelete() {
        this.repository.deleteAll();
        this.repository.delete(DOMAIN);
    }

    @Test
    public void testDeleteAll() {
        this.repository.save(DOMAIN);
        this.repository.deleteAll(Collections.singleton(DOMAIN));
        Assert.assertEquals(0, this.repository.count());
    }

    @Test
    public void testExistsById() {
        this.repository.save(DOMAIN);
        Assert.assertTrue(this.repository.existsById(DOMAIN.getNumber()));
    }

    @Test
    public void testFindAllSort() {
        final IntegerIdDomain other = new IntegerIdDomain(DOMAIN.getNumber() + 1, "other-name");
        this.repository.save(other);
        this.repository.save(DOMAIN);

        final Sort ascSort = Sort.by(Sort.Direction.ASC, "number");
        final List<IntegerIdDomain> ascending = StreamSupport
            .stream(this.repository.findAll(ascSort).spliterator(), false)
            .collect(Collectors.toList());
        Assert.assertEquals(2, ascending.size());
        Assert.assertEquals(DOMAIN, ascending.get(0));
        Assert.assertEquals(other, ascending.get(1));

        final Sort descSort = Sort.by(Sort.Direction.DESC, "number");
        final List<IntegerIdDomain> descending = StreamSupport
            .stream(this.repository.findAll(descSort).spliterator(), false)
            .collect(Collectors.toList());
        Assert.assertEquals(2, descending.size());
        Assert.assertEquals(other, descending.get(0));
        Assert.assertEquals(DOMAIN, descending.get(1));

    }

    @Test
    public void testFindAllPageable() {
        final IntegerIdDomain other = new IntegerIdDomain(DOMAIN.getNumber() + 1, "other-name");
        this.repository.save(DOMAIN);
        this.repository.save(other);

        final Page<IntegerIdDomain> page1 = this.repository.findAll(new CosmosPageRequest(0, 1, null));
        final Iterator<IntegerIdDomain> page1Iterator = page1.iterator();
        Assert.assertTrue(page1Iterator.hasNext());
        Assert.assertEquals(DOMAIN, page1Iterator.next());

        final Page<IntegerIdDomain> page2 = this.repository.findAll(new CosmosPageRequest(1, 1, null));
        final Iterator<IntegerIdDomain> page2Iterator = page2.iterator();
        Assert.assertTrue(page2Iterator.hasNext());
        Assert.assertEquals(DOMAIN, page2Iterator.next());
    }

    private static class InvalidDomain {

        private int count;

        private String location;

        InvalidDomain() {
        }

        InvalidDomain(int count, String location) {
            this.count = count;
            this.location = location;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
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
