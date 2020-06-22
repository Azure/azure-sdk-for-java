// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.data.cosmosdb.core;

import com.azure.data.cosmos.PartitionKey;
import com.microsoft.azure.spring.data.cosmosdb.core.query.Criteria;
import com.microsoft.azure.spring.data.cosmosdb.core.query.DocumentQuery;
import com.microsoft.azure.spring.data.cosmosdb.domain.Person;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.util.Assert;

import static com.microsoft.azure.spring.data.cosmosdb.core.query.CriteriaType.IS_EQUAL;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

@SuppressWarnings("unchecked")
@RunWith(MockitoJUnitRunner.class)
public class CosmosTemplateIllegalTest {
    private static final String NULL_STR = null;
    private static final String DUMMY_COLL = "dummy";
    private static final String DUMMY_ID = "ID_1";
    private static final PartitionKey DUMMY_KEY = new PartitionKey("dummy");
    private static final String EMPTY_STR = StringUtils.EMPTY;
    private static final String WHITESPACES_STR = "  ";
    private static final String CHECK_FAILURE_MSG = "Illegal argument is not checked";

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private CosmosTemplate dbTemplate;
    private Class<?> dbTemplateClass;

    @Before
    public void setUp() {
        dbTemplateClass = dbTemplate.getClass();
    }

    @Test
    public void deleteIllegalShouldFail() throws NoSuchMethodException {
        final Method method = dbTemplateClass.getMethod("delete", DocumentQuery.class, Class.class, String.class);
        final Criteria criteria = Criteria.getInstance(IS_EQUAL, "faker", Arrays.asList("faker-value"));
        final DocumentQuery query = new DocumentQuery(criteria);

        checkIllegalArgument(method, null, Person.class, DUMMY_COLL);
        checkIllegalArgument(method, query, null, DUMMY_COLL);
        checkIllegalArgument(method, query, Person.class, null);
    }

    @Test
    public void deleteIllegalContainerShouldFail() throws NoSuchMethodException {
        final Method method = dbTemplateClass.getDeclaredMethod("deleteAll", String.class, Class.class);

        checkIllegalArgument(method, NULL_STR, Person.class);
        checkIllegalArgument(method, EMPTY_STR, Person.class);
        checkIllegalArgument(method, WHITESPACES_STR, Person.class);
    }

    @Test
    public void deleteByIdIllegalArgsShouldFail() throws NoSuchMethodException {
        final Method method = dbTemplateClass.getDeclaredMethod("deleteById", String.class, Object.class,
                PartitionKey.class);

        // Test argument containerName
        checkIllegalArgument(method, null, DUMMY_ID, DUMMY_KEY);
        checkIllegalArgument(method, EMPTY_STR, DUMMY_ID, DUMMY_KEY);
        checkIllegalArgument(method, WHITESPACES_STR, DUMMY_ID, DUMMY_KEY);

        // Test argument id
        checkIllegalArgument(method, DUMMY_COLL, null, DUMMY_KEY);
        checkIllegalArgument(method, DUMMY_COLL, EMPTY_STR, DUMMY_KEY);
        checkIllegalArgument(method, DUMMY_COLL, WHITESPACES_STR, DUMMY_KEY);
    }

    @Test
    public void findByIdIllegalArgsShouldFail() throws NoSuchMethodException {
        final Method method = dbTemplateClass.getDeclaredMethod("findById", Object.class, Class.class);

        checkIllegalArgument(method, DUMMY_ID, null);
    }

    @Test
    public void findByCollIdIllegalArgsShouldFail() throws NoSuchMethodException {
        final Method method = dbTemplateClass.getDeclaredMethod("findById", String.class,
                Object.class, Class.class);

        checkIllegalArgument(method, DUMMY_COLL, null, Person.class);
        checkIllegalArgument(method, DUMMY_COLL, EMPTY_STR, Person.class);
        checkIllegalArgument(method, DUMMY_COLL, WHITESPACES_STR, Person.class);
    }

    /**
     * Check IllegalArgumentException is thrown for illegal parameters
     * @param method
     * @param args Method invocation parameters
     */
    private void checkIllegalArgument(Method method, Object... args) {
        try {
            method.invoke(dbTemplate, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            Assert.isTrue(e.getCause() instanceof IllegalArgumentException, CHECK_FAILURE_MSG);
            return; // Test passed
        }

        throw new IllegalStateException(CHECK_FAILURE_MSG, null);
    }
}
