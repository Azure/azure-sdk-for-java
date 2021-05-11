// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.repository;

import com.azure.spring.data.gremlin.common.TestRepositoryConfiguration;
import com.azure.spring.data.gremlin.common.domain.AdvancedUser;
import com.azure.spring.data.gremlin.common.repository.AdvancedUserRepository;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfiguration.class)
public class AdvancedUserRepositoryIT {

    private static final String ID_0 = "id-8000";
    private static final String ID_1 = "id-8001";

    private static final String NAME_0 = "name-9000";
    private static final String NAME_1 = "name-9001";

    private static final int LEVEL_0 = 4;
    private static final int LEVEL_1 = 38;

    private static final AdvancedUser USER_0 = new AdvancedUser(ID_0, NAME_0, LEVEL_0);
    private static final AdvancedUser USER_1 = new AdvancedUser(ID_1, NAME_1, LEVEL_1);

    @Autowired
    private AdvancedUserRepository repository;

    @Before
    public void setup() {
        this.repository.deleteAll();
    }

    @Test
    public void testCrudRepository() {
        final List<AdvancedUser> users = Arrays.asList(USER_0, USER_1);
        this.repository.saveAll(users);

        final Optional<AdvancedUser> optional = this.repository.findById(USER_0.getId());

        Assert.assertTrue(optional.isPresent());
        Assert.assertEquals(USER_0.getId(), optional.get().getId());
        Assert.assertEquals(USER_0.getName(), optional.get().getName());
        Assert.assertEquals(USER_0.getLevel(), optional.get().getLevel());

        final List<AdvancedUser> foundUsers = Lists.newArrayList(this.repository.findAll(AdvancedUser.class));
        Assert.assertEquals(foundUsers.size(), users.size());

        this.repository.deleteById(USER_0.getId());

        Assert.assertFalse(this.repository.findById(USER_0.getId()).isPresent());
        Assert.assertTrue(this.repository.findById(USER_1.getId()).isPresent());

        this.repository.deleteAll();

        Assert.assertFalse(this.repository.findById(USER_1.getId()).isPresent());
    }
}
