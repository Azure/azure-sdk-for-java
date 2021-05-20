// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.repository;

import com.azure.spring.data.gremlin.common.TestRepositoryConfiguration;
import com.azure.spring.data.gremlin.common.TestUtils;
import com.azure.spring.data.gremlin.common.domain.Orange;
import com.azure.spring.data.gremlin.common.repository.OrangeRepository;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestRepositoryConfiguration.class)
public class OrangeRepositoryIT {

    private static final String LOCATION_0 = "location-0";
    private static final String LOCATION_1 = "location-1";
    private static final String LOCATION_2 = "location-2";
    private static final String LOCATION_3 = "location-3";

    private static final Double PRINCE_0 = 1.79;
    private static final Double PRINCE_1 = 2.09;
    private static final Double PRINCE_2 = 3.29;
    private static final Double PRINCE_3 = 3.99;

    private static final Orange ORANGE_0 = new Orange(LOCATION_0, PRINCE_0);
    private static final Orange ORANGE_1 = new Orange(LOCATION_1, PRINCE_1);
    private static final Orange ORANGE_2 = new Orange(LOCATION_2, PRINCE_2);
    private static final Orange ORANGE_3 = new Orange(LOCATION_3, PRINCE_3);

    private static final List<Orange> ORANGES = Arrays.asList(ORANGE_0, ORANGE_1, ORANGE_2, ORANGE_3);

    @Autowired
    private OrangeRepository repository;

    @BeforeEach
    public void setup() {
        this.repository.deleteAll();
    }

    @AfterEach
    public void cleanup() {
        this.repository.deleteAll();
    }

    @Test
    public void testGeneratedIdFindById() {
        final Orange orange = this.repository.save(ORANGE_0);

        Assertions.assertNotNull(orange.getId());

        final Optional<Orange> optional = this.repository.findById(orange.getId());

        Assertions.assertTrue(optional.isPresent());
        Assertions.assertEquals(optional.get(), orange);
    }

    @Test
    public void testGeneratedIdFindAll() {
        final List<Orange> expect = Lists.newArrayList(this.repository.saveAll(ORANGES));
        final List<Orange> actual = Lists.newArrayList(this.repository.findAll());

        TestUtils.assertEntitiesEquals(expect, actual);
    }

    @Test
    public void testGeneratedIdDeleteById() {
        final Orange orange = this.repository.save(ORANGE_0);

        this.repository.deleteById(orange.getId());

        Assertions.assertFalse(this.repository.findById(orange.getId()).isPresent());
        Assertions.assertFalse(this.repository.existsById(orange.getId()));
    }
}
