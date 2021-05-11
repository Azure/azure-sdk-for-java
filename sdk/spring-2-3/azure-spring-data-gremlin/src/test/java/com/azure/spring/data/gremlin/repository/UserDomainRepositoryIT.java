// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.repository;

import com.azure.spring.data.gremlin.common.TestRepositoryConfiguration;
import com.azure.spring.data.gremlin.common.domain.UserDomain;
import com.azure.spring.data.gremlin.common.repository.UserDomainRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
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

    @Before
    public void setup() {
        this.repository.deleteAll();
    }

    @Test
    public void testWithNoIdName() {
        Assert.assertEquals(0, this.repository.vertexCount());
        Assert.assertFalse(this.repository.findById(NAME_0).isPresent());

        this.repository.save(DOMAIN_0);
        final Optional<UserDomain> optional = this.repository.findById(NAME_0);

        Assert.assertTrue(optional.isPresent());
        Assert.assertEquals(DOMAIN_0.getName(), optional.get().getName());
        Assert.assertEquals(DOMAIN_0.getLevel(), optional.get().getLevel());

        this.repository.deleteById(NAME_0);
        Assert.assertFalse(this.repository.findById(NAME_0).isPresent());
    }

    @Test
    public void testFindByName() {
        this.repository.save(DOMAIN_0);

        final List<UserDomain> domains = this.repository.findByName(DOMAIN_0.getName());

        Assert.assertEquals(domains.size(), 1);
        Assert.assertEquals(domains.get(0), DOMAIN_0);

        this.repository.deleteAll();

        Assert.assertTrue(this.repository.findByName(DOMAIN_0.getName()).isEmpty());
    }

    @Test
    public void testFindByEnabledExists() {
        final List<UserDomain> domains = Arrays.asList(DOMAIN_0, DOMAIN_1);

        this.repository.saveAll(domains);
        this.repository.save(DOMAIN_2);

        final List<UserDomain> foundDomains = this.repository.findByEnabledExists();

        domains.sort(Comparator.comparing(UserDomain::getName));
        foundDomains.sort(Comparator.comparing(UserDomain::getName));

        Assert.assertEquals(foundDomains.size(), domains.size());
        Assert.assertEquals(foundDomains, domains);

        this.repository.deleteAll(domains);

        Assert.assertTrue(this.repository.findByEnabledExists().isEmpty());
    }

    @Test
    public void testFindByLevelBetween() {
        final List<UserDomain> domains = Arrays.asList(DOMAIN_0, DOMAIN_1);

        this.repository.saveAll(domains);

        List<UserDomain> foundDomains = this.repository.findByLevelBetween(8, 9);
        Assert.assertTrue(foundDomains.isEmpty());

        foundDomains = this.repository.findByLevelBetween(7, 8);
        Assert.assertEquals(foundDomains.size(), 1);
        Assert.assertEquals(foundDomains.get(0), DOMAIN_1);

        foundDomains = this.repository.findByLevelBetween(0, 8);
        domains.sort(Comparator.comparing(UserDomain::getName));
        foundDomains.sort(Comparator.comparing(UserDomain::getName));

        Assert.assertEquals(foundDomains.size(), domains.size());
        Assert.assertEquals(foundDomains, domains);

        this.repository.deleteAll(domains);

        Assert.assertTrue(this.repository.findByLevelBetween(0, 8).isEmpty());
    }
}
