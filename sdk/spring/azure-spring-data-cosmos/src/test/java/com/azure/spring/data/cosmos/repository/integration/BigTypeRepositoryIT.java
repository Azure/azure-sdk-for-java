// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.integration;

import com.azure.spring.data.cosmos.IntegrationTestCollectionManager;
import com.azure.spring.data.cosmos.common.TestUtils;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.domain.BigType;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.BigTypeRepository;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class BigTypeRepositoryIT {


    public static final IntegrationTestCollectionManager collectionManager = new IntegrationTestCollectionManager();

    @Autowired
    BigTypeRepository repository;

    @Autowired
    private CosmosTemplate template;

    BigDecimal bigDecimal1 = new BigDecimal("12345678.12345678");
    BigInteger bigInteger1 = new BigInteger("12345678987654321");
    BigDecimal bigDecimal2 = new BigDecimal("87654321.12345678");
    BigInteger bigInteger2 = new BigInteger("98765432123456789");
    private BigType TEST_BIGTYPES_1 = new BigType("1", "BigType1",
        bigDecimal1, bigInteger1);

    private BigType TEST_BIGTYPES_2 = new BigType("2", "BigType2",
        bigDecimal2, bigInteger2);

    @BeforeEach
    public void setUp() {
        collectionManager.ensureContainersCreatedAndEmpty(template, BigType.class);
        repository.saveAll(Lists.newArrayList(TEST_BIGTYPES_1, TEST_BIGTYPES_2));
    }

    @Test
    public void testFindById() {
        final BigType result = repository.findById(TEST_BIGTYPES_1.getId()).get();
        assertThat(result.getBigDecimal()).isEqualTo(bigDecimal1);
        assertThat(result.getBigInteger()).isEqualTo(bigInteger1);
    }

    @Test
    public void testFindAll() {
        final List<BigType> result = TestUtils.toList(repository.findAll());

        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).getBigDecimal()).isEqualTo(bigDecimal1);
        assertThat(result.get(0).getBigInteger()).isEqualTo(bigInteger1);
        assertThat(result.get(1).getBigDecimal()).isEqualTo(bigDecimal2);
        assertThat(result.get(1).getBigInteger()).isEqualTo(bigInteger2);
    }
}
