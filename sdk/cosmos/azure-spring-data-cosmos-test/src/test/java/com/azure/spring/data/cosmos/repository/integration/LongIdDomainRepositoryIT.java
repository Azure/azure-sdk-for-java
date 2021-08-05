// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.integration;

import com.azure.spring.data.cosmos.IntegrationTestCollectionManager;
import com.azure.spring.data.cosmos.common.TestUtils;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.core.query.CosmosPageRequest;
import com.azure.spring.data.cosmos.domain.LongIdDomain;
import com.azure.spring.data.cosmos.exception.CosmosAccessException;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.LongIdDomainRepository;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class LongIdDomainRepositoryIT {

    private static final Long ID_1 = 12345L;
    private static final String NAME_1 = "moary";

    private static final Long ID_2 = 67890L;
    private static final String NAME_2 = "camille";

    private static final LongIdDomain DOMAIN_1 = new LongIdDomain(ID_1, NAME_1);
    private static final LongIdDomain DOMAIN_2 = new LongIdDomain(ID_2, NAME_2);

    @ClassRule
    public static final IntegrationTestCollectionManager collectionManager = new IntegrationTestCollectionManager();

    @Autowired
    private CosmosTemplate template;

    @Autowired
    private LongIdDomainRepository repository;

    @Before
    public void setUp() {
        collectionManager.ensureContainersCreatedAndEmpty(template, LongIdDomain.class);
        this.repository.save(DOMAIN_1);
        this.repository.save(DOMAIN_2);
    }

    @Test
    public void testLongIdDomain() {
        this.repository.deleteAll();
        Assert.assertFalse(this.repository.findById(ID_1).isPresent());

        this.repository.save(DOMAIN_1);
        final Optional<LongIdDomain> foundOptional = this.repository.findById(ID_1);

        Assert.assertTrue(foundOptional.isPresent());
        Assert.assertEquals(DOMAIN_1.getNumber(), foundOptional.get().getNumber());
        Assert.assertEquals(DOMAIN_1.getName(), foundOptional.get().getName());

        this.repository.delete(DOMAIN_1);

        Assert.assertFalse(this.repository.findById(ID_1).isPresent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidDomain() {
        new CosmosEntityInformation<InvalidDomain, Long>(InvalidDomain.class);
    }

    @Test
    public void testBasicQuery() {
        final LongIdDomain save = this.repository.save(DOMAIN_1);
        Assert.assertNotNull(save);
    }

    @Test
    public void testSaveAndFindById() {
        Assert.assertNotNull(this.repository.save(DOMAIN_1));

        final Optional<LongIdDomain> savedEntity = this.repository.findById(DOMAIN_1.getNumber());
        Assert.assertTrue(savedEntity.isPresent());
        Assert.assertEquals(DOMAIN_1, savedEntity.get());
    }

    @Test
    public void testSaveAllAndFindAll() {
        Assert.assertTrue(this.repository.findAll().iterator().hasNext());

        final Set<LongIdDomain> entitiesToSave = Stream.of(DOMAIN_1, DOMAIN_2).collect(Collectors.toSet());
        this.repository.saveAll(entitiesToSave);

        final Set<LongIdDomain> savedEntities = StreamSupport.stream(this.repository.findAll().spliterator(), false)
                                                             .collect(Collectors.toSet());

        Assert.assertTrue(entitiesToSave.containsAll(savedEntities));
    }

    @Test
    public void testFindAllById() {
        final Iterable<LongIdDomain> allById =
            TestUtils.toList(this.repository.findAllById(Arrays.asList(DOMAIN_1.getNumber(), DOMAIN_2.getNumber())));
        Assert.assertTrue(((ArrayList) allById).size() == 2);
        Iterator<LongIdDomain> it = allById.iterator();
        assertLongIdDomainEquals(Arrays.asList(it.next(), it.next()), Arrays.asList(DOMAIN_1, DOMAIN_2));
    }

    private void assertLongIdDomainEquals(List<LongIdDomain> cur, List<LongIdDomain> reference) {
        cur.sort(Comparator.comparing(LongIdDomain::getNumber));
        reference.sort(Comparator.comparing(LongIdDomain::getNumber));
        Assert.assertEquals(reference, cur);
    }

    @Test
    public void testCount() {
        Assert.assertEquals(2, repository.count());
    }

    @Test
    public void testDeleteById() {
        this.repository.save(DOMAIN_1);
        this.repository.save(DOMAIN_2);
        this.repository.deleteById(DOMAIN_1.getNumber());
        this.repository.deleteById(DOMAIN_2.getNumber());
        Assert.assertEquals(0, this.repository.count());
    }

    @Test(expected = CosmosAccessException.class)
    public void testDeleteByIdShouldFailIfNothingToDelete() {
        this.repository.deleteAll();
        this.repository.deleteById(DOMAIN_1.getNumber());
    }

    @Test
    public void testDelete() {
        this.repository.save(DOMAIN_1);
        this.repository.delete(DOMAIN_1);
        Assert.assertEquals(1, this.repository.count());
    }

    @Test(expected = CosmosAccessException.class)
    public void testDeleteShouldFailIfNothingToDelete() {
        this.repository.deleteAll();
        this.repository.delete(DOMAIN_1);
    }

    @Test
    public void testDeleteAll() {
        this.repository.save(DOMAIN_1);
        this.repository.save(DOMAIN_2);
        this.repository.deleteAll(Arrays.asList(DOMAIN_1, DOMAIN_2));
        Assert.assertEquals(0, this.repository.count());
    }

    @Test
    public void testExistsById() {
        this.repository.save(DOMAIN_1);
        Assert.assertTrue(this.repository.existsById(DOMAIN_1.getNumber()));
    }

    @Test
    public void testFindAllSort() {
        final LongIdDomain other = new LongIdDomain(DOMAIN_1.getNumber() + 1, "other-name");
        this.repository.save(other);
        this.repository.save(DOMAIN_1);

        final Sort ascSort = Sort.by(Sort.Direction.ASC, "number");
        final List<LongIdDomain> ascending = StreamSupport
            .stream(this.repository.findAll(ascSort).spliterator(), false)
            .collect(Collectors.toList());
        Assert.assertEquals(3, ascending.size());
        Assert.assertEquals(DOMAIN_1, ascending.get(0));
        Assert.assertEquals(other, ascending.get(1));
        Assert.assertEquals(DOMAIN_2, ascending.get(2));

        final Sort descSort = Sort.by(Sort.Direction.DESC, "number");
        final List<LongIdDomain> descending = StreamSupport
            .stream(this.repository.findAll(descSort).spliterator(), false)
            .collect(Collectors.toList());
        Assert.assertEquals(3, descending.size());
        Assert.assertEquals(DOMAIN_2, descending.get(0));
        Assert.assertEquals(other, descending.get(1));
        Assert.assertEquals(DOMAIN_1, descending.get(2));

    }

    @Test
    public void testFindAllPageable() {
        final LongIdDomain other = new LongIdDomain(DOMAIN_1.getNumber() + 1, "other-name");
        this.repository.save(other);

        final Page<LongIdDomain> page1 = this.repository.findAll(new CosmosPageRequest(0, 1, null));
        final Iterator<LongIdDomain> page1Iterator = page1.iterator();
        Assert.assertTrue(page1Iterator.hasNext());
        Assert.assertEquals(DOMAIN_1, page1Iterator.next());

        final Page<LongIdDomain> page2 = this.repository.findAll(new CosmosPageRequest(1, 1, null));
        final Iterator<LongIdDomain> page2Iterator = page2.iterator();
        Assert.assertTrue(page2Iterator.hasNext());
        Assert.assertEquals(DOMAIN_1, page2Iterator.next());
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
