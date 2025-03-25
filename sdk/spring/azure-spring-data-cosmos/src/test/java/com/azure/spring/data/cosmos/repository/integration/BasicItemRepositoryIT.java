// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.integration;

import com.azure.spring.data.cosmos.IntegrationTestCollectionManager;
import com.azure.spring.data.cosmos.common.ResponseDiagnosticsTestUtils;
import com.azure.spring.data.cosmos.common.TestUtils;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.domain.Address;
import com.azure.spring.data.cosmos.domain.BasicItem;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.BasicItemRepository;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import static com.azure.spring.data.cosmos.common.TestConstants.ID_1;
import static com.azure.spring.data.cosmos.common.TestConstants.ID_2;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class BasicItemRepositoryIT {

    @ClassRule
    public static final IntegrationTestCollectionManager collectionManager = new IntegrationTestCollectionManager();

    @Autowired
    BasicItemRepository repository;

    @Autowired
    CosmosConfig cosmosConfig;

    @Autowired
    private CosmosTemplate template;

    @Autowired
    private ResponseDiagnosticsTestUtils responseDiagnosticsTestUtils;

    private static final BasicItem BASIC_ITEM_1 = new BasicItem(ID_1);

    private static final BasicItem BASIC_ITEM_2 = new BasicItem(ID_2);

    @Before
    public void setUp() {
        collectionManager.ensureContainersCreatedAndEmpty(template, Address.class);
        repository.saveAll(Lists.newArrayList(BASIC_ITEM_1, BASIC_ITEM_2));
    }

    @Test
    public void testFindAllById() {
        final Iterable<BasicItem> allById =
            TestUtils.toList(this.repository.findAllById(Arrays.asList(BASIC_ITEM_1.getId(), BASIC_ITEM_2.getId())));
        Assert.assertTrue(((ArrayList) allById).size() == 2);
        Iterator<BasicItem> it = allById.iterator();
        Assert.assertEquals(BASIC_ITEM_1, it.next());
        Assert.assertEquals(BASIC_ITEM_2, it.next());
    }
}
