// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.integration;

import com.azure.spring.data.cosmos.IntegrationTestCollectionManager;
import com.azure.spring.data.cosmos.common.TestUtils;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.domain.UUIDIdDomain;
import com.azure.spring.data.cosmos.exception.CosmosNotFoundException;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.UUIDIdDomainRepository;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class UUIDIdDomainRepositoryIT {

    private static final UUID ID_1 = UUID.randomUUID();
    private static final String NAME_1 = "moary";

    private static final UUID ID_2 = UUID.randomUUID();
    private static final String NAME_2 = "camille";

    private static final UUIDIdDomain DOMAIN_1 = new UUIDIdDomain(ID_1, NAME_1);
    private static final UUIDIdDomain DOMAIN_2 = new UUIDIdDomain(ID_2, NAME_2);


    public static final IntegrationTestCollectionManager collectionManager = new IntegrationTestCollectionManager();

    @Autowired
    private CosmosTemplate template;

    @Autowired
    private UUIDIdDomainRepository repository;

    @BeforeEach
    public void setUp() {
        collectionManager.ensureContainersCreatedAndEmpty(template, UUIDIdDomain.class);
        this.repository.save(DOMAIN_1);
        this.repository.save(DOMAIN_2);
    }

    @Test
    public void testUUIDIdDomain() {
        this.repository.deleteAll();
        Assertions.assertFalse(this.repository.findById(ID_1).isPresent());

        this.repository.save(DOMAIN_1);
        final Optional<UUIDIdDomain> foundOptional = this.repository.findById(ID_1);

        Assertions.assertTrue(foundOptional.isPresent());
        assertEquals(DOMAIN_1.getNumber(), foundOptional.get().getNumber());
        assertEquals(DOMAIN_1.getName(), foundOptional.get().getName());

        this.repository.delete(DOMAIN_1);

        Assertions.assertFalse(this.repository.findById(ID_1).isPresent());
    }

    @Test
    public void testInvalidDomain() {
        assertThrows(IllegalArgumentException.class, () ->
            new CosmosEntityInformation<InvalidDomain, UUID>(InvalidDomain.class));

    }

    @Test
    public void testBasicQuery() {
        final UUIDIdDomain save = this.repository.save(DOMAIN_1);
        Assertions.assertNotNull(save);
    }

    @Test
    public void testSaveAndFindById() {
        Assertions.assertNotNull(this.repository.save(DOMAIN_1));

        final Optional<UUIDIdDomain> savedEntity = this.repository.findById(DOMAIN_1.getNumber());
        Assertions.assertTrue(savedEntity.isPresent());
        assertEquals(DOMAIN_1, savedEntity.get());
    }

    @Test
    public void testSaveAllAndFindAll() {
        Assertions.assertTrue(this.repository.findAll().iterator().hasNext());

        final Set<UUIDIdDomain> entitiesToSave = Stream.of(DOMAIN_1, DOMAIN_2).collect(Collectors.toSet());
        this.repository.saveAll(entitiesToSave);

        final Set<UUIDIdDomain> savedEntities = StreamSupport.stream(this.repository.findAll().spliterator(), false)
                                                             .collect(Collectors.toSet());

        Assertions.assertTrue(entitiesToSave.containsAll(savedEntities));
    }

    @Test
    public void testFindAllById() {
        final Iterable<UUIDIdDomain> allById =
            TestUtils.toList(this.repository.findAllById(Arrays.asList(DOMAIN_1.getNumber(), DOMAIN_2.getNumber())));
        Assertions.assertTrue(((ArrayList) allById).size() == 2);
        Iterator<UUIDIdDomain> it = allById.iterator();
        assertUUIDIdDomainEquals(Arrays.asList(it.next(), it.next()), Arrays.asList(DOMAIN_1, DOMAIN_2));
    }

    private void assertUUIDIdDomainEquals(List<UUIDIdDomain> cur, List<UUIDIdDomain> reference) {
        cur.sort(Comparator.comparing(UUIDIdDomain::getNumber));
        reference.sort(Comparator.comparing(UUIDIdDomain::getNumber));
        assertEquals(reference, cur);
    }

    @Test
    public void testCount() {
        assertEquals(2, repository.count());
    }

    @Test
    public void testDeleteById() {
        this.repository.save(DOMAIN_1);
        this.repository.save(DOMAIN_2);
        this.repository.deleteById(DOMAIN_1.getNumber());
        this.repository.deleteById(DOMAIN_2.getNumber());
        assertEquals(0, this.repository.count());
    }

    @Test
    public void testDeleteByIdShouldFailIfNothingToDelete() {
        assertThrows(CosmosNotFoundException.class, () -> {
            this.repository.deleteAll();
            this.repository.deleteById(DOMAIN_1.getNumber());
        });
    }

    @Test
    public void testDelete() {
        this.repository.save(DOMAIN_1);
        this.repository.delete(DOMAIN_1);
        assertEquals(1, this.repository.count());
    }

    @Test
    public void testDeleteShouldFailIfNothingToDelete() {
        assertThrows(CosmosNotFoundException.class, () -> {
            this.repository.deleteAll();
            this.repository.delete(DOMAIN_1);
        });
    }

    @Test
    public void testDeleteAll() {
        this.repository.save(DOMAIN_1);
        this.repository.save(DOMAIN_2);
        this.repository.deleteAll(Arrays.asList(DOMAIN_1, DOMAIN_2));
        assertEquals(0, this.repository.count());
    }

    @Test
    public void testExistsById() {
        this.repository.save(DOMAIN_1);
        Assertions.assertTrue(this.repository.existsById(DOMAIN_1.getNumber()));
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
