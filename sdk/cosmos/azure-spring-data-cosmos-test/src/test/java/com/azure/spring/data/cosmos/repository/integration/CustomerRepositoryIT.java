// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.integration;

import com.azure.spring.data.cosmos.IntegrationTestCollectionManager;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.domain.Customer;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.CustomerRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class CustomerRepositoryIT {

    private static final String USER_NAME_0 = "username-0";
    private static final String USER_NAME_1 = "username-1";
    private static final String FAKE_USER_NAME = "username-fake";

    private static final Long USER_AGE_0 = 34L;
    private static final Long USER_AGE_1 = 45L;

    private static final String CUSTOMER_ID_0 = "id-0";
    private static final String CUSTOMER_ID_1 = "id-1";
    private static final String CUSTOMER_ID_2 = "id-2";

    private static final Long CUSTOMER_LEVEL_0 = 1L;
    private static final Long CUSTOMER_LEVEL_1 = 2L;

    private static final Customer.User USER_0 = new Customer.User(USER_NAME_0, USER_AGE_0);
    private static final Customer.User USER_1 = new Customer.User(USER_NAME_1, USER_AGE_1);
    private static final Customer.User USER_2 = new Customer.User(USER_NAME_0, USER_AGE_1);

    private static final Customer CUSTOMER_0 = new Customer(CUSTOMER_ID_0, CUSTOMER_LEVEL_0, USER_0);
    private static final Customer CUSTOMER_1 = new Customer(CUSTOMER_ID_1, CUSTOMER_LEVEL_1, USER_1);
    private static final Customer CUSTOMER_2 = new Customer(CUSTOMER_ID_2, CUSTOMER_LEVEL_1, USER_2);

    @ClassRule
    public static final IntegrationTestCollectionManager collectionManager = new IntegrationTestCollectionManager();

    @Autowired
    private CustomerRepository repository;

    @Autowired
    private CosmosTemplate template;

    @Before
    public void setUp() {
        collectionManager.ensureContainersCreatedAndEmpty(template, Customer.class);
        this.repository.saveAll(Arrays.asList(CUSTOMER_0, CUSTOMER_1, CUSTOMER_2));
    }

    private void assertCustomerListEquals(@NonNull List<Customer> customers, @NonNull List<Customer> reference) {
        Assert.assertEquals(reference.size(), customers.size());

        customers.sort(Comparator.comparing(Customer::getId));
        reference.sort(Comparator.comparing(Customer::getId));

        Assert.assertEquals(reference, customers);
    }

    @Test
    public void testFindByUserAndLevel() {
        final List<Customer> references = Arrays.asList(CUSTOMER_0, CUSTOMER_2);
        Iterable<Customer> customers = this.repository.findByUser_Name(USER_NAME_0);
        List<Customer> results = new ArrayList<>();
        customers.forEach(results::add);

        assertCustomerListEquals(references, results);

        customers = this.repository.findByUser_Name(FAKE_USER_NAME);

        Assert.assertFalse(customers.iterator().hasNext());
    }

}
