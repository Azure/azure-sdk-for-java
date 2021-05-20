// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.repository;

import com.azure.spring.data.gremlin.common.TestRepositoryConfiguration;
import com.azure.spring.data.gremlin.common.domain.UserDomain;
import com.azure.spring.data.gremlin.common.repository.UserDomainRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestRepositoryConfiguration.class)
public class UserDomainRepositoryIT {

    private static final String NAME_0 = "incarnation";
    private static final String NAME_1 = "absolute";
    private static final String NAME_2 = "test_name";
    private static final int LEVEL_0 = 4;
    private static final int LEVEL_1 = 7;
    private static final int LEVEL_2 = 7;
    private static final UserDomain DOMAIN_0 = new UserDomain(NAME_0, LEVEL_0, true);
    private static final UserDomain DOMAIN_1 = new UserDomain(NAME_1, LEVEL_1, true);
    private static final UserDomain DOMAIN_2 = new UserDomain(NAME_2, LEVEL_2, false);

    @Autowired
    private UserDomainRepository repository;

    @BeforeEach
    public void setup() {
        this.repository.deleteAll();
    }

    @Test
    public void testWithNoIdName() {
        Assertions.assertEquals(0, this.repository.vertexCount());
        Assertions.assertFalse(this.repository.findById(NAME_0).isPresent());

        this.repository.save(DOMAIN_0);
        final Optional<UserDomain> optional = this.repository.findById(NAME_0);

        Assertions.assertTrue(optional.isPresent());
        Assertions.assertEquals(DOMAIN_0.getName(), optional.get().getName());
        Assertions.assertEquals(DOMAIN_0.getLevel(), optional.get().getLevel());

        this.repository.deleteById(NAME_0);
        Assertions.assertFalse(this.repository.findById(NAME_0).isPresent());
    }

    @Test
    public void testFindByName() {
        this.repository.save(DOMAIN_0);

        final List<UserDomain> domains = this.repository.findByName(DOMAIN_0.getName());

        Assertions.assertEquals(domains.size(), 1);
        Assertions.assertEquals(domains.get(0), DOMAIN_0);

        this.repository.deleteAll();

        Assertions.assertTrue(this.repository.findByName(DOMAIN_0.getName()).isEmpty());
    }

    @Test
    public void testFindByEnabledExists() {
        final List<UserDomain> domains = Arrays.asList(DOMAIN_0, DOMAIN_1);

        this.repository.saveAll(domains);
        this.repository.save(DOMAIN_2);

        final List<UserDomain> foundDomains = this.repository.findByEnabledExists();

        domains.sort(Comparator.comparing(UserDomain::getName));
        foundDomains.sort(Comparator.comparing(UserDomain::getName));

        Assertions.assertEquals(foundDomains.size(), domains.size());
        Assertions.assertEquals(foundDomains, domains);

        this.repository.deleteAll(domains);

        Assertions.assertTrue(this.repository.findByEnabledExists().isEmpty());
    }

    @Test
    public void testFindByLevelBetween() {
        final List<UserDomain> domains = Arrays.asList(DOMAIN_0, DOMAIN_1);

        this.repository.saveAll(domains);

        List<UserDomain> foundDomains = this.repository.findByLevelBetween(8, 9);
        Assertions.assertTrue(foundDomains.isEmpty());

        foundDomains = this.repository.findByLevelBetween(7, 8);
        Assertions.assertEquals(foundDomains.size(), 1);
        Assertions.assertEquals(foundDomains.get(0), DOMAIN_1);

        foundDomains = this.repository.findByLevelBetween(0, 8);
        domains.sort(Comparator.comparing(UserDomain::getName));
        foundDomains.sort(Comparator.comparing(UserDomain::getName));

        Assertions.assertEquals(foundDomains.size(), domains.size());
        Assertions.assertEquals(foundDomains, domains);

        this.repository.deleteAll(domains);

        Assertions.assertTrue(this.repository.findByLevelBetween(0, 8).isEmpty());
    }
}
